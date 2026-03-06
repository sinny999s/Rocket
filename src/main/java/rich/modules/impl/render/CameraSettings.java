
package rich.modules.impl.render;

import net.minecraft.util.Mth;
import rich.events.api.EventHandler;
import rich.events.impl.CameraEvent;
import rich.events.impl.FovEvent;
import rich.events.impl.HotBarScrollEvent;
import rich.events.impl.KeyEvent;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.player.FreeLook;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.math.MathUtils;
import rich.util.string.PlayerInteractionHelper;

public class CameraSettings
extends ModuleStructure {
    private float fov = 110.0f;
    private float smoothFov = 30.0f;
    private float lastChangedFov = 30.0f;
    private BooleanSetting clipSetting = new BooleanSetting("Camera passthrough", "Camera passes through blocks").setValue(true);
    private SliderSettings distanceSetting = new SliderSettings("Distance camera", "Camera distance setting").setValue(3.0f).range(2.0f, 5.0f);
    private BindSetting zoomSetting = new BindSetting("Zoom", "Camera zoom key");

    public CameraSettings() {
        super("CameraSettings", "Camera Settings", ModuleCategory.RENDER);
        this.settings(this.clipSetting, this.distanceSetting, this.zoomSetting);
    }

    @EventHandler
    public void onKey(KeyEvent e) {
        if (e.isKeyDown(this.zoomSetting.getKey())) {
            this.fov = Math.min(this.lastChangedFov, (float)((Integer)CameraSettings.mc.options.fov().get() - 20));
        }
        if (e.isKeyReleased(this.zoomSetting.getKey(), true)) {
            this.lastChangedFov = this.fov;
            this.fov = ((Integer)CameraSettings.mc.options.fov().get()).intValue();
        }
    }

    @EventHandler
    public void onHotBarScroll(HotBarScrollEvent e) {
        if (PlayerInteractionHelper.isKey(this.zoomSetting)) {
            this.fov = (int)Mth.clamp((double)((double)this.fov - e.getVertical() * 10.0), (double)10.0, (double)((Integer)CameraSettings.mc.options.fov().get()).intValue());
            e.cancel();
        }
    }

    @EventHandler
    public void onFov(FovEvent e) {
        this.smoothFov = MathUtils.interpolateSmooth(1.6, this.smoothFov, this.fov);
        e.setFov((int)Mth.clamp((float)(this.smoothFov + 1.0f), (float)10.0f, (float)((Integer)CameraSettings.mc.options.fov().get()).intValue()));
        e.cancel();
    }

    @EventHandler
    public void onCamera(CameraEvent e) {
        block3: {
            block2: {
                e.setCameraClip(this.clipSetting.isValue());
                e.setDistance(this.distanceSetting.getValue());
                FreeLook freeLook = Instance.get(FreeLook.class);
                if (!freeLook.isState()) break block2;
                if (PlayerInteractionHelper.isKey(FreeLook.freeLookSetting)) break block3;
            }
            e.setAngle(MathAngle.cameraAngle());
        }
        e.cancel();
    }
}

