from pathlib import Path
import runpy

root = Path(__file__).resolve().parent
props_probe = (root / 'gradle.properties').read_text(encoding='utf-8')
if 'version=5.4.0' not in props_probe:
    runpy.run_path(str(root / 'prepare_v540.py'), run_name='__main__')

# Replace the dark ground-covering sheets with a true mist volume made from
# translucent, softly textured fog cards. No particles and no giant floor quad.
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
        return super.getBoundingBoxForCulling(entity).inflate(5.0D, 1.4D, 5.0D);
    }

    @Override
    public void submit(SkeletonRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        if (state.isInvisible) return;

        float deathFade = state.deathTime > 0.0F
                ? Mth.clamp(1.0F - state.deathTime / 18.0F, 0.0F, 1.0F)
                : 1.0F;
        if (deathFade <= 0.01F) return;

        float intensity = (state.isAggressive ? 1.0F : 0.78F) * deathFade;
        float age = state.ageInTicks;

        submitNodeCollector.order(3).submitCustomGeometry(
                poseStack,
                FOG_RENDER_TYPE,
                (pose, buffer) -> renderFogVolume(pose, buffer, age, intensity)
        );
    }

    private static void renderFogVolume(PoseStack.Pose pose, VertexConsumer buffer, float age, float intensity) {
        float t = age * 0.016F;

        // Soft wide haze crossing through the body area. These are vertical cards,
        // not floor overlays, so they read as suspended mist instead of a shadow.
        fogCard(pose, buffer, 0.00F, 0.03F, 0.00F, 3.50F, 0.92F, t * 0.035F, 32.0F * intensity, false);
        fogCard(pose, buffer, 0.05F, 0.06F, 0.02F, 3.10F, 0.78F, 1.5708F - t * 0.028F, 28.0F * intensity, true);

        // Dense drifting clusters around the legs. Each cluster is crossed by a
        // second translucent card to create volume from any camera angle.
        fogCluster(pose, buffer,
                -1.55F + Mth.sin(t * 0.73F) * 0.22F,
                0.04F,
                0.82F + Mth.cos(t * 0.54F) * 0.18F,
                1.90F, 0.78F, 0.22F + t * 0.045F,
                68.0F * intensity, false);

        fogCluster(pose, buffer,
                1.55F + Mth.sin(t * 0.58F + 1.8F) * 0.25F,
                0.07F,
                -0.82F + Mth.cos(t * 0.66F + 0.7F) * 0.20F,
                1.82F, 0.74F, -0.38F - t * 0.038F,
                62.0F * intensity, true);

        fogCluster(pose, buffer,
                -0.22F + Mth.sin(t * 0.42F + 2.5F) * 0.18F,
                0.02F,
                -1.72F + Mth.cos(t * 0.48F) * 0.18F,
                1.65F, 0.62F, 0.86F + t * 0.030F,
                54.0F * intensity, false);

        fogCluster(pose, buffer,
                0.72F + Mth.sin(t * 0.51F + 4.1F) * 0.20F,
                0.05F,
                1.62F + Mth.cos(t * 0.40F + 1.5F) * 0.16F,
                1.58F, 0.60F, -0.92F + t * 0.032F,
                50.0F * intensity, true);

        // Outer wisps make the fog spread naturally instead of ending in a circle.
        fogCard(pose, buffer, -2.65F, 0.06F, -0.55F, 1.45F, 0.55F, 0.30F, 38.0F * intensity, true);
        fogCard(pose, buffer,  2.58F, 0.08F,  0.72F, 1.35F, 0.52F, -0.42F, 36.0F * intensity, false);
    }

    private static void fogCluster(PoseStack.Pose pose, VertexConsumer buffer,
                                   float cx, float y0, float cz,
                                   float halfWidth, float height, float angle,
                                   float alpha, boolean mirrored) {
        fogCard(pose, buffer, cx, y0, cz, halfWidth, height, angle, alpha, mirrored);
        fogCard(pose, buffer, cx, y0 + 0.025F, cz,
                halfWidth * 0.88F, height * 0.92F,
                angle + 1.5708F,
                alpha * 0.56F,
                !mirrored);
    }

    private static void fogCard(PoseStack.Pose pose, VertexConsumer buffer,
                                float cx, float y0, float cz,
                                float halfWidth, float height, float angle,
                                float alpha, boolean mirrored) {
        if (alpha <= 1.0F) return;

        float dx = Mth.cos(angle) * halfWidth;
        float dz = Mth.sin(angle) * halfWidth;
        float nx = -Mth.sin(angle);
        float nz = Mth.cos(angle);
        float u0 = mirrored ? 1.0F : 0.0F;
        float u1 = mirrored ? 0.0F : 1.0F;
        int a = Mth.clamp((int)alpha, 0, 255);

        // Silver-grey with a very slight violet cast. Full-bright prevents local
        // darkness from turning the mist into a black stain on the terrain.
        vertex(pose, buffer, cx - dx, y0,          cz - dz, u0, 1.0F, 235, 232, 242, a, FULL_BRIGHT, nx, 0.0F, nz);
        vertex(pose, buffer, cx + dx, y0,          cz + dz, u1, 1.0F, 235, 232, 242, a, FULL_BRIGHT, nx, 0.0F, nz);
        vertex(pose, buffer, cx + dx, y0 + height, cz + dz, u1, 0.0F, 235, 232, 242, a, FULL_BRIGHT, nx, 0.0F, nz);
        vertex(pose, buffer, cx - dx, y0 + height, cz - dz, u0, 0.0F, 235, 232, 242, a, FULL_BRIGHT, nx, 0.0F, nz);
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

# Replace the fog texture generator with a pale, highly transparent wispy texture.
# The RGB is deliberately light so alpha blending creates mist rather than a shadow.
gen = root / 'generate_assets.py'
s = gen.read_text(encoding='utf-8')
start = s.find('def make_fog():')
end = s.find('def make_json():', start)
if start == -1 or end == -1:
    raise RuntimeError('Could not find make_fog() generated by 5.4.0')

make_fog = r'''def make_fog():
    w=h=256
    fog_random=random.Random(83129)
    blobs=[(fog_random.uniform(30,226),fog_random.uniform(80,185),fog_random.uniform(22,60),fog_random.uniform(10,32),fog_random.uniform(0.35,0.90)) for _ in range(22)]
    holes=[(fog_random.uniform(35,221),fog_random.uniform(70,195),fog_random.uniform(15,42),fog_random.uniform(10,25),fog_random.uniform(0.20,0.65)) for _ in range(12)]
    pixels=[]
    for y in range(h):
        row=[]
        ny=(y-127.5)/127.5
        vertical_fade=max(0.0,min(1.0,(y-35.0)/40.0))*max(0.0,min(1.0,(225.0-y)/45.0))
        for x in range(w):
            nx=(x-127.5)/127.5
            radius=math.sqrt((nx/1.03)**2+(ny/0.82)**2)
            edge=max(0.0,min(1.0,(1.05-radius)/0.22))
            density=0.0
            for cx,cy,rx,ry,weight in blobs:
                density += weight*math.exp(-(((x-cx)/rx)**2+((y-cy)/ry)**2)*2.2)
            for cx,cy,rx,ry,weight in holes:
                density -= weight*math.exp(-(((x-cx)/rx)**2+((y-cy)/ry)**2)*2.0)
            density=max(0.0,min(1.0,density*0.43))
            density*=0.88+0.12*math.sin(x*0.06+y*0.02)
            density=max(0.0,min(1.0,density))
            alpha=clamp(145*density*edge*vertical_fade)
            if alpha<4: alpha=0
            row.append((235,232,242,alpha))
        pixels.append(row)
    save_png(ROOT/'textures/effect/dark_knight_fog.png',w,h,pixels)

'''
s = s[:start] + make_fog + s[end:]
gen.write_text(s, encoding='utf-8')

props = root / 'gradle.properties'
p = props.read_text(encoding='utf-8')
if 'version=5.4.0' not in p:
    raise RuntimeError('Expected 5.4.0 before 5.4.1 bump')
props.write_text(p.replace('version=5.4.0', 'version=5.4.1', 1), encoding='utf-8')

print('Prepared Sleepless Knight 5.4.1: pale volumetric mist cards, no floor shadow')
