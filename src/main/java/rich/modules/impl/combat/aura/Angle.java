
package rich.modules.impl.combat.aura;

import lombok.Generated;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.util.math.MathUtils;

public class Angle {
    public static Angle DEFAULT = new Angle(0.0f, 0.0f);
    private float yaw;
    private float pitch;

    public static Angle fromTargetHead(Vec3 playerPos, Vec3 targetPos, double targetHeight) {
        double headY = targetPos.y + targetHeight * 0.9;
        double deltaX = targetPos.x - playerPos.x;
        double deltaY = headY - (playerPos.y + 1.5);
        double deltaZ = targetPos.z - playerPos.z;
        float yaw = (float)Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0f;
        yaw = Mth.wrapDegrees((float)yaw);
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        float pitch = (float)Math.toDegrees(-Math.atan2(deltaY, horizontalDistance));
        pitch = Mth.clamp((float)pitch, (float)-90.0f, (float)90.0f);
        return new Angle(yaw, pitch);
    }

    public Angle adjustSensitivity() {
        double gcd = MathUtils.computeGcd();
        Angle previousAngle = AngleConnection.INSTANCE.getServerAngle();
        float adjustedYaw = this.adjustAxis(this.yaw, previousAngle.yaw, gcd);
        float adjustedPitch = this.adjustAxis(this.pitch, previousAngle.pitch, gcd);
        return new Angle(adjustedYaw, Mth.clamp((float)adjustedPitch, (float)-90.0f, (float)90.0f));
    }

    public Angle random(float f) {
        return new Angle(this.yaw + MathUtils.getRandom(-f, f), this.pitch + MathUtils.getRandom(-f, f));
    }

    private float adjustAxis(float axisValue, float previousValue, double gcd) {
        float delta = axisValue - previousValue;
        return previousValue + (float)Math.round((double)delta / gcd) * (float)gcd;
    }

    public final Vec3 toVector() {
        float f = this.pitch * ((float)Math.PI / 180);
        float g = -this.yaw * ((float)Math.PI / 180);
        float h = Mth.cos((double)g);
        float i = Mth.sin((double)g);
        float j = Mth.cos((double)f);
        float k = Mth.sin((double)f);
        return new Vec3(i * j, -k, h * j);
    }

    public Angle addYaw(float yaw) {
        return new Angle(this.yaw + yaw, this.pitch);
    }

    public Angle addPitch(float pitch) {
        this.pitch = Mth.clamp((float)(this.pitch + pitch), (float)-90.0f, (float)90.0f);
        return this;
    }

    public Angle of(Angle angle) {
        return new Angle(angle.getYaw(), angle.getPitch());
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public float getPitch() {
        return this.pitch;
    }

    @Generated
    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    @Generated
    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    @Generated
    public String toString() {
        return "Angle(yaw=" + this.getYaw() + ", pitch=" + this.getPitch() + ")";
    }

    @Generated
    public Angle(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public static class VecRotation {
        private final Angle angle;
        private final Vec3 vec;

        @Generated
        public String toString() {
            return "Angle.VecRotation(angle=" + String.valueOf(this.getAngle()) + ", vec=" + String.valueOf(this.getVec()) + ")";
        }

        @Generated
        public Angle getAngle() {
            return this.angle;
        }

        @Generated
        public Vec3 getVec() {
            return this.vec;
        }

        @Generated
        public VecRotation(Angle angle, Vec3 vec) {
            this.angle = angle;
            this.vec = vec;
        }
    }
}

