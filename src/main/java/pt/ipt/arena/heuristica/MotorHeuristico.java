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

    /**
     * Raio (em casas, distância de Chebyshev) que o radar cobre ao parar numa
     * célula. Medido ao vivo no servidor real (~3-4 casas; o simulador usa 4.5
     * euclidiano). Valor por omissão escolhido por benchmark de navegação
     * (ver docs/Relatorio_Testes_Navegacao.md): melhor compromisso
     * velocidade/cobertura sem falhar cofres.
     */
    public static final int DEFAULT_RAIO_VISAO = 3;

    private final String idRobo;
    private final MemoriaHeuristica memoria;
    private final FiltroColisoes filtroColisoes;
    private final List<AvaliadorAcao> cadeia;
    private final int raioVisao;

    /**
     * Margem de conservação por omissão. NEGATIVA (-150) = estratégia "tartaruga":
     * em PvP, sem rival no radar e sem loot conhecido por apanhar, fica parado a
     * conservar HP MESMO um pouco atrás — andar à toa só perde a vida, e deixar os
     * agressivos entre-matarem-se ganha. Escolhido por benchmark (6.2 → ~6.8/8,
     * 70% de 1.ºs lugares). Acima de 150 HP atrás do líder, volta a procurar loot.
     */
    public static final int MARGEM_CONSERVACAO = -150;

    /** Cadeia "abutre completo" — a configuração otimizada por omissão. */
    private static List<AvaliadorAcao> cadeiaOtimizada() {
        return cadeiaOtimizada(MARGEM_CONSERVACAO);
    }

    /**
     * Custo de HP por passo exigido para o loot valer a pena (gate de valorização,
     * só em PvP). Escolhido por benchmark: battle royale 6.7 → ~6.9/8 (+2-3% de 1.ºs)
     * sem perder a navegação solo (30/30).
     */
    public static final double CUSTO_LOOT = 2.0;

    /** Igual, mas com margem de conservação configurável (para benchmark de variantes). */
    private static List<AvaliadorAcao> cadeiaOtimizada(int margemConservacao) {
        return List.of(new AvaliadorReflexos(),
                new AvaliadorBatalha(),
                new AvaliadorLootValioso(CUSTO_LOOT),
                new AvaliadorEsquiva(),
                new AvaliadorConservacao(margemConservacao),
                new AvaliadorCanto(),
                new AvaliadorExploracao());
    }

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
        // Estratégia "abutre completo" (a melhor medida em simulação, ~6.0/8 vs o
        // campo): combate por REMATE + ASPIRADOR de curas (esfomeia os rivais) +
        // ESQUIVA tática da densidade inimiga + CAMPEIRO dos cantos como recurso.
        this(idRobo, new MemoriaHeuristica(), new FiltroColisoes(), cadeiaOtimizada());
    }

    /**
     * Igual à configuração otimizada por omissão, mas com um raio de visão
     * explícito — usado pelo benchmark de navegação para afinar o passo de
     * exploração (saltos do varrimento).
     */
    /** Combo com uma margem de conservação à medida (benchmark de variantes). */
    public static MotorHeuristico comConservacaoMargem(int margem) {
        return new MotorHeuristico("combo", new MemoriaHeuristica(), new FiltroColisoes(),
                cadeiaOtimizada(margem));
    }

    /**
     * Combo com atração de loot CONSCIENTE DA VALORIZAÇÃO (substitui Aspirador+Alvo
     * por {@link AvaliadorLootValioso}). {@code custo} = HP exigido por passo para
     * valer a pena ir buscar um loot. Benchmark do "cofre segundo o seu valor".
     */
    public static MotorHeuristico comLootValioso(double custo) {
        return new MotorHeuristico("combo", new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(new AvaliadorReflexos(),
                        new AvaliadorBatalha(),
                        new AvaliadorLootValioso(custo),
                        new AvaliadorEsquiva(),
                        new AvaliadorConservacao(MARGEM_CONSERVACAO),
                        new AvaliadorCanto(),
                        new AvaliadorExploracao()));
    }

    /**
     * Combo com margem de COMBATE e de CONSERVAÇÃO à medida (benchmark). Margem de
     * combate mais baixa = ataca/ripostar mais cedo (converte a vantagem de HP da
     * tartaruga em abates quando finalmente é engajado).
     */
    public static MotorHeuristico comCombateEConservacao(int margemBatalha, int margemConservacao) {
        return new MotorHeuristico("combo", new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(new AvaliadorReflexos(),
                        new AvaliadorBatalha(0, margemBatalha, true),
                        new AvaliadorAspirador(),
                        new AvaliadorEsquiva(),
                        new AvaliadorAlvo(),
                        new AvaliadorConservacao(margemConservacao),
                        new AvaliadorCanto(),
                        new AvaliadorExploracao()));
    }

    public static MotorHeuristico comExploracaoRaio(String idRobo, int raioVisao) {
        return new MotorHeuristico(idRobo, new MemoriaHeuristica(), new FiltroColisoes(),
                cadeiaOtimizada(), raioVisao);
    }

    /**
     * Motor de Machine Learning PURO: a política inteira é decidida por um agente
     * de Q-Learning ({@link AvaliadorQ}). A cadeia tem só o agente Q — sem regras
     * heurísticas — para medir honestamente o que a aprendizagem descobre.
     */
    public static MotorHeuristico comQ(AgenteQ agenteQ) {
        return new MotorHeuristico("Q", new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(agenteQ));
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

    /** Construtor de injeção (testes unitários) — raio de visão por omissão. */
    public MotorHeuristico(String idRobo, MemoriaHeuristica memoria,
                           FiltroColisoes filtroColisoes, List<AvaliadorAcao> cadeia) {
        this(idRobo, memoria, filtroColisoes, cadeia, DEFAULT_RAIO_VISAO);
    }

    /** Construtor de injeção completo (permite escolher o raio de visão). */
    public MotorHeuristico(String idRobo, MemoriaHeuristica memoria, FiltroColisoes filtroColisoes,
                           List<AvaliadorAcao> cadeia, int raioVisao) {
        this.idRobo = idRobo;
        this.memoria = memoria;
        this.filtroColisoes = filtroColisoes;
        this.cadeia = cadeia;
        this.raioVisao = Math.max(0, raioVisao);
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

            // E1a — Cobertura do radar: marca como VISTAS as células à volta (raio
            //        de visão). É o que torna a exploração eficiente (saltos).
            memoria.marcarVistasEmRaio(posicao, raioVisao);

            // E1b — Mapa acumulado: junta paredes/recursos/cofres deste turno e
            //       marca como coletado um recurso que esteja na nossa posição.
            memoria.getMapa().integrar(percecao);
            memoria.getMapa().marcarRecursoColetado(posicao);

            // E2 — Contexto do turno (pré-computado uma vez).
            ContextoDecisao ctx = ContextoDecisao.construir(percecao, memoria, idRobo);

            // E2b — Memoriza o HP dos rivais vistos (alimenta a conservação em PvP
            //        e marca a partida como combate, não modo solo).
            memoria.atualizarHpRivais(ctx.rivais());

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

    /** Marca um cofre como tentado (qualquer desfecho), para não reabrir o enigma em loop. */
    public void marcarCofreTentado(Coordenada posicaoCofre) {
        memoria.marcarCofreTentado(posicaoCofre);
    }

    /**
     * Indica se o cofre nesta posição já foi tentado — resolvido com sucesso,
     * marcado na lista negra OU simplesmente já abordado uma vez (mesmo com
     * resposta inconclusiva). O orquestrador usa isto para NÃO reabrir o mesmo
     * enigma em loop quando o agente permanece sobre a casa do cofre.
     */
    public boolean cofreJaTentado(Coordenada posicaoCofre) {
        return memoria.cofreResolvido(posicaoCofre)
                || memoria.cofreEstaNaListaNegra(posicaoCofre)
                || memoria.cofreTentado(posicaoCofre);
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
