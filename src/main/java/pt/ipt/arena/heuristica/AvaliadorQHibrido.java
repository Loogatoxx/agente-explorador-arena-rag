package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.RoboRival;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Agente de Q-Learning HÍBRIDO: o ML não escolhe o MOVIMENTO, escolhe a TÁTICA
 * (postura) — e a execução do movimento é delegada às nossas regras BFS já
 * afinadas. Junta o melhor dos dois mundos: a navegação ótima do heurístico + a
 * decisão de combate aprendida.
 *
 * <p><b>Ações (4 posturas):</b>
 * <ul>
 *   <li>{@code AGRESSIVIDADE} — vai ao rival mais próximo para o abater;</li>
 *   <li>{@code ESQUIVA} — foge do centro de massa inimigo ({@link AvaliadorEsquiva});</li>
 *   <li>{@code RECUO_CURA} — recua para o cofre/recurso mais próximo via BFS
 *       ({@link AvaliadorAlvo});</li>
 *   <li>{@code IGNORAR} — segue a missão normal (alvos + exploração).</li>
 * </ul>
 *
 * <p><b>Estado (100% combate):</b> patamar de HP × vantagem de HP sobre o rival ×
 * densidade inimiga (1v1 vs mêlée) × há cura conhecida × rival adjacente rematável.
 * (3×3×3×2×2 = 108 estados — converge depressa.)
 *
 * <p><b>Recompensa "abutre":</b> Δhp + sobreviver, com <b>bónus forte por abate</b>
 * (rematar um rival que morre de um golpe) e <b>penalização por apanhar dano numa
 * mêlée (2+ rivais) sem matar</b>.
 */
public class AvaliadorQHibrido implements AgenteQ {

    // Posturas (índice de ação).
    private static final int AGRESSIVIDADE = 0;
    private static final int ESQUIVA = 1;
    private static final int RECUO_CURA = 2;
    private static final int IGNORAR = 3;
    public static final int N_POSTURAS = 4;

    private final TabelaQ tabela;
    private final boolean treino;
    private final Random rng;
    private final RecompensaQ rec;
    private double epsilon;

    // Execução: delega o movimento às regras já afinadas (mesma recolha do combo).
    private final AvaliadorEsquiva esquiva = new AvaliadorEsquiva();
    private final AvaliadorAspirador aspirador = new AvaliadorAspirador();
    private final AvaliadorAlvo alvo = new AvaliadorAlvo();
    private final AvaliadorCanto canto = new AvaliadorCanto();
    private final AvaliadorExploracao exploracao = new AvaliadorExploracao();

    // Estado transitório do episódio.
    private String estadoAnterior;
    private int acaoAnterior = -1;
    private int hpAnterior;
    private boolean agressaoSobreRematavel;
    private int densidadeAnterior;

    public AvaliadorQHibrido(TabelaQ tabela, double epsilon, boolean treino, long seed) {
        this(tabela, epsilon, treino, seed, RecompensaQ.omissao());
    }

    public AvaliadorQHibrido(TabelaQ tabela, double epsilon, boolean treino, long seed, RecompensaQ rec) {
        this.tabela = tabela;
        this.epsilon = epsilon;
        this.treino = treino;
        this.rng = new Random(seed);
        this.rec = rec;
    }

    @Override
    public void novoEpisodio() {
        estadoAnterior = null;
        acaoAnterior = -1;
        agressaoSobreRematavel = false;
        densidadeAnterior = 0;
    }

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (validas.isEmpty()) {
            return Optional.empty();
        }
        String estado = codificarEstado(ctx, memoria);
        boolean[] todas = {true, true, true, true};

        // Aprende com a transição anterior (recompensa abutre).
        if (treino && estadoAnterior != null) {
            double r = (ctx.meuHp() - hpAnterior) * rec.hp + rec.sobreviver;
            boolean abateu = agressaoSobreRematavel; // rematável + golpe = morte certa no simulador
            if (abateu) {
                r += rec.abate;
            } else if (densidadeAnterior >= 2 && ctx.meuHp() < hpAnterior) {
                r -= rec.danoMelee; // apanhou dano numa mêlée sem matar
            }
            tabela.atualizar(estadoAnterior, acaoAnterior, r, estado, todas);
        }

        // Escolhe a POSTURA (ε-greedy em treino; greedy em jogo).
        int postura;
        if (treino && rng.nextDouble() < epsilon) {
            postura = rng.nextInt(N_POSTURAS);
        } else {
            int melhor = tabela.melhorAcao(estado, todas);
            postura = melhor < 0 ? IGNORAR : melhor;
        }

        // Memoriza o contexto para a recompensa do próximo turno.
        agressaoSobreRematavel = postura == AGRESSIVIDADE && haRematavelAdjacente(ctx);
        densidadeAnterior = ctx.rivais().size();
        estadoAnterior = estado;
        acaoAnterior = postura;
        hpAnterior = ctx.meuHp();

        return Optional.of(executar(postura, ctx, memoria, validas));
    }

    @Override
    public void fimEpisodio(int colocacao, int total) {
        if (treino && estadoAnterior != null && acaoAnterior >= 0) {
            double r = rec.colocacao * (total - 1 - colocacao); // 0 (último) .. coloc·(total-1) (1.º)
            tabela.atualizarTerminal(estadoAnterior, acaoAnterior, r);
        }
    }

    @Override
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    // ---------- Execução da postura (delegação ao BFS/regras) ----------

    private Acao executar(int postura, ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        switch (postura) {
            case AGRESSIVIDADE:
                Acao ataque = carregarSobreRival(ctx, validas);
                return ataque != null ? ataque : missao(ctx, memoria, validas);
            case ESQUIVA:
                return esquiva.avaliar(ctx, memoria, validas)
                        .orElseGet(() -> missao(ctx, memoria, validas));
            case RECUO_CURA:
                // Recua a curar-se: o Aspirador prioriza explicitamente as curas
                // (cofre +100 antes de recurso +20); senão BFS ao alvo; senão foge.
                return aspirador.avaliar(ctx, memoria, validas)
                        .orElseGet(() -> alvo.avaliar(ctx, memoria, validas)
                                .orElseGet(() -> esquiva.avaliar(ctx, memoria, validas)
                                        .orElseGet(() -> missao(ctx, memoria, validas))));
            case IGNORAR:
            default:
                return missao(ctx, memoria, validas);
        }
    }

    /** Passo que mais aproxima do rival mais próximo (ao pisá-lo, ataca). */
    private Acao carregarSobreRival(ContextoDecisao ctx, List<Bussola> validas) {
        Coordenada pos = ctx.minhaPosicao();
        RoboRival alvoRival = rivalMaisProximo(ctx, pos);
        if (alvoRival == null) {
            return null;
        }
        Bussola passo = Bussola.direcaoQueReduzManhattan(pos, alvoRival.getPosicao(), validas);
        return passo != null ? passo.paraAcao() : null;
    }

    /**
     * Missão normal = a MESMA recolha do combo (sem as partes de combate):
     * Aspirador (curas) → Alvo (BFS) → Canto → Exploração → 1.ª direção válida.
     * Garante que, fora de combate, o híbrido recolhe tão bem como o combo.
     */
    private Acao missao(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        return aspirador.avaliar(ctx, memoria, validas)
                .orElseGet(() -> alvo.avaliar(ctx, memoria, validas)
                        .orElseGet(() -> canto.avaliar(ctx, memoria, validas)
                                .orElseGet(() -> exploracao.avaliar(ctx, memoria, validas)
                                        .orElse(validas.get(0).paraAcao()))));
    }

    // ---------- Codificação do estado (combate) ----------

    private String codificarEstado(ContextoDecisao ctx, MemoriaHeuristica memoria) {
        int hp = ctx.meuHp();
        int patamarHp = hp < 60 ? 0 : (hp <= 150 ? 1 : 2);

        Coordenada pos = ctx.minhaPosicao();
        RoboRival perto = rivalMaisProximo(ctx, pos);
        int vantagem = perto == null ? 0 : (hp > perto.getHp() ? 1 : 2); // 0=sem rival,1=forte,2=fraco
        int densidade = ctx.rivais().isEmpty() ? 0 : (ctx.rivais().size() == 1 ? 1 : 2);
        // Distância da cura mais próxima: 0=adjacente, 1=perto(≤5), 2=longe/nenhuma.
        int distCura = patamar(distanciaCuraMaisProxima(ctx, memoria), 1, 5);
        // Distância do inimigo: 0=corpo-a-corpo(≤1), 1=perto(≤4), 2=longe/nenhum.
        int distInimigo = perto == null ? 2 : patamar(Bussola.manhattan(pos, perto.getPosicao()), 1, 4);
        // "Posso abatê-lo este turno?" — o rival mais próximo morre de um golpe (≤15 HP).
        int podeAbater = (perto != null && perto.getHp() <= 15) ? 1 : 0;

        return patamarHp + "|" + vantagem + "|" + densidade + "|" + distCura + "|" + distInimigo + "|" + podeAbater;
    }

    /** Distância em 3 patamares: 0=adjacente (≤adj), 1=perto (≤perto), 2=longe/nenhum (-1). */
    private int patamar(int dist, int adj, int perto) {
        if (dist < 0) {
            return 2;
        }
        return dist <= adj ? 0 : (dist <= perto ? 1 : 2);
    }

    /** Distância de Manhattan à cura (recurso/cofre) mais próxima conhecida; -1 se nenhuma. */
    private int distanciaCuraMaisProxima(ContextoDecisao ctx, MemoriaHeuristica memoria) {
        Coordenada pos = ctx.minhaPosicao();
        int melhor = -1;
        for (Coordenada c : ctx.alvos()) {
            melhor = menor(melhor, Bussola.manhattan(pos, c));
        }
        if (memoria != null) {
            for (Coordenada c : memoria.getMapa().recursos()) {
                melhor = menor(melhor, Bussola.manhattan(pos, c));
            }
            for (Coordenada c : memoria.getMapa().cofres()) {
                melhor = menor(melhor, Bussola.manhattan(pos, c));
            }
        }
        return melhor;
    }

    private int menor(int atual, int novo) {
        return (atual < 0 || novo < atual) ? novo : atual;
    }

    private boolean haRematavelAdjacente(ContextoDecisao ctx) {
        Coordenada pos = ctx.minhaPosicao();
        for (RoboRival r : ctx.rivais()) {
            if (r.getHp() <= 15 && Bussola.manhattan(pos, r.getPosicao()) == 1) {
                return true;
            }
        }
        return false;
    }

    private RoboRival rivalMaisProximo(ContextoDecisao ctx, Coordenada pos) {
        RoboRival melhor = null;
        int melhorDist = Integer.MAX_VALUE;
        for (RoboRival r : ctx.rivais()) {
            int d = Bussola.manhattan(pos, r.getPosicao());
            if (d < melhorDist) {
                melhorDist = d;
                melhor = r;
            }
        }
        return melhor;
    }
}
