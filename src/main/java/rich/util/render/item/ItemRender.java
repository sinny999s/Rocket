/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3x2fStack
 */
package rich.util.render.item;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;
import rich.util.render.Render2D;

public class ItemRender {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Map<String, CachedSprite> SPRITE_CACHE = new ConcurrentHashMap<String, CachedSprite>();
    private static final RandomSource RANDOM = RandomSource.create();
    private static final int FORCED_GUI_SCALE = 2;

    private static int getCurrentGuiScale() {
        int scale = (Integer)ItemRender.mc.options.guiScale().get();
        if (scale == 0) {
            scale = mc.getWindow().calculateScale(0, mc.isEnforceUnicode());
        }
        return scale;
    }

    private static float getScaleCompensation() {
        return 2.0f / (float)ItemRender.getCurrentGuiScale();
    }

    public static boolean isBlockItem(ItemStack stack) {
        return stack.getItem() instanceof BlockItem;
    }

    public static boolean isPotionItem(ItemStack stack) {
        return stack.getItem() == Items.POTION || stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION || stack.getItem() == Items.TIPPED_ARROW;
    }

    public static boolean hasGlint(ItemStack stack) {
        return stack.hasFoil();
    }

    public static boolean needsContextRender(ItemStack stack) {
        return ItemRender.isBlockItem(stack) || ItemRender.isPotionItem(stack) || ItemRender.hasGlint(stack);
    }

    public static void drawItem(ItemStack stack, float x, float y, float scale, float alpha) {
        ItemRender.drawItem(stack, x, y, scale, alpha, -1);
    }

    public static void drawItem(ItemStack stack, float x, float y, float scale, float alpha, int tintColor) {
        if (stack.isEmpty() || alpha <= 0.01f) {
            return;
        }
        if (ItemRender.needsContextRender(stack)) {
            return;
        }
        TextureAtlasSprite sprite = ItemRender.getSpriteForStack(stack);
        if (sprite != null) {
            int color = ItemRender.applyAlpha(tintColor, alpha);
            float size = 16.0f * scale;
            Render2D.drawSprite(sprite, x, y, size, size, color, true);
        }
    }

    public static void drawBlockItem(GuiGraphics context, ItemStack stack, float x, float y, float scale, float alpha) {
        if (stack.isEmpty() || alpha <= 0.01f) {
            return;
        }
        float compensation = ItemRender.getScaleCompensation();
        float finalScale = scale * compensation;
        float size = 16.0f * scale;
        float centerX = x + size / 2.0f;
        float centerY = y + size / 2.0f;
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(finalScale, finalScale);
        matrices.translate(-8.0f, -8.0f);
        context.renderItem(stack, 0, 0);
        matrices.popMatrix();
    }

    public static void drawItemWithContext(GuiGraphics context, ItemStack stack, float x, float y, float scale, float alpha) {
        if (stack.isEmpty() || alpha <= 0.01f) {
            return;
        }
        float compensation = ItemRender.getScaleCompensation();
        float finalScale = scale * compensation;
        float size = 16.0f * scale;
        float centerX = x + size / 2.0f;
        float centerY = y + size / 2.0f;
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(finalScale, finalScale);
        matrices.translate(-8.0f, -8.0f);
        context.renderItem(stack, 0, 0);
        matrices.popMatrix();
    }

    public static void drawItemCentered(ItemStack stack, float centerX, float centerY, float scale, float alpha) {
        float size = 16.0f * scale;
        float x = centerX - size / 2.0f;
        float y = centerY - size / 2.0f;
        ItemRender.drawItem(stack, x, y, scale, alpha);
    }

    public static void drawItemCenteredWithContext(GuiGraphics context, ItemStack stack, float centerX, float centerY, float scale, float alpha) {
        float size = 16.0f * scale;
        float x = centerX - size / 2.0f;
        float y = centerY - size / 2.0f;
        ItemRender.drawItemWithContext(context, stack, x, y, scale, alpha);
    }

    private static TextureAtlasSprite getSpriteForStack(ItemStack stack) {
        String cacheKey = ItemRender.getCacheKey(stack);
        CachedSprite cached = SPRITE_CACHE.get(cacheKey);
        if (cached != null) {
            return cached.sprite;
        }
        try {
            ItemStackRenderState state = new ItemStackRenderState();
            mc.getItemModelResolver().updateForTopItem(state, stack, ItemDisplayContext.GUI, ItemRender.mc.level, null, 0);
            TextureAtlasSprite sprite = state.pickParticleIcon(RANDOM);
            if (sprite != null) {
                SPRITE_CACHE.put(cacheKey, new CachedSprite(sprite));
                return sprite;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private static String getCacheKey(ItemStack stack) {
        return stack.getItem().toString() + "_" + stack.getComponents().hashCode();
    }

    private static int applyAlpha(int color, float alpha) {
        int a = (int)((float)(color >> 24 & 0xFF) * alpha);
        return a << 24 | color & 0xFFFFFF;
    }

    public static void clearCache() {
        SPRITE_CACHE.clear();
    }

    private record CachedSprite(TextureAtlasSprite sprite) {
    }
}

