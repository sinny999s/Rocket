
package rich.modules.impl.movement;

import antidaunleak.api.annotation.Native;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.InputEvent;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.Aura;
import rich.modules.impl.combat.AutoTotem;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;

public class TargetStrafe
extends ModuleStructure {
    private static final Minecraft mc = Minecraft.getInstance();
    public SelectSetting mode = new SelectSetting("Mode", "Strafe type").value("Matrix", "Grim").selected("Matrix");
    SelectSetting type = new SelectSetting("Walk waypoint", "Select waypoint for strafe target").value("Cube", "Center", "Circle").selected("Cube").visible(() -> this.mode.isSelected("Grim"));
    SelectSetting typeMatrix = new SelectSetting("Bypass waypoint", "Select bypass waypoint in Matrix mode").value("Cube", "Circle").selected("Circle").visible(() -> this.mode.isSelected("Matrix"));
    SliderSettings grimRadius = new SliderSettings("Bypass radius", "Bypass radius around target").setValue(0.87f).range(0.1f, 1.5f).visible(() -> this.mode.isSelected("Grim") && (this.type.isSelected("Cube") || this.type.isSelected("Circle")));
    MultiSelectSetting setting = new MultiSelectSetting("Settings", "Allows configuring strafes").value("Auto Jump", "Only Key Pressed", "In front of the target", "Direction Mode").selected("Auto Jump");
    SelectSetting directionMode = new SelectSetting("Direction", "Select bypass direction").value("Clockwise", "Counterclockwise", "Random").selected("Clockwise").visible(() -> this.setting.isSelected("Direction Mode"));
    SliderSettings radius = new SliderSettings("Radius", "Bypass radius around target").setValue(2.5f).range(0.1f, 7.0f).visible(() -> this.mode.isSelected("Matrix"));
    SliderSettings speed = new SliderSettings("Speed", "Strafe speed").setValue(0.3f).range(0.1f, 1.0f).visible(() -> this.mode.isSelected("Matrix"));
    private int grimPointIndex = 0;

    public TargetStrafe() {
        super("TargetStrafe", "Target Strafe", ModuleCategory.MOVEMENT);
        this.settings(this.mode, this.type, this.typeMatrix, this.grimRadius, this.radius, this.speed, this.setting, this.directionMode);
    }

    public static TargetStrafe getInstance() {
        return Instance.get(TargetStrafe.class);
    }

    private boolean isAutoTotemBlocking() {
        AutoTotem autoTotem = Instance.get(AutoTotem.class);
        if (autoTotem == null) {
            return false;
        }
        if (!autoTotem.isState()) {
            return false;
        }
        return autoTotem.getExecutor().isBlocking() || autoTotem.getExecutor().isRunning();
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onInput(InputEvent event) {
        if (TargetStrafe.mc.player == null || TargetStrafe.mc.level == null) {
            return;
        }
        if (this.isAutoTotemBlocking()) {
            return;
        }
        LivingEntity target = Aura.target;
        if (target == null || !target.isAlive()) {
            return;
        }
        if (!this.mode.isSelected("Grim")) {
            return;
        }
        if (!(!this.setting.isSelected("Only Key Pressed") || TargetStrafe.mc.options.keyUp.isDown() || TargetStrafe.mc.options.keyDown.isDown() || TargetStrafe.mc.options.keyLeft.isDown() || TargetStrafe.mc.options.keyRight.isDown())) {
            return;
        }
        Vec3 nextPoint = this.calculateGrimNextPoint(target);
        this.applyGrimMovement(event, nextPoint);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private Vec3 calculateGrimNextPoint(LivingEntity target) {
        Vec3 playerPos = TargetStrafe.mc.player.position();
        Vec3 targetPos = target.position();
        double r = this.grimRadius.getValue();
        int directionMultiplier = this.getDirectionMultiplier();
        if (this.setting.isSelected("In front of the target")) {
            return this.calculateFrontPoint(target, targetPos, r, directionMultiplier);
        }
        return this.calculateNormalPoint(playerPos, targetPos, r, directionMultiplier);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private Vec3 calculateFrontPoint(LivingEntity target, Vec3 targetPos, double r, int directionMultiplier) {
        float targetYaw = target.getYRot();
        if (this.type.isSelected("Center")) {
            return targetPos.add(-Math.sin(Math.toRadians(targetYaw)) * r * (double)directionMultiplier, 0.0, Math.cos(Math.toRadians(targetYaw)) * r * (double)directionMultiplier);
        }
        double offset = Math.cos((double)System.currentTimeMillis() / 500.0) * r * (double)directionMultiplier;
        return targetPos.add(-Math.sin(Math.toRadians(targetYaw)) * r + Math.cos(Math.toRadians(targetYaw)) * offset, 0.0, Math.cos(Math.toRadians(targetYaw)) * r + Math.sin(Math.toRadians(targetYaw)) * offset);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private Vec3 calculateNormalPoint(Vec3 playerPos, Vec3 targetPos, double r, int directionMultiplier) {
        if (this.type.isSelected("Cube")) {
            Vec3[] vec3dArray = new Vec3[]{new Vec3(targetPos.x - r, playerPos.y, targetPos.z - r), new Vec3(targetPos.x - r, playerPos.y, targetPos.z + r), new Vec3(targetPos.x + r, playerPos.y, targetPos.z + r), new Vec3(targetPos.x + r, playerPos.y, targetPos.z - r)};
            Vec3[] points = vec3dArray;
            if (playerPos.distanceTo(points[this.grimPointIndex]) < 0.5) {
                this.grimPointIndex = (this.grimPointIndex + directionMultiplier + points.length) % points.length;
            }
            return points[this.grimPointIndex];
        }
        if (this.type.isSelected("Circle")) {
            double baseAngle = (double)(System.currentTimeMillis() % 3600L) / 3600.0 * 4.0 * Math.PI;
            double angle = directionMultiplier > 0 ? baseAngle : Math.PI * 2 - baseAngle;
            return new Vec3(targetPos.x + Math.cos(angle) * r, playerPos.y, targetPos.z + Math.sin(angle) * r);
        }
        return new Vec3(targetPos.x, playerPos.y, targetPos.z);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void applyGrimMovement(InputEvent event, Vec3 nextPoint) {
        Vec3 playerPos = TargetStrafe.mc.player.position();
        Vec3 direction = nextPoint.subtract(playerPos).normalize();
        float yaw = AngleConnection.INSTANCE.getRotation().getYaw();
        float movementAngle = (float)Math.toDegrees(Math.atan2(direction.z, direction.x)) - 90.0f;
        float angleDiff = Mth.wrapDegrees((float)(movementAngle - yaw));
        boolean forward = false;
        boolean back = false;
        boolean left = false;
        boolean right = false;
        if ((double)angleDiff >= -22.5 && (double)angleDiff < 22.5) {
            forward = true;
        } else if ((double)angleDiff >= 22.5 && (double)angleDiff < 67.5) {
            forward = true;
            right = true;
        } else if ((double)angleDiff >= 67.5 && (double)angleDiff < 112.5) {
            right = true;
        } else if ((double)angleDiff >= 112.5 && (double)angleDiff < 157.5) {
            back = true;
            right = true;
        } else if ((double)angleDiff >= -67.5 && (double)angleDiff < -22.5) {
            forward = true;
            left = true;
        } else if ((double)angleDiff >= -112.5 && (double)angleDiff < -67.5) {
            left = true;
        } else if ((double)angleDiff >= -157.5 && (double)angleDiff < -112.5) {
            back = true;
            left = true;
        } else {
            back = true;
        }
        event.setDirectionalLow(forward, back, left, right);
        if (this.setting.isSelected("Auto Jump") && TargetStrafe.mc.player.onGround()) {
            event.setJumping(true);
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent event) {
        if (TargetStrafe.mc.player == null || TargetStrafe.mc.level == null) {
            return;
        }
        if (this.isAutoTotemBlocking()) {
            return;
        }
        LivingEntity target = Aura.target;
        if (target == null || !target.isAlive()) {
            return;
        }
        if (!this.mode.isSelected("Matrix")) {
            return;
        }
        if (!(!this.setting.isSelected("Only Key Pressed") || TargetStrafe.mc.options.keyUp.isDown() || TargetStrafe.mc.options.keyDown.isDown() || TargetStrafe.mc.options.keyLeft.isDown() || TargetStrafe.mc.options.keyRight.isDown())) {
            return;
        }
        if (this.setting.isSelected("Auto Jump") && TargetStrafe.mc.player.onGround()) {
            TargetStrafe.mc.player.jumpFromGround();
        }
        this.processMatrixStrafe(target);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processMatrixStrafe(LivingEntity target) {
        Vec3 playerPos = TargetStrafe.mc.player.position();
        Vec3 targetPos = target.position();
        double r = this.radius.getValue();
        int directionMultiplier = this.getDirectionMultiplier();
        if (this.setting.isSelected("In front of the target")) {
            this.processMatrixFrontStrafe(target, playerPos, targetPos, r, directionMultiplier);
            return;
        }
        if (this.typeMatrix.isSelected("Cube")) {
            this.processMatrixCubeStrafe(playerPos, targetPos, r, directionMultiplier);
        } else if (this.typeMatrix.isSelected("Circle")) {
            this.processMatrixCircleStrafe(playerPos, targetPos, r, directionMultiplier);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private int getDirectionMultiplier() {
        int directionMultiplier = 1;
        if (this.setting.isSelected("Direction Mode")) {
            if (this.directionMode.isSelected("Counterclockwise")) {
                directionMultiplier = -1;
            } else if (this.directionMode.isSelected("Random")) {
                long time = System.currentTimeMillis() / 3000L;
                directionMultiplier = time % 2L == 0L ? 1 : -1;
            }
        }
        return directionMultiplier;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processMatrixFrontStrafe(LivingEntity target, Vec3 playerPos, Vec3 targetPos, double r, int directionMultiplier) {
        float targetYaw = target.getYRot();
        double x = targetPos.x - Math.sin(Math.toRadians(targetYaw)) * r * (double)directionMultiplier;
        double z = targetPos.z + Math.cos(Math.toRadians(targetYaw)) * r * (double)directionMultiplier;
        float yaw = (float)Math.toDegrees(Math.atan2(z - playerPos.z, x - playerPos.x)) - 90.0f;
        double motionSpeed = this.speed.getValue();
        TargetStrafe.mc.player.setDeltaMovement(-Math.sin(Math.toRadians(yaw)) * motionSpeed, TargetStrafe.mc.player.getDeltaMovement().y, Math.cos(Math.toRadians(yaw)) * motionSpeed);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processMatrixCubeStrafe(Vec3 playerPos, Vec3 targetPos, double r, int directionMultiplier) {
        Vec3[] vec3dArray = new Vec3[]{new Vec3(targetPos.x - r, playerPos.y, targetPos.z - r), new Vec3(targetPos.x - r, playerPos.y, targetPos.z + r), new Vec3(targetPos.x + r, playerPos.y, targetPos.z + r), new Vec3(targetPos.x + r, playerPos.y, targetPos.z - r)};
        Vec3[] points = vec3dArray;
        if (playerPos.distanceTo(points[this.grimPointIndex]) < 0.5) {
            this.grimPointIndex = (this.grimPointIndex + directionMultiplier + points.length) % points.length;
        }
        Vec3 nextPoint = points[this.grimPointIndex];
        Vec3 dirVec = nextPoint.subtract(playerPos).normalize();
        float yaw = (float)Math.toDegrees(Math.atan2(dirVec.z, dirVec.x)) - 90.0f;
        double motionSpeed = this.speed.getValue();
        TargetStrafe.mc.player.setDeltaMovement(-Math.sin(Math.toRadians(yaw)) * motionSpeed, TargetStrafe.mc.player.getDeltaMovement().y, Math.cos(Math.toRadians(yaw)) * motionSpeed);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processMatrixCircleStrafe(Vec3 playerPos, Vec3 targetPos, double r, int directionMultiplier) {
        double angle = Math.atan2(playerPos.z - targetPos.z, playerPos.x - targetPos.x);
        double x = targetPos.x + r * Math.cos(angle += (double)((float)directionMultiplier * this.speed.getValue()) / Math.max(playerPos.distanceTo(targetPos), r));
        double z = targetPos.z + r * Math.sin(angle);
        float yaw = (float)Math.toDegrees(Math.atan2(z - playerPos.z, x - playerPos.x)) - 90.0f;
        double motionSpeed = this.speed.getValue();
        TargetStrafe.mc.player.setDeltaMovement(-Math.sin(Math.toRadians(yaw)) * motionSpeed, TargetStrafe.mc.player.getDeltaMovement().y, Math.cos(Math.toRadians(yaw)) * motionSpeed);
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void activate() {
        super.activate();
        this.grimPointIndex = 0;
    }
}

