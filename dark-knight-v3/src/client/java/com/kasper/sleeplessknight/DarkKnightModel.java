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
 * Heavy great-helm knight rebuilt around the supplied reference.
 * Every visible plate is spatially separated to avoid z-fighting.
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

        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.85F, -8.10F, -2.85F, 5.70F, 7.85F, 5.70F),
                PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("helmet_back",
                CubeListBuilder.create().texOffs(40, 0)
                        .addBox(-3.75F, -8.70F, -0.55F, 7.50F, 8.85F, 1.10F),
                PartPose.offset(0.0F, 0.0F, 3.35F));
        head.addOrReplaceChild("helmet_left_side",
                CubeListBuilder.create().texOffs(72, 0)
                        .addBox(-0.55F, -8.65F, -3.20F, 1.10F, 8.80F, 6.40F),
                PartPose.offset(-3.35F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet_right_side",
                CubeListBuilder.create().texOffs(72, 0)
                        .addBox(-0.55F, -8.65F, -3.20F, 1.10F, 8.80F, 6.40F),
                PartPose.offset(3.35F, 0.0F, 0.0F));
        head.addOrReplaceChild("crown",
                CubeListBuilder.create().texOffs(104, 0)
                        .addBox(-4.10F, -0.65F, -3.65F, 8.20F, 1.30F, 7.30F),
                PartPose.offset(0.0F, -9.05F, 0.0F));
        head.addOrReplaceChild("crown_flare",
                CubeListBuilder.create().texOffs(152, 0)
                        .addBox(-5.75F, -0.50F, -3.95F, 11.50F, 1.00F, 7.90F),
                PartPose.offset(0.0F, -9.95F, 0.0F));
        head.addOrReplaceChild("top_ridge",
                CubeListBuilder.create().texOffs(216, 0)
                        .addBox(-0.65F, -0.65F, -3.20F, 1.30F, 1.30F, 6.40F),
                PartPose.offset(0.0F, -10.60F, 0.0F));
        head.addOrReplaceChild("forehead",
                CubeListBuilder.create().texOffs(248, 0)
                        .addBox(-3.15F, -0.70F, -0.55F, 6.30F, 1.40F, 1.10F),
                PartPose.offset(0.0F, -6.65F, -3.38F));
        head.addOrReplaceChild("nose_guard",
                CubeListBuilder.create().texOffs(280, 0)
                        .addBox(-0.48F, -2.55F, -0.55F, 0.96F, 5.10F, 1.10F),
                PartPose.offset(0.0F, -3.60F, -3.40F));
        head.addOrReplaceChild("left_face_plate",
                CubeListBuilder.create().texOffs(296, 0)
                        .addBox(-1.18F, -2.00F, -0.52F, 2.36F, 4.00F, 1.04F),
                PartPose.offsetAndRotation(-2.08F, -2.65F, -3.38F, 0.0F, -0.04F, 0.04F));
        head.addOrReplaceChild("right_face_plate",
                CubeListBuilder.create().texOffs(296, 0)
                        .addBox(-1.18F, -2.00F, -0.52F, 2.36F, 4.00F, 1.04F),
                PartPose.offsetAndRotation(2.08F, -2.65F, -3.38F, 0.0F, 0.04F, -0.04F));
        head.addOrReplaceChild("left_jaw",
                CubeListBuilder.create().texOffs(328, 0)
                        .addBox(-1.28F, -0.60F, -0.50F, 2.56F, 1.20F, 1.00F),
                PartPose.offset(-2.02F, -0.20F, -3.25F));
        head.addOrReplaceChild("right_jaw",
                CubeListBuilder.create().texOffs(328, 0)
                        .addBox(-1.28F, -0.60F, -0.50F, 2.56F, 1.20F, 1.00F),
                PartPose.offset(2.02F, -0.20F, -3.25F));

        PartDefinition body = root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 80)
                        .addBox(-4.40F, 0.15F, -2.35F, 8.80F, 11.85F, 4.70F),
                PartPose.ZERO);
        body.addOrReplaceChild("chest_top",
                CubeListBuilder.create().texOffs(64, 80)
                        .addBox(-6.05F, -1.85F, -0.65F, 12.10F, 3.70F, 1.30F),
                PartPose.offset(0.0F, 2.70F, -2.95F));
        body.addOrReplaceChild("chest_mid",
                CubeListBuilder.create().texOffs(128, 80)
                        .addBox(-5.65F, -1.85F, -0.62F, 11.30F, 3.70F, 1.24F),
                PartPose.offset(0.0F, 6.60F, -2.90F));
        body.addOrReplaceChild("chest_low",
                CubeListBuilder.create().texOffs(192, 80)
                        .addBox(-4.90F, -1.35F, -0.60F, 9.80F, 2.70F, 1.20F),
                PartPose.offset(0.0F, 10.00F, -2.82F));
        body.addOrReplaceChild("backplate",
                CubeListBuilder.create().texOffs(256, 80)
                        .addBox(-5.10F, -5.45F, -0.55F, 10.20F, 10.90F, 1.10F),
                PartPose.offset(0.0F, 5.85F, 2.82F));
        body.addOrReplaceChild("collar_left",
                CubeListBuilder.create().texOffs(320, 80)
                        .addBox(-2.50F, -0.55F, -2.45F, 5.00F, 1.10F, 4.90F),
                PartPose.offsetAndRotation(-3.10F, 0.15F, 0.0F, 0.0F, 0.0F, -0.13F));
        body.addOrReplaceChild("collar_right",
                CubeListBuilder.create().texOffs(320, 80)
                        .addBox(-2.50F, -0.55F, -2.45F, 5.00F, 1.10F, 4.90F),
                PartPose.offsetAndRotation(3.10F, 0.15F, 0.0F, 0.0F, 0.0F, 0.13F));
        body.addOrReplaceChild("belt",
                CubeListBuilder.create().texOffs(384, 80)
                        .addBox(-5.10F, -0.55F, -2.85F, 10.20F, 1.10F, 5.70F),
                PartPose.offset(0.0F, 12.55F, 0.0F));
        body.addOrReplaceChild("buckle",
                CubeListBuilder.create().texOffs(432, 80)
                        .addBox(-1.30F, -0.95F, -0.42F, 2.60F, 1.90F, 0.84F),
                PartPose.offset(0.0F, 12.55F, -3.35F));
        body.addOrReplaceChild("chain_front",
                CubeListBuilder.create().texOffs(0, 144)
                        .addBox(-4.10F, 0.0F, -0.20F, 8.20F, 7.10F, 0.40F),
                PartPose.offset(0.0F, 13.15F, -2.52F));
        body.addOrReplaceChild("chain_back",
                CubeListBuilder.create().texOffs(0, 144)
                        .addBox(-4.10F, 0.0F, -0.20F, 8.20F, 7.10F, 0.40F),
                PartPose.offset(0.0F, 13.15F, 2.52F));
        body.addOrReplaceChild("tasset_left",
                CubeListBuilder.create().texOffs(64, 144)
                        .addBox(-1.60F, 0.0F, -0.38F, 3.20F, 6.60F, 0.76F),
                PartPose.offsetAndRotation(-3.20F, 13.05F, -3.02F, 0.03F, 0.0F, 0.08F));
        body.addOrReplaceChild("tasset_right",
                CubeListBuilder.create().texOffs(64, 144)
                        .addBox(-1.60F, 0.0F, -0.38F, 3.20F, 6.60F, 0.76F),
                PartPose.offsetAndRotation(3.20F, 13.05F, -3.02F, 0.03F, 0.0F, -0.08F));
        body.addOrReplaceChild("rune_spine",
                CubeListBuilder.create().texOffs(448, 80)
                        .addBox(-0.30F, -3.15F, -0.07F, 0.60F, 6.30F, 0.14F),
                PartPose.offset(0.0F, 5.25F, -3.65F));
        body.addOrReplaceChild("rune_upper",
                CubeListBuilder.create().texOffs(448, 80)
                        .addBox(-2.10F, -0.28F, -0.07F, 4.20F, 0.56F, 0.14F),
                PartPose.offset(0.0F, 3.35F, -3.65F));
        body.addOrReplaceChild("rune_lower",
                CubeListBuilder.create().texOffs(448, 80)
                        .addBox(-1.55F, -0.26F, -0.07F, 3.10F, 0.52F, 0.14F),
                PartPose.offset(0.0F, 5.65F, -3.65F));
        body.addOrReplaceChild("rune_fork_left",
                CubeListBuilder.create().texOffs(448, 80)
                        .addBox(-0.25F, -1.05F, -0.07F, 0.50F, 2.10F, 0.14F),
                PartPose.offsetAndRotation(-1.95F, 2.95F, -3.65F, 0.0F, 0.0F, -0.72F));
        body.addOrReplaceChild("rune_fork_right",
                CubeListBuilder.create().texOffs(448, 80)
                        .addBox(-0.25F, -1.05F, -0.07F, 0.50F, 2.10F, 0.14F),
                PartPose.offsetAndRotation(1.95F, 2.95F, -3.65F, 0.0F, 0.0F, 0.72F));
        body.addOrReplaceChild("rune_tip",
                CubeListBuilder.create().texOffs(448, 80)
                        .addBox(-0.48F, -0.48F, -0.07F, 0.96F, 0.96F, 0.14F),
                PartPose.offsetAndRotation(0.0F, 8.65F, -3.65F, 0.0F, 0.0F, 0.78F));

        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(0, 208)
                        .addBox(-2.00F, -1.65F, -2.00F, 4.00F, 11.20F, 4.00F),
                PartPose.offset(-7.15F, 2.10F, 0.0F));
        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(0, 208)
                        .addBox(-2.00F, -1.65F, -2.00F, 4.00F, 11.20F, 4.00F),
                PartPose.offset(7.15F, 2.10F, 0.0F));
        addArmArmor(rightArm, true);
        addArmArmor(leftArm, false);

        PartDefinition sword = rightArm.addOrReplaceChild("greatsword",
                CubeListBuilder.create()
                        .texOffs(256, 208).addBox(-0.58F, -3.25F, -0.58F, 1.16F, 6.50F, 1.16F)
                        .texOffs(288, 208).addBox(-4.65F, 3.05F, -0.72F, 9.30F, 1.44F, 1.44F)
                        .texOffs(336, 208).addBox(-1.82F, 4.75F, -0.54F, 3.64F, 19.80F, 1.08F)
                        .texOffs(376, 208).addBox(-1.28F, 24.55F, -0.48F, 2.56F, 3.50F, 0.96F),
                PartPose.offsetAndRotation(-0.35F, 9.25F, -0.55F, 0.10F, 0.0F, 0.48F));
        sword.addOrReplaceChild("fuller",
                CubeListBuilder.create().texOffs(416, 208)
                        .addBox(-0.30F, 0.0F, -0.06F, 0.60F, 17.20F, 0.12F),
                PartPose.offset(0.0F, 6.15F, -0.62F));

        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 288)
                        .addBox(-1.85F, 0.15F, -1.90F, 3.70F, 11.85F, 3.80F),
                PartPose.offset(-2.40F, 12.0F, 0.0F));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 288)
                        .addBox(-1.85F, 0.15F, -1.90F, 3.70F, 11.85F, 3.80F),
                PartPose.offset(2.40F, 12.0F, 0.0F));
        addLegArmor(rightLeg);
        addLegArmor(leftLeg);

        return LayerDefinition.create(mesh, 512, 512);
    }

    private static void addArmArmor(PartDefinition arm, boolean right) {
        float side = right ? -1.0F : 1.0F;
        arm.addOrReplaceChild("upper_plate",
                CubeListBuilder.create().texOffs(64, 208)
                        .addBox(-2.45F, -1.10F, -2.45F, 4.90F, 3.70F, 4.90F),
                PartPose.ZERO);
        arm.addOrReplaceChild("elbow",
                CubeListBuilder.create().texOffs(104, 208)
                        .addBox(-2.62F, -0.55F, -2.55F, 5.24F, 1.10F, 5.10F),
                PartPose.offset(0.0F, 3.55F, 0.0F));
        arm.addOrReplaceChild("forearm",
                CubeListBuilder.create().texOffs(144, 208)
                        .addBox(-2.30F, -1.95F, -2.35F, 4.60F, 3.90F, 4.70F),
                PartPose.offset(0.0F, 6.30F, 0.0F));
        arm.addOrReplaceChild("gauntlet",
                CubeListBuilder.create().texOffs(184, 208)
                        .addBox(-2.45F, -0.95F, -2.45F, 4.90F, 1.90F, 4.90F),
                PartPose.offset(0.0F, 9.70F, 0.0F));
        PartDefinition shoulder = arm.addOrReplaceChild("pauldron_top",
                CubeListBuilder.create().texOffs(64, 248)
                        .addBox(-3.55F, -0.70F, -3.15F, 7.10F, 1.40F, 6.30F),
                PartPose.offsetAndRotation(side * 0.55F, -2.60F, 0.0F, 0.0F, 0.0F, side * 0.10F));
        shoulder.addOrReplaceChild("pauldron_mid",
                CubeListBuilder.create().texOffs(120, 248)
                        .addBox(-3.15F, -1.20F, -2.95F, 6.30F, 2.40F, 5.90F),
                PartPose.offsetAndRotation(side * 1.35F, 1.80F, 0.0F, 0.0F, 0.0F, side * 0.12F));
        shoulder.addOrReplaceChild("pauldron_low",
                CubeListBuilder.create().texOffs(176, 248)
                        .addBox(-2.65F, -0.50F, -2.70F, 5.30F, 1.00F, 5.40F),
                PartPose.offsetAndRotation(side * 1.55F, 4.10F, 0.0F, 0.0F, 0.0F, side * 0.10F));
    }

    private static void addLegArmor(PartDefinition leg) {
        leg.addOrReplaceChild("thigh",
                CubeListBuilder.create().texOffs(64, 288)
                        .addBox(-2.20F, -1.95F, -2.25F, 4.40F, 3.90F, 4.50F),
                PartPose.offset(0.0F, 2.30F, 0.0F));
        leg.addOrReplaceChild("knee",
                CubeListBuilder.create().texOffs(112, 288)
                        .addBox(-2.45F, -0.62F, -2.55F, 4.90F, 1.24F, 5.10F),
                PartPose.offset(0.0F, 5.45F, -0.05F));
        leg.addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(160, 288)
                        .addBox(-2.08F, -1.90F, -2.15F, 4.16F, 3.80F, 4.30F),
                PartPose.offset(0.0F, 8.25F, 0.0F));
        leg.addOrReplaceChild("boot",
                CubeListBuilder.create().texOffs(208, 288)
                        .addBox(-2.40F, -1.05F, -3.45F, 4.80F, 2.10F, 5.70F),
                PartPose.offset(0.0F, 11.15F, -0.05F));
    }

    @Override
    public void setupAnim(SkeletonRenderState state) {
        super.setupAnim(state);
        this.rightArm.xRot = -0.32F + this.rightArm.xRot * 0.22F;
        this.rightArm.yRot = -0.10F;
        this.rightArm.zRot = 0.16F;
        this.leftArm.xRot = -0.08F + this.leftArm.xRot * 0.35F;
        this.leftArm.zRot = -0.06F;
        this.body.xRot += Mth.sin(state.ageInTicks * 0.040F) * 0.007F;
        this.head.zRot += Mth.sin(state.ageInTicks * 0.025F) * 0.006F;
        if (state.attackTime > 0.0F) {
            float t = state.attackTime;
            float swing = Mth.sin(t * Mth.PI);
            float recovery = Mth.sin((1.0F - (1.0F - t) * (1.0F - t)) * Mth.PI);
            this.rightArm.xRot = -0.85F - 1.55F * swing + 0.28F * recovery;
            this.rightArm.yRot = -0.28F + 0.18F * swing;
            this.rightArm.zRot = 0.10F;
            this.leftArm.xRot = -0.35F - 0.50F * swing;
            this.leftArm.yRot = 0.18F;
            this.body.yRot = 0.10F * swing;
        }
        this.sword.yRot = Mth.sin(state.ageInTicks * 0.055F) * 0.002F;
    }
}
