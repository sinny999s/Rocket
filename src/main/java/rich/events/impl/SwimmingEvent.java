
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import rich.events.api.events.callables.EventCancellable;

public class SwimmingEvent
extends EventCancellable {
    Vec3 vector;

    @Generated
    public void setVector(Vec3 vector) {
        this.vector = vector;
    }

    @Generated
    public Vec3 getVector() {
        return this.vector;
    }

    @Generated
    public SwimmingEvent(Vec3 vector) {
        this.vector = vector;
    }
}

