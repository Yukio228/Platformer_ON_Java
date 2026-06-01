package ai.difficulty;

import ai.metrics.AiMetricsCollector;

public class DifficultyDirector {
	private final PlayerPerformanceTracker tracker;
	private DifficultyProfile currentProfile = DifficultyProfile.neutral();
	private int updateTick;

	public DifficultyDirector(PlayerPerformanceTracker tracker) {
		this.tracker = tracker;
	}

	public void update(AiMetricsCollector metrics, int tick) {
		updateTick++;
		if (updateTick < 240)
			return;
		updateTick = 0;

		float score = tracker.getPerformanceScore();
		int reaction = clampInt(Math.round(30 - score * 18), 12, 30);
		float sight = clamp(0.90f + score * 0.25f, 0.90f, 1.15f);
		float aggression = clamp(0.85f + score * 0.35f, 0.85f, 1.20f);
		float memory = clamp(0.9f + score * 0.25f, 0.9f, 1.15f);
		int reinforcements = clampInt(Math.round(score * 2f), 0, 2);
		float boss = clamp(0.85f + score * 0.35f, 0.85f, 1.20f);
		DifficultyProfile next = new DifficultyProfile(reaction, sight, memory, aggression, reinforcements, boss);

		if (hasChanged(next)) {
			currentProfile = next;
			if (metrics != null)
				metrics.record(tick, "DIRECTOR", "DIFFICULTY_CHANGED", null, 0, 0, "reaction=" + reaction + ";sight=" + sight + ";aggression=" + aggression);
		}
	}

	private boolean hasChanged(DifficultyProfile next) {
		return currentProfile.getReactionDelay() != next.getReactionDelay()
				|| Math.abs(currentProfile.getSightDistanceMultiplier() - next.getSightDistanceMultiplier()) > 0.01f
				|| Math.abs(currentProfile.getAggressionModifier() - next.getAggressionModifier()) > 0.01f;
	}

	private int clampInt(int value, int min, int max) {
		return Math.max(min, Math.min(max, value));
	}

	private float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	public DifficultyProfile getCurrentProfile() {
		return currentProfile;
	}
}
