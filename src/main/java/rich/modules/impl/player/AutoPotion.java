
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.block.Blocks;
import rich.events.api.EventHandler;
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.TickEvent;
import rich.mixin.ClientWorldAccessor;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.AngleConfig;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.util.math.TaskPriority;
import rich.util.timer.StopWatch;

public class AutoPotion
extends ModuleStructure {
    private final BooleanSetting autoOff = new BooleanSetting("Auto disable", "Auto disable module after use").setValue(false);
    private final MultiSelectSetting potions = new MultiSelectSetting("Throw", "Select potions for auto-throw").value("Force", "Speed", "Fire resistance").selected("Force", "Speed");
    private final StopWatch timer = new StopWatch();
    private boolean spoofed = false;
    private boolean isActivePotion = false;
    private int rotationTicks = 0;
    private int selectedSlot = -1;
    private final float THROW_PITCH = 90.0f;
    private final int ROTATION_WAIT_TICKS = 2;

    public AutoPotion() {
        super("AutoPotion", ModuleCategory.PLAYER);
        this.settings(this.potions, this.autoOff);
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        this.isActivePotion = false;
        this.spoofed = false;
        this.rotationTicks = 0;
        this.selectedSlot = -1;
        AngleConnection.INSTANCE.startReturning();
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private int findPotionSlot(PotionType type) {
        if (AutoPotion.mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            PotionContents potionComponent;
            ItemStack stack = AutoPotion.mc.player.getInventory().getItem(i);
            if (!stack.is(Items.SPLASH_POTION) || (potionComponent = (PotionContents)stack.get(DataComponents.POTION_CONTENTS)) == null) continue;
            for (MobEffectInstance effect : potionComponent.getAllEffects()) {
                if (effect.getEffect() != type.effect) continue;
                return i;
            }
        }
        return -1;
    }

    private boolean hasEffect(Holder<MobEffect> effect) {
        return AutoPotion.mc.player != null && AutoPotion.mc.player.hasEffect(effect);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean canBuff(PotionType type) {
        if (this.hasEffect(type.effect)) {
            return false;
        }
        return type.isEnabled(this) && this.findPotionSlot(type) != -1;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean canBuff() {
        if (AutoPotion.mc.player == null || AutoPotion.mc.level == null) {
            return false;
        }
        return (this.canBuff(PotionType.STRENGTH) || this.canBuff(PotionType.SPEED) || this.canBuff(PotionType.FIRE_RESISTANCE)) && AutoPotion.mc.player.onGround() && this.timer.finished(500.0);
    }

    private boolean isActive() {
        return this.isActivePotion || this.canBuff(PotionType.STRENGTH) || this.canBuff(PotionType.SPEED) || this.canBuff(PotionType.FIRE_RESISTANCE);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean shouldThrow() {
        if (AutoPotion.mc.player == null || AutoPotion.mc.level == null) {
            return false;
        }
        return this.isActive() && this.canBuff() && AutoPotion.mc.level.getBlockState(AutoPotion.mc.player.blockPosition().below()).getBlock() != Blocks.AIR;
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onRotationUpdate(RotationUpdateEvent event) {
        if (AutoPotion.mc.player == null || AutoPotion.mc.level == null) {
            return;
        }
        if (event.getType() == 0 && (this.shouldThrow() || this.spoofed)) {
            this.performRotation();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void performRotation() {
        Angle throwAngle = new Angle(AutoPotion.mc.player.getYRot(), 90.0f);
        AngleConfig config = new AngleConfig(new LinearConstructor(), true, true);
        AngleConnection.INSTANCE.rotateTo(throwAngle, 3, config, TaskPriority.HIGH_IMPORTANCE_1, this);
        if (!this.spoofed) {
            this.spoofed = true;
            this.isActivePotion = true;
            this.rotationTicks = 0;
            this.selectedSlot = AutoPotion.mc.player.getInventory().getSelectedSlot();
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent event) {
        if (AutoPotion.mc.player == null || AutoPotion.mc.level == null) {
            return;
        }
        if (this.isActivePotion && !this.shouldThrow() && !this.spoofed) {
            this.isActivePotion = false;
            if (this.autoOff.isValue()) {
                this.setState(false);
            }
        }
        if (this.spoofed) {
            this.processThrow();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processThrow() {
        boolean waitedEnough;
        ++this.rotationTicks;
        Angle currentRotation = AngleConnection.INSTANCE.getRotation();
        boolean rotationReady = currentRotation != null && currentRotation.getPitch() >= 80.0f;
        boolean bl = waitedEnough = this.rotationTicks >= 2;
        if (rotationReady && waitedEnough) {
            boolean threwAny = false;
            if (this.canBuff(PotionType.STRENGTH)) {
                this.throwPotion(PotionType.STRENGTH);
                threwAny = true;
            }
            if (this.canBuff(PotionType.SPEED)) {
                this.throwPotion(PotionType.SPEED);
                threwAny = true;
            }
            if (this.canBuff(PotionType.FIRE_RESISTANCE)) {
                this.throwPotion(PotionType.FIRE_RESISTANCE);
                threwAny = true;
            }
            if (this.selectedSlot != -1) {
                AutoPotion.mc.player.connection.send(new ServerboundSetCarriedItemPacket(this.selectedSlot));
            }
            this.timer.reset();
            this.spoofed = false;
            this.rotationTicks = 0;
            this.isActivePotion = false;
            if (this.autoOff.isValue() || !threwAny) {
                this.setState(false);
            }
        }
        if (this.rotationTicks > 10) {
            this.resetThrowState();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void resetThrowState() {
        this.spoofed = false;
        this.rotationTicks = 0;
        this.isActivePotion = false;
        if (this.selectedSlot != -1) {
            AutoPotion.mc.player.connection.send(new ServerboundSetCarriedItemPacket(this.selectedSlot));
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void throwPotion(PotionType type) {
        if (!type.isEnabled(this) || this.hasEffect(type.effect)) {
            return;
        }
        if (AutoPotion.mc.player == null || AutoPotion.mc.player.connection == null) {
            return;
        }
        int slot = this.findPotionSlot(type);
        if (slot == -1) {
            return;
        }
        AutoPotion.mc.player.connection.send(new ServerboundSetCarriedItemPacket(slot));
        this.sendSequencedPacket(id -> new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, id, AutoPotion.mc.player.getYRot(), 90.0f));
    }

    private void sendSequencedPacket(PredictiveAction packetCreator) {
        if (AutoPotion.mc.player == null || AutoPotion.mc.player.connection == null || AutoPotion.mc.level == null) {
            return;
        }
        try {
            ClientWorldAccessor worldAccessor = (ClientWorldAccessor)((Object)AutoPotion.mc.level);
            BlockStatePredictionHandler pendingUpdateManager = worldAccessor.getPendingUpdateManager().startPredicting();
            int sequence = pendingUpdateManager.currentSequence();
            AutoPotion.mc.player.connection.send(packetCreator.predict(sequence));
            pendingUpdateManager.close();
        }
        catch (Exception e) {
            AutoPotion.mc.player.connection.send(packetCreator.predict(0));
        }
    }

    private static enum PotionType {
        STRENGTH(MobEffects.STRENGTH, "Force"),
        SPEED(MobEffects.SPEED, "Speed"),
        FIRE_RESISTANCE(MobEffects.FIRE_RESISTANCE, "Fire resistance");

        final Holder<MobEffect> effect;
        final String settingName;

        private PotionType(Holder<MobEffect> effect, String settingName) {
            this.effect = effect;
            this.settingName = settingName;
        }

        public boolean isEnabled(AutoPotion module) {
            return module.potions.isSelected(this.settingName);
        }
    }
}

