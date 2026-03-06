
package rich.screens.loading;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class Loading {
    private static Loading instance;
    private static final int TEXT_COLOR_BRIGHT = -1;
    private static final float FIXED_GUI_SCALE = 2.0f;
    private static final String[] LOADING_TEXTS;
    private static final long TEXT_DISPLAY_DURATION = 2200L;
    private static final long LAST_TEXT_DISPLAY_DURATION = 2500L;
    private static final long TEXT_TRANSITION_DURATION = 400L;
    private static final float ZOOM_LEVEL = 1.08f;
    private float animatedProgress = 0.0f;
    private float targetProgress = 0.0f;
    private float pulseTime = 0.0f;
    private long lastRenderTime = 0L;
    private long startTime = 0L;
    private boolean initialized = false;
    private int currentTextIndex = 0;
    private float currentTextOffsetY = 0.0f;
    private float currentTextAlpha = 1.0f;
    private float newTextOffsetY = -12.0f;
    private float newTextAlpha = 0.0f;
    private long lastTextChangeTime = 0L;
    private boolean isTransitioning = false;
    private long transitionStartTime = 0L;
    private float backgroundAlpha = 0.0f;
    private float contentAlpha = 0.0f;
    private boolean isFadingOut = false;
    private boolean readyToClose = false;
    private boolean resourcesLoaded = false;
    private boolean allTextsShown = false;
    private long lastTextShownTime = 0L;

    public Loading() {
        instance = this;
        this.lastTextChangeTime = this.startTime = Util.getMillis();
    }

    public static Loading getInstance() {
        if (instance == null) {
            instance = new Loading();
        }
        return instance;
    }

    private int getFixedScaledWidth() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return 960;
        }
        return (int)Math.ceil((double)client.getWindow().getWidth() / 2.0);
    }

    private int getFixedScaledHeight() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.getWindow() == null) {
            return 540;
        }
        return (int)Math.ceil((double)client.getWindow().getHeight() / 2.0);
    }

    public void render(int width, int height, float opacity) {
        long currentTime = Util.getMillis();
        if (!this.initialized) {
            this.lastRenderTime = currentTime;
            this.initialized = true;
        }
        float deltaTime = (float)(currentTime - this.lastRenderTime) / 1000.0f;
        this.lastRenderTime = currentTime;
        deltaTime = Mth.clamp((float)deltaTime, (float)0.001f, (float)0.1f);
        this.updateAnimations(deltaTime, currentTime);
        int fixedWidth = this.getFixedScaledWidth();
        int fixedHeight = this.getFixedScaledHeight();
        Render2D.beginOverlay();
        Render2D.backgroundImage(this.backgroundAlpha * opacity, 1.08f);
        float finalContentAlpha = this.contentAlpha * opacity;
        if (finalContentAlpha > 0.001f) {
            this.renderLogo(fixedWidth, fixedHeight, finalContentAlpha);
            this.renderLoadingText(fixedWidth, fixedHeight, finalContentAlpha, currentTime);
        }
        Render2D.endOverlay();
    }

    private void updateAnimations(float deltaTime, long currentTime) {
        long elapsed;
        this.pulseTime += deltaTime * 2.0f;
        this.animatedProgress = Mth.lerp((float)(deltaTime * 5.0f), (float)this.animatedProgress, (float)this.targetProgress);
        this.backgroundAlpha = Mth.lerp((float)(deltaTime * 5.0f), (float)this.backgroundAlpha, (float)1.0f);
        if (this.backgroundAlpha > 0.99f) {
            this.backgroundAlpha = 1.0f;
        }
        if (!this.isFadingOut) {
            this.contentAlpha = Mth.lerp((float)(deltaTime * 3.0f), (float)this.contentAlpha, (float)1.0f);
            if (this.contentAlpha > 0.99f) {
                this.contentAlpha = 1.0f;
            }
        } else {
            this.contentAlpha -= deltaTime * 2.0f;
            if (this.contentAlpha < 0.0f) {
                this.contentAlpha = 0.0f;
                this.readyToClose = true;
            }
        }
        if (!this.isFadingOut) {
            this.updateTextAnimation(currentTime, deltaTime);
        }
        if (this.allTextsShown && this.resourcesLoaded && !this.isFadingOut && (elapsed = currentTime - this.lastTextShownTime) >= 2500L) {
            this.isFadingOut = true;
        }
    }

    private void updateTextAnimation(long currentTime, float deltaTime) {
        long elapsed;
        if (this.allTextsShown) {
            return;
        }
        if (!this.isTransitioning) {
            elapsed = currentTime - this.lastTextChangeTime;
            if (this.currentTextIndex >= LOADING_TEXTS.length - 1) {
                if (!this.allTextsShown) {
                    this.allTextsShown = true;
                    this.lastTextShownTime = currentTime;
                }
                return;
            }
            if (elapsed >= 2200L) {
                this.isTransitioning = true;
                this.transitionStartTime = currentTime;
            }
        }
        if (this.isTransitioning) {
            elapsed = currentTime - this.transitionStartTime;
            float rawProgress = Mth.clamp((float)((float)elapsed / 400.0f), (float)0.0f, (float)1.0f);
            float easedProgress = this.easeOutQuad(rawProgress);
            this.currentTextOffsetY = 12.0f * easedProgress;
            this.currentTextAlpha = 1.0f - easedProgress * 1.5f;
            this.currentTextAlpha = Mth.clamp((float)this.currentTextAlpha, (float)0.0f, (float)1.0f);
            this.newTextOffsetY = -10.0f * (1.0f - easedProgress);
            this.newTextAlpha = easedProgress * 1.3f;
            this.newTextAlpha = Mth.clamp((float)this.newTextAlpha, (float)0.0f, (float)1.0f);
            if (rawProgress >= 1.0f) {
                this.isTransitioning = false;
                ++this.currentTextIndex;
                this.currentTextOffsetY = 0.0f;
                this.currentTextAlpha = 1.0f;
                this.newTextOffsetY = -12.0f;
                this.newTextAlpha = 0.0f;
                this.lastTextChangeTime = currentTime;
                if (this.currentTextIndex >= LOADING_TEXTS.length - 1) {
                    this.allTextsShown = true;
                    this.lastTextShownTime = currentTime;
                }
            }
        }
    }

    private float easeOutQuad(float x) {
        return 1.0f - (1.0f - x) * (1.0f - x);
    }

    private void renderLogo(int width, int height, float opacity) {
        float centerX = (float)width / 2.0f;
        float centerY = (float)height / 2.0f - 20.0f;
        this.renderLogoText(centerX, centerY, opacity);
    }

    private void renderLogoText(float centerX, float centerY, float opacity) {
        int textAlpha = (int)(opacity * 255.0f);
        float fontSize = 40.0f;
        float breathe = (float)Math.sin(this.pulseTime * 1.3f) * 1.3f;
        String text = "A";
        float textWidth = Fonts.ICONS.getWidth(text, fontSize);
        float textHeight = Fonts.ICONS.getHeight(fontSize);
        int shadowColor = this.withAlpha(-16777216, textAlpha / 3);
        Fonts.ICONS.draw(text, centerX - textWidth / 2.0f + 2.0f, centerY - textHeight / 2.0f + 2.0f + breathe, fontSize, shadowColor);
        int mainColor = this.withAlpha(new Color(255, 255, 255, 255).getRGB(), textAlpha);
        Fonts.ICONS.draw(text, centerX - textWidth / 2.0f, centerY - textHeight / 2.0f + breathe, fontSize, mainColor);
    }

    private void renderLoadingText(int width, int height, float opacity, long currentTime) {
        int nextIndex;
        float fontSize = 11.0f;
        float baseY = (float)height / 2.0f + 30.0f;
        float centerX = (float)width / 2.0f;
        if (this.currentTextAlpha > 0.01f && this.currentTextIndex < LOADING_TEXTS.length) {
            String currentText = LOADING_TEXTS[this.currentTextIndex];
            float currentWidth = Fonts.REGULARNEW.getWidth(currentText, fontSize);
            int alpha = (int)(opacity * this.currentTextAlpha * 255.0f);
            Fonts.REGULARNEW.draw(currentText, centerX - currentWidth / 2.0f, baseY + this.currentTextOffsetY, fontSize, this.withAlpha(-1, alpha));
        }
        if (this.isTransitioning && this.newTextAlpha > 0.01f && (nextIndex = this.currentTextIndex + 1) < LOADING_TEXTS.length) {
            String nextText = LOADING_TEXTS[nextIndex];
            float nextWidth = Fonts.REGULARNEW.getWidth(nextText, fontSize);
            int alpha = (int)(opacity * this.newTextAlpha * 255.0f);
            Fonts.REGULARNEW.draw(nextText, centerX - nextWidth / 2.0f, baseY + this.newTextOffsetY, fontSize, this.withAlpha(-1, alpha));
        }
    }

    public void markComplete() {
        this.resourcesLoaded = true;
    }

    public boolean isContentFadedOut() {
        return this.isFadingOut && this.contentAlpha <= 0.01f;
    }

    public boolean isReadyToClose() {
        return this.readyToClose;
    }

    public boolean isComplete() {
        return this.allTextsShown && this.resourcesLoaded;
    }

    public boolean isFadingOut() {
        return this.isFadingOut;
    }

    public float getContentAlpha() {
        return this.contentAlpha;
    }

    public void setProgress(float progress) {
        this.targetProgress = Mth.clamp((float)progress, (float)0.0f, (float)1.0f);
    }

    public float getProgress() {
        return this.targetProgress;
    }

    public void reset() {
        this.animatedProgress = 0.0f;
        this.targetProgress = 0.0f;
        this.pulseTime = 0.0f;
        this.lastRenderTime = 0L;
        this.startTime = Util.getMillis();
        this.initialized = false;
        this.currentTextIndex = 0;
        this.currentTextOffsetY = 0.0f;
        this.currentTextAlpha = 1.0f;
        this.newTextOffsetY = -12.0f;
        this.newTextAlpha = 0.0f;
        this.lastTextChangeTime = this.startTime;
        this.isTransitioning = false;
        this.transitionStartTime = 0L;
        this.backgroundAlpha = 0.0f;
        this.contentAlpha = 0.0f;
        this.isFadingOut = false;
        this.readyToClose = false;
        this.resourcesLoaded = false;
        this.allTextsShown = false;
        this.lastTextShownTime = 0L;
    }

    public long getStartTime() {
        return this.startTime;
    }

    private int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | Mth.clamp((int)alpha, (int)0, (int)255) << 24;
    }

    static {
        LOADING_TEXTS = new String[]{"Loading", "Preparing", "Initializing", "Almost ready"};
    }
}

