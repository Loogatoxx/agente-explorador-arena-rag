package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.List;
import java.util.Optional;

/**
 * Exploração por fronteiras. Com BFS, dá o primeiro passo para a célula
 * transitável NÃO visitada mais próxima — cobre o mapa de forma sistemática, sem
 * oscilar nem andar em círculos, contornando paredes conhecidas.
 *
 * Se já não houver fronteira alcançável (zona toda explorada), recai no mapa de
 * calor (vizinho válido menos visitado) como rede de segurança.
 */
public class AvaliadorExploracao implements AvaliadorAcao {

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (validas.isEmpty()) {
            return Optional.empty();
        }
        Coordenada pos = ctx.minhaPosicao();

        // Fronteira = célula transitável mais próxima ainda NÃO VISTA pelo radar
        // (não apenas "não pisada"). Como cada paragem "vê" um quadrado de raio R,
        // o alvo não-visto mais próximo fica tipicamente a ~R+1 casas → o agente
        // avança aos saltos em vez de varrer linha a linha (cobertura ~ (2R+1)x
        // mais rápida) sem nunca deixar uma zona por inspecionar.
        Bussola passo = Pathfinder.primeiroPassoParaFronteira(
                pos, memoria.celulasBloqueadas(), memoria.celulasVistas());
        if (passo != null && validas.contains(passo)) {
            return Optional.of(passo.paraAcao());
        }

        // Rede de segurança: vizinho válido menos visitado (mapa de calor).
        Bussola maisFria = null;
        int menosVisitas = Integer.MAX_VALUE;
        for (Bussola dir : validas) {
            int visitas = memoria.visitas(dir.aplicar(pos));
            if (visitas < menosVisitas) {
                menosVisitas = visitas;
                maisFria = dir;
            }
        }
        return (maisFria == null) ? Optional.empty() : Optional.of(maisFria.paraAcao());
    }
}
