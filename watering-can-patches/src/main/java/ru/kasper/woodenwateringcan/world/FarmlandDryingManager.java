package ru.kasper.woodenwateringcan.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class FarmlandDryingManager {
    private static final long TICKS_PER_DAY = 24_000L;
    private static final Map<ServerLevel, Set<Long>> TRACKED = new HashMap<>();
    private static final Map<ServerLevel, Long> LAST_DAY_TIME = new HashMap<>();

    private FarmlandDryingManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_LEVEL_TICK.register(FarmlandDryingManager::tickWorld);
    }

    public static void track(ServerLevel level, BlockPos pos) {
        TRACKED.computeIfAbsent(level, ignored -> new HashSet<>()).add(pos.asLong());
    }

    private static void tickWorld(ServerLevel level) {
        long now = level.getOverworldClockTime();
        Long previous = LAST_DAY_TIME.put(level, now);
        if (previous == null) {
            return;
        }

        long previousDay = Math.floorDiv(previous, TICKS_PER_DAY);
        long currentDay = Math.floorDiv(now, TICKS_PER_DAY);
        if (currentDay > previousDay) {
            dryTrackedFarmland(level);
        }
    }

    private static void dryTrackedFarmland(ServerLevel level) {
        Set<Long> tracked = TRACKED.get(level);
        if (tracked == null || tracked.isEmpty()) {
            return;
        }

        Iterator<Long> iterator = tracked.iterator();
        while (iterator.hasNext()) {
            BlockPos pos = BlockPos.of(iterator.next());
            if (!level.isLoaded(pos)) {
                continue;
            }

            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.FARMLAND)) {
                iterator.remove();
                continue;
            }

            if (state.getValue(FarmlandBlock.MOISTURE) > 0) {
                level.setBlockAndUpdate(pos, state.setValue(FarmlandBlock.MOISTURE, 0));
            }
            iterator.remove();
        }
    }
}
