
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundPingPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.Instance;

public class Velocity
extends ModuleStructure {
    private final SelectSetting mode = new SelectSetting("Mode", "Select knockback reduction mode").value("NewGrim", "OldGrim", "Matrix", "Normal").selected("NewGrim");
    private boolean flag;
    private int grimTicks;
    private int ccCooldown;
    private Vec3 pendingVelocity;

    public static Velocity getInstance() {
        return Instance.get(Velocity.class);
    }

    public Velocity() {
        super("Velocity", ModuleCategory.COMBAT);
        this.settings(this.mode);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onPacket(PacketEvent e) {
        ClientboundSetEntityMotionPacket pac;
        if (!this.state) {
            return;
        }
        if (e.getType() != PacketEvent.Type.RECEIVE) {
            return;
        }
        if (Velocity.mc.player == null || Velocity.mc.player.isInWater() || Velocity.mc.player.isUnderWater() || Velocity.mc.player.isInLava()) {
            return;
        }
        if (this.ccCooldown > 0) {
            --this.ccCooldown;
            return;
        }
        Packet<?> packet = e.getPacket();
        if (packet instanceof ClientboundSetEntityMotionPacket && (pac = (ClientboundSetEntityMotionPacket)packet).getId() == Velocity.mc.player.getId()) {
            this.handleVelocityPacket(e, pac);
        }
        this.handleAdditionalPackets(e);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleVelocityPacket(PacketEvent e, ClientboundSetEntityMotionPacket pac) {
        Vec3 velocity = pac.getMovement();
        switch (this.mode.getSelected()) {
            case "Matrix": {
                if (!this.flag) {
                    e.setCancelled(true);
                    this.flag = true;
                    break;
                }
                this.flag = false;
                e.setCancelled(true);
                this.pendingVelocity = new Vec3(velocity.x * -0.1, velocity.y, velocity.z * -0.1);
                break;
            }
            case "Normal": {
                e.setCancelled(true);
                break;
            }
            case "OldGrim": {
                e.setCancelled(true);
                this.grimTicks = 6;
                break;
            }
            case "NewGrim": {
                e.setCancelled(true);
                this.flag = true;
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void handleAdditionalPackets(PacketEvent e) {
        if (this.mode.isSelected("OldGrim") && e.getPacket() instanceof ClientboundPingPacket && this.grimTicks > 0) {
            e.setCancelled(true);
            --this.grimTicks;
        }
        if (e.getPacket() instanceof ClientboundPlayerPositionPacket && this.mode.isSelected("NewGrim")) {
            this.ccCooldown = 5;
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (!this.state || Velocity.mc.player == null || Velocity.mc.player.isInWater() || Velocity.mc.player.isUnderWater()) {
            return;
        }
        if (this.mode.isSelected("Matrix")) {
            this.handleMatrixTick();
        }
        if (this.mode.isSelected("NewGrim") && this.flag) {
            this.handleNewGrimTick();
        }
        if (this.grimTicks > 0) {
            --this.grimTicks;
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleMatrixTick() {
        if (this.pendingVelocity != null) {
            Velocity.mc.player.setDeltaMovement(this.pendingVelocity);
            this.pendingVelocity = null;
        }
        if (Velocity.mc.player.hurtTime > 0 && !Velocity.mc.player.onGround()) {
            double yaw = Velocity.mc.player.getYRot() * ((float)Math.PI / 180);
            double speed = Math.sqrt(Velocity.mc.player.getDeltaMovement().x * Velocity.mc.player.getDeltaMovement().x + Velocity.mc.player.getDeltaMovement().z * Velocity.mc.player.getDeltaMovement().z);
            Velocity.mc.player.setDeltaMovement(-Math.sin(yaw) * speed, Velocity.mc.player.getDeltaMovement().y, Math.cos(yaw) * speed);
            Velocity.mc.player.setSprinting(Velocity.mc.player.tickCount % 2 != 0);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleNewGrimTick() {
        if (this.ccCooldown <= 0) {
            Velocity.mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(Velocity.mc.player.getX(), Velocity.mc.player.getY(), Velocity.mc.player.getZ(), Velocity.mc.player.getYRot(), Velocity.mc.player.getXRot(), Velocity.mc.player.onGround(), false));
            Velocity.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, BlockPos.containing((Position)Velocity.mc.player.position()), Direction.DOWN));
        }
        this.flag = false;
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void activate() {
        super.activate();
        this.grimTicks = 0;
        this.flag = false;
        this.ccCooldown = 0;
        this.pendingVelocity = null;
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        super.deactivate();
        this.pendingVelocity = null;
    }

    @Generated
    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    @Generated
    public void setGrimTicks(int grimTicks) {
        this.grimTicks = grimTicks;
    }

    @Generated
    public void setCcCooldown(int ccCooldown) {
        this.ccCooldown = ccCooldown;
    }

    @Generated
    public void setPendingVelocity(Vec3 pendingVelocity) {
        this.pendingVelocity = pendingVelocity;
    }

    @Generated
    public SelectSetting getMode() {
        return this.mode;
    }

    @Generated
    public boolean isFlag() {
        return this.flag;
    }

    @Generated
    public int getGrimTicks() {
        return this.grimTicks;
    }

    @Generated
    public int getCcCooldown() {
        return this.ccCooldown;
    }

    @Generated
    public Vec3 getPendingVelocity() {
        return this.pendingVelocity;
    }
}

