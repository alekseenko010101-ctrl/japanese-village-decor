package com.kasper.sleeplessknight;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** One-shot spatial combat cry that follows the Dark Knight while it plays. */
public final class DarkKnightCombatCrySound extends AbstractTickableSoundInstance {
    private static final int MAX_LIFETIME_TICKS = 7 * 20;

    private final DarkKnightEntity knight;
    private int age;

    public DarkKnightCombatCrySound(DarkKnightEntity knight) {
        super(ModSounds.DARK_KNIGHT_COMBAT_CRY, SoundSource.HOSTILE, RandomSource.create());
        this.knight = knight;
        this.looping = false;
        this.delay = 0;
        this.volume = 1.8F;
        this.pitch = 1.0F;
        this.relative = false;
        this.x = knight.getX();
        this.y = knight.getY();
        this.z = knight.getZ();
    }

    @Override
    public void tick() {
        if (!this.knight.isAlive() || this.knight.isRemoved() || ++this.age > MAX_LIFETIME_TICKS) {
            this.stop();
            return;
        }

        this.x = this.knight.getX();
        this.y = this.knight.getY();
        this.z = this.knight.getZ();
    }

    public void stopNow() {
        this.stop();
    }
}
