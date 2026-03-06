
package rich.modules.impl.player;

import net.minecraft.world.item.ItemStack;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.Instance;

public class NoEntityTrace
extends ModuleStructure {
    private final BooleanSetting noSword = new BooleanSetting("Disable with sword", "d").setValue(true);

    public NoEntityTrace() {
        super("NoEntityTrace", "No Entity Trace", ModuleCategory.PLAYER);
        this.settings(this.noSword);
    }

    public static NoEntityTrace getInstance() {
        return Instance.get(NoEntityTrace.class);
    }

    public boolean shouldIgnoreEntityTrace() {
        if (!this.isState() || NoEntityTrace.mc.player == null) {
            return false;
        }
        if (!this.noSword.isValue()) {
            return true;
        }
        ItemStack stack = NoEntityTrace.mc.player.getMainHandItem();
        String key = stack.getItem().getDescriptionId().toLowerCase();
        return !key.contains("sword");
    }
}

