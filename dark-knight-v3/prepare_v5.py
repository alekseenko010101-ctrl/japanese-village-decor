from pathlib import Path

model = Path('src/client/java/com/kasper/sleeplessknight/DarkKnightModel.java')
s = model.read_text(encoding='utf-8')

# Rebuild the great helm proportions: shorter, wider, more like the supplied reference.
repls = {
    '.addBox(-2.85F, -8.10F, -2.85F, 5.70F, 7.85F, 5.70F)': '.addBox(-2.95F, -7.60F, -2.95F, 5.90F, 7.25F, 5.90F)',
    '.addBox(-3.75F, -8.70F, -0.55F, 7.50F, 8.85F, 1.10F)': '.addBox(-3.90F, -7.95F, -0.58F, 7.80F, 8.10F, 1.16F)',
    'PartPose.offset(0.0F, 0.0F, 3.35F)': 'PartPose.offset(0.0F, 0.0F, 3.48F)',
    '.addBox(-0.55F, -8.65F, -3.20F, 1.10F, 8.80F, 6.40F)': '.addBox(-0.62F, -7.90F, -3.35F, 1.24F, 8.00F, 6.70F)',
    'PartPose.offset(-3.35F, 0.0F, 0.0F)': 'PartPose.offset(-3.55F, 0.0F, 0.0F)',
    'PartPose.offset(3.35F, 0.0F, 0.0F)': 'PartPose.offset(3.55F, 0.0F, 0.0F)',
    '.addBox(-4.10F, -0.65F, -3.65F, 8.20F, 1.30F, 7.30F)': '.addBox(-4.30F, -0.70F, -3.75F, 8.60F, 1.40F, 7.50F)',
    'PartPose.offset(0.0F, -9.05F, 0.0F)': 'PartPose.offset(0.0F, -8.25F, 0.0F)',
    '.addBox(-5.75F, -0.50F, -3.95F, 11.50F, 1.00F, 7.90F)': '.addBox(-5.70F, -0.46F, -4.05F, 11.40F, 0.92F, 8.10F)',
    'PartPose.offset(0.0F, -9.95F, 0.0F)': 'PartPose.offset(0.0F, -9.20F, 0.0F)',
    '.addBox(-0.65F, -0.65F, -3.20F, 1.30F, 1.30F, 6.40F)': '.addBox(-0.58F, -0.50F, -3.30F, 1.16F, 1.00F, 6.60F)',
    'PartPose.offset(0.0F, -10.60F, 0.0F)': 'PartPose.offset(0.0F, -9.78F, 0.0F)',
    '.addBox(-3.15F, -0.70F, -0.55F, 6.30F, 1.40F, 1.10F)': '.addBox(-3.45F, -0.66F, -0.56F, 6.90F, 1.32F, 1.12F)',
    'PartPose.offset(0.0F, -6.65F, -3.38F)': 'PartPose.offset(0.0F, -6.15F, -3.55F)',
    '.addBox(-0.48F, -2.55F, -0.55F, 0.96F, 5.10F, 1.10F)': '.addBox(-0.46F, -2.45F, -0.56F, 0.92F, 4.90F, 1.12F)',
    'PartPose.offset(0.0F, -3.60F, -3.40F)': 'PartPose.offset(0.0F, -3.55F, -3.57F)',
    '.addBox(-1.18F, -2.00F, -0.52F, 2.36F, 4.00F, 1.04F)': '.addBox(-1.30F, -1.90F, -0.54F, 2.60F, 3.80F, 1.08F)',
    'PartPose.offsetAndRotation(-2.08F, -2.65F, -3.38F, 0.0F, -0.04F, 0.04F)': 'PartPose.offsetAndRotation(-2.02F, -2.55F, -3.53F, 0.0F, -0.035F, 0.025F)',
    'PartPose.offsetAndRotation(2.08F, -2.65F, -3.38F, 0.0F, 0.04F, -0.04F)': 'PartPose.offsetAndRotation(2.02F, -2.55F, -3.53F, 0.0F, 0.035F, -0.025F)',
    '.addBox(-1.28F, -0.60F, -0.50F, 2.56F, 1.20F, 1.00F)': '.addBox(-1.36F, -0.62F, -0.52F, 2.72F, 1.24F, 1.04F)',
    'PartPose.offset(-2.02F, -0.20F, -3.25F)': 'PartPose.offset(-2.02F, -0.20F, -3.40F)',
    'PartPose.offset(2.02F, -0.20F, -3.25F)': 'PartPose.offset(2.02F, -0.20F, -3.40F)',
    # Greatsword grip is centered in the gauntlet and blade hangs outside the thigh.
    'PartPose.offsetAndRotation(-0.35F, 9.25F, -0.55F, 0.10F, 0.0F, 0.48F)': 'PartPose.offsetAndRotation(-0.95F, 9.55F, -0.70F, 0.06F, 0.0F, 0.30F)',
    'this.rightArm.xRot = -0.32F + this.rightArm.xRot * 0.22F;': 'this.rightArm.xRot = -0.22F + this.rightArm.xRot * 0.20F;',
    'this.rightArm.yRot = -0.10F;': 'this.rightArm.yRot = -0.08F;',
    'this.rightArm.zRot = 0.16F;': 'this.rightArm.zRot = 0.08F;',
}

for old, new in repls.items():
    if old not in s:
        raise SystemExit(f'Expected model fragment not found: {old}')
    s = s.replace(old, new)

model.write_text(s, encoding='utf-8')

props = Path('gradle.properties')
p = props.read_text(encoding='utf-8').replace('version=4.0.0', 'version=5.0.0')
props.write_text(p, encoding='utf-8')

print('Prepared Sleepless Knight v5 model and version')
