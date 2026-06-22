package pt.ipt.arena.modelo;

/**
 * Modelo de dados (POJO) que representa, em memória RAM, um pedaço (chunk) de
 * texto do manual técnico e o respetivo vetor de embeddings produzido pelo
 * modelo {@code nomic-embed-text}.
 *
 * Cumpre o Princípio da Responsabilidade Única (SRP): apenas guarda dados
 * (com Getters/Setters); não comunica com a rede nem executa lógica.
 */
public class DocumentoVetorial {

    private String texto;
    private double[] vetor;

    public DocumentoVetorial() {
    }

    public DocumentoVetorial(String texto, double[] vetor) {
        this.texto = texto;
        this.vetor = vetor;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public double[] getVetor() {
        return vetor;
    }

    public void setVetor(double[] vetor) {
        this.vetor = vetor;
    }

    @Override
    public String toString() {
        int dimensoes = (vetor == null) ? 0 : vetor.length;
        return "DocumentoVetorial{dimensoes=" + dimensoes + ", texto='" + resumo() + "'}";
    }

    private String resumo() {
        if (texto == null) {
            return "";
        }
        return texto.length() <= 40 ? texto : texto.substring(0, 40) + "...";
    }
}
