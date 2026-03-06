
package rich.screens.clickgui.impl.configs.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;

public class ConfigAnimationHandler {
    private final Map<String, Float> hoverAnimations = new HashMap<String, Float>();
    private final Map<String, Float> deleteHoverAnimations = new HashMap<String, Float>();
    private final Map<String, Float> loadHoverAnimations = new HashMap<String, Float>();
    private final Map<String, Float> refreshHoverAnimations = new HashMap<String, Float>();
    private final Map<String, Float> itemAppearAnimations = new HashMap<String, Float>();
    private float panelAlpha = 0.0f;
    private float panelSlide = 0.0f;
    private float createBoxAnimation = 0.0f;
    private float cursorBlink = 0.0f;
    private float selectedAnimation = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();

    public void reset() {
        this.panelAlpha = 0.0f;
        this.panelSlide = 0.0f;
        this.createBoxAnimation = 0.0f;
        this.itemAppearAnimations.clear();
        this.hoverAnimations.clear();
        this.deleteHoverAnimations.clear();
        this.loadHoverAnimations.clear();
        this.refreshHoverAnimations.clear();
    }

    public void initItemAnimations(List<String> configs) {
        for (String config : configs) {
            if (this.itemAppearAnimations.containsKey(config)) continue;
            this.itemAppearAnimations.put(config, Float.valueOf(0.0f));
        }
    }

    public void update(boolean isActive, List<String> configs, boolean isCreating) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0f, 0.1f);
        this.lastUpdateTime = currentTime;
        this.updatePanelAnimations(isActive, deltaTime);
        this.updateCreateBoxAnimation(isCreating, deltaTime);
        this.updateCursorBlink(deltaTime);
        this.updateItemAnimations(isActive, configs, deltaTime);
        this.updateHoverAnimations(configs, deltaTime);
    }

    private void updatePanelAnimations(boolean isActive, float deltaTime) {
        float targetPanelAlpha = isActive ? 1.0f : 0.0f;
        float alphaDiff = targetPanelAlpha - this.panelAlpha;
        this.panelAlpha += alphaDiff * 16.0f * deltaTime;
        this.panelAlpha = Math.max(0.0f, Math.min(1.0f, this.panelAlpha));
        float targetSlide = isActive ? 1.0f : 0.0f;
        float slideDiff = targetSlide - this.panelSlide;
        this.panelSlide += slideDiff * 20.0f * deltaTime;
        this.panelSlide = Math.max(0.0f, Math.min(1.0f, this.panelSlide));
    }

    private void updateCreateBoxAnimation(boolean isCreating, float deltaTime) {
        float targetCreate = isCreating ? 1.0f : 0.0f;
        this.createBoxAnimation += (targetCreate - this.createBoxAnimation) * 14.0f * deltaTime;
    }

    private void updateCursorBlink(float deltaTime) {
        this.cursorBlink += deltaTime * 2.0f;
        if (this.cursorBlink > 1.0f) {
            this.cursorBlink -= 1.0f;
        }
    }

    private void updateItemAnimations(boolean isActive, List<String> configs, float deltaTime) {
        int index = 0;
        for (String config : configs) {
            float delay;
            float currentAppear = this.itemAppearAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
            float targetAppear = isActive ? (this.panelAlpha > (delay = (float)index * 0.02f) ? 1.0f : 0.0f) : 0.0f;
            float speed = isActive ? 20.0f : 16.0f;
            float appearDiff = targetAppear - currentAppear;
            this.itemAppearAnimations.put(config, Float.valueOf(Math.max(0.0f, Math.min(1.0f, currentAppear += appearDiff * speed * deltaTime))));
            ++index;
        }
    }

    private void updateHoverAnimations(List<String> configs, float deltaTime) {
        for (String config : configs) {
            float current = this.hoverAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
            this.hoverAnimations.put(config, Float.valueOf(current + (0.0f - current) * 8.0f * deltaTime));
            float deleteCurrent = this.deleteHoverAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
            this.deleteHoverAnimations.put(config, Float.valueOf(deleteCurrent + (0.0f - deleteCurrent) * 8.0f * deltaTime));
            float loadCurrent = this.loadHoverAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
            this.loadHoverAnimations.put(config, Float.valueOf(loadCurrent + (0.0f - loadCurrent) * 8.0f * deltaTime));
            float refreshCurrent = this.refreshHoverAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
            this.refreshHoverAnimations.put(config, Float.valueOf(refreshCurrent + (0.0f - refreshCurrent) * 8.0f * deltaTime));
        }
    }

    public void updateSelectedAnimation(boolean hasSelection, float deltaTime) {
        float targetSelected = hasSelection ? 1.0f : 0.0f;
        this.selectedAnimation += (targetSelected - this.selectedAnimation) * 8.0f * deltaTime;
    }

    public void setHoverAnimation(String config, float value) {
        this.hoverAnimations.put(config, Float.valueOf(value));
    }

    public void setDeleteHoverAnimation(String config, float value) {
        this.deleteHoverAnimations.put(config, Float.valueOf(value));
    }

    public void setLoadHoverAnimation(String config, float value) {
        this.loadHoverAnimations.put(config, Float.valueOf(value));
    }

    public void setRefreshHoverAnimation(String config, float value) {
        this.refreshHoverAnimations.put(config, Float.valueOf(value));
    }

    public float getItemAppearAnimation(String config) {
        return this.itemAppearAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
    }

    public float getHoverAnimation(String config) {
        return this.hoverAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
    }

    public float getDeleteHoverAnimation(String config) {
        return this.deleteHoverAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
    }

    public float getLoadHoverAnimation(String config) {
        return this.loadHoverAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
    }

    public float getRefreshHoverAnimation(String config) {
        return this.refreshHoverAnimations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
    }

    public boolean isFullyHidden() {
        return this.panelAlpha < 0.01f && this.panelSlide < 0.01f;
    }

    @Generated
    public Map<String, Float> getHoverAnimations() {
        return this.hoverAnimations;
    }

    @Generated
    public Map<String, Float> getDeleteHoverAnimations() {
        return this.deleteHoverAnimations;
    }

    @Generated
    public Map<String, Float> getLoadHoverAnimations() {
        return this.loadHoverAnimations;
    }

    @Generated
    public Map<String, Float> getRefreshHoverAnimations() {
        return this.refreshHoverAnimations;
    }

    @Generated
    public Map<String, Float> getItemAppearAnimations() {
        return this.itemAppearAnimations;
    }

    @Generated
    public float getPanelAlpha() {
        return this.panelAlpha;
    }

    @Generated
    public float getPanelSlide() {
        return this.panelSlide;
    }

    @Generated
    public float getCreateBoxAnimation() {
        return this.createBoxAnimation;
    }

    @Generated
    public float getCursorBlink() {
        return this.cursorBlink;
    }

    @Generated
    public float getSelectedAnimation() {
        return this.selectedAnimation;
    }

    @Generated
    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }
}

