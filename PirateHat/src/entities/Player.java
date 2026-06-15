package entities;

import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.*;
import static utilz.Constants.*;
import static utilz.Constants.Directions.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import audio.AudioPlayer;
import ai.perception.NoiseType;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

public class Player extends Entity {

	private static final int BASIC_ATTACK_COOLDOWN_TICKS = 70;
	private static final int POWER_ATTACK_COOLDOWN_TICKS = 110;
	private static final int JUMP_COOLDOWN_TICKS = 40;

	private BufferedImage[][] animations;
	private boolean moving = false, attacking = false;
	private boolean left, right, jump;
	private int[][] lvlData;
	private float xDrawOffset = 21 * Game.SCALE;
	private float yDrawOffset = 4 * Game.SCALE;

	// Jumping / Gravity
	private float jumpSpeed = -2.25f * Game.SCALE;
	private float fallSpeedAfterCollision = 0.5f * Game.SCALE;

	// StatusBarUI
	private BufferedImage statusBarImg;

	private int statusBarWidth = (int) (192 * Game.SCALE);
	private int statusBarHeight = (int) (58 * Game.SCALE);
	private int statusBarX = (int) (10 * Game.SCALE);
	private int statusBarY = (int) (10 * Game.SCALE);

	private int healthBarWidth = (int) (150 * Game.SCALE);
	private int healthBarHeight = (int) (4 * Game.SCALE);
	private int healthBarXStart = (int) (34 * Game.SCALE);
	private int healthBarYStart = (int) (14 * Game.SCALE);
	private int healthWidth = healthBarWidth;

	private int powerBarWidth = (int) (104 * Game.SCALE);
	private int powerBarHeight = (int) (2 * Game.SCALE);
	private int powerBarXStart = (int) (44 * Game.SCALE);
	private int powerBarYStart = (int) (34 * Game.SCALE);
	private int powerWidth = powerBarWidth;
	private int powerMaxValue = 200;
	private int powerValue = powerMaxValue;

	private int flipX = 0;
	private int flipW = 1;

	private boolean attackChecked;
	private Playing playing;

	private int tileY = 0;

	private boolean powerAttackActive;
	private int powerAttackTick;
	private int powerGrowSpeed = 15;
	private int powerGrowTick;
	private PlayerInventory inventory = new PlayerInventory();
	private PlayerSkin skin = PlayerSkin.DEFAULT;
	private int movementNoiseCooldown;
	private int attackNoiseCooldown;
	private int attackCooldownTick;
	private int jumpCooldownTick;
	private boolean applyJumpCooldownOnLanding;
	private boolean deathRecorded;

	public Player(float x, float y, int width, int height, Playing playing) {
		super(x, y, width, height);
		this.playing = playing;
		this.state = IDLE;
		this.maxHealth = 100;
		this.currentHealth = maxHealth;
		this.walkSpeed = Game.SCALE * 1.0f;
		loadAnimations();
		initHitbox(20, 27);
		initAttackBox();
	}

	public void setSpawn(Point spawn) {
		this.x = spawn.x;
		this.y = spawn.y;
		hitbox.x = x;
		hitbox.y = y;
	}

	public void setRespawn(Point spawn) {
		this.x = spawn.x;
		this.y = spawn.y;
	}

	public Point getRespawnPoint() {
		return new Point((int) x, (int) y);
	}

	private void initAttackBox() {
		attackBox = new Rectangle2D.Float(x, y, (int) (35 * Game.SCALE), (int) (20 * Game.SCALE));
		resetAttackBox();
	}

	public void update() {
		updateHealthBar();
		updatePowerBar();
		if (movementNoiseCooldown > 0)
			movementNoiseCooldown--;
		if (attackNoiseCooldown > 0)
			attackNoiseCooldown--;
		if (attackCooldownTick > 0)
			attackCooldownTick--;
		if (jumpCooldownTick > 0)
			jumpCooldownTick--;

		if (currentHealth <= 0) {
			if (state != DEAD) {
				if (!deathRecorded) {
					playing.recordPlayerDeath();
					deathRecorded = true;
				}
				newState(DEAD);
				resetHitReaction();
				attacking = false;
				powerAttackActive = false;
				powerAttackTick = 0;
				playing.setPlayerDying(true);
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);

				if (!IsEntityOnFloor(hitbox, lvlData)) {
					inAir = true;
					airSpeed = Math.max(airSpeed, 0);
				} else {
					inAir = false;
					airSpeed = 0;
				}
			} else if (isDeathAnimationFinished()) {
				playing.setGameOver(true);
				playing.getGame().getAudioPlayer().stopSong();
				playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
			} else {
				updateAnimationTick();
				updateAirPosition(false);

			}

			return;
		}

		updateAttackBox();

		if (state == HIT) {
			if (aniIndex <= GetSpriteAmount(state) - 3)
				pushBack(pushBackDir, lvlData, 1.25f);
			updatePushBackDrawOffset();
			updateHitAirPosition();
		} else
			updatePos();

		if (moving) {
			checkPotionTouched();
			checkSpikesTouched();
			checkInsideWater();
			tileY = (int) (hitbox.y / Game.TILES_SIZE);
			emitMovementNoise();
			if (powerAttackActive) {
				powerAttackTick++;
				if (powerAttackTick >= 35) {
					powerAttackTick = 0;
					powerAttackActive = false;
				}
			}
		}

		if (attacking || powerAttackActive)
			checkAttack();

		updateAnimationTick();
		setAnimation();
	}

	private void checkInsideWater() {
		if (IsEntityInWater(hitbox, playing.getLevelManager().getCurrentLevel().getLevelData()))
			currentHealth = 0;
	}

	private void checkSpikesTouched() {
		playing.checkSpikesTouched(this);
	}

	private void checkPotionTouched() {
		playing.checkPotionTouched(hitbox);
	}

	private void checkAttack() {
		if (attackChecked || aniIndex != 1)
			return;
		attackChecked = true;

		if (powerAttackActive)
			attackChecked = false;

		boolean enemyHit = playing.checkEnemyHit(attackBox);
		playing.recordPlayerAttack(enemyHit);
		playing.checkObjectHit(attackBox);
		if (attackNoiseCooldown <= 0) {
			playing.emitPlayerNoise(powerAttackActive ? NoiseType.POWER_ATTACK : NoiseType.BASIC_ATTACK, getCenterX(), getCenterY());
			attackNoiseCooldown = powerAttackActive ? 24 : 34;
		}
		playing.getGame().getAudioPlayer().playAttackSound();
	}

	private void setAttackBoxOnRightSide() {
		attackBox.x = hitbox.x + hitbox.width - (int) (Game.SCALE * 5);
	}

	private void setAttackBoxOnLeftSide() {
		attackBox.x = hitbox.x - hitbox.width - (int) (Game.SCALE * 10);
	}

	private void updateAttackBox() {
		if (right && left) {
			if (flipW == 1) {
				setAttackBoxOnRightSide();
			} else {
				setAttackBoxOnLeftSide();
			}

		} else if (right || (powerAttackActive && flipW == 1))
			setAttackBoxOnRightSide();
		else if (left || (powerAttackActive && flipW == -1))
			setAttackBoxOnLeftSide();

		attackBox.y = hitbox.y + (Game.SCALE * 10);
	}

	private void updateHealthBar() {
		healthWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);
	}

	private void updatePowerBar() {
		powerWidth = (int) ((powerValue / (float) powerMaxValue) * powerBarWidth);

		powerGrowTick++;
		if (powerGrowTick >= powerGrowSpeed) {
			powerGrowTick = 0;
			changePower(1);
		}
	}

	public void render(Graphics g, int lvlOffset) {
		g.drawImage(animations[state][aniIndex], (int) (hitbox.x - xDrawOffset) - lvlOffset + flipX, (int) (hitbox.y - yDrawOffset + (int) (pushDrawOffset)), width * flipW, height, null);
//		drawHitbox(g, lvlOffset);
//		drawAttackBox(g, lvlOffset);
		drawUI(g);
	}

	private void drawUI(Graphics g) {
		// Background ui
		g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

		// Health bar
		g.setColor(Color.red);
		g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

		// Power Bar
		g.setColor(Color.yellow);
		g.fillRect(powerBarXStart + statusBarX, powerBarYStart + statusBarY, powerWidth, powerBarHeight);
	}

	private void updateAnimationTick() {
		aniTick++;
		if (aniTick >= getAnimationSpeed()) {
			aniTick = 0;
			aniIndex++;
			if (aniIndex >= GetSpriteAmount(state)) {
				aniIndex = 0;
				attacking = false;
				attackChecked = false;
				if (state == HIT) {
					newState(IDLE);
					resetHitReaction();
					airSpeed = 0f;
					if (IsEntityOnFloor(hitbox, lvlData))
						inAir = false;
					else
						inAir = true;
				}
			}
		}
	}

	private int getAnimationSpeed() {
		if (skin == PlayerSkin.DEFAULT)
			return ANI_SPEED;

		return switch (state) {
		case IDLE -> 12;
		case RUNNING -> 9;
		case JUMP, FALLING -> 14;
		case ATTACK -> 10;
		case HIT -> 10;
		case DEAD -> 16;
		default -> ANI_SPEED;
		};
	}

	private boolean isDeathAnimationFinished() {
		return aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= getAnimationSpeed() - 1;
	}

	private void setAnimation() {
		int startAni = state;

		if (state == HIT)
			return;

		if (moving)
			state = RUNNING;
		else
			state = IDLE;

		if (inAir) {
			if (airSpeed < 0)
				state = JUMP;
			else
				state = FALLING;
		}

		if (powerAttackActive) {
			state = ATTACK;
			aniIndex = 1;
			aniTick = 0;
			return;
		}

		if (attacking) {
			state = ATTACK;
			if (startAni != ATTACK) {
				aniIndex = 1;
				aniTick = 0;
				return;
			}
		}
		if (startAni != state)
			resetAniTick();
	}

	private void resetAniTick() {
		aniTick = 0;
		aniIndex = 0;
	}

	private void updatePos() {
		moving = false;

		if (jump)
			jump();

		if (!inAir)
			if (!powerAttackActive)
				if ((!left && !right) || (right && left))
					return;

		float xSpeed = 0;

		if (left && !right) {
			xSpeed -= walkSpeed;
			flipX = width;
			flipW = -1;
		}
		if (right && !left) {
			xSpeed += walkSpeed;
			flipX = 0;
			flipW = 1;
		}

		if (powerAttackActive) {
			if ((!left && !right) || (left && right)) {
				if (flipW == -1)
					xSpeed = -walkSpeed;
				else
					xSpeed = walkSpeed;
			}

			xSpeed *= 3;
		}

		if (!inAir)
			if (!IsEntityOnFloor(hitbox, lvlData))
				inAir = true;

		if (inAir && !powerAttackActive) {
			if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
				hitbox.y += airSpeed;
				airSpeed += GRAVITY;
				updateXPos(xSpeed);
			} else {
				hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
				if (airSpeed > 0)
					resetInAir();
				else
					airSpeed = fallSpeedAfterCollision;
				updateXPos(xSpeed);
			}

		} else
			updateXPos(xSpeed);
		moving = true;
	}

	private void jump() {
		if (inAir || jumpCooldownTick > 0)
			return;
		playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
		playing.emitPlayerNoise(NoiseType.JUMP, getCenterX(), getCenterY());
		inAir = true;
		airSpeed = jumpSpeed;
		applyJumpCooldownOnLanding = true;
		playing.recordTutorialJump();
	}

	private void resetInAir() {
		land(true);
	}

	private void land(boolean emitNoise) {
		inAir = false;
		airSpeed = 0;
		if (applyJumpCooldownOnLanding) {
			jumpCooldownTick = JUMP_COOLDOWN_TICKS;
			applyJumpCooldownOnLanding = false;
		}
		if (emitNoise)
			playing.emitPlayerNoise(NoiseType.LANDING, getCenterX(), getCenterY());
	}

	private void updateHitAirPosition() {
		if (!inAir && !IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
		updateAirPosition(true);
	}

	private void updateAirPosition(boolean emitLandingNoise) {
		if (!inAir)
			return;

		if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
			hitbox.y += airSpeed;
			airSpeed += GRAVITY;
		} else {
			hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
			if (airSpeed > 0)
				land(emitLandingNoise);
			else
				airSpeed = fallSpeedAfterCollision;
		}
	}

	private void resetHitReaction() {
		pushDrawOffset = 0;
		pushBackOffsetDir = UP;
	}

	private void updateXPos(float xSpeed) {
		if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
			hitbox.x += xSpeed;
		else {
			hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
			if (powerAttackActive) {
				powerAttackActive = false;
				powerAttackTick = 0;
			}
		}
	}

	public void changeHealth(int value) {
		if (value < 0) {
			if (state == HIT)
				return;
			else {
				newState(HIT);
				playing.recordPlayerHitTaken();
				playing.emitPlayerNoise(NoiseType.PLAYER_HURT, getCenterX(), getCenterY());
			}
		}

		currentHealth += value;
		currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
	}

	public void changeHealth(int value, Enemy e) {
		if (state == HIT)
			return;
		changeHealth(value);
		pushBackOffsetDir = UP;
		pushDrawOffset = 0;

		if (e.getHitbox().x < hitbox.x)
			pushBackDir = RIGHT;
		else
			pushBackDir = LEFT;
	}

	public void kill() {
		currentHealth = 0;
	}

	public void changePower(int value) {
		powerValue += value;
		powerValue = Math.max(Math.min(powerValue, powerMaxValue), 0);
	}

	public int getCurrentHealth() {
		return currentHealth;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public int getPowerValue() {
		return powerValue;
	}

	public int getWorldX() {
		return (int) hitbox.x;
	}

	public int getWorldY() {
		return (int) hitbox.y;
	}

	public void restoreSession(int playerX, int playerY, int respawnX, int respawnY, int health, int power, int[] inventoryCounts) {
		resetDirBooleans();
		inAir = false;
		attacking = false;
		moving = false;
		airSpeed = 0f;
		state = IDLE;
		deathRecorded = false;
		powerAttackActive = false;
		powerAttackTick = 0;
		movementNoiseCooldown = 0;
		attackNoiseCooldown = 0;
		attackCooldownTick = 0;
		jumpCooldownTick = 0;
		applyJumpCooldownOnLanding = false;

		x = respawnX;
		y = respawnY;
		hitbox.x = playerX;
		hitbox.y = playerY;
		currentHealth = Math.max(1, Math.min(maxHealth, health));
		powerValue = Math.max(0, Math.min(powerMaxValue, power));
		inventory.setCounts(inventoryCounts);
		resetAttackBox();

		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	private void loadAnimations() {
		BufferedImage img = LoadSave.GetSpriteAtlas(skin.getAtlasFile());
		animations = new BufferedImage[7][8];
		for (int j = 0; j < animations.length; j++)
			for (int i = 0; i < animations[j].length; i++)
				animations[j][i] = img.getSubimage(i * 64, j * 40, 64, 40);

		statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
	}

	public void loadLvlData(int[][] lvlData) {
		this.lvlData = lvlData;
		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	public void resetDirBooleans() {
		left = false;
		right = false;
	}

	public void setAttacking(boolean attacking) {
		if (!attacking) {
			this.attacking = false;
			return;
		}

		if (this.attacking || powerAttackActive || attackCooldownTick > 0)
			return;

		this.attacking = true;
		attackChecked = false;
		attackCooldownTick = BASIC_ATTACK_COOLDOWN_TICKS;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public boolean isRight() {
		return right;
	}

	public void setRight(boolean right) {
		this.right = right;
	}

	public void setJump(boolean jump) {
		this.jump = jump;
	}

	public void resetAll() {
		resetDirBooleans();
		inAir = false;
		attacking = false;
		moving = false;
		airSpeed = 0f;
		state = IDLE;
		currentHealth = maxHealth;
		deathRecorded = false;
		powerAttackActive = false;
		powerAttackTick = 0;
		powerValue = powerMaxValue;
		movementNoiseCooldown = 0;
		attackNoiseCooldown = 0;
		attackCooldownTick = 0;
		jumpCooldownTick = 0;
		applyJumpCooldownOnLanding = false;
		resetHitReaction();

		hitbox.x = x;
		hitbox.y = y;
		resetAttackBox();

		if (!IsEntityOnFloor(hitbox, lvlData))
			inAir = true;
	}

	private void resetAttackBox() {
		if (flipW == 1)
			setAttackBoxOnRightSide();
		else
			setAttackBoxOnLeftSide();
	}

	public int getTileY() {
		return tileY;
	}

	public boolean powerAttack() {
		if (powerAttackActive || attacking || attackCooldownTick > 0)
			return false;
		if (powerValue >= 60) {
			powerAttackActive = true;
			attackChecked = false;
			attackCooldownTick = POWER_ATTACK_COOLDOWN_TICKS;
			changePower(-60);
			playing.emitPlayerNoise(NoiseType.POWER_ATTACK, getCenterX(), getCenterY());
			return true;
		}

		return false;
	}

	private void emitMovementNoise() {
		if (movementNoiseCooldown > 0 || inAir)
			return;
		playing.emitPlayerNoise(powerAttackActive ? NoiseType.RUN : NoiseType.WALK, getCenterX(), getCenterY());
		movementNoiseCooldown = powerAttackActive ? 16 : 28;
	}

	private float getCenterX() {
		return hitbox.x + hitbox.width / 2f;
	}

	private float getCenterY() {
		return hitbox.y + hitbox.height / 2f;
	}

	public PlayerInventory getInventory() {
		return inventory;
	}

	public PlayerSkin getSkin() {
		return skin;
	}

	public void setSkin(PlayerSkin skin) {
		if (this.skin == skin)
			return;

		this.skin = skin;
		aniTick = 0;
		aniIndex = 0;
		loadAnimations();
	}

}
