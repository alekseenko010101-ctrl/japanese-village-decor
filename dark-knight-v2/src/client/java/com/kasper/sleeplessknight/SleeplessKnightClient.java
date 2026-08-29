package com.kasper.sleeplessknight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public final class SleeplessKnightClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DARK_KNIGHT, DarkKnightRenderer::new);
    }
}
