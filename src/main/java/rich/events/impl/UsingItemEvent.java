
package rich.events.impl;

import lombok.Generated;
import rich.events.api.events.callables.EventCancellable;

public class UsingItemEvent
extends EventCancellable {
    byte type;

    @Generated
    public byte getType() {
        return this.type;
    }

    @Generated
    public void setType(byte type) {
        this.type = type;
    }

    @Generated
    public UsingItemEvent(byte type) {
        this.type = type;
    }
}

