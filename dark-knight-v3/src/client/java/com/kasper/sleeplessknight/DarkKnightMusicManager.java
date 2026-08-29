package com.kasper.sleeplessknight;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.UUID;

/**
 * Starts boss music when the knight sees the local player.
 * Music fades if line of sight is lost, or if the knight receives no hit for 7 seconds.
 */
public final class DarkKnightMusicManager {
    private static final double DETECTION_RANGE = 64.0;
    private static final long COMBAT_TIMEOUT_TICKS = 7L * 20L;

    private static DarkKnightBattleMusic music;
    private static UUID trackedKnight;
    private static boolean wasVisible;
    private static long lastCombatActivity;
    private static int previousHurtTime;

    private DarkKnightMusicManager() {}

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            fadeOutAndReset();
            return;
        }

        var player = minecraft.player;
        AABB search = player.getBoundingBox().inflate(DETECTION_RANGE);
        DarkKnightEntity knight = minecraft.level.getEntitiesOfClass(DarkKnightEntity.class, search).stream()
                .filter(DarkKnightEntity::isAlive)
                .filter(k -> k.distanceToSqr(player) <= DETECTION_RANGE * DETECTION_RANGE)
                .min(Comparator.comparingDouble(k -> k.distanceToSqr(player)))
                .orElse(null);

        if (knight == null) {
            fadeOutAndReset();
            return;
        }

        boolean visible = knight.hasLineOfSight(player);
        long now = minecraft.level.getGameTime();
        UUID id = knight.getUUID();

        if (!id.equals(trackedKnight)) {
            trackedKnight = id;
            wasVisible = false;
            previousHurtTime = 0;
        }

        if (visible && !wasVisible) {
            lastCombatActivity = now;
        }

        if (knight.hurtTime > previousHurtTime) {
            lastCombatActivity = now;
        }
        previousHurtTime = knight.hurtTime;

        boolean wanted = visible && (now - lastCombatActivity <= COMBAT_TIMEOUT_TICKS);
        wasVisible = visible;

        if (wanted) {
            if (music == null || music.isStopped()) {
                music = new DarkKnightBattleMusic();
                minecraft.getSoundManager().play(music);
            }
            music.setActive(true);
        } else if (music != null && !music.isStopped()) {
            music.setActive(false);
        }

        if (!visible) {
            trackedKnight = null;
            previousHurtTime = 0;
        }
    }

    private static void fadeOutAndReset() {
        if (music != null && !music.isStopped()) {
            music.setActive(false);
        }
        trackedKnight = null;
        wasVisible = false;
        previousHurtTime = 0;
    }
}
