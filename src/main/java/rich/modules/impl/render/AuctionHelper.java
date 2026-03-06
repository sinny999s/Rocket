
package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.Comparator;
import lombok.Generated;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import rich.events.api.EventHandler;
import rich.events.impl.HandledScreenEvent;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.screens.clickgui.impl.autobuy.AuctionUtils;

public class AuctionHelper
extends ModuleStructure {
    private final BooleanSetting filterThorns = new BooleanSetting("Thorns filter", "Don't show thorns armor").setValue(true);
    private final BooleanSetting showPricePerItem = new BooleanSetting("Price per piece", "Consider price per 1 item").setValue(false);
    private Slot firstSlot = null;
    private Slot secondSlot = null;
    private Slot thirdSlot = null;
    private boolean needUpdate = false;
    private int updateDelay = 0;
    static final int GREEN_COLOR = -16711936;
    static final int ORANGE_COLOR = -29696;
    static final int RED_COLOR = -52429;

    public AuctionHelper() {
        super("AuctionHelper", "Auction Helper", ModuleCategory.RENDER);
        this.settings(this.filterThorns, this.showPricePerItem);
    }

    @Override
    public void activate() {
        this.firstSlot = null;
        this.secondSlot = null;
        this.thirdSlot = null;
        this.needUpdate = false;
        this.updateDelay = 0;
    }

    @Override
    public void deactivate() {
        this.firstSlot = null;
        this.secondSlot = null;
        this.thirdSlot = null;
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof ClientboundContainerSetSlotPacket) {
            this.needUpdate = true;
            this.updateDelay = 2;
        }
    }

    @EventHandler
    public void onTick(TickEvent e) {
        Screen screen;
        if (AuctionHelper.mc.player == null || AuctionHelper.mc.level == null) {
            return;
        }
        if (this.updateDelay > 0) {
            --this.updateDelay;
            return;
        }
        if (this.needUpdate && (screen = AuctionHelper.mc.screen) instanceof ContainerScreen) {
            ContainerScreen screen2 = (ContainerScreen)screen;
            this.updateSlots(screen2);
            this.needUpdate = false;
        }
    }

    private void updateSlots(ContainerScreen screen) {
        ArrayList<SlotData> validSlots = new ArrayList<SlotData>();
        for (Slot slot : ((ChestMenu)screen.getMenu()).slots) {
            int price;
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || (price = AuctionUtils.getPrice(stack)) <= 0 || this.filterThorns.isValue() && AuctionUtils.isArmorItem(stack) && AuctionUtils.hasThornsEnchantment(stack)) continue;
            int effectivePrice = this.showPricePerItem.isValue() && stack.getCount() > 1 ? price / stack.getCount() : price;
            validSlots.add(new SlotData(slot, effectivePrice));
        }
        validSlots.sort(Comparator.comparingInt(data -> data.price));
        this.firstSlot = validSlots.size() > 0 ? ((SlotData)((Object)validSlots.get((int)0))).slot : null;
        this.secondSlot = validSlots.size() > 1 ? ((SlotData)((Object)validSlots.get((int)1))).slot : null;
        this.thirdSlot = validSlots.size() > 2 ? ((SlotData)((Object)validSlots.get((int)2))).slot : null;
    }

    @EventHandler
    public void onHandledScreen(HandledScreenEvent e) {
        if (AuctionHelper.mc.player == null) {
            return;
        }
        Screen screen = AuctionHelper.mc.screen;
        if (!(screen instanceof ContainerScreen)) {
            return;
        }
        ContainerScreen screen2 = (ContainerScreen)screen;
        GuiGraphics context = e.getDrawContext();
        int offsetX = (screen2.width - e.getBackgroundWidth()) / 2;
        int offsetY = (screen2.height - e.getBackgroundHeight()) / 2;
        this.highlightSlot(context, this.firstSlot, offsetX, offsetY, this.getBlinkingColor(-16711936));
        this.highlightSlot(context, this.secondSlot, offsetX, offsetY, this.getBlinkingColor(-29696));
        this.highlightSlot(context, this.thirdSlot, offsetX, offsetY, this.getBlinkingColor(-52429));
    }

    private int getBlinkingColor(int color) {
        float alpha = (float)Math.abs(Math.sin((double)System.currentTimeMillis() / 800.0 * Math.PI));
        int a = (int)((float)(color >> 24 & 0xFF) * (alpha = 0.1f + alpha * 0.4f));
        if (a < 50) {
            a = 50;
        }
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        return a << 24 | r << 16 | g << 8 | b;
    }

    private void highlightSlot(GuiGraphics context, Slot slot, int offsetX, int offsetY, int color) {
        if (slot == null) {
            return;
        }
        int x1 = offsetX + slot.x;
        int y1 = offsetY + slot.y;
        int x2 = x1 + 16;
        int y2 = y1 + 16;
        context.fill(x1, y1, x2, y2, color);
    }

    @Generated
    public BooleanSetting getFilterThorns() {
        return this.filterThorns;
    }

    @Generated
    public BooleanSetting getShowPricePerItem() {
        return this.showPricePerItem;
    }

    @Generated
    public Slot getFirstSlot() {
        return this.firstSlot;
    }

    @Generated
    public Slot getSecondSlot() {
        return this.secondSlot;
    }

    @Generated
    public Slot getThirdSlot() {
        return this.thirdSlot;
    }

    @Generated
    public boolean isNeedUpdate() {
        return this.needUpdate;
    }

    @Generated
    public int getUpdateDelay() {
        return this.updateDelay;
    }

    private record SlotData(Slot slot, int price) {
    }
}

