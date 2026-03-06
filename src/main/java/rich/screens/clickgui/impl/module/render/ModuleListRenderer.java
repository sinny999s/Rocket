
package rich.screens.clickgui.impl.module.render;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import rich.modules.module.ModuleStructure;
import rich.screens.clickgui.impl.module.handler.ModuleAnimationHandler;
import rich.screens.clickgui.impl.module.handler.ModuleBindHandler;
import rich.screens.clickgui.impl.module.handler.ModuleScrollHandler;
import rich.screens.clickgui.impl.module.util.ModuleDisplayHelper;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class ModuleListRenderer {
    private static final float MODULE_ITEM_HEIGHT = 22.0f;
    private static final float MODULE_LIST_CORNER_RADIUS = 6.0f;
    private static final float CORNER_INSET = 3.0f;
    private static final float STATE_BALL_SIZE = 3.0f;
    private static final float STATE_TEXT_OFFSET = 6.0f;
    private static final float BIND_BOX_HEIGHT = 9.0f;
    private static final float BIND_BOX_MIN_WIDTH = 18.0f;
    private static final float BIND_BOX_PADDING = 6.0f;
    private static final float BIND_WIDTH_ANIM_SPEED = 12.0f;
    private final ModuleAnimationHandler animationHandler;
    private final ModuleBindHandler bindHandler;
    private final ModuleDisplayHelper displayHelper;

    public ModuleListRenderer(ModuleAnimationHandler animationHandler, ModuleBindHandler bindHandler, ModuleDisplayHelper displayHelper) {
        this.animationHandler = animationHandler;
        this.bindHandler = bindHandler;
        this.displayHelper = displayHelper;
    }

    public void render(GuiGraphics context, List<ModuleStructure> displayModules, ModuleStructure selectedModule, ModuleStructure bindingModule, float x, float y, float width, float height, float mouseX, float mouseY, int guiScale, float alphaMultiplier, ModuleAnimationHandler animHandler, ModuleScrollHandler scrollHandler) {
        float newScale;
        float newOffsetX;
        float newAlpha;
        int panelAlpha = (int)(15.0f * alphaMultiplier);
        int outlineAlpha = (int)(215.0f * alphaMultiplier);
        Render2D.rect(x, y, width, height, new Color(64, 64, 64, panelAlpha).getRGB(), 6.0f);
        Render2D.outline(x, y, width, height, 0.5f, new Color(55, 55, 55, outlineAlpha).getRGB(), 6.0f);
        float topInset = 3.0f;
        float bottomInset = 3.0f;
        float sideInset = 3.0f;
        Scissor.enable(x + sideInset, y + topInset - 1.5f, width - sideInset * 2.0f, height - topInset - bottomInset + 3.0f, guiScale);
        if (animHandler.isCategoryTransitioning() && !animHandler.getOldModules().isEmpty()) {
            float oldAlpha = (1.0f - animHandler.getCategoryTransitionProgress()) * alphaMultiplier;
            float oldOffsetX = animHandler.easeInCubic(animHandler.getCategoryTransitionProgress()) * -animHandler.getCategorySlideDistance();
            float oldScale = 1.0f - animHandler.getCategoryTransitionProgress() * 0.1f;
            this.renderModuleItems(context, animHandler.getOldModules(), animHandler.getOldModuleAnimations(), selectedModule, bindingModule, x, y, width, height, mouseX, mouseY, oldAlpha, oldOffsetX, oldScale, (float)animHandler.getOldModuleDisplayScroll(), false, topInset, bottomInset, animHandler);
        }
        if (animHandler.isCategoryTransitioning()) {
            float entryProgress = Math.max(0.0f, (animHandler.getCategoryTransitionProgress() - 0.2f) / 0.8f);
            entryProgress = animHandler.easeOutQuart(entryProgress);
            newAlpha = entryProgress * alphaMultiplier;
            newOffsetX = (1.0f - entryProgress) * animHandler.getCategorySlideDistance();
            newScale = 0.9f + entryProgress * 0.1f;
        } else {
            newAlpha = alphaMultiplier;
            newOffsetX = 0.0f;
            newScale = 1.0f;
        }
        this.renderModuleItems(context, displayModules, animHandler.getModuleAnimations(), selectedModule, bindingModule, x, y, width, height, mouseX, mouseY, newAlpha, newOffsetX, newScale, (float)scrollHandler.getModuleDisplayScroll(), true, topInset, bottomInset, animHandler);
        Scissor.disable();
        this.renderScrollFade(x, y + topInset, width, height - topInset - bottomInset, scrollHandler.getModuleScrollTopFade() * alphaMultiplier, scrollHandler.getModuleScrollBottomFade() * alphaMultiplier, 80, 15);
    }

    private void renderModuleItems(GuiGraphics context, List<ModuleStructure> moduleList, Map<ModuleStructure, Float> animations, ModuleStructure selectedModule, ModuleStructure bindingModule, float x, float y, float width, float height, float mouseX, float mouseY, float alphaMultiplier, float offsetX, float scale, float scrollOffset, boolean interactive, float topInset, float bottomInset, ModuleAnimationHandler animHandler) {
        if (alphaMultiplier <= 0.01f) {
            return;
        }
        float startY = y + topInset + 2.0f + scrollOffset;
        float centerY = y + height / 2.0f;
        float visibleTop = y + topInset;
        float visibleBottom = y + height - bottomInset;
        for (int i = 0; i < moduleList.size(); ++i) {
            int bgColor;
            float hoverAnim;
            ModuleStructure module = moduleList.get(i);
            float modY = startY + (float)i * 24.0f;
            if (modY + 22.0f < visibleTop || modY > visibleBottom) continue;
            float itemProgress = animations.getOrDefault(module, Float.valueOf(1.0f)).floatValue();
            float posAnim = animHandler.getPositionAnimations().getOrDefault(module, Float.valueOf(1.0f)).floatValue();
            float alphaAnim = animHandler.getModuleAlphaAnimations().getOrDefault(module, Float.valueOf(1.0f)).floatValue();
            float combinedAlpha = itemProgress * alphaMultiplier * alphaAnim;
            if (combinedAlpha <= 0.01f) continue;
            float itemAnimOffset = (1.0f - itemProgress) * 20.0f;
            float posAnimOffset = (1.0f - this.easeOutCubic(posAnim)) * 15.0f;
            float scaledModY = centerY + (modY - centerY) * scale;
            float scaledHeight = 22.0f * scale;
            float animX = x + 3.0f + offsetX + itemAnimOffset + posAnimOffset;
            boolean selected = interactive && module == selectedModule;
            boolean isHighlighted = interactive && module == animHandler.getHighlightedModule() && animHandler.getHighlightAnimation() > 0.01f;
            float f = hoverAnim = interactive ? animHandler.getHoverAnimations().getOrDefault(module, Float.valueOf(0.0f)).floatValue() : 0.0f;
            float stateAnim = interactive ? animHandler.getStateAnimations().getOrDefault(module, Float.valueOf(module.isState() ? 1.0f : 0.0f)).floatValue() : (module.isState() ? 1.0f : 0.0f);
            float selectedIconAnim = interactive ? animHandler.getSelectedIconAnimations().getOrDefault(module, Float.valueOf(0.0f)).floatValue() : 0.0f;
            float favoriteAnim = interactive ? animHandler.getFavoriteAnimations().getOrDefault(module, Float.valueOf(0.0f)).floatValue() : 0.0f;
            boolean hasSettings = this.displayHelper.hasSettings(module);
            int baseBgAlpha = 25;
            int hoverBgAlpha = 45;
            int selectedBgAlpha = 55;
            int bgAlpha;
            int bgColor2;
            if (selected) {
                bgAlpha = (int)(((float)selectedBgAlpha + hoverAnim * 10.0f) * combinedAlpha);
                bgColor = new Color(71, 71, 71, bgAlpha).getRGB();
            } else {
                bgAlpha = (int)(((float)baseBgAlpha + (float)(hoverBgAlpha - baseBgAlpha) * hoverAnim) * combinedAlpha);
                int gray = (int)(64.0f + 36.0f * hoverAnim);
                bgColor = new Color(gray, gray, gray, bgAlpha).getRGB();
            }
            float scaledWidth = (width - 6.0f) * scale;
            Render2D.rect(animX, scaledModY, scaledWidth, scaledHeight, bgColor, 5.0f);
            if (selected) {
                float pulseValue = (float)(Math.sin(animHandler.getSelectedPulseAnimation()) * 0.5 + 0.5);
                float highlightBoost = isHighlighted ? animHandler.getHighlightAnimation() * 0.5f : 0.0f;
                int baseOutlineAlpha = (int)(80.0f + 80.0f * highlightBoost);
                int pulseOutlineAlpha = (int)(40.0f + 40.0f * highlightBoost);
                int outlineAlpha = (int)(((float)baseOutlineAlpha + (float)pulseOutlineAlpha * pulseValue) * combinedAlpha);
                int baseColorValue = (int)(80.0f + 50.0f * highlightBoost);
                int outlineColorValue = (int)((float)baseColorValue + 30.0f * pulseValue);
                int outlineG = (int)(80.0f + 20.0f * pulseValue + 40.0f * highlightBoost);
                int outlineB = (int)(80.0f + 20.0f * pulseValue + 40.0f * highlightBoost);
                Render2D.outline(animX, scaledModY, scaledWidth, scaledHeight, 0.5f, new Color(Math.min(255, outlineColorValue), Math.min(255, outlineG), Math.min(255, outlineB), outlineAlpha).getRGB(), 5.0f);
            } else if (hoverAnim > 0.01f) {
                int outlineAlpha = (int)(60.0f * hoverAnim * combinedAlpha);
                Render2D.outline(animX, scaledModY, scaledWidth, scaledHeight, 0.5f, new Color(120, 120, 120, outlineAlpha).getRGB(), 5.0f);
            }
            float stateTextOffset = stateAnim * 6.0f;
            if (stateAnim > 0.01f) {
                float ballAlpha = stateAnim * 200.0f * combinedAlpha;
                float ballX = animX + 4.0f;
                float ballY = scaledModY + (scaledHeight - 3.0f * scale) / 2.0f + 1.0f;
                Render2D.rect(ballX, ballY, 3.0f * scale, 3.0f * scale, new Color(255, 255, 255, (int)ballAlpha).getRGB(), 3.0f * scale / 2.0f);
            }
            String name = module.getName();
            int baseGray = 128;
            int targetWhite = 255;
            int textBrightness = (int)((float)baseGray + (float)(targetWhite - baseGray) * stateAnim);
            int textAlphaValue = (int)((180.0f + 75.0f * stateAnim) * combinedAlpha);
            if (hoverAnim > 0.01f && stateAnim < 0.99f) {
                textBrightness = (int)((float)textBrightness + 40.0f * hoverAnim * (1.0f - stateAnim));
                textAlphaValue = (int)((float)textAlphaValue + 40.0f * hoverAnim * (1.0f - stateAnim));
            }
            if (isHighlighted) {
                textBrightness = (int)Math.min(255.0f, (float)textBrightness + 30.0f * animHandler.getHighlightAnimation());
            }
            Color textColor = new Color(textBrightness, textBrightness, textBrightness, Math.min(255, textAlphaValue));
            float textX = animX + 5.0f + stateTextOffset;
            float textY = scaledModY + (scaledHeight - 6.0f * scale) / 2.0f;
            Fonts.BOLD.draw(name, textX, textY, 6.0f * scale, textColor.getRGB());
            if (!interactive) continue;
            this.renderBindBox(module, bindingModule, animX, scaledModY, scaledWidth, scaledHeight, scale, combinedAlpha, stateTextOffset, animHandler);
            float iconBaseX = animX + scaledWidth - 14.0f;
            float iconY = scaledModY + (scaledHeight - 8.0f * scale) / 2.0f;
            float starX = hasSettings ? iconBaseX - 12.0f : iconBaseX;
            int starGray = 50;
            int starR = (int)((float)starGray + (float)(255 - starGray) * favoriteAnim);
            int starG = (int)((float)starGray + (float)(215 - starGray) * favoriteAnim);
            int starB = (int)((float)starGray + (float)(0 - starGray) * favoriteAnim);
            float starAlpha = (80.0f + 120.0f * favoriteAnim + 55.0f * hoverAnim) * combinedAlpha;
            Fonts.GUI_ICONS.draw("D", starX, iconY + 1.0f, 8.0f * scale, new Color(starR, starG, starB, (int)starAlpha).getRGB());
            if (!hasSettings) continue;
            if (selectedIconAnim > 0.01f) {
                float gearAlpha = (150.0f + 50.0f * (isHighlighted ? animHandler.getHighlightAnimation() : 0.0f)) * selectedIconAnim * combinedAlpha;
                Fonts.GUI_ICONS.draw("B", iconBaseX, iconY + 1.0f, 8.0f * scale, new Color(200, 200, 200, (int)gearAlpha).getRGB());
            }
            if (!(selectedIconAnim < 0.99f)) continue;
            float dotsAlpha = 120.0f * (1.0f - selectedIconAnim) * combinedAlpha;
            Fonts.BOLD.draw("...", iconBaseX + 1.0f, iconY - 1.0f, 7.0f * scale, new Color(150, 150, 150, (int)dotsAlpha).getRGB());
        }
    }

    private void renderBindBox(ModuleStructure module, ModuleStructure bindingModule, float moduleX, float moduleY, float moduleWidth, float moduleHeight, float scale, float combinedAlpha, float stateTextOffset, ModuleAnimationHandler animHandler) {
        float currentWidth;
        boolean isBinding = module == bindingModule;
        int key = module.getKey();
        float bindAlpha = animHandler.getBindBoxAlphaAnimations().getOrDefault(module, Float.valueOf(0.0f)).floatValue();
        if (bindAlpha <= 0.01f && !isBinding && (key == -1 || key == -1)) {
            return;
        }
        String bindText = isBinding ? "..." : this.bindHandler.getBindDisplayName(key, module.getType());
        float textWidth = Fonts.BOLD.getWidth(bindText, 5.0f * scale);
        float targetWidth = Math.max(18.0f, textWidth + 12.0f);
        float widthDiff = targetWidth - (currentWidth = animHandler.getBindBoxWidthAnimations().getOrDefault(module, Float.valueOf(targetWidth)).floatValue());
        if (Math.abs(widthDiff) > 0.1f) {
            animHandler.getBindBoxWidthAnimations().put(module, Float.valueOf(currentWidth += widthDiff * 12.0f * 0.016f));
        } else {
            currentWidth = targetWidth;
            animHandler.getBindBoxWidthAnimations().put(module, Float.valueOf(currentWidth));
        }
        float boxHeight = 9.0f * scale;
        float boxWidth = currentWidth * scale * bindAlpha;
        float nameWidth = Fonts.BOLD.getWidth(module.getName(), 6.0f * scale);
        float boxX = moduleX + 5.0f + stateTextOffset + nameWidth;
        float boxY = moduleY + (moduleHeight - boxHeight) / 2.0f + 0.5f;
        float finalAlpha = combinedAlpha * bindAlpha;
        int bgAlpha = (int)(30.0f * finalAlpha);
        Color bgColor = new Color(50, 50, 55, bgAlpha);
        Render2D.rect(boxX + 3.0f, boxY + 0.5f, boxWidth - 6.0f, boxHeight, bgColor.getRGB(), 3.0f * scale);
        int outlineAlpha = (int)(60.0f * finalAlpha);
        Color outlineColor = new Color(80, 80, 85, outlineAlpha);
        Render2D.outline(boxX + 3.0f, boxY + 0.5f, boxWidth - 6.0f, boxHeight, 0.5f, outlineColor.getRGB(), 3.0f * scale);
        if (bindAlpha > 0.5f) {
            int textAlpha = (int)(160.0f * finalAlpha);
            Color textColor = new Color(140, 140, 145, textAlpha);
            float textX = boxX + (boxWidth - textWidth) / 2.0f;
            float textY = boxY + (boxHeight - 5.0f * scale) / 2.0f;
            Fonts.BOLD.draw(bindText, textX, textY, 5.0f * scale, textColor.getRGB());
        }
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

    public ModuleStructure getModuleAtPosition(List<ModuleStructure> displayModules, double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight, double scrollOffset, boolean isTransitioning) {
        if (isTransitioning) {
            return null;
        }
        if (mouseX < (double)listX || mouseX > (double)(listX + listWidth) || mouseY < (double)listY || mouseY > (double)(listY + listHeight)) {
            return null;
        }
        float startY = listY + 3.0f + 2.0f + (float)scrollOffset;
        for (int i = 0; i < displayModules.size(); ++i) {
            float modY = startY + (float)i * 24.0f;
            if (!(mouseX >= (double)(listX + 3.0f)) || !(mouseX <= (double)(listX + listWidth - 3.0f)) || !(mouseY >= (double)modY) || !(mouseY <= (double)(modY + 22.0f))) continue;
            return displayModules.get(i);
        }
        return null;
    }

    public boolean isStarClicked(List<ModuleStructure> displayModules, double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight, double scrollOffset, ModuleDisplayHelper displayHelper, boolean isTransitioning) {
        if (isTransitioning) {
            return false;
        }
        float startY = listY + 3.0f + 2.0f + (float)scrollOffset;
        for (int i = 0; i < displayModules.size(); ++i) {
            ModuleStructure module = displayModules.get(i);
            float modY = startY + (float)i * 24.0f;
            if (!(mouseY >= (double)modY) || !(mouseY <= (double)(modY + 22.0f))) continue;
            float scaledWidth = listWidth - 6.0f;
            float animX = listX + 3.0f;
            boolean hasSettings = displayHelper.hasSettings(module);
            float starX = hasSettings ? animX + scaledWidth - 14.0f - 12.0f : animX + scaledWidth - 14.0f;
            if (!(mouseX >= (double)starX) || !(mouseX <= (double)(starX + 10.0f))) continue;
            return true;
        }
        return false;
    }

    public ModuleStructure getModuleForStarClick(List<ModuleStructure> displayModules, double mouseX, double mouseY, float listX, float listY, float listWidth, float listHeight, double scrollOffset, ModuleDisplayHelper displayHelper, boolean isTransitioning) {
        if (isTransitioning) {
            return null;
        }
        float startY = listY + 3.0f + 2.0f + (float)scrollOffset;
        for (int i = 0; i < displayModules.size(); ++i) {
            ModuleStructure module = displayModules.get(i);
            float modY = startY + (float)i * 24.0f;
            if (!(mouseY >= (double)modY) || !(mouseY <= (double)(modY + 22.0f))) continue;
            float scaledWidth = listWidth - 6.0f;
            float animX = listX + 3.0f;
            boolean hasSettings = displayHelper.hasSettings(module);
            float starX = hasSettings ? animX + scaledWidth - 14.0f - 12.0f : animX + scaledWidth - 14.0f;
            if (!(mouseX >= (double)starX) || !(mouseX <= (double)(starX + 10.0f))) continue;
            return module;
        }
        return null;
    }

    private float easeOutCubic(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 3.0);
    }
}

