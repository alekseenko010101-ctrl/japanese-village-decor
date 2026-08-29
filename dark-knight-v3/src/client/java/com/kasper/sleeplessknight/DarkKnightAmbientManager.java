package com.kasper.sleeplessknight;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Keeps the custom monster sound looping for every nearby Dark Knight. */
public final class DarkKnightAmbientManager {
    private static final double RANGE = 72.0;
    private static final Map<UUID, DarkKnightAmbientSound> SOUNDS = new HashMap<>();

    private DarkKnightAmbientManager() {}

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            stopAll();
            return;
        }

        AABB box = minecraft.player.getBoundingBox().inflate(RANGE);
        Set<UUID> present = new HashSet<>();

        for (DarkKnightEntity knight : minecraft.level.getEntitiesOfClass(DarkKnightEntity.class, box)) {
            if (!knight.isAlive()) continue;
            UUID id = knight.getUUID();
            present.add(id);
            DarkKnightAmbientSound sound = SOUNDS.get(id);
            if (sound == null || sound.isStopped()) {
                sound = new DarkKnightAmbientSound(knight);
                SOUNDS.put(id, sound);
                minecraft.getSoundManager().play(sound);
            }
        }

        SOUNDS.entrySet().removeIf(entry -> {
            if (present.contains(entry.getKey())) return false;
            minecraft.getSoundManager().stop(entry.getValue());
            return true;
        });
    }

    private static void stopAll() {
        Minecraft minecraft = Minecraft.getInstance();
        for (DarkKnightAmbientSound sound : SOUNDS.values()) {
            minecraft.getSoundManager().stop(sound);
        }
        SOUNDS.clear();
    }
}
