
package rich.util.move;

import java.util.Objects;
import net.minecraft.client.player.ClientInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.AngleConnection;

public class MoveUtil
implements IMinecraft {
    public static boolean hasPlayerMovement() {
        ClientInput input = MoveUtil.mc.player.input;
        if (input.hasForwardImpulse()) {
            return true;
        }
        Vec2 vec = input.getMoveVector();
        return vec.x != 0.0f || vec.y != 0.0f;
    }

    public static double getDistanceToGround() {
        for (double y = MoveUtil.mc.player.getY(); y > 0.0; y -= 0.1) {
            if (MoveUtil.mc.level.getBlockState(MoveUtil.mc.player.blockPosition().below((int)(MoveUtil.mc.player.getY() - y + 1.0))).isAir()) continue;
            return MoveUtil.mc.player.getY() - y;
        }
        return 256.0;
    }

    public static double getDegreesRelativeToView(Vec3 positionRelativeToPlayer, float yaw) {
        float optimalYaw = (float)Math.atan2(-positionRelativeToPlayer.x, positionRelativeToPlayer.z);
        double currentYaw = Math.toRadians(Mth.wrapDegrees((float)yaw));
        return Math.toDegrees(Mth.wrapDegrees((double)((double)optimalYaw - currentYaw)));
    }

    public static void setVelocity(double velocity) {
        double[] direction = MoveUtil.calculateDirection(velocity);
        Objects.requireNonNull(MoveUtil.mc.player).setDeltaMovement(direction[0], MoveUtil.mc.player.getDeltaMovement().y(), direction[1]);
    }

    public static double[] forward(double d) {
        Vec2 movement = MoveUtil.mc.player.input.getMoveVector();
        float f = movement.y;
        float f2 = movement.x;
        float f3 = AngleConnection.INSTANCE.getRotation().getYaw();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += (float)(f > 0.0f ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += (float)(f > 0.0f ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        double d4 = (double)f * d * d3 + (double)f2 * d * d2;
        double d5 = (double)f * d * d2 - (double)f2 * d * d3;
        return new double[]{d4, d5};
    }

    public static double[] calculateDirection(double distance) {
        Vec2 movement = MoveUtil.mc.player.input.getMoveVector();
        float forward = movement.y;
        float sideways = movement.x;
        return MoveUtil.calculateDirection(forward, sideways, distance);
    }

    public static double[] calculateDirection(float forward, float sideways, double distance) {
        float yaw = AngleConnection.INSTANCE.getRotation().getYaw();
        if (forward != 0.0f) {
            if (sideways > 0.0f) {
                yaw += forward > 0.0f ? -45.0f : 45.0f;
            } else if (sideways < 0.0f) {
                yaw += forward > 0.0f ? 45.0f : -45.0f;
            }
            sideways = 0.0f;
            forward = forward > 0.0f ? 1.0f : -1.0f;
        }
        double sinYaw = Math.sin(Math.toRadians(yaw + 90.0f));
        double cosYaw = Math.cos(Math.toRadians(yaw + 90.0f));
        double xMovement = (double)forward * distance * cosYaw + (double)sideways * distance * sinYaw;
        double zMovement = (double)forward * distance * sinYaw - (double)sideways * distance * cosYaw;
        return new double[]{xMovement, zMovement};
    }

    public static final boolean moveKeyPressed(int keyNumber) {
        boolean w = MoveUtil.mc.options.keyUp.isDown();
        boolean a = MoveUtil.mc.options.keyLeft.isDown();
        boolean s = MoveUtil.mc.options.keyDown.isDown();
        boolean d = MoveUtil.mc.options.keyRight.isDown();
        return keyNumber == 0 ? w : (keyNumber == 1 ? a : (keyNumber == 2 ? s : keyNumber == 3 && d));
    }

    public static final boolean w() {
        return MoveUtil.moveKeyPressed(0);
    }

    public static final boolean a() {
        return MoveUtil.moveKeyPressed(1);
    }

    public static final boolean s() {
        return MoveUtil.moveKeyPressed(2);
    }

    public static final boolean d() {
        return MoveUtil.moveKeyPressed(3);
    }

    public static final float moveYaw(float entityYaw) {
        return entityYaw + (float)(!MoveUtil.a() || !MoveUtil.d() || MoveUtil.w() && MoveUtil.s() || !MoveUtil.w() && !MoveUtil.s() ? (MoveUtil.w() && MoveUtil.s() && (!MoveUtil.a() || !MoveUtil.d()) && (MoveUtil.a() || MoveUtil.d()) ? (MoveUtil.a() ? -90 : (MoveUtil.d() ? 90 : 0)) : (MoveUtil.a() && MoveUtil.d() && (!MoveUtil.w() || !MoveUtil.s()) || MoveUtil.w() && MoveUtil.s() && (!MoveUtil.a() || !MoveUtil.d()) ? 0 : (!(MoveUtil.a() || MoveUtil.d() || MoveUtil.s()) ? 0 : (MoveUtil.w() && !MoveUtil.s() ? 45 : (MoveUtil.s() && !MoveUtil.w() ? (!MoveUtil.a() && !MoveUtil.d() ? 180 : 135) : (!(!MoveUtil.w() && !MoveUtil.s() || MoveUtil.w() && MoveUtil.s()) ? 0 : 90))) * (MoveUtil.a() ? -1 : 1)))) : (MoveUtil.w() ? 0 : (MoveUtil.s() ? 180 : 0)));
    }

    public static float calculateBodyYaw(float yaw, float prevBodyYaw, double prevX, double prevZ, double currentX, double currentZ, float handSwingProgress) {
        double motionX = currentX - prevX;
        double motionZ = currentZ - prevZ;
        float motionSquared = (float)(motionX * motionX + motionZ * motionZ);
        float bodyYaw = prevBodyYaw;
        float swing = MoveUtil.mc.player.attackAnim;
        if (motionSquared > 0.0025000002f) {
            float movementYaw = (float)Mth.atan2((double)motionZ, (double)motionX) * 57.295776f - 90.0f;
            float yawDiff = Mth.abs((float)(Mth.wrapDegrees((float)yaw) - movementYaw));
            bodyYaw = 95.0f < yawDiff && yawDiff < 265.0f ? movementYaw - 180.0f : movementYaw;
        }
        if (MoveUtil.mc.player != null && MoveUtil.mc.player.attackAnim - 0.2f > 0.0f) {
            bodyYaw = yaw;
        }
        float deltaYaw = Mth.wrapDegrees((float)(bodyYaw - prevBodyYaw));
        bodyYaw = prevBodyYaw + deltaYaw * 0.3f;
        float yawOffsetDiff = Mth.wrapDegrees((float)(yaw - bodyYaw));
        float maxHeadRotation = 52.0f;
        if (Math.abs(yawOffsetDiff) > maxHeadRotation) {
            bodyYaw += yawOffsetDiff - (float)Mth.sign((double)yawOffsetDiff) * maxHeadRotation;
        }
        return bodyYaw;
    }

    public static Input getDirectionalInputForDegrees(Input input, double dgs, float deadAngle) {
        boolean forwards = input.forward();
        boolean backwards = input.backward();
        boolean left = input.left();
        boolean right = input.right();
        if (dgs >= (double)(-90.0f + deadAngle) && dgs <= (double)(90.0f - deadAngle)) {
            forwards = true;
        } else if (dgs < (double)(-90.0f - deadAngle) || dgs > (double)(90.0f + deadAngle)) {
            backwards = true;
        }
        if (dgs >= (double)(0.0f + deadAngle) && dgs <= (double)(180.0f - deadAngle)) {
            right = true;
        } else if (dgs >= (double)(-180.0f + deadAngle) && dgs <= (double)(0.0f - deadAngle)) {
            left = true;
        }
        return new Input(forwards, backwards, left, right, input.jump(), input.shift(), input.sprint());
    }

    public static Input getDirectionalInputForDegrees(Input input, double dgs) {
        return MoveUtil.getDirectionalInputForDegrees(input, dgs, 20.0f);
    }
}

