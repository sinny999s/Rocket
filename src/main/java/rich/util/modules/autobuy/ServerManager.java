
package rich.util.modules.autobuy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import rich.util.modules.autobuy.NetworkManager;
import rich.util.timer.TimerUtil;

public class ServerManager {
    private List<String> anarchyServers165 = new ArrayList<String>();
    private List<String> anarchyServers214 = new ArrayList<String>();
    private int currentServerIndex = 0;
    private String currentServer = "";
    private boolean inHub = false;
    private boolean waitingForServerLoad = false;
    private TimerUtil hubCheckTimer = TimerUtil.create();
    private TimerUtil serverSwitchCooldown = TimerUtil.create();

    public ServerManager() {
        this.initializeServers();
    }

    private void initializeServers() {
        int i;
        this.anarchyServers165.addAll(List.of("/an102", "/an103", "/an104", "/an105", "/an106", "/an107"));
        for (i = 203; i <= 221; ++i) {
            this.anarchyServers165.add("/an" + i);
        }
        for (i = 302; i <= 313; ++i) {
            this.anarchyServers165.add("/an" + i);
        }
        this.anarchyServers165.addAll(List.of("/an502", "/an503", "/an504", "/an505", "/an506", "/an507", "/an602"));
        for (i = 11; i <= 14; ++i) {
            this.anarchyServers214.add("/an" + i);
        }
        for (i = 21; i <= 27; ++i) {
            this.anarchyServers214.add("/an" + i);
        }
        for (i = 31; i <= 34; ++i) {
            this.anarchyServers214.add("/an" + i);
        }
        for (i = 51; i <= 53; ++i) {
            this.anarchyServers214.add("/an" + i);
        }
        this.anarchyServers214.add("/an91");
    }

    public void resetTimers() {
        this.hubCheckTimer.resetCounter();
        this.serverSwitchCooldown.resetCounter();
    }

    public void reset() {
        this.currentServerIndex = 0;
        this.currentServer = "";
        this.inHub = false;
        this.waitingForServerLoad = false;
        this.resetTimers();
    }

    public void updateHubStatus(ClientLevel world) {
        this.inHub = this.isInHubInternal(world);
    }

    private boolean isInHubInternal(ClientLevel world) {
        if (world == null) {
            return true;
        }
        Scoreboard scoreboard = world.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) {
            return true;
        }
        String displayName = objective.getDisplayName().getString();
        return !displayName.contains("Anarchy-");
    }

    private int getCurrentAnarchyNumber(ClientLevel world) {
        String[] parts;
        String displayName;
        if (world == null) {
            return -1;
        }
        Scoreboard scoreboard = world.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective != null && (displayName = objective.getDisplayName().getString()).contains("Anarchy-") && (parts = displayName.split("-")).length > 1) {
            try {
                return Integer.parseInt(parts[1].trim());
            }
            catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }

    private String getNextServer(List<String> servers, ClientLevel world) {
        String currentServerCmd;
        int currentIdx;
        if (servers.isEmpty()) {
            return null;
        }
        int currentAnarchy = this.getCurrentAnarchyNumber(world);
        if (currentAnarchy != -1 && (currentIdx = servers.indexOf(currentServerCmd = "/an" + currentAnarchy)) != -1) {
            this.currentServerIndex = currentIdx;
        }
        this.currentServerIndex = (this.currentServerIndex + 1) % servers.size();
        return servers.get(this.currentServerIndex);
    }

    public void switchToNextServer(LocalPlayer player, NetworkManager networkManager, String serverType) {
        if (!this.serverSwitchCooldown.hasTimeElapsed(3000L)) {
            return;
        }
        List<String> availableServers = this.getAvailableServers(serverType);
        if (availableServers == null || availableServers.isEmpty()) {
            return;
        }
        ClientLevel world = (ClientLevel)player.level();
        String newServer = this.getNextServer(availableServers, world);
        if (newServer != null) {
            this.currentServer = newServer;
            player.connection.sendCommand(newServer.substring(1));
            networkManager.sendServerSwitch(newServer);
            this.waitingForServerLoad = true;
            this.serverSwitchCooldown.resetCounter();
        }
    }

    public void joinAnarchyFromHub(LocalPlayer player, String serverType) {
        List<String> availableServers = this.getAvailableServers(serverType);
        if (availableServers == null || availableServers.isEmpty()) {
            return;
        }
        String server = availableServers.get(0);
        player.connection.sendCommand(server.substring(1));
        this.waitingForServerLoad = true;
        this.hubCheckTimer.resetCounter();
    }

    private List<String> getAvailableServers(String serverType) {
        if (serverType.equals("1.21.4")) {
            return new ArrayList<String>(this.anarchyServers214);
        }
        if (serverType.equals("1.16.5")) {
            return new ArrayList<String>(this.anarchyServers165);
        }
        return null;
    }

    public boolean shouldJoinAnarchy(String serverType) {
        boolean hasServerType = serverType.equals("1.16.5") || serverType.equals("1.21.4");
        return this.inHub && this.hubCheckTimer.hasTimeElapsed(3000L) && hasServerType;
    }

    public boolean isInHub() {
        return this.inHub;
    }

    public boolean isWaitingForServerLoad() {
        return this.waitingForServerLoad;
    }

    public void setWaitingForServerLoad(boolean value) {
        this.waitingForServerLoad = value;
    }

    public String getCurrentServer() {
        return this.currentServer;
    }
}

