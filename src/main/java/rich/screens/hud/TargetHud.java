
package rich.screens.hud;

import java.awt.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import rich.client.draggables.AbstractHudElement;
import rich.modules.impl.combat.Aura;
import rich.util.ColorUtil;
import rich.util.network.Network;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.timer.StopWatch;

public class TargetHud
extends AbstractHudElement {
    private final StopWatch stopWatch = new StopWatch();
    private LivingEntity lastTarget;
    private float healthAnimation = 0.0f;
    private float trailAnimation = 0.0f;
    private float absorptionAnimation = 0.0f;
    private float displayedHealth = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private long startTime = System.currentTimeMillis();

    public TargetHud() {
        super("TargetHud", 10, 80, 112, 40, true);
    }

    @Override
    public boolean visible() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity auraTarget = Aura.target;
        if (auraTarget != null) {
            this.lastTarget = auraTarget;
            this.startAnimation();
            this.stopWatch.reset();
        } else if (this.isChat(this.mc.screen)) {
            this.lastTarget = this.mc.player;
            this.startAnimation();
            this.stopWatch.reset();
        } else if (this.stopWatch.finished(10.0)) {
            this.stopAnimation();
        }
    }

    private float lerp(float current, float target, float deltaTime, float speed) {
        float factor = (float)(1.0 - Math.pow(0.001, deltaTime * speed));
        return current + (target - current) * factor;
    }

    private float snapToStep(float value, float step) {
        return (float)Math.round(value / step) * step;
    }

    private float getHealth(LivingEntity entity) {
        if (entity.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime()) {
            return entity.getMaxHealth();
        }
        return entity.getHealth();
    }

    private String getHealthString(float health) {
        if (this.lastTarget != null && this.lastTarget.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime()) {
            return "??";
        }
        if (health >= 100.0f) {
            return String.valueOf((int)health);
        }
        if (health >= 10.0f) {
            return String.format("%.1f", Float.valueOf(health));
        }
        return String.format("%.2f", Float.valueOf(health));
    }

    @Override
    public void drawDraggable(GuiGraphics context, int alpha) {
        if (alpha <= 0) {
            return;
        }
        if (this.lastTarget == null) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
        this.lastUpdateTime = currentTime;
        deltaTime = Math.min(deltaTime, 0.1f);
        float x = this.getX();
        float y = this.getY();
        this.setWidth(112);
        this.setHeight(40);
        float scaleAlpha = this.scaleAnimation.getOutput().floatValue();
        this.drawBackground(x, y, scaleAlpha);
        this.drawFace(x, y, scaleAlpha);
        this.drawContent(x, y, scaleAlpha, deltaTime);
    }

    private void drawBackground(float x, float y, float alpha) {
        int alphaInt = (int)(255.0f * alpha);
        Render2D.gradientRect(x + 2.0f, y + 2.0f, this.getWidth() - 4, this.getHeight() - 4, new int[]{new Color(52, 52, 52, alphaInt).getRGB(), new Color(22, 22, 22, alphaInt).getRGB(), new Color(52, 52, 52, alphaInt).getRGB(), new Color(22, 22, 22, alphaInt).getRGB()}, 6.0f);
        Render2D.outline(x + 2.0f, y + 2.0f, this.getWidth() - 4, this.getHeight() - 4, 0.35f, new Color(90, 90, 90, alphaInt).getRGB(), 5.0f);
        int blurTint = ColorUtil.rgba(0, 0, 0, 0);
        Render2D.blur(x + 2.0f, y + 2.0f, 1.0f, 1.0f, 0.0f, 7.0f, blurTint);
    }

    private void drawFace(float x, float y, float alpha) {
        EntityRenderer baseRenderer = this.mc.getEntityRenderDispatcher().getRenderer(this.lastTarget);
        if (!(baseRenderer instanceof LivingEntityRenderer)) {
            return;
        }
        LivingEntityRenderer renderer = (LivingEntityRenderer)baseRenderer;
        LivingEntityRenderState state = (LivingEntityRenderState)renderer.createRenderState(this.lastTarget, this.lastTickDelta);
        Identifier textureLocation = renderer.getTextureLocation(state);
        float faceSize = 24.0f;
        float faceX = x + 9.0f;
        float faceY = y + 8.0f;
        float hurtPercent = this.lastTarget.hurtTime > 0 ? (float)this.lastTarget.hurtTime / 10.0f : 0.0f;
        int r = 255;
        int g = (int)(255.0f * (1.0f - hurtPercent));
        int b = (int)(255.0f * (1.0f - hurtPercent));
        int color = new Color(r, g, b, (int)(255.0f * alpha)).getRGB();
        float u0 = 0.125f;
        float v0 = 0.125f;
        float u1 = 0.25f;
        float v1 = 0.25f;
        Render2D.texture(textureLocation, faceX, faceY, faceSize, faceSize, u0, v0, u1, v1, color, 0.0f, 4.0f);
        float hatScale = 1.1f;
        float hatSize = faceSize * hatScale;
        float hatOffset = (hatSize - faceSize) / 2.0f;
        float hatU0 = 0.625f;
        float hatV0 = 0.125f;
        float hatU1 = 0.75f;
        float hatV1 = 0.25f;
        Render2D.texture(textureLocation, faceX - hatOffset, faceY - hatOffset, hatSize, hatSize, hatU0, hatV0, hatU1, hatV1, color, 0.0f, 4.0f);
    }

    private void drawContent(float x, float y, float alpha, float deltaTime) {
        float absorptionPercent;
        float faceSize = 24.0f;
        float faceX = x + 9.0f;
        float contentX = faceX + faceSize + 6.0f;
        float nameY = y + 13.0f;
        float hp = this.getHealth(this.lastTarget);
        float maxHp = this.lastTarget.getMaxHealth();
        float absorp = this.lastTarget.getAbsorptionAmount();
        boolean isInvisible = this.lastTarget.isInvisible() && !Network.isSpookyTime() && !Network.isCopyTime();
        float targetDisplayHealth = isInvisible ? maxHp : hp + absorp;
        this.displayedHealth = this.lerp(this.displayedHealth, targetDisplayHealth, deltaTime, 5.0f);
        float snappedHealth = this.snapToStep(this.displayedHealth, 0.25f);
        String hpStr = this.getHealthString(snappedHealth);
        String name = this.lastTarget.getName().getString();
        float hpWidth = Fonts.BOLD.getWidth(hpStr, 5.5f);
        Fonts.BOLD.draw(name, contentX, nameY, 5.5f, new Color(255, 255, 255, (int)(255.0f * alpha)).getRGB());
        int hpColor = new Color(215, 215, 215, (int)(255.0f * alpha)).getRGB();
        Fonts.BOLD.draw(hpStr, x + (float)this.getWidth() - 10.0f - hpWidth, nameY, 5.5f, hpColor);
        float targetHealth = isInvisible ? 1.0f : hp / maxHp;
        this.healthAnimation = this.lerp(this.healthAnimation, targetHealth, deltaTime, 3.0f);
        if (targetHealth > this.trailAnimation) {
            this.trailAnimation = targetHealth;
        }
        this.trailAnimation = this.lerp(this.trailAnimation, targetHealth, deltaTime, 3.5f);
        float targetAbsorption = isInvisible ? 0.0f : absorp / maxHp;
        this.absorptionAnimation = this.lerp(this.absorptionAnimation, targetAbsorption, deltaTime, 3.0f);
        float barX = contentX;
        float barY = nameY + 12.0f;
        float barWidth = 64.0f;
        float barHeight = 4.0f;
        float barRadius = 2.0f;
        Render2D.rect(barX, barY, barWidth, barHeight, new Color(30, 30, 30, (int)(200.0f * alpha)).getRGB(), barRadius);
        float healthPercent = Math.max(0.0f, Math.min(1.0f, this.healthAnimation));
        float trailPercent = Math.max(0.0f, Math.min(1.0f, this.trailAnimation));
        if (trailPercent > healthPercent) {
            int trailColor = new Color(55, 55, 55, (int)(160.0f * alpha)).getRGB();
            Render2D.rect(barX, barY, barWidth * trailPercent, barHeight, trailColor, barRadius);
        }
        if (healthPercent > 0.01f) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            float waveSpeed = 1500.0f;
            float wavePhase = (float)(elapsed % (long)waveSpeed) / waveSpeed * (float)Math.PI * 2.0f;
            int[] colors = new int[4];
            for (int i = 0; i < 2; ++i) {
                float charWave = (float)Math.sin(wavePhase - (float)i * 1.5f);
                float waveFactor = (charWave + 1.0f) / 2.0f;
                int baseGray = (int)(155.0f + 100.0f * waveFactor);
                colors[i * 2] = new Color(baseGray, baseGray, baseGray, (int)(255.0f * alpha)).getRGB();
                colors[i * 2 + 1] = new Color(baseGray, baseGray, baseGray, (int)(255.0f * alpha)).getRGB();
            }
            Render2D.gradientRect(barX, barY, barWidth * healthPercent, barHeight, colors, barRadius);
        }
        if ((absorptionPercent = Math.max(0.0f, Math.min(1.0f, this.absorptionAnimation))) > 0.01f && !Network.isFunTime()) {
            long elapsed = System.currentTimeMillis() - this.startTime;
            float waveSpeed = 1200.0f;
            float wavePhase = (float)(elapsed % (long)waveSpeed) / waveSpeed * (float)Math.PI * 2.0f;
            int[] goldColors = new int[4];
            for (int i = 0; i < 2; ++i) {
                float charWave = (float)Math.sin(wavePhase - (float)i * 1.5f);
                float waveFactor = (charWave + 1.0f) / 2.0f;
                int cr = 255;
                int cg = (int)(165.0f + 50.0f * waveFactor);
                int cb = 0;
                goldColors[i * 2] = new Color(cr, cg, cb, (int)(200.0f * alpha)).getRGB();
                goldColors[i * 2 + 1] = new Color(cr, cg, cb, (int)(200.0f * alpha)).getRGB();
            }
            Render2D.gradientRect(barX, barY, barWidth * absorptionPercent, barHeight, goldColors, barRadius);
        }
    }
}

