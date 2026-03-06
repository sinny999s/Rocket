package rich.modules.impl.combat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.combat.BlockUtil;
import rich.util.combat.CrystalUtil;
import rich.util.combat.InvUtil;

public class CrystalAura extends ModuleStructure {

    private final SliderSettings targetRange = new SliderSettings("Target Range", "Range to find targets").setValue(10.0f).range(1, 16);
    private final SliderSettings placeRange = new SliderSettings("Place Range", "Range to place crystals").setValue(4.5f).range(1.0f, 6.0f);
    private final SliderSettings breakRange = new SliderSettings("Break Range", "Range to break crystals").setValue(4.5f).range(1.0f, 6.0f);
    private final SliderSettings minDamage = new SliderSettings("Min Damage", "Minimum damage to target").setValue(6.0f).range(0.0f, 20.0f);
    private final SliderSettings maxSelfDamage = new SliderSettings("Max Self Damage", "Maximum damage to yourself").setValue(6.0f).range(0.0f, 20.0f);
    private final BooleanSetting antiSuicide = new BooleanSetting("Anti Suicide", "Won't place/break if it would kill you").setValue(true);
    private final BooleanSetting doPlace = new BooleanSetting("Place", "Place crystals automatically").setValue(true);
    private final BooleanSetting doBreak = new BooleanSetting("Break", "Break crystals automatically").setValue(true);
    private final SelectSetting switchMode = new SelectSetting("Switch", "Auto switch mode").value("None", "Normal", "Silent").selected("Normal");
    private final BooleanSetting rotate = new BooleanSetting("Rotate", "Rotate towards crystals").setValue(true);
    private final BooleanSetting facePlace = new BooleanSetting("Face Place", "Face place when target is low").setValue(true);
    private final SliderSettings facePlaceHP = new SliderSettings("FP Health", "Health to start face placing").setValue(8.0f).range(0.0f, 20.0f).visible(() -> this.facePlace.isValue());
    private final SliderSettings placeDelay = new SliderSettings("Place Delay", "Delay in ticks between placements").setValue(0.0f).range(0, 10);
    private final SliderSettings breakDelay = new SliderSettings("Break Delay", "Delay in ticks between breaks").setValue(0.0f).range(0, 10);
    private final BooleanSetting pauseOnEat = new BooleanSetting("Pause Eat", "Pause while eating").setValue(false);

    private int placeTimer = 0;
    private int breakTimer = 0;

    public CrystalAura() {
        super("CrystalAura", "Crystal Aura", ModuleCategory.COMBAT);
        this.settings(this.targetRange, this.placeRange, this.breakRange, this.minDamage, this.maxSelfDamage, this.antiSuicide, this.doPlace, this.doBreak, this.switchMode, this.rotate, this.facePlace, this.facePlaceHP, this.placeDelay, this.breakDelay, this.pauseOnEat);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) return;
        if (pauseOnEat.isValue() && mc.player.isUsingItem()) return;

        if (placeTimer > 0) placeTimer--;
        if (breakTimer > 0) breakTimer--;

        boolean hasTarget = false;
        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (!player.isAlive()) continue;
            if (mc.player.distanceTo(player) > targetRange.getValue()) continue;
            hasTarget = true;
            break;
        }

        if (!hasTarget) return;

        if (doBreak.isValue() && breakTimer <= 0) {
            doBreakCrystal();
        }

        if (doPlace.isValue() && placeTimer <= 0) {
            doPlaceCrystal();
        }
    }

    private void doBreakCrystal() {
        EndCrystal crystal = CrystalUtil.findBestBreakCrystal(
            breakRange.getValue(),
            getMinDamage(),
            maxSelfDamage.getValue(),
            antiSuicide.isValue()
        );

        if (crystal == null) return;

        if (rotate.isValue()) {
            float[] rots = BlockUtil.getRotations(crystal.position());
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(rots[0], rots[1], mc.player.onGround(), mc.player.horizontalCollision));
        }

        mc.player.connection.send(ServerboundInteractPacket.createAttackPacket(crystal, mc.player.isShiftKeyDown()));
        mc.player.swing(InteractionHand.MAIN_HAND);
        breakTimer = (int) breakDelay.getValue();
    }

    private void doPlaceCrystal() {
        boolean shouldFacePlace = shouldFacePlace();
        BlockPos pos = CrystalUtil.findBestPlacePos(
            placeRange.getValue(),
            getMinDamage(),
            maxSelfDamage.getValue(),
            antiSuicide.isValue(),
            shouldFacePlace,
            1.5
        );

        if (pos == null) return;

        InteractionHand hand = InvUtil.getHand(Items.END_CRYSTAL);
        int slot = InvUtil.findInHotbar(Items.END_CRYSTAL);

        if (hand == null && slot == -1) return;

        int prevSlot = mc.player.getInventory().getSelectedSlot();
        boolean switched = false;

        if (hand == null && !switchMode.isSelected("None")) {
            InvUtil.swap(slot);
            hand = InteractionHand.MAIN_HAND;
            switched = true;
        }

        if (hand == null) return;

        BlockPos below = pos.below();
        Direction placeDir = Direction.UP;

        Vec3 hitVec = new Vec3(below.getX() + 0.5, below.getY() + 1.0, below.getZ() + 0.5);

        if (rotate.isValue()) {
            float[] rots = BlockUtil.getRotations(hitVec);
            mc.player.connection.send(new ServerboundMovePlayerPacket.Rot(rots[0], rots[1], mc.player.onGround(), mc.player.horizontalCollision));
        }

        BlockHitResult hitResult = new BlockHitResult(hitVec, placeDir, below, false);
        mc.gameMode.useItemOn(mc.player, hand, hitResult);
        mc.player.swing(hand);

        placeTimer = (int) placeDelay.getValue();

        if (switched && switchMode.isSelected("Silent")) {
            InvUtil.swap(prevSlot);
        }
    }

    private boolean shouldFacePlace() {
        if (!facePlace.isValue()) return false;
        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;
            if (mc.player.distanceTo(player) > targetRange.getValue()) continue;
            if (CrystalUtil.getTotalHealth(player) <= facePlaceHP.getValue()) return true;
        }
        return false;
    }

    private double getMinDamage() {
        return shouldFacePlace() ? Math.min(minDamage.getValue(), 1.5) : minDamage.getValue();
    }
}
