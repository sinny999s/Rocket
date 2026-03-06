/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.llamalad7.mixinextras.injector.ModifyExpressionValue
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 */
package rich.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import rich.IMinecraft;
import rich.events.api.EventManager;
import rich.events.impl.InputEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.AngleConstructor;

@Mixin(value={KeyboardInput.class})
public class KeyboardInputMixin {
    @ModifyExpressionValue(method={"tick"}, at={@At(value="NEW", target="(ZZZZZZZ)Lnet/minecraft/world/entity/player/Input;")})
    private Input tickHook(Input original) {
        InputEvent event = new InputEvent(original);
        EventManager.callEvent(event);
        return this.transformInput(event.getInput());
    }

    @Unique
    private Input transformInput(Input input) {
        AngleConnection rotationController = AngleConnection.INSTANCE;
        Angle angle = rotationController.getCurrentAngle();
        AngleConstructor configurable = rotationController.getCurrentRotationPlan();
        if (IMinecraft.mc.player == null || angle == null || configurable == null || !configurable.isMoveCorrection() || !configurable.isFreeCorrection()) {
            return input;
        }
        float deltaYaw = IMinecraft.mc.player.getYRot() - angle.getYaw();
        float z = KeyboardInput.calculateImpulse((boolean)input.forward(), (boolean)input.backward());
        float x = KeyboardInput.calculateImpulse((boolean)input.left(), (boolean)input.right());
        float newX = x * Mth.cos((double)(deltaYaw * ((float)Math.PI / 180))) - z * Mth.sin((double)(deltaYaw * ((float)Math.PI / 180)));
        float newZ = z * Mth.cos((double)(deltaYaw * ((float)Math.PI / 180))) + x * Mth.sin((double)(deltaYaw * ((float)Math.PI / 180)));
        int movementSideways = Math.round(newX);
        int movementForward = Math.round(newZ);
        return new Input((float)movementForward > 0.0f, (float)movementForward < 0.0f, (float)movementSideways > 0.0f, (float)movementSideways < 0.0f, input.jump(), input.shift(), input.sprint());
    }
}

