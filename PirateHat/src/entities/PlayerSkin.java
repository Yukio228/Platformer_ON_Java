package entities;

import utilz.LoadSave;

public enum PlayerSkin {
	DEFAULT("DEFAULT", LoadSave.PLAYER_ATLAS, 0),
	KING("KING", LoadSave.PLAYER_SKIN_KING, 50),
	BOMB_GUY("BOMB GUY", LoadSave.PLAYER_SKIN_BOMB_GUY, 120),
	KING_PIG("KING PIG", LoadSave.PLAYER_SKIN_KING_PIG, 250);

	private final String displayName;
	private final String atlasFile;
	private final int cost;

	PlayerSkin(String displayName, String atlasFile, int cost) {
		this.displayName = displayName;
		this.atlasFile = atlasFile;
		this.cost = cost;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getAtlasFile() {
		return atlasFile;
	}

	public int getCost() {
		return cost;
	}

	public static PlayerSkin[] getWardrobeSkins() {
		return new PlayerSkin[] { DEFAULT, KING, BOMB_GUY };
	}

	public boolean isPlayable() {
		return this != KING_PIG;
	}
}
