
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.api.events.render.TextFactoryEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.repository.friend.FriendUtils;

public class NameProtect
extends ModuleStructure {
    private final TextSetting nameSetting = new TextSetting("Name", "Nickname that will replace yours").setText("Protected").setMax(32);
    private final BooleanSetting friendsSetting = new BooleanSetting("Friends", "Hides friend nicknames").setValue(true);

    public NameProtect() {
        super("NameProtect", "Name Protect", ModuleCategory.PLAYER);
        this.settings(this.friendsSetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTextFactory(TextFactoryEvent e) {
        e.replaceText(mc.getUser().getName(), this.nameSetting.getText());
        if (this.friendsSetting.isValue()) {
            this.replaceFriendNames(e);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void replaceFriendNames(TextFactoryEvent e) {
        FriendUtils.getFriends().forEach(friend -> e.replaceText(friend.getName(), this.nameSetting.getText()));
    }
}

