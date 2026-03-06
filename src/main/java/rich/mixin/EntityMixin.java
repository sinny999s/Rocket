/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyVariable
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package rich.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.BoundingBoxControlEvent;
import rich.events.impl.PlayerVelocityStrafeEvent;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.render.Esp;

@Mixin(value={Entity.class})
public abstract class EntityMixin
implements IMinecraft {
    @Shadow
    private AABB bb;
    @Shadow
    public float yRot;
    @Unique
    private boolean client$local;
    @Unique
    private final Minecraft client = Minecraft.getInstance();

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(EntityType<?> type, Level world, CallbackInfo ci) {
        this.client$local = (Entity)((Object)this) instanceof LocalPlayer;
    }

    @Inject(method={"getBoundingBox"}, at={@At(value="HEAD")}, cancellable=true)
    public final void getBoundingBox(CallbackInfoReturnable<AABB> cir) {
        BoundingBoxControlEvent event = new BoundingBoxControlEvent(this.bb, (Entity)((Object)this));
        EventManager.callEvent(event);
        cir.setReturnValue(event.getBox());
    }

    @Redirect(method={"moveRelative"}, at=@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;getInputVector(Lnet/minecraft/world/phys/Vec3;FF)Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 hookVelocity(Vec3 movementInput, float speed, float yaw) {
        if ((Object)this == EntityMixin.mc.player) {
            PlayerVelocityStrafeEvent event = new PlayerVelocityStrafeEvent(movementInput, speed, yaw, Entity.getInputVector((Vec3)movementInput, (float)speed, (float)yaw));
            EventManager.callEvent(event);
            return event.getVelocity();
        }
        return Entity.getInputVector((Vec3)movementInput, (float)speed, (float)yaw);
    }

    @ModifyVariable(method={"calculateViewVector"}, at=@At(value="HEAD"), ordinal=0, argsOnly=true)
    private float modifyPitch(float pitch) {
        if ((Object)this instanceof LocalPlayer && AngleConnection.INSTANCE.getCurrentAngle() != null) {
            return AngleConnection.INSTANCE.getCurrentAngle().getPitch();
        }
        return pitch;
    }

    @Inject(method={"getTeamColor"}, at={@At(value="HEAD")}, cancellable=true)
    private void onGetTeamColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity)(Object)this;
        Esp esp = Esp.getInstance();
        if (esp != null && esp.shouldGlow(self)) {
            java.awt.Color color = esp.getGlowColor(self);
            cir.setReturnValue(color.getRGB() & 0xFFFFFF);
        }
    }
}

