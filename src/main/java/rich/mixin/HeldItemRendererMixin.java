/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.wrapoperation.Operation
 *  com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation
 *  com.llamalad7.mixinextras.sugar.Local
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.GlassHandsRenderEvent;
import rich.events.impl.HandAnimationEvent;
import rich.events.impl.HandOffsetEvent;
import rich.events.impl.HeldItemUpdateEvent;
import rich.events.impl.ItemRendererEvent;
import rich.modules.impl.render.GlassHands;

@Mixin(value={ItemInHandRenderer.class})
public abstract class HeldItemRendererMixin {
    @Shadow
    private ItemStack mainHandItem;
    @Shadow
    private ItemStack offHandItem;
    @Unique
    private boolean richCustomAnimation = false;

    @Inject(method={"tick"}, at={@At(value="TAIL")})
    private void onUpdateHeldItems(CallbackInfo ci) {
        HeldItemUpdateEvent event = new HeldItemUpdateEvent(this.mainHandItem, this.offHandItem);
        EventManager.callEvent(event);
        if (event.getMainHand() != this.mainHandItem) {
            this.mainHandItem = event.getMainHand();
        }
        if (event.getOffHand() != this.offHandItem) {
            this.offHandItem = event.getOffHand();
        }
    }

    @Inject(method={"renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"}, at={@At(value="HEAD")})
    private void onRenderItemPre(float tickProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, LocalPlayer player, int light, CallbackInfo ci) {
        GlassHands glassHands = GlassHands.getInstance();
        if (glassHands != null && glassHands.isState()) {
            GlassHandsRenderEvent event = new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.PRE, matrices, tickProgress);
            EventManager.callEvent(event);
        }
    }

    @Inject(method={"renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"}, at={@At(value="TAIL")})
    private void onRenderItemPost(float tickProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, LocalPlayer player, int light, CallbackInfo ci) {
        GlassHands glassHands = GlassHands.getInstance();
        if (glassHands != null && glassHands.isState()) {
            GlassHandsRenderEvent event = new GlassHandsRenderEvent(GlassHandsRenderEvent.Phase.POST, matrices, tickProgress);
            EventManager.callEvent(event);
        }
    }

    @WrapOperation(method={"renderHandsWithItems(FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/player/LocalPlayer;I)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V")})
    private void itemRenderHook(ItemInHandRenderer instance, AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack item, float equipProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int light, Operation<Void> original) {
        ItemRendererEvent event = new ItemRendererEvent(player, item, hand);
        EventManager.callEvent(event);
        original.call(new Object[]{instance, event.getPlayer(), Float.valueOf(tickDelta), Float.valueOf(pitch), event.getHand(), Float.valueOf(swingProgress), event.getStack(), Float.valueOf(equipProgress), matrices, orderedRenderCommandQueue, light});
    }

    @Inject(method={"renderArmWithItem"}, at={@At(value="INVOKE", target="Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift=At.Shift.AFTER)})
    private void renderFirstPersonItemHook(AbstractClientPlayer player, float tickDelta, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, int light, CallbackInfo ci) {
        HandOffsetEvent event = new HandOffsetEvent(matrices, stack, hand);
        EventManager.callEvent(event);
        float scale = event.getScale();
        if (scale != 1.0f) {
            matrices.scale(scale, scale, scale);
        }
    }

    @WrapOperation(method={"renderArmWithItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/ItemInHandRenderer;applyItemArmTransform(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/entity/HumanoidArm;F)V")})
    private void wrapApplyEquipOffset(ItemInHandRenderer instance, PoseStack matrices, HumanoidArm arm, float equipProgress, Operation<Void> original, @Local(ordinal=0, argsOnly=true) AbstractClientPlayer player, @Local(ordinal=0, argsOnly=true) InteractionHand hand, @Local(ordinal=2, argsOnly=true) float swingProgress, @Local(ordinal=0, argsOnly=true) ItemStack stack) {
        boolean isUsingItem;
        boolean bl = isUsingItem = player.isUsingItem() && player.getUsedItemHand() == hand;
        if (isUsingItem) {
            this.richCustomAnimation = false;
            original.call(new Object[]{instance, matrices, arm, Float.valueOf(equipProgress)});
            return;
        }
        HandAnimationEvent event = new HandAnimationEvent(matrices, hand, swingProgress);
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            this.richCustomAnimation = true;
            return;
        }
        this.richCustomAnimation = false;
        original.call(new Object[]{instance, matrices, arm, Float.valueOf(equipProgress)});
    }

    @WrapOperation(method={"renderArmWithItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/ItemInHandRenderer;swingArm(FLcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/entity/HumanoidArm;)V")})
    private void wrapSwingArm(ItemInHandRenderer instance, float swingProgress, PoseStack matrices, int armX, HumanoidArm arm, Operation<Void> original) {
        if (this.richCustomAnimation) {
            return;
        }
        original.call(new Object[]{instance, Float.valueOf(swingProgress), matrices, armX, arm});
    }
}

