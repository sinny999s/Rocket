
package rich.util.inventory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import rich.mixin.ClientWorldAccessor;
import rich.util.inventory.InventoryResult;
import rich.util.inventory.ItemSearcher;

public final class InventoryUtils {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};
    private static int savedSlot = -1;
    private static int silentSlot = -1;

    private InventoryUtils() {
    }

    public static int findItemInHotbar(Item item) {
        if (InventoryUtils.mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    public static int findItemInInventory(Item item) {
        if (InventoryUtils.mc.player == null) {
            return -1;
        }
        for (int i = 9; i < 36; ++i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (stack.isEmpty() || stack.getItem() != item) continue;
            return i;
        }
        return -1;
    }

    public static int findItemAnywhere(Item item) {
        int hotbar = InventoryUtils.findItemInHotbar(item);
        if (hotbar != -1) {
            return hotbar;
        }
        return InventoryUtils.findItemInInventory(item);
    }

    public static InventoryResult find(Item item) {
        return InventoryUtils.find((ItemStack stack) -> stack.getItem() == item);
    }

    public static InventoryResult find(Item ... items) {
        return InventoryUtils.find(Arrays.asList(items));
    }

    public static InventoryResult find(List<Item> items) {
        return InventoryUtils.find((ItemStack stack) -> items.contains(stack.getItem()));
    }

    public static boolean hasElytra() {
        if (InventoryUtils.mc.player == null) {
            return false;
        }
        return InventoryUtils.mc.player.getItemBySlot(EquipmentSlot.CHEST).get(DataComponents.GLIDER) != null;
    }

    public static int findHotbarItem(Item item) {
        if (InventoryUtils.mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 9; ++i) {
            if (InventoryUtils.mc.player.getInventory().getItem(i).getItem() != item) continue;
            return i;
        }
        return -1;
    }

    public static int findElytraSlot() {
        if (InventoryUtils.mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 46; ++i) {
            if (InventoryUtils.mc.player.getInventory().getItem(i).getItem() != Items.ELYTRA) continue;
            return i;
        }
        return -1;
    }

    public static int findChestArmorSlot() {
        if (InventoryUtils.mc.player == null) {
            return -1;
        }
        for (int i = 0; i < 46; ++i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            Equippable component = (Equippable)((Object)stack.get(DataComponents.EQUIPPABLE));
            if (component == null || component.slot() != EquipmentSlot.CHEST || stack.getItem() == Items.ELYTRA) continue;
            return i;
        }
        return -1;
    }

    public static InventoryResult find(ItemSearcher searcher) {
        if (InventoryUtils.mc.player == null) {
            return InventoryResult.notFound();
        }
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = InventoryUtils.mc.player.getItemBySlot(slot);
            if (!InventoryUtils.isValid(stack) || !searcher.matches(stack)) continue;
            return new InventoryResult(-2, true, stack);
        }
        for (int i = 35; i >= 0; --i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (!InventoryUtils.isValid(stack) || !searcher.matches(stack)) continue;
            int slot = i < 9 ? i + 36 : i;
            return InventoryResult.of(slot, stack);
        }
        return InventoryResult.notFound();
    }

    public static InventoryResult findHotbar(Item item) {
        return InventoryUtils.findHotbar((ItemStack stack) -> stack.getItem() == item);
    }

    public static InventoryResult findHotbar(ItemSearcher searcher) {
        if (InventoryUtils.mc.player == null) {
            return InventoryResult.notFound();
        }
        for (int i = 0; i < 9; ++i) {
            ItemStack stack = InventoryUtils.mc.player.getInventory().getItem(i);
            if (!InventoryUtils.isValid(stack) || !searcher.matches(stack)) continue;
            return InventoryResult.of(i, stack);
        }
        return InventoryResult.notFound();
    }

    public static Slot findSlot(Item item) {
        return InventoryUtils.findSlot(s -> s.getItem().getItem() == item, null);
    }

    public static Slot findSlot(Predicate<Slot> filter) {
        return InventoryUtils.findSlot(filter, null);
    }

    public static Slot findSlot(Predicate<Slot> filter, Comparator<Slot> comparator) {
        if (InventoryUtils.mc.player == null) {
            return null;
        }
        Stream<Slot> stream = InventoryUtils.mc.player.containerMenu.slots.stream().filter(filter);
        return comparator != null ? (Slot)stream.max(comparator).orElse(null) : (Slot)stream.findFirst().orElse(null);
    }

    public static Slot findSlot(Item item, Predicate<Slot> extraFilter, Comparator<Slot> comparator) {
        Predicate<Slot> combined = s -> s.getItem().getItem() == item && extraFilter.test((Slot)s);
        return InventoryUtils.findSlot(combined, comparator);
    }

    public static Slot findSlotInHotbar(Item item) {
        if (InventoryUtils.mc.player == null) {
            return null;
        }
        for (int i = 36; i <= 44; ++i) {
            Slot slot = InventoryUtils.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || slot.getItem().isEmpty() || slot.getItem().getItem() != item) continue;
            return slot;
        }
        return null;
    }

    public static Slot findSlotInInventory(Item item) {
        if (InventoryUtils.mc.player == null) {
            return null;
        }
        for (int i = 9; i <= 35; ++i) {
            Slot slot = InventoryUtils.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || slot.getItem().isEmpty() || slot.getItem().getItem() != item) continue;
            return slot;
        }
        return null;
    }

    public static Slot findSlotAnywhere(Item item) {
        Slot hotbar = InventoryUtils.findSlotInHotbar(item);
        if (hotbar != null) {
            return hotbar;
        }
        return InventoryUtils.findSlotInInventory(item);
    }

    public static Slot findRegularTotemSlot() {
        Slot slot;
        int i;
        if (InventoryUtils.mc.player == null) {
            return null;
        }
        for (i = 36; i <= 44; ++i) {
            slot = InventoryUtils.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || slot.getItem().isEmpty() || slot.getItem().getItem() != Items.TOTEM_OF_UNDYING || slot.getItem().isEnchanted()) continue;
            return slot;
        }
        for (i = 9; i <= 35; ++i) {
            slot = InventoryUtils.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || slot.getItem().isEmpty() || slot.getItem().getItem() != Items.TOTEM_OF_UNDYING || slot.getItem().isEnchanted()) continue;
            return slot;
        }
        return null;
    }

    public static Slot findEnchantedTotemSlot() {
        Slot slot;
        int i;
        if (InventoryUtils.mc.player == null) {
            return null;
        }
        for (i = 36; i <= 44; ++i) {
            slot = InventoryUtils.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || slot.getItem().isEmpty() || slot.getItem().getItem() != Items.TOTEM_OF_UNDYING || !slot.getItem().isEnchanted()) continue;
            return slot;
        }
        for (i = 9; i <= 35; ++i) {
            slot = InventoryUtils.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || slot.getItem().isEmpty() || slot.getItem().getItem() != Items.TOTEM_OF_UNDYING || !slot.getItem().isEnchanted()) continue;
            return slot;
        }
        return null;
    }

    public static Slot findTotemSlot(boolean preferNonEnchanted) {
        Slot slot;
        int i;
        if (InventoryUtils.mc.player == null) {
            return null;
        }
        Slot regularTotem = null;
        Slot enchantedTotem = null;
        for (i = 36; i <= 44; ++i) {
            slot = InventoryUtils.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || slot.getItem().isEmpty() || slot.getItem().getItem() != Items.TOTEM_OF_UNDYING) continue;
            if (!slot.getItem().isEnchanted()) {
                if (regularTotem != null) continue;
                regularTotem = slot;
                continue;
            }
            if (enchantedTotem != null) continue;
            enchantedTotem = slot;
        }
        for (i = 9; i <= 35; ++i) {
            slot = InventoryUtils.mc.player.inventoryMenu.getSlot(i);
            if (slot == null || slot.getItem().isEmpty() || slot.getItem().getItem() != Items.TOTEM_OF_UNDYING) continue;
            if (!slot.getItem().isEnchanted()) {
                if (regularTotem != null) continue;
                regularTotem = slot;
                continue;
            }
            if (enchantedTotem != null) continue;
            enchantedTotem = slot;
        }
        if (preferNonEnchanted) {
            return regularTotem != null ? regularTotem : enchantedTotem;
        }
        return regularTotem != null ? regularTotem : enchantedTotem;
    }

    public static boolean hasEnchantedTotemInOffhand() {
        if (InventoryUtils.mc.player == null) {
            return false;
        }
        ItemStack offhand = InventoryUtils.mc.player.getOffhandItem();
        return offhand.getItem() == Items.TOTEM_OF_UNDYING && offhand.isEnchanted();
    }

    public static boolean hasRegularTotemInOffhand() {
        if (InventoryUtils.mc.player == null) {
            return false;
        }
        ItemStack offhand = InventoryUtils.mc.player.getOffhandItem();
        return offhand.getItem() == Items.TOTEM_OF_UNDYING && !offhand.isEnchanted();
    }

    public static void swap(int from, int to) {
        InventoryUtils.click(from, 0, ClickType.PICKUP);
        InventoryUtils.click(to, 0, ClickType.PICKUP);
        InventoryUtils.click(from, 0, ClickType.PICKUP);
    }

    public static void swapHotbar(int slot, int hotbarSlot) {
        InventoryUtils.click(slot, hotbarSlot, ClickType.SWAP);
    }

    public static void swapToOffhand(int slot) {
        InventoryUtils.click(slot, 40, ClickType.SWAP);
    }

    public static void swapToOffhand(Slot slot) {
        if (slot != null) {
            InventoryUtils.click(slot.index, 40, ClickType.SWAP);
        }
    }

    public static void swapOffhandWithSlot(int slotId) {
        if (InventoryUtils.mc.player == null || InventoryUtils.mc.gameMode == null) {
            return;
        }
        int syncId = InventoryUtils.mc.player.inventoryMenu.containerId;
        InventoryUtils.mc.gameMode.handleInventoryMouseClick(syncId, slotId, 40, ClickType.SWAP, InventoryUtils.mc.player);
    }

    public static void moveToSlot(int from, int to) {
        InventoryUtils.swap(from, to);
    }

    public static void click(int slot, int button, ClickType type) {
        if (InventoryUtils.mc.player == null || InventoryUtils.mc.gameMode == null || slot == -1) {
            return;
        }
        InventoryUtils.mc.gameMode.handleInventoryMouseClick(InventoryUtils.mc.player.containerMenu.containerId, slot, button, type, InventoryUtils.mc.player);
    }

    public static void selectSlot(int slot) {
        if (InventoryUtils.mc.player == null || slot < 0 || slot > 8) {
            return;
        }
        if (InventoryUtils.mc.player.getInventory().getSelectedSlot() != slot) {
            InventoryUtils.mc.player.getInventory().setSelectedSlot(slot);
        }
    }

    public static void selectSlotSilent(int slot) {
        if (InventoryUtils.mc.player == null || mc.getConnection() == null || slot < 0 || slot > 8) {
            return;
        }
        mc.getConnection().send(new ServerboundSetCarriedItemPacket(slot));
    }

    public static void saveSlot() {
        if (InventoryUtils.mc.player != null) {
            savedSlot = InventoryUtils.mc.player.getInventory().getSelectedSlot();
        }
    }

    public static void restoreSlot() {
        if (savedSlot != -1) {
            InventoryUtils.selectSlot(savedSlot);
            savedSlot = -1;
        }
    }

    public static void restoreSlotSilent() {
        if (savedSlot != -1) {
            InventoryUtils.selectSlotSilent(savedSlot);
            savedSlot = -1;
        }
    }

    public static void silentUseHotbarItem(int hotbarSlot) {
        if (InventoryUtils.mc.player == null || mc.getConnection() == null) {
            return;
        }
        int currentSlot = InventoryUtils.mc.player.getInventory().getSelectedSlot();
        if (hotbarSlot != currentSlot) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(hotbarSlot));
        }
        InventoryUtils.sendUsePacket(InteractionHand.MAIN_HAND);
        if (hotbarSlot != currentSlot) {
            mc.getConnection().send(new ServerboundSetCarriedItemPacket(currentSlot));
        }
    }

    public static void silentSwapUseAndReturn(int inventorySlot) {
        if (InventoryUtils.mc.player == null || mc.getConnection() == null) {
            return;
        }
        int currentHotbarSlot = InventoryUtils.mc.player.getInventory().getSelectedSlot();
        InventoryUtils.click(inventorySlot, currentHotbarSlot, ClickType.SWAP);
        InventoryUtils.sendUsePacket(InteractionHand.MAIN_HAND);
        InventoryUtils.click(inventorySlot, currentHotbarSlot, ClickType.SWAP);
    }

    public static void silentUseItem(Item item) {
        if (InventoryUtils.mc.player == null) {
            return;
        }
        int hotbarSlot = InventoryUtils.findItemInHotbar(item);
        if (hotbarSlot != -1) {
            InventoryUtils.silentUseHotbarItem(hotbarSlot);
            return;
        }
        int invSlot = InventoryUtils.findItemInInventory(item);
        if (invSlot != -1) {
            int wrappedSlot = InventoryUtils.wrapSlot(invSlot);
            InventoryUtils.silentSwapUseAndReturn(wrappedSlot);
            InventoryUtils.closeScreen();
        }
    }

    public static void sendUsePacket(InteractionHand hand) {
        if (InventoryUtils.mc.player == null || mc.getConnection() == null || InventoryUtils.mc.level == null) {
            return;
        }
        try {
            ClientWorldAccessor worldAccessor = (ClientWorldAccessor)((Object)InventoryUtils.mc.level);
            BlockStatePredictionHandler pendingUpdateManager = worldAccessor.getPendingUpdateManager().startPredicting();
            int sequence = pendingUpdateManager.currentSequence();
            mc.getConnection().send(new ServerboundUseItemPacket(hand, sequence, InventoryUtils.mc.player.getYRot(), InventoryUtils.mc.player.getXRot()));
            pendingUpdateManager.close();
        }
        catch (Exception e) {
            mc.getConnection().send(new ServerboundUseItemPacket(hand, 0, InventoryUtils.mc.player.getYRot(), InventoryUtils.mc.player.getXRot()));
        }
    }

    public static void use(InteractionHand hand) {
        if (InventoryUtils.mc.player == null || InventoryUtils.mc.gameMode == null) {
            return;
        }
        InventoryUtils.mc.gameMode.useItem(InventoryUtils.mc.player, hand);
    }

    public static void closeScreen() {
        if (InventoryUtils.mc.player == null || mc.getConnection() == null) {
            return;
        }
        mc.getConnection().send(new ServerboundContainerClosePacket(InventoryUtils.mc.player.containerMenu.containerId));
    }

    public static boolean isScreenOpen() {
        return InventoryUtils.mc.screen != null && !(InventoryUtils.mc.screen instanceof ChatScreen);
    }

    public static int wrapSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    public static int currentSlot() {
        return InventoryUtils.mc.player != null ? InventoryUtils.mc.player.getInventory().getSelectedSlot() : 0;
    }

    public static ItemStack offhandStack() {
        return InventoryUtils.mc.player != null ? InventoryUtils.mc.player.getOffhandItem() : ItemStack.EMPTY;
    }

    public static ItemStack mainhandStack() {
        return InventoryUtils.mc.player != null ? InventoryUtils.mc.player.getMainHandItem() : ItemStack.EMPTY;
    }

    public static boolean hasTotemInOffhand() {
        return InventoryUtils.mc.player != null && InventoryUtils.mc.player.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING;
    }

    public static Item getOffhandItem() {
        return InventoryUtils.mc.player != null ? InventoryUtils.mc.player.getOffhandItem().getItem() : Items.AIR;
    }

    private static boolean isValid(ItemStack stack) {
        return !stack.isEmpty() && stack.getDamageValue() < stack.getMaxDamage() - 10;
    }
}

