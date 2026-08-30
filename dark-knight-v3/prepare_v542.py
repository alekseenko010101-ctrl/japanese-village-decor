from pathlib import Path
import runpy

root = Path(__file__).resolve().parent
props_probe = (root / 'gradle.properties').read_text(encoding='utf-8')
if 'version=5.4.1' not in props_probe:
    runpy.run_path(str(root / 'prepare_v541.py'), run_name='__main__')

# 1) Replace the fixed world-facing fog cards with camera-facing mist billboards.
# The previous fixed cards could become nearly edge-on and show as a thin stripe.
renderer = root / 'src/client/java/com/kasper/sleeplessknight/DarkKnightRenderer.java'
renderer.write_text(r'''package com.kasper.sleeplessknight;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;

public final class DarkKnightRenderer extends HumanoidMobRenderer<DarkKnightEntity, SkeletonRenderState, DarkKnightModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(SleeplessKnight.MOD_ID, "textures/entity/dark_knight.png");
    private static final Identifier FOG_TEXTURE = Identifier.fromNamespaceAndPath(SleeplessKnight.MOD_ID, "textures/effect/dark_knight_fog.png");
    private static final RenderType FOG_RENDER_TYPE = RenderTypes.entityTranslucentEmissive(FOG_TEXTURE, false);
    private static final int FULL_BRIGHT = 0x00F000F0;

    public DarkKnightRenderer(EntityRendererProvider.Context context) {
        super(context, new DarkKnightModel(DarkKnightModel.createBodyLayer().bakeRoot()), 1.25F);
    }

    @Override public Identifier getTextureLocation(SkeletonRenderState state) { return TEXTURE; }
    @Override public SkeletonRenderState createRenderState() { return new SkeletonRenderState(); }

    @Override
    public void extractRenderState(DarkKnightEntity entity, SkeletonRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.isAggressive = entity.isAggressive();
        state.isHoldingBow = false;
        state.isShaking = false;
    }

    @Override
    protected AABB getBoundingBoxForCulling(DarkKnightEntity entity) {
        return super.getBoundingBoxForCulling(entity).inflate(5.2D, 1.6D, 5.2D);
    }

    @Override
    public void submit(SkeletonRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        if (state.isInvisible) return;

        float deathFade = state.deathTime > 0.0F
                ? Mth.clamp(1.0F - state.deathTime / 18.0F, 0.0F, 1.0F)
                : 1.0F;
        if (deathFade <= 0.01F) return;

        float intensity = (state.isAggressive ? 1.0F : 0.76F) * deathFade;
        float age = state.ageInTicks;

        // Camera-facing span vector. Every fog card faces the player, so none can collapse
        // into the thin straight seam that was visible in 5.4.1.
        float vx = (float)(camera.pos.x - state.x);
        float vz = (float)(camera.pos.z - state.z);
        float len = Mth.sqrt(vx * vx + vz * vz);
        float spanX = len > 0.001F ? -vz / len : 1.0F;
        float spanZ = len > 0.001F ?  vx / len : 0.0F;

        submitNodeCollector.order(3).submitCustomGeometry(
                poseStack,
                FOG_RENDER_TYPE,
                (pose, buffer) -> renderFogVolume(pose, buffer, age, intensity, spanX, spanZ)
        );
    }

    private static void renderFogVolume(PoseStack.Pose pose, VertexConsumer buffer,
                                        float age, float intensity, float spanX, float spanZ) {
        float t = age * 0.014F;

        // Soft overlapping wisps around the ankles. No floor quad, no particles,
        // no fixed edge-on planes. Bottoms are kept slightly above the ground and
        // the texture itself fades to zero on every edge.
        mistBillboard(pose, buffer,
                -1.55F + Mth.sin(t * 0.77F) * 0.22F, 0.10F,
                 0.80F + Mth.cos(t * 0.54F) * 0.18F,
                2.10F, 0.92F, 52.0F * intensity, spanX, spanZ, false);

        mistBillboard(pose, buffer,
                 1.62F + Mth.sin(t * 0.63F + 1.9F) * 0.24F, 0.12F,
                -0.78F + Mth.cos(t * 0.67F + 0.6F) * 0.19F,
                2.00F, 0.88F, 49.0F * intensity, spanX, spanZ, true);

        mistBillboard(pose, buffer,
                -0.28F + Mth.sin(t * 0.46F + 2.3F) * 0.18F, 0.08F,
                -1.85F + Mth.cos(t * 0.49F) * 0.18F,
                1.85F, 0.78F, 43.0F * intensity, spanX, spanZ, false);

        mistBillboard(pose, buffer,
                 0.72F + Mth.sin(t * 0.52F + 4.0F) * 0.19F, 0.11F,
                 1.72F + Mth.cos(t * 0.41F + 1.5F) * 0.16F,
                1.75F, 0.74F, 40.0F * intensity, spanX, spanZ, true);

        // Thin outer wisps make the boundary irregular instead of circular.
        mistBillboard(pose, buffer,
                -2.70F + Mth.sin(t * 0.35F) * 0.16F, 0.14F, -0.48F,
                1.35F, 0.62F, 30.0F * intensity, spanX, spanZ, true);
        mistBillboard(pose, buffer,
                 2.62F + Mth.cos(t * 0.38F) * 0.15F, 0.13F,  0.62F,
                1.30F, 0.60F, 29.0F * intensity, spanX, spanZ, false);

        // A second depth layer behind the legs gives thickness while remaining billboarded.
        mistBillboard(pose, buffer,
                -0.95F, 0.18F, 0.18F,
                2.55F, 1.02F, 28.0F * intensity, spanX, spanZ, true);
        mistBillboard(pose, buffer,
                 1.08F, 0.16F, -0.12F,
                2.40F, 0.96F, 27.0F * intensity, spanX, spanZ, false);
    }

    private static void mistBillboard(PoseStack.Pose pose, VertexConsumer buffer,
                                      float cx, float y0, float cz,
                                      float halfWidth, float height, float alpha,
                                      float spanX, float spanZ, boolean mirrored) {
        if (alpha <= 1.0F) return;

        float dx = spanX * halfWidth;
        float dz = spanZ * halfWidth;
        float nx = -spanZ;
        float nz = spanX;
        float u0 = mirrored ? 1.0F : 0.0F;
        float u1 = mirrored ? 0.0F : 1.0F;
        int a = Mth.clamp((int)alpha, 0, 255);

        vertex(pose, buffer, cx - dx, y0,          cz - dz, u0, 1.0F, 222, 216, 232, a, FULL_BRIGHT, nx, 0.0F, nz);
        vertex(pose, buffer, cx + dx, y0,          cz + dz, u1, 1.0F, 222, 216, 232, a, FULL_BRIGHT, nx, 0.0F, nz);
        vertex(pose, buffer, cx + dx, y0 + height, cz + dz, u1, 0.0F, 222, 216, 232, a, FULL_BRIGHT, nx, 0.0F, nz);
        vertex(pose, buffer, cx - dx, y0 + height, cz - dz, u0, 0.0F, 222, 216, 232, a, FULL_BRIGHT, nx, 0.0F, nz);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer,
                               float x, float y, float z,
                               float u, float v,
                               int r, int g, int b, int a,
                               int light,
                               float nx, float ny, float nz) {
        buffer.addVertex(pose, x, y, z)
                .setColor(r, g, b, a)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    @Override
    protected void scale(SkeletonRenderState state, PoseStack poseStack) {
        poseStack.scale(1.45F, 1.45F, 1.45F);
    }
}
''', encoding='utf-8')

# 2) Rebuild the face of the helmet again, this time with a deliberately large,
# unmistakable black T-shaped opening and a raised lighter rim around it.
model = root / 'src/client/java/com/kasper/sleeplessknight/DarkKnightModel.java'
s = model.read_text(encoding='utf-8')
start = s.find('        // Real open visor:')
end = s.find('\n        PartDefinition body = root.addOrReplaceChild("body",', start)
if start == -1 or end == -1:
    raise RuntimeError('Could not locate 5.4.1 visor block')

front = r'''        // Strong readable great-helm face: a large physical T opening with a raised rim.
        // The black backing sits deep behind the armour so it cannot look like paint on the surface.
        head.addOrReplaceChild("visor_forehead", CubeListBuilder.create().texOffs(104, 0)
                        .addBox(-3.78F, -0.55F, -0.56F, 7.56F, 1.10F, 1.12F),
                PartPose.offset(0.0F, -6.88F, -3.10F));

        // Raised upper rim catches light and makes the black eye slit readable even at night.
        head.addOrReplaceChild("visor_rim_top", CubeListBuilder.create().texOffs(400, 400)
                        .addBox(-3.66F, -0.18F, -0.58F, 7.32F, 0.36F, 1.16F),
                PartPose.offset(0.0F, -6.18F, -3.18F));
        head.addOrReplaceChild("visor_rim_bottom_left", CubeListBuilder.create().texOffs(400, 400)
                        .addBox(-1.52F, -0.18F, -0.58F, 3.04F, 0.36F, 1.16F),
                PartPose.offset(-2.12F, -4.42F, -3.18F));
        head.addOrReplaceChild("visor_rim_bottom_right", CubeListBuilder.create().texOffs(400, 400)
                        .addBox(-1.52F, -0.18F, -0.58F, 3.04F, 0.36F, 1.16F),
                PartPose.offset(2.12F, -4.42F, -3.18F));

        // Cheek plates are separated far enough to leave the vertical part of the T truly open.
        head.addOrReplaceChild("left_face_plate", CubeListBuilder.create().texOffs(296, 0)
                        .addBox(-1.48F, -1.70F, -0.52F, 2.96F, 3.40F, 1.04F),
                PartPose.offsetAndRotation(-2.20F, -2.52F, -3.12F, 0.0F, -0.030F, 0.020F));
        head.addOrReplaceChild("right_face_plate", CubeListBuilder.create().texOffs(296, 0)
                        .addBox(-1.48F, -1.70F, -0.52F, 2.96F, 3.40F, 1.04F),
                PartPose.offsetAndRotation(2.20F, -2.52F, -3.12F, 0.0F, 0.030F, -0.020F));
        head.addOrReplaceChild("left_jaw", CubeListBuilder.create().texOffs(328, 0)
                        .addBox(-1.48F, -0.58F, -0.50F, 2.96F, 1.16F, 1.00F),
                PartPose.offset(-2.18F, -0.28F, -3.08F));
        head.addOrReplaceChild("right_jaw", CubeListBuilder.create().texOffs(328, 0)
                        .addBox(-1.48F, -0.58F, -0.50F, 2.96F, 1.16F, 1.00F),
                PartPose.offset(2.18F, -0.28F, -3.08F));

        // Deep matte-black cavity. Horizontal slit is ~1.55 model units tall;
        // the vertical slot continues almost to the jaw line.
        head.addOrReplaceChild("visor_void_horizontal", CubeListBuilder.create().texOffs(448, 448)
                        .addBox(-3.62F, -0.78F, -0.26F, 7.24F, 1.56F, 0.52F),
                PartPose.offset(0.0F, -5.28F, -2.72F));
        head.addOrReplaceChild("visor_void_vertical", CubeListBuilder.create().texOffs(448, 472)
                        .addBox(-0.78F, -2.18F, -0.26F, 1.56F, 4.36F, 0.52F),
                PartPose.offset(0.0F, -2.52F, -2.72F));
'''
s = s[:start] + front + s[end:]
model.write_text(s, encoding='utf-8')

# 3) Give the visor rim its own slightly lighter muted-purple texture patch.
gen = root / 'generate_assets.py'
s = gen.read_text(encoding='utf-8')
needle = "    rect(p,448,448,512,512,(0,0,0,255),0,False)\n"
if needle not in s:
    raise RuntimeError('Black visor texture patch not found')
s = s.replace(needle, needle + "    rect(p,400,400,448,448,(104,92,132,255),4,False)\n", 1)

# Strengthen the fade at every texture edge so no straight quad border can remain visible.
start = s.find('def make_fog():')
end = s.find('def make_json():', start)
if start == -1 or end == -1:
    raise RuntimeError('Could not locate make_fog()')
make_fog = r'''def make_fog():
    w=h=256
    fog_random=random.Random(92417)
    blobs=[(fog_random.uniform(34,222),fog_random.uniform(72,190),fog_random.uniform(28,68),fog_random.uniform(13,38),fog_random.uniform(0.35,0.92)) for _ in range(26)]
    holes=[(fog_random.uniform(40,216),fog_random.uniform(72,192),fog_random.uniform(18,46),fog_random.uniform(11,30),fog_random.uniform(0.18,0.62)) for _ in range(14)]
    pixels=[]
    for y in range(h):
        row=[]
        # Completely transparent top/bottom margins, then very soft ramps.
        top_fade=max(0.0,min(1.0,(y-48.0)/48.0))
        bottom_fade=max(0.0,min(1.0,(216.0-y)/54.0))
        for x in range(w):
            # Wide transparent side margins eliminate visible rectangular card edges.
            side_fade=max(0.0,min(1.0,(x-30.0)/52.0))*max(0.0,min(1.0,(226.0-x)/52.0))
            density=0.0
            for cx,cy,rx,ry,weight in blobs:
                density += weight*math.exp(-(((x-cx)/rx)**2+((y-cy)/ry)**2)*2.15)
            for cx,cy,rx,ry,weight in holes:
                density -= weight*math.exp(-(((x-cx)/rx)**2+((y-cy)/ry)**2)*2.0)
            density=max(0.0,min(1.0,density*0.40))
            density*=0.90+0.10*math.sin(x*0.047+y*0.021)
            density=max(0.0,min(1.0,density))
            alpha=clamp(128*density*top_fade*bottom_fade*side_fade)
            if alpha<5: alpha=0
            row.append((226,220,234,alpha))
        pixels.append(row)
    save_png(ROOT/'textures/effect/dark_knight_fog.png',w,h,pixels)

'''
s = s[:start] + make_fog + s[end:]
gen.write_text(s, encoding='utf-8')

props = root / 'gradle.properties'
p = props.read_text(encoding='utf-8')
if 'version=5.4.1' not in p:
    raise RuntimeError('Expected 5.4.1 before 5.4.2 bump')
props.write_text(p.replace('version=5.4.1', 'version=5.4.2', 1), encoding='utf-8')

print('Prepared Sleepless Knight 5.4.2: camera-facing mist + strong black visor opening')
