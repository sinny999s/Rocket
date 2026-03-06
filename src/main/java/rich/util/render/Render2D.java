/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL14
 */
package rich.util.render;

import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import rich.Initialization;
import rich.util.ColorUtil;
import rich.util.render.pipeline.Arc2D;
import rich.util.render.pipeline.ArcOutline2D;

public class Render2D {
    private static boolean inOverlayMode = false;
    private static boolean savedDepthTest = false;
    private static boolean savedDepthMask = false;
    private static boolean savedBlend = false;
    private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/menu/backmenu.png");
    private static final List<Runnable> OVERRIDE_TASKS = new ArrayList<Runnable>();
    private static final float Z_OVERRIDE = 0.0f;
    private static final float FIXED_GUI_SCALE = 2.0f;

    public static int getFixedScaledWidth() {
        Window window = Minecraft.getInstance().getWindow();
        return (int)Math.ceil((double)window.getWidth() / 2.0);
    }

    public static int getFixedScaledHeight() {
        Window window = Minecraft.getInstance().getWindow();
        return (int)Math.ceil((double)window.getHeight() / 2.0);
    }

    public static float getFixedGuiScale() {
        return 2.0f;
    }

    public static float getScaleMultiplier() {
        Minecraft client = Minecraft.getInstance();
        float currentScale = client.getWindow().getGuiScale();
        return 2.0f / currentScale;
    }

    public static void beginOverlay() {
        inOverlayMode = true;
        savedDepthTest = GL11.glIsEnabled((int)2929);
        savedDepthMask = GL11.glGetBoolean((int)2930);
        savedBlend = GL11.glIsEnabled((int)3042);
        GL11.glDisable((int)2929);
        GL11.glDepthMask((boolean)false);
        GL11.glEnable((int)3042);
        GL14.glBlendFuncSeparate((int)770, (int)771, (int)1, (int)771);
    }

    public static void endOverlay() {
        if (savedDepthMask) {
            GL11.glDepthMask((boolean)true);
        }
        if (savedDepthTest) {
            GL11.glEnable((int)2929);
        } else {
            GL11.glDisable((int)2929);
        }
        if (!savedBlend) {
            GL11.glDisable((int)3042);
        }
        inOverlayMode = false;
    }

    public static void clearDepth() {
        Minecraft client = Minecraft.getInstance();
        if (client.getMainRenderTarget() != null) {
            GL11.glClear((int)256);
        }
    }

    public static void enableBlend() {
        GL11.glEnable((int)3042);
        GL14.glBlendFuncSeparate((int)770, (int)771, (int)1, (int)771);
    }

    public static void disableBlend() {
        GL11.glDisable((int)3042);
    }

    public static void enableDepthTest() {
        GL11.glEnable((int)2929);
    }

    public static void disableDepthTest() {
        GL11.glDisable((int)2929);
    }

    public static void depthMask(boolean mask) {
        GL11.glDepthMask((boolean)mask);
    }

    public static void backgroundImage(float opacity) {
        Render2D.backgroundImage(opacity, 1.0f);
    }

    public static void backgroundImage(float opacity, float zoom) {
        int screenWidth = Render2D.getFixedScaledWidth();
        int screenHeight = Render2D.getFixedScaledHeight();
        float zoomedWidth = (float)screenWidth * zoom;
        float zoomedHeight = (float)screenHeight * zoom;
        float offsetX = ((float)screenWidth - zoomedWidth) / 2.0f;
        float offsetY = ((float)screenHeight - zoomedHeight) / 2.0f;
        int alpha = (int)(opacity * 255.0f);
        int color = alpha << 24 | 0xFFFFFF;
        Render2D.texture(BACKGROUND_TEXTURE, offsetX, offsetY, zoomedWidth, zoomedHeight, color);
    }

    public static void backgroundImage(float x, float y, float width, float height, float opacity) {
        int alpha = (int)(opacity * 255.0f);
        int color = alpha << 24 | 0xFFFFFF;
        Render2D.texture(BACKGROUND_TEXTURE, x, y, width, height, color);
    }

    public static void rect(float x, float y, float width, float height, int color) {
        int[] colors = ColorUtil.solid(color);
        float[] radii = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors, radii);
    }

    public static void rect(float x, float y, float width, float height, int color, float radius) {
        int[] colors = ColorUtil.solid(color);
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors, radii);
    }

    public static void rect(float x, float y, float width, float height, int color, float topLeft, float topRight, float bottomRight, float bottomLeft) {
        int[] colors = ColorUtil.solid(color);
        float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors, radii);
    }

    public static void gradientRect(float x, float y, float width, float height, int[] colors, float radius) {
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors, radii);
    }

    public static void gradientRect(float x, float y, float width, float height, int[] colors, float topLeft, float topRight, float bottomRight, float bottomLeft) {
        float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors, radii);
    }

    public static void gradientRect9(float x, float y, float width, float height, int topLeft, int topCenter, int topRight, int leftCenter, int center, int rightCenter, int bottomLeft, int bottomCenter, int bottomRight, float radius) {
        int[] colors = new int[]{topLeft, topCenter, topRight, leftCenter, center, rightCenter, bottomLeft, bottomCenter, bottomRight};
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors, radii);
    }

    public static void gradientRect9(float x, float y, float width, float height, int[] colors9, float radius) {
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors9, radii);
    }

    public static void gradientRect9(float x, float y, float width, float height, int[] colors9, float topLeft, float topRight, float bottomRight, float bottomLeft) {
        float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors9, radii);
    }

    public static void gradientRect9(float x, float y, float width, float height, int topLeft, int topCenter, int topRight, int leftCenter, int center, int rightCenter, int bottomLeft, int bottomCenter, int bottomRight, float radius, float innerBlur) {
        int[] colors = new int[]{topLeft, topCenter, topRight, leftCenter, center, rightCenter, bottomLeft, bottomCenter, bottomRight};
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors, radii, innerBlur);
    }

    public static void gradientRect9(float x, float y, float width, float height, int[] colors9, float radius, float innerBlur) {
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getRectPipeline().drawRect(x, y, width, height, colors9, radii, innerBlur);
    }

    public static void outline(float x, float y, float width, float height, float thickness, int color) {
        int[] colors = ColorUtil.solid8(color);
        float[] thicknesses = new float[]{thickness, thickness, thickness, thickness, thickness, thickness, thickness, thickness};
        float[] radii = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline().drawOutline(x, y, width, height, colors, thicknesses, radii, 1.0f);
    }

    public static void outline(float x, float y, float width, float height, float thickness, int color, float radius) {
        int[] colors = ColorUtil.solid8(color);
        float[] thicknesses = new float[]{thickness, thickness, thickness, thickness, thickness, thickness, thickness, thickness};
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline().drawOutline(x, y, width, height, colors, thicknesses, radii, 1.0f);
    }

    public static void outline(float x, float y, float width, float height, float thickness, int color, float topLeft, float topRight, float bottomRight, float bottomLeft) {
        int[] colors = ColorUtil.solid8(color);
        float[] thicknesses = new float[]{thickness, thickness, thickness, thickness, thickness, thickness, thickness, thickness};
        float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
        Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline().drawOutline(x, y, width, height, colors, thicknesses, radii, 1.0f);
    }

    public static void gradientOutline(float x, float y, float width, float height, float thickness, int[] colors, float radius) {
        float[] thicknesses = new float[]{thickness, thickness, thickness, thickness, thickness, thickness, thickness, thickness};
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getOutlinePipeline().drawOutline(x, y, width, height, colors, thicknesses, radii, 1.0f);
    }

    public static void blur(float x, float y, float width, float height, float blurRadius, int tintColor) {
        float[] radii = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        Initialization.getInstance().getManager().getRenderCore().getBlurPipeline().drawBlur(x, y, width, height, blurRadius, radii, tintColor);
    }

    public static void blur(float x, float y, float width, float height, float blurRadius, float cornerRadius, int tintColor) {
        float[] radii = new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius};
        Initialization.getInstance().getManager().getRenderCore().getBlurPipeline().drawBlur(x, y, width, height, blurRadius, radii, tintColor);
    }

    public static void blur(float x, float y, float width, float height, float blurRadius, float topLeft, float topRight, float bottomRight, float bottomLeft, int tintColor) {
        float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
        Initialization.getInstance().getManager().getRenderCore().getBlurPipeline().drawBlur(x, y, width, height, blurRadius, radii, tintColor);
    }

    public static void texture(Identifier id, float x, float y, float width, float height, int color) {
        Render2D.texture(id, x, y, width, height, 0.0f, 0.0f, 1.0f, 1.0f, color, 1.0f, 0.0f);
    }

    public static void texture(Identifier id, float x, float y, float width, float height, float smoothness, int color) {
        Render2D.texture(id, x, y, width, height, 0.0f, 0.0f, 1.0f, 1.0f, color, smoothness, 0.0f);
    }

    public static void texture(Identifier id, float x, float y, float width, float height, float smoothness, float radius, int color) {
        Render2D.texture(id, x, y, width, height, 0.0f, 0.0f, 1.0f, 1.0f, color, smoothness, radius);
    }

    public static void texture(Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color) {
        Render2D.texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0f, 0.0f);
    }

    public static void texture(Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float radius) {
        Render2D.texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0f, radius);
    }

    public static void texture(Identifier id, float x, float y, float width, float height, float u0, float v0, float u1, float v1, int color, float smoothness, float radius) {
        int[] colors = new int[]{color, color, color, color};
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getTexturePipeline().drawTexture(id, x, y, width, height, u0, v0, u1, v1, colors, radii, smoothness);
    }

    public static void drawTexture(GuiGraphics context, Identifier id, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, float textureWidth, float textureHeight, int color) {
        float u0 = u / textureWidth;
        float v0 = v / textureHeight;
        float u1 = (u + regionWidth) / textureWidth;
        float v1 = (v + regionHeight) / textureHeight;
        Render2D.texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0f, 0.0f);
    }

    public static void drawTexture(GuiGraphics context, Identifier id, float x, float y, float width, float height, float u, float v, float regionWidth, float regionHeight, float textureWidth, float textureHeight, int color, float radius) {
        float u0 = u / textureWidth;
        float v0 = v / textureHeight;
        float u1 = (u + regionWidth) / textureWidth;
        float v1 = (v + regionHeight) / textureHeight;
        Render2D.texture(id, x, y, width, height, u0, v0, u1, v1, color, 1.0f, radius);
    }

    public static void drawSprite(TextureAtlasSprite sprite, float x, float y, float width, float height, int color) {
        Render2D.drawSprite(sprite, x, y, width, height, color, true);
    }

    public static void drawSprite(TextureAtlasSprite sprite, float x, float y, float width, float height, int color, boolean pixelPerfect) {
        if (sprite == null || width == 0.0f || height == 0.0f) {
            return;
        }
        float smoothness = pixelPerfect ? 1.0f : 0.0f;
        Render2D.texture(sprite.atlasLocation(), x, y, width, height, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), color, smoothness, 0.0f);
    }

    public static void drawSpriteSmooth(TextureAtlasSprite sprite, float x, float y, float width, float height, int color) {
        Render2D.drawSprite(sprite, x, y, width, height, color, false);
    }

    public static void drawFramebufferTexture(int textureId, float x, float y, float width, float height, float r, float g, float b, float a) {
        int color = (int)(a * 255.0f) << 24 | (int)(r * 255.0f) << 16 | (int)(g * 255.0f) << 8 | (int)(b * 255.0f);
        int[] colors = new int[]{color, color, color, color};
        float[] radii = new float[]{0.0f, 0.0f, 0.0f, 0.0f};
        Initialization.getInstance().getManager().getRenderCore().getTexturePipeline().drawFramebufferTexture(textureId, x, y, width, height, colors, radii, a);
    }

    public static void glowOutline(float x, float y, float width, float height, float thickness, int color, float radius, float progress, float baseAlpha) {
        float[] radii = new float[]{radius, radius, radius, radius};
        Initialization.getInstance().getManager().getRenderCore().getGlowOutlinePipeline().drawGlowOutline(x, y, width, height, color, thickness, radii, progress, baseAlpha);
    }

    public static void glowOutline(float x, float y, float width, float height, float thickness, int color, float topLeft, float topRight, float bottomRight, float bottomLeft, float progress, float baseAlpha) {
        float[] radii = new float[]{topLeft, topRight, bottomRight, bottomLeft};
        Initialization.getInstance().getManager().getRenderCore().getGlowOutlinePipeline().drawGlowOutline(x, y, width, height, color, thickness, radii, progress, baseAlpha);
    }

    public static Matrix4f createProjection() {
        int width = Render2D.getFixedScaledWidth();
        int height = Render2D.getFixedScaledHeight();
        return new Matrix4f().ortho(0.0f, (float)width, (float)height, 0.0f, -1000.0f, 1000.0f);
    }

    public static void arc(GuiGraphics context, float x, float y, float size, float thickness, float degree, float rotation, int color, boolean overrideContext) {
        Render2D.arc(Render2D.createProjection(), x, y, size, thickness, degree, rotation, color, overrideContext);
    }

    public static void arc(GuiGraphics context, float x, float y, float size, float thickness, float degree, float rotation, boolean overrideContext, int ... colors) {
        Render2D.arc(Render2D.createProjection(), x, y, size, thickness, degree, rotation, overrideContext, colors);
    }

    public static void arc(Matrix4f matrix, float x, float y, float size, float thickness, float degree, float rotation, int color, boolean overrideContext) {
        if (overrideContext) {
            OVERRIDE_TASKS.add(() -> Arc2D.draw(matrix, x, y, size, thickness, degree, rotation, 0.0f, color));
            return;
        }
        Arc2D.draw(matrix, x, y, size, thickness, degree, rotation, 0.0f, color);
    }

    public static void arc(Matrix4f matrix, float x, float y, float size, float thickness, float degree, float rotation, boolean overrideContext, int ... colors) {
        if (overrideContext) {
            OVERRIDE_TASKS.add(() -> Arc2D.draw(matrix, x, y, size, thickness, degree, rotation, 0.0f, colors));
            return;
        }
        Arc2D.draw(matrix, x, y, size, thickness, degree, rotation, 0.0f, colors);
    }

    public static void arc(float x, float y, float size, float thickness, float degree, float rotation, int color) {
        Arc2D.draw(Render2D.createProjection(), x, y, size, thickness, degree, rotation, 0.0f, color);
    }

    public static void arc(float x, float y, float size, float thickness, float degree, float rotation, int ... colors) {
        Arc2D.draw(Render2D.createProjection(), x, y, size, thickness, degree, rotation, 0.0f, colors);
    }

    public static void arcOutline(float x, float y, float size, float arcThickness, float degree, float rotation, float outlineThickness, int fillColor, int outlineColor) {
        ArcOutline2D.draw(Render2D.createProjection(), x, y, size, arcThickness, degree, rotation, outlineThickness, fillColor, outlineColor, 0.0f);
    }

    public static void arcOutline(GuiGraphics context, float x, float y, float size, float arcThickness, float degree, float rotation, float outlineThickness, int fillColor, int outlineColor, boolean overrideContext) {
        Matrix4f matrix = Render2D.createProjection();
        if (overrideContext) {
            OVERRIDE_TASKS.add(() -> ArcOutline2D.draw(matrix, x, y, size, arcThickness, degree, rotation, outlineThickness, fillColor, outlineColor, 0.0f));
            return;
        }
        ArcOutline2D.draw(matrix, x, y, size, arcThickness, degree, rotation, outlineThickness, fillColor, outlineColor, 0.0f);
    }

    public static void arcOutline(Matrix4f matrix, float x, float y, float size, float arcThickness, float degree, float rotation, float outlineThickness, int fillColor, int outlineColor) {
        ArcOutline2D.draw(matrix, x, y, size, arcThickness, degree, rotation, outlineThickness, fillColor, outlineColor, 0.0f);
    }

    public static void flushOverrideTasks() {
        for (Runnable task : OVERRIDE_TASKS) {
            task.run();
        }
        OVERRIDE_TASKS.clear();
    }

    public static boolean isInOverlayMode() {
        return inOverlayMode;
    }

    public static void cleanup() {
        OVERRIDE_TASKS.clear();
        Arc2D.shutdown();
        ArcOutline2D.shutdown();
    }
}

