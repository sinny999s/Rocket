
package rich.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.impl.render.worldparticles.Particle;
import rich.modules.impl.render.worldparticles.ParticleSpawner;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.timer.StopWatch;

public class WorldParticles
extends ModuleStructure {
    private final List<Particle> particles = new ArrayList<Particle>();
    private final StopWatch timer = new StopWatch();
    private Vec3 lastPlayerPos = Vec3.ZERO;
    private Vec3 playerVelocity = Vec3.ZERO;
    private double playerSpeed = 0.0;
    public SelectSetting mode = new SelectSetting("Mode", "Particle type").value("3D Cubes", "Crown", "Cube", "Dollar", "Heart", "Lightning", "Line", "Diamond", "Snowflake", "Star", "Star 2", "Triangle", "Glowing", "Random").selected("Star");
    public SliderSettings cubeCount = new SliderSettings("Quantity", "Quantity particles").range(10.0f, 500.0f).setValue(100.0f);
    public SliderSettings lifeTime = new SliderSettings("Lifetime", "Lifetime (sec)").range(2.0f, 60.0f).setValue(10.0f);
    public SliderSettings size = new SliderSettings("Size", "Size particles").range(0.1f, 1.5f).setValue(1.5f);
    public SliderSettings glowSize = new SliderSettings("Glowing", "Glow size").range(0.1f, 5.0f).setValue(3.0f);
    public BooleanSetting physics = new BooleanSetting("Physics", "Particles fall downward").setValue(false);
    public BooleanSetting randomColor = new BooleanSetting("Random color", "Each particle has a random color").setValue(false);
    public BooleanSetting whiteOnSpawn = new BooleanSetting("White on spawn", "Particles white on spawn and smoothly change color").setValue(true);
    public BooleanSetting whiteCenter = new BooleanSetting("White center", "White center on textured particles").setValue(false).visible(() -> !this.mode.getSelected().equals("3D Cubes"));
    public ColorSetting cubeColor = new ColorSetting("Color", "Color particles").value(-7773880).visible(() -> !this.randomColor.isValue());

    public static WorldParticles getInstance() {
        return Instance.get(WorldParticles.class);
    }

    public WorldParticles() {
        super("WorldParticles", "Flying particles in the world", ModuleCategory.RENDER);
        this.settings(this.mode, this.cubeCount, this.lifeTime, this.size, this.glowSize, this.physics, this.randomColor, this.whiteOnSpawn, this.whiteCenter, this.cubeColor);
    }

    @Override
    public void deactivate() {
        this.particles.clear();
        this.lastPlayerPos = Vec3.ZERO;
        this.playerVelocity = Vec3.ZERO;
        this.playerSpeed = 0.0;
    }

    private Particle.ParticleType getParticleType() {
        String selected;
        return switch (selected = this.mode.getSelected()) {
            case "3D Cubes" -> Particle.ParticleType.CUBE_3D;
            case "Crown" -> Particle.ParticleType.CROWN;
            case "Cube" -> Particle.ParticleType.CUBE_BLAST;
            case "Dollar" -> Particle.ParticleType.DOLLAR;
            case "Heart" -> Particle.ParticleType.HEART;
            case "Lightning" -> Particle.ParticleType.LIGHTNING;
            case "Line" -> Particle.ParticleType.LINE;
            case "Diamond" -> Particle.ParticleType.RHOMBUS;
            case "Snowflake" -> Particle.ParticleType.SNOWFLAKE;
            case "Star" -> Particle.ParticleType.STAR;
            case "Star 2" -> Particle.ParticleType.STAR_ALT;
            case "Triangle" -> Particle.ParticleType.TRIANGLE;
            case "Glowing" -> Particle.ParticleType.GLOW;
            case "Random" -> Particle.ParticleType.RANDOM;
            default -> Particle.ParticleType.CUBE_3D;
        };
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (WorldParticles.mc.player == null || WorldParticles.mc.level == null) {
            return;
        }
        Vec3 currentPos = WorldParticles.mc.player.position();
        if (this.lastPlayerPos != Vec3.ZERO) {
            this.playerVelocity = currentPos.subtract(this.lastPlayerPos);
            this.playerSpeed = this.playerVelocity.horizontalDistance();
        }
        this.lastPlayerPos = currentPos;
        double despawnDistSq = ParticleSpawner.getDespawnDistanceSquared();
        for (Particle p : this.particles) {
            double distSq;
            if (p.isFadingOut() || !((distSq = p.getHorizontalDistanceSquaredTo(currentPos)) > despawnDistSq)) continue;
            p.startFadeOut();
        }
        int actualDelay = ParticleSpawner.calculateSpawnDelay(this.playerSpeed);
        if ((float)this.particles.size() < this.cubeCount.getValue() && this.timer.finished(actualDelay)) {
            int spawnCount = ParticleSpawner.calculateSpawnCount(this.playerSpeed, this.particles.size(), (int)this.cubeCount.getValue());
            long lifeTimeMs = (long)(this.lifeTime.getValue() * 1000.0f);
            Particle.ParticleType type = this.getParticleType();
            for (int i = 0; i < spawnCount && (float)this.particles.size() < this.cubeCount.getValue(); ++i) {
                Particle particle = ParticleSpawner.createParticle(currentPos, this.playerVelocity, this.playerSpeed, lifeTimeMs, type);
                particle.setPhysics(this.physics.isValue());
                particle.setSize(this.size.getValue());
                this.particles.add(particle);
            }
            this.timer.reset();
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (this.particles.isEmpty()) {
            return;
        }
        MultiBufferSource.BufferSource immediate = mc.renderBuffers().bufferSource();
        PoseStack matrices = e.getStack();
        Vec3 cameraPos = WorldParticles.mc.gameRenderer.getMainCamera().position();
        long now = System.currentTimeMillis();
        float cameraYaw = WorldParticles.mc.gameRenderer.getMainCamera().yRot();
        float cameraPitch = WorldParticles.mc.gameRenderer.getMainCamera().xRot();
        float rotation = (float)(now % 9000L) / 9000.0f * 360.0f;
        int baseColor = this.cubeColor.getColor();
        float glow = this.glowSize.getValue();
        boolean useRandomColor = this.randomColor.isValue();
        boolean useWhiteOnSpawn = this.whiteOnSpawn.isValue();
        boolean useWhiteCenter = this.whiteCenter.isValue();
        Iterator<Particle> iterator = this.particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.update(now);
            if (!p.shouldRemove()) continue;
            iterator.remove();
        }
        for (Particle p : this.particles) {
            double distSq = p.getDistanceSquaredTo(cameraPos);
            if (!(distSq < 22500.0)) continue;
            p.render(matrices, immediate, cameraPos, baseColor, rotation, cameraYaw, cameraPitch, glow, useRandomColor, useWhiteOnSpawn, useWhiteCenter);
        }
        immediate.endBatch();
    }
}

