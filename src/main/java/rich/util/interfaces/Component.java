
package rich.util.interfaces;

import net.minecraft.client.gui.GuiGraphics;

public interface Component {
    public void render(GuiGraphics var1, int var2, int var3, float var4);

    public void tick();

    public boolean mouseClicked(double var1, double var3, int var5);

    public boolean mouseReleased(double var1, double var3, int var5);

    public boolean mouseDragged(double var1, double var3, int var5, double var6, double var8);

    public boolean mouseScrolled(double var1, double var3, double var5);

    public boolean keyPressed(int var1, int var2, int var3);

    public boolean charTyped(char var1, int var2);

    public boolean isHover(double var1, double var3);
}

