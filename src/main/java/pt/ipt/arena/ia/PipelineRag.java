package pt.ipt.arena.ia;

import com.google.gson.JsonObject;

import pt.ipt.arena.modelo.Cofre;
import pt.ipt.arena.modelo.DocumentoVetorial;
import pt.ipt.arena.rede.ArenaClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquestra o pipeline RAG completo (Retrieval-Augmented Generation):
 * <ol>
 *   <li><b>Ingestão</b>: descarrega o manual técnico ({@link ArenaClient}),
 *       parte-o em chunks e vetoriza-os ({@link OllamaClient}) para a
 *       {@link BaseConhecimento};</li>
 *   <li><b>Retrieval</b>: dado um enigma, vetoriza-o e procura o chunk
 *       semanticamente mais próximo (semelhança de cossenos);</li>
 *   <li><b>Generation</b>: constrói um prompt ChatML rígido e gera a chave com
 *       o LLM (temperatura 0.0), limpando a resposta;</li>
 *   <li><b>Submissão</b>: envia a chave ao endpoint de unlock.</li>
 * </ol>
 *
 * SRP: junta os colaboradores mas não reimplementa rede, IA nem álgebra
 * vetorial.
 */
public class PipelineRag {

    private final ArenaClient arena;
    private final OllamaClient ollama;
    private final BaseConhecimento base = new BaseConhecimento();
    private boolean indexado = false;

    public PipelineRag(ArenaClient arena, OllamaClient ollama) {
        this.arena = arena;
        this.ollama = ollama;
    }

    public boolean estaIndexado() {
        return indexado;
    }

    /**
     * Descarrega o manual e indexa-o (vetoriza os chunks). Deve ser chamado uma
     * vez por partida. Devolve {@code true} se ficou pelo menos um chunk indexado.
     */
    public boolean indexarManual() {
        String manual = arena.descarregarManual();
        if (manual == null || manual.isBlank()) {
            return false;
        }
        base.limpar();
        for (String chunk : partirEmChunks(manual)) {
            double[] vetor = ollama.gerarEmbedding(chunk);
            if (vetor != null) {
                base.adicionar(new DocumentoVetorial(chunk, vetor));
            }
        }
        indexado = !base.estaVazia();
        return indexado;
    }

    /**
     * Resolve um enigma (retrieval + generation), SEM submeter. Devolve a chave
     * e o contexto/bruto associados, ou {@code null} se algo falhar.
     */
    public ResultadoRag resolver(String enigma) {
        if (!indexado || enigma == null || enigma.isBlank()) {
            return null;
        }
        double[] vetorPergunta = ollama.gerarEmbedding(enigma);
        if (vetorPergunta == null) {
            return null;
        }
        DocumentoVetorial contexto = base.maisRelevante(vetorPergunta);
        if (contexto == null) {
            return null;
        }
        String bruto = ollama.gerarTextoRaw(construirChatML(contexto.getTexto(), enigma));
        if (bruto == null) {
            return null;
        }
        return new ResultadoRag(limparChave(bruto), contexto.getTexto(), bruto);
    }

    /**
     * Resolve o enigma de um cofre e submete a chave ao servidor. Devolve a
     * resposta do unlock ({@code {"status": ...}}) ou {@code null} se não foi
     * possível resolver/submeter.
     */
    public JsonObject tentarDesbloquear(Cofre cofre) {
        if (cofre == null) {
            return null;
        }
        ResultadoRag resultado = resolver(cofre.getTerminalDesafio());
        if (resultado == null) {
            return null;
        }
        return arena.desbloquear(resultado.chave(), resultado.contexto(), resultado.bruto());
    }

    /** Parte o manual em blocos lógicos por quebras de linha, ignorando vazios. */
    static List<String> partirEmChunks(String manual) {
        List<String> chunks = new ArrayList<>();
        for (String linha : manual.split("\\R")) {
            String texto = linha.trim();
            if (!texto.isEmpty()) {
                chunks.add(texto);
            }
        }
        return chunks;
    }

    /**
     * Constrói o prompt em ChatML com restrições negativas no papel "system" e a
     * tag "assistant" aberta, forçando o LLM a iniciar a resposta diretamente
     * pela chave (anula alucinações introdutórias).
     */
    static String construirChatML(String seccaoManual, String enigma) {
        return "<|im_start|>system\n"
                + "És um extrator de dados técnico. Lês a SECÇÃO DO MANUAL e respondes "
                + "APENAS com a chave de desbloqueio exata. Não expliques, não inventes, "
                + "não escrevas frases nem pontuação extra. Responde só com a chave.\n"
                + "<|im_end|>\n"
                + "<|im_start|>user\n"
                + "SECÇÃO DO MANUAL:\n" + seccaoManual + "\n\n"
                + "ENIGMA: " + enigma + "\n"
                + "Qual é a chave?\n"
                + "<|im_end|>\n"
                + "<|im_start|>assistant\n";
    }

    /**
     * Limpa a resposta crua do LLM para isolar a chave: corta em tags ChatML
     * residuais, fica com a primeira linha não vazia e remove aspas/crase.
     */
    static String limparChave(String bruto) {
        String texto = bruto.trim();
        int fim = texto.indexOf("<|im_end|>");
        if (fim >= 0) {
            texto = texto.substring(0, fim).trim();
        }
        for (String linha : texto.split("\\R")) {
            String candidata = linha.trim();
            if (!candidata.isEmpty()) {
                texto = candidata;
                break;
            }
        }
        return texto.replaceAll("^[\"'`]+", "").replaceAll("[\"'`]+$", "").trim();
    }
}
