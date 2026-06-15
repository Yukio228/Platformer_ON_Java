package gamestates;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import main.Game;
import ui.MenuButton;
import utilz.LoadSave;
import static utilz.Constants.UI.Buttons.B_HEIGHT;

public class Menu extends State implements Statemethods {

	private MenuButton[] buttons = new MenuButton[4];
	private BufferedImage backgroundImg, backgroundImgPink;
	private int menuX, menuY, menuWidth, menuHeight;

	public Menu(Game game) {
		super(game);
		loadBackground();
		loadButtons();
		backgroundImgPink = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);

	}

	private void loadBackground() {
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND);
		menuWidth = (int) (backgroundImg.getWidth() * Game.SCALE);
		menuHeight = (int) (backgroundImg.getHeight() * Game.SCALE);
		menuX = Game.GAME_WIDTH / 2 - menuWidth / 2;
		menuY = (int) (25 * Game.SCALE);
	}

	private void loadButtons() {
		int buttonX = menuX + menuWidth / 2;
		int contentTop = menuY + (int) (112 * Game.SCALE);
		int contentBottom = menuY + menuHeight - (int) (31 * Game.SCALE);
		int buttonGap = (contentBottom - contentTop - buttons.length * B_HEIGHT) / (buttons.length - 1);

		buttons[0] = new MenuButton(buttonX, contentTop, 0, Gamestate.PLAYING);
		buttons[1] = new MenuButton(buttonX, contentTop + (B_HEIGHT + buttonGap), 1, Gamestate.OPTIONS);
		buttons[2] = new MenuButton(buttonX, contentTop + (B_HEIGHT + buttonGap) * 2, 3, Gamestate.WARDROBE);
		buttons[3] = new MenuButton(buttonX, contentTop + (B_HEIGHT + buttonGap) * 3, 2, Gamestate.QUIT);
	}

	@Override
	public void update() {
		for (MenuButton mb : buttons)
			mb.update();
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImgPink, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
		g.drawImage(backgroundImg, menuX, menuY, menuWidth, menuHeight, null);

		for (MenuButton mb : buttons)
			mb.draw(g);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		for (MenuButton mb : buttons) {
			if (isIn(e, mb)) {
				mb.setMousePressed(true);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		for (MenuButton mb : buttons) {
			if (isIn(e, mb)) {
				if (mb.isMousePressed()) {
					if (mb.getState() == Gamestate.PLAYING) {
						game.getPlaying().loadSavedSession();
						setGamestate(Gamestate.PLAYING);
					} else
						mb.applyGamestate();
				}
				break;
			}
		}
		resetButtons();
	}

	private void resetButtons() {
		for (MenuButton mb : buttons)
			mb.resetBools();

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		for (MenuButton mb : buttons)
			mb.setMouseOver(false);

		for (MenuButton mb : buttons)
			if (isIn(e, mb)) {
				mb.setMouseOver(true);
				break;
			}

	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

}
