package rich.util.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;

public final class CrystalUtil implements IMinecraft {

    public static float crystalDamage(LivingEntity target, Vec3 crystalPos) {
        if (target == null) return 0;
        Vec3 targetPos = target.position();
        double dist = targetPos.distanceTo(crystalPos);
        if (dist > 12) return 0;

        double exposure = getExposure(crystalPos, target.getBoundingBox());
        double impact = (1.0 - (dist / 12.0)) * exposure;
        float damage = (float) ((impact * impact + impact) / 2.0 * 7.0 * 12.0 + 1.0);

        damage = applyArmorReduction(target, damage);
        damage = applyBlastProtection(target, damage);
        damage = applyResistance(target, damage);

        return Math.max(0, damage);
    }

    private static float applyArmorReduction(LivingEntity entity, float damage) {
        float armor = (float) entity.getArmorValue();
        float toughness = 0;
        if (entity instanceof Player player) {
            toughness = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS);
        }
        return net.minecraft.world.damagesource.CombatRules.getDamageAfterAbsorb(entity, damage, mc.level.damageSources().explosion(null), armor, toughness);
    }

    private static float applyBlastProtection(LivingEntity entity, float damage) {
        int protLevel = 0;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (!stack.isEmpty()) {
                protLevel += getBlastProtectionLevel(stack);
            }
        }
        if (protLevel > 0) {
            int cappedProt = Math.min(protLevel, 20);
            damage *= (1.0f - cappedProt / 25.0f);
        }
        return damage;
    }

    private static int getBlastProtectionLevel(ItemStack stack) {
        // Simplified: count total protection value from enchantments
        // In 1.21.11 the enchantment system changed, so we approximate
        return 0; // TODO: implement proper enchantment reading for 1.21.11
    }

    private static float applyResistance(LivingEntity entity, float damage) {
        MobEffectInstance resistance = entity.getEffect(MobEffects.RESISTANCE);
        if (resistance != null) {
            int amp = resistance.getAmplifier() + 1;
            damage *= (1.0f - amp * 0.2f);
        }
        return Math.max(0, damage);
    }

    public static double getExposure(Vec3 source, AABB box) {
        double xStep = 1.0 / ((box.maxX - box.minX) * 2.0 + 1.0);
        double yStep = 1.0 / ((box.maxY - box.minY) * 2.0 + 1.0);
        double zStep = 1.0 / ((box.maxZ - box.minZ) * 2.0 + 1.0);

        if (xStep <= 0 || yStep <= 0 || zStep <= 0) return 0;

        double xOff = (1.0 - Math.floor(1.0 / xStep) * xStep) / 2.0;
        double zOff = (1.0 - Math.floor(1.0 / zStep) * zStep) / 2.0;

        int total = 0;
        int hit = 0;

        for (double x = 0; x <= 1; x += xStep) {
            for (double y = 0; y <= 1; y += yStep) {
                for (double z = 0; z <= 1; z += zStep) {
                    double px = box.minX + (box.maxX - box.minX) * x;
                    double py = box.minY + (box.maxY - box.minY) * y;
                    double pz = box.minZ + (box.maxZ - box.minZ) * z;

                    Vec3 point = new Vec3(px + xOff, py, pz + zOff);
                    HitResult result = mc.level.clip(new ClipContext(point, source, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));

                    if (result.getType() == HitResult.Type.MISS) {
                        hit++;
                    }
                    total++;
                }
            }
        }

        return (double) hit / total;
    }

    public static boolean canPlaceCrystal(BlockPos pos) {
        if (mc.level == null) return false;

        BlockState below = mc.level.getBlockState(pos.below());
        if (!below.is(Blocks.OBSIDIAN) && !below.is(Blocks.BEDROCK)) return false;

        BlockState at = mc.level.getBlockState(pos);
        if (!at.isAir()) return false;

        AABB crystalBox = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 2, pos.getZ() + 1);

        for (Entity entity : mc.level.getEntities(null, crystalBox)) {
            if (!entity.isSpectator() && entity.isAlive()) return false;
        }

        return true;
    }

    public static float getTotalHealth(LivingEntity entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public static EndCrystal findBestBreakCrystal(double range, double minDamage, double maxSelfDamage, boolean antiSuicide) {
        if (mc.level == null || mc.player == null) return null;

        EndCrystal best = null;
        float bestDamage = 0;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystal)) continue;
            if (!crystal.isAlive()) continue;

            double dist = mc.player.distanceTo(crystal);
            if (dist > range) continue;

            float targetDamage = 0;
            for (Player player : mc.level.players()) {
                if (player == mc.player) continue;
                if (player.distanceTo(crystal) > 12) continue;

                float dmg = crystalDamage(player, crystal.position());
                if (dmg > targetDamage) targetDamage = dmg;
            }

            float selfDmg = crystalDamage(mc.player, crystal.position());
            if (selfDmg > maxSelfDamage) continue;
            if (antiSuicide && selfDmg >= getTotalHealth(mc.player)) continue;
            if (targetDamage < minDamage) continue;

            if (targetDamage > bestDamage) {
                bestDamage = targetDamage;
                best = crystal;
            }
        }

        return best;
    }

    public static BlockPos findBestPlacePos(double range, double minDamage, double maxSelfDamage, boolean antiSuicide, boolean facePlace, double facePlaceDmg) {
        if (mc.level == null || mc.player == null) return null;

        BlockPos bestPos = null;
        float bestDamage = 0;
        int r = (int) Math.ceil(range);

        BlockPos center = mc.player.blockPosition();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (!canPlaceCrystal(pos)) continue;

                    Vec3 crystalVec = new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);

                    double dist = mc.player.position().distanceTo(crystalVec);
                    if (dist > range) continue;

                    float selfDmg = crystalDamage(mc.player, crystalVec);
                    if (selfDmg > maxSelfDamage) continue;
                    if (antiSuicide && selfDmg >= getTotalHealth(mc.player)) continue;

                    float targetDamage = 0;
                    for (Player player : mc.level.players()) {
                        if (player == mc.player) continue;
                        if (player.distanceTo(mc.player) > 12) continue;

                        float dmg = crystalDamage(player, crystalVec);
                        if (dmg > targetDamage) targetDamage = dmg;
                    }

                    double minDmg = facePlace ? facePlaceDmg : minDamage;
                    if (targetDamage < minDmg) continue;

                    if (targetDamage > bestDamage) {
                        bestDamage = targetDamage;
                        bestPos = pos;
                    }
                }
            }
        }

        return bestPos;
    }

    private CrystalUtil() {}
}
