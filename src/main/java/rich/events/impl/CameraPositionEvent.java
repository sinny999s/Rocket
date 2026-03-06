
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.phys.Vec3;
import rich.events.api.events.Event;

public class CameraPositionEvent
implements Event {
    private Vec3 pos;

    @Generated
    public Vec3 getPos() {
        return this.pos;
    }

    @Generated
    public void setPos(Vec3 pos) {
        this.pos = pos;
    }

    @Generated
    public CameraPositionEvent(Vec3 pos) {
        this.pos = pos;
    }
}

