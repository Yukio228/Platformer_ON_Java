package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import gamestates.Gamestate;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

public class GameCompletedOverlay {

	private Playing playing;
	private BufferedImage backgroundImg;
	private ArrayList<ShowEntity> entitiesList;
	private float titleY;
	private int aniTick;

	public GameCompletedOverlay(Playing playing) {
		this.playing = playing;
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
		loadEntities();
		reset();
	}

	private void loadEntities() {
		entitiesList = new ArrayList<>();
		entitiesList.add(new ShowEntity(getIdleAni(LoadSave.GetSpriteAtlas(LoadSave.PLAYER_ATLAS), 5, 64, 40), (int) (Game.GAME_WIDTH * 0.09f), (int) (Game.GAME_HEIGHT * 0.74f), 4));
		entitiesList.add(new ShowEntity(getIdleAni(LoadSave.GetSpriteAtlas(LoadSave.CRABBY_SPRITE), 9, 72, 32), (int) (Game.GAME_WIDTH * 0.24f), (int) (Game.GAME_HEIGHT * 0.79f), 4));
		entitiesList.add(new ShowEntity(getIdleAni(LoadSave.GetSpriteAtlas(LoadSave.PINKSTAR_ATLAS), 8, 34, 30), (int) (Game.GAME_WIDTH * 0.70f), (int) (Game.GAME_HEIGHT * 0.78f), 4));
		entitiesList.add(new ShowEntity(getIdleAni(LoadSave.GetSpriteAtlas(LoadSave.SHARK_ATLAS), 8, 34, 30), (int) (Game.GAME_WIDTH * 0.82f), (int) (Game.GAME_HEIGHT * 0.78f), 4));
	}

	private BufferedImage[] getIdleAni(BufferedImage atlas, int spritesAmount, int width, int height) {
		BufferedImage[] arr = new BufferedImage[spritesAmount];
		for (int i = 0; i < spritesAmount; i++)
			arr[i] = atlas.getSubimage(width * i, 0, width, height);
		return arr;
	}

	public void reset() {
		titleY = Game.GAME_HEIGHT + (int) (70 * Game.SCALE);
		aniTick = 0;
	}

	public void update() {
		aniTick++;
		float targetY = Game.GAME_HEIGHT * 0.35f;
		if (titleY > targetY)
			titleY -= 0.45f;

		for (ShowEntity se : entitiesList)
			se.update();
	}

	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

		g.setColor(new Color(0, 0, 0, 75));
		g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

		drawStars(g);
		drawTheEndText(g);

		for (ShowEntity se : entitiesList)
			se.draw(g);

		drawHint(g);
	}

	private void drawStars(Graphics g) {
		g.setColor(new Color(255, 236, 168, 140));
		for (int i = 0; i < 22; i++) {
			int x = (i * 73 + aniTick / 3) % Game.GAME_WIDTH;
			int y = (int) ((44 + i * 31) * Game.SCALE) % (int) (Game.GAME_HEIGHT * 0.62f);
			int size = 1 + ((aniTick / 20 + i) % 2);
			g.fillRect(x, y, size, size);
		}
	}

	private void drawTheEndText(Graphics g) {
		Font oldFont = g.getFont();
		g.setFont(new Font("Arial", Font.BOLD, (int) (48 * Game.SCALE)));
		FontMetrics fm = g.getFontMetrics();

		String text = "THE END";
		int x = Game.GAME_WIDTH / 2 - fm.stringWidth(text) / 2;
		int y = (int) titleY;

		g.setColor(new Color(42, 22, 34, 185));
		g.drawString(text, x + (int) (4 * Game.SCALE), y + (int) (5 * Game.SCALE));
		g.setColor(new Color(156, 72, 83));
		g.drawString(text, x - (int) (2 * Game.SCALE), y);
		g.setColor(new Color(255, 238, 190));
		g.drawString(text, x, y - (int) (2 * Game.SCALE));

		int underlineW = fm.stringWidth(text);
		int underlineY = y + (int) (12 * Game.SCALE);
		g.setColor(new Color(255, 214, 120, 210));
		g.fillRect(x + underlineW / 8, underlineY, underlineW - underlineW / 4, (int) (3 * Game.SCALE));

		g.setFont(oldFont);
	}

	private void drawHint(Graphics g) {
		if (titleY > Game.GAME_HEIGHT * 0.38f)
			return;

		Font oldFont = g.getFont();
		g.setFont(new Font("Arial", Font.BOLD, (int) (12 * Game.SCALE)));
		FontMetrics fm = g.getFontMetrics();
		String text = "CLICK OR ESC";
		int x = Game.GAME_WIDTH / 2 - fm.stringWidth(text) / 2;
		int y = Game.GAME_HEIGHT - (int) (38 * Game.SCALE);

		g.setColor(new Color(24, 18, 25, 180));
		g.fillRect(x - (int) (10 * Game.SCALE), y - fm.getAscent(), fm.stringWidth(text) + (int) (20 * Game.SCALE), fm.getHeight());
		g.setColor(new Color(244, 226, 178));
		g.drawString(text, x, y);
		g.setFont(oldFont);
	}

	public void returnToMenu() {
		playing.resetAll();
		playing.resetGameCompleted();
		playing.setGamestate(Gamestate.MENU);
		reset();
	}

	public void mouseMoved(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
		returnToMenu();
	}

	public void mousePressed(MouseEvent e) {
	}

	private class ShowEntity {
		private BufferedImage[] idleAnimation;
		private int x, y, scale, aniIndex, aniTick;

		private ShowEntity(BufferedImage[] idleAnimation, int x, int y, int scale) {
			this.idleAnimation = idleAnimation;
			this.x = x;
			this.y = y;
			this.scale = scale;
		}

		private void draw(Graphics g) {
			BufferedImage frame = idleAnimation[aniIndex];
			g.drawImage(frame, x, y, frame.getWidth() * scale, frame.getHeight() * scale, null);
		}

		private void update() {
			aniTick++;
			if (aniTick >= 25) {
				aniTick = 0;
				aniIndex++;
				if (aniIndex >= idleAnimation.length)
					aniIndex = 0;
			}
		}
	}
}
