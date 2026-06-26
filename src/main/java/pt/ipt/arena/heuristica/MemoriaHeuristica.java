package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.RoboRival;

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
    private final Set<String> cofresTentados = new HashSet<>();
    private final Set<String> celulasVistas = new HashSet<>();
    private final Map<String, Integer> hpRivais = new HashMap<>();
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

    /**
     * Marca um cofre como TENTADO (independentemente do desfecho). Distinto da
     * lista negra: não bloqueia a casa para a navegação, apenas impede reabrir o
     * mesmo enigma em loop. Retira-o ainda do mapa de alvos para o agente não
     * voltar a ser atraído para lá quando a resposta foi inconclusiva.
     */
    public void marcarCofreTentado(Coordenada c) {
        cofresTentados.add(c.chave());
        mapa.removerCofre(c);
    }

    public boolean cofreTentado(Coordenada c) {
        return cofresTentados.contains(c.chave());
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

    /**
     * Marca como VISTAS todas as células num quadrado de Chebyshev de raio
     * {@code raio} à volta do centro (o que o radar cobre ao parar ali). Com
     * {@code raio = 0} marca só a própria célula (equivale a "pisadas").
     *
     * É a base da exploração eficiente: uma célula conta como explorada assim que
     * o radar passa por cima dela, não sendo preciso PISAR cada casa — o agente
     * pode "saltar" ~2*raio+1 casas entre varrimentos sem deixar buracos.
     */
    public void marcarVistasEmRaio(Coordenada centro, int raio) {
        for (int dx = -raio; dx <= raio; dx++) {
            for (int dy = -raio; dy <= raio; dy++) {
                celulasVistas.add(new Coordenada(centro.getX() + dx, centro.getY() + dy).chave());
            }
        }
    }

    /** Cópia das células já vistas pelo radar (chaves "x,y"). */
    public Set<String> celulasVistas() {
        return new HashSet<>(celulasVistas);
    }

    // ----- HP dos rivais conhecidos (para a conservação em PvP) -----

    /** Regista o HP atual de cada rival visível (memoriza o último valor visto). */
    public void atualizarHpRivais(List<RoboRival> rivais) {
        if (rivais == null) {
            return;
        }
        for (RoboRival r : rivais) {
            if (r != null && r.getId() != null) {
                hpRivais.put(r.getId(), r.getHp());
            }
        }
    }

    /** True se já vimos algum rival nesta partida (distingue PvP de modo solo). */
    public boolean viuAlgumRival() {
        return !hpRivais.isEmpty();
    }

    /** Maior HP conhecido entre os rivais já vistos (Integer.MAX_VALUE se nenhum). */
    public int getMaxHpInimigoConhecido() {
        int max = Integer.MIN_VALUE;
        for (int hp : hpRivais.values()) {
            max = Math.max(max, hp);
        }
        return max == Integer.MIN_VALUE ? Integer.MAX_VALUE : max;
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
