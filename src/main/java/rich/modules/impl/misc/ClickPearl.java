
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.IntFunction;
import lombok.Generated;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import rich.events.api.EventHandler;
import rich.events.impl.HotBarScrollEvent;
import rich.events.impl.InputEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.TickEvent;
import rich.mixin.ClientWorldAccessor;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.inventory.InventoryUtils;
import rich.util.inventory.MovementController;
import rich.util.inventory.SwapSettings;
import rich.util.string.chat.ChatMessage;

public class ClickPearl
extends ModuleStructure {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Throw method").value("Instant", "Legit").selected("Legit");
    private final BindSetting keySetting = new BindSetting("Button", "Button for throw");
    private final MovementController movement = new MovementController();
    private Phase phase = Phase.IDLE;
    private int savedSlot = -1;
    private int pearlSlot = -1;
    private boolean fromInventory = false;
    private long phaseStartTime = 0L;
    private int currentDelay = 0;
    private long lastThrowTime = 0L;

    public ClickPearl() {
        super("ClickPearl", "Click Pearl", ModuleCategory.MISC);
        this.settings(this.modeSetting, this.keySetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onKey(KeyEvent e) {
        if (ClickPearl.mc.player == null || ClickPearl.mc.level == null) {
            return;
        }
        if (ClickPearl.mc.screen != null) {
            return;
        }
        if (this.phase != Phase.IDLE) {
            return;
        }
        if (e.action() != 1) {
            return;
        }
        int bindKey = this.keySetting.getKey();
        int bindType = this.keySetting.getType();
        if (bindKey == -1 || bindKey == -1) {
            return;
        }
        boolean matches = false;
        if (bindType == 2) {
            if (bindKey == 1002 && e.type() == InputConstants.Type.MOUSE && e.key() == 2) {
                matches = true;
            }
        } else if (bindType == 0 && e.type() == InputConstants.Type.MOUSE) {
            matches = e.key() == bindKey;
        } else if (bindType == 1 && e.type() == InputConstants.Type.KEYSYM) {
            boolean bl = matches = e.key() == bindKey;
        }
        if (matches) {
            this.tryThrowPearl();
        }
    }

    @EventHandler
    public void onScroll(HotBarScrollEvent e) {
        if (ClickPearl.mc.player == null || ClickPearl.mc.level == null) {
            return;
        }
        if (ClickPearl.mc.screen != null) {
            return;
        }
        if (this.phase != Phase.IDLE) {
            return;
        }
        int bindKey = this.keySetting.getKey();
        int bindType = this.keySetting.getType();
        if (bindType != 2) {
            return;
        }
        boolean matches = false;
        if (bindKey == 1000 && e.getVertical() > 0.0) {
            matches = true;
        } else if (bindKey == 1001 && e.getVertical() < 0.0) {
            matches = true;
        }
        if (matches) {
            e.setCancelled(true);
            this.tryThrowPearl();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void tryThrowPearl() {
        if (System.currentTimeMillis() - this.lastThrowTime < 100L) {
            return;
        }
        this.lastThrowTime = System.currentTimeMillis();
        this.startPearlProcess();
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (ClickPearl.mc.player == null || ClickPearl.mc.level == null) {
            this.resetState();
            return;
        }
        if (this.phase != Phase.IDLE) {
            boolean continueProcessing = true;
            for (int iterations = 0; continueProcessing && iterations < 10; ++iterations) {
                continueProcessing = this.processTick();
            }
        }
    }

    @EventHandler
    public void onInput(InputEvent e) {
        if (ClickPearl.mc.player == null) {
            return;
        }
        if (this.movement.isBlocked()) {
            e.setDirectionalLow(false, false, false, false);
            e.setJumping(false);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void startPearlProcess() {
        this.savedSlot = ClickPearl.mc.player.getInventory().getSelectedSlot();
        int hotbarSlot = InventoryUtils.findItemInHotbar(Items.ENDER_PEARL);
        if (hotbarSlot != -1) {
            this.pearlSlot = hotbarSlot;
            this.fromInventory = false;
            InventoryUtils.selectSlot(this.pearlSlot);
            this.startPhase(Phase.AWAIT_ITEM, 0);
            return;
        }
        int invSlot = InventoryUtils.findItemInInventory(Items.ENDER_PEARL);
        if (invSlot != -1) {
            this.pearlSlot = invSlot;
            this.fromInventory = true;
            SwapSettings settings = this.buildSettings();
            if (settings.shouldStopMovement()) {
                this.startPhase(Phase.PRE_STOP, settings.randomPreStopDelay());
            } else {
                this.startPhase(Phase.SWAP_TO_HAND, 0);
            }
        } else {
            ChatMessage.brandmessage("Pearl count");
            this.resetState();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private boolean processTick() {
        if (ClickPearl.mc.player == null || ClickPearl.mc.screen != null) {
            this.resetState();
            return false;
        }
        long elapsed = System.currentTimeMillis() - this.phaseStartTime;
        SwapSettings settings = this.buildSettings();
        switch (this.phase.ordinal()) {
            case 1: {
                if (elapsed < (long)this.currentDelay) break;
                this.movement.saveState();
                this.movement.block();
                if (settings.shouldStopSprint()) {
                    this.movement.stopSprint();
                }
                this.startPhase(Phase.STOPPING, 0);
                return true;
            }
            case 2: {
                this.movement.block();
                if (settings.shouldStopSprint()) {
                    this.movement.stopSprint();
                }
                this.startPhase(Phase.WAIT_STOP, settings.randomWaitStopDelay());
                return this.currentDelay == 0;
            }
            case 3: {
                boolean timeout;
                this.movement.block();
                boolean stopped = this.movement.isPlayerStopped(settings.getVelocityThreshold());
                boolean bl = timeout = elapsed >= (long)this.currentDelay;
                if (!stopped && !timeout) break;
                this.startPhase(Phase.PRE_SWAP, settings.randomPreSwapDelay());
                return this.currentDelay == 0;
            }
            case 4: {
                this.movement.block();
                if (elapsed < (long)this.currentDelay) break;
                this.startPhase(Phase.SWAP_TO_HAND, 0);
                return true;
            }
            case 5: {
                int hotbarSlot = ClickPearl.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.click(this.pearlSlot, hotbarSlot, ClickType.SWAP);
                this.startPhase(Phase.AWAIT_ITEM, 0);
                return true;
            }
            case 6: {
                if (ClickPearl.mc.player.getMainHandItem().getItem() != Items.ENDER_PEARL) break;
                this.startPhase(Phase.THROW, 0);
                return true;
            }
            case 7: {
                this.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, ClickPearl.mc.player.getYRot(), ClickPearl.mc.player.getXRot()));
                ClickPearl.mc.player.swing(InteractionHand.MAIN_HAND);
                this.startPhase(Phase.POST_THROW, settings.randomPostSwapDelay());
                return this.currentDelay == 0;
            }
            case 8: {
                if (elapsed < (long)this.currentDelay) break;
                if (this.fromInventory) {
                    this.startPhase(Phase.SWAP_BACK, 0);
                    return true;
                }
                InventoryUtils.selectSlot(this.savedSlot);
                this.startPhase(Phase.RESUMING, settings.randomResumeDelay());
                return this.currentDelay == 0;
            }
            case 9: {
                int hotbarSlot = ClickPearl.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.click(this.pearlSlot, hotbarSlot, ClickType.SWAP);
                InventoryUtils.selectSlot(this.savedSlot);
                if (settings.shouldCloseInventory()) {
                    InventoryUtils.closeScreen();
                }
                this.startPhase(Phase.RESUMING, settings.randomResumeDelay());
                return this.currentDelay == 0;
            }
            case 10: {
                if (elapsed < (long)this.currentDelay) break;
                if (this.buildSettings().shouldStopMovement()) {
                    this.movement.restoreFromCurrent();
                }
                this.resetState();
                return false;
            }
        }
        return false;
    }

    private void sendSequencedPacket(IntFunction<Packet<?>> packetCreator) {
        if (ClickPearl.mc.player == null || mc.getConnection() == null || ClickPearl.mc.level == null) {
            return;
        }
        try {
            ClientWorldAccessor worldAccessor = (ClientWorldAccessor)((Object)ClickPearl.mc.level);
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

    private void startPhase(Phase newPhase, int delay) {
        this.phase = newPhase;
        this.phaseStartTime = System.currentTimeMillis();
        this.currentDelay = delay;
    }

    private void resetState() {
        this.movement.reset();
        this.phase = Phase.IDLE;
        this.savedSlot = -1;
        this.pearlSlot = -1;
        this.fromInventory = false;
        this.phaseStartTime = 0L;
        this.currentDelay = 0;
    }

    public boolean isRunning() {
        return this.phase != Phase.IDLE;
    }

    @Override
    public void deactivate() {
        if (this.movement.isBlocked()) {
            this.movement.restoreFromCurrent();
        }
        this.resetState();
    }

    @Generated
    public SelectSetting getModeSetting() {
        return this.modeSetting;
    }

    @Generated
    public BindSetting getKeySetting() {
        return this.keySetting;
    }

    @Generated
    public MovementController getMovement() {
        return this.movement;
    }

    @Generated
    public Phase getPhase() {
        return this.phase;
    }

    @Generated
    public int getSavedSlot() {
        return this.savedSlot;
    }

    @Generated
    public int getPearlSlot() {
        return this.pearlSlot;
    }

    @Generated
    public boolean isFromInventory() {
        return this.fromInventory;
    }

    @Generated
    public long getPhaseStartTime() {
        return this.phaseStartTime;
    }

    @Generated
    public int getCurrentDelay() {
        return this.currentDelay;
    }

    @Generated
    public long getLastThrowTime() {
        return this.lastThrowTime;
    }

    static enum Phase {
        IDLE,
        PRE_STOP,
        STOPPING,
        WAIT_STOP,
        PRE_SWAP,
        SWAP_TO_HAND,
        AWAIT_ITEM,
        THROW,
        POST_THROW,
        SWAP_BACK,
        RESUMING;

    }
}

