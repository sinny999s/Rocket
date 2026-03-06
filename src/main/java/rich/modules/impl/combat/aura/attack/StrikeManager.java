
package rich.modules.impl.combat.aura.attack;

import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.events.impl.PacketEvent;
import rich.events.impl.UsingItemEvent;
import rich.modules.impl.combat.Aura;
import rich.modules.impl.combat.TriggerBot;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.attack.Pressing;
import rich.modules.impl.combat.aura.attack.StrikerConstructor;
import rich.modules.impl.combat.aura.target.RaycastAngle;
import rich.modules.impl.movement.ElytraTarget;
import rich.util.player.PlayerSimulation;
import rich.util.string.PlayerInteractionHelper;
import rich.util.timer.StopWatch;

public class StrikeManager
implements IMinecraft {
    private final Pressing clickScheduler = new Pressing();
    private final StopWatch attackTimer = new StopWatch();
    private final StopWatch shieldWatch = new StopWatch();
    private int count = 0;
    private int ticksOnBlock = 0;

    void tick() {
        this.ticksOnBlock = StrikeManager.mc.player != null && StrikeManager.mc.player.onGround() ? ++this.ticksOnBlock : 0;
    }

    void onUsingItem(UsingItemEvent e) {
        if (e.getType() == -1 && !this.shieldWatch.finished(50.0)) {
            e.cancel();
        }
    }

    void onPacket(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        if (packet instanceof ServerboundSwingPacket || packet instanceof ServerboundSetCarriedItemPacket) {
            this.clickScheduler.recalculate();
        }
    }

    public void resetPendingState() {
    }

    private boolean hasAnyMovementInput() {
        if (StrikeManager.mc.player == null) {
            return false;
        }
        return StrikeManager.mc.player.input.keyPresses.forward() || StrikeManager.mc.player.input.keyPresses.backward() || StrikeManager.mc.player.input.keyPresses.left() || StrikeManager.mc.player.input.keyPresses.right();
    }

    private boolean isHoldingMace() {
        return this.clickScheduler.isHoldingMace();
    }

    private boolean isPlayerEating() {
        if (StrikeManager.mc.player == null) {
            return false;
        }
        if (!StrikeManager.mc.player.isUsingItem()) {
            return false;
        }
        ItemStack activeItem = StrikeManager.mc.player.getUseItem();
        if (activeItem.isEmpty()) {
            return false;
        }
        ItemUseAnimation useAction = activeItem.getUseAnimation();
        return useAction == ItemUseAnimation.EAT || useAction == ItemUseAnimation.DRINK;
    }

    private boolean shouldWaitForEating() {
        Aura aura = Aura.getInstance();
        return aura.options.isSelected("Don't hit while eating") && this.isPlayerEating();
    }

    private boolean isInWater() {
        return StrikeManager.mc.player != null && (StrikeManager.mc.player.isInWater() || StrikeManager.mc.player.isUnderWater() || StrikeManager.mc.player.isSwimming());
    }

    private boolean hasLowCeiling() {
        if (StrikeManager.mc.player == null || StrikeManager.mc.level == null) {
            return false;
        }
        BlockPos playerPos = StrikeManager.mc.player.blockPosition();
        BlockPos above1 = playerPos.above(2);
        BlockPos above2 = playerPos.above(3);
        BlockState state1 = StrikeManager.mc.level.getBlockState(above1);
        BlockState state2 = StrikeManager.mc.level.getBlockState(above2);
        boolean blocked1 = !state1.isAir() && !state1.getCollisionShape(StrikeManager.mc.level, above1).isEmpty();
        boolean blocked2 = !state2.isAir() && !state2.getCollisionShape(StrikeManager.mc.level, above2).isEmpty();
        return blocked1 || blocked2;
    }

    private boolean isPerfectCrit() {
        if (StrikeManager.mc.player == null) {
            return false;
        }
        return StrikeManager.mc.player.fallDistance > 0.0 && !StrikeManager.mc.player.onGround() && !StrikeManager.mc.player.onClimbable() && !StrikeManager.mc.player.isInWater() && !StrikeManager.mc.player.hasEffect(MobEffects.BLINDNESS) && !StrikeManager.mc.player.isPassenger() && !StrikeManager.mc.player.getAbilities().flying;
    }

    private boolean isAscending() {
        if (StrikeManager.mc.player == null) {
            return false;
        }
        return !StrikeManager.mc.player.onGround() && StrikeManager.mc.player.getDeltaMovement().y > 0.0;
    }

    private boolean isDescending() {
        if (StrikeManager.mc.player == null) {
            return false;
        }
        return !StrikeManager.mc.player.onGround() && StrikeManager.mc.player.getDeltaMovement().y <= 0.0;
    }

    private boolean willBeCritInTicks(int ticks) {
        if (ticks == 0) {
            return this.isPerfectCrit();
        }
        PlayerSimulation sim = PlayerSimulation.simulateLocalPlayer(ticks);
        return sim.fallDistance > 0.0f && !sim.onGround && sim.velocity.y <= 0.0 && !sim.isClimbing() && !sim.player.isInWater() && !sim.hasStatusEffect(MobEffects.BLINDNESS) && !sim.player.isPassenger() && !sim.player.getAbilities().flying;
    }

    private boolean hasMovementRestrictions() {
        if (StrikeManager.mc.player == null) {
            return true;
        }
        if (this.isInWater()) {
            return false;
        }
        if (this.hasLowCeiling()) {
            return true;
        }
        if (StrikeManager.mc.player.hasEffect(MobEffects.BLINDNESS)) {
            return true;
        }
        if (StrikeManager.mc.player.hasEffect(MobEffects.LEVITATION)) {
            return true;
        }
        if (PlayerInteractionHelper.isBoxInBlock(StrikeManager.mc.player.getBoundingBox().inflate(-0.001), Blocks.COBWEB)) {
            return true;
        }
        if (StrikeManager.mc.player.isInLava()) {
            return true;
        }
        if (StrikeManager.mc.player.onClimbable()) {
            return true;
        }
        if (!PlayerInteractionHelper.canChangeIntoPose(Pose.STANDING, StrikeManager.mc.player.position())) {
            return true;
        }
        return StrikeManager.mc.player.getAbilities().flying;
    }

    private boolean shouldResetSprintForCrit() {
        if (StrikeManager.mc.player == null) {
            return false;
        }
        if (this.isInWater()) {
            return false;
        }
        if (StrikeManager.mc.player.isFallFlying()) {
            return false;
        }
        return StrikeManager.mc.player.isSprinting();
    }

    private boolean canCritNow() {
        Aura aura = Aura.getInstance();
        boolean checkCritEnabled = aura.getCheckCrit().isValue();
        boolean smartCritsEnabled = aura.getSmartCrits().isValue();
        if (this.isInWater() || this.hasLowCeiling() || this.hasMovementRestrictions()) {
            return true;
        }
        if (!checkCritEnabled) {
            return true;
        }
        if (this.isAscending()) {
            return false;
        }
        if (smartCritsEnabled) {
            if (StrikeManager.mc.player.onGround()) {
                return true;
            }
            return this.isDescending() && StrikeManager.mc.player.fallDistance > 0.0;
        }
        return this.isPerfectCrit();
    }

    void handleAttack(StrikerConstructor.AttackPerpetratorConfigurable config) {
        boolean shouldReset;
        if (config.getTarget() == null || !config.getTarget().isAlive()) {
            return;
        }
        if (this.shouldWaitForEating()) {
            return;
        }
        if (this.isHoldingMace()) {
            this.handleMaceAttack(config);
            return;
        }
        boolean elytraMode = this.checkElytraMode(config);
        if (elytraMode && !this.checkElytraRaycast(config)) {
            return;
        }
        if (!RaycastAngle.rayTrace(config)) {
            return;
        }
        if (!this.isLookingAtTarget(config)) {
            return;
        }
        if (!this.clickScheduler.isCooldownComplete(0)) {
            return;
        }
        if (!this.canCritNow()) {
            return;
        }
        this.preAttackEntity(config);
        boolean wasSprinting = StrikeManager.mc.player.isSprinting();
        boolean bl = shouldReset = wasSprinting && this.shouldResetSprintForCrit();
        if (shouldReset) {
            if (Aura.getInstance().getResetSprintMode().isSelected("Packet")) {
                mc.getConnection().send(new ServerboundPlayerCommandPacket(StrikeManager.mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
            } else {
                StrikeManager.mc.player.setSprinting(false);
            }
        }
        this.executeAttack(config);
        if (shouldReset) {
            if (Aura.getInstance().getResetSprintMode().isSelected("Packet")) {
                mc.getConnection().send(new ServerboundPlayerCommandPacket(StrikeManager.mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
            } else {
                StrikeManager.mc.player.setSprinting(true);
            }
        }
    }

    private void preAttackEntity(StrikerConstructor.AttackPerpetratorConfigurable config) {
        if (config.isShouldUnPressShield() && StrikeManager.mc.player.isUsingItem() && StrikeManager.mc.player.getUseItem().getItem().equals(Items.SHIELD)) {
            StrikeManager.mc.gameMode.releaseUsingItem(StrikeManager.mc.player);
            this.shieldWatch.reset();
        }
    }

    private void handleMaceAttack(StrikerConstructor.AttackPerpetratorConfigurable config) {
        boolean shouldReset;
        if (this.shouldWaitForEating()) {
            return;
        }
        if (StrikeManager.mc.player.distanceTo(config.getTarget()) > Aura.getInstance().getAttackrange().getValue()) {
            return;
        }
        if (!RaycastAngle.rayTrace(config)) {
            return;
        }
        if (!this.isLookingAtTarget(config)) {
            return;
        }
        if (!this.clickScheduler.isMaceFastAttack()) {
            return;
        }
        if (!this.attackTimer.finished(25.0)) {
            return;
        }
        this.preAttackEntity(config);
        boolean wasSprinting = StrikeManager.mc.player.isSprinting();
        boolean bl = shouldReset = wasSprinting && this.shouldResetSprintForCrit();
        if (shouldReset) {
            if (Aura.getInstance().getResetSprintMode().isSelected("Packet")) {
                mc.getConnection().send(new ServerboundPlayerCommandPacket(StrikeManager.mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
            } else {
                StrikeManager.mc.player.setSprinting(false);
            }
        }
        this.executeAttack(config);
        if (shouldReset) {
            if (Aura.getInstance().getResetSprintMode().isSelected("Packet")) {
                mc.getConnection().send(new ServerboundPlayerCommandPacket(StrikeManager.mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
            } else {
                StrikeManager.mc.player.setSprinting(true);
            }
        }
    }

    private boolean checkElytraMode(StrikerConstructor.AttackPerpetratorConfigurable config) {
        return Aura.target != null && Aura.target.isFallFlying() && StrikeManager.mc.player.isFallFlying() && ElytraTarget.getInstance() != null && ElytraTarget.getInstance().isState();
    }

    private boolean checkElytraRaycast(StrikerConstructor.AttackPerpetratorConfigurable config) {
        Vec3 targetVelocity = config.getTarget().getDeltaMovement();
        float leadTicks = 0.0f;
        if (ElytraTarget.shouldElytraTarget) {
            leadTicks = ElytraTarget.getInstance().elytraForward.getValue();
        }
        Vec3 predictedPos = config.getTarget().position().add(targetVelocity.scale(leadTicks));
        AABB predictedBox = new AABB(predictedPos.x - (double)(config.getTarget().getBbWidth() / 2.0f), predictedPos.y, predictedPos.z - (double)(config.getTarget().getBbWidth() / 2.0f), predictedPos.x + (double)(config.getTarget().getBbWidth() / 2.0f), predictedPos.y + (double)config.getTarget().getBbHeight(), predictedPos.z + (double)(config.getTarget().getBbWidth() / 2.0f));
        Vec3 eyePos = StrikeManager.mc.player.getEyePosition();
        Vec3 lookVec = AngleConnection.INSTANCE.getRotation().toVector();
        return predictedBox.clip(eyePos, eyePos.add(lookVec.scale(config.getMaximumRange()))).isPresent();
    }

    private void executeAttack(StrikerConstructor.AttackPerpetratorConfigurable config) {
        StrikeManager.mc.gameMode.attack(StrikeManager.mc.player, config.getTarget());
        StrikeManager.mc.player.swing(InteractionHand.MAIN_HAND);
        this.attackTimer.reset();
        ++this.count;
    }

    void handleTriggerAttack(StrikerConstructor.AttackPerpetratorConfigurable config, TriggerBot triggerBot) {
        boolean shouldReset;
        if (this.shouldWaitForEating()) {
            return;
        }
        if (!RaycastAngle.rayTrace(config)) {
            return;
        }
        if (!this.isLookingAtTarget(config)) {
            return;
        }
        if (!this.clickScheduler.isCooldownComplete(0)) {
            return;
        }
        if (!this.canAttackTrigger(config, triggerBot)) {
            return;
        }
        this.preAttackEntity(config);
        boolean wasSprinting = StrikeManager.mc.player.isSprinting();
        boolean bl = shouldReset = wasSprinting && this.shouldResetSprintForCrit();
        if (shouldReset) {
            if (Aura.getInstance().getResetSprintMode().isSelected("Packet")) {
                mc.getConnection().send(new ServerboundPlayerCommandPacket(StrikeManager.mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
            } else {
                StrikeManager.mc.player.setSprinting(false);
            }
        }
        this.executeAttack(config);
        if (shouldReset) {
            if (Aura.getInstance().getResetSprintMode().isSelected("Packet")) {
                mc.getConnection().send(new ServerboundPlayerCommandPacket(StrikeManager.mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
            } else {
                StrikeManager.mc.player.setSprinting(true);
            }
        }
    }

    private boolean canAttackTrigger(StrikerConstructor.AttackPerpetratorConfigurable config, TriggerBot triggerBot) {
        if (this.shouldWaitForEating()) {
            return false;
        }
        if (!this.clickScheduler.isCooldownComplete(0)) {
            return false;
        }
        boolean checkCritEnabled = triggerBot.isOnlyCrits();
        boolean smartCritsEnabled = triggerBot.getSmartCrits().isValue();
        if (this.isInWater() || this.hasLowCeiling() || this.hasMovementRestrictions()) {
            return true;
        }
        if (!checkCritEnabled) {
            return true;
        }
        if (this.isAscending()) {
            return false;
        }
        if (smartCritsEnabled) {
            if (StrikeManager.mc.player.onGround()) {
                return true;
            }
            return this.isDescending() && StrikeManager.mc.player.fallDistance > 0.0;
        }
        return this.isPerfectCrit();
    }

    public boolean shouldResetSprinting(StrikerConstructor.AttackPerpetratorConfigurable config) {
        if (Aura.target == null) {
            return false;
        }
        if (this.shouldWaitForEating()) {
            return false;
        }
        if (this.isHoldingMace()) {
            return true;
        }
        return this.shouldResetSprintForCrit();
    }

    public boolean shouldResetSprintingForTrigger(StrikerConstructor.AttackPerpetratorConfigurable config, TriggerBot triggerBot) {
        if (triggerBot.target == null) {
            return false;
        }
        if (this.shouldWaitForEating()) {
            return false;
        }
        return this.shouldResetSprintForCrit();
    }

    public boolean canAttack(StrikerConstructor.AttackPerpetratorConfigurable config, int ticks) {
        if (this.shouldWaitForEating()) {
            return false;
        }
        if (this.isHoldingMace()) {
            return this.attackTimer.finished(25.0) && this.clickScheduler.isMaceFastAttack();
        }
        if (!this.clickScheduler.isCooldownComplete(0)) {
            return false;
        }
        if (ticks > 0) {
            Aura aura = Aura.getInstance();
            boolean checkCritEnabled = aura.getCheckCrit().isValue();
            boolean smartCritsEnabled = aura.getSmartCrits().isValue();
            if (!checkCritEnabled) {
                return true;
            }
            if (this.isInWater() || this.hasLowCeiling() || this.hasMovementRestrictions()) {
                return true;
            }
            for (int i = 0; i <= ticks; ++i) {
                if (this.willBeCritInTicks(i)) {
                    return true;
                }
                if (!smartCritsEnabled) continue;
                PlayerSimulation sim = PlayerSimulation.simulateLocalPlayer(i);
                if (!sim.onGround) continue;
                return true;
            }
            return false;
        }
        return this.clickScheduler.isCooldownComplete(0) && this.canCritNow();
    }

    public boolean canCrit(StrikerConstructor.AttackPerpetratorConfigurable config, int ticks) {
        if (this.isHoldingMace()) {
            return true;
        }
        if (StrikeManager.mc.player.isUsingItem() && !StrikeManager.mc.player.getUseItem().getItem().equals(Items.SHIELD) && config.isEatAndAttack()) {
            return false;
        }
        if (this.isInWater() || this.hasLowCeiling() || this.hasMovementRestrictions()) {
            return true;
        }
        Aura aura = Aura.getInstance();
        boolean checkCritEnabled = aura.getCheckCrit().isValue();
        boolean smartCritsEnabled = aura.getSmartCrits().isValue();
        if (!checkCritEnabled) {
            return true;
        }
        if (ticks > 0) {
            for (int i = 0; i <= ticks; ++i) {
                if (this.willBeCritInTicks(i)) {
                    return true;
                }
                if (!smartCritsEnabled) continue;
                PlayerSimulation sim = PlayerSimulation.simulateLocalPlayer(i);
                if (!sim.onGround) continue;
                return true;
            }
            return false;
        }
        return this.canCritNow();
    }

    private boolean isLookingAtTarget(StrikerConstructor.AttackPerpetratorConfigurable config) {
        Vec3 eyePos = StrikeManager.mc.player.getEyePosition();
        Vec3 lookVec = AngleConnection.INSTANCE.getRotation().toVector();
        Vec3 endVec = eyePos.add(lookVec.scale(config.getMaximumRange()));
        return config.getBox().clip(eyePos, endVec).isPresent();
    }

    @Generated
    public void setCount(int count) {
        this.count = count;
    }

    @Generated
    public void setTicksOnBlock(int ticksOnBlock) {
        this.ticksOnBlock = ticksOnBlock;
    }

    @Generated
    public Pressing getClickScheduler() {
        return this.clickScheduler;
    }

    @Generated
    public StopWatch getAttackTimer() {
        return this.attackTimer;
    }

    @Generated
    public StopWatch getShieldWatch() {
        return this.shieldWatch;
    }

    @Generated
    public int getCount() {
        return this.count;
    }

    @Generated
    public int getTicksOnBlock() {
        return this.ticksOnBlock;
    }
}

