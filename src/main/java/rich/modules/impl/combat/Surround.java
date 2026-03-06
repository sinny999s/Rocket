package rich.modules.impl.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.combat.BlockUtil;
import rich.util.combat.InvUtil;

public class Surround extends ModuleStructure {

    private final SelectSetting centerMode = new SelectSetting("Center", "Center player on block").value("Never", "On Enable", "Always").selected("On Enable");
    private final BooleanSetting doubleHeight = new BooleanSetting("Double Height", "Place blocks 2 high to prevent face place").setValue(false);
    private final BooleanSetting onlyOnGround = new BooleanSetting("Only Ground", "Only works when on ground").setValue(true);
    private final BooleanSetting rotate = new BooleanSetting("Rotate", "Rotate towards placed blocks").setValue(true);
    private final BooleanSetting protect = new BooleanSetting("Protect", "Break crystals near surround positions").setValue(true);
    private final BooleanSetting disableOnYChange = new BooleanSetting("Disable Y Change", "Disable when Y level changes").setValue(true);
    private final BooleanSetting disableOnComplete = new BooleanSetting("Disable Complete", "Disable when all blocks placed").setValue(false);
    private final SliderSettings blocksPerTick = new SliderSettings("Blocks/Tick", "How many blocks to place per tick").setValue(4.0f).range(1, 8);
    private final SliderSettings delay = new SliderSettings("Delay", "Delay in ticks between placements").setValue(0.0f).range(0, 5);

    private int timer = 0;
    private double startY = 0;

    public Surround() {
        super("Surround", "Surround", ModuleCategory.COMBAT);
        this.settings(this.centerMode, this.doubleHeight, this.onlyOnGround, this.rotate, this.protect, this.disableOnYChange, this.disableOnComplete, this.blocksPerTick, this.delay);
    }

    @Override
    public void activate() {
        if (mc.player == null) return;
        startY = mc.player.getY();
        timer = (int) delay.getValue();

        if (centerMode.isSelected("On Enable")) {
            BlockUtil.centerPlayer();
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) return;

        if (disableOnYChange.isValue() && mc.player.getY() != startY) {
            this.setState(false);
            return;
        }

        if (onlyOnGround.isValue() && !mc.player.onGround()) return;

        if (timer > 0) {
            timer--;
            return;
        }

        if (centerMode.isSelected("Always")) {
            BlockUtil.centerPlayer();
        }

        int slot = InvUtil.findInHotbar(Items.OBSIDIAN);
        InteractionHand hand = InvUtil.getHand(Items.OBSIDIAN);
        if (slot == -1 && hand == null) {
            slot = InvUtil.findInHotbar(Items.CRYING_OBSIDIAN);
            hand = InvUtil.getHand(Items.CRYING_OBSIDIAN);
        }
        if (slot == -1 && hand == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        Direction[] horizontal = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};

        int placed = 0;
        boolean complete = true;

        for (Direction dir : horizontal) {
            BlockPos placePos = playerPos.relative(dir);

            if (mc.level.getBlockState(placePos).canBeReplaced()) {
                complete = false;

                if (protect.isValue()) {
                    breakNearbyCrystals(placePos);
                }

                if (BlockUtil.place(placePos, Items.OBSIDIAN, rotate.isValue(), true)) {
                    placed++;
                    if (placed >= (int) blocksPerTick.getValue()) break;
                }
            }
        }

        if (doubleHeight.isValue() && placed < (int) blocksPerTick.getValue()) {
            for (Direction dir : horizontal) {
                BlockPos placePos = playerPos.relative(dir).above();

                if (mc.level.getBlockState(placePos).canBeReplaced()) {
                    complete = false;

                    if (BlockUtil.place(placePos, Items.OBSIDIAN, rotate.isValue(), true)) {
                        placed++;
                        if (placed >= (int) blocksPerTick.getValue()) break;
                    }
                }
            }
        }

        timer = (int) delay.getValue();

        if (complete && disableOnComplete.isValue()) {
            this.setState(false);
        }
    }

    private void breakNearbyCrystals(BlockPos pos) {
        for (Entity entity : mc.level.getEntities(null, new net.minecraft.world.phys.AABB(
            pos.getX() - 1, pos.getY() - 1, pos.getZ() - 1,
            pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2
        ))) {
            if (!(entity instanceof EndCrystal crystal)) continue;
            if (!crystal.isAlive()) continue;

            mc.player.connection.send(ServerboundInteractPacket.createAttackPacket(crystal, mc.player.isShiftKeyDown()));
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }
}
