package ai.core;

import static utilz.Constants.EnemyConstants.*;

public class EnemyAiProfile {
	private float sightDistance;
	private float fieldOfViewAngle;
	private float hearingMultiplier;
	private int memoryDurationTicks;
	private int reactionDelayTicks;
	private float aggression;
	private float bravery;
	private float cooperation;
	private boolean canJump;
	private boolean canDropDown;
	private boolean canCallAllies;
	private boolean canRetreat;
	private boolean commander;

	public EnemyAiProfile(float sightDistance, float fieldOfViewAngle, float hearingMultiplier, int memoryDurationTicks, int reactionDelayTicks, float aggression, float bravery,
			float cooperation, boolean canJump, boolean canDropDown, boolean canCallAllies, boolean canRetreat, boolean commander) {
		this.sightDistance = sightDistance;
		this.fieldOfViewAngle = fieldOfViewAngle;
		this.hearingMultiplier = hearingMultiplier;
		this.memoryDurationTicks = memoryDurationTicks;
		this.reactionDelayTicks = reactionDelayTicks;
		this.aggression = aggression;
		this.bravery = bravery;
		this.cooperation = cooperation;
		this.canJump = canJump;
		this.canDropDown = canDropDown;
		this.canCallAllies = canCallAllies;
		this.canRetreat = canRetreat;
		this.commander = commander;
	}

	public static EnemyAiProfile forEnemyType(int enemyType) {
		return switch (enemyType) {
		case CRABBY -> new EnemyAiProfile(5.0f, 70f, 0.75f, 130, 26, 0.45f, 0.55f, 0.15f, false, false, false, false, false);
		case PINKSTAR -> new EnemyAiProfile(6.5f, 95f, 0.95f, 150, 18, 0.92f, 0.72f, 0.2f, false, false, false, false, false);
		case SHARK -> new EnemyAiProfile(8.5f, 55f, 1.05f, 160, 14, 0.82f, 0.8f, 0.25f, false, true, false, false, false);
		case CUCUMBER -> new EnemyAiProfile(7.0f, 90f, 1.2f, 180, 18, 0.62f, 0.65f, 0.55f, true, true, true, true, false);
		case PIRATE_CAPTAIN -> new EnemyAiProfile(10.5f, 110f, 1.55f, 230, 12, 0.88f, 0.9f, 1.0f, true, true, true, true, true);
		case BOSS -> new EnemyAiProfile(12.5f, 120f, 1.45f, 260, 12, 1.0f, 1.0f, 0.7f, true, true, true, true, true);
		default -> new EnemyAiProfile(7.5f, 95f, 1.15f, 180, 16, 0.72f, 0.72f, 0.65f, true, true, true, true, false);
		};
	}

	public EnemyAiProfile copy() {
		return new EnemyAiProfile(sightDistance, fieldOfViewAngle, hearingMultiplier, memoryDurationTicks, reactionDelayTicks, aggression, bravery, cooperation, canJump, canDropDown,
				canCallAllies, canRetreat, commander);
	}

	public void applyDifficulty(float sightMultiplier, float aggressionModifier, float memoryMultiplier, int reactionDelay) {
		sightDistance *= sightMultiplier;
		aggression *= aggressionModifier;
		memoryDurationTicks = Math.max(60, Math.round(memoryDurationTicks * memoryMultiplier));
		reactionDelayTicks = reactionDelay;
	}

	public float getSightDistance() {
		return sightDistance;
	}

	public float getFieldOfViewAngle() {
		return fieldOfViewAngle;
	}

	public float getHearingMultiplier() {
		return hearingMultiplier;
	}

	public int getMemoryDurationTicks() {
		return memoryDurationTicks;
	}

	public int getReactionDelayTicks() {
		return reactionDelayTicks;
	}

	public float getAggression() {
		return aggression;
	}

	public float getBravery() {
		return bravery;
	}

	public float getCooperation() {
		return cooperation;
	}

	public boolean canJump() {
		return canJump;
	}

	public boolean canDropDown() {
		return canDropDown;
	}

	public boolean canCallAllies() {
		return canCallAllies;
	}

	public boolean canRetreat() {
		return canRetreat;
	}

	public boolean isCommander() {
		return commander;
	}
}
