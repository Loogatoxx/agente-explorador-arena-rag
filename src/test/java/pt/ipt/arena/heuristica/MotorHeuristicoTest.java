package pt.ipt.arena.heuristica;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Percecao;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes comportamentais do {@link MotorHeuristico}, sem servidor nem Ollama.
 *
 * As perceções são construídas a partir de JSON (via Gson), o que valida também
 * o mapeamento {@code @SerializedName} dos modelos de telemetria.
 *
 * Convenção de eixos atual (ver {@link Bussola}): NORTE=-y, SUL=+y, ESTE=+x,
 * OESTE=-x. Se a orientação real for outra, os testes de direção devem ser
 * ajustados em conjunto com a constante da Bússola.
 */
class MotorHeuristicoTest {

    private final Gson gson = new Gson();

    private Percecao percecao(String json) {
        return gson.fromJson(json, Percecao.class);
    }

    @Test
    void evitaParedeNaColisao() {
        MotorHeuristico motor = new MotorHeuristico("eu");
        Acao acao = motor.decidir(percecao(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":100},"
                        + "\"objetos_fixos\":[{\"x\":6,\"y\":5}]}"));
        assertNotEquals(Acao.MOVER_ESTE, acao, "não deve andar contra a parede a Este");
        assertEquals(Acao.MOVER_NORTE, acao, "desempate canónico (NORTE, ESTE, SUL, OESTE)");
    }

    @Test
    void atraiPorRecurso() {
        MotorHeuristico motor = new MotorHeuristico("eu");
        Acao acao = motor.decidir(percecao(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":100},"
                        + "\"recursos_no_mundo\":[{\"x\":5,\"y\":8}]}"));
        assertEquals(Acao.MOVER_SUL, acao, "deve dirigir-se ao recurso (a Sul)");
    }

    @Test
    void exploraSemAndarEmCirculos() {
        MotorHeuristico motor = new MotorHeuristico("eu");
        int x = 5;
        int y = 5;
        Set<String> visitadas = new HashSet<>();
        visitadas.add(x + "," + y);
        for (int i = 0; i < 12; i++) {
            Acao acao = motor.decidir(percecao(
                    "{\"game_started\":true,\"o_meu_estado\":{\"x\":" + x + ",\"y\":" + y + ",\"hp\":100}}"));
            switch (acao) {
                case MOVER_NORTE: y -= 1; break;
                case MOVER_SUL:   y += 1; break;
                case MOVER_ESTE:  x += 1; break;
                case MOVER_OESTE: x -= 1; break;
                default: break;
            }
            visitadas.add(x + "," + y);
        }
        assertEquals(13, visitadas.size(), "em campo aberto não deve revisitar células (mapa de calor)");
    }

    @Test
    void fogeDeRivalMaisForte() {
        MotorHeuristico motor = new MotorHeuristico("eu");
        Acao acao = motor.decidir(percecao(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":50},"
                        + "\"outros_robots\":[{\"id\":\"r1\",\"x\":5,\"y\":6,\"hp\":200}]}"));
        assertNotEquals(Acao.MOVER_SUL, acao, "não deve investir contra um rival mais forte");
        assertEquals(Acao.MOVER_NORTE, acao, "foge maximizando a distância ao rival");
    }

    @Test
    void atacaRivalMaisFraco() {
        MotorHeuristico motor = new MotorHeuristico("eu");
        Acao acao = motor.decidir(percecao(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":200},"
                        + "\"outros_robots\":[{\"id\":\"r1\",\"x\":5,\"y\":6,\"hp\":30}]}"));
        assertEquals(Acao.MOVER_SUL, acao, "aproxima-se do rival mais fraco para o atacar");
    }

    @Test
    void rematarRivalQuaseMorto() {
        // Com o "remate" (default do otimizador), um rival adjacente que morre num
        // só golpe (<=15 HP) é abatido de graça — mesmo sem grande vantagem de HP.
        MotorHeuristico motor = new MotorHeuristico("eu");
        Acao acao = motor.decidir(percecao(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":30},"
                        + "\"outros_robots\":[{\"id\":\"r1\",\"x\":5,\"y\":6,\"hp\":10}]}"));
        assertEquals(Acao.MOVER_SUL, acao, "deve rematar (abater) o rival que morre a 1 golpe");
    }

    @Test
    void percecaoNulaNaoRebenta() {
        MotorHeuristico motor = new MotorHeuristico("eu");
        Acao acao = motor.decidir(null);
        assertNotNull(acao, "decidir() nunca devolve null");
        assertEquals(Acao.MOVER_NORTE, acao, "ação segura determinista");
    }

    @Test
    void radarMarcaQuadradoDeCelulasVistas() {
        MemoriaHeuristica m = new MemoriaHeuristica();
        m.marcarVistasEmRaio(new pt.ipt.arena.modelo.Coordenada(10, 10), 2);
        assertEquals(25, m.celulasVistas().size(), "raio 2 = quadrado 5x5 = 25 células");
        assertTrue(m.celulasVistas().contains("12,8"), "deve incluir o canto (dx=+2, dy=-2)");
        assertTrue(!m.celulasVistas().contains("13,10"), "dx=+3 está fora do raio 2");
    }

    @Test
    void cofreJaTentadoRefleteListaNegraEResolvidos() {
        MotorHeuristico motor = new MotorHeuristico("eu");
        pt.ipt.arena.modelo.Coordenada a = new pt.ipt.arena.modelo.Coordenada(3, 4);
        pt.ipt.arena.modelo.Coordenada b = new pt.ipt.arena.modelo.Coordenada(7, 1);
        pt.ipt.arena.modelo.Coordenada c = new pt.ipt.arena.modelo.Coordenada(9, 9);

        assertEquals(false, motor.cofreJaTentado(a), "cofre virgem ainda não foi tentado");
        motor.marcarCofreFalhado(a);
        motor.marcarCofreResolvido(b);
        assertEquals(true, motor.cofreJaTentado(a), "cofre falhado conta como já tentado");
        assertEquals(true, motor.cofreJaTentado(b), "cofre resolvido conta como já tentado");
        assertEquals(false, motor.cofreJaTentado(c), "cofre nunca visto continua por tentar");
    }

    @Test
    void cercadoDevolveAcaoSegura() {
        MotorHeuristico motor = new MotorHeuristico("eu");
        Acao acao = motor.decidir(percecao(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":100},"
                        + "\"objetos_fixos\":[{\"x\":6,\"y\":5},{\"x\":4,\"y\":5},{\"x\":5,\"y\":6},{\"x\":5,\"y\":4}]}"));
        assertEquals(Acao.MOVER_NORTE, acao, "cercado por 4 paredes: não rebenta, devolve ação segura");
    }
}
