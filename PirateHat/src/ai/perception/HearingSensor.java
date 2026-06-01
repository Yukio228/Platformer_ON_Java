package ai.perception;

import ai.core.EnemyAiProfile;
import entities.Enemy;

public class HearingSensor {
	public NoiseEvent hear(NoiseManager noiseManager, Enemy enemy, EnemyAiProfile profile) {
		return noiseManager.findNearestAudibleNoise(enemy.getCenterX(), enemy.getCenterY(), profile.getHearingMultiplier());
	}
}
