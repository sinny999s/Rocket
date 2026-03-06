/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.glfw.GLFW
 */
package rich.screens.clickgui;

import java.awt.Color;
import java.util.ArrayList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import rich.IMinecraft;
import rich.Initialization;
import rich.modules.module.ModuleRepository;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.screens.clickgui.impl.DragHandler;
import rich.screens.clickgui.impl.autobuy.autobuyui.AutoBuyRenderer;
import rich.screens.clickgui.impl.background.BackgroundComponent;
import rich.screens.clickgui.impl.configs.ConfigsRenderer;
import rich.screens.clickgui.impl.module.ModuleComponent;
import rich.screens.clickgui.impl.settingsrender.BindComponent;
import rich.screens.clickgui.impl.settingsrender.TextComponent;
import rich.util.animations.Direction;
import rich.util.animations.GuiAnimation;
import rich.util.interfaces.AbstractSettingComponent;
import rich.util.math.FrameRateCounter;
import rich.util.render.Render2D;
import rich.util.render.gif.GifRender;
import rich.util.render.shader.Scissor;

public class ClickGui
extends Screen
implements IMinecraft {
    public static ClickGui INSTANCE = new ClickGui();
    private static final int FIXED_GUI_SCALE = 2;
    private final BackgroundComponent background = new BackgroundComponent();
    private final ModuleComponent moduleComponent = new ModuleComponent();
    private final AutoBuyRenderer autoBuyRenderer = new AutoBuyRenderer();
    private final ConfigsRenderer configsRenderer = new ConfigsRenderer();
    private final DragHandler dragHandler = new DragHandler();
    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    private final GuiAnimation openAnimation = new GuiAnimation();
    private boolean closing = false;
    private boolean waitingForSlide = false;
    private boolean slideTriggered = false;
    private float hintAlphaAnimation = 0.0f;
    private long lastHintUpdateTime = System.currentTimeMillis();
    private static final float HINT_ANIM_SPEED = 6.0f;
    private static final float OFFSET_THRESHOLD = 5.0f;
    private int lastMouseX;
    private int lastMouseY;
    private float lastDelta;

    public ClickGui() {
        super(Component.nullToEmpty((String)"MenuScreen"));
    }

    public boolean isClosing() {
        return this.closing;
    }

    protected void init() {
        super.init();
        this.closing = false;
        this.waitingForSlide = false;
        this.slideTriggered = false;
        this.openAnimation.setMs(250).setValue(1.0).setDirection(Direction.FORWARDS).reset();
        this.hintAlphaAnimation = 0.0f;
        this.lastHintUpdateTime = System.currentTimeMillis();
        long handle = mc.getWindow().handle();
        double centerX = (double)mc.getWindow().getScreenWidth() / 2.0;
        double centerY = (double)mc.getWindow().getScreenHeight() / 2.0;
        GLFW.glfwSetCursorPos((long)handle, (double)centerX, (double)centerY);
        this.background.setSearchActive(false);
        this.autoBuyRenderer.resetForClose();
        this.updateModules();
    }

    private void updateModules() {
        ArrayList<ModuleStructure> modules = new ArrayList<ModuleStructure>();
        try {
            ModuleRepository repo = Initialization.getInstance().getManager().getModuleRepository();
            if (repo != null) {
                for (ModuleStructure m : repo.modules()) {
                    if (m.getCategory() != this.selectedCategory) continue;
                    modules.add(m);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.moduleComponent.updateModules(modules, this.selectedCategory);
    }

    public void openGui() {
        if (ClickGui.mc.screen == null) {
            this.closing = false;
            this.waitingForSlide = false;
            this.slideTriggered = false;
            this.openAnimation.setMs(250).setValue(1.0).setDirection(Direction.FORWARDS).reset();
            mc.setScreen(this);
        }
    }

    public void tick() {
        GifRender.tick();
        this.moduleComponent.tick();
        super.tick();
    }

    private float[] calculateBackground(float scale) {
        int vw = mc.getWindow().getScreenWidth() / 2;
        int vh = mc.getWindow().getScreenHeight() / 2;
        float bgX = (float)(vw - 400) / 2.0f + this.dragHandler.getOffsetX();
        float bgY = (float)(vh - 250) / 2.0f + this.dragHandler.getOffsetY();
        return new float[]{bgX, bgY, vw, vh};
    }

    private boolean isAnyBindListening() {
        for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
            BindComponent bindComponent;
            if (!(c instanceof BindComponent) || !(bindComponent = (BindComponent)c).isListening()) continue;
            return true;
        }
        return false;
    }

    private void updateHintAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastHintUpdateTime) / 1000.0f, 0.1f);
        this.lastHintUpdateTime = currentTime;
        float offsetX = Math.abs(this.dragHandler.getOffsetX());
        float offsetY = Math.abs(this.dragHandler.getOffsetY());
        boolean shouldShow = offsetX > 5.0f || offsetY > 5.0f;
        float target = shouldShow ? 1.0f : 0.0f;
        float diff = target - this.hintAlphaAnimation;
        if (Math.abs(diff) < 0.001f) {
            this.hintAlphaAnimation = target;
        } else {
            this.hintAlphaAnimation += diff * 6.0f * deltaTime;
            this.hintAlphaAnimation = Math.max(0.0f, Math.min(1.0f, this.hintAlphaAnimation));
        }
    }

    private boolean isModuleCategory(ModuleCategory category) {
        return category != ModuleCategory.AUTOBUY;
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        this.lastDelta = delta;
        FrameRateCounter.INSTANCE.recordFrame();
        if (this.waitingForSlide && this.selectedCategory == ModuleCategory.AUTOBUY) {
            if (!this.slideTriggered) {
                this.autoBuyRenderer.triggerSlideOut();
                this.slideTriggered = true;
            }
            if (this.autoBuyRenderer.isSlideOutComplete()) {
                this.waitingForSlide = false;
                this.slideTriggered = false;
                this.startActualClose();
            }
        }
        if (this.closing && !this.waitingForSlide && this.openAnimation.isFinished(Direction.BACKWARDS)) {
            this.closing = false;
            TextComponent.typing = false;
            this.moduleComponent.setBindingModule(null);
            this.dragHandler.stopDrag();
            this.autoBuyRenderer.resetForClose();
            ClickGui.mc.screen = null;
        }
    }

    public void renderOverlay(GuiGraphics context, DeltaTracker tickCounter) {
        if (mc.getWindow() == null) {
            return;
        }
        float delta = this.lastDelta;
        int mouseX = this.lastMouseX;
        int mouseY = this.lastMouseY;
        float scrollSpeed = Math.min(1.0f, 60.0f / (float)Math.max(FrameRateCounter.INSTANCE.getFps(), 1));
        float animValue = this.openAnimation.getOutput().floatValue();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        context.nextStratum();
        int dimAlpha = (int)(125.0f * animValue);
        if (dimAlpha > 0) {
            Render2D.rect(0.0f, 0.0f, 5000.0f, 5000.0f, new Color(0, 0, 0, dimAlpha).getRGB(), 0.0f);
        }
        int guiScale = mc.getWindow().calculateScale((Integer)ClickGui.mc.options.guiScale().get(), mc.isEnforceUnicode());
        float scale = 2.0f / (float)guiScale;
        float mx = (float)mouseX / scale;
        float my = (float)mouseY / scale;
        if (!this.closing || this.waitingForSlide) {
            this.dragHandler.update(mx, my);
        }
        this.updateHintAnimation();
        context.pose().pushMatrix();
        context.pose().scale(scale, scale);
        float[] bg = this.calculateBackground(scale);
        float bgX = bg[0];
        float bgY = bg[1];
        int vw = (int)bg[2];
        int vh = (int)bg[3];
        float yOffset = this.closing && !this.waitingForSlide ? (1.0f - animValue) * 30.0f : (1.0f - animValue) * -15.0f;
        float alphaMultiplier = animValue;
        context.pose().pushMatrix();
        this.background.render(context, bgX, bgY += yOffset, this.selectedCategory, delta, alphaMultiplier);
        this.background.renderCategoryPanel(bgX, bgY, alphaMultiplier);
        this.background.renderHeader(bgX, bgY, this.selectedCategory, alphaMultiplier);
        this.background.renderCategoryNames(bgX, bgY, this.selectedCategory, alphaMultiplier);
        float mlX = bgX + 92.0f;
        float mlY = bgY + 38.0f;
        float mlW = 120.0f;
        float mlH = 204.0f;
        float spX = bgX + 218.0f;
        float spY = bgY + 38.0f;
        float spW = 172.0f;
        float spH = 204.0f;
        float normalAlpha = this.background.getNormalPanelAlpha();
        float searchAlpha = this.background.getSearchPanelAlpha();
        if (normalAlpha > 0.01f) {
            boolean slidingToModuleCategory;
            this.configsRenderer.render(context, bgX, bgY, mx, my, delta, 2, alphaMultiplier * normalAlpha, this.selectedCategory);
            boolean isAutoBuySliding = this.autoBuyRenderer.isSliding();
            boolean shouldRenderModules = this.isModuleCategory(this.selectedCategory);
            boolean bl = slidingToModuleCategory = isAutoBuySliding && this.isModuleCategory(this.selectedCategory);
            if (shouldRenderModules || slidingToModuleCategory) {
                this.moduleComponent.updateScroll(delta, scrollSpeed);
                this.moduleComponent.updateScrollFades(delta, scrollSpeed, mlH, spH);
                this.moduleComponent.renderModuleList(context, mlX, mlY, mlW, mlH, mx, my, 2, alphaMultiplier * normalAlpha);
                this.moduleComponent.renderSettingsPanel(context, spX, spY, spW, spH, mx, my, delta, 2, alphaMultiplier * normalAlpha);
            }
            this.autoBuyRenderer.render(context, bgX, bgY, mx, my, delta, 2, alphaMultiplier * normalAlpha, this.selectedCategory);
        }
        if (searchAlpha > 0.01f) {
            this.background.renderSearchResults(context, bgX, bgY, mx, my, 2, alphaMultiplier);
        }
        Scissor.reset();
        context.pose().popMatrix();
        float finalHintAlpha = this.hintAlphaAnimation * alphaMultiplier;
        if (finalHintAlpha > 0.01f) {
            int hintAlpha = (int)(255.0f * finalHintAlpha);
            float centerX = (float)vw / 2.0f;
            float centerY = (float)vh / 2.0f;
            float f = centerY + 125.0f + 10.0f;
        }
        context.pose().popMatrix();
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        ModuleCategory cat;
        float bgY;
        float[] bg;
        float bgX;
        double my;
        if (this.closing) {
            return false;
        }
        int guiScale = mc.getWindow().calculateScale((Integer)ClickGui.mc.options.guiScale().get(), mc.isEnforceUnicode());
        float scale = 2.0f / (float)guiScale;
        double mx = click.x() / (double)scale;
        if (this.background.isSearchBoxHovered(mx, my = click.y() / (double)scale, bgX = (bg = this.calculateBackground(scale))[0], bgY = bg[1]) && click.button() == 0) {
            this.background.setSearchActive(true);
            return true;
        }
        if (this.background.isSearchActive()) {
            ModuleStructure searchModule;
            if (click.button() == 0) {
                ModuleStructure searchModule2 = this.background.getSearchModuleAtPosition(mx, my, bgX, bgY);
                if (searchModule2 != null) {
                    searchModule2.switchState();
                    return true;
                }
                float panelX = bgX + 92.0f;
                float panelY = bgY + 38.0f;
                float panelW = 300.0f;
                float panelH = 204.0f;
                if (mx >= (double)panelX && mx <= (double)(panelX + panelW) && my >= (double)panelY && my <= (double)(panelY + panelH)) {
                    return true;
                }
                if (!this.background.isSearchBoxHovered(mx, my, bgX, bgY)) {
                    this.background.setSearchActive(false);
                }
            } else if (click.button() == 1 && (searchModule = this.background.getSearchModuleAtPosition(mx, my, bgX, bgY)) != null) {
                this.background.setSearchActive(false);
                this.selectedCategory = searchModule.getCategory();
                this.moduleComponent.selectModuleFromSearch(searchModule);
                this.updateModules();
                return true;
            }
            return true;
        }
        ModuleStructure mouseBinding = this.moduleComponent.getBindingModule();
        if (mouseBinding != null && click.button() >= 3) {
            mouseBinding.setKey(click.button());
            mouseBinding.setType(0);
            this.moduleComponent.setBindingModule(null);
            return true;
        }
        if (this.selectedCategory == ModuleCategory.AUTOBUY && this.autoBuyRenderer.mouseClicked(mx, my, click.button(), bgX, bgY, this.selectedCategory)) {
            return true;
        }
        float mlX = bgX + 92.0f;
        float mlY = bgY + 38.0f;
        float mlW = 120.0f;
        float mlH = 202.0f;
        if (click.button() == 2) {
            if (this.isAnyBindListening()) {
                for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
                    BindComponent bindComponent;
                    if (!(c instanceof BindComponent) || !(bindComponent = (BindComponent)c).isListening()) continue;
                    bindComponent.handleMiddleMouseBind();
                    return true;
                }
            }
            if (this.moduleComponent.getBindingModule() != null) {
                return true;
            }
            ModuleStructure module = this.moduleComponent.getModuleAtPosition(mx, my, mlX, mlY, mlW, mlH);
            if (module != null) {
                this.moduleComponent.setBindingModule(module);
                return true;
            }
            if (this.dragHandler.startDrag(mx, my, bgX, bgY, 400, 250)) {
                return true;
            }
        }
        if ((cat = this.background.getCategoryAtPosition(mx, my, bgX, bgY)) != null) {
            this.selectedCategory = cat;
            this.updateModules();
            return true;
        }
        if (this.isModuleCategory(this.selectedCategory)) {
            ModuleStructure starModule = this.moduleComponent.getModuleForStarClick(mx, my, mlX, mlY, mlW, mlH);
            if (starModule != null && click.button() == 0) {
                this.moduleComponent.toggleFavorite(starModule);
                return true;
            }
            ModuleStructure module = this.moduleComponent.getModuleAtPosition(mx, my, mlX, mlY, mlW, mlH);
            if (module != null) {
                if (click.button() == 0) {
                    module.switchState();
                } else if (click.button() == 1) {
                    this.moduleComponent.selectModule(module);
                }
                return true;
            }
            float spX = bgX + 218.0f;
            float spY = bgY + 38.0f;
            float spW = 172.0f;
            float spH = 202.0f;
            if (mx >= (double)spX && mx <= (double)(spX + spW) && my >= (double)spY && my <= (double)(spY + spH)) {
                for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
                    if (!c.getSetting().isVisible() || !c.mouseClicked(mx, my, click.button())) continue;
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        if (this.closing) {
            return false;
        }
        if (this.selectedCategory == ModuleCategory.AUTOBUY) {
            this.autoBuyRenderer.mouseReleased(click.x(), click.y(), click.button());
        }
        for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
            if (!c.getSetting().isVisible() || !c.mouseReleased(click.x(), click.y(), click.button())) continue;
            return true;
        }
        return super.mouseReleased(click);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (this.closing) {
            return false;
        }
        if (this.isAnyBindListening()) {
            for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
                BindComponent bindComponent;
                if (!(c instanceof BindComponent) || !(bindComponent = (BindComponent)c).isListening()) continue;
                bindComponent.handleScrollBind(vertical);
                return true;
            }
        }
        if (this.moduleComponent.getBindingModule() != null) {
            return true;
        }
        int guiScale = mc.getWindow().calculateScale((Integer)ClickGui.mc.options.guiScale().get(), mc.isEnforceUnicode());
        float scale = 2.0f / (float)guiScale;
        double mx = mouseX / (double)scale;
        double my = mouseY / (double)scale;
        float[] bg = this.calculateBackground(scale);
        float bgX = bg[0];
        float bgY = bg[1];
        if (this.background.isSearchActive()) {
            float panelX = bgX + 92.0f;
            float panelY = bgY + 38.0f;
            float panelW = 300.0f;
            float panelH = 204.0f;
            if (mx >= (double)panelX && mx <= (double)(panelX + panelW) && my >= (double)panelY && my <= (double)(panelY + panelH)) {
                this.background.handleSearchScroll(vertical, panelH);
                return true;
            }
        }
        if (this.selectedCategory == ModuleCategory.AUTOBUY && this.autoBuyRenderer.mouseScrolled(mx, my, vertical, bgX, bgY, this.selectedCategory)) {
            return true;
        }
        float mlX = bgX + 92.0f;
        float mlY = bgY + 38.0f;
        float mlW = 120.0f;
        float mlH = 202.0f;
        if (mx >= (double)mlX && mx <= (double)(mlX + mlW) && my >= (double)mlY && my <= (double)(mlY + mlH)) {
            this.moduleComponent.handleModuleScroll(vertical, mlH);
            return true;
        }
        float spX = bgX + 218.0f;
        float spY = bgY + 38.0f;
        float spW = 172.0f;
        float spH = 202.0f;
        if (mx >= (double)spX && mx <= (double)(spX + spW) && my >= (double)spY && my <= (double)(spY + spH)) {
            this.moduleComponent.handleSettingScroll(vertical, spH);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    public boolean keyPressed(KeyEvent input) {
        if (input.key() == 256) {
            if (this.autoBuyRenderer.isEditing()) {
                return true;
            }
            if (this.configsRenderer.isEditing()) {
                return true;
            }
            if (this.background.isSearchActive()) {
                this.background.setSearchActive(false);
                return true;
            }
            this.onClose();
            return true;
        }
        if (this.closing) {
            return false;
        }
        if (this.selectedCategory == ModuleCategory.AUTOBUY && this.autoBuyRenderer.keyPressed(input.key(), input.scancode(), input.modifiers())) {
            return true;
        }
        if (this.background.isSearchActive() && this.background.handleSearchKey(input.key())) {
            return true;
        }
        if (this.dragHandler.isResetNeeded(input.key(), input.modifiers())) {
            this.dragHandler.reset();
            return true;
        }
        ModuleStructure binding = this.moduleComponent.getBindingModule();
        if (binding != null) {
            if (input.key() == 261) {
                binding.setKey(-1);
                binding.setType(1);
            } else {
                binding.setKey(input.key());
                binding.setType(1);
            }
            this.moduleComponent.setBindingModule(null);
            return true;
        }
        for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
            if (!c.getSetting().isVisible() || !c.keyPressed(input.key(), input.scancode(), input.modifiers())) continue;
            return true;
        }
        return super.keyPressed(input);
    }

    public boolean charTyped(CharacterEvent input) {
        if (this.closing) {
            return false;
        }
        if (this.selectedCategory == ModuleCategory.AUTOBUY && this.autoBuyRenderer.charTyped((char)input.codepoint(), input.modifiers())) {
            return true;
        }
        if (this.background.isSearchActive() && this.background.handleSearchChar((char)input.codepoint())) {
            return true;
        }
        for (AbstractSettingComponent c : this.moduleComponent.getSettingComponents()) {
            if (!c.getSetting().isVisible() || !c.charTyped((char)input.codepoint(), input.modifiers())) continue;
            return true;
        }
        return super.charTyped(input);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void startActualClose() {
        this.openAnimation.setDirection(Direction.BACKWARDS);
        this.openAnimation.reset();
        long handle = mc.getWindow().handle();
        double centerX = (double)mc.getWindow().getScreenWidth() / 2.0;
        double centerY = (double)mc.getWindow().getScreenHeight() / 2.0;
        GLFW.glfwSetInputMode((long)handle, (int)208897, (int)212995);
        GLFW.glfwSetCursorPos((long)handle, (double)centerX, (double)centerY);
        TextComponent.typing = false;
        this.moduleComponent.setBindingModule(null);
        this.background.setSearchActive(false);
        this.dragHandler.stopDrag();
    }

    public void onClose() {
        if (!this.closing) {
            this.closing = true;
            if (this.selectedCategory == ModuleCategory.AUTOBUY) {
                this.waitingForSlide = true;
                this.slideTriggered = false;
            } else {
                this.waitingForSlide = false;
                this.startActualClose();
            }
        }
    }
}

