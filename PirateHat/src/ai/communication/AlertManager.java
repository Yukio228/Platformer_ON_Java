package ai.communication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import ai.perception.NoiseManager;
import ai.perception.NoiseType;
import entities.Enemy;
import main.Game;

public class AlertManager {
	private final ArrayList<AlertEvent> alerts = new ArrayList<>();

	public synchronized void update() {
		Iterator<AlertEvent> iterator = alerts.iterator();
		while (iterator.hasNext()) {
			AlertEvent event = iterator.next();
			event.update();
			if (!event.isAlive())
				iterator.remove();
		}
	}

	public void sendAlert(Enemy source, float x, float y, float radius, float danger, int durationTicks, NoiseManager noiseManager) {
		synchronized (this) {
			alerts.add(new AlertEvent(x, y, radius, danger, durationTicks, source));
		}
		if (noiseManager != null)
			noiseManager.addNoise(NoiseType.ALLY_ALERT, x, y, Math.max(radius * 0.55f, Game.TILES_SIZE * 2f), danger, Math.min(durationTicks, 45));
	}

	public synchronized AlertEvent findAlertFor(Enemy enemy) {
		AlertEvent best = null;
		float bestDanger = -1f;
		for (AlertEvent event : alerts) {
			if (!event.reaches(enemy))
				continue;
			if (event.getDanger() > bestDanger) {
				bestDanger = event.getDanger();
				best = event;
			}
		}
		return best;
	}

	public synchronized List<AlertEvent> getAlerts() {
		return Collections.unmodifiableList(new ArrayList<>(alerts));
	}

	public synchronized void clear() {
		alerts.clear();
	}
}
