/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL14
 */
package rich.util.render.shader;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import rich.util.render.font.FontRenderer;
import rich.util.render.font.Fonts;
import rich.util.render.pipeline.Arc2D;
import rich.util.render.pipeline.ArcOutline2D;
import rich.util.render.pipeline.BlurPipeline;
import rich.util.render.pipeline.GlassCompositePipeline;
import rich.util.render.pipeline.GlowOutlinePipeline;
import rich.util.render.pipeline.KawaseBlurPipeline;
import rich.util.render.pipeline.MaskDiffPipeline;
import rich.util.render.pipeline.OutlinePipeline;
import rich.util.render.pipeline.RectPipeline;
import rich.util.render.pipeline.TexturePipeline;
import rich.util.render.shader.GlassHandsRenderer;

public class RenderCore {
    private final RectPipeline rectPipeline = new RectPipeline();
    private final OutlinePipeline outlinePipeline = new OutlinePipeline();
    private final GlowOutlinePipeline glowOutlinePipeline = new GlowOutlinePipeline();
    private final TexturePipeline texturePipeline = new TexturePipeline();
    private final BlurPipeline blurPipeline = new BlurPipeline();
    private final KawaseBlurPipeline kawaseBlurPipeline = new KawaseBlurPipeline();
    private final GlassCompositePipeline glassCompositePipeline = new GlassCompositePipeline();
    private final GlassHandsRenderer glassHandsRenderer = new GlassHandsRenderer();
    private final FontRenderer fontRenderer;
    private final MaskDiffPipeline maskDiffPipeline = new MaskDiffPipeline();
    private boolean fontsLoaded = false;
    private boolean arcInitialized = false;
    private boolean arcOutlineInitialized = false;

    public RenderCore() {
        this.fontRenderer = new FontRenderer();
    }

    private void ensureFontsLoaded() {
        if (this.fontsLoaded) {
            return;
        }
        this.fontsLoaded = true;
        this.fontRenderer.loadAllFonts(Fonts.getRegistry());
    }

    private void ensureArcInitialized() {
        if (this.arcInitialized) {
            return;
        }
        this.arcInitialized = true;
        Arc2D.init();
    }

    private void ensureArcOutlineInitialized() {
        if (this.arcOutlineInitialized) {
            return;
        }
        this.arcOutlineInitialized = true;
        ArcOutline2D.init();
    }

    public void setupOverlayState() {
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glEnable((int)3042);
        GL14.glBlendFuncSeparate((int)770, (int)771, (int)1, (int)771);
    }

    public void restoreState() {
        GL11.glDepthMask((boolean)true);
        GL11.glEnable((int)2929);
    }

    public void clearDepthBuffer() {
        GL11.glClear((int)256);
    }

    public void initArc() {
        this.ensureArcInitialized();
    }

    public void initArcOutline() {
        this.ensureArcOutlineInitialized();
    }

    public RectPipeline getRectPipeline() {
        return this.rectPipeline;
    }

    public OutlinePipeline getOutlinePipeline() {
        return this.outlinePipeline;
    }

    public GlowOutlinePipeline getGlowOutlinePipeline() {
        return this.glowOutlinePipeline;
    }

    public TexturePipeline getTexturePipeline() {
        return this.texturePipeline;
    }

    public BlurPipeline getBlurPipeline() {
        return this.blurPipeline;
    }

    public KawaseBlurPipeline getKawaseBlurPipeline() {
        return this.kawaseBlurPipeline;
    }

    public GlassCompositePipeline getGlassCompositePipeline() {
        return this.glassCompositePipeline;
    }

    public GlassHandsRenderer getGlassHandsRenderer() {
        return this.glassHandsRenderer;
    }

    public FontRenderer getFontRenderer() {
        this.ensureFontsLoaded();
        return this.fontRenderer;
    }

    public MaskDiffPipeline getMaskDiffPipeline() {
        return this.maskDiffPipeline;
    }

    public Minecraft getClient() {
        return Minecraft.getInstance();
    }

    public void close() {
        this.rectPipeline.close();
        this.outlinePipeline.close();
        this.glowOutlinePipeline.close();
        this.texturePipeline.close();
        this.blurPipeline.close();
        this.kawaseBlurPipeline.close();
        this.glassCompositePipeline.close();
        this.glassHandsRenderer.close();
        this.maskDiffPipeline.close();
        this.fontRenderer.close();
        Arc2D.shutdown();
        ArcOutline2D.shutdown();
    }
}

