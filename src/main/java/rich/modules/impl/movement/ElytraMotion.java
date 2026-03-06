/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  lombok.Generated
 */
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import java.util.Objects;
import java.util.Random;
import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.Aura;
import rich.modules.impl.movement.Fly;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.Instance;
import rich.util.timer.StopWatch;

public class ElytraMotion
extends ModuleStructure {
    private StopWatch timer = new StopWatch();
    private Vec3 targetPosition = null;
    private Random random = new Random();
    private double rotationAngle = 0.0;

    public static Fly getInstance() {
        return Instance.get(Fly.class);
    }

    public ElytraMotion() {
        super("ElytraMotion", "Elytra Motion", ModuleCategory.MOVEMENT);
        this.settings();
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (!this.state || ElytraMotion.mc.player == null || ElytraMotion.mc.level == null || !ElytraMotion.mc.player.isFallFlying()) {
            return;
        }
        Aura aura = Instance.get(Aura.class);
        if (aura.isState()) {
            this.handleAuraMotion(aura);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleAuraMotion(Aura aura) {
        if (aura.isState()) {
            if (Aura.target != null) {
                if (ElytraMotion.mc.player.distanceTo(Aura.target) < aura.getAttackrange().getValue() - 1.0f) {
                    ElytraMotion.mc.player.setDeltaMovement(0.0, 0.02, 0.0);
                }
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        Aura aura = Instance.get(Aura.class);
        if (aura.isState()) {
            if (Aura.target != null) {
                if (ElytraMotion.mc.player.distanceTo(Aura.target) < aura.getAttackrange().getValue() - 1.0f) {
                    Packet<?> packet = e.getPacket();
                    Objects.requireNonNull(packet);
                }
            }
        }
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        super.deactivate();
    }

    @Generated
    public StopWatch getTimer() {
        return this.timer;
    }

    @Generated
    public Vec3 getTargetPosition() {
        return this.targetPosition;
    }

    @Generated
    public Random getRandom() {
        return this.random;
    }

    @Generated
    public double getRotationAngle() {
        return this.rotationAngle;
    }
}

