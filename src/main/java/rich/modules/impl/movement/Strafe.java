
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import net.minecraft.client.Minecraft;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.Aura;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.math.TaskPriority;
import rich.util.move.MoveUtil;

public class Strafe
extends ModuleStructure {
    private static final Minecraft mc = Minecraft.getInstance();
    public SelectSetting mode = new SelectSetting("Mode", "Select strafe type").value("Matrix", "Grim").selected("Matrix");
    SliderSettings speed = new SliderSettings("Speed", "Select strafe speed").setValue(0.42f).range(0.0f, 1.0f).visible(() -> this.mode.isSelected("Matrix"));
    private float lastYaw;
    private float lastPitch;
    private final Angle rot = new Angle(0.0f, 0.0f);

    public Strafe() {
        super("Strafe", "Strafe", ModuleCategory.MOVEMENT);
        this.settings(this.mode, this.speed);
    }

    public static Strafe getInstance() {
        return Instance.get(Strafe.class);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent event) {
        if (Strafe.mc.player == null || Strafe.mc.level == null) {
            return;
        }
        boolean moving = MoveUtil.hasPlayerMovement();
        float yaw = Strafe.mc.player.getYRot();
        if (this.mode.isSelected("Matrix")) {
            this.handleMatrixMode(moving, yaw);
        } else if (this.mode.isSelected("Grim")) {
            this.handleGrimMode(moving, yaw);
        }
        this.lastYaw = yaw;
        this.lastPitch = 0.0f;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleMatrixMode(boolean moving, float yaw) {
        if (moving) {
            yaw = MoveUtil.moveYaw(Strafe.mc.player.getYRot());
            double motion = this.speed.getValue() * 1.5f;
            MoveUtil.setVelocity(motion);
        } else {
            MoveUtil.setVelocity(0.0);
        }
        Strafe.mc.player.setDeltaMovement(Strafe.mc.player.getDeltaMovement().x, Strafe.mc.player.getDeltaMovement().y, Strafe.mc.player.getDeltaMovement().z);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleGrimMode(boolean moving, float yaw) {
        if (moving) {
            AngleConfig.freeCorrection = true;
            yaw = MoveUtil.moveYaw(Strafe.mc.player.getYRot());
            this.rot.setYaw(yaw);
            this.rot.setPitch(Strafe.mc.player.getXRot());
            Aura.getInstance();
            if (Aura.target == null) {
                AngleConnection.INSTANCE.rotateTo(this.rot, AngleConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_1, this);
            }
        }
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void activate() {
        super.activate();
        this.lastYaw = Strafe.mc.player != null ? Strafe.mc.player.getYRot() : 0.0f;
        this.lastPitch = Strafe.mc.player != null ? Strafe.mc.player.getXRot() : 0.0f;
    }
}

