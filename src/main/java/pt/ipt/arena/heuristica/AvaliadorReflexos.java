package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;

import java.util.List;
import java.util.Optional;

/**
 * Primeiro andar do pipeline (prioridade absoluta): drena a fila de ações
 * planeadas (reflexos de fuga/ataque injetados em turnos anteriores).
 *
 * Espreita a próxima ação planeada e REVALIDA-a contra as colisões do turno
 * atual (o mundo pode ter mudado). Se ainda é legal, consome-a e devolve-a sem
 * reprocessar a heurística pesada. Se já é ilegal, descarta o plano obsoleto e
 * abstém-se, deixando a cadeia continuar. Drena no máximo 1 ação por turno
 * (1 ação = 1 chamada a {@code agir}).
 */
public class AvaliadorReflexos implements AvaliadorAcao {

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (!memoria.temAcoesPlaneadas()) {
            return Optional.empty();
        }

        Acao proxima = memoria.espreitarProximaPlaneada();
        if (proxima != null && validas.contains(Bussola.de(proxima))) {
            return Optional.of(memoria.consumirProximaPlaneada());
        }

        // Ação planeada já não é legal neste turno: o plano ficou obsoleto.
        memoria.limparPlano();
        return Optional.empty();
    }
}
