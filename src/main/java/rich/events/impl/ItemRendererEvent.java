
package rich.events.impl;

import lombok.Generated;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import rich.events.api.events.Event;

public class ItemRendererEvent
implements Event {
    private AbstractClientPlayer player;
    private ItemStack stack;
    private InteractionHand hand;

    @Generated
    public AbstractClientPlayer getPlayer() {
        return this.player;
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
    public void setPlayer(AbstractClientPlayer player) {
        this.player = player;
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
    public ItemRendererEvent(AbstractClientPlayer player, ItemStack stack, InteractionHand hand) {
        this.player = player;
        this.stack = stack;
        this.hand = hand;
    }
}

