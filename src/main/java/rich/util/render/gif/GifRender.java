
package rich.util.render.gif;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import rich.util.render.Render2D;

public class GifRender {
    private static final List<Identifier> avatarFrames = new ArrayList<Identifier>();
    private static final List<Identifier> backgroundFrames = new ArrayList<Identifier>();
    private static long lastAvatarTime = 0L;
    private static long lastBackgroundTime = 0L;
    private static int avatarFrameIndex = 0;
    private static int backgroundFrameIndex = 0;
    private static final long AVATAR_DELAY = 33L;
    private static final long BACKGROUND_DELAY = 50L;
    private static boolean initialized = false;

    public static void init() {
        Identifier id;
        String frameName;
        int i;
        if (initialized) {
            return;
        }
        avatarFrames.clear();
        backgroundFrames.clear();
        for (i = 1; i <= 100; ++i) {
            frameName = String.format("image%03d", i);
            id = Identifier.fromNamespaceAndPath((String)"rich", (String)("images/gifs/avatar/" + frameName + ".png"));
            avatarFrames.add(id);
        }
        for (i = 0; i <= 16; ++i) {
            frameName = String.format("frame_%02d_delay-0.05s", i);
            id = Identifier.fromNamespaceAndPath((String)"rich", (String)("images/gifs/back/" + frameName + ".png"));
            backgroundFrames.add(id);
        }
        lastAvatarTime = System.currentTimeMillis();
        lastBackgroundTime = System.currentTimeMillis();
        initialized = true;
    }

    public static void tick() {
        if (!initialized) {
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (!avatarFrames.isEmpty() && currentTime - lastAvatarTime >= 33L) {
            avatarFrameIndex = (avatarFrameIndex + 1) % avatarFrames.size();
            lastAvatarTime = currentTime;
        }
        if (!backgroundFrames.isEmpty() && currentTime - lastBackgroundTime >= 50L) {
            backgroundFrameIndex = (backgroundFrameIndex + 1) % backgroundFrames.size();
            lastBackgroundTime = currentTime;
        }
    }

    public static void drawAvatar(float x, float y, float width, float height, int color) {
        if (!initialized) {
            GifRender.init();
        }
        if (avatarFrames.isEmpty()) {
            return;
        }
        Identifier frame = avatarFrames.get(avatarFrameIndex);
        Render2D.texture(frame, x, y, width, height, 1.0f, 15.0f, color);
    }

    public static void drawAvatar(float x, float y, float width, float height, float radius, int color) {
        if (!initialized) {
            GifRender.init();
        }
        if (avatarFrames.isEmpty()) {
            return;
        }
        Identifier frame = avatarFrames.get(avatarFrameIndex);
        Render2D.texture(frame, x, y, width, height, 1.0f, radius, color);
    }

    public static void drawBackground(float x, float y, float width, float height, int color) {
        if (!initialized) {
            GifRender.init();
        }
        if (backgroundFrames.isEmpty()) {
            return;
        }
        Identifier frame = backgroundFrames.get(backgroundFrameIndex);
        Render2D.texture(frame, x, y, width, height, color);
    }

    public static void drawBackground(float x, float y, float width, float height, float radius, int color) {
        if (!initialized) {
            GifRender.init();
        }
        if (backgroundFrames.isEmpty()) {
            return;
        }
        Identifier frame = backgroundFrames.get(backgroundFrameIndex);
        Render2D.texture(frame, x, y, width, height, 1.0f, radius, color);
    }

    public static void resetAvatar() {
        avatarFrameIndex = 0;
        lastAvatarTime = System.currentTimeMillis();
    }

    public static void resetBackground() {
        backgroundFrameIndex = 0;
        lastBackgroundTime = System.currentTimeMillis();
    }

    public static void reset() {
        GifRender.resetAvatar();
        GifRender.resetBackground();
    }
}

