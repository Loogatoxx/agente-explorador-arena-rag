package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.List;

/**
 * Único ponto de verdade sobre a orientação dos eixos da Arena.
 *
 * É o único sítio do projeto que conhece a relação entre uma {@link Acao} e o
 * vetor de deslocamento (dx, dy). Toda a aritmética de coordenadas-destino
 * (colisões, atração, fuga) passa por aqui — nunca há "x+1"/"y-1" à mão
 * espalhado pelo código.
 *
 * INCERTEZA DOS EIXOS: como ainda não há sala ativa, não sabemos se MOVER_NORTE
 * faz o Y descer ou subir. Isso está resolvido num ÚNICO booleano
 * ({@link #NORTE_E_Y_NEGATIVO}). Confirmar a orientação numa sala real =
 * inverter esse booleano, nada mais.
 *
 * A ordem de declaração (NORTE, ESTE, SUL, OESTE) é também a ordem canónica de
 * desempate determinista usada em todo o motor.
 */
public enum Bussola {
    NORTE,
    ESTE,
    SUL,
    OESTE;

    /**
     * Se MOVER_NORTE corresponder a Y a descer (convenção de ecrã: o Y cresce
     * para baixo), manter {@code true}. Caso o primeiro teste real mostre o
     * contrário, basta inverter este único valor.
     */
    private static final boolean NORTE_E_Y_NEGATIVO = true;

    public int getDeltaX() {
        switch (this) {
            case ESTE:
                return +1;
            case OESTE:
                return -1;
            default:
                return 0;
        }
    }

    public int getDeltaY() {
        switch (this) {
            case NORTE:
                return NORTE_E_Y_NEGATIVO ? -1 : +1;
            case SUL:
                return NORTE_E_Y_NEGATIVO ? +1 : -1;
            default:
                return 0;
        }
    }

    /** Coordenada de destino ao aplicar este movimento a partir de {@code origem}. */
    public Coordenada aplicar(Coordenada origem) {
        return new Coordenada(origem.getX() + getDeltaX(), origem.getY() + getDeltaY());
    }

    /** A {@link Acao} (comando do servidor) correspondente a esta direção. */
    public Acao paraAcao() {
        switch (this) {
            case NORTE:
                return Acao.MOVER_NORTE;
            case SUL:
                return Acao.MOVER_SUL;
            case ESTE:
                return Acao.MOVER_ESTE;
            case OESTE:
                return Acao.MOVER_OESTE;
            default:
                throw new IllegalStateException("Bussola desconhecida: " + this);
        }
    }

    public static Bussola de(Acao acao) {
        switch (acao) {
            case MOVER_NORTE:
                return NORTE;
            case MOVER_SUL:
                return SUL;
            case MOVER_ESTE:
                return ESTE;
            case MOVER_OESTE:
                return OESTE;
            default:
                throw new IllegalArgumentException("Acao desconhecida: " + acao);
        }
    }

    /** Direções na ordem canónica de desempate (NORTE, ESTE, SUL, OESTE). */
    public static Bussola[] todas() {
        return values();
    }

    /**
     * Entre as direções {@code validas}, a que mais reduz a distância de
     * Manhattan até {@code alvo} (desempate pela ordem de {@code validas}).
     * Devolve {@code null} se nenhuma reduzir a distância (alvo ocluído).
     */
    public static Bussola direcaoQueReduzManhattan(Coordenada de, Coordenada alvo, List<Bussola> validas) {
        Bussola melhor = null;
        int melhorDist = manhattan(de, alvo);
        for (Bussola d : validas) {
            int dist = manhattan(d.aplicar(de), alvo);
            if (dist < melhorDist) {
                melhorDist = dist;
                melhor = d;
            }
        }
        return melhor;
    }

    /** Distância de Manhattan |dx| + |dy| (invariante ao sinal dos eixos). */
    public static int manhattan(Coordenada a, Coordenada b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}
