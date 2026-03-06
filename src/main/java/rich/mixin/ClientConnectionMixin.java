/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelPipeline
 *  io.netty.handler.proxy.Socks4ProxyHandler
 *  io.netty.handler.proxy.Socks5ProxyHandler
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import net.minecraft.network.BandwidthDebugMonitor;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.events.api.EventManager;
import rich.events.impl.PacketEvent;
import rich.util.config.impl.proxy.ProxyConfig;
import rich.util.proxy.Proxy;

@Mixin(value={Connection.class})
public class ClientConnectionMixin {
    @Inject(method={"genericsFtw"}, at={@At(value="HEAD")}, cancellable=true)
    private static <T extends PacketListener> void handlePacketPre(Packet<T> packet, PacketListener listener, CallbackInfo info) {
        PacketEvent packetEvent = new PacketEvent(packet, PacketEvent.Type.RECEIVE);
        EventManager.callEvent(packetEvent);
        if (packetEvent.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"send"}, at={@At(value="HEAD")}, cancellable=true)
    private void sendPre(Packet<?> packet, CallbackInfo info) {
        PacketEvent packetEvent = new PacketEvent(packet, PacketEvent.Type.SEND);
        EventManager.callEvent(packetEvent);
        if (packetEvent.isCancelled()) {
            info.cancel();
        }
    }

    @Inject(method={"configureSerialization"}, at={@At(value="RETURN")})
    private static void addHandlersHook(ChannelPipeline pipeline, PacketFlow side, boolean local, BandwidthDebugMonitor packetSizeLogger, CallbackInfo ci) {
        ProxyConfig config = ProxyConfig.getInstance();
        Proxy proxy = config.getDefaultProxy();
        if (proxy != null && config.isProxyEnabled() && !proxy.isEmpty() && side == PacketFlow.CLIENTBOUND && !local) {
            InetSocketAddress proxyAddress = new InetSocketAddress(proxy.getIp(), proxy.getPort());
            if (proxy.type == Proxy.ProxyType.SOCKS4) {
                pipeline.addFirst("rich_socks4_proxy", (ChannelHandler)new Socks4ProxyHandler((SocketAddress)proxyAddress, proxy.username));
            } else {
                pipeline.addFirst("rich_socks5_proxy", (ChannelHandler)new Socks5ProxyHandler((SocketAddress)proxyAddress, proxy.username, proxy.password));
            }
            config.setLastUsedProxy(new Proxy(proxy));
        }
    }
}

