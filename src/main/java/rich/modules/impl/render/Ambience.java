
package rich.modules.impl.render;

import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;

public class Ambience
extends ModuleStructure {
    public SliderSettings time = new SliderSettings("Time", "Time of day (0-24000)").range(0, 24000).setValue(1000.0f);
    private double animatedTime = 1000.0;

    public static Ambience getInstance() {
        return Instance.get(Ambience.class);
    }

    public Ambience() {
        super("Ambience", "Changes world time", ModuleCategory.RENDER);
        this.settings(this.time);
    }

    @Override
    public void activate() {
        this.animatedTime = this.time.getValue();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        double targetTime = this.time.getValue();
        double speed = 0.15;
        double diff = targetTime - this.animatedTime;
        this.animatedTime += diff * speed;
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.getPacket() instanceof ClientboundSetTimePacket) {
            event.cancel();
        }
    }

    public long getCustomTime() {
        return (long)this.animatedTime;
    }
}

