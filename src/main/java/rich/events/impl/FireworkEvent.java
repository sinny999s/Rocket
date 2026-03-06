
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import rich.events.api.events.Event;

public class FireworkEvent
implements Event {
    public Vec3 vector;

    @Generated
    public FireworkEvent(Vec3 vector) {
        this.vector = vector;
    }

    @Generated
    public Vec3 getVector() {
        return this.vector;
    }

    @Generated
    public void setVector(Vec3 vector) {
        this.vector = vector;
    }
}

