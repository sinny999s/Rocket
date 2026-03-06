
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.inventory.ClickType;
import rich.events.api.events.callables.EventCancellable;

public class ClickSlotEvent
extends EventCancellable {
    private int windowId;
    private int slotId;
    private int button;
    private ClickType actionType;

    @Generated
    public int getWindowId() {
        return this.windowId;
    }

    @Generated
    public int getSlotId() {
        return this.slotId;
    }

    @Generated
    public int getButton() {
        return this.button;
    }

    @Generated
    public ClickType getActionType() {
        return this.actionType;
    }

    @Generated
    public void setWindowId(int windowId) {
        this.windowId = windowId;
    }

    @Generated
    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    @Generated
    public void setButton(int button) {
        this.button = button;
    }

    @Generated
    public void setActionType(ClickType actionType) {
        this.actionType = actionType;
    }

    @Generated
    public ClickSlotEvent(int windowId, int slotId, int button, ClickType actionType) {
        this.windowId = windowId;
        this.slotId = slotId;
        this.button = button;
        this.actionType = actionType;
    }
}

