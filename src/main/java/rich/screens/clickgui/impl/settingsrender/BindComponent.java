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
import rich.modules.module.setting.implement.BindSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class BindComponent
extends AbstractSettingComponent {
    private boolean listening = false;
    private float listeningAnimation = 0.0f;
    private float hoverAnimation = 0.0f;
    private float bindHoverAnimation = 0.0f;
    private float pulseAnimation = 0.0f;
    private float scaleAnimation = 1.0f;
    private float glowAnimation = 0.0f;
    private float textChangeAnimation = 0.0f;
    private String previousBindText = "";
    private String currentBindText = "";
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float FAST_ANIMATION_SPEED = 12.0f;
    private static final float BIND_BOX_WIDTH = 32.0f;
    private static final float BIND_BOX_HEIGHT = 10.0f;
    public static final int SCROLL_UP_BIND = 1000;
    public static final int SCROLL_DOWN_BIND = 1001;
    public static final int MIDDLE_MOUSE_BIND = 1002;

    public BindComponent(BindSetting setting) {
        super(setting);
        BindSetting bindSetting = (BindSetting)this.getSetting();
        this.previousBindText = this.currentBindText = this.getBindDisplayName(bindSetting.getKey(), bindSetting.getType());
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
        String newBindText;
        float deltaTime = this.getDeltaTime();
        boolean hovered = this.isHover(mouseX, mouseY);
        boolean bindHovered = this.isBindHover(mouseX, mouseY);
        this.hoverAnimation = this.lerp(this.hoverAnimation, hovered ? 1.0f : 0.0f, deltaTime * 8.0f);
        this.bindHoverAnimation = this.lerp(this.bindHoverAnimation, bindHovered ? 1.0f : 0.0f, deltaTime * 8.0f);
        this.listeningAnimation = this.lerp(this.listeningAnimation, this.listening ? 1.0f : 0.0f, deltaTime * 12.0f);
        float scaleTarget = this.listening ? 1.05f : (bindHovered ? 1.02f : 1.0f);
        this.scaleAnimation = this.lerp(this.scaleAnimation, scaleTarget, deltaTime * 8.0f);
        this.glowAnimation = this.lerp(this.glowAnimation, this.listening ? 1.0f : 0.0f, deltaTime * 8.0f);
        if (this.listening) {
            this.pulseAnimation += deltaTime * 4.0f;
            if ((double)this.pulseAnimation > Math.PI * 2) {
                this.pulseAnimation -= (float)Math.PI * 2;
            }
        } else {
            this.pulseAnimation = this.lerp(this.pulseAnimation, 0.0f, deltaTime * 8.0f);
        }
        BindSetting bindSetting = (BindSetting)this.getSetting();
        String string = newBindText = this.listening ? "" : this.getBindDisplayName(bindSetting.getKey(), bindSetting.getType());
        if (!newBindText.equals(this.currentBindText)) {
            this.previousBindText = this.currentBindText;
            this.currentBindText = newBindText;
            this.textChangeAnimation = 0.0f;
        }
        this.textChangeAnimation = this.lerp(this.textChangeAnimation, 1.0f, deltaTime * 12.0f);
        int iconAlpha = (int)(200.0f * this.alphaMultiplier);
        Fonts.GUI_ICONS.draw("L", this.x + 1.5f, this.y + this.height / 2.0f - 6.0f, 6.0f, new Color(210, 210, 210, iconAlpha).getRGB());
        Fonts.BOLD.draw(this.getSetting().getName(), this.x + 9.5f, this.y + this.height / 2.0f - 7.5f, 6.0f, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
        String description = this.getSetting().getDescription();
        if (description != null && !description.isEmpty()) {
            Fonts.BOLD.draw(description, this.x + 0.5f, this.y + this.height / 2.0f + 0.5f, 5.0f, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
        }
        this.renderBindBox(mouseX, mouseY, bindSetting);
    }

    private void renderBindBox(int mouseX, int mouseY, BindSetting bindSetting) {
        Color outlineColor;
        Color bgColor;
        float bindBoxX = this.x + this.width - 32.0f - 2.0f;
        float bindBoxY = this.y + this.height / 2.0f - 5.0f;
        float scaledWidth = 32.0f * this.scaleAnimation;
        float scaledHeight = 10.0f * this.scaleAnimation;
        float scaledX = bindBoxX - (scaledWidth - 32.0f) / 2.0f;
        float scaledY = bindBoxY - (scaledHeight - 10.0f) / 2.0f;
        int bgAlpha = (int)(25.0f + this.bindHoverAnimation * 15.0f + this.listeningAnimation * 20.0f);
        if (this.listening) {
            float pulse = (float)(Math.sin(this.pulseAnimation) * 0.15 + 0.85);
            bgColor = new Color((int)(60.0f + 40.0f * pulse), (int)(80.0f + 40.0f * pulse), (int)(120.0f + 35.0f * pulse), (int)((float)bgAlpha * this.alphaMultiplier));
        } else {
            bgColor = bindSetting.getKey() != -1 && bindSetting.getKey() != -1 ? this.applyAlpha(new Color(40, 60, 50, bgAlpha)) : this.applyAlpha(new Color(40, 40, 45, bgAlpha));
        }
        Render2D.rect(scaledX, scaledY, scaledWidth, scaledHeight, bgColor.getRGB(), 3.0f);
        float outlineAlpha;
        if (this.listening) {
            float pulse = (float)(Math.sin(this.pulseAnimation) * 0.3 + 0.7);
            outlineAlpha = 150.0f * pulse * this.listeningAnimation;
            outlineColor = new Color(120, 160, 220, (int)(outlineAlpha * this.alphaMultiplier));
        } else if (bindSetting.getKey() != -1 && bindSetting.getKey() != -1) {
            outlineAlpha = 80.0f + this.bindHoverAnimation * 40.0f;
            outlineColor = new Color(100, 160, 120, (int)(outlineAlpha * this.alphaMultiplier));
        } else {
            outlineAlpha = 60.0f + this.bindHoverAnimation * 40.0f;
            outlineColor = new Color(120, 120, 125, (int)(outlineAlpha * this.alphaMultiplier));
        }
        Render2D.outline(scaledX, scaledY, scaledWidth, scaledHeight, 0.5f, outlineColor.getRGB(), 3.0f);
        this.renderBindText(scaledX, scaledY, scaledWidth, scaledHeight, bindSetting);
        if (this.listening) {
            this.renderListeningIndicator(scaledX, scaledY, scaledWidth, scaledHeight);
        }
    }

    private void renderBindText(float boxX, float boxY, float boxWidth, float boxHeight, BindSetting bindSetting) {
        Color textColor;
        float textY = boxY + boxHeight / 2.0f - 2.5f;
        float centerX = boxX + boxWidth / 2.0f;
        if (this.listening) {
            float pulse = (float)(Math.sin(this.pulseAnimation * 2.0f) * 0.2 + 0.8);
            int alpha = (int)(220.0f * pulse * this.alphaMultiplier);
            textColor = new Color(180, 200, 240, alpha);
        } else if (bindSetting.getKey() != -1 && bindSetting.getKey() != -1) {
            int alpha = (int)(200.0f * this.alphaMultiplier);
            textColor = new Color(140, 200, 150, alpha);
        } else {
            int alpha = (int)(150.0f * this.alphaMultiplier);
            textColor = new Color(140, 140, 150, alpha);
        }
        if (this.textChangeAnimation < 1.0f && !this.previousBindText.equals(this.currentBindText)) {
            float oldAlpha = 1.0f - this.textChangeAnimation;
            float newAlpha = this.textChangeAnimation;
            float oldOffsetY = -3.0f * this.textChangeAnimation;
            float newOffsetY = 3.0f * (1.0f - this.textChangeAnimation);
            if (oldAlpha > 0.01f) {
                Color oldColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)((float)textColor.getAlpha() * oldAlpha));
                Fonts.BOLD.drawCentered(this.previousBindText, centerX, textY + oldOffsetY, 5.0f, oldColor.getRGB());
            }
            Color newColor = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), (int)((float)textColor.getAlpha() * newAlpha));
            Fonts.BOLD.drawCentered(this.currentBindText, centerX, textY + newOffsetY, 5.0f, newColor.getRGB());
        } else {
            Fonts.BOLD.drawCentered(this.currentBindText, centerX, textY, 5.0f, textColor.getRGB());
        }
    }

    private void renderListeningIndicator(float boxX, float boxY, float boxWidth, float boxHeight) {
        float dotSpacing = 3.0f;
        float dotSize = 1.5f;
        float dotsWidth = dotSpacing * 2.0f;
        float startX = boxX + (boxWidth - dotsWidth) / 2.0f - dotSize / 2.0f;
        float dotY = boxY + boxHeight - 5.5f;
        for (int i = 0; i < 3; ++i) {
            float phase = this.pulseAnimation + (float)i * 0.5f;
            float pulse = (float)(Math.sin(phase * 2.0f) * 0.5 + 0.5);
            float currentDotSize = dotSize * (0.5f + pulse * 0.5f);
            int alpha = (int)(150.0f * (0.3f + pulse * 0.7f) * this.listeningAnimation * this.alphaMultiplier);
            float dotX = startX + (float)i * dotSpacing + (dotSize - currentDotSize) / 2.0f;
            float adjustedDotY = dotY + (dotSize - currentDotSize) / 2.0f;
            Render2D.rect(dotX, adjustedDotY, currentDotSize, currentDotSize, new Color(120, 160, 220, alpha).getRGB(), currentDotSize / 2.0f);
        }
    }

    private String getBindDisplayName(int key, int type) {
        if (key == -1 || key == -1) {
            return "None";
        }
        if (key == 1000) {
            return "ScrollUp";
        }
        if (key == 1001) {
            return "ScrollDn";
        }
        if (key == 1002) {
            return "MMB";
        }
        if (type == 0) {
            return switch (key) {
                case 0 -> "LMB";
                case 1 -> "RMB";
                case 2 -> "MMB";
                case 3 -> "M4";
                case 4 -> "M5";
                case 5 -> "M6";
                case 6 -> "M7";
                case 7 -> "M8";
                default -> "M" + key;
            };
        }
        String keyName = GLFW.glfwGetKeyName((int)key, (int)0);
        if (keyName == null) {
            return switch (key) {
                case 340 -> "LShift";
                case 344 -> "RShift";
                case 341 -> "LCtrl";
                case 345 -> "RCtrl";
                case 342 -> "LAlt";
                case 346 -> "RAlt";
                case 32 -> "Space";
                case 258 -> "Tab";
                case 280 -> "Caps";
                case 257 -> "Enter";
                case 259 -> "Back";
                case 260 -> "Ins";
                case 261 -> "Del";
                case 268 -> "Home";
                case 269 -> "End";
                case 266 -> "PgUp";
                case 267 -> "PgDn";
                case 265 -> "Up";
                case 264 -> "Down";
                case 263 -> "Left";
                case 262 -> "Right";
                case 290 -> "F1";
                case 291 -> "F2";
                case 292 -> "F3";
                case 293 -> "F4";
                case 294 -> "F5";
                case 295 -> "F6";
                case 296 -> "F7";
                case 297 -> "F8";
                case 298 -> "F9";
                case 299 -> "F10";
                case 300 -> "F11";
                case 301 -> "F12";
                case 256 -> "Esc";
                case 283 -> "Print";
                case 281 -> "Scroll";
                case 284 -> "Pause";
                case 282 -> "NumLk";
                case 320 -> "Num0";
                case 321 -> "Num1";
                case 322 -> "Num2";
                case 323 -> "Num3";
                case 324 -> "Num4";
                case 325 -> "Num5";
                case 326 -> "Num6";
                case 327 -> "Num7";
                case 328 -> "Num8";
                case 329 -> "Num9";
                case 330 -> "Num.";
                case 331 -> "Num/";
                case 332 -> "Num*";
                case 333 -> "Num-";
                case 334 -> "Num+";
                case 335 -> "NumEnt";
                default -> "Key" + key;
            };
        }
        return keyName.toUpperCase();
    }

    private boolean isBindHover(double mouseX, double mouseY) {
        float bindBoxX = this.x + this.width - 32.0f - 2.0f;
        float bindBoxY = this.y + this.height / 2.0f - 5.0f;
        return mouseX >= (double)bindBoxX && mouseX <= (double)(bindBoxX + 32.0f) && mouseY >= (double)bindBoxY && mouseY <= (double)(bindBoxY + 10.0f);
    }

    public void handleScrollBind(double vertical) {
        if (this.listening) {
            BindSetting bindSetting = (BindSetting)this.getSetting();
            if (vertical > 0.0) {
                bindSetting.setKey(1000);
            } else {
                bindSetting.setKey(1001);
            }
            bindSetting.setType(2);
            this.listening = false;
        }
    }

    public void handleMiddleMouseBind() {
        if (this.listening) {
            BindSetting bindSetting = (BindSetting)this.getSetting();
            bindSetting.setKey(1002);
            bindSetting.setType(2);
            this.listening = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isBindHover(mouseX, mouseY)) {
            if (button == 1) {
                ((BindSetting)this.getSetting()).setKey(-1);
                ((BindSetting)this.getSetting()).setType(1);
                this.listening = false;
                return true;
            }
            if (this.listening) {
                ((BindSetting)this.getSetting()).setKey(button);
                ((BindSetting)this.getSetting()).setType(0);
                this.listening = false;
                return true;
            }
            if (button == 0) {
                this.listening = true;
                return true;
            }
        } else if (this.listening) {
            this.listening = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.listening) {
            if (keyCode == 256) {
                this.listening = false;
                return true;
            }
            if (keyCode == 259 || keyCode == 261) {
                ((BindSetting)this.getSetting()).setKey(-1);
                ((BindSetting)this.getSetting()).setType(1);
                this.listening = false;
                return true;
            }
            if (keyCode != -1) {
                ((BindSetting)this.getSetting()).setKey(keyCode);
                ((BindSetting)this.getSetting()).setType(1);
                this.listening = false;
                return true;
            }
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

    public boolean isListening() {
        return this.listening;
    }
}

