
package rich.modules.impl.combat.macetarget.stage;

import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import rich.modules.impl.combat.macetarget.armor.ArmorSwapHandler;
import rich.modules.impl.combat.macetarget.armor.FireworkHandler;
import rich.modules.impl.combat.macetarget.attack.AttackHandler;
import rich.modules.impl.combat.macetarget.state.MaceState;
import rich.util.inventory.InventoryUtils;
import rich.util.timer.StopWatch;

public class StageHandler {
    private static final Minecraft mc = Minecraft.getInstance();
    private final ArmorSwapHandler armorSwapHandler;
    private final FireworkHandler fireworkHandler;
    private final AttackHandler attackHandler;
    private final StopWatch fireworkTimer;
    private MaceState.Stage stage = MaceState.Stage.PREPARE;
    private boolean silentMode = true;
    private boolean reallyWorldMode = false;
    private float height = 30.0f;

    public StageHandler(ArmorSwapHandler armorSwapHandler, FireworkHandler fireworkHandler, AttackHandler attackHandler, StopWatch fireworkTimer) {
        this.armorSwapHandler = armorSwapHandler;
        this.fireworkHandler = fireworkHandler;
        this.attackHandler = attackHandler;
        this.fireworkTimer = fireworkTimer;
    }

    public void handlePrepare(boolean hasElytra) {
        if (!hasElytra) {
            int slot = InventoryUtils.findElytraSlot();
            if (slot != -1) {
                this.armorSwapHandler.startSwap(slot, this.silentMode);
            }
            return;
        }
        this.stage = MaceState.Stage.FLYING_UP;
        this.fireworkTimer.reset();
    }

    public void handleFlyingUp(LivingEntity target, boolean hasElytra) {
        if (!hasElytra) {
            this.stage = MaceState.Stage.PREPARE;
            return;
        }
        if (StageHandler.mc.player.isFallFlying() && this.fireworkTimer.finished(300.0)) {
            this.fireworkHandler.useFirework(this.silentMode);
            this.fireworkTimer.reset();
        }
        if (StageHandler.mc.player.getY() - target.getY() >= (double)this.height) {
            this.stage = MaceState.Stage.TARGETTING;
        }
    }

    public void handleTargetting(LivingEntity target) {
        int slot;
        float swapDistance = 12.0f;
        if (InventoryUtils.hasElytra() && StageHandler.mc.player.distanceTo(target) < swapDistance && !this.armorSwapHandler.isActive() && (slot = InventoryUtils.findChestArmorSlot()) != -1) {
            this.armorSwapHandler.startSwap(slot, this.silentMode);
        }
        if (StageHandler.mc.player.distanceTo(target) < 16.0f) {
            this.stage = MaceState.Stage.ATTACKING;
        }
    }

    public void handleAttacking(LivingEntity target, boolean hasElytra) {
        if (hasElytra && !this.armorSwapHandler.isActive()) {
            int slot = InventoryUtils.findChestArmorSlot();
            if (slot != -1) {
                this.armorSwapHandler.startSwap(slot, this.silentMode);
            }
            return;
        }
        if (!hasElytra && !this.armorSwapHandler.isActive() && StageHandler.mc.player.distanceTo(target) < 5.0f) {
            this.attackHandler.setPendingAttack(true);
            if (this.reallyWorldMode) {
                this.attackHandler.setShouldDisableAfterAttack(true);
            } else {
                this.stage = MaceState.Stage.FLYING_UP;
                this.fireworkTimer.reset();
            }
        }
    }

    public void reset() {
        this.stage = MaceState.Stage.PREPARE;
    }

    @Generated
    public ArmorSwapHandler getArmorSwapHandler() {
        return this.armorSwapHandler;
    }

    @Generated
    public FireworkHandler getFireworkHandler() {
        return this.fireworkHandler;
    }

    @Generated
    public AttackHandler getAttackHandler() {
        return this.attackHandler;
    }

    @Generated
    public StopWatch getFireworkTimer() {
        return this.fireworkTimer;
    }

    @Generated
    public MaceState.Stage getStage() {
        return this.stage;
    }

    @Generated
    public boolean isSilentMode() {
        return this.silentMode;
    }

    @Generated
    public boolean isReallyWorldMode() {
        return this.reallyWorldMode;
    }

    @Generated
    public float getHeight() {
        return this.height;
    }

    @Generated
    public void setStage(MaceState.Stage stage) {
        this.stage = stage;
    }

    @Generated
    public void setSilentMode(boolean silentMode) {
        this.silentMode = silentMode;
    }

    @Generated
    public void setReallyWorldMode(boolean reallyWorldMode) {
        this.reallyWorldMode = reallyWorldMode;
    }

    @Generated
    public void setHeight(float height) {
        this.height = height;
    }
}

