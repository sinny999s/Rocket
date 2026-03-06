/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.ChatEvent;
import rich.events.impl.GameLeftEvent;
import rich.events.impl.WorldChangeEvent;
import rich.modules.impl.render.Particles;

@Mixin(value={ClientPacketListener.class})
public abstract class ClientPlayNetworkHandlerMixin
implements IMinecraft {
    @Shadow
    private ClientLevel level;
    @Unique
    private boolean worldNotNull;

    @Shadow
    private static ItemStack findTotem(Player player) {
        return null;
    }

    @Inject(method={"sendChat"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendChatMessage(String message, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(message);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"handleLogin"}, at={@At(value="HEAD")})
    private void onGameJoinHead(ClientboundLoginPacket packet, CallbackInfo info) {
        this.worldNotNull = this.level != null;
    }

    @Inject(method={"handleLogin"}, at={@At(value="TAIL")})
    private void onGameJoinTail(ClientboundLoginPacket packet, CallbackInfo info) {
        if (this.worldNotNull) {
            EventManager.callEvent(GameLeftEvent.get());
        }
    }

    @Inject(method={"handleLogin"}, at={@At(value="RETURN")})
    private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
        EventManager.callEvent(WorldChangeEvent.get());
    }

    @Inject(method={"handleRespawn"}, at={@At(value="RETURN")})
    private void onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
        EventManager.callEvent(WorldChangeEvent.get());
    }

    @Inject(method={"handleEntityEvent"}, at={@At(value="HEAD")}, cancellable=true)
    private void onEntityStatus(ClientboundEntityEventPacket packet, CallbackInfo ci) {
        Particles particlesMod;
        Entity entity;
        if (packet.getEventId() == 35 && (entity = packet.getEntity(ClientPlayNetworkHandlerMixin.mc.level)) != null && (particlesMod = Particles.getInstance()) != null && particlesMod.isState() && particlesMod.triggers.isSelected("Totem")) {
            particlesMod.onTotemPop(entity);
            ClientPlayNetworkHandlerMixin.mc.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TOTEM_USE, entity.getSoundSource(), 1.0f, 1.0f, false);
            if (entity == ClientPlayNetworkHandlerMixin.mc.player) {
                ClientPlayNetworkHandlerMixin.mc.gameRenderer.displayItemActivation(ClientPlayNetworkHandlerMixin.findTotem(ClientPlayNetworkHandlerMixin.mc.player));
            }
            ci.cancel();
        }
    }
}

