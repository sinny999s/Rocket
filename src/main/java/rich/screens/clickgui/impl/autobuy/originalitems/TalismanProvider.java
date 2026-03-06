
package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class TalismanProvider {
    public static List<AutoBuyableItem> getTalismans() {
        ArrayList<AutoBuyableItem> talismans = new ArrayList<AutoBuyableItem>();
        List krushitelLore = List.of(Component.literal((String)"Legendary symbol."), (Object)Component.literal((String)"Indestructible power,"), (Object)Component.literal((String)"Breaking barriers."));
        talismans.add(new CustomItem("[★] Crusher Talisman", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Crusher Talisman"), null, (List<Component>)krushitelLore, true));
        List razdorLore = List.of(Component.literal((String)"Discord craves chaos,"), (Object)Component.literal((String)"Granting insane tempo,"), (Object)Component.literal((String)"But destroying armor"));
        talismans.add(new CustomItem("[★] Discord Talisman", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Discord Talisman"), null, (List<Component>)razdorLore, true));
        List tiranLore = List.of(Component.literal((String)"The Tyrant suppresses the weak."), (Object)Component.literal((String)"Grants protection and strength,"), (Object)Component.literal((String)"Taking a bloody toll."));
        talismans.add(new CustomItem("[★] Tyrant Talisman", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Tyrant Talisman"), null, (List<Component>)tiranLore, true));
        List yarostLore = List.of(Component.literal((String)"Pure, wild aggression."), (Object)Component.literal((String)"Borders on madness,"), (Object)Component.literal((String)"Trading life for damage."));
        talismans.add(new CustomItem("[★] Rage Talisman", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Rage Talisman"), null, (List<Component>)yarostLore, true));
        List vihrLore = List.of(Component.literal((String)"The vortex knows no rest,"), (Object)Component.literal((String)"Speeding up the owner"), (Object)Component.literal((String)"And tempering their spirit."));
        talismans.add(new CustomItem("[★] Vortex Talisman", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Vortex Talisman"), null, (List<Component>)vihrLore, true));
        List mrakLore = List.of(Component.literal((String)"Darkness thickens nearby,"), (Object)Component.literal((String)"Sheltering the owner"), (Object)Component.literal((String)"And feeding their strength."));
        talismans.add(new CustomItem("[★] Darkness Talisman", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Darkness Talisman"), null, (List<Component>)mrakLore, true));
        List demonLore = List.of(Component.literal((String)"The seal ignites fury,"), (Object)Component.literal((String)"Speeding up heartbeats"), (Object)Component.literal((String)"And the strength of each attack."));
        talismans.add(new CustomItem("[★] Demon Talisman", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Demon Talisman"), null, (List<Component>)demonLore, true));
        List karatelLore = List.of(Component.literal((String)"Carries a harsh sentence,"), (Object)Component.literal((String)"Punishing all enemies,"), (Object)Component.literal((String)"But weakening the body."));
        talismans.add(new CustomItem("[★] Punisher Talisman", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Punisher Talisman"), null, (List<Component>)karatelLore, true));
        List grinchLore = List.of(Component.literal((String)"The holiday thief is light,"), (Object)Component.literal((String)"His pockets full of luck,"), (Object)Component.literal((String)"But the heart is too small"));
        talismans.add(new CustomItem("[★] Grinch Talisman", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice("Grinch Talisman"), null, (List<Component>)grinchLore, true));
        return talismans;
    }
}

