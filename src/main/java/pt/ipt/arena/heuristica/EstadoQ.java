package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.RoboRival;

import java.util.ArrayList;
import java.util.List;

/**
 * Codifica a perceção rica num ESTADO discreto e compacto, para o Q-Learning
 * tabular aprender de forma eficiente (poucos milhares de estados, em vez de um
 * espaço contínuo gigante).
 *
 * O estado é a concatenação de 4 atributos:
 * <ol>
 *   <li><b>Bloqueio adjacente</b> (4 bits, ordem NORTE/ESTE/SUL/OESTE): que
 *       direções estão bloqueadas (parede/ocupada). Ensina a não bater nas paredes.</li>
 *   <li><b>Direção do alvo mais próximo</b> (recurso/cofre): {nenhum, N, E, S, O}.
 *       Ensina a ir buscar curas e cofres.</li>
 *   <li><b>Rival mais próximo</b>: direção + ameaça (mais forte que eu?).
 *       Ensina a fugir dos fortes e, eventualmente, a rematar os fracos.</li>
 *   <li><b>Bucket de HP</b>: {baixo, médio, alto}. Muda o comportamento conforme
 *       a vida (mais cauteloso com pouca vida).</li>
 * </ol>
 *
 * Tamanho do espaço: 16 x 5 x 9 x 3 = 2160 estados x 4 ações ≈ 8640 valores Q.
 */
public final class EstadoQ {

    private EstadoQ() {
    }

    /** Ações do agente Q: as 4 direções na ordem canónica (NORTE, ESTE, SUL, OESTE). */
    public static final Bussola[] ACOES = Bussola.todas();

    /** Devolve a chave de estado discreta para a tabela Q. */
    public static String codificar(ContextoDecisao ctx, List<Bussola> validas, MemoriaHeuristica memoria) {
        Coordenada pos = ctx.minhaPosicao();

        // 1) Bloqueio adjacente (1 bit por direção, na ordem das ACOES).
        int bloqueio = 0;
        for (int i = 0; i < ACOES.length; i++) {
            if (!validas.contains(ACOES[i])) {
                bloqueio |= (1 << i);
            }
        }

        // 2) Direção do alvo (recurso/cofre) mais próximo — radar + memória.
        int dirAlvo = direcao(pos, maisProximo(pos, alvos(ctx, memoria)));

        // 3) Rival mais próximo: direção (1..4) + ameaça (+4 se for mais forte).
        int codRival = codificarRival(ctx, pos);

        // 4) Bucket de HP.
        int hp = ctx.meuHp();
        int bucketHp = hp < 60 ? 0 : (hp <= 150 ? 1 : 2);

        return bloqueio + "|" + dirAlvo + "|" + codRival + "|" + bucketHp;
    }

    /** Junta os alvos visíveis (ctx) com os memorizados (mapa acumulado). */
    private static List<Coordenada> alvos(ContextoDecisao ctx, MemoriaHeuristica memoria) {
        List<Coordenada> todos = new ArrayList<>(ctx.alvos());
        if (memoria != null) {
            todos.addAll(memoria.getMapa().recursos());
            todos.addAll(memoria.getMapa().cofres());
        }
        return todos;
    }

    private static Coordenada maisProximo(Coordenada de, List<Coordenada> pontos) {
        Coordenada melhor = null;
        int melhorDist = Integer.MAX_VALUE;
        for (Coordenada c : pontos) {
            int d = Math.abs(c.getX() - de.getX()) + Math.abs(c.getY() - de.getY());
            if (d > 0 && d < melhorDist) {
                melhorDist = d;
                melhor = c;
            }
        }
        return melhor;
    }

    private static int codificarRival(ContextoDecisao ctx, Coordenada pos) {
        RoboRival maisPerto = null;
        int melhorDist = Integer.MAX_VALUE;
        for (RoboRival r : ctx.rivais()) {
            int d = Math.abs(r.getX() - pos.getX()) + Math.abs(r.getY() - pos.getY());
            if (d < melhorDist) {
                melhorDist = d;
                maisPerto = r;
            }
        }
        if (maisPerto == null) {
            return 0;
        }
        int dir = direcao(pos, new Coordenada(maisPerto.getX(), maisPerto.getY())); // 1..4
        boolean maisForte = maisPerto.getHp() >= ctx.meuHp();
        return dir + (maisForte ? 4 : 0); // 1..4 (mais fraco) ou 5..8 (mais forte)
    }

    /**
     * Direção dominante de {@code pos} até {@code alvo}, no código
     * {0=nenhum, 1=NORTE, 2=ESTE, 3=SUL, 4=OESTE} (alinhado com a ordem das ACOES + 1).
     */
    private static int direcao(Coordenada pos, Coordenada alvo) {
        if (alvo == null) {
            return 0;
        }
        int dx = alvo.getX() - pos.getX();
        int dy = alvo.getY() - pos.getY();
        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx > 0 ? 2 : 4; // ESTE : OESTE
        }
        return dy < 0 ? 1 : 3; // NORTE (y a descer) : SUL
    }
}
