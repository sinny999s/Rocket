
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import lombok.Generated;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.timer.StopWatch;

public class Fly
extends ModuleStructure {
    private final SelectSetting mode = new SelectSetting("Mode", "Select flight mode").value("Normal", "Dragon Fly").selected("Normal");
    private final SliderSettings speedXZ = new SliderSettings("Speed XZ", "Horizontal speed").setValue(1.5f).range(1.0f, 10.0f).visible(() -> !this.mode.isSelected("FunTime Up"));
    private final SliderSettings speedY = new SliderSettings("Speed Y", "Vertical speed").setValue(1.5f).range(0.0f, 10.0f).visible(() -> !this.mode.isSelected("FunTime Up"));
    private StopWatch timer = new StopWatch();

    public static Fly getInstance() {
        return Instance.get(Fly.class);
    }

    public Fly() {
        super("Fly", ModuleCategory.MOVEMENT);
        this.settings(this.mode, this.speedXZ, this.speedY);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (!this.state || Fly.mc.player == null || Fly.mc.level == null) {
            return;
        }
        if (this.mode.isSelected("Normal")) {
            this.handleNormalMode();
        } else if (this.mode.isSelected("Dragon Fly")) {
            this.handleDragonFlyMode();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleNormalMode() {
        double motionY = this.getMotionY();
        this.setMotion(this.speedXZ.getValue());
        Vec3 v = Fly.mc.player.getDeltaMovement();
        Fly.mc.player.setDeltaMovement(v.x, motionY, v.z);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleDragonFlyMode() {
        if (Fly.mc.player.getAbilities().flying) {
            this.setMotion(this.speedXZ.getValue());
            double motionY = 0.0;
            if (Fly.mc.options.keyJump.isDown()) {
                motionY = this.speedY.getValue();
            }
            if (Fly.mc.options.keyShift.isDown()) {
                motionY = -this.speedY.getValue();
            }
            Vec3 v = Fly.mc.player.getDeltaMovement();
            Fly.mc.player.setDeltaMovement(v.x, motionY, v.z);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private double getMotionY() {
        if (Fly.mc.options.keyShift.isDown()) {
            return -this.speedY.getValue();
        }
        if (Fly.mc.options.keyJump.isDown()) {
            return this.speedY.getValue();
        }
        return 0.0;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void setMotion(float speed) {
        float yaw = Fly.mc.player.getYRot();
        float f = Fly.mc.player.zza;
        float s = Fly.mc.player.xxa;
        float speedScale = speed / 3.0f;
        double x = 0.0;
        double z = 0.0;
        if (f != 0.0f || s != 0.0f) {
            float yawRad = yaw * ((float)Math.PI / 180);
            x = -Mth.sin((double)yawRad) * speedScale * f + Mth.cos((double)yawRad) * speedScale * s;
            z = Mth.cos((double)yawRad) * speedScale * f + Mth.sin((double)yawRad) * speedScale * s;
        }
        Fly.mc.player.setDeltaMovement(x, Fly.mc.player.getDeltaMovement().y, z);
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        super.deactivate();
        this.timer.reset();
    }

    @Generated
    public SelectSetting getMode() {
        return this.mode;
    }

    @Generated
    public SliderSettings getSpeedXZ() {
        return this.speedXZ;
    }

    @Generated
    public SliderSettings getSpeedY() {
        return this.speedY;
    }

    @Generated
    public StopWatch getTimer() {
        return this.timer;
    }
}

