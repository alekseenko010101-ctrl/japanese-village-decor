package com.kasper.sleeplessknight;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Keeps the fight audio active while the Dark Knight is engaged.
 * A 10-second grace period prevents brief line-of-sight breaks from restarting it.
 * During an active fight the knight also lets out an occasional spatial combat cry.
 */
public final class DarkKnightMusicManager {
    private static final double DETECTION_RANGE = 96.0;
    private static final double PLAYER_HIT_CONFIRM_RANGE_SQR = 12.0 * 12.0;
    private static final long COMBAT_GRACE_TICKS = 10L * 20L;
    private static final long FIRST_CRY_MIN_TICKS = 2L * 20L;
    private static final long FIRST_CRY_MAX_TICKS = 5L * 20L;
    private static final long NEXT_CRY_MIN_TICKS = 9L * 20L;
    private static final long NEXT_CRY_MAX_TICKS = 18L * 20L;

    private static DarkKnightBattleMusic music;
    private static DarkKnightCombatCrySound combatCry;
    private static UUID trackedKnight;
    private static long lastFightActivity = Long.MIN_VALUE;
    private static long nextCryTick = Long.MIN_VALUE;
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
                nextCryTick = Long.MIN_VALUE;
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

            if (knight != null) {
                if (nextCryTick == Long.MIN_VALUE) {
                    nextCryTick = now + randomBetween(FIRST_CRY_MIN_TICKS, FIRST_CRY_MAX_TICKS);
                }

                if (now >= nextCryTick
                        && (combatCry == null || combatCry.isStopped() || !minecraft.getSoundManager().isActive(combatCry))) {
                    combatCry = new DarkKnightCombatCrySound(knight);
                    minecraft.getSoundManager().play(combatCry);
                    nextCryTick = now + randomBetween(NEXT_CRY_MIN_TICKS, NEXT_CRY_MAX_TICKS);
                }
            }
        } else {
            if (music != null && !music.isStopped()) {
                music.setActive(false);
            }
            if (combatCry != null && !combatCry.isStopped()) {
                combatCry.stopNow();
            }
            combatCry = null;
            nextCryTick = Long.MIN_VALUE;
            trackedKnight = null;
            previousKnightHurtTime = 0;
            previousPlayerHurtTime = 0;
        }
    }

    private static long randomBetween(long minInclusive, long maxInclusive) {
        return ThreadLocalRandom.current().nextLong(minInclusive, maxInclusive + 1L);
    }

    private static void stopAndReset() {
        if (music != null && !music.isStopped()) {
            music.setActive(false);
        }
        if (combatCry != null && !combatCry.isStopped()) {
            combatCry.stopNow();
        }
        combatCry = null;
        trackedKnight = null;
        lastFightActivity = Long.MIN_VALUE;
        nextCryTick = Long.MIN_VALUE;
        previousKnightHurtTime = 0;
        previousPlayerHurtTime = 0;
    }
}
