package ai.decision;

import ai.core.EnemyContext;

public interface BehaviorNode {
	BehaviorStatus tick(EnemyContext context);
}
