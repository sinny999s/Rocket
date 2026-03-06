
package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.CameraType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.animations.Animation;
import rich.util.animations.Direction;
import rich.util.animations.EaseInOutQuad;
import rich.util.animations.Easings;
import rich.util.animations.SmoothAnimation;

public class Arrows
extends ModuleStructure {
    private static final Identifier ARROW_TEXTURE = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/world/arrow.png");
    public SliderSettings arrowsDistance = new SliderSettings("Distance", "Distance from crosshair").range(1.0f, 20.0f).setValue(25.0f);
    public ColorSetting arrowColor = new ColorSetting("Color", "Color arrows").value(-7773880);
    private final SmoothAnimation animationStep = new SmoothAnimation();
    private final SmoothAnimation animatedYaw = new SmoothAnimation();
    private final SmoothAnimation animatedPitch = new SmoothAnimation();
    private final SmoothAnimation animatedCameraYaw = new SmoothAnimation();
    private final List<Arrow> playerList = new ArrayList<Arrow>();

    public static Arrows getInstance() {
        return Instance.get(Arrows.class);
    }

    public Arrows() {
        super("Arrows", "Show arrows toward players", ModuleCategory.RENDER);
        this.settings(this.arrowsDistance, this.arrowColor);
    }

    @Override
    public void deactivate() {
        this.playerList.clear();
    }

    @EventHandler
    public void onDraw(DrawEvent event) {
        if (Arrows.mc.player == null || Arrows.mc.level == null) {
            return;
        }
        if (Arrows.mc.options.getCameraType() != CameraType.FIRST_PERSON) {
            return;
        }
        GuiGraphics context = event.getDrawContext();
        float partialTicks = event.getPartialTicks();
        this.animationStep.update();
        this.animatedYaw.update();
        this.animatedPitch.update();
        this.animatedCameraYaw.update();
        float size = 45.0f + this.arrowsDistance.getValue();
        if (Arrows.mc.screen instanceof InventoryScreen) {
            size += 80.0f;
        }
        if (Arrows.mc.player.isShiftKeyDown()) {
            size -= 20.0f;
        }
        if (this.isMoving()) {
            size += 10.0f;
        }
        float strafeInput = Arrows.mc.player.input.getMoveVector().x;
        float forwardInput = Arrows.mc.player.input.getMoveVector().y;
        this.animatedYaw.run((double)(strafeInput * 5.0f), 0.75, Easings.EXPO_OUT);
        this.animatedPitch.run((double)(forwardInput * 5.0f), 0.75, Easings.EXPO_OUT);
        this.animatedCameraYaw.run(Arrows.mc.gameRenderer.getMainCamera().yRot(), 0.75, Easings.EXPO_OUT, true);
        this.animationStep.run(size, 1.0, Easings.EXPO_OUT, false);
        ArrayList<Arrow> players = new ArrayList<Arrow>();
        for (Object player : Arrows.mc.level.players()) {
            Optional<Arrow> arrowConsumer = this.playerList.stream().filter(arg_0 -> Arrows.lambda$onDraw$0((AbstractClientPlayer)player, arg_0)).findFirst();
            if (arrowConsumer.isPresent() && !this.isValidPlayer((Player)player)) continue;
            Arrow arrow = new Arrow((Player)player, arrowConsumer.map(a -> a.fadeAnimation).orElse(this.createFadeAnimation()));
            players.add(arrow);
        }
        ArrayList<Arrow> arrows = new ArrayList<Arrow>(this.playerList);
        arrows.removeIf(p -> players.stream().anyMatch(p2 -> p.player == p2.player));
        for (Arrow arrow : arrows) {
            arrow.fadeAnimation.setDirection(Direction.BACKWARDS);
            if (arrow.isDead()) continue;
            players.add(arrow);
        }
        this.playerList.clear();
        this.playerList.addAll(players);
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        float centerX = (float)screenWidth / 2.0f;
        float centerY = (float)screenHeight / 2.0f;
        for (Arrow arrow : this.playerList) {
            Player player = arrow.player;
            arrow.updateAlpha();
            float animValue = arrow.getAlpha();
            if (animValue <= 0.001f || !this.isValidPlayer(player) && arrow.fadeAnimation.isDirection(Direction.FORWARDS)) continue;
            double playerX = player.xOld + (player.getX() - player.xOld) * (double)partialTicks - Arrows.mc.gameRenderer.getMainCamera().position().x;
            double playerZ = player.zOld + (player.getZ() - player.zOld) * (double)partialTicks - Arrows.mc.gameRenderer.getMainCamera().position().z;
            double cameraYaw = this.animatedCameraYaw.getValue();
            double cos = Mth.cos((double)((float)(cameraYaw * (Math.PI / 180))));
            double sin = Mth.sin((double)((float)(cameraYaw * (Math.PI / 180))));
            double rotY = -(playerZ * cos - playerX * sin);
            double rotX = -(playerX * cos + playerZ * sin);
            float angle = (float)(Math.atan2(rotY, rotX) * 180.0 / Math.PI);
            double x2 = this.animationStep.getValue() * (double)animValue * (double)Mth.cos((double)((float)Math.toRadians(angle))) + (double)centerX;
            double y2 = this.animationStep.getValue() * (double)animValue * (double)Mth.sin((double)((float)Math.toRadians(angle))) + (double)centerY;
            int color = this.applyAlpha(this.arrowColor.getColor(), animValue);
            this.drawArrow(context, (float)(x2 += this.animatedYaw.getValue()), (float)(y2 += this.animatedPitch.getValue()), angle, color, 1.0f);
        }
    }

    private Animation createFadeAnimation() {
        Animation anim = new EaseInOutQuad().setMs(200).setValue(1.0);
        anim.setDirection(Direction.FORWARDS);
        return anim;
    }

    private void drawArrow(GuiGraphics context, float x, float y, float angle, int color, float scale) {
        float size = 17.0f * scale;
        float halfSize = size / 2.0f;
        context.pose().pushMatrix();
        context.pose().translate(x, y);
        context.pose().rotate((float)Math.toRadians(angle));
        context.pose().rotate((float)Math.toRadians(90.0));
        int intSize = (int)size;
        context.blit(RenderPipelines.GUI_TEXTURED, ARROW_TEXTURE, (int)(1.0f - halfSize), -5, 0.0f, 0.0f, intSize, intSize, intSize, intSize, color);
        context.pose().popMatrix();
    }

    private int applyAlpha(int color, float alpha) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int)(alpha * 255.0f);
        return a << 24 | r << 16 | g << 8 | b;
    }

    private boolean isValidPlayer(Player player) {
        return player != Arrows.mc.player && !player.isRemoved();
    }

    private boolean isMoving() {
        return Arrows.mc.player.input.getMoveVector().y != 0.0f || Arrows.mc.player.input.getMoveVector().x != 0.0f;
    }

    private static /* synthetic */ boolean lambda$onDraw$0(AbstractClientPlayer player, Arrow a) {
        return a.player == player;
    }

    private static class Arrow {
        final Player player;
        final Animation fadeAnimation;
        float cachedAlpha = 0.0f;
        long lastAlphaUpdate = 0L;

        Arrow(Player player, Animation fadeAnimation) {
            this.player = player;
            this.fadeAnimation = fadeAnimation;
        }

        void updateAlpha() {
            long now = System.currentTimeMillis();
            if (now - this.lastAlphaUpdate > 16L) {
                this.cachedAlpha = this.fadeAnimation.getOutput().floatValue();
                this.lastAlphaUpdate = now;
            }
        }

        float getAlpha() {
            return this.cachedAlpha;
        }

        boolean isDead() {
            return this.fadeAnimation.isDirection(Direction.BACKWARDS) && this.fadeAnimation.isDone() && this.cachedAlpha <= 0.0f;
        }
    }
}

