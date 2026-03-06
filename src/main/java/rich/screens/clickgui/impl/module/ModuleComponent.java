
package rich.screens.clickgui.impl.module;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import rich.IMinecraft;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.SettingComponentAdder;
import rich.screens.clickgui.impl.module.handler.ModuleAnimationHandler;
import rich.screens.clickgui.impl.module.handler.ModuleBindHandler;
import rich.screens.clickgui.impl.module.handler.ModuleFavoriteHandler;
import rich.screens.clickgui.impl.module.handler.ModuleScrollHandler;
import rich.screens.clickgui.impl.module.render.ModuleListRenderer;
import rich.screens.clickgui.impl.module.render.SettingsPanelRenderer;
import rich.screens.clickgui.impl.module.util.ModuleDisplayHelper;
import rich.util.interfaces.AbstractComponent;
import rich.util.interfaces.AbstractSettingComponent;

public class ModuleComponent
implements IMinecraft {
    private List<ModuleStructure> modules = new ArrayList<ModuleStructure>();
    private List<ModuleStructure> displayModules = new ArrayList<ModuleStructure>();
    private ModuleStructure selectedModule = null;
    private ModuleStructure bindingModule = null;
    private List<AbstractSettingComponent> settingComponents = new ArrayList<AbstractSettingComponent>();
    private ModuleCategory currentCategory = null;
    private final ModuleAnimationHandler animationHandler = new ModuleAnimationHandler();
    private final ModuleScrollHandler scrollHandler = new ModuleScrollHandler();
    private final ModuleFavoriteHandler favoriteHandler = new ModuleFavoriteHandler();
    private final ModuleBindHandler bindHandler = new ModuleBindHandler();
    private final ModuleListRenderer listRenderer;
    private final SettingsPanelRenderer settingsRenderer;
    private final ModuleDisplayHelper displayHelper = new ModuleDisplayHelper();
    private int savedGuiScale = 1;
    private float lastMouseX = 0.0f;
    private float lastMouseY = 0.0f;
    private float lastListX = 0.0f;
    private float lastListY = 0.0f;
    private float lastListWidth = 0.0f;
    private float lastListHeight = 0.0f;

    public ModuleComponent() {
        this.listRenderer = new ModuleListRenderer(this.animationHandler, this.bindHandler, this.displayHelper);
        this.settingsRenderer = new SettingsPanelRenderer(this.animationHandler);
    }

    public void updateModules(List<ModuleStructure> newModules, ModuleCategory category) {
        if (category == this.currentCategory) {
            return;
        }
        this.animationHandler.prepareTransition(this.modules, this.displayModules);
        this.currentCategory = category;
        this.modules = newModules;
        this.rebuildDisplayList();
        this.scrollHandler.resetModuleScroll();
        this.animationHandler.initModuleAnimations(this.displayModules);
        this.displayHelper.updateModulesWithSettings(this.displayModules);
        if (this.animationHandler.shouldScrollToModule() && this.displayModules.contains(this.animationHandler.getScrollTargetModule())) {
            this.scrollToModuleAndHighlight(this.animationHandler.getScrollTargetModule());
            this.animationHandler.clearScrollTarget();
        } else if (!(this.displayModules.isEmpty() || this.selectedModule != null && this.displayModules.contains(this.selectedModule))) {
            this.selectModule(this.displayModules.get(0));
        } else if (this.displayModules.isEmpty()) {
            this.selectedModule = null;
            this.settingComponents.clear();
        }
    }

    private void rebuildDisplayList() {
        this.displayModules.clear();
        ArrayList<ModuleStructure> favorites = new ArrayList<ModuleStructure>();
        ArrayList<ModuleStructure> nonFavorites = new ArrayList<ModuleStructure>();
        for (ModuleStructure mod : this.modules) {
            if (mod.isFavorite()) {
                favorites.add(mod);
                continue;
            }
            nonFavorites.add(mod);
        }
        this.displayModules.addAll(favorites);
        this.displayModules.addAll(nonFavorites);
    }

    public void toggleFavorite(ModuleStructure module) {
        this.favoriteHandler.toggleFavorite(module, this.displayModules, this.animationHandler);
        this.rebuildDisplayList();
    }

    public void selectModuleFromSearch(ModuleStructure module) {
        this.animationHandler.setScrollTarget(module);
    }

    public void scrollToModuleAndHighlight(ModuleStructure module) {
        if (module == null || !this.displayModules.contains(module)) {
            return;
        }
        this.selectModule(module);
        int moduleIndex = this.displayModules.indexOf(module);
        if (moduleIndex >= 0 && this.scrollHandler.getLastModuleListHeight() > 0.0f) {
            this.scrollHandler.scrollToModule(moduleIndex, this.displayModules.size());
        }
        this.animationHandler.startHighlight(module);
    }

    public void selectModule(ModuleStructure module) {
        if (module == this.selectedModule) {
            return;
        }
        this.selectedModule = module;
        this.scrollHandler.resetSettingScroll();
        this.settingComponents.clear();
        this.animationHandler.clearSettingAnimations();
        if (module == null) {
            return;
        }
        new SettingComponentAdder().addSettingComponent(module.settings(), this.settingComponents);
        this.animationHandler.initSettingAnimations(this.settingComponents);
    }

    public void renderModuleList(GuiGraphics context, float x, float y, float width, float height, float mouseX, float mouseY, int guiScale, float alphaMultiplier) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.lastListX = x;
        this.lastListY = y;
        this.lastListWidth = width;
        this.lastListHeight = height;
        this.animationHandler.updateAll(this.displayModules, this.selectedModule, mouseX, mouseY, x, y, width, height, (float)this.scrollHandler.getModuleDisplayScroll());
        this.listRenderer.render(context, this.displayModules, this.selectedModule, this.bindingModule, x, y, width, height, mouseX, mouseY, guiScale, alphaMultiplier, this.animationHandler, this.scrollHandler);
    }

    public void renderSettingsPanel(GuiGraphics context, float x, float y, float width, float height, float mouseX, float mouseY, float delta, int guiScale, float alphaMultiplier) {
        this.savedGuiScale = guiScale;
        this.settingsRenderer.render(context, this.selectedModule, this.settingComponents, x, y, width, height, mouseX, mouseY, delta, guiScale, alphaMultiplier, this.scrollHandler, this.animationHandler);
    }

    public void updateScroll(float delta, float scrollSpeed) {
        this.scrollHandler.update(delta);
    }

    public void updateScrollFades(float delta, float scrollSpeed, float moduleListHeight, float settingsPanelHeight) {
        this.scrollHandler.updateFades(this.displayModules.size(), this.calculateTotalSettingHeight(), moduleListHeight, settingsPanelHeight);
    }

    public float calculateTotalSettingHeight() {
        return this.settingsRenderer.calculateTotalHeight(this.settingComponents, this.animationHandler);
    }

    public ModuleStructure getModuleAtPosition(double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight) {
        return this.listRenderer.getModuleAtPosition(this.displayModules, mouseX, mouseY, listX, listY, listWidth, listHeight, this.scrollHandler.getModuleDisplayScroll(), this.animationHandler.isCategoryTransitioning());
    }

    public boolean isStarClicked(double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight) {
        return this.listRenderer.isStarClicked(this.displayModules, mouseX, mouseY, listX, listY, listWidth, listHeight, this.scrollHandler.getModuleDisplayScroll(), this.displayHelper, this.animationHandler.isCategoryTransitioning());
    }

    public ModuleStructure getModuleForStarClick(double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight) {
        return this.listRenderer.getModuleForStarClick(this.displayModules, mouseX, mouseY, listX, listY, listWidth, listHeight, this.scrollHandler.getModuleDisplayScroll(), this.displayHelper, this.animationHandler.isCategoryTransitioning());
    }

    public void handleModuleScroll(double vertical, float listHeight) {
        if (this.animationHandler.isCategoryTransitioning()) {
            return;
        }
        this.scrollHandler.handleModuleScroll(vertical, listHeight, this.displayModules.size());
    }

    public void handleSettingScroll(double vertical, float panelHeight) {
        this.scrollHandler.handleSettingScroll(vertical, panelHeight, this.calculateTotalSettingHeight());
    }

    public void tick() {
        this.settingComponents.forEach(AbstractComponent::tick);
    }

    public boolean isTransitioning() {
        return this.animationHandler.isCategoryTransitioning();
    }

    @Generated
    public List<ModuleStructure> getModules() {
        return this.modules;
    }

    @Generated
    public List<ModuleStructure> getDisplayModules() {
        return this.displayModules;
    }

    @Generated
    public ModuleStructure getSelectedModule() {
        return this.selectedModule;
    }

    @Generated
    public ModuleStructure getBindingModule() {
        return this.bindingModule;
    }

    @Generated
    public List<AbstractSettingComponent> getSettingComponents() {
        return this.settingComponents;
    }

    @Generated
    public ModuleCategory getCurrentCategory() {
        return this.currentCategory;
    }

    @Generated
    public ModuleAnimationHandler getAnimationHandler() {
        return this.animationHandler;
    }

    @Generated
    public ModuleScrollHandler getScrollHandler() {
        return this.scrollHandler;
    }

    @Generated
    public ModuleFavoriteHandler getFavoriteHandler() {
        return this.favoriteHandler;
    }

    @Generated
    public ModuleBindHandler getBindHandler() {
        return this.bindHandler;
    }

    @Generated
    public ModuleListRenderer getListRenderer() {
        return this.listRenderer;
    }

    @Generated
    public SettingsPanelRenderer getSettingsRenderer() {
        return this.settingsRenderer;
    }

    @Generated
    public ModuleDisplayHelper getDisplayHelper() {
        return this.displayHelper;
    }

    @Generated
    public int getSavedGuiScale() {
        return this.savedGuiScale;
    }

    @Generated
    public float getLastMouseX() {
        return this.lastMouseX;
    }

    @Generated
    public float getLastMouseY() {
        return this.lastMouseY;
    }

    @Generated
    public float getLastListX() {
        return this.lastListX;
    }

    @Generated
    public float getLastListY() {
        return this.lastListY;
    }

    @Generated
    public float getLastListWidth() {
        return this.lastListWidth;
    }

    @Generated
    public float getLastListHeight() {
        return this.lastListHeight;
    }

    @Generated
    public void setModules(List<ModuleStructure> modules) {
        this.modules = modules;
    }

    @Generated
    public void setDisplayModules(List<ModuleStructure> displayModules) {
        this.displayModules = displayModules;
    }

    @Generated
    public void setSelectedModule(ModuleStructure selectedModule) {
        this.selectedModule = selectedModule;
    }

    @Generated
    public void setBindingModule(ModuleStructure bindingModule) {
        this.bindingModule = bindingModule;
    }

    @Generated
    public void setSettingComponents(List<AbstractSettingComponent> settingComponents) {
        this.settingComponents = settingComponents;
    }

    @Generated
    public void setCurrentCategory(ModuleCategory currentCategory) {
        this.currentCategory = currentCategory;
    }

    @Generated
    public void setSavedGuiScale(int savedGuiScale) {
        this.savedGuiScale = savedGuiScale;
    }

    @Generated
    public void setLastMouseX(float lastMouseX) {
        this.lastMouseX = lastMouseX;
    }

    @Generated
    public void setLastMouseY(float lastMouseY) {
        this.lastMouseY = lastMouseY;
    }

    @Generated
    public void setLastListX(float lastListX) {
        this.lastListX = lastListX;
    }

    @Generated
    public void setLastListY(float lastListY) {
        this.lastListY = lastListY;
    }

    @Generated
    public void setLastListWidth(float lastListWidth) {
        this.lastListWidth = lastListWidth;
    }

    @Generated
    public void setLastListHeight(float lastListHeight) {
        this.lastListHeight = lastListHeight;
    }
}

