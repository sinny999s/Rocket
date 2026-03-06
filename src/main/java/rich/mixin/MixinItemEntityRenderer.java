/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.WeakHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.entity.state.ItemEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.ItemPhysic;

@Mixin(value={ItemEntityRenderer.class})
public abstract class MixinItemEntityRenderer {
    @Unique
    private static final WeakHashMap<ItemEntityRenderState, Boolean> groundStateMap = new WeakHashMap();
    @Unique
    private ItemEntityRenderState currentState = null;

    @Inject(method={"extractRenderState"}, at={@At(value="HEAD")})
    private void captureGroundState(ItemEntity entity, ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
        groundStateMap.put(state, entity.onGround());
    }

    @Redirect(method={"submit"}, at=@At(value="INVOKE", target="Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V", ordinal=0))
    private void redirectTranslate(PoseStack matrices, float x, float y, float z, ItemEntityRenderState state, PoseStack matricesArg, SubmitNodeCollector queue, CameraRenderState cameraState) {
        this.currentState = state;
        ItemPhysic itemPhysic = ItemPhysic.getInstance();
        if (itemPhysic != null && itemPhysic.isState() && itemPhysic.mode.isSelected("Normal")) {
            AABB box = state.item.getModelBoundingBox();
            float f = -((float)box.minY) + 0.0625f;
            matrices.translate(x, f, z);
        } else {
            matrices.translate(x, y, z);
        }
    }

    @Redirect(method={"submit"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/renderer/entity/ItemEntityRenderer;submitMultipleFromCount(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/ItemClusterRenderState;Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/phys/AABB;)V"))
    private void redirectRender(PoseStack matrices, SubmitNodeCollector queue, int light, ItemClusterRenderState stackState, RandomSource random, AABB box) {
        ItemPhysic itemPhysic = ItemPhysic.getInstance();
        if (itemPhysic != null && itemPhysic.isState() && itemPhysic.mode.isSelected("Normal") && this.currentState != null) {
            float age = this.currentState.ageInTicks;
            float offset = this.currentState.bobOffset;
            boolean isOnGround = groundStateMap.getOrDefault(this.currentState, false);
            float rotation = ItemEntity.getSpin((float)age, (float)offset);
            matrices.mulPose((Quaternionfc)Axis.YP.rotation(-rotation));
            if (isOnGround) {
                matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
                float yOffset = (float)box.getYsize() / 2.0f;
                matrices.translate(0.0f, -yOffset + 0.0625f, 0.0f);
            } else {
                float spinSpeed = 15.0f;
                float itemRotation = (age * spinSpeed + offset * 360.0f) % 360.0f;
                matrices.mulPose((Quaternionfc)Axis.XP.rotationDegrees(itemRotation));
            }
        }
        ItemEntityRenderer.submitMultipleFromCount((PoseStack)matrices, (SubmitNodeCollector)queue, (int)light, (ItemClusterRenderState)stackState, (RandomSource)random, (AABB)box);
    }
}

