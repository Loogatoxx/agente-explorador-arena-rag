package pt.ipt.arena.sim;

import com.google.gson.Gson;

import pt.ipt.arena.heuristica.Bussola;
import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.Percecao;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Simulador local da Arena (apenas para benchmarks/testes — NÃO faz parte do
 * agente entregue). Gera um labirinto determinista (por seed), simula a
 * perceção do radar (alcance 4.5 com oclusão por linha de visão) e aplica as
 * ações, permitindo medir objetivamente o desempenho de navegação do
 * {@link MotorHeuristico} sem depender do servidor real.
 */
public class SimuladorArena {

    private static final double RAIO_RADAR = 4.5;
    private static final Gson GSON = new Gson();

    private final int largura;
    private final int altura;
    private final boolean[][] parede;
    private final Set<String> recursos = new HashSet<>();
    private final Set<String> cofres = new HashSet<>();
    private final int totalCofres;

    private int x;
    private int y;
    private int hp;
    private final Set<String> visitadas = new HashSet<>();

    public SimuladorArena(long seed, int largura, int altura, double densidadeParede,
                          int nRecursos, int nCofres, int hpInicial) {
        this.hp = hpInicial;
        this.largura = largura;
        this.altura = altura;
        this.parede = new boolean[largura][altura];
        Random r = new Random(seed);
        for (int i = 0; i < largura; i++) {
            for (int j = 0; j < altura; j++) {
                parede[i][j] = r.nextDouble() < densidadeParede;
            }
        }
        // Bordo sólido de paredes (a arena real é fechada — ver Fig. 2 do enunciado).
        for (int i = 0; i < largura; i++) {
            parede[i][0] = true;
            parede[i][altura - 1] = true;
        }
        for (int j = 0; j < altura; j++) {
            parede[0][j] = true;
            parede[largura - 1][j] = true;
        }
        int sx = largura / 2;
        int sy = altura / 2;
        parede[sx][sy] = false;
        this.x = sx;
        this.y = sy;

        List<int[]> alcancaveis = alcancaveisDe(sx, sy);
        Collections.shuffle(alcancaveis, r);
        int idx = 0;
        int postos = 0;
        while (postos < nCofres && idx < alcancaveis.size()) {
            int[] c = alcancaveis.get(idx++);
            if (c[0] == sx && c[1] == sy) {
                continue;
            }
            cofres.add(c[0] + "," + c[1]);
            postos++;
        }
        postos = 0;
        while (postos < nRecursos && idx < alcancaveis.size()) {
            int[] c = alcancaveis.get(idx++);
            String k = c[0] + "," + c[1];
            if ((c[0] == sx && c[1] == sy) || cofres.contains(k)) {
                continue;
            }
            recursos.add(k);
            postos++;
        }
        this.totalCofres = cofres.size();
        visitadas.add(sx + "," + sy);
    }

    private boolean livre(int i, int j) {
        return i >= 0 && i < largura && j >= 0 && j < altura && !parede[i][j];
    }

    private List<int[]> alcancaveisDe(int sx, int sy) {
        List<int[]> resultado = new ArrayList<>();
        boolean[][] visto = new boolean[largura][altura];
        Deque<int[]> fila = new ArrayDeque<>();
        fila.add(new int[]{sx, sy});
        visto[sx][sy] = true;
        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        while (!fila.isEmpty()) {
            int[] c = fila.poll();
            resultado.add(c);
            for (int[] d : dirs) {
                int ni = c[0] + d[0];
                int nj = c[1] + d[1];
                if (livre(ni, nj) && !visto[ni][nj]) {
                    visto[ni][nj] = true;
                    fila.add(new int[]{ni, nj});
                }
            }
        }
        return resultado;
    }

    /** Constrói a perceção (radar) do turno atual em torno do agente. */
    public Percecao perceber() {
        List<String> paredes = new ArrayList<>();
        List<String> recs = new ArrayList<>();
        List<String> cofs = new ArrayList<>();
        int r = (int) Math.ceil(RAIO_RADAR);
        for (int i = Math.max(0, x - r); i <= Math.min(largura - 1, x + r); i++) {
            for (int j = Math.max(0, y - r); j <= Math.min(altura - 1, y + r); j++) {
                if (Math.hypot(i - x, j - y) > RAIO_RADAR || !visivel(i, j)) {
                    continue;
                }
                String k = i + "," + j;
                if (parede[i][j]) {
                    paredes.add(coord(i, j));
                } else {
                    if (recursos.contains(k)) {
                        recs.add(coord(i, j));
                    }
                    if (cofres.contains(k)) {
                        cofs.add(coord(i, j));
                    }
                }
            }
        }
        String json = "{\"game_started\":true,"
                + "\"o_meu_estado\":{\"x\":" + x + ",\"y\":" + y + ",\"hp\":" + hp + "},"
                + "\"objetos_fixos\":[" + String.join(",", paredes) + "],"
                + "\"recursos_no_mundo\":[" + String.join(",", recs) + "],"
                + "\"cofres_no_mundo\":[" + String.join(",", cofs) + "]}";
        return GSON.fromJson(json, Percecao.class);
    }

    private static String coord(int i, int j) {
        return "{\"x\":" + i + ",\"y\":" + j + "}";
    }

    /** Linha de visão (Bresenham): há visão se nenhuma parede fica ESTRITAMENTE entre. */
    private boolean visivel(int ci, int cj) {
        int x0 = x;
        int y0 = y;
        int dx = Math.abs(ci - x0);
        int dy = Math.abs(cj - y0);
        int sx = x0 < ci ? 1 : -1;
        int sy = y0 < cj ? 1 : -1;
        int err = dx - dy;
        int cx = x0;
        int cy = y0;
        while (true) {
            boolean extremo = (cx == x0 && cy == y0) || (cx == ci && cy == cj);
            if (!extremo && parede[cx][cy]) {
                return false;
            }
            if (cx == ci && cy == cj) {
                return true;
            }
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                cx += sx;
            }
            if (e2 < dx) {
                err += dx;
                cy += sy;
            }
        }
    }

    /** Aplica a ação decidida pelo motor (movimento, custo de HP, coleta, cofres). */
    public void aplicar(Acao acao, MotorHeuristico motor) {
        Bussola b = Bussola.de(acao);
        int nx = x + b.getDeltaX();
        int ny = y + b.getDeltaY();
        if (!livre(nx, ny)) {
            return; // bloqueado pelo servidor: não se move (sem custo)
        }
        x = nx;
        y = ny;
        hp -= 1;
        visitadas.add(x + "," + y);
        String k = x + "," + y;
        if (recursos.remove(k)) {
            hp = Math.min(250, hp + 20);
        }
        if (cofres.remove(k)) {
            hp = Math.min(250, hp + 100);
            if (motor != null) {
                motor.marcarCofreResolvido(new Coordenada(x, y));
            }
        }
    }

    public boolean todosCofresAlcancados() {
        return cofres.isEmpty();
    }

    public int totalCofres() {
        return totalCofres;
    }

    public int hp() {
        return hp;
    }

    public int celulasVisitadas() {
        return visitadas.size();
    }

    /** Corre uma partida e grava um replay (JSON) para visualização animada. */
    public String gravarReplayJson(MotorHeuristico motor, int maxTurnos) {
        StringBuilder paredes = new StringBuilder();
        for (int i = 0; i < largura; i++) {
            for (int j = 0; j < altura; j++) {
                if (parede[i][j]) {
                    juntar(paredes, "[" + i + "," + j + "]");
                }
            }
        }
        String recIni = celulasJson(recursos);
        String cofIni = celulasJson(cofres);
        StringBuilder frames = new StringBuilder();
        StringBuilder eventos = new StringBuilder();
        juntar(frames, "{\"x\":" + x + ",\"y\":" + y + ",\"hp\":" + hp + "}");
        for (int t = 1; t <= maxTurnos; t++) {
            Set<String> recAntes = new HashSet<>(recursos);
            Set<String> cofAntes = new HashSet<>(cofres);
            aplicar(motor.decidir(perceber()), motor);
            for (String k : recAntes) {
                if (!recursos.contains(k)) {
                    juntar(eventos, evento(t, "recurso", k));
                }
            }
            for (String k : cofAntes) {
                if (!cofres.contains(k)) {
                    juntar(eventos, evento(t, "cofre", k));
                }
            }
            juntar(frames, "{\"x\":" + x + ",\"y\":" + y + ",\"hp\":" + hp + "}");
            if (todosCofresAlcancados() || hp <= 0) {
                break;
            }
        }
        return "{\"n\":" + largura + ",\"paredes\":[" + paredes + "],\"recursos\":[" + recIni
                + "],\"cofres\":[" + cofIni + "],\"frames\":[" + frames + "],\"eventos\":[" + eventos + "]}";
    }

    private static void juntar(StringBuilder sb, String item) {
        if (sb.length() > 0) {
            sb.append(',');
        }
        sb.append(item);
    }

    private static String celulasJson(Set<String> cells) {
        StringBuilder sb = new StringBuilder();
        for (String k : cells) {
            String[] p = k.split(",");
            juntar(sb, "[" + p[0] + "," + p[1] + "]");
        }
        return sb.toString();
    }

    private static String evento(int t, String tipo, String cell) {
        String[] p = cell.split(",");
        return "{\"t\":" + t + ",\"tipo\":\"" + tipo + "\",\"x\":" + p[0] + ",\"y\":" + p[1] + "}";
    }

    public int px() {
        return x;
    }

    public int py() {
        return y;
    }

    public int cofresRestantes() {
        return cofres.size();
    }
}
