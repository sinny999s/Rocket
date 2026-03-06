
package rich.modules.impl.combat.aura.rotations;

import java.security.SecureRandom;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rich.Initialization;
import rich.modules.impl.combat.Aura;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.modules.impl.combat.aura.target.RaycastAngle;
import rich.util.move.MoveUtil;
import rich.util.timer.StopWatch;

public class MatrixAngle
extends RotateConstructor {
    public MatrixAngle() {
        super("Matrix");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3 vec3d, Entity entity) {
        float resolve2;
        float jitterYaw;
        boolean canAttack;
        StrikeManager attackHandler = Initialization.getInstance().getManager().getAttackPerpetrator().getAttackHandler();
        Aura aura = Aura.getInstance();
        StopWatch attackTimer = attackHandler.getAttackTimer();
        boolean bl = canAttack = entity != null && attackHandler.canAttack(aura.getConfig(), 0);
        if (entity != null && canAttack) {
            Vec3 aimPoint = computeHitbox(entity, 1.0f, entity.onGround() ? 0.9f : 1.4f, 1.0f, 2.0f);
            targetAngle = MathAngle.calculateAngle(aimPoint);
        }
        Angle angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float)Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean lookingAtHitbox = false;
        if (entity != null && !canAttack && RaycastAngle.rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), 4.0, entity.getBoundingBox())) {
            lookingAtHitbox = true;
        }
        float preAttackSpeed = 1.0f;
        float postAttackSpeed = lookingAtHitbox ? 0.06f : this.randomLerp(0.0f, 0.5f);
        float speed = canAttack ? preAttackSpeed : postAttackSpeed;
        float lineYaw = Math.abs(yawDelta / rotationDifference) * 360.0f;
        float linePitch = Math.abs(pitchDelta / rotationDifference) * 180.0f;
        jitterYaw = canAttack ? 0.0f : (MoveUtil.hasPlayerMovement() ? (float)(6.0 * Math.sin((double)System.currentTimeMillis() / 65.0)) : 0.0f);
        float jitterPitch = canAttack ? 0.0f : (MoveUtil.hasPlayerMovement() ? (float)(2.0 * Math.cos((double)System.currentTimeMillis() / 65.0)) : 0.0f);
        float resolve1 = canAttack ? 0.0f : 13.0f;
        float f2 = resolve2 = canAttack ? 0.0f : 8.0f;
        if (!aura.isState() || entity == null) {
            float speedFactor3 = Mth.clamp((float)(1.0f - rotationDifference / 180.0f), (float)0.1f, (float)1.0f);
            speed = !attackTimer.finished(550.0) ? 0.05f : 0.8f * speedFactor3;
            jitterYaw = 0.0f;
            resolve2 = 0.0f;
            resolve1 = 0.0f;
            jitterPitch = 0.0f;
        }
        float moveYaw = Mth.clamp((float)yawDelta, (float)(-lineYaw), (float)lineYaw) + resolve1;
        float movePitch = Mth.clamp((float)pitchDelta, (float)(-linePitch), (float)linePitch) + resolve2;
        Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(Mth.lerp((float)this.randomLerp(speed, speed), (float)currentAngle.getYaw(), (float)(currentAngle.getYaw() + moveYaw)) + jitterYaw);
        moveAngle.setPitch(Mth.lerp((float)this.randomLerp(speed, speed), (float)currentAngle.getPitch(), (float)(currentAngle.getPitch() + movePitch)) + jitterPitch);
        return moveAngle;
    }

    private float randomLerp(float min, float max) {
        return Mth.lerp((float)new SecureRandom().nextFloat(), (float)min, (float)max);
    }

    private static Vec3 computeHitbox(Entity entity, float X, float Y, float Z, float WIDTH) {
        double wHalf = entity.getBbWidth() / WIDTH;
        double yExpand = Mth.clamp((double)(entity.getEyeY() - entity.getY()), (double)0.0, (double)entity.getBbHeight());
        double xExpand = Mth.clamp((double)(mc.player.getX() - entity.getX()), (double)(-wHalf), (double)wHalf);
        double zExpand = Mth.clamp((double)(mc.player.getZ() - entity.getZ()), (double)(-wHalf), (double)wHalf);
        return new Vec3(entity.getX() + xExpand / (double)X, entity.getY() + yExpand / (double)Y, entity.getZ() + zExpand / (double)Z);
    }

    @Override
    public Vec3 randomValue() {
        return new Vec3(0.0, 0.0, 0.0);
    }
}

