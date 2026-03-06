
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.RotationUpdateEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.target.TargetFinder;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.math.MathUtils;
import rich.util.math.TaskPriority;
import rich.util.repository.friend.FriendUtils;

public class ProjectileHelper
extends ModuleStructure {
    private final SliderSettings searchDistance = new SliderSettings("Search Distance", "Target search radius around player").setValue(16.0f).range(5.0f, 64.0f);
    private final MultiSelectSetting targetType = new MultiSelectSetting("Target Type", "Filters targets by type").value("Players", "Mobs", "Animals", "Armor Stand").selected("Players", "Mobs", "Animals");
    private final TargetFinder targetFinder = new TargetFinder();
    private LivingEntity currentTarget;

    public ProjectileHelper() {
        super("ProjectileHelper", "Projectile Helper", ModuleCategory.COMBAT);
        this.settings(this.searchDistance, this.targetType);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    public LivingEntity getTarget(Level world, Iterable<Entity> entities) {
        List entityList = StreamSupport.stream(entities.spliterator(), false).collect(Collectors.toList());
        List<LivingEntity> validTargets = ((List<Object>)entityList).stream().filter(e -> e instanceof LivingEntity).map(e -> (LivingEntity)e).filter(this::isValidTarget).collect(Collectors.toList());
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        Vec3 playerPos = ProjectileHelper.mc.player.position();
        for (LivingEntity target : validTargets) {
            double distance = target.position().distanceTo(playerPos);
            if (!(distance < nearestDistance) || !(distance <= (double)this.searchDistance.getValue())) continue;
            nearestDistance = distance;
            nearestTarget = target;
        }
        this.currentTarget = nearestTarget;
        return this.currentTarget;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isValidTarget(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (entity == ProjectileHelper.mc.player) {
            return false;
        }
        if (!entity.isAlive()) {
            return false;
        }
        if (!this.targetType.isSelected("Players") && entity instanceof Player) {
            return false;
        }
        if (!this.targetType.isSelected("Mobs") && entity instanceof Mob) {
            return false;
        }
        if (!this.targetType.isSelected("Animals") && entity instanceof Animal) {
            return false;
        }
        return this.targetType.isSelected("Armor Stand") || !(entity instanceof ArmorStand);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    public Vec3 getPredictedPosition(LivingEntity target, Vec3 shooterPos, float projectileSpeed, float gravity) {
        double t;
        double c;
        Vec3 targetPos = target.position().add(0.0, (double)target.getBbHeight() * 0.5, 0.0);
        Vec3 targetVelocity = target.getDeltaMovement();
        Vec3 delta = targetPos.subtract(shooterPos);
        double a = (double)(projectileSpeed * projectileSpeed) - targetVelocity.lengthSqr();
        double b = -2.0 * delta.dot(targetVelocity);
        double discriminant = b * b - 4.0 * a * (c = -delta.lengthSqr());
        if (discriminant > 0.0) {
            double t1 = (-b + Math.sqrt(discriminant)) / (2.0 * a);
            double t2 = (-b - Math.sqrt(discriminant)) / (2.0 * a);
            t = Math.max(t1, t2);
        } else {
            t = delta.length() / (double)projectileSpeed;
        }
        Vec3 predicted = targetPos.add(targetVelocity.scale(t));
        predicted = predicted.add(0.0, 0.5 * (double)gravity * t * t, 0.0);
        return predicted;
    }

    private boolean isHoldingProjectile() {
        ItemStack main = ProjectileHelper.mc.player.getMainHandItem();
        return main.getItem() instanceof BowItem || main.getItem() instanceof CrossbowItem || main.getItem() instanceof TridentItem;
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() != 0) {
            return;
        }
        ItemStack stack = ProjectileHelper.mc.player.getMainHandItem();
        if (!this.isValidWeaponState(stack)) {
            this.currentTarget = null;
            return;
        }
        this.updateTarget();
        if (this.currentTarget != null) {
            this.performAim();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isValidWeaponState(ItemStack stack) {
        boolean holdingBow = stack.getItem() instanceof BowItem;
        boolean holdingCrossbow;
        if (stack.getItem() instanceof CrossbowItem) {
            holdingCrossbow = CrossbowItem.isCharged((ItemStack)stack);
        } else {
            holdingCrossbow = false;
        }
        boolean holdingTrident = stack.getItem() instanceof TridentItem;
        if (!(holdingBow || holdingCrossbow || holdingTrident)) {
            return false;
        }
        return !holdingBow || ProjectileHelper.mc.player.getUseItem() == stack;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void updateTarget() {
        if (this.currentTarget != null && !this.currentTarget.isAlive()) {
            this.currentTarget = null;
        }
        if (this.currentTarget == null) {
            this.currentTarget = this.getTarget(ProjectileHelper.mc.level, ProjectileHelper.mc.level.entitiesForRendering());
            if (this.currentTarget == ProjectileHelper.mc.player) {
                this.currentTarget = null;
            }
        }
        if (FriendUtils.isFriend(this.currentTarget)) {
            this.currentTarget = null;
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void performAim() {
        Vec3 shooterPos = ProjectileHelper.mc.player.position().add(0.0, ProjectileHelper.mc.player.getEyeHeight(ProjectileHelper.mc.player.getPose()), 0.0).add(ProjectileHelper.mc.player.getDeltaMovement());
        float projectileSpeed = 6.0f;
        float gravity = 0.02f;
        Vec3 predictedPos = this.getPredictedPosition(this.currentTarget, shooterPos, projectileSpeed, gravity);
        double dx = predictedPos.x - shooterPos.x;
        double dy = predictedPos.y - shooterPos.y;
        double dz = predictedPos.z - shooterPos.z;
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f + (float)MathUtils.getRandom(-1, 1);
        float pitch = (float)(-Math.toDegrees(Math.atan2(dy, distanceXZ))) + (float)MathUtils.getRandom(-1, 1);
        AngleConnection.INSTANCE.rotateTo(new Angle(yaw, pitch), AngleConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_1, this);
    }
}

