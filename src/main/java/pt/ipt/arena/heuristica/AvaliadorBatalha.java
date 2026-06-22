package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.RoboRival;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Modo Batalha (Battle Royale). Deteta o rival mais próximo; se estiver a menos
 * de 2 blocos (Manhattan), decide "Luta ou Fuga" pelo Delta de Energia:
 * <ul>
 *   <li>Mais forte (HP estritamente maior) → passo guloso de aproximação à
 *       coordenada do rival (o combate ativa-se ao pisá-la).</li>
 *   <li>Mais fraco ou empatado → foge (direção válida que maximiza a distância)
 *       e planeia uma ação de fuga adicional para o turno seguinte.</li>
 * </ul>
 * Em caso de empate de energia trata-se como "não mais forte" (foge), para
 * evitar oscilação ataque/fuga entre turnos. Só muta estado via
 * {@link MemoriaHeuristica#planear}.
 */
public class AvaliadorBatalha implements AvaliadorAcao {

    /** "A menos de 2 blocos" → distância de Manhattan estritamente menor que 2. */
    private static final int DISTANCIA_DE_ENGAJAMENTO = 2;

    /** Não engaja em combate abaixo deste HP, mesmo contra um rival mais fraco. */
    private static final int LIMIAR_MINIMO_COMBATE = 40;

    /** Margem de vantagem exigida para atacar (um golpe vale 15 de dano). */
    private static final int MARGEM_VANTAGEM = 15;

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (ctx.rivais().isEmpty() || validas.isEmpty()) {
            return Optional.empty();
        }

        Coordenada pos = ctx.minhaPosicao();
        RoboRival alvo = rivalMaisProximo(ctx.rivais(), pos);
        if (alvo == null || Bussola.manhattan(pos, alvo.getPosicao()) >= DISTANCIA_DE_ENGAJAMENTO) {
            return Optional.empty();
        }

        // Ataca só com vantagem CLARA (HP acima do limiar e bem acima do rival).
        boolean vantagemClara = ctx.meuHp() >= LIMIAR_MINIMO_COMBATE
                && ctx.meuHp() > alvo.getHp() + MARGEM_VANTAGEM;
        if (vantagemClara) {
            Bussola passo = Bussola.direcaoQueReduzManhattan(pos, alvo.getPosicao(), validas);
            if (passo != null) {
                return Optional.of(passo.paraAcao());
            }
        }
        return fugir(memoria, validas, pos, alvo.getPosicao());
    }

    /**
     * Foge do rival. Se conhecer um cofre alcançável e o caminho para lá NÃO nos
     * aproximar do rival, retira-se a curar (o cofre dá +100 HP); caso contrário,
     * foge na direção que mais aumenta a distância. Compromete-se com 1 reflexo.
     */
    private Optional<Acao> fugir(MemoriaHeuristica memoria, List<Bussola> validas, Coordenada pos, Coordenada rival) {
        Set<String> cofres = new HashSet<>();
        for (Coordenada cofre : memoria.getMapa().cofres()) {
            if (!memoria.cofreEstaNaListaNegra(cofre) && !memoria.cofreResolvido(cofre)) {
                cofres.add(cofre.chave());
            }
        }
        if (!cofres.isEmpty()) {
            Bussola paraCura = Pathfinder.primeiroPassoPara(pos, cofres, memoria.celulasBloqueadas());
            if (paraCura != null && validas.contains(paraCura)
                    && Bussola.manhattan(paraCura.aplicar(pos), rival) >= Bussola.manhattan(pos, rival)) {
                memoria.planear(List.of(paraCura.paraAcao()));
                return Optional.of(paraCura.paraAcao());
            }
        }

        Bussola fuga = direcaoDeFuga(pos, rival, validas);
        if (fuga == null) {
            return Optional.empty();
        }
        memoria.planear(List.of(fuga.paraAcao()));
        return Optional.of(fuga.paraAcao());
    }

    /** Rival mais próximo; desempate por menor HP e depois por ordem de id. */
    private RoboRival rivalMaisProximo(List<RoboRival> rivais, Coordenada pos) {
        RoboRival melhor = null;
        int melhorDist = Integer.MAX_VALUE;
        int melhorHp = Integer.MAX_VALUE;
        String melhorId = null;
        for (RoboRival rival : rivais) {
            int dist = Bussola.manhattan(pos, rival.getPosicao());
            boolean melhorEscolha = dist < melhorDist
                    || (dist == melhorDist && rival.getHp() < melhorHp)
                    || (dist == melhorDist && rival.getHp() == melhorHp
                        && compararId(rival.getId(), melhorId) < 0);
            if (melhorEscolha) {
                melhor = rival;
                melhorDist = dist;
                melhorHp = rival.getHp();
                melhorId = rival.getId();
            }
        }
        return melhor;
    }

    private static int compararId(String a, String b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        return a.compareTo(b);
    }

    /** Direção válida que mais AFASTA do rival (desempate pela ordem canónica). */
    private Bussola direcaoDeFuga(Coordenada pos, Coordenada rival, List<Bussola> validas) {
        Bussola melhor = null;
        int melhorDist = -1;
        for (Bussola direcao : validas) {
            int dist = Bussola.manhattan(direcao.aplicar(pos), rival);
            if (dist > melhorDist) {
                melhorDist = dist;
                melhor = direcao;
            }
        }
        return melhor;
    }
}
