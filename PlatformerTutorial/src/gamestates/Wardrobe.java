package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import entities.PlayerSkin;
import main.Game;
import ui.AssetText;
import utilz.LoadSave;

public class Wardrobe extends State implements Statemethods {

	private final PlayerSkin[] skins = PlayerSkin.values();
	private BufferedImage backgroundImg;
	private BufferedImage[][] previewFrames;
	private Rectangle[] skinCards;
	private Rectangle backButton;
	private int panelX, panelY, panelW, panelH;
	private int selectedSkinIndex;
	private int mouseOverIndex = -1;
	private int mousePressedIndex = -1;
	private boolean backMouseOver, backMousePressed;
	private int aniTick, aniIndex;

	public Wardrobe(Game game) {
		super(game);
		loadImgs();
		initBounds();
		selectedSkinIndex = getCurrentSkinIndex();
	}

	private void loadImgs() {
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
		previewFrames = new BufferedImage[skins.length][5];

		for (int i = 0; i < skins.length; i++) {
			BufferedImage atlas = LoadSave.GetSpriteAtlas(skins[i].getAtlasFile());
			for (int j = 0; j < previewFrames[i].length; j++)
				previewFrames[i][j] = atlas.getSubimage(j * 64, 0, 64, 40);
		}
	}

	private void initBounds() {
		panelW = (int) (620 * Game.SCALE);
		panelH = (int) (330 * Game.SCALE);
		panelX = Game.GAME_WIDTH / 2 - panelW / 2;
		panelY = (int) (32 * Game.SCALE);

		int cardW = (int) (124 * Game.SCALE);
		int cardH = (int) (168 * Game.SCALE);
		int gap = (int) (18 * Game.SCALE);
		int totalW = skins.length * cardW + (skins.length - 1) * gap;
		int startX = Game.GAME_WIDTH / 2 - totalW / 2;
		int cardY = panelY + (int) (94 * Game.SCALE);

		skinCards = new Rectangle[skins.length];
		for (int i = 0; i < skinCards.length; i++)
			skinCards[i] = new Rectangle(startX + i * (cardW + gap), cardY, cardW, cardH);

		backButton = new Rectangle(panelX + (int) (30 * Game.SCALE), panelY + panelH - (int) (55 * Game.SCALE), (int) (92 * Game.SCALE), (int) (34 * Game.SCALE));
	}

	@Override
	public void update() {
		aniTick++;
		if (aniTick >= 18) {
			aniTick = 0;
			aniIndex = (aniIndex + 1) % 5;
		}
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
		g.setColor(new Color(0, 0, 0, 80));
		g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

		drawBoard(g);
		drawTitle(g);

		for (int i = 0; i < skinCards.length; i++)
			drawSkinCard(g, i);

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
		for (int i = 0; i < 7; i++) {
			int lineY = panelY + (int) ((78 + i * 31) * Game.SCALE);
			g.fillRect(panelX + (int) (28 * Game.SCALE), lineY, panelW - (int) (56 * Game.SCALE), (int) (2 * Game.SCALE));
		}
	}

	private void drawTitle(Graphics g) {
		int titleW = (int) (176 * Game.SCALE);
		int titleH = (int) (26 * Game.SCALE);
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

		AssetText.drawCenteredInRect(g, "WARDROBE", titleX, titleY, titleW, titleH, 2);
	}

	private void drawSkinCard(Graphics g, int index) {
		Rectangle r = skinCards[index];
		boolean equipped = game.getPlaying().getPlayer().getSkin() == skins[index];
		boolean selected = selectedSkinIndex == index;
		boolean hovered = mouseOverIndex == index;

		g.setColor(new Color(0, 0, 0, 85));
		g.fillRect(r.x + (int) (4 * Game.SCALE), r.y + (int) (5 * Game.SCALE), r.width, r.height);

		g.setColor(equipped ? new Color(68, 126, 90) : new Color(52, 86, 72));
		g.fillRect(r.x, r.y, r.width, r.height);
		g.setColor(hovered ? new Color(103, 166, 102) : new Color(87, 154, 100));
		g.fillRect(r.x + (int) (5 * Game.SCALE), r.y + (int) (5 * Game.SCALE), r.width - (int) (10 * Game.SCALE), r.height - (int) (10 * Game.SCALE));

		g.setColor(new Color(31, 45, 43, 210));
		g.fillRect(r.x + (int) (12 * Game.SCALE), r.y + (int) (18 * Game.SCALE), r.width - (int) (24 * Game.SCALE), (int) (76 * Game.SCALE));

		BufferedImage preview = previewFrames[index][aniIndex];
		int previewW = (int) (96 * Game.SCALE);
		int previewH = (int) (60 * Game.SCALE);
		int previewX = r.x + r.width / 2 - previewW / 2;
		int previewY = r.y + (int) (27 * Game.SCALE);
		g.drawImage(preview, previewX, previewY, previewW, previewH, null);

		if (selected || equipped || hovered) {
			g.setColor(equipped ? new Color(255, 226, 103) : new Color(210, 235, 210));
			g.drawRect(r.x - (int) (2 * Game.SCALE), r.y - (int) (2 * Game.SCALE), r.width + (int) (4 * Game.SCALE), r.height + (int) (4 * Game.SCALE));
			g.drawRect(r.x + (int) (3 * Game.SCALE), r.y + (int) (3 * Game.SCALE), r.width - (int) (6 * Game.SCALE), r.height - (int) (6 * Game.SCALE));
		}

		AssetText.drawCentered(g, skins[index].getDisplayName(), r.x + r.width / 2, r.y + (int) (111 * Game.SCALE), 2);

		g.setColor(equipped ? new Color(38, 82, 54, 220) : new Color(66, 73, 60, 220));
		g.fillRect(r.x + (int) (10 * Game.SCALE), r.y + r.height - (int) (32 * Game.SCALE), r.width - (int) (20 * Game.SCALE), (int) (18 * Game.SCALE));
		AssetText.drawCentered(g, equipped ? "EQUIPPED" : "SELECT", r.x + r.width / 2, r.y + r.height - (int) (30 * Game.SCALE), 2);
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
		mousePressedIndex = getSkinIndex(e);
		backMousePressed = backButton.contains(e.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		int releasedIndex = getSkinIndex(e);

		if (backMousePressed && backButton.contains(e.getPoint())) {
			resetMouseState();
			setGamestate(Gamestate.MENU);
			return;
		}

		if (mousePressedIndex >= 0 && mousePressedIndex == releasedIndex) {
			selectedSkinIndex = releasedIndex;
			applySelectedSkin();
		}

		resetMouseState();
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseOverIndex = getSkinIndex(e);
		backMouseOver = backButton.contains(e.getPoint());
	}

	private int getSkinIndex(MouseEvent e) {
		for (int i = 0; i < skinCards.length; i++)
			if (skinCards[i].contains(e.getPoint()))
				return i;
		return -1;
	}

	private int getCurrentSkinIndex() {
		PlayerSkin currentSkin = game.getPlaying().getPlayer().getSkin();
		for (int i = 0; i < skins.length; i++)
			if (skins[i] == currentSkin)
				return i;
		return 0;
	}

	private void applySelectedSkin() {
		game.getPlaying().getPlayer().setSkin(skins[selectedSkinIndex]);
		game.getSaveManager().savePlayerSkin(skins[selectedSkinIndex]);
	}

	private void resetMouseState() {
		mousePressedIndex = -1;
		backMousePressed = false;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_ESCAPE:
			setGamestate(Gamestate.MENU);
			break;
		case KeyEvent.VK_LEFT, KeyEvent.VK_A:
			selectedSkinIndex = Math.max(0, selectedSkinIndex - 1);
			break;
		case KeyEvent.VK_RIGHT, KeyEvent.VK_D:
			selectedSkinIndex = Math.min(skins.length - 1, selectedSkinIndex + 1);
			break;
		case KeyEvent.VK_ENTER, KeyEvent.VK_SPACE:
			applySelectedSkin();
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}
