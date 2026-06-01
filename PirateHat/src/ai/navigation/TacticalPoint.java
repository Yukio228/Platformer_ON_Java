package ai.navigation;

import ai.core.Vector2;

public class TacticalPoint {
	private final int id;
	private final TacticalPointType type;
	private final Vector2 position;
	private boolean occupied;

	public TacticalPoint(int id, TacticalPointType type, Vector2 position) {
		this.id = id;
		this.type = type;
		this.position = position;
	}

	public int getId() {
		return id;
	}

	public TacticalPointType getType() {
		return type;
	}

	public Vector2 getPosition() {
		return position;
	}

	public boolean isOccupied() {
		return occupied;
	}

	public void setOccupied(boolean occupied) {
		this.occupied = occupied;
	}
}
