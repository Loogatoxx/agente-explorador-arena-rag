package pt.ipt.arena.ia;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testes das partes deterministas do pipeline RAG (sem Ollama). */
class PipelineRagTest {

    @Test
    void chunkingIgnoraLinhasVazias() {
        List<String> chunks = PipelineRag.partirEmChunks("linha 1\n\n   \nlinha 2\n");
        assertEquals(List.of("linha 1", "linha 2"), chunks);
    }

    @Test
    void limparChaveFicaComPrimeiraLinhaSemAspas() {
        assertEquals("POLY-SEAL", PipelineRag.limparChave("  \"POLY-SEAL\"\nlixo a seguir"));
    }

    @Test
    void limparChaveCortaTagChatMLResidual() {
        assertEquals("ABC123", PipelineRag.limparChave("ABC123<|im_end|>"));
    }

    @Test
    void chatmlContemRestricoesSeccaoEEnigma() {
        String prompt = PipelineRag.construirChatML("manual XPTO", "enigma YYY");
        assertTrue(prompt.contains("<|im_start|>system"), "deve ter o papel system");
        assertTrue(prompt.contains("<|im_start|>assistant"), "deve abrir a tag assistant");
        assertTrue(prompt.contains("manual XPTO"), "deve injetar a secção do manual");
        assertTrue(prompt.contains("enigma YYY"), "deve injetar o enigma");
    }
}
