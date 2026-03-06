
package rich.modules.module;

import lombok.Generated;
import net.minecraft.client.Minecraft;
import rich.IMinecraft;
import rich.Initialization;
import rich.events.api.EventManager;
import rich.events.impl.ModuleToggleEvent;
import rich.modules.impl.render.Hud;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.SettingRepository;
import rich.screens.hud.Notifications;
import rich.util.animations.Animation;
import rich.util.animations.Decelerate;
import rich.util.animations.Direction;

public class ModuleStructure
extends SettingRepository
implements IMinecraft {
    private final String name;
    private final String description;
    private final ModuleCategory category;
    private final Animation animation = new Decelerate().setMs(175).setValue(1.0);
    private int key = -1;
    private int type = 1;
    public boolean state;
    public boolean favorite;

    public ModuleStructure(String name, ModuleCategory category) {
        this.name = name;
        this.category = category;
        this.description = "";
    }

    public ModuleStructure(String name, String description, ModuleCategory category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void switchState() {
        this.setState(!this.state);
    }

    public void setState(boolean state) {
        this.animation.setDirection(state ? Direction.FORWARDS : Direction.BACKWARDS);
        if (state != this.state) {
            this.state = state;
            this.handleStateChange();
        }
    }

    public void switchFavorite() {
        this.setFavorite(!this.favorite);
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    private void handleStateChange() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            Hud hud = Hud.getInstance();
            Notifications notifications = Notifications.getInstance();
            if (hud != null && hud.isState() && notifications != null && hud.interfaceSettings.isSelected("Notifications")) {
                if (this.state) {
                    notifications.addNotification("Feature " + this.name + " - enabled!", 2000L);
                } else {
                    notifications.addNotification("Feature " + this.name + " - disabled!", 2000L);
                }
            }
            if (this.state) {
                this.activate();
            } else {
                this.deactivate();
            }
        }
        this.toggleSilent(this.state);
        ModuleToggleEvent event = new ModuleToggleEvent(this, this.state);
        EventManager.callEvent(event);
    }

    private void toggleSilent(boolean activate) {
        EventManager eventManager = Initialization.getInstance().getManager().getEventManager();
        if (activate) {
            EventManager.register(this);
        } else {
            EventManager.unregister(this);
        }
    }

    public void activate() {
    }

    public void deactivate() {
    }

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public String getDescription() {
        return this.description;
    }

    @Generated
    public ModuleCategory getCategory() {
        return this.category;
    }

    @Generated
    public Animation getAnimation() {
        return this.animation;
    }

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public int getType() {
        return this.type;
    }

    @Generated
    public boolean isState() {
        return this.state;
    }

    @Generated
    public boolean isFavorite() {
        return this.favorite;
    }

    @Generated
    public void setKey(int key) {
        this.key = key;
    }

    @Generated
    public void setType(int type) {
        this.type = type;
    }
}

