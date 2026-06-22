package pt.ipt.arena.modelo;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

/**
 * Modelo de dados (POJO) de uma posição (X, Y) na grelha bidimensional da
 * Arena. Usado para paredes ({@code objetos_fixos}) e recursos
 * ({@code recursos_no_mundo}).
 *
 * Nota: os nomes dos campos JSON ("x"/"y") são inferidos do enunciado;
 * confirmar com uma sala ativa e ajustar os {@code @SerializedName} se
 * necessário.
 */
public class Coordenada {

    @SerializedName("x")
    private int x;

    @SerializedName("y")
    private int y;

    public Coordenada() {
    }

    public Coordenada(int x, int y) {
        this.x = x;
        this.y = y;
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

    /** Chave textual "x,y" — útil para mapas (ex.: mapa de calor). */
    public String chave() {
        return x + "," + y;
    }

    public boolean em(int outroX, int outroY) {
        return this.x == outroX && this.y == outroY;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Coordenada)) {
            return false;
        }
        Coordenada that = (Coordenada) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
