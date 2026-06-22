package pt.ipt.arena.rede;

import java.net.http.HttpClient;

/**
 * Cliente HTTP dedicado exclusivamente à comunicação REST com o servidor
 * central da Arena (OVH).
 *
 * Responsabilidades (SRP): registo do robô, telemetria (perceção), submissão
 * de ações (movimento), submissão de chaves (unlock) e descarga do manual
 * técnico para o pipeline RAG.
 *
 * Quem comunica com a rede NÃO desenha ecrãs nem processa texto de IA.
 */
public class ArenaClient {

    private static final String URL_BASE = "https://arena.pmonteiro.ovh";

    private final HttpClient http;
    private final String codigoSala;
    private final String idRobo;

    public ArenaClient(String codigoSala, String idRobo) {
        this.codigoSala = codigoSala;
        this.idRobo = idRobo;
        this.http = HttpClient.newHttpClient();
    }

    // TODO (etapa: rede) POST /register  -> inscrever o robo na sala.
    // TODO (etapa: rede) GET  /perceive  -> obter telemetria (radar) em JSON.
    // TODO (etapa: rede) POST /action    -> submeter MOVER_NORTE/SUL/ESTE/OESTE.
    // TODO (etapa: rede) POST /unlock     -> submeter a chave extraida pelo RAG.
    // TODO (etapa: rede) descarregar o manual tecnico (endpoint indicado no Swagger).

    public String getUrlBase() {
        return URL_BASE;
    }

    public String getCodigoSala() {
        return codigoSala;
    }

    public String getIdRobo() {
        return idRobo;
    }

    protected HttpClient getHttp() {
        return http;
    }
}
