
package rich.screens.clickgui.impl.configs.render;

import java.awt.Color;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import rich.screens.clickgui.impl.configs.handler.ConfigAnimationHandler;
import rich.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import rich.screens.clickgui.impl.configs.render.ConfigNotificationRenderer;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class ConfigListRenderer {
    private static final float CONFIG_ITEM_HEIGHT = 24.0f;
    private static final float CONFIG_ITEM_SPACING = 3.0f;
    private static final float HOVER_SPEED = 0.15f;
    private final ConfigAnimationHandler animationHandler;
    private final ConfigDataHandler dataHandler;
    private final ConfigNotificationRenderer notificationRenderer;

    public ConfigListRenderer(ConfigAnimationHandler animationHandler, ConfigDataHandler dataHandler, ConfigNotificationRenderer notificationRenderer) {
        this.animationHandler = animationHandler;
        this.dataHandler = dataHandler;
        this.notificationRenderer = notificationRenderer;
    }

    public void render(GuiGraphics context, float x, float y, float mouseX, float mouseY, int guiScale, float alpha) {
        float listX = x + 8.0f;
        float listY = y + 37.0f;
        float listW = 282.0f;
        float listH = 159.0f;
        if (this.dataHandler.isCreating()) {
            listH -= 40.0f * this.animationHandler.getCreateBoxAnimation();
        }
        this.dataHandler.updateScroll(0.016f);
        this.dataHandler.updateScrollFades(listH);
        Scissor.enable(listX, listY - 8.0f, listW, listH + 15.0f, 2.0f);
        float itemY = listY + (float)this.dataHandler.getScrollOffset();
        for (String config : this.dataHandler.getConfigs()) {
            float itemAlpha = this.animationHandler.getItemAppearAnimation(config);
            if (itemAlpha < 0.01f) {
                itemY += 27.0f;
                continue;
            }
            if (itemY + 24.0f >= listY && itemY <= listY + listH) {
                float itemSlide = (1.0f - itemAlpha) * 15.0f;
                this.renderConfigItem(config, listX + itemSlide, itemY, listW, mouseX, mouseY, alpha * itemAlpha);
            }
            itemY += 27.0f;
        }
        if (this.dataHandler.getConfigs().isEmpty()) {
            this.renderEmptyMessage(x, y, alpha);
        }
        Scissor.disable();
    }

    private void renderConfigItem(String config, float x, float y, float width, float mouseX, float mouseY, float alpha) {
        boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 24.0f;
        boolean isSelected = config.equals(this.dataHandler.getSelectedConfig());
        float hoverAnim = this.animationHandler.getHoverAnimation(config);
        float target = isHovered ? 1.0f : 0.0f;
        hoverAnim += (target - hoverAnim) * 0.15f;
        this.animationHandler.setHoverAnimation(config, hoverAnim);
        this.renderItemBackground(x, y, width, isSelected, hoverAnim, alpha);
        this.renderItemName(config, x, y - 0.5f, alpha);
        this.renderActionButtons(config, x, y - 0.5f, width, mouseX, mouseY, alpha);
    }

    private void renderItemBackground(float x, float y, float width, boolean isSelected, float hoverAnim, float alpha) {
        int bgAlpha = (int)((20.0f + 15.0f * hoverAnim + (float)(isSelected ? 10 : 0)) * alpha);
        int gray = (int)(60.0f + 20.0f * hoverAnim);
        Render2D.rect(x, y, width, 24.0f, new Color(gray, gray, gray, bgAlpha).getRGB(), 5.0f);
        if (isSelected || hoverAnim > 0.01f) {
            int outlineAlpha = (int)((40.0f + 40.0f * hoverAnim) * alpha);
            Render2D.outline(x, y, width, 24.0f, 0.5f, new Color(100, 100, 100, outlineAlpha).getRGB(), 5.0f);
        }
    }

    private void renderItemName(String config, float x, float y, float alpha) {
        Fonts.GUI_ICONS.draw("B", x + 4.0f, y + 4.5f, 16.0f, new Color(220, 220, 220, (int)(25.0f * alpha)).getRGB());
        Fonts.BOLD.draw(config, x + 10.0f, y + 8.0f, 6.0f, new Color(220, 220, 220, (int)(255.0f * alpha)).getRGB());
    }

    private void renderActionButtons(String config, float x, float y, float width, float mouseX, float mouseY, float alpha) {
        float buttonSize = 18.0f;
        float buttonY = y + (24.0f - buttonSize) / 2.0f + 1.0f;
        float deleteButtonX = x + width - buttonSize - 8.0f;
        float refreshButtonX = deleteButtonX - buttonSize - 5.0f;
        float loadButtonX = refreshButtonX - buttonSize - 5.0f;
        this.renderActionButton(loadButtonX, buttonY, buttonSize, "P", 15.0f, 4.0f, 2.0f, mouseX, mouseY, this.animationHandler.getLoadHoverAnimations(), config, new Color(80, 180, 80), alpha);
        this.renderActionButton(refreshButtonX, buttonY, buttonSize, "N", 10.0f, 5.0f, 4.0f, mouseX, mouseY, this.animationHandler.getRefreshHoverAnimations(), config, new Color(80, 140, 200), alpha);
        this.renderActionButton(deleteButtonX, buttonY, buttonSize, "O", 13.0f, 4.5f, 2.5f, mouseX, mouseY, this.animationHandler.getDeleteHoverAnimations(), config, new Color(180, 80, 80), alpha);
    }

    private void renderActionButton(float x, float y, float size, String icon, float iconSize, float iconOffsetX, float iconOffsetY, float mouseX, float mouseY, Map<String, Float> animations, String config, Color hoverColor, float alpha) {
        boolean hovered = mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size;
        float anim = animations.getOrDefault(config, Float.valueOf(0.0f)).floatValue();
        float target = hovered ? 1.0f : 0.0f;
        anim += (target - anim) * 0.15f;
        animations.put(config, Float.valueOf(anim));
        int bgAlpha = (int)((25.0f + 20.0f * anim) * alpha);
        int r = (int)(60.0f + (float)(hoverColor.getRed() - 60) * anim);
        int g = (int)(60.0f + (float)(hoverColor.getGreen() - 60) * anim);
        int b = (int)(60.0f + (float)(hoverColor.getBlue() - 60) * anim);
        Render2D.rect(x, y, size, size, new Color(r, g, b, bgAlpha).getRGB(), 4.0f);
        int iconAlpha = (int)((150.0f + 105.0f * anim) * alpha);
        Fonts.GUI_ICONS.draw(icon, x + iconOffsetX, y + iconOffsetY, iconSize, new Color(200, 200, 200, iconAlpha).getRGB());
    }

    private void renderEmptyMessage(float x, float y, float alpha) {
        String text = "No configs found";
        float textWidth = Fonts.BOLD.getWidth(text, 6.0f);
        Fonts.BOLD.draw(text, x + (298.0f - textWidth) / 2.0f, y + 102.0f, 6.0f, new Color(100, 100, 100, (int)(150.0f * alpha)).getRGB());
    }

    private void renderScrollFade(float x, float y, float w, float h, float topFade, float bottomFade) {
        float fadeAlpha;
        int i;
        int size = 15;
        if (topFade > 0.01f) {
            for (i = 0; i < size; ++i) {
                fadeAlpha = 80.0f * topFade * (1.0f - (float)i / (float)size);
                Render2D.rect(x, y + (float)i, w, 1.0f, new Color(20, 20, 20, (int)fadeAlpha).getRGB(), 0.0f);
            }
        }
        if (bottomFade > 0.01f) {
            for (i = 0; i < size; ++i) {
                fadeAlpha = 80.0f * bottomFade * ((float)i / (float)size);
                Render2D.rect(x, y + h - (float)size + (float)i, w, 1.0f, new Color(20, 20, 20, (int)fadeAlpha).getRGB(), 0.0f);
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float panelX, float panelY) {
        float listX = panelX + 8.0f;
        float listY = panelY + 37.0f;
        float listW = 282.0f;
        float listH = 159.0f;
        if (this.dataHandler.isCreating()) {
            listH -= 40.0f * this.animationHandler.getCreateBoxAnimation();
        }
        if (mouseX >= (double)listX && mouseX <= (double)(listX + listW) && mouseY >= (double)listY && mouseY <= (double)(listY + listH)) {
            float itemY = listY + (float)this.dataHandler.getScrollOffset();
            for (String config : this.dataHandler.getConfigs()) {
                float itemAlpha = this.animationHandler.getItemAppearAnimation(config);
                if (itemAlpha < 0.5f) {
                    itemY += 27.0f;
                    continue;
                }
                if (mouseY >= (double)itemY && mouseY <= (double)(itemY + 24.0f)) {
                    return this.handleItemClick(config, mouseX, mouseY, button, listX, listW, itemY);
                }
                itemY += 27.0f;
            }
        }
        return false;
    }

    private boolean handleItemClick(String config, double mouseX, double mouseY, int button, float listX, float listW, float itemY) {
        float buttonSize = 18.0f;
        float buttonYPos = itemY + (24.0f - buttonSize) / 2.0f + 1.0f;
        float deleteButtonX = listX + listW - buttonSize - 8.0f;
        float refreshButtonX = deleteButtonX - buttonSize - 5.0f;
        float loadButtonX = refreshButtonX - buttonSize - 5.0f;
        if (mouseX >= (double)loadButtonX && mouseX <= (double)(loadButtonX + buttonSize) && mouseY >= (double)buttonYPos && mouseY <= (double)(buttonYPos + buttonSize) && button == 0) {
            if (this.dataHandler.loadConfig(config)) {
                this.notificationRenderer.show("Config loaded: " + config, ConfigNotificationRenderer.NotificationType.SUCCESS);
            } else {
                this.notificationRenderer.show("Config not found", ConfigNotificationRenderer.NotificationType.ERROR);
            }
            return true;
        }
        if (mouseX >= (double)refreshButtonX && mouseX <= (double)(refreshButtonX + buttonSize) && mouseY >= (double)buttonYPos && mouseY <= (double)(buttonYPos + buttonSize) && button == 0) {
            if (this.dataHandler.refreshConfig(config)) {
                this.notificationRenderer.show("Config successfully updated", ConfigNotificationRenderer.NotificationType.INFO);
            } else {
                this.notificationRenderer.show("Error refreshing config", ConfigNotificationRenderer.NotificationType.ERROR);
            }
            return true;
        }
        if (mouseX >= (double)deleteButtonX && mouseX <= (double)(deleteButtonX + buttonSize) && mouseY >= (double)buttonYPos && mouseY <= (double)(buttonYPos + buttonSize) && button == 0) {
            if (this.dataHandler.deleteConfig(config)) {
                this.notificationRenderer.show("Config deleted: " + config, ConfigNotificationRenderer.NotificationType.SUCCESS);
            } else {
                this.notificationRenderer.show("Error deleting config", ConfigNotificationRenderer.NotificationType.ERROR);
            }
            return true;
        }
        if (button == 0) {
            String current = this.dataHandler.getSelectedConfig();
            this.dataHandler.setSelectedConfig(config.equals(current) ? null : config);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double vertical, float panelX, float panelY) {
        if (mouseX >= (double)panelX && mouseX <= (double)(panelX + 298.0f) && mouseY >= (double)panelY && mouseY <= (double)(panelY + 204.0f)) {
            float visibleHeight = 159.0f;
            if (this.dataHandler.isCreating()) {
                visibleHeight -= 40.0f;
            }
            this.dataHandler.handleScroll(vertical, visibleHeight);
            return true;
        }
        return false;
    }
}

