
package rich.util.player;

import net.minecraft.world.phys.Vec3;

public interface Simulation {
    public Vec3 pos();

    public void tick();
}

