
package rich.modules.impl.combat.aura;

import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.impl.RotateConstructor;

public class AngleConstructor
implements IMinecraft {
    private final Angle angle;
    private final Vec3 vec3d;
    private final Entity entity;
    private final RotateConstructor angleSmooth;
    private final int ticksUntilReset;
    private final float resetThreshold;
    public final boolean moveCorrection;
    public final boolean freeCorrection;
    public final boolean changeLook = false;

    public Angle nextRotation(Angle fromAngle, boolean isResetting) {
        if (isResetting) {
            return this.angleSmooth.limitAngleChange(fromAngle, MathAngle.fromVec2f(AngleConstructor.mc.player.getRotationVector()));
        }
        return this.angleSmooth.limitAngleChange(fromAngle, this.angle, this.vec3d, this.entity);
    }

    @Generated
    public Angle getAngle() {
        return this.angle;
    }

    @Generated
    public Vec3 getVec3d() {
        return this.vec3d;
    }

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

    @Generated
    public RotateConstructor getAngleSmooth() {
        return this.angleSmooth;
    }

    @Generated
    public int getTicksUntilReset() {
        return this.ticksUntilReset;
    }

    @Generated
    public float getResetThreshold() {
        return this.resetThreshold;
    }

    @Generated
    public boolean isMoveCorrection() {
        return this.moveCorrection;
    }

    @Generated
    public boolean isChangeLook() {
        return this.changeLook;
    }

    @Generated
    public AngleConstructor(Angle angle, Vec3 vec3d, Entity entity, RotateConstructor angleSmooth, int ticksUntilReset, float resetThreshold, boolean moveCorrection, boolean freeCorrection) {
        this.angle = angle;
        this.vec3d = vec3d;
        this.entity = entity;
        this.angleSmooth = angleSmooth;
        this.ticksUntilReset = ticksUntilReset;
        this.resetThreshold = resetThreshold;
        this.moveCorrection = moveCorrection;
        this.freeCorrection = freeCorrection;
    }

    @Generated
    public boolean isFreeCorrection() {
        return this.freeCorrection;
    }
}

