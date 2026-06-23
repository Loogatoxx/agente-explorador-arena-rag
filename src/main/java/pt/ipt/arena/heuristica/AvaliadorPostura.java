package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.RoboRival;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Movimento PROATIVO perante rivais visíveis (não adjacentes), conforme a
 * {@link Postura} da estratégia. Corre depois do {@link AvaliadorBatalha} (que
 * trata o combate adjacente) e antes da atração por recursos/cofres.
 *
 * <ul>
 *   <li>CACAR: se tiver vantagem de HP, traça rota (BFS) até ao rival mais próximo;</li>
 *   <li>EVITAR: se for mais fraco/igual, afasta-se do rival mais próximo;</li>
 *   <li>NEUTRO: abstém-se (segue os objetivos normais).</li>
 * </ul>
 */
public class AvaliadorPostura implements AvaliadorAcao {

    private final Postura postura;

    public AvaliadorPostura(Postura postura) {
        this.postura = postura;
    }

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (postura == Postura.NEUTRO || ctx.rivais().isEmpty() || validas.isEmpty()) {
            return Optional.empty();
        }
        Coordenada pos = ctx.minhaPosicao();
        RoboRival alvo = maisProximo(ctx.rivais(), pos);
        if (alvo == null) {
            return Optional.empty();
        }

        if (postura == Postura.CACAR) {
            if (ctx.meuHp() > alvo.getHp()) {
                Bussola passo = Pathfinder.primeiroPassoPara(
                        pos, Collections.singleton(alvo.getPosicao().chave()), memoria.celulasBloqueadas());
                if (passo != null && validas.contains(passo)) {
                    return Optional.of(passo.paraAcao());
                }
            }
            return Optional.empty();
        }

        // EVITAR: afasta-se proativamente se for mais fraco ou igual.
        if (ctx.meuHp() <= alvo.getHp()) {
            Bussola fuga = maisLonge(pos, alvo.getPosicao(), validas);
            if (fuga != null) {
                return Optional.of(fuga.paraAcao());
            }
        }
        return Optional.empty();
    }

    private RoboRival maisProximo(List<RoboRival> rivais, Coordenada pos) {
        RoboRival melhor = null;
        int melhorDist = Integer.MAX_VALUE;
        for (RoboRival r : rivais) {
            int d = Bussola.manhattan(pos, r.getPosicao());
            if (d < melhorDist) {
                melhorDist = d;
                melhor = r;
            }
        }
        return melhor;
    }

    private Bussola maisLonge(Coordenada pos, Coordenada rival, List<Bussola> validas) {
        Bussola melhor = null;
        int melhorDist = -1;
        for (Bussola dir : validas) {
            int dist = Bussola.manhattan(dir.aplicar(pos), rival);
            if (dist > melhorDist) {
                melhorDist = dist;
                melhor = dir;
            }
        }
        return melhor;
    }
}
