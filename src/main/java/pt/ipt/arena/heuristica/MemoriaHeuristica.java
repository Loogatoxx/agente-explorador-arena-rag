package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Único dono de TODO o estado mutável que persiste entre turnos do agente:
 * <ul>
 *   <li>{@code historicoVisitas} — mapa de calor (quantas vezes cada coordenada
 *       foi pisada), para não andar em círculos;</li>
 *   <li>{@code cofresFalhados} — lista negra de cofres inacessíveis;</li>
 *   <li>{@code filaAcoesPlaneadas} — fila de ações de reflexo (fuga/ataque).</li>
 * </ul>
 *
 * Expõe operações <em>intencionais</em> (estilo "Tell, Don't Ask") em vez de
 * devolver as coleções mutáveis, para que nenhum colaborador corrompa o estado
 * por baixo. A interface gráfica lê o mapa de calor apenas por
 * {@link #snapshotMapaCalor()} (cópia imutável).
 */
public class MemoriaHeuristica {

    private final Map<String, Integer> historicoVisitas = new HashMap<>();
    private final Set<String> cofresFalhados = new HashSet<>();
    private final Set<String> cofresResolvidos = new HashSet<>();
    private final Deque<Acao> filaAcoesPlaneadas = new ArrayDeque<>();
    private final MapaDescoberto mapa = new MapaDescoberto();

    // ----- Mapa de calor -----

    public void registarVisita(Coordenada c) {
        historicoVisitas.merge(c.chave(), 1, Integer::sum);
    }

    public int visitas(Coordenada c) {
        return historicoVisitas.getOrDefault(c.chave(), 0);
    }

    // ----- Lista negra de cofres -----

    public void marcarCofreFalhado(Coordenada c) {
        cofresFalhados.add(c.chave());
    }

    public boolean cofreEstaNaListaNegra(Coordenada c) {
        return cofresFalhados.contains(c.chave());
    }

    /** Limpa a lista negra (usado para destrancar um cerco causado por ela). */
    public void limparListaNegra() {
        cofresFalhados.clear();
    }

    public void marcarCofreResolvido(Coordenada c) {
        cofresResolvidos.add(c.chave());
        mapa.removerCofre(c);
    }

    public boolean cofreResolvido(Coordenada c) {
        return cofresResolvidos.contains(c.chave());
    }

    // ----- Mapa acumulado e exploração -----

    /** Mapa do mundo acumulado entre turnos (paredes, recursos, cofres vistos). */
    public MapaDescoberto getMapa() {
        return mapa;
    }

    /** Cópia das células já pisadas (chaves "x,y"). */
    public Set<String> celulasVisitadas() {
        return new HashSet<>(historicoVisitas.keySet());
    }

    /** Células intransitáveis para o pathfinding: paredes conhecidas + lista negra. */
    public Set<String> celulasBloqueadas() {
        Set<String> bloqueadas = new HashSet<>(mapa.paredes());
        bloqueadas.addAll(cofresFalhados);
        return bloqueadas;
    }

    // ----- Fila de ações planeadas (reflexos) -----

    public boolean temAcoesPlaneadas() {
        return !filaAcoesPlaneadas.isEmpty();
    }

    /** Espreita a próxima ação planeada sem a remover (para revalidar antes de consumir). */
    public Acao espreitarProximaPlaneada() {
        return filaAcoesPlaneadas.peekFirst();
    }

    /** Remove e devolve a próxima ação planeada (FIFO). */
    public Acao consumirProximaPlaneada() {
        return filaAcoesPlaneadas.pollFirst();
    }

    public void planear(List<Acao> acoes) {
        if (acoes == null) {
            return;
        }
        for (Acao a : acoes) {
            if (a != null) {
                filaAcoesPlaneadas.addLast(a);
            }
        }
    }

    public void limparPlano() {
        filaAcoesPlaneadas.clear();
    }

    // ----- Leitura para a UI -----

    /** Cópia imutável do mapa de calor, apenas para o painel de telemetria. */
    public Map<String, Integer> snapshotMapaCalor() {
        return Collections.unmodifiableMap(new HashMap<>(historicoVisitas));
    }
}
