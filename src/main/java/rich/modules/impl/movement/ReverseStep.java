package rich.modules.impl.movement;

import net.minecraft.world.entity.Entity;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;

public class ReverseStep extends ModuleStructure {

    private final SliderSettings fallSpeed = new SliderSettings("Fall Speed", "How fast to fall")
            .range(1.0f, 10.0f).setValue(3.0f);
    private final SliderSettings fallDistance = new SliderSettings("Fall Distance", "Max fall distance to activate")
            .range(1.0f, 10.0f).setValue(3.0f);

    public ReverseStep() {
        super("Reverse Step", "Fall down blocks faster", ModuleCategory.MOVEMENT);
        this.settings(this.fallSpeed, this.fallDistance);
    }

    public static ReverseStep getInstance() {
        return Instance.get(ReverseStep.class);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!this.state || ReverseStep.mc.player == null || ReverseStep.mc.level == null) return;
        if (ReverseStep.mc.player.onClimbable()) return;

        Entity entity = ReverseStep.mc.player;
        if (!entity.onGround() || entity.isInWater() || entity.isInLava()
                || ReverseStep.mc.options.keyJump.isDown() || entity.noPhysics) {
            return;
        }

        if (!ReverseStep.mc.level.noCollision(entity.getBoundingBox().move(0.0, -(this.fallDistance.getValue() + 0.01), 0.0))) {
            entity.setDeltaMovement(entity.getDeltaMovement().x, -this.fallSpeed.getValue(), entity.getDeltaMovement().z);
        }
    }
}
