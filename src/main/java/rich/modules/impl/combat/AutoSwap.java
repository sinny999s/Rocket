
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import java.util.Comparator;
import java.util.function.Predicate;
import lombok.Generated;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import rich.events.api.EventHandler;
import rich.events.impl.InputEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.inventory.InventoryUtils;
import rich.util.inventory.SwapExecutor;
import rich.util.inventory.SwapSettings;
import rich.util.string.chat.ChatMessage;

public class AutoSwap
extends ModuleStructure {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Swap method").value("Instant", "Legit", "Custom").selected("Legit");
    private final BindSetting swapBind = new BindSetting("Button swap", "Button for item exchange");
    private final SelectSetting firstItem = new SelectSetting("Main item", "First item").value("Totem of Undying", "Player Head", "Golden Apple", "Shield");
    private final SelectSetting secondItem = new SelectSetting("Secondary item", "Second item").value("Totem of Undying", "Player Head", "Golden Apple", "Shield");
    private final BooleanSetting stopMovement = new BooleanSetting("Stop movement", "Stop WASD").setValue(true).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final BooleanSetting stopSprint = new BooleanSetting("Stop sprint", "Stop sprint").setValue(true).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final BooleanSetting closeInventory = new BooleanSetting("Close inventory", "Send close packet").setValue(true).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings preStopDelayMin = new SliderSettings("Before stop (min)", "Ms to stop").range(0, 300).setValue(0.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings preStopDelayMax = new SliderSettings("Before stop (max)", "Ms to stop").range(0, 300).setValue(50.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings waitStopDelayMin = new SliderSettings("Stop wait (min)", "Ms waiting for stop").range(0, 500).setValue(50.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings waitStopDelayMax = new SliderSettings("Stop wait (max)", "Ms waiting for stop").range(0, 500).setValue(150.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings preSwapDelayMin = new SliderSettings("Before swap (min)", "Ms before swap").range(0, 300).setValue(20.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings preSwapDelayMax = new SliderSettings("Before swap (max)", "Ms before swap").range(0, 300).setValue(100.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings postSwapDelayMin = new SliderSettings("After swap (min)", "Ms after swap").range(0, 300).setValue(20.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings postSwapDelayMax = new SliderSettings("After swap (max)", "Ms after swap").range(0, 300).setValue(80.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings resumeDelayMin = new SliderSettings("Recovery (min)", "Ms to recovery").range(0, 300).setValue(50.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings resumeDelayMax = new SliderSettings("Recovery (max)", "Ms to recovery").range(0, 300).setValue(150.0f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SliderSettings velocityThreshold = new SliderSettings("Speed threshold", "Threshold for stop detection").range(1.0E-4f, 0.05f).setValue(0.001f).visible(() -> this.modeSetting.getSelected().equals("Custom"));
    private final SwapExecutor executor = new SwapExecutor();

    public AutoSwap() {
        super("AutoSwap", "Auto Swap", ModuleCategory.COMBAT);
        this.settings(this.modeSetting, this.swapBind, this.firstItem, this.secondItem, this.stopMovement, this.stopSprint, this.closeInventory, this.preStopDelayMin, this.preStopDelayMax, this.waitStopDelayMin, this.waitStopDelayMax, this.preSwapDelayMin, this.preSwapDelayMax, this.postSwapDelayMin, this.postSwapDelayMax, this.resumeDelayMin, this.resumeDelayMax, this.velocityThreshold);
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        Slot targetSlot;
        if (AutoSwap.mc.player == null || AutoSwap.mc.level == null) {
            return;
        }
        if (AutoSwap.mc.screen != null) {
            return;
        }
        if (!e.isKeyDown(this.swapBind.getKey())) {
            return;
        }
        if (this.executor.isRunning()) {
            return;
        }
        Slot hotbarSlot = this.findValidSlot(s -> s.index >= 36 && s.index <= 44);
        Slot slot = targetSlot = hotbarSlot != null ? hotbarSlot : this.findValidSlot(s -> s.index >= 9 && s.index <= 35);
        if (targetSlot == null) {
            ChatMessage.brandmessage("Item not found in inventory");
            return;
        }
        SwapSettings settings = this.buildSettings();
        int slotId = targetSlot.index;
        this.executor.execute(() -> InventoryUtils.swap(slotId, 45), settings);
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AutoSwap.mc.player == null || AutoSwap.mc.level == null) {
            return;
        }
        this.executor.tick();
    }

    @EventHandler
    public void onInput(InputEvent e) {
        if (AutoSwap.mc.player == null) {
            return;
        }
        if (this.executor.isBlocking()) {
            e.setDirectionalLow(false, false, false, false);
            e.setJumping(false);
        }
    }

    private SwapSettings buildSettings() {
        String mode;
        return switch (mode = this.modeSetting.getSelected()) {
            case "Instant" -> SwapSettings.instant();
            case "Legit" -> SwapSettings.legit();
            default -> new SwapSettings().stopMovement(this.stopMovement.isValue()).stopSprint(this.stopSprint.isValue()).closeInventory(this.closeInventory.isValue()).preStopDelay(this.preStopDelayMin.getInt(), this.preStopDelayMax.getInt()).waitStopDelay(this.waitStopDelayMin.getInt(), this.waitStopDelayMax.getInt()).preSwapDelay(this.preSwapDelayMin.getInt(), this.preSwapDelayMax.getInt()).postSwapDelay(this.postSwapDelayMin.getInt(), this.postSwapDelayMax.getInt()).resumeDelay(this.resumeDelayMin.getInt(), this.resumeDelayMax.getInt()).velocityThreshold(this.velocityThreshold.getValue());
        };
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private Slot findValidSlot(Predicate<Slot> slotPredicate) {
        Slot first;
        Slot second;
        Predicate<Slot> combinedPredicate = s -> s.index != 45 && !s.getItem().isEmpty() && slotPredicate.test((Slot)s);
        Item firstType = this.getItemByType(this.firstItem.getSelected());
        Item secondType = this.getItemByType(this.secondItem.getSelected());
        Item offHandItem = AutoSwap.mc.player.getOffhandItem().getItem();
        String offHandItemName = AutoSwap.mc.player.getOffhandItem().getHoverName().getString();
        if (offHandItem == firstType && (second = InventoryUtils.findSlot(secondType, combinedPredicate.and(s -> !s.getItem().getHoverName().getString().equals(offHandItemName)), Comparator.comparing(s -> s.getItem().isEnchanted()))) != null) {
            return second;
        }
        if (offHandItem == secondType && (first = InventoryUtils.findSlot(firstType, combinedPredicate.and(s -> !s.getItem().getHoverName().getString().equals(offHandItemName)), Comparator.comparing(s -> s.getItem().isEnchanted()))) != null) {
            return first;
        }
        if (offHandItem != firstType && offHandItem != secondType) {
            first = InventoryUtils.findSlot(firstType, combinedPredicate.and(s -> !s.getItem().getHoverName().getString().equals(offHandItemName)), Comparator.comparing(s -> s.getItem().isEnchanted()));
            if (first != null) {
                return first;
            }
            Slot second2 = InventoryUtils.findSlot(secondType, combinedPredicate.and(s -> !s.getItem().getHoverName().getString().equals(offHandItemName)), Comparator.comparing(s -> s.getItem().isEnchanted()));
            if (second2 != null) {
                return second2;
            }
        }
        return null;
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "Totem of Undying" -> Items.TOTEM_OF_UNDYING;
            case "Player Head" -> Items.PLAYER_HEAD;
            case "Golden Apple" -> Items.GOLDEN_APPLE;
            case "Shield" -> Items.SHIELD;
            default -> Items.AIR;
        };
    }

    @Override
    public void deactivate() {
        this.executor.cancel();
    }

    @Generated
    public SelectSetting getModeSetting() {
        return this.modeSetting;
    }

    @Generated
    public BindSetting getSwapBind() {
        return this.swapBind;
    }

    @Generated
    public SelectSetting getFirstItem() {
        return this.firstItem;
    }

    @Generated
    public SelectSetting getSecondItem() {
        return this.secondItem;
    }

    @Generated
    public BooleanSetting getStopMovement() {
        return this.stopMovement;
    }

    @Generated
    public BooleanSetting getStopSprint() {
        return this.stopSprint;
    }

    @Generated
    public BooleanSetting getCloseInventory() {
        return this.closeInventory;
    }

    @Generated
    public SliderSettings getPreStopDelayMin() {
        return this.preStopDelayMin;
    }

    @Generated
    public SliderSettings getPreStopDelayMax() {
        return this.preStopDelayMax;
    }

    @Generated
    public SliderSettings getWaitStopDelayMin() {
        return this.waitStopDelayMin;
    }

    @Generated
    public SliderSettings getWaitStopDelayMax() {
        return this.waitStopDelayMax;
    }

    @Generated
    public SliderSettings getPreSwapDelayMin() {
        return this.preSwapDelayMin;
    }

    @Generated
    public SliderSettings getPreSwapDelayMax() {
        return this.preSwapDelayMax;
    }

    @Generated
    public SliderSettings getPostSwapDelayMin() {
        return this.postSwapDelayMin;
    }

    @Generated
    public SliderSettings getPostSwapDelayMax() {
        return this.postSwapDelayMax;
    }

    @Generated
    public SliderSettings getResumeDelayMin() {
        return this.resumeDelayMin;
    }

    @Generated
    public SliderSettings getResumeDelayMax() {
        return this.resumeDelayMax;
    }

    @Generated
    public SliderSettings getVelocityThreshold() {
        return this.velocityThreshold;
    }

    @Generated
    public SwapExecutor getExecutor() {
        return this.executor;
    }
}

