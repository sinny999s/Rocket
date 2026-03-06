/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 */
package rich.events.api.types;

import java.util.Objects;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.api.types.Listener;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.UsingItemEvent;

public class EventListener
implements Listener {
    public static boolean serverSprint;
    public static int selectedSlot;

    @EventHandler
    public void onTick(TickEvent e) {
        Initialization.getInstance().getManager().getAttackPerpetrator().tick();
        if (Initialization.getInstance().getManager().getHudManager() != null) {
            Initialization.getInstance().getManager().getHudManager().tick();
        }
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        Objects.requireNonNull(packet);
        if (packet instanceof ServerboundPlayerCommandPacket) {
                ServerboundPlayerCommandPacket command = (ServerboundPlayerCommandPacket)packet;
                serverSprint = switch (command.getAction()) {
                    case ServerboundPlayerCommandPacket.Action.START_SPRINTING -> true;
                    case ServerboundPlayerCommandPacket.Action.STOP_SPRINTING -> false;
                    default -> serverSprint;
                };
        } else if (packet instanceof ServerboundSetCarriedItemPacket) {
                ServerboundSetCarriedItemPacket slot = (ServerboundSetCarriedItemPacket)packet;
                selectedSlot = slot.getSlot();
        }
        Initialization.getInstance().getManager().getAttackPerpetrator().onPacket(e);
        Initialization.getInstance().getManager().getHudManager().onPacket(e);
    }

    @EventHandler
    public void onUsingItemEvent(UsingItemEvent e) {
        Initialization.getInstance().getManager().getAttackPerpetrator().onUsingItem(e);
    }
}

