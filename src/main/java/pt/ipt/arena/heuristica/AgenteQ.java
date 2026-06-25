package pt.ipt.arena.heuristica;

/**
 * Contrato comum dos agentes de Q-Learning (puro ou híbrido): além de decidir
 * uma ação (são {@link AvaliadorAcao}), expõem o ciclo de vida do episódio de
 * treino, para o treinador gerir o reinício e a recompensa terminal.
 */
public interface AgenteQ extends AvaliadorAcao {

    /** Reinicia a memória de transição no início de um episódio. */
    void novoEpisodio();

    /**
     * Aplica a recompensa terminal no fim do episódio, proporcional à COLOCAÇÃO
     * final (alinha a aprendizagem com o objetivo real: ganhar a partida).
     *
     * @param colocacao posição final, 0 = vencedor
     * @param total     número de bots na partida
     */
    void fimEpisodio(int colocacao, int total);

    /** Ajusta a taxa de exploração (ε) ao longo do treino. */
    void setEpsilon(double epsilon);
}
