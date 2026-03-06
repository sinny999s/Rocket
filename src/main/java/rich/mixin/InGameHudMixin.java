/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.Initialization;
import rich.events.api.EventManager;
import rich.events.impl.DrawEvent;
import rich.events.impl.HotbarItemRenderEvent;
import rich.modules.impl.render.Hud;
import rich.modules.impl.render.NoRender;
import rich.screens.clickgui.ClickGui;
import rich.util.render.Render2D;

@Mixin(value={Gui.class})
public abstract class InGameHudMixin
implements IMinecraft {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique
    private int richCurrentHotbarIndex = 0;

    @Inject(method={"renderItemHotbar"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderHotbarStart(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        this.richCurrentHotbarIndex = 0;
        if (this.richIsCustomHudActive()) {
            ci.cancel();
        }
    }

    @WrapOperation(method={"renderItemHotbar"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/Gui;renderSlot(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/client/DeltaTracker;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;I)V")})
    private void onRenderHotbarItem(Gui instance, GuiGraphics context, int x, int y, DeltaTracker tickCounter, Player player, ItemStack stack, int seed, Operation<Void> original) {
        int hotbarIndex = this.richCurrentHotbarIndex;
        if (this.richCurrentHotbarIndex < 9) {
            ++this.richCurrentHotbarIndex;
        }
        HotbarItemRenderEvent event = new HotbarItemRenderEvent(stack, hotbarIndex);
        EventManager.callEvent(event);
        original.call(new Object[]{instance, context, x, y, tickCounter, player, event.getStack(), seed});
    }

    @Inject(method={"renderConfusionOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderNauseaOverlay(GuiGraphics context, float nauseaStrength, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Nausea")) {
            ci.cancel();
        }
    }

    @Inject(method={"renderScoreboardSidebar"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderScoreboard(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Scoreboard")) {
            ci.cancel();
        }
    }

    @Inject(method={"renderBossOverlay"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderBossBar(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("BossBar")) {
            ci.cancel();
        }
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    public void onRenderCustomHud(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (this.minecraft.options.hideGui) {
            return;
        }
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return;
        }
        if (this.minecraft.getOverlay() != null) {
            return;
        }
        Screen screen = this.minecraft.screen;
        if (this.isLoadingScreen(screen)) {
            return;
        }
        context.nextStratum();
        Render2D.beginOverlay();
        context.pose().pushMatrix();
        DrawEvent event = new DrawEvent(context, drawEngine, tickCounter.getGameTimeDeltaPartialTick(false));
        EventManager.callEvent(event);
        context.pose().popMatrix();
        if (this.shouldRenderHud(screen)) {
            int mouseX = (int)this.minecraft.mouseHandler.getScaledXPos(this.minecraft.getWindow());
            int mouseY = (int)this.minecraft.mouseHandler.getScaledYPos(this.minecraft.getWindow());
            float tickDelta = tickCounter.getGameTimeDeltaPartialTick(false);
            Hud hud = Hud.getInstance();
            if (hud != null && hud.isState() && Initialization.getInstance() != null && Initialization.getInstance().getManager() != null && Initialization.getInstance().getManager().getHudManager() != null) {
                Initialization.getInstance().getManager().getHudManager().render(context, tickDelta, mouseX, mouseY);
            }
        }
        Render2D.endOverlay();
    }

    @Inject(method={"renderHotbarAndDecorations"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderHotbarAndDecorations(GuiGraphics context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (this.richIsCustomHudActive()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderPlayerHealth"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderPlayerHealth(GuiGraphics context, CallbackInfo ci) {
        if (this.richIsCustomHudActive()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderFood"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderFood(GuiGraphics context, Player player, int top, int right, CallbackInfo ci) {
        if (this.richIsCustomHudActive()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderArmor"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onRenderArmor(GuiGraphics context, Player player, int yPos, int heartRows, int maxHearts, int xPos, CallbackInfo ci) {
        if (InGameHudMixin.richIsCustomHudActiveStatic()) {
            ci.cancel();
        }
    }

    @Inject(method={"method_72739"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderContextualBar(GuiGraphics context, CallbackInfo ci) {
        if (this.richIsCustomHudActive()) {
            ci.cancel();
        }
    }

    @Inject(method={"renderSelectedItemName"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderSelectedItemName(GuiGraphics context, CallbackInfo ci) {
        if (this.richIsCustomHudActive()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean richIsCustomHudActive() {
        Hud hud = Hud.getInstance();
        return hud != null && hud.isState() && hud.interfaceSettings.isSelected("CustomHud");
    }

    @Unique
    private static boolean richIsCustomHudActiveStatic() {
        Hud hud = Hud.getInstance();
        return hud != null && hud.isState() && hud.interfaceSettings.isSelected("CustomHud");
    }

    @Unique
    private boolean shouldRenderHud(Screen screen) {
        if (screen == null) {
            return true;
        }
        if (screen instanceof ClickGui) {
            return false;
        }
        if (screen instanceof ChatScreen) {
            return false;
        }
        return !this.isLoadingScreen(screen);
    }

    @Unique
    private boolean isLoadingScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        String className = screen.getClass().getSimpleName().toLowerCase();
        String fullName = screen.getClass().getName().toLowerCase();
        if (className.contains("loading")) {
            return true;
        }
        if (className.contains("progress")) {
            return true;
        }
        if (className.contains("connecting")) {
            return true;
        }
        if (className.contains("downloading")) {
            return true;
        }
        if (className.contains("terrain")) {
            return true;
        }
        if (className.contains("generating")) {
            return true;
        }
        if (className.contains("saving")) {
            return true;
        }
        if (className.contains("reload")) {
            return true;
        }
        if (className.contains("resource")) {
            return true;
        }
        if (className.contains("pack")) {
            return true;
        }
        return fullName.contains("mojang");
    }
}

