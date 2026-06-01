package ai.decision;

import java.util.function.Function;

import ai.core.EnemyContext;

public class ActionNode implements BehaviorNode {
	private final Function<EnemyContext, BehaviorStatus> action;

	public ActionNode(Function<EnemyContext, BehaviorStatus> action) {
		this.action = action;
	}

	@Override
	public BehaviorStatus tick(EnemyContext context) {
		return action.apply(context);
	}
}
