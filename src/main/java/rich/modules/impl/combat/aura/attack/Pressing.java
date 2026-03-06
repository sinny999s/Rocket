
package rich.modules.impl.combat.aura.attack;

import net.minecraft.world.item.ItemStack;
import rich.IMinecraft;

public class Pressing
implements IMinecraft {
    private long lastClickTime = System.currentTimeMillis();

    public boolean isCooldownComplete(int ticks) {
        if (Pressing.mc.player == null) {
            return false;
        }
        if (this.isHoldingMace()) {
            return this.lastClickPassed() >= 50L;
        }
        float cooldownProgress = Pressing.mc.player.getAttackStrengthScale(ticks);
        return cooldownProgress >= 0.95f;
    }

    public boolean isMaceFastAttack() {
        return this.isHoldingMace() && this.lastClickPassed() >= 50L;
    }

    public long lastClickPassed() {
        return System.currentTimeMillis() - this.lastClickTime;
    }

    public void recalculate() {
        this.lastClickTime = System.currentTimeMillis();
    }

    public boolean isHoldingMace() {
        if (Pressing.mc.player == null) {
            return false;
        }
        ItemStack mainHand = Pressing.mc.player.getMainHandItem();
        return mainHand.getItem().getDescriptionId().toLowerCase().contains("mace");
    }

    public boolean isWeapon() {
        if (Pressing.mc.player == null) {
            return false;
        }
        ItemStack mainHand = Pressing.mc.player.getMainHandItem();
        if (mainHand.isEmpty()) {
            return false;
        }
        String itemName = mainHand.getItem().getDescriptionId().toLowerCase();
        return itemName.contains("sword") || itemName.contains("axe") || itemName.contains("trident");
    }
}

