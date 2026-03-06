/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package rich.modules.impl.render.particles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import rich.IMinecraft;
import rich.modules.impl.render.worldparticles.ParticleRenderer;
import rich.util.ColorUtil;
import rich.util.animations.Animation;
import rich.util.animations.Direction;
import rich.util.animations.EaseInOutQuad;
import rich.util.render.sliemtpipeline.ClientPipelines;

public class Particle3D
implements IMinecraft {
    private static final ParticleMode[] RANDOM_MODES = new ParticleMode[]{ParticleMode.CUBES, ParticleMode.CROWN, ParticleMode.CUBE_BLAST, ParticleMode.DOLLAR, ParticleMode.HEART, ParticleMode.LIGHTNING, ParticleMode.LINE, ParticleMode.RHOMBUS, ParticleMode.SNOWFLAKE, ParticleMode.STAR, ParticleMode.STAR_ALT, ParticleMode.TRIANGLE};
    private static final Identifier TEXTURE_CROWN = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/crown.png");
    private static final Identifier TEXTURE_CUBE_BLAST = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/cubeblast1.png");
    private static final Identifier TEXTURE_DOLLAR = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/dollar.png");
    private static final Identifier TEXTURE_HEART = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/heart.png");
    private static final Identifier TEXTURE_LIGHTNING = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/lightning.png");
    private static final Identifier TEXTURE_LINE = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/line.png");
    private static final Identifier TEXTURE_RHOMBUS = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/rhombus.png");
    private static final Identifier TEXTURE_SNOWFLAKE = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/snowflake.png");
    private static final Identifier TEXTURE_STAR = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/star.png");
    private static final Identifier TEXTURE_STAR_ALT = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/star1.png");
    private static final Identifier TEXTURE_TRIANGLE = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/triangle.png");
    private static final Identifier GLOW_BLOOM = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/dashbloom.png");
    private static final Identifier GLOW_BLOOM_SAMPLE = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/dashbloomsample.png");
    private double x;
    private double y;
    private double z;
    private double lastX;
    private double lastY;
    private double lastZ;
    private double velocityX;
    private double velocityY;
    private double velocityZ;
    private long start;
    private float phase;
    private int color;
    private float scale;
    private long lifeTimeMs;
    private float rotation;
    private Animation fadeInAnimation;
    private Animation fadeOutAnimation;
    private float cachedAlpha = 0.0f;
    private long lastAlphaUpdate = 0L;
    private boolean fadingOut = false;
    private float gravityStrength = 0.04f;
    private float velocityMultiplier = 0.98f;
    private boolean collidesWithWorld = true;
    private ParticleMode mode = ParticleMode.CUBES;
    private ParticleMode actualMode = ParticleMode.CUBES;
    private GlowMode glowMode = GlowMode.BOTH;
    private boolean spinning = true;

    public Particle3D(Vec3 pos, Vec3 velocity, int color, float scale, float maxAgeSeconds) {
        this.start = System.currentTimeMillis();
        this.phase = (float)(Math.random() * 100.0);
        this.rotation = (float)(Math.random() * 360.0);
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.lastX = pos.x;
        this.lastY = pos.y;
        this.lastZ = pos.z;
        this.velocityX = velocity.x;
        this.velocityY = velocity.y;
        this.velocityZ = velocity.z;
        this.color = color;
        this.scale = scale;
        this.lifeTimeMs = (long)(maxAgeSeconds * 1000.0f);
        this.fadeInAnimation = new EaseInOutQuad().setMs(150).setValue(1.0);
        this.fadeInAnimation.setDirection(Direction.FORWARDS);
        this.fadeOutAnimation = new EaseInOutQuad().setMs(250).setValue(1.0);
        this.fadeOutAnimation.setDirection(Direction.FORWARDS);
    }

    public Particle3D setGravity(float gravity) {
        this.gravityStrength = gravity;
        return this;
    }

    public Particle3D setVelocityMultiplier(float mult) {
        this.velocityMultiplier = mult;
        return this;
    }

    public Particle3D setCollision(boolean collision) {
        this.collidesWithWorld = collision;
        return this;
    }

    public Particle3D setMode(ParticleMode mode) {
        this.mode = mode;
        this.actualMode = mode == ParticleMode.RANDOM ? RANDOM_MODES[ThreadLocalRandom.current().nextInt(RANDOM_MODES.length)] : mode;
        return this;
    }

    public Particle3D setGlowMode(GlowMode glowMode) {
        this.glowMode = glowMode;
        return this;
    }

    public Particle3D setSpinning(boolean spinning) {
        this.spinning = spinning;
        return this;
    }

    public void update() {
        long now = System.currentTimeMillis();
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.velocityY -= (double)this.gravityStrength;
        if (this.collidesWithWorld && Particle3D.mc.level != null) {
            if (this.isHit(this.x + this.velocityX, this.y, this.z)) {
                this.velocityX *= -0.8;
            } else {
                this.x += this.velocityX;
            }
            if (this.isHit(this.x, this.y + this.velocityY, this.z)) {
                this.velocityX *= 0.999;
                this.velocityZ *= 0.999;
                this.velocityY *= -0.7;
            } else {
                this.y += this.velocityY;
            }
            if (this.isHit(this.x, this.y, this.z + this.velocityZ)) {
                this.velocityZ *= -0.8;
            } else {
                this.z += this.velocityZ;
            }
        } else {
            this.x += this.velocityX;
            this.y += this.velocityY;
            this.z += this.velocityZ;
        }
        this.velocityX /= 0.999999;
        this.velocityZ /= 0.999999;
        if (this.spinning) {
            this.rotation += 2.0f;
        }
        if (!this.fadingOut && now - this.start > this.lifeTimeMs) {
            this.fadingOut = true;
            this.fadeOutAnimation.setDirection(Direction.BACKWARDS);
        }
        if (now - this.lastAlphaUpdate > 16L) {
            this.cachedAlpha = this.fadingOut ? this.fadeOutAnimation.getOutput().floatValue() : this.fadeInAnimation.getOutput().floatValue();
            this.lastAlphaUpdate = now;
        }
    }

    private boolean isHit(double px, double py, double pz) {
        if (Particle3D.mc.level == null) {
            return false;
        }
        BlockPos pos = BlockPos.containing((double)px, (double)py, (double)pz);
        return Particle3D.mc.level.getBlockState(pos).isCollisionShapeFullBlock(Particle3D.mc.level, pos);
    }

    public boolean isDead() {
        return this.fadingOut && this.cachedAlpha <= 0.0f;
    }

    public float getAlpha() {
        return this.cachedAlpha;
    }

    private Identifier getTexture() {
        return switch (this.actualMode.ordinal()) {
            case 1 -> TEXTURE_CROWN;
            case 2 -> TEXTURE_CUBE_BLAST;
            case 3 -> TEXTURE_DOLLAR;
            case 4 -> TEXTURE_HEART;
            case 5 -> TEXTURE_LIGHTNING;
            case 6 -> TEXTURE_LINE;
            case 7 -> TEXTURE_RHOMBUS;
            case 8 -> TEXTURE_SNOWFLAKE;
            case 9 -> TEXTURE_STAR;
            case 10 -> TEXTURE_STAR_ALT;
            case 11 -> TEXTURE_TRIANGLE;
            default -> null;
        };
    }

    public void render(PoseStack matrices, MultiBufferSource immediate, float glowSize, float partialTicks) {
        float alpha = this.getAlpha();
        if (alpha <= 0.0f) {
            return;
        }
        Vec3 cameraPos = Particle3D.mc.gameRenderer.getMainCamera().position();
        float cameraYaw = Particle3D.mc.gameRenderer.getMainCamera().yRot();
        float cameraPitch = Particle3D.mc.gameRenderer.getMainCamera().xRot();
        double interpX = Mth.lerp((double)partialTicks, (double)this.lastX, (double)this.x);
        double interpY = Mth.lerp((double)partialTicks, (double)this.lastY, (double)this.y);
        double interpZ = Mth.lerp((double)partialTicks, (double)this.lastZ, (double)this.z);
        float relX = (float)(interpX - cameraPos.x);
        float relY = (float)(interpY - cameraPos.y);
        float relZ = (float)(interpZ - cameraPos.z);
        if (this.actualMode == ParticleMode.CUBES) {
            this.renderCube(matrices, immediate, relX, relY, relZ, alpha, glowSize, cameraYaw, cameraPitch);
        } else {
            this.renderTextured(matrices, immediate, relX, relY, relZ, alpha, glowSize, cameraYaw, cameraPitch);
        }
    }

    private void renderCube(PoseStack matrices, MultiBufferSource immediate, float relX, float relY, float relZ, float alpha, float glowSize, float cameraYaw, float cameraPitch) {
        long now = System.currentTimeMillis();
        float rotationAnim = (float)(now % 9000L) / 9000.0f * 360.0f;
        float alpha02 = alpha * 0.2f;
        float alpha04 = alpha * 0.4f;
        int glowCol = ColorUtil.multAlpha(this.color, alpha);
        float size = this.scale * 0.25f;
        float cubeGlow1 = size * glowSize;
        float cubeGlow2 = size * (glowSize / 3.0f);
        float rotY = rotationAnim + this.phase;
        float rotX = rotationAnim * 0.5f;
        matrices.pushPose();
        matrices.translate(relX, relY, relZ);
        matrices.mulPose((Quaternionfc)Axis.YP.rotationDegrees(rotY));
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(rotX));
        Matrix4f mat = matrices.last().pose();
        ParticleRenderer.drawCube(immediate.getBuffer(ParticleRenderer.getQuadsLayer()), mat, ColorUtil.multAlpha(this.color, alpha02), size);
        ParticleRenderer.drawLines(immediate.getBuffer(ParticleRenderer.getLinesLayer()), mat, ColorUtil.multAlpha(this.color, alpha04), size);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(relX, relY, relZ);
        matrices.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-cameraYaw));
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(cameraPitch));
        Matrix4f gMat = matrices.last().pose();
        this.renderGlowEffect(immediate, gMat, glowCol, alpha, cubeGlow1, cubeGlow2);
        matrices.popPose();
    }

    private void renderTextured(PoseStack matrices, MultiBufferSource immediate, float relX, float relY, float relZ, float alpha, float glowSize, float cameraYaw, float cameraPitch) {
        Identifier texture = this.getTexture();
        if (texture == null) {
            return;
        }
        int glowCol = ColorUtil.multAlpha(this.color, alpha);
        float size = this.scale * 0.5f;
        int r = glowCol >> 16 & 0xFF;
        int g = glowCol >> 8 & 0xFF;
        int b = glowCol & 0xFF;
        int a = (int)(255.0f * alpha);
        matrices.pushPose();
        matrices.translate(relX, relY, relZ);
        matrices.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-cameraYaw));
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(cameraPitch));
        if (this.spinning) {
            matrices.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(this.rotation));
        }
        Matrix4f mat = matrices.last().pose();
        RenderType layer = ClientPipelines.WORLD_PARTICLES_GLOW.apply(texture);
        VertexConsumer buffer = immediate.getBuffer(layer);
        float half = size / 2.0f;
        buffer.addVertex((Matrix4fc)mat, -half, -half, 0.0f).setUv(0.0f, 0.0f).setColor(r, g, b, a);
        buffer.addVertex((Matrix4fc)mat, -half, half, 0.0f).setUv(0.0f, 1.0f).setColor(r, g, b, a);
        buffer.addVertex((Matrix4fc)mat, half, half, 0.0f).setUv(1.0f, 1.0f).setColor(r, g, b, a);
        buffer.addVertex((Matrix4fc)mat, half, -half, 0.0f).setUv(1.0f, 0.0f).setColor(r, g, b, a);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(relX, relY, relZ);
        matrices.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-cameraYaw));
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(cameraPitch));
        Matrix4f gMat = matrices.last().pose();
        float glowSizePrimary = size * glowSize * 0.5f;
        float glowSizeSecondary = size * glowSize * 0.2f;
        this.renderGlowEffect(immediate, gMat, glowCol, alpha, glowSizePrimary, glowSizeSecondary);
        matrices.popPose();
    }

    private void renderGlowEffect(MultiBufferSource immediate, Matrix4f matrix, int color, float alpha, float sizePrimary, float sizeSecondary) {
        switch (this.glowMode.ordinal()) {
            case 0: {
                RenderType layerBloom = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM);
                ParticleRenderer.drawGlow(immediate.getBuffer(layerBloom), matrix, color, (int)(80.0f * alpha), sizePrimary);
                break;
            }
            case 1: {
                RenderType layerSample = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM_SAMPLE);
                ParticleRenderer.drawGlow(immediate.getBuffer(layerSample), matrix, color, (int)(140.0f * alpha), sizeSecondary);
                break;
            }
            case 2: {
                RenderType layerBloom = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM);
                RenderType layerSample = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM_SAMPLE);
                ParticleRenderer.drawGlow(immediate.getBuffer(layerBloom), matrix, color, (int)(80.0f * alpha), sizePrimary);
                ParticleRenderer.drawGlow(immediate.getBuffer(layerSample), matrix, color, (int)(140.0f * alpha), sizeSecondary);
            }
        }
    }

    public static enum ParticleMode {
        CUBES,
        CROWN,
        CUBE_BLAST,
        DOLLAR,
        HEART,
        LIGHTNING,
        LINE,
        RHOMBUS,
        SNOWFLAKE,
        STAR,
        STAR_ALT,
        TRIANGLE,
        RANDOM;

    }

    public static enum GlowMode {
        BLOOM,
        BLOOM_SAMPLE,
        BOTH;

    }
}

