package pt.ipt.arena.sim;

import pt.ipt.arena.heuristica.AvaliadorAcao;
import pt.ipt.arena.heuristica.Bussola;
import pt.ipt.arena.heuristica.ContextoDecisao;
import pt.ipt.arena.heuristica.MemoriaHeuristica;
import pt.ipt.arena.modelo.Acao;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Avaliador "mínimo" para representar um colega sem estratégia: escolhe um
 * movimento VÁLIDO ao acaso (só evita paredes). Sem mapa, sem alvos, sem combate.
 * (A semente vem de um contador, para o benchmark ser reprodutível.)
 */
public class AvaliadorAleatorio implements AvaliadorAcao {

    private static final AtomicLong CONTADOR = new AtomicLong(1);
    private final Random rng = new Random(CONTADOR.getAndIncrement());

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (validas.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(validas.get(rng.nextInt(validas.size())).paraAcao());
    }
}
