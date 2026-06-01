package ai.perception;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import main.Game;

public class NoiseManager {
	private final ArrayList<NoiseEvent> events = new ArrayList<>();

	public void update() {
		Iterator<NoiseEvent> iterator = events.iterator();
		while (iterator.hasNext()) {
			NoiseEvent event = iterator.next();
			event.update();
			if (!event.isAlive())
				iterator.remove();
		}
	}

	public void addNoise(NoiseType type, float x, float y, float radius, float intensity, int durationTicks) {
		events.add(new NoiseEvent(type, x, y, radius, intensity, durationTicks));
	}

	public void addPlayerNoise(NoiseType type, float x, float y) {
		float tile = Game.TILES_SIZE;
		switch (type) {
		case WALK -> addNoise(type, x, y, tile * 1.5f, 0.35f, 24);
		case RUN -> addNoise(type, x, y, tile * 3f, 0.55f, 30);
		case JUMP -> addNoise(type, x, y, tile * 2f, 0.45f, 26);
		case LANDING -> addNoise(type, x, y, tile * 4f, 0.75f, 36);
		case BASIC_ATTACK -> addNoise(type, x, y, tile * 4f, 0.8f, 34);
		case POWER_ATTACK -> addNoise(type, x, y, tile * 8f, 1.0f, 45);
		case PLAYER_HURT -> addNoise(type, x, y, tile * 5f, 0.9f, 40);
		default -> addNoise(type, x, y, tile * 3f, 0.5f, 30);
		}
	}

	public NoiseEvent findNearestAudibleNoise(float listenerX, float listenerY, float hearingMultiplier) {
		NoiseEvent nearest = null;
		float nearestDist = Float.MAX_VALUE;
		for (NoiseEvent event : events) {
			if (!event.canBeHeardFrom(listenerX, listenerY, hearingMultiplier))
				continue;
			float dx = event.getX() - listenerX;
			float dy = event.getY() - listenerY;
			float dist = dx * dx + dy * dy;
			if (dist < nearestDist) {
				nearestDist = dist;
				nearest = event;
			}
		}
		return nearest;
	}

	public List<NoiseEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}

	public void clear() {
		events.clear();
	}
}
