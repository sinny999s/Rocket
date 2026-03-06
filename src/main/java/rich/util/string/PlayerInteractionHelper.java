/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.lwjgl.glfw.GLFW
 */
package rich.util.string;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.Generated;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.module.setting.implement.BindSetting;

public final class PlayerInteractionHelper
implements IMinecraft {
    public static void sendSequencedPacket(PredictiveAction packetCreator) {
        PlayerInteractionHelper.mc.gameMode.startPrediction(PlayerInteractionHelper.mc.level, packetCreator);
    }

    public static void interactItem(InteractionHand hand) {
        PlayerInteractionHelper.interactItem(hand, MathAngle.cameraAngle());
    }

    public static void interactItem(InteractionHand hand, Angle angle) {
        PlayerInteractionHelper.sendSequencedPacket(i -> new ServerboundUseItemPacket(hand, i, angle.getYaw(), angle.getPitch()));
    }

    public static void interactEntity(Entity entity) {
        PlayerInteractionHelper.mc.player.connection.send(ServerboundInteractPacket.createInteractionPacket((Entity)entity, (boolean)false, (InteractionHand)InteractionHand.MAIN_HAND, (Vec3)entity.getBoundingBox().getCenter()));
        PlayerInteractionHelper.mc.player.connection.send(ServerboundInteractPacket.createInteractionPacket((Entity)entity, (boolean)false, (InteractionHand)InteractionHand.MAIN_HAND));
    }

    public static void startFallFlying() {
        PlayerInteractionHelper.mc.player.connection.send(new ServerboundPlayerCommandPacket(PlayerInteractionHelper.mc.player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
        PlayerInteractionHelper.mc.player.startFallFlying();
    }

    public static void sendPacketWithOutEvent(Packet<?> packet) {
        mc.getConnection().getConnection().send(packet, null);
    }

    public static void grimSuperBypass$$$(double y, Angle angle) {
        PlayerInteractionHelper.mc.player.connection.send(new ServerboundMovePlayerPacket.PosRot(PlayerInteractionHelper.mc.player.getX(), PlayerInteractionHelper.mc.player.getY() + y, PlayerInteractionHelper.mc.player.getZ(), angle.getYaw(), angle.getPitch(), PlayerInteractionHelper.mc.player.onGround(), PlayerInteractionHelper.mc.player.horizontalCollision));
    }

    public static String getHealthString(float hp) {
        return String.format("%.1f", Float.valueOf(hp)).replace(",", ".").replace(".0", "");
    }

    public static void jump() {
        if (PlayerInteractionHelper.mc.player.isSprinting()) {
            float g = PlayerInteractionHelper.mc.player.getYRot() * ((float)Math.PI / 180);
            PlayerInteractionHelper.mc.player.addDeltaMovement(new Vec3(-Mth.sin((double)g) * 0.2f, 0.0, Mth.cos((double)g) * 0.2f));
        }
        PlayerInteractionHelper.mc.player.needsSync = true;
    }

    public static List<BlockPos> getCube(BlockPos center, float radius) {
        return PlayerInteractionHelper.getCube(center, radius, radius, true);
    }

    public static List<BlockPos> getCube(BlockPos center, float radiusXZ, float radiusY) {
        return PlayerInteractionHelper.getCube(center, radiusXZ, radiusY, true);
    }

    public static List<BlockPos> getCube(BlockPos center, float radiusXZ, float radiusY, boolean down) {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        int centerX = center.getX();
        int centerY = center.getY();
        int centerZ = center.getZ();
        int posY = down ? centerY - (int)radiusY : centerY;
        int x = centerX - (int)radiusXZ;
        while ((float)x <= (float)centerX + radiusXZ) {
            int z = centerZ - (int)radiusXZ;
            while ((float)z <= (float)centerZ + radiusXZ) {
                int y = posY;
                while ((float)y <= (float)centerY + radiusY) {
                    positions.add(new BlockPos(x, y, z));
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return positions;
    }

    public static List<BlockPos> getCube(BlockPos start, BlockPos end) {
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        for (int x = start.getX(); x <= end.getX(); ++x) {
            for (int z = start.getZ(); z <= end.getZ(); ++z) {
                for (int y = start.getY(); y <= end.getY(); ++y) {
                    positions.add(new BlockPos(x, y, z));
                }
            }
        }
        return positions;
    }

    public static InputConstants.Type getKeyType(int key) {
        return key < 8 ? InputConstants.Type.MOUSE : InputConstants.Type.KEYSYM;
    }

    public static Stream<Entity> streamEntities() {
        return StreamSupport.stream(PlayerInteractionHelper.mc.level.entitiesForRendering().spliterator(), false);
    }

    public static boolean canChangeIntoPose(Pose pose, Vec3 pos) {
        return PlayerInteractionHelper.mc.player.level().noCollision(PlayerInteractionHelper.mc.player, PlayerInteractionHelper.mc.player.getDimensions(pose).makeBoundingBox(pos).deflate(1.0E-7));
    }

    public static boolean isPotionActive(Holder<MobEffect> statusEffect) {
        return PlayerInteractionHelper.mc.player.getActiveEffectsMap().containsKey(statusEffect);
    }

    public static boolean isPlayerInBlock(Block block) {
        return PlayerInteractionHelper.isBoxInBlock(PlayerInteractionHelper.mc.player.getBoundingBox().inflate(-0.001), block);
    }

    public static boolean isBoxInBlock(AABB box, Block block) {
        return PlayerInteractionHelper.isBox(box, pos -> PlayerInteractionHelper.mc.level.getBlockState((BlockPos)pos).getBlock().equals(block));
    }

    public static boolean isBoxInBlocks(AABB box, List<Block> blocks) {
        return PlayerInteractionHelper.isBox(box, pos -> blocks.contains(PlayerInteractionHelper.mc.level.getBlockState((BlockPos)pos).getBlock()));
    }

    public static boolean isBox(AABB box, Predicate<BlockPos> pos) {
        return BlockPos.betweenClosedStream((AABB)box).anyMatch(pos);
    }

    public static boolean isKey(BindSetting setting) {
        int key = setting.getKey();
        return PlayerInteractionHelper.mc.screen == null && setting.isVisible() && PlayerInteractionHelper.isKey(PlayerInteractionHelper.getKeyType(key), key);
    }

    public static boolean isKey(KeyMapping key) {
        return PlayerInteractionHelper.isKey(key.getDefaultKey().getType(), key.getDefaultKey().getValue());
    }

    public static boolean isKey(InputConstants.Type type, int keyCode) {
        if (keyCode != -1) {
            switch (type) {
                case KEYSYM: {
                    return GLFW.glfwGetKey((long)mc.getWindow().handle(), (int)keyCode) == 1;
                }
                case MOUSE: {
                    return GLFW.glfwGetMouseButton((long)mc.getWindow().handle(), (int)keyCode) == 1;
                }
            }
        }
        return false;
    }

    public static boolean isAir(BlockPos blockPos) {
        return PlayerInteractionHelper.isAir(PlayerInteractionHelper.mc.level.getBlockState(blockPos));
    }

    public static boolean isAir(BlockState state) {
        return state.isAir() || state.getBlock().equals(Blocks.CAVE_AIR) || state.getBlock().equals(Blocks.VOID_AIR);
    }

    public static boolean isChat(Screen screen) {
        return screen instanceof ChatScreen;
    }

    public static boolean nullCheck() {
        return PlayerInteractionHelper.mc.player == null || PlayerInteractionHelper.mc.level == null;
    }

    @Generated
    private PlayerInteractionHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

