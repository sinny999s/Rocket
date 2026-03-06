/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package rich.util.render.fakeplayer;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Generated;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import rich.IMinecraft;
import rich.modules.impl.player.FreeCam;
import rich.util.ColorUtil;
import rich.util.render.sliemtpipeline.ClientPipelines;

public final class FakePlayerRenderer
implements IMinecraft {
    private static final float HEAD_SIZE = 0.5f;
    private static final float BODY_WIDTH = 0.5f;
    private static final float BODY_HEIGHT = 0.75f;
    private static final float BODY_DEPTH = 0.25f;
    private static final float ARM_WIDTH = 0.25f;
    private static final float ARM_HEIGHT = 0.75f;
    private static final float LEG_HEIGHT = 0.75f;
    private static final float MODEL_CENTER_Y = 1.125f;
    private static int currentAlpha = 255;

    public static void render(Vec3 position, float alpha) {
        if (FakePlayerRenderer.mc.player == null || alpha <= 0.001f) {
            return;
        }
        currentAlpha = (int)(Math.min(1.0f, Math.max(0.0f, alpha)) * 255.0f);
        Vec3 camPos = FakePlayerRenderer.mc.gameRenderer.getMainCamera().position();
        MultiBufferSource.BufferSource immediate = mc.renderBuffers().bufferSource();
        PoseStack stack = new PoseStack();
        GlStateManager._disableCull();
        GlStateManager._enableBlend();
        GlStateManager._blendFuncSeparate(770, 771, 1, 0);
        stack.pushPose();
        stack.translate(position.x - camPos.x, position.y - camPos.y, position.z - camPos.z);
        FakePlayerRenderer.renderPlayerModel(stack, immediate);
        stack.popPose();
        immediate.endBatch();
        GlStateManager._disableBlend();
        GlStateManager._enableCull();
    }

    private static void renderPlayerModel(PoseStack stack, MultiBufferSource.BufferSource immediate) {
        AABB leftLeg = new AABB(-0.25, 0.0, -0.125, 0.0, 0.75, 0.125);
        AABB rightLeg = new AABB(0.0, 0.0, -0.125, 0.25, 0.75, 0.125);
        AABB body = new AABB(-0.25, 0.75, -0.125, 0.25, 1.5, 0.125);
        AABB head = new AABB(-0.25, 1.5, -0.25, 0.25, 2.0, 0.25);
        AABB leftArm = new AABB(-0.5, 0.75, -0.125, -0.25, 1.5, 0.125);
        AABB rightArm = new AABB(0.25, 0.75, -0.125, 0.5, 1.5, 0.125);
        AABB[] bodyParts = new AABB[]{leftLeg, rightLeg, body, head, leftArm, rightArm};
        VertexConsumer consumer = immediate.getBuffer(ClientPipelines.CRYSTAL_FILLED);
        Matrix4f matrix = stack.last().pose();
        float centerX = 0.0f;
        float centerY = 1.125f;
        float centerZ = 0.0f;
        float maxDist = 1.0f;
        for (AABB part : bodyParts) {
            FakePlayerRenderer.drawBoxWithVignette(matrix, consumer, part, centerX, centerY, centerZ, maxDist);
        }
    }

    private static void drawBoxWithVignette(Matrix4f matrix, VertexConsumer consumer, AABB box, float centerX, float centerY, float centerZ, float maxDist) {
        float x1 = (float)box.minX;
        float y1 = (float)box.minY;
        float z1 = (float)box.minZ;
        float x2 = (float)box.maxX;
        float y2 = (float)box.maxY;
        float z2 = (float)box.maxZ;
        FakePlayerRenderer.drawQuadVignette(matrix, consumer, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, centerX, centerY, centerZ, maxDist);
        FakePlayerRenderer.drawQuadVignette(matrix, consumer, x1, y2, z2, x2, y2, z2, x2, y2, z1, x1, y2, z1, centerX, centerY, centerZ, maxDist);
        FakePlayerRenderer.drawQuadVignette(matrix, consumer, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, centerX, centerY, centerZ, maxDist);
        FakePlayerRenderer.drawQuadVignette(matrix, consumer, x2, y1, z2, x2, y2, z2, x1, y2, z2, x1, y1, z2, centerX, centerY, centerZ, maxDist);
        FakePlayerRenderer.drawQuadVignette(matrix, consumer, x1, y1, z2, x1, y2, z2, x1, y2, z1, x1, y1, z1, centerX, centerY, centerZ, maxDist);
        FakePlayerRenderer.drawQuadVignette(matrix, consumer, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, centerX, centerY, centerZ, maxDist);
    }

    private static void drawQuadVignette(Matrix4f matrix, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float centerX, float centerY, float centerZ, float maxDist) {
        consumer.addVertex((Matrix4fc)matrix, x1, y1, z1).setColor(FakePlayerRenderer.getVignetteColor(x1, y1, z1, centerX, centerY, centerZ, maxDist));
        consumer.addVertex((Matrix4fc)matrix, x2, y2, z2).setColor(FakePlayerRenderer.getVignetteColor(x2, y2, z2, centerX, centerY, centerZ, maxDist));
        consumer.addVertex((Matrix4fc)matrix, x3, y3, z3).setColor(FakePlayerRenderer.getVignetteColor(x3, y3, z3, centerX, centerY, centerZ, maxDist));
        consumer.addVertex((Matrix4fc)matrix, x4, y4, z4).setColor(FakePlayerRenderer.getVignetteColor(x4, y4, z4, centerX, centerY, centerZ, maxDist));
    }

    private static int getVignetteColor(float x, float y, float z, float centerX, float centerY, float centerZ, float maxDist) {
        float modelHalfWidth = 0.7f;
        float modelHalfHeight = 1.0f;
        float modelHalfDepth = 1.0f;
        float dx = Math.abs(x - centerX) / modelHalfWidth;
        float dy = Math.abs(y - centerY) / modelHalfHeight;
        float dz = Math.abs(z - centerZ) / modelHalfDepth;
        float t = Math.max(Math.max(dx, dy), dz);
        t = Math.min(1.0f, t);
        float colorIntensity = 0.6f;
        FreeCam freeCam = new FreeCam();
        int clientColor = freeCam.fakeplayer.getColor();
        int white = ColorUtil.rgba(255, 255, 255, currentAlpha);
        return ColorUtil.interpolateColor(white, clientColor, t *= colorIntensity);
    }

    public static void renderFromBox(AABB playerBox, float alpha) {
        double centerX = (playerBox.minX + playerBox.maxX) / 2.0;
        double bottomY = playerBox.minY;
        double centerZ = (playerBox.minZ + playerBox.maxZ) / 2.0;
        FakePlayerRenderer.render(new Vec3(centerX, bottomY, centerZ), alpha);
    }

    @Generated
    private FakePlayerRenderer() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

