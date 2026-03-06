/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package rich.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.impl.combat.Aura;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.animations.Animation;
import rich.util.animations.Direction;
import rich.util.animations.OutBack;
import rich.util.render.Render3D;
import rich.util.render.sliemtpipeline.ClientPipelines;
import rich.util.timer.StopWatch;

public class TargetESP
extends ModuleStructure
implements IMinecraft {
    private static TargetESP instance;
    private Animation espAnim = new OutBack().setMs(300).setValue(1.0);
    private StopWatch stopWatch = new StopWatch();
    private SelectSetting mode = new SelectSetting("Mode", "TargetESP type").value("Rhomb", "Ghost", "Chain", "Crystals", "Circle").selected("Rhomb");
    private SliderSettings crystalRotationSpeed = new SliderSettings("Crystal rotation speed", "Crystal rotation speed").range(0.1f, 2.0f).visible(() -> this.mode.isSelected("Crystals"));
    private ColorSetting color1 = new ColorSetting("Color 1", "First gradient color").setColor(new Color(255, 101, 57, 255).getRGB());
    private ColorSetting color2 = new ColorSetting("Color 2", "Second gradient color").setColor(new Color(255, 50, 150, 255).getRGB());
    private ColorSetting color3 = new ColorSetting("Color 3", "Third color for Ghost").setColor(new Color(150, 50, 255, 255).getRGB()).visible(() -> this.mode.isSelected("Ghost"));
    private Vec3 smoothedPos = null;
    private LivingEntity lastTarget = null;
    private float movingValue = 0.0f;
    private float hurtProgress = 0.0f;
    private Entity lastRenderedTarget = null;
    private final List<Crystal> crystalList = new ArrayList<Crystal>();
    private float rotationAngle = 0.0f;
    private long lastFrameTime = System.currentTimeMillis();
    private static final float TARGET_FPS = 60.0f;
    private static final float TARGET_FRAME_TIME = 16.666666f;

    public static TargetESP getInstance() {
        return instance;
    }

    public TargetESP() {
        super("TargetEsp", "Target Esp", ModuleCategory.RENDER);
        instance = this;
        this.crystalRotationSpeed.setValue(0.5f);
        this.settings(this.mode, this.crystalRotationSpeed, this.color1, this.color2, this.color3);
    }

    private float getDeltaTime() {
        long currentTime = System.currentTimeMillis();
        float deltaMs = currentTime - this.lastFrameTime;
        this.lastFrameTime = currentTime;
        deltaMs = Math.max(1.0f, Math.min(deltaMs, 100.0f));
        return deltaMs / 16.666666f;
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent e) {
        float deltaTime = this.getDeltaTime();
        LivingEntity target = null;
        if (Aura.getInstance() != null && Aura.getInstance().isState()) {
            target = Aura.target;
        }
        if (target == null) {
            this.smoothedPos = null;
            this.lastTarget = null;
            this.espAnim.setDirection(Direction.BACKWARDS);
            Render3D.resetCircleSmoothing();
            return;
        }
        this.espAnim.setDirection(Direction.FORWARDS);
        float alpha = this.espAnim.getOutput().floatValue();
        if (alpha <= 0.01f) {
            return;
        }
        this.movingValue += 2.0f * deltaTime;
        if (this.movingValue > 360000.0f) {
            this.movingValue = 0.0f;
        }
        float hurtDecay = 0.1f * deltaTime;
        this.hurtProgress = target.hurtTime > 0 ? (float)target.hurtTime / 10.0f : Math.max(0.0f, this.hurtProgress - hurtDecay);
        Render3D.updateTargetEsp(deltaTime);
        if (this.mode.isSelected("Circle")) {
            this.renderCircle(e.getStack(), target, alpha);
            return;
        }
        PoseStack stack = e.getStack();
        MultiBufferSource.BufferSource provider = mc.renderBuffers().bufferSource();
        Vec3 camPos = TargetESP.mc.gameRenderer.getMainCamera().position();
        float partialTicks = e.getPartialTicks();
        Vec3 targetPos = target.getPosition(partialTicks);
        if (this.lastTarget != target || this.smoothedPos == null) {
            this.smoothedPos = targetPos;
            this.lastTarget = target;
        } else {
            float smoothingFactor = Math.min(1.0f, partialTicks * 1.5f);
            double dx = targetPos.x - this.smoothedPos.x;
            double dy = targetPos.y - this.smoothedPos.y;
            double dz = targetPos.z - this.smoothedPos.z;
            this.smoothedPos = new Vec3(this.smoothedPos.x + dx * (double)smoothingFactor, this.smoothedPos.y + dy * (double)smoothingFactor, this.smoothedPos.z + dz * (double)smoothingFactor);
        }
        stack.pushPose();
        stack.translate(this.smoothedPos.x - camPos.x, this.smoothedPos.y - camPos.y, this.smoothedPos.z - camPos.z);
        if (this.mode.isSelected("Rhomb")) {
            this.renderRhomb(stack, provider, target, alpha);
        } else if (this.mode.isSelected("Ghost")) {
            this.renderGhost(stack, provider, target, alpha);
        } else if (this.mode.isSelected("Chain")) {
            this.renderChain(stack, provider, target, alpha, deltaTime);
        } else if (this.mode.isSelected("Crystals")) {
            if (this.crystalList.isEmpty() || this.lastRenderedTarget != target) {
                this.createCrystals(target);
                this.lastRenderedTarget = target;
            }
            this.renderCrystals(stack, provider, target, alpha, deltaTime);
        }
        provider.endBatch();
        stack.popPose();
    }

    private void renderCircle(PoseStack stack, LivingEntity target, float alpha) {
        int baseColor1 = this.color1.getColor();
        int baseColor2 = this.color2.getColor();
        if (this.hurtProgress > 0.0f) {
            baseColor1 = this.lerpColor(baseColor1, -65536, this.hurtProgress);
            baseColor2 = this.lerpColor(baseColor2, -65536, this.hurtProgress);
        }
        Render3D.drawCircle(stack, target, alpha, this.hurtProgress, baseColor1, baseColor2);
    }

    private void renderChain(PoseStack stack, MultiBufferSource provider, LivingEntity target, float alpha, float deltaTime) {
        VertexConsumer consumer = provider.getBuffer(ClientPipelines.CHAIN_ESP.apply(Identifier.fromNamespaceAndPath((String)"rich", (String)"images/world/chain.png")));
        float animValue = (float)(System.currentTimeMillis() % 360000L) / 1000.0f * 60.0f;
        float gradusX = (float)(20.0 * Math.min(1.0 + Math.sin(Math.toRadians(animValue)), 1.0));
        float gradusZ = (float)(20.0 * (Math.min(1.0 + Math.sin(Math.toRadians(animValue)), 2.0) - 1.0));
        float width = target.getBbWidth() * 3.0f;
        int linksStep = 18;
        int totalAngle = 720;
        float chainSizeVal = 8.0f;
        float down = 1.5f;
        float chainScale = 0.5f;
        int alphaVal = Mth.clamp((int)((int)(alpha * 128.0f)), (int)0, (int)128);
        int baseColor1 = this.color1.getColor();
        int baseColor2 = this.color2.getColor();
        if (this.hurtProgress > 0.0f) {
            baseColor1 = this.lerpColor(baseColor1, -65536, this.hurtProgress);
            baseColor2 = this.lerpColor(baseColor2, -65536, this.hurtProgress);
        }
        int c1 = this.withAlpha(baseColor1, alphaVal);
        int c2 = this.withAlpha(baseColor2, alphaVal);
        float rotationValue = (float)(System.currentTimeMillis() % 720000L) / 1000.0f * 30.0f;
        for (int chain = 0; chain < 2; ++chain) {
            float val = 1.2f - 0.5f * (chain == 0 ? 1.0f : 0.9f);
            stack.pushPose();
            stack.translate(0.0f, target.getBbHeight() / 2.0f, 0.0f);
            stack.scale(chainScale, chainScale, chainScale);
            stack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(chain == 0 ? gradusX : -gradusX));
            stack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(chain == 0 ? gradusZ : -gradusZ));
            float x = 0.0f;
            float y = -0.5f;
            float z = 0.0f;
            Matrix4f matrix = stack.last().pose();
            int modif = linksStep / 2;
            for (int i = 0; i < totalAngle; i += modif) {
                float offsetX = (chain == 0 ? gradusX : -gradusX) / 100.0f;
                float offsetZ = (chain == 0 ? -gradusZ : gradusZ) / 100.0f;
                float prevSin = (float)((double)(x + offsetX) + Math.sin(Math.toRadians((float)(i - modif) + rotationValue)) * (double)width * (double)val);
                float prevCos = (float)((double)(z + offsetZ) + Math.cos(Math.toRadians((float)(i - modif) + rotationValue)) * (double)width * (double)val);
                float sin = (float)((double)(x + offsetX) + Math.sin(Math.toRadians((float)i + rotationValue)) * (double)width * (double)val);
                float cos = (float)((double)(z + offsetZ) + Math.cos(Math.toRadians((float)i + rotationValue)) * (double)width * (double)val);
                float u0 = 0.0027777778f * (float)(i - modif) * chainSizeVal;
                float u1 = 0.0027777778f * (float)i * chainSizeVal;
                consumer.addVertex((Matrix4fc)matrix, prevSin, y, prevCos).setUv(u0, 0.0f).setColor(c1);
                consumer.addVertex((Matrix4fc)matrix, sin, y, cos).setUv(u1, 0.0f).setColor(c1);
                consumer.addVertex((Matrix4fc)matrix, sin, y + down, cos).setUv(u1, 0.99f).setColor(c2);
                consumer.addVertex((Matrix4fc)matrix, prevSin, y + down, prevCos).setUv(u0, 0.99f).setColor(c2);
            }
            stack.popPose();
        }
    }

    private void renderRhomb(PoseStack stack, MultiBufferSource provider, LivingEntity target, float alpha) {
        VertexConsumer consumer = provider.getBuffer(ClientPipelines.ROMB_ESP.apply(Identifier.fromNamespaceAndPath((String)"rich", (String)"images/world/cube.png")));
        Quaternionf camRot = TargetESP.mc.gameRenderer.getMainCamera().rotation();
        stack.translate(0.0f, target.getBbHeight() / 2.0f, 0.0f);
        stack.mulPose((Quaternionfc)camRot);
        float timeRotation = (float)(System.currentTimeMillis() % 6283L) / 1000.0f;
        stack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)Math.sin(timeRotation) * 360.0f));
        float size = 0.5f;
        stack.scale(size, size, 1.0f);
        int c1 = this.withAlpha(this.color1.getColor(), (int)(255.0f * alpha));
        int c2 = this.withAlpha(this.color2.getColor(), (int)(255.0f * alpha));
        Vector3f[] quad = new Vector3f[]{new Vector3f(-1.0f, -1.0f, 0.0f), new Vector3f(-1.0f, 1.0f, 0.0f), new Vector3f(1.0f, 1.0f, 0.0f), new Vector3f(1.0f, -1.0f, 0.0f)};
        PoseStack.Pose m = stack.last();
        consumer.addVertex(m, quad[0].x, quad[0].y, 0.0f).setUv(0.0f, 0.0f).setColor(c2);
        consumer.addVertex(m, quad[1].x, quad[1].y, 0.0f).setUv(0.0f, 1.0f).setColor(c1);
        consumer.addVertex(m, quad[2].x, quad[2].y, 0.0f).setUv(1.0f, 1.0f).setColor(c2);
        consumer.addVertex(m, quad[3].x, quad[3].y, 0.0f).setUv(1.0f, 0.0f).setColor(c1);
    }

    private void renderGhost(PoseStack stack, MultiBufferSource consumers, LivingEntity target, float alpha) {
        VertexConsumer consumer = consumers.getBuffer(ClientPipelines.GHOSTS_ESP.apply(Identifier.fromNamespaceAndPath((String)"rich", (String)"images/particle/ghost-glow.png")));
        stack.translate(0.0f, target.getBbHeight() * 0.5f, 0.0f);
        this.particle(stack, consumer, (sin, cos) -> new Vec3(sin, cos, -cos), alpha, 0);
        this.particle(stack, consumer, (sin, cos) -> new Vec3(-sin, sin, -cos), alpha, 1);
        this.particle(stack, consumer, (sin, cos) -> new Vec3(-sin, -sin, cos), alpha, 2);
    }

    private void particle(PoseStack stack, VertexConsumer consumer, Transformation transformation, float alpha, int colorIndex) {
        double radius = 0.7f;
        double distance = 11.0;
        float particleSize = 0.5f;
        int alphaFactor = 15;
        long elapsed = System.currentTimeMillis();
        int baseColor = switch (colorIndex) {
            case 0 -> this.color1.getColor();
            case 1 -> this.color2.getColor();
            default -> this.color3.getColor();
        };
        int i = 0;
        while ((float)i < 40.0f * alpha) {
            stack.pushPose();
            double angle = 0.15 * ((double)elapsed * 0.5 - (double)i * distance) / 30.0;
            double sin = Math.sin(angle) * radius;
            double cos = Math.cos(angle) * radius;
            Vec3 trans = transformation.make(sin, cos);
            stack.translate(trans.x, trans.y, trans.z);
            stack.mulPose((Quaternionfc)TargetESP.mc.gameRenderer.getMainCamera().rotation());
            float spinRotation = (float)elapsed * 0.1f - (float)i * 10.0f;
            stack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(spinRotation));
            stack.translate(particleSize / 2.0f, particleSize / 2.0f, 0.0f);
            float x = (float)i / 40.0f;
            int lerpedColor = this.lerpColor(baseColor, this.getNextColor(colorIndex), x);
            int c1 = this.withAlpha(lerpedColor, (int)((float)(255 - i * alphaFactor) * alpha));
            int c2 = this.withAlpha(lerpedColor, (int)((float)(255 - i * alphaFactor) * alpha));
            PoseStack.Pose m = stack.last();
            consumer.addVertex(m, 0.0f, -particleSize, 0.0f).setUv(0.0f, 0.0f).setColor(c2);
            consumer.addVertex(m, -particleSize, -particleSize, 0.0f).setUv(0.0f, 1.0f).setColor(c1);
            consumer.addVertex(m, -particleSize, 0.0f, 0.0f).setUv(1.0f, 1.0f).setColor(c2);
            consumer.addVertex(m, 0.0f, 0.0f, 0.0f).setUv(1.0f, 0.0f).setColor(c1);
            stack.popPose();
            ++i;
        }
    }

    private void createCrystals(Entity target) {
        this.crystalList.clear();
        this.crystalList.add(new Crystal(new Vec3(0.0, 0.85, 0.8), new Vec3(-49.0, 0.0, 40.0)));
        this.crystalList.add(new Crystal(new Vec3(0.2, 0.85, -0.675), new Vec3(35.0, 0.0, -30.0)));
        this.crystalList.add(new Crystal(new Vec3(0.6, 1.35, 0.6), new Vec3(-30.0, 0.0, 35.0)));
        this.crystalList.add(new Crystal(new Vec3(-0.74, 1.05, 0.4), new Vec3(-25.0, 0.0, -30.0)));
        this.crystalList.add(new Crystal(new Vec3(0.74, 0.95, -0.4), new Vec3(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3(-0.475, 0.85, -0.375), new Vec3(30.0, 0.0, -25.0)));
        this.crystalList.add(new Crystal(new Vec3(0.0, 1.35, -0.6), new Vec3(45.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3(0.85, 0.7, 0.1), new Vec3(-30.0, 0.0, 30.0)));
        this.crystalList.add(new Crystal(new Vec3(-0.7, 1.35, -0.3), new Vec3(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3(-0.3, 1.35, 0.55), new Vec3(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3(-0.5, 0.7, 0.7), new Vec3(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3(0.5, 0.7, 0.7), new Vec3(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3(-0.7, 0.75, 0.0), new Vec3(0.0, 0.0, 0.0)));
        this.crystalList.add(new Crystal(new Vec3(-0.2, 0.65, -0.7), new Vec3(0.0, 0.0, 0.0)));
    }

    private void renderCrystals(PoseStack stack, MultiBufferSource provider, LivingEntity target, float alpha, float deltaTime) {
        if (target == null || this.crystalList.isEmpty()) {
            return;
        }
        this.rotationAngle += this.crystalRotationSpeed.getValue() * deltaTime;
        this.rotationAngle %= 360.0f;
        stack.pushPose();
        stack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(this.rotationAngle));
        int baseColor = this.color1.getColor();
        if (this.hurtProgress > 0.0f) {
            baseColor = this.lerpColor(baseColor, -65536, this.hurtProgress);
        }
        for (Crystal crystal : this.crystalList) {
            crystal.render(stack, provider, alpha, baseColor);
        }
        stack.popPose();
    }

    private int darkenColor(int color, float factor) {
        int a = color >> 24 & 0xFF;
        int r = (int)((float)(color >> 16 & 0xFF) * factor);
        int g = (int)((float)(color >> 8 & 0xFF) * factor);
        int b = (int)((float)(color & 0xFF) * factor);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private int lightenColor(int color, float factor) {
        int a = color >> 24 & 0xFF;
        int r = Math.min(255, (int)((float)(color >> 16 & 0xFF) * factor));
        int g = Math.min(255, (int)((float)(color >> 8 & 0xFF) * factor));
        int b = Math.min(255, (int)((float)(color & 0xFF) * factor));
        return a << 24 | r << 16 | g << 8 | b;
    }

    private int getNextColor(int colorIndex) {
        return switch (colorIndex) {
            case 0 -> this.color2.getColor();
            case 1 -> this.color3.getColor();
            default -> this.color1.getColor();
        };
    }

    private int lerpColor(int c1, int c2, float t) {
        int a1 = c1 >> 24 & 0xFF;
        int r1 = c1 >> 16 & 0xFF;
        int g1 = c1 >> 8 & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = c2 >> 24 & 0xFF;
        int r2 = c2 >> 16 & 0xFF;
        int g2 = c2 >> 8 & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (int)((float)a1 + (float)(a2 - a1) * t);
        int r = (int)((float)r1 + (float)(r2 - r1) * t);
        int g = (int)((float)g1 + (float)(g2 - g1) * t);
        int b = (int)((float)b1 + (float)(b2 - b1) * t);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private int withAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return color & 0xFFFFFF | alpha << 24;
    }

    @FunctionalInterface
    private static interface Transformation {
        public Vec3 make(double var1, double var3);
    }

    private class Crystal {
        private final Vec3 position;
        private final Vec3 rotation;
        private final float rotationSpeed;

        public Crystal(Vec3 position, Vec3 rotation) {
            this.position = position;
            this.rotation = rotation;
            this.rotationSpeed = 0.5f + (float)(Math.random() * 1.5);
        }

        public void render(PoseStack stack, MultiBufferSource provider, float alpha, int baseColor) {
            stack.pushPose();
            stack.translate(this.position.x, this.position.y, this.position.z);
            float timeSeconds = (float)(System.currentTimeMillis() % 31416L) / 1000.0f;
            float pulsation = 1.0f + (float)(Math.sin(timeSeconds * 2.0f) * (double)0.1f);
            stack.scale(pulsation, pulsation, pulsation);
            float selfRotation = (float)(System.currentTimeMillis() % 36000L) / 100.0f * this.rotationSpeed;
            stack.mulPose((Quaternionfc)Axis.XP.rotationDegrees((float)this.rotation.x));
            stack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)this.rotation.y + selfRotation));
            stack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)this.rotation.z));
            float userAlpha = 0.3f;
            VertexConsumer filledConsumer = provider.getBuffer(ClientPipelines.CRYSTAL_FILLED);
            this.drawFilledCrystal(stack, filledConsumer, baseColor, userAlpha * 0.85f, alpha);
            VertexConsumer glowConsumer = provider.getBuffer(ClientPipelines.CRYSTAL_GLOW);
            stack.pushPose();
            stack.scale(1.15f, 1.15f, 1.15f);
            this.drawFilledCrystal(stack, glowConsumer, baseColor, userAlpha * 0.25f, alpha);
            stack.popPose();
            stack.pushPose();
            stack.scale(1.3f, 1.3f, 1.3f);
            this.drawFilledCrystal(stack, glowConsumer, baseColor, userAlpha * 0.1f, alpha);
            stack.popPose();
            this.drawBloomEffect(stack, provider, baseColor, alpha);
            stack.popPose();
        }

        private void drawFilledCrystal(PoseStack stack, VertexConsumer consumer, int baseColor, float alphaMultiplier, float anim) {
            Vector3f v2;
            Vector3f v1;
            int i;
            float s = 0.05f;
            float h_prism = s * 1.0f;
            float h_pyramid = s * 1.5f;
            int numSides = 8;
            ArrayList<Vector3f> topVertices = new ArrayList<Vector3f>();
            ArrayList<Vector3f> bottomVertices = new ArrayList<Vector3f>();
            for (int i2 = 0; i2 < numSides; ++i2) {
                float angle = (float)(Math.PI * 2 * (double)i2 / (double)numSides);
                float x = (float)((double)s * Math.cos(angle));
                float z = (float)((double)s * Math.sin(angle));
                topVertices.add(new Vector3f(x, h_prism / 2.0f, z));
                bottomVertices.add(new Vector3f(x, -h_prism / 2.0f, z));
            }
            Vector3f vTop = new Vector3f(0.0f, h_prism / 2.0f + h_pyramid, 0.0f);
            Vector3f vBottom = new Vector3f(0.0f, -h_prism / 2.0f - h_pyramid, 0.0f);
            int finalAlpha = (int)(alphaMultiplier * 255.0f * anim);
            int finalColor = TargetESP.this.withAlpha(baseColor, finalAlpha);
            int darkerColor = TargetESP.this.withAlpha(TargetESP.this.darkenColor(baseColor, 0.7f), finalAlpha);
            int lighterColor = TargetESP.this.withAlpha(TargetESP.this.lightenColor(baseColor, 1.2f), finalAlpha);
            Matrix4f matrix = stack.last().pose();
            for (i = 0; i < numSides; ++i) {
                v1 = (Vector3f)bottomVertices.get(i);
                v2 = (Vector3f)bottomVertices.get((i + 1) % numSides);
                Vector3f v3 = (Vector3f)topVertices.get((i + 1) % numSides);
                Vector3f v4 = (Vector3f)topVertices.get(i);
                int sideColor = i % 2 == 0 ? finalColor : darkerColor;
                this.drawQuadFilled(matrix, consumer, v1, v2, v3, v4, sideColor);
            }
            for (i = 0; i < numSides; ++i) {
                v1 = (Vector3f)topVertices.get(i);
                v2 = (Vector3f)topVertices.get((i + 1) % numSides);
                int pyramidColor = i % 2 == 0 ? lighterColor : finalColor;
                this.drawTriangleFilled(matrix, consumer, vTop, v2, v1, pyramidColor);
            }
            for (i = 0; i < numSides; ++i) {
                v1 = (Vector3f)bottomVertices.get(i);
                v2 = (Vector3f)bottomVertices.get((i + 1) % numSides);
                int pyramidColor = i % 2 == 0 ? darkerColor : finalColor;
                this.drawTriangleFilled(matrix, consumer, vBottom, v1, v2, pyramidColor);
            }
        }

        private void drawTriangleFilled(Matrix4f matrix, VertexConsumer consumer, Vector3f v1, Vector3f v2, Vector3f v3, int color) {
            consumer.addVertex((Matrix4fc)matrix, v1.x, v1.y, v1.z).setColor(color);
            consumer.addVertex((Matrix4fc)matrix, v2.x, v2.y, v2.z).setColor(color);
            consumer.addVertex((Matrix4fc)matrix, v3.x, v3.y, v3.z).setColor(color);
            consumer.addVertex((Matrix4fc)matrix, v3.x, v3.y, v3.z).setColor(color);
        }

        private void drawQuadFilled(Matrix4f matrix, VertexConsumer consumer, Vector3f v1, Vector3f v2, Vector3f v3, Vector3f v4, int color) {
            consumer.addVertex((Matrix4fc)matrix, v1.x, v1.y, v1.z).setColor(color);
            consumer.addVertex((Matrix4fc)matrix, v2.x, v2.y, v2.z).setColor(color);
            consumer.addVertex((Matrix4fc)matrix, v3.x, v3.y, v3.z).setColor(color);
            consumer.addVertex((Matrix4fc)matrix, v4.x, v4.y, v4.z).setColor(color);
        }

        private void drawBloomEffect(PoseStack stack, MultiBufferSource provider, int baseColor, float anim) {
            Matrix4f matrix;
            float angle;
            int i;
            VertexConsumer bloomConsumer = provider.getBuffer(ClientPipelines.BLOOM_ESP.apply(Identifier.fromNamespaceAndPath((String)"rich", (String)"images/particle/glow.png")));
            int bloomAlpha = (int)(18.0f * anim);
            int bloomColor = TargetESP.this.withAlpha(baseColor, bloomAlpha);
            float bloomSize = 0.75f;
            Quaternionf camRot = IMinecraft.mc.gameRenderer.getMainCamera().rotation();
            int segments = 6;
            for (i = 0; i < segments; ++i) {
                stack.pushPose();
                angle = 360.0f / (float)segments * (float)i;
                stack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(angle));
                stack.mulPose((Quaternionfc)camRot);
                matrix = stack.last().pose();
                bloomConsumer.addVertex((Matrix4fc)matrix, -bloomSize / 2.0f, -bloomSize / 2.0f, 0.0f).setUv(0.0f, 1.0f).setColor(bloomColor);
                bloomConsumer.addVertex((Matrix4fc)matrix, bloomSize / 2.0f, -bloomSize / 2.0f, 0.0f).setUv(1.0f, 1.0f).setColor(bloomColor);
                bloomConsumer.addVertex((Matrix4fc)matrix, bloomSize / 2.0f, bloomSize / 2.0f, 0.0f).setUv(1.0f, 0.0f).setColor(bloomColor);
                bloomConsumer.addVertex((Matrix4fc)matrix, -bloomSize / 2.0f, bloomSize / 2.0f, 0.0f).setUv(0.0f, 0.0f).setColor(bloomColor);
                stack.popPose();
            }
            for (i = 0; i < segments; ++i) {
                stack.pushPose();
                angle = 360.0f / (float)segments * (float)i;
                stack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
                stack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(angle));
                stack.mulPose((Quaternionfc)camRot);
                matrix = stack.last().pose();
                bloomConsumer.addVertex((Matrix4fc)matrix, -bloomSize / 2.0f, -bloomSize / 2.0f, 0.0f).setUv(0.0f, 1.0f).setColor(bloomColor);
                bloomConsumer.addVertex((Matrix4fc)matrix, bloomSize / 2.0f, -bloomSize / 2.0f, 0.0f).setUv(1.0f, 1.0f).setColor(bloomColor);
                bloomConsumer.addVertex((Matrix4fc)matrix, bloomSize / 2.0f, bloomSize / 2.0f, 0.0f).setUv(1.0f, 0.0f).setColor(bloomColor);
                bloomConsumer.addVertex((Matrix4fc)matrix, -bloomSize / 2.0f, bloomSize / 2.0f, 0.0f).setUv(0.0f, 0.0f).setColor(bloomColor);
                stack.popPose();
            }
        }
    }
}

