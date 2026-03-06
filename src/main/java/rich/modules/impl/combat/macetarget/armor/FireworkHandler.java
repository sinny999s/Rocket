
package rich.modules.impl.combat.macetarget.armor;

import java.util.function.IntFunction;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;
import rich.mixin.ClientWorldAccessor;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.macetarget.state.MaceState;
import rich.util.inventory.InventoryUtils;
import rich.util.inventory.MovementController;
import rich.util.inventory.SwapSettings;

public class FireworkHandler {
    private static final Minecraft mc = Minecraft.getInstance();
    private final MovementController movement = new MovementController();
    private MaceState.FireworkPhase phase = MaceState.FireworkPhase.IDLE;
    private int slot = -1;
    private int savedSlot = -1;
    private boolean fromInventory = false;
    private long phaseStartTime = 0L;
    private int currentDelay = 0;
    private final SwapSettingsProvider settingsProvider;

    public FireworkHandler(SwapSettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    public boolean isActive() {
        return this.phase != MaceState.FireworkPhase.IDLE;
    }

    public void useFirework(boolean isSilent) {
        if (isSilent) {
            this.useSilent();
        } else {
            this.startLegit();
        }
    }

    private void useSilent() {
        if (FireworkHandler.mc.player == null) {
            return;
        }
        int hotbarSlot = InventoryUtils.findHotbarItem(Items.FIREWORK_ROCKET);
        if (hotbarSlot != -1) {
            int currentSlot = FireworkHandler.mc.player.getInventory().getSelectedSlot();
            if (hotbarSlot != currentSlot) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(hotbarSlot));
            }
            Angle rotation = this.getRotation();
            this.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, rotation.getYaw(), rotation.getPitch()));
            if (hotbarSlot != currentSlot) {
                mc.getConnection().send(new ServerboundSetCarriedItemPacket(currentSlot));
            }
            return;
        }
        int invSlot = InventoryUtils.findItemInInventory(Items.FIREWORK_ROCKET);
        if (invSlot != -1) {
            int currentHotbarSlot = FireworkHandler.mc.player.getInventory().getSelectedSlot();
            int wrappedSlot = InventoryUtils.wrapSlot(invSlot);
            InventoryUtils.click(wrappedSlot, currentHotbarSlot, ClickType.SWAP);
            Angle rotation = this.getRotation();
            this.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, rotation.getYaw(), rotation.getPitch()));
            InventoryUtils.click(wrappedSlot, currentHotbarSlot, ClickType.SWAP);
            InventoryUtils.closeScreen();
        }
    }

    private void startLegit() {
        if (FireworkHandler.mc.player == null) {
            return;
        }
        this.savedSlot = FireworkHandler.mc.player.getInventory().getSelectedSlot();
        int hotbarSlot = InventoryUtils.findHotbarItem(Items.FIREWORK_ROCKET);
        if (hotbarSlot != -1) {
            this.slot = hotbarSlot;
            this.fromInventory = false;
            InventoryUtils.selectSlot(this.slot);
            this.startPhase(MaceState.FireworkPhase.AWAIT_ITEM, 0);
            return;
        }
        int invSlot = InventoryUtils.findItemInInventory(Items.FIREWORK_ROCKET);
        if (invSlot != -1) {
            this.slot = invSlot;
            this.fromInventory = true;
            SwapSettings settings = this.settingsProvider.get();
            if (settings.shouldStopMovement()) {
                this.startPhase(MaceState.FireworkPhase.PRE_STOP, settings.randomPreStopDelay());
            } else {
                this.startPhase(MaceState.FireworkPhase.SWAP_TO_HAND, 0);
            }
        }
    }

    public void processLoop() {
        if (this.phase == MaceState.FireworkPhase.IDLE) {
            return;
        }
        boolean continueProcessing = true;
        for (int iterations = 0; continueProcessing && iterations < 10; ++iterations) {
            continueProcessing = this.processTick();
        }
    }

    private boolean processTick() {
        if (FireworkHandler.mc.player == null) {
            this.reset();
            return false;
        }
        long elapsed = System.currentTimeMillis() - this.phaseStartTime;
        SwapSettings settings = this.settingsProvider.get();
        switch (this.phase) {
            case PRE_STOP: {
                if (elapsed < (long)this.currentDelay) break;
                this.movement.saveState();
                this.movement.block();
                if (settings.shouldStopSprint()) {
                    this.movement.stopSprint();
                }
                this.startPhase(MaceState.FireworkPhase.STOPPING, 0);
                return true;
            }
            case STOPPING: {
                this.movement.block();
                if (settings.shouldStopSprint()) {
                    this.movement.stopSprint();
                }
                this.startPhase(MaceState.FireworkPhase.WAIT_STOP, settings.randomWaitStopDelay());
                return this.currentDelay == 0;
            }
            case WAIT_STOP: {
                boolean timeout;
                this.movement.block();
                boolean stopped = this.movement.isPlayerStopped(settings.getVelocityThreshold());
                boolean bl = timeout = elapsed >= (long)this.currentDelay;
                if (!stopped && !timeout) break;
                this.startPhase(MaceState.FireworkPhase.PRE_SWAP, settings.randomPreSwapDelay());
                return this.currentDelay == 0;
            }
            case PRE_SWAP: {
                this.movement.block();
                if (elapsed < (long)this.currentDelay) break;
                this.startPhase(MaceState.FireworkPhase.SWAP_TO_HAND, 0);
                return true;
            }
            case SWAP_TO_HAND: {
                int hotbarSlot = FireworkHandler.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.click(this.slot, hotbarSlot, ClickType.SWAP);
                this.startPhase(MaceState.FireworkPhase.AWAIT_ITEM, 0);
                return true;
            }
            case AWAIT_ITEM: {
                if (FireworkHandler.mc.player.getMainHandItem().getItem() != Items.FIREWORK_ROCKET) break;
                this.startPhase(MaceState.FireworkPhase.USE, 0);
                return true;
            }
            case USE: {
                Angle rotation = this.getRotation();
                this.sendSequencedPacket(sequence -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, sequence, rotation.getYaw(), rotation.getPitch()));
                FireworkHandler.mc.player.swing(InteractionHand.MAIN_HAND);
                this.startPhase(MaceState.FireworkPhase.POST_USE, settings.randomPostSwapDelay());
                return this.currentDelay == 0;
            }
            case POST_USE: {
                if (elapsed < (long)this.currentDelay) break;
                if (this.fromInventory) {
                    this.startPhase(MaceState.FireworkPhase.SWAP_BACK, 0);
                    return true;
                }
                InventoryUtils.selectSlot(this.savedSlot);
                this.startPhase(MaceState.FireworkPhase.RESUMING, settings.randomResumeDelay());
                return this.currentDelay == 0;
            }
            case SWAP_BACK: {
                int hotbarSlot = FireworkHandler.mc.player.getInventory().getSelectedSlot();
                InventoryUtils.click(this.slot, hotbarSlot, ClickType.SWAP);
                InventoryUtils.selectSlot(this.savedSlot);
                if (settings.shouldCloseInventory()) {
                    InventoryUtils.closeScreen();
                }
                this.startPhase(MaceState.FireworkPhase.RESUMING, settings.randomResumeDelay());
                return this.currentDelay == 0;
            }
            case RESUMING: {
                if (elapsed < (long)this.currentDelay) break;
                this.movement.restoreFromCurrent();
                this.reset();
                return false;
            }
        }
        return false;
    }

    private Angle getRotation() {
        Angle rotation = AngleConnection.INSTANCE.getRotation();
        return rotation != null ? rotation : MathAngle.cameraAngle();
    }

    private void sendSequencedPacket(IntFunction<Packet<?>> packetCreator) {
        if (FireworkHandler.mc.player == null || mc.getConnection() == null || FireworkHandler.mc.level == null) {
            return;
        }
        try {
            ClientWorldAccessor worldAccessor = (ClientWorldAccessor)((Object)FireworkHandler.mc.level);
            BlockStatePredictionHandler pendingUpdateManager = worldAccessor.getPendingUpdateManager().startPredicting();
            int sequence = pendingUpdateManager.currentSequence();
            mc.getConnection().send(packetCreator.apply(sequence));
            pendingUpdateManager.close();
        }
        catch (Exception e) {
            mc.getConnection().send(packetCreator.apply(0));
        }
    }

    private void startPhase(MaceState.FireworkPhase phase, int delay) {
        this.phase = phase;
        this.phaseStartTime = System.currentTimeMillis();
        this.currentDelay = delay;
    }

    public void reset() {
        this.movement.reset();
        this.phase = MaceState.FireworkPhase.IDLE;
        this.slot = -1;
        this.savedSlot = -1;
        this.fromInventory = false;
        this.phaseStartTime = 0L;
        this.currentDelay = 0;
    }

    public void forceRestore() {
        if (this.movement.isBlocked()) {
            this.movement.restoreFromCurrent();
        }
    }

    @Generated
    public MovementController getMovement() {
        return this.movement;
    }

    @Generated
    public MaceState.FireworkPhase getPhase() {
        return this.phase;
    }

    @Generated
    public int getSlot() {
        return this.slot;
    }

    @Generated
    public int getSavedSlot() {
        return this.savedSlot;
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
    public SwapSettingsProvider getSettingsProvider() {
        return this.settingsProvider;
    }

    @Generated
    public void setPhase(MaceState.FireworkPhase phase) {
        this.phase = phase;
    }

    @FunctionalInterface
    public static interface SwapSettingsProvider {
        public SwapSettings get();
    }
}

