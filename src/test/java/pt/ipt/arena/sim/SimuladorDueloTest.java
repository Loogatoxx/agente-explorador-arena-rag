package pt.ipt.arena.sim;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trava a descoberta do benchmark de duelos: contra um adversário agressivo, a
 * política de combate AGRESSIVA (limiar=0, margem=-15 — o default atual) perde
 * claramente menos do que a antiga política conservadora (limiar=40, margem=15).
 */
class SimuladorDueloTest {

    @Test
    void combateAgressivoPerdeMenosQueConservador() {
        int derrotasAgressivo = SimuladorDuelo.resultadosContraAgressor(0, -15, 30, 11)[1];
        int derrotasConservador = SimuladorDuelo.resultadosContraAgressor(40, 15, 30, 11)[1];
        assertTrue(derrotasAgressivo < derrotasConservador,
                "a config agressiva deve perder menos que a conservadora ("
                        + derrotasAgressivo + " vs " + derrotasConservador + ")");
    }
}
