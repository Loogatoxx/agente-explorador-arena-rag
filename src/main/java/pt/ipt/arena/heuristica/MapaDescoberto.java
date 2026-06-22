package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Cofre;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.Percecao;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mapa acumulado do mundo: ao contrário do radar (que só "vê" 4.5 unidades), vai
 * juntando, turno após turno, as paredes, recursos e cofres descobertos. É o que
 * permite ao agente lembrar-se de alvos que já saíram do radar e ao
 * {@link Pathfinder} contornar paredes que já não estão visíveis.
 *
 * SRP: só conhecimento espacial acumulado; não decide nem comunica.
 */
public class MapaDescoberto {

    private final Set<String> paredes = new HashSet<>();
    private final Map<String, Coordenada> recursos = new HashMap<>();
    private final Map<String, Coordenada> cofres = new HashMap<>();

    /** Integra a telemetria de um turno no mapa acumulado. */
    public void integrar(Percecao percecao) {
        for (Coordenada parede : percecao.getObjetosFixos()) {
            if (parede != null) {
                paredes.add(parede.chave());
            }
        }
        for (Coordenada recurso : percecao.getRecursosNoMundo()) {
            if (recurso != null) {
                recursos.put(recurso.chave(), recurso);
            }
        }
        for (Cofre cofre : percecao.getCofresNoMundo()) {
            if (cofre != null) {
                Coordenada pos = cofre.getPosicao();
                cofres.put(pos.chave(), pos);
            }
        }
    }

    /** Marca um recurso como coletado (deixa de ser alvo). */
    public void marcarRecursoColetado(Coordenada c) {
        recursos.remove(c.chave());
    }

    /**
     * Regista uma parede inferida (ex.: um movimento que o servidor bloqueou —
     * inclui os limites do mapa, que não vêm em objetos_fixos).
     */
    public void registarParede(Coordenada c) {
        paredes.add(c.chave());
    }

    /** Remove um cofre do mapa (resolvido ou já tratado). */
    public void removerCofre(Coordenada c) {
        cofres.remove(c.chave());
    }

    public boolean haParede(Coordenada c) {
        return paredes.contains(c.chave());
    }

    public Set<String> paredes() {
        return paredes;
    }

    public Collection<Coordenada> recursos() {
        return recursos.values();
    }

    public Collection<Coordenada> cofres() {
        return cofres.values();
    }
}
