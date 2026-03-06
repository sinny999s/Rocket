
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import lombok.Generated;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.move.MoveUtil;
import rich.util.timer.StopWatch;

public class Jesus
extends ModuleStructure {
    private final SelectSetting mode = new SelectSetting("Mode", "Select water movement mode").value("Matrix", "MetaHVH", "FunTime New").selected("Matrix");
    private final SliderSettings funtimeSpeed = new SliderSettings("Speed FT", "Water movement speed").range(0.01f, 0.2f).setValue(0.08f).visible(() -> this.mode.isSelected("FunTime New"));
    private final StopWatch timer = new StopWatch();
    private boolean isMoving;
    private int tickCounter = 0;
    private final float melonBallSpeed = 0.44f;

    public Jesus() {
        super("Jesus", ModuleCategory.MOVEMENT);
        this.settings(this.mode, this.funtimeSpeed);
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        this.tickCounter = 0;
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void tick(TickEvent event) {
        if (Jesus.mc.player == null || Jesus.mc.level == null) {
            return;
        }
        if (this.mode.isSelected("Matrix")) {
            this.handleMatrixMode();
        } else if (this.mode.isSelected("MetaHVH")) {
            this.handleMetaHVHMode();
        } else if (this.mode.isSelected("FunTime New")) {
            this.handleFunTimeNewMode();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleMatrixMode() {
        if (Jesus.mc.player.isInWater() || Jesus.mc.player.isInLava()) {
            MobEffectInstance speedEffect = Jesus.mc.player.getEffect(MobEffects.SPEED);
            MobEffectInstance slowEffect = Jesus.mc.player.getEffect(MobEffects.SLOWNESS);
            ItemStack offHandItem = Jesus.mc.player.getOffhandItem();
            String itemName = offHandItem.getHoverName().getString();
            float appliedSpeed = 0.0f;
            if (itemName.contains("Melon Slice") && speedEffect != null && speedEffect.getAmplifier() == 2) {
                appliedSpeed = 0.49254498f;
            } else if (speedEffect != null) {
                if (speedEffect.getAmplifier() == 2) {
                    appliedSpeed = 0.506f;
                } else if (speedEffect.getAmplifier() == 1) {
                    appliedSpeed = 0.44f;
                }
            } else {
                appliedSpeed = 0.2992f;
            }
            if (slowEffect != null) {
                appliedSpeed *= 0.85f;
            }
            MoveUtil.setVelocity(appliedSpeed);
            boolean bl = this.isMoving = Jesus.mc.options.keyUp.isDown() || Jesus.mc.options.keyDown.isDown() || Jesus.mc.options.keyLeft.isDown() || Jesus.mc.options.keyRight.isDown();
            if (!this.isMoving) {
                Jesus.mc.player.setDeltaMovement(0.0, Jesus.mc.player.getDeltaMovement().y, 0.0);
            }
            double yMotion = Jesus.mc.options.keyJump.isDown() ? 0.019 : 0.003;
            Jesus.mc.player.setDeltaMovement(Jesus.mc.player.getDeltaMovement().x, yMotion, Jesus.mc.player.getDeltaMovement().z);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleMetaHVHMode() {
        if (Jesus.mc.player.isInWater() || Jesus.mc.player.isInLava()) {
            MobEffectInstance speedEffect = Jesus.mc.player.getEffect(MobEffects.SPEED);
            MobEffectInstance slowEffect = Jesus.mc.player.getEffect(MobEffects.SLOWNESS);
            float appliedSpeed = 0.47f;
            if (speedEffect != null) {
                if (speedEffect.getAmplifier() == 2) {
                    appliedSpeed = 0.564f;
                } else if (speedEffect.getAmplifier() == 1) {
                    appliedSpeed = 0.49349996f;
                }
            } else {
                appliedSpeed = 0.329f;
            }
            if (slowEffect != null) {
                appliedSpeed *= 0.8f;
            }
            MoveUtil.setVelocity(appliedSpeed);
            boolean bl = this.isMoving = Jesus.mc.options.keyUp.isDown() || Jesus.mc.options.keyDown.isDown() || Jesus.mc.options.keyLeft.isDown() || Jesus.mc.options.keyRight.isDown();
            if (!this.isMoving) {
                Jesus.mc.player.setDeltaMovement(0.0, Jesus.mc.player.getDeltaMovement().y, 0.0);
            }
            double yMotion = Jesus.mc.options.keyJump.isDown() ? 0.025 : 0.005;
            Jesus.mc.player.setDeltaMovement(Jesus.mc.player.getDeltaMovement().x, yMotion, Jesus.mc.player.getDeltaMovement().z);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleFunTimeNewMode() {
        if (Jesus.mc.player.isInLiquid() || Jesus.mc.player.isInWater()) {
            ++this.tickCounter;
            if (this.tickCounter > 2) {
                this.tickCounter = 0;
            }
            if (MoveUtil.hasPlayerMovement()) {
                double speed = this.funtimeSpeed.getValue();
                double yaw = Math.toRadians(Jesus.mc.player.getYRot());
                double motionX = -Math.sin(yaw) * speed;
                double motionZ = Math.cos(yaw) * speed;
                double motionY = this.tickCounter == 0 ? 0.05 : (this.tickCounter == 2 ? -0.05 : 0.0);
                Jesus.mc.player.setDeltaMovement(motionX, motionY, motionZ);
            } else {
                Jesus.mc.player.setDeltaMovement(0.0, 0.0, 0.0);
            }
        } else {
            this.tickCounter = 0;
        }
    }

    @Generated
    public SelectSetting getMode() {
        return this.mode;
    }

    @Generated
    public SliderSettings getFuntimeSpeed() {
        return this.funtimeSpeed;
    }

    @Generated
    public StopWatch getTimer() {
        return this.timer;
    }

    @Generated
    public boolean isMoving() {
        return this.isMoving;
    }

    @Generated
    public int getTickCounter() {
        return this.tickCounter;
    }

    @Generated
    public float getMelonBallSpeed() {
        return this.melonBallSpeed;
    }
}

