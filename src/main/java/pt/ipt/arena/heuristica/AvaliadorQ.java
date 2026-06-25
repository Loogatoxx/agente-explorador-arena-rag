package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Agente de Q-Learning como avaliador da cadeia de decisão. Em modo TREINO,
 * aprende online enquanto joga partidas simuladas; em modo GREEDY (epsilon=0,
 * sem treino), explora a política aprendida — pronto a entrar no motor entregue.
 *
 * <p><b>Recompensa (afina a "meta" que o bot descobre):</b>
 * <ul>
 *   <li>{@code Δhp} por turno — captura tudo o que importa no simulador: cofre
 *       (+100), recurso (+20), golpe recebido (−15), custo de andar (−1);</li>
 *   <li>{@code +SOBREVIVER} por turno vivo — empurra para durar (colocação);</li>
 *   <li>{@code −MORTE} no fim, se morreu — penaliza a morte com força.</li>
 * </ul>
 * Maximizar a recompensa acumulada ≈ apanhar curas/cofres + evitar dano +
 * sobreviver = exatamente a boa colocação em battle royale. O bot tende a
 * redescobrir sozinho a meta evasiva-aproveitadora.
 */
public class AvaliadorQ implements AvaliadorAcao {

    // ---- Pesos da recompensa (afináveis) ----
    private static final double PESO_HP = 1.0;
    private static final double SOBREVIVER = 0.5;
    private static final double MORTE = 50.0;

    private final TabelaQ tabela;
    private final boolean treino;
    private final Random rng;
    private double epsilon;

    // Estado transitório do episódio atual.
    private String estadoAnterior;
    private int acaoAnterior = -1;
    private int hpAnterior;

    public AvaliadorQ(TabelaQ tabela, double epsilon, boolean treino, long seed) {
        this.tabela = tabela;
        this.epsilon = epsilon;
        this.treino = treino;
        this.rng = new Random(seed);
    }

    /** Reinicia a memória de transição (chamar no início de cada episódio de treino). */
    public void novoEpisodio() {
        estadoAnterior = null;
        acaoAnterior = -1;
    }

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (validas.isEmpty()) {
            return Optional.empty();
        }
        String estado = EstadoQ.codificar(ctx, validas, memoria);
        boolean[] valida = mascara(validas);

        // Aprende com a transição anterior (Bellman), se houver.
        if (treino && estadoAnterior != null) {
            double recompensa = (ctx.meuHp() - hpAnterior) * PESO_HP + SOBREVIVER;
            tabela.atualizar(estadoAnterior, acaoAnterior, recompensa, estado, valida);
        }

        // Escolhe a ação: ε-greedy em treino; greedy puro em exploração.
        int acao;
        if (treino && rng.nextDouble() < epsilon) {
            acao = acaoValidaAleatoria(valida);
        } else {
            acao = tabela.melhorAcao(estado, valida);
            if (acao < 0) {
                acao = acaoValidaAleatoria(valida);
            }
        }

        estadoAnterior = estado;
        acaoAnterior = acao;
        hpAnterior = ctx.meuHp();
        return Optional.of(EstadoQ.ACOES[acao].paraAcao());
    }

    /** Aplica a recompensa terminal ao fim do episódio (chamado pelo treinador). */
    public void fimEpisodio(boolean morreu) {
        if (treino && morreu && estadoAnterior != null && acaoAnterior >= 0) {
            tabela.atualizarTerminal(estadoAnterior, acaoAnterior, -MORTE);
        }
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    private boolean[] mascara(List<Bussola> validas) {
        boolean[] valida = new boolean[EstadoQ.ACOES.length];
        for (int i = 0; i < EstadoQ.ACOES.length; i++) {
            valida[i] = validas.contains(EstadoQ.ACOES[i]);
        }
        return valida;
    }

    private int acaoValidaAleatoria(boolean[] valida) {
        int contagem = 0;
        for (boolean v : valida) {
            if (v) {
                contagem++;
            }
        }
        int escolha = rng.nextInt(Math.max(1, contagem));
        for (int i = 0; i < valida.length; i++) {
            if (valida[i] && escolha-- == 0) {
                return i;
            }
        }
        return 0;
    }
}
