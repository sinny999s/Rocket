/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  lombok.Generated
 *  org.apache.commons.lang3.StringUtils
 */
package rich.util.network;

import java.util.Objects;
import lombok.Generated;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.apache.commons.lang3.StringUtils;
import rich.IMinecraft;
import rich.events.impl.PacketEvent;
import rich.util.string.PlayerInteractionHelper;
import rich.util.timer.StopWatch;

public final class Network
implements IMinecraft {
    private static final StopWatch pvpWatch = new StopWatch();
    public static String server = "Vanilla";
    public static float TPS = 20.0f;
    public static long timestamp;
    public static int anarchy;
    public static boolean pvpEnd;

    public static void tick() {
        anarchy = Network.getAnarchyMode();
        server = Network.getServer();
        pvpEnd = Network.inPvpEnd();
        if (Network.inPvp()) {
            pvpWatch.reset();
        }
    }

    public static void packet(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        Objects.requireNonNull(packet);
        if (packet instanceof ClientboundSetTimePacket) {
            ClientboundSetTimePacket time = (ClientboundSetTimePacket)packet;
            long nanoTime = System.nanoTime();
            float maxTPS = 20.0f;
            float rawTPS = maxTPS * (1.0E9f / (float)(nanoTime - timestamp));
            TPS = Mth.clamp((float)rawTPS, (float)0.0f, (float)maxTPS);
            timestamp = nanoTime;
        }
    }

    public static String getServer() {
        if (PlayerInteractionHelper.nullCheck() || mc.getConnection() == null || mc.getConnection().getServerData() == null || mc.getConnection().serverBrand() == null) {
            return "Vanilla";
        }
        String serverIp = Network.mc.getConnection().getServerData().ip.toLowerCase();
        String brand = mc.getConnection().serverBrand().toLowerCase();
        if (brand.contains("botfilter")) {
            return "FunTime";
        }
        if (brand.contains("\u00a76spooky\u00a7ccore")) {
            return "SpookyTime";
        }
        if (serverIp.contains("funtime") || serverIp.contains("skytime") || serverIp.contains("space-times") || serverIp.contains("funsky")) {
            return "CopyTime";
        }
        if (brand.contains("holyworld") || brand.contains("vk.com/idwok")) {
            return "HolyWorld";
        }
        if (serverIp.contains("reallyworld")) {
            return "ReallyWorld";
        }
        if (serverIp.contains("gulpvp")) {
            return "GulPvP";
        }
        return "Vanilla";
    }

    private static int getAnarchyMode() {
        Scoreboard scoreboard = Network.mc.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        switch (server) {
            case "FunTime": {
                String[] string;
                if (objective == null || (string = objective.getDisplayName().getString().split("-")).length <= 1) break;
                return Integer.parseInt(string[1]);
            }
            case "HolyWorld": {
                for (PlayerScoreEntry scoreboardEntry : scoreboard.listPlayerScores(objective)) {
                    String string;
                    String text = PlayerTeam.formatNameForTeam((Team)scoreboard.getPlayersTeam(scoreboardEntry.owner()), (Component)scoreboardEntry.ownerName()).getString();
                    if (text.isEmpty() || (string = StringUtils.substringBetween((String)text, (String)"#", (String)" -\u25c6-")) == null || string.isEmpty()) continue;
                    return Integer.parseInt(string.replace(" (1.20)", ""));
                }
                break;
            }
        }
        return -1;
    }

    public static boolean isPvp() {
        return !pvpWatch.finished(500.0);
    }

    private static boolean inPvp() {
        return Network.mc.gui.getBossOverlay().events.values().stream().map(c -> c.getName().getString().toLowerCase()).anyMatch(s -> s.contains("pvp"));
    }

    private static boolean inPvpEnd() {
        return Network.mc.gui.getBossOverlay().events.values().stream().map(c -> c.getName().getString().toLowerCase()).anyMatch(s -> !(!s.contains("pvp") || !s.contains("0") && !s.contains("1")));
    }

    public static String getWorldType() {
        return Network.mc.level.dimension().identifier().getPath();
    }

    public static boolean isCopyTime() {
        return server.equals("CopyTime") || server.equals("SpookyTime") || server.equals("FunTime");
    }

    public static boolean isFunTime() {
        return server.equals("FunTime");
    }

    public static boolean isReallyWorld() {
        return server.equals("ReallyWorld");
    }

    public static boolean isGulPvP() {
        return server.equals("GulPvP");
    }

    public static boolean isHolyWorld() {
        return server.equals("HolyWorld");
    }

    public static boolean isSpookyTime() {
        return server.equals("SpookyTime");
    }

    public static boolean isAresMine() {
        return server.equals("aresmine");
    }

    public static boolean isVanilla() {
        return server.equals("Vanilla");
    }

    @Generated
    private Network() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static int getAnarchy() {
        return anarchy;
    }

    @Generated
    public static boolean isPvpEnd() {
        return pvpEnd;
    }
}

