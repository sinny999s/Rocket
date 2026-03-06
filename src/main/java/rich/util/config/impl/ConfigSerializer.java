/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  com.google.gson.JsonPrimitive
 *  com.google.gson.JsonSyntaxException
 */
package rich.util.config.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Map;
import rich.Initialization;
import rich.modules.module.ModuleRepository;
import rich.modules.module.ModuleStructure;
import rich.modules.module.setting.Setting;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.GroupSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;
import rich.util.config.impl.consolelogger.Logger;

public class ConfigSerializer {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public String serialize() {
        JsonObject root = new JsonObject();
        JsonObject modulesJson = new JsonObject();
        ModuleRepository repository = this.getModuleRepository();
        if (repository != null) {
            for (ModuleStructure module : repository.modules()) {
                JsonObject moduleJson = this.serializeModule(module);
                modulesJson.add(module.getName(), (JsonElement)moduleJson);
            }
        }
        root.add("modules", (JsonElement)modulesJson);
        root.add("autobuy", (JsonElement)this.serializeAutoBuy());
        root.addProperty("version", "2.0");
        root.addProperty("timestamp", (Number)System.currentTimeMillis());
        root.addProperty("client", "Rocket");
        return GSON.toJson((JsonElement)root);
    }

    private JsonObject serializeAutoBuy() {
        JsonObject autoBuyJson = new JsonObject();
        AutoBuyConfig autoBuyConfig = AutoBuyConfig.getInstance();
        autoBuyJson.addProperty("globalEnabled", Boolean.valueOf(autoBuyConfig.isGlobalEnabled()));
        JsonObject itemsJson = new JsonObject();
        Map<String, AutoBuyConfig.ItemConfig> allItems = autoBuyConfig.getAllItemConfigs();
        for (Map.Entry<String, AutoBuyConfig.ItemConfig> entry : allItems.entrySet()) {
            JsonObject itemJson = new JsonObject();
            AutoBuyConfig.ItemConfig itemConfig = entry.getValue();
            itemJson.addProperty("enabled", Boolean.valueOf(itemConfig.isEnabled()));
            itemJson.addProperty("buyBelow", (Number)itemConfig.getBuyBelow());
            itemJson.addProperty("minQuantity", (Number)itemConfig.getMinQuantity());
            itemsJson.add(entry.getKey(), (JsonElement)itemJson);
        }
        autoBuyJson.add("items", (JsonElement)itemsJson);
        return autoBuyJson;
    }

    private JsonObject serializeModule(ModuleStructure module) {
        JsonObject moduleJson = new JsonObject();
        moduleJson.addProperty("enabled", Boolean.valueOf(module.isState()));
        moduleJson.addProperty("key", (Number)module.getKey());
        moduleJson.addProperty("type", (Number)module.getType());
        moduleJson.addProperty("favorite", Boolean.valueOf(module.isFavorite()));
        JsonObject settingsJson = new JsonObject();
        for (Setting setting : module.settings()) {
            JsonElement element = this.serializeSetting(setting);
            if (element == null) continue;
            settingsJson.add(setting.getName(), element);
        }
        moduleJson.add("settings", (JsonElement)settingsJson);
        return moduleJson;
    }

    private JsonElement serializeSetting(Setting setting) {
        if (setting instanceof BooleanSetting) {
            BooleanSetting boolSetting = (BooleanSetting)setting;
            return new JsonPrimitive(Boolean.valueOf(boolSetting.isValue()));
        }
        if (setting instanceof SliderSettings) {
            SliderSettings sliderSetting = (SliderSettings)setting;
            return new JsonPrimitive((Number)Float.valueOf(sliderSetting.getValue()));
        }
        if (setting instanceof BindSetting) {
            BindSetting bindSetting = (BindSetting)setting;
            JsonObject bindJson = new JsonObject();
            bindJson.addProperty("key", (Number)bindSetting.getKey());
            bindJson.addProperty("type", (Number)bindSetting.getType());
            return bindJson;
        }
        if (setting instanceof TextSetting) {
            TextSetting textSetting = (TextSetting)setting;
            return new JsonPrimitive(textSetting.getText() != null ? textSetting.getText() : "");
        }
        if (setting instanceof SelectSetting) {
            SelectSetting selectSetting = (SelectSetting)setting;
            return new JsonPrimitive(selectSetting.getSelected());
        }
        if (setting instanceof ColorSetting) {
            ColorSetting colorSetting = (ColorSetting)setting;
            JsonObject colorJson = new JsonObject();
            colorJson.addProperty("hue", (Number)Float.valueOf(colorSetting.getHue()));
            colorJson.addProperty("saturation", (Number)Float.valueOf(colorSetting.getSaturation()));
            colorJson.addProperty("brightness", (Number)Float.valueOf(colorSetting.getBrightness()));
            colorJson.addProperty("alpha", (Number)Float.valueOf(colorSetting.getAlpha()));
            return colorJson;
        }
        if (setting instanceof MultiSelectSetting) {
            MultiSelectSetting multiSetting = (MultiSelectSetting)setting;
            JsonArray array = new JsonArray();
            for (String value : multiSetting.getSelected()) {
                array.add(value);
            }
            return array;
        }
        if (setting instanceof GroupSetting) {
            GroupSetting groupSetting = (GroupSetting)setting;
            JsonObject groupJson = new JsonObject();
            groupJson.addProperty("value", Boolean.valueOf(groupSetting.isValue()));
            JsonObject subSettingsJson = new JsonObject();
            for (Setting subSetting : groupSetting.getSubSettings()) {
                JsonElement element = this.serializeSetting(subSetting);
                if (element == null) continue;
                subSettingsJson.add(subSetting.getName(), element);
            }
            groupJson.add("subSettings", (JsonElement)subSettingsJson);
            return groupJson;
        }
        return null;
    }

    public void deserialize(String json) {
        try {
            JsonObject root = JsonParser.parseString((String)json).getAsJsonObject();
            if (root.has("modules")) {
                JsonObject modulesJson = root.getAsJsonObject("modules");
                ModuleRepository repository = this.getModuleRepository();
                if (repository != null) {
                    for (ModuleStructure module : repository.modules()) {
                        if (!modulesJson.has(module.getName())) continue;
                        this.deserializeModule(module, modulesJson.getAsJsonObject(module.getName()));
                    }
                }
            }
            if (root.has("autobuy")) {
                this.deserializeAutoBuy(root.getAsJsonObject("autobuy"));
            }
        }
        catch (JsonSyntaxException e) {
            Logger.error("AutoConfiguration: JSON syntax error!");
        }
    }

    private void deserializeAutoBuy(JsonObject autoBuyJson) {
        AutoBuyConfig autoBuyConfig = AutoBuyConfig.getInstance();
        if (autoBuyJson.has("globalEnabled")) {
            autoBuyConfig.setGlobalEnabled(autoBuyJson.get("globalEnabled").getAsBoolean());
        }
        if (autoBuyJson.has("items")) {
            JsonObject itemsJson = autoBuyJson.getAsJsonObject("items");
            for (Map.Entry entry : itemsJson.entrySet()) {
                String itemName = (String)entry.getKey();
                JsonObject itemJson = ((JsonElement)entry.getValue()).getAsJsonObject();
                boolean enabled = itemJson.has("enabled") && itemJson.get("enabled").getAsBoolean();
                int buyBelow = itemJson.has("buyBelow") ? itemJson.get("buyBelow").getAsInt() : 1000;
                int minQuantity = itemJson.has("minQuantity") ? itemJson.get("minQuantity").getAsInt() : 1;
                AutoBuyConfig.ItemConfig itemConfig = new AutoBuyConfig.ItemConfig(enabled, buyBelow, minQuantity);
                autoBuyConfig.setItemConfig(itemName, itemConfig);
            }
        }
    }

    private void deserializeModule(ModuleStructure module, JsonObject moduleJson) {
        boolean enabled;
        if (moduleJson.has("enabled") && (enabled = moduleJson.get("enabled").getAsBoolean())) {
            module.setState(true);
        }
        if (moduleJson.has("key")) {
            module.setKey(moduleJson.get("key").getAsInt());
        }
        if (moduleJson.has("type")) {
            module.setType(moduleJson.get("type").getAsInt());
        }
        if (moduleJson.has("favorite")) {
            module.setFavorite(moduleJson.get("favorite").getAsBoolean());
        }
        if (moduleJson.has("settings")) {
            JsonObject settingsJson = moduleJson.getAsJsonObject("settings");
            for (Setting setting : module.settings()) {
                if (!settingsJson.has(setting.getName())) continue;
                this.deserializeSetting(setting, settingsJson.get(setting.getName()));
            }
        }
    }

    private void deserializeSetting(Setting setting, JsonElement element) {
        try {
            if (setting instanceof BooleanSetting) {
                BooleanSetting boolSetting = (BooleanSetting)setting;
                boolSetting.setValue(element.getAsBoolean());
            } else if (setting instanceof SliderSettings) {
                SliderSettings sliderSetting = (SliderSettings)setting;
                sliderSetting.setValue((float)element.getAsDouble());
            } else if (setting instanceof BindSetting) {
                BindSetting bindSetting = (BindSetting)setting;
                if (element.isJsonObject()) {
                    JsonObject bindJson = element.getAsJsonObject();
                    if (bindJson.has("key")) {
                        bindSetting.setKey(bindJson.get("key").getAsInt());
                    }
                    if (bindJson.has("type")) {
                        bindSetting.setType(bindJson.get("type").getAsInt());
                    }
                } else {
                    bindSetting.setKey(element.getAsInt());
                }
            } else if (setting instanceof TextSetting) {
                TextSetting textSetting = (TextSetting)setting;
                textSetting.setText(element.getAsString());
            } else if (setting instanceof SelectSetting) {
                SelectSetting selectSetting = (SelectSetting)setting;
                selectSetting.setSelected(element.getAsString());
            } else if (setting instanceof ColorSetting) {
                ColorSetting colorSetting = (ColorSetting)setting;
                if (element.isJsonObject()) {
                    JsonObject colorJson = element.getAsJsonObject();
                    if (colorJson.has("hue")) {
                        colorSetting.setHue(colorJson.get("hue").getAsFloat());
                    }
                    if (colorJson.has("saturation")) {
                        colorSetting.setSaturation(colorJson.get("saturation").getAsFloat());
                    }
                    if (colorJson.has("brightness")) {
                        colorSetting.setBrightness(colorJson.get("brightness").getAsFloat());
                    }
                    if (colorJson.has("alpha")) {
                        colorSetting.setAlpha(colorJson.get("alpha").getAsFloat());
                    }
                } else {
                    colorSetting.setColor(element.getAsInt());
                }
            } else if (setting instanceof MultiSelectSetting) {
                MultiSelectSetting multiSetting = (MultiSelectSetting)setting;
                if (element.isJsonArray()) {
                    JsonArray array = element.getAsJsonArray();
                    ArrayList<String> selected = new ArrayList<String>();
                    for (JsonElement e : array) {
                        selected.add(e.getAsString());
                    }
                    multiSetting.setSelected(selected);
                }
            } else if (setting instanceof GroupSetting) {
                GroupSetting groupSetting = (GroupSetting)setting;
                if (element.isJsonObject()) {
                    JsonObject groupJson = element.getAsJsonObject();
                    if (groupJson.has("value")) {
                        groupSetting.setValue(groupJson.get("value").getAsBoolean());
                    }
                    if (groupJson.has("subSettings")) {
                        JsonObject subSettingsJson = groupJson.getAsJsonObject("subSettings");
                        for (Setting subSetting : groupSetting.getSubSettings()) {
                            if (!subSettingsJson.has(subSetting.getName())) continue;
                            this.deserializeSetting(subSetting, subSettingsJson.get(subSetting.getName()));
                        }
                    }
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    private ModuleRepository getModuleRepository() {
        Initialization instance = Initialization.getInstance();
        if (instance != null && instance.getManager() != null) {
            return instance.getManager().getModuleRepository();
        }
        return null;
    }
}

