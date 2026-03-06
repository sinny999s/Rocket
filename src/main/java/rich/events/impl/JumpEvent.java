
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.player.Player;
import rich.events.api.events.callables.EventCancellable;

public class JumpEvent
extends EventCancellable {
    private Player player;

    @Generated
    public Player getPlayer() {
        return this.player;
    }

    @Generated
    public JumpEvent(Player player) {
        this.player = player;
    }
}

