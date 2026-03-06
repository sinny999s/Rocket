
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.Entity;
import rich.events.api.events.Event;

public class AttackEvent
implements Event {
    private final Entity target;

    @Generated
    public Entity getTarget() {
        return this.target;
    }

    @Generated
    public AttackEvent(Entity target) {
        this.target = target;
    }
}

