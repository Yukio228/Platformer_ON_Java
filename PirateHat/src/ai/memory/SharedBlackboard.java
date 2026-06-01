package ai.memory;

import java.util.HashMap;
import java.util.Map;

import ai.core.Vector2;
import entities.Enemy;

public class SharedBlackboard {
	private final Map<Enemy, Vector2> claimedTargets = new HashMap<>();

	public void claimTarget(Enemy enemy, Vector2 target) {
		if (enemy != null && target != null)
			claimedTargets.put(enemy, target);
	}

	public boolean isCrowded(Vector2 target, Enemy requester, float radius) {
		int count = 0;
		for (Map.Entry<Enemy, Vector2> entry : claimedTargets.entrySet()) {
			if (entry.getKey() == requester || !entry.getKey().isActive())
				continue;
			if (entry.getValue().distanceTo(target) <= radius)
				count++;
		}
		return count >= 2;
	}

	public void clearInactive() {
		claimedTargets.entrySet().removeIf(e -> !e.getKey().isActive());
	}

	public void clear() {
		claimedTargets.clear();
	}
}
