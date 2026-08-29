package com.kasper.sleeplessknight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;

public final class SleeplessKnightClient implements ClientModInitializer {
    public static final ModelLayerLocation DARK_KNIGHT_LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(SleeplessKnight.MOD_ID, "dark_knight"),
            "main"
    );

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(DARK_KNIGHT_LAYER, DarkKnightModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.DARK_KNIGHT, DarkKnightRenderer::new);
    }
}
