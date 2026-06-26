package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.sim.SimuladorBatalha.ResultadoPartida;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;

/**
 * Análise PROFUNDA das não-vitórias do combo: distingue as derrotas por MORTE das
 * derrotas por SOBREVIVÊNCIA (vivos no fim mas com menos HP que o vencedor), diz
 * QUE perfil nos vence e com que margem de HP, e o turno típico das mortes.
 */
public final class AnaliseProfunda {

    private AnaliseProfunda() {
    }

    public static void main(String[] args) {
        int seeds = args.length > 0 ? Integer.parseInt(args[0]) : 400;
        int n = 17;
        int nR = 8;
        int nC = 4;
        Map<String, Supplier<MotorHeuristico>> perfis = CombatePerfis.perfis();
        List<String> nomes = new ArrayList<>(perfis.keySet());
        List<Supplier<MotorHeuristico>> advs = new ArrayList<>(perfis.values());
        Supplier<MotorHeuristico> combo = () -> new MotorHeuristico("combo");

        int vitorias = 0;
        int derrotaMorte = 0;        // não ganhámos e morremos
        int derrotaSobrevivencia = 0; // não ganhámos mas sobrevivemos (perdemos no HP)
        int somaColoc = 0;
        long somaGapHp = 0;          // (hpVencedor - hpNosso) nas derrotas por sobrevivência
        int mortePrecoce = 0;        // morte < turno 60
        Map<String, Integer> vencedorContra = new TreeMap<>(); // quem nos ganha (quando perdemos)
        Map<Integer, Integer> histColoc = new TreeMap<>();

        for (int s = 0; s < seeds; s++) {
            ResultadoPartida r = SimuladorBatalha.analisarDetalhado(combo, nomes, advs, s, n, nR, nC);
            somaColoc += (perfis.size() - r.colocacao);
            histColoc.merge(r.colocacao + 1, 1, Integer::sum);
            if (r.colocacao == 0) {
                vitorias++;
                continue;
            }
            vencedorContra.merge(r.vencedor, 1, Integer::sum);
            if (r.morteNosso >= 0) {
                derrotaMorte++;
                if (r.morteNosso < 60) {
                    mortePrecoce++;
                }
            } else {
                derrotaSobrevivencia++;
                somaGapHp += (r.hpVencedor - r.hpNosso);
            }
        }

        int derrotas = seeds - vitorias;
        System.out.println("=== ANÁLISE PROFUNDA do combo (" + seeds + " seeds, " + (perfis.size() + 1) + " bots) ===");
        System.out.printf("Vitórias (1.º): %d (%.1f%%)   |   colocação média: %.3f%n",
                vitorias, 100.0 * vitorias / seeds, somaColoc / (double) seeds);
        System.out.println("--- Distribuição das nossas colocações (1=melhor) ---");
        for (Map.Entry<Integer, Integer> e : histColoc.entrySet()) {
            System.out.printf("  %d.º lugar: %3d  (%.1f%%)%n", e.getKey(), e.getValue(), 100.0 * e.getValue() / seeds);
        }
        System.out.println("--- Causa das NÃO-vitórias (" + derrotas + ") ---");
        System.out.printf("  Por MORTE:          %3d (%.1f%%)   [precoce <t60: %d]%n",
                derrotaMorte, pct(derrotaMorte, derrotas), mortePrecoce);
        System.out.printf("  Por SOBREVIVÊNCIA:  %3d (%.1f%%)   [gap HP médio p/ vencedor: %.0f]%n",
                derrotaSobrevivencia, pct(derrotaSobrevivencia, derrotas),
                derrotaSobrevivencia == 0 ? 0 : (double) somaGapHp / derrotaSobrevivencia);
        System.out.println("--- Quem nos vence (quando não ganhamos) ---");
        vencedorContra.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .forEach(e -> System.out.printf("  %-14s %3d (%.1f%% das derrotas)%n",
                        e.getKey(), e.getValue(), pct(e.getValue(), derrotas)));
    }

    private static double pct(int x, int total) {
        return total == 0 ? 0 : 100.0 * x / total;
    }
}
