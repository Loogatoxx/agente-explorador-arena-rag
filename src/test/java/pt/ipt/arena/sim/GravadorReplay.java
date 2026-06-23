package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.MotorHeuristico;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gera um ficheiro de replay (JSON) de uma partida do agente otimizado, para
 * visualização animada. Só ferramenta de demonstração.
 */
public final class GravadorReplay {

    public static void main(String[] args) throws Exception {
        String saida = args.length > 0 ? args[0] : "/tmp/replay_data.json";
        for (int seed = 0; seed < 60; seed++) {
            SimuladorArena sim = new SimuladorArena(seed, 16, 16, 0.16, 6, 2, 300);
            String json = sim.gravarReplayJson(new MotorHeuristico("demo"), 400);
            int frames = json.split("\\{\"x\"").length - 1;
            if (sim.todosCofresAlcancados() && frames >= 45 && frames <= 220) {
                Files.writeString(Path.of(saida), json);
                System.out.println("OK seed=" + seed + " frames=" + frames + " bytes=" + json.length() + " -> " + saida);
                return;
            }
        }
        System.out.println("Nenhum seed adequado encontrado.");
    }
}
