
package rich.screens.clickgui.impl.configs.render;

import java.awt.Color;
import rich.screens.clickgui.impl.configs.handler.ConfigDataHandler;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class ConfigHeaderRenderer {
    private final ConfigDataHandler dataHandler;

    public ConfigHeaderRenderer(ConfigDataHandler dataHandler) {
        this.dataHandler = dataHandler;
    }

    public void render(float x, float y, float mouseX, float mouseY, float alpha) {
        Fonts.BOLD.draw("Configurations", x + 10.0f, y + 10.0f, 7.0f, new Color(255, 255, 255, (int)(200.0f * alpha)).getRGB());
        this.renderCreateButton(x, y, mouseX, mouseY, alpha);
        this.renderSeparator(x, y, alpha);
    }

    private void renderCreateButton(float x, float y, float mouseX, float mouseY, float alpha) {
        float buttonX = x + 298.0f - 70.0f;
        float buttonY = y + 8.0f;
        float buttonW = 60.0f;
        float buttonH = 16.0f;
        boolean hovered = mouseX >= buttonX && mouseX <= buttonX + buttonW && mouseY >= buttonY && mouseY <= buttonY + buttonH;
        int bgAlpha = (int)((float)(hovered ? 40 : 25) * alpha);
        int outlineAlpha = (int)((float)(hovered ? 100 : 60) * alpha);
        Render2D.rect(buttonX, buttonY, buttonW, buttonH, new Color(64, 64, 64, bgAlpha).getRGB(), 4.0f);
        Render2D.outline(buttonX, buttonY, buttonW, buttonH, 0.5f, new Color(100, 100, 100, outlineAlpha).getRGB(), 4.0f);
        String text = this.dataHandler.isCreating() ? "Cancel" : "+ Create";
        float textWidth = Fonts.BOLD.getWidth(text, 5.0f);
        Fonts.BOLD.draw(text, buttonX + (buttonW - textWidth) / 2.0f, buttonY + 5.5f, 5.0f, new Color(180, 180, 180, (int)(255.0f * alpha)).getRGB());
    }

    private void renderSeparator(float x, float y, float alpha) {
        Render2D.rect(x + 10.0f, y + 28.0f, 278.0f, 0.5f, new Color(64, 64, 64, (int)(100.0f * alpha)).getRGB(), 0.0f);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float panelX, float panelY) {
        float buttonX = panelX + 298.0f - 70.0f;
        float buttonY = panelY + 8.0f;
        if (mouseX >= (double)buttonX && mouseX <= (double)(buttonX + 60.0f) && mouseY >= (double)buttonY && mouseY <= (double)(buttonY + 16.0f) && button == 0) {
            this.dataHandler.toggleCreating();
            return true;
        }
        return false;
    }
}

