package pt.ipt.arena.ia;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Testes da semelhança de cossenos (pesquisa semântica do RAG). */
class VetoresTest {

    @Test
    void vetoresIdenticosDaoUm() {
        double[] a = {1.0, 2.0, 3.0};
        assertEquals(1.0, Vetores.cosseno(a, new double[]{1.0, 2.0, 3.0}), 1e-9);
    }

    @Test
    void vetoresOrtogonaisDaoZero() {
        assertEquals(0.0, Vetores.cosseno(new double[]{1.0, 0.0}, new double[]{0.0, 1.0}), 1e-9);
    }

    @Test
    void vetoresOpostosDaoMenosUm() {
        assertEquals(-1.0, Vetores.cosseno(new double[]{1.0, 1.0}, new double[]{-1.0, -1.0}), 1e-9);
    }

    @Test
    void defensivoComNullDimensoesDiferentesOuNormaNula() {
        assertEquals(0.0, Vetores.cosseno(null, new double[]{1.0}));
        assertEquals(0.0, Vetores.cosseno(new double[]{1.0, 2.0}, new double[]{1.0}));
        assertEquals(0.0, Vetores.cosseno(new double[]{0.0, 0.0}, new double[]{1.0, 1.0}));
    }
}
