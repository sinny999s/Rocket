
package rich.util.modules;

import java.util.List;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import com.mojang.blaze3d.platform.InputConstants;
import rich.events.impl.KeyEvent;
import rich.modules.module.ModuleStructure;

public class ModuleSwitcher
implements IMinecraft {
    private final List<ModuleStructure> moduleStructures;

    public ModuleSwitcher(List<ModuleStructure> moduleStructures, EventManager eventManager) {
        this.moduleStructures = moduleStructures;
        EventManager.register(this);
    }

    @EventHandler
    public void onKey(KeyEvent event) {
        for (ModuleStructure moduleStructure : this.moduleStructures) {
            if (event.key() != moduleStructure.getKey() || ModuleSwitcher.mc.screen != null) continue;
            boolean isMouseEvent = event.type() == InputConstants.Type.MOUSE;
            boolean isMouseBind = moduleStructure.getType() == 0;
            if (isMouseEvent != isMouseBind) continue;
            try {
                this.handleModuleState(moduleStructure, event.action());
            }
            catch (Exception exception) {}
        }
    }

    private void handleModuleState(ModuleStructure moduleStructure, int action) {
        if (action == 1) {
            moduleStructure.switchState();
        }
    }
}

