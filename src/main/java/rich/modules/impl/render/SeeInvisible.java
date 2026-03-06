
package rich.modules.impl.render;

import rich.events.api.EventHandler;
import rich.events.impl.EntityColorEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.ColorUtil;

public class SeeInvisible
extends ModuleStructure {
    private final SliderSettings alphaSetting = new SliderSettings("Transparency", "Transparency player").setValue(0.5f).range(0.1f, 1.0f);

    public SeeInvisible() {
        super("SeeInvisible", "See Invisible", ModuleCategory.RENDER);
        this.settings(this.alphaSetting);
    }

    @EventHandler
    public void onEntityColor(EntityColorEvent e) {
        e.setColor(ColorUtil.multAlpha(e.getColor(), this.alphaSetting.getValue()));
        e.cancel();
    }
}

