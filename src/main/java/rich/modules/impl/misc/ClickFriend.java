
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import rich.events.api.EventHandler;
import rich.events.impl.KeyEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.util.repository.friend.FriendUtils;

public class ClickFriend
extends ModuleStructure {
    private final BindSetting friendBind = new BindSetting("Add friend", "Add/remove friend");

    public ClickFriend() {
        super("ClickFriend", "Click Friend", ModuleCategory.MISC);
        this.settings(this.friendBind);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onKey(KeyEvent e) {
        EntityHitResult result;
        Object object;
        if (e.isKeyDown(this.friendBind.getKey()) && (object = ClickFriend.mc.hitResult) instanceof EntityHitResult && (object = (result = (EntityHitResult)object).getEntity()) instanceof Player) {
            Player player = (Player)object;
            if (FriendUtils.isFriend(player)) {
                FriendUtils.removeFriend(player);
            } else {
                FriendUtils.addFriend(player);
            }
        }
    }
}

