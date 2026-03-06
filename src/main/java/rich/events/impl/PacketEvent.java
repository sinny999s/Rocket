
package rich.events.impl;

import lombok.Generated;
import net.minecraft.network.protocol.Packet;
import rich.events.api.events.callables.EventCancellable;

public class PacketEvent
extends EventCancellable {
    private Packet<?> packet;
    private Type type;

    public boolean isSend() {
        return this.type.equals((Object)Type.SEND);
    }

    @Generated
    public Packet<?> getPacket() {
        return this.packet;
    }

    @Generated
    public Type getType() {
        return this.type;
    }

    @Generated
    public void setPacket(Packet<?> packet) {
        this.packet = packet;
    }

    @Generated
    public void setType(Type type) {
        this.type = type;
    }

    @Generated
    public PacketEvent(Packet<?> packet, Type type) {
        this.packet = packet;
        this.type = type;
    }

    public static enum Type {
        SEND,
        RECEIVE;

    }
}

