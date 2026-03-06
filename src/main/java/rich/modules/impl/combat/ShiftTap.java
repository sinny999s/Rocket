
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import net.minecraft.client.Minecraft;
import rich.events.api.EventHandler;
import rich.events.impl.AttackEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;

public class ShiftTap
extends ModuleStructure {
    private long shiftTapEndTime = 0L;
    private boolean isModuleControllingSneak = false;
    private int shiftTapDuration = 100;
    private final Minecraft mc = Minecraft.getInstance();

    public ShiftTap() {
        super("ShiftTap", "Shift Tap", ModuleCategory.COMBAT);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void startShiftTap() {
        this.shiftTapEndTime = System.currentTimeMillis() + 25L;
        if (!this.isModuleControllingSneak) {
            this.mc.options.keyShift.setDown(true);
            this.isModuleControllingSneak = true;
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void stopShiftTap() {
        if (this.isModuleControllingSneak) {
            this.mc.options.keyShift.setDown(false);
            this.isModuleControllingSneak = false;
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onAttack(AttackEvent event) {
        if (this.mc.player == null) {
            return;
        }
        this.startShiftTap();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (this.mc.player == null || this.mc.player.isSpectator()) {
            this.stopShiftTap();
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (this.isModuleControllingSneak && currentTime > this.shiftTapEndTime) {
            this.stopShiftTap();
        }
    }
}

