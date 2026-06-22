package pt.ipt.arena.modelo;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de dados (POJO) do estado do próprio agente, recebido no campo
 * {@code o_meu_estado} da telemetria: coordenadas atuais (X, Y) e energia (HP).
 *
 * Nota: os nomes dos campos JSON ("x"/"y"/"hp") são inferidos do enunciado;
 * confirmar com uma sala ativa e ajustar os {@code @SerializedName} se
 * necessário.
 */
public class EstadoRobo {

    @SerializedName("x")
    private int x;

    @SerializedName("y")
    private int y;

    @SerializedName("hp")
    private int hp;

    public EstadoRobo() {
    }

    public EstadoRobo(int x, int y, int hp) {
        this.x = x;
        this.y = y;
        this.hp = hp;
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

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public Coordenada getPosicao() {
        return new Coordenada(x, y);
    }

    @Override
    public String toString() {
        return "EstadoRobo{pos=(" + x + ", " + y + "), hp=" + hp + "}";
    }
}
