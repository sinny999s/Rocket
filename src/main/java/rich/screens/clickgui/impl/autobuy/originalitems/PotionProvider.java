
package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class PotionProvider {
    public static List<AutoBuyableItem> getPotions() {
        ArrayList<AutoBuyableItem> potions = new ArrayList<AutoBuyableItem>();
        List hlopushkaLore = List.of(Component.literal((String)"Firecracker"));
        List hlopushkaEffects = List.of(new MobEffectInstance(MobEffects.SLOWNESS, 200, 9), (Object)new MobEffectInstance(MobEffects.SPEED, 400, 4), (Object)new MobEffectInstance(MobEffects.BLINDNESS, 100, 9), (Object)new MobEffectInstance(MobEffects.GLOWING, 3600, 0));
        potions.add(new CustomItem("[★] Firecracker", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Firecracker"), new PotionContents(Optional.empty(), Optional.of(16738740), hlopushkaEffects, Optional.empty()), hlopushkaLore));
        List holyWaterLore = List.of(Component.literal((String)"Holy Water"));
        List holyWaterEffects = List.of(new MobEffectInstance(MobEffects.REGENERATION, 1200, 2), (Object)new MobEffectInstance(MobEffects.INVISIBILITY, 12000, 1), (Object)new MobEffectInstance(MobEffects.INSTANT_HEALTH, 0, 1));
        potions.add(new CustomItem("[★] Holy Water", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Holy Water"), new PotionContents(Optional.empty(), Optional.of(0xFFFFFF), holyWaterEffects, Optional.empty()), holyWaterLore));
        List gnevLore = List.of(Component.literal((String)"Wrath Potion"));
        List gnevEffects = List.of(new MobEffectInstance(MobEffects.STRENGTH, 600, 4), (Object)new MobEffectInstance(MobEffects.SLOWNESS, 600, 3));
        potions.add(new CustomItem("[★] Wrath Potion", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Wrath Potion"), new PotionContents(Optional.empty(), Optional.of(0x993333), gnevEffects, Optional.empty()), gnevLore));
        List paladinLore = List.of(Component.literal((String)"Paladin Potion"));
        List paladinEffects = List.of(new MobEffectInstance(MobEffects.RESISTANCE, 12000, 0), (Object)new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0), (Object)new MobEffectInstance(MobEffects.INVISIBILITY, 18000, 0), (Object)new MobEffectInstance(MobEffects.HEALTH_BOOST, 1200, 2));
        potions.add(new CustomItem("[★] Paladin Potion", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Paladin Potion"), new PotionContents(Optional.empty(), Optional.of(65535), paladinEffects, Optional.empty()), paladinLore));
        List assassinLore = List.of(Component.literal((String)"Assassin Potion"));
        List assassinEffects = List.of(new MobEffectInstance(MobEffects.STRENGTH, 1200, 3), (Object)new MobEffectInstance(MobEffects.SPEED, 6000, 2), (Object)new MobEffectInstance(MobEffects.HASTE, 1200, 0), (Object)new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, 1));
        potions.add(new CustomItem("[★] Assassin Potion", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Assassin Potion"), new PotionContents(Optional.empty(), Optional.of(0x333333), assassinEffects, Optional.empty()), assassinLore));
        List radiationLore = List.of(Component.literal((String)"Radiation Potion"));
        List radiationEffects = List.of(new MobEffectInstance(MobEffects.POISON, 1200, 1), (Object)new MobEffectInstance(MobEffects.WITHER, 1200, 1), (Object)new MobEffectInstance(MobEffects.SLOWNESS, 1800, 2), (Object)new MobEffectInstance(MobEffects.HUNGER, 1200, 4), (Object)new MobEffectInstance(MobEffects.GLOWING, 2400, 0));
        potions.add(new CustomItem("[★] Radiation Potion", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Radiation Potion"), new PotionContents(Optional.empty(), Optional.of(3329330), radiationEffects, Optional.empty()), radiationLore));
        List snotvornoyeLore = List.of(Component.literal((String)"Sleeping Potion"));
        List snotvornoEffects = List.of(new MobEffectInstance(MobEffects.WEAKNESS, 1800, 1), (Object)new MobEffectInstance(MobEffects.MINING_FATIGUE, 200, 1), (Object)new MobEffectInstance(MobEffects.WITHER, 1800, 2), (Object)new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
        potions.add(new CustomItem("[★] Sleeping Potion", null, Items.SPLASH_POTION, Defaultpricec.getPrice("Sleeping Potion"), new PotionContents(Optional.empty(), Optional.of(0x484848), snotvornoEffects, Optional.empty()), snotvornoyeLore));
        List mandarinovySokLore = List.of(Component.literal((String)"Charge of vitamins and luck"));
        List mandarinovySokEffects = List.of(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3600, 0), (Object)new MobEffectInstance(MobEffects.JUMP_BOOST, 3600, 1), (Object)new MobEffectInstance(MobEffects.LUCK, 3600, 0), (Object)new MobEffectInstance(MobEffects.HASTE, 3600, 1));
        potions.add(new CustomItem("[\ud83c\udf79] Tangerine Juice", null, Items.POTION, Defaultpricec.getPrice("Tangerine Juice"), new PotionContents(Optional.empty(), Optional.of(14077507), mandarinovySokEffects, Optional.empty()), mandarinovySokLore));
        return potions;
    }
}

