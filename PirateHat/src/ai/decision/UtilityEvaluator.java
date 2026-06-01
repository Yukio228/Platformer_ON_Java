package ai.decision;

import java.util.EnumMap;
import java.util.Map;

import ai.core.EnemyAiProfile;
import ai.memory.EnemyMemory;
import entities.Enemy;
import entities.Player;
import main.Game;

public class UtilityEvaluator {
	public EnemyAction chooseAction(Enemy enemy, Player player, EnemyAiProfile profile, EnemyMemory memory, boolean attackReady, int nearbyAllies) {
		Map<EnemyAction, Float> scores = new EnumMap<>(EnemyAction.class);
		float distance = Math.max(1f, Math.abs(enemy.getCenterX() - (player.getHitbox().x + player.getHitbox().width / 2f)));
		float attackRange = Math.max(1f, enemy.getAttackDistance() * 1.25f);
		float healthRisk = 1f - enemy.getHealthPercent();
		float memoryConfidence = memory.getConfidence();

		scores.put(EnemyAction.ATTACK, clamp((1f - Math.min(1f, distance / attackRange)) * 0.55f + (attackReady ? 0.25f : 0f) + profile.getAggression() * 0.2f));
		scores.put(EnemyAction.CHASE, clamp(memoryConfidence * 0.45f + (1f - Math.min(1f, distance / (Game.TILES_SIZE * 8f))) * 0.25f + enemy.getHealthPercent() * 0.2f));
		scores.put(EnemyAction.RETREAT, profile.canRetreat() ? clamp(healthRisk * (1.25f - profile.getBravery())) : 0f);
		scores.put(EnemyAction.CALL_ALLIES, profile.canCallAllies() ? clamp(profile.getCooperation() * 0.45f + nearbyAllies * 0.12f + profile.getAggression() * 0.2f) : 0f);
		scores.put(EnemyAction.SEARCH, clamp(memoryConfidence * 0.6f));
		scores.put(EnemyAction.INVESTIGATE, memory.getLastHeardNoisePosition() != null ? clamp(memoryConfidence * 0.65f + 0.15f) : 0f);
		scores.put(EnemyAction.PATROL, 0.2f);

		EnemyAction best = EnemyAction.PATROL;
		float bestScore = -1f;
		for (Map.Entry<EnemyAction, Float> entry : scores.entrySet()) {
			if (entry.getValue() > bestScore) {
				bestScore = entry.getValue();
				best = entry.getKey();
			}
		}
		return best;
	}

	private float clamp(float value) {
		return Math.max(0f, Math.min(1f, value));
	}
}
