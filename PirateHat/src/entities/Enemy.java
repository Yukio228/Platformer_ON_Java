package entities;

import ai.core.EnemyAiController;
import ai.core.EnemyAiProfile;
import ai.core.EnemyAiState;
import ai.core.Vector2;
import ai.decision.EnemyAction;
import ai.navigation.NavigationNode;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.Dialogue.*;
import static utilz.HelpMethods.*;

import java.awt.geom.Rectangle2D;
import java.util.List;

import gamestates.Playing;

import static utilz.Constants.Directions.*;
import static utilz.Constants.*;

import main.Game;

public abstract class Enemy extends Entity {
	protected int enemyType;
	protected boolean firstUpdate = true;
	protected int walkDir = LEFT;
	protected int tileY;
	protected float attackDistance = Game.TILES_SIZE;
	protected boolean active = true;
	protected boolean attackChecked;
	protected int attackBoxOffsetX;
	protected int alertCooldownTick;
	protected float chaseSpeedMultiplier = 1.25f;
	protected EnemyAiController aiController;

	public Enemy(float x, float y, int width, int height, int enemyType) {
		super(x, y, width, height);
		this.enemyType = enemyType;

		maxHealth = GetMaxHealth(enemyType);
		currentHealth = maxHealth;
		walkSpeed = Game.SCALE * 0.35f;
		aiController = new EnemyAiController(this, EnemyAiProfile.forEnemyType(enemyType));
	}

	protected void setAiProfile(float sightTiles, float hearingTiles, float chaseSpeedMultiplier, int sightMemoryDuration, int alertDuration) {
		this.chaseSpeedMultiplier = chaseSpeedMultiplier;
		aiController = new EnemyAiController(this, EnemyAiProfile.forEnemyType(enemyType));
	}

	protected void updateAttackBox() {
		attackBox.x = hitbox.x - attackBoxOffsetX;
		attackBox.y = hitbox.y;
	}

	protected void updateAttackBoxFlip() {
		if (walkDir == RIGHT)
			attackBox.x = hitbox.x + hitbox.width;
		else
			attackBox.x = hitbox.x - attackBoxOffsetX;

		attackBox.y = hitbox.y;
	}

	protected void initAttackBox(int w, int h, int attackBoxOffsetX) {
		attackBox = new Rectangle2D.Float(x, y, (int) (w * Game.SCALE), (int) (h * Game.SCALE));
		this.attackBoxOffsetX = (int) (Game.SCALE * attackBoxOffsetX);
	}

	protected void firstUpdateCheck(int[][] lvlData) {
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
		tileY = (int) (hitbox.y / Game.TILES_SIZE);
		firstUpdate = false;
	}

	protected void inAirChecks(int[][] lvlData, Playing playing) {
		if (state != DEAD) {
			updateInAir(lvlData);
			playing.getObjectManager().checkSpikesTouched(this);
			if (IsEntityInWater(hitbox, lvlData))
				hurt(maxHealth);
		}
	}

	protected void updateInAir(int[][] lvlData) {
		if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
			hitbox.y += airSpeed;
			airSpeed += GRAVITY;
		} else {
			inAir = false;
			hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
			tileY = (int) (hitbox.y / Game.TILES_SIZE);
		}
	}

	protected boolean move(int[][] lvlData) {
		return move(lvlData, 1);
	}

	protected boolean move(int[][] lvlData, float speedMultiplier) {
		float xSpeed = 0;

		if (walkDir == LEFT)
			xSpeed = -walkSpeed;
		else
			xSpeed = walkSpeed;

		xSpeed *= speedMultiplier;

		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
			if (IsFloor(hitbox, xSpeed, lvlData)) {
				hitbox.x += xSpeed;
				return true;
			}

		changeWalkDir();
		return false;
	}

	public void turnTowardsPlayer(Player player) {
		if (player.hitbox.x > hitbox.x)
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	public void turnTowardsX(float targetX) {
		if (targetX > getCenterX())
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	protected boolean canSeePlayer(int[][] lvlData, Player player) {
		return aiController.getSuspicionLevel() >= 100f;
	}

	protected boolean isPlayerInRange(Player player) {
		int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
		return absValue <= aiController.getSightDistancePixels();
	}

	protected boolean isPlayerInFacingDirection(Player player) {
		if (walkDir == RIGHT)
			return player.hitbox.x > hitbox.x;
		return player.hitbox.x < hitbox.x;
	}

	protected boolean isPlayerCloseEnoughToHear(Player player) {
		int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
		return absValue <= aiController.getHearingRadiusPixels();
	}

	protected void updateAiState(int[][] lvlData, Playing playing) {
		EnemyAiState before = aiController.getState();
		aiController.update(lvlData, playing);
		EnemyAiState after = aiController.getState();
		if (after != before) {
			if (after == EnemyAiState.ALERT || after == EnemyAiState.CHASE)
				showAiDialogue(playing, EXCLAMATION);
			else if (after == EnemyAiState.SEARCH || after == EnemyAiState.RETURN_TO_PATROL)
				showAiDialogue(playing, QUESTION);
		}
	}

	protected void updateAiMovement(int[][] lvlData, Playing playing) {
		aiController.move(lvlData, playing);
	}

	protected void turnTowardsLastKnownPlayerPosition() {
		Vector2 target = aiController.getLastKnownPlayerPosition();
		if (target != null)
			turnTowardsX(target.x());
	}

	public float getCenterX() {
		return hitbox.x + hitbox.width / 2f;
	}

	public float getCenterY() {
		return hitbox.y + hitbox.height / 2f;
	}

	private void showAiDialogue(Playing playing, int type) {
		if (alertCooldownTick <= 0) {
			playing.addDialogue((int) hitbox.x, (int) hitbox.y, type);
			alertCooldownTick = 120;
		}
	}

	protected boolean isPlayerCloseForAttack(Player player) {
		int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
		switch (enemyType) {
		case CRABBY -> {
			return absValue <= attackDistance;
		}
		case SHARK, BOSS, BALD_PIRATE, CUCUMBER, PIRATE_CAPTAIN -> {
			return absValue <= attackDistance * 2;
		}
		}
		return false;
	}

	protected void updateHitReaction(int[][] lvlData, float pushBackSpeedMultiplier) {
		if (aniIndex <= GetSpriteAmount(enemyType, state) - 2)
			pushBack(pushBackDir, lvlData, pushBackSpeedMultiplier);
		if (!IsFloor(hitbox, lvlData))
			inAir = true;
		updatePushBackDrawOffset();
	}

	public void hurt(int amount) {
		currentHealth -= amount;
		if (currentHealth <= 0)
			newState(DEAD);
		else {
			newState(HIT);
			if (walkDir == LEFT)
				pushBackDir = RIGHT;
			else
				pushBackDir = LEFT;
			pushBackOffsetDir = UP;
			pushDrawOffset = 0;
		}
	}

	public void alertFromDamage(Player player, Playing playing) {
		if (state == DEAD)
			return;
		aiController.onDamagedByPlayer(player, playing);
		aiController.update(playing.getLevelManager().getCurrentLevel().getLevelData(), playing);
		turnTowardsPlayer(player);
		showAiDialogue(playing, EXCLAMATION);
	}

	protected void checkPlayerHit(Rectangle2D.Float attackBox, Player player) {
		if (attackBox.intersects(player.hitbox))
			player.changeHealth(-GetEnemyDmg(enemyType), this);
		else {
			if (enemyType == SHARK)
				return;
		}
		attackChecked = true;
	}

	protected void updateAnimationTick() {
		aniTick++;
		if (aniTick >= getAnimationSpeed()) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(enemyType, state)) {
				if (enemyType != PINKSTAR) {
					aniIndex = 0;

					switch (state) {
					case ATTACK -> {
						state = IDLE;
						aiController.onAttackAnimationFinished();
					}
					case HIT -> state = IDLE;
					case DEAD -> active = false;
					}
				} else {
					if (state == ATTACK)
						aniIndex = 3;
					else {
						aniIndex = 0;
						if (state == HIT) {
							state = IDLE;

						} else if (state == DEAD)
							active = false;
					}
				}
			}
		}
	}

	protected int getAnimationSpeed() {
		return switch (state) {
		case RUNNING -> 12;
		case ATTACK -> 15;
		case HIT -> 12;
		case DEAD -> 18;
		default -> ANI_SPEED;
		};
	}

	protected void changeWalkDir() {
		if (walkDir == LEFT)
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	public void resetEnemy() {
		hitbox.x = x;
		hitbox.y = y;
		firstUpdate = true;
		currentHealth = maxHealth;
		newState(IDLE);
		active = true;
		airSpeed = 0;
		inAir = false;
		attackChecked = false;
		walkDir = LEFT;
		alertCooldownTick = 0;
		aiController.reset();

		pushDrawOffset = 0;

	}

	protected void finishAiAttack() {
		aiController.onAttackAnimationFinished();
	}

	public boolean aiMove(int[][] lvlData, float speedMultiplier) {
		return move(lvlData, speedMultiplier);
	}

	public void aiJump() {
		if (inAir)
			return;
		inAir = true;
		airSpeed = -2.1f * Game.SCALE;
	}

	public void changeWalkDirPublic() {
		changeWalkDir();
	}

	public EnemyAiState getAiState() {
		return aiController.getState();
	}

	public EnemyAction getSelectedAiAction() {
		return aiController.getSelectedAction();
	}

	public float getSuspicionLevel() {
		return aiController.getSuspicionLevel();
	}

	public float getMemoryConfidence() {
		return aiController.getMemoryConfidence();
	}

	public Vector2 getLastKnownPlayerPosition() {
		return aiController.getLastKnownPlayerPosition();
	}

	public String getCurrentAiTargetText() {
		return aiController.getCurrentTargetText();
	}

	public List<NavigationNode> getCurrentPath() {
		return aiController.getCurrentPath();
	}

	public float getAiSightDistancePixels() {
		return aiController.getSightDistancePixels();
	}

	public float getAiHearingRadiusPixels() {
		return aiController.getHearingRadiusPixels();
	}

	public int getWalkDir() {
		return walkDir;
	}

	public int getEnemyType() {
		return enemyType;
	}

	public float getAttackDistance() {
		return attackDistance;
	}

	public float getChaseSpeedMultiplier() {
		return chaseSpeedMultiplier;
	}

	public float getHealthPercent() {
		return maxHealth <= 0 ? 0 : currentHealth / (float) maxHealth;
	}

	public float getSpawnX() {
		return x;
	}

	public float getSpawnY() {
		return y;
	}

	public String getEnemyDebugName() {
		return switch (enemyType) {
		case CRABBY -> "CRABBY";
		case PINKSTAR -> "PINKSTAR";
		case SHARK -> "SHARK";
		case BOSS -> "KING_PIG_BOSS";
		case BALD_PIRATE -> "BALD_PIRATE";
		case CUCUMBER -> "CUCUMBER";
		case PIRATE_CAPTAIN -> "PIRATE_CAPTAIN";
		default -> "ENEMY_" + enemyType;
		};
	}

	public int flipX() {
		if (walkDir == RIGHT)
			return width;
		else
			return 0;
	}

	public int flipW() {
		if (walkDir == RIGHT)
			return -1;
		else
			return 1;
	}

	public boolean isActive() {
		return active;
	}

	public float getPushDrawOffset() {
		return pushDrawOffset;
	}

	public int getCurrentHealth() {
		return currentHealth;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

}
