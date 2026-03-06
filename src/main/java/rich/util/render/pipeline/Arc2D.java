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

public class Arc2D {
    private static RenderPipeline pipeline;
    private static GpuBuffer uniformBuffer;
    private static final int UNIFORM_SIZE = 320;
    private static final float FIXED_GUI_SCALE = 2.0f;

    public static void init() {
        if (pipeline != null) {
            return;
        }
        try {
            pipeline = RenderPipeline.builder(new RenderPipeline.Snippet[0]).withLocation(Identifier.fromNamespaceAndPath((String)"rich", (String)"core/arc")).withVertexShader(Identifier.fromNamespaceAndPath((String)"rich", (String)"core/arc_vertex")).withFragmentShader(Identifier.fromNamespaceAndPath((String)"rich", (String)"core/arc_fragment")).withVertexFormat(VertexFormat.builder().build(), VertexFormat.Mode.TRIANGLES).withUniform("Uniforms", UniformType.UNIFORM_BUFFER).withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false).build();
            uniformBuffer = RenderSystem.getDevice().createBuffer(() -> "Arc2D Uniforms", 136, 320L);
        }
        catch (Exception e) {
            System.err.println("[Arc2D] Failed to init: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void draw(Matrix4f matrix, float x, float y, float size, float thickness, float degree, float rotation, float z, int ... colors) {
        if (pipeline == null) {
            Arc2D.init();
        }
        if (pipeline == null || uniformBuffer == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        int framebufferWidth = client.getWindow().getWidth();
        int framebufferHeight = client.getWindow().getHeight();
        int[] finalColors = Arc2D.normalizeColors(colors);
        ByteBuffer buffer = MemoryUtil.memAlloc((int)320);
        buffer.putFloat(matrix.m00()).putFloat(matrix.m01()).putFloat(matrix.m02()).putFloat(matrix.m03());
        buffer.putFloat(matrix.m10()).putFloat(matrix.m11()).putFloat(matrix.m12()).putFloat(matrix.m13());
        buffer.putFloat(matrix.m20()).putFloat(matrix.m21()).putFloat(matrix.m22()).putFloat(matrix.m23());
        buffer.putFloat(matrix.m30()).putFloat(matrix.m31()).putFloat(matrix.m32()).putFloat(matrix.m33());
        buffer.position(64);
        buffer.putFloat(x * 2.0f);
        buffer.putFloat(y * 2.0f);
        buffer.putFloat(size * 2.0f);
        buffer.putFloat(size * 2.0f);
        buffer.putFloat(size * 2.0f);
        buffer.putFloat(thickness * 2.0f);
        buffer.putFloat(degree);
        buffer.putFloat(rotation);
        buffer.putFloat(z);
        buffer.putFloat(2.0f);
        buffer.putFloat(framebufferWidth);
        buffer.putFloat(framebufferHeight);
        buffer.position(112);
        for (int i = 0; i < 9; ++i) {
            int color = finalColors[i];
            buffer.putFloat((float)(color >> 16 & 0xFF) / 255.0f);
            buffer.putFloat((float)(color >> 8 & 0xFF) / 255.0f);
            buffer.putFloat((float)(color & 0xFF) / 255.0f);
            buffer.putFloat((float)(color >> 24 & 0xFF) / 255.0f);
        }
        buffer.flip();
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.writeToBuffer(uniformBuffer.slice(), buffer);
        MemoryUtil.memFree((Buffer)buffer);
        RenderTarget framebuffer = client.getMainRenderTarget();
        try (RenderPass pass = encoder.createRenderPass(() -> "Arc2D", framebuffer.getColorTextureView(), OptionalInt.empty(), framebuffer.getDepthTextureView(), OptionalDouble.of(1.0));){
            pass.setPipeline(pipeline);
            pass.setUniform("Uniforms", uniformBuffer);
            pass.draw(0, 6);
        }
    }

    private static int[] normalizeColors(int[] colors) {
        if (colors.length == 1) {
            int c = colors[0];
            return new int[]{c, c, c, c, c, c, c, c, c};
        }
        if (colors.length >= 9) {
            return colors;
        }
        int[] result = new int[9];
        for (int i = 0; i < 9; ++i) {
            result[i] = i < colors.length ? colors[i] : colors[colors.length - 1];
        }
        return result;
    }

    public static void shutdown() {
        if (uniformBuffer != null) {
            uniformBuffer.close();
            uniformBuffer = null;
        }
        pipeline = null;
    }
}

