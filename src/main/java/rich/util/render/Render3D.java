/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package rich.util.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import rich.IMinecraft;
import rich.events.impl.WorldRenderEvent;
import rich.util.ColorUtil;
import rich.util.math.MathUtils;

public final class Render3D
implements IMinecraft {
    private static final Map<VoxelShape, Tuple<List<AABB>, List<Line>>> SHAPE_OUTLINES = new HashMap<VoxelShape, Tuple<List<AABB>, List<Line>>>();
    private static final Map<VoxelShape, List<AABB>> SHAPE_BOXES = new HashMap<VoxelShape, List<AABB>>();
    public static final List<Line> LINE_DEPTH = new ArrayList<Line>();
    public static final List<Line> LINE = new ArrayList<Line>();
    public static final List<Quad> QUAD_DEPTH = new ArrayList<Quad>();
    public static final List<Quad> QUAD = new ArrayList<Quad>();
    public static final List<GradientQuad> GRADIENT_QUAD = new ArrayList<GradientQuad>();
    public static final List<GradientQuad> GRADIENT_QUAD_DEPTH = new ArrayList<GradientQuad>();
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();
    public static PoseStack.Pose lastWorldSpaceEntry = new PoseStack().last();
    public static float lastTickDelta = 1.0f;
    public static Vec3 lastCameraPos = Vec3.ZERO;
    public static Quaternionf lastCameraRotation = new Quaternionf();
    private static float espValue = 1.0f;
    private static float espSpeed = 1.0f;
    private static float prevEspValue;
    private static float circleStep;
    private static boolean flipSpeed;
    private static double smoothY;
    private static double smoothY2;

    public static void updateTargetEsp(float deltaTime) {
        prevEspValue = espValue;
        espValue += espSpeed * deltaTime;
        if (espSpeed > 25.0f) {
            flipSpeed = true;
        }
        if (espSpeed < -25.0f) {
            flipSpeed = false;
        }
        espSpeed = flipSpeed ? espSpeed - 0.5f * deltaTime : espSpeed + 0.5f * deltaTime;
        circleStep += 0.06f * deltaTime;
    }

    public static void updateTargetEsp() {
        Render3D.updateTargetEsp(1.0f);
    }

    public static float getEspValue() {
        return espValue;
    }

    public static float getPrevEspValue() {
        return prevEspValue;
    }

    public static float getCircleStep() {
        return circleStep;
    }

    private static double easeInOutSine(double t) {
        return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
    }

    private static double smoothSinAnimation(double input) {
        double sin = (Math.sin(input) + 1.0) / 2.0;
        return Render3D.easeInOutSine(sin);
    }

    public static void onWorldRender(WorldRenderEvent e) {
        if (Render3D.mc.level == null || Render3D.mc.player == null) {
            return;
        }
        PoseStack matrices = e.getStack();
        MultiBufferSource.BufferSource immediate = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = lastCameraPos;
        Render3D.renderGradientQuads(matrices, immediate, cameraPos);
        Render3D.renderQuads(matrices, immediate, cameraPos);
        Render3D.renderLines(matrices, immediate, cameraPos);
        immediate.endBatch();
    }

    private static void renderLines(PoseStack matrices, MultiBufferSource.BufferSource immediate, Vec3 cameraPos) {
        if (LINE.isEmpty() && LINE_DEPTH.isEmpty()) {
            return;
        }
        VertexConsumer buffer = immediate.getBuffer(RenderTypes.lines());
        for (Line line : LINE) {
            Render3D.drawLineVertex(matrices, buffer, line, cameraPos);
        }
        for (Line line : LINE_DEPTH) {
            Render3D.drawLineVertex(matrices, buffer, line, cameraPos);
        }
        LINE.clear();
        LINE_DEPTH.clear();
    }

    private static void drawLineVertex(PoseStack matrices, VertexConsumer buffer, Line line, Vec3 cameraPos) {
        PoseStack.Pose entry = matrices.last();
        Vector3f normal = Render3D.getNormal(line.start.toVector3f(), line.end.toVector3f());
        float x1 = (float)(line.start.x - cameraPos.x);
        float y1 = (float)(line.start.y - cameraPos.y);
        float z1 = (float)(line.start.z - cameraPos.z);
        float x2 = (float)(line.end.x - cameraPos.x);
        float y2 = (float)(line.end.y - cameraPos.y);
        float z2 = (float)(line.end.z - cameraPos.z);
        buffer.addVertex(entry, x1, y1, z1).setColor(line.colorStart).setNormal(entry, normal).setLineWidth(line.width);
        buffer.addVertex(entry, x2, y2, z2).setColor(line.colorEnd).setNormal(entry, normal).setLineWidth(line.width);
    }

    private static void renderQuads(PoseStack matrices, MultiBufferSource.BufferSource immediate, Vec3 cameraPos) {
        if (QUAD.isEmpty() && QUAD_DEPTH.isEmpty()) {
            return;
        }
        VertexConsumer buffer = immediate.getBuffer(RenderTypes.debugFilledBox());
        for (Quad quad : QUAD) {
            Render3D.drawQuadVertex(matrices, buffer, quad, cameraPos);
        }
        for (Quad quad : QUAD_DEPTH) {
            Render3D.drawQuadVertex(matrices, buffer, quad, cameraPos);
        }
        QUAD.clear();
        QUAD_DEPTH.clear();
    }

    private static void drawQuadVertex(PoseStack matrices, VertexConsumer buffer, Quad quad, Vec3 cameraPos) {
        PoseStack.Pose entry = matrices.last();
        float x1 = (float)(quad.x.x - cameraPos.x);
        float y1 = (float)(quad.x.y - cameraPos.y);
        float z1 = (float)(quad.x.z - cameraPos.z);
        float x2 = (float)(quad.y.x - cameraPos.x);
        float y2 = (float)(quad.y.y - cameraPos.y);
        float z2 = (float)(quad.y.z - cameraPos.z);
        float x3 = (float)(quad.w.x - cameraPos.x);
        float y3 = (float)(quad.w.y - cameraPos.y);
        float z3 = (float)(quad.w.z - cameraPos.z);
        float x4 = (float)(quad.z.x - cameraPos.x);
        float y4 = (float)(quad.z.y - cameraPos.y);
        float z4 = (float)(quad.z.z - cameraPos.z);
        buffer.addVertex(entry, x1, y1, z1).setColor(quad.color);
        buffer.addVertex(entry, x2, y2, z2).setColor(quad.color);
        buffer.addVertex(entry, x3, y3, z3).setColor(quad.color);
        buffer.addVertex(entry, x4, y4, z4).setColor(quad.color);
    }

    private static void renderGradientQuads(PoseStack matrices, MultiBufferSource.BufferSource immediate, Vec3 cameraPos) {
        if (GRADIENT_QUAD.isEmpty() && GRADIENT_QUAD_DEPTH.isEmpty()) {
            return;
        }
        VertexConsumer buffer = immediate.getBuffer(RenderTypes.debugFilledBox());
        for (GradientQuad quad : GRADIENT_QUAD) {
            Render3D.drawGradientQuadVertex(matrices, buffer, quad, cameraPos);
        }
        for (GradientQuad quad : GRADIENT_QUAD_DEPTH) {
            Render3D.drawGradientQuadVertex(matrices, buffer, quad, cameraPos);
        }
        GRADIENT_QUAD.clear();
        GRADIENT_QUAD_DEPTH.clear();
    }

    private static void drawGradientQuadVertex(PoseStack matrices, VertexConsumer buffer, GradientQuad quad, Vec3 cameraPos) {
        PoseStack.Pose entry = matrices.last();
        float x1 = (float)(quad.p1.x - cameraPos.x);
        float y1 = (float)(quad.p1.y - cameraPos.y);
        float z1 = (float)(quad.p1.z - cameraPos.z);
        float x2 = (float)(quad.p2.x - cameraPos.x);
        float y2 = (float)(quad.p2.y - cameraPos.y);
        float z2 = (float)(quad.p2.z - cameraPos.z);
        float x3 = (float)(quad.p3.x - cameraPos.x);
        float y3 = (float)(quad.p3.y - cameraPos.y);
        float z3 = (float)(quad.p3.z - cameraPos.z);
        float x4 = (float)(quad.p4.x - cameraPos.x);
        float y4 = (float)(quad.p4.y - cameraPos.y);
        float z4 = (float)(quad.p4.z - cameraPos.z);
        buffer.addVertex(entry, x1, y1, z1).setColor(quad.c1);
        buffer.addVertex(entry, x2, y2, z2).setColor(quad.c2);
        buffer.addVertex(entry, x3, y3, z3).setColor(quad.c3);
        buffer.addVertex(entry, x4, y4, z4).setColor(quad.c4);
    }

    public static void drawCircle(PoseStack matrix, LivingEntity lastTarget, float anim, float red, int baseColor1, int baseColor2) {
        double cs = MathUtils.interpolate((double)circleStep - 0.17, (double)circleStep);
        Vec3 target = MathUtils.interpolate(lastTarget);
        boolean canSee = Render3D.mc.player != null && Render3D.mc.player.hasLineOfSight(lastTarget);
        float hitEffect = Math.min(red * 2.0f, 1.0f);
        float distanceMultiplier = 1.0f + (float)Math.sin((double)hitEffect * Math.PI) * 0.18f;
        int size = 64;
        float entityWidth = lastTarget.getBbWidth() * distanceMultiplier;
        float entityHeight = lastTarget.getBbHeight();
        double targetY = Render3D.smoothSinAnimation(cs) * (double)entityHeight;
        double targetY2 = Render3D.smoothSinAnimation(cs - 0.35) * (double)entityHeight;
        smoothY = Render3D.lerp(smoothY, targetY, 0.12);
        smoothY2 = Render3D.lerp(smoothY2, targetY2, 0.1);
        int color1 = ColorUtil.multRed(baseColor1, 1.0f + red * 125.0f);
        int color2 = ColorUtil.multRed(baseColor2, 1.0f + red * 125.0f);
        for (int i = 0; i < size; ++i) {
            float t = (float)i / (float)size;
            float tNext = (float)((i + 1) % size) / (float)size;
            float gradientT = (float)(0.5 - 0.5 * Math.cos((double)t * Math.PI * 2.0));
            float gradientTNext = (float)(0.5 - 0.5 * Math.cos((double)tNext * Math.PI * 2.0));
            int currentColor = ColorUtil.lerpColor(color1, color2, gradientT);
            int nextColor = ColorUtil.lerpColor(color1, color2, gradientTNext);
            int brightColor = ColorUtil.multAlpha(currentColor, 0.8f * anim);
            int brightColorNext = ColorUtil.multAlpha(nextColor, 0.8f * anim);
            int fadeColor = ColorUtil.multAlpha(currentColor, 0.0f);
            int fadeColorNext = ColorUtil.multAlpha(nextColor, 0.0f);
            Vec3 cosSin = MathUtils.cosSin(i, size, entityWidth);
            Vec3 nextCosSin = MathUtils.cosSin((i + 1) % size, size, entityWidth);
            Vec3 circlePoint = target.add(cosSin.x, smoothY, cosSin.z);
            Vec3 trailPoint = target.add(cosSin.x, smoothY2, cosSin.z);
            Vec3 nextCirclePoint = target.add(nextCosSin.x, smoothY, nextCosSin.z);
            Vec3 nextTrailPoint = target.add(nextCosSin.x, smoothY2, nextCosSin.z);
            Render3D.drawGradientQuad(circlePoint, nextCirclePoint, nextTrailPoint, trailPoint, brightColor, brightColorNext, fadeColorNext, fadeColor, canSee);
            Render3D.drawGradientQuad(trailPoint, nextTrailPoint, nextCirclePoint, circlePoint, fadeColor, fadeColorNext, brightColorNext, brightColor, canSee);
            int trailColorTop = ColorUtil.multAlpha(currentColor, 0.15f * anim);
            int trailColorBottom = ColorUtil.multAlpha(currentColor, 0.0f);
            Render3D.drawLineGradient(circlePoint, trailPoint, trailColorTop, trailColorBottom, 6.0f, canSee);
            int circleColor = ColorUtil.multAlpha(currentColor, 1.0f * anim);
            int circleColorNext = ColorUtil.multAlpha(nextColor, 1.0f * anim);
            Render3D.drawLineGradient(circlePoint, nextCirclePoint, circleColor, circleColorNext, 2.0f, canSee);
        }
    }

    public static void drawRadiusCircle(Vec3 center, float radius, int color) {
        if (Render3D.mc.player == null) {
            return;
        }
        double baseY = center.y;
        int fillColor = ColorUtil.multAlpha(color, 0.25f);
        int radiusInt = (int)Math.ceil(radius) + 1;
        for (int dx = -radiusInt; dx <= radiusInt; ++dx) {
            for (int dz = -radiusInt; dz <= radiusInt; ++dz) {
                boolean hasCornerInside = false;
                boolean hasCornerOutside = false;
                for (double ox = -0.5; ox <= 0.5; ox += 1.0) {
                    for (double oz = -0.5; oz <= 0.5; oz += 1.0) {
                        double cornerDist = Math.sqrt(((double)dx + ox) * ((double)dx + ox) + ((double)dz + oz) * ((double)dz + oz));
                        if (cornerDist <= (double)radius) {
                            hasCornerInside = true;
                            continue;
                        }
                        hasCornerOutside = true;
                    }
                }
                if (!hasCornerInside || !hasCornerOutside) continue;
                double x = center.x + (double)dx;
                double z = center.z + (double)dz;
                AABB box = new AABB(x - 0.5, baseY, z - 0.5, x + 0.5, baseY + 1.0, z + 0.5);
                Render3D.drawBoxWithCross(box, color, fillColor, 2.0f);
            }
        }
    }

    public static void drawBoxWithCross(AABB box, int lineColor, int fillColor, float lineWidth) {
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;
        Render3D.drawQuad(new Vec3(x1, y1, z1), new Vec3(x2, y1, z1), new Vec3(x2, y1, z2), new Vec3(x1, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y1, z1), new Vec3(x1, y2, z1), new Vec3(x2, y2, z1), new Vec3(x2, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3(x2, y1, z1), new Vec3(x2, y2, z1), new Vec3(x2, y2, z2), new Vec3(x2, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y1, z2), new Vec3(x2, y1, z2), new Vec3(x2, y2, z2), new Vec3(x1, y2, z2), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y1, z1), new Vec3(x1, y1, z2), new Vec3(x1, y2, z2), new Vec3(x1, y2, z1), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y2, z1), new Vec3(x1, y2, z2), new Vec3(x2, y2, z2), new Vec3(x2, y2, z1), fillColor, false);
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x2, y1, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x2, y1, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z2), new Vec3(x1, y1, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z2), new Vec3(x1, y1, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z2), new Vec3(x1, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x1, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z2), new Vec3(x2, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x2, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y2, z1), new Vec3(x2, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y2, z1), new Vec3(x2, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y2, z2), new Vec3(x1, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y2, z2), new Vec3(x1, y2, z1), lineColor, lineWidth, false);
        int crossColor = ColorUtil.multAlpha(lineColor, 0.6f);
        float crossWidth = lineWidth * 0.8f;
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x2, y1, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x1, y1, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y2, z1), new Vec3(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y2, z1), new Vec3(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x2, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x1, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z2), new Vec3(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z2), new Vec3(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z2), new Vec3(x1, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z2), new Vec3(x2, y2, z1), crossColor, crossWidth, false);
    }

    public static void drawBoxWithCrossFull(AABB box, int lineColor, int fillColor, float lineWidth) {
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;
        Render3D.drawQuad(new Vec3(x1, y1, z1), new Vec3(x2, y1, z1), new Vec3(x2, y1, z2), new Vec3(x1, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y1, z1), new Vec3(x1, y2, z1), new Vec3(x2, y2, z1), new Vec3(x2, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3(x2, y1, z1), new Vec3(x2, y2, z1), new Vec3(x2, y2, z2), new Vec3(x2, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y1, z2), new Vec3(x2, y1, z2), new Vec3(x2, y2, z2), new Vec3(x1, y2, z2), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y1, z1), new Vec3(x1, y1, z2), new Vec3(x1, y2, z2), new Vec3(x1, y2, z1), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y2, z1), new Vec3(x1, y2, z2), new Vec3(x2, y2, z2), new Vec3(x2, y2, z1), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y1, z2), new Vec3(x2, y1, z2), new Vec3(x2, y1, z1), new Vec3(x1, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3(x2, y1, z1), new Vec3(x2, y2, z1), new Vec3(x1, y2, z1), new Vec3(x1, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3(x2, y1, z2), new Vec3(x2, y2, z2), new Vec3(x2, y2, z1), new Vec3(x2, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y2, z2), new Vec3(x2, y2, z2), new Vec3(x2, y1, z2), new Vec3(x1, y1, z2), fillColor, false);
        Render3D.drawQuad(new Vec3(x1, y2, z1), new Vec3(x1, y2, z2), new Vec3(x1, y1, z2), new Vec3(x1, y1, z1), fillColor, false);
        Render3D.drawQuad(new Vec3(x2, y2, z1), new Vec3(x2, y2, z2), new Vec3(x1, y2, z2), new Vec3(x1, y2, z1), fillColor, false);
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x2, y1, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x2, y1, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z2), new Vec3(x1, y1, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z2), new Vec3(x1, y1, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z2), new Vec3(x1, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x1, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z2), new Vec3(x2, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x2, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y2, z1), new Vec3(x2, y2, z1), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y2, z1), new Vec3(x2, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x2, y2, z2), new Vec3(x1, y2, z2), lineColor, lineWidth, false);
        Render3D.drawLine(new Vec3(x1, y2, z2), new Vec3(x1, y2, z1), lineColor, lineWidth, false);
        int crossColor = ColorUtil.multAlpha(lineColor, 0.6f);
        float crossWidth = lineWidth * 0.8f;
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x2, y1, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x1, y1, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y2, z1), new Vec3(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y2, z1), new Vec3(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x2, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x1, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z2), new Vec3(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z2), new Vec3(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z1), new Vec3(x1, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x1, y1, z2), new Vec3(x1, y2, z1), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z1), new Vec3(x2, y2, z2), crossColor, crossWidth, false);
        Render3D.drawLine(new Vec3(x2, y1, z2), new Vec3(x2, y2, z1), crossColor, crossWidth, false);
    }

    public static void drawPlastShape(BlockPos playerPos, Vec3 smooth, int lineColor, int fillColor) {
        if (Render3D.mc.player == null) {
            return;
        }
        float yaw = Mth.wrapDegrees((float)Render3D.mc.player.getYRot());
        if (Math.abs(Render3D.mc.player.getXRot()) > 60.0f) {
            BlockPos blockPos = playerPos.above().relative(Render3D.mc.player.getNearestViewDirection(), 3);
            Vec3 pos1 = Vec3.atLowerCornerOf((Vec3i)blockPos.east(3).south(3).below()).add(smooth);
            Vec3 pos2 = Vec3.atLowerCornerOf((Vec3i)blockPos.west(2).north(2).above()).add(smooth);
            Render3D.drawBoxWithCrossFull(new AABB(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= -157.5f || yaw >= 157.5f) {
            BlockPos blockPos = playerPos.north(3).above();
            Vec3 pos1 = Vec3.atLowerCornerOf((Vec3i)blockPos.below(2).east(3)).add(smooth);
            Vec3 pos2 = Vec3.atLowerCornerOf((Vec3i)blockPos.above(3).west(2).south(2)).add(smooth);
            Render3D.drawBoxWithCrossFull(new AABB(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= -112.5f) {
            Render3D.drawSidePlast(playerPos.east(5).south().below(), smooth, lineColor, fillColor, -1, true);
        } else if (yaw <= -67.5f) {
            BlockPos blockPos = playerPos.east(2).above();
            Vec3 pos1 = Vec3.atLowerCornerOf((Vec3i)blockPos.below(2).south(3)).add(smooth);
            Vec3 pos2 = Vec3.atLowerCornerOf((Vec3i)blockPos.above(3).north(2).east(2)).add(smooth);
            Render3D.drawBoxWithCrossFull(new AABB(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= -22.5f) {
            Render3D.drawSidePlast(playerPos.east(5).below(), smooth, lineColor, fillColor, 1, false);
        } else if ((double)yaw >= -22.5 && (double)yaw <= 22.5) {
            BlockPos blockPos = playerPos.south(2).above();
            Vec3 pos1 = Vec3.atLowerCornerOf((Vec3i)blockPos.below(2).east(3)).add(smooth);
            Vec3 pos2 = Vec3.atLowerCornerOf((Vec3i)blockPos.above(3).west(2).south(2)).add(smooth);
            Render3D.drawBoxWithCrossFull(new AABB(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= 67.5f) {
            Render3D.drawSidePlast(playerPos.west(4).below(), smooth, lineColor, fillColor, 1, true);
        } else if (yaw <= 112.5f) {
            BlockPos blockPos = playerPos.west(3).above();
            Vec3 pos1 = Vec3.atLowerCornerOf((Vec3i)blockPos.below(2).south(3)).add(smooth);
            Vec3 pos2 = Vec3.atLowerCornerOf((Vec3i)blockPos.above(3).north(2).east(2)).add(smooth);
            Render3D.drawBoxWithCrossFull(new AABB(pos1, pos2), lineColor, fillColor, 3.0f);
        } else if (yaw <= 157.5f) {
            Render3D.drawSidePlast(playerPos.west(4).south().below(), smooth, lineColor, fillColor, -1, false);
        }
    }

    private static void drawSidePlast(BlockPos blockPos, Vec3 smooth, int lineColor, int fillColor, int i, boolean ff) {
        int f;
        Vec3 p2;
        Vec3 p1;
        int f2;
        Vec3 vec3d = Vec3.atLowerCornerOf((Vec3i)blockPos).add(smooth);
        int crossColor = ColorUtil.multAlpha(lineColor, 0.6f);
        ArrayList<Vec3> horizontalPoints = new ArrayList<Vec3>();
        float x = ff ? (float)i : (float)(-i);
        Vec3 current = vec3d;
        horizontalPoints.add(current);
        current = current.add(x, 0.0, 0.0);
        horizontalPoints.add(current);
        for (f2 = 0; f2 < 4; ++f2) {
            current = current.add(0.0, 0.0, i);
            horizontalPoints.add(current);
            current = current.add(x, 0.0, 0.0);
            horizontalPoints.add(current);
        }
        current = current.add(0.0, 0.0, i);
        horizontalPoints.add(current);
        current = current.add(x * -2.0f, 0.0, 0.0);
        horizontalPoints.add(current);
        for (f2 = 0; f2 < 3; ++f2) {
            current = current.add(0.0, 0.0, i * -1);
            horizontalPoints.add(current);
            current = current.add(x * -1.0f, 0.0, 0.0);
            horizontalPoints.add(current);
        }
        current = current.add(0.0, 0.0, i * -2);
        horizontalPoints.add(current);
        for (int p = 0; p < horizontalPoints.size() - 1; ++p) {
            p1 = (Vec3)horizontalPoints.get(p);
            p2 = (Vec3)horizontalPoints.get(p + 1);
            Render3D.drawLine(p1, p2, lineColor, 2.0f, false);
            Render3D.drawLine(p1.add(0.0, 5.0, 0.0), p2.add(0.0, 5.0, 0.0), lineColor, 2.0f, false);
        }
        for (Vec3 point : horizontalPoints) {
            Render3D.drawLine(point, point.add(0.0, 5.0, 0.0), lineColor, 2.0f, false);
        }
        for (int p = 0; p < horizontalPoints.size() - 1; ++p) {
            p1 = (Vec3)horizontalPoints.get(p);
            p2 = (Vec3)horizontalPoints.get(p + 1);
            Vec3 p1Top = p1.add(0.0, 5.0, 0.0);
            Vec3 p2Top = p2.add(0.0, 5.0, 0.0);
            Render3D.drawQuad(p1, p2, p2Top, p1Top, fillColor, false);
            Render3D.drawQuad(p1Top, p2Top, p2, p1, fillColor, false);
            Render3D.drawLine(p1, p2Top, crossColor, 1.6f, false);
            Render3D.drawLine(p2, p1Top, crossColor, 1.6f, false);
        }
        current = vec3d;
        Render3D.drawQuad(current, current.add(x, 0.0, 0.0), current.add(x, 0.0, i * 2), current.add(0.0, 0.0, i * 2), fillColor, false);
        Render3D.drawQuad(current.add(0.0, 0.0, i * 2), current.add(x, 0.0, i * 2), current.add(x, 0.0, 0.0), current, fillColor, false);
        Render3D.drawLine(current, current.add(x, 0.0, i * 2), crossColor, 1.6f, false);
        Render3D.drawLine(current.add(x, 0.0, 0.0), current.add(0.0, 0.0, i * 2), crossColor, 1.6f, false);
        for (f = 0; f < 3; ++f) {
            current = current.add(x, 0.0, i);
            Render3D.drawQuad(current, current.add(x, 0.0, 0.0), current.add(x, 0.0, i * 2), current.add(0.0, 0.0, i * 2), fillColor, false);
            Render3D.drawQuad(current.add(0.0, 0.0, i * 2), current.add(x, 0.0, i * 2), current.add(x, 0.0, 0.0), current, fillColor, false);
            Render3D.drawLine(current, current.add(x, 0.0, i * 2), crossColor, 1.6f, false);
            Render3D.drawLine(current.add(x, 0.0, 0.0), current.add(0.0, 0.0, i * 2), crossColor, 1.6f, false);
        }
        current = current.add(x, 0.0, i);
        Render3D.drawQuad(current, current.add(x, 0.0, 0.0), current.add(x, 0.0, i), current.add(0.0, 0.0, i), fillColor, false);
        Render3D.drawQuad(current.add(0.0, 0.0, i), current.add(x, 0.0, i), current.add(x, 0.0, 0.0), current, fillColor, false);
        Render3D.drawLine(current, current.add(x, 0.0, i), crossColor, 1.6f, false);
        Render3D.drawLine(current.add(x, 0.0, 0.0), current.add(0.0, 0.0, i), crossColor, 1.6f, false);
        current = vec3d.add(0.0, 5.0, 0.0);
        Render3D.drawQuad(current, current.add(0.0, 0.0, i * 2), current.add(x, 0.0, i * 2), current.add(x, 0.0, 0.0), fillColor, false);
        Render3D.drawQuad(current.add(x, 0.0, 0.0), current.add(x, 0.0, i * 2), current.add(0.0, 0.0, i * 2), current, fillColor, false);
        Render3D.drawLine(current, current.add(x, 0.0, i * 2), crossColor, 1.6f, false);
        Render3D.drawLine(current.add(x, 0.0, 0.0), current.add(0.0, 0.0, i * 2), crossColor, 1.6f, false);
        for (f = 0; f < 3; ++f) {
            current = current.add(x, 0.0, i);
            Render3D.drawQuad(current, current.add(0.0, 0.0, i * 2), current.add(x, 0.0, i * 2), current.add(x, 0.0, 0.0), fillColor, false);
            Render3D.drawQuad(current.add(x, 0.0, 0.0), current.add(x, 0.0, i * 2), current.add(0.0, 0.0, i * 2), current, fillColor, false);
            Render3D.drawLine(current, current.add(x, 0.0, i * 2), crossColor, 1.6f, false);
            Render3D.drawLine(current.add(x, 0.0, 0.0), current.add(0.0, 0.0, i * 2), crossColor, 1.6f, false);
        }
        current = current.add(x, 0.0, i);
        Render3D.drawQuad(current, current.add(0.0, 0.0, i), current.add(x, 0.0, i), current.add(x, 0.0, 0.0), fillColor, false);
        Render3D.drawQuad(current.add(x, 0.0, 0.0), current.add(x, 0.0, i), current.add(0.0, 0.0, i), current, fillColor, false);
        Render3D.drawLine(current, current.add(x, 0.0, i), crossColor, 1.6f, false);
        Render3D.drawLine(current.add(x, 0.0, 0.0), current.add(0.0, 0.0, i), crossColor, 1.6f, false);
    }

    private static double lerp(double start, double end, double delta) {
        return start + (end - start) * delta;
    }

    public static void drawGradientQuad(Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, int c1, int c2, int c3, int c4, boolean depth) {
        GradientQuad quad = new GradientQuad(p1, p2, p3, p4, c1, c2, c3, c4);
        if (depth) {
            GRADIENT_QUAD_DEPTH.add(quad);
        } else {
            GRADIENT_QUAD.add(quad);
        }
    }

    public static void drawLineGradient(Vec3 start, Vec3 end, int colorStart, int colorEnd, float width, boolean depth) {
        Line line = new Line(null, start, end, colorStart, colorEnd, width);
        if (depth) {
            LINE_DEPTH.add(line);
        } else {
            LINE.add(line);
        }
    }

    public static Vector3f getNormal(Vector3f start, Vector3f end) {
        Vector3f normal = new Vector3f((Vector3fc)start).sub((Vector3fc)end);
        float sqrt = Mth.sqrt((float)normal.lengthSquared());
        if (sqrt < 1.0E-4f) {
            return new Vector3f(0.0f, 1.0f, 0.0f);
        }
        return normal.div(sqrt);
    }

    public static void drawShape(BlockPos blockPos, VoxelShape voxelShape, int color, float width) {
        Render3D.drawShape(blockPos, voxelShape, color, width, true, false);
    }

    public static void drawShape(BlockPos blockPos, VoxelShape voxelShape, int color, float width, boolean fill, boolean depth) {
        if (SHAPE_BOXES.containsKey(voxelShape)) {
            SHAPE_BOXES.get(voxelShape).forEach(box -> {
                AABB offsetBox = box.move(blockPos);
                Render3D.drawBox(offsetBox, color, width, true, fill, depth);
            });
            return;
        }
        SHAPE_BOXES.put(voxelShape, voxelShape.toAabbs());
    }

    public static void drawShapeAlternative(BlockPos blockPos, VoxelShape voxelShape, int color, float width, boolean fill, boolean depth) {
        Vec3 vec3d = Vec3.atLowerCornerOf((Vec3i)blockPos);
        if (SHAPE_OUTLINES.containsKey(voxelShape)) {
            Tuple<List<AABB>, List<Line>> pair = SHAPE_OUTLINES.get(voxelShape);
            if (fill) {
                ((List<AABB>)pair.getA()).forEach(box -> Render3D.drawBox(box.move(vec3d), color, width, false, true, depth));
            }
            ((List<Line>)pair.getB()).forEach(line -> Render3D.drawLine(line.start.add(vec3d), line.end.add(vec3d), color, width, depth));
            return;
        }
        ArrayList lines = new ArrayList();
        voxelShape.forAllEdges((minX, minY, minZ, maxX, maxY, maxZ) -> lines.add(new Line(null, new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ), 0, 0, 0.0f)));
        SHAPE_OUTLINES.put(voxelShape, new Tuple(voxelShape.toAabbs(), lines));
    }

    public static void drawBox(AABB box, int color, float width) {
        Render3D.drawBox(box, color, width, true, true, false);
    }

    public static void drawBox(AABB box, int color, float width, boolean line, boolean fill, boolean depth) {
        Render3D.drawBox(null, box, color, width, line, fill, depth);
    }

    public static void drawBox(PoseStack.Pose entry, AABB box, int color, float width, boolean line, boolean fill, boolean depth) {
        double x1 = box.minX;
        double y1 = box.minY;
        double z1 = box.minZ;
        double x2 = box.maxX;
        double y2 = box.maxY;
        double z2 = box.maxZ;
        if (fill) {
            int fillColor = ColorUtil.multAlpha(color, 0.3f);
            Render3D.drawQuad(entry, new Vec3(x1, y1, z1), new Vec3(x2, y1, z1), new Vec3(x2, y1, z2), new Vec3(x1, y1, z2), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3(x1, y1, z1), new Vec3(x1, y2, z1), new Vec3(x2, y2, z1), new Vec3(x2, y1, z1), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3(x2, y1, z1), new Vec3(x2, y2, z1), new Vec3(x2, y2, z2), new Vec3(x2, y1, z2), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3(x1, y1, z2), new Vec3(x2, y1, z2), new Vec3(x2, y2, z2), new Vec3(x1, y2, z2), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3(x1, y1, z1), new Vec3(x1, y1, z2), new Vec3(x1, y2, z2), new Vec3(x1, y2, z1), fillColor, depth);
            Render3D.drawQuad(entry, new Vec3(x1, y2, z1), new Vec3(x1, y2, z2), new Vec3(x2, y2, z2), new Vec3(x2, y2, z1), fillColor, depth);
        }
        if (line) {
            Render3D.drawLine(entry, x1, y1, z1, x2, y1, z1, color, width, depth);
            Render3D.drawLine(entry, x2, y1, z1, x2, y1, z2, color, width, depth);
            Render3D.drawLine(entry, x2, y1, z2, x1, y1, z2, color, width, depth);
            Render3D.drawLine(entry, x1, y1, z2, x1, y1, z1, color, width, depth);
            Render3D.drawLine(entry, x1, y1, z2, x1, y2, z2, color, width, depth);
            Render3D.drawLine(entry, x1, y1, z1, x1, y2, z1, color, width, depth);
            Render3D.drawLine(entry, x2, y1, z2, x2, y2, z2, color, width, depth);
            Render3D.drawLine(entry, x2, y1, z1, x2, y2, z1, color, width, depth);
            Render3D.drawLine(entry, x1, y2, z1, x2, y2, z1, color, width, depth);
            Render3D.drawLine(entry, x2, y2, z1, x2, y2, z2, color, width, depth);
            Render3D.drawLine(entry, x2, y2, z2, x1, y2, z2, color, width, depth);
            Render3D.drawLine(entry, x1, y2, z2, x1, y2, z1, color, width, depth);
        }
    }

    public static void drawLine(PoseStack.Pose entry, double minX, double minY, double minZ, double maxX, double maxY, double maxZ, int color, float width, boolean depth) {
        Render3D.drawLine(entry, new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ), color, color, width, depth);
    }

    public static void drawLine(Vec3 start, Vec3 end, int color, float width, boolean depth) {
        Render3D.drawLine(null, start, end, color, color, width, depth);
    }

    public static void drawLine(PoseStack.Pose entry, Vec3 start, Vec3 end, int colorStart, int colorEnd, float width, boolean depth) {
        Line line = new Line(entry, start, end, colorStart, colorEnd, width);
        if (depth) {
            LINE_DEPTH.add(line);
        } else {
            LINE.add(line);
        }
    }

    public static void drawQuad(Vec3 x, Vec3 y, Vec3 w, Vec3 z, int color, boolean depth) {
        Render3D.drawQuad(null, x, y, w, z, color, depth);
    }

    public static void drawQuad(PoseStack.Pose entry, Vec3 x, Vec3 y, Vec3 w, Vec3 z, int color, boolean depth) {
        Quad quad = new Quad(entry, x, y, w, z, color);
        if (depth) {
            QUAD_DEPTH.add(quad);
        } else {
            QUAD.add(quad);
        }
    }

    public static void resetCircleSmoothing() {
        smoothY = 0.0;
        smoothY2 = 0.0;
    }

    @Generated
    private Render3D() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static void setLastWorldSpaceEntry(PoseStack.Pose lastWorldSpaceEntry) {
        Render3D.lastWorldSpaceEntry = lastWorldSpaceEntry;
    }

    @Generated
    public static void setLastTickDelta(float lastTickDelta) {
        Render3D.lastTickDelta = lastTickDelta;
    }

    @Generated
    public static void setLastCameraPos(Vec3 lastCameraPos) {
        Render3D.lastCameraPos = lastCameraPos;
    }

    @Generated
    public static void setLastCameraRotation(Quaternionf lastCameraRotation) {
        Render3D.lastCameraRotation = lastCameraRotation;
    }

    static {
        smoothY = 0.0;
        smoothY2 = 0.0;
    }

    public record Line(PoseStack.Pose entry, Vec3 start, Vec3 end, int colorStart, int colorEnd, float width) {
    }

    public record Quad(PoseStack.Pose entry, Vec3 x, Vec3 y, Vec3 w, Vec3 z, int color) {
    }

    public record GradientQuad(Vec3 p1, Vec3 p2, Vec3 p3, Vec3 p4, int c1, int c2, int c3, int c4) {
    }
}

