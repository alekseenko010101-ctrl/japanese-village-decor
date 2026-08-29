from pathlib import Path
import runpy

root = Path(__file__).resolve().parent

# Keep every 5.2.6 change first: sword grip, spawn egg, rewards and version bump.
runpy.run_path(str(root / "prepare_v526.py"), run_name="__main__")

entity = root / "src/main/java/com/kasper/sleeplessknight/DarkKnightEntity.java"
s = entity.read_text(encoding="utf-8")

# Fixed aggro radius. Once acquired, keep the same player a little farther out so
# the target does not flicker at the exact radius boundary.
s = s.replace(
    '    private static final long COMBAT_GRACE_TICKS = 10L * 20L;\n',
    '    private static final long COMBAT_GRACE_TICKS = 10L * 20L;\n'
    '    private static final double AGGRO_ACQUIRE_RADIUS = 64.0D;\n'
    '    private static final double AGGRO_RETAIN_RADIUS = 72.0D;\n'
    '    private static final double AGGRO_ACQUIRE_RADIUS_SQR = AGGRO_ACQUIRE_RADIUS * AGGRO_ACQUIRE_RADIUS;\n'
    '    private static final double AGGRO_RETAIN_RADIUS_SQR = AGGRO_RETAIN_RADIUS * AGGRO_RETAIN_RADIUS;\n',
    1,
)

old_tick = '''        if (this.level() instanceof ServerLevel) {\n            updateCombatBossBar();\n            updatePersistentChase();\n        }\n'''
new_tick = '''        if (this.level() instanceof ServerLevel serverLevel) {\n            maintainPlayerAggro(serverLevel);\n            updateCombatBossBar();\n            updatePersistentChase();\n        }\n'''
if old_tick not in s:
    raise RuntimeError("aiStep patch target not found")
s = s.replace(old_tick, new_tick, 1)

marker = '''    /**\n     * Keeps the knight actively walking toward an aggro target instead of\n'''
aggro_method = '''    /**\n     * Boss aggro is radius-driven instead of line-of-sight-driven.\n     * Once a player is acquired inside 64 blocks, short LOS breaks cannot make\n     * the knight forget them and stand still. The target is only released after\n     * leaving the 72-block retention radius, dying or leaving the dimension.\n     */\n    private void maintainPlayerAggro(ServerLevel level) {\n        ServerPlayer current = this.getTarget() instanceof ServerPlayer player ? player : null;\n\n        if (isValidChasePlayer(current, AGGRO_RETAIN_RADIUS_SQR)) {\n            return;\n        }\n\n        // bossPlayer survives the brief moments where vanilla target goals clear\n        // getTarget(), so immediately restore that target instead of idling.\n        if (isValidChasePlayer(bossPlayer, AGGRO_RETAIN_RADIUS_SQR)) {\n            this.setTarget(bossPlayer);\n            return;\n        }\n\n        ServerPlayer nearest = level.getEntitiesOfClass(\n                        ServerPlayer.class,\n                        this.getBoundingBox().inflate(AGGRO_ACQUIRE_RADIUS),\n                        player -> player.isAlive() && !player.isSpectator()\n                                && player.level() == this.level()\n                                && this.distanceToSqr(player) <= AGGRO_ACQUIRE_RADIUS_SQR\n                ).stream()\n                .min(java.util.Comparator.comparingDouble(this::distanceToSqr))\n                .orElse(null);\n\n        if (nearest != null) {\n            this.setTarget(nearest);\n        } else if (current != null) {\n            this.setTarget(null);\n        }\n    }\n\n    private boolean isValidChasePlayer(@Nullable ServerPlayer player, double maxDistanceSqr) {\n        return player != null\n                && player.isAlive()\n                && !player.isSpectator()\n                && player.level() == this.level()\n                && this.distanceToSqr(player) <= maxDistanceSqr;\n    }\n\n'''
if marker not in s:
    raise RuntimeError("aggro insertion point not found")
s = s.replace(marker, aggro_method + marker, 1)

# The old watchdog stopped helping at FOUR blocks, which is outside the real
# melee contact distance for this inherited skeleton hitbox. Keep chasing until
# the knight is actually close enough to attack.
s = s.replace(
    '        if (distanceSqr <= 16.0D) {',
    '        if (distanceSqr <= 5.25D) {',
    1,
)

# Re-path much more aggressively. If vanilla navigation drops a valid path in
# open terrain, direct MoveControl keeps the knight moving toward the target.
s = s.replace(
    '''        if (chaseRepathCooldown <= 0 || this.getNavigation().isDone()) {\n            this.getNavigation().moveTo(target, 1.0D);\n            chaseRepathCooldown = 10;\n        }\n''',
    '''        if (chaseRepathCooldown <= 0 || this.getNavigation().isDone()) {\n            boolean hasPath = this.getNavigation().moveTo(target, 1.0D);\n            chaseRepathCooldown = 4;\n            if (!hasPath || this.getNavigation().isDone()) {\n                this.getMoveControl().setWantedPosition(\n                        target.getX(), target.getY(), target.getZ(), 1.0D\n                );\n            }\n        }\n''',
    1,
)

# Faster stall detection: in open ground there is no reason to remain stationary
# for a full second while an aggro target exists.
s = s.replace(
    '            if (movedSqr < 0.01D) {',
    '            if (movedSqr < 0.04D) {',
    1,
)
s = s.replace(
    '        if (chaseStalledTicks >= 20) {',
    '        if (chaseStalledTicks >= 10) {',
    1,
)
s = s.replace(
    '''            boolean foundPath = this.getNavigation().moveTo(target, 1.0D);\n            if (!foundPath || this.horizontalCollision) {\n                this.getJumpControl().jump();\n                this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);\n            }\n''',
    '''            boolean foundPath = this.getNavigation().moveTo(target, 1.0D);\n            this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);\n            if (!foundPath || this.horizontalCollision) {\n                this.getJumpControl().jump();\n            }\n''',
    1,
)

entity.write_text(s, encoding="utf-8")

props = root / "gradle.properties"
s = props.read_text(encoding="utf-8")
s = s.replace("version=5.2.6", "version=5.2.7")
props.write_text(s, encoding="utf-8")

print("Prepared Sleepless Knight 5.2.7 persistent 64-block aggro chase")
