package tutorial;

import java.awt.Point;

import static utilz.Constants.ObjectConstants.BOX;

public final class TutorialLevelConfig {

	public static final int LEVEL_INDEX = 0;

	public static final Point PLAYER_SPAWN_TILE = new Point(3, 9);

	public static final int MOVEMENT_SHOW_UNTIL_TILE_X = 9;
	public static final int MOVEMENT_COMPLETE_DISTANCE_TILES = 1;
	public static final int JUMP_SHOW_FROM_TILE_X = 4;
	public static final int JUMP_SHOW_UNTIL_TILE_X = 10;
	public static final int JUMP_SKIP_AFTER_TILE_X = 10;
	public static final int BASIC_ATTACK_SHOW_FROM_TILE_X = 24;
	public static final int BASIC_ATTACK_SHOW_UNTIL_TILE_X = 32;
	public static final int BASIC_ATTACK_SKIP_AFTER_TILE_X = 32;
	public static final int POWER_ATTACK_SHOW_FROM_TILE_X = 32;
	public static final int POWER_ATTACK_SHOW_UNTIL_TILE_X = 38;
	public static final int POWER_ATTACK_SKIP_AFTER_TILE_X = 38;
	public static final int RED_POTION_SHOW_FROM_TILE_X = 36;
	public static final int RED_POTION_SHOW_UNTIL_TILE_X = 43;
	public static final int RED_POTION_SKIP_AFTER_TILE_X = 43;
	public static final int FINAL_SHOW_FROM_TILE_X = 39;

	public static final Point[] CRAB_TILES = {
			new Point(26, 9),
			new Point(35, 8),
			new Point(41, 9),
			new Point(45, 9)
	};

	public static final ObjectTile[] OBJECT_TILES = {
			new ObjectTile(29, 9, BOX)
	};

	public static final ObjectTile[] TREE_TILES = {};

	private TutorialLevelConfig() {
	}

	public static boolean isTutorialLevel(int levelIndex) {
		return levelIndex == LEVEL_INDEX;
	}

	public record ObjectTile(int x, int y, int type) {
	}
}
