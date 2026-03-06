
package rich.events.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import rich.events.api.events.Event;

public record BlockBreakingEvent(BlockPos blockPos, Direction direction) implements Event
{
}

