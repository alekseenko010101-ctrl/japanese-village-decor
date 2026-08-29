from pathlib import Path
import base64

root = Path(__file__).resolve().parent

# 1) Sword: center the weapon pivot inside the right gauntlet and shorten the grip
# so the hand visibly holds it instead of the blade/handle floating beside the fist.
model = root / "src/client/java/com/kasper/sleeplessknight/DarkKnightModel.java"
s = model.read_text(encoding="utf-8")
replacements = {
    '.texOffs(256, 208).addBox(-0.58F, -3.25F, -0.58F, 1.16F, 6.50F, 1.16F)':
        '.texOffs(256, 208).addBox(-0.52F, -1.55F, -0.52F, 1.04F, 4.60F, 1.04F)',
    '.texOffs(288, 208).addBox(-4.65F, 3.05F, -0.72F, 9.30F, 1.44F, 1.44F)':
        '.texOffs(288, 208).addBox(-4.65F, 2.85F, -0.68F, 9.30F, 1.36F, 1.36F)',
    '.texOffs(336, 208).addBox(-1.82F, 4.75F, -0.54F, 3.64F, 19.80F, 1.08F)':
        '.texOffs(336, 208).addBox(-1.82F, 4.35F, -0.52F, 3.64F, 20.20F, 1.04F)',
    '.texOffs(376, 208).addBox(-1.28F, 24.55F, -0.48F, 2.56F, 3.50F, 0.96F)':
        '.texOffs(376, 208).addBox(-1.28F, 24.55F, -0.46F, 2.56F, 3.50F, 0.92F)',
    'PartPose.offsetAndRotation(-0.95F, 9.55F, -0.70F, 0.06F, 0.0F, 0.30F)':
        'PartPose.offsetAndRotation(0.0F, 9.45F, -0.20F, 0.08F, 0.02F, 0.10F)',
    '.addBox(-0.30F, 0.0F, -0.06F, 0.60F, 17.20F, 0.12F)':
        '.addBox(-0.30F, 0.0F, -0.06F, 0.60F, 17.60F, 0.12F)',
    'PartPose.offset(0.0F, 6.15F, -0.62F)':
        'PartPose.offset(0.0F, 5.75F, -0.60F)',
}
for old, new in replacements.items():
    if old not in s:
        raise RuntimeError(f"Sword patch target not found: {old}")
    s = s.replace(old, new, 1)
model.write_text(s, encoding="utf-8")

# 2) Rewards: Wither-sized XP reward (50) + guaranteed one Netherite Ingot.
entity = root / "src/main/java/com/kasper/sleeplessknight/DarkKnightEntity.java"
s = entity.read_text(encoding="utf-8")
if 'import net.minecraft.world.item.Items;' not in s:
    s = s.replace('import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;\n',
                  'import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;\nimport net.minecraft.world.item.Items;\n', 1)
marker = '''    @Override\n    public @Nullable SpawnGroupData finalizeSpawn(\n'''
reward_code = '''    @Override\n    protected int getBaseExperienceReward(ServerLevel level) {\n        return 50;\n    }\n\n    @Override\n    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean killedByPlayer) {\n        super.dropCustomDeathLoot(level, damageSource, killedByPlayer);\n        this.spawnAtLocation(level, Items.NETHERITE_INGOT);\n    }\n\n'''
if 'protected int getBaseExperienceReward(ServerLevel level)' not in s:
    if marker not in s:
        raise RuntimeError('Reward insertion point not found')
    s = s.replace(marker, reward_code + marker, 1)
entity.write_text(s, encoding="utf-8")

# 3) Spawn egg: replace the tiny malformed icon with a full 16x16 dark-violet egg.
egg_png_b64 = 'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAw0lEQVR4nLVSMQ6DMAw8qm4RQ5AyZuMFvIH39AEdGDrwAN7TN/QF3RgjlaFipkNJZGI7U3uj5bPv7AP+Cdf4zTV+K/VUKrH2h1p4zwivmfWfNHLX9gAAa9y3XntIaiqJnKNrezyed1HJWbIQMQ4TAOB6u8Aah2UNrCdNotvpRglUBbuBNU4kx5vkYAOizHGYDiRNkWhBUrWsIVmjFsQvlG6Qf4FZiHLj/6MdDSxZuZVSBsQBdAj9vRZlMUh7Y4qtRv4JPoifamoCBm8AAAAAAElFTkSuQmCC'
egg_path = root / 'src/main/resources/assets/sleepless_knight/textures/item/dark_knight_spawn_egg.png'
egg_path.parent.mkdir(parents=True, exist_ok=True)
egg_path.write_bytes(base64.b64decode(egg_png_b64))

# 4) Version bump.
props = root / 'gradle.properties'
s = props.read_text(encoding='utf-8')
s = s.replace('version=5.2.5', 'version=5.2.6')
props.write_text(s, encoding='utf-8')

print('Prepared Sleepless Knight 5.2.6')
