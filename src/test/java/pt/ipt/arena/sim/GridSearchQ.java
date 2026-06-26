package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.AvaliadorQHibrido;
import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.heuristica.RecompensaQ;
import pt.ipt.arena.heuristica.TabelaQ;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Grid Search MULTITHREADED dos pesos da recompensa do híbrido. Testa várias
 * combinações em PARALELO (uma por núcleo, via {@code parallelStream}): cada uma
 * faz um treino rápido + avaliação; guarda-se a melhor e faz-se o treino completo
 * só nessa. O benchmark final usa avaliação paralela. Tudo 100% local.
 *
 * Uso: java ... GridSearchQ [episodiosBusca] [episodiosFinal] [ficheiroSaida]
 */
public final class GridSearchQ {

    private static final int N = 17;
    private static final int NR = 8;
    private static final int NC = 4;

    private GridSearchQ() {
    }

    /** Treina um combo (rápido) e devolve a colocação média em greedy. */
    private static double treinarEAvaliar(RecompensaQ rec, int episodios, int seedsEval,
                                          List<Supplier<MotorHeuristico>> campo) {
        TabelaQ tabela = new TabelaQ(AvaliadorQHibrido.N_POSTURAS, 0.10, 0.95);
        Random rng = new Random(1234); // mesma sequência para todos os combos = comparação justa
        for (int e = 0; e < episodios; e++) {
            double eps = Math.max(0.05, 1.0 - e / (0.8 * episodios));
            AvaliadorQHibrido aq = new AvaliadorQHibrido(tabela, eps, true, rng.nextLong(), rec);
            SimuladorBatalha.treinarEpisodio(aq, campo, e, N, NR, NC);
        }
        // Avaliação SEQUENCIAL (já estamos dentro do parallelStream sobre combos).
        return SimuladorBatalha.avaliarContra(
                () -> MotorHeuristico.comQ(new AvaliadorQHibrido(tabela, 0.0, false, 1, rec)),
                campo, seedsEval, N, NR, NC)[0];
    }

    public static void main(String[] args) throws Exception {
        int epBusca = args.length > 0 ? Integer.parseInt(args[0]) : 4000;
        int epFinal = args.length > 1 ? Integer.parseInt(args[1]) : 20000;
        String saida = args.length > 2 ? args[2] : "qhib_best.txt";
        List<Supplier<MotorHeuristico>> campo = new ArrayList<>(CombatePerfis.perfis().values());

        // Combos: (pesoHp, sobreviver, abate, danoMelee, colocacao)
        List<RecompensaQ> combos = List.of(
                new RecompensaQ(1.0, 0.3, 40, 15, 8),    // V2 (baseline = 5.71)
                new RecompensaQ(1.0, 0.5, 40, 15, 10),
                new RecompensaQ(1.0, 0.3, 50, 10, 12),
                new RecompensaQ(1.5, 0.3, 35, 20, 8),
                new RecompensaQ(1.0, 0.2, 45, 25, 10),
                new RecompensaQ(0.8, 0.5, 50, 15, 12),
                new RecompensaQ(1.2, 0.4, 30, 15, 14),
                new RecompensaQ(1.0, 0.4, 55, 20, 10));

        int nucleos = Runtime.getRuntime().availableProcessors();
        System.out.println("=== GRID SEARCH MULTITHREADED ===");
        System.out.println(combos.size() + " combos | " + epBusca + " ep/combo | "
                + nucleos + " nucleos | campo=" + campo.size());

        long t0 = System.currentTimeMillis();
        List<Map.Entry<RecompensaQ, Double>> resultados = combos.parallelStream()
                .map(rec -> (Map.Entry<RecompensaQ, Double>) new AbstractMap.SimpleEntry<>(
                        rec, treinarEAvaliar(rec, epBusca, 80, campo)))
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .collect(Collectors.toList());
        long dt = (System.currentTimeMillis() - t0) / 1000;

        System.out.printf("%nGrid search terminado em %ds (paralelo):%n", dt);
        for (Map.Entry<RecompensaQ, Double> r : resultados) {
            System.out.printf("  %.3f  <-  %s%n", r.getValue(), r.getKey());
        }

        RecompensaQ melhor = resultados.get(0).getKey();
        System.out.println("\n=== MELHOR combo -> treino COMPLETO (" + epFinal + " ep) ===");
        System.out.println(melhor);

        TabelaQ tabela = new TabelaQ(AvaliadorQHibrido.N_POSTURAS, 0.10, 0.95);
        Random rng = new Random(7);
        for (int e = 0; e < epFinal; e++) {
            double eps = Math.max(0.05, 1.0 - e / (0.8 * epFinal));
            AvaliadorQHibrido aq = new AvaliadorQHibrido(tabela, eps, true, rng.nextLong(), melhor);
            SimuladorBatalha.treinarEpisodio(aq, campo, e, N, NR, NC);
        }
        tabela.guardar(Path.of(saida));
        System.out.println("Tabela guardada em '" + saida + "' (" + tabela.tamanho() + " estados).");

        double[] rq = SimuladorBatalha.avaliarContraParalelo(
                () -> MotorHeuristico.comQ(new AvaliadorQHibrido(tabela, 0.0, false, 1, melhor)),
                campo, 300, N, NR, NC);
        double[] rc = SimuladorBatalha.avaliarContraParalelo(
                () -> new MotorHeuristico("combo"), campo, 300, N, NR, NC);
        System.out.println("\n=== FINAL (300 seeds em paralelo, mesmo campo) ===");
        System.out.printf("  Q-HIBRIDO V3 : colocacao=%.3f  1os=%.1f%%  ult=%.1f%%%n", rq[0], rq[1], rq[2]);
        System.out.printf("  combo        : colocacao=%.3f  1os=%.1f%%  ult=%.1f%%%n", rc[0], rc[1], rc[2]);
        System.out.println(rq[0] > rc[0]
                ? ">>> O HIBRIDO V3 BATE O COMBO (+" + String.format("%.3f", rq[0] - rc[0]) + ") <<<"
                : "combo ainda a frente por " + String.format("%.3f", rc[0] - rq[0]));
    }
}
