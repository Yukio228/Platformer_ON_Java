package ai.core;

public class Vector2 {
	private final float x;
	private final float y;

	public Vector2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float x() {
		return x;
	}

	public float y() {
		return y;
	}

	public float distanceTo(float otherX, float otherY) {
		float dx = x - otherX;
		float dy = y - otherY;
		return (float) Math.sqrt(dx * dx + dy * dy);
	}

	public float distanceTo(Vector2 other) {
		return distanceTo(other.x, other.y);
	}

	@Override
	public String toString() {
		return Math.round(x) + "," + Math.round(y);
	}
}
