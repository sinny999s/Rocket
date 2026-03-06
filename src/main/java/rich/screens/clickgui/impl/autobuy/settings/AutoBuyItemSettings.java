
package rich.screens.clickgui.impl.autobuy.settings;

import lombok.Generated;
import net.minecraft.world.item.Item;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class AutoBuyItemSettings {
    private int buyBelow;
    private int minQuantity = 1;
    private boolean canHaveQuantity = false;
    private Item material;
    private String displayName;

    public AutoBuyItemSettings(int defaultPrice, Item material, String displayName) {
        this.buyBelow = defaultPrice;
        this.material = material;
        this.displayName = displayName;
        this.loadFromConfig();
    }

    public AutoBuyItemSettings(int defaultPrice, Item material, String displayName, boolean canHaveQuantity) {
        this.buyBelow = defaultPrice;
        this.material = material;
        this.displayName = displayName;
        this.canHaveQuantity = canHaveQuantity;
        this.loadFromConfig();
    }

    private void loadFromConfig() {
        AutoBuyConfig config = AutoBuyConfig.getInstance();
        if (config.hasItemConfig(this.displayName)) {
            AutoBuyConfig.ItemConfig itemConfig = config.getItemConfig(this.displayName);
            this.buyBelow = itemConfig.getBuyBelow();
            this.minQuantity = itemConfig.getMinQuantity();
        } else {
            config.loadItemSettings(this.displayName, this.buyBelow);
        }
    }

    public void saveToConfig() {
        AutoBuyConfig config = AutoBuyConfig.getInstance();
        config.setItemBuyBelow(this.displayName, this.buyBelow);
        config.setItemMinQuantity(this.displayName, this.minQuantity);
    }

    @Generated
    public int getBuyBelow() {
        return this.buyBelow;
    }

    @Generated
    public int getMinQuantity() {
        return this.minQuantity;
    }

    @Generated
    public boolean isCanHaveQuantity() {
        return this.canHaveQuantity;
    }

    @Generated
    public Item getMaterial() {
        return this.material;
    }

    @Generated
    public String getDisplayName() {
        return this.displayName;
    }

    @Generated
    public void setBuyBelow(int buyBelow) {
        this.buyBelow = buyBelow;
    }

    @Generated
    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    @Generated
    public void setCanHaveQuantity(boolean canHaveQuantity) {
        this.canHaveQuantity = canHaveQuantity;
    }

    @Generated
    public void setMaterial(Item material) {
        this.material = material;
    }

    @Generated
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}

