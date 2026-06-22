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
    void atacaRivalMaisFracoMesmoComHpModerado() {
        // O benchmark de duelos mostrou que a agressividade vence: matar um rival
        // mais fraco (um golpe basta) é melhor do que fugir e encaixar golpes.
        MotorHeuristico motor = new MotorHeuristico("eu");
        Acao acao = motor.decidir(percecao(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":30},"
                        + "\"outros_robots\":[{\"id\":\"r1\",\"x\":5,\"y\":6,\"hp\":10}]}"));
        assertEquals(Acao.MOVER_SUL, acao, "deve atacar um rival mais fraco em vez de fugir");
    }

    @Test
    void percecaoNulaNaoRebenta() {
        MotorHeuristico motor = new MotorHeuristico("eu");
        Acao acao = motor.decidir(null);
        assertNotNull(acao, "decidir() nunca devolve null");
        assertEquals(Acao.MOVER_NORTE, acao, "ação segura determinista");
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
