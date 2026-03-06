
package rich.events.impl;

import lombok.Generated;
import net.minecraft.client.gui.screens.Screen;
import rich.events.api.events.Event;

public class SetScreenEvent
implements Event {
    public Screen screen;

    @Generated
    public Screen getScreen() {
        return this.screen;
    }

    @Generated
    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    @Generated
    public SetScreenEvent(Screen screen) {
        this.screen = screen;
    }
}

