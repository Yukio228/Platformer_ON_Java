package ai.difficulty;

public class DifficultyProfile {
	private final int reactionDelay;
	private final float sightDistanceMultiplier;
	private final float memoryDurationMultiplier;
	private final float aggressionModifier;
	private final int reinforcementCount;
	private final float bossAggressionModifier;

	public DifficultyProfile(int reactionDelay, float sightDistanceMultiplier, float memoryDurationMultiplier, float aggressionModifier, int reinforcementCount, float bossAggressionModifier) {
		this.reactionDelay = reactionDelay;
		this.sightDistanceMultiplier = sightDistanceMultiplier;
		this.memoryDurationMultiplier = memoryDurationMultiplier;
		this.aggressionModifier = aggressionModifier;
		this.reinforcementCount = reinforcementCount;
		this.bossAggressionModifier = bossAggressionModifier;
	}

	public static DifficultyProfile neutral() {
		return new DifficultyProfile(18, 1f, 1f, 1f, 1, 1f);
	}

	public int getReactionDelay() {
		return reactionDelay;
	}

	public float getSightDistanceMultiplier() {
		return sightDistanceMultiplier;
	}

	public float getMemoryDurationMultiplier() {
		return memoryDurationMultiplier;
	}

	public float getAggressionModifier() {
		return aggressionModifier;
	}

	public int getReinforcementCount() {
		return reinforcementCount;
	}

	public float getBossAggressionModifier() {
		return bossAggressionModifier;
	}
}
