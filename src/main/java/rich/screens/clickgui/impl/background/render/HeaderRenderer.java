
package rich.screens.clickgui.impl.background.render;

import java.awt.Color;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.background.search.SearchHandler;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class HeaderRenderer {
    private static final float HEADER_SLIDE_DISTANCE = 8.0f;

    public void render(float bgX, float bgY, float bgWidth, ModuleCategory selectedCategory, ModuleCategory previousCategory, ModuleCategory currentCategory, float headerTransition, SearchHandler searchHandler, float alphaMultiplier) {
        this.renderHeaderPanel(bgX, bgY, bgWidth, alphaMultiplier);
        this.renderSearchBox(bgX, bgY, searchHandler, alphaMultiplier);
        this.renderCategoryLabel(bgX, bgY, previousCategory, currentCategory, headerTransition, searchHandler, alphaMultiplier);
    }

    private void renderHeaderPanel(float bgX, float bgY, float bgWidth, float alphaMultiplier) {
        int panelAlpha = (int)(25.0f * alphaMultiplier);
        int outlineAlpha = (int)(255.0f * alphaMultiplier);
        Render2D.rect(bgX + 92.0f, bgY + 7.5f, bgWidth - 100.0f, 25.0f, new Color(128, 128, 128, panelAlpha).getRGB(), 8.0f);
        Render2D.outline(bgX + 92.0f, bgY + 7.5f, bgWidth - 100.0f, 25.0f, 0.5f, new Color(55, 55, 55, outlineAlpha).getRGB(), 8.0f);
    }

    private void renderSearchBox(float bgX, float bgY, SearchHandler searchHandler, float alphaMultiplier) {
        float searchBoxX = bgX + 315.0f;
        float searchBoxY = bgY + 12.5f;
        float searchBoxW = 70.0f;
        float searchBoxH = 15.0f;
        int outlineAlpha = (int)(255.0f * alphaMultiplier);
        int panelAlpha = (int)(25.0f * alphaMultiplier);
        Color searchOutline = searchHandler.isSearchActive() ? new Color(180, 180, 180, outlineAlpha) : new Color(55, 55, 55, outlineAlpha);
        int searchBgAlpha = (int)((25.0f + searchHandler.getSearchFocusAnimation() * 15.0f) * alphaMultiplier);
        Render2D.rect(searchBoxX, searchBoxY, searchBoxW, searchBoxH, new Color(40, 40, 45, searchBgAlpha).getRGB(), 4.0f);
        Render2D.outline(searchBoxX, searchBoxY, searchBoxW, searchBoxH, 0.5f, searchOutline.getRGB(), 4.0f);
        float textAreaX = searchBoxX + 5.0f;
        if (searchHandler.isSearchActive() && !searchHandler.getSearchText().isEmpty()) {
            this.renderSearchText(searchBoxX, searchBoxY, searchBoxW, searchBoxH, textAreaX, searchHandler, alphaMultiplier);
        } else if (searchHandler.isSearchActive()) {
            this.renderSearchPlaceholder(searchBoxX, searchBoxY, searchBoxH, textAreaX, searchHandler, alphaMultiplier, true);
        } else {
            Fonts.BOLD.draw("Search Modules...", textAreaX, searchBoxY + 5.0f, 5.0f, new Color(128, 128, 128, outlineAlpha).getRGB());
        }
        Render2D.rect(searchBoxX + 53.0f, searchBoxY + 3.5f, 1.0f, searchBoxH - 7.0f, new Color(128, 128, 128, panelAlpha).getRGB(), 8.0f);
        Fonts.ICONS.draw("U", searchBoxX + 55.0f, searchBoxY + 1.5f, 12.0f, new Color(128, 128, 128, outlineAlpha).getRGB());
    }

    private void renderSearchText(float searchBoxX, float searchBoxY, float searchBoxW, float searchBoxH, float textAreaX, SearchHandler searchHandler, float alphaMultiplier) {
        Scissor.enable(searchBoxX + 3.0f, searchBoxY, searchBoxW - 20.0f, searchBoxH, 2.0f);
        if (searchHandler.hasSearchSelection() && searchHandler.getSearchSelectionAnimation() > 0.01f) {
            this.renderSearchSelection(textAreaX, searchBoxY, searchBoxH, searchHandler, alphaMultiplier);
        }
        Fonts.BOLD.draw(searchHandler.getSearchText(), textAreaX, searchBoxY + 5.0f, 5.0f, new Color(210, 210, 220, (int)(255.0f * alphaMultiplier)).getRGB());
        Scissor.disable();
        if (!searchHandler.hasSearchSelection()) {
            this.renderSearchCursor(textAreaX, searchBoxY, searchBoxH, searchHandler, alphaMultiplier);
        }
    }

    private void renderSearchSelection(float textAreaX, float searchBoxY, float searchBoxH, SearchHandler searchHandler, float alphaMultiplier) {
        int start = searchHandler.getSearchSelectionStart();
        int end = searchHandler.getSearchSelectionEnd();
        String beforeSelection = searchHandler.getSearchText().substring(0, start);
        String selection = searchHandler.getSearchText().substring(start, end);
        float selectionX = textAreaX + Fonts.BOLD.getWidth(beforeSelection, 5.0f);
        float selectionWidth = Fonts.BOLD.getWidth(selection, 5.0f);
        int selAlpha = (int)(100.0f * searchHandler.getSearchSelectionAnimation() * alphaMultiplier);
        Render2D.rect(selectionX, searchBoxY + 2.0f, selectionWidth, searchBoxH - 4.0f, new Color(100, 140, 180, selAlpha).getRGB(), 2.0f);
    }

    private void renderSearchCursor(float textAreaX, float searchBoxY, float searchBoxH, SearchHandler searchHandler, float alphaMultiplier) {
        float cursorAlpha = (float)(Math.sin((double)searchHandler.getSearchCursorBlink() * Math.PI * 2.0) * 0.5 + 0.5);
        if (cursorAlpha > 0.3f) {
            String beforeCursor = searchHandler.getSearchText().substring(0, searchHandler.getSearchCursorPosition());
            float cursorX = textAreaX + Fonts.BOLD.getWidth(beforeCursor, 5.0f);
            int cursorAlphaInt = (int)(255.0f * cursorAlpha * alphaMultiplier);
            Render2D.rect(cursorX, searchBoxY + 3.0f, 0.5f, searchBoxH - 6.0f, new Color(180, 180, 185, cursorAlphaInt).getRGB(), 0.0f);
        }
    }

    private void renderSearchPlaceholder(float searchBoxX, float searchBoxY, float searchBoxH, float textAreaX, SearchHandler searchHandler, float alphaMultiplier, boolean showCursor) {
        float cursorAlpha;
        Fonts.BOLD.draw("Type to search...", textAreaX, searchBoxY + 5.0f, 5.0f, new Color(100, 100, 105, (int)(150.0f * alphaMultiplier)).getRGB());
        if (showCursor && (cursorAlpha = (float)(Math.sin((double)searchHandler.getSearchCursorBlink() * Math.PI * 2.0) * 0.5 + 0.5)) > 0.3f) {
            int cursorAlphaInt = (int)(255.0f * cursorAlpha * alphaMultiplier);
            Render2D.rect(textAreaX, searchBoxY + 3.0f, 0.5f, searchBoxH - 6.0f, new Color(180, 180, 185, cursorAlphaInt).getRGB(), 0.0f);
        }
    }

    private void renderCategoryLabel(float bgX, float bgY, ModuleCategory previousCategory, ModuleCategory currentCategory, float headerTransition, SearchHandler searchHandler, float alphaMultiplier) {
        int searchLabelAlphaInt;
        float searchLabelAlpha;
        float baseX = bgX + 100.0f;
        float baseY = bgY + 16.0f;
        float categoryAlpha = searchHandler.getNormalPanelAlpha() * alphaMultiplier;
        if (categoryAlpha > 0.01f) {
            float eased = this.easeOutQuart(headerTransition);
            if (previousCategory != null && headerTransition < 1.0f) {
                float oldAlpha = (1.0f - eased) * categoryAlpha;
                float oldOffsetY = eased * 8.0f;
                int oldAlphaInt = (int)(128.0f * oldAlpha);
                if (oldAlphaInt > 0) {
                    String oldName = previousCategory.getReadableName();
                    Fonts.BOLD.draw(oldName, baseX, baseY + oldOffsetY, 7.0f, new Color(128, 128, 128, oldAlphaInt).getRGB());
                }
            }
            if (currentCategory != null) {
                float newAlpha = eased * categoryAlpha;
                float newOffsetY = (1.0f - eased) * -8.0f;
                int newAlphaInt = (int)(128.0f * newAlpha);
                if (newAlphaInt > 0) {
                    String newName = currentCategory.getReadableName();
                    Fonts.BOLD.draw(newName, baseX, baseY + newOffsetY, 7.0f, new Color(128, 128, 128, newAlphaInt).getRGB());
                }
            }
        }
        if ((searchLabelAlpha = searchHandler.getSearchPanelAlpha() * alphaMultiplier) > 0.01f && (searchLabelAlphaInt = (int)(180.0f * searchLabelAlpha)) > 0) {
            Object searchLabel = "Search Results";
            String searchText = searchHandler.getSearchText();
            if (!searchText.isEmpty()) {
                searchLabel = "Results for \"" + (String)(searchText.length() > 12 ? searchText.substring(0, 12) + "..." : searchText) + "\"";
            }
            Fonts.BOLD.draw((String)searchLabel, baseX, baseY, 7.0f, new Color(160, 160, 160, searchLabelAlphaInt).getRGB());
        }
    }

    private float easeOutQuart(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 4.0);
    }

    public boolean isSearchBoxHovered(double mouseX, double mouseY, float bgX, float bgY) {
        float searchBoxX = bgX + 315.0f;
        float searchBoxY = bgY + 12.5f;
        float searchBoxW = 70.0f;
        float searchBoxH = 15.0f;
        return mouseX >= (double)searchBoxX && mouseX <= (double)(searchBoxX + searchBoxW) && mouseY >= (double)searchBoxY && mouseY <= (double)(searchBoxY + searchBoxH);
    }
}

