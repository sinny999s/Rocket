/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector4i
 */
package rich.util.render.draw;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4i;
import rich.IMinecraft;
import rich.util.render.draw.DrawEngine;

public class DrawEngineImpl
implements DrawEngine,
IMinecraft {
    @Override
    public void quad(Matrix4f matrix4f, BufferBuilder buffer, float x, float y, float width, float height) {
        buffer.addVertex((Matrix4fc)matrix4f, x, y, 0.0f);
        buffer.addVertex((Matrix4fc)matrix4f, x, y + height, 0.0f);
        buffer.addVertex((Matrix4fc)matrix4f, x + width, y + height, 0.0f);
        buffer.addVertex((Matrix4fc)matrix4f, x + width, y, 0.0f);
    }

    @Override
    public void quad(Matrix4f matrix4f, BufferBuilder buffer, float x, float y, float width, float height, int color) {
        buffer.addVertex((Matrix4fc)matrix4f, x, y, 0.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix4f, x, y + height, 0.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix4f, x + width, y + height, 0.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix4f, x + width, y, 0.0f).setColor(color);
    }

    @Override
    public void quad(Matrix4f matrix4f, float x, float y, float width, float height, int color) {
        BufferBuilder buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex((Matrix4fc)matrix4f, x, y + height, 0.0f).setUv(0.0f, 0.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix4f, x + width, y + height, 0.0f).setUv(0.0f, 1.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix4f, x + width, y, 0.0f).setUv(1.0f, 1.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix4f, x, y, 0.0f).setUv(1.0f, 0.0f).setColor(color);
    }

    @Override
    public void quadTexture(PoseStack.Pose entry, BufferBuilder buffer, float x, float y, float width, float height, Vector4i color) {
        buffer.addVertex(entry, x, y + height, 0.0f).setUv(0.0f, 0.0f).setColor(color.x);
        buffer.addVertex(entry, x + width, y + height, 0.0f).setUv(0.0f, 1.0f).setColor(color.y);
        buffer.addVertex(entry, x + width, y, 0.0f).setUv(1.0f, 1.0f).setColor(color.w);
        buffer.addVertex(entry, x, y, 0.0f).setUv(1.0f, 0.0f).setColor(color.z);
    }
}

