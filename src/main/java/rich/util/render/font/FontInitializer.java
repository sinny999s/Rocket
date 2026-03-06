/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package rich.util.render.font;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rich.Initialization;
import rich.util.render.font.FontRenderer;

public class FontInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"rich/FontInitializer");
    private static boolean registered = false;
    private static boolean initialized = false;

    public static void register() {
        if (registered) {
            return;
        }
        registered = true;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!initialized && client.getResourceManager() != null && client.getWindow() != null) {
                try {
                    FontRenderer fontRenderer = Initialization.getInstance().getManager().getRenderCore().getFontRenderer();
                    if (fontRenderer != null && !fontRenderer.isInitialized()) {
                        fontRenderer.initialize();
                        initialized = true;
                        LOGGER.info("Fonts initialized successfully");
                    }
                }
                catch (Exception e) {
                    LOGGER.error("Failed to initialize fonts", (Throwable)e);
                }
            }
        });
    }

    public static boolean isInitialized() {
        return initialized;
    }
}

