package rich.modules.impl.world;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.util.move.MoveUtil;

import java.security.SecureRandom;

public class ScaffoldAngle extends RotateConstructor {

    private float speedMultiplier = 1.0f;

    public ScaffoldAngle() {
        super("ScaffoldMatrix");
    }

    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = multiplier;
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3 vec3d, Entity entity) {
        Angle angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));

        if (rotationDifference < 0.01f) return currentAngle;

        float lineYaw = Math.abs(yawDelta / rotationDifference) * 360.0f;
        float linePitch = Math.abs(pitchDelta / rotationDifference) * 180.0f;

        float speedFactor = Mth.clamp(1.0f - rotationDifference / 180.0f, 0.1f, 1.0f);
        float speed = 0.8f * speedFactor * this.speedMultiplier;

        float jitterYaw = MoveUtil.hasPlayerMovement() ? (float)(3.0 * Math.sin((double) System.currentTimeMillis() / 80.0)) * (1.0f / this.speedMultiplier) : 0.0f;
        float jitterPitch = MoveUtil.hasPlayerMovement() ? (float)(1.5 * Math.cos((double) System.currentTimeMillis() / 80.0)) * (1.0f / this.speedMultiplier) : 0.0f;

        float moveYaw = Mth.clamp(yawDelta, -lineYaw, lineYaw);
        float movePitch = Mth.clamp(pitchDelta, -linePitch, linePitch);

        Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(Mth.lerp(randomLerp(speed, speed), currentAngle.getYaw(), currentAngle.getYaw() + moveYaw) + jitterYaw);
        moveAngle.setPitch(Mth.lerp(randomLerp(speed, speed), currentAngle.getPitch(), currentAngle.getPitch() + movePitch) + jitterPitch);
        return moveAngle;
    }

    private float randomLerp(float min, float max) {
        return Mth.lerp(new SecureRandom().nextFloat(), min, max);
    }

    @Override
    public Vec3 randomValue() {
        return Vec3.ZERO;
    }
}
