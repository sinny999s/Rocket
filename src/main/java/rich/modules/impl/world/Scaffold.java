package rich.modules.impl.world;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.PredictMovementUtils;
import rich.util.combat.BlockUtil;
import rich.util.combat.InvUtil;
import rich.util.render.Render3D;

public class Scaffold extends ModuleStructure {

    private final SelectSetting mode = new SelectSetting("Mode", "Scaffold mode").value("Normal", "GodBridge");
    private final SliderSettings delay = new SliderSettings("Delay", "Tick delay between placements").setValue(0.0f).range(0, 5);
    private final SliderSettings shiftTicks = new SliderSettings("ShiftTicks", "Max blocks per tick").setValue(1.0f).range(1, 8);
    private final BooleanSetting swing = new BooleanSetting("Swing", "Swing hand on place").setValue(true);
    private final BooleanSetting singleBlock = new BooleanSetting("SingleBlock", "Only place closest block").setValue(true);
    private final BooleanSetting render = new BooleanSetting("Render", "Render target block outline").setValue(false);
    private final SelectSetting safeWalk = new SelectSetting("SafeWalk", "Sneak at edges to prevent falling").value("Off", "Always", "Corners");

    private int cooldown = 0;
    private BlockPos renderPos = null;
    private boolean isOnRightSide = false;

    public Scaffold() {
        super("Scaffold", "Automatically scaffolds blocks beneath you.", ModuleCategory.WORLD);
        this.settings(this.mode, this.delay, this.shiftTicks, this.swing, this.singleBlock, this.render, this.safeWalk);
    }

    @Override
    public void deactivate() {
        cooldown = 0;
        renderPos = null;
        isOnRightSide = false;
        setSneakHeld(false);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) {
            cooldown = 0;
            renderPos = null;
            return;
        }

        handleSafeWalk();

        if (cooldown > 0) {
            cooldown--;
            renderPos = null;
            return;
        }

        if (mode.isSelected("GodBridge")) {
            tickGodBridge();
        } else {
            tickNormal();
        }
    }

    private void tickNormal() {
        BlockPos[] corners = getPlacements();
        int blocksPlaced = 0;
        renderPos = null;

        for (BlockPos pos : corners) {
            BlockPos targetPos = pos.below();

            if (isPlaceable(targetPos) || !isReplaceable(targetPos))
                continue;

            renderPos = targetPos;

            Item blockItem = findBlockItem();
            if (blockItem == null) break;

            if (BlockUtil.place(targetPos, blockItem, true, swing.isValue()))
                blocksPlaced++;

            if (blocksPlaced >= shiftTicks.getInt()) break;
        }

        if (blocksPlaced > 0) cooldown = delay.getInt();
    }

    private void tickGodBridge() {
        BlockPos[] corners = getPlacements();
        BlockPos bestTarget = null;
        renderPos = null;

        for (BlockPos pos : corners) {
            BlockPos targetPos = pos.below();
            if (isPlaceable(targetPos) || !isReplaceable(targetPos)) continue;
            bestTarget = targetPos;
            renderPos = targetPos;
            break;
        }

        if (bestTarget == null) return;

        Direction placeDir = BlockUtil.getPlaceDirection(bestTarget);
        if (placeDir == null) return;

        float movingYaw = getMovingYaw();
        float roundedYaw = Math.round(movingYaw / 45.0f) * 45.0f;
        boolean isStraight = roundedYaw % 90 == 0;

        float yaw;
        float pitch;

        if (isStraight) {
            if (mc.player.onGround()) {
                double cos = Math.cos(Math.toRadians(roundedYaw));
                double sin = Math.sin(Math.toRadians(roundedYaw));
                boolean shifted = Math.floor(mc.player.getX() + cos * 0.5) != Math.floor(mc.player.getX())
                        || Math.floor(mc.player.getZ() + sin * 0.5) != Math.floor(mc.player.getZ());
                isOnRightSide = shifted;

                BlockPos belowPlayer = mc.player.blockPosition().below();
                boolean leaningOff = mc.level.getBlockState(belowPlayer).isAir();
                if (leaningOff) {
                    Vec3 ahead = mc.player.position().add(
                            Math.sin(Math.toRadians(-roundedYaw)) * 0.6,
                            0,
                            Math.cos(Math.toRadians(roundedYaw)) * 0.6
                    );
                    BlockPos aheadBelow = new BlockPos((int) Math.floor(ahead.x), (int) Math.floor(mc.player.getY()) - 1, (int) Math.floor(ahead.z));
                    if (mc.level.getBlockState(aheadBelow).isAir()) {
                        isOnRightSide = !isOnRightSide;
                    }
                }
            }
            yaw = roundedYaw + (isOnRightSide ? 45.0f : -45.0f);
            pitch = 75.7f;
        } else {
            yaw = roundedYaw;
            pitch = 75.6f;
        }

        BlockPos neighbor = bestTarget.relative(placeDir);
        Direction opposite = placeDir.getOpposite();
        Vec3 hitVec = new Vec3(
                neighbor.getX() + 0.5 + opposite.getStepX() * 0.5,
                neighbor.getY() + 0.5 + opposite.getStepY() * 0.5,
                neighbor.getZ() + 0.5 + opposite.getStepZ() * 0.5
        );

        mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(
                yaw, pitch, mc.player.onGround(), mc.player.horizontalCollision));

        Item blockItem = findBlockItem();
        if (blockItem == null) return;

        int slot = InvUtil.findInHotbar(blockItem);
        InteractionHand hand = InvUtil.getHand(blockItem);
        int prevSlot = mc.player.getInventory().getSelectedSlot();
        boolean switched = false;

        if (hand == null && slot != -1) {
            InvUtil.swap(slot);
            hand = InteractionHand.MAIN_HAND;
            switched = true;
        } else if (hand == null) {
            return;
        }

        BlockHitResult hitResult = new BlockHitResult(hitVec, opposite, neighbor, false);
        mc.gameMode.useItemOn(mc.player, hand, hitResult);

        if (swing.isValue()) {
            mc.player.swing(hand);
        }

        if (switched) {
            InvUtil.swap(prevSlot);
        }

        cooldown = delay.getInt();
    }

    private float getMovingYaw() {
        float forward = 0, strafe = 0;
        if (mc.options.keyUp.isDown()) forward++;
        if (mc.options.keyDown.isDown()) forward--;
        if (mc.options.keyLeft.isDown()) strafe++;
        if (mc.options.keyRight.isDown()) strafe--;

        float yaw = mc.player.getYRot();

        if (forward == 0 && strafe == 0) return yaw + 180;

        if (forward > 0) {
            if (strafe > 0) return yaw + 225;
            if (strafe < 0) return yaw + 135;
            return yaw + 180;
        }
        if (forward < 0) {
            if (strafe > 0) return yaw + 315;
            if (strafe < 0) return yaw + 45;
            return yaw;
        }
        if (strafe > 0) return yaw + 270;
        return yaw + 90;
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (mc.player == null || mc.level == null || renderPos == null || !render.isValue()) return;

        AABB box = new AABB(renderPos);
        Render3D.drawBox(box, 0x8000FF00, 1.5f);
    }

    private Item findBlockItem() {
        if (mc.player == null) return null;

        ItemStack offhand = mc.player.getOffhandItem();
        if (!offhand.isEmpty()) {
            Block block = Block.byItem(offhand.getItem());
            if (block != Blocks.AIR) {
                return offhand.getItem();
            }
        }

        ItemStack mainhand = mc.player.getMainHandItem();
        if (!mainhand.isEmpty()) {
            Block block = Block.byItem(mainhand.getItem());
            if (block != Blocks.AIR) {
                return mainhand.getItem();
            }
        }

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            Block block = Block.byItem(stack.getItem());
            if (block == Blocks.AIR) continue;

            return stack.getItem();
        }

        return null;
    }

    private BlockPos[] getPlacements() {
        double minX = mc.player.getBoundingBox().minX;
        double maxX = mc.player.getBoundingBox().maxX;
        double minZ = mc.player.getBoundingBox().minZ;
        double maxZ = mc.player.getBoundingBox().maxZ;
        int y = (int) Math.floor(mc.player.getY());

        BlockPos[] valid = new BlockPos[]{
                new BlockPos((int) Math.floor(minX), y, (int) Math.floor(minZ)),
                new BlockPos((int) Math.floor(minX), y, (int) Math.floor(maxZ)),
                new BlockPos((int) Math.floor(maxX), y, (int) Math.floor(minZ)),
                new BlockPos((int) Math.floor(maxX), y, (int) Math.floor(maxZ))
        };

        if (!singleBlock.isValue() || valid.length <= 1)
            return valid;

        PredictMovementUtils.PredictedEntity initial = new PredictMovementUtils.PredictedEntity(
                mc.player.position(), mc.player.getDeltaMovement(),
                mc.player.getYRot(), mc.player.getXRot(),
                mc.player.onGround(), mc.player.getEyeHeight()
        );

        PredictMovementUtils.PredictedEntity predicted = PredictMovementUtils.predict(initial, 3, t -> Vec3.ZERO);
        Vec3 eyePos = predicted != null ? predicted.getEyePos() : mc.player.getEyePosition();
        BlockPos closest = null;
        double bestDist = Double.MAX_VALUE;

        for (BlockPos pos : valid) {
            Vec3 center = Vec3.atCenterOf(pos);
            double dist = center.distanceToSqr(eyePos);

            if (dist < bestDist) {
                bestDist = dist;
                closest = pos;
            }
        }

        return closest != null ? new BlockPos[]{closest} : valid;
    }

    private boolean isPlaceable(BlockPos pos) {
        AABB blockBox = new AABB(pos);
        for (net.minecraft.world.entity.Entity entity : mc.level.entitiesForRendering()) {
            if (entity.distanceToSqr(mc.player) > 10) continue;
            if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EndCrystal) continue;
            if (entity instanceof net.minecraft.world.entity.item.ItemEntity) continue;
            if (entity instanceof net.minecraft.world.entity.projectile.arrow.Arrow) continue;

            if (entity.getBoundingBox().intersects(blockBox)) {
                return true;
            }
        }
        return false;
    }

    private boolean isReplaceable(BlockPos pos) {
        return mc.level.getBlockState(pos).canBeReplaced();
    }

    // --- SafeWalk ---

    private void handleSafeWalk() {
        if (safeWalk.isSelected("Off")) {
            setSneakHeld(false);
            return;
        }

        if (safeWalk.isSelected("Always")) {
            setSneakHeld(true);
            return;
        }

        if (safeWalk.isSelected("Corners")) {
            setSneakHeld(shouldSneakAtEdges());
        }
    }

    private boolean shouldSneakAtEdges() {
        if (mc.player == null || mc.level == null) return false;

        Vec3 pos = mc.player.position();
        int blockY = (int) Math.floor(pos.y - 0.001);

        if (!mc.player.onGround()) return false;

        BlockPos basePos = new BlockPos(mc.player.blockPosition().getX(), blockY, mc.player.blockPosition().getZ());

        BlockPos closestBlock = null;
        double closestDistanceSq = Double.MAX_VALUE;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos checkPos = basePos.offset(dx, 0, dz);
                BlockState state = mc.level.getBlockState(checkPos);

                if (state.isAir()) continue;

                double centerX = checkPos.getX() + 0.5;
                double centerZ = checkPos.getZ() + 0.5;
                double distSq = pos.distanceToSqr(centerX, pos.y, centerZ);

                if (distSq < closestDistanceSq) {
                    closestDistanceSq = distSq;
                    closestBlock = checkPos;
                }
            }
        }

        if (closestBlock == null) return true;

        double centerX = closestBlock.getX() + 0.5;
        double centerZ = closestBlock.getZ() + 0.5;
        double edgeDx = pos.x - centerX;
        double edgeDz = pos.z - centerZ;

        boolean nearEdgeX = Math.abs(edgeDx) > 0.55;
        boolean nearEdgeZ = Math.abs(edgeDz) > 0.55;

        if (!nearEdgeX && !nearEdgeZ) return false;

        int offsetX = 0;
        int offsetZ = 0;

        if (Math.abs(edgeDx) >= Math.abs(edgeDz)) {
            offsetX = edgeDx > 0 ? 1 : -1;
        } else {
            offsetZ = edgeDz > 0 ? 1 : -1;
        }

        BlockPos directionToCheck = closestBlock.offset(offsetX, 0, offsetZ);
        BlockState supportBlock = mc.level.getBlockState(directionToCheck);

        return supportBlock.isAir();
    }

    private void setSneakHeld(boolean held) {
        KeyMapping sneakKey = mc.options.keyShift;
        boolean physicallyPressed = InputConstants.isKeyDown(mc.getWindow(), sneakKey.getDefaultKey().getValue());
        sneakKey.setDown(physicallyPressed || held);
    }
}
