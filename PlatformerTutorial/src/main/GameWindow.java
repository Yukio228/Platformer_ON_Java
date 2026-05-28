package main;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.JFrame;

public class GameWindow {

	public enum ScreenMode {
		WINDOWED("WINDOWED"),
		FULLSCREEN("FULLSCREEN"),
		BORDERLESS("BORDERLESS");

		private final String label;

		ScreenMode(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	private static final int[][] WINDOW_RESOLUTIONS = { { 1920, 1080 }, { 1280, 720 }, { 640, 480 } };

	private JFrame jframe;
	private GamePanel gamePanel;
	private GraphicsDevice graphicsDevice;
	private int resolutionIndex = 1;
	private ScreenMode screenMode = ScreenMode.WINDOWED;

	public GameWindow(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
		graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

		jframe = new JFrame();

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.add(gamePanel);
		
		jframe.setResizable(false);
		int[] resolution = WINDOW_RESOLUTIONS[resolutionIndex];
		gamePanel.setRenderSize(resolution[0], resolution[1]);
		jframe.pack();
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true);
		jframe.addWindowFocusListener(new WindowFocusListener() {

			@Override
			public void windowLostFocus(WindowEvent e) {
				gamePanel.getGame().windowFocusLost();
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
				gamePanel.requestFocusInWindow();

			}
		});

	}

	public void setResolutionIndex(int resolutionIndex) {
		this.resolutionIndex = resolutionIndex;
		applyDisplaySettings();
	}

	public void setScreenMode(ScreenMode screenMode) {
		this.screenMode = screenMode;
		applyDisplaySettings();
	}

	public String getResolutionLabel() {
		int[] resolution = WINDOW_RESOLUTIONS[resolutionIndex];
		return resolution[0] + "x" + resolution[1];
	}

	public String getResolutionLabel(int index) {
		int[] resolution = WINDOW_RESOLUTIONS[index];
		return resolution[0] + "x" + resolution[1];
	}

	public int getResolutionIndex() {
		return resolutionIndex;
	}

	public ScreenMode getScreenMode() {
		return screenMode;
	}

	public int getResolutionAmount() {
		return WINDOW_RESOLUTIONS.length;
	}

	public String getScreenModeLabel() {
		return screenMode.getLabel();
	}

	private void applyDisplaySettings() {
		if (graphicsDevice.getFullScreenWindow() == jframe)
			graphicsDevice.setFullScreenWindow(null);

		jframe.dispose();

		switch (screenMode) {
		case WINDOWED -> applyWindowedMode();
		case FULLSCREEN -> applyFullscreenMode();
		case BORDERLESS -> applyBorderlessMode();
		}

		gamePanel.requestFocusInWindow();
	}

	private void applyWindowedMode() {
		int[] resolution = WINDOW_RESOLUTIONS[resolutionIndex];
		jframe.setUndecorated(false);
		jframe.setResizable(false);
		gamePanel.setRenderSize(resolution[0], resolution[1]);
		jframe.pack();
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true);
	}

	private void applyFullscreenMode() {
		Rectangle bounds = graphicsDevice.getDefaultConfiguration().getBounds();
		jframe.setUndecorated(true);
		jframe.setResizable(false);
		gamePanel.setRenderSize(bounds.width, bounds.height);
		jframe.setVisible(true);
		graphicsDevice.setFullScreenWindow(jframe);
	}

	private void applyBorderlessMode() {
		Rectangle bounds = graphicsDevice.getDefaultConfiguration().getBounds();
		jframe.setUndecorated(true);
		jframe.setResizable(false);
		gamePanel.setRenderSize(bounds.width, bounds.height);
		jframe.setBounds(bounds);
		jframe.setVisible(true);
	}

}
