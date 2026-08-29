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

/**
 * Reference-driven heavy knight. Every visible plate occupies its own volume;
 * there are no coplanar duplicate shells, so shaders cannot z-fight.
 */
public final class DarkKnightModel extends SkeletonModel<SkeletonRenderState> {
    private final ModelPart sword;

    public DarkKnightModel(ModelPart root) {
        super(root);
        this.sword = this.rightArm.getChild("greatsword");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        // Tall flared bucket helmet from the reference.
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-3.75F, -9.6F, -3.65F, 7.5F, 8.9F, 7.3F),
                PartPose.ZERO);
        head.addOrReplaceChild("helmet_top",
                CubeListBuilder.create().texOffs(72, 0)
                        .addBox(-5.6F, -0.55F, -4.25F, 11.2F, 1.1F, 8.5F),
                PartPose.offset(0.0F, -10.35F, 0.0F));
        head.addOrReplaceChild("helmet_neck",
                CubeListBuilder.create().texOffs(100, 18)
                        .addBox(-3.25F, -0.45F, -3.1F, 6.5F, 0.9F, 6.2F),
                PartPose.offset(0.0F, -0.05F, 0.0F));
        head.addOrReplaceChild("visor",
                CubeListBuilder.create().texOffs(120, 0)
                        .addBox(-2.9F, -0.65F, -0.10F, 5.8F, 1.3F, 0.20F),
                PartPose.offset(0.0F, -5.25F, -3.88F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        // Broad breastplate tapering into a narrower abdomen.
        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(0, 48).addBox(-5.15F, 0.5F, -3.0F, 10.3F, 5.7F, 6.0F)
                        .texOffs(34, 48).addBox(-4.45F, 6.55F, -2.55F, 8.9F, 4.65F, 5.1F),
                PartPose.ZERO);

        // Raised collar ring, physically separated from chest.
        body.addOrReplaceChild("collar_front",
                CubeListBuilder.create().texOffs(66, 48).addBox(-4.4F, -0.35F, -0.30F, 8.8F, 0.7F, 0.6F),
                PartPose.offset(0.0F, 0.05F, -3.38F));
        body.addOrReplaceChild("collar_back",
                CubeListBuilder.create().texOffs(66, 56).addBox(-4.4F, -0.35F, -0.30F, 8.8F, 0.7F, 0.6F),
                PartPose.offset(0.0F, 0.05F, 3.38F));

        // Waist belt and hanging chainmail are below the torso, not inside it.
        body.addOrReplaceChild("belt",
                CubeListBuilder.create().texOffs(90, 48).addBox(-4.7F, -0.5F, -2.85F, 9.4F, 1.0F, 5.7F),
                PartPose.offset(0.0F, 11.85F, 0.0F));
        body.addOrReplaceChild("chain_front",
                CubeListBuilder.create().texOffs(0, 80).addBox(-4.25F, 0.0F, -0.14F, 8.5F, 5.7F, 0.28F),
                PartPose.offset(0.0F, 12.65F, -2.60F));
        body.addOrReplaceChild("chain_back",
                CubeListBuilder.create().texOffs(20, 80).addBox(-4.25F, 0.0F, -0.14F, 8.5F, 5.7F, 0.28F),
                PartPose.offset(0.0F, 12.65F, 2.60F));
        body.addOrReplaceChild("tasset_r",
                CubeListBuilder.create().texOffs(40, 80).addBox(-1.45F, 0.0F, -0.12F, 2.9F, 4.8F, 0.24F),
                PartPose.offset(-2.35F, 12.45F, -2.96F));
        body.addOrReplaceChild("tasset_l",
                CubeListBuilder.create().texOffs(54, 80).addBox(-1.45F, 0.0F, -0.12F, 2.9F, 4.8F, 0.24F),
                PartPose.offset(2.35F, 12.45F, -2.96F));

        // Pale chest sigil, as separate shallow geometry with a safe depth gap.
        body.addOrReplaceChild("rune_v",
                CubeListBuilder.create().texOffs(192, 0).addBox(-0.38F, -2.3F, -0.05F, 0.76F, 4.6F, 0.10F),
                PartPose.offset(0.0F, 4.3F, -3.22F));
        body.addOrReplaceChild("rune_h",
                CubeListBuilder.create().texOffs(208, 0).addBox(-2.15F, -0.35F, -0.05F, 4.3F, 0.70F, 0.10F),
                PartPose.offset(0.0F, 3.45F, -3.22F));
        body.addOrReplaceChild("rune_low",
                CubeListBuilder.create().texOffs(224, 0).addBox(-1.25F, -0.30F, -0.05F, 2.5F, 0.60F, 0.10F),
                PartPose.offset(0.0F, 5.95F, -3.22F));

        // Right arm: broad layered pauldron, then separated arm plates.
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(64, 64).addBox(-2.15F, 0.0F, -2.25F, 4.0F, 3.7F, 4.5F)
                        .texOffs(88, 64).addBox(-2.00F, 4.05F, -2.10F, 3.7F, 1.25F, 4.2F)
                        .texOffs(110, 64).addBox(-1.85F, 5.65F, -1.95F, 3.4F, 3.65F, 3.9F)
                        .texOffs(132, 64).addBox(-2.05F, 9.65F, -2.10F, 3.8F, 2.05F, 4.2F),
                PartPose.offset(-6.45F, 1.65F, 0.0F));
        rightArm.addOrReplaceChild("pauldron_outer",
                CubeListBuilder.create().texOffs(150, 64).addBox(-3.3F, -0.55F, -2.9F, 6.0F, 1.1F, 5.8F),
                PartPose.offset(-0.25F, -2.20F, 0.0F));
        rightArm.addOrReplaceChild("pauldron_lower",
                CubeListBuilder.create().texOffs(174, 64).addBox(-3.0F, -0.45F, -2.65F, 5.4F, 0.9F, 5.3F),
                PartPose.offset(-0.22F, -0.65F, 0.0F));

        // Greatsword is deliberately OUTSIDE the right leg. It no longer crosses
        // the body in the idle pose. The hand sits on the grip, blade hangs down.
        PartDefinition sword = rightArm.addOrReplaceChild("greatsword",
                CubeListBuilder.create()
                        .texOffs(192, 128).addBox(-0.70F, -1.1F, -0.70F, 1.4F, 5.2F, 1.4F)
                        .texOffs(208, 128).addBox(-4.1F, 4.35F, -0.70F, 8.2F, 1.2F, 1.4F)
                        .texOffs(0, 160).addBox(-1.60F, 5.8F, -0.45F, 3.2F, 15.4F, 0.9F)
                        .texOffs(32, 160).addBox(-1.05F, 21.35F, -0.38F, 2.1F, 2.8F, 0.76F),
                PartPose.offsetAndRotation(-2.75F, 9.55F, 0.15F, -0.10F, 0.0F, 0.16F));
        sword.addOrReplaceChild("blade_ridge",
                CubeListBuilder.create().texOffs(48, 160).addBox(-0.22F, 0.0F, -0.06F, 0.44F, 13.0F, 0.12F),
                PartPose.offset(0.0F, 7.1F, -0.51F));

        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(64, 96).mirror().addBox(-1.85F, 0.0F, -2.25F, 4.0F, 3.7F, 4.5F)
                        .texOffs(88, 96).mirror().addBox(-1.70F, 4.05F, -2.10F, 3.7F, 1.25F, 4.2F)
                        .texOffs(110, 96).mirror().addBox(-1.55F, 5.65F, -1.95F, 3.4F, 3.65F, 3.9F)
                        .texOffs(132, 96).mirror().addBox(-1.75F, 9.65F, -2.10F, 3.8F, 2.05F, 4.2F),
                PartPose.offset(6.45F, 1.65F, 0.0F));
        leftArm.addOrReplaceChild("pauldron_outer",
                CubeListBuilder.create().texOffs(150, 96).mirror().addBox(-2.7F, -0.55F, -2.9F, 6.0F, 1.1F, 5.8F),
                PartPose.offset(0.25F, -2.20F, 0.0F));
        leftArm.addOrReplaceChild("pauldron_lower",
                CubeListBuilder.create().texOffs(174, 96).mirror().addBox(-2.4F, -0.45F, -2.65F, 5.4F, 0.9F, 5.3F),
                PartPose.offset(0.22F, -0.65F, 0.0F));

        // Segmented legs. Every plate has a visible tiny gap before the next one.
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 112).addBox(-1.95F, 0.3F, -2.10F, 3.8F, 3.95F, 4.2F)
                        .texOffs(22, 112).addBox(-2.10F, 4.60F, -2.25F, 4.1F, 1.20F, 4.5F)
                        .texOffs(46, 112).addBox(-1.85F, 6.15F, -2.05F, 3.6F, 4.05F, 4.1F)
                        .texOffs(70, 112).addBox(-2.05F, 10.55F, -3.25F, 4.0F, 1.75F, 5.2F),
                PartPose.offset(-2.15F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create()
                        .texOffs(96, 112).mirror().addBox(-1.85F, 0.3F, -2.10F, 3.8F, 3.95F, 4.2F)
                        .texOffs(118, 112).mirror().addBox(-2.00F, 4.60F, -2.25F, 4.1F, 1.20F, 4.5F)
                        .texOffs(142, 112).mirror().addBox(-1.75F, 6.15F, -2.05F, 3.6F, 4.05F, 4.1F)
                        .texOffs(166, 112).mirror().addBox(-1.95F, 10.55F, -3.25F, 4.0F, 1.75F, 5.2F),
                PartPose.offset(2.15F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(SkeletonRenderState state) {
        super.setupAnim(state);

        // Heavy, restrained idle stance.
        this.rightArm.zRot -= 0.12F;
        this.rightArm.xRot -= 0.06F;
        this.leftArm.zRot += 0.07F;
        this.body.xRot += Mth.sin(state.ageInTicks * 0.055F) * 0.010F;
        this.head.zRot += Mth.sin(state.ageInTicks * 0.032F) * 0.009F;

        // Two-handed-feeling heavy chop. The sword stays attached to the hand;
        // its idle origin is far enough outside the body to never pierce a leg.
        if (state.attackTime > 0.0F) {
            float t = state.attackTime;
            float swing = Mth.sin(t * Mth.PI);
            float follow = Mth.sin((1.0F - (1.0F - t) * (1.0F - t)) * Mth.PI);
            this.rightArm.xRot = -0.55F - 1.95F * swing + 0.28F * follow;
            this.rightArm.yRot = -0.18F + 0.20F * swing;
            this.rightArm.zRot = -0.10F;
            this.leftArm.xRot = -0.52F - 1.20F * swing;
            this.leftArm.yRot = 0.38F - 0.24F * swing;
            this.leftArm.zRot = 0.12F;
            this.body.yRot = 0.12F * swing;
        }

        this.sword.yRot = Mth.sin(state.ageInTicks * 0.08F) * 0.004F;
    }
}
