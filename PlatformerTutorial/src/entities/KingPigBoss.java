package entities;

import static utilz.Constants.Dialogue.EXCLAMATION;
import static utilz.Constants.Directions.RIGHT;
import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.IsFloor;

import gamestates.Playing;

public class KingPigBoss extends Enemy {
	public static final int FINAL_BOSS = 0;
	public static final int GUARD_BOSS = 1;
	public static final int CHARGER_BOSS = 2;

	private int attackCooldownTick;
	private int attackCooldownDuration;
	private int bossDamage;

	public KingPigBoss(float x, float y) {
		this(x, y, FINAL_BOSS);
	}

	public KingPigBoss(float x, float y, int bossVariant) {
		super(x, y, BOSS_WIDTH, BOSS_HEIGHT, BOSS);
		initHitbox(58, 44);
		initAttackBox(88, 40, 58);
		applyBossVariant(bossVariant);
	}

	private void applyBossVariant(int bossVariant) {
		switch (bossVariant) {
		case GUARD_BOSS:
			maxHealth = 130;
			bossDamage = 22;
			attackCooldownDuration = 185;
			setAiProfile(9.5f, 3.2f, 1.35f, 190, 14);
			walkSpeed = 0.42f * main.Game.SCALE;
			attackDistance = main.Game.TILES_SIZE * 1.15f;
			break;
		case CHARGER_BOSS:
			maxHealth = 160;
			bossDamage = 24;
			attackCooldownDuration = 175;
			setAiProfile(11.5f, 3.6f, 1.75f, 220, 12);
			walkSpeed = 0.52f * main.Game.SCALE;
			attackDistance = main.Game.TILES_SIZE * 1.25f;
			break;
		default:
			maxHealth = 230;
			bossDamage = 28;
			attackCooldownDuration = 195;
			setAiProfile(13, 4, 1.6f, 240, 12);
			walkSpeed = 0.48f * main.Game.SCALE;
			attackDistance = main.Game.TILES_SIZE * 1.35f;
			break;
		}

		currentHealth = maxHealth;
	}

	public void update(int[][] lvlData, Playing playing) {
		updateBehavior(lvlData, playing);
		updateAnimationTick();
		updateAttackBoxFlip();
	}

	private void updateBehavior(int[][] lvlData, Playing playing) {
		if (firstUpdate)
			firstUpdateCheck(lvlData);

		if (attackCooldownTick > 0)
			attackCooldownTick--;

		if (inAir) {
			inAirChecks(lvlData, playing);
		} else {
			switch (state) {
			case IDLE:
				if (IsFloor(hitbox, lvlData))
					newState(RUNNING);
				else
					inAir = true;
				break;
			case RUNNING:
				updateAiState(lvlData, playing);
				if (canStartAttack(playing))
					startAttack();
				else
					updateAiMovement(lvlData, playing);
				break;
			case ATTACK:
				if (aniIndex == 0)
					attackChecked = false;
				if (aniIndex <= 1) {
					turnTowardsPlayer(playing.getPlayer());
					updateAttackBoxFlip();
				}
				if ((aniIndex == 2 || aniIndex == 3) && !attackChecked)
					checkBossPlayerHit(playing.getPlayer());
				break;
			case HIT:
				updateHitReaction(lvlData, 1.2f);
				break;
			}
		}

		if (state == RUNNING && inAir)
			playing.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);
	}

	private boolean canStartAttack(Playing playing) {
		return attackCooldownTick <= 0 && aiState == AI_CHASE && isPlayerCloseForAttack(playing.getPlayer());
	}

	private void startAttack() {
		updateAttackBoxFlip();
		newState(ATTACK);
		attackChecked = false;
		attackCooldownTick = attackCooldownDuration;
	}

	private void checkBossPlayerHit(Player player) {
		updateAttackBoxFlip();
		if (attackBox.intersects(player.hitbox))
			player.changeHealth(-bossDamage, this);
		attackChecked = true;
	}

	@Override
	public void resetEnemy() {
		super.resetEnemy();
		attackCooldownTick = 0;
		walkDir = RIGHT;
	}
}
