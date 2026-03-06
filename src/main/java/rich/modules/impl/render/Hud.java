
package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.util.Instance;

public class Hud
extends ModuleStructure {
    public final MultiSelectSetting interfaceSettings = new MultiSelectSetting("Elements", "UI element settings").value("Watermark", "HotKeys", "Potions", "Staff", "test", "TargetHud", "Info", "Notifications", "CustomHud").selected("Watermark", "HotKeys", "Potions", "Staff", "TargetHud", "Info", "Notifications", "CustomHud");
    public final BooleanSetting showBps = new BooleanSetting("Show BPS", "Show blocks per second").setValue(true).visible(() -> this.interfaceSettings.isSelected("Info"));
    public final BooleanSetting showTps = new BooleanSetting("Show TPS", "Show TPS in Watermark").setValue(true).visible(() -> this.interfaceSettings.isSelected("Watermark"));

    public static Hud getInstance() {
        return Instance.get(Hud.class);
    }

    public Hud() {
        super("Hud", ModuleCategory.RENDER);
        this.settings(this.interfaceSettings, this.showBps, this.showTps);
    }
}

