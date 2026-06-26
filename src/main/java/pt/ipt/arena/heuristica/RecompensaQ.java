package pt.ipt.arena.heuristica;

/**
 * Pesos da função de recompensa do agente híbrido, em objeto imutável — para o
 * Grid Search poder testar várias combinações sem mexer no código.
 */
public final class RecompensaQ {

    public final double hp;          // peso do Δhp por turno
    public final double sobreviver;  // bónus por turno vivo
    public final double abate;       // bónus por rematar (kill grátis)
    public final double danoMelee;   // penalização por apanhar dano em mêlée sem matar
    public final double colocacao;   // peso da recompensa terminal por colocação

    public RecompensaQ(double hp, double sobreviver, double abate, double danoMelee, double colocacao) {
        this.hp = hp;
        this.sobreviver = sobreviver;
        this.abate = abate;
        this.danoMelee = danoMelee;
        this.colocacao = colocacao;
    }

    /** Configuração equilibrada por omissão (a melhor da V2: 5.71/8). */
    public static RecompensaQ omissao() {
        return new RecompensaQ(1.0, 0.3, 40.0, 15.0, 8.0);
    }

    @Override
    public String toString() {
        return String.format("hp=%.1f surv=%.1f abate=%.0f melee=%.0f coloc=%.0f",
                hp, sobreviver, abate, danoMelee, colocacao);
    }
}
