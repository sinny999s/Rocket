
package rich.modules.impl.combat.aura.rotations;

import java.security.SecureRandom;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rich.Initialization;
import rich.modules.impl.combat.Aura;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.combat.aura.impl.RotateConstructor;

public class SnapAngle
extends RotateConstructor {
    private final SecureRandom random = new SecureRandom();
    private static long lastSnapTime = 0L;
    private static Angle snappedAngle = null;
    private static boolean holdingSnap = false;

    public SnapAngle() {
        super("Snap");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3 vec3d, Entity entity) {
        StrikeManager attackHandler = Initialization.getInstance().getManager().getAttackPerpetrator().getAttackHandler();
        Aura aura = Aura.getInstance();
        Angle angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float)Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean canAttack = entity != null && attackHandler.canAttack(aura.getConfig(), 0);
        float preAttackSpeed = 1.0f;
        float postAttackSpeed = 1.0f;
        float speed = canAttack ? preAttackSpeed : postAttackSpeed;
        float lineYaw = Math.abs(yawDelta / rotationDifference) * 180.0f;
        float linePitch = Math.abs(pitchDelta / rotationDifference) * 180.0f;
        float moveYaw = Mth.clamp((float)yawDelta, (float)(-lineYaw), (float)lineYaw);
        float movePitch = Mth.clamp((float)pitchDelta, (float)(-linePitch), (float)linePitch);
        Angle moveAngle = new Angle(currentAngle.getYaw(), currentAngle.getPitch());
        moveAngle.setYaw(Mth.lerp((float)speed, (float)currentAngle.getYaw(), (float)(currentAngle.getYaw() + moveYaw)));
        moveAngle.setPitch(Mth.lerp((float)speed, (float)currentAngle.getPitch(), (float)(currentAngle.getPitch() + movePitch)));
        return moveAngle;
    }

    private float randomLerp(float min, float max) {
        return Mth.lerp((float)this.random.nextFloat(), (float)min, (float)max);
    }

    @Override
    public Vec3 randomValue() {
        return Vec3.ZERO;
    }
}

