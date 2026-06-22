package pt.ipt.arena.ia;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Cliente independente responsável pela comunicação com o motor de IA local
 * (Ollama, porta 11434).
 *
 * Responsabilidades (SRP): gerar embeddings vetoriais ({@code nomic-embed-text})
 * e gerar texto a partir de um prompt ChatML com o modelo
 * {@code qwen2.5-coder:0.5b-instruct-q4_K_M} (temperatura 0.0, determinista).
 *
 * Quem processa texto de IA NÃO lida com a locomoção na matriz nem com a rede
 * da Arena. A construção do prompt ChatML e a pesquisa semântica vivem fora
 * desta classe (no {@code PipelineRag} / {@code Vetores}).
 */
public class OllamaClient {

    private static final String URL_BASE = "http://localhost:11434";
    private static final String MODELO_EMBEDDINGS = "nomic-embed-text";
    private static final String MODELO_LLM = "qwen2.5-coder:0.5b-instruct-q4_K_M";

    /** A geração pode demorar mais do que um pedido de rede normal. */
    private static final Duration TIMEOUT_PEDIDO = Duration.ofSeconds(60);

    private final HttpClient http;

    public OllamaClient() {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Gera o embedding (vetor) de um texto com o {@code nomic-embed-text}.
     * Devolve {@code double[]} ou {@code null} em caso de falha.
     */
    public double[] gerarEmbedding(String texto) {
        JsonObject corpo = new JsonObject();
        corpo.addProperty("model", MODELO_EMBEDDINGS);
        corpo.addProperty("prompt", texto);

        JsonObject resposta = postJson("/api/embeddings", corpo, "gerarEmbedding");
        if (resposta == null || !resposta.has("embedding")) {
            return null;
        }
        JsonArray array = resposta.getAsJsonArray("embedding");
        double[] vetor = new double[array.size()];
        for (int i = 0; i < array.size(); i++) {
            vetor[i] = array.get(i).getAsDouble();
        }
        return vetor;
    }

    /**
     * Gera texto a partir de um prompt já em formato ChatML, com temperatura 0.0
     * (determinista). Usa {@code /api/generate} com {@code raw=true} para que o
     * ChatML construído em Java seja enviado tal e qual (sem o servidor reaplicar
     * o template). Devolve a resposta crua do modelo ou {@code null} em falha.
     */
    public String gerarTextoRaw(String promptChatML) {
        JsonObject opcoes = new JsonObject();
        opcoes.addProperty("temperature", 0.0);

        JsonObject corpo = new JsonObject();
        corpo.addProperty("model", MODELO_LLM);
        corpo.addProperty("prompt", promptChatML);
        corpo.addProperty("raw", true);
        corpo.addProperty("stream", false);
        corpo.add("options", opcoes);

        JsonObject resposta = postJson("/api/generate", corpo, "gerarTextoRaw");
        if (resposta == null || !resposta.has("response")) {
            return null;
        }
        return resposta.get("response").getAsString();
    }

    private JsonObject postJson(String caminho, JsonObject corpo, String operacao) {
        HttpRequest pedido = HttpRequest.newBuilder(URI.create(URL_BASE + caminho))
                .timeout(TIMEOUT_PEDIDO)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(corpo.toString(), StandardCharsets.UTF_8))
                .build();
        try {
            HttpResponse<String> resposta = http.send(pedido, BodyHandlers.ofString());
            return JsonParser.parseString(resposta.body()).getAsJsonObject();
        } catch (java.io.IOException e) {
            System.err.println("[OllamaClient] Falha de rede em '" + operacao + "': " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[OllamaClient] Pedido '" + operacao + "' interrompido.");
            return null;
        } catch (RuntimeException e) {
            System.err.println("[OllamaClient] Resposta inválida em '" + operacao + "': " + e.getMessage());
            return null;
        }
    }

    public String getModeloEmbeddings() {
        return MODELO_EMBEDDINGS;
    }

    public String getModeloLlm() {
        return MODELO_LLM;
    }
}
