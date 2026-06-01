package entities;

import ai.core.EnemyAiState;

import static utilz.Constants.Dialogue.EXCLAMATION;
import static utilz.Constants.Directions.RIGHT;
import static utilz.Constants.EnemyConstants.*;
import static utilz.HelpMethods.IsFloor;

import gamestates.Playing;

public class KingPigBoss extends Enemy {
	public static final int FINAL_BOSS = 0;
	public static final int GUARD_BOSS = 1;
	public static final int CHARGER_BOSS = 2;

	private enum BossPhase {
		PHASE_ONE,
		PHASE_TWO,
		PHASE_THREE
	}

	private BossPhase phase = BossPhase.PHASE_ONE;
	private int attackCooldownTick;
	private int attackCooldownDuration;
	private int baseAttackCooldownDuration;
	private int bossDamage;
	private int baseBossDamage;
	private float baseWalkSpeed;
	private float baseAttackDistance;
	private float baseChaseSpeedMultiplier;
	private int attackSeriesCount;
	private int phasePulseTick;

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
		baseBossDamage = bossDamage;
		baseAttackCooldownDuration = attackCooldownDuration;
		baseWalkSpeed = walkSpeed;
		baseAttackDistance = attackDistance;
		baseChaseSpeedMultiplier = chaseSpeedMultiplier;
	}

	public void update(int[][] lvlData, Playing playing) {
		updatePhase(playing);
		updateBehavior(lvlData, playing);
		updateAnimationTick();
		updateAttackBoxFlip();
		phasePulseTick++;
	}

	private void updatePhase(Playing playing) {
		float health = getHealthPercent();
		BossPhase next = health <= 0.30f ? BossPhase.PHASE_THREE : health <= 0.60f ? BossPhase.PHASE_TWO : BossPhase.PHASE_ONE;
		if (next != phase) {
			phase = next;
			attackSeriesCount = 0;
			playing.addDialogue((int) hitbox.x, (int) hitbox.y, EXCLAMATION);
			playing.getAiMetricsCollector().record(playing.getAiTick(), this, "BOSS_PHASE_" + phase, getAiState(), 0, 0, "health=" + Math.round(health * 100));
		}

		switch (phase) {
		case PHASE_ONE:
			walkSpeed = baseWalkSpeed;
			attackDistance = baseAttackDistance;
			chaseSpeedMultiplier = baseChaseSpeedMultiplier;
			attackCooldownDuration = baseAttackCooldownDuration;
			bossDamage = baseBossDamage;
			break;
		case PHASE_TWO:
			walkSpeed = baseWalkSpeed * 1.18f;
			attackDistance = baseAttackDistance * 1.08f;
			chaseSpeedMultiplier = baseChaseSpeedMultiplier * 1.12f;
			attackCooldownDuration = Math.max(80, (int) (baseAttackCooldownDuration * 0.72f));
			bossDamage = baseBossDamage + 3;
			break;
		case PHASE_THREE:
			walkSpeed = baseWalkSpeed * 1.35f;
			attackDistance = baseAttackDistance * 1.18f;
			chaseSpeedMultiplier = baseChaseSpeedMultiplier * 1.28f;
			attackCooldownDuration = Math.max(55, (int) (baseAttackCooldownDuration * 0.55f));
			bossDamage = baseBossDamage + 5;
			break;
		}
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
		return attackCooldownTick <= 0 && getAiState() == EnemyAiState.ATTACK && isPlayerCloseForAttack(playing.getPlayer());
	}

	private void startAttack() {
		updateAttackBoxFlip();
		newState(ATTACK);
		attackChecked = false;
		attackSeriesCount++;
		if ((phase == BossPhase.PHASE_TWO && attackSeriesCount < 2) || (phase == BossPhase.PHASE_THREE && attackSeriesCount < 3))
			attackCooldownTick = Math.max(18, attackCooldownDuration / 4);
		else {
			attackCooldownTick = attackCooldownDuration;
			attackSeriesCount = 0;
		}
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
		phase = BossPhase.PHASE_ONE;
		attackSeriesCount = 0;
		phasePulseTick = 0;
	}

	@Override
	public String getEnemyDebugName() {
		return "KING_PIG_BOSS " + phase.name();
	}

	public int getPhaseIndex() {
		return phase == BossPhase.PHASE_ONE ? 1 : phase == BossPhase.PHASE_TWO ? 2 : 3;
	}

	public int getPhasePulseTick() {
		return phasePulseTick;
	}
}
