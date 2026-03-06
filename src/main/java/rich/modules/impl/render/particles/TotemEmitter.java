
package rich.modules.impl.render.particles;

import lombok.Generated;
import net.minecraft.world.entity.Entity;

public class TotemEmitter {
    private final Entity entity;
    private final int maxAge;
    private int age;

    public TotemEmitter(Entity entity, int maxAge) {
        this.entity = entity;
        this.maxAge = maxAge;
        this.age = 0;
    }

    public void tick() {
        ++this.age;
    }

    public boolean isAlive() {
        return this.age < this.maxAge && this.entity != null && !this.entity.isRemoved();
    }

    public float getProgress() {
        return (float)this.age / (float)this.maxAge;
    }

    @Generated
    public Entity getEntity() {
        return this.entity;
    }

    @Generated
    public int getMaxAge() {
        return this.maxAge;
    }

    @Generated
    public int getAge() {
        return this.age;
    }
}

