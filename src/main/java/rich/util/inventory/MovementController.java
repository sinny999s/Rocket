
package rich.util.inventory;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class MovementController {
    private static final Minecraft mc = Minecraft.getInstance();
    private boolean forward;
    private boolean back;
    private boolean left;
    private boolean right;
    private boolean jump;
    private boolean sprint;
    private boolean saved = false;
    private boolean blocked = false;

    public void saveState() {
        if (MovementController.mc.player == null) {
            return;
        }
        this.forward = this.isKeyPressed(MovementController.mc.options.keyUp);
        this.back = this.isKeyPressed(MovementController.mc.options.keyDown);
        this.left = this.isKeyPressed(MovementController.mc.options.keyLeft);
        this.right = this.isKeyPressed(MovementController.mc.options.keyRight);
        this.jump = this.isKeyPressed(MovementController.mc.options.keyJump);
        this.sprint = MovementController.mc.player.isSprinting();
        this.saved = true;
    }

    public void block() {
        if (MovementController.mc.player == null) {
            return;
        }
        MovementController.mc.options.keyUp.setDown(false);
        MovementController.mc.options.keyDown.setDown(false);
        MovementController.mc.options.keyLeft.setDown(false);
        MovementController.mc.options.keyRight.setDown(false);
        MovementController.mc.options.keyJump.setDown(false);
        MovementController.mc.options.keySprint.setDown(false);
        this.blocked = true;
    }

    public void stopSprint() {
        if (MovementController.mc.player != null) {
            MovementController.mc.player.setSprinting(false);
            MovementController.mc.options.keySprint.setDown(false);
        }
    }

    public void restore() {
        if (!this.saved) {
            return;
        }
        MovementController.mc.options.keyUp.setDown(this.forward && this.isCurrentlyPressed(MovementController.mc.options.keyUp));
        MovementController.mc.options.keyDown.setDown(this.back && this.isCurrentlyPressed(MovementController.mc.options.keyDown));
        MovementController.mc.options.keyLeft.setDown(this.left && this.isCurrentlyPressed(MovementController.mc.options.keyLeft));
        MovementController.mc.options.keyRight.setDown(this.right && this.isCurrentlyPressed(MovementController.mc.options.keyRight));
        MovementController.mc.options.keyJump.setDown(this.jump && this.isCurrentlyPressed(MovementController.mc.options.keyJump));
        this.blocked = false;
        this.saved = false;
    }

    public void restoreFromCurrent() {
        MovementController.mc.options.keyUp.setDown(this.isCurrentlyPressed(MovementController.mc.options.keyUp));
        MovementController.mc.options.keyDown.setDown(this.isCurrentlyPressed(MovementController.mc.options.keyDown));
        MovementController.mc.options.keyLeft.setDown(this.isCurrentlyPressed(MovementController.mc.options.keyLeft));
        MovementController.mc.options.keyRight.setDown(this.isCurrentlyPressed(MovementController.mc.options.keyRight));
        MovementController.mc.options.keyJump.setDown(this.isCurrentlyPressed(MovementController.mc.options.keyJump));
        MovementController.mc.options.keySprint.setDown(this.isCurrentlyPressed(MovementController.mc.options.keySprint));
        this.blocked = false;
    }

    public boolean isPlayerStopped(double threshold) {
        if (MovementController.mc.player == null) {
            return true;
        }
        double vx = Math.abs(MovementController.mc.player.getDeltaMovement().x);
        double vz = Math.abs(MovementController.mc.player.getDeltaMovement().z);
        return vx < threshold && vz < threshold;
    }

    public boolean isBlocked() {
        return this.blocked;
    }

    public void reset() {
        this.saved = false;
        this.blocked = false;
    }

    private boolean isKeyPressed(KeyMapping key) {
        return key.isDown();
    }

    private boolean isCurrentlyPressed(KeyMapping key) {
        return InputConstants.isKeyDown((Window)mc.getWindow(), (int)InputConstants.getKey((String)key.saveString()).getValue());
    }
}

