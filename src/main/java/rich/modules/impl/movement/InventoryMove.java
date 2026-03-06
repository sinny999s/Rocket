
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.SignEditScreen;
import net.minecraft.client.gui.screens.inventory.StructureBlockEditScreen;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.inventory.ClickType;
import rich.events.api.EventHandler;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.CloseScreenEvent;
import rich.events.impl.InputEvent;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.inventory.MovementController;
import rich.util.move.MoveUtil;
import rich.util.string.PlayerInteractionHelper;

public class InventoryMove
extends ModuleStructure {
    private final List<Packet<?>> packets = new ArrayList();
    private final SelectSetting mode = new SelectSetting("Mode", "Select inventory movement mode").value("Normal", "Legit").selected("Legit");
    private final BooleanSetting grimBypass = new BooleanSetting("Grim Bypass", "Stop movement on inventory close").setValue(true);
    private final MovementController movement = new MovementController();
    private MovePhase movePhase = MovePhase.READY;
    private long actionStartTime = 0L;
    private int currentDelay = 0;
    private boolean wasForwardPressed;
    private boolean wasBackPressed;
    private boolean wasLeftPressed;
    private boolean wasRightPressed;
    private boolean wasJumpPressed;
    private boolean keysOverridden = false;
    private boolean inventoryOpened = false;
    private boolean packetsHeld = false;
    private boolean pendingClose = false;
    private int closeScreenSyncId = -1;

    public InventoryMove() {
        super("InventoryMove", "Inventory Move", ModuleCategory.MOVEMENT);
        this.settings(this.mode, this.grimBypass);
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        this.resetState();
    }

    @EventHandler
    public void onInput(InputEvent e) {
        if (InventoryMove.mc.player == null) {
            return;
        }
        if (this.movement.isBlocked()) {
            e.setDirectionalLow(false, false, false, false);
            e.setJumping(false);
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onPacket(PacketEvent e) {
        if (this.mode.isSelected("Legit")) {
            this.handleLegitPackets(e);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleLegitPackets(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        if (packet instanceof ServerboundContainerClickPacket) {
            ServerboundContainerClickPacket slot = (ServerboundContainerClickPacket)packet;
            if ((this.packetsHeld || MoveUtil.hasPlayerMovement()) && this.shouldSkipExecution()) {
                this.packets.add(slot);
                e.cancel();
                this.packetsHeld = true;
            }
        } else {
            ClientboundContainerClosePacket screen;
            packet = e.getPacket();
            if (packet instanceof ClientboundContainerClosePacket && (screen = (ClientboundContainerClosePacket)packet).getContainerId() == 0) {
                e.cancel();
            }
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (InventoryMove.mc.player == null) {
            return;
        }
        if (this.mode.isSelected("Legit")) {
            this.processLegitMovement();
        } else if (!this.isServerScreen() && this.shouldSkipExecution()) {
            this.updateMoveKeys();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processLegitMovement() {
        boolean hasOpenScreen;
        boolean bl = hasOpenScreen = InventoryMove.mc.screen != null;
        if (hasOpenScreen && !this.inventoryOpened && this.movePhase == MovePhase.READY) {
            this.startLegitMovement();
            this.inventoryOpened = true;
        }
        if (this.pendingClose) {
            this.handlePendingClose();
            return;
        }
        if (!hasOpenScreen && this.inventoryOpened && this.movePhase != MovePhase.READY) {
            this.inventoryOpened = false;
            if (this.movePhase == MovePhase.ALLOW_MOVEMENT) {
                this.resetState();
            }
            return;
        }
        if (this.movePhase != MovePhase.READY) {
            this.handleMovementStates();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handlePendingClose() {
        long elapsed = System.currentTimeMillis() - this.actionStartTime;
        switch (this.movePhase.ordinal()) {
            case 2: {
                this.movement.block();
                this.movement.stopSprint();
                this.movePhase = MovePhase.WAIT_STOP;
                this.actionStartTime = System.currentTimeMillis();
                break;
            }
            case 3: {
                this.movement.block();
                boolean stopped = this.isPlayerStopped();
                if (!stopped && elapsed < 100L) break;
                this.movePhase = this.packetsHeld ? MovePhase.SEND_PACKETS : MovePhase.CLOSE_INVENTORY;
                this.actionStartTime = System.currentTimeMillis();
                break;
            }
            case 4: {
                this.blockMovementInput();
                if (elapsed <= 1L) break;
                this.movePhase = MovePhase.SEND_PACKETS;
                this.actionStartTime = System.currentTimeMillis();
                break;
            }
            case 5: {
                this.sendHeldPackets();
                this.movePhase = MovePhase.CLOSE_INVENTORY;
                this.actionStartTime = System.currentTimeMillis();
                break;
            }
            case 8: {
                this.closeInventoryNow();
                this.movePhase = MovePhase.RESUMING;
                this.currentDelay = 20 + (int)(Math.random() * 30.0);
                this.actionStartTime = System.currentTimeMillis();
                break;
            }
            case 7: {
                if (elapsed < (long)this.currentDelay) break;
                if (this.movement.isBlocked()) {
                    this.movement.restoreFromCurrent();
                }
                if (this.keysOverridden) {
                    this.restoreKeyStates();
                }
                this.movePhase = MovePhase.FINISHED;
                break;
            }
            case 9: {
                this.resetState();
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isPlayerStopped() {
        if (InventoryMove.mc.player == null) {
            return true;
        }
        double vx = Math.abs(InventoryMove.mc.player.getDeltaMovement().x);
        double vz = Math.abs(InventoryMove.mc.player.getDeltaMovement().z);
        return vx < 0.03 && vz < 0.03;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void blockMovementInput() {
        if (InventoryMove.mc.player != null && InventoryMove.mc.player.input != null) {
            InventoryMove.mc.player.input.keyPresses = new Input(false, false, false, false, InventoryMove.mc.player.input.keyPresses.jump(), InventoryMove.mc.player.input.keyPresses.shift(), InventoryMove.mc.player.input.keyPresses.sprint());
        }
        if (!this.keysOverridden) {
            InventoryMove.mc.options.keyUp.setDown(false);
            InventoryMove.mc.options.keyDown.setDown(false);
            InventoryMove.mc.options.keyLeft.setDown(false);
            InventoryMove.mc.options.keyRight.setDown(false);
            InventoryMove.mc.options.keyJump.setDown(false);
            this.keysOverridden = true;
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void sendHeldPackets() {
        if (!this.packets.isEmpty()) {
            this.packets.forEach(PlayerInteractionHelper::sendPacketWithOutEvent);
            this.packets.clear();
            this.updateSlots();
        }
        this.packetsHeld = false;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void closeInventoryNow() {
        if (InventoryMove.mc.player == null || mc.getConnection() == null) {
            return;
        }
        if (this.closeScreenSyncId != -1) {
            mc.getConnection().send(new ServerboundContainerClosePacket(this.closeScreenSyncId));
        }
        if (InventoryMove.mc.screen != null) {
            InventoryMove.mc.screen.onClose();
        }
        this.pendingClose = false;
        this.inventoryOpened = false;
        this.closeScreenSyncId = -1;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void startLegitMovement() {
        this.wasForwardPressed = this.isKeyPressed(InventoryMove.mc.options.keyUp.getDefaultKey().getValue());
        this.wasBackPressed = this.isKeyPressed(InventoryMove.mc.options.keyDown.getDefaultKey().getValue());
        this.wasLeftPressed = this.isKeyPressed(InventoryMove.mc.options.keyLeft.getDefaultKey().getValue());
        this.wasRightPressed = this.isKeyPressed(InventoryMove.mc.options.keyRight.getDefaultKey().getValue());
        this.wasJumpPressed = this.isKeyPressed(InventoryMove.mc.options.keyJump.getDefaultKey().getValue());
        this.movePhase = MovePhase.ALLOW_MOVEMENT;
        this.keysOverridden = false;
        this.packetsHeld = false;
        this.pendingClose = false;
        this.closeScreenSyncId = -1;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleMovementStates() {
        long elapsed = System.currentTimeMillis() - this.actionStartTime;
        switch (this.movePhase.ordinal()) {
            case 1: {
                if (this.isServerScreen() || !this.shouldSkipExecution()) break;
                this.updateMoveKeys();
                break;
            }
            case 6: {
                if (this.keysOverridden) {
                    this.restoreKeyStates();
                }
                if (InventoryMove.mc.player != null && elapsed > 1L && this.isKeyPressed(InventoryMove.mc.options.keyUp.getDefaultKey().getValue()) && !InventoryMove.mc.player.isSprinting()) {
                    InventoryMove.mc.player.setSprinting(true);
                }
                if (elapsed <= 1L) break;
                this.movePhase = MovePhase.FINISHED;
                break;
            }
            case 7: {
                if (elapsed < (long)this.currentDelay) break;
                this.movement.restoreFromCurrent();
                this.movePhase = MovePhase.FINISHED;
                break;
            }
            case 9: {
                this.resetState();
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void restoreKeyStates() {
        InventoryMove.mc.options.keyUp.setDown(this.wasForwardPressed && this.isKeyPressed(InventoryMove.mc.options.keyUp.getDefaultKey().getValue()));
        InventoryMove.mc.options.keyDown.setDown(this.wasBackPressed && this.isKeyPressed(InventoryMove.mc.options.keyDown.getDefaultKey().getValue()));
        InventoryMove.mc.options.keyLeft.setDown(this.wasLeftPressed && this.isKeyPressed(InventoryMove.mc.options.keyLeft.getDefaultKey().getValue()));
        InventoryMove.mc.options.keyRight.setDown(this.wasRightPressed && this.isKeyPressed(InventoryMove.mc.options.keyRight.getDefaultKey().getValue()));
        InventoryMove.mc.options.keyJump.setDown(this.wasJumpPressed && this.isKeyPressed(InventoryMove.mc.options.keyJump.getDefaultKey().getValue()));
        this.keysOverridden = false;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void resetState() {
        if (this.movement.isBlocked()) {
            this.movement.restoreFromCurrent();
        }
        if (this.keysOverridden) {
            this.restoreKeyStates();
        }
        this.movePhase = MovePhase.READY;
        this.inventoryOpened = false;
        this.packetsHeld = false;
        this.pendingClose = false;
        this.closeScreenSyncId = -1;
        this.packets.clear();
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onClickSlot(ClickSlotEvent e) {
        if (this.mode.isSelected("Legit")) {
            ClickType actionType = e.getActionType();
            if ((this.packetsHeld || MoveUtil.hasPlayerMovement()) && (e.getButton() == 1 && !actionType.equals((Object)ClickType.SWAP) && !actionType.equals((Object)ClickType.THROW) || actionType.equals((Object)ClickType.PICKUP_ALL))) {
                e.cancel();
            }
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onCloseScreen(CloseScreenEvent e) {
        if (!this.mode.isSelected("Legit")) {
            return;
        }
        if (this.movePhase != MovePhase.ALLOW_MOVEMENT) {
            return;
        }
        if (!this.shouldSkipExecution()) {
            return;
        }
        boolean needsStop = this.grimBypass.isValue() && MoveUtil.hasPlayerMovement();
        boolean hasPackets = this.packetsHeld;
        if (needsStop || hasPackets) {
            e.cancel();
            this.pendingClose = true;
            this.closeScreenSyncId = InventoryMove.mc.player != null ? InventoryMove.mc.player.containerMenu.containerId : 0;
            this.movePhase = needsStop ? MovePhase.STOPPING : MovePhase.SLOWING_DOWN;
            this.actionStartTime = System.currentTimeMillis();
        }
    }

    private boolean isKeyPressed(int keyCode) {
        return InputConstants.isKeyDown((Window)mc.getWindow(), (int)keyCode);
    }

    private void updateMoveKeys() {
        InventoryMove.mc.options.keyUp.setDown(this.isKeyPressed(InventoryMove.mc.options.keyUp.getDefaultKey().getValue()));
        InventoryMove.mc.options.keyDown.setDown(this.isKeyPressed(InventoryMove.mc.options.keyDown.getDefaultKey().getValue()));
        InventoryMove.mc.options.keyLeft.setDown(this.isKeyPressed(InventoryMove.mc.options.keyLeft.getDefaultKey().getValue()));
        InventoryMove.mc.options.keyRight.setDown(this.isKeyPressed(InventoryMove.mc.options.keyRight.getDefaultKey().getValue()));
        InventoryMove.mc.options.keyJump.setDown(this.isKeyPressed(InventoryMove.mc.options.keyJump.getDefaultKey().getValue()));
    }

    private boolean shouldSkipExecution() {
        return InventoryMove.mc.screen != null && !(InventoryMove.mc.screen instanceof ChatScreen) && !(InventoryMove.mc.screen instanceof SignEditScreen) && !(InventoryMove.mc.screen instanceof AnvilScreen) && !(InventoryMove.mc.screen instanceof AbstractCommandBlockEditScreen) && !(InventoryMove.mc.screen instanceof StructureBlockEditScreen);
    }

    private boolean isServerScreen() {
        return InventoryMove.mc.player != null && InventoryMove.mc.player.containerMenu.slots.size() != 46;
    }

    private void updateSlots() {
        if (InventoryMove.mc.player == null || InventoryMove.mc.gameMode == null) {
            return;
        }
        InventoryMove.mc.gameMode.handleInventoryMouseClick(InventoryMove.mc.player.containerMenu.containerId, 0, 0, ClickType.PICKUP_ALL, InventoryMove.mc.player);
    }

    @Generated
    public List<Packet<?>> getPackets() {
        return this.packets;
    }

    @Generated
    public SelectSetting getMode() {
        return this.mode;
    }

    @Generated
    public BooleanSetting getGrimBypass() {
        return this.grimBypass;
    }

    @Generated
    public MovementController getMovement() {
        return this.movement;
    }

    @Generated
    public MovePhase getMovePhase() {
        return this.movePhase;
    }

    @Generated
    public long getActionStartTime() {
        return this.actionStartTime;
    }

    @Generated
    public int getCurrentDelay() {
        return this.currentDelay;
    }

    @Generated
    public boolean isWasForwardPressed() {
        return this.wasForwardPressed;
    }

    @Generated
    public boolean isWasBackPressed() {
        return this.wasBackPressed;
    }

    @Generated
    public boolean isWasLeftPressed() {
        return this.wasLeftPressed;
    }

    @Generated
    public boolean isWasRightPressed() {
        return this.wasRightPressed;
    }

    @Generated
    public boolean isWasJumpPressed() {
        return this.wasJumpPressed;
    }

    @Generated
    public boolean isKeysOverridden() {
        return this.keysOverridden;
    }

    @Generated
    public boolean isInventoryOpened() {
        return this.inventoryOpened;
    }

    @Generated
    public boolean isPacketsHeld() {
        return this.packetsHeld;
    }

    @Generated
    public boolean isPendingClose() {
        return this.pendingClose;
    }

    @Generated
    public int getCloseScreenSyncId() {
        return this.closeScreenSyncId;
    }

    static enum MovePhase {
        READY,
        ALLOW_MOVEMENT,
        STOPPING,
        WAIT_STOP,
        SLOWING_DOWN,
        SEND_PACKETS,
        SPEEDING_UP,
        RESUMING,
        CLOSE_INVENTORY,
        FINISHED;

    }
}

