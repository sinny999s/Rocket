/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.KeyEvent;
import rich.screens.clickgui.ClickGui;
import rich.util.config.impl.bind.BindConfig;

@Mixin(value={KeyboardHandler.class})
public class KeyboardMixin {
    @Final
    @Shadow
    private Minecraft minecraft;

    @Inject(method={"keyPress"}, at={@At(value="HEAD")})
    private void onKey(long window, int action, net.minecraft.client.input.KeyEvent input, CallbackInfo ci) {
        if (input.key() != -1 && window == this.minecraft.getWindow().handle()) {
            if (action == 0 && input.key() == BindConfig.getInstance().getBindKey() && this.canOpenClickGui()) {
                ClickGui.INSTANCE.openGui();
            }
            EventManager.callEvent(new KeyEvent(this.minecraft.screen, InputConstants.Type.KEYSYM, input.key(), action));
        }
    }

    private boolean canOpenClickGui() {
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return false;
        }
        return this.minecraft.screen == null;
    }
}

