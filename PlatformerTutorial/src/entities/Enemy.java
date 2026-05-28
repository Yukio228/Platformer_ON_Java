package entities;

import static utilz.Constants.EnemyConstants.*;
import static utilz.Constants.Dialogue.*;
import static utilz.HelpMethods.*;

import java.awt.geom.Rectangle2D;

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
	protected static final int AI_PATROL = 0;
	protected static final int AI_ALERT = 1;
	protected static final int AI_CHASE = 2;
	protected static final int AI_SEARCH = 3;
	protected int aiState = AI_PATROL;
	protected int sightMemoryTick;
	protected int sightMemoryDuration = 140;
	protected int alertTick;
	protected int alertDuration = 18;
	protected int alertCooldownTick;
	protected float lastKnownPlayerX;
	protected float sightDistance = Game.TILES_SIZE * 5;
	protected float hearingDistance = Game.TILES_SIZE * 1.5f;
	protected float chaseSpeedMultiplier = 1.25f;

	public Enemy(float x, float y, int width, int height, int enemyType) {
		super(x, y, width, height);
		this.enemyType = enemyType;

		maxHealth = GetMaxHealth(enemyType);
		currentHealth = maxHealth;
		walkSpeed = Game.SCALE * 0.35f;
		lastKnownPlayerX = x;
	}

	protected void setAiProfile(float sightTiles, float hearingTiles, float chaseSpeedMultiplier, int sightMemoryDuration, int alertDuration) {
		this.sightDistance = Game.TILES_SIZE * sightTiles;
		this.hearingDistance = Game.TILES_SIZE * hearingTiles;
		this.chaseSpeedMultiplier = chaseSpeedMultiplier;
		this.sightMemoryDuration = sightMemoryDuration;
		this.alertDuration = alertDuration;
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

	protected void turnTowardsPlayer(Player player) {
		if (player.hitbox.x > hitbox.x)
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	protected boolean canSeePlayer(int[][] lvlData, Player player) {
		int playerTileY = (int) (player.getHitbox().y / Game.TILES_SIZE);
		if (playerTileY == tileY)
			if (isPlayerInRange(player)) {
				boolean playerInFront = isPlayerInFacingDirection(player);
				boolean closeEnoughToHear = isPlayerCloseEnoughToHear(player);
				if ((playerInFront || closeEnoughToHear) && IsSightClear(lvlData, hitbox, player.hitbox, tileY))
					return true;
			}
		return false;
	}

	protected boolean isPlayerInRange(Player player) {
		int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
		return absValue <= sightDistance;
	}

	protected boolean isPlayerInFacingDirection(Player player) {
		if (walkDir == RIGHT)
			return player.hitbox.x > hitbox.x;
		return player.hitbox.x < hitbox.x;
	}

	protected boolean isPlayerCloseEnoughToHear(Player player) {
		int absValue = (int) Math.abs(player.hitbox.x - hitbox.x);
		return absValue <= hearingDistance;
	}

	protected void updateAiState(int[][] lvlData, Playing playing) {
		if (alertCooldownTick > 0)
			alertCooldownTick--;

		Player player = playing.getPlayer();
		boolean seesPlayer = canSeePlayer(lvlData, player);

		if (seesPlayer) {
			lastKnownPlayerX = player.getHitbox().x;
			sightMemoryTick = sightMemoryDuration;
			turnTowardsPlayer(player);

			if (aiState == AI_PATROL || aiState == AI_SEARCH) {
				aiState = AI_ALERT;
				alertTick = alertDuration;
				showAiDialogue(playing, EXCLAMATION);
			} else {
				aiState = AI_CHASE;
			}
			return;
		}

		if (sightMemoryTick > 0) {
			sightMemoryTick--;
			if (aiState == AI_ALERT || aiState == AI_CHASE)
				aiState = AI_SEARCH;
		} else if (aiState != AI_PATROL) {
			aiState = AI_PATROL;
			showAiDialogue(playing, QUESTION);
		}
	}

	protected void updateAiMovement(int[][] lvlData, Playing playing) {
		switch (aiState) {
		case AI_ALERT:
			turnTowardsPlayer(playing.getPlayer());
			alertTick--;
			if (alertTick <= 0)
				aiState = AI_CHASE;
			break;
		case AI_CHASE:
			turnTowardsPlayer(playing.getPlayer());
			move(lvlData, chaseSpeedMultiplier);
			break;
		case AI_SEARCH:
			turnTowardsLastKnownPlayerPosition();
			if (Math.abs(getCenterX() - lastKnownPlayerX) <= Game.TILES_SIZE / 2f || !move(lvlData, 0.9f))
				sightMemoryTick = Math.min(sightMemoryTick, 30);
			break;
		default:
			move(lvlData);
			break;
		}
	}

	protected void turnTowardsLastKnownPlayerPosition() {
		if (lastKnownPlayerX > hitbox.x)
			walkDir = RIGHT;
		else
			walkDir = LEFT;
	}

	protected float getCenterX() {
		return hitbox.x + hitbox.width / 2f;
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
		lastKnownPlayerX = player.getHitbox().x;
		sightMemoryTick = sightMemoryDuration;
		alertTick = 0;
		aiState = AI_CHASE;
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
					case ATTACK, HIT -> state = IDLE;
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
		aiState = AI_PATROL;
		sightMemoryTick = 0;
		alertTick = 0;
		alertCooldownTick = 0;
		lastKnownPlayerX = x;

		pushDrawOffset = 0;

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
