package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import main.Game;
import ui.AssetText;
import utilz.LoadSave;

public class LevelSelect extends State implements Statemethods {

	private BufferedImage backgroundImg;
	private BufferedImage mapImg;
	private BufferedImage keyImg;
	private Rectangle[] levelCards;
	private Rectangle backButton;
	private int panelX, panelY, panelW, panelH;
	private int selectedLevelIndex;
	private int mouseOverIndex = -1;
	private int mousePressedIndex = -1;
	private boolean backMouseOver;
	private boolean backMousePressed;

	public LevelSelect(Game game) {
		super(game);
		loadImgs();
		initBounds();
	}

	private void loadImgs() {
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
		mapImg = LoadSave.GetSpriteAtlas(LoadSave.ITEM_MAP);
		keyImg = LoadSave.GetSpriteAtlas(LoadSave.ITEM_KEY);
	}

	private void initBounds() {
		int amountOfLevels = game.getPlaying().getLevelManager().getAmountOfLevels();
		levelCards = new Rectangle[amountOfLevels];

		panelW = (int) (530 * Game.SCALE);
		panelH = (int) (300 * Game.SCALE);
		panelX = Game.GAME_WIDTH / 2 - panelW / 2;
		panelY = (int) (42 * Game.SCALE);

		int cardW = (int) (74 * Game.SCALE);
		int cardH = (int) (96 * Game.SCALE);
		int gap = (int) (14 * Game.SCALE);
		int totalW = amountOfLevels * cardW + (amountOfLevels - 1) * gap;
		int startX = Game.GAME_WIDTH / 2 - totalW / 2;
		int cardY = panelY + (int) (120 * Game.SCALE);

		for (int i = 0; i < amountOfLevels; i++)
			levelCards[i] = new Rectangle(startX + i * (cardW + gap), cardY, cardW, cardH);

		backButton = new Rectangle(panelX + (int) (30 * Game.SCALE), panelY + panelH - (int) (55 * Game.SCALE), (int) (92 * Game.SCALE), (int) (34 * Game.SCALE));
	}

	@Override
	public void update() {
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

		g.setColor(new Color(0, 0, 0, 70));
		g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
		drawBoard(g);

		drawTitle(g, panelX, panelY, panelW);

		for (int i = 0; i < levelCards.length; i++)
			drawLevelCard(g, i);

		drawBackButton(g);
	}

	private void drawBoard(Graphics g) {
		g.setColor(new Color(0, 0, 0, 100));
		g.fillRect(panelX + (int) (7 * Game.SCALE), panelY + (int) (8 * Game.SCALE), panelW, panelH);

		g.setColor(new Color(34, 56, 51));
		g.fillRect(panelX, panelY, panelW, panelH);
		g.setColor(new Color(94, 154, 108));
		g.fillRect(panelX + (int) (8 * Game.SCALE), panelY + (int) (8 * Game.SCALE), panelW - (int) (16 * Game.SCALE), panelH - (int) (16 * Game.SCALE));
		g.setColor(new Color(31, 45, 43));
		g.fillRect(panelX + (int) (20 * Game.SCALE), panelY + (int) (22 * Game.SCALE), panelW - (int) (40 * Game.SCALE), panelH - (int) (44 * Game.SCALE));
		g.setColor(new Color(178, 117, 94));
		g.fillRect(panelX + (int) (28 * Game.SCALE), panelY + (int) (30 * Game.SCALE), panelW - (int) (56 * Game.SCALE), panelH - (int) (62 * Game.SCALE));

		g.setColor(new Color(112, 58, 65));
		for (int i = 0; i < 6; i++) {
			int lineY = panelY + (int) ((69 + i * 33) * Game.SCALE);
			g.fillRect(panelX + (int) (28 * Game.SCALE), lineY, panelW - (int) (56 * Game.SCALE), (int) (2 * Game.SCALE));
		}

		g.setColor(new Color(47, 82, 78));
		for (int i = 0; i < 7; i++) {
			int markX = panelX + (int) ((34 + i * 73) * Game.SCALE);
			g.fillRect(markX, panelY + (int) (12 * Game.SCALE), (int) (22 * Game.SCALE), (int) (3 * Game.SCALE));
			g.fillRect(markX + (int) (8 * Game.SCALE), panelY + panelH - (int) (16 * Game.SCALE), (int) (24 * Game.SCALE), (int) (3 * Game.SCALE));
		}
	}

	private void drawTitle(Graphics g, int panelX, int panelY, int panelW) {
		int titleW = (int) (150 * Game.SCALE);
		int titleH = (int) (24 * Game.SCALE);
		int titleX = panelX + panelW / 2 - titleW / 2;
		int titleY = panelY + (int) (38 * Game.SCALE);

		g.setColor(new Color(0, 0, 0, 95));
		g.fillRect(titleX + (int) (3 * Game.SCALE), titleY + (int) (4 * Game.SCALE), titleW, titleH);

		g.setColor(new Color(44, 38, 43));
		g.fillRect(titleX, titleY, titleW, titleH);
		g.setColor(new Color(88, 142, 104));
		g.fillRect(titleX + (int) (4 * Game.SCALE), titleY + (int) (4 * Game.SCALE), titleW - (int) (8 * Game.SCALE), titleH - (int) (8 * Game.SCALE));
		g.setColor(new Color(244, 190, 125));
		g.fillRect(titleX + (int) (12 * Game.SCALE), titleY + (int) (5 * Game.SCALE), titleW - (int) (24 * Game.SCALE), titleH - (int) (10 * Game.SCALE));
		g.setColor(new Color(174, 86, 83));
		g.fillRect(titleX + (int) (12 * Game.SCALE), titleY + titleH - (int) (8 * Game.SCALE), titleW - (int) (24 * Game.SCALE), (int) (2 * Game.SCALE));

		AssetText.drawCenteredInRect(g, "LEVELS", titleX, titleY, titleW, titleH, 2);
	}

	private void drawLevelCard(Graphics g, int index) {
		Rectangle r = levelCards[index];
		boolean unlocked = game.getSaveManager().isLevelUnlocked(index);
		boolean selected = selectedLevelIndex == index;
		boolean hovered = mouseOverIndex == index;

		g.setColor(new Color(0, 0, 0, 80));
		g.fillRect(r.x + (int) (4 * Game.SCALE), r.y + (int) (4 * Game.SCALE), r.width, r.height);

		if (unlocked) {
			g.setColor(selected ? new Color(74, 139, 93) : new Color(52, 86, 72));
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(new Color(87, 154, 100));
			g.fillRect(r.x + (int) (5 * Game.SCALE), r.y + (int) (5 * Game.SCALE), r.width - (int) (10 * Game.SCALE), r.height - (int) (10 * Game.SCALE));
			g.setColor(new Color(50, 100, 76, hovered ? 180 : 145));
			g.fillRect(r.x + (int) (10 * Game.SCALE), r.y + (int) (18 * Game.SCALE), r.width - (int) (20 * Game.SCALE), r.height - (int) (39 * Game.SCALE));
		} else {
			g.setColor(new Color(41, 31, 34, 225));
			g.fillRect(r.x, r.y, r.width, r.height);
			g.setColor(new Color(54, 42, 45, 230));
			g.fillRect(r.x + (int) (5 * Game.SCALE), r.y + (int) (5 * Game.SCALE), r.width - (int) (10 * Game.SCALE), r.height - (int) (10 * Game.SCALE));
			g.setColor(new Color(29, 22, 24, 190));
			g.fillRect(r.x + (int) (10 * Game.SCALE), r.y + (int) (18 * Game.SCALE), r.width - (int) (20 * Game.SCALE), r.height - (int) (39 * Game.SCALE));
		}

		if (selected || hovered) {
			g.setColor(selected ? new Color(255, 226, 103) : new Color(210, 235, 210));
			g.drawRect(r.x - (int) (2 * Game.SCALE), r.y - (int) (2 * Game.SCALE), r.width + (int) (4 * Game.SCALE), r.height + (int) (4 * Game.SCALE));
			g.drawRect(r.x + (int) (3 * Game.SCALE), r.y + (int) (3 * Game.SCALE), r.width - (int) (6 * Game.SCALE), r.height - (int) (6 * Game.SCALE));
		}

		AssetText.drawCentered(g, "LEVEL " + (index + 1), r.x + r.width / 2, r.y + (int) (22 * Game.SCALE), 2);

		int iconSize = unlocked ? (int) (28 * Game.SCALE) : (int) (22 * Game.SCALE);
		BufferedImage icon = unlocked ? mapImg : keyImg;
		int iconX = r.x + r.width / 2 - iconSize / 2;
		int iconY = r.y + (int) (43 * Game.SCALE);
		g.drawImage(icon, iconX, iconY, iconSize, iconSize, null);

		g.setColor(unlocked ? new Color(32, 68, 50, 220) : new Color(32, 24, 26, 220));
		g.fillRect(r.x + (int) (3 * Game.SCALE), r.y + r.height - (int) (25 * Game.SCALE), r.width - (int) (6 * Game.SCALE), (int) (19 * Game.SCALE));
		AssetText.drawCentered(g, unlocked ? "OPEN" : "LOCKED", r.x + r.width / 2, r.y + r.height - (int) (22 * Game.SCALE), 2);
	}

	private void drawBackButton(Graphics g) {
		if (backMousePressed)
			g.setColor(new Color(20, 45, 34, 230));
		else if (backMouseOver)
			g.setColor(new Color(55, 105, 76, 230));
		else
			g.setColor(new Color(34, 75, 55, 220));

		g.fillRect(backButton.x, backButton.y, backButton.width, backButton.height);
		g.setColor(new Color(15, 23, 18));
		g.drawRect(backButton.x, backButton.y, backButton.width, backButton.height);
		g.setColor(new Color(88, 142, 104, 130));
		g.fillRect(backButton.x + (int) (5 * Game.SCALE), backButton.y + (int) (5 * Game.SCALE), backButton.width - (int) (10 * Game.SCALE), (int) (7 * Game.SCALE));

		AssetText.drawCenteredInRect(g, "BACK", backButton.x, backButton.y, backButton.width, backButton.height, 2);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousePressedIndex = getLevelIndex(e);
		backMousePressed = backButton.contains(e.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int releasedIndex = getLevelIndex(e);

		if (backMousePressed && backButton.contains(e.getPoint())) {
			resetMouseState();
			setGamestate(Gamestate.MENU);
			return;
		}

		if (mousePressedIndex >= 0 && mousePressedIndex == releasedIndex) {
			selectedLevelIndex = releasedIndex;
			if (game.getSaveManager().isLevelUnlocked(releasedIndex))
				startSelectedLevel();
		}

		resetMouseState();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseOverIndex = getLevelIndex(e);
		backMouseOver = backButton.contains(e.getPoint());
	}

	private int getLevelIndex(MouseEvent e) {
		for (int i = 0; i < levelCards.length; i++)
			if (levelCards[i].contains(e.getPoint()))
				return i;
		return -1;
	}

	private void resetMouseState() {
		mousePressedIndex = -1;
		backMousePressed = false;
	}

	private void startSelectedLevel() {
		if (!game.getSaveManager().isLevelUnlocked(selectedLevelIndex))
			return;

		game.getPlaying().startLevel(selectedLevelIndex);
		setGamestate(Gamestate.PLAYING);
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
			setGamestate(Gamestate.MENU);
			break;
		case KeyEvent.VK_LEFT, KeyEvent.VK_A:
			selectedLevelIndex = Math.max(0, selectedLevelIndex - 1);
			break;
		case KeyEvent.VK_RIGHT, KeyEvent.VK_D:
			selectedLevelIndex = Math.min(levelCards.length - 1, selectedLevelIndex + 1);
			break;
		case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE:
			startSelectedLevel();
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}
