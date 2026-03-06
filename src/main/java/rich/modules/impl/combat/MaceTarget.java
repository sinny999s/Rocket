
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import lombok.Generated;
import net.minecraft.world.entity.LivingEntity;
import rich.events.api.EventHandler;
import rich.events.impl.InputEvent;
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.impl.combat.aura.target.TargetFinder;
import rich.modules.impl.combat.macetarget.armor.ArmorSwapHandler;
import rich.modules.impl.combat.macetarget.armor.FireworkHandler;
import rich.modules.impl.combat.macetarget.attack.AttackHandler;
import rich.modules.impl.combat.macetarget.flight.FlightController;
import rich.modules.impl.combat.macetarget.prediction.TargetPredictor;
import rich.modules.impl.combat.macetarget.stage.StageHandler;
import rich.modules.impl.combat.macetarget.state.MaceState;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.inventory.InventoryUtils;
import rich.util.inventory.SwapSettings;
import rich.util.math.TaskPriority;
import rich.util.timer.StopWatch;

public class MaceTarget
extends ModuleStructure {
    private final SelectSetting serverMode = new SelectSetting("Server", "Server work mode").value("Default", "ReallyWorld").selected("Default");
    private final SelectSetting modeSetting = new SelectSetting("Mode", "Swap method").value("Silent", "Legit").selected("Silent");
    private final SliderSettings height = new SliderSettings("Height", "Height flight above target").range(20.0f, 60.0f).setValue(30.0f);
    private final MultiSelectSetting targetType = new MultiSelectSetting("Targets", "Target Types").value("Players", "Mobs", "Animals").selected("Players");
    private final BooleanSetting autoEquipChest = new BooleanSetting("Auto-Chestplate", "Equip chestplate on disable").setValue(true);
    private final BooleanSetting predictMovement = new BooleanSetting("Prediction", "Predict fleeing target position").setValue(true);
    private final TargetPredictor predictor = new TargetPredictor();
    private final FlightController flightController;
    private final ArmorSwapHandler armorSwapHandler;
    private final FireworkHandler fireworkHandler;
    private final AttackHandler attackHandler = new AttackHandler();
    private final StageHandler stageHandler;
    private final TargetFinder targetFinder = new TargetFinder();
    private final StopWatch fireworkTimer = new StopWatch();
    private LivingEntity target;

    public static MaceTarget getInstance() {
        return Instance.get(MaceTarget.class);
    }

    public MaceTarget() {
        super("MaceTarget", "Mace Target", ModuleCategory.COMBAT);
        this.settings(this.serverMode, this.modeSetting, this.height, this.targetType, this.autoEquipChest, this.predictMovement);
        this.flightController = new FlightController(this.predictor);
        this.armorSwapHandler = new ArmorSwapHandler(this::buildSettings);
        this.fireworkHandler = new FireworkHandler(this::buildSettings);
        this.stageHandler = new StageHandler(this.armorSwapHandler, this.fireworkHandler, this.attackHandler, this.fireworkTimer);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isSilentMode() {
        return this.modeSetting.getSelected().equals("Silent");
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isReallyWorldMode() {
        return this.serverMode.getSelected().equals("ReallyWorld");
    }

    private SwapSettings buildSettings() {
        return this.isSilentMode() ? SwapSettings.instant() : SwapSettings.legit();
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void updateHandlers() {
        this.stageHandler.setSilentMode(this.isSilentMode());
        this.stageHandler.setReallyWorldMode(this.isReallyWorldMode());
        this.stageHandler.setHeight(this.height.getValue());
        this.flightController.setPredictionEnabled(this.predictMovement.isValue());
        this.flightController.setHeight(this.height.getValue());
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void activate() {
        this.stageHandler.reset();
        this.target = null;
        this.attackHandler.reset();
        this.armorSwapHandler.reset();
        this.fireworkHandler.reset();
        this.predictor.reset();
        this.fireworkTimer.reset();
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        if (this.autoEquipChest.isValue() && MaceTarget.mc.player != null) {
            this.equipChestplateOnDisable();
        }
        this.armorSwapHandler.forceRestore();
        this.fireworkHandler.forceRestore();
        this.target = null;
        this.targetFinder.releaseTarget();
        this.armorSwapHandler.reset();
        this.fireworkHandler.reset();
        this.predictor.reset();
        AngleConnection.INSTANCE.startReturning();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void equipChestplateOnDisable() {
        int slot;
        if (InventoryUtils.hasElytra() && (slot = InventoryUtils.findChestArmorSlot()) != -1) {
            int wrappedSlot = InventoryUtils.wrapSlot(slot);
            InventoryUtils.swap(wrappedSlot, 6);
            InventoryUtils.closeScreen();
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (MaceTarget.mc.player == null || MaceTarget.mc.level == null) {
            return;
        }
        if (event.getType() == 0) {
            this.updateHandlers();
            if (this.target == null || !this.target.isAlive()) {
                this.findTarget();
            }
            if (this.target == null) {
                return;
            }
            this.predictor.update(this.target);
            MaceState.Stage currentStage = this.stageHandler.getStage();
            switch (currentStage) {
                case FLYING_UP: {
                    if (!InventoryUtils.hasElytra() || !MaceTarget.mc.player.isFallFlying()) break;
                    Angle targetAngle = this.flightController.calculateAngle(this.target, currentStage);
                    this.rotateTo(targetAngle);
                    break;
                }
                case TARGETTING: 
                case ATTACKING: {
                    Angle targetAngle = this.flightController.calculateAngle(this.target, currentStage);
                    this.rotateTo(targetAngle);
                }
            }
        }
        if (event.getType() == 2) {
            this.handlePostRotation();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handlePostRotation() {
        if (this.attackHandler.isPendingAttack()) {
            this.attackHandler.performAttack(this.target);
            this.attackHandler.setPendingAttack(false);
            if (this.attackHandler.isShouldDisableAfterAttack()) {
                this.attackHandler.setShouldDisableAfterAttack(false);
                this.setState(false);
            }
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent event) {
        if (MaceTarget.mc.player == null || MaceTarget.mc.level == null) {
            this.resetAllStates();
            return;
        }
        if (!this.isSilentMode()) {
            this.armorSwapHandler.processLoop();
            this.fireworkHandler.processLoop();
        }
        if (this.armorSwapHandler.isActive() || this.fireworkHandler.isActive()) {
            return;
        }
        if (this.target == null || !this.target.isAlive()) {
            return;
        }
        this.processStage();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processStage() {
        boolean hasElytra = InventoryUtils.hasElytra();
        MaceState.Stage currentStage = this.stageHandler.getStage();
        switch (currentStage) {
            case PREPARE: {
                this.stageHandler.handlePrepare(hasElytra);
                break;
            }
            case FLYING_UP: {
                this.stageHandler.handleFlyingUp(this.target, hasElytra);
                break;
            }
            case TARGETTING: {
                this.stageHandler.handleTargetting(this.target);
                break;
            }
            case ATTACKING: {
                this.stageHandler.handleAttacking(this.target, hasElytra);
            }
        }
    }

    @EventHandler
    public void onInput(InputEvent event) {
        if (MaceTarget.mc.player == null) {
            return;
        }
        if (this.armorSwapHandler.getMovement().isBlocked() || this.fireworkHandler.getMovement().isBlocked()) {
            event.setDirectionalLow(false, false, false, false);
            event.setJumping(false);
        }
        if (this.target != null && InventoryUtils.hasElytra() && this.stageHandler.getStage() == MaceState.Stage.FLYING_UP) {
            if (MaceTarget.mc.player.onGround()) {
                event.setJumping(true);
            } else if (!MaceTarget.mc.player.isFallFlying() && !MaceTarget.mc.player.getAbilities().flying) {
                event.setJumping(MaceTarget.mc.player.tickCount % 2 == 0);
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void findTarget() {
        TargetFinder.EntityFilter filter = new TargetFinder.EntityFilter(this.targetType.getSelected());
        this.targetFinder.searchTargets(MaceTarget.mc.level.entitiesForRendering(), 128.0f, 360.0f, true);
        this.targetFinder.validateTarget(filter::isValid);
        this.target = this.targetFinder.getCurrentTarget();
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void rotateTo(Angle angle) {
        AngleConfig config = new AngleConfig(new LinearConstructor(), true, false);
        Angle.VecRotation rotation = new Angle.VecRotation(angle, angle.toVector());
        AngleConnection.INSTANCE.rotateTo(rotation, this.target, 1, config, TaskPriority.HIGH_IMPORTANCE_1, this);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void resetAllStates() {
        this.armorSwapHandler.reset();
        this.fireworkHandler.reset();
        this.attackHandler.reset();
        this.predictor.reset();
        this.target = null;
        this.stageHandler.reset();
    }

    @Generated
    public SelectSetting getServerMode() {
        return this.serverMode;
    }

    @Generated
    public SelectSetting getModeSetting() {
        return this.modeSetting;
    }

    @Generated
    public SliderSettings getHeight() {
        return this.height;
    }

    @Generated
    public MultiSelectSetting getTargetType() {
        return this.targetType;
    }

    @Generated
    public BooleanSetting getAutoEquipChest() {
        return this.autoEquipChest;
    }

    @Generated
    public BooleanSetting getPredictMovement() {
        return this.predictMovement;
    }

    @Generated
    public TargetPredictor getPredictor() {
        return this.predictor;
    }

    @Generated
    public FlightController getFlightController() {
        return this.flightController;
    }

    @Generated
    public ArmorSwapHandler getArmorSwapHandler() {
        return this.armorSwapHandler;
    }

    @Generated
    public FireworkHandler getFireworkHandler() {
        return this.fireworkHandler;
    }

    @Generated
    public AttackHandler getAttackHandler() {
        return this.attackHandler;
    }

    @Generated
    public StageHandler getStageHandler() {
        return this.stageHandler;
    }

    @Generated
    public TargetFinder getTargetFinder() {
        return this.targetFinder;
    }

    @Generated
    public StopWatch getFireworkTimer() {
        return this.fireworkTimer;
    }

    @Generated
    public LivingEntity getTarget() {
        return this.target;
    }
}

