package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.List;
import java.util.Optional;

/**
 * Comportamento por omissão (exploração) e último andar de movimento da cadeia.
 *
 * Entre as direções válidas, escolhe a que leva ao destino "mais frio" (menos
 * visitado no mapa de calor), com desempate determinista pela ordem canónica da
 * {@link Bussola}. Assim o agente evita andar em círculos e tende a explorar
 * zonas novas. Só se abstém se não houver nenhuma direção válida (cercado).
 */
public class AvaliadorMapaCalor implements AvaliadorAcao {

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (validas.isEmpty()) {
            return Optional.empty();
        }

        Coordenada pos = ctx.minhaPosicao();
        Bussola melhor = null;
        int menosVisitas = Integer.MAX_VALUE;
        for (Bussola direcao : validas) {
            int visitas = memoria.visitas(direcao.aplicar(pos));
            if (visitas < menosVisitas) {
                menosVisitas = visitas;
                melhor = direcao;
            }
        }

        return (melhor == null) ? Optional.empty() : Optional.of(melhor.paraAcao());
    }
}
