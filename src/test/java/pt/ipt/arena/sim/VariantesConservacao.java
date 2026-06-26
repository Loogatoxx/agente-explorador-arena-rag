package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.MotorHeuristico;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Compara variantes da MARGEM de conservação do combo em battle royale, para ver
 * se alguma supera a atual. margem 1000 ≈ "sem conservação" (nunca campa).
 */
public final class VariantesConservacao {

    private VariantesConservacao() {
    }

    public static void main(String[] args) {
        int seeds = args.length > 0 ? Integer.parseInt(args[0]) : 400;
        int n = 17;
        int nR = 8;
        int nC = 4;
        List<Supplier<MotorHeuristico>> campo = new ArrayList<>(CombatePerfis.perfis().values());

        int[] margens = {1000, 60, 30, 0, -30, -60, -100};
        System.out.println("=== Variantes de margem de conservação (" + seeds + " seeds paralelos) ===");
        System.out.printf("%-22s %-12s %-8s %-8s%n", "margem", "colocacao/8", "1os%", "ult%");
        System.out.println("------------------------------------------------------");
        for (int m : margens) {
            final int margem = m;
            Supplier<MotorHeuristico> bot = () -> MotorHeuristico.comConservacaoMargem(margem);
            double[] r = SimuladorBatalha.avaliarContraParalelo(bot, campo, seeds, n, nR, nC);
            String etiqueta = m == 1000 ? "1000 (sem conserv.)" : (m == 0 ? "0 (ATUAL)" : String.valueOf(m));
            System.out.printf("%-22s %-12.3f %-8.1f %-8.1f%n", etiqueta, r[0], r[1], r[2]);
        }
    }
}
