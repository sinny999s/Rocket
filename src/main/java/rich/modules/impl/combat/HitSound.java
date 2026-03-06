
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.world.entity.LivingEntity;
import rich.events.api.EventHandler;
import rich.events.impl.AttackEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.sounds.SoundManager;

public class HitSound
extends ModuleStructure {
    private final SelectSetting soundType = new SelectSetting("Sound type", "Select sound type").value("Moan", "Metallic", "Crime").selected("Moan");
    private final SliderSettings volume = new SliderSettings("Volume", "Set volume").range(0.1f, 2.0f).setValue(1.0f);

    public static HitSound getInstance() {
        return Instance.get(HitSound.class);
    }

    public HitSound() {
        super("HitSound", ModuleCategory.COMBAT);
        this.settings(this.soundType, this.volume);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onAttack(AttackEvent event) {
        if (HitSound.mc.player == null || HitSound.mc.level == null) {
            return;
        }
        if (!(event.getTarget() instanceof LivingEntity)) {
            return;
        }
        this.playSelectedSound();
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void playSelectedSound() {
        float vol = this.volume.getValue();
        if (this.soundType.isSelected("Moan")) {
            this.playRandomMoan(vol, 1.0f);
        }
        if (this.soundType.isSelected("Metallic")) {
            SoundManager.playSound(SoundManager.METALLIC, vol, 1.0f);
        }
        if (this.soundType.isSelected("Crime")) {
            SoundManager.playSound(SoundManager.CRIME, vol, 1.0f);
        }
    }

    private void playRandomMoan(float volume, float pitch) {
        int random = ThreadLocalRandom.current().nextInt(4);
        switch (random) {
            case 0: {
                SoundManager.playSound(SoundManager.MOAN1, volume, pitch);
                break;
            }
            case 1: {
                SoundManager.playSound(SoundManager.MOAN2, volume, pitch);
                break;
            }
            case 2: {
                SoundManager.playSound(SoundManager.MOAN3, volume, pitch);
                break;
            }
            case 3: {
                SoundManager.playSound(SoundManager.MOAN4, volume, pitch);
            }
        }
    }
}

