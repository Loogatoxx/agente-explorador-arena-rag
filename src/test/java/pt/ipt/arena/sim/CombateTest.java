package pt.ipt.arena.sim;

import org.junit.jupiter.api.Test;

import pt.ipt.arena.heuristica.MotorHeuristico;
import pt.ipt.arena.heuristica.Postura;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trava o resultado da Etapa de combate: a configuração ENTREGUE (o "combo") é a
 * melhor — supera os arquétipos em battle royale e domina o random em 1v1.
 * Mede com poucos seeds (a margem é grande), para o teste ser rápido.
 */
class CombateTest {

    private static final Supplier<MotorHeuristico> COMBO = () -> new MotorHeuristico("nos");
    private static final Supplier<MotorHeuristico> EVASIVO =
            () -> MotorHeuristico.comEstrategiaCompleta("ev", 0, 30, Postura.EVITAR, true);

    @Test
    void comboSuperaEvasivoEmBattleRoyale() {
        List<Supplier<MotorHeuristico>> campo = new ArrayList<>(CombatePerfis.perfis().values());
        double combo = SimuladorBatalha.avaliarContra(COMBO, campo, 25, 17, 8, 4)[0];
        double evasivo = SimuladorBatalha.avaliarContra(EVASIVO, campo, 25, 17, 8, 4)[0];
        assertTrue(combo > evasivo,
                "o combo entregue deve colocar-se melhor que o evasivo no free-for-all ("
                        + combo + " vs " + evasivo + ")");
    }

    @Test
    void comboDominaRandomEm1v1() {
        double vitorias = SimuladorBatalha.avaliarContra(
                COMBO, List.of((Supplier<MotorHeuristico>) () -> CombatePerfis.motorRandom("r")),
                60, 13, 4, 2)[1];
        assertTrue(vitorias > 65.0,
                "o combo deve ganhar a grande maioria dos duelos contra um bot random (" + vitorias + "%)");
    }
}
