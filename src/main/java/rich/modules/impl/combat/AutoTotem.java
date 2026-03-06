
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import java.util.HashMap;
import java.util.Map;
import lombok.Generated;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import rich.events.api.EventHandler;
import rich.events.impl.InputEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.inventory.SwapExecutor;
import rich.util.inventory.SwapSettings;

public class AutoTotem
extends ModuleStructure {
    private final SelectSetting swapMode = new SelectSetting("Mode swap", "Totem swap method").value("Instant", "Legit").selected("Legit");
    private final SliderSettings healthThreshold = new SliderSettings("Health threshold", "Minimum health for totem swap").range(1, 20).setValue(6.0f);
    private final MultiSelectSetting triggers = new MultiSelectSetting("Triggers", "Totem swap conditions").value("Crystal", "Fall", "Anti oneshot", "Dynamite", "Minecart", "Elytra").selected("Crystal", "Fall", "Anti oneshot");
    private final MultiSelectSetting options = new MultiSelectSetting("Options", "Additional settings").value("Don't take if sphere", "Item return", "Save talismans").selected("Don't take if sphere", "Item return", "Save talismans");
    private final SliderSettings crystalDistance = new SliderSettings("Distance crystal", "Maximum crystal distance").range(1, 12).setValue(6.0f).visible(() -> this.triggers.isSelected("Crystal"));
    private final SliderSettings fallHeight = new SliderSettings("Height fall", "Minimum fall height").range(5, 50).setValue(15.0f).visible(() -> this.triggers.isSelected("Fall"));
    private final SliderSettings tntDistance = new SliderSettings("Distance dynamite", "Maximum dynamite distance").range(3, 25).setValue(6.0f).visible(() -> this.triggers.isSelected("Dynamite"));
    private final SliderSettings tntMinecartDistance = new SliderSettings("Distance minecart", "Maximum minecart distance").range(3, 15).setValue(6.0f).visible(() -> this.triggers.isSelected("Minecart"));
    private final SliderSettings elytraHealth = new SliderSettings("Health elytra", "Health threshold during elytra flight").range(1, 20).setValue(10.0f).visible(() -> this.triggers.isSelected("Elytra"));
    private final SwapExecutor executor = new SwapExecutor();
    private final Map<Integer, Double> playerLastY = new HashMap<Integer, Double>();
    private final Map<Integer, Double> playerFallStartY = new HashMap<Integer, Double>();
    private int savedSlotId = -1;
    private float fallStartY = 0.0f;
    private boolean wasFalling = false;
    private Player dangerousFallingPlayer = null;
    private Player dangerousElytraPlayer = null;

    public AutoTotem() {
        super("AutoTotem", "Auto Totem", ModuleCategory.COMBAT);
        this.settings(this.swapMode, this.healthThreshold, this.triggers, this.options, this.crystalDistance, this.fallHeight, this.tntDistance, this.tntMinecartDistance, this.elytraHealth);
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void activate() {
        this.savedSlotId = -1;
        this.fallStartY = 0.0f;
        this.wasFalling = false;
        this.playerLastY.clear();
        this.playerFallStartY.clear();
        this.dangerousFallingPlayer = null;
        this.dangerousElytraPlayer = null;
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        this.executor.cancel();
        this.savedSlotId = -1;
        this.playerLastY.clear();
        this.playerFallStartY.clear();
        this.dangerousFallingPlayer = null;
        this.dangerousElytraPlayer = null;
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (AutoTotem.mc.player == null || AutoTotem.mc.level == null) {
            return;
        }
        this.executor.tick();
        if (this.executor.isRunning()) {
            return;
        }
        this.updateFallTracking();
        this.updatePlayerFallTracking();
        boolean needTotem = this.shouldEquipTotem();
        boolean hasTotemInOffhand = AutoTotem.mc.player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING;
        boolean hasEnchantedTotemInOffhand = hasTotemInOffhand && AutoTotem.mc.player.getOffhandItem().isEnchanted();
        float currentHealth = AutoTotem.mc.player.getHealth();
        float threshold = this.healthThreshold.getValue();
        if (needTotem) {
            Slot regularTotemSlot;
            if (!hasTotemInOffhand) {
                this.equipTotem();
            } else if (this.options.isSelected("Save talismans") && hasEnchantedTotemInOffhand && (regularTotemSlot = this.findRegularTotemSlot()) != null) {
                this.swapToRegularTotem(regularTotemSlot);
            }
        } else {
            this.dangerousFallingPlayer = null;
            this.dangerousElytraPlayer = null;
            if (this.savedSlotId != -1 && hasTotemInOffhand && this.options.isSelected("Item return") && currentHealth > threshold) {
                this.returnSavedItem();
            } else if (!hasTotemInOffhand) {
                this.savedSlotId = -1;
            }
        }
    }

    @EventHandler
    public void onInput(InputEvent e) {
        if (AutoTotem.mc.player == null) {
            return;
        }
        if (this.executor.isBlocking()) {
            e.setDirectionalLow(false, false, false, false);
            e.setJumping(false);
            AutoTotem.mc.player.setSprinting(false);
        }
    }

    private void updateFallTracking() {
        boolean isFalling;
        boolean bl = isFalling = AutoTotem.mc.player.getDeltaMovement().y < -0.1 && !AutoTotem.mc.player.onGround() && !AutoTotem.mc.player.onClimbable() && !AutoTotem.mc.player.isInWater() && !AutoTotem.mc.player.isFallFlying();
        if (isFalling && !this.wasFalling) {
            this.fallStartY = (float)AutoTotem.mc.player.getY();
        }
        if (AutoTotem.mc.player.onGround() || AutoTotem.mc.player.isInWater() || AutoTotem.mc.player.onClimbable()) {
            this.fallStartY = (float)AutoTotem.mc.player.getY();
        }
        this.wasFalling = isFalling;
    }

    private void updatePlayerFallTracking() {
        if (AutoTotem.mc.level == null) {
            return;
        }
        for (Player player : AutoTotem.mc.level.players()) {
            boolean isOnGroundOrWater;
            if (player == AutoTotem.mc.player) continue;
            int id = player.getId();
            double currentY = player.getY();
            Double lastY = this.playerLastY.get(id);
            boolean isGoingDown = lastY != null && currentY < lastY - 0.01;
            boolean bl = isOnGroundOrWater = player.onGround() || player.isInWater() || player.onClimbable();
            if (isGoingDown && !isOnGroundOrWater && !player.isFallFlying()) {
                if (!this.playerFallStartY.containsKey(id)) {
                    this.playerFallStartY.put(id, lastY);
                }
            } else if (isOnGroundOrWater) {
                this.playerFallStartY.remove(id);
            }
            this.playerLastY.put(id, currentY);
        }
        this.playerLastY.entrySet().removeIf(entry -> {
            for (Player player : AutoTotem.mc.level.players()) {
                if (player.getId() != ((Integer)entry.getKey()).intValue()) continue;
                return false;
            }
            return true;
        });
        this.playerFallStartY.entrySet().removeIf(entry -> {
            for (Player player : AutoTotem.mc.level.players()) {
                if (player.getId() != ((Integer)entry.getKey()).intValue()) continue;
                return false;
            }
            return true;
        });
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private boolean shouldEquipTotem() {
        float fallDistance;
        float threshold;
        float health = AutoTotem.mc.player.getHealth();
        if (health <= (threshold = this.healthThreshold.getValue())) {
            return true;
        }
        if (this.triggers.isSelected("Elytra") && AutoTotem.mc.player.isFallFlying() && health <= this.elytraHealth.getValue()) {
            return true;
        }
        if (this.triggers.isSelected("Fall") && (fallDistance = this.fallStartY - (float)AutoTotem.mc.player.getY()) >= this.fallHeight.getValue() && AutoTotem.mc.player.getDeltaMovement().y < -0.1) {
            return true;
        }
        if (this.triggers.isSelected("Anti oneshot") && this.checkOneshotDanger()) {
            return true;
        }
        if (this.triggers.isSelected("Crystal") && this.checkCrystalDanger()) {
            return true;
        }
        if (this.triggers.isSelected("Dynamite") && this.checkTntDanger()) {
            return true;
        }
        return this.triggers.isSelected("Minecart") && this.checkTntMinecartDanger();
    }

    private boolean hasHeadInOffhand() {
        return AutoTotem.mc.player.getOffhandItem().getItem() == Items.PLAYER_HEAD;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean checkCrystalDanger() {
        float health;
        if (this.options.isSelected("Don't take if sphere") && this.hasHeadInOffhand() && (health = AutoTotem.mc.player.getHealth()) > this.healthThreshold.getValue()) {
            return false;
        }
        double distance = this.crystalDistance.getValue();
        for (Entity entity : AutoTotem.mc.level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal) || !((double)AutoTotem.mc.player.distanceTo(entity) <= distance)) continue;
            return true;
        }
        return false;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private boolean checkOneshotDanger() {
        this.dangerousFallingPlayer = null;
        this.dangerousElytraPlayer = null;
        for (Player player : AutoTotem.mc.level.players()) {
            if (player == AutoTotem.mc.player) continue;
            if (this.isFallingPlayerDangerous(player)) {
                this.dangerousFallingPlayer = player;
                return true;
            }
            if (!this.checkElytraPlayer(player)) continue;
            this.dangerousElytraPlayer = player;
            return true;
        }
        return false;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isFallingPlayerDangerous(Player player) {
        if (player == AutoTotem.mc.player) {
            return false;
        }
        if (player.isFallFlying()) {
            return false;
        }
        Vec3 playerPos = AutoTotem.mc.player.position();
        double radius = 7.0;
        double height = 50.0;
        AABB checkZone = new AABB(playerPos.x - radius, playerPos.y, playerPos.z - radius, playerPos.x + radius, playerPos.y + height, playerPos.z + radius);
        if (!checkZone.intersects(player.getBoundingBox())) {
            return false;
        }
        if (player.getY() < AutoTotem.mc.player.getY()) {
            return false;
        }
        int id = player.getId();
        Double fallStart = this.playerFallStartY.get(id);
        if (fallStart == null) {
            return false;
        }
        double fallDistance = fallStart - player.getY();
        return fallDistance >= 3.0;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean checkElytraPlayer(Player player) {
        if (!player.isFallFlying()) {
            return false;
        }
        double distance = AutoTotem.mc.player.distanceTo(player);
        if (distance > 20.0) {
            return false;
        }
        Vec3 velocity = player.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 0.8) {
            return false;
        }
        Vec3 toMe = new Vec3(AutoTotem.mc.player.getX() - player.getX(), AutoTotem.mc.player.getY() - player.getY(), AutoTotem.mc.player.getZ() - player.getZ()).normalize();
        Vec3 velocityNorm = velocity.normalize();
        double dot = velocityNorm.x * toMe.x + velocityNorm.y * toMe.y + velocityNorm.z * toMe.z;
        if (dot > 0.25) {
            double timeToReach = distance / speed;
            if (timeToReach < 2.5) {
                return true;
            }
            double predictedX = player.getX() + velocity.x * timeToReach;
            double predictedY = player.getY() + velocity.y * timeToReach;
            double predictedZ = player.getZ() + velocity.z * timeToReach;
            double predictedDist = Math.sqrt(Math.pow(predictedX - AutoTotem.mc.player.getX(), 2.0) + Math.pow(predictedY - AutoTotem.mc.player.getY(), 2.0) + Math.pow(predictedZ - AutoTotem.mc.player.getZ(), 2.0));
            return predictedDist < 6.0;
        }
        return false;
    }

    private boolean checkTntDanger() {
        double distance = this.tntDistance.getValue();
        for (Entity entity : AutoTotem.mc.level.entitiesForRendering()) {
            if (!(entity instanceof PrimedTnt) || !((double)AutoTotem.mc.player.distanceTo(entity) <= distance)) continue;
            return true;
        }
        return false;
    }

    private boolean checkTntMinecartDanger() {
        double distance = this.tntMinecartDistance.getValue();
        for (Entity entity : AutoTotem.mc.level.entitiesForRendering()) {
            if (!(entity instanceof MinecartTNT) || !((double)AutoTotem.mc.player.distanceTo(entity) <= distance)) continue;
            return true;
        }
        return false;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void swapSlots(int slot) {
        if (AutoTotem.mc.player == null || AutoTotem.mc.gameMode == null) {
            return;
        }
        int syncId = AutoTotem.mc.player.inventoryMenu.containerId;
        AutoTotem.mc.gameMode.handleInventoryMouseClick(syncId, slot, 40, ClickType.SWAP, AutoTotem.mc.player);
    }

    private boolean isScreenOpen() {
        return AutoTotem.mc.screen != null && !(AutoTotem.mc.screen instanceof ChatScreen);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void equipTotem() {
        boolean hasItemInOffhand;
        Slot totemSlot = this.findTotemSlot();
        if (totemSlot == null) {
            return;
        }
        boolean bl = hasItemInOffhand = !AutoTotem.mc.player.getOffhandItem().isEmpty() && AutoTotem.mc.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING;
        if (hasItemInOffhand && this.savedSlotId == -1) {
            this.savedSlotId = totemSlot.index;
        }
        int slotId = totemSlot.index;
        if (this.isScreenOpen()) {
            this.swapSlots(slotId);
        } else if (this.swapMode.isSelected("Instant")) {
            this.executor.execute(() -> this.swapSlots(slotId), SwapSettings.instantWithStop());
        } else {
            this.executor.execute(() -> this.swapSlots(slotId), SwapSettings.legit());
        }
    }

    private void swapToRegularTotem(Slot regularTotemSlot) {
        if (regularTotemSlot == null) {
            return;
        }
        int slotId = regularTotemSlot.index;
        if (this.isScreenOpen()) {
            this.swapSlots(slotId);
        } else if (this.swapMode.isSelected("Instant")) {
            this.executor.execute(() -> this.swapSlots(slotId), SwapSettings.instantWithStop());
        } else {
            this.executor.execute(() -> this.swapSlots(slotId), SwapSettings.legit());
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void returnSavedItem() {
        if (this.savedSlotId == -1) {
            return;
        }
        int slotId = this.savedSlotId;
        if (this.isScreenOpen()) {
            this.swapSlots(slotId);
            this.savedSlotId = -1;
        } else if (this.swapMode.isSelected("Instant")) {
            this.executor.execute(() -> {
                this.swapSlots(slotId);
                this.savedSlotId = -1;
            }, SwapSettings.instantWithStop());
        } else {
            this.executor.execute(() -> {
                this.swapSlots(slotId);
                this.savedSlotId = -1;
            }, SwapSettings.legit());
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private Slot findTotemSlot() {
        if (AutoTotem.mc.player == null) {
            return null;
        }
        Slot regularTotem = this.findRegularTotemSlot();
        if (regularTotem != null) {
            return regularTotem;
        }
        return this.findAnyTotemSlot();
    }

    private Slot findRegularTotemSlot() {
        Slot slot;
        int i;
        if (AutoTotem.mc.player == null) {
            return null;
        }
        for (i = 36; i <= 44; ++i) {
            slot = AutoTotem.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || !this.isRegularTotem(slot.getItem())) continue;
            return slot;
        }
        for (i = 9; i <= 35; ++i) {
            slot = AutoTotem.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || !this.isRegularTotem(slot.getItem())) continue;
            return slot;
        }
        return null;
    }

    private Slot findAnyTotemSlot() {
        Slot slot;
        int i;
        if (AutoTotem.mc.player == null) {
            return null;
        }
        for (i = 36; i <= 44; ++i) {
            slot = AutoTotem.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || !this.isTotem(slot.getItem())) continue;
            return slot;
        }
        for (i = 9; i <= 35; ++i) {
            slot = AutoTotem.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || !this.isTotem(slot.getItem())) continue;
            return slot;
        }
        return null;
    }

    private boolean isTotem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING;
    }

    private boolean isRegularTotem(ItemStack stack) {
        if (!this.isTotem(stack)) {
            return false;
        }
        if (this.options.isSelected("Save talismans")) {
            return !stack.isEnchanted();
        }
        return true;
    }

    @Generated
    public SelectSetting getSwapMode() {
        return this.swapMode;
    }

    @Generated
    public SliderSettings getHealthThreshold() {
        return this.healthThreshold;
    }

    @Generated
    public MultiSelectSetting getTriggers() {
        return this.triggers;
    }

    @Generated
    public MultiSelectSetting getOptions() {
        return this.options;
    }

    @Generated
    public SliderSettings getCrystalDistance() {
        return this.crystalDistance;
    }

    @Generated
    public SliderSettings getFallHeight() {
        return this.fallHeight;
    }

    @Generated
    public SliderSettings getTntDistance() {
        return this.tntDistance;
    }

    @Generated
    public SliderSettings getTntMinecartDistance() {
        return this.tntMinecartDistance;
    }

    @Generated
    public SliderSettings getElytraHealth() {
        return this.elytraHealth;
    }

    @Generated
    public SwapExecutor getExecutor() {
        return this.executor;
    }

    @Generated
    public Map<Integer, Double> getPlayerLastY() {
        return this.playerLastY;
    }

    @Generated
    public Map<Integer, Double> getPlayerFallStartY() {
        return this.playerFallStartY;
    }

    @Generated
    public int getSavedSlotId() {
        return this.savedSlotId;
    }

    @Generated
    public float getFallStartY() {
        return this.fallStartY;
    }

    @Generated
    public boolean isWasFalling() {
        return this.wasFalling;
    }

    @Generated
    public Player getDangerousFallingPlayer() {
        return this.dangerousFallingPlayer;
    }

    @Generated
    public Player getDangerousElytraPlayer() {
        return this.dangerousElytraPlayer;
    }
}

