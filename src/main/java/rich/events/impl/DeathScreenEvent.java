
package rich.events.impl;

import lombok.Generated;
import rich.events.api.events.Event;

public class DeathScreenEvent
implements Event {
    private int ticksSinceDeath;

    @Generated
    public int getTicksSinceDeath() {
        return this.ticksSinceDeath;
    }

    @Generated
    public DeathScreenEvent(int ticksSinceDeath) {
        this.ticksSinceDeath = ticksSinceDeath;
    }
}

