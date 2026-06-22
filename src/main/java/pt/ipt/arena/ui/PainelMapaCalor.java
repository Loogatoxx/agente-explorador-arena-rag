package pt.ipt.arena.ui;

import javax.swing.JPanel;

/**
 * Encapsula todo o código visual (Java Swing / Graphics2D) que desenha o
 * dashboard de telemetria e o radar / mapa de calor na máquina local.
 *
 * Responsabilidade única (SRP): apenas renderização. Quem desenha ecrãs NÃO
 * comunica com a rede da Arena nem com o motor de IA.
 */
public class PainelMapaCalor extends JPanel {

    private static final long serialVersionUID = 1L;

    // TODO (etapa: UI) sobrepor paintComponent(Graphics) e desenhar:
    //   - a grelha bidimensional (X, Y);
    //   - a posicao atual do agente e o seu HP;
    //   - as paredes (objetos_fixos), recursos e cofres visiveis;
    //   - o mapa de calor a partir do historicoVisitas (Map<String,Integer>).
}
