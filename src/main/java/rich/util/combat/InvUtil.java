package rich.util.combat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import rich.IMinecraft;

public final class InvUtil implements IMinecraft {

    public static int findInHotbar(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).getItem() == item) return i;
        }
        return -1;
    }

    public static InteractionHand getHand(Item item) {
        if (mc.player == null) return null;
        if (mc.player.getMainHandItem().getItem() == item) return InteractionHand.MAIN_HAND;
        if (mc.player.getOffhandItem().getItem() == item) return InteractionHand.OFF_HAND;
        return null;
    }

    public static boolean isHolding(Item item) {
        return getHand(item) != null;
    }

    public static boolean swap(int slot) {
        if (mc.player == null || slot < 0 || slot > 8) return false;
        mc.player.getInventory().setSelectedSlot(slot);
        return true;
    }

    private InvUtil() {}
}
