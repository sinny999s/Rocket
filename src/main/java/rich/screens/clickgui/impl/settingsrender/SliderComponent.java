
package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class SliderComponent
extends AbstractSettingComponent {
    private final SliderSettings sliderSettings;
    private boolean dragging = false;
    private float animatedPercentage = 0.0f;
    private float knobAnimation = 0.0f;
    private boolean inputMode = false;
    private String inputText = "";
    private int cursorPosition = 0;
    private float inputAnimation = 0.0f;
    private float hoverAnimation = 0.0f;
    private float unitsAlpha = 1.0f;
    private float valueOffsetX = 0.0f;
    private float backgroundAlpha = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float FAST_ANIMATION_SPEED = 12.0f;

    public SliderComponent(SliderSettings setting) {
        super(setting);
        this.sliderSettings = setting;
        float range = this.sliderSettings.getMax() - this.sliderSettings.getMin();
        if (range > 0.0f) {
            this.animatedPercentage = (this.sliderSettings.getValue() - this.sliderSettings.getMin()) / range;
        }
    }

    private int clampAlpha(float alpha) {
        return Math.max(0, Math.min(255, (int)alpha));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        if (this.dragging) {
            this.updateValue(mouseX);
        }
        float deltaTime = this.getDeltaTime();
        this.updateAnimations(mouseX, mouseY, deltaTime);
        float range = this.sliderSettings.getMax() - this.sliderSettings.getMin();
        float targetPercentage = range > 0.0f ? (this.sliderSettings.getValue() - this.sliderSettings.getMin()) / range : 0.0f;
        this.animatedPercentage += (targetPercentage - this.animatedPercentage) * 0.25f;
        float knobTarget = this.dragging ? 1.0f : 0.0f;
        this.knobAnimation += (knobTarget - this.knobAnimation) * 0.25f;
        this.knobAnimation = Math.max(0.0f, Math.min(1.0f, this.knobAnimation));
        int iconAlpha = (int)(200.0f * this.alphaMultiplier);
        Fonts.GUI_ICONS.draw("H", this.x - 0.5f, this.y + 0.5f, 9.0f, new Color(210, 210, 210, iconAlpha).getRGB());
        Fonts.BOLD.draw(this.sliderSettings.getName(), this.x + 9.5f, this.y + 1.0f, 6.0f, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
        this.renderValueInput(mouseX, mouseY);
        this.renderSlider();
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

    private void updateAnimations(int mouseX, int mouseY, float deltaTime) {
        float inputTarget = this.inputMode ? 1.0f : 0.0f;
        this.inputAnimation = this.lerp(this.inputAnimation, inputTarget, deltaTime * 12.0f);
        boolean isHovered = this.isValueHover(mouseX, mouseY) && !this.inputMode;
        float hoverTarget = isHovered ? 1.0f : 0.0f;
        this.hoverAnimation = this.lerp(this.hoverAnimation, hoverTarget, deltaTime * 8.0f);
        float unitsTarget = this.inputMode ? 0.0f : 1.0f;
        this.unitsAlpha = this.lerp(this.unitsAlpha, unitsTarget, deltaTime * 8.0f);
        float offsetTarget = this.inputMode ? 1.0f : 0.0f;
        this.valueOffsetX = this.lerp(this.valueOffsetX, offsetTarget, deltaTime * 8.0f);
        float bgTarget = this.inputMode ? 1.0f : 0.0f;
        this.backgroundAlpha = this.lerp(this.backgroundAlpha, bgTarget, deltaTime * 8.0f);
    }

    private void renderValueInput(int mouseX, int mouseY) {
        float combinedOutlineAlpha;
        String valueText = this.sliderSettings.isInteger() ? String.valueOf((int)this.sliderSettings.getValue()) : String.format("%.1f", Float.valueOf(this.sliderSettings.getValue()));
        String unitsText = " units";
        String fullText = valueText + unitsText;
        float fullTextWidth = Fonts.BOLD.getWidth(fullText, 5.0f);
        float valueTextWidth = Fonts.BOLD.getWidth(valueText, 5.0f);
        float unitsTextWidth = Fonts.BOLD.getWidth(unitsText, 5.0f);
        float baseX = this.x + this.width - fullTextWidth - 4.0f;
        float textY = this.y + 2.0f;
        float centerOffset = unitsTextWidth / 2.0f * this.valueOffsetX;
        float currentValueX = baseX + centerOffset;
        float inputBoxX = baseX - 3.0f;
        float inputBoxY = textY - 1.0f;
        float inputBoxWidth = fullTextWidth + 6.0f;
        float inputBoxHeight = 8.0f;
        if (this.backgroundAlpha > 0.01f) {
            int bgAlpha = this.clampAlpha(200.0f * this.backgroundAlpha * this.alphaMultiplier);
            Render2D.rect(inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight, new Color(40, 40, 45, bgAlpha).getRGB(), 2.0f);
        }
        if ((combinedOutlineAlpha = Math.max(this.hoverAnimation * 0.4f, this.inputAnimation)) > 0.01f) {
            int outlineAlpha = this.clampAlpha(180.0f * combinedOutlineAlpha * this.alphaMultiplier);
            Render2D.outline(inputBoxX, inputBoxY, inputBoxWidth, inputBoxHeight, 0.1f, new Color(180, 180, 180, outlineAlpha).getRGB(), 2.0f);
        }
        if (this.inputMode && this.inputAnimation > 0.5f) {
            String displayText = this.inputText;
            float displayTextWidth = Fonts.BOLD.getWidth(displayText, 5.0f);
            float centeredX = inputBoxX + (inputBoxWidth - displayTextWidth) / 2.0f;
            int textAlpha = this.clampAlpha(220.0f * Math.min(1.0f, (this.inputAnimation - 0.5f) * 2.0f) * this.alphaMultiplier);
            Fonts.BOLD.draw(displayText, centeredX, textY, 5.0f, new Color(230, 230, 235, textAlpha).getRGB());
            long currentTime = System.currentTimeMillis();
            if (currentTime % 1000L < 500L) {
                String beforeCursor = this.inputText.substring(0, this.cursorPosition);
                float cursorX = centeredX + Fonts.BOLD.getWidth(beforeCursor, 5.0f);
                int cursorAlpha = this.clampAlpha(255.0f * this.inputAnimation * this.alphaMultiplier);
                Render2D.rect(cursorX, inputBoxY + 2.0f, 0.5f, inputBoxHeight - 4.0f, new Color(180, 180, 180, cursorAlpha).getRGB(), 0.0f);
            }
        } else {
            int unitsAlphaInt;
            float valueAlpha = 1.0f - this.inputAnimation * 0.5f;
            int valueAlphaInt = this.clampAlpha(160.0f * valueAlpha * this.alphaMultiplier);
            if (valueAlphaInt > 0) {
                Fonts.BOLD.draw(valueText, currentValueX, textY, 5.0f, new Color(100, 100, 105, valueAlphaInt).getRGB());
            }
            if (this.unitsAlpha > 0.01f && (unitsAlphaInt = this.clampAlpha(160.0f * this.unitsAlpha * this.alphaMultiplier)) > 0) {
                Fonts.BOLD.draw(unitsText, currentValueX + valueTextWidth, textY, 5.0f, new Color(100, 100, 105, unitsAlphaInt).getRGB());
            }
        }
    }

    private void renderSlider() {
        float sliderY = this.y + 11.0f;
        float sliderHeight = 2.5f;
        float sliderPadding = 1.0f;
        float sliderTrackWidth = this.width - 2.0f;
        Render2D.rect(this.x + sliderPadding, sliderY, sliderTrackWidth, sliderHeight, this.applyAlpha(new Color(60, 60, 65, 220)).getRGB(), 2.0f);
        float filledWidth = sliderTrackWidth * this.animatedPercentage;
        if (filledWidth > 0.0f) {
            Render2D.rect(this.x + sliderPadding, sliderY, filledWidth, sliderHeight, this.applyAlpha(new Color(130, 130, 135, 230)).getRGB(), 2.0f);
        }
        float knobBaseSize = 5.0f;
        float knobSize = knobBaseSize + this.knobAnimation * 1.0f;
        float knobX = this.x + sliderPadding + sliderTrackWidth * this.animatedPercentage - knobSize / 2.0f;
        float knobY = sliderY + sliderHeight / 2.0f - knobSize / 2.0f;
        knobX = Math.max(this.x + sliderPadding - knobSize / 2.0f, Math.min(knobX, this.x + sliderPadding + sliderTrackWidth - knobSize / 2.0f));
        Render2D.rect(knobX, knobY, knobSize, knobSize, this.applyAlpha(new Color(180, 180, 185, 255)).getRGB(), knobSize / 2.0f);
    }

    private boolean isValueHover(double mouseX, double mouseY) {
        String valueText = this.sliderSettings.isInteger() ? String.valueOf((int)this.sliderSettings.getValue()) : String.format("%.1f", Float.valueOf(this.sliderSettings.getValue()));
        String fullText = valueText + " units";
        float fullTextWidth = Fonts.BOLD.getWidth(fullText, 5.0f);
        float boxX = this.x + this.width - fullTextWidth - 7.0f;
        float boxY = this.y;
        return mouseX >= (double)boxX && mouseX <= (double)(boxX + fullTextWidth + 10.0f) && mouseY >= (double)boxY && mouseY <= (double)(boxY + 10.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.isValueHover(mouseX, mouseY) && !this.inputMode) {
                String currentValue;
                this.inputMode = true;
                this.inputText = currentValue = this.sliderSettings.isInteger() ? String.valueOf((int)this.sliderSettings.getValue()) : String.format("%.1f", Float.valueOf(this.sliderSettings.getValue()));
                this.cursorPosition = this.inputText.length();
                return true;
            }
            if (this.inputMode && !this.isValueHover(mouseX, mouseY)) {
                this.applyInputValue();
                this.inputMode = false;
                this.inputText = "";
                return true;
            }
            if (this.isSliderHover(mouseX, mouseY) && !this.inputMode) {
                this.dragging = true;
                this.updateValue(mouseX);
                return true;
            }
        }
        return false;
    }

    private boolean isSliderHover(double mouseX, double mouseY) {
        float sliderY = this.y + 6.0f;
        float sliderHeight = 12.0f;
        return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)sliderY && mouseY <= (double)(sliderY + sliderHeight);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && this.dragging) {
            this.dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragging && button == 0) {
            this.updateValue(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.inputMode) {
            return false;
        }
        switch (keyCode) {
            case 257: 
            case 335: {
                this.applyInputValue();
                this.inputMode = false;
                this.inputText = "";
                return true;
            }
            case 256: {
                this.inputMode = false;
                this.inputText = "";
                return true;
            }
            case 259: {
                if (this.cursorPosition > 0) {
                    this.inputText = this.inputText.substring(0, this.cursorPosition - 1) + this.inputText.substring(this.cursorPosition);
                    --this.cursorPosition;
                }
                return true;
            }
            case 261: {
                if (this.cursorPosition < this.inputText.length()) {
                    this.inputText = this.inputText.substring(0, this.cursorPosition) + this.inputText.substring(this.cursorPosition + 1);
                }
                return true;
            }
            case 263: {
                if (this.cursorPosition > 0) {
                    --this.cursorPosition;
                }
                return true;
            }
            case 262: {
                if (this.cursorPosition < this.inputText.length()) {
                    ++this.cursorPosition;
                }
                return true;
            }
            case 268: {
                this.cursorPosition = 0;
                return true;
            }
            case 269: {
                this.cursorPosition = this.inputText.length();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!this.inputMode) {
            return false;
        }
        if (this.isValidInputChar(chr)) {
            String newText = this.inputText.substring(0, this.cursorPosition) + chr + this.inputText.substring(this.cursorPosition);
            if (this.isValidInputFormat(newText)) {
                this.inputText = newText;
                ++this.cursorPosition;
            }
            return true;
        }
        return false;
    }

    private boolean isValidInputChar(char chr) {
        return Character.isDigit(chr) || chr == '.' || chr == '-';
    }

    private boolean isValidInputFormat(String text) {
        if (text.isEmpty() || text.equals("-") || text.equals(".") || text.equals("-.")) {
            return true;
        }
        int dotCount = 0;
        int minusCount = 0;
        int digitsAfterDot = 0;
        boolean foundDot = false;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            if (c == '-') {
                if (i != 0) {
                    return false;
                }
                if (++minusCount <= 1) continue;
                return false;
            }
            if (c == '.') {
                if (this.sliderSettings.isInteger()) {
                    return false;
                }
                if (++dotCount > 1) {
                    return false;
                }
                foundDot = true;
                continue;
            }
            if (Character.isDigit(c)) {
                if (!foundDot || ++digitsAfterDot <= 1) continue;
                return false;
            }
            return false;
        }
        return true;
    }

    private void applyInputValue() {
        if (this.inputText.isEmpty() || this.inputText.equals("-") || this.inputText.equals(".") || this.inputText.equals("-.")) {
            return;
        }
        try {
            float value = this.sliderSettings.isInteger() ? (float)Integer.parseInt(this.inputText) : Float.parseFloat(this.inputText);
            value = Math.max(this.sliderSettings.getMin(), Math.min(this.sliderSettings.getMax(), value));
            if (this.sliderSettings.isInteger()) {
                value = Math.round(value);
            }
            this.sliderSettings.setValue(value);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
    }

    private void updateValue(double mouseX) {
        float sliderPadding = 1.0f;
        float sliderTrackWidth = this.width - 2.0f;
        float percentage = (float)((mouseX - (double)this.x - (double)sliderPadding) / (double)sliderTrackWidth);
        percentage = Math.max(0.0f, Math.min(1.0f, percentage));
        float range = this.sliderSettings.getMax() - this.sliderSettings.getMin();
        float newValue = this.sliderSettings.getMin() + range * percentage;
        if (this.sliderSettings.isInteger()) {
            newValue = Math.round(newValue);
        }
        this.sliderSettings.setValue(newValue);
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height);
    }

    public boolean isInputMode() {
        return this.inputMode;
    }
}

