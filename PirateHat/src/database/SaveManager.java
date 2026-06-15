package database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;

import entities.PlayerInventory;
import entities.PlayerSkin;

public class SaveManager {

	private static final boolean DEBUG_UNLOCK_ALL_LEVELS = false;
	private static final String SAVE_DIR = ".pirate_hat";
	private static final String SAVE_FILE = "save.properties";
	private static final String KEY_UNLOCKED_LEVELS = "unlockedLevels";
	private static final String KEY_PLAYER_SKIN = "playerSkin";
	private static final String KEY_GOLD_BALANCE = "goldBalance";
	private static final String KEY_PURCHASED_SKINS = "purchasedSkins";
	private static final String KEY_AUDIO_VOLUME = "audioVolume";
	private static final String KEY_MUSIC_MUTED = "musicMuted";
	private static final String KEY_SFX_MUTED = "sfxMuted";
	private static final String KEY_RESOLUTION_INDEX = "resolutionIndex";
	private static final String KEY_SCREEN_MODE = "screenMode";
	private static final String KEY_HAS_SESSION = "hasSession";
	private static final String KEY_SESSION_LEVEL = "sessionLevel";
	private static final String KEY_PLAYER_X = "playerX";
	private static final String KEY_PLAYER_Y = "playerY";
	private static final String KEY_RESPAWN_X = "respawnX";
	private static final String KEY_RESPAWN_Y = "respawnY";
	private static final String KEY_PLAYER_HEALTH = "playerHealth";
	private static final String KEY_PLAYER_POWER = "playerPower";
	private static final String KEY_INVENTORY = "inventory";
	private static final String KEY_ENEMY_ACTIVE = "enemyActive";
	private static final String KEY_ENEMY_HEALTH = "enemyHealth";
	private static final String KEY_POTION_ACTIVE = "potionActive";
	private static final String KEY_CONTAINER_ACTIVE = "containerActive";

	private final Path savePath;
	private int unlockedLevels = 1;
	private int goldBalance;
	private Set<PlayerSkin> purchasedSkins = EnumSet.of(PlayerSkin.DEFAULT);
	private PlayerSkin playerSkin = PlayerSkin.DEFAULT;
	private float audioVolume = 0.5f;
	private boolean musicMuted;
	private boolean sfxMuted;
	private int resolutionIndex = 1;
	private String screenMode = "WINDOWED";
	private SessionData session;
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

		if (session != null)
			session.levelIndex = clampLevelIndex(session.levelIndex, totalLevels);

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
		this.playerSkin = canUseSkin(playerSkin) ? playerSkin : PlayerSkin.DEFAULT;
		save();
	}

	public int getGoldBalance() {
		return goldBalance;
	}

	public boolean addGold(int amount) {
		if (amount <= 0)
			return false;

		long newBalance = (long) goldBalance + amount;
		goldBalance = (int) Math.min(Integer.MAX_VALUE, newBalance);
		save();
		return true;
	}

	public boolean spendGold(int amount) {
		if (amount < 0 || !canAfford(amount))
			return false;

		goldBalance -= amount;
		save();
		return true;
	}

	public boolean canAfford(int amount) {
		return amount >= 0 && goldBalance >= amount;
	}

	public boolean isSkinPurchased(PlayerSkin skin) {
		return skin != null && purchasedSkins.contains(skin);
	}

	public boolean purchaseSkin(PlayerSkin skin, int cost) {
		if (skin == null || !skin.isPlayable() || cost < 0)
			return false;
		if (isSkinPurchased(skin))
			return true;
		if (!canAfford(cost))
			return false;

		goldBalance -= cost;
		purchasedSkins.add(skin);
		save();
		return true;
	}

	public float getAudioVolume() {
		return audioVolume;
	}

	public boolean isMusicMuted() {
		return musicMuted;
	}

	public boolean isSfxMuted() {
		return sfxMuted;
	}

	public void saveAudioSettings(float audioVolume, boolean musicMuted, boolean sfxMuted) {
		this.audioVolume = clampFloat(audioVolume, 0f, 1f);
		this.musicMuted = musicMuted;
		this.sfxMuted = sfxMuted;
		save();
	}

	public int getResolutionIndex() {
		return resolutionIndex;
	}

	public String getScreenMode() {
		return screenMode;
	}

	public void saveDisplaySettings(int resolutionIndex, String screenMode) {
		this.resolutionIndex = Math.max(0, resolutionIndex);
		this.screenMode = sanitizeScreenMode(screenMode);
		save();
	}

	public SessionData getSession(int totalLevels) {
		if (session == null)
			return null;

		SessionData copy = session.copy();
		copy.levelIndex = clampLevelIndex(copy.levelIndex, totalLevels);
		return copy;
	}

	public void saveSession(SessionData session, int totalLevels) {
		if (session == null)
			return;

		this.session = session.copy();
		this.session.levelIndex = clampLevelIndex(this.session.levelIndex, totalLevels);
		sanitizeSessionCurrency();
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
			boolean hasGoldBalance = properties.containsKey(KEY_GOLD_BALANCE);
			boolean hasPurchasedSkins = properties.containsKey(KEY_PURCHASED_SKINS);
			goldBalance = Math.max(0, parseInt(properties.getProperty(KEY_GOLD_BALANCE), 0));
			purchasedSkins = parsePurchasedSkins(properties.getProperty(KEY_PURCHASED_SKINS));
			PlayerSkin parsedSkin = parsePlayerSkin(properties.getProperty(KEY_PLAYER_SKIN));
			if (!hasPurchasedSkins && parsedSkin.isPlayable())
				purchasedSkins.add(parsedSkin);
			playerSkin = canUseSkin(parsedSkin) ? parsedSkin : PlayerSkin.DEFAULT;
			audioVolume = clampFloat(parseFloat(properties.getProperty(KEY_AUDIO_VOLUME), 0.5f), 0f, 1f);
			musicMuted = Boolean.parseBoolean(properties.getProperty(KEY_MUSIC_MUTED, "false"));
			sfxMuted = Boolean.parseBoolean(properties.getProperty(KEY_SFX_MUTED, "false"));
			resolutionIndex = Math.max(0, parseInt(properties.getProperty(KEY_RESOLUTION_INDEX), 1));
			screenMode = sanitizeScreenMode(properties.getProperty(KEY_SCREEN_MODE));
			session = parseSession(properties);
			migrateLegacyGold(hasGoldBalance);
			sanitizeSessionCurrency();
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
		properties.setProperty(KEY_GOLD_BALANCE, Integer.toString(goldBalance));
		properties.setProperty(KEY_PURCHASED_SKINS, toPurchasedSkinsCsv());
		properties.setProperty(KEY_AUDIO_VOLUME, Float.toString(audioVolume));
		properties.setProperty(KEY_MUSIC_MUTED, Boolean.toString(musicMuted));
		properties.setProperty(KEY_SFX_MUTED, Boolean.toString(sfxMuted));
		properties.setProperty(KEY_RESOLUTION_INDEX, Integer.toString(resolutionIndex));
		properties.setProperty(KEY_SCREEN_MODE, screenMode);
		writeSession(properties);

		try {
			Files.createDirectories(savePath.getParent());
			try (OutputStream out = Files.newOutputStream(savePath)) {
				properties.store(out, "Pirate Hat save");
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

	private float parseFloat(String value, float fallback) {
		try {
			return value == null ? fallback : Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}

	private float clampFloat(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	private String sanitizeScreenMode(String value) {
		if ("FULLSCREEN".equals(value) || "BORDERLESS".equals(value))
			return value;
		return "WINDOWED";
	}

	private SessionData parseSession(Properties properties) {
		if (!Boolean.parseBoolean(properties.getProperty(KEY_HAS_SESSION, "false")))
			return null;

		SessionData data = new SessionData();
		data.levelIndex = parseInt(properties.getProperty(KEY_SESSION_LEVEL), 0);
		data.playerX = parseInt(properties.getProperty(KEY_PLAYER_X), 0);
		data.playerY = parseInt(properties.getProperty(KEY_PLAYER_Y), 0);
		data.respawnX = parseInt(properties.getProperty(KEY_RESPAWN_X), data.playerX);
		data.respawnY = parseInt(properties.getProperty(KEY_RESPAWN_Y), data.playerY);
		data.playerHealth = parseInt(properties.getProperty(KEY_PLAYER_HEALTH), 100);
		data.playerPower = parseInt(properties.getProperty(KEY_PLAYER_POWER), 200);
		data.inventoryCounts = parseInventory(properties.getProperty(KEY_INVENTORY));
		data.enemyActiveState = properties.getProperty(KEY_ENEMY_ACTIVE, "");
		data.enemyHealthState = properties.getProperty(KEY_ENEMY_HEALTH, "");
		data.potionActiveState = properties.getProperty(KEY_POTION_ACTIVE, "");
		data.containerActiveState = properties.getProperty(KEY_CONTAINER_ACTIVE, "");
		return data;
	}

	private void writeSession(Properties properties) {
		properties.setProperty(KEY_HAS_SESSION, Boolean.toString(session != null));
		if (session == null)
			return;

		properties.setProperty(KEY_SESSION_LEVEL, Integer.toString(session.levelIndex));
		properties.setProperty(KEY_PLAYER_X, Integer.toString(session.playerX));
		properties.setProperty(KEY_PLAYER_Y, Integer.toString(session.playerY));
		properties.setProperty(KEY_RESPAWN_X, Integer.toString(session.respawnX));
		properties.setProperty(KEY_RESPAWN_Y, Integer.toString(session.respawnY));
		properties.setProperty(KEY_PLAYER_HEALTH, Integer.toString(session.playerHealth));
		properties.setProperty(KEY_PLAYER_POWER, Integer.toString(session.playerPower));
		properties.setProperty(KEY_INVENTORY, toCsv(session.inventoryCounts));
		properties.setProperty(KEY_ENEMY_ACTIVE, session.enemyActiveState == null ? "" : session.enemyActiveState);
		properties.setProperty(KEY_ENEMY_HEALTH, session.enemyHealthState == null ? "" : session.enemyHealthState);
		properties.setProperty(KEY_POTION_ACTIVE, session.potionActiveState == null ? "" : session.potionActiveState);
		properties.setProperty(KEY_CONTAINER_ACTIVE, session.containerActiveState == null ? "" : session.containerActiveState);
	}

	private int[] parseInventory(String value) {
		int[] counts = new int[PlayerInventory.ITEM_AMOUNT];
		if (value == null || value.isBlank())
			return counts;

		String[] parts = value.split(",");
		for (int i = 0; i < counts.length && i < parts.length; i++)
			counts[i] = Math.max(0, parseInt(parts[i], 0));
		return counts;
	}

	private void migrateLegacyGold(boolean hasGoldBalance) {
		if (hasGoldBalance || session == null || session.inventoryCounts == null || session.inventoryCounts.length <= PlayerInventory.GOLD_COIN)
			return;

		goldBalance = Math.max(0, session.inventoryCounts[PlayerInventory.GOLD_COIN]);
	}

	private void sanitizeSessionCurrency() {
		if (session == null)
			return;
		sanitizeCurrencyCounts(session.inventoryCounts);
	}

	private void sanitizeCurrencyCounts(int[] counts) {
		if (counts == null)
			return;
		if (counts.length > PlayerInventory.GOLD_COIN)
			counts[PlayerInventory.GOLD_COIN] = 0;
		if (counts.length > PlayerInventory.SILVER_COIN)
			counts[PlayerInventory.SILVER_COIN] = 0;
	}

	private Set<PlayerSkin> parsePurchasedSkins(String value) {
		Set<PlayerSkin> skins = EnumSet.of(PlayerSkin.DEFAULT);
		if (value == null || value.isBlank())
			return skins;

		String[] parts = value.split(",");
		for (String part : parts) {
			try {
				PlayerSkin skin = PlayerSkin.valueOf(part.trim());
				if (skin.isPlayable())
					skins.add(skin);
			} catch (IllegalArgumentException e) {
				// Ignore broken save entries and keep safe defaults.
			}
		}
		return skins;
	}

	private String toPurchasedSkinsCsv() {
		StringBuilder builder = new StringBuilder();
		for (PlayerSkin skin : PlayerSkin.values()) {
			if (!skin.isPlayable() || !purchasedSkins.contains(skin))
				continue;
			if (builder.length() > 0)
				builder.append(',');
			builder.append(skin.name());
		}
		return builder.toString();
	}

	private boolean canUseSkin(PlayerSkin skin) {
		return skin != null && skin.isPlayable() && purchasedSkins.contains(skin);
	}

	private String toCsv(int[] values) {
		if (values == null || values.length == 0)
			return "";

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < values.length; i++) {
			if (i > 0)
				builder.append(',');
			builder.append(Math.max(0, values[i]));
		}
		return builder.toString();
	}

	private PlayerSkin parsePlayerSkin(String value) {
		try {
			PlayerSkin parsedSkin = value == null ? PlayerSkin.DEFAULT : PlayerSkin.valueOf(value);
			return parsedSkin.isPlayable() ? parsedSkin : PlayerSkin.DEFAULT;
		} catch (IllegalArgumentException e) {
			return PlayerSkin.DEFAULT;
		}
	}

	private int clampUnlockedLevels(int value, int totalLevels) {
		return Math.max(1, Math.min(totalLevels, value));
	}

	private int clampLevelIndex(int value, int totalLevels) {
		if (totalLevels <= 0)
			return 0;
		return Math.max(0, Math.min(totalLevels - 1, value));
	}

	public static class SessionData {
		public int levelIndex;
		public int playerX;
		public int playerY;
		public int respawnX;
		public int respawnY;
		public int playerHealth = 100;
		public int playerPower = 200;
		public int[] inventoryCounts = new int[PlayerInventory.ITEM_AMOUNT];
		public String enemyActiveState = "";
		public String enemyHealthState = "";
		public String potionActiveState = "";
		public String containerActiveState = "";

		public SessionData copy() {
			SessionData copy = new SessionData();
			copy.levelIndex = levelIndex;
			copy.playerX = playerX;
			copy.playerY = playerY;
			copy.respawnX = respawnX;
			copy.respawnY = respawnY;
			copy.playerHealth = playerHealth;
			copy.playerPower = playerPower;
			copy.inventoryCounts = inventoryCounts == null ? new int[PlayerInventory.ITEM_AMOUNT] : inventoryCounts.clone();
			copy.enemyActiveState = enemyActiveState;
			copy.enemyHealthState = enemyHealthState;
			copy.potionActiveState = potionActiveState;
			copy.containerActiveState = containerActiveState;
			return copy;
		}
	}
}
