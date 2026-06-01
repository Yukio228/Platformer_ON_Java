package levels;

import ai.navigation.NavigationGraph;
import ai.navigation.NavigationGraphBuilder;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.Crabby;
import entities.KingPigBoss;
import entities.Pinkstar;
import entities.PirateMob;
import entities.Shark;
import main.Game;
import objects.BackgroundTree;
import objects.Cannon;
import objects.Checkpoint;
import objects.GameContainer;
import objects.Grass;
import objects.Potion;
import objects.Spike;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.ObjectConstants.*;
import static utilz.HelpMethods.IsTileSolid;

public class Level {

	private BufferedImage img;
	private int levelIndex;
	private int[][] lvlData;

	private ArrayList<Crabby> crabs = new ArrayList<>();
	private ArrayList<Pinkstar> pinkstars = new ArrayList<>();
	private ArrayList<Shark> sharks = new ArrayList<>();
	private ArrayList<KingPigBoss> bosses = new ArrayList<>();
	private ArrayList<PirateMob> pirateMobs = new ArrayList<>();
	private ArrayList<Potion> potions = new ArrayList<>();
	private ArrayList<Spike> spikes = new ArrayList<>();
	private ArrayList<GameContainer> containers = new ArrayList<>();
	private ArrayList<Cannon> cannons = new ArrayList<>();
	private ArrayList<Checkpoint> checkpoints = new ArrayList<>();
	private ArrayList<BackgroundTree> trees = new ArrayList<>();
	private ArrayList<Grass> grass = new ArrayList<>();

	private int lvlTilesWide;
	private int maxTilesOffset;
	private int maxLvlOffsetX;
	private Point playerSpawn;
	private NavigationGraph navigationGraph;

	public Level(BufferedImage img, int levelIndex) {
		this.img = img;
		this.levelIndex = levelIndex;
		lvlData = new int[img.getHeight()][img.getWidth()];
		loadLevel();
		navigationGraph = new NavigationGraphBuilder().build(lvlData);
		ensureCheckpoint();
		calcLvlOffsets();
	}

	private void loadLevel() {

		// Looping through the image colors just once. Instead of one per
		// object/enemy/etc..
		// Removed many methods in HelpMethods class.

		for (int y = 0; y < img.getHeight(); y++)
			for (int x = 0; x < img.getWidth(); x++) {
				Color c = new Color(img.getRGB(x, y));
				int red = c.getRed();
				int green = c.getGreen();
				int blue = c.getBlue();

				loadLevelData(red, x, y);
				loadEntities(green, x, y);
				loadObjects(blue, x, y);
			}
	}

	private void loadLevelData(int redValue, int x, int y) {
		if (redValue >= 50)
			lvlData[y][x] = 0;
		else
			lvlData[y][x] = redValue;
		switch (redValue) {
		case 0, 1, 2, 3, 30, 31, 33, 34, 35, 36, 37, 38, 39 -> 
		grass.add(new Grass((int) (x * Game.TILES_SIZE), (int) (y * Game.TILES_SIZE) - Game.TILES_SIZE, getRndGrassType(x)));
		}
	}

	private int getRndGrassType(int xPos) {
		return xPos % 2;
	}

	private void loadEntities(int greenValue, int x, int y) {
		switch (greenValue) {
		case CRABBY -> crabs.add(new Crabby(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
		case PINKSTAR -> pinkstars.add(new Pinkstar(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
		case SHARK -> sharks.add(new Shark(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
		case BOSS -> bosses.add(new KingPigBoss(x * Game.TILES_SIZE, y * Game.TILES_SIZE, getBossVariant()));
		case BALD_PIRATE, CUCUMBER, PIRATE_CAPTAIN -> pirateMobs.add(new PirateMob(x * Game.TILES_SIZE, y * Game.TILES_SIZE, greenValue));
		case 100 -> playerSpawn = new Point(x * Game.TILES_SIZE, y * Game.TILES_SIZE);
		}
	}

	private void loadObjects(int blueValue, int x, int y) {
		switch (blueValue) {
		case RED_POTION, BLUE_POTION -> potions.add(new Potion(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		case BOX, BARREL -> containers.add(new GameContainer(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		case SPIKE -> spikes.add(new Spike(x * Game.TILES_SIZE, y * Game.TILES_SIZE, SPIKE));
		case CANNON_LEFT, CANNON_RIGHT -> cannons.add(new Cannon(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		case CHECKPOINT -> checkpoints.add(new Checkpoint(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
		case TREE_ONE, TREE_TWO, TREE_THREE -> trees.add(new BackgroundTree(x * Game.TILES_SIZE, y * Game.TILES_SIZE, blueValue));
		}
	}

	private int getBossVariant() {
		return KingPigBoss.FINAL_BOSS;
	}

	private void ensureCheckpoint() {
		if (!checkpoints.isEmpty())
			return;

		if (addManualCheckpoints())
			return;

		Point checkpointTile = findCheckpointTile();
		if (checkpointTile != null)
			checkpoints.add(new Checkpoint(checkpointTile.x * Game.TILES_SIZE, checkpointTile.y * Game.TILES_SIZE));
		else if (playerSpawn != null)
			checkpoints.add(new Checkpoint(playerSpawn.x, playerSpawn.y));
	}

	private boolean addManualCheckpoints() {
		if (levelIndex == 1) {
			addCheckpointAtTile(37, 7);
			return true;
		}

		if (levelIndex == 2) {
			addCheckpointAtTile(50, 6);
			return true;
		}

		if (levelIndex == 4) {
			addCheckpointAtTile(26, 7);
			return true;
		}

		return false;
	}

	private void addCheckpointAtTile(int x, int y) {
		checkpoints.add(new Checkpoint(x * Game.TILES_SIZE, y * Game.TILES_SIZE));
	}

	private Point findCheckpointTile() {
		int targetX = Math.max(2, Math.min(img.getWidth() - 3, (int) (img.getWidth() * 0.55f)));

		for (int offset = 0; offset < img.getWidth(); offset++) {
			Point right = findCheckpointTileAtX(targetX + offset);
			if (right != null)
				return right;

			if (offset != 0) {
				Point left = findCheckpointTileAtX(targetX - offset);
				if (left != null)
					return left;
			}
		}

		return null;
	}

	private Point findCheckpointTileAtX(int x) {
		if (x < 1 || x >= img.getWidth() - 1)
			return null;

		for (int y = img.getHeight() - 2; y > 0; y--)
			if (isSafeCheckpointTile(x, y))
				return new Point(x, y);

		return null;
	}

	private boolean isSafeCheckpointTile(int x, int y) {
		if (IsTileSolid(x, y, lvlData) || IsTileSolid(x, y - 1, lvlData) || !IsTileSolid(x, y + 1, lvlData))
			return false;

		return isWideEnoughPlatform(x, y) && !hasDangerousObjectNear(x, y) && !hasEnemyNear(x, y);
	}

	private boolean isWideEnoughPlatform(int x, int y) {
		if (x <= 0 || x >= img.getWidth() - 1)
			return false;

		return IsTileSolid(x - 1, y + 1, lvlData) && IsTileSolid(x, y + 1, lvlData) && IsTileSolid(x + 1, y + 1, lvlData);
	}

	private boolean hasDangerousObjectNear(int x, int y) {
		for (int xOffset = -1; xOffset <= 1; xOffset++) {
			int checkX = x + xOffset;
			if (checkX >= 0 && checkX < img.getWidth())
				if (isUnsafeObject(getBlueValue(checkX, y)))
					return true;
		}
		return false;
	}

	private boolean isUnsafeObject(int blueValue) {
		return blueValue == SPIKE || blueValue == BARREL || blueValue == BOX || blueValue == CANNON_LEFT || blueValue == CANNON_RIGHT;
	}

	private boolean hasEnemyNear(int x, int y) {
		for (int xOffset = -1; xOffset <= 1; xOffset++) {
			int checkX = x + xOffset;
			if (checkX >= 0 && checkX < img.getWidth())
				if (isEnemyOrPlayerSpawn(getGreenValue(checkX, y)))
					return true;
		}
		return false;
	}

	private boolean isEnemyOrPlayerSpawn(int greenValue) {
		return greenValue == CRABBY || greenValue == PINKSTAR || greenValue == SHARK || greenValue == BOSS || IsPirateMob(greenValue) || greenValue == 100;
	}

	private int getBlueValue(int x, int y) {
		return new Color(img.getRGB(x, y)).getBlue();
	}

	private int getGreenValue(int x, int y) {
		return new Color(img.getRGB(x, y)).getGreen();
	}

	private void calcLvlOffsets() {
		lvlTilesWide = img.getWidth();
		maxTilesOffset = lvlTilesWide - Game.TILES_IN_WIDTH;
		maxLvlOffsetX = Game.TILES_SIZE * maxTilesOffset;
	}

	public int getSpriteIndex(int x, int y) {
		return lvlData[y][x];
	}

	public int[][] getLevelData() {
		return lvlData;
	}

	public int getLvlOffset() {
		return maxLvlOffsetX;
	}

	public Point getPlayerSpawn() {
		return playerSpawn;
	}

	public ArrayList<Crabby> getCrabs() {
		return crabs;
	}

	public ArrayList<Shark> getSharks() {
		return sharks;
	}

	public ArrayList<KingPigBoss> getBosses() {
		return bosses;
	}

	public ArrayList<PirateMob> getPirateMobs() {
		return pirateMobs;
	}

	public ArrayList<Potion> getPotions() {
		return potions;
	}

	public ArrayList<GameContainer> getContainers() {
		return containers;
	}

	public ArrayList<Spike> getSpikes() {
		return spikes;
	}

	public ArrayList<Cannon> getCannons() {
		return cannons;
	}

	public ArrayList<Checkpoint> getCheckpoints() {
		return checkpoints;
	}

	public ArrayList<Pinkstar> getPinkstars() {
		return pinkstars;
	}

	public ArrayList<BackgroundTree> getTrees() {
		return trees;
	}

	public ArrayList<Grass> getGrass() {
		return grass;
	}

	public NavigationGraph getNavigationGraph() {
		return navigationGraph;
	}

}
