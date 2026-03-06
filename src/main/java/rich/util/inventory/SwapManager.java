
package rich.util.inventory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import rich.util.inventory.InventoryResult;
import rich.util.inventory.InventoryUtils;
import rich.util.inventory.SwapSequence;

public final class SwapManager {
    private static final Minecraft mc = Minecraft.getInstance();
    private static final Map<String, SwapSequence> sequences = new ConcurrentHashMap<String, SwapSequence>();
    private static SwapSequence activeSequence;

    private SwapManager() {
    }

    public static void tick() {
        if (activeSequence != null) {
            activeSequence.tick();
            if (activeSequence.isFinished()) {
                activeSequence = null;
            }
        }
        sequences.values().removeIf(SwapSequence::isFinished);
        sequences.values().forEach(SwapSequence::tick);
    }

    public static void execute(SwapSequence sequence) {
        if (activeSequence != null) {
            activeSequence.cancel();
        }
        activeSequence = sequence.start();
    }

    public static void execute(String name, SwapSequence sequence) {
        SwapSequence existing = sequences.get(name);
        if (existing != null) {
            existing.cancel();
        }
        sequences.put(name, sequence.start());
    }

    public static void cancel() {
        if (activeSequence != null) {
            activeSequence.cancel();
            activeSequence = null;
        }
    }

    public static void cancel(String name) {
        SwapSequence sequence = sequences.remove(name);
        if (sequence != null) {
            sequence.cancel();
        }
    }

    public static void cancelAll() {
        SwapManager.cancel();
        sequences.values().forEach(SwapSequence::cancel);
        sequences.clear();
    }

    public static boolean isRunning() {
        return activeSequence != null && !activeSequence.isFinished();
    }

    public static boolean isRunning(String name) {
        SwapSequence seq = sequences.get(name);
        return seq != null && !seq.isFinished();
    }

    public static void swapAndUse(Item item) {
        InventoryResult result = InventoryUtils.find(item);
        if (!result.found()) {
            return;
        }
        if (result.isHotbar()) {
            SwapManager.execute(new SwapSequence().step(0, InventoryUtils::saveSlot).step(0, () -> InventoryUtils.selectSlot(result.slot())).step(1, () -> InventoryUtils.use(InteractionHand.MAIN_HAND)).step(1, InventoryUtils::restoreSlot));
        } else {
            int hotbar = InventoryUtils.currentSlot();
            SwapManager.execute(new SwapSequence().step(0, () -> InventoryUtils.swapHotbar(result.slot(), hotbar)).step(1, () -> InventoryUtils.use(InteractionHand.MAIN_HAND)).step(1, () -> InventoryUtils.swapHotbar(result.slot(), hotbar)).step(0, InventoryUtils::closeScreen));
        }
    }

    public static void swapAndUseSilent(Item item) {
        InventoryResult result = InventoryUtils.find(item);
        if (!result.found()) {
            return;
        }
        if (result.isHotbar()) {
            SwapManager.execute(new SwapSequence().step(0, InventoryUtils::saveSlot).step(0, () -> InventoryUtils.selectSlotSilent(result.slot())).step(0, () -> InventoryUtils.use(InteractionHand.MAIN_HAND)).step(0, InventoryUtils::restoreSlotSilent));
        } else {
            int hotbar = InventoryUtils.currentSlot();
            SwapManager.execute(new SwapSequence().step(0, () -> InventoryUtils.swapHotbar(result.slot(), hotbar)).step(0, () -> InventoryUtils.use(InteractionHand.MAIN_HAND)).step(0, () -> InventoryUtils.swapHotbar(result.slot(), hotbar)).step(0, InventoryUtils::closeScreen));
        }
    }

    public static void moveToHotbar(Item item, int hotbarSlot) {
        InventoryResult result = InventoryUtils.find(item);
        if (!result.found() || result.isHotbar()) {
            return;
        }
        SwapManager.execute(new SwapSequence().step(0, () -> InventoryUtils.swapHotbar(result.slot(), hotbarSlot)));
    }

    public static void swapSlots(int from, int to) {
        SwapManager.execute(new SwapSequence().step(0, () -> InventoryUtils.swap(from, to)));
    }
}

