package com.kasper.sleeplessknight;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
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

/**
 * Dark Knight boss-like monster.
 *
 * The boss bar is deliberately combat-driven rather than permanently visible:
 * - when the knight has a player target, the bar appears;
 * - if aggro is lost, the same bar remains for 10 seconds;
 * - if combat resumes during that grace period, the timer is refreshed;
 * - after 10 seconds without aggro, the bar disappears.
 *
 * The bar has PLAY_BOSS_MUSIC enabled. The client music manager uses that flag,
 * so the custom fight track exists for exactly the same window as this bar.
 */
public class DarkKnightEntity extends WitherSkeleton {
    private static final Identifier LIGHTNING_ID = Identifier.fromNamespaceAndPath("minecraft", "lightning_bolt");
    private static final long COMBAT_GRACE_TICKS = 10L * 20L;

    private final ServerBossEvent bossEvent;
    private @Nullable ServerPlayer bossPlayer;
    private long lastCombatTick = Long.MIN_VALUE;

    public DarkKnightEntity(EntityType<? extends WitherSkeleton> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();

        this.bossEvent = new ServerBossEvent(
                this.getUUID(),
                Component.translatable("entity.sleepless_knight.dark_knight"),
                BossEvent.BossBarColor.PURPLE,
                BossEvent.BossBarOverlay.PROGRESS
        );
        this.bossEvent.setPlayBossMusic(true);
        this.bossEvent.setDarkenScreen(false);
        this.bossEvent.setCreateWorldFog(false);
        this.bossEvent.setVisible(true);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // The greatsword is part of the custom animated model.
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level() instanceof ServerLevel) {
            updateCombatBossBar();
        }
    }

    private void updateCombatBossBar() {
        long now = this.level().getGameTime();

        ServerPlayer currentTarget = this.getTarget() instanceof ServerPlayer player && player.isAlive()
                ? player
                : null;

        if (currentTarget != null) {
            lastCombatTick = now;

            if (bossPlayer != currentTarget) {
                if (bossPlayer != null) {
                    bossEvent.removePlayer(bossPlayer);
                }
                bossPlayer = currentTarget;
                bossEvent.addPlayer(currentTarget);
            }
        }

        boolean graceActive = lastCombatTick != Long.MIN_VALUE
                && now - lastCombatTick <= COMBAT_GRACE_TICKS;

        if (bossPlayer != null && (!bossPlayer.isAlive() || bossPlayer.isRemoved())) {
            bossEvent.removePlayer(bossPlayer);
            bossPlayer = null;
            lastCombatTick = Long.MIN_VALUE;
        } else if (!graceActive && currentTarget == null) {
            if (bossPlayer != null) {
                bossEvent.removePlayer(bossPlayer);
                bossPlayer = null;
            }
            lastCombatTick = Long.MIN_VALUE;
        }

        bossEvent.setName(this.getDisplayName());
        bossEvent.setProgress(Math.max(0.0F, Math.min(1.0F, this.getHealth() / this.getMaxHealth())));
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
