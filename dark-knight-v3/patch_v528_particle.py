from pathlib import Path

p = Path(__file__).resolve().parent / "src/client/java/com/kasper/sleeplessknight/DarkKnightFogManager.java"
s = p.read_text(encoding="utf-8")

if "import net.minecraft.core.particles.PowerParticleOption;" not in s:
    s = s.replace(
        "import net.minecraft.core.particles.ParticleTypes;\n",
        "import net.minecraft.core.particles.ParticleTypes;\nimport net.minecraft.core.particles.PowerParticleOption;\n",
        1,
    )

s = s.replace(
    "level.addParticle(ParticleTypes.DRAGON_BREATH, x, y, z, vx, vy, vz);",
    "level.addParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, fighting ? 0.55F : 0.35F), x, y, z, vx, vy, vz);",
    1,
)

p.write_text(s, encoding="utf-8")
print("Patched Minecraft 26.2 dragon-breath particle option")
