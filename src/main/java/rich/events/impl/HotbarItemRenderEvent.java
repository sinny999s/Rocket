
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import rich.events.api.events.Event;

public class HotbarItemRenderEvent
implements Event {
    private ItemStack stack;
    private final int hotbarIndex;

    public HotbarItemRenderEvent(ItemStack stack, int hotbarIndex) {
        this.stack = stack;
        this.hotbarIndex = hotbarIndex;
    }

    @Generated
    public ItemStack getStack() {
        return this.stack;
    }

    @Generated
    public int getHotbarIndex() {
        return this.hotbarIndex;
    }

    @Generated
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
}

