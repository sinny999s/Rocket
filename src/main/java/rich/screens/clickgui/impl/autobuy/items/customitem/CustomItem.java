/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  com.mojang.authlib.properties.PropertyMap
 */
package rich.screens.clickgui.impl.autobuy.items.customitem;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class CustomItem
implements AutoBuyableItem {
    private final String displayName;
    private final CompoundTag nbt;
    private final Item material;
    private final int price;
    private final PotionContents potionContents;
    private final List<Component> loreTexts;
    private final AutoBuyItemSettings settings;
    private final boolean hasGlint;
    private boolean enabled;

    public CustomItem(String displayName, CompoundTag nbt, Item material, int price, PotionContents potionContents, List<Component> loreTexts) {
        this(displayName, nbt, material, price, potionContents, loreTexts, CustomItem.shouldHaveGlint(material, displayName), false);
    }

    public CustomItem(String displayName, CompoundTag nbt, Item material, int price, PotionContents potionContents, List<Component> loreTexts, boolean hasGlint) {
        this(displayName, nbt, material, price, potionContents, loreTexts, hasGlint, false);
    }

    public CustomItem(String displayName, CompoundTag nbt, Item material, int price, PotionContents potionContents, List<Component> loreTexts, boolean hasGlint, boolean canHaveQuantity) {
        this.displayName = displayName;
        this.nbt = nbt;
        this.material = material;
        this.price = price;
        this.potionContents = potionContents;
        this.loreTexts = loreTexts;
        this.hasGlint = hasGlint;
        this.settings = new AutoBuyItemSettings(price, material, displayName, canHaveQuantity);
        AutoBuyConfig config = AutoBuyConfig.getInstance();
        if (config.hasItemConfig(displayName)) {
            this.enabled = config.isItemEnabled(displayName);
        } else {
            this.enabled = true;
            config.loadItemSettings(displayName, price);
        }
    }

    public CustomItem(String displayName, CompoundTag nbt, Item material, int price) {
        this(displayName, nbt, material, price, null, null);
    }

    public CustomItem(String displayName, CompoundTag nbt, Item material, int price, boolean canHaveQuantity) {
        this(displayName, nbt, material, price, null, null, CustomItem.shouldHaveGlint(material, displayName), canHaveQuantity);
    }

    public CustomItem(String displayName, CompoundTag nbt, Item material, int price, PotionContents potionContents, List<Component> loreTexts, int minQuantity) {
        this(displayName, nbt, material, price, potionContents, loreTexts, CustomItem.shouldHaveGlint(material, displayName), true);
    }

    private static boolean shouldHaveGlint(Item material, String displayName) {
        if (material == Items.TOTEM_OF_UNDYING || material == Items.ELYTRA) {
            return false;
        }
        if (material == Items.NETHERITE_HELMET || material == Items.NETHERITE_CHESTPLATE || material == Items.NETHERITE_LEGGINGS || material == Items.NETHERITE_BOOTS || material == Items.NETHERITE_SWORD || material == Items.NETHERITE_PICKAXE || material == Items.NETHERITE_AXE || material == Items.NETHERITE_SHOVEL || material == Items.NETHERITE_HOE || material == Items.DIAMOND_HELMET || material == Items.DIAMOND_CHESTPLATE || material == Items.DIAMOND_LEGGINGS || material == Items.DIAMOND_BOOTS || material == Items.DIAMOND_SWORD || material == Items.DIAMOND_PICKAXE || material == Items.DIAMOND_AXE || material == Items.DIAMOND_SHOVEL || material == Items.DIAMOND_HOE || material == Items.IRON_HELMET || material == Items.IRON_CHESTPLATE || material == Items.IRON_LEGGINGS || material == Items.IRON_BOOTS || material == Items.IRON_SWORD || material == Items.IRON_PICKAXE || material == Items.IRON_AXE || material == Items.IRON_SHOVEL || material == Items.IRON_HOE || material == Items.GOLDEN_HELMET || material == Items.GOLDEN_CHESTPLATE || material == Items.GOLDEN_LEGGINGS || material == Items.GOLDEN_BOOTS || material == Items.GOLDEN_SWORD || material == Items.GOLDEN_PICKAXE || material == Items.GOLDEN_AXE || material == Items.GOLDEN_SHOVEL || material == Items.GOLDEN_HOE || material == Items.BOW || material == Items.CROSSBOW || material == Items.TRIDENT || material == Items.MACE || material == Items.SHIELD || material == Items.FISHING_ROD) {
            return true;
        }
        return displayName != null && displayName.contains("[★]") && (material == Items.POTION || material == Items.SPLASH_POTION || material == Items.LINGERING_POTION);
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public ItemStack createItemStack() {
        ItemStack stack = new ItemStack(this.material);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal((String)this.displayName));
        if (this.isPotion(this.material)) {
            if (this.potionContents != null) {
                stack.set(DataComponents.POTION_CONTENTS, this.potionContents);
            } else {
                int color = this.getPotionColorByName(this.displayName);
                stack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.of(color), List.of(), Optional.empty()));
            }
        }
        if (this.loreTexts != null && !this.loreTexts.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(this.loreTexts));
        }
        if (this.hasGlint) {
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        if (this.nbt != null) {
            CompoundTag nbtCopy = this.nbt.copy();
            if (this.material == Items.PLAYER_HEAD && nbtCopy.getCompound("SkullOwner").isPresent()) {
                Optional valueOpt;
                Optional textureNbtOpt;
                ListTag textures;
                CompoundTag properties;
                Optional texturesOpt;
                UUID id;
                CompoundTag skullOwner = (CompoundTag)nbtCopy.getCompound("SkullOwner").get();
                Optional idArray = skullOwner.getIntArray("Id");
                if (idArray.isPresent()) {
                    int[] arr = (int[])idArray.get();
                    id = CustomItem.uuidFromIntArray(arr);
                } else {
                    Optional idString = skullOwner.getString("Id");
                    id = ((Optional<String>)idString).map(UUID::fromString).orElse(UUID.randomUUID());
                }
                ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
                Optional propertiesOpt = skullOwner.getCompound("Properties");
                if (propertiesOpt.isPresent() && (texturesOpt = (properties = (CompoundTag)propertiesOpt.get()).getList("textures")).isPresent() && !(textures = (ListTag)texturesOpt.get()).isEmpty() && (textureNbtOpt = textures.getCompound(0)).isPresent() && (valueOpt = ((CompoundTag)textureNbtOpt.get()).getString("Value")).isPresent()) {
                    builder.put((Object)"textures", (Object)new Property("textures", (String)valueOpt.get()));
                }
                PropertyMap propertyMap = new PropertyMap((Multimap)builder.build());
                GameProfile profile = new GameProfile(id, "", propertyMap);
                stack.set(DataComponents.PROFILE, ResolvableProfile.createResolved((GameProfile)profile));
                nbtCopy.remove("SkullOwner");
            }
            if (!nbtCopy.isEmpty()) {
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of((CompoundTag)nbtCopy));
            }
        }
        return stack;
    }

    private boolean isPotion(Item item) {
        return item == Items.POTION || item == Items.SPLASH_POTION || item == Items.LINGERING_POTION;
    }

    private int getPotionColorByName(String name) {
        return switch (name) {
            case "Belch Potion" -> 16735488;
            case "Sulfuric Acid Potion" -> 49664;
            case "Flash Potion" -> 0xFFFFFF;
            case "Flash Urine Potion" -> 6092799;
            case "Winner Potion" -> 65280;
            case "Agent Potion" -> 0xFFFB00;
            case "Medic Potion" -> 16711902;
            case "Killer Potion" -> 0xFF0000;
            case "[★] Firecracker" -> 16738740;
            case "[★] Holy Water" -> 0xFFFFFF;
            case "[★] Wrath Potion" -> 0x993333;
            case "[★] Paladin Potion" -> 65535;
            case "[★] Assassin Potion" -> 0x333333;
            case "[★] Radiation Potion" -> 3329330;
            case "[★] Sleeping Potion" -> 0x484848;
            case "[\ud83c\udf79] Tangerine Juice" -> 14077507;
            default -> 3694022;
        };
    }

    private static UUID uuidFromIntArray(int[] arr) {
        if (arr.length != 4) {
            return UUID.randomUUID();
        }
        long mostSig = (long)arr[0] << 32 | (long)arr[1] & 0xFFFFFFFFL;
        long leastSig = (long)arr[2] << 32 | (long)arr[3] & 0xFFFFFFFFL;
        return new UUID(mostSig, leastSig);
    }

    @Override
    public int getPrice() {
        return this.price;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public AutoBuyItemSettings getSettings() {
        return this.settings;
    }
}

