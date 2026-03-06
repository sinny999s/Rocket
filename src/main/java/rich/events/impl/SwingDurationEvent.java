
package rich.events.impl;

import lombok.Generated;
import rich.events.api.events.callables.EventCancellable;

public class SwingDurationEvent
extends EventCancellable {
    private float animation;

    @Generated
    public float getAnimation() {
        return this.animation;
    }

    @Generated
    public void setAnimation(float animation) {
        this.animation = animation;
    }
}

