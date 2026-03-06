
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import lombok.Generated;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;

public class AutoGApple
extends ModuleStructure {
    private final SliderSettings healthThreshold = new SliderSettings("Health", "Health threshold for eating").range(3.0f, 20.0f).setValue(16.0f);
    private final BooleanSetting smartMode = new BooleanSetting("Smart", "Eat from hotbar, otherwise only offhand").setValue(true);
    private final BooleanSetting goldenHearts = new BooleanSetting("Golden hearts", "Consider absorption").setValue(true);
    private final BooleanSetting returnSlot = new BooleanSetting("Return slot", "Return to previous slot after eating").setValue(true);
    private boolean isEating = false;
    private int previousSlot = -1;

    public AutoGApple() {
        super("AutoGApple", "Auto GApple", ModuleCategory.COMBAT);
        this.settings(this.healthThreshold, this.smartMode, this.goldenHearts, this.returnSlot);
    }

    @Override
    public void activate() {
        this.isEating = false;
        this.previousSlot = -1;
    }

    @Override
    public void deactivate() {
        this.stopEating();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AutoGApple.mc.player == null || AutoGApple.mc.level == null) {
            return;
        }
        this.handleEating();
    }

    private void handleEating() {
        if (this.canEat()) {
            if (this.smartMode.isValue()) {
                if (!this.hasGappleInHand()) {
                    this.swapToGappleSlot();
                }
                this.startEating();
            } else if (this.hasGappleInOffhand()) {
                this.startEating();
            }
        } else if (this.isEating && !this.shouldContinueEating()) {
            this.stopEating();
        }
    }

    private boolean hasGappleInHand() {
        ItemStack mainHand = AutoGApple.mc.player.getMainHandItem();
        return mainHand.getItem() == Items.GOLDEN_APPLE;
    }

    private boolean canEat() {
        if (AutoGApple.mc.player.isDeadOrDying()) {
            return false;
        }
        if (AutoGApple.mc.player.getCooldowns().isOnCooldown(Items.GOLDEN_APPLE.getDefaultInstance())) {
            return false;
        }
        if (this.smartMode.isValue() ? !this.hasGapple() : !this.hasGappleInOffhand()) {
            return false;
        }
        float health = this.getEffectiveHealth();
        return health <= this.healthThreshold.getValue();
    }

    private boolean shouldContinueEating() {
        if (AutoGApple.mc.player.isDeadOrDying()) {
            return false;
        }
        if (!AutoGApple.mc.player.isUsingItem()) {
            return false;
        }
        ItemStack usingItem = AutoGApple.mc.player.getUseItem();
        return usingItem.getItem() == Items.GOLDEN_APPLE;
    }

    private float getEffectiveHealth() {
        float health = AutoGApple.mc.player.getHealth();
        if (this.goldenHearts.isValue()) {
            health += AutoGApple.mc.player.getAbsorptionAmount();
        }
        return health;
    }

    private boolean hasGappleInOffhand() {
        ItemStack offhandStack = AutoGApple.mc.player.getOffhandItem();
        return offhandStack.getItem() == Items.GOLDEN_APPLE;
    }

    private boolean hasGappleInHotbar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoGApple.mc.player.getInventory().getItem(i);
            if (stack.getItem() != Items.GOLDEN_APPLE) continue;
            return true;
        }
        return false;
    }

    private boolean hasGapple() {
        return this.hasGappleInOffhand() || this.hasGappleInHotbar();
    }

    private int findGappleInHotbar() {
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = AutoGApple.mc.player.getInventory().getItem(i);
            if (stack.getItem() != Items.GOLDEN_APPLE) continue;
            return i;
        }
        return -1;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void swapToGappleSlot() {
        int gappleSlot = this.findGappleInHotbar();
        if (gappleSlot == -1) {
            return;
        }
        int currentSlot = AutoGApple.mc.player.getInventory().getSelectedSlot();
        if (currentSlot != gappleSlot) {
            if (this.previousSlot == -1 && this.returnSlot.isValue()) {
                this.previousSlot = currentSlot;
            }
            AutoGApple.mc.player.getInventory().setSelectedSlot(gappleSlot);
        }
    }

    private void startEating() {
        if (!this.isEating && !AutoGApple.mc.options.keyUse.isDown()) {
            AutoGApple.mc.options.keyUse.setDown(true);
            this.isEating = true;
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void stopEating() {
        if (this.isEating) {
            AutoGApple.mc.options.keyUse.setDown(false);
            this.isEating = false;
            if (this.previousSlot != -1 && this.returnSlot.isValue()) {
                AutoGApple.mc.player.getInventory().setSelectedSlot(this.previousSlot);
                this.previousSlot = -1;
            }
        }
    }

    @Generated
    public SliderSettings getHealthThreshold() {
        return this.healthThreshold;
    }

    @Generated
    public BooleanSetting getSmartMode() {
        return this.smartMode;
    }

    @Generated
    public BooleanSetting getGoldenHearts() {
        return this.goldenHearts;
    }

    @Generated
    public BooleanSetting getReturnSlot() {
        return this.returnSlot;
    }

    @Generated
    public boolean isEating() {
        return this.isEating;
    }

    @Generated
    public int getPreviousSlot() {
        return this.previousSlot;
    }
}

