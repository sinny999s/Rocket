/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rich.modules.impl.misc.AutoBuy;
import rich.modules.impl.misc.autoparser.AutoParser;
import rich.modules.impl.misc.autoparser.dev.ItemParser;
import rich.screens.clickgui.impl.autobuy.manager.AutoBuyManager;
import rich.util.modules.autoparser.DiscountSliderWidget;
import rich.util.string.chat.ChatMessage;

@Mixin(value={ContainerScreen.class})
public abstract class GenericContainerScreenMixin
extends AbstractContainerScreen<ChestMenu> {
    @Unique
    private static long lastScreenOpenTime = 0L;
    @Unique
    private static String lastScreenTitle = "";
    @Unique
    private static final long SCREEN_REOPEN_THRESHOLD = 500L;
    @Unique
    private Button takeAllButton;
    @Unique
    private Button dropAllButton;
    @Unique
    private Button storeAllButton;
    @Unique
    private Button autoBuyButton;
    @Unique
    private Button autoParserButton;
    @Unique
    private DiscountSliderWidget discountSlider;
    @Unique
    private Button parseButton;
    @Unique
    private boolean buttonsAdded = false;
    @Unique
    private boolean isQuickReopen = false;
    @Unique
    private final AutoBuyManager autoBuyManager = AutoBuyManager.getInstance();

    public GenericContainerScreenMixin(ChestMenu handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void onInit(ChestMenu handler, Inventory inventory, Component title, CallbackInfo ci) {
        long now = System.currentTimeMillis();
        String currentTitle = title.getString();
        this.isQuickReopen = currentTitle.equals(lastScreenTitle) && now - lastScreenOpenTime < 500L;
        lastScreenOpenTime = now;
        lastScreenTitle = currentTitle;
    }

    @Inject(method={"render"}, at={@At(value="TAIL")})
    private void onRender(GuiGraphics context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        try {
            Minecraft mc = Minecraft.getInstance();
            String title = this.getTitle().getString();
            if (!this.buttonsAdded) {
                this.addButtons(mc, title);
                this.buttonsAdded = true;
            }
            this.updateAutoBuyButton();
            this.updateAutoParserButton();
            this.renderNetworkInfo(context, mc, title);
        } catch (Exception e) {
            // Prevent class loading failures from crashing the game
        }
    }

    @Unique
    private void updateAutoBuyButton() {
        String status;
        if (this.autoBuyButton == null) {
            return;
        }
        AutoBuy autoBuyModule = AutoBuy.getInstance();
        boolean moduleActive = autoBuyModule != null && autoBuyModule.isState();
        boolean buttonEnabled = this.autoBuyManager.isEnabled();
        if (!moduleActive) {
            status = "\u00a7cOFF";
            this.autoBuyButton.active = false;
        } else if (buttonEnabled) {
            status = "\u00a7aON";
            this.autoBuyButton.active = true;
        } else {
            status = "\u00a7ePAUSE";
            this.autoBuyButton.active = true;
        }
        this.autoBuyButton.setMessage(Component.literal((String)("AutoBuy: " + status)));
    }

    @Unique
    private void updateAutoParserButton() {
        AutoParser parser = AutoParser.getInstance();
        if (this.autoParserButton == null || parser == null) {
            return;
        }
        boolean isRunning = parser.isRunning();
        this.autoParserButton.setMessage(Component.literal((String)("AutoParser: " + (isRunning ? "\u00a7aON" : "\u00a7cOFF"))));
        if (this.discountSlider != null) {
            this.discountSlider.active = !isRunning;
        }
    }

    @Unique
    private void renderNetworkInfo(GuiGraphics context, Minecraft mc, String title) {
        if (this.autoBuyButton == null || !title.contains("Auction")) {
            return;
        }
        AutoBuy autoBuyModule = AutoBuy.getInstance();
        if (autoBuyModule == null || !autoBuyModule.isState()) {
            return;
        }
        int clients = autoBuyModule.getNetworkManager().getConnectedClientCount();
        int inAuction = autoBuyModule.getNetworkManager().getClientsInAuctionCount();
        boolean connected = autoBuyModule.getNetworkManager().isConnectedToServer();
        boolean isServer = autoBuyModule.getNetworkManager().isServerRunning();
        Object info = isServer ? "\u00a77Clients: \u00a7b" + clients + " \u00a77In auction: \u00a7b" + inAuction : (connected ? "\u00a7aConnected to server" : "\u00a7cNo connection");
        int infoX = (this.width - this.imageWidth) / 2 + this.imageWidth / 2;
        int infoY = (this.height - this.imageHeight) / 2 - 10;
        context.drawCenteredString(mc.font, Component.literal((String)info), infoX, infoY, 0xFFFFFF);
    }

    @Unique
    private void addButtons(Minecraft mc, String titleText) {
        int baseX = (this.width + this.imageWidth) / 2;
        int baseY = (this.height - this.imageHeight) / 2;
        this.dropAllButton = Button.builder((Component)Component.literal((String)"Drop All"), button -> this.dropAll(mc)).bounds(baseX, baseY, 80, 20).build();
        this.takeAllButton = Button.builder((Component)Component.literal((String)"Take All"), button -> this.takeAll(mc)).bounds(baseX, baseY + 22, 80, 20).build();
        this.storeAllButton = Button.builder((Component)Component.literal((String)"Store All"), button -> this.storeAll(mc)).bounds(baseX, baseY + 44, 80, 20).build();
        this.addRenderableWidget(this.dropAllButton);
        this.addRenderableWidget(this.takeAllButton);
        this.addRenderableWidget(this.storeAllButton);
        int autoBuyX = (this.width - this.imageWidth) / 2 + this.imageWidth / 2 - 55;
        int autoBuyY = (this.height - this.imageHeight) / 2 - 25;
        AutoBuy autoBuyModule = AutoBuy.getInstance();
        boolean moduleActive = autoBuyModule != null && autoBuyModule.isState();
        boolean buttonEnabled = this.autoBuyManager.isEnabled();
        String initialStatus = !moduleActive ? "\u00a7cOFF" : (buttonEnabled ? "\u00a7aON" : "\u00a7ePAUSE");
        this.autoBuyButton = Button.builder((Component)Component.literal((String)("AutoBuy: " + initialStatus)), button -> this.handleAutoBuyButtonClick(button)).bounds(autoBuyX, autoBuyY, 110, 20).build();
        this.autoBuyButton.active = moduleActive;
        this.addRenderableWidget(this.autoBuyButton);
        int leftX = (this.width - this.imageWidth) / 2 - 100;
        int leftY = (this.height - this.imageHeight) / 2;
        AutoParser parser = AutoParser.getInstance();
        int initialDiscount = parser != null ? parser.getDiscountPercent() : 60;
        this.autoParserButton = Button.builder((Component)Component.literal((String)"AutoParser: \u00a7cOFF"), button -> this.handleAutoParserButtonClick()).bounds(leftX, leftY, 95, 20).build();
        this.addRenderableWidget(this.autoParserButton);
        this.discountSlider = new DiscountSliderWidget(leftX, leftY + 24, 95, 20, initialDiscount);
        this.addRenderableWidget(this.discountSlider);
    }

    @Unique
    private void handleAutoParserButtonClick() {
        AutoParser parser = AutoParser.getInstance();
        if (parser == null) {
            ChatMessage.autobuymessageError("AutoParser not initialized!");
            return;
        }
        if (parser.isRunning()) {
            parser.stopParsing();
        } else {
            parser.startParsing();
        }
    }

    @Unique
    private void handleParseButtonClick() {
        try {
            ItemParser parser = ItemParser.getInstance();
            if (parser == null || !parser.isState()) {
                ChatMessage.autobuymessageError("Enable the Item Parser module first!");
                return;
            }
            int containerSize = ((ChestMenu)this.menu).getRowCount() * 9;
            ArrayList<Slot> containerSlots = new ArrayList<Slot>();
            for (int i = 0; i < containerSize && i < ((ChestMenu)this.menu).slots.size(); ++i) {
                containerSlots.add((Slot)((ChestMenu)this.menu).slots.get(i));
            }
            String containerTitle = this.getTitle().getString();
            parser.parseAllSlots(containerSlots, containerSize, containerTitle);
        } catch (Exception e) {
            ChatMessage.autobuymessageError("Item Parser failed to load!");
        }
    }

    @Unique
    private void handleAutoBuyButtonClick(Button button) {
        String status;
        AutoBuy autoBuyModule = AutoBuy.getInstance();
        if (autoBuyModule == null || !autoBuyModule.isState()) {
            ChatMessage.autobuymessageError("Enable the Auto Buy module first!");
            button.setMessage(Component.literal((String)"AutoBuy: \u00a7cOFF"));
            button.active = false;
            return;
        }
        boolean currentState = this.autoBuyManager.isEnabled();
        boolean newState = !currentState;
        this.autoBuyManager.setEnabled(newState);
        if (newState) {
            status = "\u00a7aON";
            ChatMessage.autobuymessageSuccess("AutoBuy enabled!");
        } else {
            status = "\u00a7ePAUSE";
            ChatMessage.autobuymessageWarning("AutoBuy paused");
        }
        button.setMessage(Component.literal((String)("AutoBuy: " + status)));
    }

    @Unique
    private void takeAll(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || player.containerMenu == null) {
            return;
        }
        for (Slot slot : player.containerMenu.slots) {
            if (slot.container == player.getInventory() || !slot.hasItem()) continue;
            mc.gameMode.handleInventoryMouseClick(player.containerMenu.containerId, slot.index, 0, ClickType.QUICK_MOVE, player);
        }
    }

    @Unique
    private void dropAll(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || player.containerMenu == null) {
            return;
        }
        for (Slot slot : player.containerMenu.slots) {
            if (slot.container == player.getInventory() || !slot.hasItem()) continue;
            mc.gameMode.handleInventoryMouseClick(player.containerMenu.containerId, slot.index, 1, ClickType.THROW, player);
        }
    }

    @Unique
    private void storeAll(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || player.containerMenu == null) {
            return;
        }
        for (Slot slot : player.containerMenu.slots) {
            if (slot.container != player.getInventory() || !slot.hasItem()) continue;
            mc.gameMode.handleInventoryMouseClick(player.containerMenu.containerId, slot.index, 0, ClickType.QUICK_MOVE, player);
        }
    }
}

