
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import java.util.Comparator;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import rich.events.api.EventHandler;
import rich.events.impl.BlockBreakingEvent;
import rich.events.impl.HeldItemUpdateEvent;
import rich.events.impl.HotBarScrollEvent;
import rich.events.impl.HotBarUpdateEvent;
import rich.events.impl.HotbarItemRenderEvent;
import rich.events.impl.ItemRendererEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;

public class AutoTool
extends ModuleStructure {
    private final BooleanSetting silentSwap = new BooleanSetting("Visually", "Visually hide tool switching").setValue(true);
    private long lastSwapTime = 0L;
    private long lastBreakTime = 0L;
    private ItemStack originalStack = null;
    private ItemStack toolStack = null;
    private int originalSlotIndex = -1;
    private int toolSlotIndex = -1;
    private BlockPos lastBreakPos = null;
    private boolean isActive = false;
    private Slot swapBackSlot = null;

    public AutoTool() {
        super("AutoTool", "Auto Tool", ModuleCategory.PLAYER);
        this.settings(this.silentSwap);
    }

    @Override
    public void activate() {
        this.resetState();
    }

    @Override
    public void deactivate() {
        this.resetState();
    }

    private void resetState() {
        this.lastSwapTime = 0L;
        this.lastBreakTime = 0L;
        this.originalStack = null;
        this.toolStack = null;
        this.originalSlotIndex = -1;
        this.toolSlotIndex = -1;
        this.lastBreakPos = null;
        this.isActive = false;
        this.swapBackSlot = null;
    }

    private void clearRenderState() {
        this.originalStack = null;
        this.toolStack = null;
        this.originalSlotIndex = -1;
        this.toolSlotIndex = -1;
    }

    @EventHandler
    public void onHeldItemUpdate(HeldItemUpdateEvent e) {
        if (!this.silentSwap.isValue()) {
            return;
        }
        if (!this.isActive) {
            return;
        }
        if (this.originalStack == null || AutoTool.mc.player == null) {
            return;
        }
        if (AutoTool.mc.player.getInventory().getSelectedSlot() != this.originalSlotIndex) {
            this.clearRenderState();
            return;
        }
        e.setMainHand(this.originalStack);
    }

    @EventHandler
    public void onItemRenderer(ItemRendererEvent e) {
        if (!this.silentSwap.isValue()) {
            return;
        }
        if (!this.isActive) {
            return;
        }
        if (this.originalStack == null) {
            return;
        }
        if (e.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (!Objects.equals(AutoTool.mc.player, e.getPlayer())) {
            return;
        }
        if (AutoTool.mc.player.getInventory().getSelectedSlot() != this.originalSlotIndex) {
            this.clearRenderState();
            return;
        }
        e.setStack(this.originalStack);
    }

    @EventHandler
    public void onHotbarItemRender(HotbarItemRenderEvent e) {
        if (!this.silentSwap.isValue()) {
            return;
        }
        if (!this.isActive) {
            return;
        }
        if (this.originalStack == null || AutoTool.mc.player == null) {
            return;
        }
        int currentSelectedSlot = AutoTool.mc.player.getInventory().getSelectedSlot();
        if (currentSelectedSlot != this.originalSlotIndex) {
            this.clearRenderState();
            return;
        }
        if (e.getHotbarIndex() == this.originalSlotIndex) {
            e.setStack(this.originalStack);
        }
        if (this.toolSlotIndex != -1 && this.toolSlotIndex != this.originalSlotIndex && e.getHotbarIndex() == this.toolSlotIndex) {
            e.setStack(this.toolStack);
        }
    }

    @EventHandler
    public void onHotBarUpdate(HotBarUpdateEvent e) {
        if (this.isActive) {
            e.cancel();
        }
    }

    @EventHandler
    public void onHotBarScroll(HotBarScrollEvent e) {
        if (this.isActive) {
            e.cancel();
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onBlockBreaking(BlockBreakingEvent e) {
        if (AutoTool.mc.player == null || AutoTool.mc.level == null) {
            return;
        }
        this.lastBreakTime = System.currentTimeMillis();
        this.lastBreakPos = e.blockPos();
        if (AutoTool.mc.player.isCreative()) {
            return;
        }
        if (this.isActive) {
            return;
        }
        if (!this.hasSwapCooldownPassed()) {
            return;
        }
        Slot bestSlot = this.findBestTool(this.lastBreakPos);
        Slot mainHandSlot = this.getMainHandSlot();
        if (bestSlot == null || mainHandSlot == null) {
            return;
        }
        if (bestSlot.index == mainHandSlot.index) {
            return;
        }
        int selectedSlot = AutoTool.mc.player.getInventory().getSelectedSlot();
        int bestToolHotbarIndex = bestSlot.index - 36;
        if (this.silentSwap.isValue()) {
            this.originalStack = AutoTool.mc.player.getInventory().getItem(selectedSlot).copy();
            this.toolStack = AutoTool.mc.player.getInventory().getItem(bestToolHotbarIndex).copy();
            this.originalSlotIndex = selectedSlot;
            this.toolSlotIndex = bestToolHotbarIndex;
        }
        this.swapBackSlot = bestSlot;
        this.swapToHand(bestSlot);
        this.isActive = true;
        this.lastSwapTime = System.currentTimeMillis();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AutoTool.mc.player == null || AutoTool.mc.level == null) {
            return;
        }
        if (!this.isActive) {
            if (this.originalStack != null) {
                this.clearRenderState();
            }
            return;
        }
        if (this.silentSwap.isValue()) {
            int currentSlot = AutoTool.mc.player.getInventory().getSelectedSlot();
            if (this.originalSlotIndex != -1 && currentSlot != this.originalSlotIndex) {
                this.forceReset();
                return;
            }
        }
        if (!this.hasSwapCooldownPassed()) {
            return;
        }
        boolean breakingStopped = this.hasBreakingCooldownPassed();
        if (breakingStopped) {
            if (this.swapBackSlot != null) {
                this.swapToHand(this.swapBackSlot);
            }
            this.clearRenderState();
            this.isActive = false;
            this.swapBackSlot = null;
            this.lastSwapTime = System.currentTimeMillis();
        }
    }

    private void forceReset() {
        this.clearRenderState();
        this.isActive = false;
        this.swapBackSlot = null;
    }

    private boolean hasSwapCooldownPassed() {
        return System.currentTimeMillis() - this.lastSwapTime >= 350L;
    }

    private boolean hasBreakingCooldownPassed() {
        return System.currentTimeMillis() - this.lastBreakTime >= 100L;
    }

    private Slot getMainHandSlot() {
        if (AutoTool.mc.player == null) {
            return null;
        }
        int selectedSlot = AutoTool.mc.player.getInventory().getSelectedSlot();
        return AutoTool.mc.player.inventoryMenu.getSlot(36 + selectedSlot);
    }

    private void swapToHand(Slot slot) {
        if (AutoTool.mc.player == null || AutoTool.mc.gameMode == null || slot == null) {
            return;
        }
        int hotbarSlot = AutoTool.mc.player.getInventory().getSelectedSlot();
        AutoTool.mc.gameMode.handleInventoryMouseClick(AutoTool.mc.player.inventoryMenu.containerId, slot.index, hotbarSlot, ClickType.SWAP, AutoTool.mc.player);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private Slot findBestTool(BlockPos blockPos) {
        if (AutoTool.mc.player == null || AutoTool.mc.level == null || blockPos == null) {
            return this.getMainHandSlot();
        }
        BlockState state = AutoTool.mc.level.getBlockState(blockPos);
        if (state.isAir()) {
            return this.getMainHandSlot();
        }
        Slot mainHandSlot = this.getMainHandSlot();
        float currentSpeed = mainHandSlot != null ? mainHandSlot.getItem().getDestroySpeed(state) : 1.0f;
        Slot bestSlot = AutoTool.mc.player.inventoryMenu.slots.stream().filter(slot -> slot.index >= 36 && slot.index <= 44).filter(slot -> !slot.getItem().isEmpty()).filter(slot -> slot.getItem().getDestroySpeed(state) > 1.0f).max(Comparator.comparingDouble(slot -> slot.getItem().getDestroySpeed(state))).orElse(null);
        if (bestSlot != null && bestSlot.getItem().getDestroySpeed(state) > currentSpeed) {
            return bestSlot;
        }
        return mainHandSlot;
    }

    @Generated
    public BooleanSetting getSilentSwap() {
        return this.silentSwap;
    }

    @Generated
    public long getLastSwapTime() {
        return this.lastSwapTime;
    }

    @Generated
    public long getLastBreakTime() {
        return this.lastBreakTime;
    }

    @Generated
    public ItemStack getOriginalStack() {
        return this.originalStack;
    }

    @Generated
    public ItemStack getToolStack() {
        return this.toolStack;
    }

    @Generated
    public int getOriginalSlotIndex() {
        return this.originalSlotIndex;
    }

    @Generated
    public int getToolSlotIndex() {
        return this.toolSlotIndex;
    }

    @Generated
    public BlockPos getLastBreakPos() {
        return this.lastBreakPos;
    }

    @Generated
    public boolean isActive() {
        return this.isActive;
    }

    @Generated
    public Slot getSwapBackSlot() {
        return this.swapBackSlot;
    }
}

