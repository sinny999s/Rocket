
package rich.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.AttackEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.impl.render.particles.Particle3D;
import rich.modules.impl.render.particles.TotemEmitter;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;

public class Particles
extends ModuleStructure {
    private final List<Particle3D> particles = new ArrayList<Particle3D>();
    private final List<TotemEmitter> totemEmitters = new ArrayList<TotemEmitter>();
    public SelectSetting mode = new SelectSetting("Mode", "Particle type").value("Cubes", "Crown", "Cube", "Dollar", "Heart", "Lightning", "Line", "Diamond", "Snowflake", "Star", "Star 2", "Triangle", "Random").selected("Star");
    public SelectSetting glowMode = new SelectSetting("Glowing", "Glow effect type").value("Bloom", "Bloom Sample", "Both").selected("Bloom Sample");
    public MultiSelectSetting triggers = new MultiSelectSetting("Triggers", "When to spawn particles").value("Strike", "Totem", "Walking", "Throwable item").selected("Strike", "Totem", "Walking", "Throwable item");
    public SliderSettings amount = new SliderSettings("Quantity", "Particles per hit").range(10, 40).setValue(40.0f);
    public SliderSettings walkAmount = new SliderSettings("Amount while walking", "Particles per second while walking").range(10, 30).setValue(30.0f).visible(() -> this.triggers.isSelected("Walking"));
    public SliderSettings spread = new SliderSettings("Spread", "Particle spread strength").range(0.5f, 3.0f).setValue(1.0f);
    public SliderSettings speed = new SliderSettings("Speed", "Particle movement speed").range(0.1f, 3.0f).setValue(2.0f);
    public SliderSettings lifeTime = new SliderSettings("Lifetime", "Particle lifetime in seconds").range(0.5f, 10.0f).setValue(2.5f);
    public SliderSettings size = new SliderSettings("Size", "Size particles").range(0.1f, 1.0f).setValue(1.0f);
    public BooleanSetting randomColor = new BooleanSetting("Random color", "Each particle gets a random color").setValue(false);
    public ColorSetting color = new ColorSetting("Color", "Color particles").value(-7773880).visible(() -> !this.randomColor.isValue());
    private static final float GLOW_SIZE = 7.5f;
    private static final int TOTEM_DURATION = 20;
    private static final float GRAVITY_STRENGTH = 0.04f;
    private static final int[] RANDOM_COLORS = new int[]{-65536, -33024, -256, -16711936, -16711681, -16776961, -7667457, -65281, -60269, -1, -16711809, -40121};
    private float walkParticleAccumulator = 0.0f;

    public static Particles getInstance() {
        return Instance.get(Particles.class);
    }

    public Particles() {
        super("Particles", "Custom particles system", ModuleCategory.RENDER);
        this.settings(this.mode, this.glowMode, this.triggers, this.amount, this.walkAmount, this.spread, this.speed, this.lifeTime, this.size, this.randomColor, this.color);
    }

    @Override
    public void deactivate() {
        this.particles.clear();
        this.totemEmitters.clear();
        this.walkParticleAccumulator = 0.0f;
    }

    private int getParticleColor() {
        if (this.randomColor.isValue()) {
            return RANDOM_COLORS[ThreadLocalRandom.current().nextInt(RANDOM_COLORS.length)];
        }
        return this.color.getColor();
    }

    private float getGravity() {
        return 0.0040000007f;
    }

    private float getSpeedMultiplier() {
        return this.speed.getValue();
    }

    private Particle3D.ParticleMode getParticleMode() {
        String selected;
        return switch (selected = this.mode.getSelected()) {
            case "Cubes" -> Particle3D.ParticleMode.CUBES;
            case "Crown" -> Particle3D.ParticleMode.CROWN;
            case "Cube" -> Particle3D.ParticleMode.CUBE_BLAST;
            case "Dollar" -> Particle3D.ParticleMode.DOLLAR;
            case "Heart" -> Particle3D.ParticleMode.HEART;
            case "Lightning" -> Particle3D.ParticleMode.LIGHTNING;
            case "Line" -> Particle3D.ParticleMode.LINE;
            case "Diamond" -> Particle3D.ParticleMode.RHOMBUS;
            case "Snowflake" -> Particle3D.ParticleMode.SNOWFLAKE;
            case "Star" -> Particle3D.ParticleMode.STAR;
            case "Star 2" -> Particle3D.ParticleMode.STAR_ALT;
            case "Triangle" -> Particle3D.ParticleMode.TRIANGLE;
            case "Random" -> Particle3D.ParticleMode.RANDOM;
            default -> Particle3D.ParticleMode.CUBES;
        };
    }

    private Particle3D.GlowMode getGlowMode() {
        String selected;
        return switch (selected = this.glowMode.getSelected()) {
            case "Bloom" -> Particle3D.GlowMode.BLOOM;
            case "Bloom Sample" -> Particle3D.GlowMode.BLOOM_SAMPLE;
            case "Both" -> Particle3D.GlowMode.BOTH;
            default -> Particle3D.GlowMode.BOTH;
        };
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (Particles.mc.player == null || Particles.mc.level == null) {
            return;
        }
        if (this.triggers.isSelected("Walking")) {
            this.handleWalkParticles();
        }
        if (this.triggers.isSelected("Throwable item")) {
            this.handleProjectileParticles();
        }
        Iterator<TotemEmitter> emitterIterator = this.totemEmitters.iterator();
        while (emitterIterator.hasNext()) {
            TotemEmitter emitter = emitterIterator.next();
            emitter.tick();
            if (emitter.isAlive()) {
                this.spawnTotemParticlesBurst(emitter.getEntity(), emitter.getProgress());
                continue;
            }
            emitterIterator.remove();
        }
        Iterator<Particle3D> iterator = this.particles.iterator();
        while (iterator.hasNext()) {
            Particle3D p = iterator.next();
            p.update();
            if (!p.isDead()) continue;
            iterator.remove();
        }
    }

    private void handleWalkParticles() {
        boolean isMoving;
        double velocitySq = Particles.mc.player.getDeltaMovement().lengthSqr();
        boolean bl = isMoving = velocitySq > 1.0E-4 && !Particles.mc.player.isShiftKeyDown();
        if (!isMoving) {
            this.walkParticleAccumulator = 0.0f;
            return;
        }
        float particlesPerSecond = this.walkAmount.getValue();
        float particlesPerTick = particlesPerSecond / 20.0f;
        this.walkParticleAccumulator += particlesPerTick;
        int particlesToSpawn = (int)this.walkParticleAccumulator;
        this.walkParticleAccumulator -= (float)particlesToSpawn;
        if (particlesToSpawn <= 0) {
            return;
        }
        float yaw = Particles.mc.player.getYRot();
        double radian = Math.toRadians(yaw + 90.0f);
        double offsetX = Math.cos(radian) * 0.5;
        double offsetZ = Math.sin(radian) * 0.5;
        float spreadValue = this.spread.getValue() * 0.05f;
        float speedMult = this.getSpeedMultiplier();
        for (int i = 0; i < particlesToSpawn; ++i) {
            double px = Particles.mc.player.getX() - offsetX + (Math.random() - 0.5) * 0.3;
            double py = Particles.mc.player.getY() + 0.3 + Math.random() * ((double)Particles.mc.player.getBbHeight() - 0.3);
            double pz = Particles.mc.player.getZ() - offsetZ + (Math.random() - 0.5) * 0.3;
            Vec3 pos = new Vec3(px, py, pz);
            double velX = (Math.random() - 0.5) * (double)spreadValue * (double)speedMult;
            double velY = (Math.random() - 0.5) * (double)spreadValue * 0.5 * (double)speedMult;
            double velZ = (Math.random() - 0.5) * (double)spreadValue * (double)speedMult;
            Vec3 velocity = new Vec3(velX, velY, velZ);
            this.particles.add(new Particle3D(pos, velocity, this.getParticleColor(), this.size.getValue() * 0.6f, this.lifeTime.getValue() * 0.5f).setGravity(this.getGravity()).setVelocityMultiplier(0.99f).setMode(this.getParticleMode()).setGlowMode(this.getGlowMode()));
        }
    }

    private void handleProjectileParticles() {
        float spreadValue = this.spread.getValue() * 0.03f;
        float speedMult = this.getSpeedMultiplier();
        for (Entity entity : Particles.mc.level.entitiesForRendering()) {
            boolean isMoving;
            if (!(entity instanceof ThrowableProjectile) && !(entity instanceof Arrow) && !(entity instanceof ThrownTrident)) continue;
            Projectile projectile = (Projectile)entity;
            double prevX = projectile.xo;
            double prevY = projectile.yo;
            double prevZ = projectile.zo;
            double currentX = projectile.getX();
            double currentY = projectile.getY();
            double currentZ = projectile.getZ();
            boolean bl = isMoving = Math.abs(currentX - prevX) > 0.01 || Math.abs(currentY - prevY) > 0.01 || Math.abs(currentZ - prevZ) > 0.01;
            if (!isMoving && !(projectile.getDeltaMovement().lengthSqr() > 0.01)) continue;
            for (int i = 0; i < 2; ++i) {
                double px = projectile.getX() + (Math.random() - 0.5) * 0.5;
                double py = projectile.getY() + Math.random() * (double)projectile.getBbHeight();
                double pz = projectile.getZ() + (Math.random() - 0.5) * 0.5;
                Vec3 pos = new Vec3(px, py, pz);
                double velX = (Math.random() - 0.5) * 2.0 * (double)spreadValue * (double)speedMult;
                double velY = (Math.random() - 0.5) * 2.0 * (double)spreadValue * (double)speedMult;
                double velZ = (Math.random() - 0.5) * 2.0 * (double)spreadValue * (double)speedMult;
                Vec3 velocity = new Vec3(velX, velY, velZ);
                this.particles.add(new Particle3D(pos, velocity, this.getParticleColor(), this.size.getValue() * 0.5f, this.lifeTime.getValue() * 0.3f).setGravity(this.getGravity()).setVelocityMultiplier(0.99f).setMode(this.getParticleMode()).setGlowMode(this.getGlowMode()));
            }
        }
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        if (!this.triggers.isSelected("Strike") || e.getTarget() == null) {
            return;
        }
        Entity target = e.getTarget();
        float spreadValue = this.spread.getValue() * 0.15f;
        float speedMult = this.getSpeedMultiplier();
        int count = this.amount.getInt();
        for (int i = 0; i < count; ++i) {
            double px = target.getX();
            double py = target.getY() + Math.random() * (double)target.getBbHeight();
            double pz = target.getZ();
            Vec3 pos = new Vec3(px, py, pz);
            Vec3 velocity = new Vec3((Math.random() - 0.5) * 2.0 * (double)spreadValue * (double)speedMult, (Math.random() - 0.5) * 2.0 * (double)spreadValue * (double)speedMult, (Math.random() - 0.5) * 2.0 * (double)spreadValue * (double)speedMult);
            this.particles.add(new Particle3D(pos, velocity, this.getParticleColor(), this.size.getValue(), this.lifeTime.getValue()).setGravity(this.getGravity()).setVelocityMultiplier(0.99f).setMode(this.getParticleMode()).setGlowMode(this.getGlowMode()));
        }
    }

    public void onTotemPop(Entity entity) {
        if (!this.triggers.isSelected("Totem")) {
            return;
        }
        this.totemEmitters.add(new TotemEmitter(entity, 20));
    }

    private void spawnTotemParticlesBurst(Entity entity, float progress) {
        if (entity == null || entity.isRemoved()) {
            return;
        }
        float spreadMultiplier = 1.0f - progress * 0.5f;
        float spreadValue = this.spread.getValue();
        float speedMult = this.getSpeedMultiplier();
        for (int i = 0; i < 4; ++i) {
            double f;
            double e;
            double d = Math.random() * 2.0 - 1.0;
            if (!(d * d + (e = Math.random() * 2.0 - 1.0) * e + (f = Math.random() * 2.0 - 1.0) * f <= 1.0)) continue;
            double px = entity.getX() + d * (double)entity.getBbWidth() * 0.5;
            double py = entity.getY(0.5) + e * (double)entity.getBbHeight() * 0.5;
            double pz = entity.getZ() + f * (double)entity.getBbWidth() * 0.5;
            Vec3 pos = new Vec3(px, py, pz);
            double velocityScale = (double)spreadValue * 0.18 * (double)spreadMultiplier * (double)speedMult;
            double initialUpward = Math.random() < 0.4 ? (0.15 + Math.random() * 0.2) * (double)speedMult : (0.03 + Math.random() * 0.07) * (double)speedMult;
            Vec3 velocity = new Vec3(d * velocityScale, initialUpward, f * velocityScale);
            int[] totemColors = new int[]{-8586240, -10496, -13447886, -23296, -16711936, -5374161};
            int particleColor = totemColors[(int)(Math.random() * (double)totemColors.length)];
            this.particles.add(new Particle3D(pos, velocity, particleColor, this.size.getValue() * 0.8f, this.lifeTime.getValue() * 0.8f).setGravity(this.getGravity()).setVelocityMultiplier(0.98f).setMode(this.getParticleMode()).setGlowMode(this.getGlowMode()));
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent e) {
        if (this.particles.isEmpty()) {
            return;
        }
        PoseStack stack = e.getStack();
        MultiBufferSource.BufferSource immediate = mc.renderBuffers().bufferSource();
        float partialTicks = e.getPartialTicks();
        for (Particle3D p : this.particles) {
            p.render(stack, immediate, 7.5f, partialTicks);
        }
        immediate.endBatch();
    }
}

