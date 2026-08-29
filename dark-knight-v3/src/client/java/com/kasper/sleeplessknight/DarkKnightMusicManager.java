package com.kasper.sleeplessknight;

import com.kasper.sleeplessknight.mixin.GuiBossOverlayAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

/**
 * The custom track follows the Dark Knight boss bar.
 *
 * Server-side combat decides when the bar exists. The bar itself has
 * PLAY_BOSS_MUSIC enabled, so the client uses that exact same state:
 * bar present -> music active; bar gone -> smooth fade-out.
 */
public final class DarkKnightMusicManager {
    private static final double KNIGHT_NEARBY_RANGE = 96.0;

    private static DarkKnightBattleMusic music;

    private DarkKnightMusicManager() {}

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            fadeOut();
            return;
        }

        var player = minecraft.player;
        AABB search = player.getBoundingBox().inflate(KNIGHT_NEARBY_RANGE);

        boolean knightNearby = minecraft.level.getEntitiesOfClass(DarkKnightEntity.class, search).stream()
                .anyMatch(k -> k.isAlive()
                        && k.distanceToSqr(player) <= KNIGHT_NEARBY_RANGE * KNIGHT_NEARBY_RANGE);

        boolean bossBarCombatActive = knightNearby
                && ((GuiBossOverlayAccessor) minecraft.gui)
                .sleeplessKnight$getBossOverlay()
                .shouldPlayMusic();

        if (bossBarCombatActive) {
            if (music == null || music.isStopped() || !minecraft.getSoundManager().isActive(music)) {
                music = new DarkKnightBattleMusic();
                minecraft.getSoundManager().play(music);
            }
            music.setActive(true);
        } else {
            fadeOut();
        }
    }

    private static void fadeOut() {
        if (music != null && !music.isStopped()) {
            music.setActive(false);
        }
    }
}
