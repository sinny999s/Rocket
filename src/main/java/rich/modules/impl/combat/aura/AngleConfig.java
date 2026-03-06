
package rich.modules.impl.combat.aura;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConstructor;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.impl.combat.aura.impl.RotateConstructor;

public class AngleConfig {
    public static AngleConfig DEFAULT = new AngleConfig(new LinearConstructor(), true, true);
    public static boolean moveCorrection;
    public static boolean freeCorrection;
    private final RotateConstructor angleSmooth;
    private final int resetThreshold = 1;

    public AngleConfig(boolean moveCorrection, boolean freeCorrection) {
        this(new LinearConstructor(), moveCorrection, freeCorrection);
    }

    public AngleConfig(boolean moveCorrection) {
        this(new LinearConstructor(), moveCorrection, true);
    }

    public AngleConfig(RotateConstructor angleSmooth, boolean moveCorrection, boolean freeCorrection) {
        this.angleSmooth = angleSmooth;
        AngleConfig.moveCorrection = moveCorrection;
        AngleConfig.freeCorrection = freeCorrection;
    }

    public AngleConstructor createRotationPlan(Angle angle, Vec3 vec, Entity entity, int reset) {
        return new AngleConstructor(angle, vec, entity, this.angleSmooth, reset, 1.0f, moveCorrection, freeCorrection);
    }

    public AngleConstructor createRotationPlan(Angle angle, Vec3 vec, Entity entity, boolean moveCorrection, boolean freeCorrection) {
        return new AngleConstructor(angle, vec, entity, this.angleSmooth, 1, 1.0f, moveCorrection, freeCorrection);
    }
}

