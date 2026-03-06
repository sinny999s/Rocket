
package rich.util.repository.way;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.DrawEvent;
import rich.util.config.impl.way.WayConfig;
import rich.util.math.Projection;
import rich.util.render.Render2D;
import rich.util.render.font.Font;
import rich.util.render.font.Fonts;
import rich.util.repository.way.Way;

public class WayRepository
implements IMinecraft {
    private static WayRepository instance;
    private final List<Way> wayList = new ArrayList<Way>();

    public WayRepository() {
        instance = this;
    }

    public static WayRepository getInstance() {
        if (instance == null) {
            instance = new WayRepository();
        }
        return instance;
    }

    public void init() {
        EventManager.register(this);
        WayConfig.getInstance().load();
    }

    public boolean isEmpty() {
        return this.wayList.isEmpty();
    }

    public void addWay(String name, BlockPos pos, String server, String dimension) {
        this.wayList.add(new Way(name, pos, server, dimension));
    }

    public void addWayAndSave(String name, BlockPos pos, String server, String dimension) {
        this.addWay(name, pos, server, dimension);
        WayConfig.getInstance().save();
    }

    public boolean hasWay(String name) {
        return this.wayList.stream().anyMatch(way -> way.name().equalsIgnoreCase(name));
    }

    public Optional<Way> getWay(String name) {
        return this.wayList.stream().filter(way -> way.name().equalsIgnoreCase(name)).findFirst();
    }

    public void deleteWay(String name) {
        this.wayList.removeIf(way -> way.name().equalsIgnoreCase(name));
    }

    public void deleteWayAndSave(String name) {
        this.deleteWay(name);
        WayConfig.getInstance().save();
    }

    public void clearList() {
        this.wayList.clear();
    }

    public void clearListAndSave() {
        this.clearList();
        WayConfig.getInstance().save();
    }

    public int size() {
        return this.wayList.size();
    }

    public List<String> getWayNames() {
        return this.wayList.stream().map(Way::name).collect(Collectors.toList());
    }

    public List<String> getWayNamesForServer(String server) {
        return this.wayList.stream().filter(way -> way.server().equalsIgnoreCase(server)).map(Way::name).collect(Collectors.toList());
    }

    public void setWays(List<Way> ways) {
        this.wayList.clear();
        this.wayList.addAll(ways);
    }

    public String getCurrentServer() {
        if (mc.getConnection() == null || mc.getConnection().getServerData() == null) {
            return "";
        }
        return WayRepository.mc.getConnection().getServerData().ip;
    }

    private boolean isInFrontOfCamera(Vec3 worldPos) {
        double lookZ;
        double lookY;
        Camera camera = WayRepository.mc.gameRenderer.getMainCamera();
        if (camera == null || !camera.isInitialized()) {
            return false;
        }
        Vec3 cameraPos = camera.position();
        Vec3 toPoint = worldPos.subtract(cameraPos);
        float yaw = camera.yRot();
        float pitch = camera.xRot();
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double lookX = -Math.sin(yawRad) * Math.cos(pitchRad);
        Vec3 lookDir = new Vec3(lookX, lookY = -Math.sin(pitchRad), lookZ = Math.cos(yawRad) * Math.cos(pitchRad));
        return lookDir.dot(toPoint) > 0.0;
    }

    @EventHandler
    public void onRender2D(DrawEvent event) {
        if (this.isEmpty() || WayRepository.mc.player == null || WayRepository.mc.level == null) {
            return;
        }
        if (mc.getConnection() == null || mc.getConnection().getServerData() == null) {
            return;
        }
        String currentServer = this.getCurrentServer();
        String currentDim = getCurrentDimension();
        for (Way way : this.wayList) {
            if (!way.server().equalsIgnoreCase(currentServer)) continue;
            Vec3 wayVec = getConvertedPos(way, currentDim);
            if (!this.isInFrontOfCamera(wayVec)) continue;
            Vec3 screenPos = Projection.worldSpaceToScreenSpace(wayVec);
            if (screenPos.z <= 0.0 || screenPos.z >= 1.0) continue;
            double distance = WayRepository.mc.player.position().distanceTo(wayVec);
            String dimLabel = getDimLabel(way.dimension(), currentDim);
            String text = way.name() + dimLabel + " - " + String.format("%.1f", distance) + "m";
            Font font = Fonts.BOLD;
            float fontSize = 6.0f;
            float textWidth = font.getWidth(text, fontSize);
            float textHeight = font.getHeight(fontSize);
            float padding = 3.0f;
            float x = (float)screenPos.x - textWidth / 2.0f;
            float y = (float)screenPos.y - textHeight / 2.0f;
            Render2D.rect(x - padding, y - padding + 0.5f, textWidth + padding * 2.0f, textHeight + padding * 2.0f, -535620843, 2.0f);
            font.drawCentered(text, (float)screenPos.x, y + 1.0f, fontSize, -1);
        }
    }

    public static String getCurrentDimension() {
        if (mc.level == null) return "overworld";
        String dim = mc.level.dimension().identifier().getPath();
        if (dim.contains("nether")) return "nether";
        if (dim.contains("end")) return "the_end";
        return "overworld";
    }

    private Vec3 getConvertedPos(Way way, String currentDim) {
        BlockPos pos = way.pos();
        String wayDim = way.dimension() != null ? way.dimension() : "overworld";
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;

        if (wayDim.equals("nether") && currentDim.equals("overworld")) {
            x = pos.getX() * 8 + 0.5;
            z = pos.getZ() * 8 + 0.5;
        } else if (wayDim.equals("overworld") && currentDim.equals("nether")) {
            x = pos.getX() / 8.0 + 0.5;
            z = pos.getZ() / 8.0 + 0.5;
        }

        return new Vec3(x, y, z);
    }

    private String getDimLabel(String wayDim, String currentDim) {
        if (wayDim == null || wayDim.equals(currentDim)) return "";
        if (wayDim.equals("nether")) return " [Nether]";
        if (wayDim.equals("the_end")) return " [End]";
        return " [OW]";
    }

    @Generated
    public List<Way> getWayList() {
        return this.wayList;
    }
}

