/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.joml.Matrix3x2fStack
 */
package rich.screens.clickgui.impl.autobuy.autobuyui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import rich.IMinecraft;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.ItemRegistry;
import rich.screens.clickgui.impl.autobuy.manager.AutoBuyManager;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.item.ItemRender;
import rich.util.render.shader.Scissor;

public class AutoBuyGuiComponent
implements IMinecraft {
    private static final int FORCED_GUI_SCALE = 2;
    private float x;
    private float y;
    private float width;
    private float height;
    private float targetScroll = 0.0f;
    private float smoothScroll = 0.0f;
    private float slideOffsetX = 0.0f;
    private float targetSlideOffsetX = 0.0f;
    private boolean slidingOut = false;
    private static final float SLIDE_SPEED = 20.0f;
    private final Map<AutoBuyableItem, Float> toggleAnimations = new HashMap<AutoBuyableItem, Float>();
    private final Map<AutoBuyableItem, Float> hoverAnimations = new HashMap<AutoBuyableItem, Float>();
    private final Map<AutoBuyableItem, Float> enabledAnimations = new HashMap<AutoBuyableItem, Float>();
    private final AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
    private AutoBuyableItem hoveredItem = null;
    private AutoBuyableItem editingItem = null;
    private EditField editingField = EditField.NONE;
    private String inputText = "";
    private float cursorBlink = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private float panelAlpha = 1.0f;
    private float currentScale = 1.0f;
    private static final float ITEM_HEIGHT = 22.0f;
    private static final float ITEM_SPACING = 3.0f;
    private static final float CATEGORY_HEIGHT = 18.0f;
    private static final float ANIM_SPEED = 11.0f;
    private static final String PRICE_LABEL = "Purchase price: ";
    private static final String QUANTITY_LABEL = "Buy from: ";
    private final List<PendingIcon> pendingIcons = new ArrayList<PendingIcon>();
    private final List<PendingContextIcon> pendingContextIcons = new ArrayList<PendingContextIcon>();

    private int getCurrentGuiScale() {
        int scale = (Integer)AutoBuyGuiComponent.mc.options.guiScale().get();
        if (scale == 0) {
            scale = mc.getWindow().calculateScale(0, mc.isEnforceUnicode());
        }
        return scale;
    }

    private float getScaleFactor() {
        return (float)this.getCurrentGuiScale() / 2.0f;
    }

    public void position(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void size(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public void setAlpha(float alpha) {
        this.panelAlpha = alpha;
    }

    public void startSlideOut() {
        this.slidingOut = true;
        this.targetSlideOffsetX = -(this.width + 100.0f);
    }

    public void startSlideIn() {
        this.slidingOut = false;
        this.targetSlideOffsetX = 0.0f;
    }

    public boolean isSlideComplete() {
        return Math.abs(this.slideOffsetX - this.targetSlideOffsetX) < 5.0f;
    }

    public boolean isSlidOut() {
        return this.slidingOut && this.isSlideComplete();
    }

    public void resetSlide() {
        this.slideOffsetX = 0.0f;
        this.targetSlideOffsetX = 0.0f;
        this.slidingOut = false;
    }

    public void setSlideInstant(float offset) {
        this.slideOffsetX = offset;
        this.targetSlideOffsetX = offset;
    }

    public boolean isEditing() {
        return this.editingItem != null && this.editingField != EditField.NONE;
    }

    private boolean isHovered(double mx, double my, float rx, float ry, float rw, float rh) {
        return mx >= (double)rx && mx <= (double)(rx + rw) && my >= (double)ry && my <= (double)(ry + rh);
    }

    private int clampAlpha(int alpha) {
        return Math.max(0, Math.min(255, alpha));
    }

    private int clampColor(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private float easeOutCubic(float x) {
        return 1.0f - (float)Math.pow(1.0f - x, 3.0);
    }

    private float easeInOutCubic(float x) {
        return x < 0.5f ? 4.0f * x * x * x : 1.0f - (float)Math.pow(-2.0f * x + 2.0f, 3.0) / 2.0f;
    }

    private float calculateSlideAlpha() {
        if (this.slideOffsetX >= 0.0f) {
            return 1.0f;
        }
        float maxOffset = this.width + 100.0f;
        float progress = Math.abs(this.slideOffsetX) / maxOffset;
        progress = Math.max(0.0f, Math.min(1.0f, progress));
        return 1.0f - this.easeOutCubic(progress);
    }

    public void render(GuiGraphics context, float mouseX, float mouseY, float delta, int guiScale, float alphaMultiplier) {
        this.panelAlpha = alphaMultiplier;
        this.currentScale = 1.0f;
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0f, 0.1f);
        this.lastUpdateTime = currentTime;
        float slideDiff = this.targetSlideOffsetX - this.slideOffsetX;
        if (Math.abs(slideDiff) > 0.5f) {
            float progress = 1.0f - Math.abs(slideDiff) / (this.width + 100.0f);
            float easedSpeed = 20.0f * (0.5f + 0.5f * this.easeOutCubic(progress));
            this.slideOffsetX += slideDiff * easedSpeed * deltaTime;
        } else {
            this.slideOffsetX = this.targetSlideOffsetX;
        }
        float slideAlpha = this.calculateSlideAlpha();
        this.updateAnimations(deltaTime);
        this.cursorBlink += deltaTime * 2.0f;
        if (this.cursorBlink > 1.0f) {
            this.cursorBlink -= 1.0f;
        }
        this.hoveredItem = null;
        this.pendingIcons.clear();
        this.pendingContextIcons.clear();
        float contentHeight = this.calculateContentHeight();
        float maxScroll = Math.max(0.0f, contentHeight - this.height + 10.0f);
        this.targetScroll = this.clamp(this.targetScroll, -maxScroll, 0.0f);
        float diff = this.targetScroll - this.smoothScroll;
        this.smoothScroll += diff * 0.3f;
        if (Math.abs(diff) < 0.1f) {
            this.smoothScroll = this.targetScroll;
        }
        this.renderPanelBackground(alphaMultiplier, slideAlpha);
        float clipX = this.x + 3.0f;
        float clipY = this.y + 1.0f;
        float clipW = this.width - 6.0f;
        float clipH = this.height - 3.0f;
        Scissor.enable(clipX, clipY, clipW, clipH, 2.0f);
        float contentOffsetX = this.slideOffsetX;
        float contentAlpha = alphaMultiplier * slideAlpha;
        float currentY = this.y + 5.0f + this.smoothScroll;
        List<CategoryItems> categories = this.getCategorizedItems();
        for (CategoryItems category : categories) {
            if (category.items.isEmpty()) continue;
            if (this.isInView(currentY, 18.0f, clipY, clipH)) {
                this.renderCategoryHeader(this.x + 5.0f + contentOffsetX, currentY, this.width - 10.0f, category.name, contentAlpha);
            }
            currentY += 18.0f;
            for (AutoBuyableItem item : category.items) {
                if (this.isInView(currentY, 22.0f, clipY, clipH)) {
                    this.renderItem(context, item, this.x + 5.0f + contentOffsetX, currentY, this.width - 10.0f, mouseX, mouseY, alphaMultiplier, slideAlpha);
                }
                currentY += 25.0f;
            }
            currentY += 8.0f;
        }
        for (PendingIcon icon : this.pendingIcons) {
            ItemRender.drawItem(icon.stack, icon.x, icon.y, 1.0f, 1.0f);
        }
        this.pendingIcons.clear();
        Scissor.disable();
        float scaleFactor = this.getScaleFactor();
        int scissorX1 = (int)(clipX * scaleFactor);
        int scissorY1 = (int)(clipY * scaleFactor);
        int scissorX2 = (int)((clipX + clipW) * scaleFactor);
        int scissorY2 = (int)((clipY + clipH) * scaleFactor);
        context.enableScissor(scissorX1, scissorY1, scissorX2, scissorY2);
        for (PendingContextIcon icon : this.pendingContextIcons) {
            this.drawItemWithScaleCompensation(context, icon.stack, icon.x, icon.y, icon.scale, 1.0f, scaleFactor);
        }
        this.pendingContextIcons.clear();
        context.disableScissor();
    }

    private void drawItemWithScaleCompensation(GuiGraphics context, ItemStack stack, float x, float y, float scale, float alpha, float scaleFactor) {
        if (stack.isEmpty() || alpha <= 0.01f) {
            return;
        }
        float size = 16.0f * scale;
        float centerX = x + size / 2.0f;
        float centerY = y + size / 2.0f;
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(scale * scaleFactor, scale * scaleFactor);
        matrices.translate(-8.0f, -8.0f);
        context.renderItem(stack, 0, 0);
        matrices.popMatrix();
    }

    private boolean isInView(float itemY, float itemHeight, float clipY, float clipH) {
        float itemBottom = itemY + itemHeight;
        float clipBottom = clipY + clipH;
        return itemBottom > clipY && itemY < clipBottom;
    }

    private void updateAnimations(float deltaTime) {
        for (AutoBuyableItem item : ItemRegistry.getAllItems()) {
            float targetToggle = item.isEnabled() ? 1.0f : 0.0f;
            float currentToggle = this.toggleAnimations.getOrDefault(item, Float.valueOf(item.isEnabled() ? 1.0f : 0.0f)).floatValue();
            float newToggle = this.smoothLerp(currentToggle, targetToggle, 11.0f * deltaTime);
            this.toggleAnimations.put(item, Float.valueOf(newToggle));
            float targetEnabled = item.isEnabled() ? 1.0f : 0.0f;
            float currentEnabled = this.enabledAnimations.getOrDefault(item, Float.valueOf(item.isEnabled() ? 1.0f : 0.0f)).floatValue();
            float newEnabled = this.smoothLerp(currentEnabled, targetEnabled, 11.0f * deltaTime);
            this.enabledAnimations.put(item, Float.valueOf(newEnabled));
            boolean isHov = item == this.hoveredItem;
            float targetHover = isHov ? 1.0f : 0.0f;
            float currentHover = this.hoverAnimations.getOrDefault(item, Float.valueOf(0.0f)).floatValue();
            this.hoverAnimations.put(item, Float.valueOf(this.smoothLerp(currentHover, targetHover, 11.0f * deltaTime)));
        }
    }

    private float smoothLerp(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) {
            return target;
        }
        return current + diff * this.clamp(speed, 0.0f, 1.0f);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private float calculateContentHeight() {
        float total = 5.0f;
        List<CategoryItems> categories = this.getCategorizedItems();
        for (CategoryItems category : categories) {
            if (category.items.isEmpty()) continue;
            total += 18.0f;
            total += (float)category.items.size() * 25.0f;
            total += 8.0f;
        }
        return total;
    }

    private void renderPanelBackground(float alphaMultiplier, float slideAlpha) {
        float bgSlideAlpha = this.slidingOut ? slideAlpha : 1.0f;
        int bgAlpha = this.clampAlpha((int)(15.0f * alphaMultiplier * bgSlideAlpha));
        int outlineAlpha = this.clampAlpha((int)(215.0f * alphaMultiplier * bgSlideAlpha));
        if (bgAlpha > 0) {
            Render2D.rect(this.x, this.y, this.width, this.height, new Color(64, 64, 64, bgAlpha).getRGB(), 7.0f);
            Render2D.outline(this.x, this.y, this.width, this.height, 0.5f, new Color(55, 55, 55, outlineAlpha).getRGB(), 7.0f);
        }
    }

    private void renderCategoryHeader(float catX, float catY, float catWidth, String name, float contentAlpha) {
        int textAlpha = this.clampAlpha((int)(180.0f * contentAlpha));
        float textWidth = Fonts.BOLD.getWidth(name, 5.0f);
        float lineWidth = (catWidth - textWidth - 16.0f) / 2.0f;
        int lineAlpha = this.clampAlpha((int)(60.0f * contentAlpha));
        Render2D.rect(catX, catY + 6.0f, lineWidth, 0.5f, new Color(100, 100, 100, lineAlpha).getRGB(), 0.0f);
        Fonts.BOLD.draw(name, catX + lineWidth + 8.0f, catY + 3.0f, 5.0f, new Color(160, 160, 160, textAlpha).getRGB());
        Render2D.rect(catX + lineWidth + textWidth + 16.0f, catY + 6.0f, lineWidth, 0.5f, new Color(100, 100, 100, lineAlpha).getRGB(), 0.0f);
    }

    private void renderItem(GuiGraphics context, AutoBuyableItem item, float itemX, float itemY, float itemW, float mouseX, float mouseY, float alphaMultiplier, float slideAlpha) {
        Object priceValue;
        if (alphaMultiplier <= 0.01f) {
            return;
        }
        float contentAlpha = alphaMultiplier * slideAlpha;
        float realItemX = itemX - this.slideOffsetX;
        boolean hovered = this.isHovered(mouseX, mouseY, realItemX, itemY, itemW, 22.0f);
        if (hovered && !this.slidingOut) {
            this.hoveredItem = item;
        }
        float toggleAnim = this.toggleAnimations.getOrDefault(item, Float.valueOf(item.isEnabled() ? 1.0f : 0.0f)).floatValue();
        float enabledAnim = this.enabledAnimations.getOrDefault(item, Float.valueOf(item.isEnabled() ? 1.0f : 0.0f)).floatValue();
        float hoverAnim = this.hoverAnimations.getOrDefault(item, Float.valueOf(0.0f)).floatValue();
        float dimFactor = 0.5f + 0.5f * enabledAnim;
        int baseBg = 64 + (int)(hoverAnim * 36.0f);
        int bgR = this.clampColor((int)((float)baseBg * dimFactor));
        int bgG = this.clampColor((int)((float)baseBg * dimFactor));
        int bgB = this.clampColor((int)((float)baseBg * dimFactor));
        int bgAlpha = this.clampAlpha((int)((25.0f + hoverAnim * 15.0f) * contentAlpha));
        Render2D.rect(itemX, itemY, itemW, 22.0f, new Color(bgR, bgG, bgB, bgAlpha).getRGB(), 5.0f);
        int baseOutlineAlpha = 60;
        int enabledOutlineAlpha = 80;
        int outlineAlphaValue = (int)(((float)baseOutlineAlpha + (float)(enabledOutlineAlpha - baseOutlineAlpha) * enabledAnim + hoverAnim * 30.0f) * contentAlpha);
        int outlineGray = (int)(50.0f + 30.0f * enabledAnim + 20.0f * hoverAnim);
        Render2D.outline(itemX, itemY, itemW, 22.0f, 0.5f, new Color(outlineGray, outlineGray, outlineGray, this.clampAlpha(outlineAlphaValue)).getRGB(), 5.0f);
        float iconSize = 16.0f;
        float iconY = itemY + (22.0f - iconSize) / 2.0f;
        float iconX = itemX + 2.0f;
        this.queueItemIcon(item, iconX, iconY, iconSize);
        String displayName = item.getDisplayName();
        int baseTextBrightness = this.clampColor((int)(120.0f + 135.0f * enabledAnim));
        int textAlpha = this.clampAlpha((int)((120.0f + 135.0f * enabledAnim) * contentAlpha));
        Color textColor = new Color(baseTextBrightness, baseTextBrightness, baseTextBrightness, textAlpha);
        Fonts.BOLD.draw(displayName, itemX + 20.0f, itemY + 5.0f, 5.0f, textColor.getRGB());
        boolean isEditingPrice = this.editingItem == item && this.editingField == EditField.PRICE;
        float priceX = itemX + 20.0f;
        float priceY = itemY + 13.0f;
        int priceBrightness = this.clampColor((int)(80.0f + 60.0f * enabledAnim));
        int priceAlpha = this.clampAlpha((int)((100.0f + 80.0f * enabledAnim) * contentAlpha));
        Color labelColor = new Color(priceBrightness, priceBrightness, priceBrightness, priceAlpha);
        Fonts.BOLD.draw(PRICE_LABEL, priceX, priceY, 4.0f, labelColor.getRGB());
        float labelWidth = Fonts.BOLD.getWidth(PRICE_LABEL, 4.0f);
        float valueX = priceX + labelWidth;
        if (isEditingPrice) {
            priceValue = this.inputText;
            float cursorAlphaVal = (float)(Math.sin((double)this.cursorBlink * Math.PI * 2.0) * 0.5 + 0.5);
            if (cursorAlphaVal > 0.5f) {
                priceValue = (String)priceValue + "|";
            }
        } else {
            priceValue = String.valueOf(item.getSettings().getBuyBelow());
        }
        int valueAlpha = this.clampAlpha(isEditingPrice ? (int)(220.0f * contentAlpha) : (int)((100.0f + 80.0f * enabledAnim) * contentAlpha));
        Color valueColor = isEditingPrice ? new Color(100, 200, 100, valueAlpha) : new Color(priceBrightness, priceBrightness, priceBrightness, valueAlpha);
        Fonts.BOLD.draw((String)priceValue, valueX, priceY, 4.0f, valueColor.getRGB());
        if (item.getSettings().isCanHaveQuantity()) {
            Object qtyValue;
            boolean isEditingQty = this.editingItem == item && this.editingField == EditField.QUANTITY;
            float priceFullWidth = labelWidth + Fonts.BOLD.getWidth((String)priceValue, 4.0f);
            float qtyLabelX = priceX + priceFullWidth + 8.0f;
            Fonts.BOLD.draw(QUANTITY_LABEL, qtyLabelX, priceY, 4.0f, labelColor.getRGB());
            float qtyLabelWidth = Fonts.BOLD.getWidth(QUANTITY_LABEL, 4.0f);
            float qtyValueX = qtyLabelX + qtyLabelWidth;
            if (isEditingQty) {
                qtyValue = this.inputText;
                float cursorAlphaVal = (float)(Math.sin((double)this.cursorBlink * Math.PI * 2.0) * 0.5 + 0.5);
                if (cursorAlphaVal > 0.5f) {
                    qtyValue = (String)qtyValue + "|";
                }
            } else {
                qtyValue = String.valueOf(item.getSettings().getMinQuantity());
            }
            int qtyValueAlpha = this.clampAlpha(isEditingQty ? (int)(220.0f * contentAlpha) : (int)((100.0f + 80.0f * enabledAnim) * contentAlpha));
            Color qtyValueColor = isEditingQty ? new Color(100, 200, 100, qtyValueAlpha) : new Color(priceBrightness, priceBrightness, priceBrightness, qtyValueAlpha);
            Fonts.BOLD.draw((String)qtyValue, qtyValueX, priceY, 4.0f, qtyValueColor.getRGB());
        }
        float toggleW = 14.0f;
        float toggleH = 8.0f;
        float toggleX = itemX + itemW - toggleW - 4.0f;
        float toggleY = itemY + 11.0f - toggleH / 2.0f;
        this.renderToggle(toggleX, toggleY, toggleW, toggleH, toggleAnim, enabledAnim, contentAlpha);
        float indicatorX = toggleX - 8.0f;
        float indicatorY = itemY + 11.0f - 2.0f;
        int indicatorR = this.clampColor((int)(70.0f + 30.0f * enabledAnim));
        int indicatorG = this.clampColor((int)(70.0f + 130.0f * enabledAnim));
        int indicatorB = this.clampColor((int)(70.0f + 30.0f * enabledAnim));
        int indicatorAlpha = this.clampAlpha((int)((80.0f + 120.0f * enabledAnim) * contentAlpha));
        Render2D.rect(indicatorX, indicatorY, 4.0f, 4.0f, new Color(indicatorR, indicatorG, indicatorB, indicatorAlpha).getRGB(), 2.0f);
    }

    private void queueItemIcon(AutoBuyableItem item, float iconX, float iconY, float iconSize) {
        ItemStack stack = item.createItemStack();
        float scale = iconSize / 16.0f;
        this.pendingContextIcons.add(new PendingContextIcon(stack, iconX, iconY, scale));
    }

    private void renderToggle(float tx, float ty, float tw, float th, float anim, float enabledAnim, float contentAlpha) {
        int bgR = this.clampColor((int)(40.0f + anim * 40.0f));
        int bgG = this.clampColor((int)(40.0f + anim * 110.0f));
        int bgB = this.clampColor((int)(45.0f + anim * 30.0f));
        bgR = this.clampColor((int)((float)bgR * (0.5f + 0.5f * enabledAnim)));
        bgG = this.clampColor((int)((float)bgG * (0.5f + 0.5f * enabledAnim)));
        bgB = this.clampColor((int)((float)bgB * (0.5f + 0.5f * enabledAnim)));
        int bgAlpha = this.clampAlpha((int)(160.0f * contentAlpha));
        Render2D.rect(tx, ty, tw, th, new Color(bgR, bgG, bgB, bgAlpha).getRGB(), th / 2.0f);
        float knobSize = th - 2.0f;
        float knobX = tx + 1.0f + anim * (tw - knobSize - 2.0f);
        float knobY = ty + 1.0f;
        int knobBrightness = this.clampColor((int)(120.0f + 80.0f * enabledAnim));
        int knobAlpha = this.clampAlpha((int)(220.0f * contentAlpha));
        Render2D.rect(knobX, knobY, knobSize, knobSize, new Color(knobBrightness, knobBrightness, knobBrightness, knobAlpha).getRGB(), knobSize / 2.0f);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float panelX, float panelY, float panelW, float panelH) {
        if (this.slidingOut) {
            return false;
        }
        if (!this.isHovered(mouseX, mouseY, panelX, panelY, panelW, panelH)) {
            if (this.isEditing()) {
                this.applyEdit();
            }
            return false;
        }
        if (button != 0) {
            if (this.isEditing()) {
                this.applyEdit();
            }
            return true;
        }
        float clipY = panelY + 3.0f;
        float clipH = panelH - 6.0f;
        float currentY = panelY + 5.0f + this.smoothScroll;
        float itemX = panelX + 5.0f;
        float itemW = panelW - 10.0f;
        List<CategoryItems> categories = this.getCategorizedItems();
        for (CategoryItems category : categories) {
            if (category.items.isEmpty()) continue;
            currentY += 18.0f;
            for (AutoBuyableItem item : category.items) {
                boolean inView;
                float itemY = currentY;
                boolean bl = inView = itemY + 22.0f > clipY && itemY < clipY + clipH;
                if (inView && this.isHovered(mouseX, mouseY, itemX, itemY, itemW, 22.0f)) {
                    String qtyValue;
                    float qtyValueWidth;
                    float qtyHitW;
                    float qtyLabelWidth;
                    float priceFullWidth;
                    float qtyLabelX;
                    float qtyHitX;
                    String priceValue;
                    float priceValueWidth;
                    float priceHitW;
                    float toggleW = 14.0f;
                    float toggleX = itemX + itemW - toggleW - 4.0f;
                    float toggleHitX = toggleX - 15.0f;
                    float toggleH = 8.0f;
                    float toggleY = itemY + 11.0f - toggleH / 2.0f;
                    float toggleHitY = toggleY - 10.0f;
                    float toggleHitW = toggleW + 30.0f;
                    float toggleHitH = toggleH + 20.0f;
                    if (this.isHovered(mouseX, mouseY, toggleHitX, toggleHitY, toggleHitW, toggleHitH)) {
                        if (this.isEditing()) {
                            this.applyEdit();
                        }
                        ItemRegistry.saveItemState(item);
                        return true;
                    }
                    float priceX = itemX + 20.0f;
                    float priceY = itemY + 11.0f;
                    float labelWidth = Fonts.BOLD.getWidth(PRICE_LABEL, 4.0f);
                    float priceHitX = priceX + labelWidth - 3.0f;
                    if (this.isHovered(mouseX, mouseY, priceHitX, priceY - 3.0f, priceHitW = (priceValueWidth = Fonts.BOLD.getWidth(priceValue = String.valueOf(item.getSettings().getBuyBelow()), 4.0f)) + 10.0f, 12.0f)) {
                        if (this.isEditing()) {
                            this.applyEdit();
                        }
                        this.startEditing(item, EditField.PRICE);
                        return true;
                    }
                    if (item.getSettings().isCanHaveQuantity() && this.isHovered(mouseX, mouseY, qtyHitX = (qtyLabelX = priceX + (priceFullWidth = labelWidth + priceValueWidth) + 8.0f) + (qtyLabelWidth = Fonts.BOLD.getWidth(QUANTITY_LABEL, 4.0f)) - 3.0f, priceY - 3.0f, qtyHitW = (qtyValueWidth = Fonts.BOLD.getWidth(qtyValue = String.valueOf(item.getSettings().getMinQuantity()), 4.0f)) + 10.0f, 12.0f)) {
                        if (this.isEditing()) {
                            this.applyEdit();
                        }
                        this.startEditing(item, EditField.QUANTITY);
                        return true;
                    }
                    if (this.isEditing()) {
                        this.applyEdit();
                    }
                    return true;
                }
                currentY += 25.0f;
            }
            currentY += 8.0f;
        }
        if (this.isEditing()) {
            this.applyEdit();
        }
        return true;
    }

    private void startEditing(AutoBuyableItem item, EditField field) {
        this.editingItem = item;
        this.editingField = field;
        this.cursorBlink = 0.0f;
        if (field == EditField.PRICE) {
            this.inputText = String.valueOf(item.getSettings().getBuyBelow());
        } else if (field == EditField.QUANTITY) {
            this.inputText = String.valueOf(item.getSettings().getMinQuantity());
        }
    }

    private void applyEdit() {
        if (this.editingItem == null || this.editingField == EditField.NONE) {
            return;
        }
        try {
            int value = Integer.parseInt(this.inputText);
            if (this.editingField == EditField.PRICE) {
                this.editingItem.getSettings().setBuyBelow(Math.max(1, value));
            } else if (this.editingField == EditField.QUANTITY) {
                this.editingItem.getSettings().setMinQuantity(Math.max(1, Math.min(64, value)));
            }
            ItemRegistry.saveItemSettings(this.editingItem);
        }
        catch (NumberFormatException numberFormatException) {
            // empty catch block
        }
        this.editingItem = null;
        this.editingField = EditField.NONE;
        this.inputText = "";
    }

    private void cancelEdit() {
        this.editingItem = null;
        this.editingField = EditField.NONE;
        this.inputText = "";
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.isEditing()) {
            return false;
        }
        if (keyCode == 257 || keyCode == 335) {
            this.applyEdit();
            return true;
        }
        if (keyCode == 256) {
            this.cancelEdit();
            return true;
        }
        if (keyCode == 259 && !this.inputText.isEmpty()) {
            this.inputText = this.inputText.substring(0, this.inputText.length() - 1);
            return true;
        }
        return true;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!this.isEditing()) {
            return false;
        }
        if (Character.isDigit(chr)) {
            int maxLen;
            int n = maxLen = this.editingField == EditField.PRICE ? 9 : 2;
            if (this.inputText.length() < maxLen) {
                this.inputText = this.inputText + chr;
            }
            return true;
        }
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount, float panelX, float panelY, float panelW, float panelH) {
        if (this.slidingOut) {
            return false;
        }
        if (this.isHovered(mouseX, mouseY, panelX, panelY, panelW, panelH)) {
            this.targetScroll += (float)amount * 25.0f;
            return true;
        }
        return false;
    }

    public void resetHover() {
        this.hoveredItem = null;
    }

    public void resetPositions() {
        this.smoothScroll = this.targetScroll;
    }

    private List<CategoryItems> getCategorizedItems() {
        ArrayList<CategoryItems> categories = new ArrayList<CategoryItems>();
        categories.add(new CategoryItems("Crusher", ItemRegistry.getKrush()));
        categories.add(new CategoryItems("Talismans", ItemRegistry.getTalismans()));
        categories.add(new CategoryItems("Spheres", ItemRegistry.getSpheres()));
        categories.add(new CategoryItems("Misc", ItemRegistry.getMisc()));
        categories.add(new CategoryItems("Donator", ItemRegistry.getDonator()));
        categories.add(new CategoryItems("Potions", ItemRegistry.getPotions()));
        return categories;
    }

    @Generated
    public float getX() {
        return this.x;
    }

    @Generated
    public float getY() {
        return this.y;
    }

    @Generated
    public float getWidth() {
        return this.width;
    }

    @Generated
    public float getHeight() {
        return this.height;
    }

    @Generated
    public float getTargetScroll() {
        return this.targetScroll;
    }

    @Generated
    public float getSmoothScroll() {
        return this.smoothScroll;
    }

    @Generated
    public float getSlideOffsetX() {
        return this.slideOffsetX;
    }

    @Generated
    public float getTargetSlideOffsetX() {
        return this.targetSlideOffsetX;
    }

    @Generated
    public boolean isSlidingOut() {
        return this.slidingOut;
    }

    @Generated
    public Map<AutoBuyableItem, Float> getToggleAnimations() {
        return this.toggleAnimations;
    }

    @Generated
    public Map<AutoBuyableItem, Float> getHoverAnimations() {
        return this.hoverAnimations;
    }

    @Generated
    public Map<AutoBuyableItem, Float> getEnabledAnimations() {
        return this.enabledAnimations;
    }

    @Generated
    public AutoBuyManager getAutoBuyManager() {
        return this.autoBuyManager;
    }

    @Generated
    public AutoBuyableItem getHoveredItem() {
        return this.hoveredItem;
    }

    @Generated
    public AutoBuyableItem getEditingItem() {
        return this.editingItem;
    }

    @Generated
    public EditField getEditingField() {
        return this.editingField;
    }

    @Generated
    public String getInputText() {
        return this.inputText;
    }

    @Generated
    public float getCursorBlink() {
        return this.cursorBlink;
    }

    @Generated
    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    @Generated
    public float getPanelAlpha() {
        return this.panelAlpha;
    }

    @Generated
    public float getCurrentScale() {
        return this.currentScale;
    }

    @Generated
    public List<PendingIcon> getPendingIcons() {
        return this.pendingIcons;
    }

    @Generated
    public List<PendingContextIcon> getPendingContextIcons() {
        return this.pendingContextIcons;
    }

    @Generated
    public void setX(float x) {
        this.x = x;
    }

    @Generated
    public void setY(float y) {
        this.y = y;
    }

    @Generated
    public void setWidth(float width) {
        this.width = width;
    }

    @Generated
    public void setHeight(float height) {
        this.height = height;
    }

    @Generated
    public void setTargetScroll(float targetScroll) {
        this.targetScroll = targetScroll;
    }

    @Generated
    public void setSmoothScroll(float smoothScroll) {
        this.smoothScroll = smoothScroll;
    }

    @Generated
    public void setSlideOffsetX(float slideOffsetX) {
        this.slideOffsetX = slideOffsetX;
    }

    @Generated
    public void setTargetSlideOffsetX(float targetSlideOffsetX) {
        this.targetSlideOffsetX = targetSlideOffsetX;
    }

    @Generated
    public void setSlidingOut(boolean slidingOut) {
        this.slidingOut = slidingOut;
    }

    @Generated
    public void setHoveredItem(AutoBuyableItem hoveredItem) {
        this.hoveredItem = hoveredItem;
    }

    @Generated
    public void setEditingItem(AutoBuyableItem editingItem) {
        this.editingItem = editingItem;
    }

    @Generated
    public void setEditingField(EditField editingField) {
        this.editingField = editingField;
    }

    @Generated
    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    @Generated
    public void setCursorBlink(float cursorBlink) {
        this.cursorBlink = cursorBlink;
    }

    @Generated
    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Generated
    public void setPanelAlpha(float panelAlpha) {
        this.panelAlpha = panelAlpha;
    }

    @Generated
    public void setCurrentScale(float currentScale) {
        this.currentScale = currentScale;
    }

    public static enum EditField {
        NONE,
        PRICE,
        QUANTITY;

    }

    private static class CategoryItems {
        String name;
        List<AutoBuyableItem> items;

        CategoryItems(String name, List<AutoBuyableItem> items) {
            this.name = name;
            this.items = items != null ? items : new ArrayList();
        }
    }

    private static class PendingIcon {
        ItemStack stack;
        float x;
        float y;

        PendingIcon(ItemStack stack, float x, float y) {
            this.stack = stack;
            this.x = x;
            this.y = y;
        }
    }

    private static class PendingContextIcon {
        ItemStack stack;
        float x;
        float y;
        float scale;

        PendingContextIcon(ItemStack stack, float x, float y, float scale) {
            this.stack = stack;
            this.x = x;
            this.y = y;
            this.scale = scale;
        }
    }
}

