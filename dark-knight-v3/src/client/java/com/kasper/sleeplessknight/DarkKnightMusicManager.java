package com.kasper.sleeplessknight;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

import java.util.Comparator;
import java.util.UUID;

/**
 * Boss music follows line of sight only:
 * while the knight can see the local player, the track stays active continuously;
 * when line of sight is lost (or the knight disappears/dies), it fades out.
 */
public final class DarkKnightMusicManager {
    private static final double DETECTION_RANGE = 64.0;

    private static DarkKnightBattleMusic music;
    private static UUID trackedKnight;

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

        trackedKnight = knight.getUUID();
        boolean visible = knight.hasLineOfSight(player);

        if (visible) {
            // IMPORTANT: no combat timeout here. As long as the knight sees the
            // player, the fight track remains active for any amount of time.
            if (music == null || music.isStopped() || !minecraft.getSoundManager().isActive(music)) {
                music = new DarkKnightBattleMusic();
                minecraft.getSoundManager().play(music);
            }
            music.setActive(true);
        } else {
            // Lose sight -> smooth fade-out.
            if (music != null && !music.isStopped()) {
                music.setActive(false);
            }
            trackedKnight = null;
        }
    }

    private static void fadeOutAndReset() {
        if (music != null && !music.isStopped()) {
            music.setActive(false);
        }
        trackedKnight = null;
    }
}
