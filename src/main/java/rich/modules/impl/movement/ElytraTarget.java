
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.impl.KeyEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.sounds.SoundManager;

public class ElytraTarget
extends ModuleStructure {
    public SliderSettings elytraFindRange = new SliderSettings("Distance aiming", "Target search range during elytra flight").setValue(32.0f).range(6.0f, 64.0f);
    public SliderSettings elytraForward = new SliderSettings("Drive value", "exhausted").setValue(3.0f).range(0.0f, 6.0f);
    final BindSetting forward = new BindSetting("Button toggle drive", "");
    public static boolean shouldElytraTarget = false;

    public static ElytraTarget getInstance() {
        return Instance.get(ElytraTarget.class);
    }

    public ElytraTarget() {
        super("ElytraTarget", "Elytra Target", ModuleCategory.MOVEMENT);
        this.settings(this.elytraFindRange, this.elytraForward, this.forward);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    private void onEventKey(KeyEvent e) {
        if (e.isKeyDown(this.forward.getKey())) {
            shouldElytraTarget = !shouldElytraTarget;
            SoundManager.playSound(shouldElytraTarget ? SoundManager.MODULE_ENABLE : SoundManager.MODULE_DISABLE, 1.0f, 1.0f);
        }
    }
}

