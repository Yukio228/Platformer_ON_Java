package entities;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import gamestates.Playing;
import levels.Level;
import utilz.LoadSave;
import static utilz.Constants.EnemyConstants.*;

public class EnemyManager {

	private Playing playing;
	private BufferedImage[][] crabbyArr, pinkstarArr, sharkArr, bossArr, baldPirateArr, cucumberArr, pirateCaptainArr;
	private Level currentLevel;

	public EnemyManager(Playing playing) {
		this.playing = playing;
		loadEnemyImgs();
	}

	public void loadEnemies(Level level) {
		this.currentLevel = level;
	}

	public void update(int[][] lvlData) {
		boolean isAnyActive = false;
		for (Crabby c : currentLevel.getCrabs())
			if (c.isActive()) {
				c.update(lvlData, playing);
				isAnyActive = true;
			}

		for (Pinkstar p : currentLevel.getPinkstars())
			if (p.isActive()) {
				p.update(lvlData, playing);
				isAnyActive = true;
			}

		for (Shark s : currentLevel.getSharks())
			if (s.isActive()) {
				s.update(lvlData, playing);
				isAnyActive = true;
			}

		for (KingPigBoss b : currentLevel.getBosses())
			if (b.isActive()) {
				b.update(lvlData, playing);
				isAnyActive = true;
			}

		for (PirateMob p : currentLevel.getPirateMobs())
			if (p.isActive()) {
				p.update(lvlData, playing);
				isAnyActive = true;
			}

		if (!isAnyActive)
			playing.setLevelCompleted(true);
	}

	public void draw(Graphics g, int xLvlOffset) {
		drawCrabs(g, xLvlOffset);
		drawPinkstars(g, xLvlOffset);
		drawSharks(g, xLvlOffset);
		drawBosses(g, xLvlOffset);
		drawPirateMobs(g, xLvlOffset);
	}

	private void drawPirateMobs(Graphics g, int xLvlOffset) {
		for (PirateMob p : currentLevel.getPirateMobs())
			if (p.isActive()) {
				BufferedImage[][] atlas = getPirateMobAtlas(p.enemyType);
				g.drawImage(atlas[p.getState()][p.getAniIndex()], (int) p.getHitbox().x - xLvlOffset - PIRATE_MOB_DRAWOFFSET_X + p.flipX(),
						(int) p.getHitbox().y - p.getDrawOffsetY(), PIRATE_MOB_WIDTH * p.flipW(), PIRATE_MOB_HEIGHT, null);
//				p.drawHitbox(g, xLvlOffset);
//				p.drawAttackBox(g, xLvlOffset);
			}
	}

	private BufferedImage[][] getPirateMobAtlas(int enemyType) {
		return switch (enemyType) {
		case CUCUMBER -> cucumberArr;
		case PIRATE_CAPTAIN -> pirateCaptainArr;
		default -> baldPirateArr;
		};
	}

	private void drawBosses(Graphics g, int xLvlOffset) {
		for (KingPigBoss b : currentLevel.getBosses())
			if (b.isActive()) {
				if (b.getPhaseIndex() > 1) {
					int pulse = 50 + (int) (Math.abs(Math.sin(b.getPhasePulseTick() / 16.0)) * 70);
					g.setColor(b.getPhaseIndex() == 2 ? new Color(255, 180, 50, pulse) : new Color(255, 55, 45, pulse));
					g.fillOval((int) b.getHitbox().x - xLvlOffset - BOSS_DRAWOFFSET_X / 2, (int) b.getHitbox().y - BOSS_DRAWOFFSET_Y / 2, BOSS_WIDTH, BOSS_HEIGHT);
				}
				g.drawImage(bossArr[b.getState()][b.getAniIndex()], (int) b.getHitbox().x - xLvlOffset - BOSS_DRAWOFFSET_X + b.flipX(),
						(int) b.getHitbox().y - BOSS_DRAWOFFSET_Y, BOSS_WIDTH * b.flipW(), BOSS_HEIGHT, null);
				drawBossHealth(g, b);
//				b.drawHitbox(g, xLvlOffset);
//				b.drawAttackBox(g, xLvlOffset);
			}
	}

	private void drawBossHealth(Graphics g, Enemy boss) {
		int w = (int) (250 * main.Game.SCALE);
		int h = (int) (12 * main.Game.SCALE);
		int x = main.Game.GAME_WIDTH / 2 - w / 2;
		int y = (int) (70 * main.Game.SCALE);
		float healthPercent = Math.max(0, boss.getCurrentHealth() / (float) boss.getMaxHealth());
		int healthW = (int) (healthPercent * (w - (int) (6 * main.Game.SCALE)));

		g.setColor(new Color(0, 0, 0, 130));
		g.fillRect(x + (int) (3 * main.Game.SCALE), y + (int) (3 * main.Game.SCALE), w, h + (int) (8 * main.Game.SCALE));
		g.setColor(new Color(53, 29, 36));
		g.fillRect(x, y, w, h + (int) (8 * main.Game.SCALE));
		g.setColor(new Color(224, 183, 93));
		g.drawRect(x, y, w, h + (int) (8 * main.Game.SCALE));
		g.setColor(new Color(39, 19, 24));
		g.fillRect(x + (int) (3 * main.Game.SCALE), y + (int) (4 * main.Game.SCALE), w - (int) (6 * main.Game.SCALE), h);
		g.setColor(new Color(193, 47, 44));
		g.fillRect(x + (int) (3 * main.Game.SCALE), y + (int) (4 * main.Game.SCALE), healthW, h);
		g.setColor(new Color(255, 103, 70, 170));
		g.fillRect(x + (int) (3 * main.Game.SCALE), y + (int) (4 * main.Game.SCALE), healthW, Math.max(1, h / 3));
	}

	private void drawSharks(Graphics g, int xLvlOffset) {
		for (Shark s : currentLevel.getSharks())
			if (s.isActive()) {
				g.drawImage(sharkArr[s.getState()][s.getAniIndex()], (int) s.getHitbox().x - xLvlOffset - SHARK_DRAWOFFSET_X + s.flipX(),
						(int) s.getHitbox().y - SHARK_DRAWOFFSET_Y, SHARK_WIDTH * s.flipW(), SHARK_HEIGHT, null);
//				s.drawHitbox(g, xLvlOffset);
//				s.drawAttackBox(g, xLvlOffset);
			}
	}

	private void drawPinkstars(Graphics g, int xLvlOffset) {
		for (Pinkstar p : currentLevel.getPinkstars())
			if (p.isActive()) {
				g.drawImage(pinkstarArr[p.getState()][p.getAniIndex()], (int) p.getHitbox().x - xLvlOffset - PINKSTAR_DRAWOFFSET_X + p.flipX(),
						(int) p.getHitbox().y - PINKSTAR_DRAWOFFSET_Y, PINKSTAR_WIDTH * p.flipW(), PINKSTAR_HEIGHT, null);
//				p.drawHitbox(g, xLvlOffset);
			}
	}

	private void drawCrabs(Graphics g, int xLvlOffset) {
		for (Crabby c : currentLevel.getCrabs())
			if (c.isActive()) {

				g.drawImage(crabbyArr[c.getState()][c.getAniIndex()], (int) c.getHitbox().x - xLvlOffset - CRABBY_DRAWOFFSET_X + c.flipX(),
						(int) c.getHitbox().y - CRABBY_DRAWOFFSET_Y, CRABBY_WIDTH * c.flipW(), CRABBY_HEIGHT, null);

//				c.drawHitbox(g, xLvlOffset);
//				c.drawAttackBox(g, xLvlOffset);
			}

	}

	public boolean checkEnemyHit(Rectangle2D.Float attackBox) {
		int damage = 20;

		for (Crabby c : currentLevel.getCrabs())
			if (c.isActive())
				if (c.getState() != DEAD && c.getState() != HIT)
					if (attackBox.intersects(c.getHitbox())) {
						c.hurt(damage);
						c.alertFromDamage(playing.getPlayer(), playing);
						recordDefeatIfDead(c);
						return true;
					}

		for (Pinkstar p : currentLevel.getPinkstars())
			if (p.isActive()) {
				if (p.getState() == ATTACK && p.getAniIndex() >= 3)
					return false;
				else {
					if (p.getState() != DEAD && p.getState() != HIT)
						if (attackBox.intersects(p.getHitbox())) {
							p.hurt(damage);
							p.alertFromDamage(playing.getPlayer(), playing);
							recordDefeatIfDead(p);
							return true;
						}
				}
			}

		for (Shark s : currentLevel.getSharks())
			if (s.isActive()) {
				if (s.getState() != DEAD && s.getState() != HIT)
					if (attackBox.intersects(s.getHitbox())) {
						s.hurt(damage);
						s.alertFromDamage(playing.getPlayer(), playing);
						recordDefeatIfDead(s);
						return true;
					}
			}

		for (KingPigBoss b : currentLevel.getBosses())
			if (b.isActive())
				if (b.getState() != DEAD && b.getState() != HIT)
					if (attackBox.intersects(b.getHitbox())) {
						b.hurt(damage);
						b.alertFromDamage(playing.getPlayer(), playing);
						recordDefeatIfDead(b);
						return true;
					}

		for (PirateMob p : currentLevel.getPirateMobs())
			if (p.isActive())
				if (p.getState() != DEAD && p.getState() != HIT)
					if (attackBox.intersects(p.getHitbox())) {
						p.hurt(damage);
						p.alertFromDamage(playing.getPlayer(), playing);
						recordDefeatIfDead(p);
						return true;
					}
		return false;
	}

	private void recordDefeatIfDead(Enemy enemy) {
		if (enemy.getState() == DEAD)
			playing.getPerformanceTracker().recordEnemyDefeated();
	}

	private void loadEnemyImgs() {
		crabbyArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.CRABBY_SPRITE), 9, 5, CRABBY_WIDTH_DEFAULT, CRABBY_HEIGHT_DEFAULT);
		pinkstarArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.PINKSTAR_ATLAS), 8, 5, PINKSTAR_WIDTH_DEFAULT, PINKSTAR_HEIGHT_DEFAULT);
		sharkArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.SHARK_ATLAS), 8, 5, SHARK_WIDTH_DEFAULT, SHARK_HEIGHT_DEFAULT);
		bossArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.BOSS_ATLAS), 8, 5, BOSS_WIDTH_DEFAULT, BOSS_HEIGHT_DEFAULT);
		baldPirateArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.BALD_PIRATE_ATLAS), 8, 5, PIRATE_MOB_WIDTH_DEFAULT, PIRATE_MOB_HEIGHT_DEFAULT);
		cucumberArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.CUCUMBER_ATLAS), 8, 5, PIRATE_MOB_WIDTH_DEFAULT, PIRATE_MOB_HEIGHT_DEFAULT);
		pirateCaptainArr = getImgArr(LoadSave.GetSpriteAtlas(LoadSave.PIRATE_CAPTAIN_ATLAS), 8, 5, PIRATE_MOB_WIDTH_DEFAULT, PIRATE_MOB_HEIGHT_DEFAULT);
	}

	private BufferedImage[][] getImgArr(BufferedImage atlas, int xSize, int ySize, int spriteW, int spriteH) {
		BufferedImage[][] tempArr = new BufferedImage[ySize][xSize];
		for (int j = 0; j < tempArr.length; j++)
			for (int i = 0; i < tempArr[j].length; i++)
				tempArr[j][i] = atlas.getSubimage(i * spriteW, j * spriteH, spriteW, spriteH);
		return tempArr;
	}

	public void resetAllEnemies() {
		for (Crabby c : currentLevel.getCrabs())
			c.resetEnemy();
		for (Pinkstar p : currentLevel.getPinkstars())
			p.resetEnemy();
		for (Shark s : currentLevel.getSharks())
			s.resetEnemy();
		for (KingPigBoss b : currentLevel.getBosses())
			b.resetEnemy();
		for (PirateMob p : currentLevel.getPirateMobs())
			p.resetEnemy();
	}

	public int getTotalEnemies() {
		return currentLevel.getCrabs().size() + currentLevel.getPinkstars().size() + currentLevel.getSharks().size() + currentLevel.getBosses().size() + currentLevel.getPirateMobs().size();
	}

	public int getDefeatedEnemies() {
		int defeated = 0;

		for (Crabby c : currentLevel.getCrabs())
			if (!c.isActive() || c.getState() == DEAD)
				defeated++;

		for (Pinkstar p : currentLevel.getPinkstars())
			if (!p.isActive() || p.getState() == DEAD)
				defeated++;

		for (Shark s : currentLevel.getSharks())
			if (!s.isActive() || s.getState() == DEAD)
				defeated++;

		for (KingPigBoss b : currentLevel.getBosses())
			if (!b.isActive() || b.getState() == DEAD)
				defeated++;

		for (PirateMob p : currentLevel.getPirateMobs())
			if (!p.isActive() || p.getState() == DEAD)
				defeated++;

		return defeated;
	}

	public List<Enemy> getAllEnemies() {
		ArrayList<Enemy> enemies = new ArrayList<>();
		enemies.addAll(currentLevel.getCrabs());
		enemies.addAll(currentLevel.getPinkstars());
		enemies.addAll(currentLevel.getSharks());
		enemies.addAll(currentLevel.getBosses());
		enemies.addAll(currentLevel.getPirateMobs());
		return enemies;
	}

	public int countAlliesNear(Enemy source, float radius) {
		int count = 0;
		float radiusSq = radius * radius;
		for (Enemy enemy : getAllEnemies()) {
			if (enemy == source || !enemy.isActive())
				continue;
			float dx = enemy.getCenterX() - source.getCenterX();
			float dy = enemy.getCenterY() - source.getCenterY();
			if (dx * dx + dy * dy <= radiusSq)
				count++;
		}
		return count;
	}

}
