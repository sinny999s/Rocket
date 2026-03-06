
package rich.modules.impl.render;

import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.Instance;

public class ItemPhysic
extends ModuleStructure {
    public final SelectSetting mode = new SelectSetting("Physics", "").value("Normal").selected("Normal");

    public static ItemPhysic getInstance() {
        return Instance.get(ItemPhysic.class);
    }

    public ItemPhysic() {
        super("ItemPhysic", "Item Physic", ModuleCategory.RENDER);
    }

    @EventHandler
    public void onTick(TickEvent e) {
    }
}

