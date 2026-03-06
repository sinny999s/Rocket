
package rich.modules.impl.combat;

import java.util.List;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.EntitySpawnEvent;
import rich.events.impl.InputEvent;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.inventory.InventoryUtils;
import rich.util.inventory.SwapExecutor;
import rich.util.inventory.SwapSettings;
import rich.util.repository.friend.FriendUtils;
import rich.util.string.PlayerInteractionHelper;

public class AutoCrystal
extends ModuleStructure {
    private final MultiSelectSetting protections = new MultiSelectSetting("Protection", "What not to explode").value("Self", "Friends", "Resources").selected("Self", "Friends", "Resources");
    private final SliderSettings itemRange = new SliderSettings("Distance to resources", "Minimum distance to resources").range(1.0f, 12.0f).setValue(6.0f);
    private final BooleanSetting legitMode = new BooleanSetting("Legit mode", "Stop movement before swap").setValue(false);
    private final SliderSettings swapDelay = new SliderSettings("Delay swap", "Ms before crystal swap").range(0, 200).setValue(50.0f).visible(() -> this.legitMode.isValue());
    private final SwapExecutor swapExecutor = new SwapExecutor();
    private BlockPos obsPosition;
    private boolean waitingForCrystal;
    private int resetTicks = 0;

    public AutoCrystal() {
        super("AutoCrystal", "Auto Crystal", ModuleCategory.COMBAT);
        this.settings(this.protections, this.itemRange, this.legitMode, this.swapDelay);
    }

    @Override
    public void activate() {
        this.obsPosition = null;
        this.waitingForCrystal = false;
    }

    @Override
    public void deactivate() {
        this.swapExecutor.cancel();
        this.obsPosition = null;
        this.waitingForCrystal = false;
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        ServerboundUseItemOnPacket interact;
        if (AutoCrystal.mc.player == null || AutoCrystal.mc.level == null) {
            return;
        }
        if (this.swapExecutor.isRunning() || this.waitingForCrystal) {
            return;
        }
        Packet<?> packet = e.getPacket();
        if (packet instanceof ServerboundUseItemOnPacket && (interact = (ServerboundUseItemOnPacket)packet).getSequence() != 0) {
            BlockPos interactPos = interact.getHitResult().getBlockPos();
            BlockPos spawnPos = interactPos.relative(interact.getHitResult().getDirection());
            BlockPos blockPos = null;
            if (AutoCrystal.mc.level.getBlockState(spawnPos).getBlock().equals(Blocks.OBSIDIAN)) {
                blockPos = spawnPos;
            } else if (AutoCrystal.mc.level.getBlockState(interactPos).getBlock().equals(Blocks.OBSIDIAN)) {
                blockPos = interactPos;
            }
            if (blockPos == null) {
                return;
            }
            Slot crystalSlot = this.findCrystalSlot();
            if (crystalSlot == null) {
                return;
            }
            if (!this.isSafePosition(blockPos)) {
                return;
            }
            BlockPos finalBlockPos = blockPos;
            this.obsPosition = blockPos;
            this.waitingForCrystal = true;
            SwapSettings settings = this.legitMode.isValue() ? new SwapSettings().stopMovement(true).stopSprint(true).preSwapDelay(this.swapDelay.getInt(), this.swapDelay.getInt() + 30).postSwapDelay(20, 50) : SwapSettings.instant();
            this.swapExecutor.execute(() -> this.placeCrystal(crystalSlot, finalBlockPos), settings, () -> this.scheduleReset());
        }
    }

    private void placeCrystal(Slot crystalSlot, BlockPos blockPos) {
        if (crystalSlot == null || blockPos == null) {
            return;
        }
        int currentSlot = InventoryUtils.currentSlot();
        if (crystalSlot.index >= 36 && crystalSlot.index <= 44) {
            int hotbarSlot = crystalSlot.index - 36;
            InventoryUtils.selectSlot(hotbarSlot);
            PlayerInteractionHelper.sendSequencedPacket(i -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(blockPos.getCenter(), Direction.UP, blockPos, false), i));
            InventoryUtils.selectSlot(currentSlot);
        } else {
            InventoryUtils.swapHotbar(crystalSlot.index, currentSlot);
            PlayerInteractionHelper.sendSequencedPacket(i -> new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, new BlockHitResult(blockPos.getCenter(), Direction.UP, blockPos, false), i));
            InventoryUtils.swapHotbar(crystalSlot.index, currentSlot);
            InventoryUtils.closeScreen();
        }
    }

    private void scheduleReset() {
        this.resetTicks = 6;
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent e) {
        if (AutoCrystal.mc.player == null || AutoCrystal.mc.level == null) {
            return;
        }
        Entity entity = e.getEntity();
        if (entity instanceof EndCrystal) {
            EndCrystal crystal = (EndCrystal)entity;
            if (this.obsPosition != null && this.obsPosition.equals(crystal.blockPosition().below())) {
                if (this.isSafeToDamage(crystal)) {
                    AutoCrystal.mc.gameMode.attack(AutoCrystal.mc.player, crystal);
                }
                this.resetState();
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AutoCrystal.mc.player == null || AutoCrystal.mc.level == null) {
            return;
        }
        this.swapExecutor.tick();
        if (this.resetTicks > 0) {
            --this.resetTicks;
            if (this.resetTicks == 0) {
                this.resetState();
            }
        }
    }

    @EventHandler
    public void onInput(InputEvent e) {
        if (this.swapExecutor.isBlocking()) {
            e.setDirectionalLow(false, false, false, false);
            e.setJumping(false);
        }
    }

    private void resetState() {
        this.obsPosition = null;
        this.waitingForCrystal = false;
        this.resetTicks = 0;
    }

    private Slot findCrystalSlot() {
        return InventoryUtils.findSlot(Items.END_CRYSTAL);
    }

    private boolean isSafePosition(BlockPos pos) {
        if (this.protections.isSelected("Self") && AutoCrystal.mc.player.getY() > (double)pos.getY()) {
            return false;
        }
        if (this.protections.isSelected("Friends")) {
            for (Player player : AutoCrystal.mc.level.players()) {
                if (player == AutoCrystal.mc.player || !FriendUtils.isFriend(player) || !(player.getY() > (double)pos.getY())) continue;
                return false;
            }
        }
        if (this.protections.isSelected("Resources")) {
            Vec3 crystalPos = pos.above().getCenter();
            double range = this.itemRange.getValue();
            AABB box = new AABB(crystalPos.x - range, crystalPos.y - range, crystalPos.z - range, crystalPos.x + range, crystalPos.y + range, crystalPos.z + range);
            List<Entity> entities = AutoCrystal.mc.level.getEntities(AutoCrystal.mc.player, box);
            for (Object _obj : entities) { Entity entity = (Entity) _obj; // {
                if (!(entity instanceof ItemEntity)) continue;
                return false;
            }
        }
        return true;
    }

    private boolean isSafeToDamage(EndCrystal crystal) {
        BlockPos crystalBlock = crystal.blockPosition().below();
        if (this.protections.isSelected("Self") && AutoCrystal.mc.player.getY() > (double)crystalBlock.getY()) {
            return false;
        }
        if (this.protections.isSelected("Friends")) {
            for (Player player : AutoCrystal.mc.level.players()) {
                if (player == AutoCrystal.mc.player || !FriendUtils.isFriend(player) || !(player.getY() > (double)crystalBlock.getY())) continue;
                return false;
            }
        }
        if (this.protections.isSelected("Resources")) {
            Vec3 crystalPos = crystal.position();
            double range = this.itemRange.getValue();
            AABB box = new AABB(crystalPos.x - range, crystalPos.y - range, crystalPos.z - range, crystalPos.x + range, crystalPos.y + range, crystalPos.z + range);
            List<Entity> entities = AutoCrystal.mc.level.getEntities(AutoCrystal.mc.player, box);
            for (Object _obj : entities) { Entity entity = (Entity) _obj; // {
                if (!(entity instanceof ItemEntity)) continue;
                return false;
            }
        }
        return true;
    }

    @Generated
    public MultiSelectSetting getProtections() {
        return this.protections;
    }

    @Generated
    public SliderSettings getItemRange() {
        return this.itemRange;
    }

    @Generated
    public BooleanSetting getLegitMode() {
        return this.legitMode;
    }

    @Generated
    public SliderSettings getSwapDelay() {
        return this.swapDelay;
    }

    @Generated
    public SwapExecutor getSwapExecutor() {
        return this.swapExecutor;
    }

    @Generated
    public BlockPos getObsPosition() {
        return this.obsPosition;
    }

    @Generated
    public boolean isWaitingForCrystal() {
        return this.waitingForCrystal;
    }

    @Generated
    public int getResetTicks() {
        return this.resetTicks;
    }
}

