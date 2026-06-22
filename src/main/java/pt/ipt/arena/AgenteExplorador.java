package pt.ipt.arena;

/**
 * Classe principal (Main) e orquestrador do ciclo de vida do agente.
 *
 * Implementa o ciclo infinito Sense-Think-Act (Perceção, Decisão, Ação),
 * delegando cada responsabilidade às classes especializadas
 * ({@code ArenaClient}, {@code OllamaClient}, {@code PainelMapaCalor}, ...).
 *
 * Princípio fundamental: NÃO concentra lógica de rede, de IA nem de desenho.
 * Evita explicitamente o anti-padrão "Classe Deus".
 */
public class AgenteExplorador {

    public static void main(String[] args) {
        System.out.println("Agente Explorador - arranque (esqueleto inicial).");
        // TODO (etapa: ciclo de vida):
        //   1. Ler configuracao (id do robo, codigo da sala, modo).
        //   2. Instanciar ArenaClient, OllamaClient e PainelMapaCalor.
        //   3. Registar o robo na Arena.
        //   4. Entrar no loop infinito Sense-Think-Act com Thread.sleep() de seguranca.
    }
}
