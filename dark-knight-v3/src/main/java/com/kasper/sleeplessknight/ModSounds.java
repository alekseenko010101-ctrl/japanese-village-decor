package com.kasper.sleeplessknight;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private static final Identifier APPEAR_ID = Identifier.fromNamespaceAndPath(
            SleeplessKnight.MOD_ID, "dark_knight_appear"
    );

    public static final SoundEvent DARK_KNIGHT_APPEAR = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            APPEAR_ID,
            SoundEvent.createVariableRangeEvent(APPEAR_ID)
    );

    private ModSounds() {}

    public static void register() {
    }
}
