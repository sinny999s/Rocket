
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import rich.events.api.EventHandler;
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.rotations.SnapAngle;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.inventory.InventoryUtils;
import rich.util.math.TaskPriority;
import rich.util.player.PlayerSimulation;
import rich.util.string.PlayerInteractionHelper;
import rich.util.timer.StopWatch;

public class Spider
extends ModuleStructure {
    private final StopWatch stopWatch = new StopWatch();
    private final SelectSetting mode = new SelectSetting("Mode", "Selects mode").value("SpookyTime", "FunTime", "Slime Block", "Water Bucket").selected("Slime Block");
    private int cooldown;
    private boolean startSetPitch = false;

    public Spider() {
        super("Spider", ModuleCategory.MOVEMENT);
        this.settings(this.mode);
    }

    private Block getBlockState(BlockPos blockPos) {
        return Spider.mc.level.getBlockState(blockPos).getBlock();
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        if (this.mode.isSelected("Slime Block")) {
            Spider.mc.options.keyJump.setDown(false);
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (Spider.mc.player == null || Spider.mc.level == null) {
            return;
        }
        if (this.mode.isSelected("FunTime")) {
            this.handleFunTimeMode();
        }
        if (this.mode.isSelected("Water Bucket")) {
            this.handleWaterBucketMode();
        }
        if (this.mode.isSelected("SpookyTime") && this.stopWatch.finished(310.0)) {
            this.handleSpookyTimeMode();
        }
        if (this.mode.isSelected("Slime Block")) {
            this.handleSlimeBlock();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleFunTimeMode() {
        if (Spider.mc.options.keyJump.isDown()) {
            return;
        }
        AABB playerBox = Spider.mc.player.getBoundingBox().inflate(-0.001);
        AABB box = new AABB(playerBox.minX, playerBox.minY, playerBox.minZ, playerBox.maxX, playerBox.minY + 0.5, playerBox.maxZ);
        if (this.stopWatch.finished(400.0) && PlayerInteractionHelper.isBox(box, this::hasCollision)) {
            box = new AABB(playerBox.minX - 0.3, playerBox.minY + 1.0, playerBox.minZ - 0.3, playerBox.maxX, playerBox.maxY, playerBox.maxZ);
            if (PlayerInteractionHelper.isBox(box, this::hasCollision)) {
                Spider.mc.player.setOnGround(true);
                Spider.mc.player.setDeltaMovement(Spider.mc.player.getDeltaMovement().x, 0.6, Spider.mc.player.getDeltaMovement().z);
            } else {
                Spider.mc.player.setOnGround(true);
                Spider.mc.player.jumpFromGround();
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void handleWaterBucketMode() {
        if (Spider.mc.player.getMainHandItem().getItem() == Items.WATER_BUCKET && Spider.mc.player.horizontalCollision) {
            Spider.mc.gameMode.useItem(Spider.mc.player, InteractionHand.MAIN_HAND);
            Spider.mc.player.swing(InteractionHand.MAIN_HAND);
            Spider.mc.player.setDeltaMovement(Spider.mc.player.getDeltaMovement().x, 0.3, Spider.mc.player.getDeltaMovement().z);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleSpookyTimeMode() {
        if (Spider.mc.player.getMainHandItem().getItem() == Items.WATER_BUCKET && Spider.mc.player.horizontalCollision) {
            Spider.mc.player.connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, Spider.mc.player.getYRot(), Spider.mc.player.getXRot()));
            Spider.mc.player.connection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            Spider.mc.player.setDeltaMovement(Spider.mc.player.getDeltaMovement().x, 0.35, Spider.mc.player.getDeltaMovement().z);
        }
        this.stopWatch.reset();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleSlimeBlock() {
        BlockPos playerPos = Spider.mc.player.blockPosition();
        BlockPos[] adjacentBlocks = new BlockPos[]{playerPos.east(), playerPos.west(), playerPos.north(), playerPos.south()};
        boolean hasAdjacentSlime = false;
        for (BlockPos pos : adjacentBlocks) {
            if (this.getBlockState(pos) != Blocks.SLIME_BLOCK) continue;
            hasAdjacentSlime = true;
            break;
        }
        if (!hasAdjacentSlime || !Spider.mc.player.horizontalCollision || Spider.mc.player.getDeltaMovement().y <= -1.0) {
            return;
        }
        HitResult crosshair = Spider.mc.hitResult;
        if (crosshair instanceof BlockHitResult) {
            BlockHitResult blockHit = (BlockHitResult)crosshair;
            Direction face = blockHit.getDirection();
            BlockPos targetPos = blockHit.getBlockPos();
            if (this.getBlockState(targetPos) == Blocks.AIR) {
                return;
            }
            int slimeSlot = this.findHotbarSlot(Items.SLIME_BLOCK);
            if (slimeSlot != -1) {
                InventoryUtils.selectSlot(slimeSlot);
                this.startSetPitch = true;
                Spider.mc.player.setXRot(54.0f);
                BlockHitResult interaction = new BlockHitResult(blockHit.getLocation(), face, targetPos, false);
                Spider.mc.gameMode.useItemOn(Spider.mc.player, InteractionHand.MAIN_HAND, interaction);
                Spider.mc.player.swing(InteractionHand.MAIN_HAND);
                if ((double)this.cooldown >= 0.5) {
                    Spider.mc.player.setDeltaMovement(Spider.mc.player.getDeltaMovement().x, 0.63, Spider.mc.player.getDeltaMovement().z);
                    this.cooldown = 0;
                } else {
                    ++this.cooldown;
                }
            }
        }
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (e.getType() != 0) {
            return;
        }
        if (Spider.mc.player == null || Spider.mc.level == null) {
            return;
        }
        if (!this.mode.isSelected("Slime Block")) {
            return;
        }
        boolean offHand = Spider.mc.player.getOffhandItem().getItem() instanceof BlockItem;
        int slotId = this.findHotbarBlockSlot();
        BlockPos blockPos = this.findPos();
        if ((offHand || slotId != -1) && !blockPos.equals(BlockPos.ZERO)) {
            ItemStack stack = offHand ? Spider.mc.player.getOffhandItem() : Spider.mc.player.getInventory().getItem(slotId);
            InteractionHand hand = offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            Vec3 vec = blockPos.getCenter();
            Direction direction = Direction.getApproximateNearest((double)(vec.x - Spider.mc.player.getX()), (double)(vec.y - Spider.mc.player.getY()), (double)(vec.z - Spider.mc.player.getZ()));
            Angle angle = MathAngle.calculateAngle(vec.subtract(new Vec3(direction.getUnitVec3i()).scale(0.1f)));
            Angle.VecRotation vecRotation = new Angle.VecRotation(angle, angle.toVector());
            AngleConnection.INSTANCE.rotateTo(vecRotation, Spider.mc.player, 1, new AngleConfig(new SnapAngle(), true, true), TaskPriority.HIGH_IMPORTANCE_1, this);
            if (this.canPlace(stack)) {
                int prev = Spider.mc.player.getInventory().getSelectedSlot();
                if (!offHand) {
                    InventoryUtils.selectSlot(slotId);
                }
                Spider.mc.gameMode.useItemOn(Spider.mc.player, hand, new BlockHitResult(vec, direction.getOpposite(), blockPos, false));
                Spider.mc.player.connection.send(new ServerboundSwingPacket(hand));
                if (!offHand) {
                    InventoryUtils.selectSlot(prev);
                }
            }
        }
    }

    private int findHotbarSlot(Item item) {
        for (int i = 0; i < 9; ++i) {
            if (Spider.mc.player.getInventory().getItem(i).getItem() != item) continue;
            return i;
        }
        return -1;
    }

    private int findHotbarBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            if (!(Spider.mc.player.getInventory().getItem(i).getItem() instanceof BlockItem)) continue;
            return i;
        }
        return -1;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean canPlace(ItemStack stack) {
        BlockPos blockPos = this.getBlockPos();
        if (blockPos.getY() >= Spider.mc.player.getBlockY()) {
            return false;
        }
        BlockItem blockItem = (BlockItem)stack.getItem();
        VoxelShape shape = blockItem.getBlock().defaultBlockState().getCollisionShape(Spider.mc.level, blockPos);
        if (shape.isEmpty()) {
            return false;
        }
        AABB box = shape.bounds().move(blockPos);
        return !box.intersects(Spider.mc.player.getBoundingBox()) && box.intersects(PlayerSimulation.simulateLocalPlayer((int)4).boundingBox);
    }

    private BlockPos findPos() {
        BlockPos blockPos = this.getBlockPos();
        if (Spider.mc.level.getBlockState(blockPos).isSolid()) {
            return BlockPos.ZERO;
        }
        return Stream.of(blockPos.west(), blockPos.east(), blockPos.south(), blockPos.north()).filter(pos -> Spider.mc.level.getBlockState((BlockPos)pos).isSolid()).findFirst().orElse(BlockPos.ZERO);
    }

    private BlockPos getBlockPos() {
        return BlockPos.containing((Position)PlayerSimulation.simulateLocalPlayer((int)1).pos.add(0.0, -0.001, 0.0));
    }

    private boolean hasCollision(BlockPos blockPos) {
        return !Spider.mc.level.getBlockState(blockPos).getCollisionShape(Spider.mc.level, blockPos).isEmpty();
    }

    @Generated
    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    @Generated
    public void setStartSetPitch(boolean startSetPitch) {
        this.startSetPitch = startSetPitch;
    }

    @Generated
    public StopWatch getStopWatch() {
        return this.stopWatch;
    }

    @Generated
    public SelectSetting getMode() {
        return this.mode;
    }

    @Generated
    public int getCooldown() {
        return this.cooldown;
    }

    @Generated
    public boolean isStartSetPitch() {
        return this.startSetPitch;
    }
}

