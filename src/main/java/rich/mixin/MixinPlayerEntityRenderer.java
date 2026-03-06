/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.render.Esp;
import rich.modules.impl.render.chinahat.ChinaHatFeatureRenderer;

@Mixin(value={AvatarRenderer.class})
public class MixinPlayerEntityRenderer {
    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void onInit(EntityRendererProvider.Context ctx, boolean slim, CallbackInfo ci) {
        AvatarRenderer renderer = (AvatarRenderer)((Object)this);
        renderer.addLayer(new ChinaHatFeatureRenderer(renderer));
    }

    @Inject(method={"submitNameTag"}, at={@At(value="HEAD")}, cancellable=true)
    private void hideBelownameScore(AvatarRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (Esp.getInstance().isState()) {
            ci.cancel();
        }
    }

    @Inject(method={"extractRenderState"}, at={@At(value="TAIL")})
    private void onUpdateRenderState(Avatar player, AvatarRenderState state, float tickDelta, CallbackInfo ci) {
        if (Esp.getInstance().isState()) {
            state.scoreText = null;
            state.nameTag = null;
        }
    }
}

