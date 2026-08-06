package ru.kasper.woodenwateringcan.interaction;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import ru.kasper.woodenwateringcan.WoodenWateringCanMod;

public final class WateringCanPickupHandler {
    private WateringCanPickupHandler() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            BlockPos pos = hitResult.getBlockPos();
            if (!player.isShiftKeyDown()
                    || !level.getBlockState(pos).is(WoodenWateringCanMod.WOODEN_WATERING_CAN_BLOCK)) {
                return InteractionResult.PASS;
            }

            if (!level.isClientSide()) {
                ItemStack pickedUp = new ItemStack(WoodenWateringCanMod.WOODEN_WATERING_CAN_ITEM);
                ItemStack held = player.getItemInHand(hand);

                if (held.isEmpty()) {
                    player.setItemInHand(hand, pickedUp);
                } else if (!player.addItem(pickedUp)) {
                    player.drop(pickedUp, false);
                }

                level.removeBlock(pos, false);
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.0F);
                player.swing(hand, true);
            }

            return InteractionResult.SUCCESS;
        });
    }
}
