
package rich.modules.module.setting.implement;

import java.awt.Color;
import java.util.function.Supplier;
import lombok.Generated;
import rich.modules.module.setting.Setting;

public class ColorSetting
extends Setting {
    private float hue = 0.0f;
    private float saturation = 1.0f;
    private float brightness = 1.0f;
    private float alpha = 1.0f;
    private int[] presets = new int[0];

    public ColorSetting(String name, String description) {
        super(name, description);
    }

    public ColorSetting value(int value) {
        this.setColor(value);
        return this;
    }

    public ColorSetting presets(int ... presets) {
        this.presets = presets;
        return this;
    }

    public ColorSetting visible(Supplier<Boolean> visible) {
        this.setVisible(visible);
        return this;
    }

    public int getColor() {
        int rgb = Color.HSBtoRGB(this.hue, this.saturation, this.brightness);
        int alphaInt = Math.round(this.alpha * 255.0f);
        return alphaInt << 24 | rgb & 0xFFFFFF;
    }

    public int getColorWithAlpha() {
        return this.getColor();
    }

    public int getColorNoAlpha() {
        return Color.HSBtoRGB(this.hue, this.saturation, this.brightness) | 0xFF000000;
    }

    public ColorSetting setColor(int color) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = color >> 24 & 0xFF;
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
        this.alpha = (float)a / 255.0f;
        return this;
    }

    public Color getAwtColor() {
        int color = this.getColor();
        return new Color(color, true);
    }

    public ColorSetting setHue(float hue) {
        this.hue = Math.max(0.0f, Math.min(1.0f, hue));
        return this;
    }

    public ColorSetting setSaturation(float saturation) {
        this.saturation = Math.max(0.0f, Math.min(1.0f, saturation));
        return this;
    }

    public ColorSetting setBrightness(float brightness) {
        this.brightness = Math.max(0.0f, Math.min(1.0f, brightness));
        return this;
    }

    public ColorSetting setAlpha(float alpha) {
        this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        return this;
    }

    @Generated
    public float getHue() {
        return this.hue;
    }

    @Generated
    public float getSaturation() {
        return this.saturation;
    }

    @Generated
    public float getBrightness() {
        return this.brightness;
    }

    @Generated
    public float getAlpha() {
        return this.alpha;
    }

    @Generated
    public int[] getPresets() {
        return this.presets;
    }

    @Generated
    public void setPresets(int[] presets) {
        this.presets = presets;
    }
}

