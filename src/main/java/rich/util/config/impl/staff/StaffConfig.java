/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParser
 */
package rich.util.config.impl.staff;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import rich.util.repository.staff.StaffUtils;

public class StaffConfig {
    private static StaffConfig instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;

    private StaffConfig() {
        Path configDir = Paths.get("Rocket", "configs");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.configPath = configDir.resolve("staff.json");
    }

    public static StaffConfig getInstance() {
        if (instance == null) {
            instance = new StaffConfig();
        }
        return instance;
    }

    public void save() {
        try {
            JsonArray array = new JsonArray();
            for (String name : StaffUtils.getStaffNames()) {
                array.add(name);
            }
            Files.writeString((Path)this.configPath, (CharSequence)this.gson.toJson((JsonElement)array), (Charset)StandardCharsets.UTF_8, (OpenOption[])new OpenOption[0]);
        }
        catch (IOException e) {
            Logger.error("StaffConfig: Save failed! " + e.getMessage());
        }
    }

    public void load() {
        try {
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                return;
            }
            String json = Files.readString((Path)this.configPath, (Charset)StandardCharsets.UTF_8);
            JsonArray array = JsonParser.parseString((String)json).getAsJsonArray();
            ArrayList<String> names = new ArrayList<String>();
            array.forEach(element -> names.add(element.getAsString()));
            StaffUtils.setStaff(names);
            Logger.success("StaffConfig: staff.json loaded successfully!");
        }
        catch (Exception e) {
            Logger.error("StaffConfig: Load failed! " + e.getMessage());
        }
    }
}

