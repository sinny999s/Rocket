/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Vector4i
 */
package rich.util.render.draw;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector4i;

public interface DrawEngine {
    public void quad(Matrix4f var1, BufferBuilder var2, float var3, float var4, float var5, float var6);

    public void quad(Matrix4f var1, BufferBuilder var2, float var3, float var4, float var5, float var6, int var7);

    public void quadTexture(PoseStack.Pose var1, BufferBuilder var2, float var3, float var4, float var5, float var6, Vector4i var7);

    public void quad(Matrix4f var1, float var2, float var3, float var4, float var5, int var6);
}

