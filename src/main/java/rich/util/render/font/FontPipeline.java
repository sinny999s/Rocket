/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.lwjgl.system.MemoryUtil
 */
package rich.util.render.font;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;
import rich.util.render.font.FontAtlas;
import rich.util.render.font.Glyph;

public class FontPipeline {
    private static final Identifier PIPELINE_ID = Identifier.fromNamespaceAndPath((String)"rich", (String)"pipeline/msdf");
    private static final Identifier SHADER_ID = Identifier.fromNamespaceAndPath((String)"rich", (String)"core/msdf");
    private static final float FIXED_GUI_SCALE = 2.0f;
    private static final RenderPipeline PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation(PIPELINE_ID).withVertexShader(SHADER_ID).withFragmentShader(SHADER_ID).withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES).withUniform("FontData", UniformType.UNIFORM_BUFFER).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static int[] LEGACY_COLORS = null;
    private static final int MAX_CHARS = 256;
    private static final int BUFFER_SIZE = 16448;
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private boolean initialized = false;
    private final List<CharData> charBatch = new ArrayList<CharData>();
    private FontAtlas currentAtlas = null;
    private float currentOutlineWidth = 0.0f;
    private int currentOutlineColor = 0;

    private static int[] getLegacyColors() {
        if (LEGACY_COLORS == null) {
            LEGACY_COLORS = new int[32];
            for (int i = 0; i < 16; ++i) {
                int j = (i >> 3 & 1) * 85;
                int r = (i >> 2 & 1) * 170 + j;
                int g = (i >> 1 & 1) * 170 + j;
                int b = (i & 1) * 170 + j;
                if (i == 6) {
                    r += 85;
                }
                FontPipeline.LEGACY_COLORS[i] = 0xFF000000 | r << 16 | g << 8 | b;
                FontPipeline.LEGACY_COLORS[i + 16] = (r & 0xFCFCFC) >> 2 << 24 | r << 16 | g << 8 | b;
            }
        }
        return LEGACY_COLORS;
    }

    private int getFixedScaledWidth() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return 960;
        }
        return (int)Math.ceil((double)client.getWindow().getWidth() / 2.0);
    }

    private int getFixedScaledHeight() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return 540;
        }
        return (int)Math.ceil((double)client.getWindow().getHeight() / 2.0);
    }

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)16448);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "rich:font_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.initialized = true;
    }

    public void drawText(FontAtlas atlas, String text, float x, float y, float size, int color) {
        this.drawText(atlas, text, x, y, size, color, 0.0f, 0, 0.0f);
    }

    public void drawText(FontAtlas atlas, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor, float rotation) {
        Minecraft client = Minecraft.getInstance();
        if (client.getMainRenderTarget() == null) {
            return;
        }
        if (text == null || text.isEmpty()) {
            return;
        }
        atlas.ensureLoaded();
        if (atlas.getGlyphCount() == 0) {
            return;
        }
        this.ensureInitialized();
        if (this.currentAtlas != null && (this.currentAtlas != atlas || this.currentOutlineWidth != outlineWidth || this.currentOutlineColor != outlineColor)) {
            this.flush();
        }
        this.currentAtlas = atlas;
        this.currentOutlineWidth = outlineWidth;
        this.currentOutlineColor = outlineColor;
        float scale = size / atlas.getFontSize();
        float cursorX = x;
        float cursorY = y;
        float textWidth = this.getTextWidth(atlas, text, size);
        float textHeight = this.getTextHeight(atlas, text, size);
        float pivotX = x + textWidth / 2.0f;
        float pivotY = y + textHeight / 2.0f;
        float rotationRad = (float)Math.toRadians(rotation);
        int currentColor = color;
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            if ((codePoint == 167 || codePoint == 38) && i + charCount < text.length()) {
                int code;
                int nextCodePoint = text.codePointAt(i + charCount);
                if (nextCodePoint == 35 && i + charCount + 6 < text.length()) {
                    try {
                        String hex = text.substring(i + charCount + 1, i + charCount + 7);
                        currentColor = 0xFF000000 | Integer.parseInt(hex, 16);
                        i += charCount + 7;
                        continue;
                    }
                    catch (Exception hex) {
                        // empty catch block
                    }
                }
                if ((code = "0123456789abcdefklmnor".indexOf(Character.toLowerCase((char)nextCodePoint))) >= 0) {
                    if (code < 16) {
                        currentColor = FontPipeline.getLegacyColors()[code];
                    } else if (code == 21) {
                        currentColor = color;
                    }
                    i += charCount + Character.charCount(nextCodePoint);
                    continue;
                }
            }
            if (codePoint == 10) {
                cursorX = x;
                cursorY += atlas.getLineHeight() * scale;
                i += charCount;
                continue;
            }
            Glyph glyph = atlas.getGlyph(codePoint);
            if (glyph == null) {
                Glyph fallback = atlas.getGlyph(63);
                cursorX = fallback != null ? (cursorX += fallback.xAdvance * scale) : (cursorX += size * 0.5f);
                i += charCount;
                continue;
            }
            float glyphX = cursorX + glyph.xOffset * scale;
            float glyphY = cursorY + glyph.yOffset * scale;
            float glyphW = glyph.width * scale;
            float glyphH = glyph.height * scale;
            if (glyph.width > 0.0f && glyph.height > 0.0f) {
                this.charBatch.add(new CharData(glyphX, glyphY, glyphW, glyphH, glyph.u0, glyph.v0, glyph.u1, glyph.v1, currentColor, rotationRad, pivotX, pivotY, scale));
            }
            cursorX += glyph.xAdvance * scale;
            if (this.charBatch.size() >= 256) {
                this.flush();
            }
            i += charCount;
        }
        if (!this.charBatch.isEmpty() && this.currentAtlas != null) {
            this.flush();
        }
    }

    public void drawTextRotatedAroundPoint(FontAtlas atlas, String text, float x, float y, float size, int color, float outlineWidth, int outlineColor, float rotation, float pivotX, float pivotY) {
        Minecraft client = Minecraft.getInstance();
        if (client.getMainRenderTarget() == null) {
            return;
        }
        if (text == null || text.isEmpty()) {
            return;
        }
        atlas.ensureLoaded();
        if (atlas.getGlyphCount() == 0) {
            return;
        }
        this.ensureInitialized();
        if (this.currentAtlas != null && (this.currentAtlas != atlas || this.currentOutlineWidth != outlineWidth || this.currentOutlineColor != outlineColor)) {
            this.flush();
        }
        this.currentAtlas = atlas;
        this.currentOutlineWidth = outlineWidth;
        this.currentOutlineColor = outlineColor;
        float scale = size / atlas.getFontSize();
        float cursorX = x;
        float cursorY = y;
        float rotationRad = (float)Math.toRadians(rotation);
        int currentColor = color;
        int i = 0;
        while (i < text.length()) {
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            if ((codePoint == 167 || codePoint == 38) && i + charCount < text.length()) {
                int code;
                int nextCodePoint = text.codePointAt(i + charCount);
                if (nextCodePoint == 35 && i + charCount + 6 < text.length()) {
                    try {
                        String hex = text.substring(i + charCount + 1, i + charCount + 7);
                        currentColor = 0xFF000000 | Integer.parseInt(hex, 16);
                        i += charCount + 7;
                        continue;
                    }
                    catch (Exception hex) {
                        // empty catch block
                    }
                }
                if ((code = "0123456789abcdefklmnor".indexOf(Character.toLowerCase((char)nextCodePoint))) >= 0) {
                    if (code < 16) {
                        currentColor = FontPipeline.getLegacyColors()[code];
                    } else if (code == 21) {
                        currentColor = color;
                    }
                    i += charCount + Character.charCount(nextCodePoint);
                    continue;
                }
            }
            if (codePoint == 10) {
                cursorX = x;
                cursorY += atlas.getLineHeight() * scale;
                i += charCount;
                continue;
            }
            Glyph glyph = atlas.getGlyph(codePoint);
            if (glyph == null) {
                Glyph fallback = atlas.getGlyph(63);
                cursorX = fallback != null ? (cursorX += fallback.xAdvance * scale) : (cursorX += size * 0.5f);
                i += charCount;
                continue;
            }
            float glyphX = cursorX + glyph.xOffset * scale;
            float glyphY = cursorY + glyph.yOffset * scale;
            float glyphW = glyph.width * scale;
            float glyphH = glyph.height * scale;
            if (glyph.width > 0.0f && glyph.height > 0.0f) {
                this.charBatch.add(new CharData(glyphX, glyphY, glyphW, glyphH, glyph.u0, glyph.v0, glyph.u1, glyph.v1, currentColor, rotationRad, pivotX, pivotY, scale));
            }
            cursorX += glyph.xAdvance * scale;
            if (this.charBatch.size() >= 256) {
                this.flush();
            }
            i += charCount;
        }
        if (!this.charBatch.isEmpty() && this.currentAtlas != null) {
            this.flush();
        }
    }

    public void flush() {
        GpuTexture gpuTexture;
        if (this.charBatch.isEmpty() || this.currentAtlas == null) {
            this.charBatch.clear();
            this.currentAtlas = null;
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.getMainRenderTarget() == null) {
            this.charBatch.clear();
            this.currentAtlas = null;
            return;
        }
        AbstractTexture texture = client.getTextureManager().getTexture(this.currentAtlas.getTextureId());
        if (texture == null) {
            this.charBatch.clear();
            this.currentAtlas = null;
            return;
        }
        try {
            gpuTexture = texture.getTexture();
        }
        catch (Exception e) {
            this.charBatch.clear();
            this.currentAtlas = null;
            return;
        }
        this.prepareUniformData(client, this.currentAtlas, this.currentOutlineWidth, this.currentOutlineColor);
        int size = this.dataBuffer.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "rich:font_uniform", 136, size);
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        GpuTextureView textureView = RenderSystem.getDevice().createTextureView(gpuTexture);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "rich:font_pass", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("Sampler0", textureView, sampler);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("FontData", this.uniformBuffer);
            renderPass.draw(0, this.charBatch.size() * 6);
        }
        textureView.close();
        this.charBatch.clear();
        this.currentAtlas = null;
    }

    private void prepareUniformData(Minecraft client, FontAtlas atlas, float outlineWidth, int outlineColor) {
        this.dataBuffer.clear();
        float screenWidth = this.getFixedScaledWidth();
        float screenHeight = this.getFixedScaledHeight();
        float guiScale = 2.0f;
        this.dataBuffer.putFloat(screenWidth);
        this.dataBuffer.putFloat(screenHeight);
        this.dataBuffer.putFloat(guiScale);
        this.dataBuffer.putFloat(outlineWidth);
        float oa = (float)(outlineColor >> 24 & 0xFF) / 255.0f;
        float or = (float)(outlineColor >> 16 & 0xFF) / 255.0f;
        float og = (float)(outlineColor >> 8 & 0xFF) / 255.0f;
        float ob = (float)(outlineColor & 0xFF) / 255.0f;
        this.dataBuffer.putFloat(or);
        this.dataBuffer.putFloat(og);
        this.dataBuffer.putFloat(ob);
        this.dataBuffer.putFloat(oa);
        this.dataBuffer.putFloat(atlas.getAtlasWidth());
        this.dataBuffer.putFloat(atlas.getAtlasHeight());
        this.dataBuffer.putFloat(atlas.getDistanceRange());
        this.dataBuffer.putFloat(atlas.getFontSize());
        this.dataBuffer.putInt(this.charBatch.size());
        this.dataBuffer.putInt(0);
        this.dataBuffer.putInt(0);
        this.dataBuffer.putInt(0);
        for (CharData cd : this.charBatch) {
            this.dataBuffer.putFloat(cd.x);
            this.dataBuffer.putFloat(cd.y);
            this.dataBuffer.putFloat(cd.width);
            this.dataBuffer.putFloat(cd.height);
            this.dataBuffer.putFloat(cd.u0);
            this.dataBuffer.putFloat(cd.v0);
            this.dataBuffer.putFloat(cd.u1);
            this.dataBuffer.putFloat(cd.v1);
            float a = (float)(cd.color >> 24 & 0xFF) / 255.0f;
            float r = (float)(cd.color >> 16 & 0xFF) / 255.0f;
            float g = (float)(cd.color >> 8 & 0xFF) / 255.0f;
            float b = (float)(cd.color & 0xFF) / 255.0f;
            this.dataBuffer.putFloat(r);
            this.dataBuffer.putFloat(g);
            this.dataBuffer.putFloat(b);
            this.dataBuffer.putFloat(a);
            this.dataBuffer.putFloat(cd.rotation);
            this.dataBuffer.putFloat(cd.pivotX);
            this.dataBuffer.putFloat(cd.pivotY);
            this.dataBuffer.putFloat(cd.glyphScale);
        }
        this.dataBuffer.flip();
    }

    public float getTextWidth(FontAtlas atlas, String text, float size) {
        atlas.ensureLoaded();
        float scale = size / atlas.getFontSize();
        float width = 0.0f;
        float maxWidth = 0.0f;
        int i = 0;
        while (i < text.length()) {
            Glyph fallback;
            int codePoint = text.codePointAt(i);
            int charCount = Character.charCount(codePoint);
            if ((codePoint == 167 || codePoint == 38) && i + charCount < text.length()) {
                int nextCodePoint = text.codePointAt(i + charCount);
                if (nextCodePoint == 35 && i + charCount + 6 < text.length()) {
                    i += charCount + 7;
                    continue;
                }
                int code = "0123456789abcdefklmnor".indexOf(Character.toLowerCase((char)nextCodePoint));
                if (code >= 0) {
                    i += charCount + Character.charCount(nextCodePoint);
                    continue;
                }
            }
            if (codePoint == 10) {
                maxWidth = Math.max(maxWidth, width);
                width = 0.0f;
                i += charCount;
                continue;
            }
            Glyph glyph = atlas.getGlyph(codePoint);
            width = glyph != null ? (width += glyph.xAdvance * scale) : ((fallback = atlas.getGlyph(63)) != null ? (width += fallback.xAdvance * scale) : (width += size * 0.5f));
            i += charCount;
        }
        return Math.max(maxWidth, width);
    }

    public float getTextHeight(FontAtlas atlas, String text, float size) {
        atlas.ensureLoaded();
        float scale = size / atlas.getFontSize();
        int lines = 1;
        for (int i = 0; i < text.length(); ++i) {
            if (text.charAt(i) != '\n') continue;
            ++lines;
        }
        return (float)lines * atlas.getLineHeight() * scale;
    }

    public void close() {
        if (this.uniformBuffer != null) {
            this.uniformBuffer.close();
            this.uniformBuffer = null;
        }
        if (this.dummyVertexBuffer != null) {
            this.dummyVertexBuffer.close();
            this.dummyVertexBuffer = null;
        }
        if (this.dataBuffer != null) {
            MemoryUtil.memFree((Buffer)this.dataBuffer);
            this.dataBuffer = null;
        }
        this.initialized = false;
    }

    private static class CharData {
        float x;
        float y;
        float width;
        float height;
        float u0;
        float v0;
        float u1;
        float v1;
        int color;
        float rotation;
        float pivotX;
        float pivotY;
        float glyphScale;

        CharData(float x, float y, float w, float h, float u0, float v0, float u1, float v1, int color, float rotation, float pivotX, float pivotY, float glyphScale) {
            this.x = x;
            this.y = y;
            this.width = w;
            this.height = h;
            this.u0 = u0;
            this.v0 = v0;
            this.u1 = u1;
            this.v1 = v1;
            this.color = color;
            this.rotation = rotation;
            this.pivotX = pivotX;
            this.pivotY = pivotY;
            this.glyphScale = glyphScale;
        }
    }
}

