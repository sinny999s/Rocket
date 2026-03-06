
package rich.screens.clickgui.impl.settingsrender;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class SelectComponent
extends AbstractSettingComponent {
    private final SelectSetting selectSetting;
    private boolean expanded = false;
    private float expandAnimation = 0.0f;
    private float hoverAnimation = 0.0f;
    private float descScrollOffset = 0.0f;
    private boolean descScrollingRight = true;
    private long descScrollPauseTime = 0L;
    private float arrowRotation = 0.0f;
    private final Map<String, Float> optionHoverAnimations = new HashMap<String, Float>();
    private final Map<String, Float> selectAnimations = new HashMap<String, Float>();
    private String previousSelected = "";
    private float selectedTextAlpha = 1.0f;
    private float selectedTextSlide = 1.0f;
    private float newSelectedTextAlpha = 0.0f;
    private float newSelectedTextSlide = 0.0f;
    private String animatingFromText = "";
    private boolean isAnimatingSelection = false;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float COLLAPSE_SPEED = 15.0f;
    private static final float BOX_WIDTH = 65.0f;
    private static final float OPTION_HEIGHT = 14.0f;
    private static final long SCROLL_PAUSE_DURATION = 2000L;
    private static final float SCROLL_PIXELS_PER_SECOND = 20.0f;
    private static final float DESC_PADDING = 8.0f;
    private static final float SELECTION_ANIMATION_SPEED = 10.0f;

    public SelectComponent(SelectSetting setting) {
        super(setting);
        this.selectSetting = setting;
        this.previousSelected = setting.getSelected();
        for (String option : setting.getList()) {
            this.optionHoverAnimations.put(option, Float.valueOf(0.0f));
            this.selectAnimations.put(option, Float.valueOf(setting.isSelected(option) ? 1.0f : 0.0f));
        }
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

    private void updateSelectionAnimation(float deltaTime) {
        String currentSelected = this.selectSetting.getSelected();
        if (!currentSelected.equals(this.previousSelected) && !this.isAnimatingSelection) {
            this.animatingFromText = this.previousSelected;
            this.isAnimatingSelection = true;
            this.selectedTextAlpha = 1.0f;
            this.selectedTextSlide = 1.0f;
            this.newSelectedTextAlpha = 0.0f;
            this.newSelectedTextSlide = 0.0f;
        }
        if (this.isAnimatingSelection) {
            this.selectedTextAlpha = this.lerp(this.selectedTextAlpha, 0.0f, deltaTime * 10.0f);
            this.selectedTextSlide = this.lerp(this.selectedTextSlide, 0.0f, deltaTime * 10.0f);
            if (this.selectedTextAlpha < 0.5f) {
                this.newSelectedTextAlpha = this.lerp(this.newSelectedTextAlpha, 1.0f, deltaTime * 10.0f);
                this.newSelectedTextSlide = this.lerp(this.newSelectedTextSlide, 1.0f, deltaTime * 10.0f);
            }
            if (this.newSelectedTextAlpha > 0.99f && this.newSelectedTextSlide > 0.99f) {
                this.isAnimatingSelection = false;
                this.previousSelected = currentSelected;
                this.selectedTextAlpha = 1.0f;
                this.selectedTextSlide = 1.0f;
                this.newSelectedTextAlpha = 1.0f;
                this.newSelectedTextSlide = 1.0f;
            }
        } else {
            this.previousSelected = currentSelected;
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        float deltaTime = this.getDeltaTime();
        this.updateSelectionAnimation(deltaTime);
        boolean mainHovered = this.isMainHover(mouseX, mouseY);
        this.hoverAnimation = this.lerp(this.hoverAnimation, mainHovered ? 1.0f : 0.0f, deltaTime * 8.0f);
        float expandSpeed = this.expanded ? 8.0f : 15.0f;
        this.expandAnimation = this.lerp(this.expandAnimation, this.expanded ? 1.0f : 0.0f, deltaTime * expandSpeed);
        float targetRotation = this.expanded ? 90.0f : 0.0f;
        this.arrowRotation = this.lerp(this.arrowRotation, targetRotation, deltaTime * 8.0f);
        Fonts.GUI_ICONS.draw("J", this.x - 0.5f, this.y + this.height / 2.0f - 8.5f, 9.0f, this.applyAlpha(new Color(210, 210, 210, 200)).getRGB());
        Fonts.BOLD.draw(this.selectSetting.getName(), this.x + 9.5f, this.y + this.height / 2.0f - 7.5f, 6.0f, this.applyAlpha(new Color(210, 210, 220, 200)).getRGB());
        String description = this.selectSetting.getDescription();
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
        this.renderAnimatedSelectedText(boxX, boxY, boxHeight);
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

    private void renderAnimatedSelectedText(float boxX, float boxY, float boxHeight) {
        float maxTextWidth = 51.0f;
        float textY = boxY + boxHeight / 2.0f - 2.5f;
        Scissor.enable(boxX + 2.0f, boxY, maxTextWidth + 2.0f, boxHeight, 2.0f);
        if (this.isAnimatingSelection) {
            if (this.selectedTextAlpha > 0.01f) {
                String displayOld = this.truncateText(this.animatingFromText, maxTextWidth);
                float slideOffset = (1.0f - this.selectedTextSlide) * -15.0f;
                int alpha = (int)(200.0f * this.selectedTextAlpha * this.alphaMultiplier);
                Fonts.BOLD.draw(displayOld, boxX + 4.0f + slideOffset, textY, 5.0f, new Color(160, 160, 165, alpha).getRGB());
            }
            if (this.newSelectedTextAlpha > 0.01f) {
                String selected = this.selectSetting.getSelected();
                String displayNew = this.truncateText(selected, maxTextWidth);
                float slideOffset = (1.0f - this.newSelectedTextSlide) * 20.0f;
                int alpha = (int)(200.0f * this.newSelectedTextAlpha * this.alphaMultiplier);
                Fonts.BOLD.draw(displayNew, boxX + 4.0f + slideOffset, textY, 5.0f, new Color(160, 160, 165, alpha).getRGB());
            }
        } else {
            String selected = this.selectSetting.getSelected();
            String displaySelected = this.truncateText(selected, maxTextWidth);
            Fonts.BOLD.draw(displaySelected, boxX + 4.0f, textY, 5.0f, this.applyAlpha(new Color(160, 160, 165, 200)).getRGB());
        }
        Scissor.disable();
    }

    private String truncateText(String text, float maxWidth) {
        if (Fonts.BOLD.getWidth(text, 5.0f) <= maxWidth) {
            return text;
        }
        String truncated = text;
        while (Fonts.BOLD.getWidth(truncated + "..", 5.0f) > maxWidth && truncated.length() > 1) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        return truncated + "..";
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

    private void renderExpandedOptions(GuiGraphics context, int mouseX, int mouseY, float boxX, float startY, float deltaTime) {
        List<String> options = this.selectSetting.getList();
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
            boolean isSelected = this.selectSetting.isSelected(option);
            float selectAnim = this.selectAnimations.getOrDefault(option, Float.valueOf(0.0f)).floatValue();
            selectAnim = this.lerp(selectAnim, isSelected ? 1.0f : 0.0f, deltaTime * 10.0f);
            this.selectAnimations.put(option, Float.valueOf(selectAnim));
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
            if (selectAnim > 0.01f) {
                float innerSize = (checkSize - 2.0f) * selectAnim;
                float innerX = checkX + (checkSize - innerSize) / 2.0f;
                float innerY = checkY + (checkSize - innerSize) / 2.0f;
                int innerAlpha = (int)(220.0f * selectAnim * panelAlpha);
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
            int textGray = (int)(140.0f + selectAnim * 40.0f + hoverAnim * 20.0f);
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
                for (String option : this.selectSetting.getList()) {
                    if (mouseX >= (double)boxX && mouseX <= (double)(boxX + 65.0f) && mouseY >= (double)optionY && mouseY <= (double)(optionY + 14.0f)) {
                        this.selectSetting.setSelected(option);
                        this.expanded = false;
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
        float expandedHeight = (float)this.selectSetting.getList().size() * 14.0f * this.expandAnimation;
        return baseHeight + expandedHeight;
    }
}

