
package rich.screens.clickgui.impl.module.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import rich.modules.module.ModuleStructure;
import rich.util.interfaces.AbstractSettingComponent;

public class ModuleAnimationHandler {
    private Map<ModuleStructure, Float> moduleAnimations = new HashMap<ModuleStructure, Float>();
    private Map<ModuleStructure, Long> moduleAnimStartTimes = new HashMap<ModuleStructure, Long>();
    private Map<ModuleStructure, Float> oldModuleAnimations = new HashMap<ModuleStructure, Float>();
    private Map<AbstractSettingComponent, Float> settingAnimations = new HashMap<AbstractSettingComponent, Float>();
    private Map<AbstractSettingComponent, Long> settingAnimStartTimes = new HashMap<AbstractSettingComponent, Long>();
    private Map<AbstractSettingComponent, Float> visibilityAnimations = new HashMap<AbstractSettingComponent, Float>();
    private Map<AbstractSettingComponent, Float> heightAnimations = new HashMap<AbstractSettingComponent, Float>();
    private Map<ModuleStructure, Float> hoverAnimations = new HashMap<ModuleStructure, Float>();
    private Map<ModuleStructure, Float> stateAnimations = new HashMap<ModuleStructure, Float>();
    private Map<ModuleStructure, Float> selectedIconAnimations = new HashMap<ModuleStructure, Float>();
    private Map<ModuleStructure, Float> favoriteAnimations = new HashMap<ModuleStructure, Float>();
    private Map<ModuleStructure, Float> positionAnimations = new HashMap<ModuleStructure, Float>();
    private Map<ModuleStructure, Float> moduleAlphaAnimations = new HashMap<ModuleStructure, Float>();
    private Map<ModuleStructure, Float> bindBoxWidthAnimations = new HashMap<ModuleStructure, Float>();
    private Map<ModuleStructure, Float> bindBoxAlphaAnimations = new HashMap<ModuleStructure, Float>();
    private List<ModuleStructure> oldModules = new ArrayList<ModuleStructure>();
    private double oldModuleDisplayScroll = 0.0;
    private float selectedPulseAnimation = 0.0f;
    private long lastHoverUpdateTime = System.currentTimeMillis();
    private long lastStateUpdateTime = System.currentTimeMillis();
    private long lastIconUpdateTime = System.currentTimeMillis();
    private long lastFavoriteUpdateTime = System.currentTimeMillis();
    private long lastBindUpdateTime = System.currentTimeMillis();
    private long lastVisibilityUpdateTime = System.currentTimeMillis();
    private ModuleStructure highlightedModule = null;
    private long highlightStartTime = 0L;
    private float highlightAnimation = 0.0f;
    private boolean scrollToModule = false;
    private ModuleStructure scrollTargetModule = null;
    private boolean isCategoryTransitioning = false;
    private float categoryTransitionProgress = 1.0f;
    private long categoryTransitionStartTime = 0L;
    private static final float MODULE_ANIM_DURATION = 300.0f;
    private static final float SETTING_ANIM_DURATION = 450.0f;
    private static final float CATEGORY_TRANSITION_DURATION = 280.0f;
    private static final float HIGHLIGHT_DURATION = 2000.0f;
    private static final float HOVER_ANIM_SPEED = 8.0f;
    private static final float STATE_ANIM_SPEED = 10.0f;
    private static final float ICON_ANIM_SPEED = 10.0f;
    private static final float FAVORITE_ANIM_SPEED = 8.0f;
    private static final float POSITION_ANIM_SPEED = 6.0f;
    private static final float BIND_WIDTH_ANIM_SPEED = 12.0f;
    private static final float PULSE_SPEED = 5.5f;
    private static final float VISIBILITY_ANIM_SPEED = 8.0f;
    private static final float HEIGHT_ANIM_SPEED = 10.0f;
    private static final float CORNER_INSET = 3.0f;
    private static final float MODULE_ITEM_HEIGHT = 22.0f;

    public void prepareTransition(List<ModuleStructure> modules, List<ModuleStructure> displayModules) {
        if (!modules.isEmpty()) {
            this.oldModules = new ArrayList<ModuleStructure>(modules);
            this.oldModuleAnimations = new HashMap<ModuleStructure, Float>(this.moduleAnimations);
            this.isCategoryTransitioning = true;
            this.categoryTransitionStartTime = System.currentTimeMillis();
            this.categoryTransitionProgress = 0.0f;
        }
    }

    public void initModuleAnimations(List<ModuleStructure> displayModules) {
        this.moduleAnimations.clear();
        this.moduleAnimStartTimes.clear();
        this.hoverAnimations.clear();
        this.stateAnimations.clear();
        this.selectedIconAnimations.clear();
        this.bindBoxWidthAnimations.clear();
        this.bindBoxAlphaAnimations.clear();
        long currentTime = System.currentTimeMillis();
        long delayBase = 84L;
        for (int i = 0; i < displayModules.size(); ++i) {
            ModuleStructure mod = displayModules.get(i);
            this.moduleAnimations.put(mod, Float.valueOf(0.0f));
            this.moduleAnimStartTimes.put(mod, currentTime + delayBase + (long)i * 25L);
            this.hoverAnimations.put(mod, Float.valueOf(0.0f));
            this.stateAnimations.put(mod, Float.valueOf(mod.isState() ? 1.0f : 0.0f));
            this.selectedIconAnimations.put(mod, Float.valueOf(0.0f));
            this.favoriteAnimations.put(mod, Float.valueOf(mod.isFavorite() ? 1.0f : 0.0f));
            this.positionAnimations.put(mod, Float.valueOf(1.0f));
            this.moduleAlphaAnimations.put(mod, Float.valueOf(1.0f));
        }
    }

    public void initSettingAnimations(List<AbstractSettingComponent> settingComponents) {
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < settingComponents.size(); ++i) {
            AbstractSettingComponent comp = settingComponents.get(i);
            this.settingAnimations.put(comp, Float.valueOf(0.0f));
            this.settingAnimStartTimes.put(comp, currentTime + (long)i * 25L);
            boolean visible = comp.getSetting().isVisible();
            this.visibilityAnimations.put(comp, Float.valueOf(visible ? 1.0f : 0.0f));
            this.heightAnimations.put(comp, Float.valueOf(visible ? 1.0f : 0.0f));
        }
    }

    public void clearSettingAnimations() {
        this.settingAnimations.clear();
        this.settingAnimStartTimes.clear();
        this.visibilityAnimations.clear();
        this.heightAnimations.clear();
    }

    public void updateAll(List<ModuleStructure> displayModules, ModuleStructure selectedModule, float mouseX, float mouseY, float listX, float listY, float listWidth, float listHeight, float scrollOffset) {
        this.updateCategoryTransition();
        this.updateModuleAnimations(displayModules);
        this.updateStateAnimations(displayModules);
        this.updateSelectedIconAnimations(displayModules, selectedModule);
        this.updateFavoriteAnimations(displayModules);
        this.updateBindAnimations(displayModules);
        this.updateHighlightAnimation();
        this.updateHoverAnimations(displayModules, mouseX, mouseY, listX, listY, listWidth, listHeight, scrollOffset);
    }

    private void updateCategoryTransition() {
        if (!this.isCategoryTransitioning) {
            return;
        }
        long elapsed = System.currentTimeMillis() - this.categoryTransitionStartTime;
        float progress = Math.min(1.0f, (float)elapsed / 280.0f);
        this.categoryTransitionProgress = this.easeOutCubic(progress);
        if (progress >= 1.0f) {
            this.isCategoryTransitioning = false;
            this.oldModules.clear();
            this.oldModuleAnimations.clear();
            this.categoryTransitionProgress = 1.0f;
        }
    }

    private void updateModuleAnimations(List<ModuleStructure> displayModules) {
        long currentTime = System.currentTimeMillis();
        for (ModuleStructure mod : displayModules) {
            Long startTime = this.moduleAnimStartTimes.get(mod);
            if (startTime == null) continue;
            float elapsed = currentTime - startTime;
            float progress = Math.min(1.0f, Math.max(0.0f, elapsed / 300.0f));
            progress = this.easeOutCubic(progress);
            this.moduleAnimations.put(mod, Float.valueOf(progress));
        }
    }

    private void updateStateAnimations(List<ModuleStructure> displayModules) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastStateUpdateTime) / 1000.0f, 0.1f);
        this.lastStateUpdateTime = currentTime;
        Iterator<ModuleStructure> iterator = displayModules.iterator();
        while (iterator.hasNext()) {
            ModuleStructure module = iterator.next();
            float currentAnim = this.stateAnimations.getOrDefault(module, Float.valueOf(module.isState() ? 1.0f : 0.0f)).floatValue();
            float targetAnim = module.isState() ? 1.0f : 0.0f;
            this.stateAnimations.put(module, Float.valueOf(this.animateTowards(currentAnim, targetAnim, 10.0f, deltaTime)));
        }
    }

    private void updateSelectedIconAnimations(List<ModuleStructure> displayModules, ModuleStructure selectedModule) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastIconUpdateTime) / 1000.0f, 0.1f);
        this.lastIconUpdateTime = currentTime;
        for (ModuleStructure module : displayModules) {
            float currentAnim = this.selectedIconAnimations.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
            float targetAnim = module == selectedModule ? 1.0f : 0.0f;
            this.selectedIconAnimations.put(module, Float.valueOf(this.animateTowards(currentAnim, targetAnim, 10.0f, deltaTime)));
        }
    }

    private void updateFavoriteAnimations(List<ModuleStructure> displayModules) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastFavoriteUpdateTime) / 1000.0f, 0.1f);
        this.lastFavoriteUpdateTime = currentTime;
        for (ModuleStructure module : displayModules) {
            float currentAlphaAnim;
            float currentFavAnim = this.favoriteAnimations.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
            float targetFavAnim = module.isFavorite() ? 1.0f : 0.0f;
            this.favoriteAnimations.put(module, Float.valueOf(this.animateTowards(currentFavAnim, targetFavAnim, 8.0f, deltaTime)));
            float currentPosAnim = this.positionAnimations.getOrDefault(module, Float.valueOf(1.0f)).floatValue();
            if (currentPosAnim < 1.0f) {
                this.positionAnimations.put(module, Float.valueOf(Math.min(1.0f, currentPosAnim + 6.0f * deltaTime)));
            }
            if (!((currentAlphaAnim = this.moduleAlphaAnimations.getOrDefault(module, Float.valueOf(1.0f)).floatValue()) < 1.0f)) continue;
            this.moduleAlphaAnimations.put(module, Float.valueOf(Math.min(1.0f, currentAlphaAnim + 6.0f * deltaTime)));
        }
    }

    private void updateBindAnimations(List<ModuleStructure> displayModules) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastBindUpdateTime) / 1000.0f, 0.1f);
        this.lastBindUpdateTime = currentTime;
        for (ModuleStructure module : displayModules) {
            int key = module.getKey();
            boolean hasBind = key != -1 && key != -1;
            float currentAlpha = this.bindBoxAlphaAnimations.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
            float targetAlpha = hasBind ? 1.0f : 0.0f;
            this.bindBoxAlphaAnimations.put(module, Float.valueOf(this.animateTowards(currentAlpha, targetAlpha, 12.0f, deltaTime)));
        }
    }

    private void updateHoverAnimations(List<ModuleStructure> displayModules, float mouseX, float mouseY, float listX, float listY, float listWidth, float listHeight, float scrollOffset) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastHoverUpdateTime) / 1000.0f, 0.1f);
        this.lastHoverUpdateTime = currentTime;
        this.selectedPulseAnimation += deltaTime * 5.5f;
        if ((double)this.selectedPulseAnimation > Math.PI * 2) {
            this.selectedPulseAnimation -= (float)Math.PI * 2;
        }
        float topInset = 3.0f;
        float bottomInset = 3.0f;
        float startY = listY + topInset + 2.0f + scrollOffset;
        float itemHeight = 22.0f;
        float visibleTop = listY + topInset;
        float visibleBottom = listY + listHeight - bottomInset;
        for (int i = 0; i < displayModules.size(); ++i) {
            ModuleStructure module = displayModules.get(i);
            float modY = startY + (float)i * (itemHeight + 2.0f);
            boolean isInVisibleArea = modY + itemHeight >= visibleTop && modY <= visibleBottom;
            boolean isHovered = !this.isCategoryTransitioning && isInVisibleArea && mouseX >= listX + 3.0f && mouseX <= listX + listWidth - 3.0f && mouseY >= Math.max(modY, visibleTop) && mouseY <= Math.min(modY + itemHeight, visibleBottom) && mouseY >= modY && mouseY <= modY + itemHeight;
            float currentHover = this.hoverAnimations.getOrDefault(module, Float.valueOf(0.0f)).floatValue();
            float targetHover = isHovered ? 1.0f : 0.0f;
            this.hoverAnimations.put(module, Float.valueOf(this.animateTowards(currentHover, targetHover, 8.0f, deltaTime)));
        }
    }

    private void updateHighlightAnimation() {
        if (this.highlightedModule == null) {
            return;
        }
        long elapsed = System.currentTimeMillis() - this.highlightStartTime;
        if ((float)elapsed >= 2000.0f) {
            long fadeElapsed = elapsed - 2000L;
            float fadeProgress = (float)fadeElapsed / 500.0f;
            if (fadeProgress >= 1.0f) {
                this.highlightedModule = null;
                this.highlightAnimation = 0.0f;
            } else {
                this.highlightAnimation = 1.0f - fadeProgress;
            }
        } else {
            this.highlightAnimation = 1.0f;
        }
    }

    public void updateSettingAnimations(List<AbstractSettingComponent> settingComponents) {
        long currentTime = System.currentTimeMillis();
        for (AbstractSettingComponent comp : settingComponents) {
            Long startTime = this.settingAnimStartTimes.get(comp);
            if (startTime == null) continue;
            float elapsed = currentTime - startTime;
            float progress = Math.min(1.0f, Math.max(0.0f, elapsed / 450.0f));
            progress = this.easeOutCubic(progress);
            this.settingAnimations.put(comp, Float.valueOf(progress));
        }
    }

    public void updateVisibilityAnimations(List<AbstractSettingComponent> settingComponents) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastVisibilityUpdateTime) / 1000.0f, 0.1f);
        this.lastVisibilityUpdateTime = currentTime;
        Iterator<AbstractSettingComponent> iterator = settingComponents.iterator();
        while (iterator.hasNext()) {
            AbstractSettingComponent comp;
            boolean isVisible = (comp = iterator.next()).getSetting().isVisible();
            float currentVisAnim = this.visibilityAnimations.getOrDefault(comp, Float.valueOf(isVisible ? 1.0f : 0.0f)).floatValue();
            float currentHeightAnim = this.heightAnimations.getOrDefault(comp, Float.valueOf(isVisible ? 1.0f : 0.0f)).floatValue();
            float visTarget = isVisible ? 1.0f : 0.0f;
            float heightTarget = isVisible ? 1.0f : 0.0f;
            this.heightAnimations.put(comp, Float.valueOf(this.animateTowards(currentHeightAnim, heightTarget, 10.0f, deltaTime)));
            this.visibilityAnimations.put(comp, Float.valueOf(this.animateTowards(currentVisAnim, visTarget, 8.0f, deltaTime)));
        }
    }

    public void startHighlight(ModuleStructure module) {
        this.highlightedModule = module;
        this.highlightStartTime = System.currentTimeMillis();
        this.highlightAnimation = 1.0f;
    }

    public void setScrollTarget(ModuleStructure module) {
        this.scrollToModule = true;
        this.scrollTargetModule = module;
    }

    public boolean shouldScrollToModule() {
        return this.scrollToModule;
    }

    public void clearScrollTarget() {
        this.scrollToModule = false;
        this.scrollTargetModule = null;
    }

    private float animateTowards(float current, float target, float speed, float deltaTime) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) {
            return target;
        }
        return current + diff * speed * deltaTime;
    }

    private float easeOutCubic(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 3.0);
    }

    public float easeInCubic(float x) {
        return x * x * x;
    }

    public float easeOutQuart(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 4.0);
    }

    public float getCategorySlideDistance() {
        return 40.0f;
    }

    @Generated
    public Map<ModuleStructure, Float> getModuleAnimations() {
        return this.moduleAnimations;
    }

    @Generated
    public Map<ModuleStructure, Long> getModuleAnimStartTimes() {
        return this.moduleAnimStartTimes;
    }

    @Generated
    public Map<ModuleStructure, Float> getOldModuleAnimations() {
        return this.oldModuleAnimations;
    }

    @Generated
    public Map<AbstractSettingComponent, Float> getSettingAnimations() {
        return this.settingAnimations;
    }

    @Generated
    public Map<AbstractSettingComponent, Long> getSettingAnimStartTimes() {
        return this.settingAnimStartTimes;
    }

    @Generated
    public Map<AbstractSettingComponent, Float> getVisibilityAnimations() {
        return this.visibilityAnimations;
    }

    @Generated
    public Map<AbstractSettingComponent, Float> getHeightAnimations() {
        return this.heightAnimations;
    }

    @Generated
    public Map<ModuleStructure, Float> getHoverAnimations() {
        return this.hoverAnimations;
    }

    @Generated
    public Map<ModuleStructure, Float> getStateAnimations() {
        return this.stateAnimations;
    }

    @Generated
    public Map<ModuleStructure, Float> getSelectedIconAnimations() {
        return this.selectedIconAnimations;
    }

    @Generated
    public Map<ModuleStructure, Float> getFavoriteAnimations() {
        return this.favoriteAnimations;
    }

    @Generated
    public Map<ModuleStructure, Float> getPositionAnimations() {
        return this.positionAnimations;
    }

    @Generated
    public Map<ModuleStructure, Float> getModuleAlphaAnimations() {
        return this.moduleAlphaAnimations;
    }

    @Generated
    public Map<ModuleStructure, Float> getBindBoxWidthAnimations() {
        return this.bindBoxWidthAnimations;
    }

    @Generated
    public Map<ModuleStructure, Float> getBindBoxAlphaAnimations() {
        return this.bindBoxAlphaAnimations;
    }

    @Generated
    public List<ModuleStructure> getOldModules() {
        return this.oldModules;
    }

    @Generated
    public double getOldModuleDisplayScroll() {
        return this.oldModuleDisplayScroll;
    }

    @Generated
    public float getSelectedPulseAnimation() {
        return this.selectedPulseAnimation;
    }

    @Generated
    public long getLastHoverUpdateTime() {
        return this.lastHoverUpdateTime;
    }

    @Generated
    public long getLastStateUpdateTime() {
        return this.lastStateUpdateTime;
    }

    @Generated
    public long getLastIconUpdateTime() {
        return this.lastIconUpdateTime;
    }

    @Generated
    public long getLastFavoriteUpdateTime() {
        return this.lastFavoriteUpdateTime;
    }

    @Generated
    public long getLastBindUpdateTime() {
        return this.lastBindUpdateTime;
    }

    @Generated
    public long getLastVisibilityUpdateTime() {
        return this.lastVisibilityUpdateTime;
    }

    @Generated
    public ModuleStructure getHighlightedModule() {
        return this.highlightedModule;
    }

    @Generated
    public long getHighlightStartTime() {
        return this.highlightStartTime;
    }

    @Generated
    public float getHighlightAnimation() {
        return this.highlightAnimation;
    }

    @Generated
    public boolean isScrollToModule() {
        return this.scrollToModule;
    }

    @Generated
    public ModuleStructure getScrollTargetModule() {
        return this.scrollTargetModule;
    }

    @Generated
    public boolean isCategoryTransitioning() {
        return this.isCategoryTransitioning;
    }

    @Generated
    public float getCategoryTransitionProgress() {
        return this.categoryTransitionProgress;
    }

    @Generated
    public long getCategoryTransitionStartTime() {
        return this.categoryTransitionStartTime;
    }

    @Generated
    public void setModuleAnimations(Map<ModuleStructure, Float> moduleAnimations) {
        this.moduleAnimations = moduleAnimations;
    }

    @Generated
    public void setModuleAnimStartTimes(Map<ModuleStructure, Long> moduleAnimStartTimes) {
        this.moduleAnimStartTimes = moduleAnimStartTimes;
    }

    @Generated
    public void setOldModuleAnimations(Map<ModuleStructure, Float> oldModuleAnimations) {
        this.oldModuleAnimations = oldModuleAnimations;
    }

    @Generated
    public void setSettingAnimations(Map<AbstractSettingComponent, Float> settingAnimations) {
        this.settingAnimations = settingAnimations;
    }

    @Generated
    public void setSettingAnimStartTimes(Map<AbstractSettingComponent, Long> settingAnimStartTimes) {
        this.settingAnimStartTimes = settingAnimStartTimes;
    }

    @Generated
    public void setVisibilityAnimations(Map<AbstractSettingComponent, Float> visibilityAnimations) {
        this.visibilityAnimations = visibilityAnimations;
    }

    @Generated
    public void setHeightAnimations(Map<AbstractSettingComponent, Float> heightAnimations) {
        this.heightAnimations = heightAnimations;
    }

    @Generated
    public void setHoverAnimations(Map<ModuleStructure, Float> hoverAnimations) {
        this.hoverAnimations = hoverAnimations;
    }

    @Generated
    public void setStateAnimations(Map<ModuleStructure, Float> stateAnimations) {
        this.stateAnimations = stateAnimations;
    }

    @Generated
    public void setSelectedIconAnimations(Map<ModuleStructure, Float> selectedIconAnimations) {
        this.selectedIconAnimations = selectedIconAnimations;
    }

    @Generated
    public void setFavoriteAnimations(Map<ModuleStructure, Float> favoriteAnimations) {
        this.favoriteAnimations = favoriteAnimations;
    }

    @Generated
    public void setPositionAnimations(Map<ModuleStructure, Float> positionAnimations) {
        this.positionAnimations = positionAnimations;
    }

    @Generated
    public void setModuleAlphaAnimations(Map<ModuleStructure, Float> moduleAlphaAnimations) {
        this.moduleAlphaAnimations = moduleAlphaAnimations;
    }

    @Generated
    public void setBindBoxWidthAnimations(Map<ModuleStructure, Float> bindBoxWidthAnimations) {
        this.bindBoxWidthAnimations = bindBoxWidthAnimations;
    }

    @Generated
    public void setBindBoxAlphaAnimations(Map<ModuleStructure, Float> bindBoxAlphaAnimations) {
        this.bindBoxAlphaAnimations = bindBoxAlphaAnimations;
    }

    @Generated
    public void setOldModules(List<ModuleStructure> oldModules) {
        this.oldModules = oldModules;
    }

    @Generated
    public void setOldModuleDisplayScroll(double oldModuleDisplayScroll) {
        this.oldModuleDisplayScroll = oldModuleDisplayScroll;
    }

    @Generated
    public void setSelectedPulseAnimation(float selectedPulseAnimation) {
        this.selectedPulseAnimation = selectedPulseAnimation;
    }

    @Generated
    public void setLastHoverUpdateTime(long lastHoverUpdateTime) {
        this.lastHoverUpdateTime = lastHoverUpdateTime;
    }

    @Generated
    public void setLastStateUpdateTime(long lastStateUpdateTime) {
        this.lastStateUpdateTime = lastStateUpdateTime;
    }

    @Generated
    public void setLastIconUpdateTime(long lastIconUpdateTime) {
        this.lastIconUpdateTime = lastIconUpdateTime;
    }

    @Generated
    public void setLastFavoriteUpdateTime(long lastFavoriteUpdateTime) {
        this.lastFavoriteUpdateTime = lastFavoriteUpdateTime;
    }

    @Generated
    public void setLastBindUpdateTime(long lastBindUpdateTime) {
        this.lastBindUpdateTime = lastBindUpdateTime;
    }

    @Generated
    public void setLastVisibilityUpdateTime(long lastVisibilityUpdateTime) {
        this.lastVisibilityUpdateTime = lastVisibilityUpdateTime;
    }

    @Generated
    public void setHighlightedModule(ModuleStructure highlightedModule) {
        this.highlightedModule = highlightedModule;
    }

    @Generated
    public void setHighlightStartTime(long highlightStartTime) {
        this.highlightStartTime = highlightStartTime;
    }

    @Generated
    public void setHighlightAnimation(float highlightAnimation) {
        this.highlightAnimation = highlightAnimation;
    }

    @Generated
    public void setScrollToModule(boolean scrollToModule) {
        this.scrollToModule = scrollToModule;
    }

    @Generated
    public void setScrollTargetModule(ModuleStructure scrollTargetModule) {
        this.scrollTargetModule = scrollTargetModule;
    }

    @Generated
    public void setCategoryTransitioning(boolean isCategoryTransitioning) {
        this.isCategoryTransitioning = isCategoryTransitioning;
    }

    @Generated
    public void setCategoryTransitionProgress(float categoryTransitionProgress) {
        this.categoryTransitionProgress = categoryTransitionProgress;
    }

    @Generated
    public void setCategoryTransitionStartTime(long categoryTransitionStartTime) {
        this.categoryTransitionStartTime = categoryTransitionStartTime;
    }
}

