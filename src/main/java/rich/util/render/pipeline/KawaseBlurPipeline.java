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
import java.util.OptionalInt;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryUtil;

public class KawaseBlurPipeline {
    private static final Identifier DOWN_PIPELINE_ID = Identifier.fromNamespaceAndPath((String)"rich", (String)"pipeline/kawase_down");
    private static final Identifier DOWN_VERTEX_SHADER = Identifier.fromNamespaceAndPath((String)"rich", (String)"core/kawase_down");
    private static final Identifier DOWN_FRAGMENT_SHADER = Identifier.fromNamespaceAndPath((String)"rich", (String)"core/kawase_down");
    private static final Identifier UP_PIPELINE_ID = Identifier.fromNamespaceAndPath((String)"rich", (String)"pipeline/kawase_up");
    private static final Identifier UP_VERTEX_SHADER = Identifier.fromNamespaceAndPath((String)"rich", (String)"core/kawase_up");
    private static final Identifier UP_FRAGMENT_SHADER = Identifier.fromNamespaceAndPath((String)"rich", (String)"core/kawase_up");
    private static final RenderPipeline DOWN_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation(DOWN_PIPELINE_ID).withVertexShader(DOWN_VERTEX_SHADER).withFragmentShader(DOWN_FRAGMENT_SHADER).withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES).withUniform("KawaseData", UniformType.UNIFORM_BUFFER).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final RenderPipeline UP_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation(UP_PIPELINE_ID).withVertexShader(UP_VERTEX_SHADER).withFragmentShader(UP_FRAGMENT_SHADER).withVertexFormat(DefaultVertexFormat.EMPTY, VertexFormat.Mode.TRIANGLES).withUniform("KawaseData", UniformType.UNIFORM_BUFFER).withSampler("Sampler0").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withDepthWrite(false).withCull(false).build());
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
    private static final Vector3f MODEL_OFFSET = new Vector3f(0.0f, 0.0f, 0.0f);
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final int MAX_ITERATIONS = 8;
    private static final int BUFFER_SIZE = 32;
    private GpuBuffer uniformBuffer;
    private GpuBuffer dummyVertexBuffer;
    private ByteBuffer dataBuffer;
    private GpuTexture[] downTextures;
    private GpuTextureView[] downTextureViews;
    private GpuTexture[] upTextures;
    private GpuTextureView[] upTextureViews;
    private int[] downWidths;
    private int[] downHeights;
    private int[] upWidths;
    private int[] upHeights;
    private GpuTexture finalTexture;
    private GpuTextureView finalTextureView;
    private int lastWidth = 0;
    private int lastHeight = 0;
    private boolean initialized = false;

    private void ensureInitialized() {
        if (this.initialized) {
            return;
        }
        this.dataBuffer = MemoryUtil.memAlloc((int)32);
        ByteBuffer dummyData = MemoryUtil.memAlloc((int)4);
        dummyData.putInt(0);
        dummyData.flip();
        this.dummyVertexBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:kawase_dummy_vertex", 32, dummyData);
        MemoryUtil.memFree((Buffer)dummyData);
        this.downTextures = new GpuTexture[8];
        this.downTextureViews = new GpuTextureView[8];
        this.upTextures = new GpuTexture[8];
        this.upTextureViews = new GpuTextureView[8];
        this.downWidths = new int[8];
        this.downHeights = new int[8];
        this.upWidths = new int[8];
        this.upHeights = new int[8];
        this.initialized = true;
    }

    private void ensureFramebuffers(int width, int height) {
        if (width == this.lastWidth && height == this.lastHeight) {
            return;
        }
        this.cleanupFramebuffers();
        this.finalTexture = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_final", 13, TextureFormat.RGBA8, width, height, 1, 1);
        this.finalTextureView = RenderSystem.getDevice().createTextureView(this.finalTexture);
        int w = width;
        int h = height;
        for (int i = 0; i < 8; ++i) {
            w = Math.max(1, w / 2);
            h = Math.max(1, h / 2);
            int index = i;
            int fw = w;
            int fh = h;
            this.downWidths[i] = fw;
            this.downHeights[i] = fh;
            this.upWidths[i] = fw;
            this.upHeights[i] = fh;
            this.downTextures[i] = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_down_" + index, 13, TextureFormat.RGBA8, fw, fh, 1, 1);
            this.downTextureViews[i] = RenderSystem.getDevice().createTextureView(this.downTextures[i]);
            this.upTextures[i] = RenderSystem.getDevice().createTexture(() -> "minecraft:kawase_up_" + index, 13, TextureFormat.RGBA8, fw, fh, 1, 1);
            this.upTextureViews[i] = RenderSystem.getDevice().createTextureView(this.upTextures[i]);
        }
        this.lastWidth = width;
        this.lastHeight = height;
    }

    private void cleanupFramebuffers() {
        if (this.finalTextureView != null) {
            this.finalTextureView.close();
            this.finalTextureView = null;
        }
        if (this.finalTexture != null) {
            this.finalTexture.close();
            this.finalTexture = null;
        }
        if (this.downTextureViews != null) {
            for (int i = 0; i < 8; ++i) {
                if (this.downTextureViews[i] != null) {
                    this.downTextureViews[i].close();
                    this.downTextureViews[i] = null;
                }
                if (this.downTextures[i] != null) {
                    this.downTextures[i].close();
                    this.downTextures[i] = null;
                }
                if (this.upTextureViews[i] != null) {
                    this.upTextureViews[i].close();
                    this.upTextureViews[i] = null;
                }
                if (this.upTextures[i] == null) continue;
                this.upTextures[i].close();
                this.upTextures[i] = null;
            }
        }
    }

    public GpuTextureView blur(GpuTexture sourceTexture, GpuTextureView sourceView, int width, int height, int iterations, float offset) {
        int i;
        if (sourceTexture == null || sourceView == null) {
            return null;
        }
        this.ensureInitialized();
        this.ensureFramebuffers(width, height);
        iterations = Math.min(iterations, 8);
        if (iterations < 1) {
            iterations = 1;
        }
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
        GpuTextureView currentSource = sourceView;
        int currentWidth = width;
        int currentHeight = height;
        for (i = 0; i < iterations; ++i) {
            int targetWidth = this.downWidths[i];
            int targetHeight = this.downHeights[i];
            this.prepareUniformData(currentWidth, currentHeight, offset);
            int size = this.dataBuffer.remaining();
            if (this.uniformBuffer == null || this.uniformBuffer.size() < (long)size) {
                if (this.uniformBuffer != null) {
                    this.uniformBuffer.close();
                }
                this.uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "minecraft:kawase_uniform", 136, size);
            }
            encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
            GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
            int finalI = i;
            try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:kawase_down_pass_" + finalI, this.downTextureViews[i], OptionalInt.empty());){
                renderPass.setPipeline(DOWN_PIPELINE);
                renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
                renderPass.bindTexture("Sampler0", currentSource, sampler);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                renderPass.setUniform("KawaseData", this.uniformBuffer);
                renderPass.draw(0, 6);
            }
            currentSource = this.downTextureViews[i];
            currentWidth = targetWidth;
            currentHeight = targetHeight;
        }
        for (i = iterations - 1; i >= 0; --i) {
            this.prepareUniformData(currentWidth, currentHeight, offset);
            encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
            GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
            int finalI = i;
            try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:kawase_up_pass_" + finalI, this.upTextureViews[i], OptionalInt.empty());){
                renderPass.setPipeline(UP_PIPELINE);
                renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
                renderPass.bindTexture("Sampler0", currentSource, sampler);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("DynamicTransforms", dynamicTransforms);
                renderPass.setUniform("KawaseData", this.uniformBuffer);
                renderPass.draw(0, 6);
            }
            currentSource = this.upTextureViews[i];
            currentWidth = this.upWidths[i];
            currentHeight = this.upHeights[i];
        }
        this.prepareUniformData(currentWidth, currentHeight, offset);
        encoder.writeToBuffer(this.uniformBuffer.slice(), this.dataBuffer);
        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)COLOR_MODULATOR, (Vector3fc)MODEL_OFFSET, (Matrix4fc)TEXTURE_MATRIX);
        try (RenderPass renderPass = encoder.createRenderPass(() -> "minecraft:kawase_final_pass", this.finalTextureView, OptionalInt.empty());){
            renderPass.setPipeline(UP_PIPELINE);
            renderPass.setVertexBuffer(0, this.dummyVertexBuffer);
            renderPass.bindTexture("Sampler0", currentSource, sampler);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setUniform("KawaseData", this.uniformBuffer);
            renderPass.draw(0, 6);
        }
        return this.finalTextureView;
    }

    private void prepareUniformData(int width, int height, float offset) {
        this.dataBuffer.clear();
        this.dataBuffer.putFloat(width);
        this.dataBuffer.putFloat(height);
        this.dataBuffer.putFloat(offset);
        this.dataBuffer.putFloat(0.0f);
        this.dataBuffer.flip();
    }

    public void close() {
        this.cleanupFramebuffers();
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

