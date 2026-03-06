
package rich.events.impl;

import lombok.Generated;
import rich.events.api.events.callables.EventCancellable;
import rich.modules.impl.combat.aura.Angle;

public class CameraEvent
extends EventCancellable {
    private boolean cameraClip;
    private float distance;
    private Angle angle;

    @Generated
    public boolean isCameraClip() {
        return this.cameraClip;
    }

    @Generated
    public float getDistance() {
        return this.distance;
    }

    @Generated
    public Angle getAngle() {
        return this.angle;
    }

    @Generated
    public void setCameraClip(boolean cameraClip) {
        this.cameraClip = cameraClip;
    }

    @Generated
    public void setDistance(float distance) {
        this.distance = distance;
    }

    @Generated
    public void setAngle(Angle angle) {
        this.angle = angle;
    }

    @Generated
    public CameraEvent(boolean cameraClip, float distance, Angle angle) {
        this.cameraClip = cameraClip;
        this.distance = distance;
        this.angle = angle;
    }
}

