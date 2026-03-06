
package rich.screens.clickgui.impl.autobuy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class KrushItems {
    public static ItemStack getHelmet() {
        ItemStack stack = new ItemStack(Items.NETHERITE_HELMET);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.FIRE_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.RESPIRATION, 3));
        enchants.add(new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        enchants.add(new EnchantmentData(Enchantments.BLAST_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.AQUA_AFFINITY, 1));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Crusher Helmet"), List.<Component>of(Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    public static ItemStack getChestplate() {
        ItemStack stack = new ItemStack(Items.NETHERITE_CHESTPLATE);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.BLAST_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.FIRE_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Crusher Chestplate"), List.<Component>of(Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    public static ItemStack getLeggings() {
        ItemStack stack = new ItemStack(Items.NETHERITE_LEGGINGS);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.FIRE_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        enchants.add(new EnchantmentData(Enchantments.BLAST_PROTECTION, 5));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Crusher Leggings"), List.<Component>of(Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    public static ItemStack getBoots() {
        ItemStack stack = new ItemStack(Items.NETHERITE_BOOTS);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.FIRE_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.SOUL_SPEED, 3));
        enchants.add(new EnchantmentData(Enchantments.FEATHER_FALLING, 4));
        enchants.add(new EnchantmentData(Enchantments.DEPTH_STRIDER, 3));
        enchants.add(new EnchantmentData(Enchantments.PROJECTILE_PROTECTION, 5));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        enchants.add(new EnchantmentData(Enchantments.BLAST_PROTECTION, 5));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Crusher Boots"), List.<Component>of(Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    public static ItemStack getSword() {
        ItemStack stack = new ItemStack(Items.NETHERITE_SWORD);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.SHARPNESS, 7));
        enchants.add(new EnchantmentData(Enchantments.BANE_OF_ARTHROPODS, 7));
        enchants.add(new EnchantmentData(Enchantments.FIRE_ASPECT, 2));
        enchants.add(new EnchantmentData(Enchantments.SWEEPING_EDGE, 3));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        enchants.add(new EnchantmentData(Enchantments.LOOTING, 5));
        enchants.add(new EnchantmentData(Enchantments.SMITE, 7));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Sword Crusher"), List.<Component>of(Component.literal((String)"Experienced III").withStyle(ChatFormatting.GRAY), Component.literal((String)"Vampirism II").withStyle(ChatFormatting.GRAY), Component.literal((String)"Oxidation II").withStyle(ChatFormatting.GRAY), Component.literal((String)"Poison III").withStyle(ChatFormatting.GRAY), Component.literal((String)"Detection III").withStyle(ChatFormatting.GRAY), Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    public static ItemStack getPickaxe() {
        ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.EFFICIENCY, 10));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        enchants.add(new EnchantmentData(Enchantments.FORTUNE, 5));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Crusher Pickaxe"), List.<Component>of(Component.literal((String)"Bulldozer II").withStyle(ChatFormatting.GRAY), Component.literal((String)"Experienced III").withStyle(ChatFormatting.GRAY), Component.literal((String)"Magnet").withStyle(ChatFormatting.GRAY), Component.literal((String)"Auto-Smelt").withStyle(ChatFormatting.GRAY), Component.literal((String)"Web").withStyle(ChatFormatting.GRAY), Component.literal((String)"Pinger").withStyle(ChatFormatting.GRAY), Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    public static ItemStack getCrossbow() {
        ItemStack stack = new ItemStack(Items.CROSSBOW);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.MULTISHOT, 1));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        enchants.add(new EnchantmentData(Enchantments.PIERCING, 5));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 3));
        enchants.add(new EnchantmentData(Enchantments.QUICK_CHARGE, 3));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Crusher Crossbow"), List.<Component>of(Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    public static ItemStack getBow() {
        ItemStack stack = new ItemStack(Items.BOW);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.POWER, 7));
        enchants.add(new EnchantmentData(Enchantments.PUNCH, 3));
        enchants.add(new EnchantmentData(Enchantments.FLAME, 1));
        enchants.add(new EnchantmentData(Enchantments.INFINITY, 1));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Crusher Bow"), List.<Component>of(Component.literal((String)"Sniper II").withStyle(ChatFormatting.GRAY), Component.literal((String)"Demolisher").withStyle(ChatFormatting.GRAY), Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    public static ItemStack getTrident() {
        ItemStack stack = new ItemStack(Items.TRIDENT);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.CHANNELING, 1));
        enchants.add(new EnchantmentData(Enchantments.SHARPNESS, 7));
        enchants.add(new EnchantmentData(Enchantments.FIRE_ASPECT, 2));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        enchants.add(new EnchantmentData(Enchantments.LOYALTY, 3));
        enchants.add(new EnchantmentData(Enchantments.IMPALING, 5));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Trident Crusher"), List.of((Component[])new Component[]{Component.literal((String)"Scout III").withStyle(ChatFormatting.GRAY), Component.literal((String)"Experienced III").withStyle(ChatFormatting.GRAY), Component.literal((String)"Vampirism II").withStyle(ChatFormatting.GRAY), Component.literal((String)"Stupor III").withStyle(ChatFormatting.GRAY), Component.literal((String)"Attraction II").withStyle(ChatFormatting.GRAY), Component.literal((String)"Oxidation II").withStyle(ChatFormatting.GRAY), Component.literal((String)"Return").withStyle(ChatFormatting.GRAY), Component.literal((String)"Demolisher").withStyle(ChatFormatting.GRAY), Component.literal((String)"Poison III").withStyle(ChatFormatting.GRAY), Component.literal((String)"Detection III").withStyle(ChatFormatting.GRAY), Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)}));
        return stack;
    }

    public static ItemStack getMace() {
        ItemStack stack = new ItemStack(Items.MACE);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.SHARPNESS, 7));
        enchants.add(new EnchantmentData(Enchantments.SMITE, 7));
        enchants.add(new EnchantmentData(Enchantments.BANE_OF_ARTHROPODS, 7));
        enchants.add(new EnchantmentData(Enchantments.DENSITY, 5));
        enchants.add(new EnchantmentData(Enchantments.BREACH, 3));
        enchants.add(new EnchantmentData(Enchantments.SWEEPING_EDGE, 3));
        enchants.add(new EnchantmentData(Enchantments.FIRE_ASPECT, 2));
        enchants.add(new EnchantmentData(Enchantments.LOOTING, 5));
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Mace Crusher"), List.<Component>of(Component.literal((String)"Experienced III").withStyle(ChatFormatting.GRAY), Component.literal((String)"Vampirism II").withStyle(ChatFormatting.GRAY), Component.literal((String)"Oxidation II").withStyle(ChatFormatting.GRAY), Component.literal((String)"Poison III").withStyle(ChatFormatting.GRAY), Component.literal((String)"Detection III").withStyle(ChatFormatting.GRAY), Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    public static ItemStack getElytra() {
        ItemStack stack = new ItemStack(Items.ELYTRA);
        ArrayList<EnchantmentData> enchants = new ArrayList<EnchantmentData>();
        enchants.add(new EnchantmentData(Enchantments.UNBREAKING, 5));
        enchants.add(new EnchantmentData(Enchantments.MENDING, 1));
        KrushItems.addEnchantments(stack, enchants);
        KrushItems.setupItem(stack, KrushItems.createStyledName("Crusher Elytra"), List.<Component>of(Component.literal((String)"[★] Original Item").withStyle(ChatFormatting.GRAY)));
        return stack;
    }

    private static void addEnchantments(ItemStack stack, List<EnchantmentData> enchantments) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            return;
        }
        try {
            RegistryAccess registryLookup = client.level.registryAccess();
            HolderLookup.RegistryLookup enchantmentRegistry = registryLookup.lookupOrThrow(Registries.ENCHANTMENT);
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable((ItemEnchantments)stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
            for (EnchantmentData data : enchantments) {
                try {
                    Optional enchantmentOpt = enchantmentRegistry.get(data.key);
                    if (!enchantmentOpt.isPresent()) continue;
                    builder.upgrade((Holder)enchantmentOpt.get(), data.level);
                }
                catch (Exception exception) {}
            }
            stack.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setupItem(ItemStack stack, Component name, List<Component> lore) {
        stack.set(DataComponents.CUSTOM_NAME, name);
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("HideFlags", 127);
        nbt.putBoolean("Unbreakable", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of((CompoundTag)nbt));
        if (!lore.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(lore));
        }
    }

    private static Component createStyledName(String baseName) {
        return Component.literal((String)baseName).withStyle(new ChatFormatting[]{ChatFormatting.BOLD, ChatFormatting.DARK_RED});
    }

    private static class EnchantmentData {
        final ResourceKey<Enchantment> key;
        final int level;

        EnchantmentData(ResourceKey<Enchantment> key, int level) {
            this.key = key;
            this.level = level;
        }
    }
}

