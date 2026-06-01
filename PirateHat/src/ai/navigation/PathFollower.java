package ai.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ai.core.EnemyAiProfile;
import entities.Enemy;
import main.Game;

public class PathFollower {
	private List<NavigationNode> path = Collections.emptyList();
	private int index;
	private float lastX;
	private int stuckTicks;
	private boolean needsRepath;

	public void setPath(List<NavigationNode> path, Enemy enemy) {
		this.path = path == null ? Collections.emptyList() : new ArrayList<>(path);
		index = this.path.size() > 1 ? 1 : 0;
		lastX = enemy.getCenterX();
		stuckTicks = 0;
		needsRepath = false;
	}

	public boolean follow(Enemy enemy, int[][] lvlData, EnemyAiProfile profile, float speedMultiplier) {
		if (path.isEmpty() || index >= path.size())
			return false;

		NavigationNode target = path.get(index);
		float dx = target.getWorldX() - enemy.getCenterX();
		if (Math.abs(dx) <= Game.TILES_SIZE * 0.25f) {
			index++;
			return index < path.size();
		}

		enemy.turnTowardsX(target.getWorldX());
		if (profile.canJump() && target.getWorldY() < enemy.getCenterY() - Game.TILES_SIZE * 0.6f)
			enemy.aiJump();

		boolean moved = enemy.aiMove(lvlData, speedMultiplier);
		if (Math.abs(enemy.getCenterX() - lastX) < 0.15f)
			stuckTicks++;
		else
			stuckTicks = 0;
		lastX = enemy.getCenterX();
		if (stuckTicks > 45) {
			needsRepath = true;
			stuckTicks = 0;
		}
		return moved;
	}

	public boolean isFinished() {
		return path.isEmpty() || index >= path.size();
	}

	public boolean needsRepath() {
		return needsRepath;
	}

	public void clearRepathRequest() {
		needsRepath = false;
	}

	public List<NavigationNode> getPath() {
		return Collections.unmodifiableList(path);
	}

	public NavigationNode getCurrentTarget() {
		if (path.isEmpty() || index >= path.size())
			return null;
		return path.get(index);
	}

	public void reset() {
		path = Collections.emptyList();
		index = 0;
		stuckTicks = 0;
		needsRepath = false;
	}
}
