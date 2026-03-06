
package rich.modules.impl.render;

import lombok.Generated;
import rich.events.api.EventHandler;
import rich.events.impl.GlassHandsRenderEvent;
import rich.events.impl.WorldChangeEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.render.shader.GlassHandsRenderer;

public class GlassHands
extends ModuleStructure {
    private static GlassHands instance;
    private final SliderSettings blurRadius = new SliderSettings("Blur strength", "Glass blur effect strength").setValue(2.5f).range(1.0f, 5.0f);
    private final SliderSettings blurIterations = new SliderSettings("Quality", "Blur iteration count").setValue(3.0f).range(1, 5);
    private final SliderSettings saturation = new SliderSettings("Saturation", "Color saturation").setValue(0.0f).range(0.0f, 2.0f);
    private final BooleanSetting enableTint = new BooleanSetting("Tint", "Enable colored glass tint").setValue(false);
    private final SliderSettings tintIntensity = new SliderSettings("Tint strength", "Tint intensity").setValue(0.2f).range(0.0f, 0.5f).visible(this.enableTint::isValue);
    private final ColorSetting tintColor = new ColorSetting("Color tint", "Color glass tint").value(-16711681).visible(this.enableTint::isValue);
    private final BooleanSetting enableEdgeGlow = new BooleanSetting("Edge glow", "Edge glow on glass").setValue(true);
    private final SliderSettings edgeGlowIntensity = new SliderSettings("Glow strength", "Edge glow intensity").setValue(0.2f).range(0.0f, 1.0f).visible(this.enableEdgeGlow::isValue);

    public GlassHands() {
        super("GlassHands", "Makes hands and items glass", ModuleCategory.RENDER);
        this.settings(this.blurRadius, this.blurIterations, this.saturation, this.enableTint, this.tintIntensity, this.tintColor, this.enableEdgeGlow, this.edgeGlowIntensity);
        instance = this;
    }

    public static GlassHands getInstance() {
        return instance;
    }

    @Override
    public void activate() {
        GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
        if (renderer != null) {
            renderer.invalidate();
            renderer.setEnabled(true);
            this.updateRendererSettings();
        }
    }

    @Override
    public void deactivate() {
        GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
        if (renderer != null) {
            renderer.setEnabled(false);
        }
    }

    @EventHandler
    public void onWorldChange(WorldChangeEvent event) {
        if (!this.isState()) {
            return;
        }
        GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
        if (renderer != null) {
            renderer.invalidate();
            renderer.setEnabled(true);
            this.updateRendererSettings();
        }
    }

    @EventHandler
    public void onGlassHandsRender(GlassHandsRenderEvent event) {
        if (!this.isState()) {
            return;
        }
        GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
        if (renderer == null) {
            return;
        }
        this.updateRendererSettings();
        if (event.getPhase() == GlassHandsRenderEvent.Phase.PRE) {
            renderer.captureSceneBeforeHands();
        } else if (event.getPhase() == GlassHandsRenderEvent.Phase.POST) {
            renderer.captureSceneAfterHands();
            renderer.renderGlassEffect();
        }
    }

    private void updateRendererSettings() {
        GlassHandsRenderer renderer = GlassHandsRenderer.getInstance();
        if (renderer == null) {
            return;
        }
        renderer.setBlurRadius(this.blurRadius.getValue());
        renderer.setBlurIterations(this.blurIterations.getInt());
        renderer.setSaturation(this.saturation.getValue());
        renderer.setReflect(true);
        if (this.enableTint.isValue()) {
            renderer.setTintColor(this.tintColor.getColor());
            renderer.setTintIntensity(this.tintIntensity.getValue());
        } else {
            renderer.setTintColor(0);
            renderer.setTintIntensity(0.0f);
        }
        if (this.enableEdgeGlow.isValue()) {
            renderer.setEdgeGlowIntensity(this.edgeGlowIntensity.getValue());
        } else {
            renderer.setEdgeGlowIntensity(0.0f);
        }
    }

    @Generated
    public SliderSettings getBlurRadius() {
        return this.blurRadius;
    }

    @Generated
    public SliderSettings getBlurIterations() {
        return this.blurIterations;
    }

    @Generated
    public SliderSettings getSaturation() {
        return this.saturation;
    }

    @Generated
    public BooleanSetting getEnableTint() {
        return this.enableTint;
    }

    @Generated
    public SliderSettings getTintIntensity() {
        return this.tintIntensity;
    }

    @Generated
    public ColorSetting getTintColor() {
        return this.tintColor;
    }

    @Generated
    public BooleanSetting getEnableEdgeGlow() {
        return this.enableEdgeGlow;
    }

    @Generated
    public SliderSettings getEdgeGlowIntensity() {
        return this.edgeGlowIntensity;
    }
}

