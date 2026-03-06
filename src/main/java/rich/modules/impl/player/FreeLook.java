
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import net.minecraft.client.CameraType;
import net.minecraft.util.Mth;
import rich.events.api.EventHandler;
import rich.events.impl.CameraEvent;
import rich.events.impl.FovEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.MouseRotationEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.util.string.PlayerInteractionHelper;

public class FreeLook
extends ModuleStructure {
    private CameraType perspective;
    private Angle angle;
    public static BindSetting freeLookSetting = new BindSetting("Free look", "Free look key");

    public FreeLook() {
        super("FreeLook", "Free Look", ModuleCategory.RENDER);
        this.settings(freeLookSetting);
        this.angle = null;
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(freeLookSetting.getKey())) {
            this.perspective = FreeLook.mc.options.getCameraType();
            if (this.angle == null) {
                this.angle = MathAngle.cameraAngle();
            }
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onFov(FovEvent e) {
        if (PlayerInteractionHelper.isKey(freeLookSetting)) {
            this.handleFreeLookActivation();
        } else if (this.perspective != null) {
            this.handleFreeLookDeactivation();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void handleFreeLookActivation() {
        if (FreeLook.mc.options.getCameraType().isFirstPerson()) {
            FreeLook.mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }
        if (this.angle == null) {
            this.angle = MathAngle.cameraAngle();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void handleFreeLookDeactivation() {
        FreeLook.mc.options.setCameraType(this.perspective);
        this.perspective = null;
        this.angle = null;
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onMouseRotation(MouseRotationEvent e) {
        if (PlayerInteractionHelper.isKey(freeLookSetting)) {
            if (this.angle == null) {
                this.angle = MathAngle.cameraAngle();
            }
            this.angle.setYaw(this.angle.getYaw() + e.getCursorDeltaX() * 0.15f);
            this.angle.setPitch(Mth.clamp((float)(this.angle.getPitch() + e.getCursorDeltaY() * 0.15f), (float)-90.0f, (float)90.0f));
            e.cancel();
        } else {
            this.angle = null;
        }
    }

    @EventHandler
    public void onCamera(CameraEvent e) {
        if (PlayerInteractionHelper.isKey(freeLookSetting) && this.angle != null) {
            e.setAngle(this.angle);
            e.cancel();
        }
    }
}

