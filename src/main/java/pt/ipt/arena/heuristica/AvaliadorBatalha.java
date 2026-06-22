package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.RoboRival;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (ctx.rivais().isEmpty() || validas.isEmpty()) {
            return Optional.empty();
        }

        Coordenada pos = ctx.minhaPosicao();
        RoboRival alvo = rivalMaisProximo(ctx.rivais(), pos);
        if (alvo == null) {
            return Optional.empty();
        }

        int distancia = Bussola.manhattan(pos, alvo.getPosicao());
        if (distancia >= DISTANCIA_DE_ENGAJAMENTO) {
            return Optional.empty();
        }

        boolean souMaisForte = ctx.meuHp() > alvo.getHp();
        if (souMaisForte) {
            Bussola passo = Bussola.direcaoQueReduzManhattan(pos, alvo.getPosicao(), validas);
            return (passo != null) ? Optional.of(passo.paraAcao()) : Optional.empty();
        }

        // Fuga: maximizar a distância ao rival.
        Bussola fuga = direcaoDeFuga(pos, alvo.getPosicao(), validas);
        if (fuga == null) {
            return Optional.empty();
        }
        // Compromete-se com a fuga: planeia repeti-la no turno seguinte (reflexo).
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
