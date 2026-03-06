
package rich.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import rich.events.api.events.Event;

public class Event3D
implements Event {
    public PoseStack stack;
    public MultiBufferSource buffer;

    public Event3D(PoseStack stack, MultiBufferSource buffer) {
        this.stack = stack;
        this.buffer = buffer;
    }
}

