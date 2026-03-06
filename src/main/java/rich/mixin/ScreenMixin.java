/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.ClickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.command.CommandManager;
import rich.screens.clickgui.ClickGui;

@Mixin(value={Screen.class})
public class ScreenMixin {
    @Inject(method={"renderBackground"}, at={@At(value="HEAD")}, cancellable=true)
    private void disableBackgroundBlurAndDimming(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object)this instanceof ClickGui) {
            ci.cancel();
        }
    }

    @Inject(method={"defaultHandleGameClickEvent"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onHandleClickEvent(ClickEvent clickEvent, Minecraft client, Screen screenAfterRun, CallbackInfo ci) {
        if (clickEvent instanceof ClickEvent.RunCommand) {
            ClickEvent.RunCommand runCommand = (ClickEvent.RunCommand)clickEvent;
            String command = runCommand.command();
            CommandManager manager = CommandManager.getInstance();
            if (manager != null && command != null && command.startsWith(manager.getPrefix())) {
                manager.execute(command.substring(manager.getPrefix().length()));
                ci.cancel();
            }
        }
    }
}

