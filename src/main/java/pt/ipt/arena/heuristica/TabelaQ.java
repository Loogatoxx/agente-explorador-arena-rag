package pt.ipt.arena.heuristica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tabela Q do Q-Learning: para cada ESTADO discreto guarda o valor estimado de
 * cada AÇÃO (qualidade esperada de longo prazo). Atualiza-se pela equação de
 * Bellman:
 *
 *   Q(s,a) ← Q(s,a) + α · [ r + γ · max_a' Q(s',a') − Q(s,a) ]
 *
 * Pura em memória (HashMap), sem dependências externas — corre 100% local. Sabe
 * persistir-se num ficheiro de texto simples para reutilizar o treino.
 */
public class TabelaQ {

    private final Map<String, double[]> q = new HashMap<>();
    private final int nAcoes;
    private final double alfa;   // taxa de aprendizagem
    private final double gama;   // fator de desconto (importância do futuro)

    public TabelaQ(int nAcoes) {
        this(nAcoes, 0.10, 0.95);
    }

    public TabelaQ(int nAcoes, double alfa, double gama) {
        this.nAcoes = nAcoes;
        this.alfa = alfa;
        this.gama = gama;
    }

    private double[] linha(String estado) {
        return q.computeIfAbsent(estado, k -> new double[nAcoes]);
    }

    /** Valor Q estimado de uma ação num estado (0 se nunca visto). */
    public double valor(String estado, int acao) {
        return linha(estado)[acao];
    }

    /** Melhor ação (argmax) entre as válidas; -1 se nenhuma for válida. */
    public int melhorAcao(String estado, boolean[] valida) {
        double[] linha = linha(estado);
        int melhor = -1;
        double melhorValor = Double.NEGATIVE_INFINITY;
        for (int a = 0; a < nAcoes; a++) {
            if (valida[a] && linha[a] > melhorValor) {
                melhorValor = linha[a];
                melhor = a;
            }
        }
        return melhor;
    }

    /** Atualização de Bellman para uma transição não-terminal. */
    public void atualizar(String s, int a, double r, String s2, boolean[] valida2) {
        double maxFuturo = 0;
        boolean algumValido = false;
        double[] linha2 = linha(s2);
        for (int b = 0; b < nAcoes; b++) {
            if (valida2[b] && (!algumValido || linha2[b] > maxFuturo)) {
                maxFuturo = linha2[b];
                algumValido = true;
            }
        }
        if (!algumValido) {
            maxFuturo = 0;
        }
        double[] linha = linha(s);
        linha[a] += alfa * (r + gama * maxFuturo - linha[a]);
    }

    /** Atualização terminal (sem estado seguinte): só a recompensa final. */
    public void atualizarTerminal(String s, int a, double r) {
        double[] linha = linha(s);
        linha[a] += alfa * (r - linha[a]);
    }

    public int tamanho() {
        return q.size();
    }

    /** Persiste a tabela: uma linha por estado, "estado;q0,q1,...". */
    public void guardar(Path caminho) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, double[]> e : q.entrySet()) {
            sb.append(e.getKey()).append(';');
            double[] v = e.getValue();
            for (int a = 0; a < v.length; a++) {
                if (a > 0) {
                    sb.append(',');
                }
                sb.append(v[a]);
            }
            sb.append('\n');
        }
        Files.writeString(caminho, sb.toString());
    }

    /** Carrega uma tabela previamente guardada. */
    public static TabelaQ carregar(Path caminho, int nAcoes) throws IOException {
        TabelaQ tabela = new TabelaQ(nAcoes);
        for (String linha : Files.readAllLines(caminho)) {
            int sep = linha.indexOf(';');
            if (sep < 0) {
                continue;
            }
            String estado = linha.substring(0, sep);
            String[] partes = linha.substring(sep + 1).split(",");
            double[] valores = new double[nAcoes];
            for (int a = 0; a < nAcoes && a < partes.length; a++) {
                valores[a] = Double.parseDouble(partes[a]);
            }
            tabela.q.put(estado, valores);
        }
        return tabela;
    }
}
