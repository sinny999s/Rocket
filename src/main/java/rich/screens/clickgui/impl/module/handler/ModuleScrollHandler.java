
package rich.screens.clickgui.impl.module.handler;

import lombok.Generated;

public class ModuleScrollHandler {
    private double moduleTargetScroll = 0.0;
    private double moduleDisplayScroll = 0.0;
    private double settingTargetScroll = 0.0;
    private double settingDisplayScroll = 0.0;
    private float moduleScrollTopFade = 0.0f;
    private float moduleScrollBottomFade = 0.0f;
    private float settingScrollTopFade = 0.0f;
    private float settingScrollBottomFade = 0.0f;
    private float lastSettingsPanelHeight = 0.0f;
    private float lastModuleListHeight = 0.0f;
    private long lastScrollUpdateTime = System.currentTimeMillis();
    private static final float SCROLL_SPEED = 12.0f;
    private static final float FADE_SPEED = 8.0f;
    private static final float CORNER_INSET = 3.0f;
    private static final float MODULE_ITEM_HEIGHT = 22.0f;

    public void resetModuleScroll() {
        this.moduleDisplayScroll = 0.0;
        this.moduleTargetScroll = 0.0;
    }

    public void resetSettingScroll() {
        this.settingDisplayScroll = 0.0;
        this.settingTargetScroll = 0.0;
    }

    public void update(float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastScrollUpdateTime) / 1000.0f, 0.1f);
        this.lastScrollUpdateTime = currentTime;
        this.moduleDisplayScroll = this.smoothScroll(this.moduleDisplayScroll, this.moduleTargetScroll, deltaTime);
        this.settingDisplayScroll = this.smoothScroll(this.settingDisplayScroll, this.settingTargetScroll, deltaTime);
    }

    private double smoothScroll(double current, double target, float deltaTime) {
        double diff = target - current;
        if (Math.abs(diff) < 0.5) {
            return target;
        }
        return current + diff * 12.0 * (double)deltaTime;
    }

    public void updateFades(int moduleCount, float totalSettingHeight, float moduleListHeight, float settingsPanelHeight) {
        this.lastSettingsPanelHeight = settingsPanelHeight;
        this.lastModuleListHeight = moduleListHeight;
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastScrollUpdateTime) / 1000.0f, 0.1f);
        float maxModuleScroll = Math.max(0.0f, (float)moduleCount * 24.0f - moduleListHeight + 10.0f);
        float maxSettingScroll = Math.max(0.0f, totalSettingHeight - settingsPanelHeight + 45.0f);
        this.moduleScrollTopFade = this.updateFade(this.moduleScrollTopFade, this.moduleDisplayScroll < -0.5, deltaTime);
        this.moduleScrollBottomFade = this.updateFade(this.moduleScrollBottomFade, this.moduleDisplayScroll > (double)(-maxModuleScroll + 0.5f) && maxModuleScroll > 0.0f, deltaTime);
        this.settingScrollTopFade = this.updateFade(this.settingScrollTopFade, this.settingDisplayScroll < -0.5, deltaTime);
        this.settingScrollBottomFade = this.updateFade(this.settingScrollBottomFade, this.settingDisplayScroll > (double)(-maxSettingScroll + 0.5f) && maxSettingScroll > 0.0f, deltaTime);
    }

    private float updateFade(float current, boolean condition, float deltaTime) {
        float target = condition ? 1.0f : 0.0f;
        float diff = target - current;
        if (Math.abs(diff) < 0.01f) {
            return target;
        }
        return current + diff * 8.0f * deltaTime;
    }

    public void handleModuleScroll(double vertical, float listHeight, int moduleCount) {
        float effectiveHeight = listHeight - 6.0f - 2.0f;
        float maxScroll = Math.max(0.0f, (float)moduleCount * 24.0f - effectiveHeight + 10.0f);
        this.moduleTargetScroll = Math.max((double)(-maxScroll), Math.min(0.0, this.moduleTargetScroll + vertical * 25.0));
    }

    public void handleSettingScroll(double vertical, float panelHeight, float totalSettingHeight) {
        float effectiveHeight = panelHeight - 31.0f - 3.0f - 3.0f;
        float maxScroll = Math.max(0.0f, totalSettingHeight - effectiveHeight + 10.0f);
        this.settingTargetScroll = Math.max((double)(-maxScroll), Math.min(0.0, this.settingTargetScroll + vertical * 25.0));
    }

    public void scrollToModule(int moduleIndex, int totalModules) {
        float moduleY = (float)moduleIndex * 24.0f;
        float visibleHeight = this.lastModuleListHeight - 6.0f - 4.0f;
        float centerOffset = (visibleHeight - 22.0f) / 2.0f;
        float targetScroll = -(moduleY - centerOffset);
        float maxScroll = Math.max(0.0f, (float)totalModules * 24.0f - visibleHeight);
        targetScroll = Math.max(-maxScroll, Math.min(0.0f, targetScroll));
        this.moduleTargetScroll = targetScroll;
    }

    public void correctSettingScrollPosition(float totalSettingHeight) {
        if (this.lastSettingsPanelHeight <= 0.0f) {
            return;
        }
        float maxScroll = Math.max(0.0f, totalSettingHeight - this.lastSettingsPanelHeight + 45.0f);
        if (this.settingTargetScroll < (double)(-maxScroll)) {
            this.settingTargetScroll = -maxScroll;
        }
        if (this.settingDisplayScroll < (double)(-maxScroll)) {
            this.settingDisplayScroll = -maxScroll;
        }
    }

    @Generated
    public double getModuleTargetScroll() {
        return this.moduleTargetScroll;
    }

    @Generated
    public double getModuleDisplayScroll() {
        return this.moduleDisplayScroll;
    }

    @Generated
    public double getSettingTargetScroll() {
        return this.settingTargetScroll;
    }

    @Generated
    public double getSettingDisplayScroll() {
        return this.settingDisplayScroll;
    }

    @Generated
    public float getModuleScrollTopFade() {
        return this.moduleScrollTopFade;
    }

    @Generated
    public float getModuleScrollBottomFade() {
        return this.moduleScrollBottomFade;
    }

    @Generated
    public float getSettingScrollTopFade() {
        return this.settingScrollTopFade;
    }

    @Generated
    public float getSettingScrollBottomFade() {
        return this.settingScrollBottomFade;
    }

    @Generated
    public float getLastSettingsPanelHeight() {
        return this.lastSettingsPanelHeight;
    }

    @Generated
    public float getLastModuleListHeight() {
        return this.lastModuleListHeight;
    }

    @Generated
    public long getLastScrollUpdateTime() {
        return this.lastScrollUpdateTime;
    }
}

