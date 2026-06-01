package ai.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationGraph {
	private final ArrayList<NavigationNode> nodes = new ArrayList<>();
	private final Map<NavigationNode, ArrayList<NavigationEdge>> edges = new HashMap<>();
	private final Map<String, NavigationNode> byTile = new HashMap<>();
	private final ArrayList<TacticalPoint> tacticalPoints = new ArrayList<>();

	public void addNode(NavigationNode node) {
		nodes.add(node);
		edges.put(node, new ArrayList<>());
		byTile.put(key(node.getTileX(), node.getTileY()), node);
	}

	public void addEdge(NavigationNode from, NavigationNode to, NavigationEdgeType type, float cost, boolean dangerous) {
		if (from == null || to == null || from.equals(to))
			return;
		edges.get(from).add(new NavigationEdge(from, to, type, cost, dangerous));
	}

	public NavigationNode getNodeAt(int tileX, int tileY) {
		return byTile.get(key(tileX, tileY));
	}

	public List<NavigationEdge> getEdges(NavigationNode node) {
		return edges.getOrDefault(node, new ArrayList<>());
	}

	public Collection<NavigationNode> getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	public List<TacticalPoint> getTacticalPoints() {
		return Collections.unmodifiableList(tacticalPoints);
	}

	public void addTacticalPoint(TacticalPoint point) {
		tacticalPoints.add(point);
	}

	public NavigationNode findNearestNode(float worldX, float worldY) {
		NavigationNode nearest = null;
		float bestDist = Float.MAX_VALUE;
		for (NavigationNode node : nodes) {
			float dx = node.getWorldX() - worldX;
			float dy = node.getWorldY() - worldY;
			float dist = dx * dx + dy * dy;
			if (dist < bestDist) {
				bestDist = dist;
				nearest = node;
			}
		}
		return nearest;
	}

	public TacticalPoint findNearestFreePoint(TacticalPointType type, float worldX, float worldY) {
		TacticalPoint nearest = null;
		float bestDist = Float.MAX_VALUE;
		for (TacticalPoint point : tacticalPoints) {
			if (point.getType() != type || point.isOccupied())
				continue;
			float dx = point.getPosition().x() - worldX;
			float dy = point.getPosition().y() - worldY;
			float dist = dx * dx + dy * dy;
			if (dist < bestDist) {
				bestDist = dist;
				nearest = point;
			}
		}
		return nearest;
	}

	private String key(int x, int y) {
		return x + ":" + y;
	}
}
