package pt.ipt.arena.heuristica;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Coordenada;

import java.util.List;
import java.util.Optional;

/**
 * Conservação de HP em PvP. Cada movimento custa 1 de HP, por isso, quando já
 * estamos em vantagem, andar à toa só serve para perder a liderança. Esta regra
 * faz o agente FICAR PARADO (movimento contra uma parede é recusado pelo servidor
 * e NÃO custa HP) quando:
 * <ol>
 *   <li>já vimos algum rival nesta partida (é PvP) — em modo SOLO nunca dispara,
 *       logo o agente continua a explorar e a abrir todos os cofres;</li>
 *   <li>não há nenhum rival no radar agora (ninguém para combater/fugir);</li>
 *   <li>temos VANTAGEM clara: mais HP do que o inimigo mais forte que já vimos.</li>
 * </ol>
 *
 * Colocado DEPOIS do {@link AvaliadorAlvo} na cadeia: se houver cofre/recurso
 * conhecido por apanhar, o agente vai buscá-lo primeiro (o loot dá vantagem); só
 * quando não há nada a ganhar é que conserva. Se ainda não estiver encostado a
 * uma parede, aproxima-se da mais próxima para campar (movimento mínimo).
 */
public class AvaliadorConservacao implements AvaliadorAcao {

    /**
     * Margem de HP exigida sobre o inimigo mais forte para começar a conservar.
     * 0 = conserva quando estritamente à frente; negativa = conserva mesmo um
     * pouco atrás (mais defensivo, deixa de sangrar HP a andar).
     */
    private final int margemVantagem;

    public AvaliadorConservacao() {
        this(0);
    }

    public AvaliadorConservacao(int margemVantagem) {
        this.margemVantagem = margemVantagem;
    }

    @Override
    public Optional<Acao> avaliar(ContextoDecisao ctx, MemoriaHeuristica memoria, List<Bussola> validas) {
        if (validas.isEmpty()) {
            return Optional.empty();
        }
        // Modo SOLO (nunca vimos rival) → nunca campa: continua a explorar/coletar.
        if (!memoria.viuAlgumRival()) {
            return Optional.empty();
        }
        // Rival à vista → deixa os avaliadores de combate decidir.
        if (!ctx.rivais().isEmpty()) {
            return Optional.empty();
        }
        // Sem a margem de vantagem exigida sobre o inimigo mais forte conhecido →
        // explora (procura loot/curas), não conserva.
        if (ctx.meuHp() <= memoria.getMaxHpInimigoConhecido() + margemVantagem) {
            return Optional.empty();
        }

        Coordenada pos = ctx.minhaPosicao();

        // Encostados a uma parede: ficamos parados (movimento bloqueado = 0 HP).
        for (Bussola dir : Bussola.todas()) {
            if (ctx.haParede(dir.aplicar(pos))) {
                return Optional.of(dir.paraAcao());
            }
        }

        // Sem parede adjacente: aproxima-se da parede conhecida mais próxima para
        // campar (poucos passos), em vez de explorar e gastar HP à toa.
        Coordenada parede = paredeMaisProxima(pos, memoria);
        if (parede != null) {
            Bussola passo = Bussola.direcaoQueReduzManhattan(pos, parede, validas);
            if (passo != null) {
                return Optional.of(passo.paraAcao());
            }
        }
        return Optional.empty();
    }

    /** Parede conhecida (mapa acumulado) mais próxima por Manhattan; null se nenhuma. */
    private Coordenada paredeMaisProxima(Coordenada pos, MemoriaHeuristica memoria) {
        Coordenada melhor = null;
        int melhorDist = Integer.MAX_VALUE;
        for (String chave : memoria.getMapa().paredes()) {
            int virgula = chave.indexOf(',');
            if (virgula < 0) {
                continue;
            }
            int wx = Integer.parseInt(chave.substring(0, virgula));
            int wy = Integer.parseInt(chave.substring(virgula + 1));
            int dist = Math.abs(wx - pos.getX()) + Math.abs(wy - pos.getY());
            if (dist < melhorDist) {
                melhorDist = dist;
                melhor = new Coordenada(wx, wy);
            }
        }
        return melhor;
    }
}
