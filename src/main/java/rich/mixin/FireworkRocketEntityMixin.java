/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 */
package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.FireworkEvent;
import rich.modules.impl.combat.aura.AngleConnection;

@Mixin(value={FireworkRocketEntity.class})
public class FireworkRocketEntityMixin
implements IMinecraft {
    @Shadow
    @Nullable
    private LivingEntity attachedToEntity;

    @WrapOperation(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getLookAngle()Lnet/minecraft/world/phys/Vec3;")})
    public Vec3 getRotationVectorHook(LivingEntity instance, Operation<Vec3> original) {
        if (this.attachedToEntity == FireworkRocketEntityMixin.mc.player && this.attachedToEntity.isFallFlying()) {
            return AngleConnection.INSTANCE.getMoveRotation().toVector();
        }
        return (Vec3)original.call(new Object[]{instance});
    }

    @WrapOperation(method={"tick"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V")})
    public void setVelocityHook(LivingEntity instance, Vec3 velocity, Operation<Void> original) {
        if (this.attachedToEntity == FireworkRocketEntityMixin.mc.player && this.attachedToEntity.isFallFlying()) {
            FireworkEvent event = new FireworkEvent(velocity);
            EventManager.callEvent(event);
            original.call(new Object[]{instance, event.getVector()});
        } else {
            original.call(new Object[]{instance, velocity});
        }
    }
}

