
package rich.events.impl;

import lombok.Generated;
import net.minecraft.world.entity.player.Input;
import rich.events.api.events.callables.EventCancellable;

public class InputEvent
extends EventCancellable {
    private Input input;

    public void setJumping(boolean jump) {
        this.input = new Input(this.input.forward(), this.input.backward(), this.input.left(), this.input.right(), jump, this.input.shift(), this.input.sprint());
    }

    public void setSprinting(boolean sprint) {
        this.input = new Input(this.input.forward(), this.input.backward(), this.input.left(), this.input.right(), this.input.jump(), this.input.shift(), sprint);
    }

    public void setDirectional(boolean forward, boolean backward, boolean left, boolean right, boolean sneak, boolean sprint, boolean jump) {
        this.input = new Input(forward, backward, left, right, jump, sneak, sprint);
    }

    public void setDirectionalLow(boolean forward, boolean backward, boolean left, boolean right) {
        this.input = new Input(forward, backward, left, right, this.input.jump(), this.input.shift(), this.input.sprint());
    }

    public void inputNone() {
        this.input = new Input(false, false, false, false, false, false, false);
    }

    public int forward() {
        return this.input.forward() ? 1 : (this.input.backward() ? -1 : 0);
    }

    public float sideways() {
        return this.input.left() ? 1.0f : (this.input.right() ? -1.0f : 0.0f);
    }

    @Generated
    public Input getInput() {
        return this.input;
    }

    @Generated
    public void setInput(Input input) {
        this.input = input;
    }

    @Generated
    public InputEvent(Input input) {
        this.input = input;
    }
}

