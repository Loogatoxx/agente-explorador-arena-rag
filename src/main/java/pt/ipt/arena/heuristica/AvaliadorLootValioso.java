package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Atração por loot CONSCIENTE DA VALORIZAÇÃO. Medimos ao vivo que um cofre dá
 * +100 fixo e um recurso +20 fixo, ambos com teto em 250. Logo o valor REAL de
 * um loot para nós é {@code min(recompensa, 250 - HP)} — a HP alta, o cofre é
 * quase desperdiçado.
 *
 * Como cada passo custa 1 HP, só vale a pena ir buscar um loot se o ganho cobrir
 * o custo do caminho: {@code valor > distancia * custoPorPasso}. Caso contrário,
 * fica para os avaliadores seguintes (tartaruga/exploração) — não sangramos HP a
 * perseguir uma cura que o teto desperdiça.
 */
public class AvaliadorLootValioso implements AvaliadorAcao {

    private static final int REC_COFRE = 100;
    private static final int REC_RECURSO = 20;
    private static final int TETO = 250;

    /** HP "gasto" exigido por passo para valer a pena (>1 = mais seletivo). */
    private final double custoPorPasso;

    public AvaliadorLootValioso() {
        this(1.0);
    }

    public AvaliadorLootValioso(double custoPorPasso) {
        this.custoPorPasso = custoPorPasso;
    }

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (validas.isEmpty()) {
            return Optional.empty();
        }
        Coordenada pos = ctx.minhaPosicao();
        int hp = ctx.meuHp();

        // Modo SOLO (nunca vimos rival): a missão é abrir TODOS os cofres → persegue
        // o mais próximo de forma gulosa (rápido), sem o gate de valorização (que só
        // faz sentido em PvP, para não sangrar HP a perseguir curas desperdiçadas).
        if (!memoria.viuAlgumRival()) {
            Coordenada alvo = maisProximoLoot(pos, memoria);
            return alvo == null ? Optional.empty() : passoPara(pos, alvo, memoria, validas);
        }

        Coordenada melhor = null;
        double melhorNet = 0;
        // Cofres conhecidos (não na lista negra nem resolvidos).
        for (Coordenada c : memoria.getMapa().cofres()) {
            if (memoria.cofreEstaNaListaNegra(c) || memoria.cofreResolvido(c)) {
                continue;
            }
            melhor = melhorAlvo(pos, hp, c, REC_COFRE, melhor, melhorNet);
            melhorNet = Math.max(melhorNet, net(pos, hp, c, REC_COFRE));
        }
        // Recursos conhecidos.
        for (Coordenada r : memoria.getMapa().recursos()) {
            double n = net(pos, hp, r, REC_RECURSO);
            if (n > melhorNet) {
                melhorNet = n;
                melhor = r;
            }
        }

        if (melhor == null || melhorNet <= 0) {
            return Optional.empty(); // nada vale a pena → deixa campar/explorar
        }
        return passoPara(pos, melhor, memoria, validas);
    }

    /** Primeiro passo (BFS, com recuo guloso) até ao alvo. */
    private Optional<Acao> passoPara(Coordenada pos, Coordenada alvo, MemoriaHeuristica memoria, List<Bussola> validas) {
        Set<String> alvos = new HashSet<>();
        alvos.add(alvo.chave());
        Bussola passo = Pathfinder.primeiroPassoPara(pos, alvos, memoria.celulasBloqueadas());
        if (passo == null || !validas.contains(passo)) {
            passo = Bussola.direcaoQueReduzManhattan(pos, alvo, validas);
        }
        return passo == null ? Optional.empty() : Optional.of(passo.paraAcao());
    }

    /** Loot mais próximo (cofres primeiro — objetivo da missão; senão recursos). */
    private Coordenada maisProximoLoot(Coordenada pos, MemoriaHeuristica memoria) {
        Coordenada melhor = null;
        int md = Integer.MAX_VALUE;
        for (Coordenada c : memoria.getMapa().cofres()) {
            if (memoria.cofreEstaNaListaNegra(c) || memoria.cofreResolvido(c)) {
                continue;
            }
            int d = Bussola.manhattan(pos, c);
            if (d < md) {
                md = d;
                melhor = c;
            }
        }
        if (melhor != null) {
            return melhor;
        }
        for (Coordenada r : memoria.getMapa().recursos()) {
            int d = Bussola.manhattan(pos, r);
            if (d < md) {
                md = d;
                melhor = r;
            }
        }
        return melhor;
    }

    private Coordenada melhorAlvo(Coordenada pos, int hp, Coordenada c, int recompensa,
                                  Coordenada atual, double netAtual) {
        return net(pos, hp, c, recompensa) > netAtual ? c : atual;
    }

    /** Valor líquido em HP de ir buscar este loot: min(recompensa, teto-HP) − dist*custo. */
    private double net(Coordenada pos, int hp, Coordenada loot, int recompensa) {
        int valor = Math.min(recompensa, TETO - hp);
        int dist = Bussola.manhattan(pos, loot);
        return valor - dist * custoPorPasso;
    }
}
