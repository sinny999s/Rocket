
package rich.events.impl;

import net.minecraft.world.phys.Vec3;
import rich.events.api.events.callables.EventCancellable;

public class PlayerTravelEvent
extends EventCancellable {
    private Vec3 motion;
    private final boolean pre;

    public PlayerTravelEvent(Vec3 motion, boolean pre) {
        this.motion = motion;
        this.pre = pre;
    }

    public Vec3 getMotion() {
        return this.motion;
    }

    public void setMotion(Vec3 motion) {
        this.motion = motion;
    }

    public boolean isPre() {
        return this.pre;
    }
}

