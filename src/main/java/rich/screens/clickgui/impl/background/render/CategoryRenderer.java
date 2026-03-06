
package rich.screens.clickgui.impl.background.render;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import rich.modules.module.category.ModuleCategory;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class CategoryRenderer {
    private static final ModuleCategory[] MAIN_CATEGORIES = new ModuleCategory[]{ModuleCategory.COMBAT, ModuleCategory.MOVEMENT, ModuleCategory.RENDER, ModuleCategory.PLAYER, ModuleCategory.MISC, ModuleCategory.WORLD};
    private static final String[] MAIN_CATEGORY_NAMES = new String[]{"Combat", "Movement", "Render", "Player", "Util", "World"};
    private static final String[] MAIN_CATEGORY_ICONS = new String[]{"a", "b", "c", "d", "e", "f"};
    private static final ModuleCategory[] EXTRA_CATEGORIES = new ModuleCategory[]{ModuleCategory.AUTOBUY};
    private static final String[] EXTRA_CATEGORY_NAMES = new String[]{"AutoBuy"};
    private static final String[] EXTRA_CATEGORY_ICONS = new String[]{"g"};
    private final Map<ModuleCategory, Float> categoryAnimations = new HashMap<ModuleCategory, Float>();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float MAX_OFFSET = 5.0f;
    private static final float BALL_SIZE = 3.0f;
    private static final float TEXT_SIZE = 6.0f;
    private static final float ICON_SIZE = 6.0f;
    private static final float ICON_SPACING = 4.0f;
    private static final float SECTION_TEXT_SIZE = 5.0f;
    private static final float EXTRA_CATEGORY_OFFSET = 10.0f;

    public CategoryRenderer() {
        for (ModuleCategory cat : MAIN_CATEGORIES) {
            this.categoryAnimations.put(cat, Float.valueOf(0.0f));
        }
        for (ModuleCategory cat : EXTRA_CATEGORIES) {
            this.categoryAnimations.put(cat, Float.valueOf(0.0f));
        }
    }

    public void updateAnimations(ModuleCategory selectedCategory, float deltaTime) {
        for (ModuleCategory cat : MAIN_CATEGORIES) {
            this.updateCategoryAnimation(cat, selectedCategory, deltaTime);
        }
        for (ModuleCategory cat : EXTRA_CATEGORIES) {
            this.updateCategoryAnimation(cat, selectedCategory, deltaTime);
        }
    }

    private void updateCategoryAnimation(ModuleCategory cat, ModuleCategory selected, float deltaTime) {
        float target = cat == selected ? 1.0f : 0.0f;
        float current = this.categoryAnimations.getOrDefault((Object)cat, Float.valueOf(0.0f)).floatValue();
        float diff = target - current;
        float change = diff * 8.0f * deltaTime;
        if (Math.abs(diff) < 0.001f) {
            this.categoryAnimations.put(cat, Float.valueOf(target));
        } else {
            this.categoryAnimations.put(cat, Float.valueOf(current + change));
        }
    }

    public void render(float bgX, float bgY, ModuleCategory selectedCategory, float alphaMultiplier) {
        this.renderSectionHeader(bgX, bgY + 52.0f, "Main", alphaMultiplier);
        this.renderMainCategories(bgX, bgY, alphaMultiplier);
        this.renderSectionHeader(bgX, bgY + 62.0f + (float)MAIN_CATEGORY_NAMES.length * 15.0f + 10.0f - 10.0f, "Friends", alphaMultiplier);
        this.renderExtraCategories(bgX, bgY, alphaMultiplier);
    }

    private void renderSectionHeader(float bgX, float sectionY, String title, float alphaMultiplier) {
        float lineWidth = 18.0f;
        float textWidth = Fonts.BOLD.getWidth(title, 5.0f);
        float totalWidth = 65.0f;
        float textX = bgX + 15.0f + (totalWidth - textWidth) / 2.0f;
        float lineY = sectionY + 3.0f;
        int lineAlpha = (int)(40.0f * alphaMultiplier);
        int textAlpha = (int)(100.0f * alphaMultiplier);
        Render2D.rect(bgX + 15.0f, lineY, lineWidth, 0.5f, new Color(255, 255, 255, lineAlpha).getRGB(), 0.0f);
        Render2D.rect(bgX + 15.0f + totalWidth - lineWidth, lineY, lineWidth, 0.5f, new Color(255, 255, 255, lineAlpha).getRGB(), 0.0f);
        Fonts.BOLD.draw(title, textX, sectionY, 5.0f, new Color(150, 150, 150, textAlpha).getRGB());
    }

    private void renderMainCategories(float bgX, float bgY, float alphaMultiplier) {
        for (int i = 0; i < MAIN_CATEGORY_NAMES.length; ++i) {
            ModuleCategory cat = MAIN_CATEGORIES[i];
            float animation = this.categoryAnimations.getOrDefault((Object)cat, Float.valueOf(0.0f)).floatValue();
            float textY = bgY + 65.0f + (float)i * 15.0f;
            this.renderCategoryItem(bgX, textY, MAIN_CATEGORY_NAMES[i], MAIN_CATEGORY_ICONS[i], animation, alphaMultiplier);
        }
    }

    private void renderExtraCategories(float bgX, float bgY, float alphaMultiplier) {
        float separatorY = bgY + 65.0f + (float)MAIN_CATEGORY_NAMES.length * 15.0f + 1.0f;
        float extraStartY = separatorY + 18.0f - 10.0f;
        for (int i = 0; i < EXTRA_CATEGORY_NAMES.length; ++i) {
            ModuleCategory cat = EXTRA_CATEGORIES[i];
            float animation = this.categoryAnimations.getOrDefault((Object)cat, Float.valueOf(0.0f)).floatValue();
            float textY = extraStartY + (float)i * 15.0f;
            this.renderCategoryItem(bgX, textY, EXTRA_CATEGORY_NAMES[i], EXTRA_CATEGORY_ICONS[i], animation, alphaMultiplier);
        }
    }

    private void renderCategoryItem(float bgX, float textY, String name, String icon, float animation, float alphaMultiplier) {
        float offsetX = animation * 5.0f;
        int baseGray = 128;
        int targetWhite = 255;
        int colorValue = (int)((float)baseGray + (float)(targetWhite - baseGray) * animation);
        int alpha = (int)((128.0f + 127.0f * animation) * alphaMultiplier);
        Color textColor = new Color(colorValue, colorValue, colorValue, alpha);
        float iconX = bgX + 17.0f + offsetX;
        float iconWidth = Fonts.CATEGORY_ICONS.getWidth(icon, 6.0f);
        float textX = iconX + iconWidth + 4.0f;
        float textWidth = Fonts.BOLD.getWidth(name, 6.0f);
        Fonts.CATEGORY_ICONS.draw(icon, iconX, textY + 0.5f, 6.0f, textColor.getRGB());
        if (animation > 0.01f) {
            float lineWidth = (iconWidth + 4.0f + textWidth) * animation;
            float lineAlpha = animation * 60.0f * alphaMultiplier;
            Render2D.rect(iconX, textY + 9.0f, lineWidth, 0.5f, new Color(255, 255, 255, (int)lineAlpha).getRGB(), 0.0f);
            float ballAlpha = animation * 200.0f * alphaMultiplier;
            float ballX = bgX + 12.0f;
            float ballY = textY + 2.5f;
            Render2D.rect(ballX, ballY, 3.0f, 3.0f, new Color(255, 255, 255, (int)ballAlpha).getRGB(), 1.5f);
        }
        Fonts.BOLD.draw(name, textX, textY, 6.0f, textColor.getRGB());
    }

    public ModuleCategory getCategoryAtPosition(double mouseX, double mouseY, float bgX, float bgY) {
        if (mouseX < (double)(bgX + 10.0f) || mouseX > (double)(bgX + 95.0f)) {
            return null;
        }
        for (int i = 0; i < MAIN_CATEGORY_NAMES.length; ++i) {
            float catY = 65.0f + (float)i * 15.0f;
            if (!(mouseY >= (double)(bgY + catY)) || !(mouseY <= (double)(bgY + catY + 13.0f))) continue;
            return MAIN_CATEGORIES[i];
        }
        float separatorY = 65.0f + (float)MAIN_CATEGORY_NAMES.length * 15.0f + 1.0f;
        float extraStartY = separatorY + 18.0f - 10.0f;
        for (int i = 0; i < EXTRA_CATEGORIES.length; ++i) {
            float catY = extraStartY + (float)i * 15.0f;
            if (!(mouseY >= (double)(bgY + catY)) || !(mouseY <= (double)(bgY + catY + 13.0f))) continue;
            return EXTRA_CATEGORIES[i];
        }
        return null;
    }
}

