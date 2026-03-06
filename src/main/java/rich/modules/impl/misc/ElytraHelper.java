
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.IntFunction;
import lombok.Generated;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import rich.events.api.EventHandler;
import rich.events.impl.HotBarScrollEvent;
import rich.events.impl.InputEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.TickEvent;
import rich.mixin.ClientWorldAccessor;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.inventory.InventoryUtils;
import rich.util.inventory.MovementController;
import rich.util.inventory.SwapSettings;
import rich.util.string.chat.ChatMessage;
import rich.util.timer.StopWatch;

public class ElytraHelper
extends ModuleStructure {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Swap method").value("Instant", "Legit").selected("Legit");
    private final BindSetting swapBind = new BindSetting("Button swap", "Button for elytra/chestplate swap");
    private final BindSetting fireworkBind = new BindSetting("Firework button", "Button for firework use");
    private final BooleanSetting autoTakeoff = new BooleanSetting("Auto takeoff", "Auto takeoff with equipped elytra").setValue(false);
    private final BooleanSetting autoFirework = new BooleanSetting("Auto firework", "Auto firework usage during flight").setValue(false).visible(() -> this.autoTakeoff.isValue());
    private final MovementController swapMovement = new MovementController();
    private final MovementController fireworkMovement = new MovementController();
    private final StopWatch fireworkCooldown = new StopWatch();
    private final StopWatch autoFireworkTimer = new StopWatch();
    private SwapPhase swapPhase = SwapPhase.IDLE;
    private FireworkPhase fireworkPhase = FireworkPhase.IDLE;
    private int armorSlot = -1;
    private int fireworkSlot = -1;
    private int savedFireworkSlot = -1;
    private boolean fireworkFromInventory = false;
    private long swapPhaseStartTime = 0L;
    private long fireworkPhaseStartTime = 0L;
    private int swapCurrentDelay = 0;
    private int fireworkCurrentDelay = 0;
    private boolean shouldJumpForTakeoff = false;

    public ElytraHelper() {
        super("ElytraHelper", "Elytra Helper", ModuleCategory.MISC);
        this.settings(this.modeSetting, this.swapBind, this.fireworkBind);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onKey(KeyEvent e) {
        if (ElytraHelper.mc.player == null || ElytraHelper.mc.level == null) {
            return;
        }
        if (ElytraHelper.mc.screen != null) {
            return;
        }
        if (e.action() != 1) {
            return;
        }
        if (this.matchesBind(e, this.swapBind) && this.swapPhase == SwapPhase.IDLE) {
            this.startArmorSwap();
        }
        if (this.matchesBind(e, this.fireworkBind) && this.fireworkPhase == FireworkPhase.IDLE) {
            if (!this.isElytraEquipped()) {
                return;
            }
            if (this.fireworkCooldown.finished(500.0)) {
                this.useFirework();
                this.fireworkCooldown.reset();
            }
        }
    }

    @EventHandler
    public void onScroll(HotBarScrollEvent e) {
        if (ElytraHelper.mc.player == null || ElytraHelper.mc.level == null) {
            return;
        }
        if (ElytraHelper.mc.screen != null) {
            return;
        }
        if (this.matchesScrollBind(e, this.swapBind) && this.swapPhase == SwapPhase.IDLE) {
            e.setCancelled(true);
            this.startArmorSwap();
        }
        if (this.matchesScrollBind(e, this.fireworkBind) && this.fireworkPhase == FireworkPhase.IDLE) {
            if (!this.isElytraEquipped()) {
                return;
            }
            if (this.fireworkCooldown.finished(500.0)) {
                e.setCancelled(true);
                this.useFirework();
                this.fireworkCooldown.reset();
            }
        }
    }

    private boolean matchesBind(KeyEvent e, BindSetting bind) {
        int bindKey = bind.getKey();
        int bindType = bind.getType();
        if (bindKey == -1 || bindKey == -1) {
            return false;
        }
        if (bindType == 2) {
            if (bindKey == 1002 && e.type() == InputConstants.Type.MOUSE && e.key() == 2) {
                return true;
            }
        } else {
            if (bindType == 0 && e.type() == InputConstants.Type.MOUSE) {
                return e.key() == bindKey;
            }
            if (bindType == 1 && e.type() == InputConstants.Type.KEYSYM) {
                return e.key() == bindKey;
            }
        }
        return false;
    }

    private boolean matchesScrollBind(HotBarScrollEvent e, BindSetting bind) {
        int bindKey = bind.getKey();
        int bindType = bind.getType();
        if (bindType != 2) {
            return false;
        }
        if (bindKey == 1000 && e.getVertical() > 0.0) {
            return true;
        }
        return bindKey == 1001 && e.getVertical() < 0.0;
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (ElytraHelper.mc.player == null || ElytraHelper.mc.level == null) {
            this.resetAllStates();
            return;
        }
        this.processArmorSwapLoop();
        this.processFireworkUseLoop();
        this.processAutoTakeoff();
        this.processAutoFirework();
    }

    private void processArmorSwapLoop() {
        if (this.swapPhase == SwapPhase.IDLE) {
            return;
        }
        boolean continueProcessing = true;
        for (int iterations = 0; continueProcessing && iterations < 10; ++iterations) {
            continueProcessing = this.processArmorSwapTick();
        }
    }

    private void processFireworkUseLoop() {
        if (this.fireworkPhase == FireworkPhase.IDLE) {
            return;
        }
        boolean continueProcessing = true;
        for (int iterations = 0; continueProcessing && iterations < 10; ++iterations) {
            continueProcessing = this.processFireworkUseTick();
        }
    }

    @EventHandler
    public void onInput(InputEvent e) {
        if (ElytraHelper.mc.player == null) {
            return;
        }
        if (this.swapMovement.isBlocked()) {
            e.setDirectionalLow(false, false, false, false);
            e.setJumping(false);
        }
        if (this.fireworkMovement.isBlocked()) {
            e.setDirectionalLow(false, false, false, false);
            e.setJumping(false);
        }
        if (this.shouldJumpForTakeoff) {
            e.setJumping(true);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isElytraEquipped() {
        if (ElytraHelper.mc.player == null) {
            return false;
        }
        ItemStack chestStack = ElytraHelper.mc.player.getItemBySlot(EquipmentSlot.CHEST);
        return chestStack.getItem() == Items.ELYTRA;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isElytraUsable() {
        if (ElytraHelper.mc.player == null) {
            return false;
        }
        ItemStack chestStack = ElytraHelper.mc.player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.getItem() != Items.ELYTRA) {
            return false;
        }
        if (chestStack.getMaxDamage() <= 0) {
            return true;
        }
        return chestStack.getDamageValue() < chestStack.getMaxDamage() - 1;
    }

    private boolean isInstantMode() {
        return this.modeSetting.getSelected().equals("Instant");
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void processAutoTakeoff() {
        if (!this.autoTakeoff.isValue()) {
            this.shouldJumpForTakeoff = false;
            return;
        }
        if (this.swapPhase != SwapPhase.IDLE) {
            this.shouldJumpForTakeoff = false;
            return;
        }
        if (this.fireworkPhase != FireworkPhase.IDLE) {
            this.shouldJumpForTakeoff = false;
            return;
        }
        if (!this.isElytraEquipped()) {
            this.shouldJumpForTakeoff = false;
            return;
        }
        if (ElytraHelper.mc.player.onGround()) {
            this.shouldJumpForTakeoff = true;
        } else {
            this.shouldJumpForTakeoff = false;
            if (this.isElytraUsable() && !ElytraHelper.mc.player.isFallFlying() && !ElytraHelper.mc.player.getAbilities().flying) {
                mc.getConnection().send(new ServerboundPlayerCommandPacket(ElytraHelper.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void processAutoFirework() {
        if (!this.autoTakeoff.isValue() || !this.autoFirework.isValue()) {
            return;
        }
        if (ElytraHelper.mc.player == null) {
            return;
        }
        if (!this.isElytraEquipped()) {
            return;
        }
        if (!ElytraHelper.mc.player.isFallFlying()) {
            return;
        }
        if (this.fireworkPhase != FireworkPhase.IDLE) {
            return;
        }
        if (ElytraHelper.mc.player.isUsingItem()) {
            return;
        }
        if (this.autoFireworkTimer.finished(1000.0)) {
            this.useFirework();
            this.autoFireworkTimer.reset();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void useFirework() {
        if (!this.isElytraEquipped()) {
            return;
        }
        if (this.isInstantMode()) {
            this.useFireworkInstant();
        } else {
            this.startFireworkUseLegit();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void useFireworkInstant() {
        int hotbarSlot = this.findItemInHotbar(Items.FIREWORK_ROCKET);
        if (hotbarSlot != -1) {
            int currentSlot = ElytraHelper.mc.player.getInventory().getSelectedSlot();
            if (hotbarSlot != currentSlot) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(hotbarSlot));
            }
            this.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, ElytraHelper.mc.player.getYRot(), ElytraHelper.mc.player.getXRot()));
            if (hotbarSlot != currentSlot) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(currentSlot));
            }
            return;
        }
        int invSlot = InventoryUtils.findItemInInventory(Items.FIREWORK_ROCKET);
        if (invSlot != -1) {
            int currentHotbarSlot = ElytraHelper.mc.player.getInventory().getSelectedSlot();
            int wrappedSlot = InventoryUtils.wrapSlot(invSlot);
            InventoryUtils.click(wrappedSlot, currentHotbarSlot, ClickType.SWAP);
            this.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, ElytraHelper.mc.player.getYRot(), ElytraHelper.mc.player.getXRot()));
            InventoryUtils.click(wrappedSlot, currentHotbarSlot, ClickType.SWAP);
            InventoryUtils.closeScreen();
        } else {
            ChatMessage.brandmessage("Firework count");
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void startFireworkUseLegit() {
        this.savedFireworkSlot = ElytraHelper.mc.player.getInventory().getSelectedSlot();
        int hotbarSlot = this.findItemInHotbar(Items.FIREWORK_ROCKET);
        if (hotbarSlot != -1) {
            this.fireworkSlot = hotbarSlot;
            this.fireworkFromInventory = false;
            InventoryUtils.selectSlot(this.fireworkSlot);
            this.startFireworkPhase(FireworkPhase.AWAIT_ITEM, 0);
            return;
        }
        int invSlot = InventoryUtils.findItemInInventory(Items.FIREWORK_ROCKET);
        if (invSlot != -1) {
            this.fireworkSlot = invSlot;
            this.fireworkFromInventory = true;
            SwapSettings settings = this.buildSettings();
            if (settings.shouldStopMovement()) {
                this.startFireworkPhase(FireworkPhase.PRE_STOP, settings.randomPreStopDelay());
            } else {
                this.startFireworkPhase(FireworkPhase.SWAP_TO_HAND, 0);
            }
        } else {
            ChatMessage.brandmessage("Firework count");
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void startArmorSwap() {
        boolean isElytra = this.isElytraEquipped();
        Item targetItem = isElytra ? null : Items.ELYTRA;
        int slot = this.findChestArmorSlot(targetItem, isElytra);
        if (slot == -1) {
            String missing = isElytra ? "chestplate" : "elytra";
            ChatMessage.brandmessage("Count " + missing);
            return;
        }
        this.armorSlot = slot;
        String itemName = isElytra ? "Chestplate" : "Elytra";
        ChatMessage.brandmessage("Swapped to " + itemName);
        SwapSettings settings = this.buildSettings();
        if (settings.shouldStopMovement()) {
            this.startSwapPhase(SwapPhase.PRE_STOP, settings.randomPreStopDelay());
        } else {
            this.startSwapPhase(SwapPhase.SWAP_ARMOR, 0);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private int findChestArmorSlot(Item targetItem, boolean isElytraEquipped) {
        for (int i = 0; i < 46; ++i) {
            ItemStack stack = ElytraHelper.mc.player.getInventory().getItem(i);
            Equippable component = (Equippable)((Object)stack.get(DataComponents.EQUIPPABLE));
            if (component == null || component.slot() != EquipmentSlot.CHEST || !(targetItem == null ? stack.getItem() != Items.ELYTRA : stack.getItem() == targetItem)) continue;
            return i;
        }
        return -1;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private boolean processArmorSwapTick() {
        if (ElytraHelper.mc.player == null) {
            this.resetSwapState();
            return false;
        }
        long elapsed = System.currentTimeMillis() - this.swapPhaseStartTime;
        SwapSettings settings = this.buildSettings();
        switch (this.swapPhase.ordinal()) {
            case 1: {
                if (elapsed < (long)this.swapCurrentDelay) break;
                this.swapMovement.saveState();
                this.swapMovement.block();
                if (settings.shouldStopSprint()) {
                    this.swapMovement.stopSprint();
                }
                this.startSwapPhase(SwapPhase.STOPPING, 0);
                return true;
            }
            case 2: {
                this.swapMovement.block();
                if (settings.shouldStopSprint()) {
                    this.swapMovement.stopSprint();
                }
                this.startSwapPhase(SwapPhase.WAIT_STOP, settings.randomWaitStopDelay());
                return this.swapCurrentDelay == 0;
            }
            case 3: {
                boolean timeout;
                this.swapMovement.block();
                boolean stopped = this.swapMovement.isPlayerStopped(settings.getVelocityThreshold());
                boolean bl = timeout = elapsed >= (long)this.swapCurrentDelay;
                if (!stopped && !timeout) break;
                this.startSwapPhase(SwapPhase.PRE_SWAP, settings.randomPreSwapDelay());
                return this.swapCurrentDelay == 0;
            }
            case 4: {
                this.swapMovement.block();
                if (elapsed < (long)this.swapCurrentDelay) break;
                this.startSwapPhase(SwapPhase.SWAP_ARMOR, 0);
                return true;
            }
            case 5: {
                int fromSlot = InventoryUtils.wrapSlot(this.armorSlot);
                InventoryUtils.swap(fromSlot, 6);
                this.startSwapPhase(SwapPhase.POST_SWAP, settings.randomPostSwapDelay());
                return this.swapCurrentDelay == 0;
            }
            case 6: {
                if (elapsed < (long)this.swapCurrentDelay) break;
                if (settings.shouldCloseInventory()) {
                    InventoryUtils.closeScreen();
                }
                this.startSwapPhase(SwapPhase.RESUMING, settings.randomResumeDelay());
                return this.swapCurrentDelay == 0;
            }
            case 7: {
                if (elapsed < (long)this.swapCurrentDelay) break;
                if (this.buildSettings().shouldStopMovement()) {
                    this.swapMovement.restoreFromCurrent();
                }
                this.resetSwapState();
                return false;
            }
        }
        return false;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private boolean processFireworkUseTick() {
        if (ElytraHelper.mc.player == null) {
            this.resetFireworkState();
            return false;
        }
        long elapsed = System.currentTimeMillis() - this.fireworkPhaseStartTime;
        SwapSettings settings = this.buildSettings();
        switch (this.fireworkPhase.ordinal()) {
            case 1: {
                if (elapsed < (long)this.fireworkCurrentDelay) break;
                this.fireworkMovement.saveState();
                this.fireworkMovement.block();
                if (settings.shouldStopSprint()) {
                    this.fireworkMovement.stopSprint();
                }
                this.startFireworkPhase(FireworkPhase.STOPPING, 0);
                return true;
            }
            case 2: {
                this.fireworkMovement.block();
                if (settings.shouldStopSprint()) {
                    this.fireworkMovement.stopSprint();
                }
                this.startFireworkPhase(FireworkPhase.WAIT_STOP, settings.randomWaitStopDelay());
                return this.fireworkCurrentDelay == 0;
            }
            case 3: {
                boolean timeout;
                this.fireworkMovement.block();
                boolean stopped = this.fireworkMovement.isPlayerStopped(settings.getVelocityThreshold());
                boolean bl = timeout = elapsed >= (long)this.fireworkCurrentDelay;
                if (!stopped && !timeout) break;
                this.startFireworkPhase(FireworkPhase.PRE_SWAP, settings.randomPreSwapDelay());
                return this.fireworkCurrentDelay == 0;
            }
            case 4: {
                this.fireworkMovement.block();
                if (elapsed < (long)this.fireworkCurrentDelay) break;
                this.startFireworkPhase(FireworkPhase.SWAP_TO_HAND, 0);
                return true;
            }
            case 5: {
                int hotbarSlot = ElytraHelper.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.click(this.fireworkSlot, hotbarSlot, ClickType.SWAP);
                this.startFireworkPhase(FireworkPhase.AWAIT_ITEM, 0);
                return true;
            }
            case 6: {
                if (ElytraHelper.mc.player.getMainHandItem().getItem() != Items.FIREWORK_ROCKET) break;
                this.startFireworkPhase(FireworkPhase.USE, 0);
                return true;
            }
            case 7: {
                this.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, ElytraHelper.mc.player.getYRot(), ElytraHelper.mc.player.getXRot()));
                ElytraHelper.mc.player.swing(InteractionHand.MAIN_HAND);
                this.startFireworkPhase(FireworkPhase.POST_USE, settings.randomPostSwapDelay());
                return this.fireworkCurrentDelay == 0;
            }
            case 8: {
                if (elapsed < (long)this.fireworkCurrentDelay) break;
                if (this.fireworkFromInventory) {
                    this.startFireworkPhase(FireworkPhase.SWAP_BACK, 0);
                    return true;
                }
                InventoryUtils.selectSlot(this.savedFireworkSlot);
                this.startFireworkPhase(FireworkPhase.RESUMING, settings.randomResumeDelay());
                return this.fireworkCurrentDelay == 0;
            }
            case 9: {
                int hotbarSlot = ElytraHelper.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.click(this.fireworkSlot, hotbarSlot, ClickType.SWAP);
                InventoryUtils.selectSlot(this.savedFireworkSlot);
                if (settings.shouldCloseInventory()) {
                    InventoryUtils.closeScreen();
                }
                this.startFireworkPhase(FireworkPhase.RESUMING, settings.randomResumeDelay());
                return this.fireworkCurrentDelay == 0;
            }
            case 10: {
                if (elapsed < (long)this.fireworkCurrentDelay) break;
                if (this.buildSettings().shouldStopMovement()) {
                    this.fireworkMovement.restoreFromCurrent();
                }
                this.resetFireworkState();
                return false;
            }
        }
        return false;
    }

    private int findItemInHotbar(Item item) {
        for (int i = 0; i < 9; ++i) {
            if (ElytraHelper.mc.player.getInventory().getItem(i).getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private void sendSequencedPacket(IntFunction<Packet<?>> packetCreator) {
        if (ElytraHelper.mc.player == null || mc.getConnection() == null || ElytraHelper.mc.level == null) {
            return;
        }
        try {
            ClientWorldAccessor worldAccessor = (ClientWorldAccessor)((Object)ElytraHelper.mc.level);
            BlockStatePredictionHandler pendingUpdateManager = worldAccessor.getPendingUpdateManager().startPredicting();
            int sequence = pendingUpdateManager.currentSequence();
            mc.getConnection().send(packetCreator.apply(sequence));
            pendingUpdateManager.close();
        }
        catch (Exception e) {
            mc.getConnection().send(packetCreator.apply(0));
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private SwapSettings buildSettings() {
        String mode;
        return switch (mode = this.modeSetting.getSelected()) {
            case "Instant" -> SwapSettings.instant();
            default -> SwapSettings.legit();
        };
    }

    private void startSwapPhase(SwapPhase phase, int delay) {
        this.swapPhase = phase;
        this.swapPhaseStartTime = System.currentTimeMillis();
        this.swapCurrentDelay = delay;
    }

    private void startFireworkPhase(FireworkPhase phase, int delay) {
        this.fireworkPhase = phase;
        this.fireworkPhaseStartTime = System.currentTimeMillis();
        this.fireworkCurrentDelay = delay;
    }

    private void resetSwapState() {
        this.swapMovement.reset();
        this.swapPhase = SwapPhase.IDLE;
        this.armorSlot = -1;
        this.swapPhaseStartTime = 0L;
        this.swapCurrentDelay = 0;
    }

    private void resetFireworkState() {
        this.fireworkMovement.reset();
        this.fireworkPhase = FireworkPhase.IDLE;
        this.fireworkSlot = -1;
        this.savedFireworkSlot = -1;
        this.fireworkFromInventory = false;
        this.fireworkPhaseStartTime = 0L;
        this.fireworkCurrentDelay = 0;
    }

    private void resetAllStates() {
        this.resetSwapState();
        this.resetFireworkState();
        this.shouldJumpForTakeoff = false;
    }

    public boolean isSwapping() {
        return this.swapPhase != SwapPhase.IDLE;
    }

    public boolean isUsingFirework() {
        return this.fireworkPhase != FireworkPhase.IDLE;
    }

    @Override
    public void deactivate() {
        if (this.swapMovement.isBlocked()) {
            this.swapMovement.restoreFromCurrent();
        }
        if (this.fireworkMovement.isBlocked()) {
            this.fireworkMovement.restoreFromCurrent();
        }
        this.resetAllStates();
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
    public BindSetting getFireworkBind() {
        return this.fireworkBind;
    }

    @Generated
    public BooleanSetting getAutoTakeoff() {
        return this.autoTakeoff;
    }

    @Generated
    public BooleanSetting getAutoFirework() {
        return this.autoFirework;
    }

    @Generated
    public MovementController getSwapMovement() {
        return this.swapMovement;
    }

    @Generated
    public MovementController getFireworkMovement() {
        return this.fireworkMovement;
    }

    @Generated
    public StopWatch getFireworkCooldown() {
        return this.fireworkCooldown;
    }

    @Generated
    public StopWatch getAutoFireworkTimer() {
        return this.autoFireworkTimer;
    }

    @Generated
    public SwapPhase getSwapPhase() {
        return this.swapPhase;
    }

    @Generated
    public FireworkPhase getFireworkPhase() {
        return this.fireworkPhase;
    }

    @Generated
    public int getArmorSlot() {
        return this.armorSlot;
    }

    @Generated
    public int getFireworkSlot() {
        return this.fireworkSlot;
    }

    @Generated
    public int getSavedFireworkSlot() {
        return this.savedFireworkSlot;
    }

    @Generated
    public boolean isFireworkFromInventory() {
        return this.fireworkFromInventory;
    }

    @Generated
    public long getSwapPhaseStartTime() {
        return this.swapPhaseStartTime;
    }

    @Generated
    public long getFireworkPhaseStartTime() {
        return this.fireworkPhaseStartTime;
    }

    @Generated
    public int getSwapCurrentDelay() {
        return this.swapCurrentDelay;
    }

    @Generated
    public int getFireworkCurrentDelay() {
        return this.fireworkCurrentDelay;
    }

    @Generated
    public boolean isShouldJumpForTakeoff() {
        return this.shouldJumpForTakeoff;
    }

    static enum SwapPhase {
        IDLE,
        PRE_STOP,
        STOPPING,
        WAIT_STOP,
        PRE_SWAP,
        SWAP_ARMOR,
        POST_SWAP,
        RESUMING;

    }

    static enum FireworkPhase {
        IDLE,
        PRE_STOP,
        STOPPING,
        WAIT_STOP,
        PRE_SWAP,
        SWAP_TO_HAND,
        AWAIT_ITEM,
        USE,
        POST_USE,
        SWAP_BACK,
        RESUMING;

    }
}

