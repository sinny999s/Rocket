/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 */
package rich.util.config.impl.drag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import rich.Initialization;
import rich.client.draggables.HudElement;
import rich.client.draggables.HudManager;
import rich.util.config.impl.consolelogger.Logger;

public class DragConfig {
    private static DragConfig instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;

    private DragConfig() {
        Path configDir = Paths.get("Rocket", "configs");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.configPath = configDir.resolve("draggables.json");
    }

    public static DragConfig getInstance() {
        if (instance == null) {
            instance = new DragConfig();
        }
        return instance;
    }

    public void save() {
        try {
            HudManager hudManager = this.getHudManager();
            if (hudManager == null || !hudManager.isInitialized()) {
                return;
            }
            JsonObject root = new JsonObject();
            for (HudElement element : hudManager.getElements()) {
                JsonObject elementJson = new JsonObject();
                elementJson.addProperty("x", (Number)element.getX());
                elementJson.addProperty("y", (Number)element.getY());
                elementJson.addProperty("width", (Number)element.getWidth());
                elementJson.addProperty("height", (Number)element.getHeight());
                root.add(element.getName(), (JsonElement)elementJson);
            }
            Files.writeString((Path)this.configPath, (CharSequence)this.gson.toJson((JsonElement)root), (Charset)StandardCharsets.UTF_8, (OpenOption[])new OpenOption[0]);
            Logger.success("DragConfig: draggables.json saved successfully!");
        }
        catch (IOException e) {
            Logger.error("DragConfig: Save failed! " + e.getMessage());
        }
    }

    public void load() {
        try {
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                Logger.info("DragConfig: No config file found, using defaults.");
                return;
            }
            HudManager hudManager = this.getHudManager();
            if (hudManager == null) {
                Logger.error("DragConfig: HudManager is null, cannot load.");
                return;
            }
            String json = Files.readString((Path)this.configPath, (Charset)StandardCharsets.UTF_8);
            if (json == null || json.trim().isEmpty()) {
                Logger.error("DragConfig: Config file is empty.");
                return;
            }
            JsonObject root = JsonParser.parseString((String)json).getAsJsonObject();
            for (HudElement element : hudManager.getElements()) {
                if (!root.has(element.getName())) continue;
                JsonObject elementJson = root.getAsJsonObject(element.getName());
                if (elementJson.has("x")) {
                    element.setX(elementJson.get("x").getAsInt());
                }
                if (elementJson.has("y")) {
                    element.setY(elementJson.get("y").getAsInt());
                }
                if (elementJson.has("width")) {
                    element.setWidth(elementJson.get("width").getAsInt());
                }
                if (!elementJson.has("height")) continue;
                element.setHeight(elementJson.get("height").getAsInt());
            }
            Logger.success("DragConfig: draggables.json loaded successfully!");
        }
        catch (Exception e) {
            Logger.error("DragConfig: Load failed! " + e.getMessage());
        }
    }

    private HudManager getHudManager() {
        if (Initialization.getInstance() == null) {
            return null;
        }
        if (Initialization.getInstance().getManager() == null) {
            return null;
        }
        return Initialization.getInstance().getManager().getHudManager();
    }
}

