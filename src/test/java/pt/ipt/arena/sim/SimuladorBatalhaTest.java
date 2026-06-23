package pt.ipt.arena.sim;

import org.junit.jupiter.api.Test;

import pt.ipt.arena.heuristica.Postura;
import pt.ipt.arena.sim.SimuladorBatalha.Arquetipo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Trava a descoberta do torneio free-for-all: a estratégia eleita (evasão
 * seletiva — postura EVITAR + atacar só com vantagem 30+) tem melhor colocação
 * média, contra o mesmo campo de adversários, do que a agressão pura.
 */
class SimuladorBatalhaTest {

    @Test
    void evasaoSeletivaSuperaAgressaoNoFreeForAll() {
        List<Arquetipo> campo = SimuladorBatalha.campoPadrao();
        double mediaEvasivo = SimuladorBatalha.avaliarCandidato(
                new Arquetipo("Evasivo", 0, 30, Postura.EVITAR), campo, 25, 17, 8, 4)[0];
        double mediaAgressor = SimuladorBatalha.avaliarCandidato(
                new Arquetipo("Agressor", 0, -1000, Postura.CACAR), campo, 25, 17, 8, 4)[0];
        assertTrue(mediaEvasivo > mediaAgressor,
                "no free-for-all, a evasão seletiva deve colocar-se melhor que a agressão pura ("
                        + mediaEvasivo + " vs " + mediaAgressor + ")");
    }
}
