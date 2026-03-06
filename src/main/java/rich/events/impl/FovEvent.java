
package rich.events.impl;

import lombok.Generated;
import rich.events.api.events.callables.EventCancellable;

public class FovEvent
extends EventCancellable {
    private int fov;

    @Generated
    public int getFov() {
        return this.fov;
    }

    @Generated
    public void setFov(int fov) {
        this.fov = fov;
    }
}

