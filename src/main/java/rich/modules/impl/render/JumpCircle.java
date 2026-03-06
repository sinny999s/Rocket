/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package rich.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.impl.JumpEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.ColorUtil;
import rich.util.render.sliemtpipeline.ClientPipelines;
import rich.util.timer.StopWatch;

public class JumpCircle
extends ModuleStructure
implements IMinecraft {
    private final List<Circle> circles = new ArrayList<Circle>();
    private final Identifier circleTexture = Identifier.fromNamespaceAndPath((String)"rich", (String)"images/circle/circle.png");
    private final Identifier glowTexture = Identifier.fromNamespaceAndPath((String)"rich", (String)"images/particle/glow.png");
    private final SliderSettings maxSize = new SliderSettings("Max Size", "Maximum circle size").setValue(2.0f).range(1.0f, 2.0f);
    private final SliderSettings speed = new SliderSettings("Speed", "Animation speed").setValue(2000.0f).range(1000.0f, 2000.0f);
    private final BooleanSetting glow = new BooleanSetting("Glow", "Glow effect").setValue(true);
    private final ColorSetting color1 = new ColorSetting("Color 1", "First color").value(ColorUtil.getColor(137, 97, 72, 255));
    private final ColorSetting color2 = new ColorSetting("Color 2", "Second color").value(ColorUtil.getColor(255, 255, 255, 255));
    private static final int SEGMENTS = 64;

    public JumpCircle() {
        super("JumpCircle", "Jump Circle", ModuleCategory.RENDER);
        this.settings(this.maxSize, this.speed, this.glow, this.color1, this.color2);
    }

    @EventHandler
    public void onJump(JumpEvent event) {
        if (JumpCircle.mc.player == null || event.getPlayer() != JumpCircle.mc.player) {
            return;
        }
        Vec3 pos = new Vec3(JumpCircle.mc.player.getX(), Math.floor(JumpCircle.mc.player.getY()) + 0.001, JumpCircle.mc.player.getZ());
        this.circles.add(new Circle(pos, new StopWatch()));
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        long maxTime = (long)this.speed.getValue();
        Iterator<Circle> iterator = this.circles.iterator();
        while (iterator.hasNext()) {
            Circle circle = iterator.next();
            if (circle.timer.elapsedTime() <= maxTime) continue;
            iterator.remove();
        }
        if (this.circles.isEmpty()) {
            return;
        }
        PoseStack matrices = e.getStack();
        MultiBufferSource.BufferSource immediate = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = JumpCircle.mc.gameRenderer.getMainCamera().position();
        for (Circle circle : this.circles) {
            this.renderSingleCircle(matrices, immediate, circle, cameraPos);
        }
        immediate.endBatch();
    }

    private void renderSingleCircle(PoseStack matrices, MultiBufferSource.BufferSource immediate, Circle circle, Vec3 cameraPos) {
        float alpha;
        float maxTime;
        float lifeTime = circle.timer.elapsedTime();
        float progress = Math.min(lifeTime / (maxTime = this.speed.getValue()), 1.0f);
        if (progress >= 1.0f) {
            return;
        }
        float easedProgress = this.bounceOut(progress);
        float scale = easedProgress * this.maxSize.getValue();
        float fadeInDuration = 0.15f;
        float glowStart = 0.65f;
        float fadeOutStart = 0.85f;
        if (progress < fadeInDuration) {
            alpha = progress / fadeInDuration;
        } else if (progress >= fadeOutStart) {
            float fadeOutProgress = (progress - fadeOutStart) / (1.0f - fadeOutStart);
            alpha = 1.0f - fadeOutProgress;
            if (progress > glowStart) {
                float glowProgress = (progress - glowStart) / (fadeOutStart - glowStart);
                float glowPulse = (float)(Math.sin((double)glowProgress * Math.PI * 3.0) * 0.3 + 0.3);
                alpha += glowPulse * (1.0f - fadeOutProgress);
            }
        } else if (progress > glowStart) {
            float glowProgress = (progress - glowStart) / (fadeOutStart - glowStart);
            float glowPulse = (float)(Math.sin((double)glowProgress * Math.PI * 3.0) * 0.3 + 0.3);
            alpha = 1.0f + glowPulse;
        } else {
            alpha = 1.0f;
        }
        alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        float rotationOffset = lifeTime / 1000.0f * 0.5f * 360.0f;
        Vec3 circlePos = circle.pos();
        if (this.glow.isValue()) {
            this.renderGradientGlow(matrices, immediate, circlePos, scale, alpha * 0.1f, rotationOffset, cameraPos);
        }
        this.renderGradientCircle(matrices, immediate, circlePos, scale, alpha, rotationOffset, cameraPos);
    }

    private void renderGradientCircle(PoseStack matrices, MultiBufferSource.BufferSource immediate, Vec3 pos, float size, float alpha, float rotationOffset, Vec3 cameraPos) {
        VertexConsumer buffer = immediate.getBuffer(ClientPipelines.BLOOM_ESP.apply(this.circleTexture));
        matrices.pushPose();
        float x = (float)(pos.x - cameraPos.x);
        float y = (float)(pos.y - cameraPos.y);
        float z = (float)(pos.z - cameraPos.z);
        matrices.translate(x, y, z);
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
        Matrix4f matrix = matrices.last().pose();
        float radius = size / 2.0f;
        int c1 = this.color1.getColor();
        int c2 = this.color2.getColor();
        for (int i = 0; i < 64; ++i) {
            float angle1 = (float)(Math.PI * 2 * (double)i / 64.0);
            float angle2 = (float)(Math.PI * 2 * (double)(i + 1) / 64.0);
            float t = (float)i / 64.0f;
            float tNext = (float)(i + 1) / 64.0f;
            float adjustedT = (t + rotationOffset / 360.0f) % 1.0f;
            float adjustedTNext = (tNext + rotationOffset / 360.0f) % 1.0f;
            int currentColor = this.getGradientColor(c1, c2, adjustedT, alpha);
            int nextColor = this.getGradientColor(c1, c2, adjustedTNext, alpha);
            float x1 = (float)(Math.cos(angle1) * (double)radius);
            float z1 = (float)(Math.sin(angle1) * (double)radius);
            float x2 = (float)(Math.cos(angle2) * (double)radius);
            float z2 = (float)(Math.sin(angle2) * (double)radius);
            float u1 = (float)(0.5 + 0.5 * Math.cos(angle1));
            float v1 = (float)(0.5 + 0.5 * Math.sin(angle1));
            float u2 = (float)(0.5 + 0.5 * Math.cos(angle2));
            float v2 = (float)(0.5 + 0.5 * Math.sin(angle2));
            int centerColor = ColorUtil.lerpColor(currentColor, nextColor, 0.5f);
            buffer.addVertex((Matrix4fc)matrix, 0.0f, 0.0f, 0.0f).setUv(0.5f, 0.5f).setColor(centerColor);
            buffer.addVertex((Matrix4fc)matrix, x1, z1, 0.0f).setUv(u1, v1).setColor(currentColor);
            buffer.addVertex((Matrix4fc)matrix, x2, z2, 0.0f).setUv(u2, v2).setColor(nextColor);
            buffer.addVertex((Matrix4fc)matrix, x2, z2, 0.0f).setUv(u2, v2).setColor(nextColor);
        }
        matrices.popPose();
    }

    private void renderGradientGlow(PoseStack matrices, MultiBufferSource.BufferSource immediate, Vec3 pos, float scale, float alpha, float rotationOffset, Vec3 cameraPos) {
        int c1 = this.color1.getColor();
        int c2 = this.color2.getColor();
        for (int layer = 0; layer < 3; ++layer) {
            float layerScale = scale * (1.3f + (float)layer * 0.4f);
            float layerAlpha = alpha * (0.35f - (float)layer * 0.1f);
            this.renderGlowLayer(matrices, immediate, pos, layerScale, layerAlpha, rotationOffset, c1, c2, cameraPos);
        }
        float coreAlpha = alpha * 0.2f;
        int coreColor1 = ColorUtil.multAlpha(c1, coreAlpha);
        int coreColor2 = ColorUtil.multAlpha(c2, coreAlpha);
        int mixedCore = ColorUtil.lerpColor(coreColor1, coreColor2, 0.5f);
        this.renderTexturedQuad(matrices, immediate, pos, scale * 2.5f, mixedCore, this.glowTexture, cameraPos);
    }

    private void renderGlowLayer(PoseStack matrices, MultiBufferSource.BufferSource immediate, Vec3 pos, float size, float alpha, float rotationOffset, int c1, int c2, Vec3 cameraPos) {
        VertexConsumer buffer = immediate.getBuffer(ClientPipelines.BLOOM_ESP.apply(this.glowTexture));
        int glowSegments = 16;
        float radius = size / 2.0f;
        for (int i = 0; i < glowSegments; ++i) {
            float angle = (float)(Math.PI * 2 * (double)i / (double)glowSegments);
            float t = (float)i / (float)glowSegments;
            float adjustedT = (t + rotationOffset / 360.0f) % 1.0f;
            int glowColor = this.getGradientColor(c1, c2, adjustedT, alpha);
            float glowX = (float)(pos.x + Math.cos(angle) * (double)radius * (double)0.8f);
            float glowZ = (float)(pos.z + Math.sin(angle) * (double)radius * (double)0.8f);
            Vec3 glowPos = new Vec3(glowX, pos.y, glowZ);
            float glowSize = size * 0.4f;
            this.renderTexturedQuadAtPos(matrices, buffer, glowPos, glowSize, glowColor, cameraPos);
        }
    }

    private void renderTexturedQuadAtPos(PoseStack matrices, VertexConsumer buffer, Vec3 pos, float size, int color, Vec3 cameraPos) {
        matrices.pushPose();
        float x = (float)(pos.x - cameraPos.x);
        float y = (float)(pos.y - cameraPos.y);
        float z = (float)(pos.z - cameraPos.z);
        matrices.translate(x, y, z);
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
        Matrix4f matrix = matrices.last().pose();
        float half = size / 2.0f;
        buffer.addVertex((Matrix4fc)matrix, -half, -half, 0.0f).setUv(0.0f, 0.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix, half, -half, 0.0f).setUv(1.0f, 0.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix, half, half, 0.0f).setUv(1.0f, 1.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix, -half, half, 0.0f).setUv(0.0f, 1.0f).setColor(color);
        matrices.popPose();
    }

    private int getGradientColor(int c1, int c2, float t, float alpha) {
        float gradientT = t <= 0.5f ? t * 2.0f : (1.0f - t) * 2.0f;
        int color = ColorUtil.lerpColor(c1, c2, gradientT);
        return ColorUtil.multAlpha(color, alpha);
    }

    private void renderTexturedQuad(PoseStack matrices, MultiBufferSource.BufferSource immediate, Vec3 pos, float size, int color, Identifier texture, Vec3 cameraPos) {
        VertexConsumer buffer = immediate.getBuffer(ClientPipelines.BLOOM_ESP.apply(texture));
        matrices.pushPose();
        float x = (float)(pos.x - cameraPos.x);
        float y = (float)(pos.y - cameraPos.y);
        float z = (float)(pos.z - cameraPos.z);
        matrices.translate(x, y, z);
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
        Matrix4f matrix = matrices.last().pose();
        float half = size / 2.0f;
        buffer.addVertex((Matrix4fc)matrix, -half, -half, 0.0f).setUv(0.0f, 0.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix, half, -half, 0.0f).setUv(1.0f, 0.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix, half, half, 0.0f).setUv(1.0f, 1.0f).setColor(color);
        buffer.addVertex((Matrix4fc)matrix, -half, half, 0.0f).setUv(0.0f, 1.0f).setColor(color);
        matrices.popPose();
    }

    private float bounceOut(float value) {
        float n1 = 7.5625f;
        float d1 = 2.75f;
        if (value < 1.0f / d1) {
            return n1 * value * value;
        }
        if (value < 2.0f / d1) {
            return n1 * (value -= 1.5f / d1) * value + 0.75f;
        }
        if (value < 2.5f / d1) {
            return n1 * (value -= 2.25f / d1) * value + 0.9375f;
        }
        return n1 * (value -= 2.625f / d1) * value + 0.984375f;
    }

    public record Circle(Vec3 pos, StopWatch timer) {
    }
}

