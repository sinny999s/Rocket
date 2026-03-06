package rich.util.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;

public final class BlockUtil implements IMinecraft {

    public static boolean place(BlockPos pos, Item item, boolean rotate, boolean swing) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return false;
        if (!mc.level.getBlockState(pos).canBeReplaced()) return false;

        int slot = InvUtil.findInHotbar(item);
        InteractionHand hand = InvUtil.getHand(item);

        if (hand == null && slot == -1) return false;

        int prevSlot = mc.player.getInventory().getSelectedSlot();
        boolean switched = false;

        if (hand == null) {
            InvUtil.swap(slot);
            hand = InteractionHand.MAIN_HAND;
            switched = true;
        }

        Direction placeDir = getPlaceDirection(pos);
        if (placeDir == null) return false;

        BlockPos neighbor = pos.relative(placeDir);
        Direction opposite = placeDir.getOpposite();

        Vec3 hitVec = new Vec3(
            neighbor.getX() + 0.5 + opposite.getStepX() * 0.5,
            neighbor.getY() + 0.5 + opposite.getStepY() * 0.5,
            neighbor.getZ() + 0.5 + opposite.getStepZ() * 0.5
        );

        if (rotate) {
            float[] rotations = getRotations(hitVec);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(rotations[0], rotations[1], mc.player.onGround(), mc.player.horizontalCollision));
        }

        BlockHitResult hitResult = new BlockHitResult(hitVec, opposite, neighbor, false);
        mc.gameMode.useItemOn(mc.player, hand, hitResult);

        if (swing) {
            mc.player.swing(hand);
        }

        if (switched) {
            InvUtil.swap(prevSlot);
        }

        return true;
    }

    public static Direction getPlaceDirection(BlockPos pos) {
        if (mc.level == null) return null;
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            if (!mc.level.getBlockState(neighbor).canBeReplaced() && !mc.level.getBlockState(neighbor).isAir()) {
                return dir;
            }
        }
        return null;
    }

    public static float[] getRotations(Vec3 target) {
        Vec3 eyes = mc.player.getEyePosition();
        double dx = target.x - eyes.x;
        double dy = target.y - eyes.y;
        double dz = target.z - eyes.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{yaw, pitch};
    }

    public static void centerPlayer() {
        if (mc.player == null) return;
        double x = Math.floor(mc.player.getX()) + 0.5;
        double z = Math.floor(mc.player.getZ()) + 0.5;
        mc.player.setPos(x, mc.player.getY(), z);
        mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(x, mc.player.getY(), z, mc.player.getYRot(), mc.player.getXRot(), mc.player.onGround(), mc.player.horizontalCollision));
    }

    private BlockUtil() {}
}
