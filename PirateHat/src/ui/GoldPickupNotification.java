package ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import main.Game;
import utilz.LoadSave;

public class GoldPickupNotification {

	private static final int VISIBLE_TICKS = 500;

	private final BufferedImage goldIcon;
	private int amount;
	private int visibleTicks;
	private float alpha;

	public GoldPickupNotification() {
		goldIcon = LoadSave.GetSpriteAtlas(LoadSave.ITEM_GOLD_COIN);
	}

	public void addGold(int amount) {
		if (amount <= 0)
			return;

		this.amount += amount;
		visibleTicks = VISIBLE_TICKS;
		alpha = Math.max(alpha, 0.25f);
	}

	public void update() {
		if (amount <= 0 && alpha <= 0)
			return;

		if (visibleTicks > 0)
			visibleTicks--;

		float targetAlpha = visibleTicks > 0 ? 1f : 0f;
		alpha += (targetAlpha - alpha) * 0.12f;

		if (visibleTicks <= 0 && alpha < 0.03f)
			clear();
	}

	public void draw(Graphics g) {
		if (amount <= 0 || alpha <= 0)
			return;

		Graphics2D g2 = (Graphics2D) g;
		Composite oldComposite = g2.getComposite();
		Object oldHint = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		int panelW = (int) (92 * Game.SCALE);
		int panelH = (int) (28 * Game.SCALE);
		int x = Game.GAME_WIDTH - panelW - (int) (18 * Game.SCALE);
		int y = (int) (58 * Game.SCALE);

		g2.setColor(new Color(14, 24, 20, 185));
		g2.fillRoundRect(x + (int) (3 * Game.SCALE), y + (int) (4 * Game.SCALE), panelW, panelH, (int) (8 * Game.SCALE), (int) (8 * Game.SCALE));
		g2.setColor(new Color(38, 82, 54, 220));
		g2.fillRoundRect(x, y, panelW, panelH, (int) (8 * Game.SCALE), (int) (8 * Game.SCALE));
		g2.setColor(new Color(102, 165, 98, 190));
		g2.drawRoundRect(x + 1, y + 1, panelW - 3, panelH - 3, (int) (8 * Game.SCALE), (int) (8 * Game.SCALE));

		int iconSize = (int) (16 * Game.SCALE);
		int iconX = x + (int) (12 * Game.SCALE);
		int iconY = y + panelH / 2 - iconSize / 2;
		g2.drawImage(goldIcon, iconX, iconY, iconSize, iconSize, null);

		String text = "+" + amount;
		Font oldFont = g2.getFont();
		g2.setFont(new Font("Arial", Font.BOLD, (int) (14 * Game.SCALE)));
		FontMetrics fm = g2.getFontMetrics();
		int textX = iconX + iconSize + (int) (9 * Game.SCALE);
		int textY = y + panelH / 2 + fm.getAscent() / 2 - (int) (2 * Game.SCALE);
		g2.setColor(new Color(35, 20, 10, 160));
		g2.drawString(text, textX + 2, textY + 2);
		g2.setColor(new Color(255, 231, 130));
		g2.drawString(text, textX, textY);
		g2.setFont(oldFont);

		if (oldHint != null)
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint);
		g2.setComposite(oldComposite);
	}

	public void clear() {
		amount = 0;
		visibleTicks = 0;
		alpha = 0;
	}
}
