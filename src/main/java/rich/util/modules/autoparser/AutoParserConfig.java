
package rich.util.modules.autoparser;

import lombok.Generated;

public class AutoParserConfig {
    private static AutoParserConfig instance;
    private boolean enabled = false;
    private int discountPercent = 60;
    private volatile boolean isRunning = false;
    private boolean debugMode = false;

    private AutoParserConfig() {
    }

    public static AutoParserConfig getInstance() {
        if (instance == null) {
            instance = new AutoParserConfig();
        }
        return instance;
    }

    public void toggle() {
        this.enabled = !this.enabled;
    }

    public void reset() {
        this.isRunning = false;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    @Generated
    public boolean isEnabled() {
        return this.enabled;
    }

    @Generated
    public int getDiscountPercent() {
        return this.discountPercent;
    }

    @Generated
    public boolean isDebugMode() {
        return this.debugMode;
    }

    @Generated
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Generated
    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    @Generated
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}

