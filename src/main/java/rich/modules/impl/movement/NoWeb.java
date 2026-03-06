
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import net.minecraft.world.level.block.Blocks;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.Instance;
import rich.util.move.MoveUtil;
import rich.util.string.PlayerInteractionHelper;

public class NoWeb
extends ModuleStructure {
    public final SelectSetting webMode = new SelectSetting("Mode", "Select bypass mode").value("Grim");

    public static NoWeb getInstance() {
        return Instance.get(NoWeb.class);
    }

    public NoWeb() {
        super("NoWeb", "No Web", ModuleCategory.MOVEMENT);
        this.settings(this.webMode);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (PlayerInteractionHelper.isPlayerInBlock(Blocks.COBWEB)) {
            double[] speed = MoveUtil.calculateDirection(0.35);
            NoWeb.mc.player.push(speed[0], 0.0, speed[1]);
            NoWeb.mc.player.setDeltaMovement(speed[0], NoWeb.mc.options.keyJump.isDown() ? (double)0.65f : (NoWeb.mc.options.keyShift.isDown() ? (double)-0.65f : 0.0), speed[1]);
        }
    }
}

