
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import java.util.stream.Stream;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import rich.events.api.EventHandler;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.HandledScreenEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.inventory.InventoryUtils;
import rich.util.string.PlayerInteractionHelper;
import rich.util.timer.StopWatch;

public class ItemScroller
extends ModuleStructure {
    private final StopWatch stopWatch = new StopWatch();
    private final SliderSettings scrollerSetting = new SliderSettings("Delay item scrolling", "Select item scrolling delay").setValue(50.0f).range(0, 200);

    public ItemScroller() {
        super("ItemScroller", "Item Scroller", ModuleCategory.PLAYER);
        this.settings(this.scrollerSetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onHandledScreen(HandledScreenEvent e) {
        if (ItemScroller.mc.player == null) {
            return;
        }
        Slot hoverSlot = e.getSlotHover();
        ClickType actionType = this.getActionType();
        if (PlayerInteractionHelper.isKey(ItemScroller.mc.options.keyShift) && !PlayerInteractionHelper.isKey(ItemScroller.mc.options.keySprint) && hoverSlot != null && hoverSlot.hasItem() && actionType != null && this.stopWatch.every(this.scrollerSetting.getValue())) {
            InventoryUtils.click(hoverSlot.index, actionType.equals((Object)ClickType.THROW) ? 1 : 0, actionType);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private ClickType getActionType() {
        return PlayerInteractionHelper.isKey(ItemScroller.mc.options.keyDrop) ? ClickType.THROW : (PlayerInteractionHelper.isKey(ItemScroller.mc.options.keyAttack) ? ClickType.QUICK_MOVE : null);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onClickSlot(ClickSlotEvent e) {
        if (ItemScroller.mc.player == null) {
            return;
        }
        int slotId = e.getSlotId();
        if (slotId < 0 || slotId >= ItemScroller.mc.player.containerMenu.slots.size()) {
            return;
        }
        Slot slot = ItemScroller.mc.player.containerMenu.getSlot(slotId);
        Item item = slot.getItem().getItem();
        if (item != null && PlayerInteractionHelper.isKey(ItemScroller.mc.options.keyShift) && PlayerInteractionHelper.isKey(ItemScroller.mc.options.keySprint) && this.stopWatch.every(50.0)) {
            this.processSlotClick(slot, item, e);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processSlotClick(Slot slot, Item item, ClickSlotEvent e) {
        this.getSlots().filter(s -> s.getItem().getItem().equals(item) && s.container.equals(slot.container)).forEach(s -> InventoryUtils.click(s.index, 1, e.getActionType()));
    }

    private Stream<Slot> getSlots() {
        return ItemScroller.mc.player.containerMenu.slots.stream();
    }
}

