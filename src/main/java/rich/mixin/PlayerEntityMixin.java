/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.PlayerTravelEvent;
import rich.events.impl.PushEvent;
import rich.events.impl.SwimmingEvent;
import rich.modules.impl.combat.aura.AngleConnection;

@Mixin(value={Player.class})
public abstract class PlayerEntityMixin
implements IMinecraft {
    @Inject(method={"isPushedByFluid"}, at={@At(value="HEAD")}, cancellable=true)
    public void isPushedByFluids(CallbackInfoReturnable<Boolean> cir) {
        PushEvent event = new PushEvent(PushEvent.Type.WATER);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            cir.setReturnValue(false);
        }
    }

    @ModifyExpressionValue(method={"causeExtraKnockback"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getYRot()F")})
    private float hookKnockbackRotation(float original) {
        if ((Object)this == PlayerEntityMixin.mc.player && AngleConnection.INSTANCE.getMoveRotation() != null) {
            return AngleConnection.INSTANCE.getMoveRotation().getYaw();
        }
        return original;
    }

    @ModifyExpressionValue(method={"doSweepAttack"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getYRot()F")})
    private float hookSweepRotation(float original) {
        if ((Object)this == PlayerEntityMixin.mc.player && AngleConnection.INSTANCE.getMoveRotation() != null) {
            return AngleConnection.INSTANCE.getMoveRotation().getYaw();
        }
        return original;
    }

    @Inject(method={"travel"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTravelPre(Vec3 movementInput, CallbackInfo ci) {
        if (PlayerEntityMixin.mc.player == null) {
            return;
        }
        PlayerTravelEvent event = new PlayerTravelEvent(movementInput, true);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method={"travel"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/player/Player;getLookAngle()Lnet/minecraft/world/phys/Vec3;")})
    public Vec3 travelHook(Vec3 vec3d) {
        SwimmingEvent event = new SwimmingEvent(vec3d);
        EventManager.callEvent(event);
        return event.getVector();
    }

    @Inject(method={"travel"}, at={@At(value="RETURN")})
    private void onTravelPost(Vec3 movementInput, CallbackInfo ci) {
        if (PlayerEntityMixin.mc.player == null) {
            return;
        }
        PlayerTravelEvent event = new PlayerTravelEvent(movementInput, false);
        EventManager.callEvent(event);
    }
}

