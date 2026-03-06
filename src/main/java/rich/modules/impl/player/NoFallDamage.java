
package rich.modules.impl.player;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;

public class NoFallDamage extends ModuleStructure {

    private final SelectSetting mode = new SelectSetting("Mode", "NoFall bypass mode")
        .value("Packet", "Anti", "Strict", "Limit", "Grim").selected("Packet");

    private double groundedY;
    private boolean sending = false;

    public NoFallDamage() {
        super("NoFallDamage", "No Fall Damage", ModuleCategory.PLAYER);
        this.settings(this.mode);
    }

    private boolean checkFalling() {
        return mc.player != null
            && mc.level != null
            && mc.player.fallDistance > 3.0f
            && !mc.player.onGround()
            && !mc.player.isFallFlying();
    }

    private double getGroundY() {
        AABB bb = mc.player.getBoundingBox();
        int startY = (int) Math.round(mc.player.getY());
        for (int y = startY; y > mc.level.getMinY(); y--) {
            AABB box = new AABB(bb.minX, y - 1.0, bb.minZ, bb.maxX, y, bb.maxZ);
            if (!mc.level.noCollision(box) && box.minY <= mc.player.getY()) {
                return y;
            }
        }
        return 0.0;
    }

    private boolean isFallingLagback() {
        groundedY = getGroundY() - 0.1;
        return mc.player.getY() - groundedY < 3.0;
    }

    private void sendNoFall(ServerboundMovePlayerPacket packet) {
        sending = true;
        try {
            mc.player.connection.send(packet);
        } finally {
            sending = false;
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (sending) return;
        if (event.getType() != PacketEvent.Type.SEND) return;
        if (!(event.getPacket() instanceof ServerboundMovePlayerPacket packet)) return;
        if (mc.player == null || mc.level == null) return;
        if (!checkFalling()) return;

        double x = packet.getX(mc.player.getX());
        double y = packet.getY(mc.player.getY());
        double z = packet.getZ(mc.player.getZ());
        float yaw = mc.player.getYRot();
        float pitch = mc.player.getXRot();
        boolean collision = packet.horizontalCollision();

        if (mode.isSelected("Packet")) {
            event.cancel();
            ServerboundMovePlayerPacket replacement;
            if (packet instanceof ServerboundMovePlayerPacket.PosRot) {
                replacement = new ServerboundMovePlayerPacket.PosRot(x, y, z, yaw, pitch, true, collision);
            } else if (packet instanceof ServerboundMovePlayerPacket.Pos) {
                replacement = new ServerboundMovePlayerPacket.Pos(x, y, z, true, collision);
            } else if (packet instanceof ServerboundMovePlayerPacket.Rot) {
                replacement = new ServerboundMovePlayerPacket.Rot(yaw, pitch, true, collision);
            } else {
                replacement = new ServerboundMovePlayerPacket.StatusOnly(true, collision);
            }
            sendNoFall(replacement);

        } else if (mode.isSelected("Anti")) {
            event.cancel();
            double antiY = y + 0.10000000149011612;
            ServerboundMovePlayerPacket replacement;
            if (packet instanceof ServerboundMovePlayerPacket.PosRot) {
                replacement = new ServerboundMovePlayerPacket.PosRot(x, antiY, z, yaw, pitch, packet.isOnGround(), collision);
            } else if (packet instanceof ServerboundMovePlayerPacket.Pos) {
                replacement = new ServerboundMovePlayerPacket.Pos(x, antiY, z, packet.isOnGround(), collision);
            } else if (packet instanceof ServerboundMovePlayerPacket.Rot) {
                replacement = new ServerboundMovePlayerPacket.Rot(yaw, pitch, packet.isOnGround(), collision);
            } else {
                replacement = new ServerboundMovePlayerPacket.StatusOnly(packet.isOnGround(), collision);
            }
            sendNoFall(replacement);

        } else if (mode.isSelected("Strict")) {
            if (!isFallingLagback() || mc.player.fallDistance < 3.0f) return;

            event.cancel();
            Vec3 prev = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(prev.x, 0.0, prev.z);
            mc.player.fallDistance = 0.0f;

            ServerboundMovePlayerPacket replacement;
            if (packet instanceof ServerboundMovePlayerPacket.PosRot) {
                replacement = new ServerboundMovePlayerPacket.PosRot(x, groundedY, z, yaw, pitch, true, collision);
            } else if (packet instanceof ServerboundMovePlayerPacket.Pos) {
                replacement = new ServerboundMovePlayerPacket.Pos(x, groundedY, z, true, collision);
            } else if (packet instanceof ServerboundMovePlayerPacket.Rot) {
                replacement = new ServerboundMovePlayerPacket.Rot(yaw, pitch, true, collision);
            } else {
                replacement = new ServerboundMovePlayerPacket.StatusOnly(true, collision);
            }
            sendNoFall(replacement);

        } else if (mode.isSelected("Limit")) {
            double sendX = x, sendY, sendZ = z;
            if (mc.level.dimensionType().hasCeiling()) {
                sendY = 0.0;
            } else {
                sendX = 0.0;
                sendY = 64.0;
                sendZ = 0.0;
            }
            sendNoFall(new ServerboundMovePlayerPacket.Pos(sendX, sendY, sendZ, true, mc.player.horizontalCollision));
            mc.player.fallDistance = 0.0f;

        } else if (mode.isSelected("Grim")) {
            sendNoFall(new ServerboundMovePlayerPacket.PosRot(x, y + 1.0e-9, z, yaw, pitch, true, collision));
            mc.player.fallDistance = 0.0f;
        }
    }
}

