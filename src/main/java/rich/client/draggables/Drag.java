
package rich.client.draggables;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.MouseButtonEvent;
import rich.Initialization;
import rich.client.draggables.AbstractHudElement;
import rich.client.draggables.HudElement;
import rich.client.draggables.HudManager;
import rich.modules.impl.render.Hud;
import rich.util.ColorUtil;
import rich.util.animations.SweepAnim;
import rich.util.config.impl.drag.DragConfig;
import rich.util.render.Render2D;

public class Drag {
    private static final float OUTLINE_OFFSET = 3.0f;
    private static final float OUTLINE_THICKNESS = 1.0f;
    private static final int OUTLINE_COLOR = ColorUtil.rgba(255, 255, 255, 255);
    private static final Set<String> EXCLUDED_ELEMENTS = Set.of("Notifications", "Watermark", "Info");
    private static HudElement draggingElement;
    private static int startX;
    private static int startY;
    private static final Map<HudElement, SweepAnim> sweepAnimations;
    private static final Map<HudElement, Boolean> wasHovered;

    public static void onDraw(GuiGraphics context, int mouseX, int mouseY, float delta, boolean isChatScreen) {
        HudManager hudManager = Drag.getHudManager();
        if (hudManager == null) {
            return;
        }
        Hud hud = Hud.getInstance();
        if (hud == null || !hud.isState()) {
            return;
        }
        if (!isChatScreen) {
            if (draggingElement != null) {
                DragConfig.getInstance().save();
                draggingElement = null;
            }
            sweepAnimations.clear();
            wasHovered.clear();
        }
        if (isChatScreen && draggingElement != null) {
            draggingElement.setX(mouseX - startX);
            draggingElement.setY(mouseY - startY);
        }
        hudManager.render(context, delta, mouseX, mouseY);
        if (isChatScreen) {
            for (HudElement element : hudManager.getEnabledElements()) {
                if (!element.visible()) {
                    sweepAnimations.remove(element);
                    wasHovered.remove(element);
                    continue;
                }
                if (EXCLUDED_ELEMENTS.contains(element.getName())) continue;
                boolean isHovered = Drag.isHovered(element, mouseX, mouseY);
                boolean previouslyHovered = wasHovered.getOrDefault(element, false);
                float rounding = element.getRoundingRadius();
                float offset = 3.0f;
                float outlineX = (float)element.getX() - offset;
                float outlineY = (float)element.getY() - offset;
                float outlineWidth = (float)element.getWidth() + offset * 2.0f;
                float outlineHeight = (float)element.getHeight() + offset * 2.0f;
                float outlineRounding = Math.max(0.0f, rounding + offset);
                SweepAnim anim = sweepAnimations.computeIfAbsent(element, e -> new SweepAnim(0.05f));
                if (isHovered && !previouslyHovered) {
                    anim.start();
                } else if (!isHovered && previouslyHovered) {
                    anim.reset();
                }
                wasHovered.put(element, isHovered);
                anim.update();
                float progress = anim.getProgress();
                if (isHovered || anim.isActive()) {
                    float baseAlpha = 0.3f;
                    Render2D.glowOutline(outlineX, outlineY, outlineWidth, outlineHeight, 1.0f, OUTLINE_COLOR, outlineRounding, progress, baseAlpha);
                }
                if (isHovered || !anim.isCompleted()) continue;
                sweepAnimations.remove(element);
                wasHovered.remove(element);
            }
        }
    }

    public static void onMouseClick(MouseButtonEvent click) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof ChatScreen)) {
            return;
        }
        if (click.button() == 0) {
            AbstractHudElement abstractElement;
            double mouseY;
            HudManager hudManager = Drag.getHudManager();
            if (hudManager == null) {
                return;
            }
            double mouseX = click.x();
            HudElement element = hudManager.getElementAt(mouseX, mouseY = click.y());
            if (element != null && element instanceof AbstractHudElement && (abstractElement = (AbstractHudElement)element).isDraggable()) {
                draggingElement = element;
                startX = (int)mouseX - element.getX();
                startY = (int)mouseY - element.getY();
            }
        }
    }

    public static void onMouseRelease(MouseButtonEvent click) {
        if (click.button() == 0 && draggingElement != null) {
            DragConfig.getInstance().save();
            draggingElement = null;
        }
    }

    public static void resetDragging() {
        if (draggingElement != null) {
            DragConfig.getInstance().save();
            draggingElement = null;
        }
        sweepAnimations.clear();
        wasHovered.clear();
    }

    public static boolean isDragging() {
        return draggingElement != null;
    }

    private static boolean isHovered(HudElement element, double mouseX, double mouseY) {
        int x = element.getX();
        int y = element.getY();
        int width = element.getWidth();
        int height = element.getHeight();
        return mouseX >= (double)x && mouseX <= (double)(x + width) && mouseY >= (double)y && mouseY <= (double)(y + height);
    }

    private static HudManager getHudManager() {
        if (Initialization.getInstance() == null) {
            return null;
        }
        if (Initialization.getInstance().getManager() == null) {
            return null;
        }
        return Initialization.getInstance().getManager().getHudManager();
    }

    public static void tick() {
        HudManager hudManager = Drag.getHudManager();
        if (hudManager != null) {
            hudManager.tick();
        }
    }

    static {
        sweepAnimations = new HashMap<HudElement, SweepAnim>();
        wasHovered = new HashMap<HudElement, Boolean>();
    }
}

