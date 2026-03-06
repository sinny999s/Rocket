
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.FireworkEvent;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;

public class SuperFireWork
extends ModuleStructure {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Select mode type").value("BravoHvH", "ReallyWorld", "PulseHVH", "Custom");
    private final SliderSettings customSpeedSetting = new SliderSettings("Speed", "Speed for Custom mode").range(1.5f, 3.0f).setValue(1.963f).visible(() -> this.modeSetting.isSelected("Custom"));
    private final BooleanSetting nearBoostSetting = new BooleanSetting("", "");

    public SuperFireWork() {
        super("SuperFireWork", "Super FireWork", ModuleCategory.MOVEMENT);
        this.settings(this.modeSetting, this.customSpeedSetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onFirework(FireworkEvent e) {
        if (SuperFireWork.mc.player == null || !SuperFireWork.mc.player.isFallFlying()) {
            return;
        }
        float yaw = AngleConnection.INSTANCE.getRotation().getYaw() % 360.0f;
        if (yaw < 0.0f) {
            yaw += 360.0f;
        }
        if (this.modeSetting.isSelected("ReallyWorld")) {
            this.handleReallyWorldMode(e, yaw);
        } else if (this.modeSetting.isSelected("BravoHvH")) {
            this.handleBravoHvHMode(e, yaw);
        } else if (this.modeSetting.isSelected("PulseHVH")) {
            this.handlePulseHVHMode(e, yaw);
        } else if (this.modeSetting.isSelected("Custom")) {
            this.handleCustomMode(e, yaw);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleReallyWorldMode(FireworkEvent e, float yaw) {
        float[] diagonals = new float[]{45.0f, 135.0f, 225.0f, 315.0f};
        float closestDiff = 180.0f;
        for (float d : diagonals) {
            float diff = Math.abs(yaw - d);
            if (!((diff = Math.min(diff, 360.0f - diff)) < closestDiff)) continue;
            closestDiff = diff;
        }
        double speedXZ = 1.5;
        double speedY = 1.5;
        if (closestDiff <= 4.0f) {
            speedXZ = 2.2;
        } else if (closestDiff <= 8.0f) {
            speedXZ = 2.06;
        } else if (closestDiff <= 12.0f) {
            speedXZ = 1.98;
        } else if (closestDiff <= 16.0f) {
            speedXZ = 1.87;
        } else if (closestDiff <= 20.0f) {
            speedXZ = 1.8;
        } else if (closestDiff <= 24.0f) {
            speedXZ = 1.74;
        } else if (closestDiff <= 28.0f) {
            speedXZ = 1.7;
        } else if (closestDiff <= 32.0f) {
            speedXZ = 1.65;
        } else if (closestDiff <= 36.0f) {
            speedXZ = 1.63;
        } else {
            speedXZ = 1.61;
            speedY = 1.61;
        }
        this.applyFireworkVelocity(e, speedXZ, speedY);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleBravoHvHMode(FireworkEvent e, float yaw) {
        double speedXZ;
        boolean isDiagonal = this.checkDiagonal(yaw, 16.0f);
        boolean nearPlayer = this.checkNearPlayer(4.0f);
        double speedY = 1.66;
        if (isDiagonal) {
            speedXZ = 1.963;
        } else if (this.nearBoostSetting.isValue() && nearPlayer) {
            speedXZ = 1.82;
            speedY = 1.67;
        } else {
            speedXZ = 1.675;
        }
        this.applyFireworkVelocity(e, speedXZ, speedY);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handlePulseHVHMode(FireworkEvent e, float yaw) {
        double speedXZ;
        boolean isDiagonal = this.checkDiagonal(yaw, 16.0f);
        boolean nearPlayer = this.checkNearPlayer(5.0f);
        double speedY = 1.66;
        if (isDiagonal) {
            speedXZ = 1.963;
        } else if (this.nearBoostSetting.isValue() && nearPlayer) {
            speedXZ = 1.82;
            speedY = 1.67;
        } else {
            speedXZ = 1.675;
        }
        this.applyFireworkVelocity(e, speedXZ, speedY);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleCustomMode(FireworkEvent e, float yaw) {
        double speedXZ;
        boolean isDiagonal = this.checkDiagonal(yaw, 16.0f);
        boolean nearPlayer = this.checkNearPlayer(5.0f);
        double speedY = 1.66;
        if (isDiagonal) {
            speedXZ = this.customSpeedSetting.getValue();
        } else if (this.nearBoostSetting.isValue() && nearPlayer) {
            speedXZ = this.customSpeedSetting.getValue() - 0.1f;
            speedY = 1.67;
        } else {
            speedXZ = 1.675;
        }
        this.applyFireworkVelocity(e, speedXZ, speedY);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean checkDiagonal(float yaw, float threshold) {
        for (float d : new float[]{45.0f, 135.0f, 225.0f, 315.0f}) {
            float diff = Math.abs(yaw - d);
            if (!((diff = Math.min(diff, 360.0f - diff)) <= threshold)) continue;
            return true;
        }
        return false;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean checkNearPlayer(float distance) {
        if (!this.nearBoostSetting.isValue() || SuperFireWork.mc.level == null) {
            return false;
        }
        for (Player player : SuperFireWork.mc.level.players()) {
            if (player == SuperFireWork.mc.player || !(player.distanceTo(SuperFireWork.mc.player) <= distance)) continue;
            return true;
        }
        return false;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void applyFireworkVelocity(FireworkEvent e, double speedXZ, double speedY) {
        Vec3 rotationVector = AngleConnection.INSTANCE.getMoveRotation().toVector();
        Vec3 currentVelocity = e.getVector();
        e.setVector(currentVelocity.add(rotationVector.x * 0.1 + (rotationVector.x * speedXZ - currentVelocity.x) * 0.5, rotationVector.y * 0.1 + (rotationVector.y * speedY - currentVelocity.y) * 0.5, rotationVector.z * 0.1 + (rotationVector.z * speedXZ - currentVelocity.z) * 0.5));
    }
}

