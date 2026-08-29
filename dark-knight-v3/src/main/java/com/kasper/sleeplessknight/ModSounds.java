package com.kasper.sleeplessknight;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    private static final Identifier AMBIENT_ID = Identifier.fromNamespaceAndPath(
            SleeplessKnight.MOD_ID, "dark_knight_ambient"
    );
    private static final Identifier BATTLE_MUSIC_ID = Identifier.fromNamespaceAndPath(
            SleeplessKnight.MOD_ID, "dark_knight_battle_music"
    );

    public static final SoundEvent DARK_KNIGHT_AMBIENT = Registry.register(
            BuiltInRegistries.SOUND_EVENT,
            AMBIENT_ID,
            SoundEvent.createVariableRangeEvent(AMBIENT_ID)
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
