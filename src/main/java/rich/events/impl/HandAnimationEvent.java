
package rich.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Generated;
import net.minecraft.world.InteractionHand;
import rich.events.api.events.callables.EventCancellable;

public class HandAnimationEvent
extends EventCancellable {
    private PoseStack matrices;
    private InteractionHand hand;
    private float swingProgress;

    @Generated
    public HandAnimationEvent(PoseStack matrices, InteractionHand hand, float swingProgress) {
        this.matrices = matrices;
        this.hand = hand;
        this.swingProgress = swingProgress;
    }

    @Generated
    public PoseStack getMatrices() {
        return this.matrices;
    }

    @Generated
    public InteractionHand getHand() {
        return this.hand;
    }

    @Generated
    public float getSwingProgress() {
        return this.swingProgress;
    }

    @Generated
    public void setMatrices(PoseStack matrices) {
        this.matrices = matrices;
    }

    @Generated
    public void setHand(InteractionHand hand) {
        this.hand = hand;
    }

    @Generated
    public void setSwingProgress(float swingProgress) {
        this.swingProgress = swingProgress;
    }
}

