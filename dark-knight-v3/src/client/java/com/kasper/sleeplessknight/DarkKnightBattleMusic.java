package com.kasper.sleeplessknight;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Looping boss track with smooth fade in/out. */
public final class DarkKnightBattleMusic extends AbstractTickableSoundInstance {
    private static final float FADE_IN_PER_TICK = 0.025F;
    private static final float FADE_OUT_PER_TICK = 0.020F;
    private static final float MAX_VOLUME = 0.90F;

    private boolean active = true;

    public DarkKnightBattleMusic() {
        super(ModSounds.DARK_KNIGHT_BATTLE_MUSIC, SoundSource.HOSTILE, RandomSource.create());
        this.looping = true;
        this.delay = 0;
        // Keep this just above zero so the sound engine creates a channel immediately.
        // canStartSilent() below is the real safeguard for the fade-in start.
        this.volume = 0.001F;
        this.pitch = 1.0F;
        this.relative = true;
        this.attenuation = SoundInstance.Attenuation.NONE;
    }

    /**
     * Minecraft normally refuses to start a HOSTILE sound whose initial volume is zero.
     * This boss track intentionally begins silent and fades in, so it must be allowed
     * to create its audio channel while effectively silent.
     */
    @Override
    public boolean canStartSilent() {
        return true;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void tick() {
        if (this.active) {
            this.volume = Math.min(MAX_VOLUME, this.volume + FADE_IN_PER_TICK);
        } else {
            this.volume = Math.max(0.0F, this.volume - FADE_OUT_PER_TICK);
            if (this.volume <= 0.001F) {
                this.stop();
            }
        }
    }
}
