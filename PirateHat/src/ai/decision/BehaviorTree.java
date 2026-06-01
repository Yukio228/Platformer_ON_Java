package ai.decision;

import ai.core.EnemyContext;

public class BehaviorTree {
	private final BehaviorNode root;

	public BehaviorTree(BehaviorNode root) {
		this.root = root;
	}

	public BehaviorStatus tick(EnemyContext context) {
		return root.tick(context);
	}
}
