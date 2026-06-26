package pt.ipt.arena.ui;

import pt.ipt.arena.modelo.Cofre;
import pt.ipt.arena.modelo.Coordenada;
import pt.ipt.arena.modelo.EstadoRobo;
import pt.ipt.arena.modelo.Percecao;
import pt.ipt.arena.modelo.RoboRival;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Encapsula todo o código visual (Java Swing / Graphics2D) que desenha o
 * dashboard de telemetria local: o radar centrado no agente (paredes, recursos,
 * cofres, rivais), o mapa de calor das células visitadas e um painel de texto
 * com o estado (posição, HP, contagens).
 *
 * SRP: quem desenha ecrãs não comunica com a rede nem com a IA. Os dados chegam
 * de fora por {@link #atualizar(Percecao, Map)}; a UI nunca toca no estado
 * mutável do motor (recebe um snapshot imutável do mapa de calor).
 */
public class PainelMapaCalor extends JPanel {

    private static final long serialVersionUID = 1L;

    /** Raio (em células) do radar desenhado à volta do agente → grelha (2R+1). */
    private static final int RAIO = 6;
    private static final int ALTURA_PAINEL_TEXTO = 150;

    private static final Color COR_FUNDO = new Color(24, 24, 28);
    private static final Color COR_GRELHA = new Color(45, 45, 52);
    private static final Color COR_PAREDE = new Color(214, 120, 38);
    private static final Color COR_RECURSO = new Color(60, 200, 120);
    private static final Color COR_COFRE = new Color(240, 200, 60);
    private static final Color COR_RIVAL = new Color(220, 70, 70);
    private static final Color COR_AGENTE = new Color(80, 160, 240);
    private static final Color COR_TEXTO = new Color(225, 225, 225);

    private transient Percecao percecaoAtual;
    private transient Map<String, Integer> mapaCalor = Collections.emptyMap();

    public PainelMapaCalor() {
        setPreferredSize(new Dimension(560, 640));
        setBackground(COR_FUNDO);
    }

    /**
     * Cria e mostra uma janela com um novo painel, devolvendo-o para ser
     * atualizado pelo ciclo do agente. Devolve {@code null} em ambiente sem
     * ecrã (headless), para o agente poder correr sem interface.
     */
    public static PainelMapaCalor abrirJanela(String titulo) {
        if (GraphicsEnvironment.isHeadless()) {
            return null;
        }
        PainelMapaCalor painel = new PainelMapaCalor();
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame(titulo);
            janela.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            janela.setContentPane(painel);
            janela.pack();
            janela.setLocationByPlatform(true);
            janela.setVisible(true);
        });
        return painel;
    }

    /** Recebe a telemetria do turno e um snapshot do mapa de calor; repinta. */
    public void atualizar(Percecao percecao, Map<String, Integer> mapaCalor) {
        this.percecaoAtual = percecao;
        this.mapaCalor = (mapaCalor != null) ? mapaCalor : Collections.emptyMap();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(COR_FUNDO);
        g2.fillRect(0, 0, getWidth(), getHeight());

        Percecao p = percecaoAtual;
        if (p == null || p.getOMeuEstado() == null) {
            g2.setColor(COR_TEXTO);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 14));
            g2.drawString("À espera de telemetria...", 20, 30);
            return;
        }

        EstadoRobo estado = p.getOMeuEstado();
        int centroX = estado.getX();
        int centroY = estado.getY();

        int lado = 2 * RAIO + 1;
        int areaGrelha = Math.min(getWidth() - 20, getHeight() - ALTURA_PAINEL_TEXTO);
        int cs = Math.max(8, areaGrelha / lado);
        int origem = 10;

        Set<String> paredes = chavesCoord(p.getObjetosFixos());
        Set<String> recursos = chavesCoord(p.getRecursosNoMundo());
        Set<String> cofres = chavesCofres(p.getCofresNoMundo());
        Set<String> rivais = chavesRivais(p.getOutrosRobots());

        for (int gy = 0; gy < lado; gy++) {
            for (int gx = 0; gx < lado; gx++) {
                int ax = centroX + (gx - RAIO);
                int ay = centroY + (gy - RAIO);
                String chave = ax + "," + ay;
                // O painel é só telemetria: espelha-se nos DOIS eixos para a
                // orientação bater certo com a vista 3D real da arena (cima/baixo
                // e esquerda/direita). NÃO afeta a lógica de movimento (correta).
                int px = origem + (lado - 1 - gx) * cs;
                int py = origem + (lado - 1 - gy) * cs;

                g2.setColor(corMapaCalor(mapaCalor.getOrDefault(chave, 0)));
                g2.fillRect(px, py, cs, cs);

                if (paredes.contains(chave)) {
                    g2.setColor(COR_PAREDE);
                    g2.fillRect(px, py, cs, cs);
                }
                if (recursos.contains(chave)) {
                    disco(g2, px, py, cs, COR_RECURSO);
                }
                if (cofres.contains(chave)) {
                    disco(g2, px, py, cs, COR_COFRE);
                }
                if (rivais.contains(chave)) {
                    disco(g2, px, py, cs, COR_RIVAL);
                }
                if (ax == centroX && ay == centroY) {
                    disco(g2, px, py, cs, COR_AGENTE);
                }

                g2.setColor(COR_GRELHA);
                g2.drawRect(px, py, cs, cs);
            }
        }

        desenharTelemetria(g2, p, estado, origem + lado * cs + 22);
    }

    private void desenharTelemetria(Graphics2D g2, Percecao p, EstadoRobo estado, int y) {
        g2.setColor(COR_TEXTO);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 13));
        int x = 12;
        int dh = 19;

        g2.drawString("Posicao: (" + estado.getX() + ", " + estado.getY() + ")     HP: " + estado.getHp(), x, y);

        int larguraBarra = 220;
        int alturaBarra = 11;
        int hpMax = 250;
        int barraY = y + 8;
        g2.setColor(new Color(60, 60, 66));
        g2.fillRect(x, barraY, larguraBarra, alturaBarra);
        int hp = Math.max(0, Math.min(estado.getHp(), hpMax));
        g2.setColor(hp < 60 ? COR_RIVAL : COR_RECURSO);
        g2.fillRect(x, barraY, (int) (larguraBarra * (hp / (double) hpMax)), alturaBarra);

        g2.setColor(COR_TEXTO);
        int linha = barraY + alturaBarra + dh;
        g2.drawString("Paredes: " + p.getObjetosFixos().size()
                + "   Recursos: " + p.getRecursosNoMundo().size()
                + "   Cofres: " + p.getCofresNoMundo().size()
                + "   Rivais: " + p.getOutrosRobots().size(), x, linha);
        linha += dh;
        g2.drawString("Celulas visitadas (mapa de calor): " + mapaCalor.size(), x, linha);
        linha += dh + 4;
        desenharLegenda(g2, x, linha);
    }

    private void desenharLegenda(Graphics2D g2, int x, int y) {
        x = legenda(g2, x, y, COR_AGENTE, "Agente");
        x = legenda(g2, x, y, COR_PAREDE, "Parede");
        x = legenda(g2, x, y, COR_RECURSO, "Recurso");
        x = legenda(g2, x, y, COR_COFRE, "Cofre");
        legenda(g2, x, y, COR_RIVAL, "Rival");
    }

    private int legenda(Graphics2D g2, int x, int y, Color cor, String etiqueta) {
        g2.setColor(cor);
        g2.fillRect(x, y - 10, 11, 11);
        g2.setColor(COR_TEXTO);
        g2.drawString(etiqueta, x + 16, y);
        return x + 16 + g2.getFontMetrics().stringWidth(etiqueta) + 16;
    }

    /** Mapa de calor: dark → verde cada vez mais vivo conforme as visitas. */
    private Color corMapaCalor(int visitas) {
        if (visitas <= 0) {
            return COR_FUNDO;
        }
        float t = Math.min(visitas, 8) / 8f;
        int r = (int) (20 + t * 35);
        int verde = (int) (55 + t * 170);
        int b = (int) (40 + t * 25);
        return new Color(Math.min(r, 255), Math.min(verde, 255), Math.min(b, 255));
    }

    private void disco(Graphics2D g2, int px, int py, int cs, Color cor) {
        int margem = Math.max(2, cs / 6);
        g2.setColor(cor);
        g2.fillOval(px + margem, py + margem, cs - 2 * margem, cs - 2 * margem);
    }

    private static Set<String> chavesCoord(List<Coordenada> coords) {
        Set<String> chaves = new HashSet<>();
        for (Coordenada c : coords) {
            if (c != null) {
                chaves.add(c.chave());
            }
        }
        return chaves;
    }

    private static Set<String> chavesCofres(List<Cofre> cofres) {
        Set<String> chaves = new HashSet<>();
        for (Cofre c : cofres) {
            if (c != null) {
                chaves.add(c.getX() + "," + c.getY());
            }
        }
        return chaves;
    }

    private static Set<String> chavesRivais(List<RoboRival> rivais) {
        Set<String> chaves = new HashSet<>();
        for (RoboRival r : rivais) {
            if (r != null) {
                chaves.add(r.getX() + "," + r.getY());
            }
        }
        return chaves;
    }
}
