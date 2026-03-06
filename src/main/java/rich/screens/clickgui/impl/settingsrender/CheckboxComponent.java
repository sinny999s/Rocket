
package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class CheckboxComponent
extends AbstractSettingComponent {
    private final BooleanSetting booleanSetting;
    private float checkAnimation = 0.0f;
    private float hoverAnimation = 0.0f;
    private float stretchAnimation = 0.0f;
    private float velocity = 0.0f;

    public CheckboxComponent(BooleanSetting setting) {
        super(setting);
        this.booleanSetting = setting;
        this.checkAnimation = setting.isValue() ? 1.0f : 0.0f;
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHover(mouseX, mouseY);
        float hoverTarget = hovered ? 1.0f : 0.0f;
        this.hoverAnimation += (hoverTarget - this.hoverAnimation) * 0.2f;
        this.hoverAnimation = this.clamp(this.hoverAnimation, 0.0f, 1.0f);
        float target = this.booleanSetting.isValue() ? 1.0f : 0.0f;
        float oldCheck = this.checkAnimation;
        float speed = 0.35f;
        this.checkAnimation += (target - this.checkAnimation) * speed;
        if (Math.abs(target - this.checkAnimation) < 0.001f) {
            this.checkAnimation = target;
        }
        this.velocity = this.checkAnimation - oldCheck;
        float absVelocity = Math.abs(this.velocity);
        float targetStretch = absVelocity * 30.0f;
        float stretchSpeed = (targetStretch = this.clamp(targetStretch, 0.0f, 1.0f)) > this.stretchAnimation ? 0.5f : 0.2f;
        this.stretchAnimation += (targetStretch - this.stretchAnimation) * stretchSpeed;
        this.stretchAnimation = this.clamp(this.stretchAnimation, 0.0f, 1.0f);
        int iconAlpha = (int)(200.0f * this.alphaMultiplier);
        Fonts.GUI_ICONS.draw("T", this.x + 0.5f, this.y + this.height / 2.0f - 11.0f, 11.0f, new Color(210, 210, 210, iconAlpha).getRGB());
        Fonts.BOLD.draw(this.booleanSetting.getName(), this.x + 9.5f, this.y + this.height / 2.0f - 7.5f, 6.0f, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
        Fonts.BOLD.draw(this.booleanSetting.getDescription(), this.x + 0.5f, this.y + this.height / 2.0f + 0.5f, 5.0f, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
        float checkboxSize = 10.0f;
        float checkboxWidth = checkboxSize + 6.0f;
        float checkboxX = this.x + this.width - checkboxWidth - 2.0f;
        float checkboxY = this.y + this.height / 2.0f - checkboxSize / 2.0f;
        Render2D.rect(checkboxX, checkboxY, checkboxWidth, checkboxSize, this.applyAlpha(new Color(55, 55, 55, 25)).getRGB(), 4.0f);
        int outlineAlpha = 60 + (int)(this.hoverAnimation * 40.0f);
        Render2D.outline(checkboxX, checkboxY, checkboxWidth, checkboxSize, 0.5f, this.applyAlpha(new Color(155, 155, 155, outlineAlpha)).getRGB(), 4.0f);
        float knobBaseSize = checkboxSize - 3.0f;
        float maxStretchExtra = 4.0f;
        float stretchExtra = this.stretchAnimation * maxStretchExtra;
        float knobWidth = knobBaseSize + stretchExtra;
        float knobHeight = knobBaseSize - this.stretchAnimation * 1.0f;
        float padding = 1.5f;
        float travelDistance = checkboxWidth - knobBaseSize - padding * 2.0f;
        float knobBaseX = checkboxX + padding;
        float stretchOffset = this.velocity > 0.0f ? -stretchExtra * 0.3f : (this.velocity < 0.0f ? stretchExtra * 0.3f : 0.0f);
        float knobX = knobBaseX + travelDistance * this.checkAnimation - stretchExtra * this.checkAnimation + stretchOffset;
        float knobY = checkboxY + (checkboxSize - knobHeight) / 2.0f;
        Color offColor = new Color(59, 59, 59, 200);
        Color onColor = new Color(159, 159, 159, 200);
        Color knobColor = this.lerpColor(offColor, onColor, this.checkAnimation);
        Render2D.rect(knobX, knobY, knobWidth, knobHeight, this.applyAlpha(knobColor).getRGB(), 4.0f);
    }

    private Color lerpColor(Color a, Color b, float t) {
        int r = (int)((float)a.getRed() + (float)(b.getRed() - a.getRed()) * t);
        int g = (int)((float)a.getGreen() + (float)(b.getGreen() - a.getGreen()) * t);
        int bl = (int)((float)a.getBlue() + (float)(b.getBlue() - a.getBlue()) * t);
        int al = (int)((float)a.getAlpha() + (float)(b.getAlpha() - a.getAlpha()) * t);
        return new Color(this.clamp(r, 0, 255), this.clamp(g, 0, 255), this.clamp(bl, 0, 255), this.clamp(al, 0, 255));
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHover(mouseX, mouseY) && button == 0) {
            this.booleanSetting.setValue(!this.booleanSetting.isValue());
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

