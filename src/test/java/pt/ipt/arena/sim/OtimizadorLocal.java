package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.Postura;
import pt.ipt.arena.sim.SimuladorBatalha.Arquetipo;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * OTIMIZADOR LOCAL (estilo "machine learning" por força bruta) — corre na
 * máquina do aluno, faz milhares de simulações de combate e escolhe a melhor
 * estratégia, escrevendo um relatório JSON.
 *
 * Como correr (na raiz do projeto, depois de `mvn test-compile`):
 *   java -cp "target/test-classes:target/classes:GSON.jar" pt.ipt.arena.sim.OtimizadorLocal 100 otimizador_resultado.json
 * (1.º arg = nº de seeds por candidato; 2.º arg = ficheiro de saída.)
 *
 * Depois, envia o JSON ao assistente para ele aplicar a melhor configuração.
 */
public final class OtimizadorLocal {

    public static void main(String[] args) throws Exception {
        int seeds = args.length > 0 ? Integer.parseInt(args[0]) : 60;
        String saida = args.length > 1 ? args[1] : "otimizador_resultado.json";

        List<Arquetipo> campo = SimuladorBatalha.campoPadrao();
        int[] limiares = {0, 20, 40, 60};
        int[] margens = {-15, 0, 15, 30, 45};
        Postura[] posturas = {Postura.NEUTRO, Postura.EVITAR, Postura.CACAR};

        int total = limiares.length * margens.length * posturas.length;
        int feito = 0;
        double melhorMedia = -1;
        String melhor = "";
        List<String> registos = new ArrayList<>();

        for (Postura p : posturas) {
            for (int li : limiares) {
                for (int mg : margens) {
                    double[] r = SimuladorBatalha.avaliarCandidato(
                            new Arquetipo("C", li, mg, p), campo, seeds, 17, 8, 4);
                    registos.add(String.format(
                            "{\"postura\":\"%s\",\"limiar\":%d,\"margem\":%d,\"media\":%.3f,\"vitorias\":%.1f,\"eliminado\":%.1f}",
                            p, li, mg, r[0], r[1], r[2]));
                    if (r[0] > melhorMedia) {
                        melhorMedia = r[0];
                        melhor = "postura=" + p + " limiar=" + li + " margem=" + mg;
                    }
                    feito++;
                    System.out.printf("[%d/%d] %-7s l=%-2d m=%-3d -> media=%.2f vit=%.1f%% elim=%.1f%%%n",
                            feito, total, p, li, mg, r[0], r[1], r[2]);
                }
            }
        }

        String json = "{\"seeds\":" + seeds + ",\"melhor\":\"" + melhor + "\",\"melhorMedia\":"
                + String.format("%.3f", melhorMedia) + ",\"resultados\":[" + String.join(",", registos) + "]}";
        Files.writeString(Path.of(saida), json);
        System.out.println("\nMELHOR: " + melhor + " (media=" + String.format("%.3f", melhorMedia) + ")");
        System.out.println("Relatorio JSON escrito em: " + saida);
    }
}
