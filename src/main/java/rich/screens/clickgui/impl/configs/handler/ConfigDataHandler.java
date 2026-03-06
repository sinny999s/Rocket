
package rich.screens.clickgui.impl.configs.handler;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import rich.screens.clickgui.impl.configs.handler.ConfigAnimationHandler;
import rich.util.config.ConfigSystem;
import rich.util.config.impl.ConfigPath;

public class ConfigDataHandler {
    private final List<String> configs = new ArrayList<String>();
    private final ConfigAnimationHandler animationHandler;
    private String selectedConfig = null;
    private boolean isCreating = false;
    private String newConfigName = "";
    private double scrollOffset = 0.0;
    private double targetScrollOffset = 0.0;
    private float scrollTopFade = 0.0f;
    private float scrollBottomFade = 0.0f;

    public ConfigDataHandler(ConfigAnimationHandler animationHandler) {
        this.animationHandler = animationHandler;
    }

    public void refreshConfigs() {
        ArrayList<String> oldConfigs = new ArrayList<String>(this.configs);
        this.configs.clear();
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            if (Files.exists(configDir, new LinkOption[0])) {
                Files.list(configDir).filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                    String name = path.getFileName().toString();
                    String configName = name.substring(0, name.length() - 5);
                    if (!configName.equalsIgnoreCase("autoconfig")) {
                        this.configs.add(configName);
                    }
                });
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        for (String config : this.configs) {
            if (oldConfigs.contains(config)) continue;
            this.animationHandler.getItemAppearAnimations().put(config, Float.valueOf(0.0f));
        }
    }

    public void updateScroll(float deltaTime) {
        this.scrollOffset += (this.targetScrollOffset - this.scrollOffset) * 12.0 * (double)deltaTime;
    }

    public void updateScrollFades(float visibleHeight) {
        float contentHeight = (float)this.configs.size() * 27.0f;
        if (contentHeight <= visibleHeight) {
            this.scrollTopFade = 0.0f;
            this.scrollBottomFade = 0.0f;
            return;
        }
        float maxScroll = contentHeight - visibleHeight;
        this.scrollTopFade = (float)Math.min(1.0, -this.scrollOffset / 20.0);
        this.scrollBottomFade = (float)Math.min(1.0, ((double)maxScroll + this.scrollOffset) / 20.0);
    }

    public void handleScroll(double vertical, float visibleHeight) {
        float contentHeight = (float)this.configs.size() * 27.0f;
        float maxScroll = Math.max(0.0f, contentHeight - visibleHeight);
        this.targetScrollOffset += vertical * 25.0;
        this.targetScrollOffset = Math.max((double)(-maxScroll), Math.min(0.0, this.targetScrollOffset));
    }

    public boolean saveConfig(String name) {
        if (name.equalsIgnoreCase("autoconfig")) {
            return false;
        }
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            Path newConfig = configDir.resolve(name + ".json");
            if (Files.exists(newConfig, new LinkOption[0])) {
                return false;
            }
            ConfigSystem.getInstance().save();
            Path currentConfig = ConfigPath.getConfigFile();
            Files.copy(currentConfig, newConfig, new CopyOption[0]);
            this.refreshConfigs();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean loadConfig(String name) {
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            Path configFile = configDir.resolve(name + ".json");
            return Files.exists(configFile, new LinkOption[0]);
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean refreshConfig(String name) {
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            Path configFile = configDir.resolve(name + ".json");
            if (!Files.exists(configFile, new LinkOption[0])) {
                return false;
            }
            ConfigSystem.getInstance().save();
            Files.deleteIfExists(configFile);
            Path currentConfig = ConfigPath.getConfigFile();
            Files.copy(currentConfig, configFile, new CopyOption[0]);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public boolean deleteConfig(String name) {
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            Path configFile = configDir.resolve(name + ".json");
            if (Files.exists(configFile, new LinkOption[0])) {
                Files.delete(configFile);
                if (name.equals(this.selectedConfig)) {
                    this.selectedConfig = null;
                }
                this.refreshConfigs();
                return true;
            }
            return false;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void toggleCreating() {
        boolean bl = this.isCreating = !this.isCreating;
        if (!this.isCreating) {
            this.newConfigName = "";
        }
    }

    public void appendChar(char chr) {
        if (this.newConfigName.length() < 32 && (Character.isLetterOrDigit(chr) || chr == '_' || chr == '-')) {
            this.newConfigName = this.newConfigName + chr;
        }
    }

    public void removeLastChar() {
        if (!this.newConfigName.isEmpty()) {
            this.newConfigName = this.newConfigName.substring(0, this.newConfigName.length() - 1);
        }
    }

    public void clearNewConfigName() {
        this.newConfigName = "";
    }

    @Generated
    public List<String> getConfigs() {
        return this.configs;
    }

    @Generated
    public ConfigAnimationHandler getAnimationHandler() {
        return this.animationHandler;
    }

    @Generated
    public String getSelectedConfig() {
        return this.selectedConfig;
    }

    @Generated
    public boolean isCreating() {
        return this.isCreating;
    }

    @Generated
    public String getNewConfigName() {
        return this.newConfigName;
    }

    @Generated
    public double getScrollOffset() {
        return this.scrollOffset;
    }

    @Generated
    public double getTargetScrollOffset() {
        return this.targetScrollOffset;
    }

    @Generated
    public float getScrollTopFade() {
        return this.scrollTopFade;
    }

    @Generated
    public float getScrollBottomFade() {
        return this.scrollBottomFade;
    }

    @Generated
    public void setSelectedConfig(String selectedConfig) {
        this.selectedConfig = selectedConfig;
    }

    @Generated
    public void setCreating(boolean isCreating) {
        this.isCreating = isCreating;
    }

    @Generated
    public void setNewConfigName(String newConfigName) {
        this.newConfigName = newConfigName;
    }

    @Generated
    public void setScrollOffset(double scrollOffset) {
        this.scrollOffset = scrollOffset;
    }

    @Generated
    public void setTargetScrollOffset(double targetScrollOffset) {
        this.targetScrollOffset = targetScrollOffset;
    }

    @Generated
    public void setScrollTopFade(float scrollTopFade) {
        this.scrollTopFade = scrollTopFade;
    }

    @Generated
    public void setScrollBottomFade(float scrollBottomFade) {
        this.scrollBottomFade = scrollBottomFade;
    }
}

