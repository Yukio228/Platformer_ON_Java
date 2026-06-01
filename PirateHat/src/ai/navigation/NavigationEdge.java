package ai.navigation;

public class NavigationEdge {
	private final NavigationNode from;
	private final NavigationNode to;
	private final NavigationEdgeType type;
	private final float cost;
	private final boolean dangerous;

	public NavigationEdge(NavigationNode from, NavigationNode to, NavigationEdgeType type, float cost, boolean dangerous) {
		this.from = from;
		this.to = to;
		this.type = type;
		this.cost = cost;
		this.dangerous = dangerous;
	}

	public NavigationNode getFrom() {
		return from;
	}

	public NavigationNode getTo() {
		return to;
	}

	public NavigationEdgeType getType() {
		return type;
	}

	public float getCost() {
		return cost;
	}

	public boolean isDangerous() {
		return dangerous;
	}
}
