
package rich.screens.clickgui.impl.autobuy.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class KrushItem
implements AutoBuyableItem {
    private final String displayName;
    private final Item material;
    private final ItemStack displayStack;
    private final int defaultPrice;
    private final AutoBuyItemSettings settings;
    private final boolean isKrushItem;
    private boolean enabled;

    public KrushItem(String displayName, Item material, ItemStack displayStack, int defaultPrice) {
        this(displayName, material, displayStack, defaultPrice, true, false);
    }

    public KrushItem(String displayName, Item material, ItemStack displayStack, int defaultPrice, boolean canHaveQuantity) {
        this(displayName, material, displayStack, defaultPrice, canHaveQuantity, false);
    }

    public KrushItem(String displayName, Item material, ItemStack displayStack, int defaultPrice, boolean canHaveQuantity, boolean isKrushItem) {
        this.displayName = displayName;
        this.material = material;
        this.displayStack = displayStack;
        this.defaultPrice = defaultPrice;
        this.isKrushItem = isKrushItem;
        this.settings = new AutoBuyItemSettings(defaultPrice, material, displayName, canHaveQuantity);
        AutoBuyConfig config = AutoBuyConfig.getInstance();
        if (config.hasItemConfig(displayName)) {
            this.enabled = config.isItemEnabled(displayName);
        } else {
            this.enabled = true;
            config.loadItemSettings(displayName, defaultPrice);
        }
    }

    private boolean shouldHaveGlint() {
        if (!this.isKrushItem) {
            return false;
        }
        return this.material == Items.TOTEM_OF_UNDYING || this.material == Items.NETHERITE_HELMET || this.material == Items.NETHERITE_CHESTPLATE || this.material == Items.NETHERITE_LEGGINGS || this.material == Items.NETHERITE_BOOTS || this.material == Items.NETHERITE_SWORD || this.material == Items.NETHERITE_PICKAXE || this.material == Items.NETHERITE_AXE || this.material == Items.NETHERITE_SHOVEL || this.material == Items.NETHERITE_HOE || this.material == Items.DIAMOND_HELMET || this.material == Items.DIAMOND_CHESTPLATE || this.material == Items.DIAMOND_LEGGINGS || this.material == Items.DIAMOND_BOOTS || this.material == Items.DIAMOND_SWORD || this.material == Items.DIAMOND_PICKAXE || this.material == Items.DIAMOND_AXE || this.material == Items.DIAMOND_SHOVEL || this.material == Items.DIAMOND_HOE || this.material == Items.IRON_HELMET || this.material == Items.IRON_CHESTPLATE || this.material == Items.IRON_LEGGINGS || this.material == Items.IRON_BOOTS || this.material == Items.IRON_SWORD || this.material == Items.IRON_PICKAXE || this.material == Items.IRON_AXE || this.material == Items.IRON_SHOVEL || this.material == Items.IRON_HOE || this.material == Items.GOLDEN_HELMET || this.material == Items.GOLDEN_CHESTPLATE || this.material == Items.GOLDEN_LEGGINGS || this.material == Items.GOLDEN_BOOTS || this.material == Items.GOLDEN_SWORD || this.material == Items.GOLDEN_PICKAXE || this.material == Items.GOLDEN_AXE || this.material == Items.GOLDEN_SHOVEL || this.material == Items.GOLDEN_HOE || this.material == Items.BOW || this.material == Items.CROSSBOW || this.material == Items.TRIDENT || this.material == Items.MACE || this.material == Items.ELYTRA || this.material == Items.SHIELD || this.material == Items.FISHING_ROD;
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public ItemStack createItemStack() {
        ItemStack copy = this.displayStack.copy();
        if (this.shouldHaveGlint()) {
            copy.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        return copy;
    }

    @Override
    public int getPrice() {
        return this.settings.getBuyBelow();
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public AutoBuyItemSettings getSettings() {
        return this.settings;
    }
}

