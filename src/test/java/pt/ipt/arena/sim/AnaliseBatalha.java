package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.MotorHeuristico;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Diagnóstico do COMBO em battle royale: corre N seeds e classifica os jogos —
 * vitórias, e nas derrotas distingue "morreu cedo" (mêlée), "morreu tarde" e
 * "sobreviveu mas com HP baixo" (perdeu no desempate). Lista os piores seeds com
 * detalhe, para percebermos PORQUE certos casos não se ganham.
 */
public final class AnaliseBatalha {

    private AnaliseBatalha() {
    }

    public static void main(String[] args) {
        int seeds = args.length > 0 ? Integer.parseInt(args[0]) : 200;
        int n = 17;
        int nR = 8;
        int nC = 4;
        List<Supplier<MotorHeuristico>> campo = new ArrayList<>(CombatePerfis.perfis().values());
        Supplier<MotorHeuristico> combo = () -> new MotorHeuristico("combo");
        int k = campo.size() + 1;

        int vitorias = 0;
        int morteCedo = 0;   // morte antes do turno 60
        int morteTarde = 0;  // morreu mas já tarde
        int vivoBaixo = 0;   // sobreviveu mas colocou-se mal (perdeu no HP)
        int somaColoc = 0;
        List<int[]> piores = new ArrayList<>(); // [seed, coloc, morte, hp, vivosNaMorte]

        for (int s = 0; s < seeds; s++) {
            int[] r = SimuladorBatalha.analisarNosso(combo, campo, s, n, nR, nC);
            int coloc = r[0];
            int morte = r[1];
            int hp = r[2];
            int vivosNaMorte = r[3];
            somaColoc += (k - 1 - coloc);
            if (coloc == 0) {
                vitorias++;
            } else {
                if (morte >= 0 && morte < 60) {
                    morteCedo++;
                } else if (morte >= 0) {
                    morteTarde++;
                } else {
                    vivoBaixo++;
                }
                if (coloc >= k - 3) { // entre os 3 piores
                    piores.add(new int[]{s, coloc, morte, hp, vivosNaMorte});
                }
            }
        }

        System.out.println("=== Análise do COMBO (" + seeds + " seeds, " + k + " bots) ===");
        System.out.printf("Colocação média: %.3f/8%n", somaColoc / (double) seeds);
        System.out.printf("Vitórias (1.º): %d (%.1f%%)%n", vitorias, 100.0 * vitorias / seeds);
        System.out.println("--- Derrotas, por causa ---");
        int derrotas = seeds - vitorias;
        System.out.printf("  Morte CEDO (<turno 60, mêlée):  %d (%.1f%% das derrotas)%n",
                morteCedo, pct(morteCedo, derrotas));
        System.out.printf("  Morte TARDE:                    %d (%.1f%%)%n", morteTarde, pct(morteTarde, derrotas));
        System.out.printf("  Vivo mas HP baixo (desempate):  %d (%.1f%%)%n", vivoBaixo, pct(vivoBaixo, derrotas));

        System.out.println("--- Piores seeds (colocação, turnoMorte, hpFinal, rivais vivos na morte) ---");
        piores.sort((a, b) -> Integer.compare(b[1], a[1]));
        int mostrar = Math.min(15, piores.size());
        for (int i = 0; i < mostrar; i++) {
            int[] p = piores.get(i);
            System.out.printf("  seed %-3d | coloc=%d | morte=%s | hp=%d | rivaisVivos=%d%n",
                    p[0], p[1], p[2] < 0 ? "vivo" : String.valueOf(p[2]), p[3], p[4]);
        }
    }

    private static double pct(int x, int total) {
        return total == 0 ? 0 : 100.0 * x / total;
    }
}
