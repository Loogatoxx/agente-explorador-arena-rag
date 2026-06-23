package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.Postura;
import pt.ipt.arena.sim.SimuladorBatalha.Arquetipo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Raffinage: testa muitas variantes (postura × limiar × margem) contra um campo
 * FIXO de adversários (os 8 arquétipos), e elege a melhor pelo equilíbrio entre
 * sobrevivência (média de colocação) e capacidade de vencer (% de 1.os lugares).
 * Só benchmark/teste.
 */
public final class RefinamentoBatalha {

    public static void main(String[] args) {
        List<Arquetipo> campo = SimuladorBatalha.campoPadrao();
        int seeds = 30;
        int n = 17;
        int nRec = 8;
        int nCofre = 4;
        Postura[] posturas = {Postura.NEUTRO, Postura.EVITAR};
        int[] limiares = {0, 20, 40};
        int[] margens = {0, 15, 30};

        List<double[]> dados = new ArrayList<>();
        List<Postura> posturaDe = new ArrayList<>();
        List<int[]> paramDe = new ArrayList<>();
        for (Postura p : posturas) {
            for (int li : limiares) {
                for (int mg : margens) {
                    double[] r = SimuladorBatalha.avaliarCandidato(
                            new Arquetipo("C", li, mg, p), campo, seeds, n, nRec, nCofre);
                    double composite = r[0] + r[1] / 25.0; // média + peso na taxa de vitória
                    dados.add(new double[]{composite, r[0], r[1], r[2]});
                    posturaDe.add(p);
                    paramDe.add(new int[]{li, mg});
                }
            }
        }

        Integer[] ordem = new Integer[dados.size()];
        for (int i = 0; i < ordem.length; i++) {
            ordem[i] = i;
        }
        Arrays.sort(ordem, (a, b) -> Double.compare(dados.get(b)[0], dados.get(a)[0]));

        System.out.println("=== Refinamento (" + dados.size() + " variantes vs campo de 8, " + seeds + " seeds) ===");
        System.out.printf("%-9s %-7s %-7s | %-7s %-7s %-7s | %-7s%n",
                "postura", "limiar", "margem", "media", "1os%", "ult%", "score");
        System.out.println("--------------------------------------------------------------------");
        for (int i : ordem) {
            double[] d = dados.get(i);
            System.out.printf("%-9s %-7d %-7d | %-7.2f %-7.1f %-7.1f | %-7.2f%n",
                    posturaDe.get(i), paramDe.get(i)[0], paramDe.get(i)[1], d[1], d[2], d[3], d[0]);
        }
        int b = ordem[0];
        System.out.println("--------------------------------------------------------------------");
        System.out.printf("MELHOR: postura=%s limiar=%d margem=%d  (media=%.2f, 1os=%.1f%%, ult=%.1f%%)%n",
                posturaDe.get(b), paramDe.get(b)[0], paramDe.get(b)[1],
                dados.get(b)[1], dados.get(b)[2], dados.get(b)[3]);
    }
}
