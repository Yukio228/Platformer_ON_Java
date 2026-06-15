package tutorial;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import main.Game;

public class TutorialHint {

	private static final float FADE_SPEED = 0.035f;
	private static final Font TEXT_FONT = new Font("Arial", Font.BOLD, (int) (12 * Game.SCALE));

	private final String text;
	private final Predicate<TutorialManager> showCondition;
	private final Predicate<TutorialManager> completeCondition;
	private boolean completed;
	private boolean visible;
	private float alpha;

	public TutorialHint(String text, Predicate<TutorialManager> showCondition, Predicate<TutorialManager> completeCondition) {
		this.text = text;
		this.showCondition = showCondition;
		this.completeCondition = completeCondition;
	}

	public void update(TutorialManager manager) {
		if (!completed && completeCondition.test(manager))
			completed = true;

		visible = !completed && showCondition.test(manager);

		if (visible)
			alpha = Math.min(1f, alpha + FADE_SPEED);
		else
			alpha = Math.max(0f, alpha - FADE_SPEED);
	}

	public void draw(Graphics g) {
		if (alpha <= 0f)
			return;

		Graphics2D g2 = (Graphics2D) g.create();
		Composite oldComposite = g2.getComposite();
		Font oldFont = g2.getFont();
		Object oldTextHint = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g2.setFont(TEXT_FONT);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		List<Run> runs = parseRuns(text);
		FontMetrics fm = g2.getFontMetrics();
		int contentW = getRunsWidth(runs, fm);
		int contentH = fm.getHeight();
		int padX = (int) (16 * Game.SCALE);
		int padY = (int) (9 * Game.SCALE);
		int panelW = Math.min(Game.GAME_WIDTH - (int) (120 * Game.SCALE), contentW + padX * 2);
		int panelH = contentH + padY * 2 + (int) (2 * Game.SCALE);
		int panelX = Game.GAME_WIDTH / 2 - panelW / 2;
		int panelY = Game.GAME_HEIGHT - panelH - (int) (30 * Game.SCALE);
		int arc = (int) (10 * Game.SCALE);

		g2.setColor(new Color(0, 0, 0, 95));
		g2.fillRoundRect(panelX + (int) (4 * Game.SCALE), panelY + (int) (4 * Game.SCALE), panelW, panelH, arc, arc);
		g2.setColor(new Color(31, 45, 43, 220));
		g2.fillRoundRect(panelX, panelY, panelW, panelH, arc, arc);
		g2.setColor(new Color(88, 142, 104, 170));
		g2.drawRoundRect(panelX + 1, panelY + 1, panelW - 2, panelH - 2, arc, arc);
		g2.setColor(new Color(244, 190, 125, 205));
		g2.drawRoundRect(panelX + (int) (4 * Game.SCALE), panelY + (int) (4 * Game.SCALE), panelW - (int) (8 * Game.SCALE), panelH - (int) (8 * Game.SCALE), arc, arc);

		int x = panelX + panelW / 2 - contentW / 2;
		int baselineY = panelY + panelH / 2 + fm.getAscent() / 2 - (int) (2 * Game.SCALE);
		for (Run run : runs) {
			if (run.key)
				x = drawKeyRun(g2, run.text, x, baselineY, fm);
			else
				x = drawTextRun(g2, run.text, x, baselineY);
		}

		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldTextHint);
		g2.setFont(oldFont);
		g2.setComposite(oldComposite);
		g2.dispose();
	}

	public boolean isFinished() {
		return completed && alpha <= 0f;
	}

	public void reset() {
		completed = false;
		visible = false;
		alpha = 0f;
	}

	private int drawTextRun(Graphics2D g2, String value, int x, int baselineY) {
		g2.setColor(new Color(13, 17, 18, 180));
		g2.drawString(value, x + 1, baselineY + 1);
		g2.setColor(new Color(255, 239, 197));
		g2.drawString(value, x, baselineY);
		return x + g2.getFontMetrics().stringWidth(value);
	}

	private int drawKeyRun(Graphics2D g2, String value, int x, int baselineY, FontMetrics fm) {
		int padX = (int) (6 * Game.SCALE);
		int padY = (int) (2 * Game.SCALE);
		int keyW = fm.stringWidth(value) + padX * 2;
		int keyH = fm.getHeight() - (int) (2 * Game.SCALE);
		int keyY = baselineY - fm.getAscent() + padY;
		int arc = (int) (5 * Game.SCALE);

		g2.setColor(new Color(14, 19, 18, 205));
		g2.fillRoundRect(x, keyY, keyW, keyH, arc, arc);
		g2.setColor(new Color(255, 223, 147, 230));
		g2.drawRoundRect(x, keyY, keyW, keyH, arc, arc);
		g2.setColor(new Color(255, 240, 200));
		g2.drawString(value, x + padX, baselineY);
		return x + keyW;
	}

	private int getRunsWidth(List<Run> runs, FontMetrics fm) {
		int width = 0;
		int keyPad = (int) (12 * Game.SCALE);
		for (Run run : runs)
			width += fm.stringWidth(run.text) + (run.key ? keyPad : 0);
		return width;
	}

	private List<Run> parseRuns(String value) {
		ArrayList<Run> runs = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inKey = false;

		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c == '[' && !inKey) {
				if (current.length() > 0) {
					runs.add(new Run(current.toString(), false));
					current.setLength(0);
				}
				inKey = true;
			} else if (c == ']' && inKey) {
				runs.add(new Run(current.toString(), true));
				current.setLength(0);
				inKey = false;
			} else
				current.append(c);
		}

		if (current.length() > 0)
			runs.add(new Run(current.toString(), inKey));

		return runs;
	}

	private static class Run {
		private final String text;
		private final boolean key;

		private Run(String text, boolean key) {
			this.text = text;
			this.key = key;
		}
	}
}
