package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Percecao;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Benchmark A/B do motor de navegação: corre a configuração "clássica" (gulosa,
 * sem mapa/pathfinding) e a "otimizada" (mapa acumulado + BFS + fronteiras) nos
 * MESMOS labirintos (deterministas por seed) e mede quantos turnos cada uma
 * demora a alcançar todos os cofres.
 *
 * Só para benchmark/teste — não faz parte do agente entregue.
 */
public final class BenchmarkNavegacao {

    public static final int SEEDS = 30;
    public static final int LARGURA = 24;
    public static final int ALTURA = 24;
    public static final double DENSIDADE = 0.20;
    public static final int RECURSOS = 5;
    public static final int COFRES = 3;
    public static final int MAX_TURNOS = 3000;
    /** HP não-limitante: isola a eficiência de NAVEGAÇÃO da gestão de bateria. */
    public static final int HP_NAVEGACAO = 1_000_000;

    private BenchmarkNavegacao() {
    }

    /** Resumo de uma configuração ao longo de todos os seeds. */
    public static final class Sumario {
        public int resolvidos;
        public long somaTurnosResolvidos;
        public long score; // turnos (se resolveu) ou MAX_TURNOS (se falhou); menor = melhor

        public double mediaTurnos() {
            return resolvidos == 0 ? 0.0 : (double) somaTurnosResolvidos / resolvidos;
        }
    }

    private static int correr(SimuladorArena sim, MotorHeuristico motor) {
        for (int t = 1; t <= MAX_TURNOS; t++) {
            Percecao percecao = sim.perceber();
            Acao acao = motor.decidir(percecao);
            sim.aplicar(acao, motor);
            if (sim.todosCofresAlcancados()) {
                return t;
            }
            if (sim.hp() <= 0) {
                return -1;
            }
        }
        return -1;
    }

    /** Pontua uma versão (fábrica de motor) na navegação: {resolvidos, score}. */
    public static int[] pontuar(Supplier<MotorHeuristico> fabrica) {
        int resolvidos = 0;
        int score = 0;
        for (int s = 0; s < SEEDS; s++) {
            int t = correr(novo(s), fabrica.get());
            if (t > 0) {
                resolvidos++;
                score += t;
            } else {
                score += MAX_TURNOS;
            }
        }
        return new int[]{resolvidos, score};
    }

    /** Lista os seeds que uma versão NÃO resolve (para diagnóstico). */
    public static List<Integer> seedsQueFalham(Supplier<MotorHeuristico> fabrica) {
        List<Integer> falhas = new ArrayList<>();
        for (int s = 0; s < SEEDS; s++) {
            if (correr(novo(s), fabrica.get()) <= 0) {
                falhas.add(s);
            }
        }
        return falhas;
    }

    public static Sumario[] correrTudo(boolean imprimir) {
        Sumario classico = new Sumario();
        Sumario otimizado = new Sumario();
        if (imprimir) {
            System.out.printf("%-6s | %-10s | %-10s%n", "seed", "classico", "otimizado");
            System.out.println("-----------------------------------------");
        }
        int comuns = 0;
        long turnosComunsClassico = 0;
        long turnosComunsOtimizado = 0;
        for (int s = 0; s < SEEDS; s++) {
            int tc = correr(novo(s), MotorHeuristico.classico("bench"));
            int to = correr(novo(s), new MotorHeuristico("bench"));
            acumular(classico, tc);
            acumular(otimizado, to);
            if (tc > 0 && to > 0) {
                comuns++;
                turnosComunsClassico += tc;
                turnosComunsOtimizado += to;
            }
            if (imprimir) {
                System.out.printf("%-6d | %-10s | %-10s%n", s, fmt(tc), fmt(to));
            }
        }
        if (imprimir) {
            System.out.println("-----------------------------------------");
            System.out.printf("Resolvidos (todos os cofres):  classico=%d/%d   otimizado=%d/%d%n",
                    classico.resolvidos, SEEDS, otimizado.resolvidos, SEEDS);
            if (comuns > 0) {
                System.out.printf("Turnos medios (%d cenarios que AMBOS resolveram): classico=%.1f   otimizado=%.1f%n",
                        comuns, turnosComunsClassico / (double) comuns, turnosComunsOtimizado / (double) comuns);
            }
            System.out.printf("Score global (menor = melhor): classico=%d   otimizado=%d%n",
                    classico.score, otimizado.score);
            if (classico.score > 0) {
                double ganho = 100.0 * (classico.score - otimizado.score) / classico.score;
                System.out.printf("Ganho global do OTIMIZADO:     %.1f%%%n", ganho);
            }
        }
        return new Sumario[]{classico, otimizado};
    }

    private static SimuladorArena novo(long seed) {
        return new SimuladorArena(seed, LARGURA, ALTURA, DENSIDADE, RECURSOS, COFRES, HP_NAVEGACAO);
    }

    private static void acumular(Sumario s, int turnos) {
        if (turnos > 0) {
            s.resolvidos++;
            s.somaTurnosResolvidos += turnos;
            s.score += turnos;
        } else {
            s.score += MAX_TURNOS;
        }
    }

    private static String fmt(int turnos) {
        return turnos > 0 ? String.valueOf(turnos) : "FALHOU";
    }

    public static void main(String[] args) {
        correrTudo(true);
    }
}
