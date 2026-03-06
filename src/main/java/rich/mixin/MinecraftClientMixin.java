/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package rich.mixin;

import antidaunleak.api.UserProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import rich.modules.impl.render.Esp;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.Initialization;
import rich.events.api.EventManager;
import rich.events.impl.GameLeftEvent;
import rich.events.impl.HotBarUpdateEvent;
import rich.events.impl.SetScreenEvent;
import rich.modules.impl.combat.NoInteract;
import rich.modules.impl.render.Hud;
import rich.screens.clickgui.ClickGui;
import rich.modules.impl.misc.BetterChat;
import rich.screens.chat.RocketChatScreen;
import rich.screens.menu.MainMenuScreen;
import rich.util.config.ConfigSystem;
import rich.util.render.font.FontRenderer;
import rich.util.session.SessionChanger;
import rich.util.window.WindowStyle;

@Mixin(value={Minecraft.class})
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public LocalPlayer player;
    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;
    @Shadow
    @Final
    public GameRenderer gameRenderer;
    @Shadow
    public ClientLevel level;
    private static boolean fontsInitialized = false;
    @Shadow
    @Mutable
    private User user;

    private void setSession(User newSession) {
        this.user = newSession;
    }

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(CallbackInfo ci) {
        new Initialization().init();
        SessionChanger.setSessionSetter(this::setSession);
    }

    @Inject(method={"destroy"}, at={@At(value="HEAD")})
    private void onStop(CallbackInfo ci) {
        ConfigSystem configSystem = ConfigSystem.getInstance();
        if (configSystem != null) {
            configSystem.shutdown();
        }
    }

    @Inject(method={"setScreen"}, at={@At(value="HEAD")})
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (!fontsInitialized && screen != null) {
            try {
                FontRenderer fontRenderer = Initialization.getInstance().getManager().getRenderCore().getFontRenderer();
                if (fontRenderer != null && !fontRenderer.isInitialized()) {
                    fontRenderer.initialize();
                    fontsInitialized = true;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Inject(method={"setScreen"}, at={@At(value="HEAD")}, cancellable=true)
    private void redirectTitleScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof TitleScreen && !(screen instanceof MainMenuScreen)) {
            ci.cancel();
            ((Minecraft)((Object)this)).setScreen(new MainMenuScreen());
        }
        if (screen instanceof net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen) {
            ci.cancel();
            ((Minecraft)((Object)this)).setScreen(new MainMenuScreen());
        }
        if (screen instanceof net.minecraft.client.gui.screens.worldselection.SelectWorldScreen) {
            ci.cancel();
            ((Minecraft)((Object)this)).setScreen(new MainMenuScreen());
        }
        if (screen instanceof ChatScreen && !(screen instanceof RocketChatScreen)) {
            BetterChat betterChat = BetterChat.getInstance();
            if (betterChat != null && betterChat.isState()) {
                ci.cancel();
                ((Minecraft)((Object)this)).setScreen(new RocketChatScreen(""));
            }
        }
    }

    @Inject(method={"disconnectFromWorld"}, at={@At(value="HEAD")})
    private void onDisconnect(net.minecraft.network.chat.Component reason, CallbackInfo info) {
        if (this.level != null) {
            EventManager.callEvent(GameLeftEvent.get());
        }
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    private void onTick(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        Hud hud = Hud.getInstance();
        if (hud != null && hud.isState() && Initialization.getInstance() != null && Initialization.getInstance().getManager() != null && Initialization.getInstance().getManager().getHudManager() != null) {
            Initialization.getInstance().getManager().getHudManager().tick();
        }
    }

    @Inject(method={"setScreen"}, at={@At(value="HEAD")}, cancellable=true)
    public void setScreenHook(Screen screen, CallbackInfo ci) {
        ClickGui clickGui;
        Minecraft client = (Minecraft)((Object)this);
        Screen screen2 = client.screen;
        if (screen2 instanceof ClickGui && (clickGui = (ClickGui)screen2).isClosing() && screen == null) {
            ci.cancel();
            return;
        }
        SetScreenEvent event = new SetScreenEvent(screen);
        EventManager.callEvent(event);
        Initialization instance = Initialization.getInstance();
        Screen eventScreen = event.getScreen();
        if (screen != eventScreen) {
            IMinecraft.mc.setScreen(eventScreen);
            ci.cancel();
        }
    }

    @Inject(method={"createTitle"}, at={@At(value="RETURN")}, cancellable=true)
    private void getWindowTitle(CallbackInfoReturnable<String> cir) {
        UserProfile userProfile = UserProfile.getInstance();
        String username = userProfile.profile("username");
        String role = userProfile.profile("role");
        cir.setReturnValue(String.format("Rocket (%s - %s)", role, username));
    }

    @Inject(method={"handleKeybinds"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;")}, cancellable=true)
    public void handleInputEventsHook(CallbackInfo ci) {
        HotBarUpdateEvent event = new HotBarUpdateEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"startUseItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;")}, cancellable=true)
    public void doItemUseHook(CallbackInfo ci) {
        if (NoInteract.getInstance().isState()) {
            for (InteractionHand hand : InteractionHand.values()) {
                InteractionResult.Success success;
                InteractionResult result;
                if (this.player.getItemInHand(hand).isEmpty() || !(result = this.gameMode.useItem(this.player, hand)).consumesAction()) continue;
                if (result instanceof InteractionResult.Success && (success = (InteractionResult.Success)result).swingSource().equals((Object)InteractionResult.SwingSource.CLIENT)) {
                    this.gameRenderer.itemInHandRenderer.itemUsed(hand);
                    this.player.swing(hand);
                }
                ci.cancel();
            }
        }
    }

    @Inject(method={"resizeDisplay"}, at={@At(value="TAIL")})
    private void applyDarkMode(CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        WindowStyle.setDarkMode(client.getWindow().handle());
    }

    @Inject(method={"shouldEntityAppearGlowing"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShouldEntityAppearGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        Esp esp = Esp.getInstance();
        if (esp != null && esp.shouldGlow(entity)) {
            cir.setReturnValue(true);
        }
    }
}

