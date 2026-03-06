
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rich.Initialization;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.TickEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.attack.StrikerConstructor;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.impl.combat.aura.impl.RotateConstructor;
import rich.modules.impl.combat.aura.target.MultiPoint;
import rich.modules.impl.combat.aura.target.TargetFinder;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.Instance;
import rich.util.string.PlayerInteractionHelper;

public class TriggerBot
extends ModuleStructure {
    private static final float RANGE_MARGIN = 0.253f;
    private final TargetFinder targetSelector = new TargetFinder();
    private final MultiPoint pointFinder = new MultiPoint();
    public LivingEntity target;
    public SliderSettings attackRange = new SliderSettings("Attack Distance", "Attack reachability to target").setValue(3.0f).range(1.0f, 6.0f);
    MultiSelectSetting targetType = new MultiSelectSetting("Target Type", "Filters target list by type").value("Players", "Mobs", "Animals", "Armor stands").selected("Players", "Mobs", "Animals");
    public MultiSelectSetting attackSetting = new MultiSelectSetting("Settings", "Attack parameters").value("Only Crits", "Crit randomization", "Hit through walls").selected("Only Crits");
    public SelectSetting sprintReset = new SelectSetting("Sprint Reset", "Select sprint reset before strike").value("Legit", "Intense").selected("Legit");
    public BooleanSetting smartCrits = new BooleanSetting("Smart Crits", "Attack on ground when cooldown ready").setValue(true).visible(() -> this.attackSetting.isSelected("Only Crits"));

    public TriggerBot() {
        super("TriggerBot", "Trigger Bot", ModuleCategory.COMBAT);
        this.settings(this.attackRange, this.targetType, this.attackSetting, this.sprintReset, this.smartCrits);
    }

    public static TriggerBot getInstance() {
        return Instance.get(TriggerBot.class);
    }

    @Override
    public void deactivate() {
        this.target = null;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private LivingEntity updateTarget() {
        TargetFinder.EntityFilter filter = new TargetFinder.EntityFilter(this.targetType.getSelected());
        float range = this.attackRange.getValue() + 0.253f;
        this.targetSelector.searchTargets(TriggerBot.mc.level.entitiesForRendering(), range, 360.0f, this.attackSetting.isSelected("Hit through walls"));
        this.targetSelector.validateTarget(filter::isValid);
        return this.targetSelector.getCurrentTarget();
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onRotationUpdate(RotationUpdateEvent e) {
        if (PlayerInteractionHelper.nullCheck()) {
            return;
        }
        switch (e.getType()) {
            case 0: {
                this.target = this.updateTarget();
                break;
            }
            case 2: {
                if (this.target == null) break;
                Initialization.getInstance().getManager().getAttackPerpetrator().performTriggerAttack(this.getConfig(), this);
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    public StrikerConstructor.AttackPerpetratorConfigurable getConfig() {
        float baseRange = this.attackRange.getValue() + 0.253f;
        Tuple<Vec3, AABB> pointData = this.pointFinder.computeVector(this.target, baseRange, AngleConnection.INSTANCE.getRotation(), this.getSmoothMode().randomValue(), this.attackSetting.isSelected("Hit through walls"));
        Vec3 computedPoint = (Vec3)pointData.getA();
        AABB hitbox = (AABB)pointData.getB();
        Angle angle = MathAngle.fromVec3d(computedPoint.subtract(Objects.requireNonNull(TriggerBot.mc.player).getEyePosition()));
        return new StrikerConstructor.AttackPerpetratorConfigurable(this.target, angle, baseRange, this.attackSetting.getSelected(), null, hitbox);
    }

    public RotateConstructor getSmoothMode() {
        return new LinearConstructor();
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    public boolean isResetSprintLegit() {
        return this.sprintReset.isSelected("Legit");
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    public boolean isResetSprintPacket() {
        return this.sprintReset.isSelected("Intense");
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    public boolean isOnlyCrits() {
        return this.attackSetting.isSelected("Only Crits");
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    public boolean isRandomizeCrit() {
        return this.attackSetting.isSelected("Crit randomization");
    }

    @EventHandler
    public void tick(TickEvent e) {
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
    }

    @Generated
    public SelectSetting getSprintReset() {
        return this.sprintReset;
    }

    @Generated
    public BooleanSetting getSmartCrits() {
        return this.smartCrits;
    }
}

