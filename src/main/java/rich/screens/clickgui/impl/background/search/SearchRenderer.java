
package rich.screens.clickgui.impl.background.search;

import java.awt.Color;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import rich.modules.module.ModuleStructure;
import rich.screens.clickgui.impl.background.search.SearchHandler;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class SearchRenderer {
    private final SearchHandler searchHandler;

    public SearchRenderer(SearchHandler searchHandler) {
        this.searchHandler = searchHandler;
    }

    public void render(GuiGraphics context, float bgX, float bgY, float bgWidth, float bgHeight, float mouseX, float mouseY, int guiScale, float alphaMultiplier) {
        if (this.searchHandler.getSearchPanelAlpha() <= 0.01f) {
            return;
        }
        float panelX = bgX + 92.0f;
        float panelY = bgY + 38.0f;
        float panelW = bgWidth - 100.0f;
        float panelH = bgHeight - 46.0f;
        float resultAlpha = this.searchHandler.getSearchPanelAlpha() * alphaMultiplier;
        this.renderPanelBackground(panelX, panelY, panelW, panelH, resultAlpha);
        List<ModuleStructure> results = this.searchHandler.getSearchResults();
        if (results.isEmpty()) {
            this.renderEmptyState(panelX, panelY, panelW, panelH, resultAlpha);
            return;
        }
        Scissor.enable(panelX + 3.0f, panelY + 3.0f, panelW - 6.0f, panelH - 6.0f, 2.0f);
        this.renderResults(panelX, panelY, panelW, panelH, mouseX, mouseY, resultAlpha);
        Scissor.disable();
        this.renderScrollIndicators(panelX, panelY, panelW, panelH, resultAlpha);
    }

    private void renderPanelBackground(float panelX, float panelY, float panelW, float panelH, float resultAlpha) {
        int panelBgAlpha = (int)(15.0f * resultAlpha);
        int outlineAlpha = (int)(215.0f * resultAlpha);
        Render2D.rect(panelX, panelY, panelW, panelH, new Color(64, 64, 64, panelBgAlpha).getRGB(), 7.0f);
        Render2D.outline(panelX, panelY, panelW, panelH, 0.5f, new Color(55, 55, 55, outlineAlpha).getRGB(), 7.0f);
    }

    private void renderEmptyState(float panelX, float panelY, float panelW, float panelH, float resultAlpha) {
        String noResults = this.searchHandler.getSearchText().isEmpty() ? "Start typing to search..." : "No modules found";
        float textSize = 6.0f;
        float textWidth = Fonts.BOLD.getWidth(noResults, textSize);
        float textHeight = Fonts.BOLD.getHeight(textSize);
        float centerX = panelX + (panelW - textWidth) / 2.0f;
        float centerY = panelY + (panelH - textHeight) / 2.0f;
        Fonts.BOLD.draw(noResults, centerX, centerY, textSize, new Color(100, 100, 100, (int)(150.0f * resultAlpha)).getRGB());
    }

    private void renderResults(float panelX, float panelY, float panelW, float panelH, float mouseX, float mouseY, float resultAlpha) {
        List<ModuleStructure> results = this.searchHandler.getSearchResults();
        float startY = panelY + 5.0f + this.searchHandler.getSearchScrollOffset();
        float resultHeight = this.searchHandler.getSearchResultHeight();
        int newHoveredIndex = -1;
        for (int i = 0; i < results.size(); ++i) {
            boolean hovered;
            float itemAnim;
            float itemAlpha;
            ModuleStructure module = results.get(i);
            float itemY = startY + (float)i * (resultHeight + 2.0f);
            if (itemY + resultHeight < panelY || itemY > panelY + panelH || (itemAlpha = (itemAnim = this.searchHandler.getSearchResultAnimations().getOrDefault(module, Float.valueOf(0.0f)).floatValue()) * resultAlpha) <= 0.01f) continue;
            float itemOffsetX = (1.0f - itemAnim) * 20.0f;
            boolean bl = hovered = mouseX >= panelX + 5.0f && mouseX <= panelX + panelW - 5.0f && mouseY >= itemY && mouseY <= itemY + resultHeight;
            if (hovered) {
                newHoveredIndex = i;
            }
            boolean selected = module == this.searchHandler.getSelectedSearchModule();
            this.renderResultItem(module, panelX, itemY, panelW, resultHeight, itemOffsetX, itemAlpha, hovered, selected);
        }
        this.searchHandler.setHoveredSearchIndex(newHoveredIndex);
    }

    private void renderResultItem(ModuleStructure module, float panelX, float itemY, float panelW, float resultHeight, float itemOffsetX, float itemAlpha, boolean hovered, boolean selected) {
        Color bg = selected ? new Color(140, 140, 140, (int)(60.0f * itemAlpha)) : (hovered ? new Color(100, 100, 100, (int)(40.0f * itemAlpha)) : new Color(64, 64, 64, (int)(25.0f * itemAlpha)));
        float itemX = panelX + 5.0f + itemOffsetX;
        float itemW = panelW - 10.0f;
        Render2D.rect(itemX, itemY, itemW, resultHeight, bg.getRGB(), 5.0f);
        if (selected) {
            Render2D.outline(itemX, itemY, itemW, resultHeight, 0.5f, new Color(160, 160, 160, (int)(100.0f * itemAlpha)).getRGB(), 5.0f);
        }
        Color textColor = module.isState() ? new Color(255, 255, 255, (int)(255.0f * itemAlpha)) : new Color(180, 180, 180, (int)(200.0f * itemAlpha));
        Fonts.BOLD.draw(module.getName(), itemX + 5.0f, itemY + 3.0f, 6.0f, textColor.getRGB());
        String categoryName = module.getCategory().getReadableName();
        Color categoryColor = new Color(140, 140, 140, (int)(180.0f * itemAlpha));
        Fonts.BOLD.draw(categoryName, itemX + 5.0f, itemY + 11.0f, 4.0f, categoryColor.getRGB());
        if (module.isState()) {
            float indicatorX = itemX + itemW - 10.0f;
            float indicatorY = itemY + resultHeight / 2.0f - 2.0f;
            Render2D.rect(indicatorX, indicatorY, 4.0f, 4.0f, new Color(100, 200, 100, (int)(200.0f * itemAlpha)).getRGB(), 2.0f);
        }
    }

    private void renderScrollIndicators(float panelX, float panelY, float panelW, float panelH, float resultAlpha) {
        List<ModuleStructure> results = this.searchHandler.getSearchResults();
        float resultHeight = this.searchHandler.getSearchResultHeight();
        float maxScroll = Math.max(0.0f, (float)results.size() * (resultHeight + 2.0f) - panelH + 10.0f);
        if (maxScroll > 0.0f) {
            float fadeAlpha;
            int i;
            if (this.searchHandler.getSearchScrollOffset() < -0.5f) {
                for (i = 0; i < 10; ++i) {
                    fadeAlpha = 60.0f * resultAlpha * (1.0f - (float)i / 10.0f);
                    Render2D.rect(panelX + 3.0f, panelY + 3.0f + (float)i, panelW - 6.0f, 1.0f, new Color(20, 20, 20, (int)fadeAlpha).getRGB(), 0.0f);
                }
            }
            if (this.searchHandler.getSearchScrollOffset() > -maxScroll + 0.5f) {
                for (i = 0; i < 10; ++i) {
                    fadeAlpha = 60.0f * resultAlpha * ((float)i / 10.0f);
                    Render2D.rect(panelX + 3.0f, panelY + panelH - 13.0f + (float)i, panelW - 6.0f, 1.0f, new Color(20, 20, 20, (int)fadeAlpha).getRGB(), 0.0f);
                }
            }
        }
    }

    public ModuleStructure getModuleAtPosition(double mouseX, double mouseY, float bgX, float bgY, float bgWidth, float bgHeight, SearchHandler handler) {
        if (!handler.isSearchActive() || handler.getSearchResults().isEmpty()) {
            return null;
        }
        float panelX = bgX + 92.0f;
        float panelY = bgY + 38.0f;
        float panelW = bgWidth - 100.0f;
        float panelH = bgHeight - 46.0f;
        if (mouseX < (double)(panelX + 5.0f) || mouseX > (double)(panelX + panelW - 5.0f) || mouseY < (double)panelY || mouseY > (double)(panelY + panelH)) {
            return null;
        }
        float startY = panelY + 5.0f + handler.getSearchScrollOffset();
        float resultHeight = handler.getSearchResultHeight();
        List<ModuleStructure> results = handler.getSearchResults();
        for (int i = 0; i < results.size(); ++i) {
            float itemY = startY + (float)i * (resultHeight + 2.0f);
            if (!(mouseY >= (double)itemY) || !(mouseY <= (double)(itemY + resultHeight))) continue;
            return results.get(i);
        }
        return null;
    }
}

