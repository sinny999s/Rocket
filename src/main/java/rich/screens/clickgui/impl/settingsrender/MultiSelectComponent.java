
package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.gui.GuiGraphics;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class MultiSelectComponent
extends AbstractSettingComponent {
    private final MultiSelectSetting multiSelectSetting;
    private boolean expanded = false;
    private float expandAnimation = 0.0f;
    private float hoverAnimation = 0.0f;
    private float scrollOffset = 0.0f;
    private float scrollOffsetAnimated = 0.0f;
    private boolean scrollingRight = true;
    private long scrollPauseTime = 0L;
    private float descScrollOffset = 0.0f;
    private boolean descScrollingRight = true;
    private long descScrollPauseTime = 0L;
    private float arrowRotation = 0.0f;
    private final Map<String, Float> optionHoverAnimations = new HashMap<String, Float>();
    private final Map<String, Float> checkAnimations = new HashMap<String, Float>();
    private final Map<String, Float> itemAlphaAnimations = new HashMap<String, Float>();
    private final Map<String, Float> itemXPositions = new HashMap<String, Float>();
    private final Map<String, Float> itemTargetPositions = new HashMap<String, Float>();
    private final Set<String> previousSelected = new HashSet<String>();
    private float noneAlphaAnimation = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float COLLAPSE_SPEED = 15.0f;
    private static final long SCROLL_PAUSE_DURATION = 2000L;
    private static final float BOX_WIDTH = 65.0f;
    private static final float OPTION_HEIGHT = 14.0f;
    private static final float SCROLL_PIXELS_PER_SECOND = 20.0f;
    private static final float DESC_PADDING = 8.0f;
    private static final float ITEM_ANIMATION_SPEED = 10.0f;
    private static final float POSITION_ANIMATION_SPEED = 8.0f;

    public MultiSelectComponent(MultiSelectSetting setting) {
        super(setting);
        this.multiSelectSetting = setting;
        for (String option : setting.getList()) {
            this.checkAnimations.put(option, Float.valueOf(setting.isSelected(option) ? 1.0f : 0.0f));
            this.optionHoverAnimations.put(option, Float.valueOf(0.0f));
        }
        this.previousSelected.addAll(setting.getSelected());
        float initX = 0.0f;
        for (String item : setting.getList()) {
            if (!setting.isSelected(item)) continue;
            this.itemAlphaAnimations.put(item, Float.valueOf(1.0f));
            this.itemXPositions.put(item, Float.valueOf(initX));
            this.itemTargetPositions.put(item, Float.valueOf(initX));
            String displayText = item + ", ";
            initX += Fonts.BOLD.getWidth(displayText, 5.0f);
        }
        this.noneAlphaAnimation = setting.getSelected().isEmpty() ? 1.0f : 0.0f;
    }

    private float getDeltaTime() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0f, 0.1f);
        this.lastUpdateTime = currentTime;
        return deltaTime;
    }

    private float lerp(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) {
            return target;
        }
        return current + diff * Math.min(speed, 1.0f);
    }

    private void updateItemAnimations(float deltaTime) {
        HashSet<String> currentSelected = new HashSet<String>(this.multiSelectSetting.getSelected());
        for (String item : currentSelected) {
            if (this.itemAlphaAnimations.containsKey(item)) continue;
            this.itemAlphaAnimations.put(item, Float.valueOf(0.0f));
            float lastPos = 0.0f;
            for (String existingItem : this.multiSelectSetting.getList()) {
                String text;
                float pos;
                float endPos;
                if (!this.itemXPositions.containsKey(existingItem) || !((endPos = (pos = this.itemXPositions.get(existingItem).floatValue()) + Fonts.BOLD.getWidth(text = existingItem + ", ", 5.0f)) > lastPos)) continue;
                lastPos = endPos;
            }
            this.itemXPositions.put(item, Float.valueOf(lastPos));
            this.itemTargetPositions.put(item, Float.valueOf(lastPos));
        }
        for (String item : this.itemAlphaAnimations.keySet()) {
            boolean isSelected = currentSelected.contains(item);
            float currentAlpha = this.itemAlphaAnimations.get(item).floatValue();
            float targetAlpha = isSelected ? 1.0f : 0.0f;
            float newAlpha = this.lerp(currentAlpha, targetAlpha, deltaTime * 10.0f);
            this.itemAlphaAnimations.put(item, Float.valueOf(newAlpha));
        }
        List<String> allItems = this.multiSelectSetting.getList();
        ArrayList<String> visibleItems = new ArrayList<String>();
        for (String item : allItems) {
            if (!this.itemAlphaAnimations.containsKey(item) || !(this.itemAlphaAnimations.get(item).floatValue() > 0.01f)) continue;
            visibleItems.add(item);
        }
        float currentTargetX = 0.0f;
        for (int i = 0; i < visibleItems.size(); ++i) {
            String item = (String)visibleItems.get(i);
            float itemAlpha = this.itemAlphaAnimations.getOrDefault(item, Float.valueOf(0.0f)).floatValue();
            this.itemTargetPositions.put(item, Float.valueOf(currentTargetX));
            Object displayText = item;
            if (i < visibleItems.size() - 1) {
                displayText = (String)displayText + ", ";
            }
            float textWidth = Fonts.BOLD.getWidth((String)displayText, 5.0f);
            currentTargetX += textWidth * itemAlpha;
        }
        for (String item : visibleItems) {
            float targetX = this.itemTargetPositions.getOrDefault(item, Float.valueOf(0.0f)).floatValue();
            float currentX = this.itemXPositions.getOrDefault(item, Float.valueOf(targetX)).floatValue();
            currentX = this.lerp(currentX, targetX, deltaTime * 8.0f);
            this.itemXPositions.put(item, Float.valueOf(currentX));
        }
        ArrayList<String> toRemove = new ArrayList<String>();
        for (String item : this.itemAlphaAnimations.keySet()) {
            boolean isSelected = currentSelected.contains(item);
            float alpha = this.itemAlphaAnimations.get(item).floatValue();
            if (isSelected || !(alpha < 0.01f)) continue;
            toRemove.add(item);
        }
        for (String item : toRemove) {
            this.itemAlphaAnimations.remove(item);
            this.itemXPositions.remove(item);
            this.itemTargetPositions.remove(item);
        }
        boolean hasVisibleItems = false;
        for (Float alpha : this.itemAlphaAnimations.values()) {
            if (!(alpha.floatValue() > 0.01f)) continue;
            hasVisibleItems = true;
            break;
        }
        float noneTarget = !hasVisibleItems && currentSelected.isEmpty() ? 1.0f : 0.0f;
        this.noneAlphaAnimation = this.lerp(this.noneAlphaAnimation, noneTarget, deltaTime * 10.0f);
        this.previousSelected.clear();
        this.previousSelected.addAll(currentSelected);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        float deltaTime = this.getDeltaTime();
        this.updateItemAnimations(deltaTime);
        boolean mainHovered = this.isMainHover(mouseX, mouseY);
        this.hoverAnimation = this.lerp(this.hoverAnimation, mainHovered ? 1.0f : 0.0f, deltaTime * 8.0f);
        float expandSpeed = this.expanded ? 8.0f : 15.0f;
        this.expandAnimation = this.lerp(this.expandAnimation, this.expanded ? 1.0f : 0.0f, deltaTime * expandSpeed);
        float targetRotation = this.expanded ? 90.0f : 0.0f;
        this.arrowRotation = this.lerp(this.arrowRotation, targetRotation, deltaTime * 8.0f);
        Fonts.GUI_ICONS.draw("I", this.x - 0.5f, this.y + this.height / 2.0f - 8.5f, 9.0f, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
        Fonts.BOLD.draw(this.multiSelectSetting.getName(), this.x + 9.5f, this.y + this.height / 2.0f - 7.5f, 6.0f, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
        String description = this.multiSelectSetting.getDescription();
        if (description != null && !description.isEmpty()) {
            this.renderScrollingDescription(description, deltaTime);
        }
        float boxX = this.x + this.width - 65.0f - 2.0f;
        float boxY = this.y + this.height / 2.0f - 5.0f;
        float boxHeight = 10.0f;
        int bgAlpha = 25 + (int)(this.hoverAnimation * 15.0f);
        Render2D.rect(boxX, boxY, 65.0f, boxHeight, this.applyAlpha(new Color(55, 55, 55, bgAlpha)).getRGB(), 3.0f);
        int outlineAlpha = 60 + (int)(this.hoverAnimation * 40.0f);
        Render2D.outline(boxX, boxY, 65.0f, boxHeight, 0.5f, this.applyAlpha(new Color(155, 155, 155, outlineAlpha)).getRGB(), 3.0f);
        this.renderSelectedText(boxX, boxY, 65.0f, boxHeight, deltaTime);
        this.renderArrowIcon(boxX + 65.0f - 8.0f, boxY + boxHeight / 2.0f - 4.0f);
        if (this.expandAnimation > 0.01f) {
            this.renderExpandedOptions(context, mouseX, mouseY, boxX, boxY + boxHeight + 2.0f, deltaTime);
        }
    }

    private void renderArrowIcon(float iconX, float iconY) {
        int arrowAlpha = 120 + (int)(this.hoverAnimation * 60.0f);
        float centerX = iconX + 4.0f;
        float centerY = iconY + 4.0f;
        float rad = (float)Math.toRadians(this.arrowRotation);
        float cos = (float)Math.cos(rad);
        float sin = (float)Math.sin(rad);
        float offsetX = -4.0f;
        float offsetY = -4.0f;
        float rotatedX = centerX + (offsetX * cos - offsetY * sin);
        float rotatedY = centerY + (offsetX * sin + offsetY * cos);
    }

    private void renderScrollingDescription(String description, float deltaTime) {
        float descY = this.y + this.height / 2.0f + 0.5f;
        float boxX = this.x + this.width - 65.0f - 2.0f;
        float availableWidth = boxX - this.x - 8.0f;
        float descWidth = Fonts.BOLD.getWidth(description, 5.0f);
        if (descWidth <= availableWidth) {
            this.descScrollOffset = 0.0f;
            Fonts.BOLD.draw(description, this.x + 0.5f, descY, 5.0f, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
        } else {
            this.updateDescScrollAnimation(deltaTime, descWidth, availableWidth);
            float maxScroll = descWidth - availableWidth + 5.0f;
            float currentScroll = this.descScrollOffset * maxScroll;
            Scissor.enable(this.x, descY - 2.0f, availableWidth, 10.0f, 2.0f);
            Fonts.BOLD.draw(description, this.x + 0.5f - currentScroll, descY, 5.0f, this.applyAlpha(new Color(128, 128, 128, 128)).getRGB());
            Scissor.disable();
        }
    }

    private void updateDescScrollAnimation(float deltaTime, float textWidth, float availableWidth) {
        float scrollDistance;
        long currentTime = System.currentTimeMillis();
        if (this.descScrollPauseTime > 0L) {
            if (currentTime - this.descScrollPauseTime < 2000L) {
                return;
            }
            this.descScrollPauseTime = 0L;
        }
        if ((scrollDistance = textWidth - availableWidth + 5.0f) <= 0.0f) {
            this.descScrollOffset = 0.0f;
            return;
        }
        float scrollSpeed = 20.0f / scrollDistance;
        if (this.descScrollingRight) {
            this.descScrollOffset += deltaTime * scrollSpeed;
            if (this.descScrollOffset >= 1.0f) {
                this.descScrollOffset = 1.0f;
                this.descScrollingRight = false;
                this.descScrollPauseTime = currentTime;
            }
        } else {
            this.descScrollOffset -= deltaTime * scrollSpeed;
            if (this.descScrollOffset <= 0.0f) {
                this.descScrollOffset = 0.0f;
                this.descScrollingRight = true;
                this.descScrollPauseTime = currentTime;
            }
        }
    }

    private void renderSelectedText(float boxX, float boxY, float boxWidth, float boxHeight, float deltaTime) {
        float textY = boxY + boxHeight / 2.0f - 2.5f;
        float availableWidth = boxWidth - 4.0f;
        float baseX = boxX + 4.0f;
        Scissor.enable(boxX + 1.0f, boxY, availableWidth + 2.0f, boxHeight, 2.0f);
        if (this.noneAlphaAnimation > 0.01f) {
            int noneAlpha = (int)(200.0f * this.noneAlphaAnimation * this.alphaMultiplier);
            Fonts.BOLD.draw("None", baseX, textY, 5.0f, new Color(160, 160, 165, noneAlpha).getRGB());
        }
        List<String> allItems = this.multiSelectSetting.getList();
        ArrayList<String> visibleItems = new ArrayList<String>();
        for (String item : allItems) {
            if (!this.itemAlphaAnimations.containsKey(item) || !(this.itemAlphaAnimations.get(item).floatValue() > 0.01f)) continue;
            visibleItems.add(item);
        }
        if (visibleItems.isEmpty()) {
            Scissor.disable();
            return;
        }
        float totalWidth = 0.0f;
        for (int i = 0; i < visibleItems.size(); ++i) {
            String item = (String)visibleItems.get(i);
            float itemAlpha = this.itemAlphaAnimations.getOrDefault(item, Float.valueOf(0.0f)).floatValue();
            Object displayText = item;
            if (i < visibleItems.size() - 1) {
                displayText = (String)displayText + ", ";
            }
            totalWidth += Fonts.BOLD.getWidth((String)displayText, 5.0f) * itemAlpha;
        }
        if (totalWidth <= availableWidth) {
            this.scrollOffset = 0.0f;
            this.scrollOffsetAnimated = this.lerp(this.scrollOffsetAnimated, 0.0f, deltaTime * 8.0f);
        } else {
            this.updateScrollAnimation(deltaTime, totalWidth, availableWidth);
            this.scrollOffsetAnimated = this.lerp(this.scrollOffsetAnimated, this.scrollOffset, deltaTime * 8.0f);
        }
        float maxScroll = Math.max(0.0f, totalWidth - availableWidth + 5.0f);
        float currentScroll = this.scrollOffsetAnimated * maxScroll;
        for (int i = 0; i < visibleItems.size(); ++i) {
            String item = (String)visibleItems.get(i);
            float itemAlpha = this.itemAlphaAnimations.getOrDefault(item, Float.valueOf(0.0f)).floatValue();
            float itemX = this.itemXPositions.getOrDefault(item, Float.valueOf(0.0f)).floatValue();
            Object displayText = item;
            if (i < visibleItems.size() - 1) {
                displayText = (String)displayText + ", ";
            }
            float renderX = baseX + itemX - currentScroll;
            int alpha = (int)(200.0f * itemAlpha * this.alphaMultiplier);
            if (alpha <= 0) continue;
            Fonts.BOLD.draw((String)displayText, renderX, textY, 5.0f, new Color(160, 160, 165, alpha).getRGB());
        }
        Scissor.disable();
    }

    private void updateScrollAnimation(float deltaTime, float textWidth, float availableWidth) {
        float scrollDistance;
        long currentTime = System.currentTimeMillis();
        if (this.scrollPauseTime > 0L) {
            if (currentTime - this.scrollPauseTime < 2000L) {
                return;
            }
            this.scrollPauseTime = 0L;
        }
        if ((scrollDistance = textWidth - availableWidth + 5.0f) <= 0.0f) {
            this.scrollOffset = 0.0f;
            return;
        }
        float scrollSpeed = 20.0f / scrollDistance;
        if (this.scrollingRight) {
            this.scrollOffset += deltaTime * scrollSpeed;
            if (this.scrollOffset >= 1.0f) {
                this.scrollOffset = 1.0f;
                this.scrollingRight = false;
                this.scrollPauseTime = currentTime;
            }
        } else {
            this.scrollOffset -= deltaTime * scrollSpeed;
            if (this.scrollOffset <= 0.0f) {
                this.scrollOffset = 0.0f;
                this.scrollingRight = true;
                this.scrollPauseTime = currentTime;
            }
        }
    }

    private void renderExpandedOptions(GuiGraphics context, int mouseX, int mouseY, float boxX, float startY, float deltaTime) {
        List<String> options = this.multiSelectSetting.getList();
        float fullPanelHeight = (float)options.size() * 14.0f;
        float visibleHeight = fullPanelHeight * this.expandAnimation;
        float panelAlpha = this.expandAnimation * this.alphaMultiplier;
        int panelBgAlpha = (int)(200.0f * panelAlpha);
        Render2D.rect(boxX, startY, 65.0f, visibleHeight, new Color(30, 30, 30, panelBgAlpha).getRGB(), 3.0f);
        int panelOutlineAlpha = (int)(100.0f * panelAlpha);
        Render2D.outline(boxX, startY, 65.0f, visibleHeight, 0.5f, new Color(80, 80, 85, panelOutlineAlpha).getRGB(), 3.0f);
        if (visibleHeight < 1.0f) {
            return;
        }
        Scissor.enable(boxX, startY, 65.0f, visibleHeight, 2.0f);
        float optionY = startY;
        for (int i = 0; i < options.size(); ++i) {
            String option = options.get(i);
            boolean optionHovered = (float)mouseX >= boxX && (float)mouseX <= boxX + 65.0f && (float)mouseY >= optionY && (float)mouseY <= optionY + 14.0f && this.expandAnimation > 0.8f;
            float hoverAnim = this.optionHoverAnimations.getOrDefault(option, Float.valueOf(0.0f)).floatValue();
            hoverAnim = this.lerp(hoverAnim, optionHovered ? 1.0f : 0.0f, deltaTime * 8.0f);
            this.optionHoverAnimations.put(option, Float.valueOf(hoverAnim));
            boolean isSelected = this.multiSelectSetting.isSelected(option);
            float checkAnim = this.checkAnimations.getOrDefault(option, Float.valueOf(0.0f)).floatValue();
            checkAnim = this.lerp(checkAnim, isSelected ? 1.0f : 0.0f, deltaTime * 10.0f);
            this.checkAnimations.put(option, Float.valueOf(checkAnim));
            if (hoverAnim > 0.01f) {
                int hoverBgAlpha = (int)(30.0f * hoverAnim * panelAlpha);
                Render2D.rect(boxX + 2.0f, optionY + 1.0f, 61.0f, 12.0f, new Color(100, 100, 105, hoverBgAlpha).getRGB(), 2.0f);
            }
            float checkSize = 6.0f;
            float checkX = boxX + 5.0f;
            float checkY = optionY + 7.0f - checkSize / 2.0f;
            int checkBgAlpha = (int)((40.0f + hoverAnim * 20.0f) * panelAlpha);
            Render2D.rect(checkX, checkY, checkSize, checkSize, new Color(55, 55, 60, checkBgAlpha).getRGB(), 2.0f);
            int checkOutlineAlpha = (int)((80.0f + hoverAnim * 40.0f) * panelAlpha);
            Render2D.outline(checkX, checkY, checkSize, checkSize, 0.5f, new Color(120, 120, 125, checkOutlineAlpha).getRGB(), 2.0f);
            if (checkAnim > 0.01f) {
                float innerSize = (checkSize - 2.0f) * checkAnim;
                float innerX = checkX + (checkSize - innerSize) / 2.0f;
                float innerY = checkY + (checkSize - innerSize) / 2.0f;
                int innerAlpha = (int)(220.0f * checkAnim * panelAlpha);
                Render2D.rect(innerX, innerY, innerSize, innerSize, new Color(140, 180, 160, innerAlpha).getRGB(), 1.5f);
            }
            float textX = checkX + checkSize + 4.0f;
            float textY = optionY + 7.0f - 2.5f;
            float availableTextWidth = 65.0f - checkSize - 14.0f;
            Object displayOption = option;
            float optionTextWidth = Fonts.BOLD.getWidth(option, 5.0f);
            if (optionTextWidth > availableTextWidth) {
                while (Fonts.BOLD.getWidth((String)displayOption + "..", 5.0f) > availableTextWidth && ((String)displayOption).length() > 1) {
                    displayOption = ((String)displayOption).substring(0, ((String)displayOption).length() - 1);
                }
                displayOption = (String)displayOption + "..";
            }
            int textGray = (int)(140.0f + checkAnim * 40.0f + hoverAnim * 20.0f);
            int textAlpha = (int)(200.0f * panelAlpha);
            Fonts.BOLD.draw((String)displayOption, textX, textY, 5.0f, new Color(textGray, textGray, textGray + 5, textAlpha).getRGB());
            optionY += 14.0f;
        }
        Scissor.disable();
    }

    private boolean isMainHover(double mouseX, double mouseY) {
        float boxX = this.x + this.width - 65.0f - 2.0f;
        float boxY = this.y + this.height / 2.0f - 5.0f;
        float boxHeight = 10.0f;
        return mouseX >= (double)boxX && mouseX <= (double)(boxX + 65.0f) && mouseY >= (double)boxY && mouseY <= (double)(boxY + boxHeight);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (this.isMainHover(mouseX, mouseY)) {
                this.expanded = !this.expanded;
                return true;
            }
            if (this.expanded && this.expandAnimation > 0.8f) {
                float startY;
                float boxX = this.x + this.width - 65.0f - 2.0f;
                float boxY = this.y + this.height / 2.0f - 5.0f;
                float optionY = startY = boxY + 10.0f + 2.0f;
                for (String option : this.multiSelectSetting.getList()) {
                    if (mouseX >= (double)boxX && mouseX <= (double)(boxX + 65.0f) && mouseY >= (double)optionY && mouseY <= (double)(optionY + 14.0f)) {
                        if (this.multiSelectSetting.isSelected(option)) {
                            this.multiSelectSetting.getSelected().remove(option);
                        } else {
                            this.multiSelectSetting.getSelected().add(option);
                        }
                        return true;
                    }
                    optionY += 14.0f;
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= (double)this.x && mouseX <= (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY <= (double)(this.y + this.height);
    }

    public float getTotalHeight() {
        float baseHeight = this.height;
        float expandedHeight = (float)this.multiSelectSetting.getList().size() * 14.0f * this.expandAnimation;
        return baseHeight + expandedHeight;
    }
}

