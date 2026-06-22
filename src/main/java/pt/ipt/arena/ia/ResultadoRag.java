package pt.ipt.arena.ia;

/**
 * Resultado imutável de uma resolução RAG de um enigma:
 * <ul>
 *   <li>{@code chave} — a chave de desbloqueio já limpa (a submeter ao cofre);</li>
 *   <li>{@code contexto} — a secção do manual recuperada (chunk mais relevante);</li>
 *   <li>{@code bruto} — a resposta crua do LLM (para transparência/auditoria).</li>
 * </ul>
 */
public record ResultadoRag(String chave, String contexto, String bruto) {
}
