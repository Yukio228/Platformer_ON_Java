package main;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

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
	private Cursor defaultCursor;
	private Cursor hiddenCursor;
	private boolean cursorHidden;

	public GamePanel(Game game) {
		mouseInputs = new MouseInputs(this);
		this.game = game;
		initCursors();
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
		updateCursorVisibility();
		Graphics2D g2 = (Graphics2D) g.create();
		g2.scale(getRenderScaleX(), getRenderScaleY());
		game.render(g2);
		g2.dispose();
	}

	public Game getGame() {
		return game;
	}

	public MouseEvent toGameMouseEvent(MouseEvent e) {
		int gameX = (int) (e.getX() / getRenderScaleX());
		int gameY = (int) (e.getY() / getRenderScaleY());

		return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(), gameX, gameY, e.getClickCount(), e.isPopupTrigger(), e.getButton());
	}

	private float getRenderScaleX() {
		float scale = getWidth() / (float) GAME_WIDTH;
		return scale <= 0 ? 1 : scale;
	}

	private float getRenderScaleY() {
		float scale = getHeight() / (float) GAME_HEIGHT;
		return scale <= 0 ? 1 : scale;
	}

	private void initCursors() {
		defaultCursor = Cursor.getDefaultCursor();
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		hiddenCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "hidden");
	}

	private void updateCursorVisibility() {
		boolean shouldHide = game.shouldHideMouseCursor();
		if (cursorHidden == shouldHide)
			return;

		setCursor(shouldHide ? hiddenCursor : defaultCursor);
		cursorHidden = shouldHide;
	}

}
