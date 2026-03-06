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

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.modules.impl.render.Esp;

@Mixin(value={EntityRenderer.class})
public class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    @Inject(method={"submitNameTag"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRenderLabelIfPresent(S state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        Esp esp = Esp.getInstance();
        if (esp != null && esp.isState()) {
            if (((EntityRenderState)state).entityType == EntityType.PLAYER && esp.entityType.isSelected("Player")) {
                ci.cancel();
            }
            if (((EntityRenderState)state).entityType == EntityType.ITEM && esp.entityType.isSelected("Item")) {
                ci.cancel();
            }
        }
    }

    @Inject(method={"getNameTag"}, at={@At(value="HEAD")}, cancellable=true)
    private void hookNametag(T entity, CallbackInfoReturnable<Component> cir) {
        Esp esp = Esp.getInstance();
        if (esp != null && esp.isState()) {
            if (entity instanceof Player && esp.entityType.isSelected("Player")) {
                cir.setReturnValue(null);
            }
            if (entity instanceof ItemEntity && esp.entityType.isSelected("Item")) {
                cir.setReturnValue(null);
            }
        }
    }
}

