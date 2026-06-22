package pt.ipt.arena.modelo;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de dados (POJO) de um Terminal de Plasma (cofre), recebido no campo
 * {@code cofres_no_mundo}. Quando o agente está exatamente por cima do cofre,
 * a telemetria inclui o enigma em {@code terminal_desafio} (caso contrário,
 * fica {@code null}).
 *
 * Nota: nomes de campos inferidos do enunciado; confirmar com sala ativa.
 */
public class Cofre {

    @SerializedName("x")
    private int x;

    @SerializedName("y")
    private int y;

    @SerializedName("terminal_desafio")
    private String terminalDesafio;

    public Cofre() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getTerminalDesafio() {
        return terminalDesafio;
    }

    public void setTerminalDesafio(String terminalDesafio) {
        this.terminalDesafio = terminalDesafio;
    }

    /** Indica se o agente está em cima deste cofre (tem o enigma disponível). */
    public boolean temDesafioAtivo() {
        return terminalDesafio != null && !terminalDesafio.isBlank();
    }

    public Coordenada getPosicao() {
        return new Coordenada(x, y);
    }

    @Override
    public String toString() {
        return "Cofre{pos=(" + x + ", " + y + "), desafioAtivo=" + temDesafioAtivo() + "}";
    }
}
