
package rich.modules.impl.combat.macetarget.attack;

import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import rich.util.inventory.InventoryUtils;

public class AttackHandler {
    private static final Minecraft mc = Minecraft.getInstance();
    private boolean pendingAttack = false;
    private boolean shouldDisableAfterAttack = false;

    public void performAttack(LivingEntity target) {
        if (AttackHandler.mc.player == null || target == null) {
            return;
        }
        int maceSlot = InventoryUtils.findHotbarItem(Items.MACE);
        int prevSlot = AttackHandler.mc.player.getInventory().getSelectedSlot();
        if (maceSlot != -1 && maceSlot != prevSlot) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(maceSlot));
        }
        AttackHandler.mc.gameMode.attack(AttackHandler.mc.player, target);
        AttackHandler.mc.player.swing(InteractionHand.MAIN_HAND);
        if (maceSlot != -1 && maceSlot != prevSlot) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(prevSlot));
        }
    }

    public void reset() {
        this.pendingAttack = false;
        this.shouldDisableAfterAttack = false;
    }

    @Generated
    public boolean isPendingAttack() {
        return this.pendingAttack;
    }

    @Generated
    public boolean isShouldDisableAfterAttack() {
        return this.shouldDisableAfterAttack;
    }

    @Generated
    public void setPendingAttack(boolean pendingAttack) {
        this.pendingAttack = pendingAttack;
    }

    @Generated
    public void setShouldDisableAfterAttack(boolean shouldDisableAfterAttack) {
        this.shouldDisableAfterAttack = shouldDisableAfterAttack;
    }
}

