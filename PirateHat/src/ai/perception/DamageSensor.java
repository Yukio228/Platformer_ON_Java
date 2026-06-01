package ai.perception;

import ai.core.Vector2;

public class DamageSensor {
	private Vector2 lastDamageSource;

	public void recordDamageSource(float x, float y) {
		lastDamageSource = new Vector2(x, y);
	}

	public Vector2 consumeDamageSource() {
		Vector2 result = lastDamageSource;
		lastDamageSource = null;
		return result;
	}
}
