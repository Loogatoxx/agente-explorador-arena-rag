package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Campeiro dos cantos: na ausência de cofres a abrir e de curas a roubar, dirige-se
 * a um canto/limite do mapa (estimado a partir das paredes já descobertas) para
 * reduzir os ângulos de ataque e deixar os bots agressivos entre-matar-se no centro.
 *
 * Deve correr DEPOIS dos avaliadores de objetivo/cura (só age quando não há nada
 * melhor a fazer). SRP: só posicionamento defensivo nos cantos.
 */
public class AvaliadorCanto implements AvaliadorAcao {

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        // Só faz sentido camuflar-se nos cantos quando HÁ rivais (modo Batalha).
        // Em Missão (sem rivais) iria para um canto em vez de explorar e procurar
        // os cofres — por isso abstém-se.
        if (validas.isEmpty() || ctx.rivais().isEmpty()) {
            return Optional.empty();
        }
        Set<String> paredes = memoria.getMapa().paredes();
        if (paredes.isEmpty()) {
            return Optional.empty();
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (String chave : paredes) {
            int virgula = chave.indexOf(',');
            int x = Integer.parseInt(chave.substring(0, virgula));
            int y = Integer.parseInt(chave.substring(virgula + 1));
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }
        if (maxX - minX < 2 || maxY - minY < 2) {
            return Optional.empty(); // ainda não há mapa suficiente
        }

        // Os 4 cantos, logo dentro da borda conhecida.
        Set<String> cantos = new HashSet<>();
        cantos.add((minX + 1) + "," + (minY + 1));
        cantos.add((maxX - 1) + "," + (minY + 1));
        cantos.add((minX + 1) + "," + (maxY - 1));
        cantos.add((maxX - 1) + "," + (maxY - 1));

        Bussola passo = Pathfinder.primeiroPassoPara(ctx.minhaPosicao(), cantos, memoria.celulasBloqueadas());
        if (passo != null && validas.contains(passo)) {
            return Optional.of(passo.paraAcao());
        }
        return Optional.empty();
    }
}
