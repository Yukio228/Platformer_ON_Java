package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import entities.PlayerInventory;
import main.Game;
import utilz.LoadSave;

public class InventoryUI {

	private BufferedImage slotImg;
	private BufferedImage panelImg;
	private BufferedImage[] itemImgs = new BufferedImage[PlayerInventory.ITEM_AMOUNT];
	private static final int[] VISIBLE_ITEMS = { PlayerInventory.RED_POTION, PlayerInventory.BLUE_POTION, PlayerInventory.KEY };
	private static final String[] VISIBLE_ITEM_NAMES = { "HEALTH", "POWER", "KEY" };
	private int mouseOverItem = -1;

	public InventoryUI() {
		loadImgs();
	}

	public void drawHotbar(Graphics g, PlayerInventory inventory) {
		for (int i = 0; i < VISIBLE_ITEMS.length; i++) {
			int itemType = VISIBLE_ITEMS[i];
			Rectangle bounds = getHotbarSlotBounds(i);
			drawSlot(g, inventory, itemType, bounds.x, bounds.y, bounds.width, bounds.height, false, null, mouseOverItem == itemType);
			drawHotkey(g, itemType, bounds);
		}
	}

	public void drawOverlay(Graphics g, PlayerInventory inventory) {
		int panelW = (int) (270 * Game.SCALE);
		int panelH = (int) (165 * Game.SCALE);
		int panelX = Game.GAME_WIDTH / 2 - panelW / 2;
		int panelY = Game.GAME_HEIGHT / 2 - panelH / 2;

		g.setColor(new Color(0, 0, 0, 160));
		g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
		g.drawImage(panelImg, panelX, panelY, panelW, panelH, null);

		Font oldFont = g.getFont();
		g.setFont(new Font("Arial", Font.BOLD, (int) (16 * Game.SCALE)));
		drawCenteredText(g, "INVENTORY", panelX, panelY + (int) (26 * Game.SCALE), panelW, Color.WHITE);

		g.setFont(new Font("Arial", Font.BOLD, (int) (9 * Game.SCALE)));
		for (int i = 0; i < VISIBLE_ITEMS.length; i++) {
			int itemType = VISIBLE_ITEMS[i];
			Rectangle bounds = getOverlaySlotBounds(i);
			drawSlot(g, inventory, itemType, bounds.x, bounds.y, bounds.width, bounds.height, true, VISIBLE_ITEM_NAMES[i], mouseOverItem == itemType);
		}

		g.setFont(oldFont);
	}

	private void drawSlot(Graphics g, PlayerInventory inventory, int itemType, int x, int y, int w, int h, boolean drawName, String itemName, boolean mouseOver) {
		g.drawImage(slotImg, x, y, w, h, null);
		if (mouseOver)
			drawMouseOver(g, x, y, w, h, inventory.getCount(itemType) > 0);

		int iconW = Math.max(1, (int) (itemImgs[itemType].getWidth() * Game.SCALE));
		int iconH = Math.max(1, (int) (itemImgs[itemType].getHeight() * Game.SCALE));
		int iconX = x + w / 2 - iconW / 2;
		int iconY = y + h / 2 - iconH / 2 - (drawName ? (int) (4 * Game.SCALE) : 0);
		g.drawImage(itemImgs[itemType], iconX, iconY, iconW, iconH, null);

		drawCount(g, inventory.getCount(itemType), x, y, w, h);

		if (drawName)
			drawCenteredText(g, itemName, x, y + h + (int) (9 * Game.SCALE), w, new Color(255, 235, 170));
	}

	private void drawMouseOver(Graphics g, int x, int y, int w, int h, boolean hasItem) {
		g.setColor(hasItem ? new Color(255, 226, 104, 95) : new Color(255, 255, 255, 45));
		g.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 8, 8);
		g.setColor(hasItem ? new Color(255, 238, 150) : new Color(170, 170, 175));
		g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, 8, 8);
	}

	private void drawHotkey(Graphics g, int itemType, Rectangle bounds) {
		String key = switch (itemType) {
		case PlayerInventory.RED_POTION -> "Q";
		default -> "";
		};

		if (key.isEmpty())
			return;

		Font oldFont = g.getFont();
		g.setFont(new Font("Arial", Font.BOLD, (int) (8 * Game.SCALE)));
		g.setColor(new Color(25, 15, 12, 185));
		g.fillRect(bounds.x + (int) (3 * Game.SCALE), bounds.y + (int) (3 * Game.SCALE), (int) (10 * Game.SCALE), (int) (10 * Game.SCALE));
		g.setColor(Color.WHITE);
		g.drawString(key, bounds.x + (int) (5 * Game.SCALE), bounds.y + (int) (12 * Game.SCALE));
		g.setFont(oldFont);
	}

	private void drawCount(Graphics g, int count, int x, int y, int w, int h) {
		Font oldFont = g.getFont();
		g.setFont(new Font("Arial", Font.BOLD, (int) (9 * Game.SCALE)));
		String text = String.valueOf(count);
		FontMetrics fm = g.getFontMetrics();
		int tx = x + w - fm.stringWidth(text) - (int) (4 * Game.SCALE);
		int ty = y + h - (int) (4 * Game.SCALE);

		g.setColor(new Color(25, 15, 12, 180));
		g.fillRoundRect(tx - 3, ty - fm.getAscent(), fm.stringWidth(text) + 6, fm.getHeight(), 6, 6);
		g.setColor(Color.WHITE);
		g.drawString(text, tx, ty);
		g.setFont(oldFont);
	}

	private void drawCenteredText(Graphics g, String text, int x, int baselineY, int width, Color color) {
		FontMetrics fm = g.getFontMetrics();
		g.setColor(color);
		g.drawString(text, x + width / 2 - fm.stringWidth(text) / 2, baselineY);
	}

	private void loadImgs() {
		slotImg = LoadSave.GetSpriteAtlas(LoadSave.INVENTORY_SLOT);
		panelImg = LoadSave.GetSpriteAtlas(LoadSave.UI_GREEN_PANEL);
		itemImgs[PlayerInventory.RED_POTION] = LoadSave.GetSpriteAtlas(LoadSave.ITEM_RED_POTION);
		itemImgs[PlayerInventory.BLUE_POTION] = LoadSave.GetSpriteAtlas(LoadSave.ITEM_BLUE_POTION);
		itemImgs[PlayerInventory.KEY] = LoadSave.GetSpriteAtlas(LoadSave.ITEM_KEY);
	}

	public void mouseMoved(MouseEvent e, boolean overlayOpen) {
		mouseOverItem = overlayOpen ? getOverlayItemAt(e.getX(), e.getY()) : getHotbarItemAt(e.getX(), e.getY());
	}

	public void clearMouseOver() {
		mouseOverItem = -1;
	}

	public int getClickedItem(MouseEvent e, boolean overlayOpen) {
		return overlayOpen ? getOverlayItemAt(e.getX(), e.getY()) : getHotbarItemAt(e.getX(), e.getY());
	}

	private int getHotbarItemAt(int mouseX, int mouseY) {
		for (int i = 0; i < VISIBLE_ITEMS.length; i++)
			if (getHotbarSlotBounds(i).contains(mouseX, mouseY))
				return VISIBLE_ITEMS[i];
		return -1;
	}

	private int getOverlayItemAt(int mouseX, int mouseY) {
		for (int i = 0; i < VISIBLE_ITEMS.length; i++)
			if (getOverlaySlotBounds(i).contains(mouseX, mouseY))
				return VISIBLE_ITEMS[i];
		return -1;
	}

	private Rectangle getHotbarSlotBounds(int index) {
		int slotW = (int) (27 * Game.SCALE);
		int slotH = (int) (30 * Game.SCALE);
		int gap = (int) (4 * Game.SCALE);
		int startX = Game.GAME_WIDTH - (slotW + gap) * VISIBLE_ITEMS.length - (int) (12 * Game.SCALE);
		int y = (int) (12 * Game.SCALE);
		return new Rectangle(startX + index * (slotW + gap), y, slotW, slotH);
	}

	private Rectangle getOverlaySlotBounds(int index) {
		int panelW = (int) (270 * Game.SCALE);
		int panelH = (int) (165 * Game.SCALE);
		int panelX = Game.GAME_WIDTH / 2 - panelW / 2;
		int panelY = Game.GAME_HEIGHT / 2 - panelH / 2;
		int slotW = (int) (38 * Game.SCALE);
		int slotH = (int) (42 * Game.SCALE);
		int startX = panelX + (int) (38 * Game.SCALE);
		int startY = panelY + (int) (48 * Game.SCALE);
		int gapX = (int) (52 * Game.SCALE);
		int gapY = (int) (46 * Game.SCALE);
		int col = index % 3;
		int row = index / 3;
		return new Rectangle(startX + col * gapX, startY + row * gapY, slotW, slotH);
	}
}
