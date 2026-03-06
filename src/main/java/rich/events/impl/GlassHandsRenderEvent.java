
package rich.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Generated;
import rich.events.api.events.callables.EventCancellable;

public class GlassHandsRenderEvent
extends EventCancellable {
    private Phase phase;
    private PoseStack matrices;
    private float tickDelta;

    @Generated
    public GlassHandsRenderEvent(Phase phase, PoseStack matrices, float tickDelta) {
        this.phase = phase;
        this.matrices = matrices;
        this.tickDelta = tickDelta;
    }

    @Generated
    public Phase getPhase() {
        return this.phase;
    }

    @Generated
    public PoseStack getMatrices() {
        return this.matrices;
    }

    @Generated
    public float getTickDelta() {
        return this.tickDelta;
    }

    @Generated
    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    @Generated
    public void setMatrices(PoseStack matrices) {
        this.matrices = matrices;
    }

    @Generated
    public void setTickDelta(float tickDelta) {
        this.tickDelta = tickDelta;
    }

    public static enum Phase {
        PRE,
        POST;

    }
}

