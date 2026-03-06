/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.lwjgl.system.MemoryUtil
 */
package rich.util.render.pipeline;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public class ArcOutline2D {
    private static RenderPipeline pipeline;
    private static GpuBuffer uniformBuffer;
    private static final int UNIFORM_SIZE = 176;
    private static final float FIXED_GUI_SCALE = 2.0f;

    public static void init() {
        if (pipeline != null) {
            return;
        }
        try {
            pipeline = RenderPipeline.builder(new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"rich", (String)"core/arc_outline")).withVertexShader(Identifier.fromNamespaceAndPath((String)"rich", (String)"core/arc_outline_vertex")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"rich", (String)"core/arc_outline_fragment")).withVertexFormat(VertexFormat.builder().build(), VertexFormat.Mode.TRIANGLES).withUniform("Uniforms", UniformType.UNIFORM_BUFFER).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false).build();
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "ArcOutline2D Uniforms", 136, 176L);
        }
        catch (Exception e) {
            System.err.println("[ArcOutline2D] Failed to init: " + e.getMessage());
        }
    }

    public static void draw(Matrix4f matrix, float x, float y, float size, float arcThickness, float degree, float rotation, float outlineThickness, int fillColor, int outlineColor, float z) {
        if (pipeline == null) {
            ArcOutline2D.init();
        }
        if (pipeline == null || uniformBuffer == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        int framebufferWidth = client.getWindow().getWidth();
        int framebufferHeight = client.getWindow().getHeight();
        float fr = (float)(fillColor >> 16 & 0xFF) / 255.0f;
        float fg = (float)(fillColor >> 8 & 0xFF) / 255.0f;
        float fb = (float)(fillColor & 0xFF) / 255.0f;
        float fa = (float)(fillColor >> 24 & 0xFF) / 255.0f;
        float or = (float)(outlineColor >> 16 & 0xFF) / 255.0f;
        float og = (float)(outlineColor >> 8 & 0xFF) / 255.0f;
        float ob = (float)(outlineColor & 0xFF) / 255.0f;
        float oa = (float)(outlineColor >> 24 & 0xFF) / 255.0f;
        ByteBuffer buffer = MemoryUtil.memAlloc((int)176);
        buffer.putFloat(matrix.m00()).putFloat(matrix.m01()).putFloat(matrix.m02()).putFloat(matrix.m03());
        buffer.putFloat(matrix.m10()).putFloat(matrix.m11()).putFloat(matrix.m12()).putFloat(matrix.m13());
        buffer.putFloat(matrix.m20()).putFloat(matrix.m21()).putFloat(matrix.m22()).putFloat(matrix.m23());
        buffer.putFloat(matrix.m30()).putFloat(matrix.m31()).putFloat(matrix.m32()).putFloat(matrix.m33());
        buffer.position(64);
        buffer.putFloat(x * 2.0f).putFloat(y * 2.0f).putFloat(size * 2.0f).putFloat(size * 2.0f);
        buffer.putFloat(size * 2.0f).putFloat(arcThickness * 2.0f).putFloat(degree).putFloat(rotation);
        buffer.putFloat(z).putFloat(outlineThickness * 2.0f).putFloat(framebufferWidth).putFloat(framebufferHeight);
        buffer.putFloat(fr).putFloat(fg).putFloat(fb).putFloat(fa);
        buffer.putFloat(or).putFloat(og).putFloat(ob).putFloat(oa);
        buffer.putFloat(2.0f).putFloat(0.0f).putFloat(0.0f).putFloat(0.0f);
        buffer.flip();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(uniformBuffer.slice(), buffer);
        MemoryUtil.memFree((Buffer)buffer);
        RenderTarget framebuffer = client.getMainRenderTarget();
        try (RenderPass pass = encoder.createRenderPass(() -> "ArcOutline2D", framebuffer.getColorTextureView(), OptionalInt.empty(), framebuffer.getDepthTextureView(), OptionalDouble.of(1.0));){
            pass.setPipeline(pipeline);
            pass.setUniform("Uniforms", uniformBuffer);
            pass.draw(0, 6);
        }
    }

    public static void shutdown() {
        if (uniformBuffer != null) {
            uniformBuffer.close();
            uniformBuffer = null;
        }
        pipeline = null;
    }
}

