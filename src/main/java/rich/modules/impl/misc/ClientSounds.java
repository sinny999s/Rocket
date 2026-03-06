
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import rich.events.api.EventHandler;
import rich.events.impl.ModuleToggleEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.sounds.SoundManager;

public class ClientSounds
extends ModuleStructure {
    private final SelectSetting soundType = new SelectSetting("Sound type", "Select sound type").value("New", "Old").selected("New");
    private final SliderSettings volume = new SliderSettings("Volume", "Set volume").range(0.1f, 2.0f).setValue(1.0f);

    public static ClientSounds getInstance() {
        return Instance.get(ClientSounds.class);
    }

    public ClientSounds() {
        super("ClientSounds", ModuleCategory.MISC);
        this.settings(this.soundType, this.volume);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onModuleToggle(ModuleToggleEvent event) {
        if (ClientSounds.mc.player == null || ClientSounds.mc.level == null) {
            return;
        }
        if (event.getModule() == this) {
            return;
        }
        this.playToggleSound(event.isEnabled());
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void playToggleSound(boolean enabled) {
        float vol = this.volume.getValue();
        if (enabled) {
            if (this.soundType.isSelected("New")) {
                SoundManager.playSound(SoundManager.MODULE_ENABLE, vol, 1.0f);
            } else {
                SoundManager.playSound(SoundManager.ON, vol, 1.0f);
            }
        } else if (this.soundType.isSelected("New")) {
            SoundManager.playSound(SoundManager.MODULE_DISABLE, vol, 1.0f);
        } else {
            SoundManager.playSound(SoundManager.OFF, vol, 1.0f);
        }
    }
}

