
package rich.events.impl;

import lombok.Generated;
import rich.events.api.events.callables.EventCancellable;

public class PushEvent
extends EventCancellable {
    private Type type;

    @Generated
    public Type getType() {
        return this.type;
    }

    @Generated
    public PushEvent(Type type) {
        this.type = type;
    }

    public static enum Type {
        COLLISION,
        BLOCK,
        WATER;

    }
}

