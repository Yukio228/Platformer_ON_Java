package ai.debug;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.List;

import ai.communication.AlertEvent;
import ai.core.Vector2;
import ai.navigation.NavigationGraph;
import ai.navigation.NavigationNode;
import ai.navigation.TacticalPoint;
import ai.perception.NoiseEvent;
import entities.Enemy;
import gamestates.Playing;
import main.Game;

public class AiDebugRenderer {
	private boolean enabled;
	private boolean drawNavigation;
	private boolean drawNoise;
	private boolean drawAlerts;

	public void toggleEnabled() {
		enabled = !enabled;
	}

	public void toggleNavigation() {
		drawNavigation = !drawNavigation;
	}

	public void toggleNoise() {
		drawNoise = !drawNoise;
	}

	public void toggleAlerts() {
		drawAlerts = !drawAlerts;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void draw(Graphics g, Playing playing, int xLvlOffset) {
		if (!enabled && !drawNavigation && !drawNoise && !drawAlerts)
			return;

		if (drawNavigation)
			drawNavigation(g, playing.getLevelManager().getCurrentLevel().getNavigationGraph(), xLvlOffset);
		if (drawNoise)
			drawNoise(g, playing, xLvlOffset);
		if (drawAlerts)
			drawAlerts(g, playing, xLvlOffset);
		if (enabled)
			drawEnemyInfo(g, playing.getEnemyManager().getAllEnemies(), xLvlOffset);
	}

	private void drawEnemyInfo(Graphics g, List<Enemy> enemies, int xLvlOffset) {
		Font old = g.getFont();
		g.setFont(old.deriveFont(Font.BOLD, 11f));
		for (Enemy enemy : enemies) {
			if (!enemy.isActive())
				continue;
			int x = (int) enemy.getHitbox().x - xLvlOffset;
			int y = Math.max(18, (int) enemy.getHitbox().y - 62);
			g.setColor(new Color(0, 0, 0, 170));
			g.fillRect(x - 2, y - 12, 178, 58);
			g.setColor(Color.WHITE);
			g.drawString(enemy.getEnemyDebugName() + " " + enemy.getAiState(), x, y);
			g.drawString("Action: " + enemy.getSelectedAiAction(), x, y + 11);
			g.drawString("Suspicion: " + Math.round(enemy.getSuspicionLevel()) + "%", x, y + 22);
			g.drawString("Memory: " + String.format("%.2f", enemy.getMemoryConfidence()), x, y + 33);
			g.drawString("Target: " + enemy.getCurrentAiTargetText(), x, y + 44);
			drawPath(g, enemy, xLvlOffset);
			drawVision(g, enemy, xLvlOffset);
		}
		g.setFont(old);
	}

	private void drawVision(Graphics g, Enemy enemy, int xLvlOffset) {
		int x = (int) enemy.getCenterX() - xLvlOffset;
		int y = (int) enemy.getCenterY();
		int sight = (int) enemy.getAiSightDistancePixels();
		g.setColor(new Color(255, 220, 60, 70));
		if (enemy.getWalkDir() == utilz.Constants.Directions.RIGHT)
			g.fillArc(x - sight, y - sight, sight * 2, sight * 2, -40, 80);
		else
			g.fillArc(x - sight, y - sight, sight * 2, sight * 2, 140, 80);
		g.setColor(new Color(60, 180, 255, 90));
		int hearing = (int) enemy.getAiHearingRadiusPixels();
		g.drawOval(x - hearing, y - hearing, hearing * 2, hearing * 2);
		Vector2 lastSeen = enemy.getLastKnownPlayerPosition();
		if (lastSeen != null) {
			g.setColor(new Color(255, 60, 60, 180));
			g.fillOval((int) lastSeen.x() - xLvlOffset - 5, (int) lastSeen.y() - 5, 10, 10);
		}
	}

	private void drawPath(Graphics g, Enemy enemy, int xLvlOffset) {
		List<NavigationNode> path = enemy.getCurrentPath();
		if (path.isEmpty())
			return;
		g.setColor(new Color(80, 255, 120, 180));
		for (int i = 0; i < path.size(); i++) {
			NavigationNode node = path.get(i);
			int x = (int) node.getWorldX() - xLvlOffset;
			int y = (int) node.getWorldY();
			g.fillOval(x - 4, y - 4, 8, 8);
			if (i > 0) {
				NavigationNode prev = path.get(i - 1);
				g.drawLine((int) prev.getWorldX() - xLvlOffset, (int) prev.getWorldY(), x, y);
			}
		}
	}

	private void drawNavigation(Graphics g, NavigationGraph graph, int xLvlOffset) {
		if (graph == null)
			return;
		g.setColor(new Color(90, 190, 255, 110));
		for (NavigationNode node : graph.getNodes()) {
			int x = (int) node.getWorldX() - xLvlOffset;
			int y = (int) node.getWorldY();
			g.fillOval(x - 2, y - 2, 4, 4);
		}
		g.setColor(new Color(255, 180, 60, 140));
		for (TacticalPoint point : graph.getTacticalPoints()) {
			int x = (int) point.getPosition().x() - xLvlOffset;
			int y = (int) point.getPosition().y();
			g.drawRect(x - 5, y - 5, 10, 10);
		}
	}

	private void drawNoise(Graphics g, Playing playing, int xLvlOffset) {
		for (NoiseEvent event : playing.getNoiseManager().getEvents()) {
			int radius = (int) event.getRadius();
			int x = (int) event.getX() - xLvlOffset;
			int y = (int) event.getY();
			g.setColor(new Color(120, 210, 255, 60));
			g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
			g.setColor(Color.CYAN);
			g.drawString(event.getType().name(), x + 4, y - 4);
		}
	}

	private void drawAlerts(Graphics g, Playing playing, int xLvlOffset) {
		for (AlertEvent event : playing.getAlertManager().getAlerts()) {
			int radius = (int) event.getRadius();
			int x = (int) event.getX() - xLvlOffset;
			int y = (int) event.getY();
			g.setColor(new Color(255, 80, 50, 50));
			g.fillOval(x - radius, y - radius, radius * 2, radius * 2);
			g.setColor(Color.RED);
			g.drawString("ALERT " + Math.round(event.getDanger() * 100f), x, Math.max(12, y - 8));
		}
	}

	public String overlayText() {
		return "AI F3=" + enabled + " F4=" + drawNavigation + " F5=" + drawNoise + " F6=" + drawAlerts;
	}
}
