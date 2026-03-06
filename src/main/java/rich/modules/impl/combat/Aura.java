
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.util.Mth;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.impl.InputEvent;
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.impl.combat.aura.attack.StrikerConstructor;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.modules.impl.combat.aura.rotations.FTAngle;
import rich.modules.impl.combat.aura.rotations.MatrixAngle;
import rich.modules.impl.combat.aura.rotations.SPAngle;
import rich.modules.impl.combat.aura.rotations.SnapAngle;
import rich.modules.impl.combat.aura.target.MultiPoint;
import rich.modules.impl.combat.aura.target.TargetFinder;
import rich.modules.impl.movement.ElytraTarget;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.math.TaskPriority;

public class Aura
extends ModuleStructure {
    private final SelectSetting mode = new SelectSetting("Aim Mode", "Select aim mode").value("Matrix", "FunTime Snap", "Snap", "SpookyTime", "Legit").selected("Matrix");
    private final SelectSetting moveFix = new SelectSetting("Movement Correction", "Select move fix mode").value("Focused", "Free", "Pursuit", "Target", "Disabled").selected("Focus");
    public final SliderSettings attackrange = new SliderSettings("Attack Distance", "Set range value").range(2.0f, 6.0f).setValue(3.0f);
    private final SliderSettings lookrange = new SliderSettings("Search Distance", "Set look range value").range(0.0f, 10.0f).setValue(1.5f);
    public final MultiSelectSetting options = new MultiSelectSetting("Settings", "Select settings").value("Hit through walls", "Crit randomization", "Don't hit while eating").selected("Hit through walls", "Crit randomization", "Don't hit while eating");
    private final MultiSelectSetting targetType = new MultiSelectSetting("Target Settings", "Select target settings").value("Players", "Mobs", "Animals", "Friends", "Armor stands").selected("Players", "Mobs", "Animals");
    private final SelectSetting resetSprintMode = new SelectSetting("Sprint Reset", "Reset sprint mode").value("Legit", "Packet").selected("Legit");
    private final BooleanSetting checkCrit = new BooleanSetting("Only Crits", "Only critical hits").setValue(true);
    private final BooleanSetting smartCrits = new BooleanSetting("Smart Crits", "Smart crits - attack on ground when possible").setValue(true).visible(() -> this.checkCrit.isValue());
    public static LivingEntity target;
    public LivingEntity lastTarget;
    TargetFinder targetSelector = new TargetFinder();
    MultiPoint pointFinder = new MultiPoint();

    @Native(type=Native.Type.VMProtectBeginUltra)
    public static Aura getInstance() {
        return Instance.get(Aura.class);
    }

    public Aura() {
        super("Aura", ModuleCategory.COMBAT);
        this.settings(this.mode, this.attackrange, this.lookrange, this.options, this.targetType, this.moveFix, this.resetSprintMode, this.checkCrit, this.smartCrits);
    }

    @Override
    public void deactivate() {
        AngleConnection.INSTANCE.startReturning();
        Initialization.getInstance().getManager().getAttackPerpetrator().getAttackHandler().resetPendingState();
        target = null;
        this.lastTarget = null;
    }

    @EventHandler
    private void tick(TickEvent event) {
    }

    @EventHandler
    public void onRotationUpdate(RotationUpdateEvent e) {
        switch (e.getType()) {
            case 0: {
                LivingEntity previousTarget = target;
                target = this.updateTarget();
                if (previousTarget != null && target == null) {
                    Initialization.getInstance().getManager().getAttackPerpetrator().getAttackHandler().resetPendingState();
                }
                boolean passed = false;
                if (this.mode.isSelected("FunTime Snap") || this.mode.isSelected("HolyWorld")) {
                    passed = true;
                }
                if (target != null && passed && target.distanceTo(Aura.mc.player) <= this.attackrange.getValue() + 0.25f) {
                    this.rotateToTarget(this.getConfig());
                    this.lastTarget = target;
                }
                if (target == null || passed) break;
                this.rotateToTarget(this.getConfig());
                this.lastTarget = target;
                break;
            }
            case 2: {
                if (target == null) break;
                Initialization.getInstance().getManager().getAttackPerpetrator().performAttack(this.getConfig());
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    public StrikerConstructor.AttackPerpetratorConfigurable getConfig() {
        float baseRange = this.attackrange.getValue();
        if (target == null || Aura.mc.player == null) {
            Angle fallback = AngleConnection.INSTANCE.getRotation();
            AABB emptyBox = Aura.mc.player != null ? Aura.mc.player.getBoundingBox() : new AABB(0, 0, 0, 0, 0, 0);
            return new StrikerConstructor.AttackPerpetratorConfigurable(null, fallback, baseRange, this.options.getSelected(), this.mode, emptyBox);
        }
        Tuple<Vec3, AABB> pointData = this.pointFinder.computeVector(target, baseRange, AngleConnection.INSTANCE.getRotation(), this.getSmoothMode().randomValue(), this.options.isSelected("Hit through walls"));
        Vec3 computedPoint = (Vec3)pointData.getA();
        AABB hitbox = (AABB)pointData.getB();
        if (Aura.mc.player.isFallFlying() && target.isFallFlying()) {
            Vec3 targetVelocity = target.getDeltaMovement();
            double targetSpeed = targetVelocity.horizontalDistance();
            float leadTicks = 0.0f;
            if (ElytraTarget.shouldElytraTarget && ElytraTarget.getInstance() != null && ElytraTarget.getInstance().isState()) {
                leadTicks = ElytraTarget.getInstance().elytraForward.getValue();
            }
            if (targetSpeed > 0.35) {
                Vec3 predictedPos = target.position().add(targetVelocity.scale(leadTicks));
                computedPoint = predictedPos.add(0.0, target.getBbHeight() / 2.0f, 0.0);
                hitbox = new AABB(predictedPos.x - (double)(target.getBbWidth() / 2.0f), predictedPos.y, predictedPos.z - (double)(target.getBbWidth() / 2.0f), predictedPos.x + (double)(target.getBbWidth() / 2.0f), predictedPos.y + (double)target.getBbHeight(), predictedPos.z + (double)(target.getBbWidth() / 2.0f));
            }
        }
        Angle angle = MathAngle.fromVec3d(computedPoint.subtract(Objects.requireNonNull(Aura.mc.player).getEyePosition()));
        return new StrikerConstructor.AttackPerpetratorConfigurable(target, angle, baseRange, this.options.getSelected(), this.mode, hitbox);
    }

    public AngleConfig getRotationConfig() {
        boolean visibleCorrection = !this.moveFix.isSelected("Disabled");
        boolean freeCorrection = this.moveFix.isSelected("Free");
        return new AngleConfig(this.getSmoothMode(), visibleCorrection, freeCorrection);
    }

    private void rotateToTarget(StrikerConstructor.AttackPerpetratorConfigurable config) {
        StrikeManager attackHandler = Initialization.getInstance().getManager().getAttackPerpetrator().getAttackHandler();
        AngleConnection controller = AngleConnection.INSTANCE;
        Angle.VecRotation rotation = new Angle.VecRotation(config.getAngle(), config.getAngle().toVector());
        AngleConfig rotationConfig = this.getRotationConfig();
        boolean elytraMode = Aura.mc.player.isFallFlying() && ElytraTarget.getInstance() != null && ElytraTarget.getInstance().isState();
        switch (this.mode.getSelected()) {
            case "FunTime Snap": {
                if (!attackHandler.canAttack(config, 5)) break;
                controller.clear();
                controller.rotateTo(rotation, target, 60, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                break;
            }
            case "Snap": {
                if (!attackHandler.canAttack(config, 0)) break;
                controller.rotateTo(rotation, target, 0, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
                break;
            }
            case "Matrix": 
            case "SpookyTime": 
            case "Legit": {
                controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
            }
        }
        if (elytraMode) {
            controller.rotateTo(rotation, target, 1, rotationConfig, TaskPriority.HIGH_IMPORTANCE_1, this);
        }
    }

    @EventHandler
    public void onInput(InputEvent event) {
        if (Aura.mc.player == null || Aura.mc.level == null) {
            return;
        }
        Input input = event.getInput();
        if (input == null) {
            return;
        }
        if (!this.isState()) {
            return;
        }
        if (target == null || !target.isAlive()) {
            return;
        }
        boolean w = Aura.mc.options.keyUp.isDown();
        boolean s = Aura.mc.options.keyDown.isDown();
        boolean a = Aura.mc.options.keyLeft.isDown();
        boolean d = Aura.mc.options.keyRight.isDown();
        if (this.moveFix.isSelected("Target")) {
            Vec3 playerPos = Aura.mc.player.position();
            Vec3 targetPos = target.position();
            Vec3 moveTarget = new Vec3(targetPos.x, playerPos.y, targetPos.z);
            Vec3 dir = moveTarget.subtract(playerPos).normalize();
            float yaw = AngleConnection.INSTANCE.getRotation().getYaw();
            float moveAngle = (float)Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0f;
            float angleDiff = Mth.wrapDegrees((float)(moveAngle - yaw));
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
            return;
        }
        if (this.moveFix.isSelected("Pursuit")) {
            if (!(w || s || a || d)) {
                return;
            }
            Vec3 playerPos = Aura.mc.player.position();
            AABB targetBox = target.getBoundingBox();
            Vec3 center = targetBox.getCenter();
            float targetYaw = target.getYRot();
            double rad = Math.toRadians(targetYaw);
            Vec3 forwardDir = new Vec3(-Math.sin(rad), 0.0, Math.cos(rad)).normalize();
            Vec3 rightDir = new Vec3(-forwardDir.z, 0.0, forwardDir.x).normalize();
            Vec3 leftDir = rightDir.scale(-1.0);
            double halfWidth = (double)target.getBbWidth() / 2.0;
            double offset = halfWidth + 0.1;
            Vec3 moveTargetVec = center;
            Vec3 offsetVec = Vec3.ZERO;
            if (w) {
                offsetVec = offsetVec.add(forwardDir);
            }
            if (s) {
                offsetVec = offsetVec.add(forwardDir.scale(-1.0));
            }
            if (a) {
                offsetVec = offsetVec.add(leftDir);
            }
            if (d) {
                offsetVec = offsetVec.add(rightDir);
            }
            if (offsetVec.lengthSqr() > 0.0) {
                offsetVec = offsetVec.normalize().scale(offset);
                moveTargetVec = center.add(offsetVec);
            }
            moveTargetVec = new Vec3(moveTargetVec.x, playerPos.y, moveTargetVec.z);
            Vec3 dir = moveTargetVec.subtract(playerPos).normalize();
            float yaw = AngleConnection.INSTANCE.getRotation().getYaw();
            float moveAngle = (float)Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0f;
            float angleDiff = Mth.wrapDegrees((float)(moveAngle - yaw));
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
        }
    }

    private LivingEntity updateTarget() {
        TargetFinder.EntityFilter filter = new TargetFinder.EntityFilter(this.targetType.getSelected());
        float range = this.attackrange.getValue() + 0.25f + (Aura.mc.player.isFallFlying() && ElytraTarget.getInstance() != null && ElytraTarget.getInstance().isState() ? ElytraTarget.getInstance().elytraFindRange.getValue() : this.lookrange.getValue());
        float dynamicFov = 360.0f;
        this.targetSelector.searchTargets(Aura.mc.level.entitiesForRendering(), range, dynamicFov, this.options.isSelected("Hit through walls"));
        this.targetSelector.validateTarget(filter::isValid);
        return this.targetSelector.getCurrentTarget();
    }

    public RotateConstructor getSmoothMode() {
        if (Aura.mc.player.isFallFlying() && ElytraTarget.getInstance() != null && ElytraTarget.getInstance().isState()) {
            return new LinearConstructor();
        }
        return switch (this.mode.getSelected()) {
            case "FunTime Snap" -> new FTAngle();
            case "SpookyTime" -> new SPAngle();
            case "Snap" -> new SnapAngle();
            case "Legit" -> new SnapAngle();
            case "Matrix" -> new MatrixAngle();
            default -> new LinearConstructor();
        };
    }

    @Generated
    public SliderSettings getAttackrange() {
        return this.attackrange;
    }

    @Generated
    public SelectSetting getResetSprintMode() {
        return this.resetSprintMode;
    }

    @Generated
    public BooleanSetting getCheckCrit() {
        return this.checkCrit;
    }

    @Generated
    public BooleanSetting getSmartCrits() {
        return this.smartCrits;
    }
}

