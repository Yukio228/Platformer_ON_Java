package ai.decision;

import java.util.ArrayList;
import java.util.List;

import ai.core.EnemyContext;

public class SelectorNode implements BehaviorNode {
	private final List<BehaviorNode> children = new ArrayList<>();

	public SelectorNode add(BehaviorNode child) {
		children.add(child);
		return this;
	}

	@Override
	public BehaviorStatus tick(EnemyContext context) {
		for (BehaviorNode child : children) {
			BehaviorStatus status = child.tick(context);
			if (status != BehaviorStatus.FAILURE)
				return status;
		}
		return BehaviorStatus.FAILURE;
	}
}
