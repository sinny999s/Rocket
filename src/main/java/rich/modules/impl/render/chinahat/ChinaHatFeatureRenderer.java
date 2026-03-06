/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 */
package rich.modules.impl.render.chinahat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import rich.modules.impl.render.ChinaHat;
import rich.util.ColorUtil;
import rich.util.render.sliemtpipeline.ClientPipelines;

public class ChinaHatFeatureRenderer
extends RenderLayer<AvatarRenderState, PlayerModel> {
    private static final float PI2 = (float)Math.PI * 2;
    private static final int CIRCLE_SEGMENTS = 720;
    private static final int OUTLINE_SEGMENTS = 360;

    public ChinaHatFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> context) {
        super(context);
    }

    @Override
    public void submit(PoseStack matrixStack, SubmitNodeCollector queue, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
        Minecraft mc = Minecraft.getInstance();
        ChinaHat chinaHat = ChinaHat.getInstance();
        if (chinaHat == null || !chinaHat.isState()) {
            return;
        }
        if (mc.player == null) {
            return;
        }
        if (mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        if (!this.isLocalPlayer(state, mc)) {
            return;
        }
        matrixStack.pushPose();
        ((PlayerModel)this.getParentModel()).head.translateAndRotate(matrixStack);
        matrixStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f));
        matrixStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
        matrixStack.translate(0.0f, 0.42f, 0.0f);
        MultiBufferSource.BufferSource immediate = mc.renderBuffers().bufferSource();
        this.renderFlatHat(matrixStack, immediate, chinaHat);
        this.renderOutline(matrixStack, immediate, chinaHat);
        immediate.endBatch();
        matrixStack.popPose();
    }

    private boolean isLocalPlayer(AvatarRenderState state, Minecraft mc) {
        try {
            if (state.id == mc.player.getId()) {
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            if (state.scoreText != null && mc.player.getName() != null) {
                return state.scoreText.getString().equals(mc.player.getName().getString());
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private void renderFlatHat(PoseStack stack, MultiBufferSource provider, ChinaHat chinaHat) {
        float z;
        float x;
        float angle;
        int color;
        int i;
        VertexConsumer consumer = provider.getBuffer(ClientPipelines.CHINA_HAT);
        Matrix4f matrix = stack.last().pose();
        float width = 0.55f;
        float coneHeight = 0.31f;
        int alpha = 185;
        float animSpeed = 5.0f;
        int centerColor = this.getGradientColor(0, 720, chinaHat, animSpeed);
        centerColor = ColorUtil.replAlpha(centerColor, alpha);
        consumer.addVertex((Matrix4fc)matrix, 0.0f, coneHeight, 0.0f).setColor(centerColor);
        for (i = 0; i <= 720; ++i) {
            color = this.getGradientColor(i, 720, chinaHat, animSpeed);
            color = ColorUtil.replAlpha(color, alpha);
            angle = (float)i * ((float)Math.PI * 2) / 720.0f;
            x = -Mth.sin((double)angle) * width;
            z = Mth.cos((double)angle) * width;
            consumer.addVertex((Matrix4fc)matrix, x, 0.0f, z).setColor(color);
        }
        for (i = 720; i >= 0; --i) {
            color = this.getGradientColor(i, 720, chinaHat, animSpeed);
            color = ColorUtil.replAlpha(color, alpha);
            angle = (float)i * ((float)Math.PI * 2) / 720.0f;
            x = -Mth.sin((double)angle) * width;
            z = Mth.cos((double)angle) * width;
            consumer.addVertex((Matrix4fc)matrix, x, 0.0f, z).setColor(color);
        }
        consumer.addVertex((Matrix4fc)matrix, 0.0f, coneHeight, 0.0f).setColor(centerColor);
    }

    private void renderOutline(PoseStack stack, MultiBufferSource provider, ChinaHat chinaHat) {
        VertexConsumer consumer = provider.getBuffer(ClientPipelines.CHINA_HAT_OUTLINE);
        Matrix4f matrix = stack.last().pose();
        float width = 0.55f;
        float animSpeed = 5.0f;
        int outlineAlpha = 255;
        for (int i = 0; i <= 360; ++i) {
            int color = this.getGradientColor(i * 2, 720, chinaHat, animSpeed);
            color = ColorUtil.replAlpha(color, outlineAlpha);
            float angle = (float)i * ((float)Math.PI * 2) / 360.0f;
            float x = -Mth.sin((double)angle) * width;
            float z = Mth.cos((double)angle) * width;
            consumer.addVertex((Matrix4fc)matrix, x, 0.0f, z).setColor(color);
        }
    }

    private int getGradientColor(int index, int size, ChinaHat chinaHat, float animSpeed) {
        long time = System.currentTimeMillis();
        float timeOffset = (float)time / (1000.0f / animSpeed) % (float)size;
        int adjustedIndex = (int)(((float)index + timeOffset) % (float)size);
        int color1 = chinaHat.color1.getColor();
        int color2 = chinaHat.color2.getColor();
        float progress = (float)adjustedIndex / (float)size;
        if (progress < 0.5f) {
            return ColorUtil.interpolateColor(color1, color2, progress * 2.0f);
        }
        return ColorUtil.interpolateColor(color2, color1, (progress - 0.5f) * 2.0f);
    }
}

