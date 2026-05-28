package objects;

import static utilz.Constants.ObjectConstants.CHECKPOINT;
import static utilz.Constants.ObjectConstants.CHECKPOINT_HEIGHT;
import static utilz.Constants.ObjectConstants.CHECKPOINT_WIDTH;
import static utilz.Constants.ObjectConstants.CHECKPOINT_WIDTH_DEFAULT;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import main.Game;
import utilz.LoadSave;

public class Checkpoint extends GameObject {

	private static final int FRAME_AMOUNT = 4;
	private static final int INACTIVE_FRAME = 3;
	private static final int ACTIVE_FRAME = 2;
	private static final int CAPTURE_ANI_SPEED = 18;
	private static final int[] CAPTURE_FRAMES = { 3, 0, 1, 2 };
	private static BufferedImage[] checkpointImgs;
	private boolean activated;
	private boolean captureAnimation;

	public Checkpoint(int x, int y) {
		super(x, y, CHECKPOINT);
		initHitbox(CHECKPOINT_WIDTH_DEFAULT, 32);
		loadCheckpointImg();
	}

	public void activate() {
		if (activated)
			return;

		activated = true;
		captureAnimation = true;
		aniTick = 0;
		aniIndex = 0;
	}

	public boolean isActivated() {
		return activated;
	}

	public Point getRespawnPoint() {
		return new Point(x, y);
	}

	public void draw(Graphics g, int xLvlOffset) {
		int drawX = x - xLvlOffset;
		int drawY = y + Game.TILES_SIZE - CHECKPOINT_HEIGHT;
		g.drawImage(checkpointImgs[getDrawFrame()], drawX, drawY, CHECKPOINT_WIDTH, CHECKPOINT_HEIGHT, null);
	}

	public void update() {
		if (!captureAnimation)
			return;

		aniTick++;
		if (aniTick >= CAPTURE_ANI_SPEED) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= CAPTURE_FRAMES.length) {
				aniIndex = CAPTURE_FRAMES.length - 1;
				captureAnimation = false;
			}
		}
	}

	private void loadCheckpointImg() {
		if (checkpointImgs != null)
			return;

		BufferedImage atlas = LoadSave.GetSpriteAtlas(LoadSave.CHECKPOINT);
		int frameWidth = atlas.getWidth() / FRAME_AMOUNT;
		checkpointImgs = new BufferedImage[FRAME_AMOUNT];
		for (int i = 0; i < checkpointImgs.length; i++)
			checkpointImgs[i] = atlas.getSubimage(i * frameWidth, 0, frameWidth, atlas.getHeight());
	}

	private int getDrawFrame() {
		if (!activated)
			return INACTIVE_FRAME;
		if (!captureAnimation)
			return ACTIVE_FRAME;
		return CAPTURE_FRAMES[aniIndex];
	}

	@Override
	public void reset() {
		super.reset();
		activated = false;
		captureAnimation = false;
	}
}
