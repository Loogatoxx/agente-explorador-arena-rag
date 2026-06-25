package pt.ipt.arena.modelo;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

/**
 * Modelo de dados (POJO) que mapeia o JSON de telemetria devolvido pelo
 * endpoint {@code GET /arena/{room_id}/perceive/{robot_id}}.
 *
 * Agrega tudo o que o radar "vê" num dado turno: estado do agente, paredes,
 * recursos, cofres e robôs rivais, mais os indicadores de início/fim de jogo.
 *
 * Os getters de listas são null-safe (devolvem lista vazia quando o campo vem
 * a {@code null}), para o ciclo de decisão não rebentar com NullPointerException.
 *
 * Nota: nomes de campos de topo retirados do enunciado (secção 5.1); a estrutura
 * interna de cada elemento deve ser confirmada com uma sala ativa.
 */
public class Percecao {

    @SerializedName("game_started")
    private boolean gameStarted;

    @SerializedName("game_over")
    private boolean gameOver;

    @SerializedName("o_meu_estado")
    private EstadoRobo oMeuEstado;

    @SerializedName("objetos_fixos")
    private List<Coordenada> objetosFixos;

    @SerializedName("recursos_no_mundo")
    private List<Coordenada> recursosNoMundo;

    @SerializedName("cofres_no_mundo")
    private List<Cofre> cofresNoMundo;

    // No servidor real, outros_robots vem como DICIONÁRIO {id:{estado}}; o
    // ArenaClient normaliza-o para lista antes do Gson (ver normalizarOutrosRobots).
    @SerializedName("outros_robots")
    private List<RoboRival> outrosRobots;

    public boolean isGameStarted() {
        return gameStarted;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public EstadoRobo getOMeuEstado() {
        return oMeuEstado;
    }

    public List<Coordenada> getObjetosFixos() {
        return objetosFixos != null ? objetosFixos : Collections.emptyList();
    }

    public List<Coordenada> getRecursosNoMundo() {
        return recursosNoMundo != null ? recursosNoMundo : Collections.emptyList();
    }

    public List<Cofre> getCofresNoMundo() {
        return cofresNoMundo != null ? cofresNoMundo : Collections.emptyList();
    }

    public List<RoboRival> getOutrosRobots() {
        return outrosRobots != null ? outrosRobots : Collections.emptyList();
    }

    /**
     * Devolve o cofre sobre o qual o agente está posicionado (com enigma ativo),
     * ou {@code null} se não houver nenhum. Gatilho do pipeline RAG.
     */
    public Cofre getCofreSobOAgente() {
        for (Cofre cofre : getCofresNoMundo()) {
            if (cofre.temDesafioAtivo()) {
                return cofre;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Percecao{started=" + gameStarted + ", over=" + gameOver
                + ", estado=" + oMeuEstado
                + ", paredes=" + getObjetosFixos().size()
                + ", recursos=" + getRecursosNoMundo().size()
                + ", cofres=" + getCofresNoMundo().size()
                + ", rivais=" + getOutrosRobots().size() + "}";
    }
}
