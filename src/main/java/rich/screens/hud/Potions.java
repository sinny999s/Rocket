
package rich.screens.hud;

import java.awt.Color;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import rich.client.draggables.AbstractHudElement;
import rich.util.animations.Direction;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class Potions
extends AbstractHudElement {
    private List<MobEffectInstance> effectsList = new ArrayList<MobEffectInstance>();
    private Map<String, Float> effectAnimations = new LinkedHashMap<String, Float>();
    private Map<String, MobEffectInstance> cachedEffects = new LinkedHashMap<String, MobEffectInstance>();
    private Set<String> activeEffectIds = new HashSet<String>();
    private float animatedWidth = 80.0f;
    private float animatedHeight = 23.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private long lastEffectChange = 0L;
    private String currentRandomEffect = "speed";
    private static final List<String> RANDOM_EFFECTS = List.of((String[])new String[]{"speed", "slowness", "haste", "mining_fatigue", "strength", "jump_boost", "regeneration", "resistance", "fire_resistance", "water_breathing", "invisibility", "night_vision", "hunger", "weakness", "poison", "wither", "health_boost", "absorption"});
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float ICON_SIZE = 9.0f;
    private static final int BLINK_THRESHOLD_TICKS = 100;

    public Potions() {
        super("Potions", 300, 100, 80, 23, true);
        this.stopAnimation();
    }

    @Override
    public boolean visible() {
        return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
    }

    @Override
    public void tick() {
        long currentTime;
        if (this.mc.player == null) {
            this.effectsList = new ArrayList<MobEffectInstance>();
            this.activeEffectIds.clear();
            this.stopAnimation();
            return;
        }
        Collection<MobEffectInstance> effects = this.mc.player.getActiveEffects();
        this.effectsList = new ArrayList<MobEffectInstance>(effects.stream().filter(MobEffectInstance::showIcon).toList());
        this.activeEffectIds.clear();
        for (MobEffectInstance effect : this.effectsList) {
            String id = this.getEffectId(effect);
            this.activeEffectIds.add(id);
            this.cachedEffects.put(id, effect);
            if (this.effectAnimations.containsKey(id)) continue;
            this.effectAnimations.put(id, Float.valueOf(0.0f));
        }
        boolean hasActiveEffects = !this.activeEffectIds.isEmpty() || !this.effectAnimations.isEmpty();
        boolean inChat = this.isChat(this.mc.screen);
        if (hasActiveEffects || inChat) {
            this.startAnimation();
        } else {
            this.stopAnimation();
        }
        if (this.effectsList.isEmpty() && inChat && (currentTime = System.currentTimeMillis()) - this.lastEffectChange >= 1000L) {
            this.currentRandomEffect = RANDOM_EFFECTS.get(new Random().nextInt(RANDOM_EFFECTS.size()));
            this.lastEffectChange = currentTime;
        }
    }

    private String getEffectId(MobEffectInstance effect) {
        return effect.getEffect().unwrapKey().map(key -> key.identifier().toString()).orElse("unknown_" + effect.hashCode());
    }

    private float lerp(float current, float target, float deltaTime) {
        float factor = (float)(1.0 - Math.pow(0.001, deltaTime * 8.0f));
        return current + (target - current) * factor;
    }

    private String formatDuration(int ticks) {
        if (ticks == -1) {
            return "\u221e\u221e:\u221e\u221e";
        }
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private String getEffectName(MobEffectInstance effect) {
        return ((MobEffect)effect.getEffect().value()).getDisplayName().getString();
    }

    private String getLevelText(int amplifier) {
        if (amplifier <= 0) {
            return "";
        }
        return "LVL " + (amplifier + 1);
    }

    private float getFullNameWidth(MobEffectInstance effect) {
        String name = this.getEffectName(effect);
        int amplifier = effect.getAmplifier();
        float nameWidth = Fonts.BOLD.getWidth(name, 6.0f);
        if (amplifier > 0) {
            String levelText = this.getLevelText(amplifier);
            float levelWidth = Fonts.REGULAR.getWidth(levelText, 6.0f);
            return nameWidth + 3.0f + levelWidth;
        }
        return nameWidth;
    }

    private Identifier getEffectTexture(Holder<MobEffect> effect) {
        return effect.unwrapKey().map(ResourceKey::identifier).map(id -> id.withPrefix("mob_effect/")).orElse(Identifier.withDefaultNamespace((String)"mob_effect/speed"));
    }

    private Identifier getRandomEffectTexture() {
        return Identifier.withDefaultNamespace((String)("mob_effect/" + this.currentRandomEffect));
    }

    private int getBlinkAlpha(int duration, int baseAlpha) {
        if (duration == -1 || duration > 100) {
            return baseAlpha;
        }
        long currentTime = System.currentTimeMillis();
        double blinkSpeed = 0.008;
        double blinkWave = Math.sin((double)currentTime * blinkSpeed);
        float blinkFactor = (float)((blinkWave + 1.0) / 2.0);
        int minAlpha = Math.max(50, baseAlpha - 150);
        return (int)((float)minAlpha + (float)(baseAlpha - minAlpha) * (1.0f - blinkFactor));
    }

    @Override
    public void drawDraggable(GuiGraphics context, int alpha) {
        if (alpha <= 0) {
            return;
        }
        float alphaFactor = (float)alpha / 255.0f;
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
        this.lastUpdateTime = currentTime;
        deltaTime = Math.min(deltaTime, 0.1f);
        ArrayList<String> toRemove = new ArrayList<String>();
        for (Map.Entry<String, Float> entry : this.effectAnimations.entrySet()) {
            float targetAnim;
            String id = entry.getKey();
            float currentAnim = entry.getValue().floatValue();
            float newAnim = this.lerp(currentAnim, targetAnim = this.activeEffectIds.contains(id) ? 1.0f : 0.0f, deltaTime);
            if (Math.abs(newAnim - targetAnim) < 0.01f) {
                newAnim = targetAnim;
            }
            if (newAnim <= 0.01f && targetAnim == 0.0f) {
                toRemove.add(id);
                continue;
            }
            this.effectAnimations.put(id, Float.valueOf(newAnim));
        }
        for (String id : toRemove) {
            this.effectAnimations.remove(id);
            this.cachedEffects.remove(id);
        }
        float x = this.getX();
        float y = this.getY();
        boolean hasAnimatingEffects = !this.effectAnimations.isEmpty();
        boolean showExample = !hasAnimatingEffects && this.isChat(this.mc.screen);
        int offset = 23;
        float targetWidth = 80.0f;
        String exampleTimer = "00:00";
        if (showExample) {
            offset += 11;
            String name = "Example Effect";
            String string = "LVL";
            float timerWidth = Fonts.BOLD.getWidth(exampleTimer, 6.0f);
            float nameWidth = Fonts.BOLD.getWidth(name, 6.0f);
            float levelWidth = Fonts.REGULAR.getWidth(string, 6.0f);
            targetWidth = Math.max(nameWidth + 3.0f + levelWidth + timerWidth + 60.0f, targetWidth);
        } else if (hasAnimatingEffects) {
            for (Map.Entry entry : this.effectAnimations.entrySet()) {
                MobEffectInstance effect;
                String id = (String)entry.getKey();
                float animation = ((Float)entry.getValue()).floatValue();
                if (animation <= 0.0f || (effect = this.cachedEffects.get(id)) == null) continue;
                offset += (int)(animation * 11.0f);
                String timer = this.formatDuration(effect.getDuration());
                float timerWidth = Fonts.BOLD.getWidth(timer, 6.0f);
                float fullNameWidth = this.getFullNameWidth(effect);
                targetWidth = Math.max(fullNameWidth + timerWidth + 60.0f, targetWidth);
            }
        }
        float targetHeight = offset + 2;
        this.animatedWidth = this.lerp(this.animatedWidth, targetWidth, deltaTime);
        this.animatedHeight = this.lerp(this.animatedHeight, targetHeight, deltaTime);
        if (Math.abs(this.animatedWidth - targetWidth) < 0.3f) {
            this.animatedWidth = targetWidth;
        }
        if (Math.abs(this.animatedHeight - targetHeight) < 0.3f) {
            this.animatedHeight = targetHeight;
        }
        this.setWidth((int)Math.ceil(this.animatedWidth));
        this.setHeight((int)Math.ceil(this.animatedHeight));
        float f = this.animatedHeight;
        int bgAlpha = (int)(255.0f * alphaFactor);
        if (f > 0.0f) {
            Render2D.gradientRect(x, y, this.getWidth(), f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB()}, 5.0f);
            Render2D.outline(x, y, this.getWidth(), f, 0.35f, new Color(90, 90, 90, bgAlpha).getRGB(), 5.0f);
        }
        Scissor.enable(x, y, this.getWidth(), f, 2.0f);
        int effectsCount = this.activeEffectIds.isEmpty() ? 1 : this.activeEffectIds.size();
        String countText = String.valueOf(effectsCount);
        float countTextWidth = Fonts.BOLD.getWidth(countText, 6.0f);
        float potionsTextWidth = Fonts.BOLD.getWidth("Potions", 6.0f);
        Render2D.gradientRect(x + (float)this.getWidth() - countTextWidth - potionsTextWidth + 3.0f, y + 5.0f, 14.0f, 12.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB()}, 3.0f);
        Fonts.HUD_ICONS.draw("f", x + (float)this.getWidth() - countTextWidth - potionsTextWidth + 5.0f, y + 6.0f, 10.0f, new Color(165, 165, 165, bgAlpha).getRGB());
        Fonts.BOLD.draw("Potions", x + 8.0f, y + 6.5f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
        int moduleOffset = 23;
        if (showExample) {
            String name = "Example Effect";
            String levelText = "LVL";
            String timer = "00:00";
            float timerWidth = Fonts.BOLD.getWidth(timer, 6.0f);
            float timerBoxX = x + (float)this.getWidth() - timerWidth - 11.5f;
            Render2D.gradientRect(timerBoxX, y + (float)moduleOffset - 2.0f, timerWidth + 4.0f, 9.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB()}, 3.0f);
            Render2D.outline(timerBoxX, y + (float)moduleOffset - 2.0f, timerWidth + 4.0f, 9.0f, 0.05f, new Color(132, 132, 132, bgAlpha).getRGB(), 2.0f);
            Identifier randomTexture = this.getRandomEffectTexture();
            float scale = 0.5f;
            float iconX = x + 8.0f;
            float iconY = y + (float)moduleOffset - 2.5f;
            context.pose().pushMatrix();
            context.pose().translate(iconX, iconY);
            context.pose().scale(scale, scale);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, randomTexture, 0, 0, 18, 18);
            context.pose().popMatrix();
            float nameX = x + 20.0f;
            Fonts.BOLD.draw(name, nameX, y + (float)moduleOffset - 1.5f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
            float nameWidth = Fonts.BOLD.getWidth(name, 6.0f);
            Fonts.TEST.draw(levelText, nameX + nameWidth + 2.0f, y + (float)moduleOffset - 0.5f, 5.0f, new Color(155, 155, 155, bgAlpha).getRGB());
            Fonts.BOLD.draw(timer, timerBoxX + 2.0f, y + (float)moduleOffset - 1.0f, 6.0f, new Color(165, 165, 165, bgAlpha).getRGB());
        } else if (hasAnimatingEffects) {
            for (Map.Entry<String, Float> entry : this.effectAnimations.entrySet()) {
                MobEffectInstance effect;
                String id = entry.getKey();
                float animation = entry.getValue().floatValue();
                if (animation <= 0.0f || (effect = this.cachedEffects.get(id)) == null) continue;
                String name = this.getEffectName(effect);
                int amplifier = effect.getAmplifier();
                String levelText = this.getLevelText(amplifier);
                String timer = this.formatDuration(effect.getDuration());
                int duration = effect.getDuration();
                float timerWidth = Fonts.BOLD.getWidth(timer, 6.0f);
                int baseAlpha = (int)(255.0f * animation * alphaFactor);
                int blinkAlpha = this.getBlinkAlpha(duration, baseAlpha);
                int textColor = new Color(255, 255, 255, blinkAlpha).getRGB();
                int levelColor = new Color(155, 155, 155, blinkAlpha).getRGB();
                int timerColor = new Color(165, 165, 165, blinkAlpha).getRGB();
                float timerBoxX = x + (float)this.getWidth() - timerWidth - 11.5f;
                Render2D.gradientRect(timerBoxX, y + (float)moduleOffset - 2.0f, timerWidth + 4.0f, 9.0f, new int[]{new Color(52, 52, 52, blinkAlpha).getRGB(), new Color(52, 52, 52, blinkAlpha).getRGB(), new Color(52, 52, 52, blinkAlpha).getRGB(), new Color(52, 52, 52, blinkAlpha).getRGB()}, 3.0f);
                Render2D.outline(timerBoxX, y + (float)moduleOffset - 2.0f, timerWidth + 4.0f, 9.0f, 0.05f, new Color(132, 132, 132, blinkAlpha).getRGB(), 2.0f);
                Identifier effectTexture = this.getEffectTexture(effect.getEffect());
                float scale = 0.5f;
                float iconX = x + 8.0f;
                float iconY = y + (float)moduleOffset - 2.5f;
                context.pose().pushMatrix();
                context.pose().translate(iconX, iconY);
                context.pose().scale(scale, scale);
                int iconColor = new Color(255, 255, 255, blinkAlpha).getRGB();
                context.blitSprite(RenderPipelines.GUI_TEXTURED, effectTexture, 0, 0, 18, 18, iconColor);
                context.pose().popMatrix();
                float nameX = x + 20.0f;
                Fonts.BOLD.draw(name, nameX, y + (float)moduleOffset - 1.5f, 6.0f, textColor);
                if (amplifier > 0) {
                    float nameWidth = Fonts.BOLD.getWidth(name, 6.0f);
                    Fonts.TEST.draw(levelText, nameX + nameWidth + 2.0f, y + (float)moduleOffset - 0.5f, 5.0f, levelColor);
                }
                Fonts.BOLD.draw(timer, timerBoxX + 2.0f, y + (float)moduleOffset - 1.0f, 6.0f, timerColor);
                moduleOffset += (int)(animation * 11.0f);
            }
        }
        Scissor.disable();
    }
}

