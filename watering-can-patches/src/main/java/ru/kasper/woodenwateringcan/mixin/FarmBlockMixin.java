package ru.kasper.woodenwateringcan.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

import ru.kasper.woodenwateringcan.world.FarmlandDryingManager;

@Mixin(FarmlandBlock.class)
public abstract class FarmBlockMixin {
    @Inject(method = "turnToDirt", at = @At("HEAD"), cancellable = true)
    private static void woodenWateringCan$preventTrampling(
            @Nullable Entity entity,
            BlockState state,
            Level level,
            BlockPos pos,
            CallbackInfo ci
    ) {
        if (entity != null) {
            ci.cancel();
        }
    }

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void woodenWateringCan$manualMoisture(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci
    ) {
        if (state.getValue(FarmlandBlock.MOISTURE) > 0) {
            FarmlandDryingManager.track(level, pos);
        }
        ci.cancel();
    }
}
