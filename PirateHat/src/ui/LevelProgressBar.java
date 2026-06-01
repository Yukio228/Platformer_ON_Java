package ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;

import main.Game;

public class LevelProgressBar {

	private static final int SEGMENT_AMOUNT = 10;

	private float shownProgress;
	private int aniTick;

	public void update(float targetProgress) {
		targetProgress = Math.max(0, Math.min(1, targetProgress));
		shownProgress += (targetProgress - shownProgress) * 0.16f;
		aniTick++;
	}

	public void reset(float targetProgress) {
		shownProgress = Math.max(0, Math.min(1, targetProgress));
		aniTick = 0;
	}

	public void draw(Graphics g) {
		int totalW = scaled(232);
		int x = Game.GAME_WIDTH / 2 - totalW / 2;
		int y = scaled(13);

		int mastX = x;
		int mastY = y + scaled(1);
		int barX = x + scaled(34);
		int barY = y + scaled(31);
		int barW = scaled(178);
		int barH = scaled(23);

		drawShadow(g, mastX, mastY, barX, barY, barW, barH);
		drawMast(g, mastX, mastY);
		drawFlag(g, mastX, mastY);
		drawBar(g, barX, barY, barW, barH);
		drawFill(g, barX, barY, barW, barH);
		drawCap(g, barX, barY, barW, barH);
		drawGlowParticles(g, barX, barY, barW, barH);
	}

	private int scaled(int value) {
		return (int) (value * Game.SCALE);
	}

	private void drawShadow(Graphics g, int mastX, int mastY, int barX, int barY, int barW, int barH) {
		g.setColor(new Color(0, 0, 0, 95));
		g.fillRect(mastX + scaled(3), mastY + scaled(5), scaled(21), scaled(54));
		g.fillRect(barX + scaled(4), barY + scaled(4), barW, barH);
	}

	private void drawMast(Graphics g, int x, int y) {
		g.setColor(new Color(18, 16, 16));
		g.fillRect(x + scaled(6), y + scaled(7), scaled(15), scaled(50));

		g.setColor(new Color(61, 48, 43));
		g.fillRect(x + scaled(8), y + scaled(11), scaled(11), scaled(43));
		g.setColor(new Color(151, 82, 38));
		g.fillRect(x + scaled(11), y + scaled(13), scaled(5), scaled(39));
		g.setColor(new Color(222, 133, 58));
		g.fillRect(x + scaled(13), y + scaled(18), scaled(2), scaled(14));

		g.setColor(new Color(35, 36, 40));
		g.fillRect(x + scaled(7), y + scaled(3), scaled(13), scaled(5));
		g.fillRect(x + scaled(5), y + scaled(50), scaled(18), scaled(6));

		g.setColor(new Color(211, 215, 211));
		g.fillRect(x + scaled(9), y, scaled(8), scaled(4));
		g.fillRect(x + scaled(8), y + scaled(9), scaled(12), scaled(5));
		g.fillRect(x + scaled(7), y + scaled(48), scaled(14), scaled(5));

		g.setColor(new Color(9, 10, 12));
		g.fillRect(x + scaled(12), y - scaled(4), scaled(3), scaled(4));
	}

	private void drawFlag(Graphics g, int x, int y) {
		int flagX = x + scaled(17);
		int flagY = y + scaled(17);
		int wave = (aniTick / 10) % 3;
		int w = scaled(35 + wave);
		int h = scaled(15);

		Polygon shadow = new Polygon();
		shadow.addPoint(flagX, flagY + scaled(2));
		shadow.addPoint(flagX + w, flagY + scaled(1));
		shadow.addPoint(flagX + w - scaled(6), flagY + h / 2);
		shadow.addPoint(flagX + w, flagY + h);
		shadow.addPoint(flagX, flagY + h - scaled(2));
		g.setColor(new Color(84, 13, 17));
		g.fillPolygon(shadow);

		g.setColor(new Color(205, 32, 30));
		g.fillRect(flagX + scaled(2), flagY + scaled(3), w - scaled(9), h - scaled(5));
		g.setColor(new Color(255, 72, 39));
		g.fillRect(flagX + scaled(4), flagY + scaled(5), w / 2, scaled(3));
		g.setColor(new Color(122, 18, 20));
		g.fillRect(flagX + scaled(5), flagY + h - scaled(5), w - scaled(16), scaled(3));
	}

	private void drawBar(Graphics g, int x, int y, int w, int h) {
		g.setColor(new Color(10, 9, 9));
		g.fillRect(x, y, w, h);

		g.setColor(new Color(64, 38, 26));
		g.fillRect(x + scaled(3), y + scaled(2), w - scaled(6), h - scaled(4));
		g.setColor(new Color(189, 139, 88));
		g.fillRect(x + scaled(7), y + scaled(4), w - scaled(14), scaled(3));
		g.setColor(new Color(31, 24, 22));
		g.fillRect(x + scaled(7), y + scaled(8), w - scaled(14), h - scaled(13));

		g.setColor(new Color(13, 12, 12));
		g.fillRect(x + scaled(3), y + h - scaled(5), w - scaled(6), scaled(3));

		g.setColor(new Color(214, 215, 207));
		for (int i = 0; i < 5; i++) {
			int boltX = x + scaled(22 + i * 30);
			g.fillRect(boltX, y - scaled(5), scaled(5), scaled(5));
			g.setColor(new Color(64, 65, 70));
			g.fillRect(boltX, y - scaled(1), scaled(5), scaled(2));
			g.setColor(new Color(214, 215, 207));
		}
	}

	private void drawFill(Graphics g, int x, int y, int w, int h) {
		int innerX = x + scaled(10);
		int innerY = y + scaled(9);
		int innerW = w - scaled(22);
		int innerH = h - scaled(14);
		float filledSegments = shownProgress * SEGMENT_AMOUNT;
		int segmentW = (innerW - scaled(2) * (SEGMENT_AMOUNT - 1)) / SEGMENT_AMOUNT;

		for (int i = 0; i < SEGMENT_AMOUNT; i++) {
			float amount = Math.max(0, Math.min(1, filledSegments - i));
			if (amount <= 0)
				continue;

			int sx = innerX + i * (segmentW + scaled(2));
			int currentW = Math.max(1, (int) (segmentW * amount));
			int pulse = (int) (Math.sin((aniTick + i * 6) * 0.18) * 18);

			g.setColor(new Color(48, 114, 14));
			g.fillRect(sx, innerY, currentW, innerH);
			g.setColor(new Color(99, Math.max(185, 225 + pulse), 24));
			g.fillRect(sx + scaled(1), innerY + scaled(1), Math.max(1, currentW - scaled(2)), Math.max(1, innerH - scaled(2)));
			g.setColor(new Color(184, 255, 63, 150));
			g.fillRect(sx + scaled(1), innerY + scaled(1), Math.max(1, currentW - scaled(2)), scaled(2));
		}
	}

	private void drawCap(Graphics g, int x, int y, int w, int h) {
		int capX = x + w - scaled(13);
		g.setColor(new Color(15, 14, 14));
		g.fillRect(capX, y + scaled(3), scaled(15), h - scaled(6));
		g.setColor(new Color(70, 72, 72));
		g.fillRect(capX + scaled(3), y + scaled(6), scaled(7), h - scaled(12));
		g.setColor(new Color(180, 184, 178));
		g.fillRect(capX + scaled(4), y + scaled(7), scaled(4), scaled(4));

		if (shownProgress >= 0.98f)
			drawFire(g, capX + scaled(8), y - scaled(6));
	}

	private void drawFire(Graphics g, int x, int y) {
		int flame = (aniTick / 7) % 2;
		g.setColor(new Color(150, 32, 12, 180));
		g.fillRect(x - scaled(2), y + scaled(8), scaled(9), scaled(9));
		g.setColor(flame == 0 ? new Color(255, 94, 18, 210) : new Color(255, 166, 28, 210));
		g.fillRect(x, y + scaled(4), scaled(5), scaled(11));
		g.setColor(new Color(255, 226, 75, 210));
		g.fillRect(x + scaled(2), y + scaled(7), scaled(2), scaled(6));
	}

	private void drawGlowParticles(Graphics g, int x, int y, int w, int h) {
		if (shownProgress <= 0.05f)
			return;

		int innerX = x + scaled(10);
		int innerW = w - scaled(22);
		int fillEnd = innerX + (int) (innerW * shownProgress);

		for (int i = 0; i < 8; i++) {
			int life = (aniTick + i * 9) % 44;
			if (life > 15)
				continue;

			int px = fillEnd - scaled(3) - (i * scaled(7)) % Math.max(1, (int) (innerW * Math.max(0.15f, shownProgress)));
			int py = y + scaled(3 + ((i * 5 + life) % 18));
			int size = Math.max(1, scaled(1));
			g.setColor(i % 2 == 0 ? new Color(181, 255, 55, 160) : new Color(91, 210, 22, 145));
			g.fillRect(px, py, size, size);
		}
	}
}
