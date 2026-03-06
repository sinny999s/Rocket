
package rich.events.impl;

import lombok.Generated;
import rich.events.api.events.Event;

public class RotationUpdateEvent
implements Event {
    byte type;

    @Generated
    public byte getType() {
        return this.type;
    }

    @Generated
    public RotationUpdateEvent(byte type) {
        this.type = type;
    }
}

