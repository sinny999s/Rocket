
package rich.screens.hud;

import net.minecraft.client.gui.GuiGraphics;
import rich.client.draggables.AbstractHudElement;
import rich.util.ColorUtil;
import rich.util.render.Render2D;

public class test
extends AbstractHudElement {
    private float rotation = 0.0f;
    private float pulsePhase = 0.0f;
    private float toggleProgress = 0.0f;
    private boolean toggled = false;
    private float loadingProgress = 0.0f;
    private float hue = 0.0f;

    public test() {
        super("test", 10, 10, 340, 240, false);
    }

    @Override
    public void tick() {
        this.rotation += 3.0f;
        if (this.rotation >= 360.0f) {
            this.rotation -= 360.0f;
        }
        this.pulsePhase += 0.05f;
        if (this.pulsePhase >= (float)Math.PI * 2) {
            this.pulsePhase -= (float)Math.PI * 2;
        }
        if (this.toggled) {
            if (this.toggleProgress < 1.0f) {
                this.toggleProgress = Math.min(1.0f, this.toggleProgress + 0.05f);
            }
        } else if (this.toggleProgress > 0.0f) {
            this.toggleProgress = Math.max(0.0f, this.toggleProgress - 0.05f);
        }
        this.loadingProgress += 0.02f;
        if (this.loadingProgress >= 1.0f) {
            this.loadingProgress = 0.0f;
            this.toggled = !this.toggled;
        }
        this.hue += 0.005f;
        if (this.hue >= 1.0f) {
            this.hue -= 1.0f;
        }
    }

    @Override
    public void drawDraggable(GuiGraphics context, int alpha) {
        float x = this.getX();
        float y = this.getY();
        int bgColor = ColorUtil.applyAlpha(Integer.MIN_VALUE, alpha);
        Render2D.rect(x, y, this.width, this.height, bgColor, 8.0f);
        int borderColor = ColorUtil.applyAlpha(-12566464, alpha);
        Render2D.outline(x, y, this.width, this.height, 1.0f, borderColor, 8.0f);
        this.drawSpinnerArc(x + 30.0f, y + 30.0f, alpha);
        this.drawPulsingArc(x + 90.0f, y + 30.0f, alpha);
        this.drawToggleArc(x + 150.0f, y + 30.0f, alpha);
        this.drawLoadingArc(x + 210.0f, y + 30.0f, alpha);
        this.drawRainbowArc(x + 270.0f, y + 30.0f, alpha);
        this.drawSegmentedArc(x + 30.0f, y + 90.0f, alpha);
        this.drawDoubleArc(x + 90.0f, y + 90.0f, alpha);
        this.drawProgressRingArc(x + 150.0f, y + 90.0f, alpha);
        this.drawGradientSpinner(x + 210.0f, y + 90.0f, alpha);
        this.drawPieChart(x + 270.0f, y + 90.0f, alpha);
        this.drawCooldownArc(x + 30.0f, y + 150.0f, alpha);
        this.drawHealthRing(x + 90.0f, y + 150.0f, alpha);
        this.drawOutlinedSpinner(x + 150.0f, y + 150.0f, alpha);
        this.drawOutlinedProgress(x + 210.0f, y + 150.0f, alpha);
        this.drawOutlinedPulsing(x + 270.0f, y + 150.0f, alpha);
        this.drawOutlinedToggle(x + 30.0f, y + 210.0f, alpha);
        this.drawOutlinedRainbow(x + 90.0f, y + 210.0f, alpha);
        this.drawOutlinedDouble(x + 150.0f, y + 210.0f, alpha);
    }

    private void drawSpinnerArc(float cx, float cy, int alpha) {
        int color = ColorUtil.applyAlpha(-16733441, alpha);
        Render2D.arc(cx, cy, 20.0f, 3.0f, 270.0f, this.rotation, color);
    }

    private void drawPulsingArc(float cx, float cy, int alpha) {
        float pulse = (float)(Math.sin(this.pulsePhase) * 0.3 + 0.7);
        int baseColor = -16711800;
        int r = (int)((float)(baseColor >> 16 & 0xFF) * pulse);
        int g = (int)((float)(baseColor >> 8 & 0xFF) * pulse);
        int b = (int)((float)(baseColor & 0xFF) * pulse);
        int color = ColorUtil.applyAlpha(0xFF000000 | r << 16 | g << 8 | b, alpha);
        float thickness = 2.0f + pulse * 2.0f;
        Render2D.arc(cx, cy, 20.0f, thickness, 360.0f, 0.0f, color);
    }

    private void drawToggleArc(float cx, float cy, int alpha) {
        float degree = 90.0f + this.toggleProgress * 270.0f;
        int offColor = -10066330;
        int onColor = -16711936;
        int r = (int)((float)(offColor >> 16 & 0xFF) * (1.0f - this.toggleProgress) + (float)(onColor >> 16 & 0xFF) * this.toggleProgress);
        int g = (int)((float)(offColor >> 8 & 0xFF) * (1.0f - this.toggleProgress) + (float)(onColor >> 8 & 0xFF) * this.toggleProgress);
        int b = (int)((float)(offColor & 0xFF) * (1.0f - this.toggleProgress) + (float)(onColor & 0xFF) * this.toggleProgress);
        int color = ColorUtil.applyAlpha(0xFF000000 | r << 16 | g << 8 | b, alpha);
        Render2D.arc(cx, cy, 20.0f, 4.0f, degree, -90.0f, color);
        int bgColor = ColorUtil.applyAlpha(0x40FFFFFF, alpha);
        Render2D.arc(cx, cy, 20.0f, 2.0f, 360.0f, 0.0f, bgColor);
    }

    private void drawLoadingArc(float cx, float cy, int alpha) {
        int bgColor = ColorUtil.applyAlpha(0x40FFFFFF, alpha);
        Render2D.arc(cx, cy, 20.0f, 3.0f, 360.0f, 0.0f, bgColor);
        int fillColor = ColorUtil.applyAlpha(-22016, alpha);
        float degree = this.loadingProgress * 360.0f;
        Render2D.arc(cx, cy, 20.0f, 3.0f, degree, -90.0f, fillColor);
    }

    private void drawRainbowArc(float cx, float cy, int alpha) {
        int color1 = ColorUtil.applyAlpha(this.hsvToRgb(this.hue, 1.0f, 1.0f), alpha);
        int color2 = ColorUtil.applyAlpha(this.hsvToRgb((this.hue + 0.33f) % 1.0f, 1.0f, 1.0f), alpha);
        int color3 = ColorUtil.applyAlpha(this.hsvToRgb((this.hue + 0.66f) % 1.0f, 1.0f, 1.0f), alpha);
        Render2D.arc(cx, cy, 20.0f, 4.0f, 360.0f, this.rotation, color1, color2, color3);
    }

    private void drawSegmentedArc(float cx, float cy, int alpha) {
        int[] colors = new int[]{ColorUtil.applyAlpha(-65536, alpha), ColorUtil.applyAlpha(-30720, alpha), ColorUtil.applyAlpha(-256, alpha), ColorUtil.applyAlpha(-16711936, alpha)};
        for (int i = 0; i < 4; ++i) {
            float segmentRotation = (float)i * 90.0f + this.rotation * 0.5f;
            Render2D.arc(cx, cy, 20.0f, 3.0f, 80.0f, segmentRotation, colors[i]);
        }
    }

    private void drawDoubleArc(float cx, float cy, int alpha) {
        int outerColor = ColorUtil.applyAlpha(-7864065, alpha);
        Render2D.arc(cx, cy, 20.0f, 2.0f, 180.0f, this.rotation, outerColor);
        int innerColor = ColorUtil.applyAlpha(-65400, alpha);
        Render2D.arc(cx, cy, 14.0f, 2.0f, 180.0f, -this.rotation * 1.5f, innerColor);
    }

    private void drawProgressRingArc(float cx, float cy, int alpha) {
        int bgColor = ColorUtil.applyAlpha(0x30FFFFFF, alpha);
        Render2D.arc(cx, cy, 20.0f, 6.0f, 360.0f, 0.0f, bgColor);
        float progress = this.loadingProgress;
        int progressColor = ColorUtil.applyAlpha(-16720385, alpha);
        Render2D.arc(cx, cy, 20.0f, 6.0f, progress * 360.0f, -90.0f, progressColor);
        int glowColor = ColorUtil.applyAlpha(1610669567, alpha);
        Render2D.arc(cx, cy, 22.0f, 2.0f, progress * 360.0f, -90.0f, glowColor);
    }

    private void drawGradientSpinner(float cx, float cy, int alpha) {
        int startColor = ColorUtil.applyAlpha(-65536, alpha);
        int midColor = ColorUtil.applyAlpha(-256, alpha);
        int endColor = ColorUtil.applyAlpha(-16711936, alpha);
        Render2D.arc(cx, cy, 16.0f, 4.0f, 300.0f, this.rotation, startColor, midColor, endColor, midColor, endColor, startColor, endColor, startColor, midColor);
    }

    private void drawPieChart(float cx, float cy, int alpha) {
        float[] values = new float[]{0.35f, 0.25f, 0.25f, 0.15f};
        int[] colors = new int[]{ColorUtil.applyAlpha(-48060, alpha), ColorUtil.applyAlpha(-12255420, alpha), ColorUtil.applyAlpha(-12303105, alpha), ColorUtil.applyAlpha(-188, alpha)};
        float currentRotation = -90.0f;
        for (int i = 0; i < values.length; ++i) {
            float degree = values[i] * 360.0f;
            Render2D.arc(cx, cy, 18.0f, 18.0f, degree - 2.0f, currentRotation, colors[i]);
            currentRotation += degree;
        }
    }

    private void drawCooldownArc(float cx, float cy, int alpha) {
        float cooldown = 1.0f - this.loadingProgress;
        int readyColor = ColorUtil.applyAlpha(-16711936, alpha);
        int cooldownColor = ColorUtil.applyAlpha(-7829368, alpha);
        Render2D.arc(cx, cy, 18.0f, 18.0f, 360.0f, -90.0f, cooldownColor);
        if (cooldown < 1.0f) {
            float degree = (1.0f - cooldown) * 360.0f;
            Render2D.arc(cx, cy, 18.0f, 18.0f, degree, -90.0f, readyColor);
        }
        int borderColor = ColorUtil.applyAlpha(-1, alpha);
        Render2D.arc(cx, cy, 18.0f, 1.0f, 360.0f, 0.0f, borderColor);
    }

    private void drawHealthRing(float cx, float cy, int alpha) {
        float health = 0.5f + (float)Math.sin(this.pulsePhase) * 0.3f;
        int bgColor = ColorUtil.applyAlpha(0x40FF0000, alpha);
        Render2D.arc(cx, cy, 18.0f, 5.0f, 360.0f, -90.0f, bgColor);
        int healthColor = health > 0.6f ? ColorUtil.applyAlpha(-16711936, alpha) : (health > 0.3f ? ColorUtil.applyAlpha(-22016, alpha) : ColorUtil.applyAlpha(-65536, alpha));
        float degree = health * 360.0f;
        Render2D.arc(cx, cy, 18.0f, 5.0f, degree, -90.0f, healthColor);
        if (health < 0.3f) {
            float pulse = (float)(Math.sin(this.pulsePhase * 4.0f) * 0.5 + 0.5);
            int pulseColor = ColorUtil.applyAlpha((int)(96.0f * pulse) << 24 | 0xFF0000, alpha);
            Render2D.arc(cx, cy, 20.0f, 2.0f, degree, -90.0f, pulseColor);
        }
    }

    private void drawOutlinedSpinner(float cx, float cy, int alpha) {
        int fillColor = ColorUtil.applyAlpha(-16733441, alpha);
        int outlineColor = ColorUtil.applyAlpha(-1, alpha);
        Render2D.arcOutline(cx, cy, 20.0f, 4.0f, 270.0f, this.rotation, 1.5f, fillColor, outlineColor);
    }

    private void drawOutlinedProgress(float cx, float cy, int alpha) {
        int bgFill = ColorUtil.applyAlpha(0x40FFFFFF, alpha);
        int bgOutline = ColorUtil.applyAlpha(-2130706433, alpha);
        Render2D.arcOutline(cx, cy, 20.0f, 5.0f, 360.0f, 0.0f, 1.0f, bgFill, bgOutline);
        float degree = this.loadingProgress * 360.0f;
        int fillColor = ColorUtil.applyAlpha(-16711800, alpha);
        int outlineColor = ColorUtil.applyAlpha(-1, alpha);
        Render2D.arcOutline(cx, cy, 20.0f, 5.0f, degree, -90.0f, 1.5f, fillColor, outlineColor);
    }

    private void drawOutlinedPulsing(float cx, float cy, int alpha) {
        float pulse = (float)(Math.sin(this.pulsePhase) * 0.3 + 0.7);
        float thickness = 3.0f + pulse * 2.0f;
        int fillColor = ColorUtil.applyAlpha(-39424, alpha);
        int outlineColor = ColorUtil.applyAlpha(-1, alpha);
        Render2D.arcOutline(cx, cy, 20.0f, thickness, 360.0f, 0.0f, 1.0f, fillColor, outlineColor);
    }

    private void drawOutlinedToggle(float cx, float cy, int alpha) {
        float degree = 90.0f + this.toggleProgress * 270.0f;
        int offFill = -12303292;
        int onFill = -16720640;
        int r = (int)((float)(offFill >> 16 & 0xFF) * (1.0f - this.toggleProgress) + (float)(onFill >> 16 & 0xFF) * this.toggleProgress);
        int g = (int)((float)(offFill >> 8 & 0xFF) * (1.0f - this.toggleProgress) + (float)(onFill >> 8 & 0xFF) * this.toggleProgress);
        int b = (int)((float)(offFill & 0xFF) * (1.0f - this.toggleProgress) + (float)(onFill & 0xFF) * this.toggleProgress);
        int fillColor = ColorUtil.applyAlpha(0xFF000000 | r << 16 | g << 8 | b, alpha);
        int outlineColor = ColorUtil.applyAlpha(-1, alpha);
        Render2D.arcOutline(cx, cy, 20.0f, 5.0f, degree, -90.0f, 2.0f, fillColor, outlineColor);
    }

    private void drawOutlinedRainbow(float cx, float cy, int alpha) {
        int fillColor = ColorUtil.applyAlpha(this.hsvToRgb(this.hue, 0.8f, 1.0f), alpha);
        int outlineColor = ColorUtil.applyAlpha(-1, alpha);
        Render2D.arcOutline(cx, cy, 20.0f, 4.0f, 300.0f, this.rotation, 1.5f, fillColor, outlineColor);
    }

    private void drawOutlinedDouble(float cx, float cy, int alpha) {
        int outerFill = ColorUtil.applyAlpha(-7864065, alpha);
        int outerOutline = ColorUtil.applyAlpha(-1, alpha);
        Render2D.arcOutline(cx, cy, 20.0f, 3.0f, 180.0f, this.rotation, 1.0f, outerFill, outerOutline);
        int innerFill = ColorUtil.applyAlpha(-65400, alpha);
        int innerOutline = ColorUtil.applyAlpha(-1, alpha);
        Render2D.arcOutline(cx, cy, 12.0f, 3.0f, 180.0f, -this.rotation * 1.5f, 1.0f, innerFill, innerOutline);
    }

    private int hsvToRgb(float h, float s, float v) {
        float b;
        float g;
        float r;
        float c = v * s;
        float x = c * (1.0f - Math.abs(h * 6.0f % 2.0f - 1.0f));
        float m = v - c;
        if (h < 0.16666667f) {
            r = c;
            g = x;
            b = 0.0f;
        } else if (h < 0.33333334f) {
            r = x;
            g = c;
            b = 0.0f;
        } else if (h < 0.5f) {
            r = 0.0f;
            g = c;
            b = x;
        } else if (h < 0.6666667f) {
            r = 0.0f;
            g = x;
            b = c;
        } else if (h < 0.8333333f) {
            r = x;
            g = 0.0f;
            b = c;
        } else {
            r = c;
            g = 0.0f;
            b = x;
        }
        int ri = (int)((r + m) * 255.0f);
        int gi = (int)((g + m) * 255.0f);
        int bi = (int)((b + m) * 255.0f);
        return 0xFF000000 | ri << 16 | gi << 8 | bi;
    }

    public void toggle() {
        this.toggled = !this.toggled;
    }

    public boolean isToggled() {
        return this.toggled;
    }
}

