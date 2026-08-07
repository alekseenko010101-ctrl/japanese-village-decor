package dev.kasper.woodenbucket3d;

import dev.kasper.woodenbucket.WoodenBucketMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.fish.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public final class WoodenBucketEntityMechanics implements ModInitializer {
    @Override
    public void onInitialize() {
        UseEntityCallback.EVENT.register(WoodenBucketEntityMechanics::interactWithEntity);
    }

    private static InteractionResult interactWithEntity(
            Player player,
            Level level,
            InteractionHand hand,
            Entity entity,
            EntityHitResult hitResult
    ) {
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() != WoodenBucketMod.WOODEN_BUCKET) {
            return InteractionResult.PASS;
        }

        if (entity instanceof Cow cow && !cow.isBaby()) {
            if (!level.isClientSide()) {
                player.setItemInHand(hand, new ItemStack(WoodenBucket3DMod.WOODEN_MILK_BUCKET));
                level.playSound(null, cow.blockPosition(), SoundEvents.COW_MILK, SoundSource.PLAYERS, 1.0F, 1.0F);
                player.swing(hand, true);
            }
            return InteractionResult.SUCCESS;
        }

        if (entity instanceof AbstractFish fish && fish.isAlive()) {
            if (!level.isClientSide()) {
                ItemStack vanillaFishBucket = fish.getBucketItemStack();
                fish.saveToBucketTag(vanillaFishBucket);
                Item woodenItem = WoodenBucketBottleMechanics.woodenFishBucketForVanilla(vanillaFishBucket.getItem());
                ItemStack woodenFishBucket = vanillaFishBucket.transmuteCopy(woodenItem, 1);
                player.setItemInHand(hand, woodenFishBucket);
                level.playSound(null, fish.blockPosition(), SoundEvents.BUCKET_FILL_FISH, SoundSource.PLAYERS, 1.0F, 1.0F);
                fish.discard();
                player.swing(hand, true);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
