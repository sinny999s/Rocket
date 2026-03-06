package rich.modules.impl.player;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.Instance;

public class AutoEat extends ModuleStructure {

    private final SliderSettings hungerThreshold = new SliderSettings("Hunger Threshold", "Start eating at this hunger level")
            .range(1, 19).setValue(14);
    private final SliderSettings healthThreshold = new SliderSettings("Health Threshold", "Start eating at this health")
            .range(1, 19).setValue(10);
    private final BooleanSetting preferGaps = new BooleanSetting("Prefer Gaps", "Prefer golden apples").setValue(false);

    private boolean eating = false;
    private int previousSlot = -1;

    public AutoEat() {
        super("Auto Eat", "Automatically eats food when hungry", ModuleCategory.PLAYER);
        this.settings(this.hungerThreshold, this.healthThreshold, this.preferGaps);
    }

    public static AutoEat getInstance() {
        return Instance.get(AutoEat.class);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!this.state || AutoEat.mc.player == null || AutoEat.mc.level == null) return;
        if (AutoEat.mc.player.isCreative() || AutoEat.mc.player.isSpectator()) return;

        FoodData food = AutoEat.mc.player.getFoodData();
        float health = AutoEat.mc.player.getHealth();

        boolean shouldEat = food.getFoodLevel() <= this.hungerThreshold.getInt()
                || health <= this.healthThreshold.getInt();

        if (this.eating) {
            if (!shouldEat || !AutoEat.mc.player.isUsingItem()) {
                stopEating();
                return;
            }
            ItemStack held = AutoEat.mc.player.getMainHandItem();
            if (held.get(DataComponents.FOOD) == null) {
                stopEating();
            }
            return;
        }

        if (shouldEat && food.needsFood()) {
            int slot = findBestFood();
            if (slot != -1) {
                startEating(slot);
            }
        }
    }

    private void startEating(int slot) {
        this.previousSlot = AutoEat.mc.player.getInventory().getSelectedSlot();
        AutoEat.mc.player.getInventory().setSelectedSlot(slot);
        AutoEat.mc.options.keyUse.setDown(true);
        this.eating = true;
    }

    private void stopEating() {
        AutoEat.mc.options.keyUse.setDown(false);
        if (this.previousSlot != -1) {
            AutoEat.mc.player.getInventory().setSelectedSlot(this.previousSlot);
            this.previousSlot = -1;
        }
        this.eating = false;
    }

    private int findBestFood() {
        int bestSlot = -1;
        float bestValue = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = AutoEat.mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food == null) continue;

            if (isBlacklisted(stack)) continue;

            float value = food.nutrition() + food.saturation();
            if (this.preferGaps.isValue() && (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE))) {
                value += 100;
            }

            if (value > bestValue) {
                bestValue = value;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

    private boolean isBlacklisted(ItemStack stack) {
        return stack.is(Items.POISONOUS_POTATO)
                || stack.is(Items.PUFFERFISH)
                || stack.is(Items.CHICKEN)
                || stack.is(Items.ROTTEN_FLESH)
                || stack.is(Items.SPIDER_EYE)
                || stack.is(Items.SUSPICIOUS_STEW)
                || stack.is(Items.CHORUS_FRUIT);
    }

    @Override
    public void deactivate() {
        if (this.eating) {
            stopEating();
        }
    }
}
