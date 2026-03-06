
package rich.events.impl;

import lombok.Generated;
import net.minecraft.client.gui.screens.Screen;
import rich.events.api.events.callables.EventCancellable;

public class CloseScreenEvent
extends EventCancellable {
    private Screen screen;

    @Generated
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof CloseScreenEvent)) {
            return false;
        }
        CloseScreenEvent other = (CloseScreenEvent)o;
        if (!other.canEqual(this)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        Screen this$screen = this.getScreen();
        Screen other$screen = other.getScreen();
        return !(this$screen == null ? other$screen != null : !this$screen.equals(other$screen));
    }

    @Generated
    protected boolean canEqual(Object other) {
        return other instanceof CloseScreenEvent;
    }

    @Generated
    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Screen $screen = this.getScreen();
        result = result * 59 + ($screen == null ? 43 : $screen.hashCode());
        return result;
    }

    @Generated
    public Screen getScreen() {
        return this.screen;
    }

    @Generated
    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    @Generated
    public String toString() {
        return "CloseScreenEvent(screen=" + String.valueOf(this.getScreen()) + ")";
    }

    @Generated
    public CloseScreenEvent(Screen screen) {
        this.screen = screen;
    }
}

