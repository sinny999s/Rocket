
package rich.modules.impl.combat.aura.impl;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.impl.RotateConstructor;

public class LinearConstructor
extends RotateConstructor {
    public static final LinearConstructor INSTANCE = new LinearConstructor();

    public LinearConstructor() {
        super("Linear");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3 vec3d, Entity entity) {
        Angle angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float)Math.hypot(yawDelta, pitchDelta);
        float straightLineYaw = Math.abs(yawDelta / rotationDifference) * 360.0f;
        float straightLinePitch = Math.abs(pitchDelta / rotationDifference) * 360.0f;
        float newYaw = currentAngle.getYaw() + Math.min(Math.max(yawDelta, -straightLineYaw), straightLineYaw);
        float newPitch = currentAngle.getPitch() + Math.min(Math.max(pitchDelta, -straightLinePitch), straightLinePitch);
        return new Angle(newYaw, newPitch);
    }

    @Override
    public Vec3 randomValue() {
        return new Vec3(0.0, 0.0, 0.0);
    }
}

