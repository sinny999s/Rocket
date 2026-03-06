
package rich.events.impl;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.screens.Screen;
import rich.IMinecraft;
import rich.events.api.events.Event;

public record KeyEvent(Screen screen, InputConstants.Type type, int key, int action) implements Event,
IMinecraft
{
    public boolean isKeyDown(int key) {
        return this.isKeyDown(key, KeyEvent.mc.screen == null);
    }

    public boolean isKeyDown(int key, boolean screen) {
        return this.key == key && this.action == 1 && screen;
    }

    public boolean isKeyReleased(int key) {
        return this.isKeyReleased(key, KeyEvent.mc.screen == null);
    }

    public boolean isKeyReleased(int key, boolean screen) {
        return this.key == key && this.action == 0 && screen;
    }
}

