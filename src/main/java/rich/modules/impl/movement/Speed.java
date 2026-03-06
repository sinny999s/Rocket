
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.vehicle.boat.Boat;
import rich.events.api.EventHandler;
import rich.events.impl.PlayerTravelEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.move.MoveUtil;

public class Speed
extends ModuleStructure {
    private final SelectSetting mode = new SelectSetting("Mode", "Select speed mode").value("Vanilla", "Grim", "FunTime", "HolyWorld").selected("Grim");
    private final SliderSettings speed = new SliderSettings("Speed", "Movement speed setting").range(1.0f, 5.0f).setValue(1.5f).visible(() -> this.mode.isSelected("Vanilla"));

    public Speed() {
        super("Speed", "Speed", ModuleCategory.MOVEMENT);
        this.settings(this.mode, this.speed);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onTick(TickEvent e) {
        if (this.mode.isSelected("Vanilla")) {
            MoveUtil.setVelocity(this.speed.getValue() / 3.0f);
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onMotion(PlayerTravelEvent e) {
        if (this.mode.isSelected("FunTime")) {
            this.handleFunTimeMode();
        }
        if (this.mode.isSelected("Grim") && e.isPre() && MoveUtil.hasPlayerMovement()) {
            this.handleGrimMode();
        }
        if (this.mode.isSelected("HolyWorld") && e.isPre() && MoveUtil.hasPlayerMovement()) {
            this.handleHolyWorldMode();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleFunTimeMode() {
        if (!Speed.mc.player.isSwimming() && !Speed.mc.player.isFallFlying() && !Speed.mc.player.isShiftKeyDown() && Speed.mc.player.getBoundingBox().maxY - Speed.mc.player.getBoundingBox().minY < 1.5) {
            float motion = Speed.mc.player.hasEffect(MobEffects.SPEED) ? 0.32f : 0.28f;
            MoveUtil.setVelocity(motion);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleGrimMode() {
        int collisions = 0;
        for (Entity ent : Speed.mc.level.entitiesForRendering()) {
            if (ent == Speed.mc.player || ent instanceof ArmorStand || !(ent instanceof LivingEntity) && !(ent instanceof Boat) || !Speed.mc.player.getBoundingBox().inflate(0.5).intersects(ent.getBoundingBox())) continue;
            ++collisions;
        }
        double[] motion = MoveUtil.forward(0.07 * (double)collisions);
        Speed.mc.player.push(motion[0], 0.0, motion[1]);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleHolyWorldMode() {
        int collisions = 0;
        for (Entity ent : Speed.mc.level.entitiesForRendering()) {
            if (ent == Speed.mc.player || ent instanceof ArmorStand || !(ent instanceof LivingEntity) && !(ent instanceof Boat) || !Speed.mc.player.getBoundingBox().inflate(0.35f).intersects(ent.getBoundingBox())) continue;
            ++collisions;
        }
        double[] motion = MoveUtil.forward(0.0205 * (double)collisions);
        Speed.mc.player.push(motion[0], 0.0, motion[1]);
    }

    private boolean hasSprintingTarget() {
        return false;
    }
}

