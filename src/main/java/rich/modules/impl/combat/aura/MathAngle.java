/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.jetbrains.annotations.NotNull
 */
package rich.modules.impl.combat.aura;

import lombok.Generated;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;

public final class MathAngle
implements IMinecraft {
    public static Angle fromVec2f(Vec2 vector2f) {
        return new Angle(vector2f.y, vector2f.x);
    }

    public static float computeAngleDifference(float a, float b) {
        return Mth.wrapDegrees((float)(a - b));
    }

    public static Angle fromVec3d(Vec3 vector) {
        return new Angle((float)Mth.wrapDegrees((double)(Math.toDegrees(Math.atan2(vector.z, vector.x)) - 90.0)), (float)Mth.wrapDegrees((double)Math.toDegrees(-Math.atan2(vector.y, Math.hypot(vector.x, vector.z)))));
    }

    public static Angle calculateDelta(Angle start, Angle end) {
        float deltaYaw = Mth.wrapDegrees((float)(end.getYaw() - start.getYaw()));
        float deltaPitch = Mth.wrapDegrees((float)(end.getPitch() - start.getPitch()));
        return new Angle(deltaYaw, deltaPitch);
    }

    public static Angle calculateAngle(Vec3 to) {
        return MathAngle.fromVec3d(to.subtract(MathAngle.mc.player.getEyePosition()));
    }

    public static Angle pitch(float pitch) {
        return new Angle(MathAngle.mc.player.getYRot(), pitch);
    }

    public static Angle cameraAngle() {
        return new Angle(MathAngle.mc.player.getYRot(), MathAngle.mc.player.getXRot());
    }

    public static boolean rayTrace(float yaw, float pitch, float distance, float wallDistance, Entity entity) {
        double maxDistance;
        AABB entityArea;
        Vec3 rotationVector;
        Vec3 endPoint;
        EntityHitResult ehr;
        HitResult result = MathAngle.rayTrace(distance, yaw, pitch);
        Vec3 startPoint = MathAngle.mc.player.position().add(0.0, MathAngle.mc.player.getEyeHeight(MathAngle.mc.player.getPose()), 0.0);
        double distancePow2 = Math.pow(distance, 2.0);
        if (result != null) {
            distancePow2 = startPoint.distanceToSqr(result.getLocation());
        }
        if ((ehr = ProjectileUtil.getEntityHitResult((Entity)MathAngle.mc.player, (Vec3)startPoint, (Vec3)(endPoint = startPoint.add(rotationVector = MathAngle.getRotationVector(pitch, yaw).scale(distance))), (AABB)(entityArea = MathAngle.mc.player.getBoundingBox().expandTowards(rotationVector).inflate(1.0, 1.0, 1.0)), e -> !e.isSpectator() && e.isPickable() && e == entity, (double)(maxDistance = Math.max(distancePow2, Math.pow(wallDistance, 2.0))))) != null) {
            double targetHeight;
            double minY;
            double minHitY;
            boolean wallBehindEntity;
            boolean allowedWallDistance = startPoint.distanceToSqr(ehr.getLocation()) <= Math.pow(wallDistance, 2.0);
            boolean wallMissing = result == null;
            boolean bl = wallBehindEntity = startPoint.distanceToSqr(ehr.getLocation()) < distancePow2;
            if (startPoint.distanceToSqr(ehr.getLocation()) <= Math.pow(distance, 2.0) && ehr.getLocation().y >= (minHitY = (minY = entity.getY()) + (targetHeight = (double)entity.getBbHeight()) * 0.3)) {
                return ehr.getEntity() == entity;
            }
        }
        return false;
    }

    public static HitResult rayTrace(double dst, float yaw, float pitch) {
        Vec3 vec3d = MathAngle.mc.player.getEyePosition(1.0f);
        Vec3 vec3d2 = MathAngle.getRotationVector(pitch, yaw);
        Vec3 vec3d3 = vec3d.add(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
        return MathAngle.mc.level.clip(new ClipContext(vec3d, vec3d3, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, MathAngle.mc.player));
    }

    @NotNull
    public static Vec3 getRotationVector(float yaw, float pitch) {
        return new Vec3(Mth.sin((double)(-pitch * ((float)Math.PI / 180))) * Mth.cos((double)(yaw * ((float)Math.PI / 180))), -Mth.sin((double)(yaw * ((float)Math.PI / 180))), Mth.cos((double)(-pitch * ((float)Math.PI / 180))) * Mth.cos((double)(yaw * ((float)Math.PI / 180))));
    }

    @Generated
    private MathAngle() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

