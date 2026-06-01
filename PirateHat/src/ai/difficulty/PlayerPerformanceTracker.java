package ai.difficulty;

public class PlayerPerformanceTracker {
	private int deaths;
	private int hitsTaken;
	private int defeatedEnemies;
	private int healingUsed;
	private int trapFalls;
	private int missedAttacks;
	private int successfulAttacks;
	private int levelTicks;
	private int trapCooldown;

	public void update() {
		levelTicks++;
		if (trapCooldown > 0)
			trapCooldown--;
	}

	public void recordDeath() {
		deaths++;
	}

	public void recordHitTaken() {
		hitsTaken++;
	}

	public void recordEnemyDefeated() {
		defeatedEnemies++;
	}

	public void recordHealingUsed() {
		healingUsed++;
	}

	public void recordTrapContact() {
		if (trapCooldown > 0)
			return;
		trapFalls++;
		trapCooldown = 120;
	}

	public void recordAttack(boolean hit) {
		if (hit)
			successfulAttacks++;
		else
			missedAttacks++;
	}

	public float getPerformanceScore() {
		float success = defeatedEnemies * 0.18f + successfulAttacks * 0.015f;
		float pressure = deaths * 0.55f + hitsTaken * 0.08f + healingUsed * 0.06f + trapFalls * 0.12f + missedAttacks * 0.01f;
		float timePenalty = Math.min(0.25f, levelTicks / 60000f);
		return clamp(0.5f + success - pressure - timePenalty);
	}

	public void resetForLevel() {
		hitsTaken = 0;
		defeatedEnemies = 0;
		healingUsed = 0;
		trapFalls = 0;
		missedAttacks = 0;
		successfulAttacks = 0;
		levelTicks = 0;
		trapCooldown = 0;
	}

	private float clamp(float value) {
		return Math.max(0f, Math.min(1f, value));
	}

	public int getDeaths() {
		return deaths;
	}

	public int getHitsTaken() {
		return hitsTaken;
	}

	public int getDefeatedEnemies() {
		return defeatedEnemies;
	}
}
