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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.mixin.IScreen;
import rich.util.config.impl.proxy.ProxyConfig;
import rich.util.proxy.GuiProxy;
import rich.util.proxy.ProxyServer;

@Mixin(value={JoinMultiplayerScreen.class})
public class MultiplayerScreenOpenMixin {
    @Inject(method={"init"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/multiplayer/JoinMultiplayerScreen;onSelectedChange()V")})
    public void multiplayerGuiOpen(CallbackInfo ci) {
        JoinMultiplayerScreen ms = (JoinMultiplayerScreen)((Object)this);
        Minecraft client = Minecraft.getInstance();
        ProxyConfig config = ProxyConfig.getInstance();
        String buttonText = config.isProxyEnabled() && !config.getDefaultProxy().isEmpty() ? "\u00a7aProxy: Active" : "\u00a77Proxy";
        ProxyServer.proxyMenuButton = Button.builder((Component)Component.literal((String)buttonText), buttonWidget -> Minecraft.getInstance().setScreen(new GuiProxy(ms))).bounds(5, 5, 100, 20).build();
        IScreen si = (IScreen)((Object)ms);
        si.getDrawables().add(ProxyServer.proxyMenuButton);
        si.getSelectables().add(ProxyServer.proxyMenuButton);
        si.getChildren().add(ProxyServer.proxyMenuButton);
    }
}

