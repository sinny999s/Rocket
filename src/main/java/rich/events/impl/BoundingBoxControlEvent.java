
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import rich.events.api.events.callables.EventCancellable;

public class BoundingBoxControlEvent
extends EventCancellable {
    public AABB box;
    public Entity entity;

    @Generated
    public AABB getBox() {
        return this.box;
    }

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

    @Generated
    public void setBox(AABB box) {
        this.box = box;
    }

    @Generated
    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Generated
    public BoundingBoxControlEvent(AABB box, Entity entity) {
        this.box = box;
        this.entity = entity;
    }
}

