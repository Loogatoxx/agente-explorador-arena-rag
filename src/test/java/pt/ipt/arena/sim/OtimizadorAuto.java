package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.Postura;
import pt.ipt.arena.sim.SimuladorBatalha.Arquetipo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Otimizador AUTOMÁTICO (hill-climbing com reinícios aleatórios) — auto-ajusta-se,
 * SEM grelha de parâmetros predefinida. Parte de configurações, muta o genoma da
 * estratégia, e move-se sempre para o vizinho com melhor desempenho até
 * estabilizar (ótimo local); repete com reinícios aleatórios para fugir a ótimos
 * locais. Corre no PC do aluno e escreve a melhor configuração em JSON.
 *
 * Genoma: limiar (HP), margem (vantagem), postura (NEUTRO/EVITAR/CACAR), remate.
 *
 * Uso: java ... OtimizadorAuto [reinicios] [seedsBusca] [seedsFinal] [saida.json]
 */
public final class OtimizadorAuto {

    static final Postura[] POSTURAS = {Postura.NEUTRO, Postura.EVITAR, Postura.CACAR};

    static final class Genoma {
        int limiar;
        int margem;
        int postura;
        boolean remate;

        Genoma(int limiar, int margem, int postura, boolean remate) {
            this.limiar = limiar;
            this.margem = margem;
            this.postura = postura;
            this.remate = remate;
        }

        Genoma copia() {
            return new Genoma(limiar, margem, postura, remate);
        }

        Arquetipo arquetipo() {
            return new Arquetipo("C", limiar, margem, POSTURAS[postura], remate);
        }

        @Override
        public String toString() {
            return "postura=" + POSTURAS[postura] + " limiar=" + limiar + " margem=" + margem + " remate=" + remate;
        }
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** Vizinhos do genoma: mutações de um parâmetro de cada vez. */
    static List<Genoma> vizinhos(Genoma g) {
        List<Genoma> v = new ArrayList<>();
        for (int d : new int[]{-20, 20}) {
            Genoma n = g.copia();
            n.limiar = clamp(g.limiar + d, 0, 100);
            v.add(n);
        }
        for (int d : new int[]{-15, 15, 45}) {
            Genoma n = g.copia();
            n.margem = clamp(g.margem + d, -30, 200);
            v.add(n);
        }
        for (int p = 0; p < POSTURAS.length; p++) {
            if (p != g.postura) {
                Genoma n = g.copia();
                n.postura = p;
                v.add(n);
            }
        }
        Genoma toggle = g.copia();
        toggle.remate = !g.remate;
        v.add(toggle);
        return v;
    }

    private static double fit(Genoma g, List<Arquetipo> campo, int seeds) {
        return SimuladorBatalha.avaliarCandidato(g.arquetipo(), campo, seeds, 17, 8, 4)[0];
    }

    public static void main(String[] args) throws Exception {
        int reinicios = args.length > 0 ? Integer.parseInt(args[0]) : 6;
        int seedsBusca = args.length > 1 ? Integer.parseInt(args[1]) : 30;
        int seedsFinal = args.length > 2 ? Integer.parseInt(args[2]) : 200;
        String saida = args.length > 3 ? args[3] : "otimizador_auto.json";

        List<Arquetipo> campo = SimuladorBatalha.campoPadrao();
        Random rng = new Random(42);
        Genoma melhorGlobal = null;
        double melhorFit = -1;

        for (int r = 0; r < reinicios; r++) {
            Genoma atual = (r == 0)
                    ? new Genoma(0, 30, 1, false)  // arranca da estratégia atual (EVITAR + margem 30)
                    : new Genoma(rng.nextInt(101), -30 + rng.nextInt(231), rng.nextInt(POSTURAS.length), rng.nextBoolean());
            double fitAtual = fit(atual, campo, seedsBusca);

            boolean melhorou = true;
            int passos = 0;
            while (melhorou && passos < 40) {
                melhorou = false;
                passos++;
                Genoma melhorViz = null;
                double melhorVizFit = fitAtual;
                for (Genoma viz : vizinhos(atual)) {
                    double f = fit(viz, campo, seedsBusca);
                    if (f > melhorVizFit) {
                        melhorVizFit = f;
                        melhorViz = viz;
                    }
                }
                if (melhorViz != null) {
                    atual = melhorViz;
                    fitAtual = melhorVizFit;
                    melhorou = true;
                }
            }

            double fitFinal = fit(atual, campo, seedsFinal);
            System.out.printf("Reinicio %d -> %s | fit(busca)=%.3f fit(%d)=%.3f%n",
                    r, atual, fitAtual, seedsFinal, fitFinal);
            if (fitFinal > melhorFit) {
                melhorFit = fitFinal;
                melhorGlobal = atual.copia();
            }
        }

        System.out.printf("%n=== MELHOR (re-avaliado com %d seeds) ===%n%s%nfit=%.3f%n",
                seedsFinal, melhorGlobal, melhorFit);
        String json = String.format(
                "{\"postura\":\"%s\",\"limiar\":%d,\"margem\":%d,\"remate\":%b,\"fit\":%.3f,\"seedsFinal\":%d}",
                POSTURAS[melhorGlobal.postura], melhorGlobal.limiar, melhorGlobal.margem,
                melhorGlobal.remate, melhorFit, seedsFinal);
        Files.writeString(Path.of(saida), json);
        System.out.println("JSON escrito em: " + saida);
    }
}
