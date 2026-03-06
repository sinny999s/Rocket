
package rich.util.inventory;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemSearcher {
    public boolean matches(ItemStack var1);
}

