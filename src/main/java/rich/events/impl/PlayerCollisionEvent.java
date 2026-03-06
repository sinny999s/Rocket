
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.level.block.Block;
import rich.events.api.events.callables.EventCancellable;

public class PlayerCollisionEvent
extends EventCancellable {
    private Block block;

    @Generated
    public void setBlock(Block block) {
        this.block = block;
    }

    @Generated
    public Block getBlock() {
        return this.block;
    }

    @Generated
    public PlayerCollisionEvent(Block block) {
        this.block = block;
    }
}

