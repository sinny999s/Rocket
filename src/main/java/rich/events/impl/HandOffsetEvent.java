
package rich.events.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Generated;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import rich.events.api.events.callables.EventCancellable;

public class HandOffsetEvent
extends EventCancellable {
    private PoseStack matrices;
    private ItemStack stack;
    private InteractionHand hand;
    private float scale;

    public HandOffsetEvent(PoseStack matrices, ItemStack stack, InteractionHand hand) {
        this.matrices = matrices;
        this.stack = stack;
        this.hand = hand;
        this.scale = 1.0f;
    }

    @Generated
    public PoseStack getMatrices() {
        return this.matrices;
    }

    @Generated
    public ItemStack getStack() {
        return this.stack;
    }

    @Generated
    public InteractionHand getHand() {
        return this.hand;
    }

    @Generated
    public float getScale() {
        return this.scale;
    }

    @Generated
    public void setMatrices(PoseStack matrices) {
        this.matrices = matrices;
    }

    @Generated
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Generated
    public void setHand(InteractionHand hand) {
        this.hand = hand;
    }

    @Generated
    public void setScale(float scale) {
        this.scale = scale;
    }
}

