
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BowItem;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;

public class BowSpammer
extends ModuleStructure {
    private final SliderSettings delay = new SliderSettings("Delay", "Delay between shots").range(2.2f, 5.0f).setValue(2.5f);

    public BowSpammer() {
        super("BowSpammer", "Bow Spammer", ModuleCategory.COMBAT);
        this.settings(this.delay);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (BowSpammer.mc.player == null || BowSpammer.mc.level == null) {
            return;
        }
        if (mc.getConnection() == null) {
            return;
        }
        if (!this.canShoot()) {
            return;
        }
        this.sendShootPackets();
        BowSpammer.mc.player.releaseUsingItem();
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean canShoot() {
        if (!(BowSpammer.mc.player.getMainHandItem().getItem() instanceof BowItem)) {
            return false;
        }
        if (!BowSpammer.mc.player.isUsingItem()) {
            return false;
        }
        return (float)BowSpammer.mc.player.getTicksUsingItem() >= this.delay.getValue();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void sendShootPackets() {
        mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        mc.getConnection().send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, BowSpammer.mc.player.getYRot(), BowSpammer.mc.player.getXRot()));
    }

    @Generated
    public SliderSettings getDelay() {
        return this.delay;
    }
}

