package rich.modules.impl.combat;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.effect.MobEffects;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.Instance;

public class Criticals extends ModuleStructure {

    private final SelectSetting mode = new SelectSetting("Mode", "Criticals mode")
            .value("Packet", "Grim", "GrimNew");

    public Criticals() {
        super("Criticals", "Forces critical hits on attacks", ModuleCategory.COMBAT);
        this.settings(this.mode);
    }

    public static Criticals getInstance() {
        return Instance.get(Criticals.class);
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!this.state || event.getType() != PacketEvent.Type.SEND) return;
        if (Criticals.mc.player == null || Criticals.mc.level == null) return;

        Packet<?> packet = event.getPacket();
        if (!(packet instanceof ServerboundInteractPacket)) return;

        if (Criticals.mc.player.isPassenger() || Criticals.mc.player.isFallFlying()
                || Criticals.mc.player.isInWater() || Criticals.mc.player.isInLava()
                || Criticals.mc.player.onClimbable()
                || Criticals.mc.player.hasEffect(MobEffects.BLINDNESS)) {
            return;
        }

        if (!Criticals.mc.player.onGround()) return;

        double x = Criticals.mc.player.getX();
        double y = Criticals.mc.player.getY();
        double z = Criticals.mc.player.getZ();

        switch (this.mode.getSelected()) {
            case "Packet" -> {
                Criticals.mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, false));
                Criticals.mc.player.connection.send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
            }
            case "Grim" -> {
                float yaw = Criticals.mc.player.getYRot();
                float pitch = Criticals.mc.player.getXRot();
                Criticals.mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(x, y + 0.0625, z, yaw, pitch, false, false));
                Criticals.mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(x, y + 0.0625013579, z, yaw, pitch, false, false));
                Criticals.mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(x, y + 1.3579e-6, z, yaw, pitch, false, false));
            }
            case "GrimNew" -> {
                float yaw = Criticals.mc.player.getYRot();
                float pitch = Criticals.mc.player.getXRot();
                Criticals.mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(x, y + 0.0626, z, yaw, pitch, false, false));
                Criticals.mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(x, y + 0.0455, z, yaw, pitch, false, false));
            }
        }
    }
}
