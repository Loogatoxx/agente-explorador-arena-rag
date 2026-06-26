package pt.ipt.arena.sim;

import com.google.gson.Gson;

import pt.ipt.arena.heuristica.AgenteQ;
import pt.ipt.arena.heuristica.Bussola;
import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.heuristica.Postura;
import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.Percecao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Simulador Battle Royale FREE-FOR-ALL (vários bots em simultâneo na mesma
 * arena) e liga de torneio — apenas benchmark/teste, não faz parte do agente.
 *
 * Cada estratégia (arquétipo) é pontuada pela sua COLOCAÇÃO em cada partida
 * (último vivo = 1.º; quem morre mais tarde fica à frente; empate ao tempo por
 * HP). Média alta + bom pior-caso = estratégia robusta contra todas as outras.
 */
public final class SimuladorBatalha {

    private static final double RAIO = 4.5;
    private static final Gson GSON = new Gson();
    private static final int MAX_TURNOS = 400;

    private final int n;
    private final boolean[][] parede;
    private final Set<String> recursos = new HashSet<>();
    private final Set<String> cofres = new HashSet<>();

    /** Robô numa partida. */
    static final class Bot {
        final String nome;
        final MotorHeuristico motor;
        int x;
        int y;
        int hp = 200;
        boolean vivo = true;
        boolean atacou;
        int morteTurno = Integer.MAX_VALUE;

        Bot(String nome, MotorHeuristico motor, int x, int y) {
            this.nome = nome;
            this.motor = motor;
            this.x = x;
            this.y = y;
        }
    }

    /** Estratégia a testar no torneio. */
    public static final class Arquetipo {
        final String nome;
        final int limiar;
        final int margem;
        final Postura postura;
        final boolean remate;

        public Arquetipo(String nome, int limiar, int margem, Postura postura) {
            this(nome, limiar, margem, postura, false);
        }

        public Arquetipo(String nome, int limiar, int margem, Postura postura, boolean remate) {
            this.nome = nome;
            this.limiar = limiar;
            this.margem = margem;
            this.postura = postura;
            this.remate = remate;
        }

        MotorHeuristico motor() {
            return MotorHeuristico.comEstrategiaCompleta(nome, limiar, margem, postura, remate);
        }
    }

    SimuladorBatalha(long seed, int n, int nRecursos, int nCofres) {
        this.n = n;
        this.parede = new boolean[n][n];
        Random r = new Random(seed);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                parede[i][j] = (i == 0 || j == 0 || i == n - 1 || j == n - 1) || r.nextDouble() < 0.12;
            }
        }
        colocar(r, cofres, nCofres);
        colocar(r, recursos, nRecursos);
    }

    private void colocar(Random r, Set<String> destino, int quantos) {
        int postos = 0;
        int tentativas = 0;
        while (postos < quantos && tentativas++ < 2000) {
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

    private Percecao perceber(Bot me, List<Bot> bots) {
        List<String> pr = new ArrayList<>();
        List<String> rc = new ArrayList<>();
        List<String> cf = new ArrayList<>();
        List<String> rv = new ArrayList<>();
        int r = (int) Math.ceil(RAIO);
        for (int i = Math.max(0, me.x - r); i <= Math.min(n - 1, me.x + r); i++) {
            for (int j = Math.max(0, me.y - r); j <= Math.min(n - 1, me.y + r); j++) {
                if (Math.hypot(i - me.x, j - me.y) > RAIO || !visivel(me.x, me.y, i, j)) {
                    continue;
                }
                String k = i + "," + j;
                if (parede[i][j]) {
                    pr.add(coord(i, j));
                } else if (recursos.contains(k)) {
                    rc.add(coord(i, j));
                } else if (cofres.contains(k)) {
                    cf.add(coord(i, j));
                }
            }
        }
        for (Bot o : bots) {
            if (o != me && o.vivo
                    && Math.hypot(o.x - me.x, o.y - me.y) <= RAIO && visivel(me.x, me.y, o.x, o.y)) {
                rv.add("{\"id\":\"" + o.nome + "\",\"x\":" + o.x + ",\"y\":" + o.y + ",\"hp\":" + o.hp + "}");
            }
        }
        String json = "{\"game_started\":true,\"o_meu_estado\":{\"x\":" + me.x + ",\"y\":" + me.y + ",\"hp\":" + me.hp + "},"
                + "\"objetos_fixos\":[" + String.join(",", pr) + "],\"recursos_no_mundo\":[" + String.join(",", rc) + "],"
                + "\"cofres_no_mundo\":[" + String.join(",", cf) + "],\"outros_robots\":[" + String.join(",", rv) + "]}";
        return GSON.fromJson(json, Percecao.class);
    }

    private static String coord(int i, int j) {
        return "{\"x\":" + i + ",\"y\":" + j + "}";
    }

    /** Corre uma partida e devolve os bots por ordem de classificação (índice 0 = vencedor). */
    List<Bot> jogar(List<Bot> bots) {
        for (int t = 0; t < MAX_TURNOS; t++) {
            Map<Bot, Acao> acoes = new HashMap<>();
            Map<String, Bot> ocupado = new HashMap<>();
            for (Bot b : bots) {
                if (b.vivo) {
                    acoes.put(b, b.motor.decidir(perceber(b, bots)));
                    ocupado.put(b.x + "," + b.y, b);
                    b.atacou = false;
                }
            }
            // Ataques (com base nas posições do início do turno).
            for (Bot b : bots) {
                if (!b.vivo) {
                    continue;
                }
                Bussola dir = Bussola.de(acoes.get(b));
                Bot vitima = ocupado.get((b.x + dir.getDeltaX()) + "," + (b.y + dir.getDeltaY()));
                if (vitima != null && vitima != b && vitima.vivo) {
                    vitima.hp -= 15;
                    b.hp -= 1;
                    b.atacou = true;
                }
            }
            // Movimentos (quem não atacou).
            Set<String> reclamado = new HashSet<>();
            for (Bot b : bots) {
                if (!b.vivo || b.atacou) {
                    continue;
                }
                Bussola dir = Bussola.de(acoes.get(b));
                int nx = b.x + dir.getDeltaX();
                int ny = b.y + dir.getDeltaY();
                String cell = nx + "," + ny;
                if (livre(nx, ny) && !ocupado.containsKey(cell) && !reclamado.contains(cell)) {
                    b.x = nx;
                    b.y = ny;
                    b.hp -= 1;
                    reclamado.add(cell);
                    if (recursos.remove(cell)) {
                        b.hp = Math.min(250, b.hp + 20);
                    }
                    if (cofres.remove(cell)) {
                        b.hp = Math.min(250, b.hp + 100);
                        b.motor.marcarCofreResolvido(new Coordenada(nx, ny));
                    }
                }
            }
            // Mortes.
            int vivos = 0;
            for (Bot b : bots) {
                if (b.vivo && b.hp <= 0) {
                    b.vivo = false;
                    b.morteTurno = t;
                }
                if (b.vivo) {
                    vivos++;
                }
            }
            if (vivos <= 1) {
                break;
            }
        }
        List<Bot> ranking = new ArrayList<>(bots);
        ranking.sort((a, b) -> {
            if (a.vivo != b.vivo) {
                return a.vivo ? -1 : 1;
            }
            if (a.vivo) {
                return Integer.compare(b.hp, a.hp);
            }
            return Integer.compare(b.morteTurno, a.morteTurno);
        });
        return ranking;
    }

    private List<int[]> posicoesIniciais(int k, long seed) {
        List<int[]> livres = new ArrayList<>();
        for (int i = 1; i < n - 1; i++) {
            for (int j = 1; j < n - 1; j++) {
                if (!parede[i][j]) {
                    livres.add(new int[]{i, j});
                }
            }
        }
        List<int[]> escolhidos = new ArrayList<>();
        Random r = new Random(seed * 31 + 7);
        escolhidos.add(livres.get(r.nextInt(livres.size())));
        while (escolhidos.size() < k && escolhidos.size() < livres.size()) {
            int[] melhor = null;
            int melhorMin = -1;
            for (int[] c : livres) {
                int min = Integer.MAX_VALUE;
                for (int[] e : escolhidos) {
                    min = Math.min(min, Math.abs(c[0] - e[0]) + Math.abs(c[1] - e[1]));
                }
                if (min > melhorMin) {
                    melhorMin = min;
                    melhor = c;
                }
            }
            escolhidos.add(melhor);
        }
        return escolhidos;
    }

    /** Liga: cada arquétipo joga em todas as partidas (free-for-all), N seeds. */
    public static void correrLiga(List<Arquetipo> pool, int seeds, int n, int nRecursos, int nCofres) {
        int k = pool.size();
        double[] soma = new double[k];
        double[] somaQuad = new double[k];
        int[] pior = new int[k];
        Arrays.fill(pior, Integer.MAX_VALUE);
        int[] primeiros = new int[k];
        int[] ultimos = new int[k];

        for (int s = 0; s < seeds; s++) {
            SimuladorBatalha arena = new SimuladorBatalha(s, n, nRecursos, nCofres);
            List<int[]> starts = arena.posicoesIniciais(k, s);
            List<Bot> bots = new ArrayList<>();
            for (int a = 0; a < k; a++) {
                int[] st = starts.get(a);
                bots.add(new Bot(pool.get(a).nome, pool.get(a).motor(), st[0], st[1]));
            }
            List<Bot> ranking = arena.jogar(bots);
            for (int place = 0; place < ranking.size(); place++) {
                int idx = bots.indexOf(ranking.get(place));
                int pts = k - 1 - place;
                soma[idx] += pts;
                somaQuad[idx] += (double) pts * pts;
                pior[idx] = Math.min(pior[idx], pts);
                if (place == 0) {
                    primeiros[idx]++;
                }
                if (place == k - 1) {
                    ultimos[idx]++;
                }
            }
        }

        Integer[] ordem = new Integer[k];
        for (int i = 0; i < k; i++) {
            ordem[i] = i;
        }
        Arrays.sort(ordem, (a, b) -> Double.compare(soma[b], soma[a]));
        System.out.printf("%-16s %-7s %-7s %-6s %-7s %-7s%n", "arquetipo", "media", "desvio", "pior", "1os%", "ult%");
        System.out.println("---------------------------------------------------------");
        for (int i : ordem) {
            double media = soma[i] / seeds;
            double desvio = Math.sqrt(Math.max(0, somaQuad[i] / seeds - media * media));
            System.out.printf("%-16s %-7.2f %-7.2f %-6d %-7.0f %-7.0f%n",
                    pool.get(i).nome, media, desvio, pior[i],
                    100.0 * primeiros[i] / seeds, 100.0 * ultimos[i] / seeds);
        }
    }

    /** Conjunto de arquétipos representativos do "campo" de adversários. */
    public static List<Arquetipo> campoPadrao() {
        return List.of(
                new Arquetipo("Agressor", 0, -1000, Postura.CACAR),
                new Arquetipo("Cacador", 0, -15, Postura.CACAR),
                new Arquetipo("Equilibrado", 0, -15, Postura.NEUTRO),
                new Arquetipo("Oportunista", 0, 30, Postura.NEUTRO),
                new Arquetipo("Conservador", 40, 15, Postura.EVITAR),
                new Arquetipo("Cauteloso", 60, 30, Postura.EVITAR),
                new Arquetipo("Pacifista", 99999, 99999, Postura.EVITAR),
                new Arquetipo("CacadorSeletivo", 20, 30, Postura.CACAR));
    }

    /** Avalia um candidato contra um campo fixo: {média de pontos, 1ºs %, últimos %}. */
    public static double[] avaliarCandidato(Arquetipo candidato, List<Arquetipo> campo,
                                            int seeds, int n, int nRecursos, int nCofres) {
        int k = campo.size() + 1;
        double soma = 0;
        int primeiros = 0;
        int ultimos = 0;
        for (int s = 0; s < seeds; s++) {
            SimuladorBatalha arena = new SimuladorBatalha(s, n, nRecursos, nCofres);
            List<int[]> starts = arena.posicoesIniciais(k, s);
            List<Bot> bots = new ArrayList<>();
            bots.add(new Bot(candidato.nome, candidato.motor(), starts.get(0)[0], starts.get(0)[1]));
            for (int a = 0; a < campo.size(); a++) {
                int[] st = starts.get(a + 1);
                bots.add(new Bot(campo.get(a).nome, campo.get(a).motor(), st[0], st[1]));
            }
            int place = arena.jogar(bots).indexOf(bots.get(0));
            soma += (k - 1 - place);
            if (place == 0) {
                primeiros++;
            }
            if (place == k - 1) {
                ultimos++;
            }
        }
        return new double[]{soma / seeds, 100.0 * primeiros / seeds, 100.0 * ultimos / seeds};
    }

    /** Avalia "nosso" contra um campo de adversários arbitrários: {média, 1ºs %, últimos %}. */
    public static double[] avaliarContra(java.util.function.Supplier<MotorHeuristico> nosso,
                                         List<java.util.function.Supplier<MotorHeuristico>> adversarios,
                                         int seeds, int n, int nRecursos, int nCofres) {
        int k = adversarios.size() + 1;
        double soma = 0;
        int primeiros = 0;
        int ultimos = 0;
        for (int s = 0; s < seeds; s++) {
            SimuladorBatalha arena = new SimuladorBatalha(s, n, nRecursos, nCofres);
            List<int[]> starts = arena.posicoesIniciais(k, s);
            List<Bot> bots = new ArrayList<>();
            bots.add(new Bot("NOSSO", nosso.get(), starts.get(0)[0], starts.get(0)[1]));
            for (int a = 0; a < adversarios.size(); a++) {
                int[] st = starts.get(a + 1);
                bots.add(new Bot("adv" + a, adversarios.get(a).get(), st[0], st[1]));
            }
            int place = arena.jogar(bots).indexOf(bots.get(0));
            soma += (k - 1 - place);
            if (place == 0) {
                primeiros++;
            }
            if (place == k - 1) {
                ultimos++;
            }
        }
        return new double[]{soma / seeds, 100.0 * primeiros / seeds, 100.0 * ultimos / seeds};
    }

    /**
     * Versão PARALELA de {@link #avaliarContra}: corre os seeds em paralelo
     * ({@code parallelStream}) usando todos os núcleos. Cada seed cria a sua
     * própria arena/bots (independentes), por isso é seguro; a tabela Q só é lida.
     */
    public static double[] avaliarContraParalelo(java.util.function.Supplier<MotorHeuristico> nosso,
                                                 List<java.util.function.Supplier<MotorHeuristico>> adversarios,
                                                 int seeds, int n, int nRecursos, int nCofres) {
        int k = adversarios.size() + 1;
        int[] colocacoes = java.util.stream.IntStream.range(0, seeds).parallel().map(s -> {
            SimuladorBatalha arena = new SimuladorBatalha(s, n, nRecursos, nCofres);
            List<int[]> starts = arena.posicoesIniciais(k, s);
            List<Bot> bots = new ArrayList<>();
            bots.add(new Bot("NOSSO", nosso.get(), starts.get(0)[0], starts.get(0)[1]));
            for (int a = 0; a < adversarios.size(); a++) {
                int[] st = starts.get(a + 1);
                bots.add(new Bot("adv" + a, adversarios.get(a).get(), st[0], st[1]));
            }
            return arena.jogar(bots).indexOf(bots.get(0));
        }).toArray();
        double soma = 0;
        int primeiros = 0;
        int ultimos = 0;
        for (int place : colocacoes) {
            soma += (k - 1 - place);
            if (place == 0) {
                primeiros++;
            }
            if (place == k - 1) {
                ultimos++;
            }
        }
        return new double[]{soma / seeds, 100.0 * primeiros / seeds, 100.0 * ultimos / seeds};
    }

    /**
     * Diagnóstico: corre UMA partida e devolve o desfecho do NOSSO bot:
     * {@code [colocacao, turnoDeMorte (-1 se sobreviveu), hpFinal, vivosQuandoMorri]}.
     * Serve para perceber PORQUE perdemos (morte cedo em mêlée vs HP baixo no fim).
     */
    public static int[] analisarNosso(java.util.function.Supplier<MotorHeuristico> nosso,
                                      List<java.util.function.Supplier<MotorHeuristico>> adversarios,
                                      long seed, int n, int nRecursos, int nCofres) {
        int k = adversarios.size() + 1;
        SimuladorBatalha arena = new SimuladorBatalha(seed, n, nRecursos, nCofres);
        List<int[]> starts = arena.posicoesIniciais(k, seed);
        List<Bot> bots = new ArrayList<>();
        Bot meu = new Bot("NOSSO", nosso.get(), starts.get(0)[0], starts.get(0)[1]);
        bots.add(meu);
        for (int a = 0; a < adversarios.size(); a++) {
            int[] st = starts.get(a + 1);
            bots.add(new Bot("adv" + a, adversarios.get(a).get(), st[0], st[1]));
        }
        List<Bot> ranking = arena.jogar(bots);
        int colocacao = ranking.indexOf(meu);
        int turnoMorte = meu.vivo ? -1 : meu.morteTurno;
        // Quantos rivais estavam vivos no turno em que morremos (densidade da ameaça).
        int vivosNaMorte = 0;
        if (!meu.vivo) {
            for (Bot b : bots) {
                if (b != meu && b.morteTurno > meu.morteTurno) {
                    vivosNaMorte++;
                }
            }
        }
        return new int[]{colocacao, turnoMorte, meu.hp, vivosNaMorte};
    }

    /**
     * Corre UM episódio de treino de Q-Learning: o agente {@code aq} (1 bot)
     * joga em free-for-all contra o {@code campo}, reutilizando toda a física do
     * simulador. Trata o início/fim de episódio (recompensa terminal) e devolve a
     * colocação do agente (0 = vencedor). Encapsula o acesso às posições iniciais.
     */
    public static int treinarEpisodio(AgenteQ aq, List<java.util.function.Supplier<MotorHeuristico>> campo,
                                      long seed, int n, int nRecursos, int nCofres) {
        SimuladorBatalha arena = new SimuladorBatalha(seed, n, nRecursos, nCofres);
        int k = campo.size() + 1;
        List<int[]> starts = arena.posicoesIniciais(k, seed);
        List<Bot> bots = new ArrayList<>();
        Bot meu = new Bot("Q", MotorHeuristico.comQ(aq), starts.get(0)[0], starts.get(0)[1]);
        bots.add(meu);
        for (int a = 0; a < campo.size(); a++) {
            int[] st = starts.get(a + 1);
            bots.add(new Bot("adv" + a, campo.get(a).get(), st[0], st[1]));
        }
        aq.novoEpisodio();
        List<Bot> ranking = arena.jogar(bots);
        int colocacao = ranking.indexOf(meu);
        aq.fimEpisodio(colocacao, bots.size());
        return colocacao;
    }

    public static void main(String[] args) {
        List<Arquetipo> pool = campoPadrao();
        System.out.println("=== Liga free-for-all (" + pool.size() + " bots, 60 seeds, arena 17x17) ===");
        System.out.println("Pontos por colocacao: 1o lugar = " + (pool.size() - 1) + " ... ultimo = 0");
        correrLiga(pool, 60, 17, 8, 4);
    }
}
