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
package rich.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public class RectPipeline {
    private static final Identifier PIPELINE_ID = Identifier.fromNamespaceAndPath((String)"rich", (String)"pipeline/rect");
    private static final Identifier VERTEX_SHADER = Identifier.fromNamespaceAndPath((String)"rich", (String)"core/rect");
    private static final Identifier FRAGMENT_SHADER = Identifier.fromNamespaceAndPath((String)"rich", (String)"core/rect");
    private static final Vector3f MODEL_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final float FIXED_GUI_SCALE = 2.0f;
    private static final RenderPipeline PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation(PIPELINE_ID).withVertexShader(VERTEX_SHADER).withFragmentShader(FRAGMENT_SHADER).withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES).withUniform("RectData", UniformType.UNIFORM_BUFFER).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final int BUFFER_SIZE = 256;
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private boolean initialized = false;

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)256);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.initialized = true;
    }

    public void drawRect(float x, float y, float width, float height, int[] colors, float[] radii) {
        this.drawRect(x, y, width, height, colors, radii, 0.0f);
    }

    public void drawRect(float x, float y, float width, float height, int[] colors, float[] radii, float innerBlur) {
        Minecraft client = Minecraft.getInstance();
        if (client.getMainRenderTarget() == null) {
            return;
        }
        this.ensureInitialized();
        int framebufferWidth = client.getWindow().getWidth();
        int framebufferHeight = client.getWindow().getHeight();
        float fixedScreenWidth = (float)framebufferWidth / 2.0f;
        float fixedScreenHeight = (float)framebufferHeight / 2.0f;
        int[] colors9 = this.convertTo9Colors(colors);
        this.prepareUniformData(x, y, width, height, fixedScreenWidth, fixedScreenHeight, 2.0f, innerBlur, colors9, radii);
        this.uploadAndDraw(client);
    }

    private int[] convertTo9Colors(int[] colors) {
        int[] result = new int[9];
        if (colors.length == 1) {
            for (int i = 0; i < 9; ++i) {
                result[i] = colors[0];
            }
        } else if (colors.length == 4) {
            result[0] = colors[0];
            result[1] = this.blendColors(colors[0], colors[1]);
            result[2] = colors[1];
            result[3] = this.blendColors(colors[0], colors[3]);
            result[4] = this.blendColors(colors[0], colors[1], colors[2], colors[3]);
            result[5] = this.blendColors(colors[1], colors[2]);
            result[6] = colors[3];
            result[7] = this.blendColors(colors[3], colors[2]);
            result[8] = colors[2];
        } else if (colors.length >= 9) {
            System.arraycopy(colors, 0, result, 0, 9);
        } else {
            for (int i = 0; i < 9; ++i) {
                result[i] = colors[i % colors.length];
            }
        }
        return result;
    }

    private int blendColors(int ... colors) {
        int r = 0;
        int g = 0;
        int b = 0;
        int a = 0;
        for (int color : colors) {
            a += color >> 24 & 0xFF;
            r += color >> 16 & 0xFF;
            g += color >> 8 & 0xFF;
            b += color & 0xFF;
        }
        int count = colors.length;
        return a / count << 24 | r / count << 16 | g / count << 8 | b / count;
    }

    private void prepareUniformData(float x, float y, float width, float height, float screenWidth, float screenHeight, float guiScale, float innerBlur, int[] colors, float[] radii) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(x);
        this.dataBuffer.putFloat(y);
        this.dataBuffer.putFloat(width);
        this.dataBuffer.putFloat(height);
        this.dataBuffer.putFloat(screenWidth);
        this.dataBuffer.putFloat(screenHeight);
        this.dataBuffer.putFloat(guiScale);
        this.dataBuffer.putFloat(innerBlur);
        this.dataBuffer.putFloat(radii[0]);
        this.dataBuffer.putFloat(radii[1]);
        this.dataBuffer.putFloat(radii[2]);
        this.dataBuffer.putFloat(radii[3]);
        for (int i = 0; i < 9; ++i) {
            int color = colors[i];
            float a = (float)(color >> 24 & 0xFF) / 255.0f;
            float r = (float)(color >> 16 & 0xFF) / 255.0f;
            float g = (float)(color >> 8 & 0xFF) / 255.0f;
            float bl = (float)(color & 0xFF) / 255.0f;
            this.dataBuffer.putFloat(r);
            this.dataBuffer.putFloat(g);
            this.dataBuffer.putFloat(bl);
            this.dataBuffer.putFloat(a);
        }
        this.dataBuffer.flip();
    }

    private void uploadAndDraw(Minecraft client) {
        int size = this.dataBuffer.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:rect_uniform", 136, size);
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:rect_pass", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("RectData", this.uniformBuffer);
            renderPass.draw(0, 6);
        }
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
}

