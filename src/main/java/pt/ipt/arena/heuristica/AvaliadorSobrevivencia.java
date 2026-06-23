package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Gestão de sobrevivência: quando o HP fica abaixo de um limiar, deixa tudo e
 * traça rota (BFS) para a cura conhecida mais próxima — preferindo cofres
 * (+100 HP) e, na falta, recursos (+20 HP). Evita a "morte térmica" por
 * esgotamento durante explorações longas.
 *
 * Corre logo após o combate adjacente e antes da exploração normal, para que a
 * sobrevivência tenha prioridade sobre apanhar alvos distantes ou explorar.
 */
public class AvaliadorSobrevivencia implements AvaliadorAcao {

    private final int limiarHp;

    public AvaliadorSobrevivencia(int limiarHp) {
        this.limiarHp = limiarHp;
    }

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (ctx.meuHp() >= limiarHp || validas.isEmpty()) {
            return Optional.empty();
        }
        MapaDescoberto mapa = memoria.getMapa();
        Coordenada pos = ctx.minhaPosicao();

        // Curas conhecidas: cofres (preferidos, +100) e recursos (+20).
        Set<String> curas = new HashSet<>();
        for (Coordenada cofre : mapa.cofres()) {
            if (!memoria.cofreEstaNaListaNegra(cofre) && !memoria.cofreResolvido(cofre)) {
                curas.add(cofre.chave());
            }
        }
        for (Coordenada recurso : mapa.recursos()) {
            if (!recurso.em(pos.getX(), pos.getY())) {
                curas.add(recurso.chave());
            }
        }
        if (curas.isEmpty()) {
            return Optional.empty();
        }

        Bussola passo = Pathfinder.primeiroPassoPara(pos, curas, memoria.celulasBloqueadas());
        if (passo == null || !validas.contains(passo)) {
            return Optional.empty();
        }
        return Optional.of(passo.paraAcao());
    }
}
