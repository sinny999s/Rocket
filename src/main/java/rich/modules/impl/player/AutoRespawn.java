
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.impl.DeathScreenEvent;
import rich.events.impl.PacketEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;

public class AutoRespawn
extends ModuleStructure {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Select what will be used").value("Default");

    public AutoRespawn() {
        super("AutoRespawn", "Auto Respawn", ModuleCategory.PLAYER);
        this.settings(this.modeSetting);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onDeathScreen(DeathScreenEvent e) {
        if (this.modeSetting.isSelected("Default")) {
            AutoRespawn.mc.player.respawn();
            mc.setScreen(null);
        }
    }
}

