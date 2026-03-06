
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.screens.clickgui.impl.autobuy.AuctionUtils;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.manager.AutoBuyManager;
import rich.util.modules.autobuy.BuyRequest;
import rich.util.modules.autobuy.NetworkManager;
import rich.util.modules.autobuy.ServerManager;
import rich.util.timer.TimerUtil;

public class AutoBuy
extends ModuleStructure {
    private static AutoBuy instance;
    private final SelectSetting mode = new SelectSetting("Mode", "Checker").value("Checker", "Buying");
    private final SelectSetting serverType = new SelectSetting("Servers", "Off").value("Off", "1.16.5", "1.21.4");
    private final SliderSettings updateDelay = new SliderSettings("Delay update", "").range(300, 1000).setValue(500.0f);
    private final SliderSettings serverSwitchTime = new SliderSettings("Server switch time", "").range(30, 120).setValue(30.0f);
    private final BooleanSetting notifications = new BooleanSetting("Notifications", "").setValue(true);
    private final AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
    private final NetworkManager network = new NetworkManager();
    private final ServerManager serverManager = new ServerManager();
    private final TimerUtil updateTimer = TimerUtil.create();
    private final TimerUtil ahOpenTimer = TimerUtil.create();
    private final TimerUtil serverSwitchTimer = TimerUtil.create();
    private boolean inAuction = false;
    private boolean notifiedEnter = false;
    private Set<String> sentItems = new HashSet<String>();
    private Set<String> boughtItems = new HashSet<String>();
    private volatile boolean pendingUpdate = false;

    public AutoBuy() {
        super("Auto Buy", "Automatic auction purchase", ModuleCategory.MISC);
        instance = this;
        this.serverType.setVisible(() -> this.mode.isSelected("Buying"));
        this.serverSwitchTime.setVisible(() -> this.mode.isSelected("Buying") && !this.serverType.isSelected("Off"));
        this.updateDelay.setVisible(() -> this.mode.isSelected("Buying"));
        this.settings(this.mode, this.serverType, this.updateDelay, this.serverSwitchTime, this.notifications);
    }

    public static AutoBuy getInstance() {
        return instance;
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void activate() {
        super.activate();
        this.autoBuyManager.setEnabled(true);
        this.reset();
        if (this.mode.isSelected("Buying")) {
            this.network.startAsServer();
            if (!this.serverType.isSelected("Off")) {
                AutoBuy.mc.options.pauseOnLostFocus = false;
            }
        } else {
            this.network.startAsClient();
        }
        this.msg("\u00a7aModule enabled. Mode: \u00a7b" + this.mode.getSelected());
        if (this.mode.isSelected("Buying") && !this.serverType.isSelected("Off")) {
            this.msg("\u00a77Servers: \u00a7b" + this.serverType.getSelected() + " \u00a77| Change every \u00a7b" + (int)this.serverSwitchTime.getValue() + "s");
        }
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void deactivate() {
        super.deactivate();
        this.autoBuyManager.setEnabled(false);
        this.network.stop();
        this.serverManager.reset();
        this.reset();
        this.msg("\u00a7cModule disabled");
    }

    private void reset() {
        this.inAuction = false;
        this.notifiedEnter = false;
        this.sentItems.clear();
        this.boughtItems.clear();
        this.pendingUpdate = false;
        this.updateTimer.resetCounter();
        this.ahOpenTimer.resetCounter();
        this.serverSwitchTimer.resetCounter();
        this.serverManager.resetTimers();
    }

    public void sendPauseSync(boolean paused) {
        this.network.sendPauseState(paused);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        Screen screen;
        if (AutoBuy.mc.player == null || AutoBuy.mc.level == null) {
            return;
        }
        if (!this.isState()) {
            return;
        }
        this.handlePauseSync();
        if (!this.autoBuyManager.isEnabled()) {
            return;
        }
        if (this.mode.isSelected("Checker")) {
            this.handleServerSwitchCommand();
            this.handleUpdateCommand();
        }
        if (this.mode.isSelected("Buying") && !this.serverType.isSelected("Off")) {
            this.handleServerLogic();
        }
        if (!((screen = AutoBuy.mc.screen) instanceof ContainerScreen)) {
            this.handleNotInScreen();
            return;
        }
        ContainerScreen screen2 = (ContainerScreen)screen;
        String title = screen2.getTitle().getString().toLowerCase();
        int slots = ((ChestMenu)screen2.getMenu()).slots.size();
        if (this.isSuspiciousPriceScreen(title, slots)) {
            this.confirmSuspiciousPrice(screen2);
            return;
        }
        if (!title.contains("auction") && !title.contains("search")) {
            this.handleNotInAuction();
            return;
        }
        this.handleInAuction(screen2);
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void handlePauseSync() {
        Boolean pauseState = this.network.pollPauseState();
        if (pauseState != null) {
            this.autoBuyManager.setEnabledSilent(pauseState == false);
            if (pauseState.booleanValue()) {
                this.msg("\u00a7e[SYNC] Pause enabled");
            } else {
                this.msg("\u00a7a[SYNC] Pause disabled");
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void handleServerSwitchCommand() {
        String switchCmd = this.network.pollServerSwitch();
        if (switchCmd != null) {
            this.msg("\u00a7e[CHECKER] Switching to server: " + switchCmd);
            AutoBuy.mc.player.connection.sendCommand(switchCmd.substring(1));
            this.serverManager.setWaitingForServerLoad(true);
            this.inAuction = false;
            this.notifiedEnter = false;
            this.sentItems.clear();
        }
    }

    private void handleUpdateCommand() {
        if (this.network.pollUpdateCommand()) {
            this.pendingUpdate = true;
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleServerLogic() {
        this.serverManager.updateHubStatus(AutoBuy.mc.level);
        if (this.serverManager.shouldJoinAnarchy(this.serverType.getSelected())) {
            this.serverManager.joinAnarchyFromHub(AutoBuy.mc.player, this.serverType.getSelected());
            this.msg("\u00a7e[BUYER] Joining anarchy...");
        }
        if (this.serverManager.isWaitingForServerLoad() && !this.serverManager.isInHub()) {
            this.serverManager.setWaitingForServerLoad(false);
            this.serverSwitchTimer.resetCounter();
            this.ahOpenTimer.resetCounter();
            this.msg("\u00a7a[BUYER] Connected to server");
        }
        long switchInterval = (long)(this.serverSwitchTime.getValue() * 1000.0f);
        if (!this.serverManager.isInHub() && this.serverSwitchTimer.hasTimeElapsed(switchInterval)) {
            this.serverManager.switchToNextServer(AutoBuy.mc.player, this.network, this.serverType.getSelected());
            this.serverSwitchTimer.resetCounter();
            this.inAuction = false;
            this.sentItems.clear();
            this.boughtItems.clear();
        }
    }

    private void handleNotInScreen() {
        if (this.inAuction) {
            this.inAuction = false;
            this.sentItems.clear();
            this.boughtItems.clear();
            if (this.mode.isSelected("Checker") && this.notifiedEnter) {
                this.network.sendLeaveAuction();
                this.notifiedEnter = false;
            }
        }
        if (this.ahOpenTimer.hasTimeElapsed(11000L)) {
            AutoBuy.mc.player.connection.sendCommand("ah");
            this.ahOpenTimer.resetCounter();
        }
    }

    private void handleNotInAuction() {
        if (this.inAuction) {
            this.inAuction = false;
            this.sentItems.clear();
            this.boughtItems.clear();
            if (this.mode.isSelected("Checker") && this.notifiedEnter) {
                this.network.sendLeaveAuction();
                this.notifiedEnter = false;
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleInAuction(ContainerScreen screen) {
        if (!this.inAuction) {
            this.inAuction = true;
            this.sentItems.clear();
            this.boughtItems.clear();
            this.msg("\u00a7aIn auction");
            if (this.mode.isSelected("Checker") && !this.notifiedEnter) {
                this.network.sendEnterAuction();
                this.notifiedEnter = true;
            }
        }
        if (this.mode.isSelected("Buying")) {
            this.processBuyRequestsInstant(screen);
            if (this.updateTimer.hasTimeElapsed((long)this.updateDelay.getValue() - 200L)) {
                int clientsInAuction = this.network.getClientsInAuctionCount();
                if (clientsInAuction > 0) {
                    this.network.sendUpdateCommand();
                }
                this.updateAuction(screen);
                this.updateTimer.resetCounter();
            }
        } else {
            if (this.pendingUpdate) {
                this.updateAuction(screen);
                this.pendingUpdate = false;
            }
            this.scanAndSendInstant(screen);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean isSuspiciousPriceScreen(String title, int slots) {
        if (title.contains("suspicious")) {
            return true;
        }
        if (title.contains("confirm")) {
            return true;
        }
        if (title.contains("suspicious")) {
            return true;
        }
        if (title.contains("confirm")) {
            return true;
        }
        return (slots == 63 || slots == 36) && !title.contains("auction") && !title.contains("search") && !title.contains("inventory");
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void confirmSuspiciousPrice(ContainerScreen screen) {
        int syncId = ((ChestMenu)screen.getMenu()).containerId;
        AutoBuy.mc.gameMode.handleInventoryMouseClick(syncId, 1, 0, ClickType.PICKUP, AutoBuy.mc.player);
        this.msg("\u00a7a\u2713 Confirmed purchase");
    }

    private void updateAuction(ContainerScreen screen) {
        int syncId = ((ChestMenu)screen.getMenu()).containerId;
        AutoBuy.mc.gameMode.handleInventoryMouseClick(syncId, 49, 0, ClickType.QUICK_MOVE, AutoBuy.mc.player);
    }

    private String generateLoreHash(ItemStack stack) {
        ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) {
            return "nolore";
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (Component line : lore.lines()) {
            if (count >= 3) break;
            String text = line.getString();
            if (text.contains("$") || text.contains("Seller") || text.contains("Expires") || text.contains("Press")) continue;
            sb.append(text.hashCode());
            ++count;
        }
        return sb.toString();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void scanAndSendInstant(ContainerScreen screen) {
        if (!this.network.isConnected()) {
            return;
        }
        List<AutoBuyableItem> items = this.autoBuyManager.getEnabledItems();
        if (items.isEmpty()) {
            return;
        }
        for (int i = 0; i < 45 && i < ((ChestMenu)screen.getMenu()).slots.size(); ++i) {
            int price;
            Slot slot = (Slot)((ChestMenu)screen.getMenu()).slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || AuctionUtils.isArmorItem(stack) && AuctionUtils.hasThornsEnchantment(stack) || (price = AuctionUtils.getPrice(stack)) <= 0) continue;
            String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            String loreHash = this.generateLoreHash(stack);
            String key = itemId + "|" + price + "|" + stack.getCount() + "|" + loreHash;
            if (this.sentItems.contains(key)) continue;
            this.processItemForSending(stack, items, key, itemId, price, loreHash);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void processItemForSending(ItemStack stack, List<AutoBuyableItem> items, String key, String itemId, int price, String loreHash) {
        for (AutoBuyableItem item : items) {
            int minQuantity;
            int maxPrice = item.getSettings().getBuyBelow();
            int n = minQuantity = item.getSettings().isCanHaveQuantity() ? item.getSettings().getMinQuantity() : 1;
            if (price > maxPrice || item.getSettings().isCanHaveQuantity() && stack.getCount() < minQuantity || !AuctionUtils.compareItem(stack, item.createItemStack())) continue;
            this.sentItems.add(key);
            String displayName = item.getDisplayName();
            this.network.sendBuyCommand(price, itemId, displayName, stack.getCount(), loreHash, maxPrice, minQuantity);
            break;
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void processBuyRequestsInstant(ContainerScreen screen) {
        BuyRequest request;
        int syncId = ((ChestMenu)screen.getMenu()).containerId;
        while ((request = this.network.pollBuyRequest()) != null) {
            boolean found;
            String buyKey = request.itemId + "|" + request.price + "|" + request.count + "|" + request.loreHash;
            if (this.boughtItems.contains(buyKey) || (found = this.tryExactMatch(screen, syncId, request, buyKey))) continue;
            this.tryFallbackMatch(screen, syncId, request, buyKey);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private boolean tryExactMatch(ContainerScreen screen, int syncId, BuyRequest request, String buyKey) {
        for (int i = 0; i < 45 && i < ((ChestMenu)screen.getMenu()).slots.size(); ++i) {
            String stackLoreHash;
            int stackPrice;
            String stackItemId;
            Slot slot = (Slot)((ChestMenu)screen.getMenu()).slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || !(stackItemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).equals(request.itemId) || (stackPrice = AuctionUtils.getPrice(stack)) != request.price || stack.getCount() != request.count || !(stackLoreHash = this.generateLoreHash(stack)).equals(request.loreHash) || AuctionUtils.isArmorItem(stack) && AuctionUtils.hasThornsEnchantment(stack)) continue;
            AutoBuy.mc.gameMode.handleInventoryMouseClick(syncId, slot.index, 0, ClickType.QUICK_MOVE, AutoBuy.mc.player);
            this.boughtItems.add(buyKey);
            this.msg("\u00a7a\u26a1 BOUGHT: \u00a7f" + request.displayName + " \u00a7afor " + stackPrice + "$");
            return true;
        }
        return false;
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private boolean tryFallbackMatch(ContainerScreen screen, int syncId, BuyRequest request, String buyKey) {
        for (int i = 0; i < 45 && i < ((ChestMenu)screen.getMenu()).slots.size(); ++i) {
            String fallbackKey;
            int stackPrice;
            String stackItemId;
            Slot slot = (Slot)((ChestMenu)screen.getMenu()).slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || !(stackItemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()).equals(request.itemId) || (stackPrice = AuctionUtils.getPrice(stack)) <= 0 || stackPrice > request.maxPrice || stack.getCount() < request.minQuantity || AuctionUtils.isArmorItem(stack) && AuctionUtils.hasThornsEnchantment(stack) || this.boughtItems.contains(fallbackKey = stackItemId + "|" + stackPrice + "|" + stack.getCount() + "|" + this.generateLoreHash(stack))) continue;
            AutoBuy.mc.gameMode.handleInventoryMouseClick(syncId, slot.index, 0, ClickType.QUICK_MOVE, AutoBuy.mc.player);
            this.boughtItems.add(fallbackKey);
            this.boughtItems.add(buyKey);
            this.msg("\u00a7a\u26a1 BOUGHT: \u00a7f" + request.displayName + " \u00a7afor " + stackPrice + "$");
            return true;
        }
        return false;
    }

    private void msg(String text) {
        if (!this.notifications.isValue() || AutoBuy.mc.player != null) {
            // empty if block
        }
    }

    public NetworkManager getNetworkManager() {
        return this.network;
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    public boolean isFullyEnabled() {
        return this.isState() && this.autoBuyManager.isEnabled();
    }

    @Generated
    public SelectSetting getMode() {
        return this.mode;
    }

    @Generated
    public SelectSetting getServerType() {
        return this.serverType;
    }

    @Generated
    public SliderSettings getUpdateDelay() {
        return this.updateDelay;
    }

    @Generated
    public SliderSettings getServerSwitchTime() {
        return this.serverSwitchTime;
    }

    @Generated
    public BooleanSetting getNotifications() {
        return this.notifications;
    }

    @Generated
    public AutoBuyManager getAutoBuyManager() {
        return this.autoBuyManager;
    }

    @Generated
    public NetworkManager getNetwork() {
        return this.network;
    }

    @Generated
    public ServerManager getServerManager() {
        return this.serverManager;
    }

    @Generated
    public TimerUtil getUpdateTimer() {
        return this.updateTimer;
    }

    @Generated
    public TimerUtil getAhOpenTimer() {
        return this.ahOpenTimer;
    }

    @Generated
    public TimerUtil getServerSwitchTimer() {
        return this.serverSwitchTimer;
    }

    @Generated
    public boolean isInAuction() {
        return this.inAuction;
    }

    @Generated
    public boolean isNotifiedEnter() {
        return this.notifiedEnter;
    }

    @Generated
    public Set<String> getSentItems() {
        return this.sentItems;
    }

    @Generated
    public Set<String> getBoughtItems() {
        return this.boughtItems;
    }

    @Generated
    public boolean isPendingUpdate() {
        return this.pendingUpdate;
    }
}

