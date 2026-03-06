
package rich.events.impl;

import lombok.Generated;
import rich.events.api.events.callables.EventCancellable;

public class EntityColorEvent
extends EventCancellable {
    private int color;

    @Generated
    public int getColor() {
        return this.color;
    }

    @Generated
    public void setColor(int color) {
        this.color = color;
    }

    @Generated
    public EntityColorEvent(int color) {
        this.color = color;
    }
}

