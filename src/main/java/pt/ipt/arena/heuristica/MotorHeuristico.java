package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.EstadoRobo;
import pt.ipt.arena.modelo.Percecao;

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

    /** Wiring por omissão (sem id próprio). */
    public MotorHeuristico() {
        this(null);
    }

    /** Wiring por omissão com o id do próprio robô (para filtrar o eco em batalha). */
    public MotorHeuristico(String idRobo) {
        this(idRobo, new MemoriaHeuristica(), new FiltroColisoes(),
                List.of(new AvaliadorReflexos(),
                        new AvaliadorBatalha(),
                        new AvaliadorAtracao(),
                        new AvaliadorMapaCalor()));
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
            return acaoSegura();
        }

        try {
            // E1 — Mapa de calor (escrita): regista a pisada deste turno (exatamente 1 vez).
            EstadoRobo estado = percecao.getOMeuEstado();
            Coordenada posicao = estado.getPosicao();
            memoria.registarVisita(posicao);

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
                    return decisao.get();
                }
            }

            // E5b — Sem decisão (cercado de facto): saída segura determinista.
            return acaoSegura();

        } catch (RuntimeException e) {
            System.err.println("[MotorHeuristico] Erro inesperado na decisão: " + e.getMessage());
            return acaoSegura();
        }
    }

    /**
     * Marca um cofre como inacessível (lista negra). Chamado de FORA do ciclo
     * pelo orquestrador RAG quando um enigma falha.
     */
    public void marcarCofreFalhado(Coordenada posicaoCofre) {
        memoria.marcarCofreFalhado(posicaoCofre);
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
