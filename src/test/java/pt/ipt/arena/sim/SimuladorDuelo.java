package pt.ipt.arena.sim;

import com.google.gson.Gson;

import pt.ipt.arena.heuristica.Bussola;
import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.Percecao;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Simulador de DUELO 1v1 (apenas benchmark/teste — não faz parte do agente).
 * Duas configurações de combate enfrentam-se numa pequena arena; serve para
 * procurar a melhor "fórmula" de luta/fuga (limiar de HP + margem de vantagem),
 * medindo derrotas (quanto mais perto de 0, melhor).
 */
public final class SimuladorDuelo {

    private static final double RAIO_RADAR = 4.5;
    private static final Gson GSON = new Gson();
    private static final int MAX_TURNOS = 250;

    private final int n;
    private final boolean[][] parede;
    private final Set<String> recursos = new HashSet<>();
    private final Set<String> cofres = new HashSet<>();

    private SimuladorDuelo(long seed, int n) {
        this.n = n;
        this.parede = new boolean[n][n];
        Random r = new Random(seed);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                parede[i][j] = (i == 0 || j == 0 || i == n - 1 || j == n - 1) || r.nextDouble() < 0.08;
            }
        }
        // posições de cura (cofres = +100) e recursos (+20) em células interiores livres
        colocar(r, cofres, 2);
        colocar(r, recursos, 3);
    }

    private void colocar(Random r, Set<String> destino, int quantos) {
        int postos = 0;
        int tentativas = 0;
        while (postos < quantos && tentativas++ < 500) {
            int i = 1 + r.nextInt(n - 2);
            int j = 1 + r.nextInt(n - 2);
            String k = i + "," + j;
            if (!parede[i][j] && !cofres.contains(k) && !recursos.contains(k)) {
                destino.add(k);
                postos++;
            }
        }
    }

    private boolean livre(int i, int j) {
        return i >= 0 && i < n && j >= 0 && j < n && !parede[i][j];
    }

    private boolean visivel(int ox, int oy, int ci, int cj) {
        int dx = Math.abs(ci - ox);
        int dy = Math.abs(cj - oy);
        int sx = ox < ci ? 1 : -1;
        int sy = oy < cj ? 1 : -1;
        int err = dx - dy;
        int cx = ox;
        int cy = oy;
        while (true) {
            boolean extremo = (cx == ox && cy == oy) || (cx == ci && cy == cj);
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

    private Percecao perceber(int meX, int meY, int meHp, int opX, int opY, int opHp) {
        StringBuilder pr = new StringBuilder();
        StringBuilder rc = new StringBuilder();
        StringBuilder cf = new StringBuilder();
        int r = (int) Math.ceil(RAIO_RADAR);
        for (int i = Math.max(0, meX - r); i <= Math.min(n - 1, meX + r); i++) {
            for (int j = Math.max(0, meY - r); j <= Math.min(n - 1, meY + r); j++) {
                if (Math.hypot(i - meX, j - meY) > RAIO_RADAR || !visivel(meX, meY, i, j)) {
                    continue;
                }
                String k = i + "," + j;
                if (parede[i][j]) {
                    juntar(pr, "{\"x\":" + i + ",\"y\":" + j + "}");
                } else if (recursos.contains(k)) {
                    juntar(rc, "{\"x\":" + i + ",\"y\":" + j + "}");
                } else if (cofres.contains(k)) {
                    juntar(cf, "{\"x\":" + i + ",\"y\":" + j + "}");
                }
            }
        }
        String rivais = "";
        if (Math.hypot(opX - meX, opY - meY) <= RAIO_RADAR && visivel(meX, meY, opX, opY)) {
            rivais = "{\"id\":\"OP\",\"x\":" + opX + ",\"y\":" + opY + ",\"hp\":" + opHp + "}";
        }
        String json = "{\"game_started\":true,\"o_meu_estado\":{\"x\":" + meX + ",\"y\":" + meY + ",\"hp\":" + meHp + "},"
                + "\"objetos_fixos\":[" + pr + "],\"recursos_no_mundo\":[" + rc + "],"
                + "\"cofres_no_mundo\":[" + cf + "],\"outros_robots\":[" + rivais + "]}";
        return GSON.fromJson(json, Percecao.class);
    }

    private static void juntar(StringBuilder sb, String item) {
        if (sb.length() > 0) {
            sb.append(',');
        }
        sb.append(item);
    }

    /** Devolve +1 se A vence, -1 se B vence, 0 empate. */
    private int duelo(MotorHeuristico a, MotorHeuristico b) {
        int ax = 2;
        int ay = n / 2;
        int bx = n - 3;
        int by = n / 2;
        int ahp = 200;
        int bhp = 200;
        for (int t = 0; t < MAX_TURNOS; t++) {
            Acao acA = a.decidir(perceber(ax, ay, ahp, bx, by, bhp));
            Acao acB = b.decidir(perceber(bx, by, bhp, ax, ay, ahp));
            Bussola da = Bussola.de(acA);
            Bussola db = Bussola.de(acB);
            int atx = ax + da.getDeltaX();
            int aty = ay + da.getDeltaY();
            int btx = bx + db.getDeltaX();
            int bty = by + db.getDeltaY();

            boolean aAtaca = atx == bx && aty == by;
            boolean bAtaca = btx == ax && bty == ay;
            if (aAtaca) {
                bhp -= 15;
                ahp -= 1;
            }
            if (bAtaca) {
                ahp -= 15;
                bhp -= 1;
            }
            if (!aAtaca && livre(atx, aty) && !(atx == bx && aty == by)) {
                ax = atx;
                ay = aty;
                ahp = Math.min(250, ahp - 1 + ganho(ax, ay, a));
            }
            if (!bAtaca && livre(btx, bty) && !(btx == ax && bty == ay)) {
                bx = btx;
                by = bty;
                bhp = Math.min(250, bhp - 1 + ganho(bx, by, b));
            }
            if (ahp <= 0 && bhp <= 0) {
                return 0;
            }
            if (ahp <= 0) {
                return -1;
            }
            if (bhp <= 0) {
                return 1;
            }
        }
        return Integer.compare(ahp, bhp);
    }

    /** Aplica e consome cura na célula (recurso +20, cofre +100). */
    private int ganho(int x, int y, MotorHeuristico motor) {
        String k = x + "," + y;
        if (recursos.remove(k)) {
            return 20;
        }
        if (cofres.remove(k)) {
            motor.marcarCofreResolvido(new Coordenada(x, y));
            return 100;
        }
        return 0;
    }

    // ---------- Sweep de parâmetros ----------

    /** Corre {@code seeds} duelos de uma config (limiar/margem) contra um agressor puro. */
    public static int[] resultadosContraAgressor(int limiar, int margem, int seeds, int n) {
        int vit = 0;
        int der = 0;
        int emp = 0;
        for (int s = 0; s < seeds; s++) {
            SimuladorDuelo arena = new SimuladorDuelo(s, n);
            MotorHeuristico nosso = MotorHeuristico.comCombate("A", limiar, margem);
            MotorHeuristico agressor = MotorHeuristico.comCombate("B", 0, -1000);
            int r = arena.duelo(nosso, agressor);
            if (r > 0) {
                vit++;
            } else if (r < 0) {
                der++;
            } else {
                emp++;
            }
        }
        return new int[]{vit, der, emp};
    }

    public static void main(String[] args) {
        int seeds = 40;
        int n = 11;
        int[] limiares = {0, 20, 40, 60};
        int[] margens = {-15, 0, 15, 30};

        System.out.printf("%-8s %-8s | %-6s %-6s %-7s | %-6s%n",
                "limiar", "margem", "vit", "der", "empate", "score");
        System.out.println("------------------------------------------------------");
        int melhorDer = Integer.MAX_VALUE;
        int melhorVit = -1;
        String melhor = "";
        for (int limiar : limiares) {
            for (int margem : margens) {
                int[] res = resultadosContraAgressor(limiar, margem, seeds, n);
                int vit = res[0];
                int der = res[1];
                int emp = res[2];
                int score = der; // quanto mais perto de 0, melhor
                System.out.printf("%-8d %-8d | %-6d %-6d %-7d | %-6d%n", limiar, margem, vit, der, emp, score);
                if (der < melhorDer || (der == melhorDer && vit > melhorVit)) {
                    melhorDer = der;
                    melhorVit = vit;
                    melhor = "limiar=" + limiar + ", margem=" + margem;
                }
            }
        }
        System.out.println("------------------------------------------------------");
        System.out.println("MELHOR (score mais perto de 0): " + melhor
                + "  ->  derrotas=" + melhorDer + ", vitorias=" + melhorVit + " em " + seeds + " duelos");
        System.out.println("(Configuracao atual do agente: limiar=40, margem=15)");
    }
}
