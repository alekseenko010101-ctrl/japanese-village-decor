package com.kasper.sleeplessknight;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.Identifier;

public final class DarkKnightRenderer extends HumanoidMobRenderer<DarkKnightEntity, SkeletonRenderState, DarkKnightModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            SleeplessKnight.MOD_ID,
            "textures/entity/dark_knight.png"
    );

    public DarkKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new DarkKnightModel(DarkKnightModel.createBodyLayer().bakeRoot()), 1.15F);
    }

    @Override
    public Identifier getTextureLocation(SkeletonRenderState state) {
        return TEXTURE;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    public void extractRenderState(DarkKnightEntity entity, SkeletonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
        state.isHoldingBow = false;
        state.isShaking = false;
    }

    @Override
    protected void scale(SkeletonRenderState state, PoseStack poseStack) {
        poseStack.scale(2.0F, 2.0F, 2.0F);
    }
}
