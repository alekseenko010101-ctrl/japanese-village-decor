package com.kasper.sleeplessknight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;

public final class SleeplessKnightClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DARK_KNIGHT, WitherSkeletonRenderer::new);
    }
}
