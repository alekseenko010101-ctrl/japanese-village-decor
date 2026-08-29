from pathlib import Path
import runpy

root = Path(__file__).resolve().parent
props_probe = (root / 'gradle.properties').read_text(encoding='utf-8')
if 'version=5.3.0' not in props_probe:
    runpy.run_path(str(root / 'prepare_v530.py'), run_name='__main__')

client = root / 'src/client/java/com/kasper/sleeplessknight/SleeplessKnightClient.java'
s = client.read_text(encoding='utf-8')
s = s.replace('        ClientTickEvents.END_CLIENT_TICK.register(DarkKnightFogManager::tick);\n', '')
client.write_text(s, encoding='utf-8')

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
    private static final RenderType FOG_RENDER_TYPE = RenderTypes.entityTranslucent(FOG_TEXTURE, false);

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
        return super.getBoundingBoxForCulling(entity).inflate(5.5D, 1.0D, 5.5D);
    }

    @Override
    public void submit(SkeletonRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submit(state, poseStack, submitNodeCollector, camera);
        if (state.isInvisible) return;
        float deathFade = state.deathTime > 0.0F ? Mth.clamp(1.0F - state.deathTime / 18.0F, 0.0F, 1.0F) : 1.0F;
        if (deathFade <= 0.01F) return;
        float intensity = (state.isAggressive ? 1.0F : 0.78F) * deathFade;
        float age = state.ageInTicks;
        int light = state.lightCoords;
        submitNodeCollector.order(-4).submitCustomGeometry(poseStack, FOG_RENDER_TYPE,
                (pose, buffer) -> renderFogVolume(pose, buffer, age, intensity, light));
    }

    private static void renderFogVolume(PoseStack.Pose pose, VertexConsumer buffer, float age, float intensity, int light) {
        float slow = age * 0.012F;
        horizontalSheet(pose, buffer, 4.20F * (1.0F + 0.035F * Mth.sin(slow)), 3.55F, 0.035F, slow * 0.17F, 118, intensity, light, false);
        horizontalSheet(pose, buffer, 3.65F, 3.15F * (1.0F + 0.045F * Mth.sin(slow + 1.7F)), 0.105F, -slow * 0.13F + 0.55F, 105, intensity, light, true);
        horizontalSheet(pose, buffer, 3.05F, 2.70F, 0.205F, slow * 0.10F + 1.15F, 88, intensity, light, false);
        float curtainAlpha = 72.0F * intensity;
        verticalCurtain(pose, buffer, 3.25F, 0.06F, 0.78F, slow * 0.06F, curtainAlpha, light, false);
        verticalCurtain(pose, buffer, 3.00F, 0.04F, 0.68F, slow * 0.06F + 0.78F, curtainAlpha * 0.88F, light, true);
        verticalCurtain(pose, buffer, 2.85F, 0.08F, 0.72F, slow * 0.06F + 1.56F, curtainAlpha * 0.78F, light, false);
        verticalCurtain(pose, buffer, 2.65F, 0.03F, 0.58F, slow * 0.06F + 2.34F, curtainAlpha * 0.70F, light, true);
        driftingPatch(pose, buffer, -1.45F + Mth.sin(slow * 0.7F) * 0.24F, 0.12F, 1.15F, 1.75F, 1.15F, -0.34F, 82.0F * intensity, light, true);
        driftingPatch(pose, buffer, 1.55F + Mth.sin(slow * 0.55F + 2.0F) * 0.22F, 0.16F, -0.95F, 1.55F, 1.05F, 0.44F, 74.0F * intensity, light, false);
    }

    private static void horizontalSheet(PoseStack.Pose pose, VertexConsumer buffer, float radiusX, float radiusZ, float y, float angle, int alpha, float intensity, int light, boolean mirrored) {
        float a = alpha * intensity;
        if (a <= 1.0F) return;
        float c = Mth.cos(angle), s = Mth.sin(angle);
        float[][] p = {{-radiusX,-radiusZ},{-radiusX,radiusZ},{radiusX,radiusZ},{radiusX,-radiusZ}};
        float[][] uv = mirrored ? new float[][]{{1,0},{0,0},{0,1},{1,1}} : new float[][]{{0,0},{0,1},{1,1},{1,0}};
        for (int i=0;i<4;i++) {
            float x=p[i][0]*c-p[i][1]*s, z=p[i][0]*s+p[i][1]*c;
            vertex(pose,buffer,x,y,z,uv[i][0],uv[i][1],174,164,188,(int)a,light,0,1,0);
        }
    }

    private static void verticalCurtain(PoseStack.Pose pose, VertexConsumer buffer, float halfWidth, float y0, float y1, float angle, float alpha, int light, boolean mirrored) {
        float dx=Mth.cos(angle)*halfWidth, dz=Mth.sin(angle)*halfWidth;
        float nx=-Mth.sin(angle), nz=Mth.cos(angle);
        float u0=mirrored?1.0F:0.0F, u1=mirrored?0.0F:1.0F;
        vertex(pose,buffer,-dx,y0,-dz,u0,1,156,146,172,(int)alpha,light,nx,0,nz);
        vertex(pose,buffer, dx,y0, dz,u1,1,156,146,172,(int)alpha,light,nx,0,nz);
        vertex(pose,buffer, dx,y1, dz,u1,0,156,146,172,(int)(alpha*0.60F),light,nx,0,nz);
        vertex(pose,buffer,-dx,y1,-dz,u0,0,156,146,172,(int)(alpha*0.60F),light,nx,0,nz);
    }

    private static void driftingPatch(PoseStack.Pose pose, VertexConsumer buffer, float cx, float y, float cz, float radiusX, float radiusZ, float angle, float alpha, int light, boolean mirrored) {
        float c=Mth.cos(angle), s=Mth.sin(angle);
        float[][] p={{-radiusX,-radiusZ},{-radiusX,radiusZ},{radiusX,radiusZ},{radiusX,-radiusZ}};
        float[][] uv=mirrored?new float[][]{{1,1},{1,0},{0,0},{0,1}}:new float[][]{{0,0},{0,1},{1,1},{1,0}};
        for(int i=0;i<4;i++){
            float x=cx+p[i][0]*c-p[i][1]*s, z=cz+p[i][0]*s+p[i][1]*c;
            vertex(pose,buffer,x,y,z,uv[i][0],uv[i][1],162,150,180,(int)alpha,light,0,1,0);
        }
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, int r, int g, int b, int a, int light, float nx, float ny, float nz) {
        buffer.addVertex(pose,x,y,z).setColor(r,g,b,Mth.clamp(a,0,255)).setUv(u,v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose,nx,ny,nz);
    }

    @Override
    protected void scale(SkeletonRenderState state, PoseStack poseStack) { poseStack.scale(1.45F,1.45F,1.45F); }
}
''', encoding='utf-8')

model = root / 'src/client/java/com/kasper/sleeplessknight/DarkKnightModel.java'
s = model.read_text(encoding='utf-8')
s = s.replace('.addBox(-2.70F, -7.05F, -1.78F, 5.40F, 6.55F, 4.38F)', '.addBox(-2.60F, -7.05F, -1.10F, 5.20F, 6.55F, 3.70F)', 1)
start = s.find('        // Front armour is split around a genuine T-shaped opening.')
end = s.find('\n        PartDefinition body = root.addOrReplaceChild("body",', start)
if start == -1 or end == -1:
    raise RuntimeError('Could not find helmet front block after 5.3.0 preparation')
front = r'''        // Real open visor: separate top plate, cheek plates and a deep black T cavity.
        head.addOrReplaceChild("visor_upper_plate", CubeListBuilder.create().texOffs(104, 0)
                        .addBox(-3.72F, -0.72F, -0.55F, 7.44F, 1.44F, 1.10F), PartPose.offset(0.0F, -6.72F, -3.08F));
        head.addOrReplaceChild("visor_top_rim", CubeListBuilder.create().texOffs(152, 0)
                        .addBox(-3.58F, -0.14F, -0.50F, 7.16F, 0.28F, 1.00F), PartPose.offset(0.0F, -5.92F, -3.12F));
        head.addOrReplaceChild("visor_bottom_rim_left", CubeListBuilder.create().texOffs(152, 0)
                        .addBox(-1.48F, -0.14F, -0.50F, 2.96F, 0.28F, 1.00F), PartPose.offset(-2.02F, -4.45F, -3.12F));
        head.addOrReplaceChild("visor_bottom_rim_right", CubeListBuilder.create().texOffs(152, 0)
                        .addBox(-1.48F, -0.14F, -0.50F, 2.96F, 0.28F, 1.00F), PartPose.offset(2.02F, -4.45F, -3.12F));
        head.addOrReplaceChild("left_face_plate", CubeListBuilder.create().texOffs(296, 0)
                        .addBox(-1.42F, -1.72F, -0.50F, 2.84F, 3.44F, 1.00F), PartPose.offsetAndRotation(-2.12F, -2.56F, -3.10F, 0.0F, -0.025F, 0.018F));
        head.addOrReplaceChild("right_face_plate", CubeListBuilder.create().texOffs(296, 0)
                        .addBox(-1.42F, -1.72F, -0.50F, 2.84F, 3.44F, 1.00F), PartPose.offsetAndRotation(2.12F, -2.56F, -3.10F, 0.0F, 0.025F, -0.018F));
        head.addOrReplaceChild("left_jaw", CubeListBuilder.create().texOffs(328, 0)
                        .addBox(-1.44F, -0.58F, -0.48F, 2.88F, 1.16F, 0.96F), PartPose.offset(-2.10F, -0.30F, -3.04F));
        head.addOrReplaceChild("right_jaw", CubeListBuilder.create().texOffs(328, 0)
                        .addBox(-1.44F, -0.58F, -0.48F, 2.88F, 1.16F, 0.96F), PartPose.offset(2.10F, -0.30F, -3.04F));
        head.addOrReplaceChild("visor_void_horizontal", CubeListBuilder.create().texOffs(448, 448)
                        .addBox(-3.62F, -0.70F, -0.22F, 7.24F, 1.40F, 0.44F), PartPose.offset(0.0F, -5.18F, -2.68F));
        head.addOrReplaceChild("visor_void_vertical", CubeListBuilder.create().texOffs(448, 472)
                        .addBox(-0.82F, -2.26F, -0.22F, 1.64F, 4.52F, 0.44F), PartPose.offset(0.0F, -2.45F, -2.68F));
'''
s = s[:start] + front + s[end:]
model.write_text(s, encoding='utf-8')

gen = root / 'generate_assets.py'
s = gen.read_text(encoding='utf-8')
s = s.replace("    rect(p,480,480,512,512,(1,1,3,255),0,False)\n", "    rect(p,448,448,512,512,(0,0,0,255),0,False)\n", 1)
make_fog = r'''
def make_fog():
    w=h=256
    fog_random=random.Random(48291)
    blobs=[(fog_random.uniform(30,226),fog_random.uniform(30,226),fog_random.uniform(25,70),fog_random.uniform(18,55),fog_random.uniform(0.35,0.9)) for _ in range(26)]
    holes=[(fog_random.uniform(50,206),fog_random.uniform(50,206),fog_random.uniform(18,45),fog_random.uniform(18,45),fog_random.uniform(0.2,0.6)) for _ in range(10)]
    pixels=[]
    for y in range(h):
        row=[]
        ny=(y-127.5)/127.5
        for x in range(w):
            nx=(x-127.5)/127.5
            radius=math.sqrt(nx*nx+ny*ny)
            edge=max(0.0,min(1.0,(1.05-radius)/0.28))
            density=0.0
            for cx,cy,rx,ry,weight in blobs:
                density += weight*math.exp(-(((x-cx)/rx)**2+((y-cy)/ry)**2)*2.0)
            for cx,cy,rx,ry,weight in holes:
                density -= weight*math.exp(-(((x-cx)/rx)**2+((y-cy)/ry)**2)*2.0)
            density += 0.10*math.sin(x*0.075+y*0.025)+0.08*math.sin(y*0.065-x*0.018)
            density=max(0.0,min(1.0,density*0.5))
            alpha=clamp(180*(density**0.85)*(edge**1.5))
            if alpha<4: alpha=0
            tone=clamp(104+20*density)
            row.append((clamp(tone-8),clamp(tone-12),clamp(tone+4),alpha))
        pixels.append(row)
    save_png(ROOT/'textures/effect/dark_knight_fog.png',w,h,pixels)

'''
if 'def make_fog()' not in s:
    s=s.replace('def make_json():\n',make_fog+'def make_json():\n',1)
s=s.replace("make_entity(); make_egg(); make_json()\nprint('Generated v4 knight texture and spawn egg assets')\n", "make_entity(); make_egg(); make_fog(); make_json()\nprint('Generated v4 knight texture, spawn egg, and custom fog texture')\n", 1)
gen.write_text(s, encoding='utf-8')

props=root/'gradle.properties'
p=props.read_text(encoding='utf-8')
if 'version=5.3.0' not in p: raise RuntimeError('Expected 5.3.0 before 5.4.0 bump')
props.write_text(p.replace('version=5.3.0','version=5.4.0',1),encoding='utf-8')
print('Prepared Sleepless Knight 5.4.0: custom fog renderer + rebuilt visor')
