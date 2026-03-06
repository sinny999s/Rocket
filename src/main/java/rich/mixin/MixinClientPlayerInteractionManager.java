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

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.AttackEvent;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.InteractEntityEvent;

@Mixin(value={MultiPlayerGameMode.class})
public class MixinClientPlayerInteractionManager {
    @Inject(method={"attack"}, at={@At(value="HEAD")}, cancellable=true)
    public void attackEntityHook(Player player, Entity target, CallbackInfo info) {
        InteractEntityEvent event = new InteractEntityEvent(target);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"attack"}, at={@At(value="HEAD")})
    private void onAttackEntity(Player player, Entity target, CallbackInfo ci) {
        AttackEvent event = new AttackEvent(target);
        EventManager.callEvent(event);
    }

    @Inject(method={"handleInventoryMouseClick"}, at={@At(value="HEAD")}, cancellable=true)
    public void clickSlotHook(int syncId, int slotId, int button, ClickType actionType, Player player, CallbackInfo info) {
        ClickSlotEvent event = new ClickSlotEvent(syncId, slotId, button, actionType);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }
}

