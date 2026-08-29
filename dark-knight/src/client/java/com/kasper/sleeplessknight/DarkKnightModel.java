package com.kasper.sleeplessknight;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.monster.skeleton.SkeletonModel;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.util.Mth;

public final class DarkKnightModel extends SkeletonModel<SkeletonRenderState> {
    private final ModelPart sword;

    public DarkKnightModel(ModelPart root) {
        super(root);
        this.sword = this.rightArm.getChild("greatsword");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -10.0F, -4.5F, 9.0F, 10.0F, 9.0F)
                        .texOffs(40, 0).addBox(-4.0F, -3.0F, -4.8F, 8.0F, 3.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F)
        );
        head.addOrReplaceChild(
                "helmet_flare",
                CubeListBuilder.create().texOffs(72, 0).addBox(-6.5F, -1.0F, -5.0F, 13.0F, 2.0F, 10.0F),
                PartPose.offset(0.0F, -10.0F, 0.0F)
        );
        head.addOrReplaceChild(
                "visor",
                CubeListBuilder.create().texOffs(120, 0).addBox(-3.4F, -1.0F, -0.075F, 6.8F, 2.0F, 0.15F),
                PartPose.offset(0.0F, -5.2F, -4.62F)
        );
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-5.0F, 0.0F, -3.0F, 10.0F, 12.0F, 6.0F)
                        .texOffs(40, 48).addBox(-6.0F, -0.5F, -3.5F, 12.0F, 2.0F, 7.0F)
                        .texOffs(80, 48).addBox(-5.5F, 10.2F, -3.4F, 11.0F, 2.0F, 6.8F),
                PartPose.ZERO
        );
        body.addOrReplaceChild(
                "chain_skirt",
                CubeListBuilder.create().texOffs(0, 80).addBox(-4.8F, 0.0F, -2.7F, 9.6F, 7.0F, 5.4F),
                PartPose.offset(0.0F, 12.0F, 0.0F)
        );
        body.addOrReplaceChild(
                "right_tasset",
                CubeListBuilder.create().texOffs(40, 80).addBox(-3.8F, 0.0F, -1.0F, 3.8F, 7.5F, 2.0F),
                PartPose.offset(-1.0F, 11.5F, -3.0F)
        );
        body.addOrReplaceChild(
                "left_tasset",
                CubeListBuilder.create().texOffs(56, 80).addBox(0.0F, 0.0F, -1.0F, 3.8F, 7.5F, 2.0F),
                PartPose.offset(1.0F, 11.5F, -3.0F)
        );
        body.addOrReplaceChild(
                "rune_vertical",
                CubeListBuilder.create().texOffs(192, 0).addBox(-0.55F, -3.0F, -0.06F, 1.1F, 6.0F, 0.12F),
                PartPose.offset(0.0F, 5.5F, -3.10F)
        );
        body.addOrReplaceChild(
                "rune_cross",
                CubeListBuilder.create().texOffs(208, 0).addBox(-3.0F, -0.55F, -0.06F, 6.0F, 1.1F, 0.12F),
                PartPose.offset(0.0F, 4.6F, -3.10F)
        );
        body.addOrReplaceChild(
                "rune_lower",
                CubeListBuilder.create().texOffs(224, 0).addBox(-1.8F, -0.45F, -0.06F, 3.6F, 0.9F, 0.12F),
                PartPose.offset(0.0F, 7.3F, -3.10F)
        );

        PartDefinition rightArm = root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(64, 64).addBox(-3.0F, -2.0F, -3.0F, 5.5F, 6.0F, 6.0F)
                        .texOffs(96, 64).addBox(-2.6F, 3.0F, -2.5F, 4.7F, 7.0F, 5.0F)
                        .texOffs(120, 64).addBox(-2.9F, 9.0F, -2.7F, 5.2F, 3.0F, 5.4F),
                PartPose.offset(-6.0F, 2.0F, 0.0F)
        );
        rightArm.addOrReplaceChild(
                "pauldron",
                CubeListBuilder.create().texOffs(144, 64)
                        .addBox(-4.0F, -2.0F, -3.5F, 7.0F, 4.0F, 7.0F)
                        .texOffs(176, 64).addBox(-4.4F, 1.2F, -3.1F, 7.4F, 1.5F, 6.2F),
                PartPose.offset(-0.35F, -1.0F, 0.0F)
        );
        PartDefinition sword = rightArm.addOrReplaceChild(
                "greatsword",
                CubeListBuilder.create()
                        .texOffs(192, 64).addBox(-0.9F, -1.0F, -0.9F, 1.8F, 7.0F, 1.8F)
                        .texOffs(208, 64).addBox(-5.0F, 5.0F, -1.0F, 10.0F, 1.8F, 2.0F)
                        .texOffs(0, 160).addBox(-2.3F, 6.5F, -0.7F, 4.6F, 20.0F, 1.4F)
                        .texOffs(32, 160).addBox(-1.4F, 26.0F, -0.6F, 2.8F, 4.0F, 1.2F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.08F, 0.0F, -0.52F)
        );
        sword.addOrReplaceChild(
                "fuller",
                CubeListBuilder.create().texOffs(48, 160).addBox(-0.55F, 0.0F, -0.15F, 1.1F, 17.0F, 0.3F),
                PartPose.offset(0.0F, 8.0F, -0.72F)
        );

        PartDefinition leftArm = root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(64, 96).mirror().addBox(-2.5F, -2.0F, -3.0F, 5.5F, 6.0F, 6.0F)
                        .texOffs(96, 96).mirror().addBox(-2.1F, 3.0F, -2.5F, 4.7F, 7.0F, 5.0F)
                        .texOffs(120, 96).mirror().addBox(-2.3F, 9.0F, -2.7F, 5.2F, 3.0F, 5.4F),
                PartPose.offset(6.0F, 2.0F, 0.0F)
        );
        leftArm.addOrReplaceChild(
                "pauldron",
                CubeListBuilder.create().texOffs(144, 96).mirror()
                        .addBox(-3.0F, -2.0F, -3.5F, 7.0F, 4.0F, 7.0F)
                        .texOffs(176, 96).mirror().addBox(-3.0F, 1.2F, -3.1F, 7.4F, 1.5F, 6.2F),
                PartPose.offset(0.35F, -1.0F, 0.0F)
        );

        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 112).addBox(-2.4F, 0.0F, -2.5F, 4.5F, 6.0F, 5.0F)
                        .texOffs(24, 112).addBox(-2.6F, 5.0F, -2.7F, 4.8F, 6.5F, 5.4F)
                        .texOffs(52, 112).addBox(-2.8F, 10.0F, -4.1F, 5.1F, 2.7F, 7.0F),
                PartPose.offset(-2.5F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(88, 112).mirror().addBox(-2.1F, 0.0F, -2.5F, 4.5F, 6.0F, 5.0F)
                        .texOffs(112, 112).mirror().addBox(-2.2F, 5.0F, -2.7F, 4.8F, 6.5F, 5.4F)
                        .texOffs(140, 112).mirror().addBox(-2.3F, 10.0F, -4.1F, 5.1F, 2.7F, 7.0F),
                PartPose.offset(2.5F, 12.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(SkeletonRenderState state) {
        super.setupAnim(state);

        float idle = Mth.sin(state.ageInTicks * 0.065F) * 0.025F;
        this.body.xRot += idle * 0.35F;
        this.head.zRot += Mth.sin(state.ageInTicks * 0.035F) * 0.012F;
        this.rightArm.zRot -= 0.08F;
        this.rightArm.xRot -= 0.08F;
        this.leftArm.zRot += 0.06F;

        if (state.attackTime > 0.0F) {
            float t = state.attackTime;
            float swing = Mth.sin(t * Mth.PI);
            float followThrough = Mth.sin((1.0F - (1.0F - t) * (1.0F - t)) * Mth.PI);

            this.rightArm.xRot = -0.55F - swing * 2.15F + followThrough * 0.35F;
            this.rightArm.yRot = -0.20F + swing * 0.28F;
            this.rightArm.zRot = -0.10F;
            this.leftArm.xRot = -0.75F - swing * 1.45F + followThrough * 0.20F;
            this.leftArm.yRot = 0.58F - swing * 0.42F;
            this.leftArm.zRot = 0.18F;
            this.body.yRot = swing * 0.18F;
            this.head.yRot -= swing * 0.10F;
        }

        this.sword.yRot = Mth.sin(state.ageInTicks * 0.09F) * 0.008F;
    }
}
