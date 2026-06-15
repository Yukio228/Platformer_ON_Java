package entities;

import static utilz.Constants.ObjectConstants.BLUE_POTION_VALUE;
import static utilz.Constants.ObjectConstants.RED_POTION_VALUE;

public class PlayerInventory {

	public static final int RED_POTION = 0;
	public static final int BLUE_POTION = 1;
	public static final int GOLD_COIN = 2;
	public static final int SILVER_COIN = 3;
	public static final int KEY = 4;
	public static final int ITEM_AMOUNT = 5;
	public static final int GOLD_COIN_VALUE = 10;

	private int[] itemCounts = new int[ITEM_AMOUNT];

	public void addItem(int itemType, int amount) {
		if (itemType < 0 || itemType >= itemCounts.length || amount <= 0)
			return;

		long newCount = (long) itemCounts[itemType] + amount;
		itemCounts[itemType] = (int) Math.min(Integer.MAX_VALUE, newCount);
	}

	public int getCount(int itemType) {
		if (itemType < 0 || itemType >= itemCounts.length)
			return 0;

		return itemCounts[itemType];
	}

	public int[] getCountsCopy() {
		return itemCounts.clone();
	}

	public void setCounts(int[] counts) {
		for (int i = 0; i < itemCounts.length; i++)
			itemCounts[i] = counts != null && i < counts.length ? Math.max(0, counts[i]) : 0;
	}

	public boolean useRedPotion(Player player) {
		if (itemCounts[RED_POTION] <= 0)
			return false;

		itemCounts[RED_POTION]--;
		player.changeHealth(RED_POTION_VALUE);
		return true;
	}

	public boolean useBluePotion(Player player) {
		if (itemCounts[BLUE_POTION] <= 0)
			return false;

		itemCounts[BLUE_POTION]--;
		player.changePower(BLUE_POTION_VALUE);
		return true;
	}
}
