package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Cofre;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.EstadoRobo;
import pt.ipt.arena.modelo.Percecao;
import pt.ipt.arena.modelo.RoboRival;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Objeto-parâmetro IMUTÁVEL com tudo o que os avaliadores precisam num turno,
 * pré-computado uma só vez a partir da {@link Percecao} (evita varrer o JSON
 * repetidamente em cada andar do pipeline):
 * posição e HP atuais, conjunto de chaves de paredes (lookup O(1)), alvos
 * (recursos + cofres não listados na lista negra) e rivais (sem o eco do
 * próprio agente).
 */
public class ContextoDecisao {

    private final Coordenada minhaPosicao;
    private final int meuHp;
    private final Set<String> chavesParedes;
    private final List<Coordenada> alvos;
    private final List<RoboRival> rivais;

    private ContextoDecisao(Coordenada minhaPosicao, int meuHp, Set<String> chavesParedes,
                            List<Coordenada> alvos, List<RoboRival> rivais) {
        this.minhaPosicao = minhaPosicao;
        this.meuHp = meuHp;
        this.chavesParedes = chavesParedes;
        this.alvos = alvos;
        this.rivais = rivais;
    }

    /**
     * Constrói o contexto a partir da perceção. A {@code memoria} é usada para
     * excluir dos alvos os cofres já na lista negra. {@code idProprio} pode ser
     * {@code null} (nesse caso o eco do próprio agente é filtrado só por posição).
     */
    public static ContextoDecisao construir(Percecao p, MemoriaHeuristica memoria, String idProprio) {
        EstadoRobo estado = p.getOMeuEstado();
        Coordenada pos = estado.getPosicao();
        int hp = estado.getHp();

        Set<String> paredes = new HashSet<>();
        for (Coordenada parede : p.getObjetosFixos()) {
            if (parede != null) {
                paredes.add(parede.chave());
            }
        }

        List<Coordenada> alvos = new ArrayList<>();
        for (Coordenada recurso : p.getRecursosNoMundo()) {
            if (recurso != null) {
                alvos.add(recurso);
            }
        }
        for (Cofre cofre : p.getCofresNoMundo()) {
            if (cofre == null) {
                continue;
            }
            Coordenada posCofre = cofre.getPosicao();
            if (!memoria.cofreEstaNaListaNegra(posCofre)) {
                alvos.add(posCofre);
            }
        }

        List<RoboRival> rivais = new ArrayList<>();
        for (RoboRival rival : p.getOutrosRobots()) {
            if (rival == null) {
                continue;
            }
            boolean ecoPorPosicao = rival.getX() == pos.getX() && rival.getY() == pos.getY();
            boolean ecoPorId = idProprio != null && idProprio.equals(rival.getId());
            if (!ecoPorPosicao && !ecoPorId) {
                rivais.add(rival);
            }
        }

        return new ContextoDecisao(pos, hp, paredes, alvos, rivais);
    }

    public Coordenada minhaPosicao() {
        return minhaPosicao;
    }

    public int meuHp() {
        return meuHp;
    }

    public boolean haParede(Coordenada destino) {
        return chavesParedes.contains(destino.chave());
    }

    public List<Coordenada> alvos() {
        return alvos;
    }

    public List<RoboRival> rivais() {
        return rivais;
    }
}
