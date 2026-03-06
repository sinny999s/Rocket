
package rich;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import rich.util.render.draw.DrawEngine;
import rich.util.render.draw.DrawEngineImpl;

public interface IMinecraft {
    public static final Minecraft mc = Minecraft.getInstance();
    public static final Window window = Minecraft.getInstance().getWindow();
    public static final Tesselator tessellator = Tesselator.getInstance();
    public static final DeltaTracker tickCounter = mc.getDeltaTracker();
    public static final DrawEngine drawEngine = new DrawEngineImpl();
}

