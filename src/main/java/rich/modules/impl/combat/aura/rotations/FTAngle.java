
package rich.modules.impl.combat.aura.rotations;

import java.security.SecureRandom;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rich.Initialization;
import rich.modules.impl.combat.Aura;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.util.timer.StopWatch;

public class FTAngle
extends RotateConstructor {
    private final SecureRandom random = new SecureRandom();
    private static int lastCount = -1;
    private static int hitsAfterMiss = 0;
    private static long missEndTime = 0L;
    private static int swingsDone = 0;
    private float currentJitterYaw = 0.0f;
    private float currentJitterPitch = 0.0f;
    private float targetJitterYaw = 0.0f;
    private float targetJitterPitch = 0.0f;

    public FTAngle() {
        super("FunTime");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3 vec3d, Entity entity) {
        StrikeManager attackHandler = Initialization.getInstance().getManager().getAttackPerpetrator().getAttackHandler();
        StopWatch attackTimer = attackHandler.getAttackTimer();
        int count = attackHandler.getCount();
        Aura aura = Aura.getInstance();
        long now = System.currentTimeMillis();
        if (count != lastCount) {
            ++hitsAfterMiss;
            lastCount = count;
        }
        if (hitsAfterMiss >= 40 && missEndTime == 0L) {
            missEndTime = now + 350L;
            hitsAfterMiss = 0;
            swingsDone = 0;
        }
        if (missEndTime != 0L) {
            if (now < missEndTime) {
                long elapsed = now - (missEndTime - 350L);
                if (swingsDone == 0 && elapsed >= 50L) {
                    FTAngle.mc.player.swing(InteractionHand.MAIN_HAND);
                    swingsDone = 1;
                } else if (swingsDone == 1 && elapsed >= 180L) {
                    FTAngle.mc.player.swing(InteractionHand.MAIN_HAND);
                    swingsDone = 2;
                }
                return new Angle(currentAngle.getYaw() + this.random.nextFloat() * 6.0f - 3.0f, -80.0f);
            }
            missEndTime = 0L;
        }
        Angle angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float)Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        if (rotationDifference < 0.01f) {
            rotationDifference = 1.0f;
        }
        int suck = count % 3;
        float timeRandom = (float)attackTimer.elapsedTime() / 80.0f + (float)(count % 6);
        Angle randomAngle = switch (suck) {
            case 0 -> new Angle((float)Math.cos(timeRandom), (float)Math.sin(timeRandom));
            case 1 -> new Angle((float)Math.sin(timeRandom), (float)Math.cos(timeRandom));
            case 2 -> new Angle((float)Math.sin(timeRandom), (float)(-Math.cos(timeRandom)));
            default -> new Angle((float)(-Math.cos(timeRandom)), (float)Math.sin(timeRandom));
        };
        this.targetJitterYaw = this.randomLerp(11.0f, 20.0f) * randomAngle.getYaw();
        this.targetJitterPitch = this.randomLerp(1.0f, 6.0f) * randomAngle.getPitch() + this.randomLerp(2.0f, 1.0f) * (float)Math.cos((double)System.currentTimeMillis() / 8000.0);
        float jitterSmoothSpeed = 1.0f;
        this.currentJitterYaw += (this.targetJitterYaw - this.currentJitterYaw) * jitterSmoothSpeed;
        this.currentJitterPitch += (this.targetJitterPitch - this.currentJitterPitch) * jitterSmoothSpeed;
        if (entity != null) {
            float speed = attackHandler.canAttack(aura.getConfig(), 0) ? 0.9f : (this.random.nextBoolean() ? 0.1f : 0.2f);
            float lineYaw = Math.abs(yawDelta / rotationDifference) * 180.0f;
            float linePitch = Math.abs(pitchDelta / rotationDifference) * 180.0f;
            float moveYaw = Mth.clamp((float)yawDelta, (float)(-lineYaw), (float)lineYaw);
            float movePitch = Mth.clamp((float)pitchDelta, (float)(-linePitch), (float)linePitch);
            float lerpSpeed = this.randomLerp(speed, speed + 0.6f);
            float newYaw = Mth.lerp((float)lerpSpeed, (float)currentAngle.getYaw(), (float)(currentAngle.getYaw() + moveYaw)) + this.currentJitterYaw;
            float newPitch = Mth.lerp((float)lerpSpeed, (float)currentAngle.getPitch(), (float)(currentAngle.getPitch() + movePitch)) + this.currentJitterPitch;
            return new Angle(newYaw, Mth.clamp((float)newPitch, (float)-90.0f, (float)90.0f));
        }
        float speed = attackTimer.finished(650.0) ? (this.random.nextBoolean() ? 0.85f : 0.2f) : -0.2f;
        float yawJitter = !attackTimer.finished(2000.0) ? this.currentJitterYaw : 0.0f;
        float pitchJitter = !attackTimer.finished(2000.0) ? this.currentJitterPitch : 0.0f;
        float lineYaw = Math.abs(yawDelta / rotationDifference) * 180.0f;
        float linePitch = Math.abs(pitchDelta / rotationDifference) * 180.0f;
        float moveYaw = Mth.clamp((float)yawDelta, (float)(-lineYaw), (float)lineYaw);
        float movePitch = Mth.clamp((float)pitchDelta, (float)(-linePitch), (float)linePitch);
        float lerpSpeed = Math.clamp((float)this.randomLerp(speed, speed + 0.2f), (float)0.0f, (float)1.0f);
        float newYaw = Mth.lerp((float)lerpSpeed, (float)currentAngle.getYaw(), (float)(currentAngle.getYaw() + moveYaw)) + yawJitter;
        float newPitch = Mth.lerp((float)lerpSpeed, (float)currentAngle.getPitch(), (float)(currentAngle.getPitch() + movePitch)) + pitchJitter;
        return new Angle(newYaw, Mth.clamp((float)newPitch, (float)-90.0f, (float)90.0f));
    }

    private float randomLerp(float min, float max) {
        return Mth.lerp((float)this.random.nextFloat(), (float)min, (float)max);
    }

    @Override
    public Vec3 randomValue() {
        return Vec3.ZERO;
    }
}

