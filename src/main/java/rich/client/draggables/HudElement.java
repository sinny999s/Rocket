
package rich.client.draggables;

import net.minecraft.client.gui.GuiGraphics;
import rich.events.impl.PacketEvent;

public interface HudElement {
    public void render(GuiGraphics var1, float var2);

    public void tick();

    default public void onPacket(PacketEvent e) {
    }

    public boolean isEnabled();

    public void setEnabled(boolean var1);

    public String getName();

    public int getX();

    public int getY();

    public void setX(int var1);

    public void setY(int var1);

    public int getWidth();

    public int getHeight();

    public void setWidth(int var1);

    public void setHeight(int var1);

    default public float getRoundingRadius() {
        return 4.0f;
    }

    default public boolean visible() {
        return true;
    }

    default public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    default public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
}

