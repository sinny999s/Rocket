
package rich.screens.hud;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import rich.client.draggables.AbstractHudElement;
import rich.modules.impl.render.Hud;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.item.ItemRender;

public class CustomHud
extends AbstractHudElement {

    private float animatedHealth = 0f;
    private float animatedMaxHealth = 20f;
    private float animatedFood = 20f;
    private float animatedXp = 0f;
    private float animatedAbsorption = 0f;
    private int selectedSlot = 0;
    private long lastUpdateTime = System.currentTimeMillis();

    // Colors
    private static final int BG_DARK = new Color(22, 22, 22, 220).getRGB();
    private static final int BG_LIGHT = new Color(42, 42, 42, 220).getRGB();
    private static final int OUTLINE_COL = new Color(70, 70, 70, 200).getRGB();
    private static final int SLOT_BG = new Color(28, 28, 28, 230).getRGB();
    private static final int SLOT_SELECTED = new Color(55, 55, 55, 240).getRGB();
    private static final int SELECTED_OUTLINE = new Color(200, 200, 200, 255).getRGB();
    private static final int HEALTH_COLOR = new Color(255, 70, 70, 255).getRGB();
    private static final int HEALTH_BG = new Color(80, 20, 20, 200).getRGB();
    private static final int POISON_COLOR = new Color(130, 180, 50, 255).getRGB();
    private static final int WITHER_COLOR = new Color(50, 50, 50, 255).getRGB();
    private static final int FROZEN_COLOR = new Color(100, 200, 255, 255).getRGB();
    private static final int ABSORPTION_COLOR = new Color(255, 200, 50, 255).getRGB();
    private static final int HUNGER_COLOR = new Color(180, 120, 40, 255).getRGB();
    private static final int HUNGER_BG = new Color(60, 40, 10, 200).getRGB();
    private static final int HUNGER_EFFECT_COLOR = new Color(130, 160, 50, 255).getRGB();
    private static final int XP_COLOR = new Color(100, 220, 50, 255).getRGB();
    private static final int XP_BG = new Color(30, 50, 15, 200).getRGB();
    private static final int ARMOR_COLOR = new Color(150, 170, 200, 255).getRGB();
    private static final int ARMOR_BG = new Color(40, 45, 55, 200).getRGB();
    private static final int TEXT_WHITE = new Color(255, 255, 255, 255).getRGB();
    private static final int TEXT_GRAY = new Color(170, 170, 170, 255).getRGB();
    private static final int TEXT_DIM = new Color(120, 120, 120, 255).getRGB();

    public CustomHud() {
        super("CustomHud", 0, 0, 200, 60, false);
        this.startAnimation();
    }

    @Override
    public void tick() {
    }

    private float lerp(float current, float target, float dt) {
        float factor = (float)(1.0 - Math.pow(0.001, dt * 10.0f));
        return current + (target - current) * factor;
    }

    @Override
    public void drawDraggable(GuiGraphics context, int alpha) {
        if (alpha <= 0) return;
        Player player = this.mc.player;
        if (player == null) return;

        long now = System.currentTimeMillis();
        float dt = Math.min((float)(now - this.lastUpdateTime) / 1000f, 0.1f);
        this.lastUpdateTime = now;

        // Gather player data
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float absorption = player.getAbsorptionAmount();
        FoodData foodData = player.getFoodData();
        int foodLevel = foodData.getFoodLevel();
        float xpProgress = player.experienceProgress;
        int xpLevel = player.experienceLevel;
        int armorValue = player.getArmorValue();
        this.selectedSlot = player.getInventory().getSelectedSlot();

        // Smooth animations
        this.animatedHealth = this.lerp(this.animatedHealth, health, dt);
        this.animatedMaxHealth = this.lerp(this.animatedMaxHealth, maxHealth, dt);
        this.animatedFood = this.lerp(this.animatedFood, (float) foodLevel, dt);
        this.animatedXp = this.lerp(this.animatedXp, xpProgress, dt);
        this.animatedAbsorption = this.lerp(this.animatedAbsorption, absorption, dt);

        // Layout constants
        int screenWidth = Render2D.getFixedScaledWidth();
        int screenHeight = Render2D.getFixedScaledHeight();

        float slotSize = 16f;
        float slotGap = 2f;
        float hotbarPadding = 3f;
        float hotbarWidth = 9 * slotSize + 8 * slotGap + hotbarPadding * 2;
        float hotbarHeight = slotSize + hotbarPadding * 2;
        float hotbarX = (screenWidth - hotbarWidth) / 2f;
        float hotbarY = screenHeight - hotbarHeight - 3f;

        float barHeight = 4.5f;
        float barGap = 2f;
        float halfBarWidth = (hotbarWidth - 8f) / 2f;
        float leftBarX = hotbarX + 2f;
        float rightBarX = hotbarX + 2f + halfBarWidth + 4f;

        // === VANILLA-STYLE LAYOUT ===
        // Row 1 (bottom): Health (left) + Hunger (right) — just above hotbar
        float statusBarsY = hotbarY - barHeight - 8f;
        // Row 2: Armor above health (left side only)
        float armorBarY = statusBarsY - barHeight - barGap - 1f;
        // Row 3 (below status bars): XP bar spans full width below health+hunger
        float xpBarY = statusBarsY + barHeight + 2f;

        // Total element bounds
        float topY = (armorValue > 0 ? armorBarY - 8f : statusBarsY - 8f);
        this.setX((int) hotbarX);
        this.setY((int) topY);
        this.setWidth((int) hotbarWidth);
        this.setHeight((int) (screenHeight - topY));

        // === HEALTH BAR (left side, above hotbar) ===
        int healthCol = HEALTH_COLOR;
        if (player.hasEffect(MobEffects.POISON)) {
            healthCol = POISON_COLOR;
        } else if (player.hasEffect(MobEffects.WITHER)) {
            healthCol = WITHER_COLOR;
        } else if (player.isFullyFrozen()) {
            healthCol = FROZEN_COLOR;
        }
        Render2D.rect(leftBarX, statusBarsY, halfBarWidth, barHeight, HEALTH_BG, 1.5f);
        float healthRatio = Math.max(0f, Math.min(1f, this.animatedHealth / Math.max(1f, this.animatedMaxHealth)));
        float healthFillWidth = halfBarWidth * healthRatio;
        if (healthFillWidth > 0.5f) {
            Render2D.rect(leftBarX, statusBarsY, healthFillWidth, barHeight, healthCol, 1.5f);
        }
        if (this.animatedAbsorption > 0.5f) {
            float absRatio = Math.min(1f, this.animatedAbsorption / this.animatedMaxHealth);
            float absFillWidth = halfBarWidth * absRatio;
            Render2D.rect(leftBarX, statusBarsY, absFillWidth, barHeight, ABSORPTION_COLOR, 1.5f);
        }
        String healthText = String.valueOf((int) Math.ceil(this.animatedHealth));
        if (absorption > 0) {
            healthText += " +" + (int) Math.ceil(absorption);
        }
        Fonts.BOLD.draw(healthText, leftBarX + 2f, statusBarsY - 7f, 4.5f, healthCol);

        // === HUNGER BAR (right side, above hotbar, next to health) ===
        int hungerCol = HUNGER_COLOR;
        if (player.hasEffect(MobEffects.HUNGER)) {
            hungerCol = HUNGER_EFFECT_COLOR;
        }
        Render2D.rect(rightBarX, statusBarsY, halfBarWidth, barHeight, HUNGER_BG, 1.5f);
        float hungerRatio = Math.max(0f, Math.min(1f, this.animatedFood / 20f));
        float hungerFillWidth = halfBarWidth * hungerRatio;
        if (hungerFillWidth > 0.5f) {
            float hungerFillX = rightBarX + halfBarWidth - hungerFillWidth;
            Render2D.rect(hungerFillX, statusBarsY, hungerFillWidth, barHeight, hungerCol, 1.5f);
        }
        String hungerText = String.valueOf(foodLevel);
        float hungerTextW = Fonts.BOLD.getWidth(hungerText, 4.5f);
        Fonts.BOLD.draw(hungerText, rightBarX + halfBarWidth - hungerTextW - 2f, statusBarsY - 7f, 4.5f, hungerCol);

        // === ARMOR BAR (above health bar, left side only) ===
        if (armorValue > 0) {
            Render2D.rect(leftBarX, armorBarY, halfBarWidth, barHeight, ARMOR_BG, 1.5f);
            float armorRatio = Math.max(0f, Math.min(1f, (float) armorValue / 20f));
            float armorFillWidth = halfBarWidth * armorRatio;
            if (armorFillWidth > 0.5f) {
                Render2D.rect(leftBarX, armorBarY, armorFillWidth, barHeight, ARMOR_COLOR, 1.5f);
            }
            String armorText = String.valueOf(armorValue);
            Fonts.BOLD.draw(armorText, leftBarX + 2f, armorBarY - 7f, 4.5f, ARMOR_COLOR);
        }

        // === XP BAR (full width, between status bars and hotbar) ===
        float xpFullWidth = hotbarWidth - 4f;
        Render2D.rect(leftBarX, xpBarY, xpFullWidth, barHeight, XP_BG, 1.5f);
        float xpFillWidth = xpFullWidth * Math.max(0f, Math.min(1f, this.animatedXp));
        if (xpFillWidth > 0.5f) {
            Render2D.rect(leftBarX, xpBarY, xpFillWidth, barHeight, XP_COLOR, 1.5f);
        }
        if (xpLevel > 0) {
            String lvlText = String.valueOf(xpLevel);
            Fonts.BOLD.draw(lvlText, leftBarX + 2f, xpBarY - 7f, 4.5f, new Color(100, 255, 80, 255).getRGB());
        }

        // === HOTBAR ===
        // Background panel
        Render2D.gradientRect(hotbarX, hotbarY, hotbarWidth, hotbarHeight,
                new int[]{BG_LIGHT, BG_DARK, BG_LIGHT, BG_DARK}, 4f);
        Render2D.outline(hotbarX, hotbarY, hotbarWidth, hotbarHeight, 0.35f, OUTLINE_COL, 4f);

        // Slots
        List<CountLabel> countLabels = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            float slotX = hotbarX + hotbarPadding + i * (slotSize + slotGap);
            float slotY = hotbarY + hotbarPadding;
            boolean selected = (i == this.selectedSlot);

            // Slot background
            Render2D.rect(slotX, slotY, slotSize, slotSize, selected ? SLOT_SELECTED : SLOT_BG, 2.5f);
            if (selected) {
                Render2D.outline(slotX, slotY, slotSize, slotSize, 0.5f, SELECTED_OUTLINE, 2.5f);
            }

            // Item
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                float itemSize = 12f;
                float itemX = slotX + (slotSize - itemSize) / 2f;
                float itemY = slotY + (slotSize - itemSize) / 2f;
                float itemScale = itemSize / 16f;
                if (ItemRender.needsContextRender(stack)) {
                    ItemRender.drawItemWithContext(context, stack, itemX, itemY, itemScale, 1f);
                } else {
                    ItemRender.drawItem(stack, itemX, itemY, itemScale, 1f);
                }
                int count = stack.getCount();
                if (count > 1) {
                    countLabels.add(new CountLabel(slotX, slotY, count));
                }

                // Durability bar for damageable items
                if (stack.isDamageableItem()) {
                    int maxDmg = stack.getMaxDamage();
                    int dmg = stack.getDamageValue();
                    float durRatio = 1f - (float) dmg / (float) maxDmg;
                    float durBarWidth = (slotSize - 4f) * durRatio;
                    float durBarX = slotX + 2f;
                    float durBarY = slotY + slotSize - 3f;
                    // Durability color: green -> yellow -> red
                    int durColor = getDurabilityColor(durRatio);
                    Render2D.rect(durBarX, durBarY, slotSize - 4f, 1.5f, new Color(20, 20, 20, 200).getRGB(), 0.5f);
                    if (durBarWidth > 0.3f) {
                        Render2D.rect(durBarX, durBarY, durBarWidth, 1.5f, durColor, 0.5f);
                    }
                }
            }
        }

        // Draw item count labels on top
        for (CountLabel label : countLabels) {
            String countText = String.valueOf(label.count);
            float countW = Fonts.BOLD.getWidth(countText, 4.5f);
            Fonts.BOLD.draw(countText, label.slotX + slotSize - countW - 1f, label.slotY + slotSize - 6f, 4.5f, TEXT_WHITE);
        }

        // Held item name
        ItemStack heldStack = player.getInventory().getItem(this.selectedSlot);
        if (!heldStack.isEmpty()) {
            String itemName = heldStack.getHoverName().getString();
            Fonts.BOLD.drawCentered(itemName, hotbarX + hotbarWidth / 2f, hotbarY - 9f, 5f, TEXT_WHITE);
        }
    }

    private static int getDurabilityColor(float ratio) {
        if (ratio > 0.66f) {
            return new Color(80, 220, 80, 255).getRGB();
        } else if (ratio > 0.33f) {
            return new Color(220, 220, 50, 255).getRGB();
        } else {
            return new Color(220, 50, 50, 255).getRGB();
        }
    }

    private record CountLabel(float slotX, float slotY, int count) {
    }
}
