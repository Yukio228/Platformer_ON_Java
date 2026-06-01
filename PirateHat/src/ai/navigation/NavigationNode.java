package ai.navigation;

import main.Game;

public class NavigationNode {
	private final int id;
	private final int tileX;
	private final int tileY;

	public NavigationNode(int id, int tileX, int tileY) {
		this.id = id;
		this.tileX = tileX;
		this.tileY = tileY;
	}

	public int getId() {
		return id;
	}

	public int getTileX() {
		return tileX;
	}

	public int getTileY() {
		return tileY;
	}

	public float getWorldX() {
		return tileX * Game.TILES_SIZE + Game.TILES_SIZE / 2f;
	}

	public float getWorldY() {
		return tileY * Game.TILES_SIZE + Game.TILES_SIZE / 2f;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof NavigationNode other))
			return false;
		return id == other.id;
	}

	@Override
	public int hashCode() {
		return id;
	}

	@Override
	public String toString() {
		return Integer.toString(id);
	}
}
