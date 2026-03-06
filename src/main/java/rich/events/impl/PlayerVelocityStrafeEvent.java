
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import rich.events.api.events.Event;

public class PlayerVelocityStrafeEvent
implements Event {
    private final Vec3 movementInput;
    private final float speed;
    private final float yaw;
    private Vec3 velocity;

    @Generated
    public Vec3 getMovementInput() {
        return this.movementInput;
    }

    @Generated
    public float getSpeed() {
        return this.speed;
    }

    @Generated
    public float getYaw() {
        return this.yaw;
    }

    @Generated
    public Vec3 getVelocity() {
        return this.velocity;
    }

    @Generated
    public void setVelocity(Vec3 velocity) {
        this.velocity = velocity;
    }

    @Generated
    public PlayerVelocityStrafeEvent(Vec3 movementInput, float speed, float yaw, Vec3 velocity) {
        this.movementInput = movementInput;
        this.speed = speed;
        this.yaw = yaw;
        this.velocity = velocity;
    }
}

