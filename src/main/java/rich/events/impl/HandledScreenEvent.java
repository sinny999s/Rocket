
package rich.events.impl;

import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.inventory.Slot;
import rich.events.api.events.Event;

public class HandledScreenEvent
implements Event {
    private GuiGraphics drawContext;
    private Slot slotHover;
    private int backgroundWidth;
    private int backgroundHeight;

    @Generated
    public GuiGraphics getDrawContext() {
        return this.drawContext;
    }

    @Generated
    public Slot getSlotHover() {
        return this.slotHover;
    }

    @Generated
    public int getBackgroundWidth() {
        return this.backgroundWidth;
    }

    @Generated
    public int getBackgroundHeight() {
        return this.backgroundHeight;
    }

    @Generated
    public HandledScreenEvent(GuiGraphics drawContext, Slot slotHover, int backgroundWidth, int backgroundHeight) {
        this.drawContext = drawContext;
        this.slotHover = slotHover;
        this.backgroundWidth = backgroundWidth;
        this.backgroundHeight = backgroundHeight;
    }
}

