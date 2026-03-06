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
package rich.util.config.impl.prefix;

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
import rich.command.CommandManager;
import rich.util.config.impl.consolelogger.Logger;

public class PrefixConfig {
    private static PrefixConfig instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;
    private String prefix = ".";

    private PrefixConfig() {
        Path configDir = Paths.get("Rocket", "configs");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.configPath = configDir.resolve("prefix.json");
    }

    public static PrefixConfig getInstance() {
        if (instance == null) {
            instance = new PrefixConfig();
        }
        return instance;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
        if (CommandManager.getInstance() != null) {
            CommandManager.getInstance().setPrefix(prefix);
        }
    }

    public void setPrefixAndSave(String prefix) {
        this.setPrefix(prefix);
        this.save();
    }

    public void save() {
        try {
            JsonObject obj = new JsonObject();
            obj.addProperty("prefix", this.prefix);
            Files.writeString((Path)this.configPath, (CharSequence)this.gson.toJson((JsonElement)obj), (Charset)StandardCharsets.UTF_8, (OpenOption[])new OpenOption[0]);
        }
        catch (IOException e) {
            Logger.error("PrefixConfig: Save failed! " + e.getMessage());
        }
    }

    public void load() {
        try {
            String loadedPrefix;
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                return;
            }
            String json = Files.readString((Path)this.configPath, (Charset)StandardCharsets.UTF_8);
            JsonObject obj = JsonParser.parseString((String)json).getAsJsonObject();
            if (obj.has("prefix") && !(loadedPrefix = obj.get("prefix").getAsString()).isEmpty()) {
                this.prefix = loadedPrefix;
            }
            Logger.success("PrefixConfig: prefix.json loaded successfully!");
        }
        catch (Exception e) {
            Logger.error("PrefixConfig: Load failed! " + e.getMessage());
        }
    }

    @Generated
    public String getPrefix() {
        return this.prefix;
    }
}

