package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;

import java.util.List;
import java.util.Optional;

/**
 * Contrato (padrão Strategy) de cada andar do pipeline de decisão.
 *
 * Permite ordenar os comportamentos por prioridade, testá-los isoladamente e
 * acrescentar novos sem tocar no {@link MotorHeuristico} (princípio Open/Closed).
 *
 * Recebe o contexto do turno, a memória e as direções já validadas por colisão.
 * Devolver {@link Optional#empty()} significa "abstenho-me" — a cadeia passa ao
 * avaliador seguinte.
 */
public interface AvaliadorAcao {

    Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas);
}
