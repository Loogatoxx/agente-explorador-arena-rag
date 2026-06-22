package pt.ipt.arena;

import com.google.gson.JsonObject;

import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Percecao;
import pt.ipt.arena.rede.ArenaClient;

/**
 * Classe principal (Main) e orquestrador do ciclo de vida do agente.
 *
 * Implementa o ciclo infinito Sense-Think-Act (Perceção, Decisão, Ação),
 * delegando cada responsabilidade às classes especializadas
 * ({@link ArenaClient} para a rede, {@link MotorHeuristico} para a decisão).
 *
 * Princípio fundamental: NÃO concentra lógica de rede, de IA nem de desenho.
 * Evita explicitamente o anti-padrão "Classe Deus".
 */
public class AgenteExplorador {

    /**
     * Pausa anti-flood no fim de cada iteração. O enunciado exige uma folga à
     * rede (Tarpit / Anti-DDOS) e recomenda não descer abaixo dos 350-400 ms,
     * para absorver o jitter do Wi-Fi sem ativar o bloqueio do servidor.
     */
    private static final long PAUSA_ANTI_FLOOD_MS = 400L;

    public static void main(String[] args) {
        // Configuração simples por argumentos (com valores por omissão).
        String sala = (args.length > 0) ? args[0] : "aluno_treino_2026";
        String idRobo = (args.length > 1) ? args[1] : "grupoE_alfa";

        ArenaClient arena = new ArenaClient(sala, idRobo);
        MotorHeuristico motor = new MotorHeuristico(idRobo);

        System.out.println("[Agente] A registar '" + idRobo + "' na sala '" + sala + "'...");
        JsonObject registo = arena.registar();
        System.out.println("[Agente] Resposta do registo: " + registo);

        // Ciclo de vida Sense-Think-Act.
        while (true) {
            try {
                // ---------- SENSE (Perceção) ----------
                Percecao percecao = arena.percecionarTipado();

                if (percecao == null) {
                    // Falha de rede: não rebenta, espera e volta a tentar.
                    System.err.println("[Agente] Sem telemetria (falha de rede?). A tentar de novo...");
                } else if (percecao.isGameOver()) {
                    // Fim de jogo: desliga os motores e encerra.
                    System.out.println("[Agente] game_over recebido. A desligar os motores.");
                    break;
                } else if (!percecao.isGameStarted()) {
                    // Jogo ainda não começou: estado de espera (lobby).
                    System.out.println("[Agente] Em espera no lobby (game_started=false)...");
                } else {
                    // ---------- THINK (Decisão) ----------
                    Acao acao = motor.decidir(percecao);

                    // ---------- ACT (Ação) ----------
                    JsonObject resposta = arena.agir(acao);
                    System.out.println("[Agente] " + percecao + " -> " + acao + " | resposta: " + resposta);
                }
            } catch (RuntimeException e) {
                // Rede de segurança final: um turno mau nunca derruba o agente.
                System.err.println("[Agente] Erro inesperado no ciclo: " + e.getMessage());
            }

            // Folga obrigatória à rede (anti-flood).
            dormir(PAUSA_ANTI_FLOOD_MS);
        }

        System.out.println("[Agente] Terminado.");
    }

    /** Pausa o ciclo, preservando corretamente o estado de interrupção. */
    private static void dormir(long milissegundos) {
        try {
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
