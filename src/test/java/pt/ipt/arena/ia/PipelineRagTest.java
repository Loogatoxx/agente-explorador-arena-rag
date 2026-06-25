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
    void limparChaveRemovePrefixoVerboso() {
        assertEquals("POLY-SEAL", PipelineRag.limparChave("A chave é POLY-SEAL."));
        assertEquals("ABC123", PipelineRag.limparChave("chave: ABC123"));
        assertEquals("XPTO-9", PipelineRag.limparChave("Resposta: \"XPTO-9\""));
    }

    @Test
    void chatmlContemRestricoesSeccaoEEnigma() {
        String prompt = PipelineRag.construirChatML("manual XPTO", "enigma YYY");
        assertTrue(prompt.contains("<|im_start|>system"), "deve ter o papel system");
        assertTrue(prompt.contains("<|im_start|>assistant"), "deve abrir a tag assistant");
        assertTrue(prompt.contains("manual XPTO"), "deve injetar a secção do manual");
        assertTrue(prompt.contains("enigma YYY"), "deve injetar o enigma");
    }

    // ----- Rede de segurança determinista sobre a alucinação do LLM pequeno -----

    private static final String SECCAO_04 =
            "SECÇÃO 04 - MANUAL DE DINÂMICA DE FLUIDOS: Quando a pressão dos canos "
            + "reserva exceder o limiar crítico, o operador deve ativar a VALVULA-ALT "
            + "para drenar o excesso de carga hidrostática.";

    @Test
    void validarAceitaChaveCorretaDoLlmPresenteNaSeccao() {
        // O LLM acertou: a chave consta da secção -> é aceite tal como veio.
        assertEquals("VALVULA-ALT", PipelineRag.validarOuExtrair("VALVULA-ALT", SECCAO_04));
    }

    @Test
    void validarCorrigeAlucinacaoExtraindoCodigoDaSeccao() {
        // O LLM alucinou um código de outra secção (VENT-OUT não está aqui):
        // a rede de segurança extrai o código real (último token em MAIÚSCULAS).
        assertEquals("VALVULA-ALT", PipelineRag.validarOuExtrair("VENT-OUT", SECCAO_04));
    }

    @Test
    void validarExtraiUltimoCodigoIgnorandoFalsosPositivosDoTitulo() {
        // O título "ULTRA-FREQUÊNCIA" é um falso positivo; o código real (LINK-UP)
        // está no fim da frase e é o que deve ser devolvido.
        String seccao13 = "SECÇÃO 13 - ANTENA ULTRA-FREQUÊNCIA: para restabelecer os "
                + "canais da API, ative o amplificador através do protocolo LINK-UP.";
        assertEquals("LINK-UP", PipelineRag.validarOuExtrair("disparate", seccao13));
    }
}
