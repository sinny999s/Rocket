
package rich.modules.impl.combat.macetarget.flight;

import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.macetarget.prediction.TargetPredictor;
import rich.modules.impl.combat.macetarget.state.MaceState;

public class FlightController {
    private static final Minecraft mc = Minecraft.getInstance();
    private final TargetPredictor predictor;
    private boolean predictionEnabled = false;
    private float height = 30.0f;

    public FlightController(TargetPredictor predictor) {
        this.predictor = predictor;
    }

    public void setPredictionEnabled(boolean enabled) {
        this.predictionEnabled = enabled;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public Vec3 getTargetPosition(LivingEntity target, MaceState.Stage stage) {
        if (target == null) {
            return Vec3.ZERO;
        }
        if (this.predictionEnabled && this.predictor.isMoving()) {
            return this.predictor.getPredictedPosition(target, stage);
        }
        return target.getEyePosition();
    }

    public Angle calculateAngle(LivingEntity target, MaceState.Stage stage) {
        if (target == null || FlightController.mc.player == null) {
            return MathAngle.cameraAngle();
        }
        Vec3 targetPos = this.getTargetPosition(target, stage);
        switch (stage) {
            case FLYING_UP: {
                Vec3 flyTarget = targetPos.add(0.0, this.height, 0.0);
                return MathAngle.fromVec3d(flyTarget.subtract(FlightController.mc.player.getEyePosition()));
            }
            case TARGETTING: 
            case ATTACKING: {
                return MathAngle.fromVec3d(targetPos.subtract(FlightController.mc.player.getEyePosition()));
            }
        }
        return MathAngle.cameraAngle();
    }

    @Generated
    public TargetPredictor getPredictor() {
        return this.predictor;
    }

    @Generated
    public boolean isPredictionEnabled() {
        return this.predictionEnabled;
    }

    @Generated
    public float getHeight() {
        return this.height;
    }
}

