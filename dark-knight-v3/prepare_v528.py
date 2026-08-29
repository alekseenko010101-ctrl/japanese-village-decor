from pathlib import Path
import runpy

root = Path(__file__).resolve().parent

# Apply all earlier changes when building from the GitHub branch. A released
# 5.2.7 source zip already contains them, so do not patch the same code twice.
props_probe = (root / "gradle.properties").read_text(encoding="utf-8")
if "version=5.2.5" in props_probe or "version=5.2.6" in props_probe:
    runpy.run_path(str(root / "prepare_v527.py"), run_name="__main__")
elif "version=5.2.7" not in props_probe:
    raise RuntimeError("5.2.8 preparation expects a 5.2.5/5.2.6 branch or 5.2.7 source")

# 1) A true black backing plane behind the helmet's T-shaped visor opening.
model = root / "src/client/java/com/kasper/sleeplessknight/DarkKnightModel.java"
s = model.read_text(encoding="utf-8")
marker = '''        head.addOrReplaceChild("right_jaw",
                CubeListBuilder.create().texOffs(328, 0)
                        .addBox(-1.42F, -0.60F, -0.48F, 2.84F, 1.20F, 0.96F),
                PartPose.offset(2.00F, -0.28F, -3.04F));
'''
visor = marker + '''
        // Recessed matte-black backing inside the real T-shaped visor gap.
        // It sits just in front of the inner head and behind the armour plates,
        // so the opening reads as a deep black void instead of visible face pixels.
        head.addOrReplaceChild("visor_dark_horizontal",
                CubeListBuilder.create().texOffs(480, 480)
                        .addBox(-3.36F, -0.38F, -0.03F, 6.72F, 0.76F, 0.06F),
                PartPose.offset(0.0F, -5.17F, -2.64F));
        head.addOrReplaceChild("visor_dark_vertical",
                CubeListBuilder.create().texOffs(480, 490)
                        .addBox(-0.61F, -2.05F, -0.03F, 1.22F, 4.10F, 0.06F),
                PartPose.offset(0.0F, -2.72F, -2.64F));
'''
if 'visor_dark_horizontal' not in s:
    if marker not in s:
        raise RuntimeError('Helmet visor insertion point not found')
    s = s.replace(marker, visor, 1)
model.write_text(s, encoding="utf-8")

# 2) Reserve a guaranteed pure-black texture patch for the visor backing.
gen = root / "generate_assets.py"
s = gen.read_text(encoding="utf-8")
old = "    for _ in range(320):\n"
new = '''    # Reserved unlit visor swatch used by DarkKnightModel's recessed T opening.\n    rect(p,480,480,512,512,(1,1,3,255),0,False)\n    for _ in range(320):\n'''
if 'Reserved unlit visor swatch' not in s:
    if old not in s:
        raise RuntimeError('Texture generator patch point not found')
    s = s.replace(old, new, 1)
gen.write_text(s, encoding="utf-8")

# 3) Expose the music/combat window so fog can intensify for the same knight.
music = root / "src/client/java/com/kasper/sleeplessknight/DarkKnightMusicManager.java"
s = music.read_text(encoding="utf-8")
marker = '''    private static long randomBetween(long minInclusive, long maxInclusive) {
'''
method = '''    public static boolean isFightActiveFor(DarkKnightEntity knight) {
        if (knight == null || trackedKnight == null || !trackedKnight.equals(knight.getUUID())) {
            return false;
        }
        if (knight.level() == null || lastFightActivity == Long.MIN_VALUE) {
            return false;
        }
        long now = knight.level().getGameTime();
        return now - lastFightActivity <= COMBAT_GRACE_TICKS;
    }

'''
if 'isFightActiveFor(DarkKnightEntity knight)' not in s:
    if marker not in s:
        raise RuntimeError('Music manager insertion point not found')
    s = s.replace(marker, method + marker, 1)
music.write_text(s, encoding="utf-8")

# 4) Client-side local fog around every nearby Dark Knight. Built-in particles
# keep it shader/resource-pack friendly and avoid changing global world fog.
fog = root / "src/client/java/com/kasper/sleeplessknight/DarkKnightFogManager.java"
fog.write_text('''package com.kasper.sleeplessknight;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;

import java.util.concurrent.ThreadLocalRandom;

/** Local ground fog that follows the Dark Knight without changing global world fog. */
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

            // Dense violet breath hugs the ground. In combat the radius grows and
            // the cloud becomes visibly thicker, but the screen itself is never globally fogged.
            int violetCount = fighting ? 4 : 2;
            double radius = fighting ? 5.8D : 4.2D;
            double height = fighting ? 1.55D : 0.85D;

            for (int i = 0; i < violetCount; i++) {
                double angle = random.nextDouble(0.0D, Math.PI * 2.0D);
                double r = Math.sqrt(random.nextDouble()) * radius;
                double x = knight.getX() + Math.cos(angle) * r;
                double z = knight.getZ() + Math.sin(angle) * r;
                double y = knight.getY() + 0.05D + random.nextDouble() * height;

                // Very slow inward drift makes the fog curl around the knight.
                double vx = (knight.getX() - x) * 0.0025D + random.nextDouble(-0.004D, 0.004D);
                double vz = (knight.getZ() - z) * 0.0025D + random.nextDouble(-0.004D, 0.004D);
                double vy = random.nextDouble(0.002D, 0.010D);
                level.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, vx, vy, vz);
            }

            // Dark wisps keep the cloud from looking like a bright magical aura.
            // Spawn less often so they layer through the violet mist instead of hiding it.
            if (random.nextInt(fighting ? 2 : 5) == 0) {
                int smokeCount = fighting ? 2 : 1;
                for (int i = 0; i < smokeCount; i++) {
                    double angle = random.nextDouble(0.0D, Math.PI * 2.0D);
                    double r = Math.sqrt(random.nextDouble()) * (radius * 0.82D);
                    double x = knight.getX() + Math.cos(angle) * r;
                    double z = knight.getZ() + Math.sin(angle) * r;
                    double y = knight.getY() + 0.08D + random.nextDouble() * 0.65D;
                    level.addParticle(
                            ParticleTypes.SMOKE,
                            x, y, z,
                            random.nextDouble(-0.006D, 0.006D),
                            random.nextDouble(0.002D, 0.009D),
                            random.nextDouble(-0.006D, 0.006D)
                    );
                }
            }

            // Sparse purple motes only during combat, mixed into the fog rather than filling the screen.
            if (fighting && random.nextInt(4) == 0) {
                double angle = random.nextDouble(0.0D, Math.PI * 2.0D);
                double r = random.nextDouble(0.6D, 3.6D);
                level.addParticle(
                        ParticleTypes.PORTAL,
                        knight.getX() + Math.cos(angle) * r,
                        knight.getY() + random.nextDouble(0.25D, 1.8D),
                        knight.getZ() + Math.sin(angle) * r,
                        0.0D, random.nextDouble(0.005D, 0.020D), 0.0D
                );
            }
        }
    }
}
''', encoding='utf-8')

# 5) Register fog tick after the combat manager so it sees the current fight state.
client = root / "src/client/java/com/kasper/sleeplessknight/SleeplessKnightClient.java"
s = client.read_text(encoding="utf-8")
old = '        ClientTickEvents.END_CLIENT_TICK.register(DarkKnightAmbientManager::tick);\n'
new = old + '        ClientTickEvents.END_CLIENT_TICK.register(DarkKnightFogManager::tick);\n'
if 'DarkKnightFogManager::tick' not in s:
    if old not in s:
        raise RuntimeError('Client initializer insertion point not found')
    s = s.replace(old, new, 1)
client.write_text(s, encoding="utf-8")

# 6) Version bump.
props = root / "gradle.properties"
s = props.read_text(encoding="utf-8")
if 'version=5.2.7' not in s:
    raise RuntimeError('Expected version 5.2.7 before 5.2.8 bump')
s = s.replace('version=5.2.7', 'version=5.2.8', 1)
props.write_text(s, encoding="utf-8")

print('Prepared Sleepless Knight 5.2.8 local violet fog + black visor')
