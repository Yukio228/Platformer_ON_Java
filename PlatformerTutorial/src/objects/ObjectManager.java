package objects;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Enemy;
import entities.Player;
import entities.PlayerInventory;
import gamestates.Playing;
import levels.Level;
import main.Game;
import utilz.LoadSave;
import static utilz.Constants.ObjectConstants.*;
import static utilz.HelpMethods.CanCannonSeePlayer;
import static utilz.HelpMethods.IsProjectileHittingLevel;
import static utilz.Constants.Projectiles.*;

public class ObjectManager {

	private Playing playing;
	private BufferedImage[][] potionImgs, containerImgs;
	private BufferedImage[] inventoryItemImgs;
	private BufferedImage[] cannonImgs, grassImgs;
	private BufferedImage[][] treeImgs;
	private BufferedImage spikeImg, cannonBallImg;
	private ArrayList<Potion> potions;
	private ArrayList<GameContainer> containers;
	private ArrayList<InventoryPickup> inventoryPickups = new ArrayList<>();
	private ArrayList<Projectile> projectiles = new ArrayList<>();

	private Level currentLevel;

	public ObjectManager(Playing playing) {
		this.playing = playing;
		currentLevel = playing.getLevelManager().getCurrentLevel();
		loadImgs();
	}

	public void checkSpikesTouched(Player p) {
		for (Spike s : currentLevel.getSpikes())
			if (s.getHitbox().intersects(p.getHitbox()))
				p.kill();
	}

	public void checkSpikesTouched(Enemy e) {
		for (Spike s : currentLevel.getSpikes())
			if (s.getHitbox().intersects(e.getHitbox()))
				e.hurt(200);
	}

	public void checkObjectTouched(Rectangle2D.Float hitbox) {
		for (Potion p : potions)
			if (p.isActive()) {
				if (hitbox.intersects(p.getHitbox())) {
					p.setActive(false);
					applyEffectToPlayer(p);
				}
			}

		for (InventoryPickup pickup : inventoryPickups)
			if (pickup.isActive() && hitbox.intersects(pickup.getHitbox())) {
				pickup.setActive(false);
				playing.getPlayer().getInventory().addItem(pickup.getItemType(), 1);
			}
	}

	private void checkCheckpointTouched(Player player) {
		for (Checkpoint c : currentLevel.getCheckpoints())
			if (!c.isActivated())
				if (c.getHitbox().intersects(player.getHitbox())) {
					c.activate();
					playing.activateCheckpoint(c.getRespawnPoint());
				}
	}

	public void applyEffectToPlayer(Potion p) {
		if (p.getObjType() == RED_POTION)
			playing.getPlayer().getInventory().addItem(PlayerInventory.RED_POTION, 1);
		else
			playing.getPlayer().getInventory().addItem(PlayerInventory.BLUE_POTION, 1);
	}

	public void checkObjectHit(Rectangle2D.Float attackbox) {
		for (GameContainer gc : containers)
			if (gc.isActive() && !gc.doAnimation) {
				if (gc.getHitbox().intersects(attackbox)) {
					gc.setAnimation(true);
					spawnContainerLoot(gc);
					return;
				}
			}
	}

	private void spawnContainerLoot(GameContainer gc) {
		int centerX = (int) (gc.getHitbox().x + gc.getHitbox().width / 2);
		int topY = (int) (gc.getHitbox().y - gc.getHitbox().height / 2);
		int tileX = centerX / Game.TILES_SIZE;

		if (gc.getObjType() == BARREL) {
			potions.add(new Potion(centerX, topY, BLUE_POTION));
			inventoryPickups.add(new InventoryPickup(centerX - (int) (12 * Game.SCALE), topY - (int) (10 * Game.SCALE), PlayerInventory.GOLD_COIN));
			if (tileX % 5 == 0)
				inventoryPickups.add(new InventoryPickup(centerX + (int) (12 * Game.SCALE), topY - (int) (10 * Game.SCALE), PlayerInventory.KEY));
		} else {
			potions.add(new Potion(centerX, topY, RED_POTION));
			inventoryPickups.add(new InventoryPickup(centerX - (int) (12 * Game.SCALE), topY - (int) (10 * Game.SCALE), PlayerInventory.SILVER_COIN));
		}
	}

	public void loadObjects(Level newLevel) {
		currentLevel = newLevel;
		potions = new ArrayList<>(newLevel.getPotions());
		containers = new ArrayList<>(newLevel.getContainers());
		inventoryPickups.clear();
		projectiles.clear();
	}

	private void loadImgs() {
		BufferedImage potionSprite = LoadSave.GetSpriteAtlas(LoadSave.POTION_ATLAS);
		potionImgs = new BufferedImage[2][7];

		for (int j = 0; j < potionImgs.length; j++)
			for (int i = 0; i < potionImgs[j].length; i++)
				potionImgs[j][i] = potionSprite.getSubimage(12 * i, 16 * j, 12, 16);

		inventoryItemImgs = new BufferedImage[PlayerInventory.ITEM_AMOUNT];
		inventoryItemImgs[PlayerInventory.RED_POTION] = LoadSave.GetSpriteAtlas(LoadSave.ITEM_RED_POTION);
		inventoryItemImgs[PlayerInventory.BLUE_POTION] = LoadSave.GetSpriteAtlas(LoadSave.ITEM_BLUE_POTION);
		inventoryItemImgs[PlayerInventory.GOLD_COIN] = LoadSave.GetSpriteAtlas(LoadSave.ITEM_GOLD_COIN);
		inventoryItemImgs[PlayerInventory.SILVER_COIN] = LoadSave.GetSpriteAtlas(LoadSave.ITEM_SILVER_COIN);
		inventoryItemImgs[PlayerInventory.KEY] = LoadSave.GetSpriteAtlas(LoadSave.ITEM_KEY);
		inventoryItemImgs[PlayerInventory.MAP] = LoadSave.GetSpriteAtlas(LoadSave.ITEM_MAP);

		BufferedImage containerSprite = LoadSave.GetSpriteAtlas(LoadSave.CONTAINER_ATLAS);
		containerImgs = new BufferedImage[2][8];

		for (int j = 0; j < containerImgs.length; j++)
			for (int i = 0; i < containerImgs[j].length; i++)
				containerImgs[j][i] = containerSprite.getSubimage(40 * i, 30 * j, 40, 30);

		spikeImg = LoadSave.GetSpriteAtlas(LoadSave.TRAP_ATLAS);

		cannonImgs = new BufferedImage[7];
		BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.CANNON_ATLAS);

		for (int i = 0; i < cannonImgs.length; i++)
			cannonImgs[i] = temp.getSubimage(i * 40, 0, 40, 26);

		cannonBallImg = LoadSave.GetSpriteAtlas(LoadSave.CANNON_BALL);
		treeImgs = new BufferedImage[2][4];
		BufferedImage treeOneImg = LoadSave.GetSpriteAtlas(LoadSave.TREE_ONE_ATLAS);
		for (int i = 0; i < 4; i++)
			treeImgs[0][i] = treeOneImg.getSubimage(i * 39, 0, 39, 92);

		BufferedImage treeTwoImg = LoadSave.GetSpriteAtlas(LoadSave.TREE_TWO_ATLAS);
		for (int i = 0; i < 4; i++)
			treeImgs[1][i] = treeTwoImg.getSubimage(i * 62, 0, 62, 54);

		BufferedImage grassTemp = LoadSave.GetSpriteAtlas(LoadSave.GRASS_ATLAS);
		grassImgs = new BufferedImage[2];
		for (int i = 0; i < grassImgs.length; i++)
			grassImgs[i] = grassTemp.getSubimage(32 * i, 0, 32, 32);
	}

	public void update(int[][] lvlData, Player player) {
		updateBackgroundTrees();
		checkCheckpointTouched(player);

		for (Checkpoint c : currentLevel.getCheckpoints())
			c.update();

		for (Potion p : potions)
			if (p.isActive())
				p.update();

		for (InventoryPickup pickup : inventoryPickups)
			if (pickup.isActive())
				pickup.update();

		for (GameContainer gc : containers)
			if (gc.isActive())
				gc.update();

		updateCannons(lvlData, player);
		updateProjectiles(lvlData, player);

	}

	private void updateBackgroundTrees() {
		for (BackgroundTree bt : currentLevel.getTrees())
			bt.update();
	}

	private void updateProjectiles(int[][] lvlData, Player player) {
		for (Projectile p : projectiles)
			if (p.isActive()) {
				p.updatePos();
				if (p.getHitbox().intersects(player.getHitbox())) {
					player.changeHealth(-25);
					p.setActive(false);
				} else if (IsProjectileHittingLevel(p, lvlData))
					p.setActive(false);
			}
	}

	private boolean isPlayerInRange(Cannon c, Player player) {
		int absValue = (int) Math.abs(player.getHitbox().x - c.getHitbox().x);
		return absValue <= Game.TILES_SIZE * 5;
	}

	private boolean isPlayerInfrontOfCannon(Cannon c, Player player) {
		if (c.getObjType() == CANNON_LEFT) {
			if (c.getHitbox().x > player.getHitbox().x)
				return true;

		} else if (c.getHitbox().x < player.getHitbox().x)
			return true;
		return false;
	}

	private void updateCannons(int[][] lvlData, Player player) {
		for (Cannon c : currentLevel.getCannons()) {
			if (!c.doAnimation)
				if (c.getTileY() == player.getTileY())
					if (isPlayerInRange(c, player))
						if (isPlayerInfrontOfCannon(c, player))
							if (CanCannonSeePlayer(lvlData, player.getHitbox(), c.getHitbox(), c.getTileY()))
								c.setAnimation(true);

			c.update();
			if (c.getAniIndex() == 4 && c.getAniTick() == 0)
				shootCannon(c);
		}
	}

	private void shootCannon(Cannon c) {
		int dir = 1;
		if (c.getObjType() == CANNON_LEFT)
			dir = -1;

		projectiles.add(new Projectile((int) c.getHitbox().x, (int) c.getHitbox().y, dir));
	}

	public void draw(Graphics g, int xLvlOffset) {
		drawPotions(g, xLvlOffset);
		drawInventoryPickups(g, xLvlOffset);
		drawContainers(g, xLvlOffset);
		drawTraps(g, xLvlOffset);
		drawCannons(g, xLvlOffset);
		drawProjectiles(g, xLvlOffset);
		drawGrass(g, xLvlOffset);
		drawCheckpoints(g, xLvlOffset);
	}

	private void drawCheckpoints(Graphics g, int xLvlOffset) {
		for (Checkpoint c : currentLevel.getCheckpoints())
			c.draw(g, xLvlOffset);
	}

	private void drawGrass(Graphics g, int xLvlOffset) {
		for (Grass grass : currentLevel.getGrass())
			g.drawImage(grassImgs[grass.getType()], grass.getX() - xLvlOffset, grass.getY(), (int) (32 * Game.SCALE), (int) (32 * Game.SCALE), null);
	}

	public void drawBackgroundTrees(Graphics g, int xLvlOffset) {
		for (BackgroundTree bt : currentLevel.getTrees()) {

			int type = bt.getType();
			if (type == 9)
				type = 8;
			g.drawImage(treeImgs[type - 7][bt.getAniIndex()], bt.getX() - xLvlOffset + GetTreeOffsetX(bt.getType()), (int) (bt.getY() + GetTreeOffsetY(bt.getType())), GetTreeWidth(bt.getType()),
					GetTreeHeight(bt.getType()), null);
		}
	}

	private void drawProjectiles(Graphics g, int xLvlOffset) {
		for (Projectile p : projectiles)
			if (p.isActive())
				g.drawImage(cannonBallImg, (int) (p.getHitbox().x - xLvlOffset), (int) (p.getHitbox().y), CANNON_BALL_WIDTH, CANNON_BALL_HEIGHT, null);
	}

	private void drawInventoryPickups(Graphics g, int xLvlOffset) {
		int iconSize = (int) (18 * Game.SCALE);
		for (InventoryPickup pickup : inventoryPickups)
			if (pickup.isActive()) {
				int x = (int) (pickup.getHitbox().x + pickup.getHitbox().width / 2 - iconSize / 2 - xLvlOffset);
				int y = (int) (pickup.getHitbox().y + pickup.getHitbox().height / 2 - iconSize / 2);
				g.drawImage(inventoryItemImgs[pickup.getItemType()], x, y, iconSize, iconSize, null);
			}
	}

	private void drawCannons(Graphics g, int xLvlOffset) {
		for (Cannon c : currentLevel.getCannons()) {
			int x = (int) (c.getHitbox().x - xLvlOffset);
			int width = CANNON_WIDTH;

			if (c.getObjType() == CANNON_RIGHT) {
				x += width;
				width *= -1;
			}
			g.drawImage(cannonImgs[c.getAniIndex()], x, (int) (c.getHitbox().y), width, CANNON_HEIGHT, null);
		}
	}

	private void drawTraps(Graphics g, int xLvlOffset) {
		for (Spike s : currentLevel.getSpikes())
			g.drawImage(spikeImg, (int) (s.getHitbox().x - xLvlOffset), (int) (s.getHitbox().y - s.getyDrawOffset()), SPIKE_WIDTH, SPIKE_HEIGHT, null);

	}

	private void drawContainers(Graphics g, int xLvlOffset) {
		for (GameContainer gc : containers)
			if (gc.isActive()) {
				int type = 0;
				if (gc.getObjType() == BARREL)
					type = 1;
				g.drawImage(containerImgs[type][gc.getAniIndex()], (int) (gc.getHitbox().x - gc.getxDrawOffset() - xLvlOffset), (int) (gc.getHitbox().y - gc.getyDrawOffset()), CONTAINER_WIDTH,
						CONTAINER_HEIGHT, null);
			}
	}

	private void drawPotions(Graphics g, int xLvlOffset) {
		for (Potion p : potions)
			if (p.isActive()) {
				int type = 0;
				if (p.getObjType() == RED_POTION)
					type = 1;
				g.drawImage(potionImgs[type][p.getAniIndex()], (int) (p.getHitbox().x - p.getxDrawOffset() - xLvlOffset), (int) (p.getHitbox().y - p.getyDrawOffset()), POTION_WIDTH, POTION_HEIGHT,
						null);
			}
	}

	public void resetAllObjects() {
		loadObjects(playing.getLevelManager().getCurrentLevel());
		for (Potion p : potions)
			p.reset();
		inventoryPickups.clear();
		for (GameContainer gc : containers)
			gc.reset();
		for (Cannon c : currentLevel.getCannons())
			c.reset();
		for (Checkpoint c : currentLevel.getCheckpoints())
			c.reset();
	}

	public void clearProjectiles() {
		projectiles.clear();
	}
}
