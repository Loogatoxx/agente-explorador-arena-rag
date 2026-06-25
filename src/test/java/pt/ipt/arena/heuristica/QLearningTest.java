package pt.ipt.arena.heuristica;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testes das partes deterministas do Q-Learning (sem treino nem simulador). */
class QLearningTest {

    @Test
    void atualizacaoDeBellmanAproximaDaRecompensa() {
        // alfa=0.5, gama=0.9; s2 sem valor futuro -> Q(s,a) move-se 0.5*r.
        TabelaQ tabela = new TabelaQ(4, 0.5, 0.9);
        boolean[] todas = {true, true, true, true};
        tabela.atualizar("s", 1, 10.0, "s2", todas);
        assertEquals(5.0, tabela.valor("s", 1), 1e-9, "Q deve mover-se alfa*(r + gama*0 - 0) = 5");
        // Segundo update aproxima mais (5 + 0.5*(10-5) = 7.5).
        tabela.atualizar("s", 1, 10.0, "s2", todas);
        assertEquals(7.5, tabela.valor("s", 1), 1e-9, "convergencia para a recompensa");
    }

    @Test
    void melhorAcaoRespeitaMascaraDeValidas() {
        TabelaQ tabela = new TabelaQ(4, 0.5, 0.9);
        // Dá valor à ação 0 (a melhor em absoluto) mas marca-a como INVÁLIDA.
        tabela.atualizar("s", 0, 100.0, "s", new boolean[]{true, true, true, true});
        tabela.atualizar("s", 2, 10.0, "s", new boolean[]{true, true, true, true});
        boolean[] semAcao0 = {false, true, true, false};
        assertEquals(2, tabela.melhorAcao("s", semAcao0),
                "deve escolher a melhor AÇÃO VÁLIDA (2), ignorando a 0 que está bloqueada");
    }

    @Test
    void terminalAplicaSoARecompensaFinal() {
        TabelaQ tabela = new TabelaQ(4, 0.5, 0.9);
        tabela.atualizarTerminal("s", 3, -50.0);
        assertEquals(-25.0, tabela.valor("s", 3), 1e-9, "terminal: 0 + 0.5*(-50 - 0) = -25");
    }

    @Test
    void persistenciaPreservaOsValores() throws Exception {
        TabelaQ tabela = new TabelaQ(4, 0.5, 0.9);
        tabela.atualizar("0|1|2|0", 1, 8.0, "0|0|0|0", new boolean[]{true, true, true, true});
        java.nio.file.Path tmp = java.nio.file.Files.createTempFile("qtable", ".txt");
        tabela.guardar(tmp);
        TabelaQ recarregada = TabelaQ.carregar(tmp, 4);
        assertEquals(tabela.valor("0|1|2|0", 1), recarregada.valor("0|1|2|0", 1), 1e-9);
        assertTrue(recarregada.tamanho() >= 1);
        java.nio.file.Files.deleteIfExists(tmp);
    }
}
