
package rich.screens.clickgui.impl.background.render;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class BackgroundRenderer {
    public void render(GuiGraphics context, float bgX, float bgY, float alphaMultiplier) {
        int baseAlpha = (int)(255.0f * alphaMultiplier);
        int[] gradientColors = new int[]{new Color(26, 26, 26, baseAlpha).getRGB(), new Color(0, 0, 0, baseAlpha).getRGB(), new Color(26, 26, 26, baseAlpha).getRGB(), new Color(0, 0, 0, baseAlpha).getRGB(), new Color(26, 26, 20, baseAlpha).getRGB()};
        Render2D.gradientRect(bgX, bgY, 400.0f, 250.0f, gradientColors, 15.0f);
    }

    public void renderCategoryPanel(float bgX, float bgY, float bgHeight, float alphaMultiplier) {
        int panelAlpha = (int)(25.0f * alphaMultiplier);
        int outlineAlpha = (int)(255.0f * alphaMultiplier);
        int blurAlpha = (int)(155.0f * alphaMultiplier);
        Render2D.rect(bgX + 7.5f, bgY + 7.5f, 80.0f, bgHeight - 15.0f, new Color(128, 128, 128, panelAlpha).getRGB(), 10.0f);
        Render2D.outline(bgX + 7.5f, bgY + 7.5f, 80.0f, bgHeight - 15.0f, 0.5f, new Color(55, 55, 55, outlineAlpha).getRGB(), 10.0f);
        Render2D.outline(bgX + 12.5f, bgY + 220.5f, 70.0f, 17.0f, 0.5f, new Color(55, 55, 55, outlineAlpha).getRGB(), 5.0f);
        Fonts.GUI_ICONS.draw("X", bgX + 21.15f, bgY + 217.5f, 19.0f, new Color(58, 58, 58, outlineAlpha).getRGB());
        Fonts.GUI_ICONS.draw("Y", bgX + 40.0f, bgY + 217.0f, 20.0f, new Color(58, 58, 58, outlineAlpha).getRGB());
        Fonts.GUI_ICONS.draw("Z", bgX + 60.0f, bgY + 217.0f, 20.0f, new Color(58, 58, 58, outlineAlpha).getRGB());
        Render2D.blur(bgX + 12.5f, bgY + 220.5f, 70.0f, 17.0f, 4.0f, 5.0f, new Color(25, 25, 25, blurAlpha).getRGB());
        float textSize = 6.0f;
        String soonText = "Soon...";
        float textWidth = Fonts.BOLD.getWidth(soonText, textSize);
        float textHeight = Fonts.BOLD.getHeight(textSize);
        float centerX = bgX + 12.5f + (70.0f - textWidth) / 2.0f;
        float centerY = bgY + 220.5f + (17.0f - textHeight) / 2.0f;
        Fonts.BOLD.draw(soonText, centerX, centerY, textSize, new Color(150, 150, 150, (int)(200.0f * alphaMultiplier)).getRGB());
    }
}

