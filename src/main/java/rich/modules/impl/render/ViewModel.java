
package rich.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import rich.events.api.EventHandler;
import rich.events.impl.HandOffsetEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;

public class ViewModel
extends ModuleStructure {
    private final SliderSettings mainHandXSetting = new SliderSettings("Main hand X", "Main hand X value setting").setValue(0.0f).range(-1.0f, 1.0f);
    private final SliderSettings mainHandYSetting = new SliderSettings("Main hand Y", "Main hand Y value setting").setValue(0.0f).range(-1.0f, 1.0f);
    private final SliderSettings mainHandZSetting = new SliderSettings("Main hand Z", "Main hand Z value setting").setValue(0.0f).range(-2.5f, 2.5f);
    private final SliderSettings offHandXSetting = new SliderSettings("Offhand X", "Offhand X value setting").setValue(0.0f).range(-1.0f, 1.0f);
    private final SliderSettings offHandYSetting = new SliderSettings("Offhand Y", "Offhand Y value setting").setValue(0.0f).range(-1.0f, 1.0f);
    private final SliderSettings offHandZSetting = new SliderSettings("Offhand Z", "Offhand Z value setting").setValue(0.0f).range(-2.5f, 2.5f);

    public ViewModel() {
        super("ViewModel", "View Model", ModuleCategory.RENDER);
        this.settings(this.mainHandXSetting, this.mainHandYSetting, this.mainHandZSetting, this.offHandXSetting, this.offHandYSetting, this.offHandZSetting);
    }

    @EventHandler
    public void onHandOffset(HandOffsetEvent e) {
        InteractionHand hand = e.getHand();
        if (hand.equals((Object)InteractionHand.MAIN_HAND) && e.getStack().getItem() instanceof CrossbowItem) {
            return;
        }
        PoseStack matrix = e.getMatrices();
        if (hand.equals((Object)InteractionHand.MAIN_HAND)) {
            matrix.translate(this.mainHandXSetting.getValue(), this.mainHandYSetting.getValue(), this.mainHandZSetting.getValue());
        } else {
            matrix.translate(this.offHandXSetting.getValue(), this.offHandYSetting.getValue(), this.offHandZSetting.getValue());
        }
    }
}

