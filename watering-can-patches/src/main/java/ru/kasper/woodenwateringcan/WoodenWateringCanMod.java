package ru.kasper.woodenwateringcan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import ru.kasper.woodenwateringcan.block.WoodenWateringCanBlock;
import ru.kasper.woodenwateringcan.component.ModComponents;
import ru.kasper.woodenwateringcan.item.WoodenWateringCanItem;
import ru.kasper.woodenwateringcan.interaction.WateringCanPickupHandler;
import ru.kasper.woodenwateringcan.world.FarmlandDryingManager;

public final class WoodenWateringCanMod implements ModInitializer {
    public static final String MOD_ID = "wooden_watering_can";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier WATERING_CAN_IDENTIFIER = id("wooden_watering_can");
    public static final BlockItemId WATERING_CAN_ID = BlockItemId.create(
            WATERING_CAN_IDENTIFIER,
            WATERING_CAN_IDENTIFIER
    );

    public static final WoodenWateringCanBlock WOODEN_WATERING_CAN_BLOCK = Registry.register(
            BuiltInRegistries.BLOCK,
            WATERING_CAN_ID.block(),
            new WoodenWateringCanBlock(
                    BlockBehaviour.Properties.of()
                            .strength(1.2F)
                            .sound(SoundType.WOOD)
                            .noOcclusion()
                            .setId(WATERING_CAN_ID.block())
            )
    );

    public static final WoodenWateringCanItem WOODEN_WATERING_CAN_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            WATERING_CAN_ID.item(),
            new WoodenWateringCanItem(
                    WOODEN_WATERING_CAN_BLOCK,
                    new Item.Properties()
                            .useBlockDescriptionPrefix()
                            .stacksTo(1)
                            .component(ModComponents.WATER, 0)
                            .setId(WATERING_CAN_ID.item())
            )
    );

    @Override
    public void onInitialize() {
        ModComponents.initialize();
        FarmlandDryingManager.initialize();
        WateringCanPickupHandler.initialize();

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> output.accept(WOODEN_WATERING_CAN_ITEM));

        LOGGER.info("Wooden Watering Can initialized for Minecraft 26.2");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
