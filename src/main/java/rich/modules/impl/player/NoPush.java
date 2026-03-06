
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import rich.events.api.EventHandler;
import rich.events.impl.PlayerCollisionEvent;
import rich.events.impl.PushEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.MultiSelectSetting;

public class NoPush
extends ModuleStructure {
    MultiSelectSetting ignoreSetting = new MultiSelectSetting("Ignore", "").value("Water", "Blocks", "Entity collision", "Powder snow", "Berries");

    public NoPush() {
        super("AntiPush", "Anti Push", ModuleCategory.PLAYER);
        this.settings(this.ignoreSetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onPush(PushEvent e) {
        switch (e.getType()) {
            case COLLISION: {
                e.setCancelled(this.ignoreSetting.isSelected("Entity collision"));
                break;
            }
            case WATER: {
                e.setCancelled(this.ignoreSetting.isSelected("Water"));
                break;
            }
            case BLOCK: {
                e.setCancelled(this.ignoreSetting.isSelected("Blocks"));
            }
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onPlayerCollision(PlayerCollisionEvent e) {
        Block block = e.getBlock();
        if (block.equals(Blocks.POWDER_SNOW)) {
            e.setCancelled(this.ignoreSetting.isSelected("Powder snow"));
        } else if (block.equals(Blocks.SWEET_BERRY_BUSH)) {
            e.setCancelled(this.ignoreSetting.isSelected("Berries"));
        }
    }
}

