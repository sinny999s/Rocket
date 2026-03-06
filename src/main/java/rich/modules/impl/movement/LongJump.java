
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import lombok.Generated;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlimeBlock;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.timer.StopWatch;

public class LongJump
extends ModuleStructure {
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Jump mode").value("Boat", "Shulker Screen", "Slime Boost", "FunTime Soul Sand").selected("Always");
    private boolean wasInShulkerScreen = false;
    private boolean wasOnSlimeBlock = false;
    private final StopWatch timer = new StopWatch();

    public LongJump() {
        super("LongJump", "Long Jump", ModuleCategory.MOVEMENT);
        this.settings(this.modeSetting);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    private void tickEvent(TickEvent event) {
        if (this.modeSetting.isSelected("Shulker Screen")) {
            this.handleShulkerScreen();
        }
        if (this.modeSetting.isSelected("FunTime Soul Sand") && LongJump.mc.player.isInWater() && !LongJump.mc.player.isUnderWater()) {
            LongJump.mc.player.push(0.0, 0.56, 0.0);
        }
        if (this.modeSetting.isSelected("Boat")) {
            this.handleBoatMode();
        }
        if (this.modeSetting.isSelected("Slime Boost")) {
            this.handleSlimeBoost();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void handleShulkerScreen() {
        if (LongJump.mc.screen instanceof ShulkerBoxScreen) {
            StopWatch speedTimer = new StopWatch();
            float speed = 0.9f;
            LongJump.mc.player.push(0.0, speed, 0.0);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleBoatMode() {
        if (LongJump.mc.screen instanceof ShulkerBoxScreen) {
            float yaw = (float)Math.toRadians(LongJump.mc.player.getYRot());
            double x = -Math.sin(yaw) * 1.0;
            double z = Math.cos(yaw) * 1.0;
            LongJump.mc.player.push(0.0, 1.0, 0.0);
            LongJump.mc.player.setPosRaw(LongJump.mc.player.getX(), LongJump.mc.player.getY() + 0.24, LongJump.mc.player.getZ());
        }
        if (LongJump.mc.screen instanceof ShulkerBoxScreen) {
            this.wasInShulkerScreen = true;
        } else if (this.wasInShulkerScreen && LongJump.mc.screen == null && this.isNearShulkerBox()) {
            this.wasInShulkerScreen = false;
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void handleSlimeBoost() {
        if (LongJump.mc.player.onGround() && this.isOnSlimeBlock()) {
            this.wasOnSlimeBlock = true;
        } else if (this.wasOnSlimeBlock && !LongJump.mc.player.onGround() && LongJump.mc.player.getDeltaMovement().y() > 0.0) {
            LongJump.mc.player.push(0.0, 1.35, 0.0);
            this.wasOnSlimeBlock = false;
        } else if (!this.isOnSlimeBlock()) {
            this.wasOnSlimeBlock = false;
        }
    }

    private boolean isNearShulkerBox() {
        BlockPos playerPos = LongJump.mc.player.blockPosition();
        for (int x = -1; x <= 1; ++x) {
            for (int y = -1; y <= 1; ++y) {
                for (int z = -1; z <= 1; ++z) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    if (!(LongJump.mc.level.getBlockState(pos).getBlock() instanceof ShulkerBoxBlock)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isOnSlimeBlock() {
        BlockPos playerPos = LongJump.mc.player.blockPosition();
        BlockPos belowPos = playerPos.below();
        return LongJump.mc.level.getBlockState(belowPos).getBlock() instanceof SlimeBlock;
    }

    @Generated
    public SelectSetting getModeSetting() {
        return this.modeSetting;
    }

    @Generated
    public boolean isWasInShulkerScreen() {
        return this.wasInShulkerScreen;
    }

    @Generated
    public boolean isWasOnSlimeBlock() {
        return this.wasOnSlimeBlock;
    }

    @Generated
    public StopWatch getTimer() {
        return this.timer;
    }
}

