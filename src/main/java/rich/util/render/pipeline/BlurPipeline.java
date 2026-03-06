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
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
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

public class BlurPipeline {
    private static final Identifier PIPELINE_ID = Identifier.fromNamespaceAndPath((String)"rich", (String)"pipeline/blur");
    private static final Identifier VERTEX_SHADER = Identifier.fromNamespaceAndPath((String)"rich", (String)"core/blur");
    private static final Identifier FRAGMENT_SHADER = Identifier.fromNamespaceAndPath((String)"rich", (String)"core/blur");
    private static final float FIXED_GUI_SCALE = 2.0f;
    private static final RenderPipeline PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation(PIPELINE_ID).withVertexShader(VERTEX_SHADER).withFragmentShader(FRAGMENT_SHADER).withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES).withUniform("BlurData", UniformType.UNIFORM_BUFFER).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final int BUFFER_SIZE = 128;
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private GpuTexture copyTexture;
    private GpuTextureView copyTextureView;
    private int lastWidth = 0;
    private int lastHeight = 0;
    private boolean initialized = false;

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
        this.dataBuffer = MemoryUtil.memAlloc((int)128);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:blur_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.initialized = true;
    }

    private void ensureCopyTexture(int width, int height) {
        if (this.copyTexture == null || this.lastWidth != width || this.lastHeight != height) {
            if (this.copyTextureView != null) {
                this.copyTextureView.close();
                this.copyTextureView = null;
            }
            if (this.copyTexture != null) {
                this.copyTexture.close();
                this.copyTexture = null;
            }
            this.copyTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:blur_copy", 5, TextureFormat.RGBA8, width, height, 1, 1);
            this.copyTextureView = RenderSystem.getDevice().createTextureView(this.copyTexture);
            this.lastWidth = width;
            this.lastHeight = height;
        }
    }

    public void drawBlur(float x, float y, float width, float height, float radius, float[] radii, int color) {
        Minecraft client = Minecraft.getInstance();
        if (client.getMainRenderTarget() == null) {
            return;
        }
        if (client.getMainRenderTarget().getColorTexture() == null) {
            return;
        }
        this.ensureInitialized();
        int fbWidth = client.getMainRenderTarget().width;
        int fbHeight = client.getMainRenderTarget().height;
        this.ensureCopyTexture(fbWidth, fbHeight);
        int fixedScreenWidth = this.getFixedScaledWidth();
        int fixedScreenHeight = this.getFixedScaledHeight();
        this.prepareUniformData(x, y, width, height, fixedScreenWidth, fixedScreenHeight, fbWidth, fbHeight, 2.0f, radius, radii, color);
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.copyTextureToTexture(client.getMainRenderTarget().getColorTexture(), this.copyTexture, 0, 0, 0, 0, 0, fbWidth, fbHeight);
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:blur_pass", client.getMainRenderTarget().getColorTextureView(), OptionalInt.empty(), client.getMainRenderTarget().getDepthTextureView(), OptionalDouble.empty());){
            renderPass.setPipeline(PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("Sampler0", this.copyTextureView, sampler);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("BlurData", this.uniformBuffer);
            renderPass.draw(0, 6);
        }
    }

    private void prepareUniformData(float x, float y, float width, float height, float screenWidth, float screenHeight, int fbWidth, int fbHeight, float guiScale, float blurRadius, float[] radii, int color) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(x);
        this.dataBuffer.putFloat(y);
        this.dataBuffer.putFloat(width);
        this.dataBuffer.putFloat(height);
        this.dataBuffer.putFloat(screenWidth);
        this.dataBuffer.putFloat(screenHeight);
        this.dataBuffer.putFloat(guiScale);
        this.dataBuffer.putFloat(blurRadius);
        this.dataBuffer.putFloat(fbWidth);
        this.dataBuffer.putFloat(fbHeight);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.putFloat(radii[0]);
        this.dataBuffer.putFloat(radii[1]);
        this.dataBuffer.putFloat(radii[2]);
        this.dataBuffer.putFloat(radii[3]);
        float a = (float)(color >> 24 & 0xFF) / 255.0f;
        float r = (float)(color >> 16 & 0xFF) / 255.0f;
        float g = (float)(color >> 8 & 0xFF) / 255.0f;
        float b = (float)(color & 0xFF) / 255.0f;
        this.dataBuffer.putFloat(r);
        this.dataBuffer.putFloat(g);
        this.dataBuffer.putFloat(b);
        this.dataBuffer.putFloat(a);
        this.dataBuffer.flip();
        int size = this.dataBuffer.remaining();
        if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
            if (this.uniformBuffer != null) {
                this.uniformBuffer.close();
            }
            this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:blur_uniform", 136, size);
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
        if (this.copyTextureView != null) {
            this.copyTextureView.close();
            this.copyTextureView = null;
        }
        if (this.copyTexture != null) {
            this.copyTexture.close();
            this.copyTexture = null;
        }
        this.lastWidth = 0;
        this.lastHeight = 0;
        this.initialized = false;
    }
}

