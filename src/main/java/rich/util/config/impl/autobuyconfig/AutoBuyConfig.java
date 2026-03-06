/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  lombok.Generated
 */
package rich.util.config.impl.autobuyconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;

public class AutoBuyConfig {
    private static AutoBuyConfig instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;
    private ConfigData data = new ConfigData();

    private AutoBuyConfig() {
        Path configDir = Paths.get("Rocket", "configs", "autobuy");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.configPath = configDir.resolve("autobuy.json");
        this.load();
    }

    public static AutoBuyConfig getInstance() {
        if (instance == null) {
            instance = new AutoBuyConfig();
        }
        return instance;
    }

    public void load() {
        try {
            String json;
            ConfigData loaded;
            if (Files.exists(this.configPath, new LinkOption[0]) && (loaded = (ConfigData)this.gson.fromJson(json = Files.readString((Path)this.configPath), ConfigData.class)) != null) {
                this.data = loaded;
                if (this.data.getItems() == null) {
                    this.data.setItems(new HashMap<String, ItemConfig>());
                }
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void save() {
        try {
            String json = this.gson.toJson((Object)this.data);
            Files.writeString((Path)this.configPath, (CharSequence)json, (OpenOption[])new OpenOption[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void reset() {
        this.data = new ConfigData();
        try {
            if (Files.exists(this.configPath, new LinkOption[0])) {
                Files.delete(this.configPath);
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.save();
    }

    public boolean isGlobalEnabled() {
        return this.data.isGlobalEnabled();
    }

    public void setGlobalEnabled(boolean enabled) {
        this.data.setGlobalEnabled(enabled);
    }

    public void setGlobalEnabledAndSave(boolean enabled) {
        this.data.setGlobalEnabled(enabled);
        this.save();
    }

    public ItemConfig getItemConfig(String itemName) {
        return this.data.getItems().computeIfAbsent(itemName, k -> new ItemConfig());
    }

    public ItemConfig getItemConfigOrNull(String itemName) {
        return this.data.getItems().get(itemName);
    }

    public void setItemConfig(String itemName, ItemConfig config) {
        this.data.getItems().put(itemName, config);
    }

    public void setItemConfigAndSave(String itemName, ItemConfig config) {
        this.data.getItems().put(itemName, config);
        this.save();
    }

    public void setItemEnabled(String itemName, boolean enabled) {
        ItemConfig config = this.getItemConfig(itemName);
        config.setEnabled(enabled);
    }

    public void setItemEnabledAndSave(String itemName, boolean enabled) {
        ItemConfig config = this.getItemConfig(itemName);
        config.setEnabled(enabled);
        this.save();
    }

    public void setItemBuyBelow(String itemName, int buyBelow) {
        ItemConfig config = this.getItemConfig(itemName);
        config.setBuyBelow(buyBelow);
    }

    public void setItemBuyBelowAndSave(String itemName, int buyBelow) {
        ItemConfig config = this.getItemConfig(itemName);
        config.setBuyBelow(buyBelow);
        this.save();
    }

    public void setItemMinQuantity(String itemName, int minQuantity) {
        ItemConfig config = this.getItemConfig(itemName);
        config.setMinQuantity(minQuantity);
    }

    public void setItemMinQuantityAndSave(String itemName, int minQuantity) {
        ItemConfig config = this.getItemConfig(itemName);
        config.setMinQuantity(minQuantity);
        this.save();
    }

    public boolean isItemEnabled(String itemName) {
        ItemConfig config = this.getItemConfigOrNull(itemName);
        return config != null && config.isEnabled();
    }

    public int getItemBuyBelow(String itemName) {
        return this.getItemConfig(itemName).getBuyBelow();
    }

    public int getItemMinQuantity(String itemName) {
        return this.getItemConfig(itemName).getMinQuantity();
    }

    public boolean hasItemConfig(String itemName) {
        return this.data.getItems().containsKey(itemName);
    }

    public void loadItemSettings(String itemName, int defaultPrice) {
        if (!this.hasItemConfig(itemName)) {
            ItemConfig config = new ItemConfig(false, defaultPrice, 1);
            this.data.getItems().put(itemName, config);
        }
    }

    public Map<String, ItemConfig> getAllItemConfigs() {
        return new HashMap<String, ItemConfig>(this.data.getItems());
    }

    @Generated
    public ConfigData getData() {
        return this.data;
    }

    public static class ConfigData {
        private boolean globalEnabled = false;
        private Map<String, ItemConfig> items = new HashMap<String, ItemConfig>();

        @Generated
        public boolean isGlobalEnabled() {
            return this.globalEnabled;
        }

        @Generated
        public Map<String, ItemConfig> getItems() {
            return this.items;
        }

        @Generated
        public void setGlobalEnabled(boolean globalEnabled) {
            this.globalEnabled = globalEnabled;
        }

        @Generated
        public void setItems(Map<String, ItemConfig> items) {
            this.items = items;
        }
    }

    public static class ItemConfig {
        private boolean enabled = false;
        private int buyBelow = 1000;
        private int minQuantity = 1;

        public ItemConfig() {
        }

        public ItemConfig(boolean enabled, int buyBelow, int minQuantity) {
            this.enabled = enabled;
            this.buyBelow = buyBelow;
            this.minQuantity = minQuantity;
        }

        @Generated
        public boolean isEnabled() {
            return this.enabled;
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
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        @Generated
        public void setBuyBelow(int buyBelow) {
            this.buyBelow = buyBelow;
        }

        @Generated
        public void setMinQuantity(int minQuantity) {
            this.minQuantity = minQuantity;
        }
    }
}

