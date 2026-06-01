package ai.memory;

import ai.core.Vector2;

public class MemoryRecord {
	private final MemoryType type;
	private final Vector2 position;
	private final int tick;
	private final float confidence;

	public MemoryRecord(MemoryType type, Vector2 position, int tick, float confidence) {
		this.type = type;
		this.position = position;
		this.tick = tick;
		this.confidence = confidence;
	}

	public MemoryType getType() {
		return type;
	}

	public Vector2 getPosition() {
		return position;
	}

	public int getTick() {
		return tick;
	}

	public float getConfidence() {
		return confidence;
	}
}
