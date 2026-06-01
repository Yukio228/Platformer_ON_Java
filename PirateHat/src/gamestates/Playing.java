package gamestates;

import ai.communication.AlertManager;
import ai.debug.AiDebugRenderer;
import ai.difficulty.DifficultyDirector;
import ai.difficulty.PlayerPerformanceTracker;
import ai.memory.SharedBlackboard;
import ai.metrics.AiMetricsCollector;
import ai.perception.NoiseManager;
import ai.perception.NoiseType;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;
import java.util.ArrayList;

import entities.EnemyManager;
import entities.Player;
import entities.PlayerInventory;
import levels.LevelManager;
import main.Game;
import objects.ObjectManager;
import ui.GameCompletedOverlay;
import ui.GameOverOverlay;
import ui.InventoryUI;
import ui.LevelCompletedOverlay;
import ui.PauseOverlay;
import utilz.LoadSave;
import effects.DialogueEffect;
import effects.Rain;

import static utilz.Constants.Environment.*;
import static utilz.Constants.Dialogue.*;

public class Playing extends State implements Statemethods {

	private Player player;
	private LevelManager levelManager;
	private EnemyManager enemyManager;
	private ObjectManager objectManager;
	private PauseOverlay pauseOverlay;
	private GameOverOverlay gameOverOverlay;
	private GameCompletedOverlay gameCompletedOverlay;
	private LevelCompletedOverlay levelCompletedOverlay;
	private InventoryUI inventoryUI;
	private Rain rain;
	private NoiseManager noiseManager;
	private AlertManager alertManager;
	private SharedBlackboard sharedBlackboard;
	private PlayerPerformanceTracker performanceTracker;
	private DifficultyDirector difficultyDirector;
	private AiMetricsCollector aiMetricsCollector;
	private AiDebugRenderer aiDebugRenderer;
	private int aiTick;

	private boolean paused = false;
	private boolean inventoryOpen;

	private int xLvlOffset;
	private int leftBorder = (int) (0.25 * Game.GAME_WIDTH);
	private int rightBorder = (int) (0.75 * Game.GAME_WIDTH);
	private int maxLvlOffsetX;

	private BufferedImage backgroundImg, bigCloud, smallCloud, shipImgs[];
	private BufferedImage[] questionImgs, exclamationImgs;
	private ArrayList<DialogueEffect> dialogEffects = new ArrayList<>();

	private int[] smallCloudsPos;
	private Random rnd = new Random();

	private boolean gameOver;
	private boolean lvlCompleted;
	private boolean gameCompleted;
	private boolean playerDying;
	private boolean drawRain;

	// Ship will be decided to drawn here. It's just a cool addition to the game
	// for the first level. Hinting on that the player arrived with the boat.

	// If you would like to have it on more levels, add a value for objects when
	// creating the level from lvlImgs. Just like any other object.

	// Then play around with position values so it looks correct depending on where
	// you want
	// it.

	private boolean drawShip = true;
	private int shipAni, shipTick, shipDir = 1;
	private float shipHeightDelta, shipHeightChange = 0.05f * Game.SCALE;

	public Playing(Game game) {
		super(game);
		initClasses();

		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.PLAYING_BG_IMG);
		bigCloud = LoadSave.GetSpriteAtlas(LoadSave.BIG_CLOUDS);
		smallCloud = LoadSave.GetSpriteAtlas(LoadSave.SMALL_CLOUDS);
		smallCloudsPos = new int[8];
		for (int i = 0; i < smallCloudsPos.length; i++)
			smallCloudsPos[i] = (int) (90 * Game.SCALE) + rnd.nextInt((int) (100 * Game.SCALE));

		shipImgs = new BufferedImage[4];
		BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.SHIP);
		for (int i = 0; i < shipImgs.length; i++)
			shipImgs[i] = temp.getSubimage(i * 78, 0, 78, 72);

		loadDialogue();
		calcLvlOffset();
		loadStartLevel();
		setDrawRainBoolean();
	}

	private void loadDialogue() {
		loadDialogueImgs();

		// Load dialogue array with premade objects, that gets activated when needed.
		// This is a simple
		// way of avoiding ConcurrentModificationException error. (Adding to a list that
		// is being looped through.

		for (int i = 0; i < 10; i++)
			dialogEffects.add(new DialogueEffect(0, 0, EXCLAMATION));
		for (int i = 0; i < 10; i++)
			dialogEffects.add(new DialogueEffect(0, 0, QUESTION));

		for (DialogueEffect de : dialogEffects)
			de.deactive();
	}

	private void loadDialogueImgs() {
		BufferedImage qtemp = LoadSave.GetSpriteAtlas(LoadSave.QUESTION_ATLAS);
		questionImgs = new BufferedImage[5];
		for (int i = 0; i < questionImgs.length; i++)
			questionImgs[i] = qtemp.getSubimage(i * 14, 0, 14, 12);

		BufferedImage etemp = LoadSave.GetSpriteAtlas(LoadSave.EXCLAMATION_ATLAS);
		exclamationImgs = new BufferedImage[5];
		for (int i = 0; i < exclamationImgs.length; i++)
			exclamationImgs[i] = etemp.getSubimage(i * 14, 0, 14, 12);
	}

	public void loadNextLevel() {
		levelManager.setLevelIndex(levelManager.getLevelIndex() + 1);
		levelManager.loadNextLevel();
		player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		resetAll();
		drawShip = false;
	}

	public void startLevel(int levelIndex) {
		levelManager.setLevelIndex(levelIndex);
		levelManager.loadNextLevel();
		player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
		resetAll();
		drawShip = levelIndex == 0;
		gameCompleted = false;
	}

	private void loadStartLevel() {
		enemyManager.loadEnemies(levelManager.getCurrentLevel());
		objectManager.loadObjects(levelManager.getCurrentLevel());
	}

	private void calcLvlOffset() {
		maxLvlOffsetX = levelManager.getCurrentLevel().getLvlOffset();
	}

	private void initClasses() {
		levelManager = new LevelManager(game);
		enemyManager = new EnemyManager(this);
		objectManager = new ObjectManager(this);
		noiseManager = new NoiseManager();
		alertManager = new AlertManager();
		sharedBlackboard = new SharedBlackboard();
		performanceTracker = new PlayerPerformanceTracker();
		difficultyDirector = new DifficultyDirector(performanceTracker);
		aiMetricsCollector = new AiMetricsCollector();
		aiDebugRenderer = new AiDebugRenderer();

		player = new Player(200, 200, (int) (64 * Game.SCALE), (int) (40 * Game.SCALE), this);
		player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
		player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());

		pauseOverlay = new PauseOverlay(this);
		gameOverOverlay = new GameOverOverlay(this);
		levelCompletedOverlay = new LevelCompletedOverlay(this);
		gameCompletedOverlay = new GameCompletedOverlay(this);
		inventoryUI = new InventoryUI();

		rain = new Rain();
	}

	@Override
	public void update() {
		if (paused)
			pauseOverlay.update();
		else if (lvlCompleted)
			levelCompletedOverlay.update();
		else if (gameCompleted)
			gameCompletedOverlay.update();
		else if (gameOver)
			gameOverOverlay.update();
		else if (inventoryOpen)
			updateDialogue();
		else if (playerDying)
			player.update();
		else {
			updateDialogue();
			if (drawRain)
				rain.update(xLvlOffset);
			levelManager.update();
			objectManager.update(levelManager.getCurrentLevel().getLevelData(), player);
			player.update();
			aiTick++;
			noiseManager.update();
			alertManager.update();
			sharedBlackboard.clearInactive();
			performanceTracker.update();
			difficultyDirector.update(aiMetricsCollector, aiTick);
			enemyManager.update(levelManager.getCurrentLevel().getLevelData());
			checkCloseToBorder();
			if (drawShip)
				updateShipAni();
		}
	}

	private void updateShipAni() {
		shipTick++;
		if (shipTick >= 35) {
			shipTick = 0;
			shipAni++;
			if (shipAni >= 4)
				shipAni = 0;
		}

		shipHeightDelta += shipHeightChange * shipDir;
		shipHeightDelta = Math.max(Math.min(10 * Game.SCALE, shipHeightDelta), 0);

		if (shipHeightDelta == 0)
			shipDir = 1;
		else if (shipHeightDelta == 10 * Game.SCALE)
			shipDir = -1;

	}

	private void updateDialogue() {
		for (DialogueEffect de : dialogEffects)
			if (de.isActive())
				de.update();
	}

	private void drawDialogue(Graphics g, int xLvlOffset) {
		for (DialogueEffect de : dialogEffects)
			if (de.isActive()) {
				if (de.getType() == QUESTION)
					g.drawImage(questionImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
				else
					g.drawImage(exclamationImgs[de.getAniIndex()], de.getX() - xLvlOffset, de.getY(), DIALOGUE_WIDTH, DIALOGUE_HEIGHT, null);
			}
	}

	public void addDialogue(int x, int y, int type) {
		// Not adding a new one, we are recycling. #ThinkGreen lol
		int effectY = Math.max(0, y - (int) (Game.SCALE * 15));
		dialogEffects.add(new DialogueEffect(x, effectY, type));
		for (DialogueEffect de : dialogEffects)
			if (!de.isActive())
				if (de.getType() == type) {
					de.reset(x, effectY);
					return;
				}
	}

	private void checkCloseToBorder() {
		int playerX = (int) player.getHitbox().x;
		int diff = playerX - xLvlOffset;

		if (diff > rightBorder)
			xLvlOffset += diff - rightBorder;
		else if (diff < leftBorder)
			xLvlOffset += diff - leftBorder;

		xLvlOffset = Math.max(Math.min(xLvlOffset, maxLvlOffsetX), 0);
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

		drawClouds(g);
		if (drawRain)
			rain.draw(g, xLvlOffset);

		if (drawShip)
			g.drawImage(shipImgs[shipAni], (int) (100 * Game.SCALE) - xLvlOffset, (int) ((288 * Game.SCALE) + shipHeightDelta), (int) (78 * Game.SCALE), (int) (72 * Game.SCALE), null);

		levelManager.draw(g, xLvlOffset);
		objectManager.draw(g, xLvlOffset);
		enemyManager.draw(g, xLvlOffset);
		player.render(g, xLvlOffset);
		objectManager.drawBackgroundTrees(g, xLvlOffset);
		drawDialogue(g, xLvlOffset);
		aiDebugRenderer.draw(g, this, xLvlOffset);
		inventoryUI.drawHotbar(g, player.getInventory());

		if (paused) {
			g.setColor(new Color(0, 0, 0, 150));
			g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
			pauseOverlay.draw(g);
		} else if (gameOver)
			gameOverOverlay.draw(g);
		else if (lvlCompleted)
			levelCompletedOverlay.draw(g);
		else if (gameCompleted)
			gameCompletedOverlay.draw(g);
		else if (inventoryOpen)
			inventoryUI.drawOverlay(g, player.getInventory());

	}

	private void drawClouds(Graphics g) {
		for (int i = 0; i < 4; i++)
			g.drawImage(bigCloud, i * BIG_CLOUD_WIDTH - (int) (xLvlOffset * 0.3), (int) (204 * Game.SCALE), BIG_CLOUD_WIDTH, BIG_CLOUD_HEIGHT, null);

		for (int i = 0; i < smallCloudsPos.length; i++)
			g.drawImage(smallCloud, SMALL_CLOUD_WIDTH * 4 * i - (int) (xLvlOffset * 0.7), smallCloudsPos[i], SMALL_CLOUD_WIDTH, SMALL_CLOUD_HEIGHT, null);
	}

	public void setGameCompleted() {
		gameCompleted = true;
		gameCompletedOverlay.reset();
	}

	public void resetGameCompleted() {
		gameCompleted = false;
	}

	public void resetAll() {
		gameOver = false;
		paused = false;
		lvlCompleted = false;
		playerDying = false;
		drawRain = false;

		setDrawRainBoolean();

		player.resetAll();
		enemyManager.resetAllEnemies();
		objectManager.resetAllObjects();
		noiseManager.clear();
		alertManager.clear();
		sharedBlackboard.clear();
		performanceTracker.resetForLevel();
		dialogEffects.clear();
		inventoryOpen = false;
	}

	public void respawnPlayer() {
		gameOver = false;
		paused = false;
		lvlCompleted = false;
		playerDying = false;
		inventoryOpen = false;

		player.resetAll();
		objectManager.clearProjectiles();
		noiseManager.clear();
		alertManager.clear();
		sharedBlackboard.clear();
		dialogEffects.clear();
	}

	private void setDrawRainBoolean() {
		// This method makes it rain 20% of the time you load a level.
		if (rnd.nextFloat() >= 0.8f)
			drawRain = true;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
		if (gameOver)
			playerDying = false;
	}

	public void checkObjectHit(Rectangle2D.Float attackBox) {
		objectManager.checkObjectHit(attackBox);
	}

	public boolean checkEnemyHit(Rectangle2D.Float attackBox) {
		return enemyManager.checkEnemyHit(attackBox);
	}

	public void checkPotionTouched(Rectangle2D.Float hitbox) {
		objectManager.checkObjectTouched(hitbox);
	}

	public void activateCheckpoint(Point checkpointSpawn) {
		player.setRespawn(checkpointSpawn);
		addDialogue(checkpointSpawn.x, checkpointSpawn.y, EXCLAMATION);
	}

	public void checkSpikesTouched(Player p) {
		performanceTracker.recordTrapContact();
		objectManager.checkSpikesTouched(p);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (gameCompleted)
			return;

		if (!gameOver && !paused && !lvlCompleted && inventoryOpen) {
			if (e.getButton() == MouseEvent.BUTTON1)
				handleInventoryClick(e);
			return;
		}

		if (!gameOver && !paused && !lvlCompleted && e.getButton() == MouseEvent.BUTTON1)
			if (handleInventoryClick(e))
				return;

		if (!gameOver) {
			if (e.getButton() == MouseEvent.BUTTON1)
				player.setAttacking(true);
			else if (e.getButton() == MouseEvent.BUTTON3)
				player.powerAttack();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (gameCompleted) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE)
				gameCompletedOverlay.returnToMenu();
			return;
		}

		if (gameOver) {
			if (e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_SPACE) {
				respawnPlayer();
				game.getAudioPlayer().setLevelSong(levelManager.getLevelIndex());
			} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				resetAll();
				setGamestate(Gamestate.MENU);
			}
			return;
		}

		if (!gameOver && !gameCompleted && !lvlCompleted && (e.getKeyCode() == KeyEvent.VK_I || e.getKeyCode() == KeyEvent.VK_TAB)) {
			inventoryOpen = !inventoryOpen;
			if (!inventoryOpen)
				inventoryUI.clearMouseOver();
			player.resetDirBooleans();
			return;
		}

		if (inventoryOpen) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				inventoryOpen = false;
				inventoryUI.clearMouseOver();
			}
			return;
		}

		if (!gameOver && !gameCompleted && !lvlCompleted)
			switch (e.getKeyCode()) {
			case KeyEvent.VK_F3:
				aiDebugRenderer.toggleEnabled();
				break;
			case KeyEvent.VK_F4:
				aiDebugRenderer.toggleNavigation();
				break;
			case KeyEvent.VK_F5:
				aiDebugRenderer.toggleNoise();
				break;
			case KeyEvent.VK_F6:
				aiDebugRenderer.toggleAlerts();
				break;
			case KeyEvent.VK_F7:
				exportAiMetrics();
				break;
			case KeyEvent.VK_Q:
				player.getInventory().useRedPotion(player);
				performanceTracker.recordHealingUsed();
				break;
			case KeyEvent.VK_A, KeyEvent.VK_LEFT:
				player.setLeft(true);
				break;
			case KeyEvent.VK_D, KeyEvent.VK_RIGHT:

				player.setRight(true);
				break;
			case KeyEvent.VK_SPACE, KeyEvent.VK_W, KeyEvent.VK_UP:
				player.setJump(true);
				break;
			case KeyEvent.VK_ESCAPE:
				paused = !paused;
			}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if (inventoryOpen)
			return;

		if (!gameOver && !gameCompleted && !lvlCompleted)
			switch (e.getKeyCode()) {
			case KeyEvent.VK_A, KeyEvent.VK_LEFT:
				player.setLeft(false);
				break;
			case KeyEvent.VK_D, KeyEvent.VK_RIGHT:
				player.setRight(false);
				break;
			case KeyEvent.VK_SPACE, KeyEvent.VK_W, KeyEvent.VK_UP:
				player.setJump(false);
				break;
			}
	}

	public void mouseDragged(MouseEvent e) {
		if (!gameOver && !gameCompleted && !lvlCompleted)
			if (paused)
				pauseOverlay.mouseDragged(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (gameOver)
			gameOverOverlay.mousePressed(e);
		else if (paused)
			pauseOverlay.mousePressed(e);
		else if (lvlCompleted)
			levelCompletedOverlay.mousePressed(e);
		else if (gameCompleted)
			gameCompletedOverlay.mousePressed(e);

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (gameOver)
			gameOverOverlay.mouseReleased(e);
		else if (paused)
			pauseOverlay.mouseReleased(e);
		else if (lvlCompleted)
			levelCompletedOverlay.mouseReleased(e);
		else if (gameCompleted)
			gameCompletedOverlay.mouseReleased(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (gameOver)
			gameOverOverlay.mouseMoved(e);
		else if (paused)
			pauseOverlay.mouseMoved(e);
		else if (lvlCompleted)
			levelCompletedOverlay.mouseMoved(e);
		else if (gameCompleted)
			gameCompletedOverlay.mouseMoved(e);
		else
			inventoryUI.mouseMoved(e, inventoryOpen);
	}

	private boolean handleInventoryClick(MouseEvent e) {
		int itemType = inventoryUI.getClickedItem(e, inventoryOpen);
		if (itemType < 0)
			return false;

		useInventoryItem(itemType);
		return true;
	}

	private void useInventoryItem(int itemType) {
		switch (itemType) {
		case PlayerInventory.RED_POTION -> {
			player.getInventory().useRedPotion(player);
			performanceTracker.recordHealingUsed();
		}
		case PlayerInventory.BLUE_POTION -> {
			player.getInventory().useBluePotion(player);
			performanceTracker.recordHealingUsed();
		}
		}
	}

	public void setLevelCompleted(boolean levelCompleted) {
		game.getAudioPlayer().lvlCompleted();
		game.getSaveManager().unlockNextLevel(levelManager.getLevelIndex(), levelManager.getAmountOfLevels());
		if (levelManager.getLevelIndex() + 1 >= levelManager.getAmountOfLevels()) {
			// No more levels
			gameCompleted = true;
			gameCompletedOverlay.reset();
			levelManager.setLevelIndex(0);
			levelManager.loadNextLevel();
			resetAll();
			return;
		}
		this.lvlCompleted = levelCompleted;
	}

	public void setMaxLvlOffset(int lvlOffset) {
		this.maxLvlOffsetX = lvlOffset;
	}

	public void unpauseGame() {
		paused = false;
	}

	public void windowFocusLost() {
		player.resetDirBooleans();
	}

	public Player getPlayer() {
		return player;
	}

	public EnemyManager getEnemyManager() {
		return enemyManager;
	}

	public ObjectManager getObjectManager() {
		return objectManager;
	}

	public LevelManager getLevelManager() {
		return levelManager;
	}

	public void setPlayerDying(boolean playerDying) {
		this.playerDying = playerDying;
	}

	public void emitPlayerNoise(NoiseType type, float x, float y) {
		noiseManager.addPlayerNoise(type, x, y);
	}

	public void recordPlayerAttack(boolean hit) {
		performanceTracker.recordAttack(hit);
	}

	public void recordPlayerHitTaken() {
		performanceTracker.recordHitTaken();
	}

	public void recordPlayerDeath() {
		performanceTracker.recordDeath();
	}

	private void exportAiMetrics() {
		try {
			Path file = aiMetricsCollector.exportCsv(Path.of(System.getProperty("user.dir")));
			System.out.println("AI metrics exported: " + file.toAbsolutePath());
		} catch (IOException e) {
			System.out.println("AI metrics export failed: " + e.getMessage());
		}
	}

	public NoiseManager getNoiseManager() {
		return noiseManager;
	}

	public AlertManager getAlertManager() {
		return alertManager;
	}

	public SharedBlackboard getSharedBlackboard() {
		return sharedBlackboard;
	}

	public PlayerPerformanceTracker getPerformanceTracker() {
		return performanceTracker;
	}

	public DifficultyDirector getDifficultyDirector() {
		return difficultyDirector;
	}

	public AiMetricsCollector getAiMetricsCollector() {
		return aiMetricsCollector;
	}

	public int getAiTick() {
		return aiTick;
	}

	public boolean shouldHideMouseCursor() {
		return !paused && !inventoryOpen && !gameOver && !lvlCompleted && !gameCompleted;
	}
}
