from pathlib import Path
import runpy

root = Path(__file__).resolve().parent

# Bring branch sources to 5.2.8 first.
runpy.run_path(str(root / "prepare_v528.py"), run_name="__main__")

# Replace the particle blizzard with low, restrained ground fog.
fog = root / "src/client/java/com/kasper/sleeplessknight/DarkKnightFogManager.java"
fog.write_text('''package com.kasper.sleeplessknight;\n\nimport net.minecraft.client.Minecraft;\nimport net.minecraft.core.particles.ParticleTypes;\nimport net.minecraft.core.particles.PowerParticleOption;\nimport net.minecraft.world.phys.AABB;\n\nimport java.util.concurrent.ThreadLocalRandom;\n\n/** Local low ground fog around the Dark Knight. */\npublic final class DarkKnightFogManager {\n    private static final double RENDER_RANGE = 48.0D;\n    private static final double RENDER_RANGE_SQR = RENDER_RANGE * RENDER_RANGE;\n\n    private DarkKnightFogManager() {}\n\n    public static void tick(Minecraft minecraft) {\n        if (minecraft.level == null || minecraft.player == null) return;\n\n        var level = minecraft.level;\n        var player = minecraft.player;\n        AABB search = player.getBoundingBox().inflate(RENDER_RANGE);\n\n        for (DarkKnightEntity knight : level.getEntitiesOfClass(DarkKnightEntity.class, search)) {\n            if (!knight.isAlive() || knight.distanceToSqr(player) > RENDER_RANGE_SQR) continue;\n\n            boolean fighting = DarkKnightMusicManager.isFightActiveFor(knight);\n            ThreadLocalRandom random = ThreadLocalRandom.current();\n            double radius = fighting ? 4.8D : 3.3D;\n            double maxHeight = fighting ? 0.55D : 0.35D;\n\n            // Main body: low dark smoke that stays around the feet.\n            int smokeCount = fighting ? 4 : 2;\n            for (int i = 0; i < smokeCount; i++) {\n                double angle = random.nextDouble(0.0D, Math.PI * 2.0D);\n                double r = Math.sqrt(random.nextDouble()) * radius;\n                double x = knight.getX() + Math.cos(angle) * r;\n                double z = knight.getZ() + Math.sin(angle) * r;\n                double y = knight.getY() + 0.03D + random.nextDouble() * maxHeight;\n                level.addParticle(ParticleTypes.SMOKE, x, y, z,\n                        random.nextDouble(-0.003D, 0.003D),\n                        random.nextDouble(0.0008D, 0.003D),\n                        random.nextDouble(-0.003D, 0.003D));\n            }\n\n            // Small amount of violet haze, kept near ground level.\n            int violetCount = fighting ? 2 : 1;\n            for (int i = 0; i < violetCount; i++) {\n                double angle = random.nextDouble(0.0D, Math.PI * 2.0D);\n                double r = Math.sqrt(random.nextDouble()) * (radius * 0.9D);\n                double x = knight.getX() + Math.cos(angle) * r;\n                double z = knight.getZ() + Math.sin(angle) * r;\n                double y = knight.getY() + 0.02D + random.nextDouble() * (maxHeight * 0.75D);\n                double vx = (knight.getX() - x) * 0.0014D + random.nextDouble(-0.002D, 0.002D);\n                double vz = (knight.getZ() - z) * 0.0014D + random.nextDouble(-0.002D, 0.002D);\n                level.addParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, fighting ? 0.18F : 0.12F),\n                        x, y, z, vx, random.nextDouble(0.0008D, 0.003D), vz);\n            }\n\n            // Very rare combat mote.\n            if (fighting && random.nextInt(10) == 0) {\n                double angle = random.nextDouble(0.0D, Math.PI * 2.0D);\n                double r = random.nextDouble(0.5D, 2.4D);\n                level.addParticle(ParticleTypes.PORTAL,\n                        knight.getX() + Math.cos(angle) * r,\n                        knight.getY() + random.nextDouble(0.25D, 1.1D),\n                        knight.getZ() + Math.sin(angle) * r,\n                        0.0D, random.nextDouble(0.001D, 0.006D), 0.0D);\n            }\n        }\n    }\n}\n''', encoding='utf-8')

# Make the T-shaped visor visibly black from gameplay distance.
model = root / "src/client/java/com/kasper/sleeplessknight/DarkKnightModel.java"
s = model.read_text(encoding='utf-8')
old = '''        head.addOrReplaceChild("visor_dark_horizontal",\n                CubeListBuilder.create().texOffs(480, 480)\n                        .addBox(-3.36F, -0.38F, -0.03F, 6.72F, 0.76F, 0.06F),\n                PartPose.offset(0.0F, -5.17F, -2.64F));\n        head.addOrReplaceChild("visor_dark_vertical",\n                CubeListBuilder.create().texOffs(480, 490)\n                        .addBox(-0.61F, -2.05F, -0.03F, 1.22F, 4.10F, 0.06F),\n                PartPose.offset(0.0F, -2.72F, -2.64F));\n'''
new = '''        // Larger recessed black backing: a clearly readable T-shaped void.\n        head.addOrReplaceChild("visor_dark_horizontal",\n                CubeListBuilder.create().texOffs(480, 480)\n                        .addBox(-3.55F, -0.52F, -0.05F, 7.10F, 1.04F, 0.10F),\n                PartPose.offset(0.0F, -5.18F, -2.58F));\n        head.addOrReplaceChild("visor_dark_vertical",\n                CubeListBuilder.create().texOffs(480, 492)\n                        .addBox(-0.82F, -2.55F, -0.05F, 1.64F, 5.10F, 0.10F),\n                PartPose.offset(0.0F, -2.58F, -2.58F));\n        head.addOrReplaceChild("visor_void",\n                CubeListBuilder.create().texOffs(480, 504)\n                        .addBox(-2.10F, -2.70F, -0.24F, 4.20F, 5.40F, 0.22F),\n                PartPose.offset(0.0F, -3.45F, -2.36F));\n'''
if old not in s:
    raise RuntimeError('5.2.8 visor block not found')
s = s.replace(old, new, 1)

anchor = '''        head.addOrReplaceChild("brow_right",\n                CubeListBuilder.create().texOffs(248, 0)\n                        .addBox(-1.45F, -0.43F, -0.52F, 2.90F, 0.86F, 1.04F),\n                PartPose.offset(1.95F, -6.05F, -3.12F));\n'''
if '"forehead_lip"' not in s:
    s = s.replace(anchor, anchor + '''        head.addOrReplaceChild("forehead_lip",\n                CubeListBuilder.create().texOffs(264, 0)\n                        .addBox(-1.15F, -0.42F, -0.50F, 2.30F, 0.84F, 1.00F),\n                PartPose.offset(0.0F, -6.02F, -3.10F));\n''', 1)
model.write_text(s, encoding='utf-8')

props = root / 'gradle.properties'
p = props.read_text(encoding='utf-8')
if 'version=5.2.8' not in p:
    raise RuntimeError('Expected version 5.2.8 before bump')
p = p.replace('version=5.2.8', 'version=5.2.9', 1)
props.write_text(p, encoding='utf-8')

print('Prepared Sleepless Knight 5.2.9 restrained ground fog + stronger visor')
