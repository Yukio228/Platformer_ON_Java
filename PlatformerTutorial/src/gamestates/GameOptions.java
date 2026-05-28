package gamestates;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import main.Game;
import main.GameWindow.ScreenMode;
import ui.AssetText;
import ui.AudioOptions;
import ui.PauseButton;
import ui.UrmButton;
import utilz.LoadSave;
import static utilz.Constants.UI.URMButtons.*;

public class GameOptions extends State implements Statemethods {

	private AudioOptions audioOptions;
	private BufferedImage backgroundImg, optionsBackgroundImg;
	private int bgX, bgY, bgW, bgH;
	private UrmButton menuB;
	private Rectangle screenSelectBounds, resolutionSelectBounds;
	private final ScreenMode[] screenModes = ScreenMode.values();
	private Rectangle[] screenModeOptionBounds = new Rectangle[screenModes.length];
	private Rectangle[] resolutionOptionBounds = new Rectangle[3];
	private boolean screenSelectMouseOver, screenSelectMousePressed, resolutionSelectMouseOver, resolutionSelectMousePressed;
	private boolean screenDropdownOpen, resolutionDropdownOpen;
	private boolean[] screenModeMouseOver = new boolean[screenModes.length];
	private boolean[] screenModeMousePressed = new boolean[screenModes.length];
	private boolean[] resolutionMouseOver = new boolean[3];
	private boolean[] resolutionMousePressed = new boolean[3];
	private static final Color UI_DARK = new Color(51, 50, 61);
	private static final Color UI_PAPER_LINE = new Color(219, 134, 96);

	public GameOptions(Game game) {
		super(game);
		loadImgs();
		loadButton();
		audioOptions = game.getAudioOptions();
	}

	private void loadButton() {
		int leftX = bgX + (int) (62 * Game.SCALE);
		int selectorX = leftX;
		int selectorW = (int) (142 * Game.SCALE);
		int selectorH = (int) (20 * Game.SCALE);
		int optionH = (int) (13 * Game.SCALE);
		int menuX = bgX + (int) (207 * Game.SCALE);
		int menuY = bgY + (int) (322 * Game.SCALE);

		menuB = new UrmButton(menuX, menuY, URM_SIZE, URM_SIZE, 2);
		screenSelectBounds = new Rectangle(selectorX, bgY + (int) (304 * Game.SCALE), selectorW, selectorH);
		resolutionSelectBounds = new Rectangle(selectorX, bgY + (int) (340 * Game.SCALE), selectorW, selectorH);

		for (int i = 0; i < screenModeOptionBounds.length; i++)
			screenModeOptionBounds[i] = new Rectangle(screenSelectBounds.x, screenSelectBounds.y + screenSelectBounds.height + i * optionH, selectorW, optionH);

		int resolutionDropdownY = resolutionSelectBounds.y - resolutionOptionBounds.length * optionH;
		for (int i = 0; i < resolutionOptionBounds.length; i++)
			resolutionOptionBounds[i] = new Rectangle(resolutionSelectBounds.x, resolutionDropdownY + i * optionH, selectorW, optionH);
	}

	private void loadImgs() {
		backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
		optionsBackgroundImg = LoadSave.GetSpriteAtlas(LoadSave.OPTIONS_MENU);

		bgW = (int) (optionsBackgroundImg.getWidth() * Game.SCALE);
		bgH = (int) (optionsBackgroundImg.getHeight() * Game.SCALE);
		bgX = Game.GAME_WIDTH / 2 - bgW / 2;
		bgY = (int) (24 * Game.SCALE);
	}

	@Override
	public void update() {
		menuB.update();
		audioOptions.update();
	}

	@Override
	public void draw(Graphics g) {
		g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
		g.drawImage(optionsBackgroundImg, bgX, bgY, bgW, bgH, null);

		menuB.draw(g);
		audioOptions.draw(g);
		drawDisplayOptions(g);
	}

	private void drawDisplayOptions(Graphics g) {
		if (g instanceof Graphics2D g2)
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

		if (!resolutionDropdownOpen) {
			AssetText.draw(g, "SCREEN", bgX + (int) (62 * Game.SCALE), bgY + (int) (291 * Game.SCALE), 2);
			drawScreenSelector(g);
		}

		if (resolutionDropdownOpen)
			drawResolutionList(g);

		if (!screenDropdownOpen) {
			if (!resolutionDropdownOpen)
				AssetText.draw(g, "RESOLUTION", bgX + (int) (62 * Game.SCALE), bgY + (int) (327 * Game.SCALE), 2);
			drawResolutionSelector(g);
		}

		if (screenDropdownOpen)
			drawScreenModeList(g);
	}

	private void drawScreenSelector(Graphics g) {
		drawSelectorButton(g, screenSelectBounds, game.getGameWindow().getScreenModeLabel(), screenDropdownOpen, screenSelectMouseOver || screenSelectMousePressed);
	}

	private void drawResolutionSelector(Graphics g) {
		drawSelectorButton(g, resolutionSelectBounds, game.getGameWindow().getResolutionLabel(), resolutionDropdownOpen, resolutionSelectMouseOver || resolutionSelectMousePressed);
	}

	private void drawResolutionList(Graphics g) {
		Rectangle first = resolutionOptionBounds[0];
		Rectangle last = resolutionOptionBounds[resolutionOptionBounds.length - 1];
		int s = (int) Game.SCALE;
		Rectangle list = new Rectangle(first.x - 4 * s, first.y - 3 * s, first.width + 8 * s, last.y + last.height - first.y + 6 * s);

		drawPixelPanel(g, list, false);

		for (int i = 0; i < resolutionOptionBounds.length; i++)
			drawResolutionListOption(g, i);
	}

	private void drawScreenModeList(Graphics g) {
		Rectangle first = screenModeOptionBounds[0];
		Rectangle last = screenModeOptionBounds[screenModeOptionBounds.length - 1];
		int s = (int) Game.SCALE;
		Rectangle list = new Rectangle(first.x - 4 * s, first.y - 3 * s, first.width + 8 * s, last.y + last.height - first.y + 6 * s);

		drawPixelPanel(g, list, false);

		for (int i = 0; i < screenModeOptionBounds.length; i++)
			drawScreenModeOption(g, i);
	}

	private void drawScreenModeOption(Graphics g, int index) {
		Rectangle row = screenModeOptionBounds[index];
		int s = (int) Game.SCALE;
		ScreenMode mode = screenModes[index];
		boolean selected = game.getGameWindow().getScreenMode() == mode;
		boolean hover = screenModeMouseOver[index] || screenModeMousePressed[index];

		if (selected) {
			drawPixelPanel(g, row.x - 2 * s, row.y, row.width + 4 * s, row.height, true);
			drawSelectorArrow(g, row.x + 6 * s, row.y + 4 * s, s);
		} else if (hover) {
			g.setColor(new Color(231, 159, 103));
			g.fillRect(row.x, row.y + 2 * s, row.width, row.height - 4 * s);
		}

		if (index > 0) {
			g.setColor(selected ? UI_DARK : UI_PAPER_LINE);
			g.fillRect(row.x + 5 * s, row.y - s, row.width - 10 * s, s);
		}

		AssetText.draw(g, mode.getLabel(), row.x + 18 * s, row.y + 2 * s, 2);
	}

	private void drawResolutionListOption(Graphics g, int index) {
		Rectangle row = resolutionOptionBounds[index];
		int s = (int) Game.SCALE;
		boolean selected = game.getGameWindow().getResolutionIndex() == index;
		boolean hover = resolutionMouseOver[index] || resolutionMousePressed[index];

		if (selected) {
			drawPixelPanel(g, row.x - 2 * s, row.y, row.width + 4 * s, row.height, true);
			drawSelectorArrow(g, row.x + 6 * s, row.y + 5 * s, s);
		} else if (hover) {
			g.setColor(new Color(231, 159, 103));
			g.fillRect(row.x, row.y + 2 * s, row.width, row.height - 4 * s);
		}

		if (index > 0) {
			g.setColor(selected ? UI_DARK : UI_PAPER_LINE);
			g.fillRect(row.x + 5 * s, row.y - s, row.width - 10 * s, s);
		}

		String text = game.getGameWindow().getResolutionLabel(index);
		AssetText.draw(g, text, row.x + 18 * s, row.y + 2 * s, 2);
	}

	private void drawSelectorButton(Graphics g, Rectangle button, String text, boolean open, boolean hover) {
		int s = (int) Game.SCALE;
		drawPixelPanel(g, button, hover || open);
		AssetText.draw(g, text, button.x + 8 * s, button.y + 6 * s, 2);
		drawDropdownArrow(g, button.x + button.width - 14 * s, button.y + 9 * s, s, open);
	}

	private void drawDropdownArrow(Graphics g, int x, int y, int s, boolean open) {
		g.setColor(UI_DARK);
		if (open) {
			g.fillRect(x + 2 * s, y, 2 * s, 2 * s);
			g.fillRect(x, y + 2 * s, 6 * s, 2 * s);
		} else {
			g.fillRect(x, y, 6 * s, 2 * s);
			g.fillRect(x + 2 * s, y + 2 * s, 2 * s, 2 * s);
		}
	}

	private void drawPixelPanel(Graphics g, Rectangle bounds, boolean green) {
		drawPixelPanel(g, bounds.x, bounds.y, bounds.width, bounds.height, green);
	}

	private void drawPixelPanel(Graphics g, int x, int y, int w, int h, boolean green) {
		int s = (int) Game.SCALE;
		Color edge = green ? new Color(45, 91, 64) : new Color(103, 57, 65);
		Color fill = green ? new Color(87, 154, 100) : new Color(224, 171, 103);
		Color highlight = green ? new Color(137, 191, 122) : new Color(246, 205, 137);
		Color shade = green ? new Color(47, 100, 76) : new Color(181, 90, 84);

		g.setColor(UI_DARK);
		g.fillRect(x, y, w, h);
		g.setColor(edge);
		g.fillRect(x + s, y + s, w - 2 * s, h - 2 * s);
		g.setColor(fill);
		g.fillRect(x + 2 * s, y + 2 * s, w - 4 * s, h - 4 * s);
		g.setColor(highlight);
		g.fillRect(x + 5 * s, y + 3 * s, w - 10 * s, 2 * s);
		g.setColor(shade);
		g.fillRect(x + 4 * s, y + h - 5 * s, w - 8 * s, 2 * s);
	}

	private void drawSelectorArrow(Graphics g, int x, int y, int s) {
		g.setColor(UI_DARK);
		g.fillRect(x, y, 2 * s, 2 * s);
		g.fillRect(x + 2 * s, y + 2 * s, 2 * s, 2 * s);
		g.fillRect(x, y + 4 * s, 2 * s, 2 * s);
	}

	public void mouseDragged(MouseEvent e) {
		audioOptions.mouseDragged(e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (screenDropdownOpen && pressScreenModeButton(e))
			return;
		else if (resolutionDropdownOpen && pressResolutionButton(e))
			return;
		else if (screenSelectBounds.contains(e.getPoint()))
			screenSelectMousePressed = true;
		else if (resolutionSelectBounds.contains(e.getPoint()))
			resolutionSelectMousePressed = true;
		else if (isIn(e, menuB)) {
			menuB.setMousePressed(true);
		} else
			audioOptions.mousePressed(e);
	}

	private boolean pressResolutionButton(MouseEvent e) {
		for (int i = 0; i < resolutionOptionBounds.length; i++)
			if (resolutionOptionBounds[i].contains(e.getPoint())) {
				resolutionMousePressed[i] = true;
				return true;
			}
		return false;
	}

	private boolean pressScreenModeButton(MouseEvent e) {
		for (int i = 0; i < screenModeOptionBounds.length; i++)
			if (screenModeOptionBounds[i].contains(e.getPoint())) {
				screenModeMousePressed[i] = true;
				return true;
			}
		return false;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (screenSelectMousePressed && screenSelectBounds.contains(e.getPoint())) {
			screenDropdownOpen = !screenDropdownOpen;
			resolutionDropdownOpen = false;
		} else if (resolutionSelectMousePressed && resolutionSelectBounds.contains(e.getPoint())) {
			resolutionDropdownOpen = !resolutionDropdownOpen;
			screenDropdownOpen = false;
		} else if (releaseScreenModeButton(e))
			return;
		else if (releaseResolutionButton(e))
			return;
		else if (isIn(e, menuB)) {
			if (menuB.isMousePressed())
				Gamestate.state = Gamestate.MENU;
		} else
			audioOptions.mouseReleased(e);

		if (!screenSelectBounds.contains(e.getPoint()) && !resolutionSelectBounds.contains(e.getPoint())) {
			screenDropdownOpen = false;
			resolutionDropdownOpen = false;
		}
		resetDisplayButtons();
	}

	private boolean releaseScreenModeButton(MouseEvent e) {
		for (int i = 0; i < screenModeOptionBounds.length; i++)
			if (screenModeMousePressed[i] && screenModeOptionBounds[i].contains(e.getPoint())) {
				game.getGameWindow().setScreenMode(screenModes[i]);
				screenDropdownOpen = false;
				resetDisplayButtons();
				return true;
			}
		return false;
	}

	private boolean releaseResolutionButton(MouseEvent e) {
		for (int i = 0; i < resolutionOptionBounds.length; i++)
			if (resolutionMousePressed[i] && resolutionOptionBounds[i].contains(e.getPoint())) {
				game.getGameWindow().setResolutionIndex(i);
				resolutionDropdownOpen = false;
				resetDisplayButtons();
				return true;
			}
		return false;
	}

	private void resetDisplayButtons() {
		menuB.resetBools();
		screenSelectMousePressed = false;
		resolutionSelectMousePressed = false;
		for (int i = 0; i < screenModeMousePressed.length; i++)
			screenModeMousePressed[i] = false;
		for (int i = 0; i < resolutionMousePressed.length; i++)
			resolutionMousePressed[i] = false;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		menuB.setMouseOver(false);
		screenSelectMouseOver = false;
		resolutionSelectMouseOver = false;
		for (int i = 0; i < screenModeMouseOver.length; i++)
			screenModeMouseOver[i] = false;
		for (int i = 0; i < resolutionMouseOver.length; i++)
			resolutionMouseOver[i] = false;
		audioOptions.mouseMoved(e);

		if (screenSelectBounds.contains(e.getPoint()))
			screenSelectMouseOver = true;
		else if (resolutionSelectBounds.contains(e.getPoint()))
			resolutionSelectMouseOver = true;
		else if (screenDropdownOpen && updateScreenModeMouseOver(e))
			return;
		else if (resolutionDropdownOpen && updateResolutionMouseOver(e))
			return;
		else if (isIn(e, menuB))
			menuB.setMouseOver(true);
	}

	private boolean updateResolutionMouseOver(MouseEvent e) {
		for (int i = 0; i < resolutionOptionBounds.length; i++)
			if (resolutionOptionBounds[i].contains(e.getPoint())) {
				resolutionMouseOver[i] = true;
				return true;
			}
		return false;
	}

	private boolean updateScreenModeMouseOver(MouseEvent e) {
		for (int i = 0; i < screenModeOptionBounds.length; i++)
			if (screenModeOptionBounds[i].contains(e.getPoint())) {
				screenModeMouseOver[i] = true;
				return true;
			}
		return false;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			Gamestate.state = Gamestate.MENU;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	private boolean isIn(MouseEvent e, PauseButton b) {
		return b.getBounds().contains(e.getX(), e.getY());
	}

}
