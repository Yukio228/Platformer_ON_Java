package ai.perception;

import static utilz.Constants.Directions.RIGHT;
import static utilz.HelpMethods.IsTileSolid;

import ai.core.EnemyAiProfile;
import entities.Enemy;
import entities.Player;
import main.Game;

public class VisionSensor {
	private float suspicionLevel;
	private boolean hasLineOfSight;

	public boolean update(int[][] lvlData, Enemy enemy, Player player, EnemyAiProfile profile) {
		float enemyX = enemy.getCenterX();
		float enemyY = enemy.getCenterY();
		float playerX = player.getHitbox().x + player.getHitbox().width / 2f;
		float playerY = player.getHitbox().y + player.getHitbox().height / 2f;
		float dx = playerX - enemyX;
		float dy = playerY - enemyY;
		float distance = (float) Math.sqrt(dx * dx + dy * dy);
		float maxDistance = profile.getSightDistance() * Game.TILES_SIZE;
		boolean closePeripheral = distance <= Game.TILES_SIZE * 1.15f;
		boolean inDistance = distance <= maxDistance;
		boolean inFov = closePeripheral || isInFieldOfView(enemy, dx, dy, profile.getFieldOfViewAngle());

		hasLineOfSight = inDistance && inFov && isLineClear(lvlData, enemyX, enemyY, playerX, playerY);

		if (hasLineOfSight) {
			float closeness = 1f - Math.min(1f, distance / Math.max(1f, maxDistance));
			suspicionLevel += 2.5f + closeness * 8f;
		} else {
			suspicionLevel -= 1.35f;
		}

		suspicionLevel = Math.max(0, Math.min(100, suspicionLevel));
		return suspicionLevel >= 100;
	}

	private boolean isInFieldOfView(Enemy enemy, float dx, float dy, float fovDegrees) {
		if (dx == 0 && dy == 0)
			return true;

		float facingX = enemy.getWalkDir() == RIGHT ? 1f : -1f;
		float dot = dx * facingX;
		float length = (float) Math.sqrt(dx * dx + dy * dy);
		float cos = dot / length;
		float limit = (float) Math.cos(Math.toRadians(fovDegrees / 2f));
		return cos >= limit;
	}

	private boolean isLineClear(int[][] lvlData, float x1, float y1, float x2, float y2) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		int steps = Math.max(1, (int) (Math.max(Math.abs(dx), Math.abs(dy)) / (Game.TILES_SIZE / 4f)));
		for (int i = 1; i < steps; i++) {
			float x = x1 + dx * (i / (float) steps);
			float y = y1 + dy * (i / (float) steps);
			int tileX = (int) (x / Game.TILES_SIZE);
			int tileY = (int) (y / Game.TILES_SIZE);
			if (IsTileSolid(tileX, tileY, lvlData))
				return false;
		}
		return true;
	}

	public float getSuspicionLevel() {
		return suspicionLevel;
	}

	public boolean hasLineOfSight() {
		return hasLineOfSight;
	}

	public void forceSuspicion(float value) {
		suspicionLevel = Math.max(suspicionLevel, value);
	}

	public void reset() {
		suspicionLevel = 0;
		hasLineOfSight = false;
	}
}
