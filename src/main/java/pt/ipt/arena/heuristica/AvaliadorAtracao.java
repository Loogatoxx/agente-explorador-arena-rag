package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Atração magnética. Se houver alvo visível (recurso ou cofre fora da lista
 * negra), escolhe o de menor distância de Manhattan e dá o passo guloso (entre
 * as direções válidas) que mais reduz essa distância — sobrepondo-se ao mapa de
 * calor por estar acima dele na cadeia.
 *
 * Se nenhuma direção válida reduzir a distância (alvo ocluído atrás de uma
 * parede), abstém-se para o mapa de calor contornar, em vez de oscilar contra a
 * parede. Entre passos que reduzem igualmente, desempata pelo destino mais frio
 * (menos visitado).
 */
public class AvaliadorAtracao implements AvaliadorAcao {

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        List<Coordenada> alvos = ctx.alvos();
        if (alvos.isEmpty() || validas.isEmpty()) {
            return Optional.empty();
        }

        Coordenada pos = ctx.minhaPosicao();
        Coordenada alvo = alvoMaisProximo(alvos, pos);
        int distanciaAtual = Bussola.manhattan(pos, alvo);
        if (distanciaAtual == 0) {
            // Já estou em cima do alvo: deixa o pipeline de RAG/coleta agir.
            return Optional.empty();
        }

        // Reúne as direções válidas que MAIS reduzem a distância ao alvo.
        int melhorDist = distanciaAtual;
        List<Bussola> melhores = new ArrayList<>();
        for (Bussola direcao : validas) {
            int dist = Bussola.manhattan(direcao.aplicar(pos), alvo);
            if (dist < melhorDist) {
                melhorDist = dist;
                melhores.clear();
                melhores.add(direcao);
            } else if (dist == melhorDist && !melhores.isEmpty()) {
                melhores.add(direcao);
            }
        }

        if (melhores.isEmpty()) {
            // Alvo ocluído: nenhuma direção válida aproxima. Deixa o mapa de calor contornar.
            return Optional.empty();
        }

        Bussola escolhida = maisFria(melhores, pos, memoria);
        return Optional.of(escolhida.paraAcao());
    }

    private Coordenada alvoMaisProximo(List<Coordenada> alvos, Coordenada pos) {
        Coordenada melhor = null;
        int melhorDist = Integer.MAX_VALUE;
        for (Coordenada alvo : alvos) {
            int dist = Bussola.manhattan(pos, alvo);
            if (dist < melhorDist) {
                melhorDist = dist;
                melhor = alvo;
            }
        }
        return melhor;
    }

    /** Entre as candidatas (já na ordem canónica), a de destino menos visitado. */
    private Bussola maisFria(List<Bussola> candidatas, Coordenada pos, MemoriaHeuristica memoria) {
        Bussola melhor = null;
        int menosVisitas = Integer.MAX_VALUE;
        for (Bussola direcao : candidatas) {
            int visitas = memoria.visitas(direcao.aplicar(pos));
            if (visitas < menosVisitas) {
                menosVisitas = visitas;
                melhor = direcao;
            }
        }
        return melhor;
    }
}
