
package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import rich.modules.module.setting.implement.ButtonSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class ButtonComponent
extends AbstractSettingComponent {
    private final ButtonSetting buttonSetting;
    private float pressAnimation = 0.0f;
    private float hoverAnimation = 0.0f;
    private float scaleAnimation = 1.0f;
    private float rippleAnimation = 0.0f;
    private float rippleX = 0.0f;
    private float rippleY = 0.0f;
    private boolean wasPressed = false;
    private boolean rippleActive = false;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float FAST_ANIMATION_SPEED = 12.0f;
    private static final float BUTTON_WIDTH = 65.0f;
    private static final float BUTTON_HEIGHT = 12.0f;

    public ButtonComponent(ButtonSetting setting) {
        super(setting);
        this.buttonSetting = setting;
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

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        float deltaTime = this.getDeltaTime();
        boolean hovered = this.isButtonHover(mouseX, mouseY);
        this.hoverAnimation = this.lerp(this.hoverAnimation, hovered ? 1.0f : 0.0f, deltaTime * 8.0f);
        float scaleTarget = this.wasPressed ? 0.95f : (hovered ? 1.02f : 1.0f);
        this.scaleAnimation = this.lerp(this.scaleAnimation, scaleTarget, deltaTime * 12.0f);
        this.pressAnimation = this.lerp(this.pressAnimation, this.wasPressed ? 1.0f : 0.0f, deltaTime * 12.0f);
        if (this.rippleActive) {
            this.rippleAnimation += deltaTime * 3.0f;
            if (this.rippleAnimation >= 1.0f) {
                this.rippleAnimation = 0.0f;
                this.rippleActive = false;
            }
        }
        if (this.pressAnimation < 0.05f && this.wasPressed) {
            this.wasPressed = false;
        }
        int iconAlpha = (int)(200.0f * this.alphaMultiplier);
        Fonts.GUI_ICONS.draw("U", this.x + 0.5f, this.y + this.height / 2.0f - 12.0f, 13.0f, new Color(210, 210, 210, iconAlpha).getRGB());
        Fonts.BOLD.draw(this.buttonSetting.getName(), this.x + 9.5f, this.y + this.height / 2.0f - 7.5f, 6.0f, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
        String description = this.buttonSetting.getDescription();
        if (description != null && !description.isEmpty()) {
            Fonts.BOLD.draw(description, this.x + 0.5f, this.y + this.height / 2.0f + 0.5f, 5.0f, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
        }
        this.renderButton(mouseX, mouseY);
    }

    private void renderButton(int mouseX, int mouseY) {
        float buttonX = this.x + this.width - 65.0f - 2.0f;
        float buttonY = this.y + this.height / 2.0f - 6.0f;
        float scaledWidth = 65.0f * this.scaleAnimation;
        float scaledHeight = 12.0f * this.scaleAnimation;
        float scaledX = buttonX - (scaledWidth - 65.0f) / 2.0f;
        float scaledY = buttonY - (scaledHeight - 12.0f) / 2.0f;
        float pressOffset = this.pressAnimation * 1.0f;
        int bgAlpha = this.clamp((int)((30.0f + this.hoverAnimation * 20.0f + this.pressAnimation * 15.0f) * this.alphaMultiplier));
        int bgGray = this.clamp((int)(35.0f + this.hoverAnimation * 15.0f + this.pressAnimation * 20.0f));
        Color bgColor = new Color(bgGray, bgGray, bgGray, bgAlpha);
        Render2D.rect(scaledX, scaledY += pressOffset, scaledWidth, scaledHeight, bgColor.getRGB(), 4.0f);
        if (this.rippleActive && this.rippleAnimation > 0.0f) {
            float currentRippleSize = 20.0f * this.rippleAnimation;
            float rippleAlpha = (1.0f - this.rippleAnimation) * 0.4f;
            int rippleAlphaInt = this.clamp((int)(255.0f * rippleAlpha * this.alphaMultiplier));
            float localRippleX = this.rippleX - scaledX;
            float localRippleY = this.rippleY - scaledY;
            Render2D.rect(scaledX + localRippleX - currentRippleSize / 2.0f, scaledY + localRippleY - currentRippleSize / 2.0f, currentRippleSize, currentRippleSize, new Color(200, 200, 210, rippleAlphaInt).getRGB(), currentRippleSize / 2.0f);
        }
        int outlineAlpha = this.clamp((int)((60.0f + this.hoverAnimation * 60.0f + this.pressAnimation * 40.0f) * this.alphaMultiplier));
        int outlineGray = this.clamp((int)(80.0f + this.hoverAnimation * 40.0f + this.pressAnimation * 30.0f));
        Color outlineColor = new Color(outlineGray, outlineGray, outlineGray, outlineAlpha);
        Render2D.outline(scaledX, scaledY, scaledWidth, scaledHeight, 0.5f, outlineColor.getRGB(), 4.0f);
        this.renderButtonContent(scaledX, scaledY, scaledWidth, scaledHeight);
    }

    private void renderButtonContent(float buttonX, float buttonY, float buttonWidth, float buttonHeight) {
        float startX;
        String buttonText = this.buttonSetting.getButtonName() != null ? this.buttonSetting.getButtonName() : "Run";
        float iconSize = 4.0f;
        float textWidth = Fonts.BOLD.getWidth(buttonText, 5.0f);
        float totalWidth = iconSize + 4.0f + textWidth;
        float iconX = startX = buttonX + (buttonWidth - totalWidth) / 2.0f;
        float iconY = buttonY + buttonHeight / 2.0f - iconSize / 2.0f;
        this.renderPlayIcon(iconX - 5.0f, iconY, iconSize);
        float textX = startX + iconSize;
        float textY = buttonY + buttonHeight / 2.0f - 3.0f;
        int textAlpha = this.clamp((int)((180.0f + this.hoverAnimation * 50.0f + this.pressAnimation * 25.0f) * this.alphaMultiplier));
        int textGray = this.clamp((int)(180.0f + this.hoverAnimation * 40.0f + this.pressAnimation * 30.0f));
        Color textColor = new Color(textGray, textGray, textGray, textAlpha);
        Fonts.BOLD.draw(buttonText, textX, textY, 5.0f, textColor.getRGB());
    }

    private void renderPlayIcon(float iconX, float iconY, float size) {
        int iconAlpha = this.clamp((int)((160.0f + this.hoverAnimation * 60.0f + this.pressAnimation * 35.0f) * this.alphaMultiplier));
        int iconGray = this.clamp((int)(170.0f + this.hoverAnimation * 50.0f + this.pressAnimation * 30.0f));
        Color iconColor = new Color(iconGray, iconGray, iconGray, iconAlpha);
        float triangleWidth = size * 0.8f;
        float triangleHeight = size;
        Render2D.rect(iconX, iconY, triangleWidth * 0.4f, triangleHeight, iconColor.getRGB(), 1.0f);
        float dotSize = size * 0.35f;
        float dotX = iconX + triangleWidth * 0.5f;
        float dotY = iconY + (triangleHeight - dotSize) / 2.0f;
        Render2D.rect(dotX, dotY, dotSize, dotSize, iconColor.getRGB(), dotSize / 2.0f);
    }

    private boolean isButtonHover(double mouseX, double mouseY) {
        float buttonX = this.x + this.width - 65.0f - 2.0f;
        float buttonY = this.y + this.height / 2.0f - 6.0f;
        return mouseX >= (double)buttonX && mouseX <= (double)(buttonX + 65.0f) && mouseY >= (double)buttonY && mouseY <= (double)(buttonY + 12.0f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isButtonHover(mouseX, mouseY) && button == 0) {
            if (this.buttonSetting.getRunnable() != null) {
                this.buttonSetting.getRunnable().run();
            }
            this.wasPressed = true;
            this.pressAnimation = 1.0f;
            this.rippleActive = true;
            this.rippleAnimation = 0.0f;
            this.rippleX = (float)mouseX;
            this.rippleY = (float)mouseY;
            return true;
        }
        return false;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height);
    }
}

