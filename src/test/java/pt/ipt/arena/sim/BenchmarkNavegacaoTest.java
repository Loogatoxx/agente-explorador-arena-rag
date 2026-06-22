package pt.ipt.arena.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica, com o simulador, que a configuração OTIMIZADA do motor (mapa
 * acumulado + pathfinding BFS + exploração por fronteiras) supera claramente a
 * configuração clássica (gulosa) na navegação. Trava regressões de desempenho.
 */
class BenchmarkNavegacaoTest {

    @Test
    void otimizadoSuperaClassicoNaNavegacao() {
        BenchmarkNavegacao.Sumario[] resultado = BenchmarkNavegacao.correrTudo(false);
        BenchmarkNavegacao.Sumario classico = resultado[0];
        BenchmarkNavegacao.Sumario otimizado = resultado[1];

        assertTrue(otimizado.resolvidos > classico.resolvidos,
                "o otimizado deve resolver mais cenários que o clássico ("
                        + otimizado.resolvidos + " vs " + classico.resolvidos + ")");
        assertTrue(otimizado.score < classico.score,
                "o otimizado deve ter score global (turnos/falhas) muito menor ("
                        + otimizado.score + " vs " + classico.score + ")");
    }
}
