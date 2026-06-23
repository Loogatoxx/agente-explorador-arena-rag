package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.EstadoRobo;
import pt.ipt.arena.modelo.Percecao;
import pt.ipt.arena.modelo.RoboRival;

import java.util.List;
import java.util.Optional;

/**
 * Motor de decisão determinístico (heurística) do agente — fachada FINA que
 * orquestra um pipeline de avaliadores por prioridade
 * (Reflexos → Batalha → Atração → Mapa de Calor) e delega o estado mutável à
 * {@link MemoriaHeuristica}.
 *
 * Responsabilidade única (SRP): não contém aritmética de eixos nem algoritmos —
 * compõe os colaboradores e corre a cadeia. Mantém o {@code AgenteExplorador}
 * fino, evitando a "Classe Deus".
 *
 * Invariante: {@link #decidir(Percecao)} NUNCA devolve {@code null} e NUNCA
 * propaga exceções (um turno mau nunca derruba o agente).
 */
public class MotorHeuristico {

    private final String idRobo;
    private final MemoriaHeuristica memoria;
    private final FiltroColisoes filtroColisoes;
    private final List<AvaliadorAcao> cadeia;

    /** Posição e ação do turno anterior, para aprender com movimentos bloqueados. */
    private Coordenada posicaoAnterior;
    private Acao acaoAnterior;

    /** Wiring por omissão (sem id próprio). */
    public MotorHeuristico() {
        this(null);
    }

    /**
     * Wiring por omissão (OTIMIZADO) com o id do próprio robô. Usa pathfinding:
     * atração a alvos por BFS sobre o mapa acumulado e exploração por fronteiras.
     */
    public MotorHeuristico(String idRobo) {
        // Estratégia "abutre" eleita pelo OTIMIZADOR AUTOMATICO (hill-climbing):
        // postura EVITAR (foge da mêlea) + combate quase só por REMATE (abate quem
        // morre a 1 golpe) + sobrevivência (cura quando o HP baixa). ~4.985/8 vs o campo.
        this(idRobo, new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(new AvaliadorReflexos(),
                        new AvaliadorBatalha(),
                        new AvaliadorSobrevivencia(70),
                        new AvaliadorPostura(Postura.EVITAR),
                        new AvaliadorAlvo(),
                        new AvaliadorExploracao()));
    }

    /**
     * Configuração "clássica" (gulosa: atração de 1 passo por Manhattan + mapa de
     * calor, SEM mapa acumulado nem pathfinding). Mantida para o benchmark A/B
     * que comprova o ganho da versão otimizada.
     */
    public static MotorHeuristico classico(String idRobo) {
        return new MotorHeuristico(idRobo, new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(new AvaliadorReflexos(),
                        new AvaliadorBatalha(),
                        new AvaliadorAtracao(),
                        new AvaliadorMapaCalor()));
    }

    /**
     * Configuração otimizada com parâmetros de combate à medida (limiar de HP e
     * margem de vantagem). Usada pelo benchmark de duelos para procurar a melhor
     * fórmula de luta/fuga.
     */
    public static MotorHeuristico comCombate(String idRobo, int limiarHp, int margem) {
        return new MotorHeuristico(idRobo, new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(new AvaliadorReflexos(),
                        new AvaliadorBatalha(limiarHp, margem),
                        new AvaliadorAlvo(),
                        new AvaliadorExploracao()));
    }

    /**
     * Estratégia completa para o torneio: parâmetros de combate (limiar/margem) +
     * {@link Postura} proativa perante rivais visíveis (caçar/neutro/evitar).
     */
    public static MotorHeuristico comEstrategia(String idRobo, int limiarHp, int margem, Postura postura) {
        return comEstrategiaCompleta(idRobo, limiarHp, margem, postura, false);
    }

    /** Estratégia completa, incluindo o "remate" (abater rivais a 1 golpe). Para o otimizador. */
    public static MotorHeuristico comEstrategiaCompleta(String idRobo, int limiarHp, int margem,
                                                        Postura postura, boolean remate) {
        return new MotorHeuristico(idRobo, new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(new AvaliadorReflexos(),
                        new AvaliadorBatalha(limiarHp, margem, remate),
                        new AvaliadorSobrevivencia(70),
                        new AvaliadorPostura(postura),
                        new AvaliadorAlvo(),
                        new AvaliadorExploracao()));
    }

    /** Construtor de injeção (testes unitários). */
    public MotorHeuristico(String idRobo, MemoriaHeuristica memoria,
                           FiltroColisoes filtroColisoes, List<AvaliadorAcao> cadeia) {
        this.idRobo = idRobo;
        this.memoria = memoria;
        this.filtroColisoes = filtroColisoes;
        this.cadeia = cadeia;
    }

    /**
     * Ponto de entrada único do ciclo Sense-Think-Act. Decide a próxima ação
     * motora a partir da perceção atual.
     */
    public Acao decidir(Percecao percecao) {
        // E0 — Sanidade: sem perceção/estado válidos, sai em segurança sem mexer no estado.
        if (percecao == null || percecao.getOMeuEstado() == null) {
            return registarDecisao(acaoSegura(), null);
        }

        Coordenada posicao = percecao.getOMeuEstado().getPosicao();
        try {
            // E0b — Aprende com o turno anterior: se não nos mexemos, o movimento foi
            //       bloqueado (parede ou limite do mapa) → memoriza essa parede.
            aprenderComMovimentoBloqueado(percecao, posicao);

            // E1 — Mapa de calor (escrita): regista a pisada deste turno (exatamente 1 vez).
            memoria.registarVisita(posicao);

            // E1b — Mapa acumulado: junta paredes/recursos/cofres deste turno e
            //       marca como coletado um recurso que esteja na nossa posição.
            memoria.getMapa().integrar(percecao);
            memoria.getMapa().marcarRecursoColetado(posicao);

            // E2 — Contexto do turno (pré-computado uma vez).
            ContextoDecisao ctx = ContextoDecisao.construir(percecao, memoria, idRobo);

            // E3 — Colisões (uma só vez, partilhadas por toda a cadeia).
            List<Bussola> validas = filtroColisoes.direcoesValidas(ctx, memoria);

            // E5a — Cercado? Tenta destrancar um cerco causado pela própria lista negra.
            if (validas.isEmpty()) {
                memoria.limparListaNegra();
                validas = filtroColisoes.direcoesValidas(ctx, memoria);
            }

            // E4 — Cadeia por prioridade: o primeiro avaliador a decidir ganha.
            for (AvaliadorAcao avaliador : cadeia) {
                Optional<Acao> decisao = avaliador.avaliar(ctx, memoria, validas);
                if (decisao.isPresent()) {
                    return registarDecisao(decisao.get(), posicao);
                }
            }

            // E5b — Sem decisão (cercado de facto): saída segura determinista.
            return registarDecisao(acaoSegura(), posicao);

        } catch (RuntimeException e) {
            System.err.println("[MotorHeuristico] Erro inesperado na decisão: " + e.getMessage());
            return registarDecisao(acaoSegura(), posicao);
        }
    }

    /** Guarda a posição/ação deste turno (para o turno seguinte) e devolve a ação. */
    private Acao registarDecisao(Acao decisao, Coordenada posicao) {
        this.posicaoAnterior = posicao;
        this.acaoAnterior = decisao;
        return decisao;
    }

    /**
     * Se a posição não mudou desde o turno anterior, a ação anterior foi
     * bloqueada: regista a célula-destino como parede. Exceções: cofres (são
     * transponíveis) e células ocupadas por rivais (foi um ataque, não parede).
     */
    private void aprenderComMovimentoBloqueado(Percecao percecao, Coordenada posicao) {
        if (posicaoAnterior == null || acaoAnterior == null || !posicaoAnterior.equals(posicao)) {
            return;
        }
        Coordenada alvo = Bussola.de(acaoAnterior).aplicar(posicao);
        for (Coordenada cofre : memoria.getMapa().cofres()) {
            if (cofre.em(alvo.getX(), alvo.getY())) {
                return;
            }
        }
        for (RoboRival rival : percecao.getOutrosRobots()) {
            if (rival != null && rival.getX() == alvo.getX() && rival.getY() == alvo.getY()) {
                return;
            }
        }
        memoria.getMapa().registarParede(alvo);
    }

    /**
     * Marca um cofre como inacessível (lista negra). Chamado de FORA do ciclo
     * pelo orquestrador RAG quando um enigma falha.
     */
    public void marcarCofreFalhado(Coordenada posicaoCofre) {
        memoria.marcarCofreFalhado(posicaoCofre);
    }

    /** Marca um cofre como resolvido (deixa de ser alvo). Chamado após unlock com sucesso. */
    public void marcarCofreResolvido(Coordenada posicaoCofre) {
        memoria.marcarCofreResolvido(posicaoCofre);
    }

    /** Expõe a memória (ex.: para a UI ler o snapshot do mapa de calor). */
    public MemoriaHeuristica getMemoria() {
        return memoria;
    }

    /** Saída de emergência determinista (primeira direção da ordem canónica). */
    private Acao acaoSegura() {
        return Bussola.todas()[0].paraAcao();
    }
}
