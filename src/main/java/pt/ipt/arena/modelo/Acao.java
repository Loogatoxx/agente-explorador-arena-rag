package pt.ipt.arena.modelo;

/**
 * Ações motoras autorizadas pela API da Arena (movimentos discretos bloco a
 * bloco). O nome de cada constante coincide exatamente com a string que o
 * servidor espera no campo {@code action}, pelo que {@link #getComando()}
 * devolve simplesmente {@link #name()}.
 */
public enum Acao {
    MOVER_NORTE,
    MOVER_SUL,
    MOVER_ESTE,
    MOVER_OESTE;

    /** Comando textual a enviar ao servidor (ex.: "MOVER_NORTE"). */
    public String getComando() {
        return name();
    }
}
