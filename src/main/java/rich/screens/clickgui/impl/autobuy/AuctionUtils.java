
package rich.screens.clickgui.impl.autobuy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class AuctionUtils {
    public static final Pattern funTimePricePattern = Pattern.compile("\\$([\\d]+(?:[\\s,][\\d]{3})*(?:\\.[\\d]{2})?)");
    private static final Pattern digitPattern = Pattern.compile("([\\d][\\d\\s,.]*)");

    public static int getPrice(ItemStack stack) {
        Matcher matcher;
        String componentString;
        DataComponentMap components;
        Matcher matcher2;
        String itemName;
        String priceStr = null;
        ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
        if (lore != null && !lore.lines().isEmpty()) {
            for (Component line : lore.lines()) {
                String lineStr = line.getString();
                if (!lineStr.contains("$") && !lineStr.toLowerCase().contains("price")) continue;
                Matcher matcher3 = funTimePricePattern.matcher(lineStr);
                if (matcher3.find()) {
                    priceStr = matcher3.group(1);
                    break;
                }
                matcher3 = digitPattern.matcher(lineStr);
                if (!matcher3.find()) continue;
                priceStr = matcher3.group(1);
                break;
            }
        }
        if ((priceStr == null || priceStr.isEmpty()) && (itemName = stack.getHoverName().getString()) != null && (matcher2 = funTimePricePattern.matcher(itemName)).find()) {
            priceStr = matcher2.group(1);
        }
        if ((priceStr == null || priceStr.isEmpty()) && (components = stack.getComponents()) != null && (componentString = components.toString()).contains("$") && (matcher = funTimePricePattern.matcher(componentString)).find()) {
            priceStr = matcher.group(1);
        }
        if (priceStr == null || priceStr.isEmpty()) {
            return -1;
        }
        try {
            priceStr = priceStr.replaceAll("[\\s,.$]", "").trim();
            if (priceStr.isEmpty()) {
                return -1;
            }
            return Integer.parseInt(priceStr);
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String cleanString(String str) {
        if (str == null) {
            return "";
        }
        return str.toLowerCase().trim().replaceAll("\u00a7.", "").replaceAll("[^a-zа-яё0-9\\s\\[\\]★⚒+()]", "").replaceAll("\\s+", " ");
    }

    private static List<String> getLoreStrings(ItemStack stack) {
        ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
        if (lore == null || lore.lines().isEmpty()) {
            return List.of();
        }
        return lore.lines().stream().map(text -> text.getString().toLowerCase()).collect(Collectors.toList());
    }

    private static boolean loreContains(ItemStack stack, String phrase) {
        List<String> loreLines = AuctionUtils.getLoreStrings(stack);
        String phraseLower = phrase.toLowerCase();
        for (String line : loreLines) {
            if (!line.contains(phraseLower)) continue;
            return true;
        }
        return false;
    }

    private static boolean loreContainsAny(ItemStack stack, String ... phrases) {
        List<String> loreLines = AuctionUtils.getLoreStrings(stack);
        for (String phrase : phrases) {
            String phraseLower = phrase.toLowerCase();
            for (String line : loreLines) {
                if (!line.contains(phraseLower)) continue;
                return true;
            }
        }
        return false;
    }

    private static String extractChunkLoaderSize(ItemStack stack) {
        List<String> loreLines = AuctionUtils.getLoreStrings(stack);
        for (String line : loreLines) {
            if (line.contains("(1x1)") || line.contains("area (1x1)")) {
                return "1x1";
            }
            if (line.contains("(3x3)") || line.contains("area (3x3)")) {
                return "3x3";
            }
            if (!line.contains("(5x5)") && !line.contains("area (5x5)")) continue;
            return "5x5";
        }
        return null;
    }

    private static boolean isTntBlackType(ItemStack stack) {
        return AuctionUtils.loreContains(stack, "obsidian") || AuctionUtils.loreContains(stack, "can explode obsidian");
    }

    private static String getLockpickType(ItemStack stack) {
        List<String> loreLines = AuctionUtils.getLoreStrings(stack);
        for (String line : loreLines) {
            if (line.contains("with spheres") || line.contains("spheres")) {
                return "spheres";
            }
            if (line.contains("with keys") || line.contains("keys")) {
                return "keys";
            }
            if (!line.contains("with coins") && !line.contains("coins")) continue;
            return "coins";
        }
        return "unknown";
    }

    private static boolean isDragonSkin(ItemStack stack) {
        return AuctionUtils.loreContains(stack, "dragon skin");
    }

    private static String getSkinType(ItemStack stack) {
        List<String> loreLines = AuctionUtils.getLoreStrings(stack);
        for (String line : loreLines) {
            if (line.contains("dragon skin")) {
                return "dragon";
            }
            if (line.contains("ice skin")) {
                return "ice";
            }
            if (!line.contains("fire skin")) continue;
            return "fire";
        }
        return "unknown";
    }

    private static boolean isValidTrap(ItemStack stack) {
        return AuctionUtils.loreContains(stack, "indestructible cage");
    }

    private static String getTrapSkinType(ItemStack stack) {
        if (!AuctionUtils.isValidTrap(stack)) {
            return "invalid";
        }
        List<String> loreLines = AuctionUtils.getLoreStrings(stack);
        for (String line : loreLines) {
            if (line.contains("dragon") || line.contains("dragon")) {
                return "dragon";
            }
            if (line.contains("icy") || line.contains("icy") || line.contains("ice")) {
                return "ice";
            }
            if (!line.contains("fiery") && !line.contains("fire")) continue;
            return "fire";
        }
        return "standard";
    }

    private static String getSignalFireLootLevel(ItemStack stack) {
        List<String> loreLines = AuctionUtils.getLoreStrings(stack);
        for (String line : loreLines) {
            if (!line.contains("loot level:") && !line.contains("loot level")) continue;
            if (line.contains("legendary")) {
                return "legendary";
            }
            if (line.contains("rich")) {
                return "rich";
            }
            if (line.contains("normal")) {
                return "ordinary";
            }
            if (!line.contains("random")) continue;
            return "random";
        }
        return "unknown";
    }

    private static boolean isSignalFire(ItemStack stack) {
        return stack.getItem() == Items.CAMPFIRE || stack.getItem() == Items.SOUL_CAMPFIRE;
    }

    private static boolean isValidSignalFire(ItemStack stack) {
        return AuctionUtils.isSignalFire(stack) && AuctionUtils.loreContains(stack, "mystical chest");
    }

    private static boolean isValidLockpick(ItemStack stack) {
        return AuctionUtils.loreContainsAny(stack, "open storage", "with this lockpick you can");
    }

    private static boolean isValidExperienceBottle(ItemStack stack) {
        return AuctionUtils.loreContainsAny(stack, "contains", "lv exp", "lv. exp");
    }

    private static boolean isValidTnt(ItemStack stack) {
        return AuctionUtils.loreContains(stack, "dynamite explodes");
    }

    private static boolean isValidDragonSkin(ItemStack stack) {
        return AuctionUtils.loreContains(stack, "dragon skin");
    }

    private static boolean isValidChunkLoader(ItemStack stack) {
        return AuctionUtils.loreContains(stack, "loads chunk");
    }

    public static boolean isArmorItem(ItemStack stack) {
        return stack.getItem() == Items.NETHERITE_HELMET || stack.getItem() == Items.NETHERITE_CHESTPLATE || stack.getItem() == Items.NETHERITE_LEGGINGS || stack.getItem() == Items.NETHERITE_BOOTS || stack.getItem() == Items.DIAMOND_HELMET || stack.getItem() == Items.DIAMOND_CHESTPLATE || stack.getItem() == Items.DIAMOND_LEGGINGS || stack.getItem() == Items.DIAMOND_BOOTS || stack.getItem() == Items.IRON_HELMET || stack.getItem() == Items.IRON_CHESTPLATE || stack.getItem() == Items.IRON_LEGGINGS || stack.getItem() == Items.IRON_BOOTS || stack.getItem() == Items.GOLDEN_HELMET || stack.getItem() == Items.GOLDEN_CHESTPLATE || stack.getItem() == Items.GOLDEN_LEGGINGS || stack.getItem() == Items.GOLDEN_BOOTS || stack.getItem() == Items.CHAINMAIL_HELMET || stack.getItem() == Items.CHAINMAIL_CHESTPLATE || stack.getItem() == Items.CHAINMAIL_LEGGINGS || stack.getItem() == Items.CHAINMAIL_BOOTS || stack.getItem() == Items.LEATHER_HELMET || stack.getItem() == Items.LEATHER_CHESTPLATE || stack.getItem() == Items.LEATHER_LEGGINGS || stack.getItem() == Items.LEATHER_BOOTS || stack.getItem() == Items.TURTLE_HELMET;
    }

    public static boolean hasThornsEnchantment(ItemStack stack) {
        ItemEnchantments enchants = (ItemEnchantments)stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            return false;
        }
        for (Holder entry : enchants.keySet()) {
            String lowerEnchantId;
            String enchantId = ((net.minecraft.core.Holder<?>)entry).getRegisteredName();
            if (enchantId == null || !(lowerEnchantId = enchantId.toLowerCase()).contains("thorns") && !lowerEnchantId.contains("thorn")) continue;
            return true;
        }
        ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
        if (lore != null) {
            for (Component line : lore.lines()) {
                String loreStr = line.getString().toLowerCase();
                if (!loreStr.contains("thorns") && !loreStr.contains("thorn")) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean hasVanishingCurse(ItemStack stack) {
        ItemEnchantments enchants = (ItemEnchantments)stack.get(DataComponents.ENCHANTMENTS);
        if (enchants == null || enchants.isEmpty()) {
            return false;
        }
        for (Holder entry : enchants.keySet()) {
            String enchantId = ((net.minecraft.core.Holder<?>)entry).getRegisteredName();
            if (enchantId == null || !enchantId.toLowerCase().contains("vanishing")) continue;
            return true;
        }
        return false;
    }

    public static boolean isUnbreakableItem(ItemStack stack) {
        CompoundTag nbt;
        CustomData customData = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && (nbt = customData.copyTag()).getBooleanOr("Unbreakable", false)) {
            return true;
        }
        String name = stack.getHoverName().getString().toLowerCase();
        return name.contains("indestructible") || name.contains("[⚒]");
    }

    public static boolean isSplashPotion(ItemStack stack) {
        return stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION;
    }

    public static Map<Holder<MobEffect>, EffectData> getPotionEffects(ItemStack stack) {
        HashMap<Holder<MobEffect>, EffectData> effects = new HashMap<Holder<MobEffect>, EffectData>();
        PotionContents potionContents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents == null) {
            return effects;
        }
        for (MobEffectInstance effect : potionContents.customEffects()) {
            effects.put(effect.getEffect(), new EffectData(effect.getAmplifier(), effect.getDuration()));
        }
        return effects;
    }

    public static boolean hasEffect(ItemStack stack, Holder<MobEffect> effectType, int minAmplifier) {
        Map<Holder<MobEffect>, EffectData> effects = AuctionUtils.getPotionEffects(stack);
        EffectData data = effects.get(effectType);
        return data != null && data.amplifier >= minAmplifier;
    }

    public static boolean matchesPotionEffects(ItemStack auctionItem, List<PotionEffectRequirement> requirements) {
        if (!AuctionUtils.isSplashPotion(auctionItem)) {
            return false;
        }
        Map<Holder<MobEffect>, EffectData> auctionEffects = AuctionUtils.getPotionEffects(auctionItem);
        if (auctionEffects.isEmpty()) {
            return false;
        }
        for (PotionEffectRequirement req : requirements) {
            EffectData data = auctionEffects.get(req.effect);
            if (data == null) {
                return false;
            }
            if (data.amplifier >= req.minAmplifier) continue;
            return false;
        }
        return true;
    }

    public static boolean compareItem(ItemStack a, ItemStack b) {
        boolean hasLore;
        if (!(a.getItem() == b.getItem() || AuctionUtils.isSignalFire(a) && AuctionUtils.isSignalFire(b))) {
            return false;
        }
        if (AuctionUtils.isArmorItem(a) && AuctionUtils.hasThornsEnchantment(a)) {
            return false;
        }
        if (a.getItem() == Items.NETHERITE_SCRAP) {
            if (!AuctionUtils.isValidTrap(a)) {
                return false;
            }
            if (!AuctionUtils.isValidTrap(b)) {
                return true;
            }
            String aType = AuctionUtils.getTrapSkinType(a);
            String bType = AuctionUtils.getTrapSkinType(b);
            if (bType.equals("standard")) {
                return aType.equals("standard") || aType.equals("dragon") || aType.equals("ice") || aType.equals("fire");
            }
            return aType.equals(bType);
        }
        if (AuctionUtils.isSignalFire(a) && AuctionUtils.isSignalFire(b)) {
            if (!AuctionUtils.isValidSignalFire(a)) {
                return false;
            }
            String aLevel = AuctionUtils.getSignalFireLootLevel(a);
            String bLevel = AuctionUtils.getSignalFireLootLevel(b);
            return bLevel.equals("unknown") || aLevel.equals(bLevel);
        }
        if (a.getItem() == Items.STRUCTURE_BLOCK) {
            if (!AuctionUtils.isValidChunkLoader(a)) {
                return false;
            }
            String aSize = AuctionUtils.extractChunkLoaderSize(a);
            String bSize = AuctionUtils.extractChunkLoaderSize(b);
            return bSize == null || aSize != null && aSize.equals(bSize);
        }
        if (a.getItem() == Items.TNT) {
            boolean bIsBlack;
            if (!AuctionUtils.isValidTnt(a)) {
                return false;
            }
            boolean aIsBlack = AuctionUtils.isTntBlackType(a);
            return aIsBlack == (bIsBlack = AuctionUtils.isTntBlackType(b));
        }
        if (a.getItem() == Items.TRIPWIRE_HOOK) {
            if (!AuctionUtils.isValidLockpick(a)) {
                return false;
            }
            String aType = AuctionUtils.getLockpickType(a);
            String bType = AuctionUtils.getLockpickType(b);
            return bType.equals("unknown") || aType.equals(bType);
        }
        if (a.getItem() == Items.PAPER) {
            boolean bIsDragon = AuctionUtils.isDragonSkin(b);
            if (bIsDragon && !AuctionUtils.isValidDragonSkin(a)) {
                return false;
            }
            String aType = AuctionUtils.getSkinType(a);
            String bType = AuctionUtils.getSkinType(b);
            return bType.equals("unknown") || aType.equals(bType);
        }
        if (a.getItem() == Items.EXPERIENCE_BOTTLE) {
            ItemLore bLoreComp = (ItemLore)b.get(DataComponents.LORE);
            if (bLoreComp != null && !bLoreComp.lines().isEmpty()) {
                if (!AuctionUtils.isValidExperienceBottle(a)) {
                    return false;
                }
                String aExpLevel = AuctionUtils.getExperienceLevel(a);
                String bExpLevel = AuctionUtils.getExperienceLevel(b);
                if (!bExpLevel.equals("unknown") && !aExpLevel.equals(bExpLevel)) {
                    return false;
                }
            }
            return true;
        }
        String aName = a.getHoverName().getString();
        aName = funTimePricePattern.matcher(aName).replaceAll("").trim();
        String bName = b.getHoverName().getString();
        String aNameClean = AuctionUtils.cleanString(aName);
        String bNameClean = AuctionUtils.cleanString(bName);
        if (bNameClean.contains("⚒") || bNameClean.contains("indestructible")) {
            if (!AuctionUtils.isUnbreakableItem(a) && !AuctionUtils.hasVanishingCurse(a)) {
                return false;
            }
            if (aNameClean.contains("indestructible") && bNameClean.contains("indestructible")) {
                return aNameClean.contains("elytra") && bNameClean.contains("elytra");
            }
        }
        ItemLore aLore = (ItemLore)a.get(DataComponents.LORE);
        ItemLore bLoreComp = (ItemLore)b.get(DataComponents.LORE);
        boolean bl = hasLore = bLoreComp != null && !bLoreComp.lines().isEmpty();
        if (AuctionUtils.isSplashPotion(a) && AuctionUtils.isSplashPotion(b)) {
            return AuctionUtils.comparePotionsByEffects(a, b);
        }
        if (hasLore) {
            double matchRatio;
            List<Component> expectedLore = (List<Component>)bLoreComp.lines();
            if (aLore == null || aLore.lines().isEmpty()) {
                return false;
            }
            List<String> auctionLoreStrings = aLore.lines().stream().map(text -> AuctionUtils.cleanString(text.getString())).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            String auctionLoreJoined = String.join((CharSequence)" ", auctionLoreStrings);
            List<String> criticalPhrases = List.of((String[])new String[]{"with spheres", "spheres", "dragon skin", "obsidian", "can explode obsidian", "area (1x1)", "area (3x3)", "area (5x5)", "indestructible cage", "tier black", "tier white", "loot level legendary", "loot level rich", "loot level normal", "loot level random", "mystical chest", "loads chunk", "dynamite explodes", "open storage"});
            for (Object _exp : expectedLore) { Component expected = (Component)_exp;
                String expectedStr = AuctionUtils.cleanString(expected.getString());
                if (expectedStr.isEmpty()) continue;
                for (String critical : criticalPhrases) {
                    if (!expectedStr.contains(critical)) continue;
                    boolean foundInAuction = false;
                    for (String auctionLine : auctionLoreStrings) {
                        if (!auctionLine.contains(critical)) continue;
                        foundInAuction = true;
                        break;
                    }
                    if (foundInAuction || auctionLoreJoined.contains(critical)) continue;
                    return false;
                }
            }
            boolean hasOriginalMarker = false;
            boolean hasUnbreakableMarker = false;
            for (String line : auctionLoreStrings) {
                if (line.contains("original item") || line.contains("★")) {
                    hasOriginalMarker = true;
                }
                if (!line.contains("indestructible") && !line.contains("⚒")) continue;
                hasUnbreakableMarker = true;
            }
            int matchCount = 0;
            int requiredMatches = 0;
            for (Object _exp : expectedLore) { Component expected = (Component)_exp;
                boolean isUnbreakableMarker;
                String expectedStr = AuctionUtils.cleanString(expected.getString());
                if (expectedStr.isEmpty()) continue;
                boolean isOriginalMarker = expectedStr.contains("original item") || expectedStr.contains("★");
                boolean bl2 = isUnbreakableMarker = expectedStr.contains("indestructible") || expectedStr.contains("⚒");
                if (isOriginalMarker) {
                    if (!hasOriginalMarker) {
                        return false;
                    }
                    ++matchCount;
                    ++requiredMatches;
                    continue;
                }
                if (isUnbreakableMarker) {
                    if (!(hasUnbreakableMarker || AuctionUtils.isUnbreakableItem(a) || AuctionUtils.hasVanishingCurse(a))) {
                        return false;
                    }
                    ++matchCount;
                    ++requiredMatches;
                    continue;
                }
                ++requiredMatches;
                boolean found = false;
                for (String auctionLine : auctionLoreStrings) {
                    if (!auctionLine.contains(expectedStr) && !expectedStr.contains(auctionLine)) continue;
                    found = true;
                    break;
                }
                if (!found && auctionLoreJoined.contains(expectedStr)) {
                    found = true;
                }
                if (!found) continue;
                ++matchCount;
            }
            double d = matchRatio = requiredMatches > 0 ? (double)matchCount / (double)requiredMatches : 1.0;
            if (matchRatio < 0.7) {
                return false;
            }
            if (hasOriginalMarker) {
                ItemEnchantments aEnchants = (ItemEnchantments)a.get(DataComponents.ENCHANTMENTS);
                ItemEnchantments bEnchants = (ItemEnchantments)b.get(DataComponents.ENCHANTMENTS);
                if (bEnchants != null && !bEnchants.isEmpty()) {
                    if (aEnchants == null || aEnchants.isEmpty()) {
                        return false;
                    }
                    HashMap<String, Integer> aEnchantMap = new HashMap<String, Integer>();
                    for (Object entry : (Iterable<?>)aEnchants.keySet()) {
                        String enchantId = ((net.minecraft.core.Holder<?>)entry).getRegisteredName();
                        if (enchantId == null) continue;
                        String string = enchantId.replace("minecraft:", "").toLowerCase();
                        int level = aEnchants.getLevel((Holder)entry);
                        aEnchantMap.put(string, level);
                    }
                    HashMap<String, Integer> bEnchantMap = new HashMap<String, Integer>();
                    for (Object entry : (Iterable<?>)bEnchants.keySet()) {
                        String string = ((net.minecraft.core.Holder<?>)entry).getRegisteredName();
                        if (string == null) continue;
                        String enchantName = string.replace("minecraft:", "").toLowerCase();
                        int level = bEnchants.getLevel((Holder)entry);
                        bEnchantMap.put(enchantName, level);
                    }
                    if (bEnchantMap.isEmpty()) {
                        return true;
                    }
                    int enchantMatchCount = 0;
                    for (Map.Entry entry : bEnchantMap.entrySet()) {
                        String bEnchantName = (String)entry.getKey();
                        Integer aLevel = (Integer)aEnchantMap.get(bEnchantName);
                        if (aLevel == null || aLevel < 1) continue;
                        ++enchantMatchCount;
                    }
                    double enchantMatchRatio = (double)enchantMatchCount / (double)bEnchantMap.size();
                    if (enchantMatchRatio < 1.0) {
                        return false;
                    }
                }
            }
        } else if (!aNameClean.contains(bNameClean) && !bNameClean.contains(aNameClean)) {
            return false;
        }
        return true;
    }

    private static String getExperienceLevel(ItemStack stack) {
        List<String> loreLines = AuctionUtils.getLoreStrings(stack);
        for (String line : loreLines) {
            if (line.contains("15")) {
                return "15";
            }
            if (line.contains("30")) {
                return "30";
            }
            if (!line.contains("50")) continue;
            return "50";
        }
        return "unknown";
    }

    private static boolean comparePotionsByEffects(ItemStack auctionPotion, ItemStack templatePotion) {
        Map<Holder<MobEffect>, EffectData> auctionEffects = AuctionUtils.getPotionEffects(auctionPotion);
        Map<Holder<MobEffect>, EffectData> templateEffects = AuctionUtils.getPotionEffects(templatePotion);
        if (templateEffects.isEmpty()) {
            return false;
        }
        if (auctionEffects.isEmpty()) {
            return false;
        }
        for (Map.Entry<Holder<MobEffect>, EffectData> entry : templateEffects.entrySet()) {
            Holder<MobEffect> requiredEffect = entry.getKey();
            int requiredAmplifier = entry.getValue().amplifier;
            EffectData auctionData = auctionEffects.get(requiredEffect);
            if (auctionData == null) {
                return false;
            }
            if (auctionData.amplifier >= requiredAmplifier) continue;
            return false;
        }
        return true;
    }

    public static class EffectData {
        public final int amplifier;
        public final int duration;

        public EffectData(int amplifier, int duration) {
            this.amplifier = amplifier;
            this.duration = duration;
        }
    }

    public static class PotionEffectRequirement {
        public final Holder<MobEffect> effect;
        public final int minAmplifier;

        public PotionEffectRequirement(Holder<MobEffect> effect, int minAmplifier) {
            this.effect = effect;
            this.minAmplifier = minAmplifier;
        }
    }
}

