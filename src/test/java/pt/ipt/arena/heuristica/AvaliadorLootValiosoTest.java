package pt.ipt.arena.heuristica;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import pt.ipt.arena.modelo.Percecao;
import pt.ipt.arena.modelo.RoboRival;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trava a atração de loot consciente da valorização: em SOLO persegue sempre os
 * cofres (missão); em PvP só vai buscar loot se valer a pena em HP (não sangra
 * HP a perseguir uma cura desperdiçada pelo teto).
 */
class AvaliadorLootValiosoTest {

    private final Gson gson = new Gson();
    private final AvaliadorLootValioso av = new AvaliadorLootValioso(2.0);
    private final FiltroColisoes filtro = new FiltroColisoes();

    /** Bot em (5,5) com HP dado e um cofre LONGE a Este (35,5), distância 30. */
    private boolean decide(int hp, boolean pvp) {
        MemoriaHeuristica mem = new MemoriaHeuristica();
        Percecao p = gson.fromJson(
                "{\"game_started\":true,\"o_meu_estado\":{\"x\":5,\"y\":5,\"hp\":" + hp + "},"
                        + "\"cofres_no_mundo\":[{\"x\":35,\"y\":5}]}", Percecao.class);
        mem.getMapa().integrar(p);
        if (pvp) {
            RoboRival r = new RoboRival();
            r.setId("r1");
            r.setX(40);
            r.setY(40);
            r.setHp(200);
            mem.atualizarHpRivais(List.of(r)); // marca a partida como PvP
        }
        ContextoDecisao ctx = ContextoDecisao.construir(p, mem, "eu");
        return av.avaliar(ctx, mem, filtro.direcoesValidas(ctx, mem)).isPresent();
    }

    @Test
    void soloPersegueSempreOCofre() {
        assertTrue(decide(240, false), "em solo persegue o cofre mesmo a HP alto (missão)");
    }

    @Test
    void pvpHpAltoIgnoraCofreLongeDesperdicado() {
        // HP 240 → valor do cofre = min(100, 10) = 10; dist 30 * custo 2 = 60 → net < 0.
        assertTrue(!decide(240, true), "em PvP a HP alto NÃO persegue um cofre longe (cura desperdiçada)");
    }

    @Test
    void pvpHpBaixoPersegueCofre() {
        // HP 50 → valor = min(100, 200) = 100; dist 30 * 2 = 60 → net +40 > 0.
        assertTrue(decide(50, true), "em PvP a HP baixo vai buscar o cofre (cura cheia compensa)");
    }
}
