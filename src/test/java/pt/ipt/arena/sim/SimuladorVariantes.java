package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.AvaliadorAlvo;
import pt.ipt.arena.heuristica.AvaliadorAspirador;
import pt.ipt.arena.heuristica.AvaliadorAtracao;
import pt.ipt.arena.heuristica.AvaliadorBatalha;
import pt.ipt.arena.heuristica.AvaliadorCanto;
import pt.ipt.arena.heuristica.AvaliadorEsquiva;
import pt.ipt.arena.heuristica.AvaliadorExploracao;
import pt.ipt.arena.heuristica.AvaliadorAcao;
import pt.ipt.arena.heuristica.AvaliadorMapaCalor;
import pt.ipt.arena.heuristica.AvaliadorPostura;
import pt.ipt.arena.heuristica.AvaliadorReflexos;
import pt.ipt.arena.heuristica.AvaliadorSobrevivencia;
import pt.ipt.arena.heuristica.FiltroColisoes;
import pt.ipt.arena.heuristica.MemoriaHeuristica;
import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.heuristica.Postura;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Compara as VARIANTES propostas (aspirador, esquiva V2, campeiro de cantos e
 * combinações) contra dois campos: os 8 arquétipos (métrica média /8, alvo > 5.0)
 * e os colegas "mínimo" (métrica de vitórias, alvo > 27.5%). Só benchmark.
 */
public final class SimuladorVariantes {

    private static MotorHeuristico m(String id, AvaliadorAcao... avaliadores) {
        return new MotorHeuristico(id, new MemoriaHeuristica(), new FiltroColisoes(), List.of(avaliadores));
    }

    public static void main(String[] args) {
        int seeds = args.length > 0 ? Integer.parseInt(args[0]) : 200;

        List<Supplier<MotorHeuristico>> campo8 = new ArrayList<>();
        for (SimuladorBatalha.Arquetipo arc : SimuladorBatalha.campoPadrao()) {
            campo8.add(() -> arc.motor());
        }

        Supplier<MotorHeuristico> aleat = () -> m("r", new AvaliadorAleatorio());
        Supplier<MotorHeuristico> basP = () -> m("bp", new AvaliadorReflexos(), new AvaliadorAtracao(), new AvaliadorMapaCalor());
        Supplier<MotorHeuristico> basA = () -> m("ba", new AvaliadorReflexos(), new AvaliadorBatalha(0, -1000, false),
                new AvaliadorAtracao(), new AvaliadorMapaCalor());
        List<Supplier<MotorHeuristico>> campoMin = List.of(aleat, aleat, basP, basP, basP, basA, basA);

        record V(String nome, Supplier<MotorHeuristico> motor) {
        }
        List<V> variantes = List.of(
                new V("ATUAL vautour", () -> new MotorHeuristico("x")),
                new V("Aspirador", () -> m("x", new AvaliadorReflexos(), new AvaliadorBatalha(0, 90, true),
                        new AvaliadorAspirador(), new AvaliadorPostura(Postura.EVITAR), new AvaliadorAlvo(), new AvaliadorExploracao())),
                new V("Esquiva V2", () -> m("x", new AvaliadorReflexos(), new AvaliadorBatalha(0, 90, true),
                        new AvaliadorSobrevivencia(70), new AvaliadorEsquiva(), new AvaliadorAlvo(), new AvaliadorExploracao())),
                new V("Canto", () -> m("x", new AvaliadorReflexos(), new AvaliadorBatalha(0, 90, true),
                        new AvaliadorSobrevivencia(70), new AvaliadorPostura(Postura.EVITAR), new AvaliadorAlvo(), new AvaliadorCanto(), new AvaliadorExploracao())),
                new V("Combo asp+esq+canto", () -> m("x", new AvaliadorReflexos(), new AvaliadorBatalha(0, 90, true),
                        new AvaliadorAspirador(), new AvaliadorEsquiva(), new AvaliadorAlvo(), new AvaliadorCanto(), new AvaliadorExploracao())));

        System.out.printf("=== Variantes (%d partidas) ===%n", seeds);
        System.out.printf("%-22s | %-9s %-7s | %-8s%n", "variante", "media/8", "win8%", "winMin%");
        System.out.println("---------------------------------------------------------");
        for (V v : variantes) {
            double[] r8 = SimuladorBatalha.avaliarContra(v.motor(), campo8, seeds, 17, 8, 4);
            double[] rMin = SimuladorBatalha.avaliarContra(v.motor(), campoMin, seeds, 17, 8, 4);
            System.out.printf("%-22s | %-9.3f %-7.1f | %-8.1f%n", v.nome(), r8[0], r8[1], rMin[1]);
        }
        System.out.println("\n(Alvos a bater: media/8 > 5.0 ; winMin > 27.5% ; acaso winMin = 12.5%)");
    }
}
