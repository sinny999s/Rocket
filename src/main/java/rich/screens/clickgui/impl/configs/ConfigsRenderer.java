
package rich.screens.clickgui.impl.configs;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.configs.handler.ConfigAnimationHandler;
import rich.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import rich.screens.clickgui.impl.configs.render.ConfigCreateBoxRenderer;
import rich.screens.clickgui.impl.configs.render.ConfigHeaderRenderer;
import rich.screens.clickgui.impl.configs.render.ConfigListRenderer;
import rich.screens.clickgui.impl.configs.render.ConfigNotificationRenderer;
import rich.util.render.Render2D;

public class ConfigsRenderer {
    public static final float PANEL_X_OFFSET = 92.0f;
    public static final float PANEL_Y_OFFSET = 38.0f;
    public static final float PANEL_WIDTH = 298.0f;
    public static final float PANEL_HEIGHT = 204.0f;
    public static final float CORNER_RADIUS = 6.0f;
    private final ConfigAnimationHandler animationHandler = new ConfigAnimationHandler();
    private final ConfigDataHandler dataHandler = new ConfigDataHandler(this.animationHandler);
    private final ConfigHeaderRenderer headerRenderer;
    private final ConfigListRenderer listRenderer;
    private final ConfigCreateBoxRenderer createBoxRenderer;
    private final ConfigNotificationRenderer notificationRenderer = new ConfigNotificationRenderer();
    private boolean isActive = false;
    private boolean wasActive = false;

    public ConfigsRenderer() {
        this.headerRenderer = new ConfigHeaderRenderer(this.dataHandler);
        this.listRenderer = new ConfigListRenderer(this.animationHandler, this.dataHandler, this.notificationRenderer);
        this.createBoxRenderer = new ConfigCreateBoxRenderer(this.dataHandler, this.notificationRenderer);
    }

    public void render(GuiGraphics context, float bgX, float bgY, float mouseX, float mouseY, float delta, int guiScale, float alphaMultiplier, ModuleCategory category) {
        if (this.animationHandler.isFullyHidden() && !this.isActive) {
            return;
        }
        if (this.isActive) {
            this.dataHandler.refreshConfigs();
            this.animationHandler.initItemAnimations(this.dataHandler.getConfigs());
        }
        float panelX = bgX + 92.0f;
        float panelY = bgY + 38.0f;
        float slideOffset = (1.0f - this.animationHandler.getPanelSlide()) * 20.0f;
        float finalAlpha = alphaMultiplier * this.animationHandler.getPanelAlpha();
        context.pose().pushMatrix();
        context.pose().translate(slideOffset, 0.0f);
        this.renderPanel(panelX, panelY, finalAlpha);
        this.headerRenderer.render(panelX, panelY, mouseX - slideOffset, mouseY, finalAlpha);
        this.listRenderer.render(context, panelX, panelY, mouseX - slideOffset, mouseY, guiScale, finalAlpha);
        this.createBoxRenderer.render(panelX, panelY, finalAlpha);
        this.notificationRenderer.render(panelX, panelY, finalAlpha);
        context.pose().popMatrix();
    }

    private void renderPanel(float x, float y, float alpha) {
        int panelAlpha = (int)(15.0f * alpha);
        int outlineAlpha = (int)(215.0f * alpha);
        Render2D.rect(x, y, 298.0f, 204.0f, new Color(64, 64, 64, panelAlpha).getRGB(), 6.0f);
        Render2D.outline(x, y, 298.0f, 204.0f, 0.5f, new Color(55, 55, 55, outlineAlpha).getRGB(), 6.0f);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float bgX, float bgY, ModuleCategory category) {
        if (this.animationHandler.getPanelAlpha() < 0.5f) {
            return false;
        }
        float panelX = bgX + 92.0f;
        float panelY = bgY + 38.0f;
        float slideOffset = (1.0f - this.animationHandler.getPanelSlide()) * 20.0f;
        if (this.headerRenderer.mouseClicked(mouseX -= (double)slideOffset, mouseY, button, panelX, panelY)) {
            return true;
        }
        if (this.createBoxRenderer.mouseClicked(mouseX, mouseY, button, panelX, panelY)) {
            return true;
        }
        return this.listRenderer.mouseClicked(mouseX, mouseY, button, panelX, panelY);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double vertical, float bgX, float bgY, ModuleCategory category) {
        if (this.animationHandler.getPanelAlpha() < 0.5f) {
            return false;
        }
        float panelX = bgX + 92.0f;
        float panelY = bgY + 38.0f;
        return this.listRenderer.mouseScrolled(mouseX, mouseY, vertical, panelX, panelY);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.createBoxRenderer.keyPressed(keyCode);
    }

    public boolean charTyped(char chr, int modifiers) {
        return this.createBoxRenderer.charTyped(chr);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
    }

    public boolean isEditing() {
        return this.dataHandler.isCreating();
    }
}

