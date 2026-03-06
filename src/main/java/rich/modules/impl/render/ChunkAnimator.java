
package rich.modules.impl.render;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;

public class ChunkAnimator
extends ModuleStructure {
    private static ChunkAnimator instance;
    private final SliderSettings speed = new SliderSettings("Speed", "").range(1, 20).setValue(10.0f);

    public ChunkAnimator() {
        super("Chunk Animator", "Animates appearing chunks", ModuleCategory.RENDER);
        instance = this;
        this.settings(this.speed);
    }

    public static ChunkAnimator getInstance() {
        return instance;
    }

    public float getSpeed() {
        return this.speed.getValue();
    }
}

