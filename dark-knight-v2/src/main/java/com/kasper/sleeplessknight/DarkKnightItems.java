package com.kasper.sleeplessknight;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

public final class DarkKnightItems {
    private static final ResourceKey<Item> SPAWN_EGG_KEY = ResourceKey.create(
            Registries.ITEM,
            Identifier.fromNamespaceAndPath(SleeplessKnight.MOD_ID, "dark_knight_spawn_egg")
    );

    public static final Item DARK_KNIGHT_SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().setId(SPAWN_EGG_KEY).spawnEgg(ModEntities.DARK_KNIGHT))
    );

    private DarkKnightItems() {}

    public static void register() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.SPAWN_EGGS).register(output ->
                output.accept(new ItemStack(DARK_KNIGHT_SPAWN_EGG), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)
        );
    }
}
