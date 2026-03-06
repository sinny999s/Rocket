
package rich.screens.clickgui.impl.configs.render;

import java.awt.Color;
import rich.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import rich.screens.clickgui.impl.configs.render.ConfigNotificationRenderer;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class ConfigCreateBoxRenderer {
    private final ConfigDataHandler dataHandler;
    private final ConfigNotificationRenderer notificationRenderer;
    private float createBoxAnimation = 0.0f;
    private float cursorBlink = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();

    public ConfigCreateBoxRenderer(ConfigDataHandler dataHandler, ConfigNotificationRenderer notificationRenderer) {
        this.dataHandler = dataHandler;
        this.notificationRenderer = notificationRenderer;
    }

    public void render(float x, float y, float alpha) {
        this.updateAnimations();
        if (this.createBoxAnimation < 0.01f) {
            return;
        }
        float boxY = y + 204.0f - 40.0f;
        float boxAlpha = this.createBoxAnimation * alpha;
        this.renderBackground(x, boxY, boxAlpha);
        this.renderInput(x, boxY, boxAlpha);
        this.renderSaveButton(x, boxY, boxAlpha);
    }

    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0f, 0.1f);
        this.lastUpdateTime = currentTime;
        float targetCreate = this.dataHandler.isCreating() ? 1.0f : 0.0f;
        this.createBoxAnimation += (targetCreate - this.createBoxAnimation) * 14.0f * deltaTime;
        this.cursorBlink += deltaTime * 2.0f;
        if (this.cursorBlink > 1.0f) {
            this.cursorBlink -= 1.0f;
        }
    }

    private void renderBackground(float x, float boxY, float alpha) {
        Render2D.rect(x + 8.0f, boxY, 282.0f, 32.0f, new Color(50, 50, 55, (int)(30.0f * alpha)).getRGB(), 5.0f);
        Render2D.outline(x + 8.0f, boxY, 282.0f, 32.0f, 0.5f, new Color(80, 80, 85, (int)(100.0f * alpha)).getRGB(), 5.0f);
    }

    private void renderInput(float x, float boxY, float alpha) {
        float inputX = x + 15.0f;
        float inputY = boxY + 8.0f;
        float inputW = 198.0f;
        float inputH = 16.0f;
        Render2D.rect(inputX, inputY, inputW, inputH, new Color(40, 40, 45, (int)(40.0f * alpha)).getRGB(), 4.0f);
        Render2D.outline(inputX, inputY, inputW, inputH, 0.5f, new Color(70, 70, 75, (int)(80.0f * alpha)).getRGB(), 4.0f);
        String configName = this.dataHandler.getNewConfigName();
        if (configName.isEmpty()) {
            Fonts.BOLD.draw("Enter config name...", inputX + 5.0f, inputY + 5.0f, 5.0f, new Color(100, 100, 105, (int)(150.0f * alpha)).getRGB());
        } else {
            Fonts.BOLD.draw(configName, inputX + 5.0f, inputY + 5.0f, 5.0f, new Color(210, 210, 220, (int)(255.0f * alpha)).getRGB());
        }
        if (this.dataHandler.isCreating()) {
            this.renderCursor(inputX, inputY, inputH, configName, alpha);
        }
    }

    private void renderCursor(float inputX, float inputY, float inputH, String text, float alpha) {
        float cursorAlpha = (float)(Math.sin((double)this.cursorBlink * Math.PI * 2.0) * 0.5 + 0.5);
        if (cursorAlpha > 0.3f) {
            float cursorX = inputX + 5.0f + Fonts.BOLD.getWidth(text, 5.0f);
            Render2D.rect(cursorX, inputY + 3.0f, 0.5f, inputH - 6.0f, new Color(180, 180, 185, (int)(255.0f * cursorAlpha * alpha)).getRGB(), 0.0f);
        }
    }

    private void renderSaveButton(float x, float boxY, float alpha) {
        float saveX = x + 298.0f - 75.0f;
        float saveY = boxY + 6.0f;
        float saveW = 60.0f;
        float saveH = 20.0f;
        Render2D.rect(saveX, saveY, saveW, saveH, new Color(80, 140, 80, (int)(40.0f * alpha)).getRGB(), 4.0f);
        Render2D.outline(saveX, saveY, saveW, saveH, 0.5f, new Color(100, 180, 100, (int)(80.0f * alpha)).getRGB(), 4.0f);
        float textWidth = Fonts.BOLD.getWidth("Save", 5.0f);
        Fonts.BOLD.draw("Save", saveX + (saveW - textWidth) / 2.0f, saveY + 7.0f, 5.0f, new Color(180, 220, 180, (int)(255.0f * alpha)).getRGB());
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float panelX, float panelY) {
        if (!this.dataHandler.isCreating() || this.createBoxAnimation < 0.5f) {
            return false;
        }
        float saveX = panelX + 298.0f - 75.0f;
        float saveY = panelY + 204.0f - 34.0f;
        if (mouseX >= (double)saveX && mouseX <= (double)(saveX + 60.0f) && mouseY >= (double)saveY && mouseY <= (double)(saveY + 20.0f) && button == 0) {
            this.saveConfig();
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode) {
        if (!this.dataHandler.isCreating()) {
            return false;
        }
        if (keyCode == 259) {
            this.dataHandler.removeLastChar();
            return true;
        }
        if (keyCode == 257) {
            this.saveConfig();
            return true;
        }
        return false;
    }

    public boolean charTyped(char chr) {
        if (!this.dataHandler.isCreating()) {
            return false;
        }
        this.dataHandler.appendChar(chr);
        return true;
    }

    private void saveConfig() {
        String name = this.dataHandler.getNewConfigName();
        if (name.isEmpty()) {
            this.notificationRenderer.show("Enter a config name", ConfigNotificationRenderer.NotificationType.ERROR);
            return;
        }
        if (name.equalsIgnoreCase("autoconfig")) {
            this.notificationRenderer.show("This name is reserved", ConfigNotificationRenderer.NotificationType.ERROR);
            return;
        }
        if (this.dataHandler.saveConfig(name)) {
            this.notificationRenderer.show("Config saved: " + name, ConfigNotificationRenderer.NotificationType.SUCCESS);
            this.dataHandler.clearNewConfigName();
            this.dataHandler.setCreating(false);
        } else {
            this.notificationRenderer.show("Config already exists", ConfigNotificationRenderer.NotificationType.ERROR);
        }
    }
}

