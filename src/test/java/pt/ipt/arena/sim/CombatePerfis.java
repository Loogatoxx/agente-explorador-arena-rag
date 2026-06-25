package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.AvaliadorAcao;
import pt.ipt.arena.heuristica.AvaliadorReflexos;
import pt.ipt.arena.heuristica.FiltroColisoes;
import pt.ipt.arena.heuristica.MemoriaHeuristica;
import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.heuristica.Postura;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Comparador de COMBATE: mede candidatos (incluindo o agente ENTREGUE — o "combo"
 * por omissão) em duas frentes, contra um campo de adversários que inclui um bot
 * ALEATÓRIO (random):
 *   - Battle royale: colocação média / 1ºs % / últimos % contra o campo completo.
 *   - 1v1: taxa de vitória contra cada perfil, um a um.
 *
 * Só para benchmark — não faz parte do agente entregue.
 */
public final class CombatePerfis {

    private CombatePerfis() {
    }

    /** Motor de um colega "random": reflexos de sobrevivência + movimento ao acaso. */
    static MotorHeuristico motorRandom(String id) {
        List<AvaliadorAcao> cadeia = new ArrayList<>();
        cadeia.add(new AvaliadorReflexos());
        cadeia.add(new AvaliadorAleatorio());
        return new MotorHeuristico(id, new MemoriaHeuristica(), new FiltroColisoes(), cadeia);
    }

    /** Adversários (perfis) com fábrica de motor, incluindo dois random. */
    static Map<String, Supplier<MotorHeuristico>> perfis() {
        Map<String, Supplier<MotorHeuristico>> m = new LinkedHashMap<>();
        m.put("Agressor", () -> MotorHeuristico.comEstrategiaCompleta("Agressor", 0, -1000, Postura.CACAR, true));
        m.put("Cacador", () -> MotorHeuristico.comEstrategiaCompleta("Cacador", 0, -15, Postura.CACAR, true));
        m.put("Equilibrado", () -> MotorHeuristico.comEstrategiaCompleta("Equilibrado", 0, -15, Postura.NEUTRO, true));
        m.put("Oportunista", () -> MotorHeuristico.comEstrategiaCompleta("Oportunista", 0, 30, Postura.NEUTRO, true));
        m.put("Conservador", () -> MotorHeuristico.comEstrategiaCompleta("Conservador", 40, 15, Postura.EVITAR, false));
        m.put("Cauteloso", () -> MotorHeuristico.comEstrategiaCompleta("Cauteloso", 60, 30, Postura.EVITAR, false));
        m.put("Random1", () -> motorRandom("Random1"));
        m.put("Random2", () -> motorRandom("Random2"));
        return m;
    }

    /** Candidatos NOSSOS a comparar. */
    static Map<String, Supplier<MotorHeuristico>> candidatos() {
        Map<String, Supplier<MotorHeuristico>> m = new LinkedHashMap<>();
        m.put("ENTREGUE(combo)", () -> new MotorHeuristico("nos"));
        m.put("Evasivo", () -> MotorHeuristico.comEstrategiaCompleta("nos", 0, 30, Postura.EVITAR, true));
        m.put("CacadorSel", () -> MotorHeuristico.comEstrategiaCompleta("nos", 20, 30, Postura.CACAR, true));
        m.put("Cauteloso", () -> MotorHeuristico.comEstrategiaCompleta("nos", 60, 30, Postura.EVITAR, true));
        return m;
    }

    public static void main(String[] args) {
        int seedsBR = args.length > 0 ? Integer.parseInt(args[0]) : 150;
        int seeds1v1 = args.length > 1 ? Integer.parseInt(args[1]) : 200;
        Map<String, Supplier<MotorHeuristico>> perfis = perfis();
        List<Supplier<MotorHeuristico>> campo = new ArrayList<>(perfis.values());

        System.out.println("====== BATTLE ROYALE (" + (campo.size() + 1) + " bots, " + seedsBR
                + " seeds, arena 17x17) ======");
        System.out.printf("%-18s %-12s %-8s %-8s%n", "candidato", "colocacao", "1os%", "ult%");
        System.out.println("--------------------------------------------------");
        for (Map.Entry<String, Supplier<MotorHeuristico>> c : candidatos().entrySet()) {
            double[] r = SimuladorBatalha.avaliarContra(c.getValue(), campo, seedsBR, 17, 8, 4);
            System.out.printf("%-18s %-12.3f %-8.1f %-8.1f%n", c.getKey(), r[0], r[1], r[2]);
        }

        System.out.println();
        System.out.println("====== 1v1 — taxa de VITORIA (%) por perfil, " + seeds1v1 + " seeds ======");
        System.out.printf("%-18s", "candidato \\ perfil");
        for (String p : perfis.keySet()) {
            System.out.printf("%-12s", p);
        }
        System.out.printf("%-8s%n", "MEDIA");
        System.out.println("---------------------------------------------------------------------------------------------------------------");
        for (Map.Entry<String, Supplier<MotorHeuristico>> c : candidatos().entrySet()) {
            System.out.printf("%-18s", c.getKey());
            double soma = 0;
            for (Supplier<MotorHeuristico> adv : perfis.values()) {
                double vit = SimuladorBatalha.avaliarContra(c.getValue(), List.of(adv), seeds1v1, 13, 4, 2)[1];
                soma += vit;
                System.out.printf("%-12.0f", vit);
            }
            System.out.printf("%-8.1f%n", soma / perfis.size());
        }
    }
}
