package database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import entities.PlayerSkin;

public class SaveManager {

	private static final boolean DEBUG_UNLOCK_ALL_LEVELS = true;
	private static final String SAVE_DIR = ".platformer_tutorial";
	private static final String SAVE_FILE = "save.properties";
	private static final String KEY_UNLOCKED_LEVELS = "unlockedLevels";
	private static final String KEY_PLAYER_SKIN = "playerSkin";

	private final Path savePath;
	private int unlockedLevels = 1;
	private PlayerSkin playerSkin = PlayerSkin.DEFAULT;
	private boolean enabled = true;

	public SaveManager() {
		savePath = Path.of(System.getProperty("user.home"), SAVE_DIR, SAVE_FILE);
		load();
	}

	public void ensureProgress(int totalLevels) {
		if (DEBUG_UNLOCK_ALL_LEVELS)
			unlockedLevels = totalLevels;
		else
			unlockedLevels = clampUnlockedLevels(unlockedLevels, totalLevels);

		save();
	}

	public void unlockNextLevel(int completedLevelIndex, int totalLevels) {
		int nextUnlocked = DEBUG_UNLOCK_ALL_LEVELS ? totalLevels : clampUnlockedLevels(completedLevelIndex + 2, totalLevels);
		if (nextUnlocked <= unlockedLevels)
			return;

		unlockedLevels = nextUnlocked;
		save();
	}

	public boolean isLevelUnlocked(int levelIndex) {
		return DEBUG_UNLOCK_ALL_LEVELS || levelIndex < unlockedLevels;
	}

	public int getUnlockedLevels() {
		return unlockedLevels;
	}

	public PlayerSkin getPlayerSkin() {
		return playerSkin;
	}

	public void savePlayerSkin(PlayerSkin playerSkin) {
		this.playerSkin = playerSkin == null ? PlayerSkin.DEFAULT : playerSkin;
		save();
	}

	public boolean isEnabled() {
		return enabled;
	}

	private void load() {
		if (!Files.isRegularFile(savePath))
			return;

		Properties properties = new Properties();
		try (InputStream in = Files.newInputStream(savePath)) {
			properties.load(in);
			unlockedLevels = parseInt(properties.getProperty(KEY_UNLOCKED_LEVELS), 1);
			playerSkin = parsePlayerSkin(properties.getProperty(KEY_PLAYER_SKIN));
			System.out.println("Local saves loaded: " + savePath);
		} catch (IOException e) {
			enabled = false;
			System.out.println("Local saves disabled: " + e.getMessage());
		}
	}

	private void save() {
		if (!enabled)
			return;

		Properties properties = new Properties();
		properties.setProperty(KEY_UNLOCKED_LEVELS, Integer.toString(unlockedLevels));
		properties.setProperty(KEY_PLAYER_SKIN, playerSkin.name());

		try {
			Files.createDirectories(savePath.getParent());
			try (OutputStream out = Files.newOutputStream(savePath)) {
				properties.store(out, "Platformer tutorial save");
			}
		} catch (IOException e) {
			enabled = false;
			System.out.println("Local saves disabled: " + e.getMessage());
		}
	}

	private int parseInt(String value, int fallback) {
		try {
			return value == null ? fallback : Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private PlayerSkin parsePlayerSkin(String value) {
		try {
			return value == null ? PlayerSkin.DEFAULT : PlayerSkin.valueOf(value);
		} catch (IllegalArgumentException e) {
			return PlayerSkin.DEFAULT;
		}
	}

	private int clampUnlockedLevels(int value, int totalLevels) {
		return Math.max(1, Math.min(totalLevels, value));
	}
}
