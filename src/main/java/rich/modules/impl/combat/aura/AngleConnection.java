/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  lombok.Generated
 */
package rich.modules.impl.combat.aura;

import java.util.Objects;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.PacketEvent;
import rich.events.impl.PlayerVelocityStrafeEvent;
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConstructor;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.module.ModuleStructure;
import rich.util.math.TaskPriority;
import rich.util.math.TaskProcessor;

public class AngleConnection
implements IMinecraft {
    public static AngleConnection INSTANCE = new AngleConnection();
    private AngleConstructor lastRotationPlan;
    private final TaskProcessor<AngleConstructor> rotationPlanTaskProcessor = new TaskProcessor();
    public Angle currentAngle;
    private Angle previousAngle;
    private Angle serverAngle = Angle.DEFAULT;
    private Angle fakeAngle;
    private boolean returning = false;

    public AngleConnection() {
        Initialization.getInstance().getManager().getEventManager();
        EventManager.register(this);
    }

    public void setRotation(Angle value) {
        this.previousAngle = value == null ? (this.currentAngle != null ? this.currentAngle : MathAngle.cameraAngle()) : this.currentAngle;
        this.currentAngle = value;
    }

    public Angle getRotation() {
        return this.currentAngle != null ? this.currentAngle : MathAngle.cameraAngle();
    }

    public Angle getFakeRotation() {
        if (this.fakeAngle != null) {
            return this.fakeAngle;
        }
        return this.currentAngle != null ? this.currentAngle : (this.previousAngle != null ? this.previousAngle : MathAngle.cameraAngle());
    }

    public void setFakeRotation(Angle angle) {
        this.fakeAngle = angle;
    }

    public Angle getPreviousRotation() {
        return this.currentAngle != null && this.previousAngle != null ? this.previousAngle : new Angle(AngleConnection.mc.player.yRotO, AngleConnection.mc.player.xRotO);
    }

    public Angle getMoveRotation() {
        AngleConstructor rotationPlan = this.getCurrentRotationPlan();
        return this.currentAngle != null && rotationPlan != null && rotationPlan.isMoveCorrection() ? this.currentAngle : MathAngle.cameraAngle();
    }

    public AngleConstructor getCurrentRotationPlan() {
        return this.rotationPlanTaskProcessor.fetchActiveTaskValue() != null ? this.rotationPlanTaskProcessor.fetchActiveTaskValue() : this.lastRotationPlan;
    }

    public void rotateTo(Angle.VecRotation vecRotation, LivingEntity entity, int reset, AngleConfig configurable, TaskPriority taskPriority, ModuleStructure provider) {
        this.rotateTo(configurable.createRotationPlan(vecRotation.getAngle(), vecRotation.getVec(), entity, reset), taskPriority, provider);
    }

    public void rotateTo(Angle angle, int reset, AngleConfig configurable, TaskPriority taskPriority, ModuleStructure provider) {
        this.rotateTo(configurable.createRotationPlan(angle, angle.toVector(), null, reset), taskPriority, provider);
    }

    public void rotateTo(Angle angle, AngleConfig configurable, TaskPriority taskPriority, ModuleStructure provider) {
        this.rotateTo(configurable.createRotationPlan(angle, angle.toVector(), null, 1), taskPriority, provider);
    }

    public void rotateTo(AngleConstructor plan, TaskPriority taskPriority, ModuleStructure provider) {
        this.returning = false;
        this.rotationPlanTaskProcessor.addTask(new TaskProcessor.Task<AngleConstructor>(1, taskPriority.getPriority(), provider, plan));
    }

    public void update() {
        AngleConstructor activePlan = this.getCurrentRotationPlan();
        if (activePlan == null) {
            if (this.currentAngle != null && this.returning) {
                Angle cameraAngle = MathAngle.cameraAngle();
                double diff = AngleConnection.computeRotationDifference(this.currentAngle, cameraAngle);
                if (diff < 0.5) {
                    this.setRotation(null);
                    this.lastRotationPlan = null;
                    this.returning = false;
                } else {
                    float speed = 0.25f;
                    float distanceFactor = Math.min(1.0f, (float)diff / 30.0f);
                    float yawDiff = Mth.wrapDegrees((float)(cameraAngle.getYaw() - this.currentAngle.getYaw()));
                    float newYaw = this.currentAngle.getYaw() + yawDiff * (speed += 0.4f * distanceFactor);
                    float newPitch = Mth.lerp((float)speed, (float)this.currentAngle.getPitch(), (float)cameraAngle.getPitch());
                    this.setRotation(new Angle(newYaw, newPitch).adjustSensitivity());
                }
            }
            return;
        }
        this.returning = false;
        Angle clientAngle = MathAngle.cameraAngle();
        if (this.lastRotationPlan != null) {
            double differenceFromCurrentToPlayer = AngleConnection.computeRotationDifference(this.serverAngle, clientAngle);
            if (activePlan.getTicksUntilReset() <= this.rotationPlanTaskProcessor.tickCounter && differenceFromCurrentToPlayer < (double)activePlan.getResetThreshold()) {
                this.setRotation(null);
                this.lastRotationPlan = null;
                this.rotationPlanTaskProcessor.tickCounter = 0;
                return;
            }
        }
        Angle newAngle = activePlan.nextRotation(this.currentAngle != null ? this.currentAngle : clientAngle, this.rotationPlanTaskProcessor.fetchActiveTaskValue() == null).adjustSensitivity();
        this.setRotation(newAngle);
        this.lastRotationPlan = activePlan;
        this.rotationPlanTaskProcessor.tick(1);
    }

    public static double computeRotationDifference(Angle a, Angle b) {
        return Math.hypot(Math.abs(AngleConnection.computeAngleDifference(a.getYaw(), b.getYaw())), Math.abs(a.getPitch() - b.getPitch()));
    }

    public static float computeAngleDifference(float a, float b) {
        return Mth.wrapDegrees((float)(a - b));
    }

    private Vec3 fixVelocity(Vec3 currVelocity, Vec3 movementInput, float speed) {
        if (this.currentAngle != null) {
            float yaw = this.currentAngle.getYaw();
            double d = movementInput.lengthSqr();
            if (d < 1.0E-7) {
                return Vec3.ZERO;
            }
            Vec3 vec3d = (d > 1.0 ? movementInput.normalize() : movementInput).scale(speed);
            float f = Mth.sin((double)(yaw * ((float)Math.PI / 180)));
            float g = Mth.cos((double)(yaw * ((float)Math.PI / 180)));
            return new Vec3(vec3d.x() * (double)g - vec3d.z() * (double)f, vec3d.y(), vec3d.z() * (double)g + vec3d.x() * (double)f);
        }
        return currVelocity;
    }

    public void clear() {
        this.rotationPlanTaskProcessor.activeTasks.clear();
    }

    public void startReturning() {
    }

    public void reset() {
        this.currentAngle = null;
        this.previousAngle = null;
        this.fakeAngle = null;
        this.lastRotationPlan = null;
        this.rotationPlanTaskProcessor.tickCounter = 0;
    }

    @EventHandler
    public void onPlayerVelocityStrafe(PlayerVelocityStrafeEvent e) {
        AngleConstructor currentRotationPlan = this.getCurrentRotationPlan();
        if (currentRotationPlan != null && currentRotationPlan.isMoveCorrection()) {
            e.setVelocity(this.fixVelocity(e.getVelocity(), e.getMovementInput(), e.getSpeed()));
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        EventManager.callEvent(new RotationUpdateEvent((byte)0));
        this.update();
        EventManager.callEvent(new RotationUpdateEvent((byte)2));
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (!event.isCancelled()) {
            Packet<?> packet = event.getPacket();
            Objects.requireNonNull(packet);
            if (packet instanceof ServerboundMovePlayerPacket) {
                ServerboundMovePlayerPacket player = (ServerboundMovePlayerPacket)packet;
                if (player.hasRotation()) {
                    this.serverAngle = new Angle(player.getYRot(1.0f), player.getXRot(1.0f));
                }
            } else if (packet instanceof ClientboundPlayerPositionPacket) {
                ClientboundPlayerPositionPacket player = (ClientboundPlayerPositionPacket)packet;
                this.serverAngle = new Angle(player.change().yRot(), player.change().xRot());
            }
        }
    }

    @Generated
    public AngleConstructor getLastRotationPlan() {
        return this.lastRotationPlan;
    }

    @Generated
    public TaskProcessor<AngleConstructor> getRotationPlanTaskProcessor() {
        return this.rotationPlanTaskProcessor;
    }

    @Generated
    public Angle getCurrentAngle() {
        return this.currentAngle;
    }

    @Generated
    public Angle getPreviousAngle() {
        return this.previousAngle;
    }

    @Generated
    public Angle getServerAngle() {
        return this.serverAngle;
    }

    @Generated
    public Angle getFakeAngle() {
        return this.fakeAngle;
    }

    @Generated
    public boolean isReturning() {
        return this.returning;
    }
}

