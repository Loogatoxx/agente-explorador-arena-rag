package pt.ipt.arena.modelo;

import com.google.gson.annotations.SerializedName;

/**
 * Modelo de dados (POJO) de um robô rival detetado no radar, recebido no campo
 * {@code outros_robots} (relevante sobretudo no modo Batalha / Battle Royale).
 * Guarda a posição e a energia (HP) do rival para a decisão de "Luta ou Fuga"
 * (comparação do Delta de Energia).
 *
 * Nota: nomes de campos inferidos do enunciado; confirmar com sala ativa.
 */
public class RoboRival {

    @SerializedName("id")
    private String id;

    @SerializedName("x")
    private int x;

    @SerializedName("y")
    private int y;

    @SerializedName("hp")
    private int hp;

    public RoboRival() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return "RoboRival{id=" + id + ", pos=(" + x + ", " + y + "), hp=" + hp + "}";
    }
}
