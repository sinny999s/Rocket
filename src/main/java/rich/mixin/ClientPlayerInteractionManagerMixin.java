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

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.events.api.EventManager;
import rich.events.impl.BlockBreakingEvent;
import rich.events.impl.ClickSlotEvent;
import rich.events.impl.StartBlockBreakEvent;
import rich.events.impl.UsingItemEvent;

@Mixin(value={MultiPlayerGameMode.class})
public class ClientPlayerInteractionManagerMixin {
    @Inject(method={"useItem"}, at={@At(value="RETURN")})
    public void interactItemHook(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult.Success success;
        Object object = cir.getReturnValue();
        if (object instanceof InteractionResult.Success && !(success = (InteractionResult.Success)object).swingSource().equals((Object)InteractionResult.SwingSource.CLIENT)) {
            UsingItemEvent event = new UsingItemEvent((byte)0);
            EventManager.callEvent(event);
        }
    }

    @Inject(method={"releaseUsingItem"}, at={@At(value="HEAD")}, cancellable=true)
    public void stopUsingItemHook(CallbackInfo ci) {
        UsingItemEvent event = new UsingItemEvent((byte)2);
        EventManager.callEvent(event);
    }

    @Inject(method={"useItem"}, at={@At(value="HEAD")}, cancellable=true)
    private void gameModeHook(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        UsingItemEvent event = new UsingItemEvent((byte)-1);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }

    @Inject(method={"startDestroyBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void injectStartBlockBreak(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        StartBlockBreakEvent event = new StartBlockBreakEvent(pos, direction);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method={"continueDestroyBlock"}, at={@At(value="HEAD")})
    private void injectBlockBreaking(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        EventManager.callEvent(new BlockBreakingEvent(pos, direction));
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

