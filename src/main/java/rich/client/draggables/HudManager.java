
package rich.client.draggables;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import rich.client.draggables.HudElement;
import rich.events.impl.PacketEvent;
import rich.modules.impl.render.Hud;
import rich.screens.hud.CoolDowns;
import rich.screens.hud.HotKeys;
import rich.screens.hud.Info;
import rich.screens.hud.Inventory;
import rich.screens.hud.Notifications;
import rich.screens.hud.Potions;
import rich.screens.hud.Staff;
import rich.screens.hud.TargetHud;
import rich.screens.hud.Watermark;
import rich.screens.hud.CustomHud;
import rich.screens.hud.test;
import rich.util.config.impl.drag.DragConfig;

public class HudManager {
    private final List<HudElement> elements = new ArrayList<HudElement>();
    private boolean initialized = false;

    public void initElements() {
        if (this.initialized) {
            return;
        }
        this.register(new Watermark());
        this.register(new HotKeys());
        this.register(new Notifications());
        this.register(new test());
        this.register(new Potions());
        this.register(new CoolDowns());
        this.register(new TargetHud());
        this.register(new Info());
        this.register(new Staff());
        this.register(new Inventory());
        this.register(new CustomHud());
        this.initialized = true;
        DragConfig.getInstance().load();
    }

    public void register(HudElement element) {
        this.elements.add(element);
    }

    public void onPacket(PacketEvent e) {
        for (HudElement element : this.elements) {
            element.onPacket(e);
        }
    }

    public void render(GuiGraphics context, float tickDelta, int mouseX, int mouseY) {
        Hud hud = Hud.getInstance();
        if (hud == null || !hud.isState()) {
            return;
        }
        for (HudElement element : this.elements) {
            if (!this.isElementEnabled(element)) continue;
            element.render(context, tickDelta);
        }
    }

    public void tick() {
        for (HudElement element : this.elements) {
            if (!this.isElementEnabled(element)) continue;
            element.tick();
        }
    }

    private boolean isElementEnabled(HudElement element) {
        Hud hud = Hud.getInstance();
        if (hud == null || !hud.isState()) {
            return false;
        }
        String name = element.getName();
        return hud.interfaceSettings.isSelected(name);
    }

    public HudElement getElementAt(double mouseX, double mouseY) {
        for (int i = this.elements.size() - 1; i >= 0; --i) {
            HudElement element = this.elements.get(i);
            if (!this.isElementEnabled(element) || !element.visible() || !(mouseX >= (double)element.getX()) || !(mouseX <= (double)(element.getX() + element.getWidth())) || !(mouseY >= (double)element.getY()) || !(mouseY <= (double)(element.getY() + element.getHeight()))) continue;
            return element;
        }
        return null;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (HudElement element : this.elements) {
            if (!this.isElementEnabled(element) || !element.mouseClicked(mouseX, mouseY, button)) continue;
            return true;
        }
        return false;
    }

    public void saveConfig() {
        DragConfig.getInstance().save();
    }

    public void loadConfig() {
        DragConfig.getInstance().load();
    }

    public List<HudElement> getElements() {
        return this.elements;
    }

    public List<HudElement> getEnabledElements() {
        ArrayList<HudElement> enabled = new ArrayList<HudElement>();
        for (HudElement element : this.elements) {
            if (!this.isElementEnabled(element)) continue;
            enabled.add(element);
        }
        return enabled;
    }

    public boolean isInitialized() {
        return this.initialized;
    }
}

