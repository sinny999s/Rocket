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
package rich.util.config.impl.blockesp;

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
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import rich.util.config.impl.consolelogger.Logger;

public class BlockESPConfig {
    private static BlockESPConfig instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;
    private final Set<String> blocks = new CopyOnWriteArraySet<String>();

    private BlockESPConfig() {
        Path configDir = Paths.get("Rocket", "configs");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.configPath = configDir.resolve("blockesp.json");
    }

    public static BlockESPConfig getInstance() {
        if (instance == null) {
            instance = new BlockESPConfig();
        }
        return instance;
    }

    public Set<String> getBlocks() {
        return this.blocks;
    }

    public void addBlock(String block) {
        this.blocks.add(block);
    }

    public void addBlockAndSave(String block) {
        this.addBlock(block);
        this.save();
    }

    public boolean removeBlock(String block) {
        boolean removed = this.blocks.remove(block);
        return removed;
    }

    public boolean removeBlockAndSave(String block) {
        boolean removed = this.removeBlock(block);
        if (removed) {
            this.save();
        }
        return removed;
    }

    public boolean hasBlock(String block) {
        return this.blocks.contains(block);
    }

    public void clear() {
        this.blocks.clear();
    }

    public void clearAndSave() {
        this.clear();
        this.save();
    }

    public int size() {
        return this.blocks.size();
    }

    public List<String> getBlockList() {
        return new ArrayList<String>(this.blocks);
    }

    public void save() {
        try {
            JsonArray array = new JsonArray();
            for (String block : this.blocks) {
                array.add(block);
            }
            Files.writeString((Path)this.configPath, (CharSequence)this.gson.toJson((JsonElement)array), (Charset)StandardCharsets.UTF_8, (OpenOption[])new OpenOption[0]);
        }
        catch (IOException e) {
            Logger.error("BlockESPConfig: Save failed! " + e.getMessage());
        }
    }

    public void load() {
        try {
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                return;
            }
            String json = Files.readString((Path)this.configPath, (Charset)StandardCharsets.UTF_8);
            JsonArray array = JsonParser.parseString((String)json).getAsJsonArray();
            this.blocks.clear();
            array.forEach(element -> this.blocks.add(element.getAsString()));
            Logger.success("BlockESPConfig: blockesp.json loaded successfully!");
        }
        catch (Exception e) {
            Logger.error("BlockESPConfig: Load failed! " + e.getMessage());
        }
    }
}

