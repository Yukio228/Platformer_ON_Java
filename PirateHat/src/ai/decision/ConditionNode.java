package ai.decision;

import java.util.function.Predicate;

import ai.core.EnemyContext;

public class ConditionNode implements BehaviorNode {
	private final Predicate<EnemyContext> condition;

	public ConditionNode(Predicate<EnemyContext> condition) {
		this.condition = condition;
	}

	@Override
	public BehaviorStatus tick(EnemyContext context) {
		return condition.test(context) ? BehaviorStatus.SUCCESS : BehaviorStatus.FAILURE;
	}
}
