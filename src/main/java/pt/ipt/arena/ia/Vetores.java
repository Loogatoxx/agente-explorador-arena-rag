package pt.ipt.arena.ia;

/**
 * Utilitários de álgebra vetorial para a pesquisa semântica do pipeline RAG.
 * Classe utilitária sem estado (apenas métodos estáticos).
 */
public final class Vetores {

    private Vetores() {
        // Sem instâncias.
    }

    /**
     * Semelhança de cossenos entre dois vetores: produto interno a dividir pelo
     * produto das normas. Resultado em [-1, 1] (quanto mais perto de 1, mais
     * semanticamente parecidos).
     *
     * Defensivo: devolve 0 se algum vetor for {@code null}, de dimensão
     * diferente, vazio, ou de norma nula.
     */
    public static double cosseno(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) {
            return 0.0;
        }
        double produtoInterno = 0.0;
        double normaA = 0.0;
        double normaB = 0.0;
        for (int i = 0; i < a.length; i++) {
            produtoInterno += a[i] * b[i];
            normaA += a[i] * a[i];
            normaB += b[i] * b[i];
        }
        if (normaA == 0.0 || normaB == 0.0) {
            return 0.0;
        }
        return produtoInterno / (Math.sqrt(normaA) * Math.sqrt(normaB));
    }
}
