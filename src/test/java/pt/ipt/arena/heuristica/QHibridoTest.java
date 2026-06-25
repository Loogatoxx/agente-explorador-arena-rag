package pt.ipt.arena.heuristica;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Percecao;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testa o caminho completo do agente híbrido (estado de combate → postura →
 * execução delegada às regras BFS) sem treino: o motor com Q-híbrido decide
 * sempre uma ação válida e nunca rebenta.
 */
class QHibridoTest {

    private final Gson gson = new Gson();

    private Percecao p(String json) {
        return gson.fromJson(json, Percecao.class);
    }

    private MotorHeuristico motorHibrido() {
        TabelaQ tabela = new TabelaQ(AvaliadorQHibrido.N_POSTURAS);
        return MotorHeuristico.comQ(new AvaliadorQHibrido(tabela, 0.0, false, 1));
    }

    @Test
    void decideAcaoComRivalRematavelAdjacente() {
        Acao a = motorHibrido().decidir(p(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":200},"
                        + "\"outros_robots\":[{\"id\":\"r1\",\"x\":5,\"y\":6,\"hp\":10}]}"));
        assertNotNull(a, "o híbrido devolve sempre uma ação");
    }

    @Test
    void decideAcaoEmMeleeComCuraConhecida() {
        Acao a = motorHibrido().decidir(p(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":8,\"y\":8,\"hp\":40},"
                        + "\"recursos_no_mundo\":[{\"x\":8,\"y\":11}],"
                        + "\"outros_robots\":[{\"id\":\"a\",\"x\":8,\"y\":9,\"hp\":200},"
                        + "{\"id\":\"b\",\"x\":9,\"y\":8,\"hp\":180}]}"));
        assertNotNull(a, "em mêlée com pouca vida, decide na mesma uma ação");
    }

    @Test
    void naoRebentaSemPercecao() {
        assertNotNull(motorHibrido().decidir(null), "decidir(null) devolve ação segura");
    }
}
