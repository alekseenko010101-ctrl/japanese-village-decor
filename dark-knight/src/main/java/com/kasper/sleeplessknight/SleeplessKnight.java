package com.kasper.sleeplessknight;

import net.fabricmc.api.ModInitializer;

public final class SleeplessKnight implements ModInitializer {
    public static final String MOD_ID = "sleepless_knight";

    @Override
    public void onInitialize() {
        ModEntities.register();
        KnightSpawner.register();
    }
}
