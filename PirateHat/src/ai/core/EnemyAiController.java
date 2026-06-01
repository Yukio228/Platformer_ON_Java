package ai.core;

import static utilz.Constants.EnemyConstants.BOSS;
import static utilz.Constants.EnemyConstants.IsPirateMob;

import java.util.Collections;
import java.util.List;

import ai.communication.AlertEvent;
import ai.decision.ActionNode;
import ai.decision.BehaviorStatus;
import ai.decision.BehaviorTree;
import ai.decision.ConditionNode;
import ai.decision.EnemyAction;
import ai.decision.SelectorNode;
import ai.decision.SequenceNode;
import ai.decision.UtilityEvaluator;
import ai.memory.EnemyMemory;
import ai.navigation.NavigationGraph;
import ai.navigation.NavigationNode;
import ai.navigation.PathFollower;
import ai.navigation.Pathfinder;
import ai.navigation.TacticalPoint;
import ai.navigation.TacticalPointType;
import ai.perception.DamageSensor;
import ai.perception.HearingSensor;
import ai.perception.NoiseEvent;
import ai.perception.VisionSensor;
import entities.Enemy;
import entities.Player;
import gamestates.Playing;
import main.Game;

public class EnemyAiController {
	private final Enemy enemy;
	private final EnemyAiProfile baseProfile;
	private EnemyAiProfile profile;
	private final VisionSensor vision = new VisionSensor();
	private final HearingSensor hearing = new HearingSensor();
	private final DamageSensor damage = new DamageSensor();
	private final EnemyMemory memory = new EnemyMemory();
	private final Pathfinder pathfinder = new Pathfinder();
	private final PathFollower pathFollower = new PathFollower();
	private final UtilityEvaluator utility = new UtilityEvaluator();
	private final BehaviorTree pirateMobTree;
	private EnemyAiState state = EnemyAiState.PATROL;
	private EnemyAction selectedAction = EnemyAction.PATROL;
	private int stateTick;
	private int searchTick;
	private int tick;
	private int alertCooldown;
	private int repathCooldown;
	private Vector2 currentTarget;
	private Vector2 lastPathTarget;
	private int lastNoiseTick = -100000;
	private int lastAlertTick = -100000;

	public EnemyAiController(Enemy enemy, EnemyAiProfile profile) {
		this.enemy = enemy;
		this.baseProfile = profile;
		this.profile = profile.copy();
		this.pirateMobTree = buildPirateMobTree();
	}

	public void update(int[][] lvlData, Playing playing) {
		tick = playing.getAiTick();
		refreshDifficultyProfile(playing);
		if (enemy.getState() == utilz.Constants.EnemyConstants.DEAD || !enemy.isActive()) {
			changeState(EnemyAiState.DEAD, playing, "dead");
			return;
		}

		if (alertCooldown > 0)
			alertCooldown--;
		if (repathCooldown > 0)
			repathCooldown--;

		memory.update(tick, profile.getMemoryDurationTicks());
		EnemyContext context = new EnemyContext(enemy, playing, lvlData, tick);
		processDamage(context);
		boolean playerSeen = processVision(context);
		processHearing(context);
		processAlerts(context);

		if (IsPirateMob(enemy.getEnemyType()))
			pirateMobTree.tick(context);

		updateStateMachine(context, playerSeen);
		selectedAction = utility.chooseAction(enemy, context.player(), profile, memory, isPlayerInAttackRange(context.player()), playing.getEnemyManager().countAlliesNear(enemy, Game.TILES_SIZE * 5));
		if (state == EnemyAiState.RETREAT)
			selectedAction = EnemyAction.RETREAT;
		else if (state == EnemyAiState.ATTACK)
			selectedAction = EnemyAction.ATTACK;
		else if (state == EnemyAiState.INVESTIGATE)
			selectedAction = EnemyAction.INVESTIGATE;
		else if (state == EnemyAiState.SEARCH)
			selectedAction = EnemyAction.SEARCH;
		else if (state == EnemyAiState.CHASE)
			selectedAction = selectedAction == EnemyAction.CALL_ALLIES ? EnemyAction.CALL_ALLIES : EnemyAction.CHASE;

		stateTick++;
	}

	private void refreshDifficultyProfile(Playing playing) {
		profile = baseProfile.copy();
		profile.applyDifficulty(playing.getDifficultyDirector().getCurrentProfile().getSightDistanceMultiplier(),
				playing.getDifficultyDirector().getCurrentProfile().getAggressionModifier(),
				playing.getDifficultyDirector().getCurrentProfile().getMemoryDurationMultiplier(),
				playing.getDifficultyDirector().getCurrentProfile().getReactionDelay());
		if (enemy.getEnemyType() == BOSS)
			profile.applyDifficulty(1f, playing.getDifficultyDirector().getCurrentProfile().getBossAggressionModifier(), 1f, profile.getReactionDelayTicks());
	}

	private void processDamage(EnemyContext context) {
		Vector2 source = damage.consumeDamageSource();
		if (source == null)
			return;
		memory.rememberDamage(source.x(), source.y(), tick);
		currentTarget = source;
		changeState(EnemyAiState.CHASE, context.playing(), "damage");
		callAllies(context, source, 1f);
		context.metrics().record(tick, enemy, "DAMAGE_SOURCE", state, 0, 0, source.toString());
	}

	private boolean processVision(EnemyContext context) {
		Player player = context.player();
		boolean detected = vision.update(context.levelData(), enemy, player, profile);
		if (vision.hasLineOfSight()) {
			float px = player.getHitbox().x + player.getHitbox().width / 2f;
			float py = player.getHitbox().y + player.getHitbox().height / 2f;
			currentTarget = new Vector2(px, py);
			if (vision.getSuspicionLevel() >= 70f)
				memory.rememberPlayer(px, py, tick);
		}
		if (detected) {
			memory.rememberPlayer(player.getHitbox().x + player.getHitbox().width / 2f, player.getHitbox().y + player.getHitbox().height / 2f, tick);
			if (state == EnemyAiState.PATROL || state == EnemyAiState.INVESTIGATE || state == EnemyAiState.SEARCH || state == EnemyAiState.RETURN_TO_PATROL)
				changeState(EnemyAiState.ALERT, context.playing(), "player seen");
			else if (state != EnemyAiState.ATTACK && state != EnemyAiState.RETREAT)
				changeState(EnemyAiState.CHASE, context.playing(), "player tracked");
			callAllies(context, currentTarget, 0.85f);
			context.metrics().record(tick, enemy, "PLAYER_SEEN", state, 0, 0, "suspicion=" + Math.round(vision.getSuspicionLevel()));
			return true;
		}
		if (vision.getSuspicionLevel() >= 70f && state == EnemyAiState.PATROL)
			changeState(EnemyAiState.INVESTIGATE, context.playing(), "suspicion");
		return false;
	}

	private void processHearing(EnemyContext context) {
		NoiseEvent noise = hearing.hear(context.noiseManager(), enemy, profile);
		if (noise == null || tick - lastNoiseTick < 18)
			return;
		if (state == EnemyAiState.CHASE || state == EnemyAiState.ATTACK || state == EnemyAiState.RETREAT)
			return;
		lastNoiseTick = tick;
		memory.rememberNoise(noise.getX(), noise.getY(), tick, Math.min(0.85f, 0.35f + noise.getIntensity() * 0.5f));
		currentTarget = new Vector2(noise.getX(), noise.getY());
		changeState(EnemyAiState.INVESTIGATE, context.playing(), "noise " + noise.getType());
		context.metrics().record(tick, enemy, "HEARD_NOISE", state, 0, 0, noise.getType().name());
	}

	private void processAlerts(EnemyContext context) {
		AlertEvent alert = context.alertManager().findAlertFor(enemy);
		if (alert == null || tick - lastAlertTick < 35)
			return;
		if (state == EnemyAiState.CHASE || state == EnemyAiState.ATTACK)
			return;
		lastAlertTick = tick;
		memory.rememberAlert(alert.getX(), alert.getY(), tick, Math.min(0.9f, 0.45f + alert.getDanger() * 0.45f));
		currentTarget = new Vector2(alert.getX(), alert.getY());
		changeState(EnemyAiState.INVESTIGATE, context.playing(), "ally alert");
		context.metrics().record(tick, enemy, "ALLY_ALERT_RECEIVED", state, 0, 0, "danger=" + alert.getDanger());
	}

	private void updateStateMachine(EnemyContext context, boolean playerSeen) {
		Player player = context.player();
		switch (state) {
		case IDLE -> changeState(EnemyAiState.PATROL, context.playing(), "idle");
		case PATROL -> {
			if (playerSeen)
				changeState(EnemyAiState.ALERT, context.playing(), "patrol sees player");
		}
		case INVESTIGATE -> {
			if (playerSeen)
				changeState(EnemyAiState.ALERT, context.playing(), "investigate sees player");
			else if (isAtCurrentTarget())
				changeState(EnemyAiState.SEARCH, context.playing(), "noise reached");
			else if (memory.getConfidence() <= 0.08f)
				changeState(EnemyAiState.RETURN_TO_PATROL, context.playing(), "stale investigation");
		}
		case ALERT -> {
			enemy.turnTowardsPlayer(player);
			if (stateTick >= profile.getReactionDelayTicks())
				changeState(EnemyAiState.CHASE, context.playing(), "reaction elapsed");
		}
		case CHASE -> {
			if (profile.canRetreat() && enemy.getHealthPercent() < 0.22f)
				changeState(EnemyAiState.RETREAT, context.playing(), "low health");
			else if (isPlayerInAttackRange(player))
				changeState(EnemyAiState.ATTACK, context.playing(), "attack range");
			else if (!playerSeen && stateTick > 55 && memory.getConfidence() > 0.05f)
				changeState(EnemyAiState.SEARCH, context.playing(), "lost visual");
			else if (!playerSeen && memory.getConfidence() <= 0.05f)
				changeState(EnemyAiState.RETURN_TO_PATROL, context.playing(), "memory expired");
		}
		case ATTACK -> {
			if (profile.canRetreat() && enemy.getHealthPercent() < 0.14f)
				changeState(EnemyAiState.RETREAT, context.playing(), "critical health");
		}
		case SEARCH -> {
			if (playerSeen)
				changeState(EnemyAiState.CHASE, context.playing(), "search found player");
			else if (searchTick++ > 160 || memory.getConfidence() <= 0.03f) {
				searchTick = 0;
				changeState(EnemyAiState.RETURN_TO_PATROL, context.playing(), "search timeout");
			}
		}
		case RETREAT -> {
			float playerDistance = Math.abs(player.getHitbox().x - enemy.getHitbox().x);
			if (playerDistance > Game.TILES_SIZE * 5f)
				changeState(memory.getConfidence() > 0.45f && enemy.getHealthPercent() > 0.18f ? EnemyAiState.CHASE : EnemyAiState.PATROL, context.playing(), "retreat complete");
		}
		case RETURN_TO_PATROL -> {
			if (Math.abs(enemy.getCenterX() - enemy.getSpawnX()) < Game.TILES_SIZE * 0.6f)
				changeState(EnemyAiState.PATROL, context.playing(), "home reached");
		}
		default -> {
		}
		}
	}

	private BehaviorTree buildPirateMobTree() {
		SelectorNode root = new SelectorNode();
		root.add(new SequenceNode()
				.add(new ConditionNode(ctx -> enemy.getHealthPercent() < 0.2f && profile.canRetreat()))
				.add(new ActionNode(ctx -> {
					selectedAction = EnemyAction.RETREAT;
					changeState(EnemyAiState.RETREAT, ctx.playing(), "behavior survival");
					return BehaviorStatus.SUCCESS;
				})));
		root.add(new SequenceNode()
				.add(new ConditionNode(ctx -> isPlayerInAttackRange(ctx.player())))
				.add(new ActionNode(ctx -> {
					selectedAction = EnemyAction.ATTACK;
					changeState(EnemyAiState.ATTACK, ctx.playing(), "behavior attack");
					return BehaviorStatus.SUCCESS;
				})));
		root.add(new SequenceNode()
				.add(new ConditionNode(ctx -> memory.getLastSeenPlayerPosition() != null && profile.canCallAllies()))
				.add(new ConditionNode(ctx -> ctx.playing().getEnemyManager().countAlliesNear(enemy, Game.TILES_SIZE * 5f) > 0))
				.add(new ActionNode(ctx -> {
					selectedAction = EnemyAction.CALL_ALLIES;
					callAllies(ctx, memory.getLastSeenPlayerPosition(), 0.75f);
					return BehaviorStatus.SUCCESS;
				})));
		root.add(new SequenceNode()
				.add(new ConditionNode(ctx -> memory.getBestKnownTarget() != null && memory.getConfidence() > 0.15f))
				.add(new ActionNode(ctx -> {
					if (state == EnemyAiState.PATROL)
						changeState(EnemyAiState.INVESTIGATE, ctx.playing(), "behavior target");
					return BehaviorStatus.SUCCESS;
				})));
		root.add(new ActionNode(ctx -> BehaviorStatus.SUCCESS));
		return new BehaviorTree(root);
	}

	public void move(int[][] lvlData, Playing playing) {
		switch (state) {
		case ALERT, STUNNED, DEAD, ATTACK -> {
			if (memory.getBestKnownTarget() != null)
				enemy.turnTowardsX(memory.getBestKnownTarget().x());
		}
		case CHASE -> moveToTarget(lvlData, playing, getChaseTarget(playing), enemy.getChaseSpeedMultiplier());
		case INVESTIGATE -> moveToTarget(lvlData, playing, memory.getLastHeardNoisePosition() != null ? memory.getLastHeardNoisePosition() : currentTarget, 0.95f);
		case SEARCH -> searchMove(lvlData);
		case RETREAT -> retreatMove(lvlData, playing);
		case RETURN_TO_PATROL -> moveToTarget(lvlData, playing, new Vector2(enemy.getSpawnX(), enemy.getSpawnY()), 0.9f);
		default -> enemy.aiMove(lvlData, 1f);
		}
	}

	private Vector2 getChaseTarget(Playing playing) {
		if (vision.hasLineOfSight()) {
			Player player = playing.getPlayer();
			return new Vector2(player.getHitbox().x + player.getHitbox().width / 2f, player.getHitbox().y + player.getHitbox().height / 2f);
		}
		return memory.getBestKnownTarget();
	}

	private void moveToTarget(int[][] lvlData, Playing playing, Vector2 target, float speedMultiplier) {
		if (target == null) {
			enemy.aiMove(lvlData, 0.8f);
			return;
		}
		currentTarget = target;
		playing.getSharedBlackboard().claimTarget(enemy, target);
		if (shouldUseNavigation())
			if (followPath(lvlData, playing, target, speedMultiplier))
				return;
		enemy.turnTowardsX(target.x());
		enemy.aiMove(lvlData, speedMultiplier);
	}

	private boolean followPath(int[][] lvlData, Playing playing, Vector2 target, float speedMultiplier) {
		NavigationGraph graph = playing.getLevelManager().getCurrentLevel().getNavigationGraph();
		if (graph == null)
			return false;
		if (pathFollower.isFinished() || pathFollower.needsRepath() || shouldRepath(target)) {
			buildPath(graph, playing, target);
			pathFollower.clearRepathRequest();
		}
		if (pathFollower.getPath().isEmpty())
			return false;
		return pathFollower.follow(enemy, lvlData, profile, speedMultiplier);
	}

	private boolean shouldUseNavigation() {
		return IsPirateMob(enemy.getEnemyType()) || enemy.getEnemyType() == BOSS;
	}

	private boolean shouldRepath(Vector2 target) {
		if (repathCooldown > 0)
			return false;
		if (lastPathTarget == null)
			return true;
		return lastPathTarget.distanceTo(target) > Game.TILES_SIZE * 1.25f;
	}

	private void buildPath(NavigationGraph graph, Playing playing, Vector2 target) {
		NavigationNode start = graph.findNearestNode(enemy.getCenterX(), enemy.getCenterY());
		NavigationNode end = graph.findNearestNode(target.x(), target.y());
		long startNs = System.nanoTime();
		List<NavigationNode> path = pathfinder.findPath(graph, start, end, profile);
		float ms = (System.nanoTime() - startNs) / 1_000_000f;
		pathFollower.setPath(path, enemy);
		lastPathTarget = target;
		repathCooldown = 35;
		String event = path.isEmpty() ? "PATH_FAILED" : "PATH_BUILT";
		playing.getAiMetricsCollector().record(tick, enemy, event, state, path.size(), ms, end == null ? "" : "target=" + end.getId());
	}

	private void searchMove(int[][] lvlData) {
		if (stateTick % 55 == 0)
			enemy.changeWalkDirPublic();
		enemy.aiMove(lvlData, 0.65f);
	}

	private void retreatMove(int[][] lvlData, Playing playing) {
		NavigationGraph graph = playing.getLevelManager().getCurrentLevel().getNavigationGraph();
		Vector2 retreat = null;
		if (graph != null) {
			TacticalPoint point = graph.findNearestFreePoint(TacticalPointType.RETREAT_POINT, enemy.getCenterX(), enemy.getCenterY());
			if (point == null)
				point = graph.findNearestFreePoint(TacticalPointType.SAFE_POINT, enemy.getCenterX(), enemy.getCenterY());
			if (point != null)
				retreat = point.getPosition();
		}
		if (retreat == null) {
			float playerX = playing.getPlayer().getHitbox().x;
			retreat = new Vector2(enemy.getCenterX() + (enemy.getCenterX() < playerX ? -Game.TILES_SIZE * 3f : Game.TILES_SIZE * 3f), enemy.getCenterY());
		}
		moveToTarget(lvlData, playing, retreat, 1.15f);
	}

	private boolean isAtCurrentTarget() {
		if (currentTarget == null)
			return false;
		return currentTarget.distanceTo(enemy.getCenterX(), enemy.getCenterY()) < Game.TILES_SIZE * 0.55f;
	}

	private boolean isPlayerInAttackRange(Player player) {
		float px = player.getHitbox().x + player.getHitbox().width / 2f;
		float py = player.getHitbox().y + player.getHitbox().height / 2f;
		float dx = px - enemy.getCenterX();
		float dy = py - enemy.getCenterY();
		return Math.sqrt(dx * dx + dy * dy) <= enemy.getAttackDistance() * 1.25f && Math.abs(dy) < Game.TILES_SIZE * 1.1f;
	}

	private void callAllies(EnemyContext context, Vector2 position, float danger) {
		if (!profile.canCallAllies() || position == null || alertCooldown > 0)
			return;
		float radius = (profile.isCommander() ? 8f : 4.5f) * Game.TILES_SIZE;
		danger = Math.min(1f, danger + profile.getCooperation() * 0.2f);
		context.alertManager().sendAlert(enemy, position.x(), position.y(), radius, danger, 120, context.noiseManager());
		alertCooldown = profile.isCommander() ? 80 : 130;
		context.metrics().record(tick, enemy, "ALLY_ALERT_SENT", state, 0, 0, "radius=" + Math.round(radius));
	}

	private void changeState(EnemyAiState next, Playing playing, String details) {
		if (state == next)
			return;
		EnemyAiState previous = state;
		state = next;
		stateTick = 0;
		if (next != EnemyAiState.SEARCH)
			searchTick = 0;
		if (next != EnemyAiState.CHASE && next != EnemyAiState.INVESTIGATE && next != EnemyAiState.RETREAT)
			pathFollower.reset();
		if (playing != null)
			playing.getAiMetricsCollector().record(tick, enemy, "STATE_" + previous + "_TO_" + next, state, 0, 0, details);
	}

	public void onDamagedByPlayer(Player player, Playing playing) {
		float px = player.getHitbox().x + player.getHitbox().width / 2f;
		float py = player.getHitbox().y + player.getHitbox().height / 2f;
		damage.recordDamageSource(px, py);
		vision.forceSuspicion(100f);
	}

	public void onAttackAnimationFinished() {
		if (state == EnemyAiState.ATTACK)
			state = enemy.getHealthPercent() < 0.14f && profile.canRetreat() ? EnemyAiState.RETREAT : EnemyAiState.CHASE;
	}

	public void reset() {
		state = EnemyAiState.PATROL;
		selectedAction = EnemyAction.PATROL;
		stateTick = 0;
		searchTick = 0;
		tick = 0;
		alertCooldown = 0;
		repathCooldown = 0;
		currentTarget = null;
		lastPathTarget = null;
		lastNoiseTick = -100000;
		lastAlertTick = -100000;
		memory.reset();
		vision.reset();
		pathFollower.reset();
	}

	public EnemyAiState getState() {
		return state;
	}

	public EnemyAction getSelectedAction() {
		return selectedAction;
	}

	public float getSuspicionLevel() {
		return vision.getSuspicionLevel();
	}

	public float getMemoryConfidence() {
		return memory.getConfidence();
	}

	public Vector2 getLastKnownPlayerPosition() {
		return memory.getLastSeenPlayerPosition();
	}

	public String getCurrentTargetText() {
		if (currentTarget == null)
			return "-";
		NavigationNode node = pathFollower.getCurrentTarget();
		if (node != null)
			return currentTarget + " node=" + node.getId();
		return currentTarget.toString();
	}

	public List<NavigationNode> getCurrentPath() {
		if (pathFollower == null)
			return Collections.emptyList();
		return pathFollower.getPath();
	}

	public float getSightDistancePixels() {
		return profile.getSightDistance() * Game.TILES_SIZE;
	}

	public float getHearingRadiusPixels() {
		return Game.TILES_SIZE * 3f * profile.getHearingMultiplier();
	}

	public EnemyAiProfile getProfile() {
		return profile;
	}
}
