package dev.kasper.woodenbucket3d;

import dev.kasper.woodenbucket.WoodenBucketMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;

public final class WoodenBucketBottleMechanics implements ModInitializer {
    public static final Item WATER_BOTTLE = registerItem("wooden_water_bottle", 16);
    public static final Item MILK_BOTTLE = registerItem("wooden_milk_bottle", 16);
    public static final Item POWDER_SNOW_BOTTLE = registerItem("wooden_powder_snow_bottle", 16);

    public static final Item WOODEN_COD_BUCKET = registerItem("wooden_cod_bucket", 1);
    public static final Item WOODEN_SALMON_BUCKET = registerItem("wooden_salmon_bucket", 1);
    public static final Item WOODEN_PUFFERFISH_BUCKET = registerItem("wooden_pufferfish_bucket", 1);
    public static final Item WOODEN_TROPICAL_FISH_BUCKET = registerItem("wooden_tropical_fish_bucket", 1);

    private static final Block WATER_LEVEL_2 = registerBlock("wooden_water_bucket_decor_level_2");
    private static final Block WATER_LEVEL_1 = registerBlock("wooden_water_bucket_decor_level_1");
    private static final Block MILK_LEVEL_2 = registerBlock("wooden_milk_bucket_decor_level_2");
    private static final Block MILK_LEVEL_1 = registerBlock("wooden_milk_bucket_decor_level_1");
    private static final Block SNOW_LEVEL_2 = registerBlock("wooden_powder_snow_bucket_decor_level_2");
    private static final Block SNOW_LEVEL_1 = registerBlock("wooden_powder_snow_bucket_decor_level_1");

    public static final Item WATER_BUCKET_LEVEL_2 = registerItem("wooden_water_bucket_level_2", 1);
    public static final Item WATER_BUCKET_LEVEL_1 = registerItem("wooden_water_bucket_level_1", 1);
    public static final Item MILK_BUCKET_LEVEL_2 = registerItem("wooden_milk_bucket_level_2", 1);
    public static final Item MILK_BUCKET_LEVEL_1 = registerItem("wooden_milk_bucket_level_1", 1);
    public static final Item SNOW_BUCKET_LEVEL_2 = registerItem("wooden_powder_snow_bucket_level_2", 1);
    public static final Item SNOW_BUCKET_LEVEL_1 = registerItem("wooden_powder_snow_bucket_level_1", 1);

    @Override
    public void onInitialize() {
        UseBlockCallback.EVENT.register(WoodenBucketBottleMechanics::useBlock);
        System.out.println("[Wooden Bucket 3D] exact fish identity enabled (1.1.7)");
    }

    private static InteractionResult useBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        BlockPos pos = hit.getBlockPos();
        Block clicked = level.getBlockState(pos).getBlock();

        if (held.getItem() == Items.GLASS_BOTTLE) {
            BottleStep step = bottleStep(clicked);
            if (step != null) {
                if (!level.isClientSide()) {
                    exchangeOne(player, hand, new ItemStack(step.bottle));
                    level.setBlockAndUpdate(pos, step.nextBlock.defaultBlockState());
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    player.swing(hand, true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if (player.isShiftKeyDown()) {
            Item pickup = partialItemForBlock(clicked);
            if (pickup != null) {
                if (!level.isClientSide()) {
                    giveToHandOrInventory(player, hand, new ItemStack(pickup));
                    level.removeBlock(pos, false);
                    level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 1.0F);
                    player.swing(hand, true);
                }
                return InteractionResult.SUCCESS;
            }

            Block partialBlock = partialBlockForItem(held.getItem());
            if (partialBlock != null) {
                BlockPos placePos = level.getBlockState(pos).isAir() ? pos : pos.relative(hit.getDirection());
                if (!level.getBlockState(placePos).isAir()) {
                    return InteractionResult.PASS;
                }
                if (!level.isClientSide()) {
                    level.setBlockAndUpdate(placePos, partialBlock.defaultBlockState());
                    held.shrink(1);
                    player.swing(hand, true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        Item vanillaFishBucket = vanillaFishBucketForWooden(held.getItem());
        if (vanillaFishBucket != null && isWaterTarget(level, hit)) {
            if (!level.isClientSide()) {
                releaseFish(player, level, hand, held.copy(), new ItemStack(WoodenBucketMod.WOODEN_BUCKET), vanillaFishBucket);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    private static BottleStep bottleStep(Block block) {
        if (block == WoodenBucket3DMod.WATER_DECOR) return new BottleStep(WATER_BOTTLE, WATER_LEVEL_2);
        if (block == WATER_LEVEL_2) return new BottleStep(WATER_BOTTLE, WATER_LEVEL_1);
        if (block == WATER_LEVEL_1) return new BottleStep(WATER_BOTTLE, WoodenBucket3DMod.EMPTY_DECOR);

        if (block == WoodenBucket3DMod.MILK_DECOR) return new BottleStep(MILK_BOTTLE, MILK_LEVEL_2);
        if (block == MILK_LEVEL_2) return new BottleStep(MILK_BOTTLE, MILK_LEVEL_1);
        if (block == MILK_LEVEL_1) return new BottleStep(MILK_BOTTLE, WoodenBucket3DMod.EMPTY_DECOR);

        if (block == WoodenBucket3DMod.SNOW_DECOR) return new BottleStep(POWDER_SNOW_BOTTLE, SNOW_LEVEL_2);
        if (block == SNOW_LEVEL_2) return new BottleStep(POWDER_SNOW_BOTTLE, SNOW_LEVEL_1);
        if (block == SNOW_LEVEL_1) return new BottleStep(POWDER_SNOW_BOTTLE, WoodenBucket3DMod.EMPTY_DECOR);

        return null;
    }

    private static Item partialItemForBlock(Block block) {
        if (block == WATER_LEVEL_2) return WATER_BUCKET_LEVEL_2;
        if (block == WATER_LEVEL_1) return WATER_BUCKET_LEVEL_1;
        if (block == MILK_LEVEL_2) return MILK_BUCKET_LEVEL_2;
        if (block == MILK_LEVEL_1) return MILK_BUCKET_LEVEL_1;
        if (block == SNOW_LEVEL_2) return SNOW_BUCKET_LEVEL_2;
        if (block == SNOW_LEVEL_1) return SNOW_BUCKET_LEVEL_1;
        return null;
    }

    private static Block partialBlockForItem(Item item) {
        if (item == WATER_BUCKET_LEVEL_2) return WATER_LEVEL_2;
        if (item == WATER_BUCKET_LEVEL_1) return WATER_LEVEL_1;
        if (item == MILK_BUCKET_LEVEL_2) return MILK_LEVEL_2;
        if (item == MILK_BUCKET_LEVEL_1) return MILK_LEVEL_1;
        if (item == SNOW_BUCKET_LEVEL_2) return SNOW_LEVEL_2;
        if (item == SNOW_BUCKET_LEVEL_1) return SNOW_LEVEL_1;
        return null;
    }

    private static void giveToHandOrInventory(Player player, InteractionHand hand, ItemStack stack) {
        ItemStack inHand = player.getItemInHand(hand);
        if (inHand.isEmpty()) {
            player.setItemInHand(hand, stack);
        } else if (!player.addItem(stack)) {
            player.drop(stack, false);
        }
    }

    private static void exchangeOne(Player player, InteractionHand hand, ItemStack result) {
        ItemStack bottles = player.getItemInHand(hand);
        if (bottles.getCount() <= 1) {
            player.setItemInHand(hand, result);
        } else {
            bottles.shrink(1);
            if (!player.addItem(result)) {
                player.drop(result, false);
            }
        }
    }

    private static boolean isWaterTarget(Level level, BlockHitResult hit) {
        BlockPos pos = hit.getBlockPos();
        if (level.getFluidState(pos).is(FluidTags.WATER)) return true;
        return level.getFluidState(pos.relative(hit.getDirection())).is(FluidTags.WATER);
    }

    public static Item woodenFishBucketForVanilla(Item vanillaBucket) {
        if (vanillaBucket == Items.COD_BUCKET) return WOODEN_COD_BUCKET;
        if (vanillaBucket == Items.SALMON_BUCKET) return WOODEN_SALMON_BUCKET;
        if (vanillaBucket == Items.PUFFERFISH_BUCKET) return WOODEN_PUFFERFISH_BUCKET;
        if (vanillaBucket == Items.TROPICAL_FISH_BUCKET) return WOODEN_TROPICAL_FISH_BUCKET;
        return WoodenBucket3DMod.WOODEN_FISH_BUCKET;
    }

    private static Item vanillaFishBucketForWooden(Item woodenBucket) {
        if (woodenBucket == WOODEN_COD_BUCKET) return Items.COD_BUCKET;
        if (woodenBucket == WOODEN_SALMON_BUCKET) return Items.SALMON_BUCKET;
        if (woodenBucket == WOODEN_PUFFERFISH_BUCKET) return Items.PUFFERFISH_BUCKET;
        if (woodenBucket == WOODEN_TROPICAL_FISH_BUCKET) return Items.TROPICAL_FISH_BUCKET;
        if (woodenBucket == WoodenBucket3DMod.WOODEN_FISH_BUCKET) return Items.TROPICAL_FISH_BUCKET;
        return null;
    }

    private static void releaseFish(Player player, Level level, InteractionHand hand, ItemStack originalFishBucket, ItemStack emptyWoodenBucket, Item vanillaBucketItem) {
        ItemStack vanillaFishBucket = originalFishBucket.transmuteCopy(vanillaBucketItem, 1);
        player.setItemInHand(hand, vanillaFishBucket);
        InteractionResult result = vanillaBucketItem.use(level, player, hand);
        if (result == InteractionResult.PASS) {
            player.setItemInHand(hand, originalFishBucket);
        } else {
            player.setItemInHand(hand, emptyWoodenBucket);
        }
    }

    private static Item registerItem(String path, int stackSize) {
        Identifier id = Identifier.fromNamespaceAndPath("wooden_bucket", path);
        BlockItemId ids = BlockItemId.create(id, id);
        return Registry.register(
                BuiltInRegistries.ITEM,
                ids.item(),
                new Item(new Item.Properties().stacksTo(stackSize).setId(ids.item()))
        );
    }

    private static Block registerBlock(String path) {
        Identifier id = Identifier.fromNamespaceAndPath("wooden_bucket", path);
        BlockItemId ids = BlockItemId.create(id, id);
        return Registry.register(
                BuiltInRegistries.BLOCK,
                ids.block(),
                new DecorativeBucketBlock(
                        BlockBehaviour.Properties.of()
                                .strength(1.0F)
                                .sound(SoundType.WOOD)
                                .noOcclusion()
                                .setId(ids.block())
                )
        );
    }

    private record BottleStep(Item bottle, Block nextBlock) {}
}
