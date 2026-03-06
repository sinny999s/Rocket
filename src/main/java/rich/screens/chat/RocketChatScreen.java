package rich.screens.chat;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.screens.ChatScreen;
import rich.Initialization;
import rich.mixin.ChatScreenAccessor;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class RocketChatScreen extends ChatScreen {

    private static final float BAR_WIDTH = 280.0f;
    private static final float BAR_HEIGHT = 18.0f;
    private static final float BAR_MARGIN = 5.0f;
    private static final float BAR_RADIUS = 5.0f;
    private static final float FONT_SIZE = 6.5f;
    private static final float TEXT_PADDING = 8.0f;

    public RocketChatScreen(String initialText) {
        super(initialText, false);
    }

    @Override
    protected void init() {
        super.init();

        if (this.input != null) {
            Minecraft mc = Minecraft.getInstance();
            int guiScale = mc.getWindow().calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode());
            float scaleFactor = 2.0f / (float) guiScale;

            float fixedX = BAR_MARGIN + TEXT_PADDING;
            float fixedY = Render2D.getFixedScaledHeight() - BAR_HEIGHT - BAR_MARGIN;
            float fixedW = BAR_WIDTH - TEXT_PADDING * 2;

            int guiX = (int)(fixedX * scaleFactor);
            int guiY = (int)((fixedY + (BAR_HEIGHT - 8) / 2.0f) * scaleFactor);
            int guiW = (int)(fixedW * scaleFactor);

            this.input.setPosition(guiX, guiY);
            this.input.setWidth(guiW);
            this.input.setBordered(false);
            this.input.setTextColor(0xFFE0E0E0);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTick) {
        int sw = Render2D.getFixedScaledWidth();
        int sh = Render2D.getFixedScaledHeight();

        float barX = BAR_MARGIN;
        float barY = sh - BAR_HEIGHT - BAR_MARGIN;

        int bgAlpha = 200;
        Render2D.blur(barX, barY, BAR_WIDTH, BAR_HEIGHT, 15.0f, BAR_RADIUS, new Color(0, 0, 0, 40).getRGB());
        Render2D.gradientRect(barX, barY, BAR_WIDTH, BAR_HEIGHT,
                new int[]{
                        new Color(40, 40, 45, bgAlpha).getRGB(),
                        new Color(30, 30, 35, bgAlpha).getRGB(),
                        new Color(40, 40, 45, bgAlpha).getRGB(),
                        new Color(30, 30, 35, bgAlpha).getRGB()
                }, BAR_RADIUS);
        Render2D.outline(barX, barY, BAR_WIDTH, BAR_HEIGHT, 0.4f,
                new Color(100, 100, 110, 120).getRGB(), BAR_RADIUS);

        String text = this.input != null ? this.input.getValue() : "";
        boolean isEmpty = text == null || text.isEmpty();

        if (isEmpty) {
            Fonts.REGULARNEW.draw("Type a message...",
                    barX + TEXT_PADDING, barY + (BAR_HEIGHT - FONT_SIZE) / 2.0f,
                    FONT_SIZE, new Color(130, 130, 130, 180).getRGB());
        } else {
            float maxTextWidth = BAR_WIDTH - TEXT_PADDING * 2;
            String displayText = text;
            float textWidth = Fonts.REGULARNEW.getWidth(displayText, FONT_SIZE);
            if (textWidth > maxTextWidth) {
                while (displayText.length() > 0 && Fonts.REGULARNEW.getWidth(displayText, FONT_SIZE) > maxTextWidth) {
                    displayText = displayText.substring(1);
                }
                displayText = "..." + displayText;
            }
            Fonts.REGULARNEW.draw(displayText,
                    barX + TEXT_PADDING, barY + (BAR_HEIGHT - FONT_SIZE) / 2.0f,
                    FONT_SIZE, new Color(230, 230, 235, 255).getRGB());

            if (System.currentTimeMillis() % 1000 < 500) {
                float cursorX = barX + TEXT_PADDING + Fonts.REGULARNEW.getWidth(displayText, FONT_SIZE) + 1.0f;
                float cursorY = barY + (BAR_HEIGHT - FONT_SIZE) / 2.0f;
                Render2D.rect(cursorX, cursorY, 0.5f, FONT_SIZE, new Color(220, 220, 220, 200).getRGB());
            }
        }

        if (this.input != null) {
            this.input.setPosition(-9999, -9999);
            this.input.render(context, mouseX, mouseY, partialTick);
        }

        try {
            CommandSuggestions suggestions = ((ChatScreenAccessor)(Object)this).rocket$getCommandSuggestions();
            if (suggestions != null) {
                suggestions.render(context, mouseX, mouseY);
            }
        } catch (Exception ignored) {}
    }
}
