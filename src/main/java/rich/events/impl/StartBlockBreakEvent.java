package rich.events.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import rich.events.api.events.callables.EventCancellable;

public class StartBlockBreakEvent extends EventCancellable {
    private final BlockPos blockPos;
    private final Direction direction;

    public StartBlockBreakEvent(BlockPos blockPos, Direction direction) {
        this.blockPos = blockPos;
        this.direction = direction;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public Direction getDirection() {
        return this.direction;
    }
}
