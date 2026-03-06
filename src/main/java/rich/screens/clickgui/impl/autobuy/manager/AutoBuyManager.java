
package rich.screens.clickgui.impl.autobuy.manager;

import java.util.ArrayList;
import java.util.List;
import rich.modules.impl.misc.AutoBuy;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.ItemRegistry;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;
import rich.util.string.chat.ChatMessage;

public class AutoBuyManager {
    private static AutoBuyManager instance;

    private AutoBuyManager() {
    }

    public static AutoBuyManager getInstance() {
        if (instance == null) {
            instance = new AutoBuyManager();
        }
        return instance;
    }

    public void setEnabled(boolean enabled) {
        boolean wasEnabled = AutoBuyConfig.getInstance().isGlobalEnabled();
        AutoBuyConfig.getInstance().setGlobalEnabled(enabled);
        if (wasEnabled != enabled) {
            if (enabled) {
                ChatMessage.autobuymessageSuccess("Global autobuy enabled");
            } else {
                ChatMessage.autobuymessageWarning("Global autobuy disabled");
            }
            AutoBuy autoBuy = AutoBuy.getInstance();
            if (autoBuy != null && autoBuy.isState()) {
                autoBuy.sendPauseSync(!enabled);
            }
        }
    }

    public void setEnabledSilent(boolean enabled) {
        AutoBuyConfig.getInstance().setGlobalEnabled(enabled);
    }

    public boolean isEnabled() {
        return AutoBuyConfig.getInstance().isGlobalEnabled();
    }

    public List<AutoBuyableItem> getAllItems() {
        return ItemRegistry.getAllItems();
    }

    public List<AutoBuyableItem> getEnabledItems() {
        ArrayList<AutoBuyableItem> enabled = new ArrayList<AutoBuyableItem>();
        for (AutoBuyableItem item : this.getAllItems()) {
            if (!item.isEnabled()) continue;
            enabled.add(item);
        }
        return enabled;
    }

    public int getEnabledCount() {
        int count = 0;
        for (AutoBuyableItem item : this.getAllItems()) {
            if (!item.isEnabled()) continue;
            ++count;
        }
        return count;
    }

    public void toggleItem(AutoBuyableItem item) {
        item.setEnabled(!item.isEnabled());
        ItemRegistry.saveItemSettings(item);
    }

    public void enableAll() {
        for (AutoBuyableItem item : this.getAllItems()) {
            item.setEnabled(true);
            ItemRegistry.saveItemSettings(item);
        }
    }

    public void disableAll() {
        for (AutoBuyableItem item : this.getAllItems()) {
            item.setEnabled(false);
            ItemRegistry.saveItemSettings(item);
        }
    }
}

