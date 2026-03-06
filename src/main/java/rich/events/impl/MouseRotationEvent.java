
package rich.events.impl;

import lombok.Generated;
import rich.events.api.events.callables.EventCancellable;

public class MouseRotationEvent
extends EventCancellable {
    float cursorDeltaX;
    float cursorDeltaY;

    @Generated
    public float getCursorDeltaX() {
        return this.cursorDeltaX;
    }

    @Generated
    public float getCursorDeltaY() {
        return this.cursorDeltaY;
    }

    @Generated
    public void setCursorDeltaX(float cursorDeltaX) {
        this.cursorDeltaX = cursorDeltaX;
    }

    @Generated
    public void setCursorDeltaY(float cursorDeltaY) {
        this.cursorDeltaY = cursorDeltaY;
    }

    @Generated
    public MouseRotationEvent(float cursorDeltaX, float cursorDeltaY) {
        this.cursorDeltaX = cursorDeltaX;
        this.cursorDeltaY = cursorDeltaY;
    }
}

