
package rich.screens.clickgui.impl.autobuy.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.Items;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;
import rich.screens.clickgui.impl.autobuy.util.KrushItem;
import rich.screens.clickgui.impl.autobuy.util.KrushItems;

public class KrushProvider {
    public static List<AutoBuyableItem> getKrush() {
        ArrayList<AutoBuyableItem> krush = new ArrayList<AutoBuyableItem>();
        krush.add(new KrushItem("Crusher Helmet", Items.NETHERITE_HELMET, KrushItems.getHelmet(), Defaultpricec.getPrice("Crusher Helmet"), false, true));
        krush.add(new KrushItem("Crusher Chestplate", Items.NETHERITE_CHESTPLATE, KrushItems.getChestplate(), Defaultpricec.getPrice("Crusher Chestplate"), false, true));
        krush.add(new KrushItem("Crusher Leggings", Items.NETHERITE_LEGGINGS, KrushItems.getLeggings(), Defaultpricec.getPrice("Crusher Leggings"), false, true));
        krush.add(new KrushItem("Crusher Boots", Items.NETHERITE_BOOTS, KrushItems.getBoots(), Defaultpricec.getPrice("Crusher Boots"), false, true));
        krush.add(new KrushItem("Sword Crusher", Items.NETHERITE_SWORD, KrushItems.getSword(), Defaultpricec.getPrice("Crusher Sword"), false, true));
        krush.add(new KrushItem("Crusher Pickaxe", Items.NETHERITE_PICKAXE, KrushItems.getPickaxe(), Defaultpricec.getPrice("Crusher Pickaxe"), false, true));
        krush.add(new KrushItem("Crusher Crossbow", Items.CROSSBOW, KrushItems.getCrossbow(), Defaultpricec.getPrice("Crusher Crossbow"), false, true));
        krush.add(new KrushItem("Crusher Bow", Items.BOW, KrushItems.getBow(), Defaultpricec.getPrice("Crusher Bow"), false, true));
        krush.add(new KrushItem("Trident Crusher", Items.TRIDENT, KrushItems.getTrident(), Defaultpricec.getPrice("Crusher Trident"), false, true));
        krush.add(new KrushItem("Mace Crusher", Items.MACE, KrushItems.getMace(), Defaultpricec.getPrice("Crusher Mace"), false, true));
        krush.add(new KrushItem("Crusher Elytra", Items.ELYTRA, KrushItems.getElytra(), Defaultpricec.getPrice("Crusher Elytra"), false, true));
        return krush;
    }
}

