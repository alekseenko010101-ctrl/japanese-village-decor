package com.kasper.sleeplessknight;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private static final Identifier APPEAR_ID = Identifier.fromNamespaceAndPath(
            SleeplessKnight.MOD_ID, "dark_knight_appear"
    );
    private static final Identifier BATTLE_MUSIC_ID = Identifier.fromNamespaceAndPath(
            SleeplessKnight.MOD_ID, "dark_knight_battle_music"
    );

    public static final SoundEvent DARK_KNIGHT_APPEAR = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            APPEAR_ID,
            SoundEvent.createVariableRangeEvent(APPEAR_ID)
    );

    public static final SoundEvent DARK_KNIGHT_BATTLE_MUSIC = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            BATTLE_MUSIC_ID,
            SoundEvent.createVariableRangeEvent(BATTLE_MUSIC_ID)
    );

    private ModSounds() {}

    public static void register() {
    }
}
