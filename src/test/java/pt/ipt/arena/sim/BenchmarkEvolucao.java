package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.AvaliadorAcao;
import pt.ipt.arena.heuristica.FiltroColisoes;
import pt.ipt.arena.heuristica.MemoriaHeuristica;
import pt.ipt.arena.heuristica.MotorHeuristico;

import java.util.List;

/**
 * Pontua a EVOLUÇÃO do algoritmo de navegação ao longo das versões e diagnostica
 * os cenários que a versão atual ainda não resolve. Só benchmark/demonstração.
 */
public final class BenchmarkEvolucao {

    public static void main(String[] args) {
        int[] v0 = BenchmarkNavegacao.pontuar(() -> new MotorHeuristico(
                "v0", new MemoriaHeuristica(), new FiltroColisoes(), List.<AvaliadorAcao>of()));
        int[] v1 = BenchmarkNavegacao.pontuar(() -> MotorHeuristico.classico("v1"));
        int[] v2 = BenchmarkNavegacao.pontuar(() -> new MotorHeuristico("v2"));

        System.out.println("=== Evolucao da NAVEGACAO (" + BenchmarkNavegacao.SEEDS + " labirintos; score menor = melhor) ===");
        System.out.printf("%-30s | %-11s | %-8s%n", "versao", "resolvidos", "score");
        System.out.println("-------------------------------------------------------------");
        linha("V0 placeholder (so MOVER_NORTE)", v0);
        linha("V1 classico (guloso, radar)", v1);
        linha("V2 otimizado (mapa+BFS+fronteiras)", v2);

        List<Integer> falhas = BenchmarkNavegacao.seedsQueFalham(() -> new MotorHeuristico("v2"));
        System.out.println("\nDiagnostico V2 — falha nos seeds: " + falhas);
        for (int seed : falhas) {
            SimuladorArena sim = new SimuladorArena(seed,
                    BenchmarkNavegacao.LARGURA, BenchmarkNavegacao.ALTURA, BenchmarkNavegacao.DENSIDADE,
                    BenchmarkNavegacao.RECURSOS, BenchmarkNavegacao.COFRES, BenchmarkNavegacao.HP_NAVEGACAO);
            MotorHeuristico m = new MotorHeuristico("v2");
            for (int t = 0; t < BenchmarkNavegacao.MAX_TURNOS; t++) {
                sim.aplicar(m.decidir(sim.perceber()), m);
                if (sim.todosCofresAlcancados()) {
                    break;
                }
            }
            System.out.printf("  seed %d -> cofresRestantes=%d, celulasVisitadas=%d, posFinal=(%d,%d)%n",
                    seed, sim.cofresRestantes(), sim.celulasVisitadas(), sim.px(), sim.py());
        }
    }

    private static void linha(String nome, int[] r) {
        System.out.printf("%-30s | %-11s | %-8d%n", nome, r[0] + "/" + BenchmarkNavegacao.SEEDS, r[1]);
    }
}
