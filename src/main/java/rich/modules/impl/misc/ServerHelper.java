/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.lwjgl.glfw.GLFW
 */
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import rich.events.api.EventHandler;
import rich.events.impl.RotationUpdateEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.ColorUtil;
import rich.util.inventory.InventoryUtils;
import rich.util.inventory.MovementController;
import rich.util.inventory.SwapSettings;
import rich.util.math.MathUtils;
import rich.util.render.Render3D;
import rich.util.repository.friend.FriendUtils;
import rich.util.string.PlayerInteractionHelper;
import rich.util.timer.StopWatch;

public class ServerHelper
extends ModuleStructure {
    private final SelectSetting mode = new SelectSetting("Server type", "Allows selecting server type").value("ReallyWorld", "HolyWorld", "FunTime").selected("FunTime");
    private final SelectSetting swapMode = new SelectSetting("Mode swap", "Item swap method").value("Instant", "Legit").selected("Legit");
    private final ColorSetting boxFillColor = new ColorSetting("Color fill", "Color box fill").value(ColorUtil.getColor(130, 32, 16, 40)).visible(() -> this.mode.isSelected("FunTime"));
    private final ColorSetting boxLineColor = new ColorSetting("Color lines", "Color box lines").value(ColorUtil.getColor(130, 32, 16, 255)).visible(() -> this.mode.isSelected("FunTime"));
    private final List<KeyBind> keyBindings = new ArrayList<KeyBind>();
    private final List<String> potionQueue = new ArrayList<String>();
    private final StopWatch potionTimer = new StopWatch();
    private final Map<String, ItemInfo> itemConfig = new HashMap<String, ItemInfo>();
    private final Map<String, Boolean> lastKeyStates = new HashMap<String, Boolean>();
    private final Map<String, Boolean> keyPressedThisTick = new HashMap<String, Boolean>();
    private int originalSlot = -1;
    private int targetSlot = -1;
    private ActionState actionState = ActionState.IDLE;
    private long actionTimer = 0L;
    private String pendingItemKey = null;
    private long stopMovementUntil = 0L;
    private boolean keysOverridden = false;
    private boolean wasForwardPressed;
    private boolean wasBackPressed;
    private boolean wasLeftPressed;
    private boolean wasRightPressed;
    private int originalSourceSlot = -1;
    private final MovementController movement = new MovementController();

    public ServerHelper() {
        super("Server Assist", "Server helper", ModuleCategory.MISC);
        this.initialize();
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    public void initialize() {
        this.settings(this.mode, this.swapMode, this.boxFillColor, this.boxLineColor);
        this.keyBindings.add(new KeyBind(Items.FIREWORK_STAR, new BindSetting("Anti Flight", "Anti flight key").visible(() -> this.mode.isSelected("ReallyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.FLOWER_BANNER_PATTERN, new BindSetting("Experience Scroll", "Experience scroll key").visible(() -> this.mode.isSelected("ReallyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.PRISMARINE_SHARD, new BindSetting("Explosive Trap", "Explosive trap key").visible(() -> this.mode.isSelected("HolyWorld")), 5.0f));
        this.keyBindings.add(new KeyBind(Items.POPPED_CHORUS_FRUIT, new BindSetting("Normal Trap", "Normal trap key").visible(() -> this.mode.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.NETHER_STAR, new BindSetting("Stun", "Stun key").visible(() -> this.mode.isSelected("HolyWorld")), 30.0f));
        this.keyBindings.add(new KeyBind(Items.FIRE_CHARGE, new BindSetting("Explosive thing", "Explosive thing key").visible(() -> this.mode.isSelected("HolyWorld")), 5.0f));
        this.keyBindings.add(new KeyBind(Items.SNOWBALL, new BindSetting("Frost Snowball", "Snowball key").visible(() -> this.mode.isSelected("HolyWorld") || this.mode.isSelected("FunTime")), 7.0f));
        this.keyBindings.add(new KeyBind(Items.PHANTOM_MEMBRANE, new BindSetting("Divine Aura", "Divine aura key").visible(() -> this.mode.isSelected("FunTime")), 2.0f));
        this.keyBindings.add(new KeyBind(Items.NETHERITE_SCRAP, new BindSetting("Trap", "Trap key").visible(() -> this.mode.isSelected("FunTime")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.DRIED_KELP, new BindSetting("Plast", "Plast key").visible(() -> this.mode.isSelected("FunTime")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.SUGAR, new BindSetting("Visible Dust", "Visible dust key").visible(() -> this.mode.isSelected("FunTime")), 10.0f));
        this.keyBindings.add(new KeyBind(Items.FIRE_CHARGE, new BindSetting("Fire tornado", "Fire tornado key").visible(() -> this.mode.isSelected("FunTime")), 10.0f));
        this.keyBindings.add(new KeyBind(Items.ENDER_EYE, new BindSetting("Disorientation", "Disorientation key").visible(() -> this.mode.isSelected("FunTime")), 10.0f));
        this.keyBindings.add(new KeyBind(Items.JACK_O_LANTERN, new BindSetting("Jack o'Lantern", "Jack o'lantern key").visible(() -> this.mode.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.EXPERIENCE_BOTTLE, new BindSetting("Experience bubble", "Experience bubble key").visible(() -> this.mode.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.PINK_SHULKER_BOX, new BindSetting("Backpack Level 1", "Backpack level 1 key").visible(() -> this.mode.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.BLUE_SHULKER_BOX, new BindSetting("Backpack Level 2", "Backpack level 2 key").visible(() -> this.mode.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.RED_SHULKER_BOX, new BindSetting("Backpack Level 3", "Backpack level 3 key").visible(() -> this.mode.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.PINK_SHULKER_BOX, new BindSetting("Backpack Level 4", "Backpack level 4 key").visible(() -> this.mode.isSelected("HolyWorld")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.SPLASH_POTION, new BindSetting("Firecracker", "Firecracker key").visible(() -> this.mode.isSelected("FunTime")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.SPLASH_POTION, new BindSetting("Holy Water", "Holy water key").visible(() -> this.mode.isSelected("FunTime")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.SPLASH_POTION, new BindSetting("Wrath Potion", "Wrath potion key").visible(() -> this.mode.isSelected("FunTime")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.SPLASH_POTION, new BindSetting("Paladin Potion", "Paladin potion key").visible(() -> this.mode.isSelected("FunTime")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.SPLASH_POTION, new BindSetting("Assassin Potion", "Assassin potion key").visible(() -> this.mode.isSelected("FunTime")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.SPLASH_POTION, new BindSetting("Radiation Potion", "Radiation potion key").visible(() -> this.mode.isSelected("FunTime")), 0.0f));
        this.keyBindings.add(new KeyBind(Items.SPLASH_POTION, new BindSetting("Sleeping Potion", "Sleeping potion key").visible(() -> this.mode.isSelected("FunTime")), 0.0f));
        this.keyBindings.forEach(bind -> this.settings(bind.setting));
        this.itemConfig.put("sugar", new ItemInfo(List.of("light flash", "radius: 10 blocks", "glowing", "blindness"), "visible dust", Items.SUGAR, "Visible Dust", true));
        this.itemConfig.put("disorientation", new ItemInfo(List.of("the closer the target, the longer the effect duration"), "disorientation", Items.ENDER_EYE, "Disorientation", true));
        this.itemConfig.put("trap", new ItemInfo(List.of("indestructible cage", "duration: 15 seconds"), "trap", Items.NETHERITE_SCRAP, "Trap", true));
        this.itemConfig.put("plast", new ItemInfo(List.of("indestructible wall", "vertical:", "horizontal:"), "plast", Items.DRIED_KELP, "Plast", true));
        this.itemConfig.put("fireSwirl", new ItemInfo(List.of("fire wave", "radius: 10 blocks", "ignition"), "fire tornado", Items.FIRE_CHARGE, "Fire tornado", true));
        this.itemConfig.put("snow", new ItemInfo(List.of("ice sphere", "radius: 7 blocks", "freezing", "weakness"), "frost snowball", Items.SNOWBALL, "Frost Snowball", true));
        this.itemConfig.put("bojaura", new ItemInfo(List.of("divine aura", "radius: 2 blocks", "remove all effects", "invisibility"), "divine aura", Items.PHANTOM_MEMBRANE, "Divine Aura", true));
        this.itemConfig.put("hlopushka", new ItemInfo(null, "firecracker", Items.SPLASH_POTION, "Firecracker", true, List.of(new EffectRequirement(MobEffects.SLOWNESS, 9), new EffectRequirement(MobEffects.SPEED, 4), new EffectRequirement(MobEffects.BLINDNESS, 9), new EffectRequirement(MobEffects.GLOWING, 0))));
        this.itemConfig.put("holywater", new ItemInfo(null, "holy water", Items.SPLASH_POTION, "Holy Water", true, List.of(new EffectRequirement(MobEffects.REGENERATION, 2), new EffectRequirement(MobEffects.INVISIBILITY, 1), new EffectRequirement(MobEffects.INSTANT_HEALTH, 1))));
        this.itemConfig.put("gnev", new ItemInfo(null, "wrath potion", Items.SPLASH_POTION, "Wrath Potion", true, List.of(new EffectRequirement(MobEffects.STRENGTH, 4), new EffectRequirement(MobEffects.SLOWNESS, 3))));
        this.itemConfig.put("paladin", new ItemInfo(null, "paladin potion", Items.SPLASH_POTION, "Paladin Potion", true, List.of(new EffectRequirement(MobEffects.RESISTANCE, 0), new EffectRequirement(MobEffects.FIRE_RESISTANCE, 0), new EffectRequirement(MobEffects.INVISIBILITY, 0), new EffectRequirement(MobEffects.HEALTH_BOOST, 2))));
        this.itemConfig.put("assassin", new ItemInfo(null, "assassin potion", Items.SPLASH_POTION, "Assassin Potion", true, List.of(new EffectRequirement(MobEffects.STRENGTH, 3), new EffectRequirement(MobEffects.SPEED, 2), new EffectRequirement(MobEffects.HASTE, 0), new EffectRequirement(MobEffects.INSTANT_DAMAGE, 1))));
        this.itemConfig.put("radiation", new ItemInfo(null, "radiation potion", Items.SPLASH_POTION, "Radiation Potion", true, List.of(new EffectRequirement(MobEffects.POISON, 1), new EffectRequirement(MobEffects.WITHER, 1), new EffectRequirement(MobEffects.SLOWNESS, 2), new EffectRequirement(MobEffects.HUNGER, 4), new EffectRequirement(MobEffects.GLOWING, 0))));
        this.itemConfig.put("snotvornoe", new ItemInfo(null, "sleeping potion", Items.SPLASH_POTION, "Sleeping Potion", true, List.of(new EffectRequirement(MobEffects.WEAKNESS, 1), new EffectRequirement(MobEffects.MINING_FATIGUE, 1), new EffectRequirement(MobEffects.WITHER, 2), new EffectRequirement(MobEffects.BLINDNESS, 0))));
        this.itemConfig.put("antiflight", new ItemInfo("anti flight", Items.FIREWORK_STAR, "Anti Flight"));
        this.itemConfig.put("expscroll", new ItemInfo("experience scroll", Items.FLOWER_BANNER_PATTERN, "Experience Scroll"));
        this.itemConfig.put("dtrap", new ItemInfo("explosive trap", Items.PRISMARINE_SHARD, "Explosive Trap"));
        this.itemConfig.put("trap_holy", new ItemInfo("trap", Items.POPPED_CHORUS_FRUIT, "Normal Trap"));
        this.itemConfig.put("stan", new ItemInfo("stun", Items.NETHER_STAR, "Stun"));
        this.itemConfig.put("ditem", new ItemInfo("explosive", Items.FIRE_CHARGE, "Explosive thing"));
        this.itemConfig.put("tikva", new ItemInfo("jack o'lantern", Items.JACK_O_LANTERN, "Jack o'Lantern"));
        this.itemConfig.put("exp", new ItemInfo("experience bottle", Items.EXPERIENCE_BOTTLE, "Experience bubble"));
        this.itemConfig.put("shulker1", new ItemInfo("backpack (level i)", Items.PINK_SHULKER_BOX, "Backpack Level 1"));
        this.itemConfig.put("shulker2", new ItemInfo("backpack (level ii)", Items.BLUE_SHULKER_BOX, "Backpack Level 2"));
        this.itemConfig.put("shulker3", new ItemInfo("backpack (level iii)", Items.RED_SHULKER_BOX, "Backpack Level 3"));
        this.itemConfig.put("shulker4", new ItemInfo("backpack (level iv)", Items.PINK_SHULKER_BOX, "Backpack Level 4"));
        this.itemConfig.keySet().forEach(key -> {
            this.lastKeyStates.put((String)key, false);
            this.keyPressedThisTick.put((String)key, false);
        });
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void activate() {
        this.potionQueue.clear();
        this.potionTimer.reset();
        this.lastKeyStates.replaceAll((k, v) -> false);
        this.keyPressedThisTick.replaceAll((k, v) -> false);
        this.actionState = ActionState.IDLE;
        this.originalSlot = -1;
        this.targetSlot = -1;
        this.originalSourceSlot = -1;
        this.pendingItemKey = null;
        this.stopMovementUntil = 0L;
        this.keysOverridden = false;
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        this.lastKeyStates.replaceAll((k, v) -> false);
        this.keyPressedThisTick.replaceAll((k, v) -> false);
        this.potionQueue.clear();
        this.potionTimer.reset();
        this.actionState = ActionState.IDLE;
        this.originalSlot = -1;
        this.targetSlot = -1;
        this.originalSourceSlot = -1;
        this.pendingItemKey = null;
        this.stopMovementUntil = 0L;
        if (this.keysOverridden) {
            ServerHelper.mc.options.keyUp.setDown(false);
            ServerHelper.mc.options.keyDown.setDown(false);
            ServerHelper.mc.options.keyLeft.setDown(false);
            ServerHelper.mc.options.keyRight.setDown(false);
        }
        this.keysOverridden = false;
        this.movement.reset();
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private SwapSettings buildSettings() {
        return switch (this.swapMode.getSelected()) {
            case "Instant" -> SwapSettings.instant();
            default -> SwapSettings.legit();
        };
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isKeyPressed(int keyCode) {
        if (keyCode == -1) {
            return false;
        }
        long handle = mc.getWindow().handle();
        if (keyCode >= 0 && keyCode <= 7) {
            return GLFW.glfwGetMouseButton((long)handle, (int)keyCode) == 1;
        }
        return GLFW.glfwGetKey((long)handle, (int)keyCode) == 1;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void blockMovement() {
        ServerHelper.mc.options.keyUp.setDown(false);
        ServerHelper.mc.options.keyDown.setDown(false);
        ServerHelper.mc.options.keyLeft.setDown(false);
        ServerHelper.mc.options.keyRight.setDown(false);
        if (ServerHelper.mc.player != null && ServerHelper.mc.player.isSprinting()) {
            ServerHelper.mc.player.setSprinting(false);
        }
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onRotationUpdate(RotationUpdateEvent e) {
        boolean noMoveOrAction;
        if (ServerHelper.mc.screen != null) {
            return;
        }
        boolean bl = noMoveOrAction = System.currentTimeMillis() < this.stopMovementUntil || this.actionState != ActionState.IDLE && this.actionState != ActionState.SPEEDING_UP;
        if (noMoveOrAction) {
            this.blockMovement();
        }
        this.processKeyBindings();
        if (this.actionState != ActionState.IDLE) {
            this.processItemAction();
        }
        this.processItemQueue();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processKeyBindings() {
        for (KeyBind bind : this.keyBindings) {
            Slot slot;
            ItemInfo info;
            String key = this.getKeyFromBinding(bind.setting.getName());
            if (key == null || !bind.setting.getVisible().get().booleanValue()) continue;
            boolean currentKey = this.isKeyPressed(bind.setting.getKey());
            boolean wasPressedLastTick = this.lastKeyStates.getOrDefault(key, false);
            if (!currentKey && wasPressedLastTick && (info = this.itemConfig.get(key)) != null && (slot = this.findSlotByItem(info)) != null) {
                ItemStack stack = slot.getItem();
                if (!ServerHelper.mc.player.getCooldowns().isOnCooldown(stack) && !this.potionQueue.contains(key)) {
                    this.potionQueue.add(key);
                }
            }
            this.lastKeyStates.put(key, currentKey);
            this.keyPressedThisTick.put(key, currentKey);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processItemQueue() {
        String potionKey;
        ItemInfo info;
        if (this.actionState == ActionState.IDLE && !this.potionQueue.isEmpty() && this.potionTimer.finished(150.0) && (info = this.itemConfig.get(potionKey = this.potionQueue.remove(0))) != null) {
            Slot slot = this.findSlotByItem(info);
            if (slot != null) {
                ItemStack stack = slot.getItem();
                if (!ServerHelper.mc.player.getCooldowns().isOnCooldown(stack)) {
                    this.startItemUse(slot, info);
                }
            }
            this.potionTimer.reset();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void startItemUse(Slot slot, ItemInfo info) {
        this.originalSlot = ServerHelper.mc.player.getInventory().getSelectedSlot();
        this.originalSourceSlot = slot.index;
        this.targetSlot = slot.index;
        this.pendingItemKey = info.displayName;
        boolean needsSwap = !(slot.index >= 0 && slot.index < 9 || slot.index >= 36 && slot.index < 45);
        long handle = mc.getWindow().handle();
        this.wasForwardPressed = GLFW.glfwGetKey((long)handle, (int)ServerHelper.mc.options.keyUp.getDefaultKey().getValue()) == 1;
        this.wasBackPressed = GLFW.glfwGetKey((long)handle, (int)ServerHelper.mc.options.keyDown.getDefaultKey().getValue()) == 1;
        this.wasLeftPressed = GLFW.glfwGetKey((long)handle, (int)ServerHelper.mc.options.keyLeft.getDefaultKey().getValue()) == 1;
        this.wasRightPressed = GLFW.glfwGetKey((long)handle, (int)ServerHelper.mc.options.keyRight.getDefaultKey().getValue()) == 1;
        SwapSettings settings = this.buildSettings();
        if (needsSwap && settings.shouldStopMovement()) {
            this.actionState = ActionState.SLOWING_DOWN;
            this.actionTimer = System.currentTimeMillis();
            this.stopMovementUntil = System.currentTimeMillis() + (long)settings.randomWaitStopDelay();
            this.keysOverridden = true;
            this.movement.saveState();
            this.movement.block();
        } else {
            this.actionState = ActionState.SWAP_TO_ITEM;
            this.actionTimer = System.currentTimeMillis();
            this.stopMovementUntil = System.currentTimeMillis() + 95L;
            this.keysOverridden = true;
            this.blockMovement();
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processItemAction() {
        long currentTime = System.currentTimeMillis();
        long elapsed = currentTime - this.actionTimer;
        SwapSettings settings = this.buildSettings();
        switch (this.actionState.ordinal()) {
            case 1: {
                this.blockMovement();
                if (elapsed <= (long)settings.randomPreStopDelay()) break;
                this.actionState = ActionState.WAITING_STOP;
                break;
            }
            case 2: {
                this.blockMovement();
                double velocityX = Math.abs(ServerHelper.mc.player.getDeltaMovement().x);
                double velocityZ = Math.abs(ServerHelper.mc.player.getDeltaMovement().z);
                if (!(velocityX < settings.getVelocityThreshold() && velocityZ < settings.getVelocityThreshold()) && elapsed <= (long)settings.randomWaitStopDelay()) break;
                this.actionState = ActionState.SWAP_TO_ITEM;
                this.actionTimer = currentTime;
                break;
            }
            case 3: {
                if (elapsed <= (long)settings.randomPreSwapDelay()) break;
                this.performSwapToItem();
                this.actionState = ActionState.USE_ITEM;
                this.actionTimer = currentTime;
                break;
            }
            case 4: {
                if (elapsed <= 40L) break;
                this.performUseItem();
                this.actionState = ActionState.SWAP_BACK;
                this.actionTimer = currentTime;
                break;
            }
            case 5: {
                if (elapsed <= (long)settings.randomPostSwapDelay()) break;
                this.performSwapBack();
                this.restoreKeyStates();
                this.actionState = ActionState.SPEEDING_UP;
                this.actionTimer = currentTime;
                break;
            }
            case 6: {
                if (elapsed <= (long)settings.randomResumeDelay()) break;
                this.actionState = ActionState.IDLE;
                this.originalSlot = -1;
                this.targetSlot = -1;
                this.originalSourceSlot = -1;
                this.pendingItemKey = null;
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void performSwapToItem() {
        if (this.targetSlot >= 0 && this.targetSlot < 9) {
            ServerHelper.mc.player.connection.send(new ServerboundSetCarriedItemPacket(this.targetSlot));
            ServerHelper.mc.player.getInventory().setSelectedSlot(this.targetSlot);
        } else if (this.targetSlot >= 36 && this.targetSlot < 45) {
            int hotbarSlot = this.targetSlot - 36;
            ServerHelper.mc.player.connection.send(new ServerboundSetCarriedItemPacket(hotbarSlot));
            ServerHelper.mc.player.getInventory().setSelectedSlot(hotbarSlot);
            this.targetSlot = hotbarSlot;
        } else {
            int swapSlot = 8;
            InventoryUtils.click(this.targetSlot, swapSlot, ClickType.SWAP);
            this.targetSlot = swapSlot;
            ServerHelper.mc.player.connection.send(new ServerboundSetCarriedItemPacket(swapSlot));
            ServerHelper.mc.player.getInventory().setSelectedSlot(swapSlot);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void performUseItem() {
        Angle angle = MathAngle.cameraAngle();
        ServerHelper.mc.player.connection.send(new ServerboundUseItemPacket(InteractionHand.MAIN_HAND, 0, angle.getYaw(), angle.getPitch()));
        ServerHelper.mc.player.swing(InteractionHand.MAIN_HAND);
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void performSwapBack() {
        boolean wasFromInventory;
        boolean bl = wasFromInventory = !(this.originalSourceSlot >= 0 && this.originalSourceSlot < 9 || this.originalSourceSlot >= 36 && this.originalSourceSlot < 45);
        if (wasFromInventory) {
            if (this.targetSlot >= 0 && this.targetSlot < 9) {
                InventoryUtils.click(this.originalSourceSlot, this.targetSlot, ClickType.SWAP);
            }
        } else if (this.originalSourceSlot >= 36 && this.originalSourceSlot < 45) {
            int hotbarSlot = this.originalSourceSlot - 36;
            if (this.targetSlot != hotbarSlot) {
                ServerHelper.mc.player.connection.send(new ServerboundSetCarriedItemPacket(hotbarSlot));
                ServerHelper.mc.player.getInventory().setSelectedSlot(hotbarSlot);
            }
        } else if (this.originalSourceSlot >= 0 && this.originalSourceSlot < 9 && this.targetSlot != this.originalSourceSlot) {
            ServerHelper.mc.player.connection.send(new ServerboundSetCarriedItemPacket(this.originalSourceSlot));
            ServerHelper.mc.player.getInventory().setSelectedSlot(this.originalSourceSlot);
        }
        if (ServerHelper.mc.player.getInventory().getSelectedSlot() != this.originalSlot) {
            ServerHelper.mc.player.connection.send(new ServerboundSetCarriedItemPacket(this.originalSlot));
            ServerHelper.mc.player.getInventory().setSelectedSlot(this.originalSlot);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void restoreKeyStates() {
        if (!this.keysOverridden) {
            return;
        }
        ServerHelper.mc.options.keyUp.setDown(this.wasForwardPressed);
        ServerHelper.mc.options.keyDown.setDown(this.wasBackPressed);
        ServerHelper.mc.options.keyLeft.setDown(this.wasLeftPressed);
        ServerHelper.mc.options.keyRight.setDown(this.wasRightPressed);
        this.keysOverridden = false;
        this.movement.reset();
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        this.keyBindings.stream().filter(bind -> PlayerInteractionHelper.isKey(bind.setting) && this.findSlotByBinding((KeyBind)((Object)bind)) != null).forEach(bind -> {
            BlockPos playerPos = ServerHelper.mc.player.blockPosition();
            Vec3 smooth = MathUtils.interpolate(Vec3.atLowerCornerOf((Vec3i)BlockPos.containing((double)ServerHelper.mc.player.xo, (double)ServerHelper.mc.player.yo, (double)ServerHelper.mc.player.zo)), Vec3.atLowerCornerOf((Vec3i)playerPos)).subtract(Vec3.atLowerCornerOf((Vec3i)playerPos));
            int lineColor = this.mode.isSelected("FunTime") ? this.boxLineColor.getColor() : this.getDefaultLineColor();
            int fillColor = this.mode.isSelected("FunTime") ? this.boxFillColor.getColor() : this.getDefaultFillColor();
            switch (bind.setting.getName()) {
                case "Trap": 
                case "Normal Trap": {
                    this.drawItemCube(playerPos, smooth, 1.99f, lineColor, fillColor);
                    break;
                }
                case "Disorientation": 
                case "Fire tornado": 
                case "Visible Dust": {
                    Render3D.drawRadiusCircle(MathUtils.interpolate(ServerHelper.mc.player), bind.distance, this.validDistance(bind.distance) ? ColorUtil.getFriendColor() : lineColor);
                    break;
                }
                case "Explosive thing": {
                    Render3D.drawRadiusCircle(MathUtils.interpolate(ServerHelper.mc.player), 5.0f, this.validDistance(5.0f) ? ColorUtil.getFriendColor() : lineColor);
                    break;
                }
                case "Plast": {
                    Render3D.drawPlastShape(playerPos, smooth, lineColor, fillColor);
                    break;
                }
                case "Explosive Trap": {
                    this.drawItemCube(playerPos, smooth, 3.99f, lineColor, fillColor);
                    break;
                }
                case "Stun": {
                    this.drawItemCube(playerPos, smooth, 15.01f, lineColor, fillColor);
                    break;
                }
                case "Frost Snowball": {
                    Render3D.drawRadiusCircle(MathUtils.interpolate(ServerHelper.mc.player), 7.0f, this.validDistance(7.0f) ? ColorUtil.getFriendColor() : lineColor);
                    break;
                }
                case "Divine Aura": {
                    Render3D.drawRadiusCircle(MathUtils.interpolate(ServerHelper.mc.player), 2.0f, this.validDistance(2.0f) ? ColorUtil.getFriendColor() : lineColor);
                }
            }
        });
    }

    private int getDefaultLineColor() {
        return -8249328;
    }

    private int getDefaultFillColor() {
        return 679616528;
    }

    private void drawItemCube(BlockPos playerPos, Vec3 smooth, float size, int lineColor, int fillColor) {
        AABB box = new AABB(playerPos.above()).move(smooth).inflate(size);
        boolean inBox = ServerHelper.mc.level.players().stream().anyMatch(player -> player != ServerHelper.mc.player && box.intersects(player.getBoundingBox()) && !FriendUtils.isFriend(player));
        if (inBox) {
            Render3D.drawBoxWithCrossFull(box, ColorUtil.getFriendColor(), ColorUtil.multAlpha(ColorUtil.getFriendColor(), 0.15f), 2.0f);
        } else {
            Render3D.drawBoxWithCrossFull(box, lineColor, fillColor, 2.0f);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private List<String> getLore(ItemStack stack) {
        ArrayList<String> lore = new ArrayList<String>();
        if (stack == null || stack.isEmpty()) {
            return lore;
        }
        try {
            ItemLore loreComponent = (ItemLore)stack.get(DataComponents.LORE);
            if (loreComponent != null) {
                for (Component text : loreComponent.lines()) {
                    String line = this.getCleanName(text);
                    if (line.isEmpty()) continue;
                    lore.add(line);
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return lore;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean matchesLore(ItemStack stack, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return false;
        }
        List<String> lore = this.getLore(stack);
        if (lore.isEmpty()) {
            return false;
        }
        String fullLore = String.join((CharSequence)" ", lore).toLowerCase();
        int matchCount = 0;
        for (String keyword : keywords) {
            if (!fullLore.contains(keyword.toLowerCase())) continue;
            ++matchCount;
        }
        return matchCount >= Math.min(2, keywords.size());
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private Map<Holder<MobEffect>, Integer> getPotionEffects(ItemStack stack) {
        HashMap<Holder<MobEffect>, Integer> effects = new HashMap<Holder<MobEffect>, Integer>();
        if (stack == null || stack.isEmpty()) {
            return effects;
        }
        PotionContents potionContents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) {
            return effects;
        }
        for (MobEffectInstance effect : potionContents.customEffects()) {
            effects.put(effect.getEffect(), effect.getAmplifier());
        }
        return effects;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean matchesPotionEffects(ItemStack stack, List<EffectRequirement> requirements) {
        if (requirements == null || requirements.isEmpty()) {
            return false;
        }
        if (stack.getItem() != Items.SPLASH_POTION && stack.getItem() != Items.LINGERING_POTION) {
            return false;
        }
        Map<Holder<MobEffect>, Integer> effects = this.getPotionEffects(stack);
        if (effects.isEmpty()) {
            return false;
        }
        int matchCount = 0;
        for (EffectRequirement req : requirements) {
            Integer amplifier = effects.get(req.effect);
            if (amplifier == null || amplifier < req.minAmplifier) continue;
            ++matchCount;
        }
        return matchCount >= Math.min(2, requirements.size());
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private Slot findSlotByItem(ItemInfo info) {
        if (this.mode.isSelected("FunTime") && info.funTimeOnly) {
            Slot loreMatch;
            Slot effectMatch;
            if (info.effectRequirements != null && !info.effectRequirements.isEmpty() && (effectMatch = InventoryUtils.findSlot(s -> {
                ItemStack stack = s.getItem();
                if (stack.isEmpty() || !stack.getItem().equals(info.item)) {
                    return false;
                }
                return this.matchesPotionEffects(stack, info.effectRequirements);
            })) != null) {
                return effectMatch;
            }
            if (info.loreKeywords != null && !info.loreKeywords.isEmpty() && (loreMatch = InventoryUtils.findSlot(s -> {
                ItemStack stack = s.getItem();
                if (stack.isEmpty() || !stack.getItem().equals(info.item)) {
                    return false;
                }
                return this.matchesLore(stack, info.loreKeywords);
            })) != null) {
                return loreMatch;
            }
            if (info.nameFallback != null && !info.nameFallback.isEmpty()) {
                return InventoryUtils.findSlot(s -> {
                    ItemStack stack = s.getItem();
                    if (stack.isEmpty() || !stack.getItem().equals(info.item)) {
                        return false;
                    }
                    List<String> lore = this.getLore(stack);
                    if (!lore.isEmpty()) {
                        String fullLore = String.join((CharSequence)" ", lore).toLowerCase();
                        return fullLore.contains(info.nameFallback.toLowerCase());
                    }
                    return this.getCleanName(stack.getHoverName()).contains(info.nameFallback.toLowerCase());
                });
            }
            return null;
        }
        if (info.nameFallback != null && !info.nameFallback.isEmpty()) {
            return InventoryUtils.findSlot(s -> {
                ItemStack stack = s.getItem();
                if (stack.isEmpty() || !stack.getItem().equals(info.item)) {
                    return false;
                }
                return this.getCleanName(stack.getHoverName()).contains(info.nameFallback.toLowerCase());
            });
        }
        return null;
    }

    private Slot findSlotByBinding(KeyBind bind) {
        ItemInfo info;
        String key = this.getKeyFromBinding(bind.setting.getName());
        if (key != null && (info = this.itemConfig.get(key)) != null) {
            return this.findSlotByItem(info);
        }
        return InventoryUtils.findSlot(s -> s.getItem().getItem().equals(bind.item));
    }

    private String getCleanName(Component name) {
        return name.getString().toLowerCase().replaceAll("\u00a7[0-9a-fk-or]", "");
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private String getKeyFromBinding(String bindingName) {
        return switch (bindingName) {
            case "Anti Flight" -> "antiflight";
            case "Experience Scroll" -> "expscroll";
            case "Explosive Trap" -> "dtrap";
            case "Normal Trap" -> "trap_holy";
            case "Stun" -> "stan";
            case "Explosive thing" -> "ditem";
            case "Frost Snowball" -> "snow";
            case "Divine Aura" -> "bojaura";
            case "Trap" -> "trap";
            case "Plast" -> "plast";
            case "Visible Dust" -> "sugar";
            case "Fire tornado" -> "fireSwirl";
            case "Disorientation" -> "disorientation";
            case "Jack o'Lantern" -> "tikva";
            case "Experience bubble" -> "exp";
            case "Backpack Level 1" -> "shulker1";
            case "Backpack Level 2" -> "shulker2";
            case "Backpack Level 3" -> "shulker3";
            case "Backpack Level 4" -> "shulker4";
            case "Firecracker" -> "hlopushka";
            case "Holy Water" -> "holywater";
            case "Wrath Potion" -> "gnev";
            case "Paladin Potion" -> "paladin";
            case "Assassin Potion" -> "assassin";
            case "Radiation Potion" -> "radiation";
            case "Sleeping Potion" -> "snotvornoe";
            default -> null;
        };
    }

    private boolean validDistance(float dist) {
        return dist == 0.0f || ServerHelper.mc.level.players().stream().anyMatch(p -> p != ServerHelper.mc.player && !FriendUtils.isFriend(p) && ServerHelper.mc.player.distanceTo((Entity)p) <= dist);
    }

    public BindSetting getSetting(String name) {
        return this.keyBindings.stream().filter(bind -> bind.setting().getName().equals(name)).map(KeyBind::setting).findFirst().orElse(null);
    }

    @Generated
    public SelectSetting getMode() {
        return this.mode;
    }

    @Generated
    public SelectSetting getSwapMode() {
        return this.swapMode;
    }

    @Generated
    public ColorSetting getBoxFillColor() {
        return this.boxFillColor;
    }

    @Generated
    public ColorSetting getBoxLineColor() {
        return this.boxLineColor;
    }

    @Generated
    public List<KeyBind> getKeyBindings() {
        return this.keyBindings;
    }

    @Generated
    public List<String> getPotionQueue() {
        return this.potionQueue;
    }

    @Generated
    public StopWatch getPotionTimer() {
        return this.potionTimer;
    }

    @Generated
    public Map<String, ItemInfo> getItemConfig() {
        return this.itemConfig;
    }

    @Generated
    public Map<String, Boolean> getLastKeyStates() {
        return this.lastKeyStates;
    }

    @Generated
    public Map<String, Boolean> getKeyPressedThisTick() {
        return this.keyPressedThisTick;
    }

    @Generated
    public int getOriginalSlot() {
        return this.originalSlot;
    }

    @Generated
    public int getTargetSlot() {
        return this.targetSlot;
    }

    @Generated
    public ActionState getActionState() {
        return this.actionState;
    }

    @Generated
    public long getActionTimer() {
        return this.actionTimer;
    }

    @Generated
    public String getPendingItemKey() {
        return this.pendingItemKey;
    }

    @Generated
    public long getStopMovementUntil() {
        return this.stopMovementUntil;
    }

    @Generated
    public boolean isKeysOverridden() {
        return this.keysOverridden;
    }

    @Generated
    public boolean isWasForwardPressed() {
        return this.wasForwardPressed;
    }

    @Generated
    public boolean isWasBackPressed() {
        return this.wasBackPressed;
    }

    @Generated
    public boolean isWasLeftPressed() {
        return this.wasLeftPressed;
    }

    @Generated
    public boolean isWasRightPressed() {
        return this.wasRightPressed;
    }

    @Generated
    public int getOriginalSourceSlot() {
        return this.originalSourceSlot;
    }

    @Generated
    public MovementController getMovement() {
        return this.movement;
    }

    private static enum ActionState {
        IDLE,
        SLOWING_DOWN,
        WAITING_STOP,
        SWAP_TO_ITEM,
        USE_ITEM,
        SWAP_BACK,
        SPEEDING_UP;

    }

    public record KeyBind(Item item, BindSetting setting, float distance) {
    }

    private static class ItemInfo {
        List<String> loreKeywords;
        String nameFallback;
        Item item;
        String displayName;
        boolean funTimeOnly;
        List<EffectRequirement> effectRequirements;

        ItemInfo(List<String> loreKeywords, String nameFallback, Item item, String displayName, boolean funTimeOnly) {
            this.loreKeywords = loreKeywords;
            this.nameFallback = nameFallback;
            this.item = item;
            this.displayName = displayName;
            this.funTimeOnly = funTimeOnly;
            this.effectRequirements = null;
        }

        ItemInfo(List<String> loreKeywords, String nameFallback, Item item, String displayName, boolean funTimeOnly, List<EffectRequirement> effectRequirements) {
            this.loreKeywords = loreKeywords;
            this.nameFallback = nameFallback;
            this.item = item;
            this.displayName = displayName;
            this.funTimeOnly = funTimeOnly;
            this.effectRequirements = effectRequirements;
        }

        ItemInfo(String nameFallback, Item item, String displayName) {
            this.loreKeywords = null;
            this.nameFallback = nameFallback;
            this.item = item;
            this.displayName = displayName;
            this.funTimeOnly = false;
            this.effectRequirements = null;
        }
    }

    private static class EffectRequirement {
        Holder<MobEffect> effect;
        int minAmplifier;

        EffectRequirement(Holder<MobEffect> effect, int minAmplifier) {
            this.effect = effect;
            this.minAmplifier = minAmplifier;
        }
    }
}

