/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  lombok.Generated
 */
package rich.util.config.impl.bind;

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
import lombok.Generated;
import rich.util.config.impl.consolelogger.Logger;

public class BindConfig {
    private static BindConfig instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;
    private int BindKey = 344;

    private BindConfig() {
        Path configDir = Paths.get("Rocket", "configs");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.configPath = configDir.resolve("Bind.json");
        this.load();
    }

    public static BindConfig getInstance() {
        if (instance == null) {
            instance = new BindConfig();
        }
        return instance;
    }

    public void setKey(int key) {
        this.BindKey = key;
    }

    public void setKeyAndSave(int key) {
        this.setKey(key);
        this.save();
    }

    public void save() {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("BindKey", (Number)this.BindKey);
            Files.writeString((Path)this.configPath, (CharSequence)this.gson.toJson((JsonElement)obj), (Charset)StandardCharsets.UTF_8, (OpenOption[])new OpenOption[0]);
        }
        catch (IOException e) {
            Logger.error("BindConfig: Save failed! " + e.getMessage());
        }
    }

    public void load() {
        try {
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                return;
            }
            String json = Files.readString((Path)this.configPath, (Charset)StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString((String)json).getAsJsonObject();
            if (obj.has("BindKey")) {
                this.BindKey = obj.get("BindKey").getAsInt();
            }
            Logger.success("BindConfig: Bind.json loaded successfully!");
        }
        catch (Exception e) {
            Logger.error("BindConfig: Load failed! " + e.getMessage());
        }
    }

    @Generated
    public int getBindKey() {
        return this.BindKey;
    }
}

