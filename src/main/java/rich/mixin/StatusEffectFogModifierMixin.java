/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package rich.mixin;

import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.modules.impl.render.NoRender;

@Mixin(value={MobEffectFogEnvironment.class})
public abstract class StatusEffectFogModifierMixin {
    @Shadow
    public abstract Holder<MobEffect> getMobEffect();

    @Inject(method={"isApplicable"}, at={@At(value="HEAD")}, cancellable=true)
    private void onShouldApply(@Nullable FogType submersionType, Entity cameraEntity, CallbackInfoReturnable<Boolean> cir) {
        NoRender noRender = NoRender.getInstance();
        if (!noRender.isState()) {
            return;
        }
        Holder<MobEffect> effect = this.getMobEffect();
        if (noRender.modeSetting.isSelected("Bad Effects") && effect == MobEffects.BLINDNESS) {
            cir.setReturnValue(false);
        }
        if (noRender.modeSetting.isSelected("Darkness") && effect == MobEffects.DARKNESS) {
            cir.setReturnValue(false);
        }
    }
}

