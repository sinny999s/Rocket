
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import java.util.Arrays;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.network.Network;
import rich.util.repository.friend.FriendUtils;

public class AutoTpAccept
extends ModuleStructure {
    private final String[] teleportMessages = new String[]{"has requested teleport", "requests to teleport", "wants to teleport to you", "requests to teleport to you"};
    private boolean canAccept;
    private final BooleanSetting friendSetting = new BooleanSetting("Only friends", "Will only accept requests from friends").setValue(true);

    public AutoTpAccept() {
        super("AutoTpAccept", "Auto Tp Accept", ModuleCategory.MISC);
        this.settings(this.friendSetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onPacket(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        if (packet instanceof ClientboundSystemChatPacket) {
            boolean validPlayer;
            ClientboundSystemChatPacket m = (ClientboundSystemChatPacket)packet;
            String message = m.content().getString();
            boolean bl = validPlayer = !this.friendSetting.isValue() || FriendUtils.getFriends().stream().anyMatch(s -> message.contains(s.getName()));
            if (this.isTeleportMessage(message)) {
                this.canAccept = validPlayer;
            }
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onTick(TickEvent e) {
        if (!Network.isPvp() && this.canAccept) {
            AutoTpAccept.mc.player.connection.sendCommand("tpaccept");
            this.canAccept = false;
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isTeleportMessage(String message) {
        return Arrays.stream(this.teleportMessages).map(String::toLowerCase).anyMatch(message::contains);
    }
}

