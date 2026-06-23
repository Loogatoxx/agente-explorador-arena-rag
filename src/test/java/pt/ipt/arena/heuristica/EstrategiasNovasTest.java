package pt.ipt.arena.heuristica;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Percecao;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes das 3 estratégias novas: aspirador de curas (rouba mesmo full life),
 * esquiva tática (foge da zona densa) e campeiro dos cantos (esconde-se no canto).
 */
class EstrategiasNovasTest {

    private final Gson gson = new Gson();

    private Percecao p(String json) {
        return gson.fromJson(json, Percecao.class);
    }

    private MotorHeuristico motor(AvaliadorAcao... avaliadores) {
        return new MotorHeuristico("eu", new MemoriaHeuristica(), new FiltroColisoes(), List.of(avaliadores));
    }

    @Test
    void aspiradorRoubaCuraMesmoFullLife() {
        MotorHeuristico m = motor(new AvaliadorAspirador(), new AvaliadorExploracao());
        Acao a = m.decidir(p("{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":200},"
                + "\"cofres_no_mundo\":[{\"x\":5,\"y\":8}]}"));
        assertEquals(Acao.MOVER_SUL, a, "deve ir buscar o cofre mesmo com 200 HP (full life)");
    }

    @Test
    void aspiradorPrefereCofreARecursoMaisProximo() {
        MotorHeuristico m = motor(new AvaliadorAspirador(), new AvaliadorExploracao());
        Acao a = m.decidir(p("{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":200},"
                + "\"cofres_no_mundo\":[{\"x\":5,\"y\":8}],\"recursos_no_mundo\":[{\"x\":5,\"y\":4}]}"));
        assertEquals(Acao.MOVER_SUL, a, "prioriza o cofre (+100) ao recurso mais próximo (+20)");
    }

    @Test
    void esquivaFogeDaZonaDensa() {
        MotorHeuristico m = motor(new AvaliadorEsquiva(), new AvaliadorExploracao());
        Acao a = m.decidir(p("{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":150},"
                + "\"outros_robots\":[{\"id\":\"a\",\"x\":7,\"y\":6,\"hp\":100},"
                + "{\"id\":\"b\",\"x\":6,\"y\":7,\"hp\":100},{\"id\":\"c\",\"x\":7,\"y\":7,\"hp\":100}]}"));
        assertEquals(Acao.MOVER_NORTE, a, "foge do centro de massa dos rivais (a Sudeste) -> Norte");
        assertNotEquals(Acao.MOVER_SUL, a, "nunca para dentro da zona densa");
    }

    @Test
    void cantoDirigeSeAoCanto() {
        MotorHeuristico m = motor(new AvaliadorCanto(), new AvaliadorExploracao());
        Acao a = m.decidir(p("{\"game_started\":true,\"o_meu_estado\":{\"x\":4,\"y\":4,\"hp\":150},"
                + "\"objetos_fixos\":[{\"x\":0,\"y\":0},{\"x\":10,\"y\":0},{\"x\":0,\"y\":10},{\"x\":10,\"y\":10}],"
                + "\"outros_robots\":[{\"id\":\"z\",\"x\":8,\"y\":8,\"hp\":100}]}"));
        assertTrue(a == Acao.MOVER_NORTE || a == Acao.MOVER_OESTE,
                "dirige-se ao canto mais próximo (1,1): NORTE ou OESTE");
    }
}
