package ai.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import ai.core.EnemyAiProfile;

public class Pathfinder {
	public List<NavigationNode> findPath(NavigationGraph graph, NavigationNode start, NavigationNode target) {
		return findPath(graph, start, target, null);
	}

	public List<NavigationNode> findPath(NavigationGraph graph, NavigationNode start, NavigationNode target, EnemyAiProfile profile) {
		if (graph == null || start == null || target == null)
			return Collections.emptyList();
		if (start.equals(target))
			return List.of(start);

		PriorityQueue<NavigationNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> fScore.getOrDefault(n, Float.MAX_VALUE)));
		Set<NavigationNode> closedSet = new HashSet<>();
		cameFrom.clear();
		gScore.clear();
		fScore.clear();

		gScore.put(start, 0f);
		fScore.put(start, heuristic(start, target));
		openSet.add(start);

		while (!openSet.isEmpty()) {
			NavigationNode current = openSet.poll();
			if (current.equals(target))
				return reconstruct(current);

			closedSet.add(current);
			for (NavigationEdge edge : graph.getEdges(current)) {
				if (!canUse(edge, profile) || closedSet.contains(edge.getTo()))
					continue;
				float tentative = gScore.getOrDefault(current, Float.MAX_VALUE) + transitionCost(edge);
				if (tentative >= gScore.getOrDefault(edge.getTo(), Float.MAX_VALUE))
					continue;
				cameFrom.put(edge.getTo(), current);
				gScore.put(edge.getTo(), tentative);
				fScore.put(edge.getTo(), tentative + heuristic(edge.getTo(), target));
				if (!openSet.contains(edge.getTo()))
					openSet.add(edge.getTo());
			}
		}

		return Collections.emptyList();
	}

	private final Map<NavigationNode, NavigationNode> cameFrom = new HashMap<>();
	private final Map<NavigationNode, Float> gScore = new HashMap<>();
	private final Map<NavigationNode, Float> fScore = new HashMap<>();

	private boolean canUse(NavigationEdge edge, EnemyAiProfile profile) {
		if (profile == null)
			return true;
		return switch (edge.getType()) {
		case JUMP -> profile.canJump();
		case DROP_DOWN -> profile.canDropDown();
		default -> true;
		};
	}

	private float transitionCost(NavigationEdge edge) {
		float cost = switch (edge.getType()) {
		case WALK -> 1f;
		case DROP_DOWN -> 2f;
		case JUMP -> 3f;
		case CLIMB, USE_PLATFORM -> edge.getCost();
		};
		if (edge.isDangerous())
			cost += 5f;
		return cost;
	}

	private float heuristic(NavigationNode a, NavigationNode b) {
		return Math.abs(a.getTileX() - b.getTileX()) + Math.abs(a.getTileY() - b.getTileY()) * 1.4f;
	}

	private List<NavigationNode> reconstruct(NavigationNode current) {
		ArrayList<NavigationNode> path = new ArrayList<>();
		path.add(current);
		while (cameFrom.containsKey(current)) {
			current = cameFrom.get(current);
			path.add(current);
		}
		Collections.reverse(path);
		return path;
	}
}
