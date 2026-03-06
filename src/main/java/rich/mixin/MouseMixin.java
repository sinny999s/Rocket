/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.v2.WrapWithCondition
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.FovEvent;
import rich.events.impl.HotBarScrollEvent;
import rich.events.impl.KeyEvent;
import rich.events.impl.MouseRotationEvent;
import rich.screens.clickgui.ClickGui;

@Mixin(value={MouseHandler.class})
public abstract class MouseMixin {
    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    private boolean mouseGrabbed;
    @Shadow
    private double xpos;
    @Shadow
    private double ypos;
    @Shadow
    private double accumulatedDX;
    @Shadow
    private double accumulatedDY;
    @Shadow
    private boolean ignoreFirstMove;

    @Inject(method={"onButton"}, at={@At(value="HEAD")})
    public void onMouseButtonHook(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        if (input.button() != -1 && window == this.minecraft.getWindow().handle()) {
            EventManager.callEvent(new KeyEvent(this.minecraft.screen, InputConstants.Type.MOUSE, input.button(), action));
        }
    }

    @Inject(method={"onScroll"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;")}, cancellable=true)
    public void onMouseScrollHook(long window, double horizontal, double vertical, CallbackInfo ci) {
        HotBarScrollEvent event = new HotBarScrollEvent(horizontal, vertical);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"grabMouse"}, at={@At(value="HEAD")}, cancellable=true)
    private void onLockCursor(CallbackInfo ci) {
        ClickGui clickGui;
        Screen screen = this.minecraft.screen;
        if (screen instanceof ClickGui && (clickGui = (ClickGui)screen).isClosing()) {
            this.mouseGrabbed = true;
            this.accumulatedDX = 0.0;
            this.accumulatedDY = 0.0;
            this.xpos = (double)this.minecraft.getWindow().getScreenWidth() / 2.0;
            this.ypos = (double)this.minecraft.getWindow().getScreenHeight() / 2.0;
            this.ignoreFirstMove = true;
            ci.cancel();
        }
    }

    @Inject(method={"turnPlayer"}, at={@At(value="HEAD")})
    private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
        FovEvent event = new FovEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            double slowdown = (double)event.getFov() / (double)((Integer)this.minecraft.options.fov().get()).intValue();
            this.accumulatedDX *= slowdown;
            this.accumulatedDY *= slowdown;
        }
    }

    @WrapWithCondition(method={"turnPlayer"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")}, require=1, allow=1)
    private boolean modifyMouseRotationInput(LocalPlayer instance, double cursorDeltaX, double cursorDeltaY) {
        MouseRotationEvent event = new MouseRotationEvent((float)cursorDeltaX, (float)cursorDeltaY);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        instance.turn(event.getCursorDeltaX(), event.getCursorDeltaY());
        return false;
    }

    @Inject(method={"handleAccumulatedMovement"}, at={@At(value="HEAD")})
    private void onTick(CallbackInfo ci) {
        ClickGui clickGui;
        Screen screen = this.minecraft.screen;
        if (screen instanceof ClickGui && (clickGui = (ClickGui)screen).isClosing() && !this.mouseGrabbed) {
            this.mouseGrabbed = true;
            this.accumulatedDX = 0.0;
            this.accumulatedDY = 0.0;
            this.ignoreFirstMove = true;
        }
    }
}

