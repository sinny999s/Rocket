/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.Redirect
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package rich.mixin;

import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import rich.Initialization;
import rich.modules.impl.render.FullBright;
import rich.modules.impl.render.NoRender;

@Mixin(value={LightTexture.class})
public class LightmapTextureManagerMixin {
    @Redirect(method={"updateLightTexture"}, at=@At(value="INVOKE", target="Ljava/lang/Double;floatValue()F", ordinal=1))
    private float leet$getValue(Double instance) {
        if (Initialization.getInstance().getManager().getModuleProvider().get(FullBright.class).isState()) {
            return 200.0f;
        }
        return instance.floatValue();
    }

    @Inject(method={"calculateDarknessScale"}, at={@At(value="HEAD")}, cancellable=true)
    private void removeDarknessEffect(CallbackInfoReturnable<Float> cir) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Darkness")) {
            cir.setReturnValue(Float.valueOf(0.0f));
        }
    }
}

