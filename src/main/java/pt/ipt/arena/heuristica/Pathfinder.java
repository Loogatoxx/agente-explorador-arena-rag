package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Coordenada;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Pesquisa em largura (BFS) sobre o mapa acumulado para devolver o PRIMEIRO
 * passo ({@link Bussola}) do caminho mais curto até ao alvo mais próximo,
 * contornando paredes conhecidas.
 *
 * As células desconhecidas são tratadas como transitáveis (otimismo): permite
 * traçar rota através de zonas ainda por explorar. O número de expansões é
 * limitado para o custo por turno ficar sempre baixo.
 */
public final class Pathfinder {

    private static final int LIMITE_EXPANSAO = 5000;

    private Pathfinder() {
        // Sem instâncias.
    }

    /** Primeiro passo do caminho mais curto até à célula-alvo (chave "x,y") mais próxima. */
    public static Bussola primeiroPassoPara(Coordenada origem, Set<String> alvos, Set<String> bloqueadas) {
        if (alvos == null || alvos.isEmpty()) {
            return null;
        }
        return bfs(origem, bloqueadas, destino -> alvos.contains(destino.chave()));
    }

    /**
     * Primeiro passo até à fronteira mais próxima — a célula transitável mais
     * próxima que ainda NÃO foi visitada (exploração eficiente sem oscilar).
     */
    public static Bussola primeiroPassoParaFronteira(Coordenada origem, Set<String> bloqueadas, Set<String> visitadas) {
        return bfs(origem, bloqueadas, destino -> !visitadas.contains(destino.chave()));
    }

    private static Bussola bfs(Coordenada origem, Set<String> bloqueadas, Predicate<Coordenada> ehAlvo) {
        Set<String> muros = (bloqueadas != null) ? bloqueadas : new HashSet<>();
        Queue<No> fila = new ArrayDeque<>();
        Set<String> vistos = new HashSet<>();
        vistos.add(origem.chave());

        for (Bussola dir : Bussola.todas()) {
            Coordenada vizinho = dir.aplicar(origem);
            String chave = vizinho.chave();
            if (vistos.contains(chave) || muros.contains(chave)) {
                continue;
            }
            vistos.add(chave);
            if (ehAlvo.test(vizinho)) {
                return dir;
            }
            fila.add(new No(vizinho, dir));
        }

        int expansoes = 0;
        while (!fila.isEmpty() && expansoes < LIMITE_EXPANSAO) {
            No atual = fila.poll();
            expansoes++;
            for (Bussola dir : Bussola.todas()) {
                Coordenada vizinho = dir.aplicar(atual.coord);
                String chave = vizinho.chave();
                if (vistos.contains(chave) || muros.contains(chave)) {
                    continue;
                }
                vistos.add(chave);
                if (ehAlvo.test(vizinho)) {
                    return atual.primeiroPasso;
                }
                fila.add(new No(vizinho, atual.primeiroPasso));
            }
        }
        return null;
    }

    /** Nó da BFS que recorda o primeiro passo dado a partir da origem. */
    private static final class No {
        final Coordenada coord;
        final Bussola primeiroPasso;

        No(Coordenada coord, Bussola primeiroPasso) {
            this.coord = coord;
            this.primeiroPasso = primeiroPasso;
        }
    }
}
