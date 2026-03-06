
package rich.util.tps;

import lombok.Generated;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.util.Mth;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.PacketEvent;

public class TPSCalculate {
    private static TPSCalculate instance;
    private float TPS = 20.0f;
    private float adjustTicks = 0.0f;
    private long timestamp;

    public TPSCalculate() {
        instance = this;
        Initialization.getInstance().getManager().getEventManager();
        EventManager.register(this);
    }

    public static TPSCalculate getInstance() {
        return instance;
    }

    @EventHandler
    private void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof ClientboundSetTimePacket) {
            this.updateTPS();
        }
    }

    private void updateTPS() {
        long delay = System.nanoTime() - this.timestamp;
        float maxTPS = 20.0f;
        float rawTPS = maxTPS * (1.0E9f / (float)delay);
        float boundedTPS = Mth.clamp((float)rawTPS, (float)0.0f, (float)maxTPS);
        this.TPS = (float)this.round(boundedTPS);
        this.adjustTicks = boundedTPS - maxTPS;
        this.timestamp = System.nanoTime();
    }

    public double round(double input) {
        return (double)Math.round(input * 100.0) / 100.0;
    }

    public float getTpsRounded() {
        return (float)((double)Math.round(this.TPS * 2.0f) / 2.0);
    }

    @Generated
    public float getTPS() {
        return this.TPS;
    }

    @Generated
    public float getAdjustTicks() {
        return this.adjustTicks;
    }

    @Generated
    public long getTimestamp() {
        return this.timestamp;
    }
}

