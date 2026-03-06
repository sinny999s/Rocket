/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InventoryScreen.class})
public abstract class InventoryScreenMixin {
    @Inject(method={"init"}, at={@At(value="TAIL")})
    private void addDropAllButton(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        InventoryScreen screen = (InventoryScreen)((Object)this);
        int x = screen.width / 2 - 40;
        int y = screen.height / 2 - 120;
        Button dropAllButton = Button.builder((Component)Component.nullToEmpty((String)"Drop All"), button -> this.dropAllItems(mc)).pos(x, y).size(80, 20).build();
        screen.addRenderableWidget(dropAllButton);
    }

    private void dropAllItems(Minecraft mc) {
        ItemStack stack;
        int i;
        LocalPlayer player = mc.player;
        if (player == null || player.containerMenu == null) {
            return;
        }
        for (i = 9; i < 36; ++i) {
            stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            mc.gameMode.handleInventoryMouseClick(player.containerMenu.containerId, i, 1, ClickType.THROW, player);
        }
        for (i = 0; i < 9; ++i) {
            stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;
            mc.gameMode.handleInventoryMouseClick(player.containerMenu.containerId, i + 36, 1, ClickType.THROW, player);
        }
    }
}

