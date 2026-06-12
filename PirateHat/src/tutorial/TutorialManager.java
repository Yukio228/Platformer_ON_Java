package tutorial;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import entities.PlayerInventory;
import gamestates.Playing;
import main.Game;

public class TutorialManager {

	private final Playing playing;
	private final List<TutorialHint> hints = new ArrayList<>();
	private int currentHintIndex;
	private int spawnX;
	private boolean jumped;
	private boolean basicAttackUsed;
	private boolean powerAttackUsed;
	private boolean redPotionUsed;

	public TutorialManager(Playing playing) {
		this.playing = playing;
		buildHints();
	}

	private void buildHints() {
		hints.add(new TutorialHint("Используйте [A] и [D], чтобы двигаться",
				t -> t.isTutorialLevel() && t.playerTileX() < TutorialLevelConfig.MOVEMENT_SHOW_UNTIL_TILE_X,
				t -> t.isTutorialLevel() && Math.abs(t.playerX() - t.spawnX) >= Game.TILES_SIZE * TutorialLevelConfig.MOVEMENT_COMPLETE_DISTANCE_TILES));

		hints.add(new TutorialHint("Нажмите [SPACE], чтобы прыгнуть",
				t -> t.isTutorialLevel() && t.playerTileX() >= TutorialLevelConfig.JUMP_SHOW_FROM_TILE_X && t.playerTileX() < TutorialLevelConfig.JUMP_SHOW_UNTIL_TILE_X,
				t -> t.jumped || t.playerTileX() >= TutorialLevelConfig.JUMP_SKIP_AFTER_TILE_X));

		hints.add(new TutorialHint("Нажмите [ЛКМ], чтобы атаковать",
				t -> t.isTutorialLevel() && t.playerTileX() >= TutorialLevelConfig.BASIC_ATTACK_SHOW_FROM_TILE_X && t.playerTileX() < TutorialLevelConfig.BASIC_ATTACK_SHOW_UNTIL_TILE_X,
				t -> t.basicAttackUsed || t.playerTileX() >= TutorialLevelConfig.BASIC_ATTACK_SKIP_AFTER_TILE_X));

		hints.add(new TutorialHint("Нажмите [ПКМ], чтобы выполнить силовую атаку",
				t -> t.isTutorialLevel() && t.playerTileX() >= TutorialLevelConfig.POWER_ATTACK_SHOW_FROM_TILE_X && t.playerTileX() < TutorialLevelConfig.POWER_ATTACK_SHOW_UNTIL_TILE_X,
				t -> t.powerAttackUsed || t.playerTileX() >= TutorialLevelConfig.POWER_ATTACK_SKIP_AFTER_TILE_X));

		hints.add(new TutorialHint("Нажмите [Q], чтобы использовать красное зелье",
				t -> t.isTutorialLevel() && t.playerTileX() >= TutorialLevelConfig.RED_POTION_SHOW_FROM_TILE_X && t.playerTileX() < TutorialLevelConfig.RED_POTION_SHOW_UNTIL_TILE_X
						&& (t.hasRedPotion() || t.playerTileX() >= TutorialLevelConfig.POWER_ATTACK_SKIP_AFTER_TILE_X),
				t -> t.redPotionUsed || t.playerTileX() >= TutorialLevelConfig.RED_POTION_SKIP_AFTER_TILE_X));

		hints.add(new TutorialHint("Победите всех противников, чтобы завершить уровень",
				t -> t.isTutorialLevel() && t.playerTileX() >= TutorialLevelConfig.FINAL_SHOW_FROM_TILE_X && t.hasActiveEnemies(),
				t -> t.areAllEnemiesDefeated()));
	}

	public void update() {
		if (!isTutorialLevel())
			return;

		while (currentHintIndex < hints.size()) {
			TutorialHint hint = hints.get(currentHintIndex);
			hint.update(this);
			if (!hint.isFinished())
				break;
			currentHintIndex++;
		}
	}

	public void draw(Graphics g) {
		if (!isTutorialLevel() || currentHintIndex >= hints.size())
			return;

		hints.get(currentHintIndex).draw(g);
	}

	public void resetForLevel(int levelIndex) {
		currentHintIndex = 0;
		jumped = false;
		basicAttackUsed = false;
		powerAttackUsed = false;
		redPotionUsed = false;
		for (TutorialHint hint : hints)
			hint.reset();

		if (TutorialLevelConfig.isTutorialLevel(levelIndex) && playing.getPlayer() != null)
			spawnX = playing.getPlayer().getWorldX();
	}

	public void recordJump() {
		if (isTutorialLevel())
			jumped = true;
	}

	public void recordBasicAttack() {
		if (isTutorialLevel())
			basicAttackUsed = true;
	}

	public void recordPowerAttack() {
		if (isTutorialLevel())
			powerAttackUsed = true;
	}

	public void recordRedPotionUsed() {
		if (isTutorialLevel())
			redPotionUsed = true;
	}

	private boolean isTutorialLevel() {
		return TutorialLevelConfig.isTutorialLevel(playing.getLevelManager().getLevelIndex());
	}

	private int playerX() {
		return playing.getPlayer().getWorldX();
	}

	private int playerTileX() {
		return playerX() / Game.TILES_SIZE;
	}

	private boolean hasRedPotion() {
		return playing.getPlayer().getInventory().getCount(PlayerInventory.RED_POTION) > 0;
	}

	private boolean hasActiveEnemies() {
		return playing.getEnemyManager().getDefeatedEnemies() < playing.getEnemyManager().getTotalEnemies();
	}

	private boolean areAllEnemiesDefeated() {
		int totalEnemies = playing.getEnemyManager().getTotalEnemies();
		return totalEnemies > 0 && playing.getEnemyManager().getDefeatedEnemies() >= totalEnemies;
	}
}
