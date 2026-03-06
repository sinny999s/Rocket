
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
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.TickEvent;
import rich.mixin.ClientWorldAccessor;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.inventory.InventoryUtils;
import rich.util.inventory.MovementController;
import rich.util.inventory.SwapSettings;
import rich.util.math.TaskPriority;
import rich.util.string.chat.ChatMessage;

public class WindJump
extends ModuleStructure {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Throw method").value("Instant", "Legit").selected("Legit");
    private final BindSetting keySetting = new BindSetting("Button", "Button for wind charge");
    private final MovementController movement = new MovementController();
    private final float THROW_PITCH = 90.0f;
    private Phase phase = Phase.IDLE;
    private int savedSlot = -1;
    private int chargeSlot = -1;
    private boolean fromInventory = false;
    private long phaseStartTime = 0L;
    private int currentDelay = 0;
    private long lastThrowTime = 0L;
    private int rotationTicks = 0;
    private boolean pendingThrow = false;

    public WindJump() {
        super("WindJump", "Wind Jump", ModuleCategory.MISC);
        this.settings(this.modeSetting, this.keySetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onKey(KeyEvent e) {
        if (WindJump.mc.player == null || WindJump.mc.level == null) {
            return;
        }
        if (WindJump.mc.screen != null) {
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
            this.tryThrowCharge();
        }
    }

    @EventHandler
    public void onScroll(HotBarScrollEvent e) {
        if (WindJump.mc.player == null || WindJump.mc.level == null) {
            return;
        }
        if (WindJump.mc.screen != null) {
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
            this.tryThrowCharge();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void tryThrowCharge() {
        if (System.currentTimeMillis() - this.lastThrowTime < 100L) {
            return;
        }
        this.lastThrowTime = System.currentTimeMillis();
        this.startChargeProcess();
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (WindJump.mc.player == null || WindJump.mc.level == null) {
            return;
        }
        if (event.getType() == 0 && (this.phase == Phase.ROTATE_DOWN || this.phase == Phase.THROW)) {
            Angle throwAngle = new Angle(WindJump.mc.player.getYRot(), 90.0f);
            AngleConfig config = new AngleConfig(new LinearConstructor(), true, true);
            AngleConnection.INSTANCE.rotateTo(throwAngle, 3, config, TaskPriority.HIGH_IMPORTANCE_1, this);
        }
        if (event.getType() == 2 && this.pendingThrow) {
            this.performThrow();
            this.pendingThrow = false;
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void performThrow() {
        if (WindJump.mc.player == null) {
            return;
        }
        Angle rotation = AngleConnection.INSTANCE.getRotation();
        if (rotation == null) {
            rotation = MathAngle.cameraAngle();
        }
        Angle finalRotation = rotation;
        this.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, finalRotation.getYaw(), finalRotation.getPitch()));
        WindJump.mc.player.swing(InteractionHand.MAIN_HAND);
        SwapSettings settings = this.buildSettings();
        this.startPhase(Phase.POST_THROW, settings.randomPostSwapDelay());
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (WindJump.mc.player == null || WindJump.mc.level == null) {
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
        if (WindJump.mc.player == null) {
            return;
        }
        if (this.movement.isBlocked()) {
            e.setDirectionalLow(false, false, false, false);
            e.setJumping(false);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void startChargeProcess() {
        this.savedSlot = WindJump.mc.player.getInventory().getSelectedSlot();
        int hotbarSlot = InventoryUtils.findItemInHotbar(Items.WIND_CHARGE);
        if (hotbarSlot != -1) {
            this.chargeSlot = hotbarSlot;
            this.fromInventory = false;
            InventoryUtils.selectSlot(this.chargeSlot);
            this.startPhase(Phase.AWAIT_ITEM, 0);
            return;
        }
        int invSlot = InventoryUtils.findItemInInventory(Items.WIND_CHARGE);
        if (invSlot != -1) {
            this.chargeSlot = invSlot;
            this.fromInventory = true;
            SwapSettings settings = this.buildSettings();
            if (settings.shouldStopMovement()) {
                this.startPhase(Phase.PRE_STOP, settings.randomPreStopDelay());
            } else {
                this.startPhase(Phase.SWAP_TO_HAND, 0);
            }
        } else {
            ChatMessage.brandmessage("Wind charge count");
            this.resetState();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private boolean processTick() {
        if (WindJump.mc.player == null || WindJump.mc.screen != null) {
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
                int hotbarSlot = WindJump.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.click(this.chargeSlot, hotbarSlot, ClickType.SWAP);
                this.startPhase(Phase.AWAIT_ITEM, 0);
                return true;
            }
            case 6: {
                if (WindJump.mc.player.getMainHandItem().getItem() != Items.WIND_CHARGE) break;
                this.rotationTicks = 0;
                this.startPhase(Phase.ROTATE_DOWN, 0);
                return true;
            }
            case 7: {
                boolean waitedEnough;
                ++this.rotationTicks;
                Angle currentRotation = AngleConnection.INSTANCE.getRotation();
                boolean rotationReady = currentRotation != null && currentRotation.getPitch() >= 80.0f;
                boolean bl = waitedEnough = this.rotationTicks >= 2;
                if (rotationReady && waitedEnough) {
                    this.startPhase(Phase.THROW, 0);
                    return true;
                }
                if (this.rotationTicks <= 10) break;
                this.resetState();
                return false;
            }
            case 8: {
                this.pendingThrow = true;
                return false;
            }
            case 9: {
                if (elapsed < (long)this.currentDelay) break;
                if (this.fromInventory) {
                    this.startPhase(Phase.SWAP_BACK, 0);
                    return true;
                }
                InventoryUtils.selectSlot(this.savedSlot);
                this.startPhase(Phase.RESUMING, settings.randomResumeDelay());
                return this.currentDelay == 0;
            }
            case 10: {
                int hotbarSlot = WindJump.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.click(this.chargeSlot, hotbarSlot, ClickType.SWAP);
                InventoryUtils.selectSlot(this.savedSlot);
                if (settings.shouldCloseInventory()) {
                    InventoryUtils.closeScreen();
                }
                this.startPhase(Phase.RESUMING, settings.randomResumeDelay());
                return this.currentDelay == 0;
            }
            case 11: {
                if (elapsed < (long)this.currentDelay) break;
                if (this.buildSettings().shouldStopMovement()) {
                    this.movement.restoreFromCurrent();
                }
                AngleConnection.INSTANCE.startReturning();
                this.resetState();
                return false;
            }
        }
        return false;
    }

    private void sendSequencedPacket(IntFunction<Packet<?>> packetCreator) {
        if (WindJump.mc.player == null || mc.getConnection() == null || WindJump.mc.level == null) {
            return;
        }
        try {
            ClientWorldAccessor worldAccessor = (ClientWorldAccessor)((Object)WindJump.mc.level);
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
        this.chargeSlot = -1;
        this.fromInventory = false;
        this.phaseStartTime = 0L;
        this.currentDelay = 0;
        this.rotationTicks = 0;
        this.pendingThrow = false;
    }

    public boolean isRunning() {
        return this.phase != Phase.IDLE;
    }

    @Override
    public void deactivate() {
        if (this.movement.isBlocked()) {
            this.movement.restoreFromCurrent();
        }
        AngleConnection.INSTANCE.startReturning();
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
    public float getTHROW_PITCH() {
        return this.THROW_PITCH;
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
    public int getChargeSlot() {
        return this.chargeSlot;
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

    @Generated
    public int getRotationTicks() {
        return this.rotationTicks;
    }

    @Generated
    public boolean isPendingThrow() {
        return this.pendingThrow;
    }

    static enum Phase {
        IDLE,
        PRE_STOP,
        STOPPING,
        WAIT_STOP,
        PRE_SWAP,
        SWAP_TO_HAND,
        AWAIT_ITEM,
        ROTATE_DOWN,
        THROW,
        POST_THROW,
        SWAP_BACK,
        RESUMING;

    }
}

