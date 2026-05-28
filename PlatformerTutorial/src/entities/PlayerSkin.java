package entities;

import utilz.LoadSave;

public enum PlayerSkin {
	DEFAULT("DEFAULT", LoadSave.PLAYER_ATLAS),
	KING("KING", LoadSave.PLAYER_SKIN_KING),
	BOMB_GUY("BOMB GUY", LoadSave.PLAYER_SKIN_BOMB_GUY),
	KING_PIG("KING PIG", LoadSave.PLAYER_SKIN_KING_PIG);

	private final String displayName;
	private final String atlasFile;

	PlayerSkin(String displayName, String atlasFile) {
		this.displayName = displayName;
		this.atlasFile = atlasFile;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getAtlasFile() {
		return atlasFile;
	}
}
