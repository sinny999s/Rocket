
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.Instance;

public class AutoSprint
extends ModuleStructure {
    private static volatile boolean serverSprintState = false;
    private final BooleanSetting noReset = new BooleanSetting("Don't reset sprint", "Don't reset sprint for crits").setValue(false);

    public static AutoSprint getInstance() {
        return Instance.get(AutoSprint.class);
    }

    public AutoSprint() {
        super("AutoSprint", ModuleCategory.MOVEMENT);
        this.settings(this.noReset);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onPacket(PacketEvent event) {
        if (event.getType() != PacketEvent.Type.SEND) {
            return;
        }
        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ServerboundPlayerCommandPacket)) {
            return;
        }
        ServerboundPlayerCommandPacket packet2 = (ServerboundPlayerCommandPacket)packet;
        if (packet2.getAction() == ServerboundPlayerCommandPacket.Action.START_SPRINTING) {
            if (serverSprintState) {
                event.cancel();
                return;
            }
            serverSprintState = true;
        } else if (packet2.getAction() == ServerboundPlayerCommandPacket.Action.STOP_SPRINTING) {
            if (!serverSprintState) {
                event.cancel();
                return;
            }
            serverSprintState = false;
        }
    }

    public static boolean isServerSprinting() {
        return serverSprintState;
    }

    public static void resetServerState() {
        serverSprintState = false;
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (AutoSprint.mc.player == null) {
            return;
        }
        this.processSprint();
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void processSprint() {
        boolean canSprint;
        boolean horizontal = AutoSprint.mc.player.horizontalCollision && !AutoSprint.mc.player.minorHorizontalCollision;
        boolean sneaking = AutoSprint.mc.player.isShiftKeyDown() && !AutoSprint.mc.player.isSwimming();
        boolean bl = canSprint = !horizontal && AutoSprint.mc.player.zza > 0.0f;
        if (sneaking) {
            return;
        }
        if (canSprint && !AutoSprint.mc.player.isSprinting()) {
            AutoSprint.mc.player.setSprinting(true);
        }
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        AutoSprint.resetServerState();
    }

    @Generated
    public BooleanSetting getNoReset() {
        return this.noReset;
    }
}

