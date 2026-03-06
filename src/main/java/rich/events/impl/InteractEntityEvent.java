
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.Entity;
import rich.events.api.events.callables.EventCancellable;

public class InteractEntityEvent
extends EventCancellable {
    private Entity entity;

    @Generated
    public InteractEntityEvent(Entity entity) {
        this.entity = entity;
    }

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

    @Generated
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}

