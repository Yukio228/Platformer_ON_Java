package ai.navigation;

import static utilz.HelpMethods.IsTileSolid;

import ai.core.Vector2;
import main.Game;

public class NavigationGraphBuilder {
	public NavigationGraph build(int[][] lvlData) {
		NavigationGraph graph = new NavigationGraph();
		int id = 0;
		for (int y = 1; y < lvlData.length - 1; y++)
			for (int x = 0; x < lvlData[y].length; x++)
				if (canStandAt(lvlData, x, y))
					graph.addNode(new NavigationNode(id++, x, y));

		for (NavigationNode node : graph.getNodes()) {
			addWalkEdges(graph, node);
			addDropEdges(graph, node, lvlData);
			addJumpEdges(graph, node, lvlData);
		}

		buildTacticalPoints(graph);
		return graph;
	}

	private boolean canStandAt(int[][] lvlData, int x, int y) {
		return !IsTileSolid(x, y, lvlData) && !IsTileSolid(x, y - 1, lvlData) && IsTileSolid(x, y + 1, lvlData);
	}

	private void addWalkEdges(NavigationGraph graph, NavigationNode node) {
		for (int dx = -1; dx <= 1; dx += 2) {
			NavigationNode to = graph.getNodeAt(node.getTileX() + dx, node.getTileY());
			if (to != null)
				graph.addEdge(node, to, NavigationEdgeType.WALK, 1f, false);
		}
	}

	private void addDropEdges(NavigationGraph graph, NavigationNode node, int[][] lvlData) {
		for (int dx = -1; dx <= 1; dx++) {
			int x = node.getTileX() + dx;
			for (int y = node.getTileY() + 1; y < Math.min(lvlData.length - 1, node.getTileY() + 6); y++) {
				NavigationNode to = graph.getNodeAt(x, y);
				if (to != null) {
					graph.addEdge(node, to, NavigationEdgeType.DROP_DOWN, 2f + (y - node.getTileY()) * 0.2f, false);
					break;
				}
				if (IsTileSolid(x, y, lvlData))
					break;
			}
		}
	}

	private void addJumpEdges(NavigationGraph graph, NavigationNode node, int[][] lvlData) {
		for (NavigationNode to : graph.getNodes()) {
			int dx = Math.abs(to.getTileX() - node.getTileX());
			int dy = node.getTileY() - to.getTileY();
			if (dx == 0 || dx > 4 || dy < -1 || dy > 3)
				continue;
			if (isArcClear(lvlData, node, to))
				graph.addEdge(node, to, NavigationEdgeType.JUMP, 3f + dx * 0.25f + Math.max(0, dy) * 0.5f, false);
		}
	}

	private boolean isArcClear(int[][] lvlData, NavigationNode from, NavigationNode to) {
		float x1 = from.getWorldX();
		float y1 = from.getWorldY() - Game.TILES_SIZE;
		float x2 = to.getWorldX();
		float y2 = to.getWorldY() - Game.TILES_SIZE;
		int steps = Math.max(2, Math.abs(to.getTileX() - from.getTileX()) * 3);
		for (int i = 0; i <= steps; i++) {
			float t = i / (float) steps;
			float x = x1 + (x2 - x1) * t;
			float y = y1 + (y2 - y1) * t - (float) Math.sin(t * Math.PI) * Game.TILES_SIZE * 1.6f;
			if (IsTileSolid((int) (x / Game.TILES_SIZE), (int) (y / Game.TILES_SIZE), lvlData))
				return false;
		}
		return true;
	}

	private void buildTacticalPoints(NavigationGraph graph) {
		int id = 0;
		for (NavigationNode node : graph.getNodes()) {
			if (node.getId() % 11 == 0)
				graph.addTacticalPoint(new TacticalPoint(id++, TacticalPointType.PATROL_POINT, new Vector2(node.getWorldX(), node.getWorldY())));
			if (node.getId() % 17 == 0)
				graph.addTacticalPoint(new TacticalPoint(id++, TacticalPointType.SAFE_POINT, new Vector2(node.getWorldX(), node.getWorldY())));
			if (node.getId() % 23 == 0)
				graph.addTacticalPoint(new TacticalPoint(id++, TacticalPointType.ATTACK_POINT, new Vector2(node.getWorldX(), node.getWorldY())));
			if (node.getId() % 29 == 0)
				graph.addTacticalPoint(new TacticalPoint(id++, TacticalPointType.RETREAT_POINT, new Vector2(node.getWorldX(), node.getWorldY())));
			if (node.getId() % 31 == 0)
				graph.addTacticalPoint(new TacticalPoint(id++, TacticalPointType.AMBUSH_POINT, new Vector2(node.getWorldX(), node.getWorldY())));
		}
	}
}
