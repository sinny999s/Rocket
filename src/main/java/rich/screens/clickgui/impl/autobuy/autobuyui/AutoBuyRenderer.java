
package rich.screens.clickgui.impl.autobuy.autobuyui;

import net.minecraft.client.gui.GuiGraphics;
import rich.IMinecraft;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.autobuy.autobuyui.AutoBuyGuiComponent;

public class AutoBuyRenderer
implements IMinecraft {
    private final AutoBuyGuiComponent autoBuyComponent = new AutoBuyGuiComponent();
    private ModuleCategory lastCategory = null;
    private float categoryAlpha = 0.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private boolean wasActive = false;
    private boolean pendingSlideOut = false;
    private boolean slideOutComplete = false;
    private static final float FADE_SPEED = 14.0f;

    public void render(GuiGraphics context, float bgX, float bgY, float mouseX, float mouseY, float delta, int guiScale, float alphaMultiplier, ModuleCategory currentCategory) {
        boolean isActive;
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0f, 0.1f);
        this.lastUpdateTime = currentTime;
        boolean bl = isActive = currentCategory == ModuleCategory.AUTOBUY;
        if (this.wasActive && !isActive && !this.pendingSlideOut) {
            this.pendingSlideOut = true;
            this.slideOutComplete = false;
            this.autoBuyComponent.startSlideOut();
        }
        if (isActive && !this.wasActive) {
            this.pendingSlideOut = false;
            this.slideOutComplete = false;
            this.autoBuyComponent.resetSlide();
            this.autoBuyComponent.resetPositions();
        }
        if (this.pendingSlideOut && this.autoBuyComponent.isSlidOut()) {
            this.slideOutComplete = true;
            this.pendingSlideOut = false;
        }
        this.wasActive = isActive;
        float targetAlpha = isActive ? 1.0f : (this.pendingSlideOut ? 1.0f : 0.0f);
        float diff = targetAlpha - this.categoryAlpha;
        this.categoryAlpha = Math.abs(diff) < 0.01f ? targetAlpha : (this.categoryAlpha += diff * 14.0f * deltaTime);
        this.categoryAlpha = Math.max(0.0f, Math.min(1.0f, this.categoryAlpha));
        if (this.categoryAlpha <= 0.01f && !this.pendingSlideOut) {
            return;
        }
        float panelX = bgX + 92.0f;
        float panelY = bgY + 38.0f;
        float panelW = 300.0f;
        float panelH = 204.0f;
        this.autoBuyComponent.position(panelX, panelY);
        this.autoBuyComponent.size(panelW, panelH);
        this.autoBuyComponent.resetHover();
        this.autoBuyComponent.render(context, mouseX, mouseY, delta, guiScale, alphaMultiplier * this.categoryAlpha);
    }

    public boolean isSliding() {
        return this.pendingSlideOut && !this.slideOutComplete;
    }

    public void triggerSlideOut() {
        if (!this.autoBuyComponent.isSlidingOut()) {
            this.pendingSlideOut = true;
            this.slideOutComplete = false;
            this.autoBuyComponent.startSlideOut();
        }
    }

    public boolean isSlideOutComplete() {
        return this.slideOutComplete || this.autoBuyComponent.isSlidOut();
    }

    public void resetForClose() {
        this.pendingSlideOut = false;
        this.slideOutComplete = false;
        this.autoBuyComponent.resetSlide();
        this.categoryAlpha = 0.0f;
        this.wasActive = false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, float bgX, float bgY, ModuleCategory currentCategory) {
        if (currentCategory != ModuleCategory.AUTOBUY) {
            return false;
        }
        if (this.categoryAlpha < 0.5f) {
            return false;
        }
        if (this.autoBuyComponent.isSlidingOut()) {
            return false;
        }
        float panelX = bgX + 92.0f;
        float panelY = bgY + 38.0f;
        float panelW = 300.0f;
        float panelH = 204.0f;
        return this.autoBuyComponent.mouseClicked(mouseX, mouseY, button, panelX, panelY, panelW, panelH);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount, float bgX, float bgY, ModuleCategory currentCategory) {
        if (currentCategory != ModuleCategory.AUTOBUY) {
            return false;
        }
        if (this.categoryAlpha < 0.5f) {
            return false;
        }
        if (this.autoBuyComponent.isSlidingOut()) {
            return false;
        }
        float panelX = bgX + 92.0f;
        float panelY = bgY + 38.0f;
        float panelW = 300.0f;
        float panelH = 204.0f;
        return this.autoBuyComponent.mouseScrolled(mouseX, mouseY, amount, panelX, panelY, panelW, panelH);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.categoryAlpha < 0.5f) {
            return false;
        }
        return this.autoBuyComponent.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.categoryAlpha < 0.5f) {
            return false;
        }
        return this.autoBuyComponent.charTyped(chr, modifiers);
    }

    public boolean isEditing() {
        return this.autoBuyComponent.isEditing();
    }

    public float getCategoryAlpha() {
        return this.categoryAlpha;
    }

    public boolean isOnAutoBuy() {
        return this.wasActive;
    }
}

