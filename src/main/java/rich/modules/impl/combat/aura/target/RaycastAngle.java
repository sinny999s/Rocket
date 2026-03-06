
package rich.modules.impl.combat.aura.target;

import java.util.Objects;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.attack.StrikerConstructor;

public final class RaycastAngle
implements IMinecraft {
    public static BlockHitResult raycast(double range, Angle angle, boolean includeFluids) {
        return RaycastAngle.raycast(Objects.requireNonNull(RaycastAngle.mc.player).getEyePosition(1.0f), range, angle, includeFluids);
    }

    public static BlockHitResult raycast(Vec3 vec, double range, Angle angle, boolean includeFluids) {
        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return null;
        }
        Vec3 rotationVec = angle.toVector();
        Vec3 end = vec.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
        ClientLevel world = RaycastAngle.mc.level;
        if (world == null) {
            return null;
        }
        ClipContext.Fluid fluidHandling = includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        ClipContext context = new ClipContext(vec, end, ClipContext.Block.OUTLINE, fluidHandling, entity);
        return world.clip(context);
    }

    public static BlockHitResult raycast(Vec3 start, Vec3 end, ClipContext.Block shapeType) {
        return RaycastAngle.raycast(start, end, shapeType, RaycastAngle.mc.player);
    }

    public static BlockHitResult raycast(Vec3 start, Vec3 end, ClipContext.Block shapeType, Entity entity) {
        return RaycastAngle.raycast(start, end, shapeType, ClipContext.Fluid.NONE, entity);
    }

    public static BlockHitResult raycast(Vec3 start, Vec3 end, ClipContext.Block shapeType, ClipContext.Fluid fluidHandling, Entity entity) {
        return RaycastAngle.mc.level.clip(new ClipContext(start, end, shapeType, fluidHandling, entity));
    }

    public static EntityHitResult raytraceEntity(double range, Angle angle, Predicate<Entity> filter) {
        LocalPlayer entity = RaycastAngle.mc.player;
        if (entity == null) {
            return null;
        }
        Vec3 cameraVec = entity.getEyePosition(1.0f);
        Vec3 rotationVec = angle.toVector();
        Vec3 vec3d3 = cameraVec.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
        AABB box = entity.getBoundingBox().expandTowards(rotationVec.scale(range)).inflate(1.0, 1.0, 1.0);
        return ProjectileUtil.getEntityHitResult((Entity)entity, (Vec3)cameraVec, (Vec3)vec3d3, (AABB)box, e -> !e.isSpectator() && filter.test((Entity)e), (double)(range * range));
    }

    public static boolean rayTrace(StrikerConstructor.AttackPerpetratorConfigurable config) {
        boolean elytraMode;
        boolean bl = elytraMode = RaycastAngle.mc.player.isFallFlying() && config.getTarget().isFallFlying();
        if (elytraMode) {
            return true;
        }
        return RaycastAngle.rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), config.getMaximumRange(), config.getBox());
    }

    public static boolean rayTrace(double range, AABB box) {
        return RaycastAngle.rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), range, box);
    }

    public static boolean rayTrace(Vec3 clientVec, double range, AABB box) {
        Vec3 cameraVec = Objects.requireNonNull(RaycastAngle.mc.player).getEyePosition();
        AABB expandedBox = box.inflate(0.15);
        return expandedBox.contains(cameraVec) || expandedBox.clip(cameraVec, cameraVec.add(clientVec.scale(range))).isPresent();
    }

    @Generated
    private RaycastAngle() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

