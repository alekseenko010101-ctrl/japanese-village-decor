package com.kasper.sleeplessknight;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.UUID;

/**
 * Keeps the fight audio active while the Dark Knight is engaged.
 * A 10-second grace period prevents brief line-of-sight breaks from restarting it.
 */
public final class DarkKnightMusicManager {
    private static final double DETECTION_RANGE = 96.0;
    private static final double PLAYER_HIT_CONFIRM_RANGE_SQR = 12.0 * 12.0;
    private static final long COMBAT_GRACE_TICKS = 10L * 20L;

    private static DarkKnightBattleMusic music;
    private static UUID trackedKnight;
    private static long lastFightActivity = Long.MIN_VALUE;
    private static int previousKnightHurtTime;
    private static int previousPlayerHurtTime;

    private DarkKnightMusicManager() {}

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            stopAndReset();
            return;
        }

        var player = minecraft.player;
        long now = minecraft.level.getGameTime();
        AABB search = player.getBoundingBox().inflate(DETECTION_RANGE);

        DarkKnightEntity knight = minecraft.level.getEntitiesOfClass(DarkKnightEntity.class, search).stream()
                .filter(DarkKnightEntity::isAlive)
                .filter(k -> k.distanceToSqr(player) <= DETECTION_RANGE * DETECTION_RANGE)
                .min(Comparator.comparingDouble(k -> k.distanceToSqr(player)))
                .orElse(null);

        if (knight != null) {
            UUID id = knight.getUUID();
            if (!id.equals(trackedKnight)) {
                trackedKnight = id;
                previousKnightHurtTime = knight.hurtTime;
                previousPlayerHurtTime = player.hurtTime;
            }

            if (knight.hasLineOfSight(player)) {
                lastFightActivity = now;
            }

            if (knight.hurtTime > previousKnightHurtTime) {
                lastFightActivity = now;
            }
            previousKnightHurtTime = knight.hurtTime;

            if (player.hurtTime > previousPlayerHurtTime
                    && knight.distanceToSqr(player) <= PLAYER_HIT_CONFIRM_RANGE_SQR) {
                lastFightActivity = now;
            }
            previousPlayerHurtTime = player.hurtTime;
        }

        boolean fightStillWarm = lastFightActivity != Long.MIN_VALUE
                && now - lastFightActivity <= COMBAT_GRACE_TICKS;

        if (fightStillWarm) {
            if (music == null || music.isStopped() || !minecraft.getSoundManager().isActive(music)) {
                music = new DarkKnightBattleMusic();
                minecraft.getSoundManager().play(music);
            }
            music.setActive(true);
        } else {
            if (music != null && !music.isStopped()) {
                music.setActive(false);
            }
            trackedKnight = null;
            previousKnightHurtTime = 0;
            previousPlayerHurtTime = 0;
        }
    }

    private static void stopAndReset() {
        if (music != null && !music.isStopped()) {
            music.setActive(false);
        }
        trackedKnight = null;
        lastFightActivity = Long.MIN_VALUE;
        previousKnightHurtTime = 0;
        previousPlayerHurtTime = 0;
    }
}
