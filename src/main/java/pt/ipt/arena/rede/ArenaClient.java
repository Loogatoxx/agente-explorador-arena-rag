package pt.ipt.arena.rede;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

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

    // TODO (etapa: acao)  POST /arena/action            -> submeter MOVER_NORTE/SUL/ESTE/OESTE.
    // TODO (etapa: RAG)    POST /arena/{room_id}/unlock  -> submeter a chave extraida.
    // TODO (etapa: RAG)    GET  /arena/{room_id}/download_manual -> obter o manual tecnico.

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
