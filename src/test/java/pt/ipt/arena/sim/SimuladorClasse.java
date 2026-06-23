package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.AvaliadorAtracao;
import pt.ipt.arena.heuristica.AvaliadorBatalha;
import pt.ipt.arena.heuristica.AvaliadorMapaCalor;
import pt.ipt.arena.heuristica.AvaliadorReflexos;
import pt.ipt.arena.heuristica.FiltroColisoes;
import pt.ipt.arena.heuristica.MemoriaHeuristica;
import pt.ipt.arena.heuristica.MotorHeuristico;

import java.util.List;
import java.util.function.Supplier;

/**
 * Estima as hipóteses do NOSSO algoritmo contra um "campo" de colegas que fazem
 * apenas o mínimo (sem estratégia sofisticada): marcha aleatória, navegação
 * básica (só radar, sem mapa/BFS) e combate naïf. Free-for-all, N partidas.
 */
public final class SimuladorClasse {

    public static void main(String[] args) {
        int seeds = args.length > 0 ? Integer.parseInt(args[0]) : 200;

        // --- Bots "mínimo" (o que um colega sem ajuda sofisticada entrega) ---
        Supplier<MotorHeuristico> aleatorio = () -> new MotorHeuristico(
                "r", new MemoriaHeuristica(), new FiltroColisoes(), List.of(new AvaliadorAleatorio()));
        Supplier<MotorHeuristico> basicoPacifista = () -> new MotorHeuristico(
                "bp", new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(new AvaliadorReflexos(), new AvaliadorAtracao(), new AvaliadorMapaCalor()));
        Supplier<MotorHeuristico> basicoAgressivo = () -> new MotorHeuristico(
                "ba", new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(new AvaliadorReflexos(), new AvaliadorBatalha(0, -1000, false),
                        new AvaliadorAtracao(), new AvaliadorMapaCalor()));

        // Campo de 7 "colegas": 2 aleatórios + 3 básicos pacifistas + 2 básicos agressivos.
        List<Supplier<MotorHeuristico>> campo = List.of(
                aleatorio, aleatorio, basicoPacifista, basicoPacifista, basicoPacifista,
                basicoAgressivo, basicoAgressivo);

        Supplier<MotorHeuristico> nosso = () -> new MotorHeuristico("NOSSO");

        System.out.println("=== NOSSO algoritmo vs " + campo.size() + " colegas 'minimo' (" + seeds + " partidas) ===");
        double[] r = SimuladorBatalha.avaliarContra(nosso, campo, seeds, 17, 8, 4);
        System.out.printf("  Colocacao media : %.2f / %d  (7 = 1o sempre)%n", r[0], campo.size());
        System.out.printf("  1o lugar (VENCE): %.1f %%   <-- hipotese de ter o melhor%n", r[1]);
        System.out.printf("  Eliminado 1o    : %.1f %%%n", r[2]);

        System.out.println("\n--- Referencia: e se fossemos NOS um colega 'minimo'? (mesmo campo) ---");
        double[] refP = SimuladorBatalha.avaliarContra(basicoPacifista, campo, seeds, 17, 8, 4);
        double[] refA = SimuladorBatalha.avaliarContra(basicoAgressivo, campo, seeds, 17, 8, 4);
        double[] refR = SimuladorBatalha.avaliarContra(aleatorio, campo, seeds, 17, 8, 4);
        System.out.printf("  basico pacifista: media=%.2f  1o=%.1f%%%n", refP[0], refP[1]);
        System.out.printf("  basico agressivo: media=%.2f  1o=%.1f%%%n", refA[0], refA[1]);
        System.out.printf("  aleatorio       : media=%.2f  1o=%.1f%%%n", refR[0], refR[1]);
        System.out.printf("%n(Acaso puro = %.1f%% de 1o lugar entre %d bots)%n", 100.0 / (campo.size() + 1), campo.size() + 1);
    }
}
