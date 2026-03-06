
package rich.util.config;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import rich.util.config.impl.ConfigFileHandler;
import rich.util.config.impl.ConfigPath;
import rich.util.config.impl.ConfigSerializer;
import rich.util.config.impl.autosaver.ConfigAutoSaver;
import rich.util.config.impl.consolelogger.Logger;

public class ConfigSystem {
    private static ConfigSystem instance;
    private final ConfigSerializer serializer;
    private final ConfigFileHandler fileHandler;
    private final ConfigAutoSaver autoSaver;
    private final AtomicBoolean initialized;
    private final AtomicBoolean saving;

    public ConfigSystem() {
        instance = this;
        this.serializer = new ConfigSerializer();
        this.fileHandler = new ConfigFileHandler();
        this.autoSaver = new ConfigAutoSaver(this::save);
        this.initialized = new AtomicBoolean(false);
        this.saving = new AtomicBoolean(false);
    }

    public static ConfigSystem getInstance() {
        return instance;
    }

    public void init() {
        if (this.initialized.compareAndSet(false, true)) {
            ConfigPath.init();
            this.fileHandler.createDirectories();
            this.load();
            this.autoSaver.start();
            this.registerShutdownHook();
            Logger.success("AutoConfiguration: System initialized!");
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("AutoConfiguration: Shutdown detected, saving...");
            this.shutdown();
        }, "Rich-ConfigShutdown"));
    }

    public void save() {
        if (!this.initialized.get()) {
            return;
        }
        if (!this.saving.compareAndSet(false, true)) {
            return;
        }
        try {
            String data = this.serializer.serialize();
            boolean success = this.fileHandler.write(data);
            if (success) {
                Logger.success("AutoConfiguration: autoconfig.json saved successfully!");
            } else {
                Logger.error("AutoConfiguration: autoconfig.json save failed!");
            }
        }
        catch (Exception e) {
            Logger.error("AutoConfiguration: Save error! " + e.getMessage());
        }
        finally {
            this.saving.set(false);
        }
    }

    public CompletableFuture<Void> saveAsync() {
        return CompletableFuture.runAsync(this::save);
    }

    public void load() {
        if (!this.fileHandler.exists()) {
            Logger.info("AutoConfiguration: No config found, creating new...");
            this.save();
            return;
        }
        try {
            String data = this.fileHandler.read();
            if (data != null && !data.isEmpty()) {
                this.serializer.deserialize(data);
                Logger.success("AutoConfiguration: autoconfig.json loaded successfully!");
            }
        }
        catch (Exception e) {
            Logger.error("AutoConfiguration: Load error! " + e.getMessage());
        }
    }

    public void shutdown() {
        if (!this.initialized.get()) {
            return;
        }
        this.autoSaver.shutdown();
        this.save();
        Logger.success("AutoConfiguration: Shutdown complete!");
    }

    public void reload() {
        this.load();
        Logger.success("AutoConfiguration: Config reloaded!");
    }

    public boolean isInitialized() {
        return this.initialized.get();
    }

    public boolean isSaving() {
        return this.saving.get();
    }

    public ConfigAutoSaver getAutoSaver() {
        return this.autoSaver;
    }
}

