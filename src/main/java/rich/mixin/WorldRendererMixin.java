/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.ModifyArg
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package rich.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.IMinecraft;
import rich.modules.impl.render.ChunkAnimator;
import rich.modules.impl.render.NoRender;

@Mixin(value={LevelRenderer.class}, priority = 1500)
public class WorldRendererMixin
implements IMinecraft {
    @ModifyArg(method={"prepareChunkRenders"}, at=@At(value="INVOKE", target="Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal=0), index=0, require=0)
    private Object modifyChunkSectionsValue(Object value) {
        if (value instanceof DynamicUniforms.ChunkSectionInfo) {
            DynamicUniforms.ChunkSectionInfo original = (DynamicUniforms.ChunkSectionInfo)value;
            ChunkAnimator chunkAnimator = ChunkAnimator.getInstance();
            if (chunkAnimator != null && chunkAnimator.isState()) {
                float visibility = original.visibility();
                float animOffset = (1.0f - visibility) * 100.0f;
                int newY = original.y() - (int)animOffset;
                return new DynamicUniforms.ChunkSectionInfo(original.modelView(), original.x(), newY, original.z(), original.visibility(), original.textureAtlasWidth(), original.textureAtlasHeight());
            }
        }
        return value;
    }

    @Inject(method={"doesMobEffectBlockSky"}, at={@At(value="HEAD")}, cancellable=true)
    private void onHasBlindnessOrDarkness(Camera camera, CallbackInfoReturnable<Boolean> cir) {
        NoRender noRender = NoRender.getInstance();
        if (noRender == null || !noRender.isState()) {
            return;
        }
        Entity entity = camera.entity();
        if (!(entity instanceof LivingEntity)) {
            return;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        boolean hasBlindness = livingEntity.hasEffect(MobEffects.BLINDNESS);
        boolean hasDarkness = livingEntity.hasEffect(MobEffects.DARKNESS);
        if (noRender.modeSetting.isSelected("Bad Effects") && hasBlindness && !hasDarkness) {
            cir.setReturnValue(false);
        }
        if (noRender.modeSetting.isSelected("Darkness") && hasDarkness && !hasBlindness) {
            cir.setReturnValue(false);
        }
        if (noRender.modeSetting.isSelected("Bad Effects") && noRender.modeSetting.isSelected("Darkness")) {
            cir.setReturnValue(false);
        }
    }
}

