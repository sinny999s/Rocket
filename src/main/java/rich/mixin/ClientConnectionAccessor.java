/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.gen.Accessor
 *  org.spongepowered.asm.mixin.gen.Invoker
 */
package rich.mixin;

import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Connection.class})
public interface ClientConnectionAccessor {
    @Accessor(value="packetListener")
    public PacketListener client$listener();

    @Invoker(value="genericsFtw")
    public static <T extends PacketListener> void handlePacket(Packet<T> packet, PacketListener listener) {
        throw new UnsupportedOperationException();
    }
}

