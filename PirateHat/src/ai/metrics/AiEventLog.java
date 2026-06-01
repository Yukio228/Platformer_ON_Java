package ai.metrics;

import ai.core.EnemyAiState;

public class AiEventLog {
	private final int timestamp;
	private final String enemyType;
	private final String event;
	private final EnemyAiState state;
	private final int pathLength;
	private final float pathfindingMs;
	private final String details;

	public AiEventLog(int timestamp, String enemyType, String event, EnemyAiState state, int pathLength, float pathfindingMs, String details) {
		this.timestamp = timestamp;
		this.enemyType = enemyType;
		this.event = event;
		this.state = state;
		this.pathLength = pathLength;
		this.pathfindingMs = pathfindingMs;
		this.details = details == null ? "" : details;
	}

	public String toCsv() {
		String stateText = state == null ? "" : state.name();
		return timestamp + "," + enemyType + "," + event + "," + stateText + "," + pathLength + "," + pathfindingMs + "," + escape(details);
	}

	private String escape(String value) {
		if (!value.contains(",") && !value.contains("\""))
			return value;
		return "\"" + value.replace("\"", "\"\"") + "\"";
	}
}
