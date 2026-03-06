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
package rich.util.config.impl.way;

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
import net.minecraft.core.BlockPos;
import rich.util.config.impl.consolelogger.Logger;
import rich.util.repository.way.Way;
import rich.util.repository.way.WayRepository;

public class WayConfig {
    private static WayConfig instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;

    private WayConfig() {
        Path configDir = Paths.get("Rocket", "configs");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.configPath = configDir.resolve("waypoints.json");
    }

    public static WayConfig getInstance() {
        if (instance == null) {
            instance = new WayConfig();
        }
        return instance;
    }

    public void save() {
        try {
            JsonArray array = new JsonArray();
            for (Way way : WayRepository.getInstance().getWayList()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", way.name());
                obj.addProperty("x", (Number)way.pos().getX());
                obj.addProperty("y", (Number)way.pos().getY());
                obj.addProperty("z", (Number)way.pos().getZ());
                obj.addProperty("server", way.server());
                obj.addProperty("dimension", way.dimension() != null ? way.dimension() : "overworld");
                array.add((JsonElement)obj);
            }
            Files.writeString((Path)this.configPath, (CharSequence)this.gson.toJson((JsonElement)array), (Charset)StandardCharsets.UTF_8, (OpenOption[])new OpenOption[0]);
        }
        catch (IOException e) {
            Logger.error("WayConfig: Save failed! " + e.getMessage());
        }
    }

    public void load() {
        try {
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                return;
            }
            String json = Files.readString((Path)this.configPath, (Charset)StandardCharsets.UTF_8);
            JsonArray array = JsonParser.parseString((String)json).getAsJsonArray();
            ArrayList<Way> ways = new ArrayList<Way>();
            array.forEach(element -> {
                JsonObject obj = element.getAsJsonObject();
                String name = obj.get("name").getAsString();
                int x = obj.get("x").getAsInt();
                int y = obj.get("y").getAsInt();
                int z = obj.get("z").getAsInt();
                String server = obj.get("server").getAsString();
                String dimension = obj.has("dimension") ? obj.get("dimension").getAsString() : "overworld";
                ways.add(new Way(name, new BlockPos(x, y, z), server, dimension));
            });
            WayRepository.getInstance().setWays(ways);
            Logger.success("WayConfig: waypoints.json loaded successfully!");
        }
        catch (Exception e) {
            Logger.error("WayConfig: Load failed! " + e.getMessage());
        }
    }
}

