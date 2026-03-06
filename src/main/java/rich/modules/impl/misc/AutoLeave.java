
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.network.Network;
import rich.util.repository.friend.FriendUtils;

public class AutoLeave
extends ModuleStructure {
    private final SelectSetting leaveType = new SelectSetting("Disconnect type", "Allows selecting disconnect type").value("Hub", "Main Menu").selected("Main Menu");
    private final MultiSelectSetting triggerSetting = new MultiSelectSetting("Triggers", "Select when to disconnect").value("Players", "Staff").selected("Players", "Staff");
    private final SliderSettings distanceSetting = new SliderSettings("Maximum distance", "Maximum distance for auto-leave activation").setValue(10.0f).range(5, 40).visible(() -> this.triggerSetting.isSelected("Players"));

    public AutoLeave() {
        super("AutoLeave", "Auto Leave", ModuleCategory.MISC);
        this.settings(this.leaveType, this.triggerSetting, this.distanceSetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (Network.isPvp()) {
            return;
        }
        if (this.triggerSetting.isSelected("Players")) {
            AutoLeave.mc.level.players().stream().filter(p -> AutoLeave.mc.player.distanceTo((Entity)p) < this.distanceSetting.getValue() && AutoLeave.mc.player != p && !FriendUtils.isFriend(p)).findFirst().ifPresent(p -> this.leave(p.getName().copy().append(" - Appeared nearby " + AutoLeave.mc.player.distanceTo((Entity)p) + "m")));
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    public void leave(Component text) {
        switch (this.leaveType.getSelected()) {
            case "Hub": {
                mc.getConnection().sendCommand("hub");
                break;
            }
            case "Main Menu": {
                mc.getConnection().getConnection().disconnect(Component.nullToEmpty((String)"[Auto Leave] \n").copy().append(text));
            }
        }
        this.setState(false);
    }
}

