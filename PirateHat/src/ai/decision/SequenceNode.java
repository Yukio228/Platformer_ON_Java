package ai.decision;

import java.util.ArrayList;
import java.util.List;

import ai.core.EnemyContext;

public class SequenceNode implements BehaviorNode {
	private final List<BehaviorNode> children = new ArrayList<>();

	public SequenceNode add(BehaviorNode child) {
		children.add(child);
		return this;
	}

	@Override
	public BehaviorStatus tick(EnemyContext context) {
		for (BehaviorNode child : children) {
			BehaviorStatus status = child.tick(context);
			if (status != BehaviorStatus.SUCCESS)
				return status;
		}
		return BehaviorStatus.SUCCESS;
	}
}
