
package rich.modules.impl.combat.macetarget.prediction;

import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import rich.modules.impl.combat.macetarget.state.MaceState;

public class TargetPredictor {
    private static final Minecraft mc = Minecraft.getInstance();
    private Vec3 lastPosition = null;
    private Vec3 velocity = Vec3.ZERO;
    private Vec3 smoothedVelocity = Vec3.ZERO;
    private long lastUpdateTime = 0L;
    private int sampleCount = 0;
    private static final int MIN_SAMPLES = 2;
    private static final double SMOOTHING = 0.6;

    public void update(LivingEntity target) {
        long deltaTime;
        if (target == null) {
            this.reset();
            return;
        }
        Vec3 currentPos = target.position();
        long currentTime = System.currentTimeMillis();
        if (this.lastPosition != null && this.lastUpdateTime > 0L && (deltaTime = currentTime - this.lastUpdateTime) > 0L && deltaTime < 500L) {
            this.velocity = currentPos.subtract(this.lastPosition);
            this.smoothedVelocity = this.smoothedVelocity.scale(0.6).add(this.velocity.scale(0.4));
            ++this.sampleCount;
        }
        this.lastPosition = currentPos;
        this.lastUpdateTime = currentTime;
    }

    public Vec3 getPredictedPosition(LivingEntity target, MaceState.Stage stage) {
        if (target == null || TargetPredictor.mc.player == null) {
            return Vec3.ZERO;
        }
        Vec3 currentPos = target.position();
        if (this.sampleCount < 2 || this.smoothedVelocity.horizontalDistanceSqr() < 1.0E-4) {
            return currentPos;
        }
        double distance = TargetPredictor.mc.player.distanceTo(target);
        double playerSpeed = TargetPredictor.mc.player.getDeltaMovement().length();
        double targetSpeed = this.smoothedVelocity.horizontalDistance();
        double ticksToReach = switch (stage) {
            case MaceState.Stage.FLYING_UP -> {
                double heightDiff = Math.abs(TargetPredictor.mc.player.getY() - target.getY());
                yield (heightDiff + distance) / Math.max(playerSpeed * 20.0, 1.0) * 1.2;
            }
            case MaceState.Stage.TARGETTING -> distance / Math.max(playerSpeed * 20.0, 0.8);
            case MaceState.Stage.ATTACKING -> distance / Math.max(playerSpeed * 20.0, 1.5) * 0.8;
            default -> distance / 2.0;
        };
        ticksToReach = Math.min(ticksToReach, 40.0);
        double leadMultiplier = 1.0;
        if (targetSpeed > 0.2) {
            leadMultiplier = 1.3;
        }
        if (targetSpeed > 0.4) {
            leadMultiplier = 1.5;
        }
        Vec3 prediction = this.smoothedVelocity.scale(ticksToReach * leadMultiplier);
        return currentPos.add(prediction);
    }

    public boolean isMoving() {
        return this.smoothedVelocity.horizontalDistanceSqr() > 0.001;
    }

    public void reset() {
        this.lastPosition = null;
        this.velocity = Vec3.ZERO;
        this.smoothedVelocity = Vec3.ZERO;
        this.lastUpdateTime = 0L;
        this.sampleCount = 0;
    }

    @Generated
    public Vec3 getLastPosition() {
        return this.lastPosition;
    }

    @Generated
    public Vec3 getVelocity() {
        return this.velocity;
    }

    @Generated
    public Vec3 getSmoothedVelocity() {
        return this.smoothedVelocity;
    }

    @Generated
    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    @Generated
    public int getSampleCount() {
        return this.sampleCount;
    }
}

