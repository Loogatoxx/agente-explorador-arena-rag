package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Atração por alvos com PATHFINDING. A partir do mapa acumulado (recursos e
 * cofres já vistos, mesmo que já tenham saído do radar), traça com BFS o caminho
 * mais curto até ao alvo alcançável mais próximo, contornando paredes
 * conhecidas, e devolve o primeiro passo desse caminho.
 *
 * Vantagem sobre a atração gulosa: lembra-se de alvos fora do radar e contorna
 * mesmo obstáculos que escondem o alvo (não fica preso contra a parede).
 */
public class AvaliadorAlvo implements AvaliadorAcao {

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (validas.isEmpty()) {
            return Optional.empty();
        }
        MapaDescoberto mapa = memoria.getMapa();
        Coordenada pos = ctx.minhaPosicao();

        Set<String> alvos = new HashSet<>();
        for (Coordenada recurso : mapa.recursos()) {
            if (!recurso.em(pos.getX(), pos.getY())) {
                alvos.add(recurso.chave());
            }
        }
        for (Coordenada cofre : mapa.cofres()) {
            if (!memoria.cofreEstaNaListaNegra(cofre) && !memoria.cofreResolvido(cofre)) {
                alvos.add(cofre.chave());
            }
        }
        if (alvos.isEmpty()) {
            return Optional.empty();
        }

        Bussola passo = Pathfinder.primeiroPassoPara(pos, alvos, memoria.celulasBloqueadas());
        if (passo == null || !validas.contains(passo)) {
            return Optional.empty();
        }
        return Optional.of(passo.paraAcao());
    }
}
