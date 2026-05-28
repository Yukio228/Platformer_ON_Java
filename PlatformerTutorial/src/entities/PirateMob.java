package entities;

import static utilz.Constants.Directions.LEFT;
import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.IsFloor;

import gamestates.Playing;

public class PirateMob extends Enemy {

	public PirateMob(float x, float y, int enemyType) {
		super(x, y, PIRATE_MOB_WIDTH, PIRATE_MOB_HEIGHT, enemyType);
		initHitbox(getHitboxWidth(enemyType), getHitboxHeight(enemyType));
		initAttackBox(getAttackBoxWidth(enemyType), 28, 30);
		setAiProfile(getSightTiles(enemyType), getHearingTiles(enemyType), getChaseSpeed(enemyType), 170, 14);
		walkSpeed = getWalkSpeed(enemyType);
		attackDistance = getAttackDistance(enemyType);
	}

	public void update(int[][] lvlData, Playing playing) {
		updateBehavior(lvlData, playing);
		updateAnimationTick();
		updateAttackBoxFlip();
	}

	private void updateBehavior(int[][] lvlData, Playing playing) {
		if (firstUpdate)
			firstUpdateCheck(lvlData);

		if (inAir) {
			inAirChecks(lvlData, playing);
			return;
		}

		switch (state) {
		case IDLE:
			if (IsFloor(hitbox, lvlData))
				newState(RUNNING);
			else
				inAir = true;
			break;
		case RUNNING:
			updateAiState(lvlData, playing);
			if (aiState == AI_CHASE && isPlayerCloseForAttack(playing.getPlayer()))
				newState(ATTACK);
			else
				updateAiMovement(lvlData, playing);
			break;
		case ATTACK:
			if (aniIndex == 0)
				attackChecked = false;
			if (aniIndex == 3 && !attackChecked)
				checkPlayerHit(attackBox, playing.getPlayer());
			break;
		case HIT:
			if (aniIndex <= GetSpriteAmount(enemyType, state) - 2)
				pushBack(pushBackDir, lvlData, 1.45f);
			updatePushBackDrawOffset();
			break;
		}
	}

	@Override
	public int flipX() {
		if (walkDir == LEFT)
			return width;
		return 0;
	}

	@Override
	public int flipW() {
		if (walkDir == LEFT)
			return -1;
		return 1;
	}

	public int getDrawOffsetY() {
		return switch (enemyType) {
		case CUCUMBER -> (int) (13 * main.Game.SCALE);
		case PIRATE_CAPTAIN -> (int) (12 * main.Game.SCALE);
		default -> (int) (14 * main.Game.SCALE);
		};
	}

	private static int getHitboxWidth(int enemyType) {
		return switch (enemyType) {
		case CUCUMBER -> 18;
		case PIRATE_CAPTAIN -> 23;
		default -> 22;
		};
	}

	private static int getHitboxHeight(int enemyType) {
		return switch (enemyType) {
		case CUCUMBER -> 30;
		case PIRATE_CAPTAIN -> 31;
		default -> 29;
		};
	}

	private static int getAttackBoxWidth(int enemyType) {
		return enemyType == PIRATE_CAPTAIN ? 42 : 36;
	}

	private static float getSightTiles(int enemyType) {
		return enemyType == PIRATE_CAPTAIN ? 9.5f : 7.5f;
	}

	private static float getHearingTiles(int enemyType) {
		return enemyType == PIRATE_CAPTAIN ? 2.6f : 2.0f;
	}

	private static float getChaseSpeed(int enemyType) {
		return enemyType == CUCUMBER ? 1.1f : enemyType == PIRATE_CAPTAIN ? 1.45f : 1.25f;
	}

	private static float getWalkSpeed(int enemyType) {
		return (enemyType == CUCUMBER ? 0.32f : enemyType == PIRATE_CAPTAIN ? 0.48f : 0.4f) * main.Game.SCALE;
	}

	private static float getAttackDistance(int enemyType) {
		return (enemyType == PIRATE_CAPTAIN ? 1.45f : 1.1f) * main.Game.TILES_SIZE;
	}
}
