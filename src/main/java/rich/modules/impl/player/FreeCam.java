/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 */
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import java.lang.invoke.LambdaMetafactory;
import java.util.Objects;
import net.minecraft.client.CameraType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.CameraPositionEvent;
import rich.events.impl.ChunkOcclusionEvent;
import rich.events.impl.GameLeftEvent;
import rich.events.impl.InputEvent;
import rich.events.impl.MoveEvent;
import rich.events.impl.PacketEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.ColorUtil;
import rich.util.Instance;
import rich.util.math.MathUtils;
import rich.util.move.MoveUtil;

public class FreeCam
extends ModuleStructure {
    private final SliderSettings speedSetting = new SliderSettings("Speed", "Select debug camera speed").setValue(2.0f).range(0.5f, 5.0f);
    private final BooleanSetting freezeSetting = new BooleanSetting("Freeze", "You freeze in place").setValue(false);
    private final BooleanSetting reloadChunksSetting = new BooleanSetting("Reload Chunks", "Disables cave culling").setValue(true);
    private final BooleanSetting toggleOnLogSetting = new BooleanSetting("Toggle On Log", "Disable on disconnect").setValue(true);
    public final ColorSetting fakeplayer = new ColorSetting("Color 1", "First gradient color").value(ColorUtil.getColor(255, 50, 100, 255));
    public Vec3 pos;
    public Vec3 prevPos;

    public static FreeCam getInstance() {
        return Instance.get(FreeCam.class);
    }

    public FreeCam() {
        super("FreeCam", "Free Cam", ModuleCategory.PLAYER);
        this.settings(this.speedSetting, this.freezeSetting, this.reloadChunksSetting, this.toggleOnLogSetting);
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void activate() {
        this.prevPos = this.pos = FreeCam.mc.getEntityRenderDispatcher().camera.position();
        if (this.reloadChunksSetting.isValue()) {
            FreeCam.mc.levelRenderer.allChanged();
        }
        super.activate();
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        if (this.reloadChunksSetting.isValue()) {
            mc.execute(() -> FreeCam.mc.levelRenderer.allChanged());
        }
        super.deactivate();
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onPacket(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        Objects.requireNonNull(packet);
        if (packet instanceof ServerboundMovePlayerPacket) {
            if (this.freezeSetting.isValue()) {
                e.cancel();
            }
        } else if (packet instanceof ClientboundRespawnPacket) {
            this.setState(false);
        } else if (packet instanceof ClientboundLoginPacket) {
            this.setState(false);
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onMove(MoveEvent e) {
        if (this.freezeSetting.isValue()) {
            e.setMovement(Vec3.ZERO);
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onInput(InputEvent e) {
        float speed = this.speedSetting.getValue();
        double[] motion = MoveUtil.calculateDirection(e.forward(), e.sideways(), speed);
        this.prevPos = this.pos;
        this.pos = this.pos.add(motion[0], e.getInput().jump() ? (double)speed : (e.getInput().shift() ? (double)(-speed) : 0.0), motion[1]);
        e.inputNone();
    }

    @EventHandler
    public void onCameraPosition(CameraPositionEvent e) {
        e.setPos(MathUtils.interpolate(this.prevPos, this.pos));
        FreeCam.mc.options.setCameraType(CameraType.FIRST_PERSON);
    }

    @EventHandler
    public void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onGameLeft(GameLeftEvent event) {
        if (this.toggleOnLogSetting.isValue()) {
            this.setState(false);
        }
    }
}

