
package rich.modules.impl.combat.aura.attack;

import java.util.List;
import lombok.Generated;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import rich.IMinecraft;
import rich.events.impl.PacketEvent;
import rich.events.impl.UsingItemEvent;
import rich.modules.impl.combat.TriggerBot;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.attack.StrikeManager;
import rich.modules.module.setting.implement.SelectSetting;

public class StrikerConstructor
implements IMinecraft {
    StrikeManager attackHandler = new StrikeManager();

    public void tick() {
        this.attackHandler.tick();
    }

    public void onPacket(PacketEvent e) {
        this.attackHandler.onPacket(e);
    }

    public void performAttack(AttackPerpetratorConfigurable configurable) {
        this.attackHandler.handleAttack(configurable);
    }

    public void performTriggerAttack(AttackPerpetratorConfigurable configurable, TriggerBot triggerBot) {
        this.attackHandler.handleTriggerAttack(configurable, triggerBot);
    }

    public void onUsingItem(UsingItemEvent e) {
        this.attackHandler.onUsingItem(e);
    }

    @Generated
    public StrikeManager getAttackHandler() {
        return this.attackHandler;
    }

    public static class AttackPerpetratorConfigurable {
        private final LivingEntity target;
        private final Angle angle;
        private final float maximumRange;
        private final boolean onlyCritical;
        private final boolean shouldBreakShield;
        private final boolean shouldUnPressShield;
        private final boolean eatAndAttack;
        private final boolean multiPoints;
        private final boolean ignoreWalls;
        private final AABB box;
        private final SelectSetting aimMode;

        public AttackPerpetratorConfigurable(LivingEntity target, Angle angle, float maximumRange, List<String> options, SelectSetting aimMode, AABB box) {
            this.target = target;
            this.angle = angle;
            this.maximumRange = maximumRange;
            this.onlyCritical = options.contains("Only Crits") || options.contains("Only Critical") || options.contains("Crits with space");
            this.shouldBreakShield = options.contains("Break Shield");
            this.shouldUnPressShield = options.contains("UnPress Shield");
            this.multiPoints = options.contains("Multi Points");
            this.eatAndAttack = options.contains("No Attack When Eat");
            this.ignoreWalls = options.contains("Hit through walls") || options.contains("Ignore The Walls");
            this.box = box;
            this.aimMode = aimMode;
        }

        @Generated
        public LivingEntity getTarget() {
            return this.target;
        }

        @Generated
        public Angle getAngle() {
            return this.angle;
        }

        @Generated
        public float getMaximumRange() {
            return this.maximumRange;
        }

        @Generated
        public boolean isOnlyCritical() {
            return this.onlyCritical;
        }

        @Generated
        public boolean isShouldBreakShield() {
            return this.shouldBreakShield;
        }

        @Generated
        public boolean isShouldUnPressShield() {
            return this.shouldUnPressShield;
        }

        @Generated
        public boolean isEatAndAttack() {
            return this.eatAndAttack;
        }

        @Generated
        public boolean isMultiPoints() {
            return this.multiPoints;
        }

        @Generated
        public boolean isIgnoreWalls() {
            return this.ignoreWalls;
        }

        @Generated
        public AABB getBox() {
            return this.box;
        }

        @Generated
        public SelectSetting getAimMode() {
            return this.aimMode;
        }
    }
}

