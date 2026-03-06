
package rich.screens.clickgui.impl.background;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import rich.IMinecraft;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.background.render.AvatarRenderer;
import rich.screens.clickgui.impl.background.render.BackgroundRenderer;
import rich.screens.clickgui.impl.background.render.CategoryRenderer;
import rich.screens.clickgui.impl.background.render.HeaderRenderer;
import rich.screens.clickgui.impl.background.search.SearchHandler;
import rich.screens.clickgui.impl.background.search.SearchRenderer;

public class BackgroundComponent
implements IMinecraft {
    public static final int BG_WIDTH = 400;
    public static final int BG_HEIGHT = 250;
    private final BackgroundRenderer backgroundRenderer;
    private final CategoryRenderer categoryRenderer;
    private final HeaderRenderer headerRenderer;
    private final AvatarRenderer avatarRenderer;
    private final SearchHandler searchHandler;
    private final SearchRenderer searchRenderer;
    private ModuleCategory previousCategory = null;
    private ModuleCategory currentCategory = null;
    private float headerTransition = 1.0f;
    private static final float HEADER_SPEED = 3.0f;
    private long lastUpdateTime = System.currentTimeMillis();

    public BackgroundComponent() {
        this.backgroundRenderer = new BackgroundRenderer();
        this.categoryRenderer = new CategoryRenderer();
        this.headerRenderer = new HeaderRenderer();
        this.avatarRenderer = new AvatarRenderer();
        this.searchHandler = new SearchHandler();
        this.searchRenderer = new SearchRenderer(this.searchHandler);
    }

    public boolean isSearchActive() {
        return this.searchHandler.isSearchActive();
    }

    public float getSearchPanelAlpha() {
        return this.searchHandler.getSearchPanelAlpha();
    }

    public float getNormalPanelAlpha() {
        return this.searchHandler.getNormalPanelAlpha();
    }

    public void setSearchActive(boolean active) {
        this.searchHandler.setSearchActive(active);
    }

    public String getSearchText() {
        return this.searchHandler.getSearchText();
    }

    public List<ModuleStructure> getSearchResults() {
        return this.searchHandler.getSearchResults();
    }

    public ModuleStructure getSelectedSearchModule() {
        return this.searchHandler.getSelectedSearchModule();
    }

    public void updateAnimations(ModuleCategory selectedCategory, float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0f, 0.1f);
        this.lastUpdateTime = currentTime;
        if (this.currentCategory != selectedCategory) {
            this.previousCategory = this.currentCategory;
            this.currentCategory = selectedCategory;
            this.headerTransition = 0.0f;
        }
        if (this.headerTransition < 1.0f) {
            this.headerTransition += 3.0f * deltaTime;
            if (this.headerTransition > 1.0f) {
                this.headerTransition = 1.0f;
            }
        }
        this.categoryRenderer.updateAnimations(selectedCategory, deltaTime);
        this.searchHandler.updateAnimations(deltaTime);
    }

    public void render(GuiGraphics context, float bgX, float bgY, ModuleCategory selectedCategory, float delta, float alphaMultiplier) {
        this.updateAnimations(selectedCategory, delta);
        this.backgroundRenderer.render(context, bgX, bgY, alphaMultiplier);
        this.avatarRenderer.render(context, bgX, bgY, alphaMultiplier);
    }

    public void renderCategoryPanel(float bgX, float bgY, float alphaMultiplier) {
        this.backgroundRenderer.renderCategoryPanel(bgX, bgY, 250.0f, alphaMultiplier);
    }

    public void renderHeader(float bgX, float bgY, ModuleCategory selectedCategory, float alphaMultiplier) {
        this.headerRenderer.render(bgX, bgY, 400.0f, selectedCategory, this.previousCategory, this.currentCategory, this.headerTransition, this.searchHandler, alphaMultiplier);
    }

    public void renderCategoryNames(float bgX, float bgY, ModuleCategory selectedCategory, float alphaMultiplier) {
        this.categoryRenderer.render(bgX, bgY, selectedCategory, alphaMultiplier);
    }

    public void renderSearchResults(GuiGraphics context, float bgX, float bgY, float mouseX, float mouseY, int guiScale, float alphaMultiplier) {
        this.searchRenderer.render(context, bgX, bgY, 400.0f, 250.0f, mouseX, mouseY, guiScale, alphaMultiplier);
    }

    public boolean handleSearchChar(char chr) {
        return this.searchHandler.handleSearchChar(chr);
    }

    public boolean handleSearchKey(int keyCode) {
        return this.searchHandler.handleSearchKey(keyCode);
    }

    public void handleSearchScroll(double vertical, float panelHeight) {
        this.searchHandler.handleSearchScroll(vertical, panelHeight);
    }

    public boolean isSearchBoxHovered(double mouseX, double mouseY, float bgX, float bgY) {
        return this.headerRenderer.isSearchBoxHovered(mouseX, mouseY, bgX, bgY);
    }

    public ModuleStructure getSearchModuleAtPosition(double mouseX, double mouseY, float bgX, float bgY) {
        return this.searchRenderer.getModuleAtPosition(mouseX, mouseY, bgX, bgY, 400.0f, 250.0f, this.searchHandler);
    }

    public ModuleCategory getCategoryAtPosition(double mouseX, double mouseY, float bgX, float bgY) {
        return this.categoryRenderer.getCategoryAtPosition(mouseX, mouseY, bgX, bgY);
    }
}

