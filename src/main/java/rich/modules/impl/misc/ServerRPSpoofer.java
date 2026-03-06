
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.timer.TimerUtil;

public class ServerRPSpoofer
extends ModuleStructure {
    private ResourcePackAction currentAction = ResourcePackAction.WAIT;
    private final TimerUtil counter = TimerUtil.create();

    public ServerRPSpoofer() {
        super("ServerRPSpoof", "Server RP Spoof", ModuleCategory.MISC);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof ClientboundResourcePackPushPacket) {
            this.currentAction = ResourcePackAction.ACCEPT;
            e.cancel();
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        ClientPacketListener networkHandler = mc.getConnection();
        if (networkHandler != null) {
            this.processResourcePackAction(networkHandler);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processResourcePackAction(ClientPacketListener networkHandler) {
        if (this.currentAction == ResourcePackAction.ACCEPT) {
            networkHandler.send(new ServerboundResourcePackPacket(ServerRPSpoofer.mc.player.getUUID(), ServerboundResourcePackPacket.Action.ACCEPTED));
            this.currentAction = ResourcePackAction.SEND;
            this.counter.resetCounter();
        } else if (this.currentAction == ResourcePackAction.SEND && this.counter.isReached(300L)) {
            networkHandler.send(new ServerboundResourcePackPacket(ServerRPSpoofer.mc.player.getUUID(), ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
            this.currentAction = ResourcePackAction.WAIT;
        }
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        this.currentAction = ResourcePackAction.WAIT;
        super.deactivate();
    }

    public static enum ResourcePackAction {
        ACCEPT,
        SEND,
        WAIT;

    }
}

