package pt.ipt.arena;

import com.google.gson.JsonObject;

import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.ia.OllamaClient;
import pt.ipt.arena.ia.PipelineRag;
import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Cofre;
import pt.ipt.arena.modelo.Percecao;
import pt.ipt.arena.rede.ArenaClient;
import pt.ipt.arena.ui.PainelMapaCalor;

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
        OllamaClient ollama = new OllamaClient();
        PipelineRag pipeline = new PipelineRag(arena, ollama);
        PainelMapaCalor painel = PainelMapaCalor.abrirJanela("Telemetria - " + idRobo);

        System.out.println("[Agente] A registar '" + idRobo + "' na sala '" + sala + "'...");
        JsonObject registo = arena.registar();
        System.out.println("[Agente] Resposta do registo: " + registo);

        // Pré-carrega e indexa o manual técnico para o pipeline RAG (best-effort).
        System.out.println("[Agente] A indexar o manual técnico (RAG)...");
        System.out.println("[Agente] Manual indexado: " + pipeline.indexarManual());

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
                    // ---------- Jogo a decorrer: atualiza o dashboard e decide ----------
                    if (painel != null) {
                        painel.atualizar(percecao, motor.getMemoria().snapshotMapaCalor());
                    }

                    Cofre cofre = percecao.getCofreSobOAgente();
                    if (cofre != null && !motor.cofreJaTentado(cofre.getPosicao())) {
                        // RAG: resolve e desbloqueia o cofre. É a ÚNICA chamada ao
                        // servidor neste turno — encadear unlock + movimento sem pausa
                        // dispara o anti-flood do servidor ("Ausência de Thread.sleep()")
                        // e faz o unlock devolver corpo nulo. O agente NÃO fica preso:
                        // como o cofre passa a "já tentado", no turno seguinte cai no
                        // ramo de navegação e sai de cima da casa.
                        resolverCofre(cofre, pipeline, motor);
                    } else {
                        // THINK + ACT: navegação heurística (uma ação por turno).
                        Acao acao = motor.decidir(percecao);
                        JsonObject resposta = arena.agir(acao);
                        System.out.println("[Agente] " + percecao + " -> " + acao + " | resposta: " + resposta);
                    }
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

    /**
     * Trata um cofre sob o agente: garante o manual indexado, resolve o enigma
     * via RAG e submete a chave. Em caso de falha, marca o cofre na lista negra
     * para o motor heurístico forçar uma fuga no turno seguinte.
     */
    private static void resolverCofre(Cofre cofre, PipelineRag pipeline, MotorHeuristico motor) {
        if (!pipeline.estaIndexado()) {
            pipeline.indexarManual();
        }
        System.out.println("[Agente] Cofre sob o agente. A resolver via RAG: " + cofre.getTerminalDesafio());
        JsonObject resposta = pipeline.tentarDesbloquear(cofre);
        System.out.println("[Agente] Resultado do unlock: " + resposta);

        // Marca SEMPRE como tentado: garante que no turno seguinte o agente sai de
        // cima da casa em vez de reabrir o mesmo enigma (vale também para o caso de
        // resposta nula). Não bloqueia a casa — isso só acontece numa falha real.
        motor.marcarCofreTentado(cofre.getPosicao());

        if (resposta == null || !resposta.has("status")) {
            // Corpo nulo: tipicamente anti-flood do servidor (unlock cedo demais).
            // NÃO blacklista — a chave pode estar correta; o cofre fica só "tentado".
            System.out.println("[Agente] Resposta inconclusiva (nula) — provável anti-flood. Cofre não bloqueado.");
            return;
        }
        String status = resposta.get("status").getAsString();
        if ("sucesso".equalsIgnoreCase(status)) {
            motor.marcarCofreResolvido(cofre.getPosicao());
            System.out.println("[Agente] Cofre resolvido com sucesso (+HP).");
        } else {
            // Chave recusada pelo servidor: lista negra para o motor fugir da casa.
            motor.marcarCofreFalhado(cofre.getPosicao());
            System.out.println("[Agente] Cofre marcado na lista negra (status=" + status + ").");
        }
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
