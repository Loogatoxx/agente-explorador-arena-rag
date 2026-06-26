package pt.ipt.arena;

import com.google.gson.JsonObject;

import pt.ipt.arena.heuristica.Bussola;
import pt.ipt.arena.heuristica.ContextoDecisao;
import pt.ipt.arena.heuristica.FiltroColisoes;
import pt.ipt.arena.heuristica.MemoriaHeuristica;
import pt.ipt.arena.heuristica.Pathfinder;
import pt.ipt.arena.ia.OllamaClient;
import pt.ipt.arena.ia.PipelineRag;
import pt.ipt.arena.modelo.Cofre;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.Percecao;
import pt.ipt.arena.rede.ArenaClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Robô de DIAGNÓSTICO da cura (no servidor REAL): queima HP a oscilar (sem tocar
 * em loot) até ~25, depois mede o ganho de HP de um RECURSO e de um COFRE.
 *
 * Objetivo: descobrir se o cofre dá +100 fixo (a 20 HP → 120) ou enche ao máximo
 * (→ 250), e o valor do recurso — para validar a meta do "guardar o cofre para HP
 * baixo". Uso: java ... DiagnosticoCura <SALA> <ID>
 */
public final class DiagnosticoCura {

    private static final int HP_ALVO_QUEIMA = 25;
    private static final long PAUSA_MS = 420L;

    private DiagnosticoCura() {
    }

    enum Fase { QUEIMAR, MEDIR_RECURSO, MEDIR_COFRE, FIM }

    public static void main(String[] args) throws Exception {
        String sala = args.length > 0 ? args[0] : "aluno_treino_2026";
        String id = args.length > 1 ? args[1] : "diagCura";
        ArenaClient arena = new ArenaClient(sala, id);
        MemoriaHeuristica mem = new MemoriaHeuristica();
        FiltroColisoes filtro = new FiltroColisoes();
        PipelineRag pipeline = new PipelineRag(arena, new OllamaClient());

        System.out.println("[Diag] A registar '" + id + "' na sala '" + sala + "'...");
        System.out.println("[Diag] registo: " + arena.registar());
        System.out.println("[Diag] manual indexado: " + pipeline.indexarManual());

        Fase fase = Fase.QUEIMAR;
        Bussola dirQueima = Bussola.ESTE;
        int hpAntes = -1;

        while (fase != Fase.FIM) {
            Percecao p = arena.percecionarTipado();
            if (p == null) {
                dormir();
                continue;
            }
            if (p.isGameOver()) {
                System.out.println("[Diag] game_over.");
                break;
            }
            if (!p.isGameStarted()) {
                System.out.println("[Diag] em espera no lobby...");
                dormir();
                continue;
            }

            Coordenada pos = p.getOMeuEstado().getPosicao();
            int hp = p.getOMeuEstado().getHp();
            mem.getMapa().integrar(p);
            ContextoDecisao ctx = ContextoDecisao.construir(p, mem, id);
            List<Bussola> validas = filtro.direcoesValidas(ctx, mem);
            Set<String> loot = new HashSet<>();
            for (Coordenada c : mem.getMapa().recursos()) {
                loot.add(c.chave());
            }
            for (Coordenada c : mem.getMapa().cofres()) {
                loot.add(c.chave());
            }

            switch (fase) {
                case QUEIMAR:
                    System.out.println("[Diag] QUEIMAR — HP=" + hp + " pos=" + pos.chave()
                            + " (recursos vistos=" + mem.getMapa().recursos().size()
                            + ", cofres=" + mem.getMapa().cofres().size() + ")");
                    if (hp <= HP_ALVO_QUEIMA) {
                        fase = Fase.MEDIR_RECURSO;
                        break;
                    }
                    // Oscila/anda só para casas válidas SEM loot (não curar sem querer).
                    Bussola m = escolherQueima(pos, validas, loot, dirQueima);
                    if (m == null) {
                        // sem casa segura: anda para qualquer válida.
                        m = validas.isEmpty() ? Bussola.ESTE : validas.get(0);
                    }
                    dirQueima = m;
                    arena.agir(m.paraAcao());
                    break;

                case MEDIR_RECURSO:
                    if (mem.getMapa().recursos().isEmpty()) {
                        // ainda não vimos um recurso: continua a explorar (gastando HP).
                        andarPara(arena, pos, mem.celulasBloqueadas(), fronteira(pos, mem, validas), validas);
                        break;
                    }
                    Coordenada rec = maisProximo(pos, mem.getMapa().recursos());
                    if (pos.em(rec.getX(), rec.getY())) {
                        // (não devia acontecer: pisar = apanhar) — passa ao cofre.
                        fase = Fase.MEDIR_COFRE;
                        break;
                    }
                    if (Bussola.manhattan(pos, rec) == 1) {
                        hpAntes = hp; // vamos pisar o recurso AGORA
                        System.out.println("[Diag] >>> a pisar RECURSO com HP=" + hpAntes);
                    }
                    if (hpAntes > 0 && Bussola.manhattan(pos, rec) > 1) {
                        // já pisámos no turno anterior → o HP atual reflete o ganho.
                        System.out.println("[Diag] *** RECURSO: antes=" + hpAntes + " depois=" + hp
                                + "  -> DELTA = " + (hp - hpAntes) + " (1 já é o custo do movimento)");
                        hpAntes = -1;
                        fase = Fase.MEDIR_COFRE;
                        break;
                    }
                    andarPara(arena, pos, mem.celulasBloqueadas(), rec, validas);
                    break;

                case MEDIR_COFRE:
                    Cofre sob = p.getCofreSobOAgente();
                    if (sob != null) {
                        hpAntes = hp;
                        System.out.println("[Diag] >>> SOBRE o cofre com HP=" + hpAntes + " — a resolver via RAG...");
                        JsonObject r = pipeline.tentarDesbloquear(sob);
                        System.out.println("[Diag] unlock: " + r);
                        dormir();
                        Percecao p2 = arena.percecionarTipado();
                        if (p2 != null && p2.getOMeuEstado() != null) {
                            int hpDepois = p2.getOMeuEstado().getHp();
                            System.out.println("[Diag] *** COFRE: antes=" + hpAntes + " depois=" + hpDepois
                                    + "  -> DELTA = " + (hpDepois - hpAntes));
                        }
                        fase = Fase.FIM;
                        break;
                    }
                    if (mem.getMapa().cofres().isEmpty()) {
                        andarPara(arena, pos, mem.celulasBloqueadas(), fronteira(pos, mem, validas), validas);
                        break;
                    }
                    Coordenada cof = maisProximo(pos, mem.getMapa().cofres());
                    andarPara(arena, pos, mem.celulasBloqueadas(), cof, validas);
                    break;

                default:
                    break;
            }
            dormir();
        }
        System.out.println("[Diag] Terminado.");
    }

    /** Direção válida cujo destino NÃO é loot (prefere manter a direção atual). */
    private static Bussola escolherQueima(Coordenada pos, List<Bussola> validas, Set<String> loot, Bussola atual) {
        if (validas.contains(atual) && !loot.contains(atual.aplicar(pos).chave())) {
            return atual;
        }
        for (Bussola d : validas) {
            if (!loot.contains(d.aplicar(pos).chave())) {
                return d;
            }
        }
        return null;
    }

    private static void andarPara(ArenaClient arena, Coordenada pos, Set<String> bloqueadas,
                                  Coordenada alvo, List<Bussola> validas) {
        if (alvo == null) {
            if (!validas.isEmpty()) {
                arena.agir(validas.get(0).paraAcao());
            }
            return;
        }
        Set<String> alvos = new HashSet<>();
        alvos.add(alvo.chave());
        Bussola passo = Pathfinder.primeiroPassoPara(pos, alvos, bloqueadas);
        if (passo == null || !validas.contains(passo)) {
            passo = Bussola.direcaoQueReduzManhattan(pos, alvo, validas);
        }
        if (passo != null) {
            arena.agir(passo.paraAcao());
        } else if (!validas.isEmpty()) {
            arena.agir(validas.get(0).paraAcao());
        }
    }

    private static Coordenada fronteira(Coordenada pos, MemoriaHeuristica mem, List<Bussola> validas) {
        Bussola passo = Pathfinder.primeiroPassoParaFronteira(pos, mem.celulasBloqueadas(), mem.celulasVistas());
        return passo != null ? passo.aplicar(pos) : (validas.isEmpty() ? null : validas.get(0).aplicar(pos));
    }

    private static Coordenada maisProximo(Coordenada pos, java.util.Collection<Coordenada> pontos) {
        Coordenada melhor = null;
        int md = Integer.MAX_VALUE;
        for (Coordenada c : pontos) {
            int d = Bussola.manhattan(pos, c);
            if (d < md) {
                md = d;
                melhor = c;
            }
        }
        return melhor;
    }

    private static void dormir() {
        try {
            Thread.sleep(PAUSA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
