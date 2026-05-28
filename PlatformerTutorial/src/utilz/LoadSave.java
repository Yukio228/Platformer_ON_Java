package utilz;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import javax.imageio.ImageIO;

public class LoadSave {

	public static final String PLAYER_ATLAS = "player_sprites.png";
	public static final String PLAYER_SKIN_KING = "player_skin_king.png";
	public static final String PLAYER_SKIN_BOMB_GUY = "player_skin_bomb_guy.png";
	public static final String PLAYER_SKIN_KING_PIG = "player_skin_king_pig.png";
	public static final String LEVEL_ATLAS = "outside_sprites.png";
	public static final String MENU_BUTTONS = "button_atlas.png";
	public static final String MENU_BACKGROUND = "menu_background.png";
	public static final String PAUSE_BACKGROUND = "pause_menu.png";
	public static final String SOUND_BUTTONS = "sound_button.png";
	public static final String URM_BUTTONS = "urm_buttons.png";
	public static final String VOLUME_BUTTONS = "volume_buttons.png";
	public static final String MENU_BACKGROUND_IMG = "background_menu.png";
	public static final String PLAYING_BG_IMG = "playing_bg_img.png";
	public static final String BIG_CLOUDS = "big_clouds.png";
	public static final String SMALL_CLOUDS = "small_clouds.png";
	public static final String CRABBY_SPRITE = "crabby_sprite.png";
	public static final String STATUS_BAR = "health_power_bar.png";
	public static final String COMPLETED_IMG = "completed_sprite.png";
	public static final String POTION_ATLAS = "potions_sprites.png";
	public static final String CONTAINER_ATLAS = "objects_sprites.png";
	public static final String TRAP_ATLAS = "trap_atlas.png";
	public static final String CANNON_ATLAS = "cannon_atlas.png";
	public static final String CANNON_BALL = "ball.png";
	public static final String DEATH_SCREEN = "death_screen.png";
	public static final String OPTIONS_MENU = "options_background.png";
	public static final String PINKSTAR_ATLAS = "pinkstar_atlas.png";
	public static final String QUESTION_ATLAS = "question_atlas.png";
	public static final String EXCLAMATION_ATLAS = "exclamation_atlas.png";
	public static final String SHARK_ATLAS = "shark_atlas.png";
	public static final String BOSS_ATLAS = "boss_king_pig.png";
	public static final String BALD_PIRATE_ATLAS = "bald_pirate_atlas.png";
	public static final String CUCUMBER_ATLAS = "cucumber_atlas.png";
	public static final String PIRATE_CAPTAIN_ATLAS = "pirate_captain_atlas.png";
	public static final String GRASS_ATLAS = "grass_atlas.png";
	public static final String TREE_ONE_ATLAS = "tree_one_atlas.png";
	public static final String TREE_TWO_ATLAS = "tree_two_atlas.png";
	public static final String GAME_COMPLETED = "game_completed.png";
	public static final String RAIN_PARTICLE = "rain_particle.png";
	public static final String WATER_TOP = "water_atlas_animation.png";
	public static final String WATER_BOTTOM = "water.png";
	public static final String SHIP = "ship.png";
	public static final String CHECKPOINT = "checkpoint_atlas.png";
	public static final String INVENTORY_SLOT = "inventory_slot.png";
	public static final String UI_GREEN_PANEL = "ui_green_panel.png";
	public static final String UI_GREEN_BUTTON = "ui_green_button.png";
	public static final String UI_SMALL_BANNER = "ui_small_banner.png";
	public static final String UI_FONT_SMALL = "ui_font_small.png";
	public static final String UI_FONT_BIG = "ui_font_big.png";
	public static final String UI_YELLOW_BUTTON_TILES = "ui_yellow_button_tiles.png";
	public static final String UI_GREEN_BUTTON_TILES = "ui_green_button_tiles.png";
	public static final String UI_SLIDER_TILES = "ui_slider_tiles.png";
	public static final String ITEM_RED_POTION = "item_red_potion.png";
	public static final String ITEM_BLUE_POTION = "item_blue_potion.png";
	public static final String ITEM_GOLD_COIN = "item_gold_coin.png";
	public static final String ITEM_SILVER_COIN = "item_silver_coin.png";
	public static final String ITEM_KEY = "item_key.png";
	public static final String ITEM_MAP = "item_map.png";

	public static BufferedImage GetSpriteAtlas(String fileName) {
		try (InputStream is = LoadSave.class.getResourceAsStream("/" + fileName)) {
			if (is != null)
				return ImageIO.read(is);
		} catch (IOException e) {
			e.printStackTrace();
		}

		File file = getResourceFile(fileName);
		if (file != null)
			try {
				return ImageIO.read(file);
			} catch (IOException e) {
				e.printStackTrace();
			}

		throw new IllegalStateException("Could not load resource: " + fileName);
	}

	public static BufferedImage[] GetAllLevels() {
		URL url = LoadSave.class.getResource("/lvls");
		File file = null;

		if (url != null) {
			try {
				file = new File(url.toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}

		if (file == null || !file.isDirectory())
			file = getResourceDir("lvls");

		if (file == null)
			throw new IllegalStateException("Could not load levels directory");

		File[] files = file.listFiles();
		if (files == null)
			throw new IllegalStateException("Could not read levels directory: " + file.getAbsolutePath());

		File[] filesSorted = new File[files.length];

		for (int i = 0; i < filesSorted.length; i++)
			for (int j = 0; j < files.length; j++) {
				if (files[j].getName().equals((i + 1) + ".png"))
					filesSorted[i] = files[j];

			}

		BufferedImage[] imgs = new BufferedImage[filesSorted.length];

		for (int i = 0; i < imgs.length; i++)
			try {
				imgs[i] = ImageIO.read(filesSorted[i]);
			} catch (IOException e) {
				e.printStackTrace();
			}

		return imgs;
	}

	private static File getResourceFile(String fileName) {
		File[] candidates = { new File("res", fileName), new File("PlatformerTutorial/res", fileName), new File("../res", fileName), new File("../../res", fileName) };

		for (File file : candidates)
			if (file.isFile())
				return file;

		return null;
	}

	private static File getResourceDir(String dirName) {
		File[] candidates = { new File("res", dirName), new File("PlatformerTutorial/res", dirName), new File("../res", dirName), new File("../../res", dirName) };

		for (File file : candidates)
			if (file.isDirectory())
				return file;

		return null;
	}

}
