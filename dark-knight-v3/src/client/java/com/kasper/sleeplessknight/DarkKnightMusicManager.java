package com.kasper.sleeplessknight;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.UUID;

/**
 * Boss music starts when the knight engages the local player and stays continuous.
 * If aggro/line of sight is lost, a 10-second combat grace period keeps the same
 * track playing so brief LOS breaks do not restart the music. Hits during that
 * grace period refresh the timer. After 10 seconds without aggro or combat activity,
 * the music fades out smoothly.
 */
public final class DarkKnightMusicManager {
    private static final double DETECTION_RANGE = 64.0;
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

            // In this mob's AI, seeing the player is the moment it engages and starts
            // pursuing. While this is true the fight is unquestionably active.
            boolean aggroVisible = knight.hasLineOfSight(player);
            if (aggroVisible) {
                lastFightActivity = now;
            }

            // Player hitting the knight keeps combat alive even if LOS is temporarily
            // broken around a wall/tree/corner.
            if (knight.hurtTime > previousKnightHurtTime) {
                lastFightActivity = now;
            }
            previousKnightHurtTime = knight.hurtTime;

            // If the player gets hit while close to the tracked knight, count that as
            // ongoing combat too. This prevents a fight from going silent during a
            // brief visual obstruction while the knight is still landing attacks.
            if (player.hurtTime > previousPlayerHurtTime
                    && knight.distanceToSqr(player) <= PLAYER_HIT_CONFIRM_RANGE_SQR) {
                lastFightActivity = now;
            }
            previousPlayerHurtTime = player.hurtTime;
        }

        boolean fightStillWarm = lastFightActivity != Long.MIN_VALUE
                && now - lastFightActivity <= COMBAT_GRACE_TICKS;

        if (fightStillWarm) {
            // Reuse one looping sound instance. Brief LOS/aggro drops never restart
            // the track from the beginning; they only run down the 10-second grace.
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
