package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.RoboRival;

import java.util.List;
import java.util.Optional;

/**
 * Fuga ANTI-CERCO. Em vez de só fugir do centro de massa inimigo (que pode levar a
 * um canto/beco e a sermos encurralados), escolhe a direção que combina:
 *   maximizar a distância aos rivais  +  pesoAbertura · (n.º de saídas livres do destino).
 *
 * Assim afastamo-nos do perigo SEM nos metermos numa ratoeira — mantemos sempre
 * vias de escape. Substitui a esquiva simples no combo.
 */
public class AvaliadorAntiCerco implements AvaliadorAcao {

    /** Peso da "abertura" (saídas livres do destino) face à distância ao inimigo. */
    private final double pesoAbertura;

    public AvaliadorAntiCerco() {
        this(1.0);
    }

    public AvaliadorAntiCerco(double pesoAbertura) {
        this.pesoAbertura = pesoAbertura;
    }

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        List<RoboRival> rivais = ctx.rivais();
        if (rivais.isEmpty() || validas.isEmpty()) {
            return Optional.empty();
        }
        double cx = 0;
        double cy = 0;
        for (RoboRival r : rivais) {
            cx += r.getX();
            cy += r.getY();
        }
        cx /= rivais.size();
        cy /= rivais.size();

        Coordenada pos = ctx.minhaPosicao();
        Bussola melhor = null;
        double melhorScore = Double.NEGATIVE_INFINITY;
        for (Bussola d : validas) {
            Coordenada destino = d.aplicar(pos);
            double dist = Math.abs(destino.getX() - cx) + Math.abs(destino.getY() - cy);
            double score = dist + pesoAbertura * abertura(destino, memoria);
            if (score > melhorScore) {
                melhorScore = score;
                melhor = d;
            }
        }
        return melhor == null ? Optional.empty() : Optional.of(melhor.paraAcao());
    }

    /** Número de vizinhos livres (não-parede) do destino — 0..4. Cantos têm poucos. */
    private int abertura(Coordenada c, MemoriaHeuristica memoria) {
        int n = 0;
        for (Bussola d : Bussola.todas()) {
            Coordenada v = d.aplicar(c);
            if (!memoria.getMapa().haParede(v) && !memoria.cofreEstaNaListaNegra(v)) {
                n++;
            }
        }
        return n;
    }
}
