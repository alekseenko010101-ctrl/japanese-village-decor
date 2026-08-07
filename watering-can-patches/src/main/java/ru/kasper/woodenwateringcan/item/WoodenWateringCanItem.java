package ru.kasper.woodenwateringcan.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import ru.kasper.woodenwateringcan.component.ModComponents;
import ru.kasper.woodenwateringcan.world.FarmlandDryingManager;

public final class WoodenWateringCanItem extends BlockItem {
    public static final int MAX_WATER = 5;

    public WoodenWateringCanItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        FluidState fluid = level.getFluidState(pos);
        if (!fluid.is(FluidTags.WATER) || !fluid.isSource()) {
            return InteractionResult.PASS;
        }

        if (water(stack) >= MAX_WATER) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.wooden_watering_can.full"));
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            setWater(stack, MAX_WATER);
            level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.PLAYERS, 0.9F, 1.15F);
            player.swing(hand, true);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            return super.useOn(context);
        }

        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);
        ItemStack stack = context.getItemInHand();
        int currentWater = water(stack);

        // Watering a grass block behaves like using bone meal on it.
        if (clickedState.is(Blocks.GRASS_BLOCK)) {
            if (currentWater <= 0) {
                if (!level.isClientSide()) {
                    player.sendOverlayMessage(Component.translatable("message.wooden_watering_can.empty"));
                }
                return InteractionResult.SUCCESS;
            }

            if (!level.isClientSide()) {
                ItemStack fakeBoneMeal = new ItemStack(Items.BONE_MEAL);
                if (BoneMealItem.growCrop(fakeBoneMeal, level, clickedPos)) {
                    setWater(stack, currentWater - 1);
                    level.playSound(null, clickedPos, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.7F, 1.35F);
                    level.levelEvent(1505, clickedPos, 15);
                    spawnWateringParticles((ServerLevel) level, clickedPos, Direction.UP);
                    player.swing(context.getHand(), true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Clicking farmland directly or clicking a crop/block standing on farmland waters the farmland below.
        BlockPos farmlandPos = null;
        BlockState farmlandState = null;
        if (clickedState.is(Blocks.FARMLAND)) {
            farmlandPos = clickedPos;
            farmlandState = clickedState;
        } else {
            BlockPos below = clickedPos.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.is(Blocks.FARMLAND)) {
                farmlandPos = below;
                farmlandState = belowState;
            }
        }

        if (farmlandPos == null) {
            return InteractionResult.PASS;
        }

        if (currentWater <= 0) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.wooden_watering_can.empty"));
            }
            return InteractionResult.SUCCESS;
        }

        int moisture = farmlandState.getValue(FarmlandBlock.MOISTURE);
        if (moisture >= 7) {
            if (!level.isClientSide()) {
                player.sendOverlayMessage(Component.translatable("message.wooden_watering_can.already_wet"));
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            level.setBlockAndUpdate(farmlandPos, farmlandState.setValue(FarmlandBlock.MOISTURE, 7));
            FarmlandDryingManager.track((ServerLevel) level, farmlandPos);
            setWater(stack, currentWater - 1);
            level.playSound(null, farmlandPos, SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS, 0.75F, 1.3F);
            spawnWateringParticles((ServerLevel) level, farmlandPos, Direction.UP);
            player.swing(context.getHand(), true);
        }
        return InteractionResult.SUCCESS;
    }

    public static void spawnWateringParticles(ServerLevel level, BlockPos pos, Direction face) {
        double x = pos.getX() + 0.5 + face.getStepX() * 0.12;
        double y = pos.getY() + 1.08;
        double z = pos.getZ() + 0.5 + face.getStepZ() * 0.12;
        level.sendParticles(ParticleTypes.SPLASH, x, y, z, 18, 0.34, 0.08, 0.34, 0.08);
        level.sendParticles(ParticleTypes.FALLING_WATER, x, y + 0.18, z, 8, 0.28, 0.05, 0.28, 0.02);
    }

    public static int water(ItemStack stack) {
        return Math.max(0, Math.min(MAX_WATER, stack.getOrDefault(ModComponents.WATER, 0)));
    }

    public static void setWater(ItemStack stack, int amount) {
        stack.set(ModComponents.WATER, Math.max(0, Math.min(MAX_WATER, amount)));
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay displayComponent,
            Consumer<Component> textConsumer,
            TooltipFlag type
    ) {
        textConsumer.accept(Component.translatable(
                "tooltip.wooden_watering_can.water",
                water(stack),
                MAX_WATER
        ).withStyle(ChatFormatting.AQUA));
        textConsumer.accept(Component.translatable("tooltip.wooden_watering_can.fill").withStyle(ChatFormatting.GRAY));
        textConsumer.accept(Component.translatable("tooltip.wooden_watering_can.water_farmland").withStyle(ChatFormatting.GRAY));
        textConsumer.accept(Component.translatable("tooltip.wooden_watering_can.place").withStyle(ChatFormatting.DARK_GRAY));
    }
}
