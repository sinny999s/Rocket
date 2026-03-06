/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.Redirect
 */
package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.EntityColorEvent;
import rich.modules.impl.combat.aura.AngleConnection;

@Mixin(value={LivingEntityRenderer.class})
public abstract class LivingEntityRendererMixin<S extends LivingEntityRenderState, M extends EntityModel<? super S>>
implements IMinecraft {
    @Shadow
    @Nullable
    protected abstract RenderType getRenderType(S var1, boolean var2, boolean var3, boolean var4);

    @ModifyExpressionValue(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/util/Mth;rotLerp(FFF)F")})
    private float lerpAngleDegreesHook(float original, @Local(ordinal=0, argsOnly=true) LivingEntity entity, @Local(ordinal=0, argsOnly=true) float delta) {
        AngleConnection controller = AngleConnection.INSTANCE;
        if (entity.equals(LivingEntityRendererMixin.mc.player) && controller.getCurrentAngle() != null && !(LivingEntityRendererMixin.mc.screen instanceof AbstractContainerScreen)) {
            float prevYaw = controller.getPreviousRotation().getYaw();
            float currentYaw = controller.getRotation().getYaw();
            return Mth.rotLerp((float)delta, (float)prevYaw, (float)currentYaw);
        }
        return original;
    }

    @ModifyExpressionValue(method={"extractRenderState"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/LivingEntity;getXRot(F)F")})
    private float getLerpedPitchHook(float original, @Local(ordinal=0, argsOnly=true) LivingEntity entity, @Local(ordinal=0, argsOnly=true) float delta) {
        AngleConnection controller = AngleConnection.INSTANCE;
        if (entity.equals(LivingEntityRendererMixin.mc.player) && controller.getCurrentAngle() != null && !(LivingEntityRendererMixin.mc.screen instanceof AbstractContainerScreen)) {
            float prevPitch = controller.getPreviousRotation().getPitch();
            float currentPitch = controller.getRotation().getPitch();
            return Mth.lerp((float)delta, (float)prevPitch, (float)currentPitch);
        }
        return original;
    }

    @Redirect(method={"submit"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;"))
    private RenderType renderLayerHook(LivingEntityRenderer<?, ?, ?> instance, LivingEntityRenderState state, boolean showBody, boolean translucent, boolean showOutline) {
        if (!translucent && state.boundingBoxWidth == 0.6f) {
            EntityColorEvent event = new EntityColorEvent(-1);
            EventManager.callEvent(event);
            if (event.isCancelled()) {
                translucent = true;
            }
        }
        return this.getRenderType((S)(Object)state, showBody, translucent, showOutline);
    }

    @ModifyArg(method={"submit"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"), index=6)
    private int modifyColor(int color, @Local(argsOnly=true) S renderState) {
        if (((S)(Object)renderState).isInvisibleToPlayer) {
            EntityColorEvent event = new EntityColorEvent(color);
            EventManager.callEvent(event);
            return event.getColor();
        }
        return color;
    }
}

