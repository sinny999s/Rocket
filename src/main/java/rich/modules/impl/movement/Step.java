package rich.modules.impl.movement;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.Identifier;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;

public class Step extends ModuleStructure {

    private static final Identifier STEP_MODIFIER_ID = Identifier.fromNamespaceAndPath("rich", "step_height");

    private final SliderSettings height = new SliderSettings("Height", "Step height in blocks")
            .range(1.0f, 2.5f).setValue(1.0f);

    public Step() {
        super("Step", "Step up blocks without jumping", ModuleCategory.MOVEMENT);
        this.settings(this.height);
    }

    public static Step getInstance() {
        return Instance.get(Step.class);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!this.state || Step.mc.player == null) return;
        if (Step.mc.player.isInWater() || Step.mc.player.isInLava() || Step.mc.player.isFallFlying()) return;

        AttributeInstance attr = Step.mc.player.getAttribute(Attributes.STEP_HEIGHT);
        if (attr == null) return;

        attr.removeModifier(STEP_MODIFIER_ID);
        double extra = this.height.getValue() - 0.6;
        if (extra > 0) {
            attr.addTransientModifier(new AttributeModifier(STEP_MODIFIER_ID, extra, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void deactivate() {
        if (Step.mc.player != null) {
            AttributeInstance attr = Step.mc.player.getAttribute(Attributes.STEP_HEIGHT);
            if (attr != null) {
                attr.removeModifier(STEP_MODIFIER_ID);
            }
        }
    }
}
