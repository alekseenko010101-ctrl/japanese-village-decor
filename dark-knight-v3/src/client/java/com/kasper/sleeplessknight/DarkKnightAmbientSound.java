package com.kasper.sleeplessknight;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Spatial looping sound that follows a living Dark Knight. */
public final class DarkKnightAmbientSound extends AbstractTickableSoundInstance {
    private final DarkKnightEntity knight;

    public DarkKnightAmbientSound(DarkKnightEntity knight) {
        super(ModSounds.DARK_KNIGHT_AMBIENT, SoundSource.HOSTILE, RandomSource.create());
        this.knight = knight;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.relative = false;
        this.x = knight.getX();
        this.y = knight.getY();
        this.z = knight.getZ();
    }

    public DarkKnightEntity knight() {
        return this.knight;
    }

    @Override
    public void tick() {
        if (!this.knight.isAlive() || this.knight.isRemoved()) {
            this.stop();
            return;
        }
        this.x = this.knight.getX();
        this.y = this.knight.getY();
        this.z = this.knight.getZ();
    }
}
