
package rich.screens.clickgui.impl.background.render;

import antidaunleak.api.UserProfile;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.gif.GifRender;
import rich.util.render.shader.Scissor;

public class AvatarRenderer {
    private static final int FORCED_GUI_SCALE = 2;
    private static final Minecraft mc = Minecraft.getInstance();

    public void render(GuiGraphics context, float bgX, float bgY, float alphaMultiplier) {
        int alpha = (int)(255.0f * alphaMultiplier);
        int alphaFon = (int)(105.0f * alphaMultiplier);
        int alphaText = (int)(200.0f * alphaMultiplier);
        UserProfile userProfile = UserProfile.getInstance();
        String username = userProfile.profile("username");
        String uid = userProfile.profile("uid");
        Render2D.blur(bgX + 15.0f, bgY + 15.0f, 1.0f, 1.0f, 0.0f, 0.0f, alphaText);
        context.pose().pushMatrix();
        Render2D.blur(bgX + 15.0f, bgY + 15.0f, 1.0f, 1.0f, 0.0f, 0.0f, alphaText);
        GifRender.drawBackground(bgX + 12.5f, bgY + 12.5f, 70.0f, 30.0f, 7.0f, this.applyAlpha(-1, alpha));
        Render2D.rect(bgX + 15.0f, bgY + 15.0f, 25.0f, 25.0f, new Color(42, 42, 42, alpha).getRGB(), 15.0f);
        GifRender.drawAvatar(bgX + 16.0f, bgY + 16.0f, 23.0f, 23.0f, 15.0f, this.applyAlpha(-1, alpha));
        Render2D.rect(bgX + 33.0f, bgY + 33.0f, 5.0f, 5.0f, new Color(0, 255, 0, alpha).getRGB(), 10.0f);
        context.pose().popMatrix();
        Render2D.rect(bgX + 12.5f, bgY + 12.5f, 70.0f, 30.0f, new Color(0, 0, 0, alphaFon).getRGB(), 7.0f);
        float textX = bgX + 44.0f;
        float textY = bgY + 22.0f;
        float maxTextWidth = 35.0f;
        float textHeight = 14.0f;
        Scissor.enable(textX, textY - 2.0f, maxTextWidth, textHeight, 2.0f);
        Fonts.BOLD.draw(username, textX, textY, 6.0f, new Color(255, 255, 255, alphaText).getRGB());
        Fonts.BOLD.draw("Uid: " + uid, textX, textY + 7.0f, 5.0f, new Color(255, 255, 255, alphaText).getRGB());
        Render2D.blur(textX, textY + 7.0f, 1.0f, 1.0f, 0.0f, 0.0f, alphaText);
        Scissor.disable();
        Render2D.blur(textX, textY + 7.0f, 1.0f, 1.0f, 0.0f, 0.0f, alphaText);
    }

    private int applyAlpha(int color, int alpha) {
        return color & 0xFFFFFF | alpha << 24;
    }
}

