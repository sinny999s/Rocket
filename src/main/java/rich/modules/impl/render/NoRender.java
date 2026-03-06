
package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.util.Instance;

public class NoRender
extends ModuleStructure {
    public final MultiSelectSetting modeSetting = new MultiSelectSetting("Elements", "Select elements to ignore").value("Fire", "Bad Effects", "Block Overlay", "Darkness", "Damage", "Nausea", "Scoreboard", "BossBar").selected("Fire", "Bad Effects", "Block Overlay", "Darkness", "Damage", "Nausea");

    public static NoRender getInstance() {
        return Instance.get(NoRender.class);
    }

    public NoRender() {
        super("NoRender", "No Render", ModuleCategory.RENDER);
        this.settings(this.modeSetting);
    }
}

