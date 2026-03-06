
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import rich.events.api.EventHandler;
import rich.events.impl.SwimmingEvent;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;

public class WaterSpeed
extends ModuleStructure {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Select bypass mode").value("FunTime").selected("FunTime");
    private final BooleanSetting iceBoost = new BooleanSetting("Acceleration under ice", "Accelerates when head hits ice").setValue(true);
    private final SliderSettings iceBoostSpeed = new SliderSettings("Speed under ice", "Speed multiplier under ice").range(1.0f, 3.0f).setValue(1.5f).visible(() -> this.iceBoost.isValue());

    public WaterSpeed() {
        super("WaterSpeed", "Water Speed", ModuleCategory.MOVEMENT);
        this.settings(this.modeSetting, this.iceBoost, this.iceBoostSpeed);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (WaterSpeed.mc.player == null || WaterSpeed.mc.level == null) {
            return;
        }
        if (this.modeSetting.isSelected("FunTime") && WaterSpeed.mc.player.isSwimming() && WaterSpeed.mc.player.onGround()) {
            WaterSpeed.mc.player.jumpFromGround();
            WaterSpeed.mc.player.setDeltaMovement(WaterSpeed.mc.player.getDeltaMovement().x, 0.1, WaterSpeed.mc.player.getDeltaMovement().z);
        }
        if (this.iceBoost.isValue() && WaterSpeed.mc.player.isSwimming() && this.isHeadUnderIce()) {
            this.applyIceBoost();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void applyIceBoost() {
        float speedMultiplier = this.iceBoostSpeed.getValue();
        double yaw = Math.toRadians(WaterSpeed.mc.player.getYRot());
        double pitch = Math.toRadians(WaterSpeed.mc.player.getXRot());
        double baseSpeed = 0.04 * (double)speedMultiplier;
        double horizontalSpeed = Math.cos(pitch) * baseSpeed;
        double motionX = -Math.sin(yaw) * horizontalSpeed;
        double motionZ = Math.cos(yaw) * horizontalSpeed;
        WaterSpeed.mc.player.setDeltaMovement(WaterSpeed.mc.player.getDeltaMovement().x + motionX, WaterSpeed.mc.player.getDeltaMovement().y, WaterSpeed.mc.player.getDeltaMovement().z + motionZ);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onSwimming(SwimmingEvent e) {
        if (WaterSpeed.mc.player == null || WaterSpeed.mc.level == null) {
            return;
        }
        if (this.modeSetting.isSelected("FunTime")) {
            this.processSwimmingBoost(e);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void processSwimmingBoost(SwimmingEvent e) {
        if (WaterSpeed.mc.options.keyJump.isDown()) {
            float pitch = AngleConnection.INSTANCE.getRotation().getPitch();
            float boost = pitch >= 0.0f ? Mth.clamp((float)(pitch / 45.0f), (float)1.0f, (float)2.0f) : 0.5f;
            e.getVector().y = 0.1 * (double)boost;
        }
        if (this.iceBoost.isValue() && this.isHeadUnderIce()) {
            float speedMultiplier = this.iceBoostSpeed.getValue();
            e.getVector().x *= (double)speedMultiplier;
            e.getVector().z *= (double)speedMultiplier;
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isHeadUnderIce() {
        if (WaterSpeed.mc.player == null || WaterSpeed.mc.level == null) {
            return false;
        }
        BlockPos headPos = WaterSpeed.mc.player.blockPosition().above(1);
        BlockPos aboveHeadPos = WaterSpeed.mc.player.blockPosition().above(2);
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                BlockPos checkPos = headPos.offset(dx, 0, dz);
                BlockPos checkPosAbove = aboveHeadPos.offset(dx, 0, dz);
                if (!this.isIceBlock(checkPos) && !this.isIceBlock(checkPosAbove)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean isIceBlock(BlockPos pos) {
        Block block = WaterSpeed.mc.level.getBlockState(pos).getBlock();
        return block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE || block == Blocks.FROSTED_ICE;
    }
}

