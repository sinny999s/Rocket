/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.mojang.authlib.GameProfile
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.CloseScreenEvent;
import rich.events.impl.MoveEvent;
import rich.events.impl.PlayerTravelEvent;
import rich.events.impl.PushEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.UsingItemEvent;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.util.move.MoveUtil;

@Mixin(value={LocalPlayer.class})
public abstract class ClientPlayerEntityMixin
extends AbstractClientPlayer {
    @Final
    @Shadow
    protected Minecraft minecraft;
    @Shadow
    public ClientInput input;
    private double prevX = 0.0;
    private double prevZ = 0.0;
    private float prevBodyYaw = 0.0f;

    @Shadow
    protected abstract void updateAutoJump(float var1, float var2);

    @Shadow
    public abstract boolean isUsingItem();

    public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method={"tick"}, at={@At(value="HEAD")})
    public void tick(CallbackInfo info) {
        if (this.minecraft.player != null && this.minecraft.level != null) {
            EventManager.callEvent(new TickEvent());
        }
    }

    @Inject(method={"aiStep"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/ClientInput;tick()V", shift=At.Shift.AFTER)})
    private void onInputTick(CallbackInfo ci) {
        if (IMinecraft.mc.player == null) {
            return;
        }
        PlayerTravelEvent event = new PlayerTravelEvent(Vec3.ZERO, false);
        EventManager.callEvent(event);
    }

    @Redirect(method={"modifyInput"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/phys/Vec2;scale(F)Lnet/minecraft/world/phys/Vec2;", ordinal=1))
    private Vec2 cancelItemSlowdown(Vec2 vec2f, float multiplier) {
        UsingItemEvent event = new UsingItemEvent((byte)1);
        EventManager.callEvent(event);
        if (event.isCancelled() && this.isUsingItem() && !this.isPassenger()) {
            return vec2f.scale(1.0f);
        }
        return vec2f.scale(multiplier);
    }

    @Inject(method={"closeContainer"}, at={@At(value="HEAD")}, cancellable=true)
    private void closeHandledScreenHook(CallbackInfo info) {
        CloseScreenEvent event = new CloseScreenEvent(this.minecraft.screen);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"moveTowardsClosestSpace"}, at={@At(value="HEAD")}, cancellable=true)
    public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
        PushEvent event = new PushEvent(PushEvent.Type.BLOCK);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method={"move"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/AbstractClientPlayer;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V")}, cancellable=true)
    public void onMoveHook(MoverType movementType, Vec3 movement, CallbackInfo ci) {
        MoveEvent event = new MoveEvent(movement);
        EventManager.callEvent(event);
        double d = this.getX();
        double e = this.getZ();
        super.move(movementType, event.getMovement());
        this.updateAutoJump((float)(this.getX() - d), (float)(this.getZ() - e));
        ci.cancel();
    }

    @ModifyExpressionValue(method={"sendPosition", "tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getYRot()F")})
    private float hookSilentRotationYaw(float original) {
        if (IMinecraft.mc.player != null && AngleConnection.INSTANCE.getRotation() != null) {
            float newBodyYaw;
            float currentYaw = AngleConnection.INSTANCE.getRotation().getYaw();
            this.prevBodyYaw = newBodyYaw = MoveUtil.calculateBodyYaw(currentYaw, this.prevBodyYaw, this.prevX, this.prevZ, IMinecraft.mc.player.getX(), IMinecraft.mc.player.getZ(), IMinecraft.mc.player.attackAnim);
            this.prevX = IMinecraft.mc.player.getX();
            this.prevZ = IMinecraft.mc.player.getZ();
            IMinecraft.mc.player.setYBodyRot(newBodyYaw);
            return currentYaw;
        }
        return original;
    }

    @ModifyExpressionValue(method={"sendPosition", "tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/player/LocalPlayer;getXRot()F")})
    private float hookSilentRotationPitch(float original) {
        if (AngleConnection.INSTANCE.getRotation() != null) {
            return AngleConnection.INSTANCE.getRotation().getPitch();
        }
        return original;
    }
}

