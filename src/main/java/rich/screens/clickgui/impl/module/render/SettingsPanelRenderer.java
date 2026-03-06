
package rich.screens.clickgui.impl.module.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import rich.modules.module.ModuleStructure;
import rich.screens.clickgui.impl.module.handler.ModuleAnimationHandler;
import rich.screens.clickgui.impl.module.handler.ModuleScrollHandler;
import rich.screens.clickgui.impl.settingsrender.ColorComponent;
import rich.screens.clickgui.impl.settingsrender.MultiSelectComponent;
import rich.screens.clickgui.impl.settingsrender.SelectComponent;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class SettingsPanelRenderer {
    private static final float SETTINGS_PANEL_CORNER_RADIUS = 7.0f;
    private static final float CORNER_INSET = 3.0f;
    private static final int SETTING_HEIGHT = 16;
    private static final int SETTING_SPACING = 2;
    private final ModuleAnimationHandler animationHandler;

    public SettingsPanelRenderer(ModuleAnimationHandler animationHandler) {
        this.animationHandler = animationHandler;
    }

    public void render(GuiGraphics context, ModuleStructure selectedModule, List<AbstractSettingComponent> settingComponents, float x, float y, float width, float height, float mouseX, float mouseY, float delta, int guiScale, float alphaMultiplier, ModuleScrollHandler scrollHandler, ModuleAnimationHandler animHandler) {
        float visAnim;
        animHandler.updateSettingAnimations(settingComponents);
        animHandler.updateVisibilityAnimations(settingComponents);
        int panelAlpha = (int)(15.0f * alphaMultiplier);
        int outlineAlpha = (int)(215.0f * alphaMultiplier);
        Render2D.rect(x, y, width, height, new Color(64, 64, 64, panelAlpha).getRGB(), 7.0f);
        Render2D.outline(x, y, width, height, 0.5f, new Color(55, 55, 55, outlineAlpha).getRGB(), 7.0f);
        if (selectedModule == null) {
            String text = "Select a module";
            float textSize = 6.0f;
            float textWidth = Fonts.BOLD.getWidth(text, textSize);
            float textHeight = Fonts.BOLD.getHeight(textSize);
            float centerX = x + (width - textWidth) / 2.0f;
            float centerY = y + (height - textHeight) / 2.0f;
            Fonts.BOLD.draw(text, centerX, centerY, textSize, new Color(100, 100, 100, (int)(150.0f * alphaMultiplier)).getRGB());
            return;
        }
        Fonts.BOLD.draw(selectedModule.getName(), x + 8.0f, y + 8.0f, 7.0f, new Color(255, 255, 255, (int)(200.0f * alphaMultiplier)).getRGB());
        String desc = selectedModule.getDescription();
        if (desc != null && !desc.isEmpty()) {
            Fonts.BOLD.draw((String)(desc.length() > 52 ? desc.substring(0, 55) + "..." : desc), x + 15.0f, y + 20.0f, 5.0f, new Color(128, 128, 128, (int)(150.0f * alphaMultiplier)).getRGB());
            Fonts.GUI_ICONS.draw("C", x + 8.0f, y + 20.0f, 6.0f, new Color(128, 128, 128, (int)(150.0f * alphaMultiplier)).getRGB());
        }
        Render2D.rect(x + 8.0f, y + 30.0f, width - 16.0f, 1.25f, new Color(64, 64, 64, (int)(64.0f * alphaMultiplier)).getRGB(), 10.0f);
        float sideInset = 3.0f;
        float bottomInset = 6.0f;
        float clipY = y + 31.0f;
        float clipH = height - 26.0f - bottomInset;
        float clipX = x + sideInset;
        float clipW = width - sideInset * 2.0f;
        Scissor.enable(clipX, clipY, clipW, clipH, guiScale);
        ArrayList<Float> finalYPositions = new ArrayList<Float>();
        ArrayList<Float> animatedHeights = new ArrayList<Float>();
        float posY = y + 38.0f + (float)scrollHandler.getSettingDisplayScroll();
        for (AbstractSettingComponent c : settingComponents) {
            float heightAnim = animHandler.getHeightAnimations().getOrDefault(c, Float.valueOf(c.getSetting().isVisible() ? 1.0f : 0.0f)).floatValue();
            if (heightAnim <= 0.001f) {
                finalYPositions.add(null);
                animatedHeights.add(Float.valueOf(0.0f));
                continue;
            }
            finalYPositions.add(Float.valueOf(posY));
            float baseHeight = this.getComponentBaseHeight(c);
            float layoutHeight = baseHeight * heightAnim;
            animatedHeights.add(Float.valueOf(layoutHeight));
            posY += layoutHeight + 2.0f * heightAnim;
        }
        float visibleTop = clipY;
        float visibleBottom = clipY + clipH;
        for (int i = 0; i < settingComponents.size(); ++i) {
            AbstractSettingComponent c = settingComponents.get(i);
            Float startY = (Float)finalYPositions.get(i);
            if (startY == null) continue;
            visAnim = animHandler.getVisibilityAnimations().getOrDefault(c, Float.valueOf(c.getSetting().isVisible() ? 1.0f : 0.0f)).floatValue();
            float heightAnim = animHandler.getHeightAnimations().getOrDefault(c, Float.valueOf(c.getSetting().isVisible() ? 1.0f : 0.0f)).floatValue();
            if (visAnim <= 0.001f && heightAnim <= 0.001f) continue;
            float animatedHeight = ((Float)animatedHeights.get(i)).floatValue();
            float progress = animHandler.getSettingAnimations().getOrDefault(c, Float.valueOf(1.0f)).floatValue();
            float componentAlpha = progress * visAnim * alphaMultiplier;
            c.position(x + 8.0f, startY.floatValue());
            c.size(width - 16.0f, 16.0f);
            c.setAlphaMultiplier(componentAlpha);
            if (!(startY.floatValue() + animatedHeight >= visibleTop) || !(startY.floatValue() <= visibleBottom) || !(componentAlpha > 0.01f)) continue;
            float itemClipTop = Math.max(startY.floatValue(), visibleTop);
            float itemClipBottom = Math.min(startY.floatValue() + animatedHeight, visibleBottom);
            float itemClipHeight = itemClipBottom - itemClipTop;
            if (!(itemClipHeight > 0.5f)) continue;
            Scissor.enable(clipX, itemClipTop, clipW, itemClipHeight, guiScale);
            context.pose().pushMatrix();
            c.render(context, (int)mouseX, (int)mouseY, delta);
            context.pose().popMatrix();
            Scissor.disable();
        }
        Scissor.disable();
        boolean hasVisibleSettings = false;
        for (AbstractSettingComponent c : settingComponents) {
            visAnim = animHandler.getVisibilityAnimations().getOrDefault(c, Float.valueOf(0.0f)).floatValue();
            if (!(visAnim > 0.01f)) continue;
            hasVisibleSettings = true;
            break;
        }
        if (!hasVisibleSettings) {
            String text = "This module doesn't have settings";
            float textSize = 6.0f;
            float textWidth = Fonts.BOLD.getWidth(text, textSize);
            float textHeight = Fonts.BOLD.getHeight(textSize);
            float centerX = x + (width - textWidth) / 2.0f;
            float centerY = y + (height - textHeight) / 2.0f + 10.0f;
            Fonts.BOLD.draw(text, centerX, centerY, textSize, new Color(100, 100, 100, (int)(150.0f * alphaMultiplier)).getRGB());
        }
        this.renderScrollFade(x + sideInset, clipY, width - sideInset * 2.0f, clipH, scrollHandler.getSettingScrollTopFade() * alphaMultiplier, scrollHandler.getSettingScrollBottomFade() * alphaMultiplier, 60, 12);
    }

    public float calculateTotalHeight(List<AbstractSettingComponent> settingComponents, ModuleAnimationHandler animHandler) {
        float total = 0.0f;
        for (AbstractSettingComponent c : settingComponents) {
            float heightAnim = animHandler.getHeightAnimations().getOrDefault(c, Float.valueOf(c.getSetting().isVisible() ? 1.0f : 0.0f)).floatValue();
            if (heightAnim <= 0.001f) continue;
            float baseHeight = this.getComponentBaseHeight(c);
            total += (baseHeight + 2.0f) * heightAnim;
        }
        return total;
    }

    private float getComponentBaseHeight(AbstractSettingComponent c) {
        if (c instanceof SelectComponent) {
            return ((SelectComponent)c).getTotalHeight();
        }
        if (c instanceof MultiSelectComponent) {
            return ((MultiSelectComponent)c).getTotalHeight();
        }
        if (c instanceof ColorComponent) {
            return ((ColorComponent)c).getTotalHeight();
        }
        return 16.0f;
    }

    private void renderScrollFade(float x, float y, float w, float h, float topFade, float bottomFade, int alpha, int size) {
        float fadeAlpha;
        int i;
        if (topFade > 0.01f) {
            for (i = 0; i < size; ++i) {
                fadeAlpha = (float)alpha * topFade * (1.0f - (float)i / (float)size);
                Render2D.rect(x, y + (float)i, w, 1.0f, new Color(20, 20, 20, (int)fadeAlpha).getRGB(), 0.0f);
            }
        }
        if (bottomFade > 0.01f) {
            for (i = 0; i < size; ++i) {
                fadeAlpha = (float)alpha * bottomFade * ((float)i / (float)size);
                Render2D.rect(x, y + h - (float)size + (float)i, w, 1.0f, new Color(20, 20, 20, (int)fadeAlpha).getRGB(), 0.0f);
            }
        }
    }
}

