package ru.kasper.woodenwateringcan.interaction;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import ru.kasper.woodenwateringcan.WoodenWateringCanMod;
import ru.kasper.woodenwateringcan.item.WoodenWateringCanItem;

public final class WoodenBucketFillHandler {
    private WoodenBucketFillHandler() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register(WoodenBucketFillHandler::interact);
    }

    private static InteractionResult interact(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() != WoodenWateringCanMod.WOODEN_WATERING_CAN_ITEM) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        String blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        if (!isWoodenWaterBucket(blockId)) {
            return InteractionResult.PASS;
        }

        if (WoodenWateringCanItem.water(stack) >= WoodenWateringCanItem.MAX_WATER) {
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            WoodenWateringCanItem.setWater(stack, WoodenWateringCanItem.MAX_WATER);

            if (!replaceWithEmptyBucket(level, pos)) {
                WoodenWateringCanMod.LOGGER.warn("Could not replace wooden water bucket decor with empty bucket");
            }

            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.95F, 1.08F);
            if (level instanceof ServerLevel serverLevel) {
                spawnPickupParticles(serverLevel, pos);
            }
            player.swing(hand, true);
        }

        return InteractionResult.SUCCESS;
    }

    private static boolean isWoodenWaterBucket(String id) {
        return id.equals("wooden_bucket:wooden_water_bucket_decor")
                || id.equals("wooden_bucket:wooden_water_bucket_decor_level_2")
                || id.equals("wooden_bucket:wooden_water_bucket_decor_level_1");
    }

    private static boolean replaceWithEmptyBucket(Level level, BlockPos pos) {
        try {
            Class<?> mod = Class.forName("dev.kasper.woodenbucket3d.WoodenBucket3DMod");
            Object emptyDecor = mod.getField("EMPTY_DECOR").get(null);
            if (emptyDecor instanceof Block block) {
                return level.setBlock(pos, block.defaultBlockState(), 3);
            }
        } catch (Throwable error) {
            WoodenWateringCanMod.LOGGER.warn("Wooden Bucket integration failed", error);
        }
        return false;
    }

    private static void spawnPickupParticles(ServerLevel level, BlockPos pos) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.72;
        double z = pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.SPLASH, x, y, z, 24, 0.30, 0.12, 0.30, 0.08);
        level.sendParticles(ParticleTypes.FALLING_WATER, x, y + 0.20, z, 10, 0.24, 0.06, 0.24, 0.03);
    }
}
