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
 * Dark Knight model rebuilt to avoid z-fighting.
 *
 * Important rule used here: visible armor cuboids never occupy the same volume
 * and never share coplanar outer faces. Small air gaps are intentional. This
 * prevents the rapid flicker that appeared when the old layered boxes crossed
 * through each other.
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

        // ---------------- HEAD ----------------
        // One solid helmet shell. The old second helmet cube intersected this
        // shell and was the strongest source of flicker around the face.
        PartDefinition head = root.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5F, -10.0F, -4.5F, 9.0F, 10.0F, 9.0F),
                PartPose.ZERO
        );

        // Top flare is physically above the helmet with a 0.20 block-model gap.
        head.addOrReplaceChild(
                "helmet_flare",
                CubeListBuilder.create()
                        .texOffs(72, 0)
                        .addBox(-6.4F, -0.55F, -5.0F, 12.8F, 1.1F, 10.0F),
                PartPose.offset(0.0F, -10.75F, 0.0F)
        );

        // Thin visor sits in front of the helmet instead of inside it.
        head.addOrReplaceChild(
                "visor",
                CubeListBuilder.create()
                        .texOffs(120, 0)
                        .addBox(-3.4F, -0.8F, -0.08F, 6.8F, 1.6F, 0.16F),
                PartPose.offset(0.0F, -5.15F, -4.72F)
        );
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        // ---------------- BODY ----------------
        // Main breastplate ends before the belt. Collar and belt are separate
        // pieces with visible air gaps rather than boxes layered through it.
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 48)
                        .addBox(-5.0F, 0.35F, -3.0F, 10.0F, 11.15F, 6.0F),
                PartPose.ZERO
        );

        // Collar consists of four disjoint plates above the breastplate.
        body.addOrReplaceChild(
                "collar_front",
                CubeListBuilder.create().texOffs(40, 48)
                        .addBox(-4.8F, -0.35F, -0.35F, 9.6F, 0.7F, 0.7F),
                PartPose.offset(0.0F, -0.25F, -3.25F)
        );
        body.addOrReplaceChild(
                "collar_back",
                CubeListBuilder.create().texOffs(40, 56)
                        .addBox(-4.8F, -0.35F, -0.35F, 9.6F, 0.7F, 0.7F),
                PartPose.offset(0.0F, -0.25F, 3.25F)
        );
        body.addOrReplaceChild(
                "collar_left",
                CubeListBuilder.create().texOffs(80, 48)
                        .addBox(-0.35F, -0.35F, -2.55F, 0.7F, 0.7F, 5.1F),
                PartPose.offset(5.25F, -0.25F, 0.0F)
        );
        body.addOrReplaceChild(
                "collar_right",
                CubeListBuilder.create().texOffs(92, 48)
                        .addBox(-0.35F, -0.35F, -2.55F, 0.7F, 0.7F, 5.1F),
                PartPose.offset(-5.25F, -0.25F, 0.0F)
        );

        // Belt sits below the body rather than intersecting its lower rows.
        body.addOrReplaceChild(
                "belt",
                CubeListBuilder.create().texOffs(80, 56)
                        .addBox(-5.25F, -0.6F, -3.25F, 10.5F, 1.2F, 6.5F),
                PartPose.offset(0.0F, 12.25F, 0.0F)
        );

        // Chain skirt is made of front/back panels, not a solid box surrounding
        // and clipping through both legs.
        body.addOrReplaceChild(
                "chain_skirt_front",
                CubeListBuilder.create().texOffs(0, 80)
                        .addBox(-4.7F, 0.0F, -0.15F, 9.4F, 6.3F, 0.3F),
                PartPose.offset(0.0F, 13.05F, -2.95F)
        );
        body.addOrReplaceChild(
                "chain_skirt_back",
                CubeListBuilder.create().texOffs(20, 80)
                        .addBox(-4.7F, 0.0F, -0.15F, 9.4F, 6.3F, 0.3F),
                PartPose.offset(0.0F, 13.05F, 2.95F)
        );

        // Tassets are placed further forward than the chain panel with a gap.
        body.addOrReplaceChild(
                "right_tasset",
                CubeListBuilder.create().texOffs(40, 80)
                        .addBox(-3.5F, 0.0F, -0.12F, 3.4F, 6.7F, 0.24F),
                PartPose.offset(-0.35F, 12.95F, -3.32F)
        );
        body.addOrReplaceChild(
                "left_tasset",
                CubeListBuilder.create().texOffs(56, 80)
                        .addBox(0.1F, 0.0F, -0.12F, 3.4F, 6.7F, 0.24F),
                PartPose.offset(0.35F, 12.95F, -3.32F)
        );

        // Rune pieces float slightly in front of the chest. None touches the
        // breastplate surface, so shaders cannot fight over the same depth.
        body.addOrReplaceChild(
                "rune_vertical",
                CubeListBuilder.create().texOffs(192, 0)
                        .addBox(-0.5F, -2.8F, -0.05F, 1.0F, 5.6F, 0.10F),
                PartPose.offset(0.0F, 5.55F, -3.24F)
        );
        body.addOrReplaceChild(
                "rune_cross",
                CubeListBuilder.create().texOffs(208, 0)
                        .addBox(-2.7F, -0.5F, -0.05F, 5.4F, 1.0F, 0.10F),
                PartPose.offset(0.0F, 4.65F, -3.24F)
        );
        body.addOrReplaceChild(
                "rune_lower",
                CubeListBuilder.create().texOffs(224, 0)
                        .addBox(-1.55F, -0.4F, -0.05F, 3.1F, 0.8F, 0.10F),
                PartPose.offset(0.0F, 7.25F, -3.24F)
        );

        // ---------------- ARMS ----------------
        // Arms are moved outward so their upper-arm boxes do not enter the torso.
        PartDefinition rightArm = root.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(64, 64).addBox(-2.6F, -1.5F, -2.6F, 4.8F, 4.3F, 5.2F)
                        .texOffs(96, 64).addBox(-2.45F, 3.05F, -2.45F, 4.5F, 1.3F, 4.9F)
                        .texOffs(120, 64).addBox(-2.30F, 4.60F, -2.30F, 4.2F, 4.8F, 4.6F)
                        .texOffs(120, 76).addBox(-2.50F, 9.65F, -2.50F, 4.6F, 2.25F, 5.0F),
                PartPose.offset(-7.2F, 2.0F, 0.0F)
        );
        rightArm.addOrReplaceChild(
                "pauldron",
                CubeListBuilder.create().texOffs(144, 64)
                        .addBox(-3.7F, -0.65F, -3.2F, 6.7F, 1.3F, 6.4F),
                PartPose.offset(-0.2F, -2.45F, 0.0F)
        );

        // Sword grip is offset from the forearm instead of passing through the
        // same center volume. The blade/crossguard still form one weapon.
        PartDefinition sword = rightArm.addOrReplaceChild(
                "greatsword",
                CubeListBuilder.create()
                        .texOffs(192, 128).addBox(-0.8F, -0.8F, -0.8F, 1.6F, 5.5F, 1.6F)
                        .texOffs(208, 128).addBox(-4.8F, 4.9F, -0.85F, 9.6F, 1.5F, 1.7F)
                        .texOffs(0, 160).addBox(-2.15F, 6.65F, -0.60F, 4.3F, 19.0F, 1.2F)
                        .texOffs(32, 160).addBox(-1.25F, 25.9F, -0.52F, 2.5F, 3.5F, 1.04F),
                PartPose.offsetAndRotation(1.65F, 9.3F, 0.45F, 0.08F, 0.0F, -0.52F)
        );
        sword.addOrReplaceChild(
                "fuller",
                CubeListBuilder.create().texOffs(48, 160)
                        .addBox(-0.42F, 0.0F, -0.07F, 0.84F, 16.5F, 0.14F),
                PartPose.offset(0.0F, 8.0F, -0.69F)
        );

        PartDefinition leftArm = root.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(64, 96).mirror().addBox(-2.2F, -1.5F, -2.6F, 4.8F, 4.3F, 5.2F)
                        .texOffs(96, 96).mirror().addBox(-2.05F, 3.05F, -2.45F, 4.5F, 1.3F, 4.9F)
                        .texOffs(120, 96).mirror().addBox(-1.90F, 4.60F, -2.30F, 4.2F, 4.8F, 4.6F)
                        .texOffs(120, 108).mirror().addBox(-2.10F, 9.65F, -2.50F, 4.6F, 2.25F, 5.0F),
                PartPose.offset(7.2F, 2.0F, 0.0F)
        );
        leftArm.addOrReplaceChild(
                "pauldron",
                CubeListBuilder.create().texOffs(144, 96).mirror()
                        .addBox(-3.0F, -0.65F, -3.2F, 6.7F, 1.3F, 6.4F),
                PartPose.offset(0.2F, -2.45F, 0.0F)
        );

        // ---------------- LEGS ----------------
        // Four separated armor sections per leg: thigh, knee, shin, sabaton.
        root.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create()
                        .texOffs(0, 112).addBox(-2.20F, 0.25F, -2.30F, 4.2F, 4.35F, 4.6F)
                        .texOffs(24, 112).addBox(-2.35F, 4.90F, -2.45F, 4.5F, 1.35F, 4.9F)
                        .texOffs(48, 112).addBox(-2.15F, 6.55F, -2.25F, 4.1F, 4.35F, 4.5F)
                        .texOffs(72, 112).addBox(-2.45F, 11.20F, -3.75F, 4.7F, 2.0F, 6.2F),
                PartPose.offset(-2.5F, 12.0F, 0.0F)
        );
        root.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create()
                        .texOffs(96, 112).mirror().addBox(-2.00F, 0.25F, -2.30F, 4.2F, 4.35F, 4.6F)
                        .texOffs(120, 112).mirror().addBox(-2.15F, 4.90F, -2.45F, 4.5F, 1.35F, 4.9F)
                        .texOffs(144, 112).mirror().addBox(-1.95F, 6.55F, -2.25F, 4.1F, 4.35F, 4.5F)
                        .texOffs(168, 112).mirror().addBox(-2.25F, 11.20F, -3.75F, 4.7F, 2.0F, 6.2F),
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
