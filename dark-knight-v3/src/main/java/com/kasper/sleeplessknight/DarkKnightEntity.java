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
import net.minecraft.world.damagesource.DamageSource;
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

/** Dark Knight boss-like monster. */
public class DarkKnightEntity extends WitherSkeleton {
    private static final Identifier LIGHTNING_ID = Identifier.fromNamespaceAndPath("minecraft", "lightning_bolt");
    private static final long COMBAT_GRACE_TICKS = 10L * 20L;

    private final ServerBossEvent bossEvent;
    private @Nullable ServerPlayer bossPlayer;
    private long lastCombatTick = Long.MIN_VALUE;

    // Extra pursuit watchdog. Vanilla melee navigation occasionally gives up on a
    // path for a few seconds around corners, water edges and small obstacles.
    // We only intervene while the target is clearly outside melee range.
    private int chaseRepathCooldown;
    private int chaseSampleTicks;
    private int chaseStalledTicks;
    private double lastChaseX;
    private double lastChaseZ;

    public DarkKnightEntity(EntityType<? extends WitherSkeleton> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.lastChaseX = this.getX();
        this.lastChaseZ = this.getZ();

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
    public void die(DamageSource damageSource) {
        // The entity stops ticking shortly after death, so waiting for aiStep()
        // leaves the last boss bar packet on the client. Remove it immediately.
        bossEvent.removeAllPlayers();
        bossPlayer = null;
        lastCombatTick = Long.MIN_VALUE;
        super.die(damageSource);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level() instanceof ServerLevel) {
            updateCombatBossBar();
            updatePersistentChase();
        }
    }

    /**
     * Keeps the knight actively walking toward an aggro target instead of
     * occasionally freezing after vanilla pathfinding abandons a path.
     * This does not teleport him and does not speed up normal movement.
     */
    private void updatePersistentChase() {
        if (!(this.getTarget() instanceof ServerPlayer target) || !target.isAlive()) {
            resetChaseWatchdog();
            return;
        }

        double distanceSqr = this.distanceToSqr(target);

        // Close enough for the normal melee attack goal. Do not fight with its
        // attack positioning or make the knight jitter around the player.
        if (distanceSqr <= 16.0D) {
            chaseStalledTicks = 0;
            chaseSampleTicks = 0;
            lastChaseX = this.getX();
            lastChaseZ = this.getZ();
            return;
        }

        if (chaseRepathCooldown > 0) {
            chaseRepathCooldown--;
        }

        // Refresh the path regularly, and immediately if vanilla navigation has
        // already decided that its path is finished.
        if (chaseRepathCooldown <= 0 || this.getNavigation().isDone()) {
            this.getNavigation().moveTo(target, 1.0D);
            chaseRepathCooldown = 10;
        }

        chaseSampleTicks++;
        if (chaseSampleTicks >= 10) {
            double dx = this.getX() - lastChaseX;
            double dz = this.getZ() - lastChaseZ;
            double movedSqr = dx * dx + dz * dz;

            if (movedSqr < 0.01D) {
                chaseStalledTicks += chaseSampleTicks;
            } else {
                chaseStalledTicks = 0;
            }

            lastChaseX = this.getX();
            lastChaseZ = this.getZ();
            chaseSampleTicks = 0;
        }

        // If he has wanted to chase for about a second but has barely moved,
        // throw away the stale path and force a fresh pursuit. A small jump is
        // enough to clear common one-block lips without turning him into a
        // jumping mob.
        if (chaseStalledTicks >= 20) {
            this.getNavigation().stop();
            boolean foundPath = this.getNavigation().moveTo(target, 1.0D);
            if (!foundPath || this.horizontalCollision) {
                this.getJumpControl().jump();
                this.getMoveControl().setWantedPosition(target.getX(), target.getY(), target.getZ(), 1.0D);
            }
            chaseStalledTicks = 0;
            chaseRepathCooldown = 5;
        }
    }

    private void resetChaseWatchdog() {
        chaseRepathCooldown = 0;
        chaseSampleTicks = 0;
        chaseStalledTicks = 0;
        lastChaseX = this.getX();
        lastChaseZ = this.getZ();
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
