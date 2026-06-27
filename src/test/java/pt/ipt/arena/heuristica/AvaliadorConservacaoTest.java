package pt.ipt.arena.heuristica;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import pt.ipt.arena.modelo.Acao;
import pt.ipt.arena.modelo.Percecao;
import pt.ipt.arena.modelo.RoboRival;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trava o comportamento da conservação de HP: NUNCA campa em modo solo; em PvP só
 * campa com vantagem clara e fica parado encostado a uma parede.
 */
class AvaliadorConservacaoTest {

    private final Gson gson = new Gson();
    private final AvaliadorConservacao av = new AvaliadorConservacao();

    /** Cenário base: HP 200 em (5,5), com uma parede a NORTE (5,4), sem rival no radar. */
    private ContextoDecisao ctx(MemoriaHeuristica mem) {
        Percecao p = gson.fromJson(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":200},"
                        + "\"objetos_fixos\":[{\"x\":5,\"y\":4}]}", Percecao.class);
        return ContextoDecisao.construir(p, mem, "eu");
    }

    private List<Bussola> validas(ContextoDecisao c, MemoriaHeuristica mem) {
        return new FiltroColisoes().direcoesValidas(c, mem);
    }

    private RoboRival rival(int hp) {
        RoboRival r = new RoboRival();
        r.setId("r1");
        r.setX(9);
        r.setY(9);
        r.setHp(hp);
        return r;
    }

    @Test
    void emSoloNuncaCampa() {
        MemoriaHeuristica mem = new MemoriaHeuristica(); // nunca viu rival
        ContextoDecisao c = ctx(mem);
        assertTrue(av.avaliar(c, mem, validas(c, mem)).isEmpty(),
                "em modo solo a conservação não dispara (continua a explorar/coletar)");
    }

    @Test
    void emPvpComVantagemFicaParadoContraParede() {
        MemoriaHeuristica mem = new MemoriaHeuristica();
        mem.atualizarHpRivais(List.of(rival(150))); // PvP: vimos um rival com 150 HP (nós: 200)
        ContextoDecisao c = ctx(mem);
        Optional<Acao> dec = av.avaliar(c, mem, validas(c, mem));
        assertTrue(dec.isPresent(), "com vantagem e parede adjacente, conserva");
        assertEquals(Acao.MOVER_NORTE, dec.get(), "fica parado contra a parede a Norte (movimento bloqueado)");
    }

    @Test
    void rivalProximoNaoCampaDeixaFugir() {
        MemoriaHeuristica mem = new MemoriaHeuristica();
        mem.atualizarHpRivais(List.of(rival(150)));
        // Rival a 2 casas (5,7) do agente (5,5), parede a Norte (5,4).
        Percecao p = gson.fromJson(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":200},"
                        + "\"objetos_fixos\":[{\"x\":5,\"y\":4}],"
                        + "\"outros_robots\":[{\"id\":\"r1\",\"x\":5,\"y\":7,\"hp\":150}]}", Percecao.class);
        ContextoDecisao c = ContextoDecisao.construir(p, mem, "eu");
        assertTrue(av.avaliar(c, mem, validas(c, mem)).isEmpty(),
                "com um rival a ≤2 casas NÃO campa — deixa o combate/fuga decidir");
    }

    @Test
    void emPvpSemVantagemNaoCampa() {
        MemoriaHeuristica mem = new MemoriaHeuristica();
        mem.atualizarHpRivais(List.of(rival(250))); // inimigo mais forte (250 > nós 200)
        ContextoDecisao c = ctx(mem);
        assertTrue(av.avaliar(c, mem, validas(c, mem)).isEmpty(),
                "sem vantagem, não campa — explora à procura de loot/curas");
    }
}
