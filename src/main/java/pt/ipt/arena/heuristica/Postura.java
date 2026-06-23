package pt.ipt.arena.heuristica;

/**
 * Postura do agente perante um rival VISÍVEL mas ainda não adjacente (o combate
 * corpo-a-corpo é tratado pelo {@link AvaliadorBatalha}). Define o "temperamento"
 * estratégico usado no torneio:
 * <ul>
 *   <li>{@link #CACAR} — vai proativamente ao encontro do rival (quando tem vantagem);</li>
 *   <li>{@link #NEUTRO} — ignora até ficar adjacente (foca-se nos objetivos);</li>
 *   <li>{@link #EVITAR} — afasta-se proativamente (quando é mais fraco).</li>
 * </ul>
 */
public enum Postura {
    CACAR,
    NEUTRO,
    EVITAR
}
