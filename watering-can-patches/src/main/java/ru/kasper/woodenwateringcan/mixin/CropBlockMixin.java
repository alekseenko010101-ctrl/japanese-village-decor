package ru.kasper.woodenwateringcan.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {
    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void woodenWateringCan$requireMoisture(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo ci
    ) {
        BlockState below = level.getBlockState(pos.below());
        if (below.hasProperty(FarmlandBlock.MOISTURE) && below.getValue(FarmlandBlock.MOISTURE) == 0) {
            ci.cancel();
        }
    }
}
