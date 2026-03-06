/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package rich.modules.impl.render.worldparticles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import rich.util.render.sliemtpipeline.ClientPipelines;

public class ParticleRenderer {
    private static final Identifier GLOW_TEXTURE = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/dashbloom.png");
    private static final Identifier GLOW_TEXTURE_SECONDARY = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/dashbloomsample.png");

    public static RenderType getQuadsLayer() {
        return ClientPipelines.WORLD_PARTICLES_QUADS;
    }

    public static RenderType getLinesLayer() {
        return ClientPipelines.WORLD_PARTICLES_LINES;
    }

    public static RenderType getGlowLayer() {
        return ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_TEXTURE);
    }

    public static RenderType getGlowLayerSecondary() {
        return ClientPipelines.WORLD_PARTICLES_GLOW.apply(GLOW_TEXTURE_SECONDARY);
    }

    public static void drawCube(VertexConsumer b, Matrix4f m, int color, float s) {
        float h = s / 2.0f;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int bl = color & 0xFF;
        int a = color >> 24 & 0xFF;
        b.addVertex((Matrix4fc)m, -h, h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, -h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, -h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, -h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, -h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, -h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, -h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, -h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, -h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, -h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, -h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, -h, h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, h, -h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, -h, h).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, h, -h, -h).setColor(r, g, bl, a);
    }

    public static void drawLines(VertexConsumer b, Matrix4f m, int c, float s) {
        float h = s / 2.0f;
        int r = c >> 16 & 0xFF;
        int g = c >> 8 & 0xFF;
        int bl = c & 0xFF;
        int a = c >> 24 & 0xFF;
        ParticleRenderer.line(b, m, -h, -h, -h, h, -h, -h, r, g, bl, a);
        ParticleRenderer.line(b, m, h, -h, -h, h, -h, h, r, g, bl, a);
        ParticleRenderer.line(b, m, h, -h, h, -h, -h, h, r, g, bl, a);
        ParticleRenderer.line(b, m, -h, -h, h, -h, -h, -h, r, g, bl, a);
        ParticleRenderer.line(b, m, -h, h, -h, h, h, -h, r, g, bl, a);
        ParticleRenderer.line(b, m, h, h, -h, h, h, h, r, g, bl, a);
        ParticleRenderer.line(b, m, h, h, h, -h, h, h, r, g, bl, a);
        ParticleRenderer.line(b, m, -h, h, h, -h, h, -h, r, g, bl, a);
        ParticleRenderer.line(b, m, -h, -h, -h, -h, h, -h, r, g, bl, a);
        ParticleRenderer.line(b, m, h, -h, -h, h, h, -h, r, g, bl, a);
        ParticleRenderer.line(b, m, h, -h, h, h, h, h, r, g, bl, a);
        ParticleRenderer.line(b, m, -h, -h, h, -h, h, h, r, g, bl, a);
    }

    private static void line(VertexConsumer b, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, int r, int g, int bl, int a) {
        b.addVertex((Matrix4fc)m, x1, y1, z1).setColor(r, g, bl, a);
        b.addVertex((Matrix4fc)m, x2, y2, z2).setColor(r, g, bl, a);
    }

    public static void drawGlow(VertexConsumer buffer, Matrix4f matrix, int color, int alpha, float size) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        float half = size / 2.0f;
        buffer.addVertex((Matrix4fc)matrix, -half, -half, 0.0f).setUv(0.0f, 0.0f).setColor(r, g, b, alpha);
        buffer.addVertex((Matrix4fc)matrix, -half, half, 0.0f).setUv(0.0f, 1.0f).setColor(r, g, b, alpha);
        buffer.addVertex((Matrix4fc)matrix, half, half, 0.0f).setUv(1.0f, 1.0f).setColor(r, g, b, alpha);
        buffer.addVertex((Matrix4fc)matrix, half, -half, 0.0f).setUv(1.0f, 0.0f).setColor(r, g, b, alpha);
    }
}

