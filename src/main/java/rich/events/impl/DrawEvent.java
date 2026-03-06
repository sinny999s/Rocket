
package rich.events.impl;

import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import rich.events.api.events.Event;
import rich.util.render.draw.DrawEngine;

public class DrawEvent
implements Event {
    private GuiGraphics drawContext;
    private DrawEngine drawEngine;
    private float partialTicks;

    @Generated
    public GuiGraphics getDrawContext() {
        return this.drawContext;
    }

    @Generated
    public DrawEngine getDrawEngine() {
        return this.drawEngine;
    }

    @Generated
    public float getPartialTicks() {
        return this.partialTicks;
    }

    @Generated
    public DrawEvent(GuiGraphics drawContext, DrawEngine drawEngine, float partialTicks) {
        this.drawContext = drawContext;
        this.drawEngine = drawEngine;
        this.partialTicks = partialTicks;
    }
}

