package com.kasper.sleeplessknight;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class ModEntities {
    public static final EntityType<DarkKnightEntity> DARK_KNIGHT = registerEntity(
            "dark_knight",
            EntityType.Builder.<DarkKnightEntity>of(DarkKnightEntity::new, MobCategory.MONSTER)
                    .sized(1.15F, 3.15F)
                    .clientTrackingRange(12)
    );

    private ModEntities() {}

    private static <T extends Entity> EntityType<T> registerEntity(String name, EntityType.Builder<T> builder) {
        ResourceKey<EntityType<?>> key = ResourceKey.create(
                Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(SleeplessKnight.MOD_ID, name)
        );
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
    }

    public static void register() {
        FabricDefaultAttributeRegistry.register(DARK_KNIGHT, DarkKnightEntity.createDarkKnightAttributes());
    }
}
