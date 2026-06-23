package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.RoboRival;

import java.util.List;
import java.util.Optional;

/**
 * Esquiva tática (Vautour 2.0): em vez de fugir só do rival mais próximo, foge
 * do CENTRO DE MASSA (densidade) de todos os rivais visíveis — afasta-se
 * ativamente das zonas de combate em curso, deixando os agressivos entre-matar-se.
 *
 * O "remate" (abater um rival adjacente com <=15 HP) é tratado ANTES, no
 * {@link AvaliadorBatalha}; por isso esta esquiva nunca rouba esse kill grátis.
 *
 * SRP: só evasão da densidade inimiga.
 */
public class AvaliadorEsquiva implements AvaliadorAcao {

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        List<RoboRival> rivais = ctx.rivais();
        if (rivais.isEmpty() || validas.isEmpty()) {
            return Optional.empty();
        }

        double centroX = 0;
        double centroY = 0;
        for (RoboRival rival : rivais) {
            centroX += rival.getX();
            centroY += rival.getY();
        }
        centroX /= rivais.size();
        centroY /= rivais.size();

        Coordenada pos = ctx.minhaPosicao();
        Bussola melhor = null;
        double melhorDist = -1;
        for (Bussola dir : validas) {
            Coordenada destino = dir.aplicar(pos);
            double dist = Math.abs(destino.getX() - centroX) + Math.abs(destino.getY() - centroY);
            if (dist > melhorDist) {
                melhorDist = dist;
                melhor = dir;
            }
        }
        return melhor != null ? Optional.of(melhor.paraAcao()) : Optional.empty();
    }
}
