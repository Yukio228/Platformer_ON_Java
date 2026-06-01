package ai.metrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ai.core.EnemyAiState;
import entities.Enemy;

public class AiMetricsCollector {
	private final ArrayList<AiEventLog> logs = new ArrayList<>();

	public void record(int timestamp, Enemy enemy, String event, EnemyAiState state, int pathLength, float pathfindingMs, String details) {
		record(timestamp, enemy == null ? "UNKNOWN" : enemy.getEnemyDebugName(), event, state, pathLength, pathfindingMs, details);
	}

	public void record(int timestamp, String enemyType, String event, EnemyAiState state, int pathLength, float pathfindingMs, String details) {
		logs.add(new AiEventLog(timestamp, enemyType, event, state, pathLength, pathfindingMs, details));
	}

	public Path exportCsv(Path root) throws IOException {
		Path logsDir = root.resolve("logs");
		Files.createDirectories(logsDir);
		Path file = logsDir.resolve("ai_metrics.csv");
		ArrayList<String> lines = new ArrayList<>();
		lines.add("timestamp,enemyType,event,state,pathLength,pathfindingMs,details");
		for (AiEventLog log : logs)
			lines.add(log.toCsv());
		Files.write(file, lines, StandardCharsets.UTF_8);
		return file;
	}

	public List<AiEventLog> getLogs() {
		return List.copyOf(logs);
	}

	public void clear() {
		logs.clear();
	}
}
