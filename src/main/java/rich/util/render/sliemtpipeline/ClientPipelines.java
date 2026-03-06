
package rich.util.render.sliemtpipeline;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public class ClientPipelines {
    public static final RenderPipeline ROMB_ESP_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/wtex").withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color").withSampler("Sampler0").withBlend(BlendFunction.LIGHTNING).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).build());
    public static final Function<Identifier, RenderType> ROMB_ESP = Util.memoize(texture -> {
        RenderSetup setup = RenderSetup.builder((RenderPipeline)ROMB_ESP_PIPELINE).withTexture("Sampler0", (Identifier)texture).sortOnUpload().bufferSize(1536).createRenderSetup();
        return RenderType.create((String)"wtex", (RenderSetup)setup);
    });
    public static final RenderPipeline GHOSTS_ESP_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/wtex").withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color").withSampler("Sampler0").withBlend(BlendFunction.LIGHTNING).withDepthTestFunction(DepthTestFunction.LESS_DEPTH_TEST).withDepthWrite(false).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).build());
    public static final Function<Identifier, RenderType> GHOSTS_ESP = Util.memoize(texture -> {
        RenderSetup setup = RenderSetup.builder((RenderPipeline)GHOSTS_ESP_PIPELINE).withTexture("Sampler0", (Identifier)texture).sortOnUpload().bufferSize(1536).createRenderSetup();
        return RenderType.create((String)"wtex", (RenderSetup)setup);
    });
    public static final RenderPipeline CHAIN_ESP_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/wtex").withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color").withSampler("Sampler0").withBlend(BlendFunction.LIGHTNING).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).build());
    public static final Function<Identifier, RenderType> CHAIN_ESP = Util.memoize(texture -> {
        RenderSetup setup = RenderSetup.builder((RenderPipeline)CHAIN_ESP_PIPELINE).withTexture("Sampler0", (Identifier)texture).sortOnUpload().bufferSize(1536).createRenderSetup();
        return RenderType.create((String)"wtex", (RenderSetup)setup);
    });
    public static final RenderPipeline CRYSTAL_FILLED_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/crystal_filled").withVertexShader("core/position_color").withFragmentShader("core/position_color").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build());
    public static final RenderType CRYSTAL_FILLED = RenderType.create((String)"crystal_filled", (RenderSetup)RenderSetup.builder((RenderPipeline)CRYSTAL_FILLED_PIPELINE).sortOnUpload().bufferSize(8192).createRenderSetup());
    public static final RenderPipeline CRYSTAL_GLOW_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/crystal_glow").withVertexShader("core/position_color").withFragmentShader("core/position_color").withBlend(BlendFunction.LIGHTNING).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).build());
    public static final RenderType CRYSTAL_GLOW = RenderType.create((String)"crystal_glow", (RenderSetup)RenderSetup.builder((RenderPipeline)CRYSTAL_GLOW_PIPELINE).sortOnUpload().bufferSize(4096).createRenderSetup());
    public static final RenderPipeline BLOOM_ESP_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/bloom_esp").withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color").withSampler("Sampler0").withBlend(BlendFunction.LIGHTNING).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).build());
    public static final Function<Identifier, RenderType> BLOOM_ESP = Util.memoize(texture -> {
        RenderSetup setup = RenderSetup.builder((RenderPipeline)BLOOM_ESP_PIPELINE).withTexture("Sampler0", (Identifier)texture).sortOnUpload().bufferSize(2048).createRenderSetup();
        return RenderType.create((String)"bloom_esp", (RenderSetup)setup);
    });
    public static final RenderPipeline CHINA_HAT_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/china_hat").withVertexShader("core/position_color").withFragmentShader("core/position_color").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(true).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN).build());
    public static final RenderType CHINA_HAT = RenderType.create((String)"china_hat", (RenderSetup)RenderSetup.builder((RenderPipeline)CHINA_HAT_PIPELINE).sortOnUpload().bufferSize(8192).createRenderSetup());
    public static final RenderPipeline CHINA_HAT_OUTLINE_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/china_hat_outline").withVertexShader("core/position_color").withFragmentShader("core/position_color").withBlend(BlendFunction.TRANSLUCENT).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(true).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINE_STRIP).build());
    public static final RenderType CHINA_HAT_OUTLINE = RenderType.create((String)"china_hat_outline", (RenderSetup)RenderSetup.builder((RenderPipeline)CHINA_HAT_OUTLINE_PIPELINE).sortOnUpload().bufferSize(4096).createRenderSetup());
    public static final RenderPipeline WORLD_PARTICLES_COLOR_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation(Identifier.fromNamespaceAndPath((String)"rich", (String)"world_particles_color")).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS).withCull(false).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.LIGHTNING).build());
    public static final RenderType WORLD_PARTICLES_QUADS = RenderType.create((String)"world_particles_cube", (RenderSetup)RenderSetup.builder((RenderPipeline)WORLD_PARTICLES_COLOR_PIPELINE).sortOnUpload().bufferSize(2048).createRenderSetup());
    public static final RenderPipeline WORLD_PARTICLES_LINES_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET).withLocation(Identifier.fromNamespaceAndPath((String)"rich", (String)"world_particles_lines")).withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES).withCull(false).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.LIGHTNING).build());
    public static final RenderType WORLD_PARTICLES_LINES = RenderType.create((String)"world_particles_lines", (RenderSetup)RenderSetup.builder((RenderPipeline)WORLD_PARTICLES_LINES_PIPELINE).sortOnUpload().bufferSize(2048).createRenderSetup());
    public static final RenderPipeline WORLD_PARTICLES_GLOW_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET).withLocation(Identifier.fromNamespaceAndPath((String)"rich", (String)"world_particles_glow")).withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).withCull(false).withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST).withDepthWrite(false).withBlend(BlendFunction.LIGHTNING).withSampler("Sampler0").build());
    public static final Function<Identifier, RenderType> WORLD_PARTICLES_GLOW = Util.memoize(texture -> {
        RenderSetup setup = RenderSetup.builder((RenderPipeline)WORLD_PARTICLES_GLOW_PIPELINE).withTexture("Sampler0", (Identifier)texture).sortOnUpload().bufferSize(2048).createRenderSetup();
        return RenderType.create((String)"world_particles_glow", (RenderSetup)setup);
    });
    public static final RenderPipeline GUI_ARROW_BLEND_PIPELINE = RenderPipelines.register((RenderPipeline)RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET).withLocation("pipeline/gui_arrow_blend").withVertexShader("core/position_tex_color").withFragmentShader("core/position_tex_color").withSampler("Sampler0").withBlend(BlendFunction.LIGHTNING).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST).withCull(false).withVertexFormat(DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS).build());
    public static final Function<Identifier, RenderType> GUI_ARROW_BLEND = Util.memoize(texture -> {
        RenderSetup setup = RenderSetup.builder((RenderPipeline)GUI_ARROW_BLEND_PIPELINE).withTexture("Sampler0", (Identifier)texture).sortOnUpload().bufferSize(256).createRenderSetup();
        return RenderType.create((String)"gui_arrow_blend", (RenderSetup)setup);
    });
}

