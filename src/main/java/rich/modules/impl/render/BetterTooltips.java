package rich.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import rich.events.api.EventHandler;
import rich.events.impl.HandledScreenEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.Instance;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.item.ItemRender;

public class BetterTooltips extends ModuleStructure {

    public final BooleanSetting shulkerPreview = new BooleanSetting("Shulker Preview", "Show shulker box contents as visual grid").setValue(true);
    public final BooleanSetting foodInfo = new BooleanSetting("Food Info", "Show hunger and saturation values").setValue(true);

    private static BetterTooltips instance;

    private static final float SLOT_SIZE = 14f;
    private static final float SLOT_GAP = 1.5f;
    private static final float PADDING = 4f;
    private static final float ITEM_SCALE = 10f / 16f;
    private static final int COLS = 9;
    private static final int ROWS = 3;

    public BetterTooltips() {
        super("Better Tooltips", "Enhanced item tooltips", ModuleCategory.RENDER);
        instance = this;
        this.settings(this.shulkerPreview, this.foodInfo);
    }

    public static BetterTooltips getInstance() {
        if (instance == null) {
            instance = Instance.get(BetterTooltips.class);
        }
        return instance;
    }

    public void modifyTooltip(ItemStack stack, List<Component> tooltip) {
        if (!this.state) return;

        if (this.foodInfo.isValue()) {
            FoodProperties food = stack.get(DataComponents.FOOD);
            if (food != null) {
                tooltip.add(Component.literal(String.format("Hunger: %d | Saturation: %.1f", food.nutrition(), food.saturation()))
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    @EventHandler
    public void onHandledScreen(HandledScreenEvent event) {
        if (!this.state || !this.shulkerPreview.isValue()) return;

        Slot hoveredSlot = event.getSlotHover();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) return;

        ItemStack stack = hoveredSlot.getItem();
        if (!isShulkerBox(stack)) return;

        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) return;

        List<ItemStack> items = new ArrayList<>();
        for (ItemStack s : contents.nonEmptyItems()) {
            items.add(s);
        }

        ItemStack[] slots = new ItemStack[27];
        for (int i = 0; i < 27; i++) {
            slots[i] = ItemStack.EMPTY;
        }

        int idx = 0;
        for (ItemStack item : contents.nonEmptyItems()) {
            if (idx < 27) {
                slots[idx] = item;
            }
            idx++;
        }

        GuiGraphics context = event.getDrawContext();
        Minecraft mc = Minecraft.getInstance();

        float gridW = COLS * SLOT_SIZE + (COLS - 1) * SLOT_GAP + PADDING * 2;
        float gridH = ROWS * SLOT_SIZE + (ROWS - 1) * SLOT_GAP + PADDING * 2 + 10f;

        int guiScale = mc.getWindow().calculateScale(mc.options.guiScale().get(), mc.isEnforceUnicode());
        float scaleFactor = 2.0f / (float) guiScale;

        float mouseX = (float) mc.mouseHandler.xpos() / mc.getWindow().getWidth() * mc.getWindow().getScreenWidth() / 2f;
        float mouseY = (float) mc.mouseHandler.ypos() / mc.getWindow().getHeight() * mc.getWindow().getScreenHeight() / 2f;

        float panelX = mouseX + 8f;
        float panelY = mouseY - gridH - 4f;

        int sw = Render2D.getFixedScaledWidth();
        int sh = Render2D.getFixedScaledHeight();
        if (panelX + gridW > sw) panelX = mouseX - gridW - 4f;
        if (panelY < 2f) panelY = mouseY + 16f;

        int bgAlpha = 220;
        Render2D.blur(panelX, panelY, gridW, gridH, 12f, 5f, new Color(0, 0, 0, 50).getRGB());
        Render2D.gradientRect(panelX, panelY, gridW, gridH,
                new int[]{
                        new Color(35, 35, 40, bgAlpha).getRGB(),
                        new Color(25, 25, 30, bgAlpha).getRGB(),
                        new Color(35, 35, 40, bgAlpha).getRGB(),
                        new Color(25, 25, 30, bgAlpha).getRGB()
                }, 5f);
        Render2D.outline(panelX, panelY, gridW, gridH, 0.4f,
                new Color(100, 100, 110, 130).getRGB(), 5f);

        String title = stack.getHoverName().getString();
        Fonts.BOLD.draw(title, panelX + PADDING, panelY + 2f, 5f, new Color(220, 220, 225, 255).getRGB());

        float startX = panelX + PADDING;
        float startY = panelY + 10f;

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int slotIdx = row * COLS + col;
                float slotX = startX + col * (SLOT_SIZE + SLOT_GAP);
                float slotY = startY + row * (SLOT_SIZE + SLOT_GAP);

                Render2D.rect(slotX, slotY, SLOT_SIZE, SLOT_SIZE,
                        new Color(20, 20, 22, 200).getRGB(), 2f);

                if (slotIdx < 27 && !slots[slotIdx].isEmpty()) {
                    ItemStack item = slots[slotIdx];
                    float itemX = slotX + (SLOT_SIZE - 10f) / 2f;
                    float itemY = slotY + (SLOT_SIZE - 10f) / 2f;

                    if (ItemRender.needsContextRender(item)) {
                        ItemRender.drawItemWithContext(context, item, itemX, itemY, ITEM_SCALE, 1f);
                    } else {
                        ItemRender.drawItem(item, itemX, itemY, ITEM_SCALE, 1f);
                    }

                    int count = item.getCount();
                    if (count > 1) {
                        String countText = String.valueOf(count);
                        float countW = Fonts.BOLD.getWidth(countText, 4f);
                        Fonts.BOLD.draw(countText, slotX + SLOT_SIZE - countW - 1f, slotY + SLOT_SIZE - 5.5f,
                                4f, new Color(255, 255, 255, 255).getRGB());
                    }
                }
            }
        }
    }

    private static boolean isShulkerBox(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock() instanceof ShulkerBoxBlock;
        }
        return false;
    }
}
