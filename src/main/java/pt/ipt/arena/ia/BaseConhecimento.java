package pt.ipt.arena.ia;

import pt.ipt.arena.modelo.DocumentoVetorial;

import java.util.ArrayList;
import java.util.List;

/**
 * Base de conhecimento vetorial em memória (RAM): guarda os pedaços (chunks) do
 * manual técnico já vetorizados e faz a pesquisa semântica por semelhança de
 * cossenos.
 *
 * SRP: apenas armazena e procura; não comunica com a rede nem com o Ollama.
 */
public class BaseConhecimento {

    private final List<DocumentoVetorial> documentos = new ArrayList<>();

    public void adicionar(DocumentoVetorial documento) {
        if (documento != null && documento.getVetor() != null) {
            documentos.add(documento);
        }
    }

    public int tamanho() {
        return documentos.size();
    }

    public boolean estaVazia() {
        return documentos.isEmpty();
    }

    public void limpar() {
        documentos.clear();
    }

    /**
     * Devolve o documento (chunk) semanticamente mais próximo do vetor da
     * pergunta — o de maior semelhança de cossenos — ou {@code null} se a base
     * estiver vazia ou o vetor for inválido.
     */
    public DocumentoVetorial maisRelevante(double[] vetorPergunta) {
        DocumentoVetorial melhor = null;
        double melhorSemelhanca = Double.NEGATIVE_INFINITY;
        for (DocumentoVetorial documento : documentos) {
            double semelhanca = Vetores.cosseno(vetorPergunta, documento.getVetor());
            if (semelhanca > melhorSemelhanca) {
                melhorSemelhanca = semelhanca;
                melhor = documento;
            }
        }
        return melhor;
    }
}
