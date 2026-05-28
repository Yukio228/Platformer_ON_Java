package ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import utilz.LoadSave;

public final class AssetText {

	private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	private static final int CELL_W = 10;
	private static final int CELL_H = 11;
	private static final int SPACE_W = 6;
	private static BufferedImage bigFont;

	private AssetText() {
	}

	public static void draw(Graphics g, String text, int x, int y, int scale) {
		ensureLoaded();
		if (g instanceof Graphics2D g2)
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		int currentX = x;
		for (int i = 0; i < text.length(); i++) {
			char c = Character.toUpperCase(text.charAt(i));
			if (c == ' ') {
				currentX += SPACE_W * scale;
				continue;
			}

			int index = CHARS.indexOf(c);
			if (index < 0)
				continue;

			int sx = index * CELL_W;
			g.drawImage(bigFont, currentX, y, currentX + CELL_W * scale, y + CELL_H * scale, sx, 0, sx + CELL_W, CELL_H, null);
			currentX += (CELL_W + 1) * scale;
		}
	}

	public static void drawCentered(Graphics g, String text, int centerX, int y, int scale) {
		draw(g, text, centerX - width(text, scale) / 2, y, scale);
	}

	public static void drawCenteredInRect(Graphics g, String text, int x, int y, int width, int height, int scale) {
		int textX = x + width / 2 - width(text, scale) / 2;
		int textY = y + height / 2 - height(scale) / 2;
		draw(g, text, textX, textY, scale);
	}

	public static int width(String text, int scale) {
		int width = 0;
		boolean hasGlyph = false;
		for (int i = 0; i < text.length(); i++) {
			char c = Character.toUpperCase(text.charAt(i));
			int charWidth = c == ' ' ? SPACE_W : CHARS.indexOf(c) >= 0 ? CELL_W : 0;
			if (charWidth <= 0)
				continue;
			if (hasGlyph)
				width += scale;
			width += charWidth * scale;
			hasGlyph = true;
		}
		return width;
	}

	public static int height(int scale) {
		return CELL_H * scale;
	}

	private static void ensureLoaded() {
		if (bigFont == null)
			bigFont = LoadSave.GetSpriteAtlas(LoadSave.UI_FONT_BIG);
	}
}
