package main;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import inputs.KeyboardInputs;
import inputs.MouseInputs;
import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

public class GamePanel extends JPanel {

	private MouseInputs mouseInputs;
	private Game game;
	private int renderWidth = GAME_WIDTH;
	private int renderHeight = GAME_HEIGHT;

	public GamePanel(Game game) {
		mouseInputs = new MouseInputs(this);
		this.game = game;
		setPanelSize();
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		addKeyListener(new KeyboardInputs(this));
		addMouseListener(mouseInputs);
		addMouseMotionListener(mouseInputs);
	}

	private void setPanelSize() {
		Dimension size = new Dimension(renderWidth, renderHeight);
		setPreferredSize(size);
	}

	public void setRenderSize(int width, int height) {
		renderWidth = width;
		renderHeight = height;
		setPreferredSize(new Dimension(renderWidth, renderHeight));
		setSize(renderWidth, renderHeight);
		revalidate();
		repaint();
	}

	public void updateGame() {

	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.translate(getRenderOffsetX(), getRenderOffsetY());
		g2.scale(getRenderScale(), getRenderScale());
		game.render(g2);
		g2.dispose();
	}

	public Game getGame() {
		return game;
	}

	public MouseEvent toGameMouseEvent(MouseEvent e) {
		float scale = getRenderScale();
		int gameX = (int) ((e.getX() - getRenderOffsetX()) / scale);
		int gameY = (int) ((e.getY() - getRenderOffsetY()) / scale);

		return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), gameX, gameY, e.getClickCount(), e.isPopupTrigger(), e.getButton());
	}

	private float getRenderScale() {
		float scale = Math.min(getWidth() / (float) GAME_WIDTH, getHeight() / (float) GAME_HEIGHT);
		return scale <= 0 ? 1 : scale;
	}

	private int getRenderOffsetX() {
		return (int) ((getWidth() - GAME_WIDTH * getRenderScale()) / 2);
	}

	private int getRenderOffsetY() {
		return (int) ((getHeight() - GAME_HEIGHT * getRenderScale()) / 2);
	}

}
