
package rich.modules.impl.misc.autoparser;

import java.time.Instant;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.screens.clickgui.impl.autobuy.AuctionUtils;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.manager.AutoBuyManager;
import rich.util.modules.autoparser.AutoParserItems;
import rich.util.string.chat.ChatMessage;
import rich.util.timer.StopWatch;
import rich.util.timer.TimerUtil;

public class AutoParser
extends ModuleStructure {
    private static AutoParser instance;
    private final AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();
    private final TimerUtil actionTimer = TimerUtil.create();
    private final TimerUtil commandTimer = TimerUtil.create();
    private final TimerUtil retryTimer = TimerUtil.create();
    private final StopWatch antiAfkWatch = new StopWatch();
    private ParserState state = ParserState.IDLE;
    private int discountPercent = 60;
    private boolean debugMode = true;
    private int maxRetriesCount = 3;
    private long commandDelayMs = 150L;
    private int currentItemIndex = 0;
    private int currentPage = 1;
    private int totalPages = 1;
    private String currentSearchItem = "";
    private String[] currentAutoBuyNames = new String[0];
    private final Map<String, Integer> lowestPricesFound = new HashMap<String, Integer>();
    private int updatedCount = 0;
    private int skippedCount = 0;
    private static final Pattern PAGE_PATTERN;
    private static final int MAX_PAGES_TO_SCAN = 2;
    private static final long ANTI_AFK_INTERVAL = 20000L;
    private static final long PAGE_CLICK_DELAY = 200L;
    private static final long CHECK_INTERVAL = 75L;
    private int waitAttempts = 0;
    private int antiAfkAction = 0;
    private int retryCount = 0;
    private String lastFoundTitle = "";
    private int pageChangeAttempts = 0;
    private String titleBeforePageClick = "";
    private boolean commandSentThisCycle = false;
    private long lastCommandTime = 0L;

    public AutoParser() {
        super("Auto Parser", null);
        instance = this;
    }

    public static AutoParser getInstance() {
        return instance;
    }

    public void startParsing() {
        List<AutoParserItems.ParserItemEntry> items = AutoParserItems.getItems();
        if (items.isEmpty()) {
            ChatMessage.autobuymessageError("Item list for parsing is empty!");
            return;
        }
        if (this.state != ParserState.IDLE) {
            ChatMessage.autobuymessageWarning("AutoParser already running!");
            return;
        }
        this.fullReset();
        ChatMessage.autobuymessageSuccess("\u00a7a\u2550\u2550\u2550\u2550\u2550\u2550 AutoParser \u2550\u2550\u2550\u2550\u2550\u2550");
        ChatMessage.autobuymessage("\u00a77Items: \u00a7b" + items.size() + " \u00a77| Discount: \u00a7b" + this.discountPercent + "%");
        ChatMessage.autobuymessageSuccess("\u00a7a\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550");
        this.prepareNextItem();
    }

    public void stopParsing() {
        if (this.state == ParserState.IDLE) {
            return;
        }
        ChatMessage.autobuymessageWarning("AutoParser stopped");
        if (this.updatedCount > 0 || this.skippedCount > 0) {
            ChatMessage.autobuymessage("\u00a77Updated: \u00a7a" + this.updatedCount);
        }
        this.fullReset();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        if (AutoParser.mc.player == null || AutoParser.mc.level == null) {
            return;
        }
        if (this.state == ParserState.IDLE) {
            return;
        }
        if (this.antiAfkWatch.finished(20000.0)) {
            this.performAntiAfk();
            this.antiAfkWatch.reset();
        }
        switch (this.state.ordinal()) {
            case 1: {
                this.handleClosingScreen();
                break;
            }
            case 2: {
                this.handleSendingCommand();
                break;
            }
            case 3: {
                this.handleWaitingForAuction();
                break;
            }
            case 4: {
                this.handleScanningPage();
                break;
            }
            case 5: {
                this.handleClickingNextPage();
                break;
            }
            case 6: {
                this.handleWaitingPageChange();
                break;
            }
            case 7: {
                this.handleFinishingItem();
                break;
            }
            case 8: {
                this.handleNextItem();
                break;
            }
            case 9: {
                this.handleFinished();
            }
        }
    }

    private void performAntiAfk() {
        if (AutoParser.mc.player == null) {
            return;
        }
        ++this.antiAfkAction;
        switch (this.antiAfkAction % 4) {
            case 0: {
                AutoParser.mc.player.swing(InteractionHand.MAIN_HAND);
                AutoParser.mc.player.setYRot(AutoParser.mc.player.getYRot() + 5.0f);
                break;
            }
            case 1: {
                AutoParser.mc.player.swing(InteractionHand.OFF_HAND);
                AutoParser.mc.player.setYRot(AutoParser.mc.player.getYRot() - 5.0f);
                break;
            }
            case 2: {
                AutoParser.mc.player.setXRot(AutoParser.mc.player.getXRot() + 3.0f);
                AutoParser.mc.player.swing(InteractionHand.MAIN_HAND);
                break;
            }
            case 3: {
                AutoParser.mc.player.setXRot(AutoParser.mc.player.getXRot() - 3.0f);
                AutoParser.mc.player.swing(InteractionHand.OFF_HAND);
            }
        }
    }

    private void handleClosingScreen() {
        if (AutoParser.mc.screen != null) {
            AutoParser.mc.player.closeContainer();
            this.actionTimer.resetCounter();
            return;
        }
        if (this.actionTimer.hasTimeElapsed(200L)) {
            this.state = ParserState.SENDING_COMMAND;
            this.commandSentThisCycle = false;
            this.commandTimer.resetCounter();
        }
    }

    private void handleSendingCommand() {
        if (AutoParser.mc.player == null || AutoParser.mc.player.connection == null) {
            this.retryTimer.resetCounter();
            return;
        }
        if (!this.commandSentThisCycle) {
            if (System.currentTimeMillis() - this.lastCommandTime < this.commandDelayMs) {
                return;
            }
            String command = "/ah search " + this.currentSearchItem;
            LastSeenMessages.Update ack = new LastSeenMessages.Update((byte)0, new BitSet(20), (byte)0);
            ServerboundChatPacket packet = new ServerboundChatPacket(command, Instant.now(), 0L, null, ack);
            AutoParser.mc.player.connection.send(packet);
            this.commandSentThisCycle = true;
            this.lastCommandTime = System.currentTimeMillis();
            this.commandTimer.resetCounter();
            this.waitAttempts = 0;
            return;
        }
        if (this.commandTimer.hasTimeElapsed(500L)) {
            this.state = ParserState.WAITING_FOR_AUCTION;
            this.actionTimer.resetCounter();
        }
    }

    private void handleWaitingForAuction() {
        boolean titleMatchesSearch;
        if (!this.actionTimer.hasTimeElapsed(75L)) {
            return;
        }
        this.actionTimer.resetCounter();
        ++this.waitAttempts;
        if (this.waitAttempts > 5) {
            ++this.retryCount;
            if (this.retryCount < this.maxRetriesCount) {
                this.state = ParserState.CLOSING_SCREEN;
                this.commandSentThisCycle = false;
                this.waitAttempts = 0;
                this.lastFoundTitle = "";
                this.actionTimer.resetCounter();
                return;
            }
            ++this.skippedCount;
            this.state = ParserState.NEXT_ITEM;
            return;
        }
        Screen screen = AutoParser.mc.screen;
        if (!(screen instanceof ContainerScreen)) {
            return;
        }
        ContainerScreen screen2 = (ContainerScreen)screen;
        String title = screen2.getTitle().getString();
        String titleLower = title.toLowerCase();
        if (title.equals(this.lastFoundTitle)) {
            return;
        }
        if (titleLower.contains("not found") || titleLower.contains("nothing") || titleLower.contains("empty") || titleLower.contains("no results") || titleLower.contains("items not found") || titleLower.contains("not found")) {
            ++this.skippedCount;
            this.state = ParserState.NEXT_ITEM;
            return;
        }
        Matcher matcher = PAGE_PATTERN.matcher(title);
        if (matcher.find()) {
            this.currentPage = Integer.parseInt(matcher.group(1));
            int realTotalPages = Integer.parseInt(matcher.group(2));
            this.totalPages = Math.min(realTotalPages, 2);
            this.lastFoundTitle = title;
            this.state = ParserState.SCANNING_PAGE;
            return;
        }
        String searchLower = this.currentSearchItem.toLowerCase();
        boolean bl = titleMatchesSearch = titleLower.contains(searchLower) || this.containsAnyWord(titleLower, searchLower);
        if (titleLower.contains("search") || titleLower.contains("search") || titleLower.contains("auction") || titleLower.contains("auction") || titleLower.contains("ah") || titleMatchesSearch) {
            boolean hasItems = false;
            int slotsToCheck = Math.min(45, ((ChestMenu)screen2.getMenu()).slots.size());
            for (int i = 0; i < slotsToCheck; ++i) {
                int price;
                Slot slot = (Slot)((ChestMenu)screen2.getMenu()).slots.get(i);
                if (slot.getItem().isEmpty() || (price = AuctionUtils.getPrice(slot.getItem())) <= 0) continue;
                hasItems = true;
                break;
            }
            if (!hasItems) {
                return;
            }
            this.currentPage = 1;
            this.totalPages = 1;
            this.lastFoundTitle = title;
            this.state = ParserState.SCANNING_PAGE;
        }
    }

    private void handleScanningPage() {
        Screen screen = AutoParser.mc.screen;
        if (!(screen instanceof ContainerScreen)) {
            this.state = ParserState.FINISHING_ITEM;
            return;
        }
        ContainerScreen screen2 = (ContainerScreen)screen;
        String currentTitle = screen2.getTitle().getString();
        Matcher matcher = PAGE_PATTERN.matcher(currentTitle);
        if (matcher.find()) {
            int actualPage = Integer.parseInt(matcher.group(1));
            if (actualPage > 2) {
                this.state = ParserState.FINISHING_ITEM;
                return;
            }
            this.currentPage = actualPage;
        }
        int slotsToScan = Math.min(45, ((ChestMenu)screen2.getMenu()).slots.size());
        for (int i = 0; i < slotsToScan; ++i) {
            int price;
            Slot slot = (Slot)((ChestMenu)screen2.getMenu()).slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || (price = AuctionUtils.getPrice(stack)) <= 0) continue;
            String itemName = stack.getHoverName().getString();
            for (String autoBuyName : this.currentAutoBuyNames) {
                int currentLowest;
                if (!this.matchesItem(stack, itemName, autoBuyName) || price >= (currentLowest = this.lowestPricesFound.getOrDefault(autoBuyName, Integer.MAX_VALUE).intValue())) continue;
                this.lowestPricesFound.put(autoBuyName, price);
            }
        }
        if (this.currentPage < this.totalPages && this.currentPage < 2) {
            this.state = ParserState.CLICKING_NEXT_PAGE;
            this.titleBeforePageClick = currentTitle;
            this.actionTimer.resetCounter();
        } else {
            this.state = ParserState.FINISHING_ITEM;
        }
    }

    private void handleClickingNextPage() {
        if (!this.actionTimer.hasTimeElapsed(200L)) {
            return;
        }
        Screen screen = AutoParser.mc.screen;
        if (!(screen instanceof ContainerScreen)) {
            this.state = ParserState.FINISHING_ITEM;
            return;
        }
        ContainerScreen screen2 = (ContainerScreen)screen;
        try {
            int syncId = ((ChestMenu)screen2.getMenu()).containerId;
            AutoParser.mc.gameMode.handleInventoryMouseClick(syncId, 50, 0, ClickType.PICKUP, AutoParser.mc.player);
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.state = ParserState.WAITING_PAGE_CHANGE;
        this.pageChangeAttempts = 0;
        this.actionTimer.resetCounter();
    }

    private void handleWaitingPageChange() {
        if (!this.actionTimer.hasTimeElapsed(100L)) {
            return;
        }
        this.actionTimer.resetCounter();
        ++this.pageChangeAttempts;
        if (this.pageChangeAttempts > 30) {
            this.state = ParserState.FINISHING_ITEM;
            return;
        }
        Screen screen = AutoParser.mc.screen;
        if (!(screen instanceof ContainerScreen)) {
            this.state = ParserState.FINISHING_ITEM;
            return;
        }
        ContainerScreen screen2 = (ContainerScreen)screen;
        String newTitle = screen2.getTitle().getString();
        if (!newTitle.equals(this.titleBeforePageClick)) {
            int newPage;
            Matcher matcher = PAGE_PATTERN.matcher(newTitle);
            if (matcher.find() && (newPage = Integer.parseInt(matcher.group(1))) > 2) {
                this.state = ParserState.FINISHING_ITEM;
                return;
            }
            this.state = ParserState.SCANNING_PAGE;
        }
    }

    private void handleFinishingItem() {
        for (Map.Entry<String, Integer> entry : this.lowestPricesFound.entrySet()) {
            int discountedPrice;
            boolean updated;
            String autoBuyName = entry.getKey();
            int lowestPrice = entry.getValue();
            if (lowestPrice >= Integer.MAX_VALUE || !(updated = this.updateAutoBuyPrice(autoBuyName, discountedPrice = this.calculateDiscountedPrice(lowestPrice)))) continue;
            ChatMessage.autobuymessageSuccess("\u00a7a\u2713 \u00a7f" + autoBuyName + "\u00a77: " + this.formatPrice(lowestPrice) + " \u2192 \u00a7b" + this.formatPrice(discountedPrice));
            ++this.updatedCount;
        }
        this.state = ParserState.NEXT_ITEM;
    }

    private void handleNextItem() {
        ++this.currentItemIndex;
        this.lastFoundTitle = "";
        this.commandSentThisCycle = false;
        this.retryCount = 0;
        this.waitAttempts = 0;
        List<AutoParserItems.ParserItemEntry> items = AutoParserItems.getItems();
        if (this.currentItemIndex >= items.size()) {
            this.state = ParserState.FINISHED;
            return;
        }
        this.prepareNextItem();
    }

    private void handleFinished() {
        try {
            if (AutoParser.mc.player != null && AutoParser.mc.screen != null) {
                AutoParser.mc.player.closeContainer();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        ChatMessage.autobuymessageSuccess("\u00a7a\u2713 AutoParser finished!");
        this.fullReset();
    }

    private void prepareNextItem() {
        List<AutoParserItems.ParserItemEntry> items = AutoParserItems.getItems();
        AutoParserItems.ParserItemEntry entry = items.get(this.currentItemIndex);
        this.currentSearchItem = entry.getSearchQuery();
        this.currentAutoBuyNames = entry.getAutoBuyNames();
        this.currentPage = 1;
        this.totalPages = 1;
        this.lowestPricesFound.clear();
        this.waitAttempts = 0;
        this.retryCount = 0;
        this.commandSentThisCycle = false;
        this.lastFoundTitle = "";
        for (String name : this.currentAutoBuyNames) {
            this.lowestPricesFound.put(name, Integer.MAX_VALUE);
        }
        this.debug("\u00a77[" + (this.currentItemIndex + 1) + "/" + items.size() + "] \u00a7b" + this.currentSearchItem);
        this.state = ParserState.CLOSING_SCREEN;
        this.actionTimer.resetCounter();
    }

    private void fullReset() {
        this.currentItemIndex = 0;
        this.currentPage = 1;
        this.totalPages = 1;
        this.currentSearchItem = "";
        this.currentAutoBuyNames = new String[0];
        this.lowestPricesFound.clear();
        this.updatedCount = 0;
        this.skippedCount = 0;
        this.waitAttempts = 0;
        this.antiAfkAction = 0;
        this.retryCount = 0;
        this.lastFoundTitle = "";
        this.pageChangeAttempts = 0;
        this.titleBeforePageClick = "";
        this.commandSentThisCycle = false;
        this.lastCommandTime = 0L;
        this.state = ParserState.IDLE;
        this.actionTimer.resetCounter();
        this.commandTimer.resetCounter();
        this.retryTimer.resetCounter();
        this.antiAfkWatch.reset();
    }

    private boolean containsAnyWord(String title, String search) {
        String[] words;
        for (String word : words = search.split("\\s+")) {
            if (word.length() < 3 || !title.contains(word)) continue;
            return true;
        }
        return false;
    }

    private boolean matchesItem(ItemStack stack, String itemName, String autoBuyName) {
        String cleanAutoBuyName;
        String cleanItemName = itemName.toLowerCase().replaceAll("\u00a7.", "").trim();
        if (cleanItemName.contains(cleanAutoBuyName = autoBuyName.toLowerCase().replace("[★] ", "").replace("[⚒] ", "").replace("[\u2744] ", "").replace("[\ud83c\udf79] ", "").trim())) {
            return true;
        }
        try {
            for (AutoBuyableItem item : this.autoBuyManager.getAllItems()) {
                if (!item.getDisplayName().equals(autoBuyName) || !AuctionUtils.compareItem(stack, item.createItemStack())) continue;
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private int calculateDiscountedPrice(int originalPrice) {
        double discount = (double)this.discountPercent / 100.0;
        return (int)((double)originalPrice * (1.0 - discount));
    }

    private boolean updateAutoBuyPrice(String itemName, int newPrice) {
        try {
            for (AutoBuyableItem item : this.autoBuyManager.getAllItems()) {
                String cleanItemName;
                String displayName = item.getDisplayName();
                if (displayName.equals(itemName)) {
                    item.getSettings().setBuyBelow(newPrice);
                    item.getSettings().saveToConfig();
                    return true;
                }
                String cleanDisplayName = displayName.replace("[★] ", "").replace("[⚒] ", "").replace("[\u2744] ", "").replace("[\ud83c\udf79] ", "").trim();
                if (!cleanDisplayName.equalsIgnoreCase(cleanItemName = itemName.replace("[★] ", "").replace("[⚒] ", "").replace("[\u2744] ", "").replace("[\ud83c\udf79] ", "").trim())) continue;
                item.getSettings().setBuyBelow(newPrice);
                item.getSettings().saveToConfig();
                return true;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return false;
    }

    private void debug(String message) {
        if (this.debugMode) {
            ChatMessage.autobuymessage(message);
        }
    }

    private String formatPrice(int price) {
        if (price >= 1000000) {
            return String.format("%.2fM$", (double)price / 1000000.0);
        }
        if (price >= 1000) {
            return String.format("%.1fK$", (double)price / 1000.0);
        }
        return price + "$";
    }

    public boolean isRunning() {
        return this.state != ParserState.IDLE;
    }

    public int getDiscountPercent() {
        return this.discountPercent;
    }

    public void setDiscountPercent(int percent) {
        this.discountPercent = percent;
    }

    public int getCurrentProgress() {
        return this.currentItemIndex;
    }

    public int getTotalItems() {
        return AutoParserItems.getItems().size();
    }

    public String getCurrentItem() {
        return this.currentSearchItem;
    }

    static {
        PAGE_PATTERN = Pattern.compile("\\[(\\d+)/(\\d+)]");
    }

    private static enum ParserState {
        IDLE,
        CLOSING_SCREEN,
        SENDING_COMMAND,
        WAITING_FOR_AUCTION,
        SCANNING_PAGE,
        CLICKING_NEXT_PAGE,
        WAITING_PAGE_CHANGE,
        FINISHING_ITEM,
        NEXT_ITEM,
        FINISHED;

    }
}

