package objects;

import java.awt.geom.Rectangle2D;

import main.Game;

public class InventoryPickup {

	private int x, y, itemType;
	private Rectangle2D.Float hitbox;
	private boolean active = true;
	private float hoverOffset;
	private int hoverDir = 1;
	private int maxHoverOffset = (int) (6 * Game.SCALE);

	public InventoryPickup(int x, int y, int itemType) {
		this.x = x;
		this.y = y;
		this.itemType = itemType;
		initHitbox();
	}

	private void initHitbox() {
		int size = (int) (14 * Game.SCALE);
		hitbox = new Rectangle2D.Float(x - size / 2, y - size / 2, size, size);
	}

	public void update() {
		hoverOffset += 0.06f * Game.SCALE * hoverDir;

		if (hoverOffset >= maxHoverOffset)
			hoverDir = -1;
		else if (hoverOffset <= 0)
			hoverDir = 1;

		hitbox.y = y - hitbox.height / 2 + hoverOffset;
	}

	public int getItemType() {
		return itemType;
	}

	public Rectangle2D.Float getHitbox() {
		return hitbox;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
