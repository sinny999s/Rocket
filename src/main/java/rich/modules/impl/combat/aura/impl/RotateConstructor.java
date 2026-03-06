
package rich.modules.impl.combat.aura.impl;

import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.modules.impl.combat.aura.Angle;

public abstract class RotateConstructor
implements IMinecraft {
    private final String name;

    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle) {
        return this.limitAngleChange(currentAngle, targetAngle, null, null);
    }

    public Angle limitAngleChange(Angle currentAngle, Angle targetAngle, Vec3 vec3d) {
        return this.limitAngleChange(currentAngle, targetAngle, vec3d, null);
    }

    public abstract Angle limitAngleChange(Angle var1, Angle var2, Vec3 var3, Entity var4);

    public abstract Vec3 randomValue();

    @Generated
    public String getName() {
        return this.name;
    }

    @Generated
    public RotateConstructor(String name) {
        this.name = name;
    }
}

