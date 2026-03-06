
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.Instance;
import rich.util.math.TaskPriority;

public class AutoPilot
extends ModuleStructure {
    private static final Minecraft mc = Minecraft.getInstance();
    public ItemEntity target;
    private float lastYaw;
    private float lastPitch;
    private float targetYaw;
    private float targetPitch;
    Angle rot = new Angle(0.0f, 0.0f);

    public AutoPilot() {
        super("AutoPilot", "Auto Pilot", ModuleCategory.MISC);
    }

    public static AutoPilot getInstance() {
        return Instance.get(AutoPilot.class);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent event) {
        if (AutoPilot.mc.player == null || AutoPilot.mc.level == null || mc.getConnection() == null) {
            this.target = null;
            return;
        }
        this.target = this.findTarget();
        if (this.target != null) {
            this.processRotation();
        } else {
            this.lastYaw = AutoPilot.mc.player.getYRot();
            this.lastPitch = AutoPilot.mc.player.getXRot();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processRotation() {
        double dx = this.target.position().x() - AutoPilot.mc.player.position().x();
        double dy = this.target.position().y() - (AutoPilot.mc.player.position().y() + (double)AutoPilot.mc.player.getEyeHeight(AutoPilot.mc.player.getPose()));
        double dz = this.target.position().z() - AutoPilot.mc.player.position().z();
        this.targetYaw = (float)(Math.atan2(dz, dx) * 180.0 / Math.PI - 90.0);
        this.targetPitch = (float)(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180.0 / Math.PI);
        float maxRotation = 1024.0f;
        float yawDiff = Mth.wrapDegrees((float)(this.targetYaw - this.lastYaw));
        float yawStep = Mth.clamp((float)yawDiff, (float)(-maxRotation), (float)maxRotation);
        this.lastYaw += yawStep;
        float pitchDiff = Mth.wrapDegrees((float)(this.targetPitch - this.lastPitch));
        float pitchStep = Mth.clamp((float)pitchDiff, (float)(-maxRotation), (float)maxRotation);
        this.lastPitch += pitchStep;
        AutoPilot.mc.player.setYRot(this.lastYaw);
        AutoPilot.mc.player.setXRot(this.lastPitch);
        this.rot.setYaw(this.lastYaw);
        this.rot.setPitch(this.lastPitch);
        AngleConnection.INSTANCE.rotateTo(this.rot, AngleConfig.DEFAULT, TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private ItemEntity findTarget() {
        List items = AutoPilot.mc.level.getEntitiesOfClass(ItemEntity.class, AutoPilot.mc.player.getBoundingBox().inflate(50.0), e -> e.isAlive() && this.isValidItem((ItemEntity)e)).stream().sorted(Comparator.comparingDouble(e -> AutoPilot.mc.player.distanceToSqr((Entity)e))).collect(Collectors.toList());
        return items.isEmpty() ? null : (ItemEntity)items.get(0);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isValidItem(ItemEntity item) {
        ItemStack stack = item.getItem();
        return stack.getItem() == Items.SPAWNER || stack.getItem() == Items.PLAYER_HEAD || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE || stack.getItem().toString().contains("_spawn_egg");
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        this.target = null;
        if (AutoPilot.mc.player != null) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.PosRot(AutoPilot.mc.player.position().x(), AutoPilot.mc.player.position().y(), AutoPilot.mc.player.position().z(), AutoPilot.mc.player.getYRot(), AutoPilot.mc.player.getXRot(), AutoPilot.mc.player.onGround(), false));
        }
    }
}

