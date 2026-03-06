
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.MultiSelectSetting;

public class NoDelay
extends ModuleStructure {
    public MultiSelectSetting ignoreSetting = new MultiSelectSetting("Type", "").value("Jump", "Right click", "Delay breaking").selected("Jump");

    public NoDelay() {
        super("NoDelay", "No Delay", ModuleCategory.PLAYER);
        this.settings(this.ignoreSetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (NoDelay.mc.player == null) {
            return;
        }
        if (this.ignoreSetting.isSelected("Delay breaking")) {
            NoDelay.mc.gameMode.destroyDelay = 0;
        }
        if (this.ignoreSetting.isSelected("Jump")) {
            NoDelay.mc.player.noJumpDelay = 0;
        }
        if (this.ignoreSetting.isSelected("Right click")) {
            NoDelay.mc.rightClickDelay = 0;
        }
    }
}

