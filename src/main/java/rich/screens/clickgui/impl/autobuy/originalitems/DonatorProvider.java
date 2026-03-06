
package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.UnbreakableItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class DonatorProvider {
    public static List<AutoBuyableItem> getDonator() {
        ArrayList<AutoBuyableItem> donator = new ArrayList<AutoBuyableItem>();
        List lightDustLore = List.of(Component.literal((String)"Cast: Light Flash"), (Object)Component.literal((String)"Radius: 10 blocks"), (Object)Component.literal((String)"Effects for enemies:"), (Object)Component.literal((String)" \u2022 Glowing (00:30)"), (Object)Component.literal((String)" \u2022 Blindness (00:01)"), (Object)Component.literal((String)"The closer the target, the longer the effect duration"));
        donator.add(new CustomItem("[★] Visible Dust", null, Items.SUGAR, Defaultpricec.getPrice("Visible Dust"), null, lightDustLore, false, true));
        List disorientationLore = List.of(Component.literal((String)"The closer the target, the longer the effect duration"));
        donator.add(new CustomItem("[★] Disorientation", null, Items.ENDER_EYE, Defaultpricec.getPrice("Disorientation"), null, disorientationLore, false, true));
        List trapkaLore = List.of(Component.literal((String)"Cast: Indestructible Cage"), (Object)Component.literal((String)"Duration: 15 seconds"), (Object)Component.literal((String)"Use skins: /tskins"));
        donator.add(new CustomItem("[★] Trap", null, Items.NETHERITE_SCRAP, Defaultpricec.getPrice("Trap"), null, trapkaLore, false, true));
        List lockpickSpheresLore = List.of(Component.literal((String)"With this lockpick you can"), (Object)Component.literal((String)"Open storage"), (Object)Component.literal((String)"With Spheres"));
        donator.add(new CustomItem("[★] Sphere Key", null, Items.TRIPWIRE_HOOK, Defaultpricec.getPrice("Sphere Key"), null, lockpickSpheresLore, false, true));
        List plast = List.of(Component.literal((String)"Cast: Indestructible Wall"), (Object)Component.literal((String)"Duration:"), (Object)Component.literal((String)"Vertical: 20 seconds"), (Object)Component.literal((String)"Horizontal: 60 seconds"));
        donator.add(new CustomItem("[★] Plast", null, Items.DRIED_KELP, Defaultpricec.getPrice("Plast"), null, plast, false, true));
        List exp15Lore = List.of(Component.literal((String)"Contains Lv.15 exp"));
        donator.add(new CustomItem("Exp Bottle [Lv.15]", null, Items.EXPERIENCE_BOTTLE, Defaultpricec.getPrice("Exp Bottle [Lv.15]"), null, exp15Lore, false, true));
        List exp30Lore = List.of(Component.literal((String)"Contains: Lv.30 exp"));
        donator.add(new CustomItem("Exp Bottle [Lv.30]", null, Items.EXPERIENCE_BOTTLE, Defaultpricec.getPrice("Exp Bottle [Lv.30]"), null, exp30Lore, false, true));
        List exp50Lore = List.of(Component.literal((String)"Contains Lv.50 exp"));
        donator.add(new CustomItem("Exp Bottle [Lv.50]", null, Items.EXPERIENCE_BOTTLE, Defaultpricec.getPrice("Exp Bottle [Lv.50]"), null, exp50Lore, false, true));
        List tntWhiteLore = List.of(Component.literal((String)"This dynamite explodes"), (Object)Component.literal((String)"10x stronger than normal"));
        donator.add(new CustomItem("[★] TNT - TIER WHITE", null, Items.TNT, Defaultpricec.getPrice("TNT - TIER WHITE"), null, tntWhiteLore, false, true));
        List tntBlackLore = List.of(Component.literal((String)"This dynamite explodes"), (Object)Component.literal((String)"10x stronger than normal"), (Object)Component.literal((String)"and can explode obsidian"));
        donator.add(new CustomItem("[★] TNT - TIER BLACK", null, Items.TNT, Defaultpricec.getPrice("TNT - TIER BLACK"), null, tntBlackLore, false, true));
        List signalRandomLore = List.of(Component.literal((String)"Loot Level: Random"), (Object)Component.literal((String)"With this item"), (Object)Component.literal((String)"can summon original"), (Object)Component.literal((String)"Mystical Chest!"));
        donator.add(new CustomItem("Signal Fire [Random]", null, Items.CAMPFIRE, Defaultpricec.getPrice("Signal Fire [Random]"), null, signalRandomLore));
        List signalOrdinaryLore = List.of(Component.literal((String)"Loot Level: Normal"), (Object)Component.literal((String)"With this item"), (Object)Component.literal((String)"can summon original"), (Object)Component.literal((String)"Mystical Chest!"));
        donator.add(new CustomItem("Signal Fire [Normal]", null, Items.CAMPFIRE, Defaultpricec.getPrice("Signal Fire [Normal]"), null, signalOrdinaryLore));
        List signalRichLore = List.of(Component.literal((String)"Loot Level: Rich"), (Object)Component.literal((String)"With this item"), (Object)Component.literal((String)"can summon original"), (Object)Component.literal((String)"Mystical Chest!"));
        donator.add(new CustomItem("Signal Fire [Rich]", null, Items.CAMPFIRE, Defaultpricec.getPrice("Signal Fire [Rich]"), null, signalRichLore));
        List signalLegendaryLore = List.of(Component.literal((String)"Loot Level: Legendary"), (Object)Component.literal((String)"With this item"), (Object)Component.literal((String)"can summon original"), (Object)Component.literal((String)"Mystical Chest!"));
        donator.add(new CustomItem("Signal Fire [Legendary]", null, Items.SOUL_CAMPFIRE, Defaultpricec.getPrice("Signal Fire [Legendary]"), null, signalLegendaryLore));
        List blockDamagerLore = List.of(Component.literal((String)"\u25cf Cast: Damage Dealing"), (Object)Component.literal((String)"\u25cf Radius: 1,5 blocks"));
        donator.add(new CustomItem("[★] Block Damager", null, Items.JIGSAW, Defaultpricec.getPrice("Block Damager"), null, blockDamagerLore));
        List chunkLoader1x1Lore = List.of(Component.literal((String)"Loads the chunk in which"), (Object)Component.literal((String)"this chunk loader is located."), (Object)Component.literal((String)"Press it to"), (Object)Component.literal((String)"30 seconds to see borders"), (Object)Component.literal((String)"loaded area (1x1)."));
        donator.add(new CustomItem("[★] Chunk Loader [1x1]", null, Items.STRUCTURE_BLOCK, Defaultpricec.getPrice("Chunk Loader [1x1]"), null, chunkLoader1x1Lore));
        List chunkLoader3x3Lore = List.of(Component.literal((String)"Loads the chunk in which"), (Object)Component.literal((String)"this chunk loader is located."), (Object)Component.literal((String)"Press it to"), (Object)Component.literal((String)"30 seconds to see borders"), (Object)Component.literal((String)"loaded area (3x3)."));
        donator.add(new CustomItem("[★] Chunk Loader [3x3]", null, Items.STRUCTURE_BLOCK, Defaultpricec.getPrice("Chunk Loader [3x3]"), null, chunkLoader3x3Lore));
        List chunkLoader5x5Lore = List.of(Component.literal((String)"Loads the chunk in which"), (Object)Component.literal((String)"this chunk loader is located."), (Object)Component.literal((String)"Press it to"), (Object)Component.literal((String)"30 seconds to see borders"), (Object)Component.literal((String)"loaded area (5x5)."));
        donator.add(new CustomItem("[★] Chunk Loader [5x5]", null, Items.STRUCTURE_BLOCK, Defaultpricec.getPrice("Chunk Loader [5x5]"), null, chunkLoader5x5Lore));
        List mysteriousBeaconLore = List.of(Component.literal((String)"Beacon will set temporary"), (Object)Component.literal((String)"event giving out Coins"), (Object)Component.literal((String)"players nearby."));
        donator.add(new CustomItem("Mysterious Beacon", null, Items.BEACON, Defaultpricec.getPrice("Mysterious Beacon"), null, mysteriousBeaconLore));
        List cursedSoulLore = List.of(Component.literal((String)"Exchange souls for valuable"), (Object)Component.literal((String)"resources at the Soul Collector"), (Object)Component.literal((String)"/warp soulcollector"));
        donator.add(new CustomItem("[★] Cursed Soul", null, Items.SOUL_LANTERN, Defaultpricec.getPrice("Cursed Soul"), null, cursedSoulLore, false, true));
        List dragonSkinLore = List.of(Component.literal((String)"Using this item"), (Object)Component.literal((String)"You spend it"), (Object)Component.literal((String)"and get Dragon Skin in return"), (Object)Component.literal((String)"[RMB] to use x1 skin"), (Object)Component.literal((String)"[SHIFT+RMB] to use all skins"), (Object)Component.literal((String)"Item must be held in hand"));
        donator.add(new CustomItem("[★] Dragon Skin", null, Items.PAPER, Defaultpricec.getPrice("Dragon Skin"), null, dragonSkinLore, false, true));
        List fireWhirlwindLore = List.of(Component.literal((String)"\u25cf Cast: Fire Wave"), (Object)Component.literal((String)"\u25cf Radius: 10 blocks"), (Object)Component.literal((String)""), (Object)Component.literal((String)"\u25cf Effects for enemies:"), (Object)Component.literal((String)" - Ignition (00:03)"), (Object)Component.literal((String)""), (Object)Component.literal((String)"The closer the target, the longer"), (Object)Component.literal((String)"effect duration"));
        donator.add(new CustomItem("[★] Fire tornado", null, Items.FIRE_CHARGE, Defaultpricec.getPrice("Fire tornado"), null, fireWhirlwindLore, false, true));
        List freezingSnowballLore = List.of(Component.literal((String)"\u25cf Cast: Ice Sphere"), (Object)Component.literal((String)"\u25cf Radius: 7 blocks"), (Object)Component.literal((String)""), (Object)Component.literal((String)"\u25cf Effects for enemies:"), (Object)Component.literal((String)" - Freeze (00:01)"), (Object)Component.literal((String)" - Weakness (03:00)"));
        donator.add(new CustomItem("[★] Frost Snowball", null, Items.SNOWBALL, Defaultpricec.getPrice("Frost Snowball"), null, freezingSnowballLore, false, true));
        List godsAuraLore = List.of(Component.literal((String)"\u25cf Cast: Divine Aura"), (Object)Component.literal((String)"\u25cf Radius: 2 blocks"), (Object)Component.literal((String)""), (Object)Component.literal((String)"\u25cf Effects for allies:"), (Object)Component.literal((String)" - Remove all effects"), (Object)Component.literal((String)" - Invisibility (04:00)"), (Object)Component.literal((String)" - Strength II (03:00)"), (Object)Component.literal((String)" - Speed II (03:00)"));
        donator.add(new CustomItem("[★] Divine Aura", null, Items.PHANTOM_MEMBRANE, Defaultpricec.getPrice("Divine Aura"), null, godsAuraLore, false, true));
        List silverLore = List.of(Component.literal((String)"This is currency for buying"), (Object)Component.literal((String)"lockpicks to caches"), (Object)Component.literal((String)"at the Herbalist (/warp stash)"));
        donator.add(new CustomItem("[★] Silver", null, Items.IRON_NUGGET, Defaultpricec.getPrice("Silver"), null, silverLore, false, true));
        List godsTouchLore = List.of(Component.literal((String)"Divine Touch"), (Object)Component.literal((String)"Can mine a spawner,"), (Object)Component.literal((String)"but only once"));
        donator.add(new CustomItem("[★] Divine Touch", null, Items.GOLDEN_PICKAXE, Defaultpricec.getPrice("Divine Touch"), null, godsTouchLore));
        List powerfulHitLore = List.of(Component.literal((String)"Powerful Strike"), (Object)Component.literal((String)"Can destroy bedrock,"), (Object)Component.literal((String)"but only once"));
        donator.add(new CustomItem("[★] Powerful Strike", null, Items.GOLDEN_PICKAXE, Defaultpricec.getPrice("Powerful Strike"), null, powerfulHitLore));
        List megaBulldozerLore = List.of(Component.literal((String)"Digs up territory"), (Object)Component.literal((String)"size 9x9x5 blocks"));
        donator.add(new CustomItem("[★] Mega Bulldozer Pickaxe", null, Items.NETHERITE_PICKAXE, Defaultpricec.getPrice("Mega Bulldozer Pickaxe"), null, megaBulldozerLore));
        List unbreakableElytraLore = List.of(Component.literal((String)"[⚒] Indestructible Item"));
        donator.add(new UnbreakableItem("[⚒] Indestructible Elytra", Items.ELYTRA, Defaultpricec.getPrice("Indestructible Elytra"), unbreakableElytraLore));
        return donator;
    }
}

