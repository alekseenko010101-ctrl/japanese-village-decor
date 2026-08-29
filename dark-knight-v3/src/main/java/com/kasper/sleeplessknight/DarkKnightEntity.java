package com.kasper.sleeplessknight;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

public class DarkKnightEntity extends WitherSkeleton {
    private static final Identifier LIGHTNING_ID = Identifier.fromNamespaceAndPath("minecraft", "lightning_bolt");

    public DarkKnightEntity(EntityType<? extends WitherSkeleton> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // The greatsword is part of the custom animated model.
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            EntitySpawnReason spawnReason,
            @Nullable SpawnGroupData groupData
    ) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnReason, groupData);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(30.0);

        // Natural spawn and spawn egg both get the same dry lightning entrance.
        // The bolt is visual-only: thunder and flash, but no rain, fire or damage.
        if (level instanceof ServerLevel serverLevel) {
            EntityType<?> lightningType = BuiltInRegistries.ENTITY_TYPE.getValue(LIGHTNING_ID);
            if (lightningType != null) {
                Entity created = lightningType.create(serverLevel, EntitySpawnReason.TRIGGERED);
                if (created instanceof LightningBolt bolt) {
                    bolt.snapTo(this.getX(), this.getY(), this.getZ(), 0.0F, 0.0F);
                    bolt.setVisualOnly(true);
                    serverLevel.addFreshEntity(bolt);
                }
            }

            serverLevel.playSound(
                    null,
                    this.blockPosition(),
                    ModSounds.DARK_KNIGHT_APPEAR,
                    SoundSource.HOSTILE,
                    2.2F,
                    1.0F
            );
        }

        return data;
    }

    public static AttributeSupplier.Builder createDarkKnightAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.ATTACK_DAMAGE, 30.0)
                .add(Attributes.ARMOR, 20.0)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.18)
                .add(Attributes.FOLLOW_RANGE, 64.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }
}
