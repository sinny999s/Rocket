
package rich.modules.impl.combat.macetarget.state;

public class MaceState {

    public static enum FireworkPhase {
        IDLE,
        PRE_STOP,
        STOPPING,
        WAIT_STOP,
        PRE_SWAP,
        SWAP_TO_HAND,
        AWAIT_ITEM,
        USE,
        POST_USE,
        SWAP_BACK,
        RESUMING;

    }

    public static enum SwapPhase {
        IDLE,
        PRE_STOP,
        STOPPING,
        WAIT_STOP,
        PRE_SWAP,
        DO_SWAP,
        POST_SWAP,
        RESUMING;

    }

    public static enum Stage {
        PREPARE,
        FLYING_UP,
        TARGETTING,
        ATTACKING;

    }
}

