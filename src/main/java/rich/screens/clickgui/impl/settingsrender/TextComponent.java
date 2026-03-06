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
import rich.modules.module.setting.implement.TextSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class TextComponent
extends AbstractSettingComponent {
    public static boolean typing = false;
    private final TextSetting textSetting;
    private boolean focused = false;
    private int cursorPosition = 0;
    private int selectionStart = -1;
    private int selectionEnd = -1;
    private long lastClickTime = 0L;
    private String text = "";
    private float focusAnimation = 0.0f;
    private float hoverAnimation = 0.0f;
    private float textScrollOffset = 0.0f;
    private float targetScrollOffset = 0.0f;
    private float cursorBlinkAnimation = 0.0f;
    private float selectionAnimation = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float SCROLL_ANIMATION_SPEED = 10.0f;
    private static final float INPUT_BOX_WIDTH = 65.0f;
    private static final float INPUT_BOX_HEIGHT = 10.0f;
    private static final float TEXT_PADDING = 4.0f;

    public TextComponent(TextSetting setting) {
        super(setting);
        this.textSetting = setting;
        this.text = this.textSetting.getText() != null ? this.textSetting.getText() : "";
        this.cursorPosition = this.text.length();
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

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        float deltaTime = this.getDeltaTime();
        boolean hovered = this.isInputBoxHover(mouseX, mouseY);
        this.hoverAnimation = this.lerp(this.hoverAnimation, hovered ? 1.0f : 0.0f, deltaTime * 8.0f);
        this.focusAnimation = this.lerp(this.focusAnimation, this.focused ? 1.0f : 0.0f, deltaTime * 8.0f);
        this.selectionAnimation = this.lerp(this.selectionAnimation, this.hasSelection() ? 1.0f : 0.0f, deltaTime * 8.0f);
        if (this.focused) {
            this.cursorBlinkAnimation += deltaTime * 2.0f;
            if (this.cursorBlinkAnimation > 1.0f) {
                this.cursorBlinkAnimation -= 1.0f;
            }
        } else {
            this.cursorBlinkAnimation = 0.0f;
        }
        int iconAlpha = (int)(200.0f * this.alphaMultiplier);
        Fonts.GUI_ICONS.draw("S", this.x + 0.5f, this.y + this.height / 2.0f - 10.25f, 11.0f, new Color(210, 210, 220, iconAlpha).getRGB());
        Fonts.BOLD.draw(this.textSetting.getName(), this.x + 9.5f, this.y + this.height / 2.0f - 7.5f, 6.0f, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
        String description = this.textSetting.getDescription();
        if (description != null && !description.isEmpty()) {
            Fonts.BOLD.draw(description, this.x + 0.5f, this.y + this.height / 2.0f + 0.5f, 5.0f, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
        }
        float boxX = this.x + this.width - 65.0f - 2.0f;
        float boxY = this.y + this.height / 2.0f - 5.0f;
        int bgAlpha = (int)(25.0f + this.focusAnimation * 15.0f + this.hoverAnimation * 10.0f);
        Render2D.rect(boxX, boxY, 65.0f, 10.0f, this.applyAlpha(new Color(40, 40, 45, bgAlpha)).getRGB(), 3.0f);
        float outlineAlpha = 60.0f + this.hoverAnimation * 40.0f + this.focusAnimation * 60.0f;
        Color outlineColor = this.focused ? new Color(100, 140, 180, (int)(outlineAlpha * this.alphaMultiplier)) : new Color(155, 155, 155, (int)(outlineAlpha * this.alphaMultiplier));
        Render2D.outline(boxX, boxY, 65.0f, 10.0f, 0.5f, outlineColor.getRGB(), 3.0f);
        this.renderTextContent(boxX, boxY, deltaTime);
    }

    private void renderTextContent(float boxX, float boxY, float deltaTime) {
        float textAreaX = boxX + 4.0f;
        float textAreaWidth = 57.0f;
        float textY = boxY + 5.0f - 2.5f;
        String displayText = this.text;
        float fullTextWidth = Fonts.BOLD.getWidth(displayText, 5.0f);
        if (this.focused) {
            String beforeCursor = this.text.substring(0, this.cursorPosition);
            float cursorX = Fonts.BOLD.getWidth(beforeCursor, 5.0f);
            if (cursorX - this.targetScrollOffset > textAreaWidth - 2.0f) {
                this.targetScrollOffset = cursorX - textAreaWidth + 2.0f;
            } else if (cursorX - this.targetScrollOffset < 0.0f) {
                this.targetScrollOffset = cursorX;
            }
            if (fullTextWidth <= textAreaWidth) {
                this.targetScrollOffset = 0.0f;
            }
            this.targetScrollOffset = Math.max(0.0f, Math.min(this.targetScrollOffset, Math.max(0.0f, fullTextWidth - textAreaWidth)));
        } else {
            this.targetScrollOffset = 0.0f;
        }
        this.textScrollOffset = this.lerp(this.textScrollOffset, this.targetScrollOffset, deltaTime * 10.0f);
        Scissor.enable(boxX + 2.0f, boxY, 61.0f, 10.0f, 2.0f);
        if (this.text.isEmpty() && !this.focused) {
            Fonts.BOLD.draw("Enter text...", textAreaX, textY, 5.0f, this.applyAlpha(new Color(100, 100, 105, 100)).getRGB());
        } else {
            float cursorAlpha;
            if (this.focused && this.hasSelection() && this.selectionAnimation > 0.01f) {
                int start = this.getStartOfSelection();
                int end = this.getEndOfSelection();
                String beforeSelection = this.text.substring(0, start);
                String selection = this.text.substring(start, end);
                float selectionX = textAreaX + Fonts.BOLD.getWidth(beforeSelection, 5.0f) - this.textScrollOffset;
                float selectionWidth = Fonts.BOLD.getWidth(selection, 5.0f);
                int selAlpha = (int)(100.0f * this.selectionAnimation * this.alphaMultiplier);
                Render2D.rect(selectionX, boxY + 2.0f, selectionWidth, 6.0f, new Color(100, 140, 180, selAlpha).getRGB(), 2.0f);
            }
            int textAlpha = (int)((160.0f + this.focusAnimation * 60.0f) * this.alphaMultiplier);
            Fonts.BOLD.draw(displayText, textAreaX - this.textScrollOffset, textY, 5.0f, new Color(210, 210, 220, textAlpha).getRGB());
            if (this.focused && !this.hasSelection() && (cursorAlpha = (float)(Math.sin((double)this.cursorBlinkAnimation * Math.PI * 2.0) * 0.5 + 0.5)) > 0.3f) {
                String beforeCursor = this.text.substring(0, this.cursorPosition);
                float cursorXPos = textAreaX + Fonts.BOLD.getWidth(beforeCursor, 5.0f) - this.textScrollOffset;
                int cursorAlphaInt = (int)(255.0f * cursorAlpha * this.focusAnimation * this.alphaMultiplier);
                Render2D.rect(cursorXPos, boxY + 2.0f, 0.5f, 6.0f, new Color(180, 180, 185, cursorAlphaInt).getRGB(), 0.0f);
            }
        }
        Scissor.disable();
    }

    private boolean isInputBoxHover(double mouseX, double mouseY) {
        float boxX = this.x + this.width - 65.0f - 2.0f;
        float boxY = this.y + this.height / 2.0f - 5.0f;
        return mouseX >= (double)boxX && mouseX <= (double)(boxX + 65.0f) && mouseY >= (double)boxY && mouseY <= (double)(boxY + 10.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean wasInside = this.isInputBoxHover(mouseX, mouseY);
        if (wasInside && button == 0) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - this.lastClickTime < 250L && this.focused) {
                this.selectAllText();
            } else {
                this.focused = true;
                typing = true;
                this.selectionStart = this.cursorPosition = this.getCursorIndexAt(mouseX);
                this.selectionEnd = this.cursorPosition;
            }
            this.lastClickTime = currentTime;
            return true;
        }
        if (!wasInside && this.focused) {
            this.applyText();
            this.focused = false;
            typing = false;
            this.clearSelection();
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.focused && button == 0) {
            this.selectionEnd = this.cursorPosition = this.getCursorIndexAt(mouseX);
            return true;
        }
        return false;
    }

    private boolean isControlDown() {
        long window = mc.getWindow().handle();
        return GLFW.glfwGetKey((long)window, (int)341) == 1 || GLFW.glfwGetKey((long)window, (int)345) == 1;
    }

    private boolean isShiftDown() {
        long window = mc.getWindow().handle();
        return GLFW.glfwGetKey((long)window, (int)340) == 1 || GLFW.glfwGetKey((long)window, (int)344) == 1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.focused) {
            return false;
        }
        if (this.isControlDown()) {
            switch (keyCode) {
                case 65: {
                    this.selectAllText();
                    return true;
                }
                case 86: {
                    this.pasteFromClipboard();
                    return true;
                }
                case 67: {
                    this.copyToClipboard();
                    return true;
                }
                case 88: {
                    if (this.hasSelection()) {
                        this.copyToClipboard();
                        this.deleteSelectedText();
                    }
                    return true;
                }
            }
        } else {
            switch (keyCode) {
                case 259: {
                    this.handleBackspace();
                    return true;
                }
                case 261: {
                    this.handleDelete();
                    return true;
                }
                case 263: {
                    this.moveCursor(-1);
                    return true;
                }
                case 262: {
                    this.moveCursor(1);
                    return true;
                }
                case 268: {
                    this.cursorPosition = 0;
                    this.updateSelectionAfterCursorMove();
                    return true;
                }
                case 269: {
                    this.cursorPosition = this.text.length();
                    this.updateSelectionAfterCursorMove();
                    return true;
                }
                case 257: {
                    this.applyText();
                    this.focused = false;
                    typing = false;
                    return true;
                }
                case 256: {
                    this.text = this.textSetting.getText() != null ? this.textSetting.getText() : "";
                    this.cursorPosition = this.text.length();
                    this.focused = false;
                    typing = false;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        int maxLength;
        if (!this.focused) {
            return false;
        }
        if (Character.isISOControl(chr)) {
            return false;
        }
        int n = maxLength = this.textSetting.getMax() > 0 ? this.textSetting.getMax() : Integer.MAX_VALUE;
        if (this.text.length() < maxLength || this.hasSelection()) {
            this.deleteSelectedText();
            this.text = this.text.substring(0, this.cursorPosition) + chr + this.text.substring(this.cursorPosition);
            ++this.cursorPosition;
            this.clearSelection();
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
    }

    private void applyText() {
        int maxLength;
        int minLength = this.textSetting.getMin() > 0 ? this.textSetting.getMin() : 0;
        int n = maxLength = this.textSetting.getMax() > 0 ? this.textSetting.getMax() : Integer.MAX_VALUE;
        if (this.text.length() >= minLength && this.text.length() <= maxLength) {
            this.textSetting.setText(this.text);
        } else {
            this.text = this.textSetting.getText() != null ? this.textSetting.getText() : "";
            this.cursorPosition = this.text.length();
        }
    }

    private void handleBackspace() {
        if (this.hasSelection()) {
            this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
        } else if (this.cursorPosition > 0) {
            this.replaceText(this.cursorPosition - 1, this.cursorPosition, "");
        }
    }

    private void handleDelete() {
        if (this.hasSelection()) {
            this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
        } else if (this.cursorPosition < this.text.length()) {
            this.text = this.text.substring(0, this.cursorPosition) + this.text.substring(this.cursorPosition + 1);
        }
    }

    private void moveCursor(int direction) {
        if (this.hasSelection() && !this.isShiftDown()) {
            this.cursorPosition = direction < 0 ? this.getStartOfSelection() : this.getEndOfSelection();
            this.clearSelection();
        } else {
            if (direction < 0 && this.cursorPosition > 0) {
                --this.cursorPosition;
            } else if (direction > 0 && this.cursorPosition < this.text.length()) {
                ++this.cursorPosition;
            }
            this.updateSelectionAfterCursorMove();
        }
    }

    private void updateSelectionAfterCursorMove() {
        if (this.isShiftDown()) {
            if (this.selectionStart == -1) {
                this.selectionStart = this.selectionEnd != -1 ? this.selectionEnd : this.cursorPosition;
            }
            this.selectionEnd = this.cursorPosition;
        } else {
            this.clearSelection();
        }
    }

    private void pasteFromClipboard() {
        String clipboardText = GLFW.glfwGetClipboardString((long)mc.getWindow().handle());
        if (clipboardText != null && !clipboardText.isEmpty()) {
            clipboardText = clipboardText.replaceAll("[\n\r\t]", "");
            if (this.hasSelection()) {
                this.deleteSelectedText();
            }
            int maxLength = this.textSetting.getMax() > 0 ? this.textSetting.getMax() : Integer.MAX_VALUE;
            int remainingSpace = maxLength - this.text.length();
            if (clipboardText.length() > remainingSpace) {
                clipboardText = clipboardText.substring(0, remainingSpace);
            }
            if (!clipboardText.isEmpty()) {
                this.text = this.text.substring(0, this.cursorPosition) + clipboardText + this.text.substring(this.cursorPosition);
                this.cursorPosition += clipboardText.length();
            }
        }
    }

    private void copyToClipboard() {
        if (this.hasSelection()) {
            GLFW.glfwSetClipboardString((long)mc.getWindow().handle(), (CharSequence)this.getSelectedText());
        }
    }

    private void selectAllText() {
        this.selectionStart = 0;
        this.selectionEnd = this.text.length();
        this.cursorPosition = this.text.length();
    }

    private void replaceText(int start, int end, String replacement) {
        if (start < 0) {
            start = 0;
        }
        if (end > this.text.length()) {
            end = this.text.length();
        }
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }
        this.text = this.text.substring(0, start) + replacement + this.text.substring(end);
        this.cursorPosition = start + replacement.length();
        this.clearSelection();
    }

    private void deleteSelectedText() {
        if (this.hasSelection()) {
            this.replaceText(this.getStartOfSelection(), this.getEndOfSelection(), "");
        }
    }

    private boolean hasSelection() {
        return this.selectionStart != -1 && this.selectionEnd != -1 && this.selectionStart != this.selectionEnd;
    }

    private String getSelectedText() {
        if (!this.hasSelection()) {
            return "";
        }
        return this.text.substring(this.getStartOfSelection(), this.getEndOfSelection());
    }

    private int getStartOfSelection() {
        return Math.min(this.selectionStart, this.selectionEnd);
    }

    private int getEndOfSelection() {
        return Math.max(this.selectionStart, this.selectionEnd);
    }

    private void clearSelection() {
        this.selectionStart = -1;
        this.selectionEnd = -1;
    }

    private int getCursorIndexAt(double mouseX) {
        float boxX = this.x + this.width - 65.0f - 2.0f;
        float textAreaX = boxX + 4.0f;
        float relativeX = (float)(mouseX - (double)textAreaX + (double)this.textScrollOffset);
        if (relativeX <= 0.0f) {
            return 0;
        }
        float lastWidth = 0.0f;
        for (int position = 0; position < this.text.length(); ++position) {
            float currentWidth = Fonts.BOLD.getWidth(this.text.substring(0, position + 1), 5.0f);
            float midPoint = (lastWidth + currentWidth) / 2.0f;
            if (relativeX < midPoint) {
                return position;
            }
            lastWidth = currentWidth;
        }
        return this.text.length();
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height);
    }

    public boolean isFocused() {
        return this.focused;
    }
}

