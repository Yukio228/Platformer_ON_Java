package ai.perception;

public class NoiseEvent {
	private final NoiseType type;
	private final float x;
	private final float y;
	private final float radius;
	private final float intensity;
	private int remainingTicks;

	public NoiseEvent(NoiseType type, float x, float y, float radius, float intensity, int remainingTicks) {
		this.type = type;
		this.x = x;
		this.y = y;
		this.radius = radius;
		this.intensity = intensity;
		this.remainingTicks = remainingTicks;
	}

	public void update() {
		remainingTicks--;
	}

	public boolean isAlive() {
		return remainingTicks > 0;
	}

	public boolean canBeHeardFrom(float listenerX, float listenerY, float hearingMultiplier) {
		float dx = x - listenerX;
		float dy = y - listenerY;
		float audibleRadius = radius * hearingMultiplier;
		return dx * dx + dy * dy <= audibleRadius * audibleRadius;
	}

	public NoiseType getType() {
		return type;
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

	public float getIntensity() {
		return intensity;
	}

	public int getRemainingTicks() {
		return remainingTicks;
	}
}
