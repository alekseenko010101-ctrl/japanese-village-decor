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
 * Rebuilt from zero from the supplied knight reference.
 * The visible armor is assembled from separated plates: no duplicated shells,
 * no coplanar cubes and no second skin over the same face.
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

        // ---------- HELMET ----------
        // The head itself is the black void inside the helmet. The metal shell
        // is built around it from individual walls, leaving a real T-shaped opening.
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3.15F, -9.45F, -3.15F, 6.30F, 9.20F, 6.30F),
                PartPose.ZERO
        );
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("helmet_back",
                CubeListBuilder.create().texOffs(64, 0)
                        .addBox(-4.20F, -10.10F, -0.55F, 8.40F, 10.40F, 1.10F),
                PartPose.offset(0.0F, 0.0F, 3.75F));
        head.addOrReplaceChild("helmet_left_wall",
                CubeListBuilder.create().texOffs(64, 0)
                        .addBox(-0.70F, -10.10F, -3.70F, 1.40F, 10.40F, 7.40F),
                PartPose.offset(-3.75F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet_right_wall",
                CubeListBuilder.create().texOffs(64, 0)
                        .addBox(-0.70F, -10.10F, -3.70F, 1.40F, 10.40F, 7.40F),
                PartPose.offset(3.75F, 0.0F, 0.0F));
        head.addOrReplaceChild("helmet_top",
                CubeListBuilder.create().texOffs(128, 0)
                        .addBox(-4.45F, -0.65F, -4.05F, 8.90F, 1.30F, 8.10F),
                PartPose.offset(0.0F, -10.65F, 0.0F));
        head.addOrReplaceChild("helmet_flare",
                CubeListBuilder.create().texOffs(128, 0)
                        .addBox(-6.55F, -0.55F, -4.75F, 13.10F, 1.10F, 9.50F),
                PartPose.offset(0.0F, -11.85F, 0.0F));
        head.addOrReplaceChild("brow",
                CubeListBuilder.create().texOffs(192, 0)
                        .addBox(-3.30F, -0.70F, -0.55F, 6.60F, 1.40F, 1.10F),
                PartPose.offset(0.0F, -7.20F, -3.78F));
        head.addOrReplaceChild("nose_guard",
                CubeListBuilder.create().texOffs(192, 0)
                        .addBox(-0.60F, -2.80F, -0.55F, 1.20F, 5.60F, 1.10F),
                PartPose.offset(0.0F, -4.05F, -3.78F));
        head.addOrReplaceChild("left_cheek",
                CubeListBuilder.create().texOffs(192, 0)
                        .addBox(-0.85F, -2.15F, -0.55F, 1.70F, 4.30F, 1.10F),
                PartPose.offset(-2.45F, -2.90F, -3.78F));
        head.addOrReplaceChild("right_cheek",
                CubeListBuilder.create().texOffs(192, 0)
                        .addBox(-0.85F, -2.15F, -0.55F, 1.70F, 4.30F, 1.10F),
                PartPose.offset(2.45F, -2.90F, -3.78F));

        // ---------- TORSO ----------
        // Mail core, then three progressively narrower breastplate sections.
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(0, 64)
                        .addBox(-4.35F, 0.25F, -2.45F, 8.70F, 11.80F, 4.90F),
                PartPose.ZERO
        );
        body.addOrReplaceChild("upper_breastplate",
                CubeListBuilder.create().texOffs(64, 64)
                        .addBox(-6.15F, -2.45F, -0.75F, 12.30F, 4.90F, 1.50F),
                PartPose.offset(0.0F, 2.80F, -3.05F));
        body.addOrReplaceChild("middle_breastplate",
                CubeListBuilder.create().texOffs(64, 64)
                        .addBox(-5.70F, -2.05F, -0.70F, 11.40F, 4.10F, 1.40F),
                PartPose.offset(0.0F, 7.30F, -2.95F));
        body.addOrReplaceChild("lower_breastplate",
                CubeListBuilder.create().texOffs(128, 64)
                        .addBox(-4.85F, -1.20F, -0.65F, 9.70F, 2.40F, 1.30F),
                PartPose.offset(0.0F, 10.85F, -2.85F));
        body.addOrReplaceChild("backplate",
                CubeListBuilder.create().texOffs(128, 64)
                        .addBox(-5.25F, -5.55F, -0.60F, 10.50F, 11.10F, 1.20F),
                PartPose.offset(0.0F, 5.90F, 2.95F));
        body.addOrReplaceChild("left_sideplate",
                CubeListBuilder.create().texOffs(192, 64)
                        .addBox(-0.55F, -4.80F, -2.50F, 1.10F, 9.60F, 5.00F),
                PartPose.offset(-4.95F, 5.75F, 0.0F));
        body.addOrReplaceChild("right_sideplate",
                CubeListBuilder.create().texOffs(192, 64)
                        .addBox(-0.55F, -4.80F, -2.50F, 1.10F, 9.60F, 5.00F),
                PartPose.offset(4.95F, 5.75F, 0.0F));
        body.addOrReplaceChild("left_collar",
                CubeListBuilder.create().texOffs(192, 64)
                        .addBox(-2.75F, -0.65F, -2.60F, 5.50F, 1.30F, 5.20F),
                PartPose.offsetAndRotation(-3.10F, 0.05F, 0.0F, 0.0F, 0.0F, -0.16F));
        body.addOrReplaceChild("right_collar",
                CubeListBuilder.create().texOffs(192, 64)
                        .addBox(-2.75F, -0.65F, -2.60F, 5.50F, 1.30F, 5.20F),
                PartPose.offsetAndRotation(3.10F, 0.05F, 0.0F, 0.0F, 0.0F, 0.16F));

        body.addOrReplaceChild("belt",
                CubeListBuilder.create().texOffs(256, 64)
                        .addBox(-5.15F, -0.60F, -3.00F, 10.30F, 1.20F, 6.00F),
                PartPose.offset(0.0F, 12.65F, 0.0F));
        body.addOrReplaceChild("belt_buckle",
                CubeListBuilder.create().texOffs(256, 64)
                        .addBox(-1.45F, -1.10F, -0.45F, 2.90F, 2.20F, 0.90F),
                PartPose.offset(0.0F, 12.65F, -3.55F));

        // Thin chain panels sit clearly in front/behind the legs, never coplanar.
        body.addOrReplaceChild("chain_front",
                CubeListBuilder.create().texOffs(320, 64)
                        .addBox(-4.35F, 0.0F, -0.22F, 8.70F, 7.20F, 0.44F),
                PartPose.offset(0.0F, 13.25F, -2.55F));
        body.addOrReplaceChild("chain_back",
                CubeListBuilder.create().texOffs(320, 64)
                        .addBox(-4.35F, 0.0F, -0.22F, 8.70F, 7.20F, 0.44F),
                PartPose.offset(0.0F, 13.25F, 2.55F));
        body.addOrReplaceChild("right_tasset",
                CubeListBuilder.create().texOffs(384, 64)
                        .addBox(-1.65F, 0.0F, -0.42F, 3.30F, 7.00F, 0.84F),
                PartPose.offsetAndRotation(-3.25F, 13.15F, -3.15F, 0.04F, 0.0F, 0.08F));
        body.addOrReplaceChild("left_tasset",
                CubeListBuilder.create().texOffs(384, 64)
                        .addBox(-1.65F, 0.0F, -0.42F, 3.30F, 7.00F, 0.84F),
                PartPose.offsetAndRotation(3.25F, 13.15F, -3.15F, 0.04F, 0.0F, -0.08F));

        // Reference rune: vertical spine, two crosses, upper forks and diamond tip.
        body.addOrReplaceChild("rune_spine",
                CubeListBuilder.create().texOffs(448, 64)
                        .addBox(-0.38F, -3.15F, -0.08F, 0.76F, 6.30F, 0.16F),
                PartPose.offset(0.0F, 5.15F, -3.88F));
        body.addOrReplaceChild("rune_cross_upper",
                CubeListBuilder.create().texOffs(448, 64)
                        .addBox(-2.15F, -0.35F, -0.08F, 4.30F, 0.70F, 0.16F),
                PartPose.offset(0.0F, 3.45F, -3.88F));
        body.addOrReplaceChild("rune_cross_lower",
                CubeListBuilder.create().texOffs(448, 64)
                        .addBox(-1.60F, -0.32F, -0.08F, 3.20F, 0.64F, 0.16F),
                PartPose.offset(0.0F, 5.65F, -3.88F));
        body.addOrReplaceChild("rune_left_fork",
                CubeListBuilder.create().texOffs(448, 64)
                        .addBox(-0.28F, -1.25F, -0.08F, 0.56F, 2.50F, 0.16F),
                PartPose.offsetAndRotation(-2.05F, 3.00F, -3.88F, 0.0F, 0.0F, -0.72F));
        body.addOrReplaceChild("rune_right_fork",
                CubeListBuilder.create().texOffs(448, 64)
                        .addBox(-0.28F, -1.25F, -0.08F, 0.56F, 2.50F, 0.16F),
                PartPose.offsetAndRotation(2.05F, 3.00F, -3.88F, 0.0F, 0.0F, 0.72F));
        body.addOrReplaceChild("rune_tip",
                CubeListBuilder.create().texOffs(448, 64)
                        .addBox(-0.55F, -0.55F, -0.08F, 1.10F, 1.10F, 0.16F),
                PartPose.offsetAndRotation(0.0F, 8.55F, -3.88F, 0.0F, 0.0F, 0.78F));

        // ---------- ARMS ----------
        PartDefinition rightArm = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(0, 128)
                        .addBox(-2.10F, -1.65F, -2.10F, 4.20F, 11.40F, 4.20F),
                PartPose.offset(-7.15F, 2.15F, 0.0F));
        rightArm.addOrReplaceChild("upper_arm_plate",
                CubeListBuilder.create().texOffs(64, 128)
                        .addBox(-2.55F, -1.10F, -2.55F, 5.10F, 3.90F, 5.10F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        rightArm.addOrReplaceChild("elbow_plate",
                CubeListBuilder.create().texOffs(64, 128)
                        .addBox(-2.75F, -0.60F, -2.70F, 5.50F, 1.20F, 5.40F),
                PartPose.offset(0.0F, 3.65F, 0.0F));
        rightArm.addOrReplaceChild("forearm_plate",
                CubeListBuilder.create().texOffs(64, 128)
                        .addBox(-2.40F, -2.10F, -2.45F, 4.80F, 4.20F, 4.90F),
                PartPose.offset(0.0F, 6.35F, 0.0F));
        rightArm.addOrReplaceChild("gauntlet",
                CubeListBuilder.create().texOffs(192, 128)
                        .addBox(-2.55F, -1.00F, -2.55F, 5.10F, 2.00F, 5.10F),
                PartPose.offset(0.0F, 9.95F, 0.0F));
        PartDefinition rp = rightArm.addOrReplaceChild("pauldron",
                CubeListBuilder.create().texOffs(128, 128)
                        .addBox(-3.65F, -0.75F, -3.35F, 7.30F, 1.50F, 6.70F),
                PartPose.offsetAndRotation(-0.55F, -2.55F, 0.0F, 0.0F, 0.0F, -0.09F));
        rp.addOrReplaceChild("outer_plate",
                CubeListBuilder.create().texOffs(128, 128)
                        .addBox(-2.85F, -1.65F, -3.05F, 5.70F, 3.30F, 6.10F),
                PartPose.offsetAndRotation(-1.75F, 1.85F, 0.0F, 0.0F, 0.0F, -0.16F));
        rp.addOrReplaceChild("lower_strip",
                CubeListBuilder.create().texOffs(128, 128)
                        .addBox(-2.55F, -0.55F, -2.85F, 5.10F, 1.10F, 5.70F),
                PartPose.offsetAndRotation(-1.60F, 4.15F, 0.0F, 0.0F, 0.0F, -0.12F));

        PartDefinition leftArm = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(0, 128)
                        .addBox(-2.10F, -1.65F, -2.10F, 4.20F, 11.40F, 4.20F),
                PartPose.offset(7.15F, 2.15F, 0.0F));
        leftArm.addOrReplaceChild("upper_arm_plate",
                CubeListBuilder.create().texOffs(64, 128)
                        .addBox(-2.55F, -1.10F, -2.55F, 5.10F, 3.90F, 5.10F),
                PartPose.ZERO);
        leftArm.addOrReplaceChild("elbow_plate",
                CubeListBuilder.create().texOffs(64, 128)
                        .addBox(-2.75F, -0.60F, -2.70F, 5.50F, 1.20F, 5.40F),
                PartPose.offset(0.0F, 3.65F, 0.0F));
        leftArm.addOrReplaceChild("forearm_plate",
                CubeListBuilder.create().texOffs(64, 128)
                        .addBox(-2.40F, -2.10F, -2.45F, 4.80F, 4.20F, 4.90F),
                PartPose.offset(0.0F, 6.35F, 0.0F));
        leftArm.addOrReplaceChild("gauntlet",
                CubeListBuilder.create().texOffs(192, 128)
                        .addBox(-2.55F, -1.00F, -2.55F, 5.10F, 2.00F, 5.10F),
                PartPose.offset(0.0F, 9.95F, 0.0F));
        PartDefinition lp = leftArm.addOrReplaceChild("pauldron",
                CubeListBuilder.create().texOffs(128, 128)
                        .addBox(-3.65F, -0.75F, -3.35F, 7.30F, 1.50F, 6.70F),
                PartPose.offsetAndRotation(0.55F, -2.55F, 0.0F, 0.0F, 0.0F, 0.09F));
        lp.addOrReplaceChild("outer_plate",
                CubeListBuilder.create().texOffs(128, 128)
                        .addBox(-2.85F, -1.65F, -3.05F, 5.70F, 3.30F, 6.10F),
                PartPose.offsetAndRotation(1.75F, 1.85F, 0.0F, 0.0F, 0.0F, 0.16F));
        lp.addOrReplaceChild("lower_strip",
                CubeListBuilder.create().texOffs(128, 128)
                        .addBox(-2.55F, -0.55F, -2.85F, 5.10F, 1.10F, 5.70F),
                PartPose.offsetAndRotation(1.60F, 4.15F, 0.0F, 0.0F, 0.0F, 0.12F));

        // ---------- GREATSWORD ----------
        // The weapon hangs OUTSIDE the right leg in idle. Its pivot is near the hand,
        // and the blade begins below the guard instead of starting inside the body.
        PartDefinition sword = rightArm.addOrReplaceChild("greatsword",
                CubeListBuilder.create()
                        .texOffs(256, 192).addBox(-0.80F, -2.70F, -0.80F, 1.60F, 5.40F, 1.60F)
                        .texOffs(320, 192).addBox(-5.15F, 2.95F, -0.85F, 10.30F, 1.70F, 1.70F)
                        .texOffs(384, 192).addBox(-2.35F, 4.95F, -0.65F, 4.70F, 21.50F, 1.30F)
                        .texOffs(384, 192).addBox(-1.55F, 26.70F, -0.58F, 3.10F, 4.10F, 1.16F),
                PartPose.offsetAndRotation(-3.55F, 9.55F, -0.65F, 0.06F, -0.04F, 0.10F));
        sword.addOrReplaceChild("fuller",
                CubeListBuilder.create().texOffs(448, 192)
                        .addBox(-0.38F, 0.0F, -0.08F, 0.76F, 18.50F, 0.16F),
                PartPose.offset(0.0F, 6.20F, -0.78F));

        // ---------- LEGS ----------
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 192)
                        .addBox(-1.90F, 0.20F, -1.95F, 3.80F, 11.80F, 3.90F),
                PartPose.offset(-2.45F, 12.0F, 0.0F));
        rightLeg.addOrReplaceChild("thigh_plate",
                CubeListBuilder.create().texOffs(64, 192)
                        .addBox(-2.35F, -2.15F, -2.40F, 4.70F, 4.30F, 4.80F),
                PartPose.offset(0.0F, 2.45F, 0.0F));
        rightLeg.addOrReplaceChild("knee",
                CubeListBuilder.create().texOffs(128, 192)
                        .addBox(-2.60F, -0.70F, -2.65F, 5.20F, 1.40F, 5.30F),
                PartPose.offset(0.0F, 5.55F, -0.10F));
        rightLeg.addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(128, 192)
                        .addBox(-2.20F, -2.00F, -2.25F, 4.40F, 4.00F, 4.50F),
                PartPose.offset(0.0F, 8.35F, 0.0F));
        rightLeg.addOrReplaceChild("boot",
                CubeListBuilder.create().texOffs(192, 192)
                        .addBox(-2.55F, -1.10F, -3.80F, 5.10F, 2.20F, 6.20F),
                PartPose.offset(0.0F, 11.35F, -0.10F));

        PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 192)
                        .addBox(-1.90F, 0.20F, -1.95F, 3.80F, 11.80F, 3.90F),
                PartPose.offset(2.45F, 12.0F, 0.0F));
        leftLeg.addOrReplaceChild("thigh_plate",
                CubeListBuilder.create().texOffs(64, 192)
                        .addBox(-2.35F, -2.15F, -2.40F, 4.70F, 4.30F, 4.80F),
                PartPose.offset(0.0F, 2.45F, 0.0F));
        leftLeg.addOrReplaceChild("knee",
                CubeListBuilder.create().texOffs(128, 192)
                        .addBox(-2.60F, -0.70F, -2.65F, 5.20F, 1.40F, 5.30F),
                PartPose.offset(0.0F, 5.55F, -0.10F));
        leftLeg.addOrReplaceChild("shin",
                CubeListBuilder.create().texOffs(128, 192)
                        .addBox(-2.20F, -2.00F, -2.25F, 4.40F, 4.00F, 4.50F),
                PartPose.offset(0.0F, 8.35F, 0.0F));
        leftLeg.addOrReplaceChild("boot",
                CubeListBuilder.create().texOffs(192, 192)
                        .addBox(-2.55F, -1.10F, -3.80F, 5.10F, 2.20F, 6.20F),
                PartPose.offset(0.0F, 11.35F, -0.10F));

        return LayerDefinition.create(mesh, 512, 512);
    }

    @Override
    public void setupAnim(SkeletonRenderState state) {
        super.setupAnim(state);

        // Heavy neutral stance. The weapon stays outside the leg.
        this.rightArm.xRot -= 0.18F;
        this.rightArm.yRot -= 0.04F;
        this.rightArm.zRot += 0.03F;
        this.leftArm.xRot -= 0.06F;
        this.leftArm.zRot += 0.08F;
        this.body.xRot += Mth.sin(state.ageInTicks * 0.050F) * 0.008F;
        this.head.zRot += Mth.sin(state.ageInTicks * 0.028F) * 0.008F;

        // Deliberate two-arm greatsword chop.
        if (state.attackTime > 0.0F) {
            float t = state.attackTime;
            float swing = Mth.sin(t * Mth.PI);
            float follow = Mth.sin((1.0F - (1.0F - t) * (1.0F - t)) * Mth.PI);
            this.rightArm.xRot = -0.80F - 1.65F * swing + 0.22F * follow;
            this.rightArm.yRot = -0.22F + 0.18F * swing;
            this.rightArm.zRot = 0.02F;
            this.leftArm.xRot = -0.72F - 1.05F * swing;
            this.leftArm.yRot = 0.48F - 0.28F * swing;
            this.leftArm.zRot = -0.08F;
            this.body.yRot = 0.11F * swing;
        }

        this.sword.yRot = Mth.sin(state.ageInTicks * 0.075F) * 0.003F;
    }
}
