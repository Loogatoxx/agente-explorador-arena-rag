package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.AgenteQ;
import pt.ipt.arena.heuristica.AvaliadorQ;
import pt.ipt.arena.heuristica.AvaliadorQHibrido;
import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.heuristica.TabelaQ;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Treinador de Q-Learning 100% LOCAL: corre milhares de partidas free-for-all no
 * {@link SimuladorBatalha} (rápido, sem rede nem API) e deixa o agente
 * {@link AvaliadorQ} aprender a política por reforço. Mostra a curva de
 * aprendizagem, guarda a tabela Q em ficheiro e, no fim, compara em GREEDY o
 * agente aprendido com o agente ENTREGUE (o "combo") no MESMO campo.
 *
 * Uso: java ... TreinadorQ [episodios] [ficheiroSaida]
 */
public final class TreinadorQ {

    private TreinadorQ() {
    }

    public static void main(String[] args) throws Exception {
        int episodios = args.length > 0 ? Integer.parseInt(args[0]) : 40000;
        String modo = args.length > 1 ? args[1] : "hibrido";
        String saida = args.length > 2 ? args[2] : "qtable.txt";
        boolean hibrido = !"puro".equalsIgnoreCase(modo);
        int n = 17;
        int nRecursos = 8;
        int nCofres = 4;

        TabelaQ tabela = new TabelaQ(4, 0.10, 0.95);
        List<Supplier<MotorHeuristico>> campo = new ArrayList<>(CombatePerfis.perfis().values());
        Random rng = new Random(123);
        int k = campo.size() + 1;

        System.out.println("=== Treino Q-Learning (LOCAL) — modo " + (hibrido ? "HIBRIDO" : "PURO") + " ===");
        System.out.println("episodios=" + episodios + " | campo=" + campo.size()
                + " adversarios | arena " + n + "x" + n + " | acoes=4");
        System.out.printf("%-10s %-9s %-16s %-7s%n", "episodio", "epsilon", "colocacao(med/8)", "1os%");
        System.out.println("------------------------------------------------");

        int janela = Math.max(1000, episodios / 20);
        double somaPts = 0;
        int primeiros = 0;
        int conta = 0;
        for (int e = 0; e < episodios; e++) {
            double eps = Math.max(0.05, 1.0 - e / (0.8 * episodios));
            AgenteQ aq = hibrido
                    ? new AvaliadorQHibrido(tabela, eps, true, rng.nextLong())
                    : new AvaliadorQ(tabela, eps, true, rng.nextLong());
            int place = SimuladorBatalha.treinarEpisodio(aq, campo, e, n, nRecursos, nCofres);
            somaPts += (k - 1 - place);
            if (place == 0) {
                primeiros++;
            }
            conta++;
            if ((e + 1) % janela == 0) {
                System.out.printf("%-10d %-9.3f %-16.3f %-7.1f%n",
                        e + 1, eps, somaPts / conta, 100.0 * primeiros / conta);
                somaPts = 0;
                primeiros = 0;
                conta = 0;
            }
        }

        tabela.guardar(Path.of(saida));
        System.out.println("\nTabela Q guardada em '" + saida + "' (" + tabela.tamanho() + " estados).");

        // --- Avaliacao final GREEDY vs combo, no mesmo campo ---
        Supplier<MotorHeuristico> qGreedy = () -> MotorHeuristico.comQ(
                hibrido ? new AvaliadorQHibrido(tabela, 0.0, false, 1) : new AvaliadorQ(tabela, 0.0, false, 1));
        Supplier<MotorHeuristico> combo = () -> new MotorHeuristico("combo");
        double[] rq = SimuladorBatalha.avaliarContra(qGreedy, campo, 200, n, nRecursos, nCofres);
        double[] rc = SimuladorBatalha.avaliarContra(combo, campo, 200, n, nRecursos, nCofres);
        System.out.println("\n=== GREEDY vs combo (200 seeds, mesmo campo) ===");
        System.out.printf("%-14s %-12s %-8s %-8s%n", "agente", "colocacao/8", "1os%", "ult%");
        System.out.printf("%-14s %-12.3f %-8.1f %-8.1f%n", "Q-Learning", rq[0], rq[1], rq[2]);
        System.out.printf("%-14s %-12.3f %-8.1f %-8.1f%n", "combo", rc[0], rc[1], rc[2]);
    }
}
