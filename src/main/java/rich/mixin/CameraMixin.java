/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.CameraEvent;
import rich.events.impl.CameraPositionEvent;
import rich.modules.impl.combat.aura.Angle;

@Mixin(value={Camera.class})
public abstract class CameraMixin {
    @Shadow
    private Vec3 position;
    @Shadow
    @Final
    private BlockPos.MutableBlockPos blockPosition;
    @Shadow
    private float yRot;
    @Shadow
    private float xRot;

    @Shadow
    public abstract void setRotation(float var1, float var2);

    @Shadow
    protected abstract void move(float var1, float var2, float var3);

    @Shadow
    protected abstract float getMaxZoom(float var1);

    @Inject(method={"setup"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/Camera;setPosition(DDD)V", shift=At.Shift.AFTER)}, cancellable=true)
    private void updateHook(Level area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickProgress, CallbackInfo ci) {
        LocalPlayer player;
        CameraEvent event = new CameraEvent(false, 4.0f, new Angle(this.yRot, this.xRot));
        EventManager.callEvent(event);
        Angle angle = event.getAngle();
        if (event.isCancelled() && focusedEntity instanceof LocalPlayer && !(player = (LocalPlayer)focusedEntity).isSleeping() && thirdPerson) {
            float pitch = inverseView ? -angle.getPitch() : angle.getPitch();
            float yaw = angle.getYaw() - (float)(inverseView ? 180 : 0);
            float distance = event.getDistance();
            this.setRotation(yaw, pitch);
            this.move(event.isCameraClip() ? -distance : -this.getMaxZoom(distance), 0.0f, 0.0f);
            ci.cancel();
        }
    }

    @Inject(method={"setPosition(DDD)V"}, at={@At(value="HEAD")}, cancellable=true)
    private void posHook(double x, double y, double z, CallbackInfo ci) {
        Vec3 pos = new Vec3(x, y, z);
        CameraPositionEvent event = new CameraPositionEvent(pos);
        EventManager.callEvent(event);
        pos = event.getPos();
        this.position = pos;
        this.blockPosition.set(pos.x, pos.y, pos.z);
        ci.cancel();
    }
}

