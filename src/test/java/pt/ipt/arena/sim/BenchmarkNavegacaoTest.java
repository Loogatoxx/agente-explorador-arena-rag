package pt.ipt.arena.sim;

import org.junit.jupiter.api.Test;

import pt.ipt.arena.heuristica.MotorHeuristico;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    /**
     * Exploração ciente do radar (saltos): com o raio de visão por omissão, o
     * agente resolve TODOS os cenários e é muito mais rápido do que a exploração
     * "pisar cada casa" (raio 0). Trava uma regressão para o varrimento lento e
     * garante que não caímos na armadilha do guloso (cobertura incompleta).
     */
    @Test
    void exploracaoComRadarSuperaPisarCadaCasa() {
        int[] raio0 = BenchmarkNavegacao.pontuar(() -> MotorHeuristico.comExploracaoRaio("b", 0));
        int[] raioPadrao = BenchmarkNavegacao.pontuar(
                () -> MotorHeuristico.comExploracaoRaio("b", MotorHeuristico.DEFAULT_RAIO_VISAO));

        assertEquals(BenchmarkNavegacao.SEEDS, raioPadrao[0],
                "o raio por omissão deve resolver TODOS os cenários (sem buracos de cobertura)");
        assertTrue(raioPadrao[0] >= raio0[0],
                "o raio por omissão nunca resolve menos cenários que pisar cada casa");
        assertTrue(raioPadrao[1] < raio0[1] / 2,
                "o raio por omissão deve ser >2x mais rápido em turnos ("
                        + raioPadrao[1] + " vs " + raio0[1] + ")");
    }
}
