/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package rich.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.Initialization;
import rich.client.draggables.Drag;

@Mixin(value={ChatScreen.class})
public abstract class ChatScreenMixin
extends Screen {
    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        Drag.onDraw(context, mouseX, mouseY, deltaTicks, true);
    }

    @Inject(method={"mouseClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void onMouseClicked(MouseButtonEvent click, boolean doubled, CallbackInfoReturnable<Boolean> cir) {
        int mouseX = (int)click.x();
        int mouseY = (int)click.y();
        int button = click.button();
        if (Initialization.getInstance() != null && Initialization.getInstance().getManager() != null && Initialization.getInstance().getManager().getHudManager() != null && Initialization.getInstance().getManager().getHudManager().mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            return;
        }
        Drag.onMouseClick(click);
        if (Drag.isDragging()) {
            cir.setReturnValue(true);
        }
    }

    public boolean mouseReleased(MouseButtonEvent click) {
        Drag.onMouseRelease(click);
        return super.mouseReleased(click);
    }

    public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
        return super.mouseDragged(click, deltaX, deltaY);
    }

    public void removed() {
        Drag.resetDragging();
        super.removed();
    }

    public void onClose() {
        Drag.resetDragging();
        super.onClose();
    }
}

