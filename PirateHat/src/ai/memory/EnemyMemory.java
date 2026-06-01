package ai.memory;

import ai.core.Vector2;

public class EnemyMemory {
	private Vector2 lastSeenPlayerPosition;
	private Vector2 lastHeardNoisePosition;
	private Vector2 lastDamageSourcePosition;
	private int lastSeenTick = -100000;
	private int lastHeardTick = -100000;
	private int lastDamageTick = -100000;
	private float confidence;
	private MemoryRecord latestRecord;

	public void update(int currentTick, int memoryDurationTicks) {
		int newestTick = Math.max(lastSeenTick, Math.max(lastHeardTick, lastDamageTick));
		int age = Math.max(0, currentTick - newestTick);
		float duration = Math.max(1, memoryDurationTicks);
		confidence = Math.max(0, 1f - age / duration);
	}

	public void rememberPlayer(float x, float y, int tick) {
		lastSeenPlayerPosition = new Vector2(x, y);
		lastSeenTick = tick;
		confidence = 1f;
		latestRecord = new MemoryRecord(MemoryType.PLAYER_SEEN, lastSeenPlayerPosition, tick, confidence);
	}

	public void rememberNoise(float x, float y, int tick, float confidence) {
		lastHeardNoisePosition = new Vector2(x, y);
		lastHeardTick = tick;
		this.confidence = Math.max(this.confidence, confidence);
		latestRecord = new MemoryRecord(MemoryType.NOISE_HEARD, lastHeardNoisePosition, tick, confidence);
	}

	public void rememberDamage(float x, float y, int tick) {
		lastDamageSourcePosition = new Vector2(x, y);
		lastDamageTick = tick;
		confidence = 1f;
		latestRecord = new MemoryRecord(MemoryType.DAMAGE_SOURCE, lastDamageSourcePosition, tick, confidence);
	}

	public void rememberAlert(float x, float y, int tick, float confidence) {
		lastHeardNoisePosition = new Vector2(x, y);
		lastHeardTick = tick;
		this.confidence = Math.max(this.confidence, confidence);
		latestRecord = new MemoryRecord(MemoryType.ALLY_ALERT, lastHeardNoisePosition, tick, confidence);
	}

	public Vector2 getBestKnownTarget() {
		if (lastSeenPlayerPosition != null && confidence > 0.08f)
			return lastSeenPlayerPosition;
		if (lastDamageSourcePosition != null && confidence > 0.08f)
			return lastDamageSourcePosition;
		if (lastHeardNoisePosition != null && confidence > 0.08f)
			return lastHeardNoisePosition;
		return null;
	}

	public Vector2 getLastSeenPlayerPosition() {
		return lastSeenPlayerPosition;
	}

	public Vector2 getLastHeardNoisePosition() {
		return lastHeardNoisePosition;
	}

	public Vector2 getLastDamageSourcePosition() {
		return lastDamageSourcePosition;
	}

	public int getLastSeenTick() {
		return lastSeenTick;
	}

	public int getLastHeardTick() {
		return lastHeardTick;
	}

	public int getLastDamageTick() {
		return lastDamageTick;
	}

	public float getConfidence() {
		return confidence;
	}

	public MemoryRecord getLatestRecord() {
		return latestRecord;
	}

	public void reset() {
		lastSeenPlayerPosition = null;
		lastHeardNoisePosition = null;
		lastDamageSourcePosition = null;
		lastSeenTick = -100000;
		lastHeardTick = -100000;
		lastDamageTick = -100000;
		confidence = 0;
		latestRecord = null;
	}
}
