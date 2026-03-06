/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  com.llamalad7.mixinextras.sugar.Local
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.At$Shift
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.Initialization;
import rich.client.draggables.Drag;
import rich.events.api.EventManager;
import rich.events.impl.FovEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.impl.player.NoEntityTrace;
import rich.modules.impl.render.Hud;
import rich.modules.impl.render.NoRender;
import rich.screens.clickgui.ClickGui;
import rich.util.render.Render3D;

@Mixin(value={GameRenderer.class})
public abstract class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    @Final
    private Camera mainCamera;
    @Shadow
    @Final
    GuiRenderState guiRenderState;
    @Shadow
    @Final
    private GuiRenderer guiRenderer;
    @Shadow
    @Final
    private FogRenderer fogRenderer;
    @Unique
    private final PoseStack matrices = new PoseStack();

    @Shadow
    protected abstract void bobView(PoseStack var1, float var2);

    @Shadow
    protected abstract void bobHurt(PoseStack var1, float var2);

    @Shadow
    public abstract float getFov(Camera var1, float var2, boolean var3);

    @Inject(method={"close"}, at={@At(value="RETURN")})
    private void onClose(CallbackInfo ci) {
        if (Initialization.getInstance() != null && Initialization.getInstance().getManager() != null && Initialization.getInstance().getManager().getRenderCore() != null) {
            Initialization.getInstance().getManager().getRenderCore().close();
        }
    }

    @ModifyExpressionValue(method={"getFov"}, at={@At(value="INVOKE", target="Ljava/lang/Integer;intValue()I", remap=false)})
    private int hookGetFov(int original) {
        FovEvent event = new FovEvent();
        EventManager.callEvent(event);
        if (event.isCancelled()) {
            return event.getFov();
        }
        return original;
    }

    @Inject(method={"pick"}, at={@At(value="HEAD")}, cancellable=true)
    private void updateCrosshairTargetHook(float tickProgress, CallbackInfo ci) {
        Entity entity;
        NoEntityTrace noEntityTrace = NoEntityTrace.getInstance();
        if (noEntityTrace != null && noEntityTrace.shouldIgnoreEntityTrace() && (entity = this.minecraft.getCameraEntity()) != null && this.minecraft.level != null && this.minecraft.player != null) {
            double range = Math.max(this.minecraft.player.blockInteractionRange(), this.minecraft.player.entityInteractionRange());
            this.minecraft.hitResult = entity.pick(range, tickProgress, false);
            this.minecraft.crosshairPickEntity = null;
            ci.cancel();
        }
    }

    @Inject(method={"renderLevel"}, at={@At(value="INVOKE_STRING", target="Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args={"ldc=hand"})})
    public void hookWorldRender(DeltaTracker tickCounter, CallbackInfo ci, @Local(ordinal=0) Matrix4f projection, @Local(ordinal=1) Matrix4f view, @Local(ordinal=0) float tickDelta, @Local PoseStack matrixStack) {
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return;
        }
        PoseStack worldSpaceStack = new PoseStack();
        worldSpaceStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(this.mainCamera.xRot()));
        worldSpaceStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(this.mainCamera.yRot() + 180.0f));
        Render3D.lastProjMat.set((Matrix4fc)this.minecraft.gameRenderer.getProjectionMatrix(this.getFov(this.mainCamera, tickDelta, true)));
        Render3D.lastModMat.set((Matrix4fc)RenderSystem.getModelViewMatrix());
        Render3D.lastWorldSpaceMatrix.set((Matrix4fc)worldSpaceStack.last().pose());
        Render3D.setLastWorldSpaceEntry(matrixStack.last());
        Render3D.setLastTickDelta(tickDelta);
        Render3D.setLastCameraPos(this.mainCamera.position());
        Render3D.setLastCameraRotation(new Quaternionf((Quaternionfc)this.mainCamera.rotation()));
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix().mul((Matrix4fc)view);
        this.matrices.pushPose();
        this.bobHurt(this.matrices, this.mainCamera.getPartialTickTime());
        if (((Boolean)this.minecraft.options.bobView().get()).booleanValue()) {
            this.bobView(this.matrices, this.mainCamera.getPartialTickTime());
        }
        modelViewStack.mul((Matrix4fc)this.matrices.last().pose().invert(new Matrix4f()));
        this.matrices.popPose();
        WorldRenderEvent event = new WorldRenderEvent(matrixStack, tickDelta);
        EventManager.callEvent(event);
        Render3D.onWorldRender(event);
        modelViewStack.popMatrix();
    }

    @Inject(method={"bobHurt"}, at={@At(value="HEAD")}, cancellable=true)
    private void onTiltViewWhenHurt(PoseStack matrices, float tickDelta, CallbackInfo ci) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Damage")) {
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method={"renderLevel"}, at={@At(value="INVOKE", target="Ljava/lang/Math;max(FF)F", ordinal=0)})
    private float onNauseaDistortion(float original) {
        NoRender noRender = NoRender.getInstance();
        if (noRender != null && noRender.isState() && noRender.modeSetting.isSelected("Nausea")) {
            return 0.0f;
        }
        return original;
    }

    @Inject(method={"render"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V", shift=At.Shift.AFTER)})
    private void afterGuiRender(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        Screen screen;
        if (this.minecraft.level == null || this.minecraft.player == null) {
            return;
        }
        if (this.isLoadingScreen(this.minecraft.screen)) {
            return;
        }
        if (this.minecraft.getOverlay() != null) {
            return;
        }
        if (!this.shouldRenderOnTop(this.minecraft.screen)) {
            return;
        }
        this.guiRenderState.reset();
        int mouseX = (int)this.minecraft.mouseHandler.getScaledXPos(this.minecraft.getWindow());
        int mouseY = (int)this.minecraft.mouseHandler.getScaledYPos(this.minecraft.getWindow());
        float tickDelta = tickCounter.getGameTimeDeltaPartialTick(false);
        GuiGraphics context = new GuiGraphics(this.minecraft, this.guiRenderState, mouseX, mouseY);
        Hud hud = Hud.getInstance();
        if (hud != null && hud.isState()) {
            boolean isChatScreen = this.minecraft.screen instanceof ChatScreen;
            Drag.onDraw(context, mouseX, mouseY, tickDelta, isChatScreen);
        }
        if ((screen = this.minecraft.screen) instanceof ClickGui) {
            ClickGui clickGui = (ClickGui)screen;
            clickGui.renderOverlay(context, tickCounter);
        }
        this.guiRenderer.render(this.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
    }

    @Unique
    private boolean shouldRenderOnTop(Screen screen) {
        if (screen == null) {
            return true;
        }
        if (screen instanceof ClickGui) {
            return true;
        }
        return screen instanceof ChatScreen;
    }

    @Unique
    private boolean isLoadingScreen(Screen screen) {
        if (screen == null) {
            return false;
        }
        String className = screen.getClass().getSimpleName().toLowerCase();
        String fullName = screen.getClass().getName().toLowerCase();
        if (className.contains("loading")) {
            return true;
        }
        if (className.contains("progress")) {
            return true;
        }
        if (className.contains("connecting")) {
            return true;
        }
        if (className.contains("downloading")) {
            return true;
        }
        if (className.contains("terrain")) {
            return true;
        }
        if (className.contains("generating")) {
            return true;
        }
        if (className.contains("saving")) {
            return true;
        }
        if (className.contains("reload")) {
            return true;
        }
        if (className.contains("resource")) {
            return true;
        }
        if (className.contains("pack")) {
            return true;
        }
        return fullName.contains("mojang");
    }
}

