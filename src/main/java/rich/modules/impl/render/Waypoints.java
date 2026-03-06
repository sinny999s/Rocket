package rich.modules.impl.render;

import java.text.SimpleDateFormat;
import java.util.Date;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ButtonSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.Instance;
import rich.util.repository.way.WayRepository;
import rich.util.string.chat.ChatMessage;

public class Waypoints extends ModuleStructure {

    public final BooleanSetting deathWaypoints = new BooleanSetting("Death Waypoints", "Save waypoint on death").setValue(true);
    public final SliderSettings maxDeathWaypoints = new SliderSettings("Max Deaths", "Maximum death waypoints to keep").setValue(5.0f).range(1, 20).visible(() -> this.deathWaypoints.isValue());
    public final BooleanSetting deathChat = new BooleanSetting("Death Chat", "Send death position in chat").setValue(true).visible(() -> this.deathWaypoints.isValue());
    public final SliderSettings fadeDistance = new SliderSettings("Fade Distance", "Distance at which waypoints start fading").setValue(10.0f).range(0, 50);
    public final SliderSettings maxDistance = new SliderSettings("Max Distance", "Maximum render distance for waypoints (0 = unlimited)").setValue(0.0f).range(0, 5000);
    public final TextSetting waypointName = new TextSetting("Name", "Type a waypoint name").setText("").setMax(32);
    public final ButtonSetting createButton = new ButtonSetting("Create", "Create waypoint at current position").setButtonName("Create").setRunnable(this::createWaypoint);

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private boolean wasDead = false;

    public static Waypoints getInstance() {
        return Instance.get(Waypoints.class);
    }

    public Waypoints() {
        super("Waypoints", "Waypoints", ModuleCategory.RENDER);
        this.settings(this.waypointName, this.createButton, this.deathWaypoints, this.maxDeathWaypoints, this.deathChat, this.fadeDistance, this.maxDistance);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null) {
            wasDead = false;
            return;
        }

        if (mc.player.isDeadOrDying() && !wasDead) {
            wasDead = true;
            onDeath();
        } else if (!mc.player.isDeadOrDying()) {
            wasDead = false;
        }
    }

    private void onDeath() {
        if (!deathWaypoints.isValue() || mc.player == null) return;

        Vec3 deathPos = mc.player.position();
        String time = dateFormat.format(new Date());

        if (deathChat.isValue()) {
            ChatMessage.brandmessage(String.format("Died at %d, %d, %d at %s",
                (int) deathPos.x, (int) deathPos.y, (int) deathPos.z, time));
        }

        WayRepository repo = WayRepository.getInstance();
        if (repo == null) return;

        String server = repo.getCurrentServer();
        if (server.isEmpty()) return;

        String name = "Death " + time;
        BlockPos pos = BlockPos.containing(deathPos);
        String dimension = WayRepository.getCurrentDimension();
        repo.addWayAndSave(name, pos, server, dimension);

        cleanDeathWaypoints();
    }

    private void cleanDeathWaypoints() {
        WayRepository repo = WayRepository.getInstance();
        if (repo == null) return;

        int max = (int) maxDeathWaypoints.getValue();
        int count = 0;

        java.util.List<rich.util.repository.way.Way> toRemove = new java.util.ArrayList<>();
        for (rich.util.repository.way.Way way : repo.getWayList()) {
            if (way.name().startsWith("Death ")) {
                count++;
                if (count > max) {
                    toRemove.add(way);
                }
            }
        }

        for (rich.util.repository.way.Way way : toRemove) {
            repo.deleteWayAndSave(way.name());
        }
    }

    private void createWaypoint() {
        if (mc.player == null || mc.level == null) return;

        String name = waypointName.getText();
        if (name == null || name.trim().isEmpty()) {
            ChatMessage.brandmessage("Enter a waypoint name first!");
            return;
        }
        name = name.trim();

        WayRepository repo = WayRepository.getInstance();
        if (repo == null) return;

        String server = repo.getCurrentServer();
        if (server.isEmpty()) {
            ChatMessage.brandmessage("Could not determine server!");
            return;
        }

        if (repo.hasWay(name)) {
            ChatMessage.brandmessage("Waypoint '" + name + "' already exists!");
            return;
        }

        BlockPos pos = mc.player.blockPosition();
        String dimension = WayRepository.getCurrentDimension();
        repo.addWayAndSave(name, pos, server, dimension);
        ChatMessage.brandmessage("Waypoint '" + name + "' created at " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        waypointName.setText("");
    }

    public float getFadeDistance() {
        return this.isState() ? this.fadeDistance.getValue() : 0;
    }

    public float getMaxDistance() {
        return this.isState() ? this.maxDistance.getValue() : 0;
    }
}
