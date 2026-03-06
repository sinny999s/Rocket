
package rich.screens.clickgui.impl.autobuy;

import net.minecraft.world.item.ItemStack;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;

public interface AutoBuyableItem {
    public String getDisplayName();

    public ItemStack createItemStack();

    public int getPrice();

    public boolean isEnabled();

    public void setEnabled(boolean var1);

    public AutoBuyItemSettings getSettings();
}

