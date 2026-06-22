package pt.ipt.arena.ia;

import java.net.http.HttpClient;

/**
 * Cliente independente responsável pela comunicação com o motor de IA local
 * (Ollama, porta 11434).
 *
 * Responsabilidades (SRP): geração de embeddings vetoriais
 * ({@code nomic-embed-text}) e fase de geração RAG via prompts ChatML com o
 * modelo {@code qwen2.5-coder:0.5b-instruct} (temperatura 0.0).
 *
 * Quem processa texto de IA NÃO lida com a locomoção na matriz nem com a rede
 * da Arena.
 */
public class OllamaClient {

    private static final String URL_BASE = "http://localhost:11434";
    private static final String MODELO_EMBEDDINGS = "nomic-embed-text";
    private static final String MODELO_LLM = "qwen2.5-coder:0.5b-instruct";

    private final HttpClient http;

    public OllamaClient() {
        this.http = HttpClient.newHttpClient();
    }

    // TODO (etapa: RAG) gerar embedding de um texto            -> double[].
    // TODO (etapa: RAG) semelhanca de cossenos entre 2 vetores -> double.
    // TODO (etapa: RAG) gerar a chave via prompt ChatML (system/user/assistant).

    public String getUrlBase() {
        return URL_BASE;
    }

    public String getModeloEmbeddings() {
        return MODELO_EMBEDDINGS;
    }

    public String getModeloLlm() {
        return MODELO_LLM;
    }

    protected HttpClient getHttp() {
        return http;
    }
}
