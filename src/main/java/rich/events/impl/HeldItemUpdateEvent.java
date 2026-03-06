
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import rich.events.api.events.Event;

public class HeldItemUpdateEvent
implements Event {
    private ItemStack mainHand;
    private ItemStack offHand;

    @Generated
    public ItemStack getMainHand() {
        return this.mainHand;
    }

    @Generated
    public ItemStack getOffHand() {
        return this.offHand;
    }

    @Generated
    public void setMainHand(ItemStack mainHand) {
        this.mainHand = mainHand;
    }

    @Generated
    public void setOffHand(ItemStack offHand) {
        this.offHand = offHand;
    }

    @Generated
    public HeldItemUpdateEvent(ItemStack mainHand, ItemStack offHand) {
        this.mainHand = mainHand;
        this.offHand = offHand;
    }
}

