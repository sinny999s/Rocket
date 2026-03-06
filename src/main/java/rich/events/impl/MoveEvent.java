
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import rich.events.api.events.Event;

public class MoveEvent
implements Event {
    private Vec3 movement;

    @Generated
    public Vec3 getMovement() {
        return this.movement;
    }

    @Generated
    public void setMovement(Vec3 movement) {
        this.movement = movement;
    }

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof MoveEvent)) {
            return false;
        }
        MoveEvent other = (MoveEvent)o;
        if (!other.canEqual(this)) {
            return false;
        }
        Vec3 this$movement = this.getMovement();
        Vec3 other$movement = other.getMovement();
        return !(this$movement == null ? other$movement != null : !((Object)this$movement).equals(other$movement));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof MoveEvent;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Vec3 $movement = this.getMovement();
        result = result * 59 + ($movement == null ? 43 : ((Object)$movement).hashCode());
        return result;
    }

    @Generated
    public String toString() {
        return "MoveEvent(movement=" + String.valueOf(this.getMovement()) + ")";
    }

    @Generated
    public MoveEvent(Vec3 movement) {
        this.movement = movement;
    }
}

