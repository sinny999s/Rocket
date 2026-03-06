/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.glfw.GLFW
 */
package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class ColorComponent
extends AbstractSettingComponent {
    private final ColorSetting colorSetting;
    private boolean expanded = false;
    private float expandAnimation = 0.0f;
    private float hoverAnimation = 0.0f;
    private float previewHoverAnimation = 0.0f;
    private float contentAlpha = 0.0f;
    private boolean draggingPalette = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;
    private float paletteHandleAnimation = 0.0f;
    private float hueHandleAnimation = 0.0f;
    private float alphaHandleAnimation = 0.0f;
    private boolean hexInputActive = false;
    private String hexInputText = "";
    private int hexCursorPosition = 0;
    private int hexSelectionStart = -1;
    private int hexSelectionEnd = -1;
    private float hexInputAnimation = 0.0f;
    private float hexSelectionAnimation = 0.0f;
    private float hexCursorBlinkAnimation = 0.0f;
    private float displayHue;
    private float displaySaturation;
    private float displayBrightness;
    private float displayAlpha;
    private boolean colorInitialized = false;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float FAST_ANIMATION_SPEED = 15.0f;
    private static final float COLOR_TRANSITION_SPEED = 6.0f;
    private static final float CONTENT_FADE_SPEED = 15.0f;
    private static final float PALETTE_SIZE = 70.0f;
    private static final float SLIDER_WIDTH = 8.0f;
    private static final float SPACING = 4.0f;
    private static final float PREVIEW_SIZE = 12.0f;

    public ColorComponent(ColorSetting setting) {
        super(setting);
        this.colorSetting = setting;
        this.updateHexFromColor();
        this.displayHue = setting.getHue();
        this.displaySaturation = setting.getSaturation();
        this.displayBrightness = setting.getBrightness();
        this.displayAlpha = setting.getAlpha();
        this.colorInitialized = true;
    }

    private float getDeltaTime() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0f, 0.1f);
        this.lastUpdateTime = currentTime;
        return deltaTime;
    }

    private float lerp(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) {
            return target;
        }
        return current + diff * Math.min(speed, 1.0f);
    }

    private float lerpHue(float current, float target, float speed) {
        float diff = target - current;
        if (diff > 0.5f) {
            diff -= 1.0f;
        } else if (diff < -0.5f) {
            diff += 1.0f;
        }
        if (Math.abs(diff) < 0.001f) {
            return target;
        }
        float result = current + diff * Math.min(speed, 1.0f);
        if (result < 0.0f) {
            result += 1.0f;
        }
        if (result > 1.0f) {
            result -= 1.0f;
        }
        return result;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private void updateDisplayColors(float deltaTime) {
        if (!this.colorInitialized) {
            this.displayHue = this.colorSetting.getHue();
            this.displaySaturation = this.colorSetting.getSaturation();
            this.displayBrightness = this.colorSetting.getBrightness();
            this.displayAlpha = this.colorSetting.getAlpha();
            this.colorInitialized = true;
            return;
        }
        float speed = deltaTime * 6.0f;
        if (this.draggingPalette || this.draggingHue || this.draggingAlpha) {
            this.displayHue = this.colorSetting.getHue();
            this.displaySaturation = this.colorSetting.getSaturation();
            this.displayBrightness = this.colorSetting.getBrightness();
            this.displayAlpha = this.colorSetting.getAlpha();
        } else {
            this.displayHue = this.lerpHue(this.displayHue, this.colorSetting.getHue(), speed);
            this.displaySaturation = this.lerp(this.displaySaturation, this.colorSetting.getSaturation(), speed);
            this.displayBrightness = this.lerp(this.displayBrightness, this.colorSetting.getBrightness(), speed);
            this.displayAlpha = this.lerp(this.displayAlpha, this.colorSetting.getAlpha(), speed);
        }
    }

    private int getDisplayColor() {
        int rgb = Color.HSBtoRGB(this.displayHue, this.displaySaturation, this.displayBrightness);
        int alphaInt = Math.round(this.displayAlpha * 255.0f);
        return alphaInt << 24 | rgb & 0xFFFFFF;
    }

    private int getDisplayColorNoAlpha() {
        return Color.HSBtoRGB(this.displayHue, this.displaySaturation, this.displayBrightness) | 0xFF000000;
    }

    private Color applyContentAlpha(Color color) {
        int newAlpha = Math.max(0, Math.min(255, (int)((float)color.getAlpha() * this.alphaMultiplier * this.contentAlpha)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
    }

    private int applyContentAlpha(int color) {
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int newAlpha = Math.max(0, Math.min(255, (int)((float)a * this.alphaMultiplier * this.contentAlpha)));
        return newAlpha << 24 | r << 16 | g << 8 | b;
    }

    private boolean isControlDown() {
        long window = mc.getWindow().handle();
        return GLFW.glfwGetKey((long)window, (int)341) == 1 || GLFW.glfwGetKey((long)window, (int)345) == 1;
    }

    private boolean isShiftDown() {
        long window = mc.getWindow().handle();
        return GLFW.glfwGetKey((long)window, (int)340) == 1 || GLFW.glfwGetKey((long)window, (int)344) == 1;
    }

    private boolean hasHexSelection() {
        return this.hexSelectionStart != -1 && this.hexSelectionEnd != -1 && this.hexSelectionStart != this.hexSelectionEnd;
    }

    private int getHexSelectionStart() {
        return Math.min(this.hexSelectionStart, this.hexSelectionEnd);
    }

    private int getHexSelectionEnd() {
        return Math.max(this.hexSelectionStart, this.hexSelectionEnd);
    }

    private String getHexSelectedText() {
        if (!this.hasHexSelection()) {
            return "";
        }
        return this.hexInputText.substring(this.getHexSelectionStart(), this.getHexSelectionEnd());
    }

    private void clearHexSelection() {
        this.hexSelectionStart = -1;
        this.hexSelectionEnd = -1;
    }

    private void selectAllHexText() {
        this.hexSelectionStart = 0;
        this.hexSelectionEnd = this.hexInputText.length();
        this.hexCursorPosition = this.hexInputText.length();
    }

    private void deleteHexSelectedText() {
        if (this.hasHexSelection()) {
            int start = this.getHexSelectionStart();
            int end = this.getHexSelectionEnd();
            this.hexInputText = this.hexInputText.substring(0, start) + this.hexInputText.substring(end);
            this.hexCursorPosition = start;
            this.clearHexSelection();
        }
    }

    private void pasteHexFromClipboard() {
        String clipboardText = GLFW.glfwGetClipboardString((long)mc.getWindow().handle());
        if (clipboardText != null && !clipboardText.isEmpty()) {
            clipboardText = clipboardText.replace("#", "").replaceAll("[^0-9A-Fa-f]", "").toUpperCase();
            if (this.hasHexSelection()) {
                this.deleteHexSelectedText();
            }
            int remainingSpace = 8 - this.hexInputText.length();
            if (clipboardText.length() > remainingSpace) {
                clipboardText = clipboardText.substring(0, remainingSpace);
            }
            if (!clipboardText.isEmpty()) {
                this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition) + clipboardText + this.hexInputText.substring(this.hexCursorPosition);
                this.hexCursorPosition += clipboardText.length();
            }
        }
    }

    private void copyHexToClipboard() {
        if (this.hasHexSelection()) {
            GLFW.glfwSetClipboardString((long)mc.getWindow().handle(), (CharSequence)("#" + this.getHexSelectedText()));
        } else if (!this.hexInputText.isEmpty()) {
            GLFW.glfwSetClipboardString((long)mc.getWindow().handle(), (CharSequence)("#" + this.hexInputText));
        }
    }

    private void moveHexCursor(int direction) {
        if (this.hasHexSelection() && !this.isShiftDown()) {
            this.hexCursorPosition = direction < 0 ? this.getHexSelectionStart() : this.getHexSelectionEnd();
            this.clearHexSelection();
        } else {
            if (direction < 0 && this.hexCursorPosition > 0) {
                --this.hexCursorPosition;
            } else if (direction > 0 && this.hexCursorPosition < this.hexInputText.length()) {
                ++this.hexCursorPosition;
            }
            this.updateHexSelectionAfterCursorMove();
        }
    }

    private void updateHexSelectionAfterCursorMove() {
        if (this.isShiftDown()) {
            if (this.hexSelectionStart == -1) {
                this.hexSelectionStart = this.hexSelectionEnd != -1 ? this.hexSelectionEnd : this.hexCursorPosition;
            }
            this.hexSelectionEnd = this.hexCursorPosition;
        } else {
            this.clearHexSelection();
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        float deltaTime = this.getDeltaTime();
        this.updateDisplayColors(deltaTime);
        if (this.draggingPalette) {
            this.updatePalette(mouseX, mouseY);
        }
        if (this.draggingHue) {
            this.updateHue(mouseY);
        }
        if (this.draggingAlpha) {
            this.updateAlpha(mouseY);
        }
        boolean hovered = this.isHover(mouseX, mouseY);
        boolean previewHovered = this.isPreviewHover(mouseX, mouseY);
        this.hoverAnimation = this.lerp(this.hoverAnimation, hovered ? 1.0f : 0.0f, deltaTime * 8.0f);
        this.previewHoverAnimation = this.lerp(this.previewHoverAnimation, previewHovered ? 1.0f : 0.0f, deltaTime * 8.0f);
        this.expandAnimation = this.lerp(this.expandAnimation, this.expanded ? 1.0f : 0.0f, deltaTime * 8.0f);
        this.hexInputAnimation = this.lerp(this.hexInputAnimation, this.hexInputActive ? 1.0f : 0.0f, deltaTime * 15.0f);
        this.hexSelectionAnimation = this.lerp(this.hexSelectionAnimation, this.hasHexSelection() ? 1.0f : 0.0f, deltaTime * 8.0f);
        if (this.hexInputActive) {
            this.hexCursorBlinkAnimation += deltaTime * 2.0f;
            if (this.hexCursorBlinkAnimation > 1.0f) {
                this.hexCursorBlinkAnimation -= 1.0f;
            }
        } else {
            this.hexCursorBlinkAnimation = 0.0f;
        }
        float contentAlphaTarget = this.expanded ? 1.0f : 0.0f;
        float contentAlphaSpeed = this.expanded ? 15.0f : 22.5f;
        this.contentAlpha = this.lerp(this.contentAlpha, contentAlphaTarget, deltaTime * contentAlphaSpeed);
        this.paletteHandleAnimation = this.lerp(this.paletteHandleAnimation, this.draggingPalette ? 1.0f : 0.0f, deltaTime * 15.0f);
        this.hueHandleAnimation = this.lerp(this.hueHandleAnimation, this.draggingHue ? 1.0f : 0.0f, deltaTime * 15.0f);
        this.alphaHandleAnimation = this.lerp(this.alphaHandleAnimation, this.draggingAlpha ? 1.0f : 0.0f, deltaTime * 15.0f);
        int iconAlpha = (int)(200.0f * this.alphaMultiplier);
        Fonts.GUI_ICONS.draw("R", this.x + 0.5f, this.y + this.height / 2.0f - 11.5f, 16.0f, new Color(210, 210, 210, iconAlpha).getRGB());
        Fonts.BOLD.draw(this.colorSetting.getName(), this.x + 11.5f, this.y + this.height / 2.0f - 6.5f, 6.0f, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
        String description = this.colorSetting.getDescription();
        if (description != null && !description.isEmpty()) {
            Fonts.BOLD.draw(description, this.x + 8.5f, this.y + this.height / 2.0f + 0.5f, 5.0f, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
        }
        this.renderColorPreview(mouseX, mouseY);
        if (this.expandAnimation > 0.01f) {
            this.renderColorPicker(context, mouseX, mouseY, deltaTime);
        }
    }

    private void renderColorPreview(int mouseX, int mouseY) {
        float previewX = this.x + this.width - 14.0f;
        float previewY = this.y + this.height / 2.0f / 2.0f;
        float scale = 1.0f + this.previewHoverAnimation * 0.1f;
        float scaledX = previewX - scale / 2.0f + 1.0f;
        float scaledY = previewY - scale / 2.0f;
        int colorValue = this.getDisplayColor();
        Color previewColor = new Color(colorValue, true);
        Render2D.rect(scaledX + 0.5f, scaledY + 0.5f, 9.0f, 9.0f, this.applyAlpha(previewColor).getRGB(), 15.0f);
        int outlineAlpha = this.clamp((int)((255.0f + this.previewHoverAnimation * 60.0f) * this.alphaMultiplier));
        Render2D.outline(scaledX, scaledY, 10.0f, 10.0f, 1.0f, new Color(125, 125, 125, outlineAlpha).getRGB(), 15.0f);
    }

    private void renderColorPicker(GuiGraphics context, int mouseX, int mouseY, float deltaTime) {
        float pickerX = this.x;
        float pickerY = this.y + this.height + 4.0f;
        float pickerWidth = this.width;
        float totalExpandedHeight = 96.0f;
        float visibleHeight = totalExpandedHeight * this.expandAnimation;
        int outlineAlpha = this.clamp((int)(60.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.outline(pickerX, pickerY, pickerWidth, visibleHeight + 2.0f, 0.5f, new Color(80, 80, 85, outlineAlpha).getRGB(), 4.0f);
        if (this.expandAnimation < 0.3f || this.contentAlpha < 0.01f) {
            return;
        }
        Scissor.enable(pickerX, pickerY, pickerWidth, visibleHeight, 2.0f);
        float contentX = pickerX + 4.0f;
        float contentY = pickerY + 4.0f;
        float contentWidth = pickerWidth - 8.0f;
        float slidersWidth = 20.0f;
        float paletteWidth = contentWidth - slidersWidth - 4.0f;
        this.renderHueSlider(contentX, contentY, 8.0f, 70.0f, mouseX, mouseY);
        this.renderAlphaSlider(contentX + 8.0f + 4.0f, contentY, 8.0f, 70.0f, mouseX, mouseY);
        this.renderSaturationBrightnessPalette(contentX + slidersWidth + 4.0f, contentY, paletteWidth, 70.0f, mouseX, mouseY);
        this.renderHexInput(contentX, contentY += 74.0f, contentWidth, 16.0f, mouseX, mouseY);
        Scissor.disable();
    }

    private void renderSaturationBrightnessPalette(float paletteX, float paletteY, float paletteWidth, float paletteHeight, int mouseX, int mouseY) {
        int pureColor = Color.HSBtoRGB(this.displayHue, 1.0f, 1.0f);
        Color pure = new Color(pureColor);
        int[] gradientColors = new int[]{this.applyContentAlpha(Color.WHITE).getRGB(), this.applyContentAlpha(pure).getRGB(), this.applyContentAlpha(pure).getRGB(), this.applyContentAlpha(Color.WHITE).getRGB()};
        Render2D.gradientRect(paletteX, paletteY, paletteWidth, paletteHeight - 0.5f, gradientColors, 5.0f);
        int[] blackGradient = new int[]{new Color(0, 0, 0, 0).getRGB(), new Color(0, 0, 0, 0).getRGB(), this.applyContentAlpha(Color.BLACK).getRGB(), this.applyContentAlpha(Color.BLACK).getRGB()};
        Render2D.gradientRect(paletteX, paletteY, paletteWidth, paletteHeight, blackGradient, 3.0f);
        float handleX = paletteX + this.displaySaturation * paletteWidth;
        float handleY = paletteY + (1.0f - this.displayBrightness) * paletteHeight;
        float handleSize = 6.0f + this.paletteHandleAnimation * 2.0f;
        int handleOutlineAlpha = this.clamp((int)(255.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.rect(handleX - handleSize / 2.0f, handleY - handleSize / 2.0f, handleSize, handleSize, new Color(255, 255, 255, handleOutlineAlpha).getRGB(), handleSize / 2.0f);
        int currentColor = Color.HSBtoRGB(this.displayHue, this.displaySaturation, this.displayBrightness);
        Color handleColor = new Color(currentColor);
        Render2D.rect(handleX - handleSize / 2.0f + 1.0f, handleY - handleSize / 2.0f + 1.0f, handleSize - 2.0f, handleSize - 2.0f, this.applyContentAlpha(handleColor).getRGB(), (handleSize - 2.0f) / 2.0f);
    }

    private void renderHueSlider(float sliderX, float sliderY, float sliderWidth, float sliderHeight, int mouseX, int mouseY) {
        int[] hueColors = new int[]{Color.HSBtoRGB(0.0f, 1.0f, 1.0f), Color.HSBtoRGB(0.16666667f, 1.0f, 1.0f), Color.HSBtoRGB(0.33333334f, 1.0f, 1.0f), Color.HSBtoRGB(0.5f, 1.0f, 1.0f), Color.HSBtoRGB(0.6666667f, 1.0f, 1.0f), Color.HSBtoRGB(0.8333333f, 1.0f, 1.0f), Color.HSBtoRGB(1.0f, 1.0f, 1.0f)};
        float segmentHeight = sliderHeight / 6.0f;
        int[] colorsTop = new int[]{this.applyContentAlpha(new Color(hueColors[0])).getRGB(), this.applyContentAlpha(new Color(hueColors[0])).getRGB(), this.applyContentAlpha(new Color(hueColors[1])).getRGB(), this.applyContentAlpha(new Color(hueColors[1])).getRGB()};
        Render2D.gradientRect(sliderX, sliderY, sliderWidth, segmentHeight, colorsTop, 2.0f, 2.0f, 0.0f, 0.0f);
        for (int i = 1; i < 5; ++i) {
            float segY = sliderY + (float)i * segmentHeight;
            int[] colors = new int[]{this.applyContentAlpha(new Color(hueColors[i])).getRGB(), this.applyContentAlpha(new Color(hueColors[i])).getRGB(), this.applyContentAlpha(new Color(hueColors[i + 1])).getRGB(), this.applyContentAlpha(new Color(hueColors[i + 1])).getRGB()};
            Render2D.gradientRect(sliderX, segY - 0.5f, sliderWidth, segmentHeight + 0.5f, colors, 0.0f);
        }
        int[] colorsBottom = new int[]{this.applyContentAlpha(new Color(hueColors[5])).getRGB(), this.applyContentAlpha(new Color(hueColors[5])).getRGB(), this.applyContentAlpha(new Color(hueColors[6])).getRGB(), this.applyContentAlpha(new Color(hueColors[6])).getRGB()};
        Render2D.gradientRect(sliderX, sliderY + 5.0f * segmentHeight - 0.5f, sliderWidth, segmentHeight, colorsBottom, 0.0f, 0.0f, 2.0f, 2.0f);
        int hueOutlineAlpha = this.clamp((int)(80.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.outline(sliderX, sliderY, sliderWidth, sliderHeight, 0.5f, new Color(100, 100, 105, hueOutlineAlpha).getRGB(), 3.0f);
        float handleY = sliderY + this.displayHue * sliderHeight;
        float handleHeight = 3.0f + this.hueHandleAnimation * 1.0f;
        float handleWidth = sliderWidth + 2.0f;
        int handleAlpha = this.clamp((int)(255.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.rect(sliderX - 1.0f, handleY - handleHeight / 2.0f, handleWidth, handleHeight, new Color(255, 255, 255, handleAlpha).getRGB(), 1.5f);
        int handleShadowAlpha = this.clamp((int)(100.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.outline(sliderX - 1.0f, handleY - handleHeight / 2.0f, handleWidth, handleHeight, 0.5f, new Color(0, 0, 0, handleShadowAlpha).getRGB(), 1.5f);
    }

    private void renderAlphaSlider(float sliderX, float sliderY, float sliderWidth, float sliderHeight, int mouseX, int mouseY) {
        int baseColor;
        int checkAlpha = this.clamp((int)(150.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.rect(sliderX, sliderY, sliderWidth, sliderHeight, new Color(180, 180, 180, checkAlpha).getRGB(), 2.0f);
        int transparentColor = baseColor = this.getDisplayColorNoAlpha() & 0xFFFFFF;
        int opaqueColor = baseColor | 0xFF000000;
        int[] alphaGradient = new int[]{this.applyContentAlpha(new Color(transparentColor, true), 0.0f).getRGB(), this.applyContentAlpha(new Color(transparentColor, true), 0.0f).getRGB(), this.applyContentAlpha(new Color(opaqueColor, true)).getRGB(), this.applyContentAlpha(new Color(opaqueColor, true)).getRGB()};
        Render2D.gradientRect(sliderX, sliderY, sliderWidth, sliderHeight, alphaGradient, 2.0f);
        int alphaOutlineAlpha = this.clamp((int)(80.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.outline(sliderX, sliderY, sliderWidth, sliderHeight, 0.5f, new Color(100, 100, 105, alphaOutlineAlpha).getRGB(), 3.0f);
        float handleY = sliderY + this.displayAlpha * sliderHeight;
        float handleHeight = 3.0f + this.alphaHandleAnimation * 1.0f;
        float handleWidth = sliderWidth + 2.0f;
        int handleAlpha = this.clamp((int)(255.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.rect(sliderX - 1.0f, handleY - handleHeight / 2.0f, handleWidth, handleHeight, new Color(255, 255, 255, handleAlpha).getRGB(), 1.5f);
        int handleShadowAlpha = this.clamp((int)(100.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.outline(sliderX - 1.0f, handleY - handleHeight / 2.0f, handleWidth, handleHeight, 0.5f, new Color(0, 0, 0, handleShadowAlpha).getRGB(), 1.5f);
    }

    private Color applyContentAlpha(Color color, float extraAlpha) {
        int newAlpha = Math.max(0, Math.min(255, (int)((float)color.getAlpha() * this.alphaMultiplier * this.contentAlpha * extraAlpha)));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha);
    }

    private void renderHexInput(float inputX, float inputY, float inputWidth, float inputHeight, int mouseX, int mouseY) {
        float cursorAlpha;
        boolean inputHovered = (float)mouseX >= inputX && (float)mouseX <= inputX + inputWidth && (float)mouseY >= inputY && (float)mouseY <= inputY + inputHeight;
        int bgAlpha = this.clamp((int)((40.0f + this.hexInputAnimation * 20.0f + (float)(inputHovered ? 10 : 0)) * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.rect(inputX, inputY, inputWidth, inputHeight, new Color(35, 35, 40, bgAlpha).getRGB(), 3.0f);
        int hexOutlineAlpha = this.clamp((int)((60.0f + this.hexInputAnimation * 80.0f + (float)(inputHovered ? 20 : 0)) * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Color outlineColor = this.hexInputActive ? new Color(100, 140, 180, hexOutlineAlpha) : new Color(80, 80, 85, hexOutlineAlpha);
        Render2D.outline(inputX, inputY, inputWidth, inputHeight, 0.5f, outlineColor.getRGB(), 3.0f);
        int iconAlpha = this.clamp((int)(200.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Fonts.GUI_ICONS.draw("V", inputX + 4.0f, inputY + inputHeight / 2.0f - 7.5f, 12.0f, new Color(210, 210, 210, iconAlpha).getRGB());
        String label = "HEX: ";
        float iconOffset = 10.0f;
        float labelWidth = Fonts.BOLD.getWidth(label, 5.0f);
        int labelAlpha = this.clamp((int)(150.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Fonts.BOLD.draw(label, inputX + 4.0f + iconOffset, inputY + inputHeight / 2.0f - 2.5f, 5.0f, new Color(140, 140, 150, labelAlpha).getRGB());
        String displayText = this.hexInputActive ? this.hexInputText : this.getDisplayHexString();
        float textStartX = inputX + 4.0f + iconOffset + labelWidth;
        float textY = inputY + inputHeight / 2.0f - 2.5f;
        if (this.hexInputActive && this.hasHexSelection() && this.hexSelectionAnimation > 0.01f) {
            int start = this.getHexSelectionStart();
            int end = this.getHexSelectionEnd();
            String beforeSelection = "#" + this.hexInputText.substring(0, start);
            String selection = this.hexInputText.substring(start, end);
            float selectionX = textStartX + Fonts.BOLD.getWidth(beforeSelection, 5.0f);
            float selectionWidth = Fonts.BOLD.getWidth(selection, 5.0f);
            int n = this.clamp((int)(100.0f * this.hexSelectionAnimation * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        }
        int textAlpha = this.clamp((int)((180.0f + this.hexInputAnimation * 40.0f) * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Fonts.BOLD.draw("#" + displayText, textStartX, textY, 5.0f, new Color(210, 210, 220, textAlpha).getRGB());
        if (this.hexInputActive && !this.hasHexSelection() && (cursorAlpha = (float)(Math.sin((double)this.hexCursorBlinkAnimation * Math.PI * 2.0) * 0.5 + 0.5)) > 0.3f) {
            String beforeCursor = "#" + this.hexInputText.substring(0, this.hexCursorPosition);
            float cursorX = textStartX + Fonts.BOLD.getWidth(beforeCursor, 5.0f);
            int cursorAlphaInt = this.clamp((int)(255.0f * cursorAlpha * this.hexInputAnimation * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
            Render2D.rect(cursorX, inputY + 3.0f, 0.5f, inputHeight - 6.0f, new Color(180, 180, 185, cursorAlphaInt).getRGB(), 0.0f);
        }
        float miniPreviewX = inputX + inputWidth - 15.0f;
        float miniPreviewY = inputY + 3.0f;
        float miniPreviewSize = inputHeight - 6.0f;
        int miniCheckAlpha = this.clamp((int)(120.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.rect(miniPreviewX, miniPreviewY, miniPreviewSize, miniPreviewSize, new Color(150, 150, 150, miniCheckAlpha).getRGB(), 3.0f);
        Render2D.rect(miniPreviewX, miniPreviewY, miniPreviewSize, miniPreviewSize, this.applyContentAlpha(new Color(this.getDisplayColor(), true)).getRGB(), 3.0f);
        int miniOutlineAlpha = this.clamp((int)(80.0f * this.expandAnimation * this.contentAlpha * this.alphaMultiplier));
        Render2D.outline(miniPreviewX, miniPreviewY, miniPreviewSize, miniPreviewSize, 0.5f, new Color(80, 80, 85, miniOutlineAlpha).getRGB(), 3.0f);
    }

    private String getDisplayHexString() {
        int color = this.getDisplayColor();
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        return String.format("%02X%02X%02X%02X", r, g, b, a);
    }

    private boolean isPreviewHover(double mouseX, double mouseY) {
        float previewX = this.x + this.width - 12.0f - 4.0f;
        float previewY = this.y + this.height / 2.0f - 6.0f;
        return mouseX >= (double)previewX && mouseX <= (double)(previewX + 12.0f) && mouseY >= (double)previewY && mouseY <= (double)(previewY + 12.0f);
    }

    private boolean isPaletteHover(double mouseX, double mouseY) {
        float pickerX = this.x;
        float pickerY = this.y + this.height + 4.0f;
        float contentX = pickerX + 4.0f;
        float contentY = pickerY + 4.0f;
        float contentWidth = this.width - 8.0f;
        float slidersWidth = 20.0f;
        float paletteWidth = contentWidth - slidersWidth - 4.0f;
        float paletteX = contentX + slidersWidth + 4.0f;
        return mouseX >= (double)paletteX && mouseX <= (double)(paletteX + paletteWidth) && mouseY >= (double)contentY && mouseY <= (double)(contentY + 70.0f);
    }

    private boolean isHueSliderHover(double mouseX, double mouseY) {
        float pickerX = this.x;
        float pickerY = this.y + this.height + 4.0f;
        float contentX = pickerX + 4.0f;
        float contentY = pickerY + 4.0f;
        return mouseX >= (double)contentX && mouseX <= (double)(contentX + 8.0f) && mouseY >= (double)contentY && mouseY <= (double)(contentY + 70.0f);
    }

    private boolean isAlphaSliderHover(double mouseX, double mouseY) {
        float pickerX = this.x;
        float pickerY = this.y + this.height + 4.0f;
        float contentX = pickerX + 4.0f;
        float contentY = pickerY + 4.0f;
        float alphaSliderX = contentX + 8.0f + 4.0f;
        return mouseX >= (double)alphaSliderX && mouseX <= (double)(alphaSliderX + 8.0f) && mouseY >= (double)contentY && mouseY <= (double)(contentY + 70.0f);
    }

    private boolean isHexInputHover(double mouseX, double mouseY) {
        float pickerX = this.x;
        float pickerY = this.y + this.height + 4.0f;
        float contentX = pickerX + 4.0f;
        float contentY = pickerY + 4.0f + 70.0f + 4.0f;
        float contentWidth = this.width - 8.0f;
        return mouseX >= (double)contentX && mouseX <= (double)(contentX + contentWidth) && mouseY >= (double)contentY && mouseY <= (double)(contentY + 16.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.isPreviewHover(mouseX, mouseY)) {
                boolean bl = this.expanded = !this.expanded;
                if (!this.expanded) {
                    this.hexInputActive = false;
                    this.draggingPalette = false;
                    this.draggingHue = false;
                    this.draggingAlpha = false;
                    this.clearHexSelection();
                }
                return true;
            }
            if (this.expanded && this.expandAnimation > 0.8f && this.contentAlpha > 0.5f) {
                if (this.isPaletteHover(mouseX, mouseY)) {
                    this.draggingPalette = true;
                    this.updatePalette(mouseX, mouseY);
                    this.hexInputActive = false;
                    this.clearHexSelection();
                    return true;
                }
                if (this.isHueSliderHover(mouseX, mouseY)) {
                    this.draggingHue = true;
                    this.updateHue(mouseY);
                    this.hexInputActive = false;
                    this.clearHexSelection();
                    return true;
                }
                if (this.isAlphaSliderHover(mouseX, mouseY)) {
                    this.draggingAlpha = true;
                    this.updateAlpha(mouseY);
                    this.hexInputActive = false;
                    this.clearHexSelection();
                    return true;
                }
                if (this.isHexInputHover(mouseX, mouseY)) {
                    this.hexInputActive = true;
                    this.hexInputText = this.getHexString();
                    this.hexCursorPosition = this.hexInputText.length();
                    this.hexSelectionStart = 0;
                    this.hexSelectionEnd = this.hexInputText.length();
                    return true;
                }
                if (this.hexInputActive) {
                    this.applyHexInput();
                    this.hexInputActive = false;
                    this.clearHexSelection();
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            boolean wasDragging = this.draggingPalette || this.draggingHue || this.draggingAlpha;
            this.draggingPalette = false;
            this.draggingHue = false;
            this.draggingAlpha = false;
            if (wasDragging) {
                this.updateHexFromColor();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            if (this.draggingPalette) {
                this.updatePalette(mouseX, mouseY);
                return true;
            }
            if (this.draggingHue) {
                this.updateHue(mouseY);
                return true;
            }
            if (this.draggingAlpha) {
                this.updateAlpha(mouseY);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.hexInputActive) {
            return false;
        }
        if (this.isControlDown()) {
            switch (keyCode) {
                case 65: {
                    this.selectAllHexText();
                    return true;
                }
                case 86: {
                    this.pasteHexFromClipboard();
                    return true;
                }
                case 67: {
                    this.copyHexToClipboard();
                    return true;
                }
                case 88: {
                    if (this.hasHexSelection()) {
                        this.copyHexToClipboard();
                        this.deleteHexSelectedText();
                    }
                    return true;
                }
            }
        }
        switch (keyCode) {
            case 257: 
            case 335: {
                this.applyHexInput();
                this.hexInputActive = false;
                this.clearHexSelection();
                return true;
            }
            case 256: {
                this.hexInputActive = false;
                this.clearHexSelection();
                return true;
            }
            case 259: {
                if (this.hasHexSelection()) {
                    this.deleteHexSelectedText();
                } else if (this.hexCursorPosition > 0) {
                    this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition - 1) + this.hexInputText.substring(this.hexCursorPosition);
                    --this.hexCursorPosition;
                }
                return true;
            }
            case 261: {
                if (this.hasHexSelection()) {
                    this.deleteHexSelectedText();
                } else if (this.hexCursorPosition < this.hexInputText.length()) {
                    this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition) + this.hexInputText.substring(this.hexCursorPosition + 1);
                }
                return true;
            }
            case 263: {
                this.moveHexCursor(-1);
                return true;
            }
            case 262: {
                this.moveHexCursor(1);
                return true;
            }
            case 268: {
                this.hexCursorPosition = 0;
                this.updateHexSelectionAfterCursorMove();
                return true;
            }
            case 269: {
                this.hexCursorPosition = this.hexInputText.length();
                this.updateHexSelectionAfterCursorMove();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.hexInputActive) {
            return false;
        }
        if (this.isHexChar(chr)) {
            if (this.hasHexSelection()) {
                this.deleteHexSelectedText();
            }
            if (this.hexInputText.length() < 8) {
                this.hexInputText = this.hexInputText.substring(0, this.hexCursorPosition) + Character.toUpperCase(chr) + this.hexInputText.substring(this.hexCursorPosition);
                ++this.hexCursorPosition;
            }
            return true;
        }
        return false;
    }

    private boolean isHexChar(char c) {
        return c >= '0' && c <= '9' || c >= 'A' && c <= 'F' || c >= 'a' && c <= 'f';
    }

    private void updatePalette(double mouseX, double mouseY) {
        float pickerX = this.x;
        float pickerY = this.y + this.height + 4.0f;
        float contentX = pickerX + 4.0f;
        float contentY = pickerY + 4.0f;
        float contentWidth = this.width - 8.0f;
        float slidersWidth = 20.0f;
        float paletteWidth = contentWidth - slidersWidth - 4.0f;
        float paletteX = contentX + slidersWidth + 4.0f;
        float saturation = (float)((mouseX - (double)paletteX) / (double)paletteWidth);
        float brightness = 1.0f - (float)((mouseY - (double)contentY) / 70.0);
        this.colorSetting.setSaturation(saturation);
        this.colorSetting.setBrightness(brightness);
    }

    private void updateHue(double mouseY) {
        float pickerY = this.y + this.height + 4.0f;
        float contentY = pickerY + 4.0f;
        float hue = (float)((mouseY - (double)contentY) / 70.0);
        this.colorSetting.setHue(hue);
    }

    private void updateAlpha(double mouseY) {
        float pickerY = this.y + this.height + 4.0f;
        float contentY = pickerY + 4.0f;
        float alpha = (float)((mouseY - (double)contentY) / 70.0);
        this.colorSetting.setAlpha(alpha);
    }

    private String getHexString() {
        int color = this.colorSetting.getColor();
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        return String.format("%02X%02X%02X%02X", r, g, b, a);
    }

    private void updateHexFromColor() {
        this.hexInputText = this.getHexString();
        this.hexCursorPosition = this.hexInputText.length();
    }

    private void applyHexInput() {
        String hex = this.hexInputText.toUpperCase();
        try {
            int b;
            int g;
            int r;
            int a = 255;
            if (hex.length() == 6) {
                r = Integer.parseInt(hex.substring(0, 2), 16);
                g = Integer.parseInt(hex.substring(2, 4), 16);
                b = Integer.parseInt(hex.substring(4, 6), 16);
            } else if (hex.length() == 8) {
                r = Integer.parseInt(hex.substring(0, 2), 16);
                g = Integer.parseInt(hex.substring(2, 4), 16);
                b = Integer.parseInt(hex.substring(4, 6), 16);
                a = Integer.parseInt(hex.substring(6, 8), 16);
            } else if (hex.length() == 3) {
                r = Integer.parseInt(hex.substring(0, 1) + hex.substring(0, 1), 16);
                g = Integer.parseInt(hex.substring(1, 2) + hex.substring(1, 2), 16);
                b = Integer.parseInt(hex.substring(2, 3) + hex.substring(2, 3), 16);
            } else {
                this.updateHexFromColor();
                return;
            }
            float[] hsb = Color.RGBtoHSB(r, g, b, null);
            this.colorSetting.setHue(hsb[0]);
            this.colorSetting.setSaturation(hsb[1]);
            this.colorSetting.setBrightness(hsb[2]);
            this.colorSetting.setAlpha((float)a / 255.0f);
        }
        catch (NumberFormatException e) {
            this.updateHexFromColor();
        }
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height);
    }

    public float getTotalHeight() {
        float totalExpandedHeight = 104.0f;
        float expandedHeight = totalExpandedHeight * this.expandAnimation;
        return this.height + expandedHeight;
    }

    public boolean isExpanded() {
        return this.expanded;
    }

    public boolean isHexInputActive() {
        return this.hexInputActive;
    }

    public boolean isDragging() {
        return this.draggingPalette || this.draggingHue || this.draggingAlpha;
    }
}

