package ai.core;

import ai.communication.AlertManager;
import ai.difficulty.DifficultyProfile;
import ai.memory.SharedBlackboard;
import ai.metrics.AiMetricsCollector;
import ai.perception.NoiseManager;
import entities.Enemy;
import entities.Player;
import gamestates.Playing;
import levels.Level;

public class EnemyContext {
	private final Enemy enemy;
	private final Playing playing;
	private final int[][] levelData;
	private final int tick;

	public EnemyContext(Enemy enemy, Playing playing, int[][] levelData, int tick) {
		this.enemy = enemy;
		this.playing = playing;
		this.levelData = levelData;
		this.tick = tick;
	}

	public Enemy enemy() {
		return enemy;
	}

	public Player player() {
		return playing.getPlayer();
	}

	public Playing playing() {
		return playing;
	}

	public Level level() {
		return playing.getLevelManager().getCurrentLevel();
	}

	public int[][] levelData() {
		return levelData;
	}

	public int tick() {
		return tick;
	}

	public NoiseManager noiseManager() {
		return playing.getNoiseManager();
	}

	public AlertManager alertManager() {
		return playing.getAlertManager();
	}

	public SharedBlackboard blackboard() {
		return playing.getSharedBlackboard();
	}

	public DifficultyProfile difficulty() {
		return playing.getDifficultyDirector().getCurrentProfile();
	}

	public AiMetricsCollector metrics() {
		return playing.getAiMetricsCollector();
	}
}
