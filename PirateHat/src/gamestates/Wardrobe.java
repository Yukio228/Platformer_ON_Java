package gamestates;

import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import entities.PlayerSkin;
import main.Game;
import ui.AssetText;
import utilz.LoadSave;

public class Wardrobe extends State implements Statemethods {

	private final PlayerSkin[] skins = PlayerSkin.getWardrobeSkins();
	private BufferedImage backgroundImg;
	private BufferedImage goldIcon;
	private BufferedImage[][] previewFrames;
	private Rectangle[] skinCards;
	private Rectangle backButton;
	private int panelX, panelY, panelW, panelH;
	private int selectedSkinIndex;
	private int mouseOverIndex = -1;
	private int mousePressedIndex = -1;
	private boolean backMouseOver, backMousePressed;
	private int aniTick, aniIndex;
	private int noticeTicks;
	private float noticeAlpha;
	private String noticeText = "";

	public Wardrobe(Game game) {
		super(game);
		loadImgs();
		initBounds();
		selectedSkinIndex = getCurrentSkinIndex();
	}

	private void loadImgs() {
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
		goldIcon = LoadSave.GetSpriteAtlas(LoadSave.ITEM_GOLD_COIN);
		previewFrames = new BufferedImage[skins.length][5];

		for (int i = 0; i < skins.length; i++) {
			BufferedImage atlas = LoadSave.GetSpriteAtlas(skins[i].getAtlasFile());
			for (int j = 0; j < previewFrames[i].length; j++)
				previewFrames[i][j] = atlas.getSubimage(j * 64, 0, 64, 40);
		}
	}

	private void initBounds() {
		panelW = (int) (620 * Game.SCALE);
		panelH = (int) (348 * Game.SCALE);
		panelX = Game.GAME_WIDTH / 2 - panelW / 2;
		panelY = (int) (24 * Game.SCALE);

		int cardW = (int) (124 * Game.SCALE);
		int cardH = (int) (178 * Game.SCALE);
		int gap = (int) (22 * Game.SCALE);
		int totalW = skins.length * cardW + (skins.length - 1) * gap;
		int startX = Game.GAME_WIDTH / 2 - totalW / 2;
		int cardY = panelY + (int) (96 * Game.SCALE);

		skinCards = new Rectangle[skins.length];
		for (int i = 0; i < skinCards.length; i++)
			skinCards[i] = new Rectangle(startX + i * (cardW + gap), cardY, cardW, cardH);

		int backW = (int) (92 * Game.SCALE);
		int backH = (int) (34 * Game.SCALE);
		backButton = new Rectangle(panelX + panelW / 2 - backW / 2, panelY + panelH - (int) (55 * Game.SCALE), backW, backH);
	}

	@Override
	public void update() {
		aniTick++;
		if (aniTick >= 18) {
			aniTick = 0;
			aniIndex = (aniIndex + 1) % 5;
		}
		updateNotice();
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
		g.setColor(new Color(0, 0, 0, 80));
		g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

		drawBoard(g);
		drawTitle(g);
		drawGoldBalance(g);

		for (int i = 0; i < skinCards.length; i++)
			drawSkinCard(g, i);

		drawBackButton(g);
		drawNotice(g);
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
		int titleW = (int) (170 * Game.SCALE);
		int titleH = (int) (26 * Game.SCALE);
		int titleX = panelX + panelW / 2 - titleW / 2;
		int titleY = panelY + (int) (30 * Game.SCALE);

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

	private void drawGoldBalance(Graphics g) {
		int balanceW = (int) (138 * Game.SCALE);
		int balanceH = (int) (24 * Game.SCALE);
		int balanceX = panelX + panelW - balanceW - (int) (36 * Game.SCALE);
		int balanceY = panelY + (int) (31 * Game.SCALE);

		g.setColor(new Color(0, 0, 0, 85));
		g.fillRect(balanceX + (int) (2 * Game.SCALE), balanceY + (int) (3 * Game.SCALE), balanceW, balanceH);
		g.setColor(new Color(44, 38, 43));
		g.fillRect(balanceX, balanceY, balanceW, balanceH);
		g.setColor(new Color(90, 143, 91));
		g.fillRect(balanceX + (int) (4 * Game.SCALE), balanceY + (int) (4 * Game.SCALE), balanceW - (int) (8 * Game.SCALE), balanceH - (int) (8 * Game.SCALE));

		Font oldFont = g.getFont();
		g.setFont(new Font("Arial", Font.BOLD, (int) (9 * Game.SCALE)));
		FontMetrics fm = g.getFontMetrics();
		String label = "Золото:";
		int labelX = balanceX + (int) (10 * Game.SCALE);
		int baselineY = balanceY + balanceH / 2 + fm.getAscent() / 2 - (int) (2 * Game.SCALE);
		g.setColor(new Color(255, 238, 174));
		g.drawString(label, labelX, baselineY);

		int iconSize = (int) (12 * Game.SCALE);
		int iconX = labelX + fm.stringWidth(label) + (int) (8 * Game.SCALE);
		int iconY = balanceY + balanceH / 2 - iconSize / 2;
		g.drawImage(goldIcon, iconX, iconY, iconSize, iconSize, null);

		String amount = String.valueOf(game.getSaveManager().getGoldBalance());
		g.setColor(Color.WHITE);
		g.drawString(amount, iconX + iconSize + (int) (6 * Game.SCALE), baselineY);
		g.setFont(oldFont);
	}

	private void drawSkinCard(Graphics g, int index) {
		Rectangle r = skinCards[index];
		PlayerSkin skin = skins[index];
		boolean equipped = game.getPlaying().getPlayer().getSkin() == skin;
		boolean purchased = game.getSaveManager().isSkinPurchased(skin);
		boolean locked = !purchased;
		boolean affordable = game.getSaveManager().canAfford(skin.getCost());
		boolean selected = selectedSkinIndex == index;
		boolean hovered = mouseOverIndex == index;

		g.setColor(new Color(0, 0, 0, 85));
		g.fillRect(r.x + (int) (4 * Game.SCALE), r.y + (int) (5 * Game.SCALE), r.width, r.height);

		g.setColor(equipped ? new Color(68, 126, 90) : locked ? new Color(48, 65, 60) : new Color(52, 86, 72));
		g.fillRect(r.x, r.y, r.width, r.height);
		g.setColor(hovered ? new Color(103, 166, 102) : locked ? new Color(73, 112, 84) : new Color(87, 154, 100));
		g.fillRect(r.x + (int) (5 * Game.SCALE), r.y + (int) (5 * Game.SCALE), r.width - (int) (10 * Game.SCALE), r.height - (int) (10 * Game.SCALE));

		g.setColor(new Color(31, 45, 43, 210));
		g.fillRect(r.x + (int) (12 * Game.SCALE), r.y + (int) (16 * Game.SCALE), r.width - (int) (24 * Game.SCALE), (int) (76 * Game.SCALE));

		BufferedImage preview = previewFrames[index][aniIndex];
		int previewW = (int) (96 * Game.SCALE);
		int previewH = (int) (60 * Game.SCALE);
		int previewX = r.x + r.width / 2 - previewW / 2;
		int previewY = r.y + (int) (25 * Game.SCALE);
		g.drawImage(preview, previewX, previewY, previewW, previewH, null);

		if (selected || equipped || hovered) {
			g.setColor(equipped ? new Color(255, 226, 103) : new Color(210, 235, 210));
			g.drawRect(r.x - (int) (2 * Game.SCALE), r.y - (int) (2 * Game.SCALE), r.width + (int) (4 * Game.SCALE), r.height + (int) (4 * Game.SCALE));
			g.drawRect(r.x + (int) (3 * Game.SCALE), r.y + (int) (3 * Game.SCALE), r.width - (int) (6 * Game.SCALE), r.height - (int) (6 * Game.SCALE));
		}

		AssetText.drawCentered(g, skin.getDisplayName(), r.x + r.width / 2, r.y + (int) (101 * Game.SCALE), 2);

		if (locked)
			drawSkinPrice(g, r, skin, affordable);

		String buttonText = equipped ? "ВЫБРАН" : locked ? "КУПИТЬ" : "ВЫБРАТЬ";
		boolean disabled = equipped || (locked && !affordable);
		Color buttonColor = equipped ? new Color(38, 82, 54, 220) : disabled ? new Color(71, 68, 66, 210) : new Color(66, 73, 60, 220);
		Color textColor = disabled && !equipped ? new Color(180, 178, 170) : Color.WHITE;
		drawCardButton(g, r, buttonText, buttonColor, textColor);
	}

	private void drawSkinPrice(Graphics g, Rectangle r, PlayerSkin skin, boolean affordable) {
		String price = String.valueOf(skin.getCost());
		Font oldFont = g.getFont();
		g.setFont(new Font("Arial", Font.BOLD, (int) (8 * Game.SCALE)));
		FontMetrics fm = g.getFontMetrics();
		int iconSize = (int) (10 * Game.SCALE);
		int gap = (int) (4 * Game.SCALE);
		int priceW = fm.stringWidth(price);
		int contentW = priceW + gap + iconSize;
		int badgeW = Math.max((int) (38 * Game.SCALE), contentW + (int) (12 * Game.SCALE));
		int badgeH = (int) (16 * Game.SCALE);
		int badgeX = r.x + r.width / 2 - badgeW / 2;
		int badgeY = r.y + (int) (120 * Game.SCALE);
		int contentX = badgeX + (badgeW - contentW) / 2;
		int baselineY = badgeY + (badgeH - fm.getHeight()) / 2 + fm.getAscent();
		int iconY = badgeY + (badgeH - iconSize) / 2;

		g.setColor(new Color(24, 28, 25, 160));
		g.fillRoundRect(badgeX, badgeY, badgeW, badgeH, (int) (4 * Game.SCALE), (int) (4 * Game.SCALE));
		g.setColor(affordable ? new Color(255, 235, 170) : new Color(190, 184, 170));
		g.drawString(price, contentX, baselineY);
		g.drawImage(goldIcon, contentX + priceW + gap, iconY, iconSize, iconSize, null);
		g.setFont(oldFont);
	}

	private void drawCardButton(Graphics g, Rectangle r, String text, Color buttonColor, Color textColor) {
		int x = r.x + (int) (10 * Game.SCALE);
		int y = r.y + r.height - (int) (32 * Game.SCALE);
		int w = r.width - (int) (20 * Game.SCALE);
		int h = (int) (20 * Game.SCALE);

		g.setColor(buttonColor);
		g.fillRect(x, y, w, h);
		g.setColor(new Color(25, 28, 25, 150));
		g.drawRect(x, y, w, h);

		drawFittedText(g, text, x, y, w, h, (int) (8 * Game.SCALE), textColor);
	}

	private void drawFittedText(Graphics g, String text, int x, int y, int w, int h, int maxSize, Color color) {
		Font oldFont = g.getFont();
		int size = maxSize;
		int minSize = Math.max(8, (int) (6 * Game.SCALE));
		FontMetrics fm;

		do {
			g.setFont(new Font("Arial", Font.BOLD, size));
			fm = g.getFontMetrics();
			if (fm.stringWidth(text) <= w - (int) (10 * Game.SCALE) || size <= minSize)
				break;
			size--;
		} while (true);

		g.setColor(color);
		g.drawString(text, x + w / 2 - fm.stringWidth(text) / 2, y + h / 2 + fm.getAscent() / 2 - (int) (2 * Game.SCALE));
		g.setFont(oldFont);
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

	private void drawNotice(Graphics g) {
		if (noticeText.isEmpty() || noticeAlpha <= 0)
			return;

		Graphics2D g2 = (Graphics2D) g;
		Composite oldComposite = g2.getComposite();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, noticeAlpha)));

		int noticeW = (int) (210 * Game.SCALE);
		int noticeH = (int) (24 * Game.SCALE);
		int noticeX = panelX + panelW / 2 - noticeW / 2;
		int noticeY = panelY + panelH - (int) (52 * Game.SCALE);

		g2.setColor(new Color(0, 0, 0, 95));
		g2.fillRect(noticeX + (int) (2 * Game.SCALE), noticeY + (int) (3 * Game.SCALE), noticeW, noticeH);
		g2.setColor(new Color(44, 38, 43, 235));
		g2.fillRect(noticeX, noticeY, noticeW, noticeH);
		g2.setColor(new Color(174, 86, 83, 220));
		g2.fillRect(noticeX + (int) (4 * Game.SCALE), noticeY + (int) (4 * Game.SCALE), noticeW - (int) (8 * Game.SCALE), noticeH - (int) (8 * Game.SCALE));

		Font oldFont = g2.getFont();
		g2.setFont(new Font("Arial", Font.BOLD, (int) (10 * Game.SCALE)));
		FontMetrics fm = g2.getFontMetrics();
		g2.setColor(Color.WHITE);
		g2.drawString(noticeText, noticeX + noticeW / 2 - fm.stringWidth(noticeText) / 2, noticeY + noticeH / 2 + fm.getAscent() / 2 - (int) (2 * Game.SCALE));
		g2.setFont(oldFont);
		g2.setComposite(oldComposite);
	}

	private void updateNotice() {
		if (noticeText.isEmpty() && noticeAlpha <= 0)
			return;

		if (noticeTicks > 0)
			noticeTicks--;

		float targetAlpha = noticeTicks > 0 ? 1f : 0f;
		noticeAlpha += (targetAlpha - noticeAlpha) * 0.12f;
		if (noticeTicks <= 0 && noticeAlpha < 0.03f) {
			noticeAlpha = 0;
			noticeText = "";
		}
	}

	private void showNotice(String text) {
		noticeText = text;
		noticeTicks = 360;
		noticeAlpha = Math.max(noticeAlpha, 0.35f);
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
			handleSkinAction(releasedIndex);
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

	private void handleSkinAction(int index) {
		if (index < 0 || index >= skins.length)
			return;

		PlayerSkin skin = skins[index];
		if (game.getPlaying().getPlayer().getSkin() == skin)
			return;

		if (!game.getSaveManager().isSkinPurchased(skin)) {
			if (!game.getSaveManager().purchaseSkin(skin, skin.getCost()))
				showNotice("Недостаточно золота");
			return;
		}

		applySelectedSkin();
	}

	private void applySelectedSkin() {
		if (!game.getSaveManager().isSkinPurchased(skins[selectedSkinIndex]))
			return;

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
			handleSkinAction(selectedSkinIndex);
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}
