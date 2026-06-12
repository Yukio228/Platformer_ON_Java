package main;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.DisplayMode;
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
	private DisplayMode desktopDisplayMode;
	private int resolutionIndex = 1;
	private ScreenMode screenMode = ScreenMode.WINDOWED;

	public GameWindow(GamePanel gamePanel) {
		this.gamePanel = gamePanel;
		graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		desktopDisplayMode = graphicsDevice.getDisplayMode();

		jframe = new JFrame();

		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jframe.add(gamePanel);
		
		jframe.setResizable(false);
		loadSavedDisplaySettings();
		applyDisplaySettings();
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
		int clampedIndex = clampResolutionIndex(resolutionIndex);
		if (this.resolutionIndex == clampedIndex)
			return;

		this.resolutionIndex = clampedIndex;
		applyDisplaySettings();
		saveDisplaySettings();
	}

	public void setScreenMode(ScreenMode screenMode) {
		ScreenMode safeScreenMode = screenMode == null ? ScreenMode.WINDOWED : screenMode;
		if (this.screenMode == safeScreenMode)
			return;

		this.screenMode = safeScreenMode;
		applyDisplaySettings();
		saveDisplaySettings();
	}

	public String getResolutionLabel() {
		if (screenMode == ScreenMode.BORDERLESS)
			return "DESKTOP";
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

	public boolean canChangeResolutionInCurrentMode() {
		return screenMode != ScreenMode.BORDERLESS;
	}

	private void loadSavedDisplaySettings() {
		resolutionIndex = clampResolutionIndex(gamePanel.getGame().getSaveManager().getResolutionIndex());
		screenMode = parseScreenMode(gamePanel.getGame().getSaveManager().getScreenMode());
	}

	private void saveDisplaySettings() {
		gamePanel.getGame().getSaveManager().saveDisplaySettings(resolutionIndex, screenMode.name());
	}

	private int clampResolutionIndex(int resolutionIndex) {
		return Math.max(0, Math.min(WINDOW_RESOLUTIONS.length - 1, resolutionIndex));
	}

	private ScreenMode parseScreenMode(String value) {
		try {
			return value == null ? ScreenMode.WINDOWED : ScreenMode.valueOf(value);
		} catch (IllegalArgumentException e) {
			return ScreenMode.WINDOWED;
		}
	}

	private void applyDisplaySettings() {
		if (graphicsDevice.getFullScreenWindow() == jframe) {
			restoreDesktopDisplayMode();
			graphicsDevice.setFullScreenWindow(null);
		}

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
		jframe.setUndecorated(true);
		jframe.setResizable(false);
		int[] resolution = WINDOW_RESOLUTIONS[resolutionIndex];
		gamePanel.setRenderSize(resolution[0], resolution[1]);
		jframe.pack();
		jframe.setVisible(true);
		graphicsDevice.setFullScreenWindow(jframe);
		applyFullscreenDisplayMode(resolution[0], resolution[1]);
		DisplayMode activeMode = graphicsDevice.getDisplayMode();
		gamePanel.setRenderSize(activeMode.getWidth(), activeMode.getHeight());
		jframe.validate();
	}

	private void applyBorderlessMode() {
		restoreDesktopDisplayMode();
		Rectangle bounds = graphicsDevice.getDefaultConfiguration().getBounds();
		jframe.setUndecorated(true);
		jframe.setResizable(false);
		gamePanel.setRenderSize(bounds.width, bounds.height);
		jframe.setBounds(bounds);
		jframe.setVisible(true);
	}

	private void applyFullscreenDisplayMode(int width, int height) {
		if (!graphicsDevice.isDisplayChangeSupported())
			return;

		DisplayMode displayMode = findDisplayMode(width, height);
		if (displayMode != null)
			graphicsDevice.setDisplayMode(displayMode);
	}

	private DisplayMode findDisplayMode(int width, int height) {
		DisplayMode bestMode = null;
		for (DisplayMode mode : graphicsDevice.getDisplayModes()) {
			if (mode.getWidth() != width || mode.getHeight() != height)
				continue;
			if (bestMode == null || displayModeScore(mode) > displayModeScore(bestMode))
				bestMode = mode;
		}
		return bestMode;
	}

	private int displayModeScore(DisplayMode mode) {
		int bitDepth = mode.getBitDepth() == DisplayMode.BIT_DEPTH_MULTI ? 32 : mode.getBitDepth();
		int refreshRate = mode.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN ? 0 : mode.getRefreshRate();
		return bitDepth * 1000 + refreshRate;
	}

	private void restoreDesktopDisplayMode() {
		if (desktopDisplayMode == null || !graphicsDevice.isDisplayChangeSupported())
			return;
		DisplayMode currentMode = graphicsDevice.getDisplayMode();
		if (currentMode.getWidth() == desktopDisplayMode.getWidth() && currentMode.getHeight() == desktopDisplayMode.getHeight() && currentMode.getBitDepth() == desktopDisplayMode.getBitDepth()
				&& currentMode.getRefreshRate() == desktopDisplayMode.getRefreshRate())
			return;
		graphicsDevice.setDisplayMode(desktopDisplayMode);
	}

}
