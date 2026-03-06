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
package rich.util.config.impl.account;

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
import java.util.List;
import net.minecraft.resources.Identifier;
import rich.screens.account.AccountEntry;
import rich.util.config.impl.consolelogger.Logger;
import rich.util.session.SessionChanger;

public class AccountConfig {
    private static AccountConfig instance;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path configPath;
    private final List<AccountEntry> accounts = new ArrayList<AccountEntry>();
    private String activeAccountName = "";
    private String activeAccountDate = "";
    private String activeAccountSkin = "";

    private AccountConfig() {
        Path configDir = Paths.get("Rocket", "configs");
        try {
            Files.createDirectories(configDir, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            // empty catch block
        }
        this.configPath = configDir.resolve("accounts.json");
    }

    public static AccountConfig getInstance() {
        if (instance == null) {
            instance = new AccountConfig();
        }
        return instance;
    }

    public void save() {
        try {
            JsonObject root = new JsonObject();
            JsonArray accountsArray = new JsonArray();
            for (AccountEntry entry : this.accounts) {
                JsonObject accountObj = new JsonObject();
                accountObj.addProperty("name", entry.getName());
                accountObj.addProperty("date", entry.getDate());
                accountObj.addProperty("skin", entry.getSkin() != null ? entry.getSkin().toString() : "");
                accountObj.addProperty("pinned", Boolean.valueOf(entry.isPinned()));
                accountObj.addProperty("originalIndex", (Number)entry.getOriginalIndex());
                accountsArray.add((JsonElement)accountObj);
            }
            root.add("accounts", (JsonElement)accountsArray);
            JsonObject activeObj = new JsonObject();
            activeObj.addProperty("name", this.activeAccountName);
            activeObj.addProperty("date", this.activeAccountDate);
            activeObj.addProperty("skin", this.activeAccountSkin);
            root.add("active", (JsonElement)activeObj);
            Files.writeString((Path)this.configPath, (CharSequence)this.gson.toJson((JsonElement)root), (Charset)StandardCharsets.UTF_8, (OpenOption[])new OpenOption[0]);
            Logger.success("AccountConfig: accounts.json saved successfully!");
        }
        catch (IOException e) {
            Logger.error("AccountConfig: Save failed! " + e.getMessage());
        }
    }

    public void load() {
        try {
            if (!Files.exists(this.configPath, new LinkOption[0])) {
                Logger.info("AccountConfig: No config file found, using defaults.");
                return;
            }
            String json = Files.readString((Path)this.configPath, (Charset)StandardCharsets.UTF_8);
            if (json == null || json.trim().isEmpty()) {
                Logger.error("AccountConfig: Config file is empty.");
                return;
            }
            JsonObject root = JsonParser.parseString((String)json).getAsJsonObject();
            this.accounts.clear();
            if (root.has("accounts")) {
                JsonArray accountsArray = root.getAsJsonArray("accounts");
                for (int i = 0; i < accountsArray.size(); ++i) {
                    JsonObject accountObj = accountsArray.get(i).getAsJsonObject();
                    String name = accountObj.has("name") ? accountObj.get("name").getAsString() : "";
                    String date = accountObj.has("date") ? accountObj.get("date").getAsString() : "";
                    String skinStr = accountObj.has("skin") ? accountObj.get("skin").getAsString() : "";
                    boolean pinned = accountObj.has("pinned") && accountObj.get("pinned").getAsBoolean();
                    int originalIndex = accountObj.has("originalIndex") ? accountObj.get("originalIndex").getAsInt() : i;
                    Identifier skin = null;
                    if (!skinStr.isEmpty()) {
                        try {
                            skin = Identifier.parse((String)skinStr);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                    AccountEntry entry = new AccountEntry(name, date, skin, pinned, originalIndex);
                    this.accounts.add(entry);
                }
            }
            if (root.has("active")) {
                JsonObject activeObj = root.getAsJsonObject("active");
                this.activeAccountName = activeObj.has("name") ? activeObj.get("name").getAsString() : "";
                this.activeAccountDate = activeObj.has("date") ? activeObj.get("date").getAsString() : "";
                String string = this.activeAccountSkin = activeObj.has("skin") ? activeObj.get("skin").getAsString() : "";
            }
            if (!this.activeAccountName.isEmpty()) {
                SessionChanger.changeUsername(this.activeAccountName);
            }
            Logger.success("AccountConfig: accounts.json loaded successfully!");
        }
        catch (Exception e) {
            Logger.error("AccountConfig: Load failed! " + e.getMessage());
        }
    }

    public List<AccountEntry> getAccounts() {
        return this.accounts;
    }

    public List<AccountEntry> getSortedAccounts() {
        ArrayList<AccountEntry> sorted = new ArrayList<AccountEntry>(this.accounts);
        sorted.sort((a, b) -> {
            if (a.isPinned() && !b.isPinned()) {
                return -1;
            }
            if (!a.isPinned() && b.isPinned()) {
                return 1;
            }
            return Integer.compare(a.getOriginalIndex(), b.getOriginalIndex());
        });
        return sorted;
    }

    public void addAccount(AccountEntry entry) {
        entry.setOriginalIndex(this.accounts.size());
        this.accounts.add(entry);
        this.save();
    }

    public void removeAccount(AccountEntry entry) {
        this.accounts.remove(entry);
        this.updateOriginalIndices();
        this.save();
    }

    public void removeAccountByIndex(int sortedIndex) {
        List<AccountEntry> sorted = this.getSortedAccounts();
        if (sortedIndex >= 0 && sortedIndex < sorted.size()) {
            AccountEntry toRemove = sorted.get(sortedIndex);
            this.accounts.remove(toRemove);
            this.updateOriginalIndices();
            this.save();
        }
    }

    public void clearAllAccounts() {
        this.accounts.clear();
        this.activeAccountName = "";
        this.activeAccountDate = "";
        this.activeAccountSkin = "";
        this.save();
    }

    public AccountEntry getAccountBySortedIndex(int sortedIndex) {
        List<AccountEntry> sorted = this.getSortedAccounts();
        if (sortedIndex >= 0 && sortedIndex < sorted.size()) {
            return sorted.get(sortedIndex);
        }
        return null;
    }

    private void updateOriginalIndices() {
        ArrayList<AccountEntry> unpinned = new ArrayList<AccountEntry>();
        for (AccountEntry entry : this.accounts) {
            if (entry.isPinned()) continue;
            unpinned.add(entry);
        }
        for (int i = 0; i < unpinned.size(); ++i) {
            ((AccountEntry)unpinned.get(i)).setOriginalIndex(i);
        }
    }

    public void togglePin(int sortedIndex) {
        List<AccountEntry> sorted = this.getSortedAccounts();
        if (sortedIndex >= 0 && sortedIndex < sorted.size()) {
            AccountEntry entry = sorted.get(sortedIndex);
            entry.togglePinned();
            this.save();
        }
    }

    public String getActiveAccountName() {
        return this.activeAccountName;
    }

    public String getActiveAccountDate() {
        return this.activeAccountDate;
    }

    public Identifier getActiveAccountSkin() {
        if (this.activeAccountSkin.isEmpty()) {
            return null;
        }
        try {
            return Identifier.parse((String)this.activeAccountSkin);
        }
        catch (Exception e) {
            return null;
        }
    }

    public void setActiveAccount(String name, String date, Identifier skin) {
        this.activeAccountName = name;
        this.activeAccountDate = date;
        this.activeAccountSkin = skin != null ? skin.toString() : "";
        SessionChanger.changeUsername(name);
        this.save();
    }
}

