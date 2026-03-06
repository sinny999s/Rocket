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
 */
package rich.util.config.impl.macro;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import java.util.ArrayList;
import rich.util.config.impl.consolelogger.Logger;
import rich.util.repository.macro.Macro;
import rich.util.repository.macro.MacroRepository;

public class MacroConfig {
    private static MacroConfig instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;

    private MacroConfig() {
        Path configDir = Paths.get("Rocket", "configs");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.configPath = configDir.resolve("macros.json");
    }

    public static MacroConfig getInstance() {
        if (instance == null) {
            instance = new MacroConfig();
        }
        return instance;
    }

    public void save() {
        try {
            JsonArray array = new JsonArray();
            for (Macro macro : MacroRepository.getInstance().getMacroList()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", macro.name());
                obj.addProperty("message", macro.message());
                obj.addProperty("key", (Number)macro.key());
                array.add((JsonElement)obj);
            }
            Files.writeString((Path)this.configPath, (CharSequence)this.gson.toJson((JsonElement)array), (Charset)StandardCharsets.UTF_8, (OpenOption[])new OpenOption[0]);
        }
        catch (IOException e) {
            Logger.error("MacroConfig: Save failed! " + e.getMessage());
        }
    }

    public void load() {
        try {
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                return;
            }
            String json = Files.readString((Path)this.configPath, (Charset)StandardCharsets.UTF_8);
            JsonArray array = JsonParser.parseString((String)json).getAsJsonArray();
            ArrayList<Macro> macros = new ArrayList<Macro>();
            array.forEach(element -> {
                JsonObject obj = element.getAsJsonObject();
                String name = obj.get("name").getAsString();
                String message = obj.get("message").getAsString();
                int key = obj.get("key").getAsInt();
                macros.add(new Macro(name, message, key));
            });
            MacroRepository.getInstance().setMacros(macros);
            Logger.success("MacroConfig: macros.json loaded successfully!");
        }
        catch (Exception e) {
            Logger.error("MacroConfig: Load failed! " + e.getMessage());
        }
    }
}

