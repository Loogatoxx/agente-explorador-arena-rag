package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Coordenada;

import java.util.ArrayList;
import java.util.List;

/**
 * Regra transversal de colisões, reutilizada por todos os avaliadores e
 * calculada uma só vez por turno.
 *
 * Dada a posição atual, devolve as direções cujo destino NÃO cai numa parede
 * ({@code objetos_fixos}) NEM num cofre da lista negra. Centraliza a regra
 * "parede em (X+1,Y) → remover MOVER_ESTE" através da {@link Bussola}, sem
 * qualquer suposição de eixos no código.
 */
public class FiltroColisoes {

    public List<Bussola> direcoesValidas(ContextoDecisao ctx, MemoriaHeuristica memoria) {
        List<Bussola> validas = new ArrayList<>();
        for (Bussola direcao : Bussola.todas()) {
            if (movimentoValido(ctx, memoria, direcao)) {
                validas.add(direcao);
            }
        }
        return validas;
    }

    public boolean movimentoValido(ContextoDecisao ctx, MemoriaHeuristica memoria, Bussola direcao) {
        Coordenada destino = direcao.aplicar(ctx.minhaPosicao());
        return !ctx.haParede(destino) && !memoria.cofreEstaNaListaNegra(destino);
    }
}
