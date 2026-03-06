
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
import rich.util.timer.StopWatch;

public class SPAngle
extends RotateConstructor {
    private final SecureRandom random = new SecureRandom();
    private float currentJitterYaw = 0.0f;
    private float currentJitterPitch = 0.0f;
    private float targetJitterYaw = 0.0f;
    private float targetJitterPitch = 0.0f;
    private float circlePhase = 0.0f;
    private float circleRadius = 0.0f;
    private float targetCircleRadius = 0.0f;
    private float currentSpeed = 0.0f;

    public SPAngle() {
        super("SpookyTime");
    }

    @Override
    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3 vec3d, Entity entity) {
        float targetSpeed;
        Angle randomAngle;
        boolean canAttack;
        StrikeManager attackHandler = Initialization.getInstance().getManager().getAttackPerpetrator().getAttackHandler();
        Aura aura = Aura.getInstance();
        StopWatch attackTimer = attackHandler.getAttackTimer();
        int count = attackHandler.getCount();
        boolean bl = canAttack = entity != null && attackHandler.canAttack(aura.getConfig(), 0);
        if (entity != null && canAttack) {
            Vec3 aimPoint = computeHitbox(entity, 1.0f, entity.onGround() ? 1.0f : 1.256f, 1.0f, 2.0f);
            targetAngle = MathAngle.calculateAngle(aimPoint);
        }
        Angle angleDelta = MathAngle.calculateDelta(currentAngle, targetAngle);
        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();
        float rotationDifference = (float)Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        if (rotationDifference < 0.01f) {
            rotationDifference = 1.0f;
        }
        boolean lookingAtHitbox = false;
        if (entity != null && !canAttack) {
            lookingAtHitbox = RaycastAngle.rayTrace(AngleConnection.INSTANCE.getRotation().toVector(), 4.0, entity.getBoundingBox());
        }
        float deltaTime = 0.75f;
        this.circlePhase += deltaTime * this.randomLerp(7.5f, 12.5f);
        if ((double)this.circlePhase > Math.PI * 2) {
            this.circlePhase = (float)((double)this.circlePhase - Math.PI * 2);
        }
        this.targetCircleRadius = canAttack ? this.randomLerp(0.5f, 4.5f) : (lookingAtHitbox ? this.randomLerp(12.0f, 12.0f) : this.randomLerp(8.0f, 12.0f));
        this.circleRadius += (this.targetCircleRadius - this.circleRadius) * 0.18f;
        float circleYaw = (float)(Math.cos(this.circlePhase) * (double)this.circleRadius);
        float circlePitch = (float)(Math.sin(this.circlePhase * 11.3f) * (double)this.circleRadius * (double)0.4f);
        float timeRandom = (float)attackTimer.elapsedTime() / 100.0f + (float)(count % 5);
        int pattern = count % 4;
        switch (pattern) {
            case 0: {
                randomAngle = new Angle((float)Math.cos(timeRandom), (float)Math.sin(timeRandom));
                break;
            }
            case 1: {
                randomAngle = new Angle((float)Math.sin(timeRandom * 2.2f), (float)Math.cos(timeRandom * 0.6f));
                break;
            }
            case 2: {
                randomAngle = new Angle((float)Math.sin(timeRandom), (float)(-Math.cos(timeRandom)));
                break;
            }
            default: {
                randomAngle = new Angle((float)(-Math.cos(timeRandom * 0.5f)), (float)Math.sin(timeRandom * 2.1f));
            }
        }
        float jitterMultiplier = canAttack ? 0.5f : (lookingAtHitbox ? 0.6f : 1.0f);
        this.targetJitterYaw = this.randomLerp(35.0f, 32.0f) * randomAngle.getYaw() * jitterMultiplier;
        this.targetJitterPitch = this.randomLerp(5.0f, 2.0f) * randomAngle.getPitch() * jitterMultiplier;
        float jitterSmoothSpeed = 0.15f;
        this.currentJitterYaw += (this.targetJitterYaw - this.currentJitterYaw) * jitterSmoothSpeed;
        this.currentJitterPitch += (this.targetJitterPitch - this.currentJitterPitch) * jitterSmoothSpeed;
        if (canAttack) {
            targetSpeed = this.randomLerp(1.0f, 1.0f);
        } else if (lookingAtHitbox) {
            targetSpeed = this.randomLerp(0.35f, 0.15f);
        } else if (entity != null) {
            float distanceFactor = Mth.clamp((float)(rotationDifference / 30.0f), (float)0.1f, (float)1.0f);
            targetSpeed = this.randomLerp(0.45f, 0.25f) * distanceFactor;
        } else {
            targetSpeed = !attackTimer.finished(600.0) ? 0.53f : this.randomLerp(0.2f, 0.35f);
        }
        this.currentSpeed += (targetSpeed - this.currentSpeed) * 0.65f;
        float lineYaw = Math.abs(yawDelta / rotationDifference) * 180.0f;
        float linePitch = Math.abs(pitchDelta / rotationDifference) * 90.0f;
        float moveYaw = Mth.clamp((float)yawDelta, (float)(-lineYaw), (float)lineYaw);
        float movePitch = Mth.clamp((float)pitchDelta, (float)(-linePitch), (float)linePitch);
        float totalJitterYaw = this.currentJitterYaw + circleYaw;
        float totalJitterPitch = this.currentJitterPitch + circlePitch;
        if ((!aura.isState() || entity == null) && attackTimer.finished(800.0)) {
            totalJitterYaw *= 0.3f;
            totalJitterPitch *= 0.3f;
        }
        float newYaw = Mth.lerp((float)this.currentSpeed, (float)currentAngle.getYaw(), (float)(currentAngle.getYaw() + moveYaw)) + totalJitterYaw;
        float newPitch = Mth.lerp((float)this.currentSpeed, (float)currentAngle.getPitch(), (float)(currentAngle.getPitch() + movePitch)) + totalJitterPitch;
        return new Angle(newYaw, Mth.clamp((float)newPitch, (float)-90.0f, (float)90.0f));
    }

    private float randomLerp(float min, float max) {
        return Mth.lerp((float)this.random.nextFloat(), (float)min, (float)max);
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
        return Vec3.ZERO;
    }
}

