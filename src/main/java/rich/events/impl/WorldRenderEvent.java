
package rich.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Generated;
import rich.events.api.events.Event;

public class WorldRenderEvent
implements Event {
    private PoseStack stack;
    private float partialTicks;

    @Generated
    public WorldRenderEvent(PoseStack stack, float partialTicks) {
        this.stack = stack;
        this.partialTicks = partialTicks;
    }

    @Generated
    public PoseStack getStack() {
        return this.stack;
    }

    @Generated
    public float getPartialTicks() {
        return this.partialTicks;
    }
}

