from pathlib import Path
import runpy

root = Path(__file__).resolve().parent

# Build on top of every previous 5.2.9 change.
runpy.run_path(str(root / "prepare_v529.py"), run_name="__main__")

# 1) Replace the fake bright particle halo with low grey/black fog only.
fog = root / "src/client/java/com/kasper/sleeplessknight/DarkKnightFogManager.java"
fog.write_text('''package com.kasper.sleeplessknight;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.ThreadLocalRandom;

/** Low, restrained ground mist around the Dark Knight. */
public final class DarkKnightFogManager {
    private static final double RENDER_RANGE = 48.0D;
    private static final double RENDER_RANGE_SQR = RENDER_RANGE * RENDER_RANGE;

    private DarkKnightFogManager() {}

    public static void tick(Minecraft minecraft) {
        if (minecraft.level == null || minecraft.player == null) {
            return;
        }

        var level = minecraft.level;
        var player = minecraft.player;
        AABB search = player.getBoundingBox().inflate(RENDER_RANGE);

        for (DarkKnightEntity knight : level.getEntitiesOfClass(DarkKnightEntity.class, search)) {
            if (!knight.isAlive() || knight.distanceToSqr(player) > RENDER_RANGE_SQR) {
                continue;
            }

            boolean fighting = DarkKnightMusicManager.isFightActiveFor(knight);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            double radius = fighting ? 4.6D : 3.2D;

            // Wide, low smoke puffs. No portal, no dragon-breath, no neon particles.
            int clouds = fighting ? 3 : 2;
            for (int i = 0; i < clouds; i++) {
                double angle = random.nextDouble(0.0D, Math.PI * 2.0D);
                double r = Math.sqrt(random.nextDouble()) * radius;
                double x = knight.getX() + Math.cos(angle) * r;
                double z = knight.getZ() + Math.sin(angle) * r;
                double y = knight.getY() + 0.04D + random.nextDouble() * 0.22D;
                level.addParticle(
                        ParticleTypes.CLOUD,
                        x, y, z,
                        random.nextDouble(-0.006D, 0.006D),
                        random.nextDouble(0.0003D, 0.0015D),
                        random.nextDouble(-0.006D, 0.006D)
                );
            }

            // Darker smoke underneath the pale mist, kept very sparse.
            if (random.nextInt(fighting ? 2 : 4) == 0) {
                double angle = random.nextDouble(0.0D, Math.PI * 2.0D);
                double r = Math.sqrt(random.nextDouble()) * (radius * 0.80D);
                level.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        knight.getX() + Math.cos(angle) * r,
                        knight.getY() + 0.03D + random.nextDouble() * 0.18D,
                        knight.getZ() + Math.sin(angle) * r,
                        random.nextDouble(-0.003D, 0.003D),
                        random.nextDouble(0.0002D, 0.0010D),
                        random.nextDouble(-0.003D, 0.003D)
                );
            }

            // Occasional soft white wisps during combat to fill gaps without becoming a particle storm.
            if (fighting && random.nextInt(3) == 0) {
                double angle = random.nextDouble(0.0D, Math.PI * 2.0D);
                double r = Math.sqrt(random.nextDouble()) * (radius * 0.92D);
                level.addParticle(
                        ParticleTypes.WHITE_SMOKE,
                        knight.getX() + Math.cos(angle) * r,
                        knight.getY() + 0.05D + random.nextDouble() * 0.20D,
                        knight.getZ() + Math.sin(angle) * r,
                        random.nextDouble(-0.004D, 0.004D),
                        random.nextDouble(0.0003D, 0.0012D),
                        random.nextDouble(-0.004D, 0.004D)
                );
            }
        }
    }
}
''', encoding='utf-8')

# 2) Make the helmet slit physically open: recess the inner head well behind the front armour,
# then place a solid black backing between the armour and the recessed head.
model = root / "src/client/java/com/kasper/sleeplessknight/DarkKnightModel.java"
s = model.read_text(encoding="utf-8")

s = s.replace(
    '.addBox(-2.70F, -7.05F, -2.60F, 5.40F, 6.55F, 5.20F)',
    '.addBox(-2.70F, -7.05F, -1.78F, 5.40F, 6.55F, 4.38F)',
    1
)

# Make the horizontal eye slit taller and the center vertical slit wider by moving the surrounding armour apart.
s = s.replace('PartPose.offset(-1.95F, -6.05F, -3.12F)', 'PartPose.offset(-2.05F, -6.20F, -3.12F)', 1)
s = s.replace('PartPose.offset(1.95F, -6.05F, -3.12F)', 'PartPose.offset(2.05F, -6.20F, -3.12F)', 1)
s = s.replace('PartPose.offsetAndRotation(-2.00F, -2.85F, -3.10F', 'PartPose.offsetAndRotation(-2.15F, -2.65F, -3.10F', 1)
s = s.replace('PartPose.offsetAndRotation(2.00F, -2.85F, -3.10F', 'PartPose.offsetAndRotation(2.15F, -2.65F, -3.10F', 1)
s = s.replace('PartPose.offset(-2.00F, -0.28F, -3.04F)', 'PartPose.offset(-2.15F, -0.18F, -3.04F)', 1)
s = s.replace('PartPose.offset(2.00F, -0.28F, -3.04F)', 'PartPose.offset(2.15F, -0.18F, -3.04F)', 1)

# Replace the old backing with a much larger, unmistakably black T opening directly behind the face plates.
start = s.find('        // Deeper and wider black visor backing')
if start == -1:
    start = s.find('        // Recessed matte-black backing')
end_marker = '\n\n        PartDefinition body = root.addOrReplaceChild("body",'
end = s.find(end_marker, start)
if start == -1 or end == -1:
    raise RuntimeError('Could not find visor backing block')
visor = '''        // Deep black void visible through the physically open T-shaped slit.
        head.addOrReplaceChild("visor_dark_horizontal",
                CubeListBuilder.create().texOffs(480, 480)
                        .addBox(-3.75F, -0.70F, -0.10F, 7.50F, 1.40F, 0.20F),
                PartPose.offset(0.0F, -5.18F, -2.82F));
        head.addOrReplaceChild("visor_dark_vertical",
                CubeListBuilder.create().texOffs(480, 492)
                        .addBox(-0.98F, -2.65F, -0.10F, 1.96F, 5.30F, 0.20F),
                PartPose.offset(0.0F, -2.55F, -2.82F));
'''
s = s[:start] + visor + s[end:]
model.write_text(s, encoding='utf-8')

# 3) Version bump.
props = root / 'gradle.properties'
p = props.read_text(encoding='utf-8')
if 'version=5.2.9' not in p:
    raise RuntimeError('Expected 5.2.9 before bump')
props.write_text(p.replace('version=5.2.9', 'version=5.3.0', 1), encoding='utf-8')

print('Prepared Sleepless Knight 5.3.0: real low fog + physically open black visor')
