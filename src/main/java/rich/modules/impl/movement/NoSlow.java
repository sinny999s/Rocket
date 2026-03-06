
package rich.modules.impl.movement;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.events.impl.UsingItemEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.Instance;
import rich.util.inventory.script.Script;
import rich.util.string.PlayerInteractionHelper;
import rich.util.timer.StopWatch;

public class NoSlow
extends ModuleStructure {
    private final StopWatch notifWatch = new StopWatch();
    private final Script script = new Script();
    private boolean finish;
    public final SelectSetting itemMode = new SelectSetting("Item mode", "Select bypass mode").value("Grim Old", "ReallyWorld", "SpookyTime", "Funtime");
    private int ticks = 0;
    private int cycleCounter = 0;

    public static NoSlow getInstance() {
        return Instance.get(NoSlow.class);
    }

    public NoSlow() {
        super("NoSlow", "No Slow", ModuleCategory.MOVEMENT);
        this.settings(this.itemMode);
    }

    private boolean isOnSnowOrCarpet() {
        if (NoSlow.mc.player == null || NoSlow.mc.level == null) {
            return false;
        }
        BlockPos playerPos = NoSlow.mc.player.blockPosition();
        Block block = NoSlow.mc.level.getBlockState(playerPos).getBlock();
        return block == Blocks.SNOW || block == Blocks.WHITE_CARPET || block == Blocks.ORANGE_CARPET || block == Blocks.MAGENTA_CARPET || block == Blocks.LIGHT_BLUE_CARPET || block == Blocks.YELLOW_CARPET || block == Blocks.LIME_CARPET || block == Blocks.PINK_CARPET || block == Blocks.GRAY_CARPET || block == Blocks.LIGHT_GRAY_CARPET || block == Blocks.CYAN_CARPET || block == Blocks.PURPLE_CARPET || block == Blocks.BLUE_CARPET || block == Blocks.BROWN_CARPET || block == Blocks.GREEN_CARPET || block == Blocks.RED_CARPET || block == Blocks.BLACK_CARPET;
    }

    @EventHandler
    public void onUpdate(TickEvent event) {
        if (NoSlow.mc.player == null) {
            return;
        }
        if (this.itemMode.isSelected("ReallyWorld") || this.itemMode.isSelected("SpookyTime")) {
            if (!NoSlow.mc.player.isAutoSpinAttack()) {
                if (NoSlow.mc.player.isUsingItem()) {
                    ++this.ticks;
                } else {
                    this.ticks = 0;
                    this.cycleCounter = 0;
                }
            }
        } else {
            this.ticks = NoSlow.mc.player.getUsedItemHand() == InteractionHand.MAIN_HAND || NoSlow.mc.player.getUsedItemHand() == InteractionHand.OFF_HAND ? ++this.ticks : 0;
        }
    }

    @EventHandler
    public void onUsingItem(UsingItemEvent e) {
        if (NoSlow.mc.player == null) {
            return;
        }
        InteractionHand first = NoSlow.mc.player.getUsedItemHand();
        InteractionHand second = first.equals((Object)InteractionHand.MAIN_HAND) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        switch (e.getType()) {
            case 1: {
                this.handleItemUse(e, first, second);
                break;
            }
            case 2: {
                while (!this.script.isFinished()) {
                    this.script.update();
                }
                break;
            }
        }
    }

    private void handleItemUse(UsingItemEvent e, InteractionHand first, InteractionHand second) {
        switch (this.itemMode.getSelected()) {
            case "Grim Old": {
                if (!NoSlow.mc.player.getOffhandItem().getUseAnimation().equals(ItemUseAnimation.NONE) && !NoSlow.mc.player.getMainHandItem().getUseAnimation().equals(ItemUseAnimation.NONE)) break;
                PlayerInteractionHelper.interactItem(first);
                PlayerInteractionHelper.interactItem(second);
                e.cancel();
                break;
            }
            case "ReallyWorld": {
                int[] thresholds = NoSlow.mc.player.isJumping() ? new int[]{2, 2, 2} : new int[]{2, 3, 3};
                int threshold = thresholds[this.cycleCounter % thresholds.length];
                if (this.ticks < threshold) break;
                e.cancel();
                this.ticks = 0;
                ++this.cycleCounter;
                break;
            }
            case "SpookyTime": {
                int[] thresholds = new int[]{2, 2, 2};
                int threshold = thresholds[this.cycleCounter % 2];
                if (this.ticks < threshold) break;
                e.cancel();
                this.ticks = 0;
                ++this.cycleCounter;
                break;
            }
            case "Funtime": {
                if (!((float)this.ticks > 0.0f) || !((float)NoSlow.mc.player.getTicksUsingItem() > 1.0f)) break;
                boolean mainHandCrossbow = NoSlow.mc.player.getMainHandItem().getItem() instanceof CrossbowItem;
                boolean offHandCrossbow = NoSlow.mc.player.getOffhandItem().getItem() instanceof CrossbowItem;
                if (mainHandCrossbow || offHandCrossbow) {
                    if (!((float)this.ticks > 0.0f) || NoSlow.mc.player.getTicksUsingItem() <= 1) break;
                    e.cancel();
                    this.ticks = 0;
                    break;
                }
                if (!NoSlow.mc.player.onGround() || !this.isOnSnowOrCarpet()) break;
                NoSlow.mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, NoSlow.mc.player.blockPosition().above(), Direction.DOWN));
                NoSlow.mc.player.setDeltaMovement(NoSlow.mc.player.getDeltaMovement().x, NoSlow.mc.player.getDeltaMovement().y, NoSlow.mc.player.getDeltaMovement().z);
                e.cancel();
                this.ticks = 0;
            }
        }
    }
}

