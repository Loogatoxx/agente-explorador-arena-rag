package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * "Aspirador de curas" (ladrão de soins): vai SEMPRE buscar curas, mesmo com HP
 * cheio — prioridade aos cofres (+100 HP) e depois aos recursos (+20 HP) — para
 * esvaziar o mapa e esfomear os adversários.
 *
 * SRP: só recolha agressiva de curas (não combate, não explora, não desenha).
 */
public class AvaliadorAspirador implements AvaliadorAcao {

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (validas.isEmpty()) {
            return Optional.empty();
        }
        MapaDescoberto mapa = memoria.getMapa();
        Coordenada pos = ctx.minhaPosicao();
        Set<String> bloqueadas = memoria.celulasBloqueadas();

        // 1) Cofres (+100 HP) primeiro.
        Set<String> cofres = new HashSet<>();
        for (Coordenada cofre : mapa.cofres()) {
            if (!memoria.cofreEstaNaListaNegra(cofre) && !memoria.cofreResolvido(cofre)) {
                cofres.add(cofre.chave());
            }
        }
        Bussola paraCofre = Pathfinder.primeiroPassoPara(pos, cofres, bloqueadas);
        if (paraCofre != null && validas.contains(paraCofre)) {
            return Optional.of(paraCofre.paraAcao());
        }

        // 2) Recursos (+20 HP).
        Set<String> recursos = new HashSet<>();
        for (Coordenada recurso : mapa.recursos()) {
            if (!recurso.em(pos.getX(), pos.getY())) {
                recursos.add(recurso.chave());
            }
        }
        Bussola paraRecurso = Pathfinder.primeiroPassoPara(pos, recursos, bloqueadas);
        if (paraRecurso != null && validas.contains(paraRecurso)) {
            return Optional.of(paraRecurso.paraAcao());
        }

        return Optional.empty();
    }
}
