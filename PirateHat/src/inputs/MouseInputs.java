package inputs;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import gamestates.Gamestate;
import main.GamePanel;

public class MouseInputs implements MouseListener, MouseMotionListener {

	private GamePanel gamePanel;

	public MouseInputs(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void mouseDragged(MouseEvent e) {
		e = gamePanel.toGameMouseEvent(e);
		switch (Gamestate.state) {
		case PLAYING -> gamePanel.getGame().getPlaying().mouseDragged(e);
		case OPTIONS -> gamePanel.getGame().getGameOptions().mouseDragged(e);
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void mouseMoved(MouseEvent e) {
		e = gamePanel.toGameMouseEvent(e);
		switch (Gamestate.state) {
		case MENU -> gamePanel.getGame().getMenu().mouseMoved(e);
		case WARDROBE -> gamePanel.getGame().getWardrobe().mouseMoved(e);
		case PLAYING -> gamePanel.getGame().getPlaying().mouseMoved(e);
		case OPTIONS -> gamePanel.getGame().getGameOptions().mouseMoved(e);
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void mouseClicked(MouseEvent e) {
		e = gamePanel.toGameMouseEvent(e);
		switch (Gamestate.state) {
		case WARDROBE -> gamePanel.getGame().getWardrobe().mouseClicked(e);
		case PLAYING -> gamePanel.getGame().getPlaying().mouseClicked(e);
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void mousePressed(MouseEvent e) {
		gamePanel.requestFocusInWindow();
		e = gamePanel.toGameMouseEvent(e);

		switch (Gamestate.state) {
		case MENU -> gamePanel.getGame().getMenu().mousePressed(e);
		case WARDROBE -> gamePanel.getGame().getWardrobe().mousePressed(e);
		case PLAYING -> gamePanel.getGame().getPlaying().mousePressed(e);
		case OPTIONS -> gamePanel.getGame().getGameOptions().mousePressed(e);
		}
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void mouseReleased(MouseEvent e) {
		e = gamePanel.toGameMouseEvent(e);
		switch (Gamestate.state) {
		case MENU -> gamePanel.getGame().getMenu().mouseReleased(e);
		case WARDROBE -> gamePanel.getGame().getWardrobe().mouseReleased(e);
		case PLAYING -> gamePanel.getGame().getPlaying().mouseReleased(e);
		case OPTIONS -> gamePanel.getGame().getGameOptions().mouseReleased(e);
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Not In use
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Not In use
	}

}
