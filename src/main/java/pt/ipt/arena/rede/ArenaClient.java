package pt.ipt.arena.rede;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Percecao;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Cliente HTTP dedicado exclusivamente à comunicação REST com o servidor
 * central da Arena (OVH).
 *
 * Responsabilidades (SRP): registo do robô, telemetria (perceção), submissão
 * de ações (movimento), submissão de chaves (unlock) e descarga do manual
 * técnico para o pipeline RAG.
 *
 * Quem comunica com a rede NÃO desenha ecrãs nem processa texto de IA.
 *
 * Endpoints reais (descobertos no OpenAPI/Swagger do servidor):
 *   POST /arena/{room_id}/register?robot_id=...            -> resposta {status, message}
 *   GET  /arena/{room_id}/perceive/{robot_id}              -> telemetria (JSON)
 *   POST /arena/action            (body {room_id, robot_id, action})
 *   POST /arena/{room_id}/unlock?robot_id=&code=&rag_chunk=&llm_raw=
 *   GET  /arena/{room_id}/download_manual
 */
public class ArenaClient {

    private static final String URL_BASE = "https://arena.pmonteiro.ovh";

    /** Timeout por pedido. Margem alta para tolerar o jitter da rede Wi-Fi. */
    private static final Duration TIMEOUT_PEDIDO = Duration.ofSeconds(20);

    /** Instância reutilizável do Gson para (de)serialização JSON. */
    private static final Gson GSON = new Gson();

    private final HttpClient http;
    private final String codigoSala;
    private final String idRobo;

    public ArenaClient(String codigoSala, String idRobo) {
        this.codigoSala = codigoSala;
        this.idRobo = idRobo;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * POST /arena/{room_id}/register?robot_id=...
     *
     * Inscreve o robô na sala. Devolve o JSON de resposta do servidor
     * (tipicamente {@code {"status": ..., "message": ...}}) ou {@code null}
     * se a chamada de rede falhar (nesse caso o ciclo deve voltar a tentar).
     */
    public JsonObject registar() {
        String url = URL_BASE + "/arena/" + segmento(codigoSala)
                + "/register?robot_id=" + parametro(idRobo);

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT_PEDIDO)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return enviarEParsear(pedido, "registar");
    }

    /**
     * GET /arena/{room_id}/perceive/{robot_id}
     *
     * Obtém a telemetria atual do radar em JSON (estado do robô, paredes,
     * recursos, cofres e robôs visíveis). Devolve o objeto JSON recebido ou
     * {@code null} em caso de falha de rede.
     */
    public JsonObject percecionar() {
        String url = URL_BASE + "/arena/" + segmento(codigoSala)
                + "/perceive/" + segmento(idRobo);

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT_PEDIDO)
                .header("Accept", "application/json")
                .GET()
                .build();

        return enviarEParsear(pedido, "percecionar");
    }

    /**
     * Igual a {@link #percecionar()} mas devolve a telemetria já mapeada para o
     * modelo tipado {@link Percecao} (via Gson). Devolve {@code null} se a
     * chamada falhar ou se o JSON não puder ser convertido.
     */
    public Percecao percecionarTipado() {
        JsonObject json = percecionar();
        if (json == null) {
            return null;
        }
        try {
            normalizarOutrosRobots(json);
            return GSON.fromJson(json, Percecao.class);
        } catch (RuntimeException e) {
            System.err.println("[ArenaClient] Não foi possível mapear a telemetria: " + e.getMessage());
            return null;
        }
    }

    /**
     * O servidor real envia {@code outros_robots} como um dicionário
     * {@code {id: {estado}}}, mas o nosso modelo (e os simuladores) usam uma
     * lista. Converte o dicionário em lista (injetando o id a partir da chave)
     * antes do mapeamento Gson. Se já for lista, não toca em nada.
     */
    private static void normalizarOutrosRobots(JsonObject json) {
        JsonElement outros = json.get("outros_robots");
        if (outros != null && outros.isJsonObject()) {
            JsonArray lista = new JsonArray();
            for (Map.Entry<String, JsonElement> entrada : outros.getAsJsonObject().entrySet()) {
                if (entrada.getValue().isJsonObject()) {
                    JsonObject rival = entrada.getValue().getAsJsonObject();
                    if (!rival.has("id")) {
                        rival.addProperty("id", entrada.getKey());
                    }
                    lista.add(rival);
                }
            }
            json.add("outros_robots", lista);
        }
    }

    /**
     * POST /arena/action  (body {@code {room_id, robot_id, action}}).
     *
     * Submete uma ação motora. Devolve a resposta do servidor (sucesso /
     * bloqueado / eliminado) ou {@code null} em caso de falha de rede.
     */
    public JsonObject agir(Acao acao) {
        return agir(acao.getComando());
    }

    public JsonObject agir(String comando) {
        JsonObject corpo = new JsonObject();
        corpo.addProperty("room_id", codigoSala);
        corpo.addProperty("robot_id", idRobo);
        corpo.addProperty("action", comando);

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(URL_BASE + "/arena/action"))
                .timeout(TIMEOUT_PEDIDO)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo.toString(), StandardCharsets.UTF_8))
                .build();

        return enviarEParsear(pedido, "agir");
    }

    /**
     * GET /arena/{room_id}/download_manual
     *
     * Descarrega o manual técnico (texto) que alimenta o pipeline RAG. Devolve o
     * corpo da resposta tal como recebido (texto a ser dividido em chunks), ou
     * {@code null} em caso de falha de rede.
     */
    public String descarregarManual() {
        String url = URL_BASE + "/arena/" + segmento(codigoSala) + "/download_manual";

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT_PEDIDO)
                .GET()
                .build();

        return enviarTexto(pedido, "descarregarManual");
    }

    /**
     * POST /arena/{room_id}/unlock?robot_id=&code=&rag_chunk=&llm_raw=
     *
     * Submete ao cofre a chave extraída pelo pipeline RAG. {@code ragChunk} (a
     * secção do manual usada) e {@code llmRaw} (a resposta crua do LLM) são
     * opcionais — enviá-los dá transparência ao servidor. Devolve o JSON de
     * resposta ({@code {"status": "sucesso"|"falha", ...}}) ou {@code null}.
     */
    public JsonObject desbloquear(String code, String ragChunk, String llmRaw) {
        String url = URL_BASE + "/arena/" + segmento(codigoSala) + "/unlock"
                + "?robot_id=" + parametro(idRobo)
                + "&code=" + parametro(code)
                + "&rag_chunk=" + parametro(ragChunk == null ? "Não enviado" : ragChunk)
                + "&llm_raw=" + parametro(llmRaw == null ? "Não enviado" : llmRaw);

        HttpRequest pedido = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT_PEDIDO)
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        return enviarDesbloqueio(pedido);
    }

    /**
     * Envia o pedido de unlock com tratamento ESPECIAL da resposta. O servidor
     * (FastAPI) devolve o corpo {@code null} (ou vazio) com HTTP 200 quando o
     * unlock TEM SUCESSO — confirmado ao vivo pela recompensa de +HP no cofre.
     * Os casos de erro/recusa devolvem um objeto JSON ({@code {"status": ...}}).
     * Por isso, um corpo nulo/vazio com 200 é normalizado para
     * {@code {"status":"sucesso"}}; uma falha de rede continua a devolver
     * {@code null} (inconclusivo).
     */
    private JsonObject enviarDesbloqueio(HttpRequest pedido) {
        try {
            HttpResponse<String> resposta = http.send(pedido, BodyHandlers.ofString());
            String corpo = resposta.body() == null ? "" : resposta.body().trim();
            if (resposta.statusCode() == 200 && (corpo.isEmpty() || "null".equalsIgnoreCase(corpo))) {
                JsonObject sucesso = new JsonObject();
                sucesso.addProperty("status", "sucesso");
                return sucesso;
            }
            JsonElement elemento = JsonParser.parseString(corpo);
            return elemento.isJsonObject() ? elemento.getAsJsonObject() : null;
        } catch (java.io.IOException e) {
            System.err.println("[ArenaClient] Falha de rede em 'desbloquear': " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[ArenaClient] Pedido 'desbloquear' interrompido.");
            return null;
        } catch (RuntimeException e) {
            System.err.println("[ArenaClient] Resposta inválida em 'desbloquear': " + e.getMessage());
            return null;
        }
    }

    /** Envia um pedido e devolve o corpo da resposta como texto cru (ex.: manual). */
    private String enviarTexto(HttpRequest pedido, String operacao) {
        try {
            HttpResponse<String> resposta = http.send(pedido, BodyHandlers.ofString());
            return resposta.body();
        } catch (java.io.IOException e) {
            System.err.println("[ArenaClient] Falha de rede em '" + operacao + "': " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[ArenaClient] Pedido '" + operacao + "' interrompido.");
            return null;
        }
    }

    /**
     * Envia o pedido e converte o corpo da resposta num {@link JsonObject}.
     *
     * Encapsula o tratamento robusto de falhas de rede (timeouts, micro-cortes
     * de Wi-Fi, respostas inválidas) para que o agente NUNCA rebente — um
     * requisito explícito de resiliência do enunciado. Em caso de erro devolve
     * {@code null} e regista a ocorrência, deixando o ciclo recuperar no turno
     * seguinte.
     */
    private JsonObject enviarEParsear(HttpRequest pedido, String operacao) {
        try {
            HttpResponse<String> resposta = http.send(pedido, BodyHandlers.ofString());
            return JsonParser.parseString(resposta.body()).getAsJsonObject();
        } catch (java.io.IOException e) {
            System.err.println("[ArenaClient] Falha de rede em '" + operacao + "': " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[ArenaClient] Pedido '" + operacao + "' interrompido.");
            return null;
        } catch (RuntimeException e) {
            System.err.println("[ArenaClient] Resposta inválida em '" + operacao + "': " + e.getMessage());
            return null;
        }
    }

    /** Codifica um valor para ser usado como parâmetro de query (?chave=valor). */
    private static String parametro(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    /**
     * Devolve um valor para usar como segmento de caminho (path). Os ids da
     * Arena são alfanuméricos simples; mantém-se o valor tal como está.
     */
    private static String segmento(String valor) {
        return valor;
    }

    public String getUrlBase() {
        return URL_BASE;
    }

    public String getCodigoSala() {
        return codigoSala;
    }

    public String getIdRobo() {
        return idRobo;
    }
}
