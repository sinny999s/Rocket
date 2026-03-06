/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.screens.loading.Loading;

@Mixin(value={LoadingOverlay.class})
public abstract class SplashOverlayMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private ReloadInstance reload;
    @Shadow
    @Final
    private boolean fadeIn;
    @Shadow
    private float currentProgress;
    @Shadow
    private long fadeOutStart;
    @Shadow
    private long fadeInStart;
    @Unique
    private Loading loadingScreen;
    @Unique
    private boolean resourcesMarkedComplete = false;

    @Inject(method={"render"}, at={@At(value="HEAD")}, cancellable=true)
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (this.fadeIn) {
            return;
        }
        ci.cancel();
        if (this.loadingScreen == null) {
            this.loadingScreen = new Loading();
        }
        int width = context.guiWidth();
        int height = context.guiHeight();
        long currentTime = Util.getMillis();
        if (this.fadeInStart == -1L) {
            this.fadeInStart = currentTime;
        }
        float reloadProgress = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp((float)(this.currentProgress * 0.95f + reloadProgress * 0.05f), (float)0.0f, (float)1.0f);
        this.loadingScreen.setProgress(this.currentProgress);
        if (this.reload.isDone() && !this.resourcesMarkedComplete) {
            this.resourcesMarkedComplete = true;
            this.loadingScreen.markComplete();
            if (this.fadeOutStart == -1L) {
                this.fadeOutStart = currentTime;
            }
        }
        this.loadingScreen.render(width, height, 1.0f);
        if (this.loadingScreen.isReadyToClose()) {
            this.minecraft.setOverlay(null);
            this.loadingScreen.reset();
            this.loadingScreen = null;
            this.resourcesMarkedComplete = false;
        }
    }
}

