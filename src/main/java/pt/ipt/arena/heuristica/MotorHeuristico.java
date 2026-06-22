package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Percecao;

/**
 * Motor de decisão determinístico (heurística) do agente.
 *
 * Responsabilidade única (SRP): a partir da perceção atual, escolher a próxima
 * {@link Acao} motora. Mantém o {@code AgenteExplorador} fino (só orquestra),
 * evitando a "Classe Deus".
 *
 * ESTADO ATUAL: implementação mínima (placeholder) que devolve sempre o mesmo
 * movimento. A lógica real será desenvolvida na etapa de heurística:
 *   - evitar colisões com {@code objetos_fixos} (remover movimentos bloqueados);
 *   - mapa de calor ({@code historicoVisitas}: Map&lt;String,Integer&gt;) para
 *     escolher o caminho "mais frio" e não andar em círculos;
 *   - atração magnética por recursos e cofres próximos;
 *   - fuga/ataque em modo Batalha consoante o Delta de Energia.
 */
public class MotorHeuristico {

    /**
     * Decide a próxima ação com base na perceção. Placeholder até à etapa de
     * heurística.
     *
     * @param percecao telemetria atual (pode ser usada na lógica futura)
     * @return a ação motora escolhida
     */
    public Acao decidir(Percecao percecao) {
        // TODO (etapa: heurística): substituir por decisão real (colisões +
        //   mapa de calor + atração por alvos + luta/fuga).
        return Acao.MOVER_NORTE;
    }
}
