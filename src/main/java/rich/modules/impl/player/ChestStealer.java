
package rich.modules.impl.player;

import antidaunleak.api.annotation.Native;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import rich.events.api.EventHandler;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.inventory.InventoryUtils;
import rich.util.timer.StopWatch;

public class ChestStealer
extends ModuleStructure {
    private final StopWatch stopWatch = new StopWatch();
    private final SelectSetting modeSetting = new SelectSetting("Type", "Selects style type").value("FunTime", "WhiteList", "Default").selected("FunTime");
    private final SliderSettings delaySetting = new SliderSettings("Delay", "Delay between slot clicks").setValue(100.0f).range(0, 1000).visible(() -> this.modeSetting.isSelected("WhiteList") || this.modeSetting.isSelected("Default"));
    private final MultiSelectSetting itemSettings = new MultiSelectSetting("Items", "Select items the stealer will pick up").value("Player Head", "Totem Of Undying", "Elytra", "Netherite Sword", "Netherite Helmet", "Netherite ChestPlate", "Netherite Leggings", "Netherite Boots", "Netherite Ingot", "Netherite Scrap").visible(() -> this.modeSetting.isSelected("WhiteList"));

    public ChestStealer() {
        super("ChestStealer", "Chest Stealer", ModuleCategory.PLAYER);
        this.settings(this.modeSetting, this.delaySetting, this.itemSettings);
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent e) {
        if (ChestStealer.mc.player == null) {
            return;
        }
        switch (this.modeSetting.getSelected()) {
            case "FunTime": {
                this.handleFunTimeMode();
                break;
            }
            case "WhiteList": 
            case "Default": {
                this.handleDefaultMode();
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleFunTimeMode() {
        ContainerScreen sh;
        Screen screen = ChestStealer.mc.screen;
        if (screen instanceof ContainerScreen && (sh = (ContainerScreen)screen).getTitle().getString().toLowerCase().contains("mystical") && !ChestStealer.mc.player.getCooldowns().isOnCooldown(Items.GUNPOWDER.getDefaultInstance())) {
            ((ChestMenu)sh.getMenu()).slots.stream().filter(s -> s.hasItem() && !s.container.equals(ChestStealer.mc.player.getInventory()) && this.stopWatch.every(150.0)).forEach(s -> InventoryUtils.click(s.index, 0, ClickType.QUICK_MOVE));
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleDefaultMode() {
        AbstractContainerMenu screenHandler = ChestStealer.mc.player.containerMenu;
        if (screenHandler instanceof ChestMenu) {
            ChestMenu sh = (ChestMenu)screenHandler;
            sh.slots.forEach(s -> {
                if (s.hasItem() && !s.container.equals(ChestStealer.mc.player.getInventory()) && (this.modeSetting.isSelected("Default") || this.whiteList(s.getItem().getItem())) && this.stopWatch.every(this.delaySetting.getValue())) {
                    InventoryUtils.click(s.index, 0, ClickType.QUICK_MOVE);
                }
            });
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private boolean whiteList(Item item) {
        return this.itemSettings.getSelected().toString().toLowerCase().contains(item.toString().toLowerCase().replace("_", ""));
    }
}

