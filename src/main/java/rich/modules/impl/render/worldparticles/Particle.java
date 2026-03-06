/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package rich.modules.impl.render.worldparticles;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
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

public class Particle
implements IMinecraft {
    private static final ParticleType[] RANDOM_TYPES = new ParticleType[]{ParticleType.CROWN, ParticleType.CUBE_BLAST, ParticleType.DOLLAR, ParticleType.HEART, ParticleType.LIGHTNING, ParticleType.LINE, ParticleType.RHOMBUS, ParticleType.SNOWFLAKE, ParticleType.STAR, ParticleType.STAR_ALT, ParticleType.TRIANGLE, ParticleType.GLOW};
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
    private static final Identifier TEXTURE_GLOW = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/dashbloom.png");
    private static final Identifier GLOW_BLOOM = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/dashbloom.png");
    private static final Identifier GLOW_BLOOM_SAMPLE = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/dashbloomsample.png");
    double x;
    double y;
    double z;
    double prevX;
    double prevY;
    double prevZ;
    double mX;
    double mY;
    double mZ;
    long start = System.currentTimeMillis();
    float phase;
    Animation fadeInAnimation;
    Animation fadeOutAnimation;
    float cachedAlpha = 0.0f;
    long lastAlphaUpdate = 0L;
    boolean fadingOut = false;
    long lifeTimeMs;
    boolean forceFadeOut = false;
    private ParticleType type;
    private ParticleType actualType;
    private Identifier texture;
    private float rotation;
    private float rotationSpeed;
    private int randomColor;
    private long spawnTime = System.currentTimeMillis();
    private boolean physicsEnabled = true;
    private float size = 0.5f;

    public Particle(double x, double y, double z, long lifeTimeMs) {
        this.phase = (float)(Math.random() * 100.0);
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.mX = (Math.random() - 0.5) * 0.04;
        this.mY = (Math.random() - 0.5) * 0.04;
        this.mZ = (Math.random() - 0.5) * 0.04;
        this.fadeInAnimation = new EaseInOutQuad().setMs(600).setValue(1.0);
        this.fadeInAnimation.setDirection(Direction.FORWARDS);
        this.fadeOutAnimation = new EaseInOutQuad().setMs(400).setValue(1.0);
        this.fadeOutAnimation.setDirection(Direction.FORWARDS);
        this.lifeTimeMs = lifeTimeMs;
        this.rotation = (float)(Math.random() * 360.0);
        this.rotationSpeed = (float)(Math.random() * 1.5 + 0.5);
        this.randomColor = this.generateRandomColor();
        this.type = ParticleType.CUBE_3D;
        this.actualType = ParticleType.CUBE_3D;
    }

    public Particle(double x, double y, double z, double mx, double my, double mz, long lifeTimeMs) {
        this(x, y, z, lifeTimeMs);
        this.mX = mx;
        this.mY = my;
        this.mZ = mz;
    }

    private int generateRandomColor() {
        int[] colors = new int[]{-65536, -33024, -256, -16711936, -16711681, -16776961, -7667457, -65281, -60269, -1, -16711809, -40121};
        return colors[ThreadLocalRandom.current().nextInt(colors.length)];
    }

    public Particle setType(ParticleType type) {
        this.type = type;
        this.actualType = type == ParticleType.RANDOM ? RANDOM_TYPES[ThreadLocalRandom.current().nextInt(RANDOM_TYPES.length)] : type;
        this.texture = this.getTextureForType(this.actualType);
        return this;
    }

    public Particle setPhysics(boolean enabled) {
        this.physicsEnabled = enabled;
        return this;
    }

    public Particle setSize(float size) {
        this.size = size;
        return this;
    }

    private Identifier getTextureForType(ParticleType type) {
        return switch (type.ordinal()) {
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
            case 12 -> TEXTURE_GLOW;
            default -> null;
        };
    }

    public double getDistanceSquaredTo(Vec3 pos) {
        double dx = this.x - pos.x;
        double dy = this.y - pos.y;
        double dz = this.z - pos.z;
        return dx * dx + dy * dy + dz * dz;
    }

    public double getHorizontalDistanceSquaredTo(Vec3 pos) {
        double dx = this.x - pos.x;
        double dz = this.z - pos.z;
        return dx * dx + dz * dz;
    }

    public void startFadeOut() {
        if (!this.fadingOut) {
            this.fadingOut = true;
            this.forceFadeOut = true;
            this.fadeOutAnimation.setDirection(Direction.BACKWARDS);
        }
    }

    public void update(long now) {
        if (Particle.mc.level == null) {
            return;
        }
        this.prevX = this.x;
        this.prevY = this.y;
        this.prevZ = this.z;
        double velMagSq = this.mX * this.mX + this.mY * this.mY + this.mZ * this.mZ;
        if (velMagSq > 1.0E-4) {
            if (this.isHit(this.x + this.mX, this.y, this.z)) {
                this.mX *= -0.8;
            } else {
                this.x += this.mX;
            }
            if (this.isHit(this.x, this.y + this.mY, this.z)) {
                this.mY *= -0.8;
            } else {
                this.y += this.mY;
            }
            if (this.isHit(this.x, this.y, this.z + this.mZ)) {
                this.mZ *= -0.8;
            } else {
                this.z += this.mZ;
            }
        } else {
            this.x += this.mX;
            this.y += this.mY;
            this.z += this.mZ;
        }
        this.mX *= 0.99;
        this.mY *= 0.99;
        this.mZ *= 0.99;
        if (this.physicsEnabled) {
            this.mY -= 2.0E-4;
        }
        this.rotation += this.rotationSpeed;
        if (!this.fadingOut && now - this.start > this.lifeTimeMs) {
            this.fadingOut = true;
            this.fadeOutAnimation.setDirection(Direction.BACKWARDS);
        }
        if (now - this.lastAlphaUpdate > 16L) {
            this.cachedAlpha = this.fadingOut ? this.fadeOutAnimation.getOutput().floatValue() : this.fadeInAnimation.getOutput().floatValue();
            this.lastAlphaUpdate = now;
        }
    }

    public float getAlpha() {
        return this.cachedAlpha;
    }

    private boolean isHit(double px, double py, double pz) {
        if (Particle.mc.level == null) {
            return false;
        }
        BlockPos pos = BlockPos.containing((double)px, (double)py, (double)pz);
        return Particle.mc.level.getBlockState(pos).isCollisionShapeFullBlock(Particle.mc.level, pos);
    }

    public boolean shouldRemove() {
        return this.fadingOut && this.cachedAlpha <= 0.0f;
    }

    public boolean isFadingOut() {
        return this.fadingOut;
    }

    public int getColor(int baseColor, boolean useRandomColor, boolean whiteOnSpawn) {
        long transitionDuration;
        long currentTime;
        long timeSinceSpawn;
        if (useRandomColor) {
            return ColorUtil.multAlpha(this.randomColor, this.cachedAlpha);
        }
        if (whiteOnSpawn && (timeSinceSpawn = (currentTime = System.currentTimeMillis()) - this.spawnTime) < (transitionDuration = 7000L)) {
            float progress = (float)timeSinceSpawn / (float)transitionDuration;
            int targetR = baseColor >> 16 & 0xFF;
            int targetG = baseColor >> 8 & 0xFF;
            int targetB = baseColor & 0xFF;
            int targetA = baseColor >> 24 & 0xFF;
            int r = (int)(255.0f + (float)(targetR - 255) * progress);
            int g = (int)(255.0f + (float)(targetG - 255) * progress);
            int b = (int)(255.0f + (float)(targetB - 255) * progress);
            int color = targetA << 24 | r << 16 | g << 8 | b;
            return ColorUtil.multAlpha(color, this.cachedAlpha);
        }
        return ColorUtil.multAlpha(baseColor, this.cachedAlpha);
    }

    public void render(PoseStack matrices, MultiBufferSource immediate, Vec3 cameraPos, int baseColor, float globalRotation, float cameraYaw, float cameraPitch, float glowSize, boolean useRandomColor, boolean whiteOnSpawn, boolean whiteCenter) {
        float alpha = this.getAlpha();
        if (alpha <= 0.0f) {
            return;
        }
        float partialTicks = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        double interpX = this.prevX + (this.x - this.prevX) * (double)partialTicks;
        double interpY = this.prevY + (this.y - this.prevY) * (double)partialTicks;
        double interpZ = this.prevZ + (this.z - this.prevZ) * (double)partialTicks;
        float relX = (float)(interpX - cameraPos.x);
        float relY = (float)(interpY - cameraPos.y);
        float relZ = (float)(interpZ - cameraPos.z);
        int color = this.getColor(baseColor, useRandomColor, whiteOnSpawn);
        if (this.actualType == ParticleType.CUBE_3D) {
            this.renderCube3D(matrices, immediate, relX, relY, relZ, alpha, color, globalRotation, cameraYaw, cameraPitch, glowSize);
        } else {
            this.renderTextured(matrices, immediate, relX, relY, relZ, alpha, color, cameraYaw, cameraPitch, glowSize, whiteCenter);
        }
    }

    private void renderCube3D(PoseStack matrices, MultiBufferSource immediate, float relX, float relY, float relZ, float alpha, int color, float globalRotation, float cameraYaw, float cameraPitch, float glowSize) {
        float alpha02 = alpha * 0.2f;
        float alpha04 = alpha * 0.4f;
        int glowCol = color;
        float cubeSize = this.size * 0.5f;
        float cubeGlow1 = cubeSize * glowSize;
        float cubeGlow2 = cubeSize * (glowSize / 3.0f);
        float rotY = globalRotation + this.phase;
        float rotX = globalRotation * 0.5f;
        matrices.pushPose();
        matrices.translate(relX, relY, relZ);
        matrices.mulPose((Quaternionfc)Axis.YP.rotationDegrees(rotY));
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(rotX));
        Matrix4f mat = matrices.last().pose();
        ParticleRenderer.drawCube(immediate.getBuffer(ParticleRenderer.getQuadsLayer()), mat, ColorUtil.multAlpha(color, alpha02), cubeSize);
        ParticleRenderer.drawLines(immediate.getBuffer(ParticleRenderer.getLinesLayer()), mat, ColorUtil.multAlpha(color, alpha04), cubeSize);
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(relX, relY, relZ);
        matrices.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-cameraYaw));
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(cameraPitch));
        Matrix4f gMat = matrices.last().pose();
        ParticleRenderer.drawGlow(immediate.getBuffer(ParticleRenderer.getGlowLayer()), gMat, glowCol, (int)(80.0f * alpha), cubeGlow1);
        ParticleRenderer.drawGlow(immediate.getBuffer(ParticleRenderer.getGlowLayerSecondary()), gMat, glowCol, (int)(140.0f * alpha), cubeGlow2);
        matrices.popPose();
    }

    private void renderTextured(PoseStack matrices, MultiBufferSource immediate, float relX, float relY, float relZ, float alpha, int color, float cameraYaw, float cameraPitch, float glowSize, boolean whiteCenter) {
        if (this.texture == null) {
            return;
        }
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int)(255.0f * alpha);
        float textureSize = this.size * 0.5f;
        matrices.pushPose();
        matrices.translate(relX, relY, relZ);
        matrices.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-cameraYaw));
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(cameraPitch));
        matrices.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(this.rotation));
        Matrix4f mat = matrices.last().pose();
        RenderType layer = ClientPipelines.WORLD_PARTICLES_GLOW.apply(this.texture);
        VertexConsumer buffer = immediate.getBuffer(layer);
        float half = textureSize / 2.0f;
        buffer.addVertex((Matrix4fc)mat, -half, -half, 0.0f).setUv(0.0f, 0.0f).setColor(r, g, b, a);
        buffer.addVertex((Matrix4fc)mat, -half, half, 0.0f).setUv(0.0f, 1.0f).setColor(r, g, b, a);
        buffer.addVertex((Matrix4fc)mat, half, half, 0.0f).setUv(1.0f, 1.0f).setColor(r, g, b, a);
        buffer.addVertex((Matrix4fc)mat, half, -half, 0.0f).setUv(1.0f, 0.0f).setColor(r, g, b, a);
        if (whiteCenter) {
            float centerSize = half * 0.5f;
            int whiteA = (int)(200.0f * alpha);
            buffer.addVertex((Matrix4fc)mat, -centerSize, -centerSize, 0.001f).setUv(0.0f, 0.0f).setColor(255, 255, 255, whiteA);
            buffer.addVertex((Matrix4fc)mat, -centerSize, centerSize, 0.001f).setUv(0.0f, 1.0f).setColor(255, 255, 255, whiteA);
            buffer.addVertex((Matrix4fc)mat, centerSize, centerSize, 0.001f).setUv(1.0f, 1.0f).setColor(255, 255, 255, whiteA);
            buffer.addVertex((Matrix4fc)mat, centerSize, -centerSize, 0.001f).setUv(1.0f, 0.0f).setColor(255, 255, 255, whiteA);
        }
        matrices.popPose();
        matrices.pushPose();
        matrices.translate(relX, relY, relZ);
        matrices.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-cameraYaw));
        matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(cameraPitch));
        Matrix4f gMat = matrices.last().pose();
        float glowSizePrimary = textureSize * glowSize * 0.5f;
        float glowSizeSecondary = textureSize * glowSize * 0.2f;
        RenderType layerBloom = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM);
        RenderType layerSample = ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_BLOOM_SAMPLE);
        ParticleRenderer.drawGlow(immediate.getBuffer(layerBloom), gMat, color, (int)(80.0f * alpha), glowSizePrimary);
        ParticleRenderer.drawGlow(immediate.getBuffer(layerSample), gMat, color, (int)(140.0f * alpha), glowSizeSecondary);
        matrices.popPose();
    }

    public static enum ParticleType {
        CUBE_3D,
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
        GLOW,
        RANDOM;

    }
}

