package ai.communication;

import entities.Enemy;

public class AlertEvent {
	private final float x;
	private final float y;
	private final float radius;
	private final float danger;
	private final Enemy source;
	private int remainingTicks;

	public AlertEvent(float x, float y, float radius, float danger, int remainingTicks, Enemy source) {
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.danger = danger;
		this.remainingTicks = remainingTicks;
		this.source = source;
	}

	public void update() {
		remainingTicks--;
	}

	public boolean isAlive() {
		return remainingTicks > 0;
	}

	public boolean reaches(Enemy enemy) {
		if (enemy == source)
			return false;
		float dx = x - enemy.getCenterX();
		float dy = y - enemy.getCenterY();
		return dx * dx + dy * dy <= radius * radius;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getRadius() {
		return radius;
	}

	public float getDanger() {
		return danger;
	}

	public Enemy getSource() {
		return source;
	}

	public int getRemainingTicks() {
		return remainingTicks;
	}
}
