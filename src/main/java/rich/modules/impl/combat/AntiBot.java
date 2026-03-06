/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  java.lang.runtime.SwitchBootstraps
 */
package rich.modules.impl.combat;

import antidaunleak.api.annotation.Native;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SelectSetting;
import rich.util.Instance;

public class AntiBot
extends ModuleStructure {
    private final Set<UUID> suspectSet = new HashSet<UUID>();
    static Set<UUID> botSet = new HashSet<UUID>();
    private final SelectSetting mode = new SelectSetting("Mode", "Select bot detection mode").value("Matrix", "ReallyWorld").selected("ReallyWorld");
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    @Native(type=Native.Type.VMProtectBeginUltra)
    public static AntiBot getInstance() {
        return Instance.get(AntiBot.class);
    }

    public AntiBot() {
        super("AntiBot", "Anti Bot", ModuleCategory.COMBAT);
        this.settings(this.mode);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        Packet<?> packet = e.getPacket();
        Objects.requireNonNull(packet);
        if (packet instanceof ClientboundPlayerInfoUpdatePacket) {
            ClientboundPlayerInfoUpdatePacket list = (ClientboundPlayerInfoUpdatePacket)packet;
            this.checkPlayerAfterSpawn(list);
        } else if (packet instanceof ClientboundPlayerInfoRemovePacket) {
            ClientboundPlayerInfoRemovePacket remove = (ClientboundPlayerInfoRemovePacket)packet;
            this.removePlayerBecauseLeftServer(remove);
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (!this.suspectSet.isEmpty()) {
            AntiBot.mc.level.players().stream().filter(p -> this.suspectSet.contains(p.getUUID())).forEach(this::evaluateSuspectPlayer);
        }
        if (this.mode.isSelected("Matrix")) {
            this.matrixMode();
        } else if (this.mode.isSelected("ReallyWorld")) {
            this.ReallyWorldMode();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void checkPlayerAfterSpawn(ClientboundPlayerInfoUpdatePacket listS2CPacket) {
        listS2CPacket.newEntries().forEach(entry -> {
            GameProfile profile = entry.profile();
            if (profile == null || this.isRealPlayer((ClientboundPlayerInfoUpdatePacket.Entry)((Object)entry), profile)) {
                return;
            }
            if (this.isDuplicateProfile(profile)) {
                botSet.add(profile.id());
            } else {
                this.suspectSet.add(profile.id());
            }
        });
    }

    private void removePlayerBecauseLeftServer(ClientboundPlayerInfoRemovePacket removeS2CPacket) {
        removeS2CPacket.profileIds().forEach(uuid -> {
            this.suspectSet.remove(uuid);
            botSet.remove(uuid);
        });
    }

    private boolean isRealPlayer(ClientboundPlayerInfoUpdatePacket.Entry entry, GameProfile profile) {
        return entry.latency() < 2 || profile.properties() != null && !profile.properties().isEmpty();
    }

    private void evaluateSuspectPlayer(Player player) {
        List<ItemStack> armor = null;
        if (!this.isFullyEquipped(player)) {
            armor = this.getArmorItems(player);
        }
        if (this.isFullyEquipped(player) || this.hasArmorChanged(player, armor)) {
            botSet.add(player.getUUID());
        }
        this.suspectSet.remove(player.getUUID());
    }

    private List<ItemStack> getArmorItems(Player entity) {
        ArrayList<ItemStack> armorItems = new ArrayList<ItemStack>();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            armorItems.add(entity.getItemBySlot(slot));
        }
        return armorItems;
    }

    private ItemStack getArmorStack(Player entity, int index) {
        if (index >= 0 && index < ARMOR_SLOTS.length) {
            return entity.getItemBySlot(ARMOR_SLOTS[index]);
        }
        return ItemStack.EMPTY;
    }

    private boolean isArmorItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Equippable equippable = (Equippable)((Object)stack.get(DataComponents.EQUIPPABLE));
        if (equippable == null) {
            return false;
        }
        EquipmentSlot slot = equippable.slot();
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST || slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET;
    }

    private void matrixMode() {
        Iterator<UUID> iterator = this.suspectSet.iterator();
        while (iterator.hasNext()) {
            UUID susPlayer = iterator.next();
            Player entity = AntiBot.mc.level.getPlayerByUUID(susPlayer);
            if (entity != null) {
                boolean isFakeUUID;
                String playerName = entity.getName().getString();
                boolean isNameBot = playerName.startsWith("CIT-") && !playerName.contains("NPC") && !playerName.contains("[ZNPC]");
                int armorCount = 0;
                for (EquipmentSlot slot : ARMOR_SLOTS) {
                    ItemStack item = entity.getItemBySlot(slot);
                    if (item.isEmpty()) continue;
                    ++armorCount;
                }
                boolean isFullArmor = armorCount == 4;
                boolean bl = isFakeUUID = !entity.getUUID().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + playerName).getBytes()));
                if (isFullArmor || isNameBot || isFakeUUID) {
                    botSet.add(susPlayer);
                }
            }
            iterator.remove();
        }
        if (AntiBot.mc.player.tickCount % 100 == 0) {
            botSet.removeIf(uuid -> AntiBot.mc.level.getPlayerByUUID((UUID)uuid) == null);
        }
    }

    private void ReallyWorldMode() {
        for (Player entity : AntiBot.mc.level.players()) {
            if (entity.getUUID().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + entity.getName().getString()).getBytes())) || botSet.contains(entity.getUUID()) || entity.getName().getString().contains("NPC") || entity.getName().getString().startsWith("[ZNPC]")) continue;
            botSet.add(entity.getUUID());
        }
    }

    private void newMatrixMode() {
        for (Player entity : AntiBot.mc.level.players()) {
            if (entity == AntiBot.mc.player) continue;
            List<ItemStack> armorItems = this.getArmorItems(entity);
            boolean allArmorValid = true;
            for (ItemStack item : armorItems) {
                if (!item.isEmpty() && item.isEnchantable() && item.getDamageValue() <= 0) continue;
                allArmorValid = false;
                break;
            }
            boolean hasSpecificArmor = false;
            for (ItemStack item : armorItems) {
                if (item.getItem() != Items.LEATHER_BOOTS && item.getItem() != Items.LEATHER_LEGGINGS && item.getItem() != Items.LEATHER_CHESTPLATE && item.getItem() != Items.LEATHER_HELMET && item.getItem() != Items.IRON_BOOTS && item.getItem() != Items.IRON_LEGGINGS && item.getItem() != Items.IRON_CHESTPLATE && item.getItem() != Items.IRON_HELMET) continue;
                hasSpecificArmor = true;
                break;
            }
            if (allArmorValid && hasSpecificArmor && entity.getItemInHand(InteractionHand.OFF_HAND).getItem() == Items.AIR && entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() != Items.AIR && entity.getFoodData().getFoodLevel() == 20 && !entity.getName().getString().contains("NPC") && !entity.getName().getString().startsWith("[ZNPC]")) {
                botSet.add(entity.getUUID());
                continue;
            }
            botSet.remove(entity.getUUID());
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    public boolean isDuplicateProfile(GameProfile profile) {
        return Objects.requireNonNull(mc.getConnection()).getOnlinePlayers().stream().filter(player -> player.getProfile().name().equals(profile.name()) && !player.getProfile().id().equals(profile.id())).count() == 1L;
    }

    public boolean isFullyEquipped(Player entity) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (this.isArmorItem(stack) && !stack.isEnchanted()) continue;
            return false;
        }
        return true;
    }

    public boolean hasArmorChanged(Player entity, List<ItemStack> prevArmor) {
        if (prevArmor == null) {
            return true;
        }
        List<ItemStack> currentArmorList = this.getArmorItems(entity);
        if (currentArmorList.size() != prevArmor.size()) {
            return true;
        }
        for (int i = 0; i < currentArmorList.size(); ++i) {
            if (ItemStack.matches((ItemStack)currentArmorList.get(i), (ItemStack)prevArmor.get(i))) continue;
            return true;
        }
        return false;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    public boolean isBot(Player entity) {
        String playerName = entity.getName().getString();
        boolean isNameBot = playerName.startsWith("CIT-") && !playerName.contains("NPC") && !playerName.startsWith("[ZNPC]");
        boolean isMarkedBot = botSet.contains(entity.getUUID());
        this.isBotU(entity);
        return isNameBot || isMarkedBot;
    }

    public boolean isBot(UUID uuid) {
        return botSet.contains(uuid);
    }

    public boolean isBotU(Entity entity) {
        return !entity.getUUID().equals(UUID.nameUUIDFromBytes(("OfflinePlayer:" + entity.getName().getString()).getBytes())) && entity.isInvisible() && !entity.getName().getString().contains("NPC") && !entity.getName().getString().startsWith("[ZNPC]");
    }

    public void reset() {
        this.suspectSet.clear();
        botSet.clear();
    }

    @Override
    public void deactivate() {
        this.reset();
        super.deactivate();
    }
}

