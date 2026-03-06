
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import lombok.Generated;
import net.minecraft.world.InteractionHand;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.timer.StopWatch;

public class TapeMouse
extends ModuleStructure {
    private final SelectSetting modeClick = new SelectSetting("Type", "Click type").value("Left button", "Right button").selected("Left button");
    private final SliderSettings delayForClick = new SliderSettings("Delay", "Delay between clicks").range(1.0f, 15.0f).setValue(1.0f);
    private final StopWatch delay = new StopWatch();

    public TapeMouse() {
        super("TapeMouse", "Tape Mouse", ModuleCategory.COMBAT);
        this.settings(this.modeClick, this.delayForClick);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onTick(TickEvent e) {
        if (TapeMouse.mc.player == null || TapeMouse.mc.level == null) {
            return;
        }
        if (TapeMouse.mc.screen != null) {
            return;
        }
        long delayMs = (long)(this.delayForClick.getValue() * 300.0f);
        if (!this.delay.finished(delayMs)) {
            return;
        }
        this.performClick();
        this.delay.reset();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void performClick() {
        if (this.modeClick.isSelected("Left button")) {
            this.leftClick();
        } else {
            this.rightClick();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void leftClick() {
        if (TapeMouse.mc.gameMode == null) {
            return;
        }
        if (TapeMouse.mc.crosshairPickEntity != null) {
            TapeMouse.mc.gameMode.attack(TapeMouse.mc.player, TapeMouse.mc.crosshairPickEntity);
            TapeMouse.mc.player.swing(InteractionHand.MAIN_HAND);
        } else if (TapeMouse.mc.hitResult != null) {
            mc.startAttack();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void rightClick() {
        if (TapeMouse.mc.gameMode == null) {
            return;
        }
        mc.startUseItem();
    }

    @Generated
    public SelectSetting getModeClick() {
        return this.modeClick;
    }

    @Generated
    public SliderSettings getDelayForClick() {
        return this.delayForClick;
    }

    @Generated
    public StopWatch getDelay() {
        return this.delay;
    }
}

