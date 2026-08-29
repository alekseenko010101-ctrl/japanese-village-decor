package com.kasper.sleeplessknight;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class KnightSpawner {
    private static final Map<UUID, Long> LAST_ATTEMPT_NIGHT = new HashMap<>();
    private static int ticker = 0;

    private KnightSpawner() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (++ticker < 200) return;
            ticker = 0;

            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                trySpawnFor(player);
            }
        });
    }

    private static void trySpawnFor(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;
        if (!level.isNight()) return;

        int timeSinceRest = player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
        if (timeSinceRest < 120000) return;

        long day = level.getDayTime() / 24000L;
        UUID id = player.getUUID();
        if (LAST_ATTEMPT_NIGHT.getOrDefault(id, Long.MIN_VALUE) == day) return;
        LAST_ATTEMPT_NIGHT.put(id, day);

        if (!level.getEntitiesOfClass(DarkKnightEntity.class, player.getBoundingBox().inflate(128.0)).isEmpty()) return;
        if (level.random.nextFloat() > 0.08f) return;

        double angle = level.random.nextDouble() * Math.PI * 2.0;
        double distance = 25.0 + level.random.nextDouble() * 20.0;
        int x = (int)Math.floor(player.getX() + Math.cos(angle) * distance);
        int z = (int)Math.floor(player.getZ() + Math.sin(angle) * distance);
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, player.blockPosition().getY(), z));

        DarkKnightEntity knight = ModEntities.DARK_KNIGHT.create(level, null);
        if (knight == null) return;
        knight.moveTo(top.getX() + 0.5, top.getY(), top.getZ() + 0.5, level.random.nextFloat() * 360.0f, 0.0f);
        if (!level.noCollision(knight)) return;

        level.addFreshEntity(knight);
    }
}
