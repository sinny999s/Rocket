
package rich.screens.clickgui.impl.autobuy.items.customitem;

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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.settings.AutoBuyItemSettings;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class UnbreakableItem
implements AutoBuyableItem {
    private final String displayName;
    private final Item material;
    private final int price;
    private final List<Component> loreTexts;
    private final AutoBuyItemSettings settings;
    private boolean enabled;

    public UnbreakableItem(String displayName, Item material, int price, List<Component> loreTexts) {
        this.displayName = displayName;
        this.material = material;
        this.price = price;
        this.loreTexts = loreTexts;
        this.settings = new AutoBuyItemSettings(price, material, displayName);
        AutoBuyConfig config = AutoBuyConfig.getInstance();
        if (config.hasItemConfig(displayName)) {
            this.enabled = config.isItemEnabled(displayName);
        } else {
            this.enabled = true;
            config.loadItemSettings(displayName, price);
        }
    }

    @Override
    public String getDisplayName() {
        return this.displayName;
    }

    @Override
    public ItemStack createItemStack() {
        ItemStack stack = new ItemStack(this.material);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal((String)this.displayName).withStyle(ChatFormatting.LIGHT_PURPLE));
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("HideFlags", 127);
        nbt.putBoolean("Unbreakable", true);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of((CompoundTag)nbt));
        Minecraft client = Minecraft.getInstance();
        if (client.level != null) {
            try {
                RegistryAccess registryLookup = client.level.registryAccess();
                HolderLookup.RegistryLookup enchantmentRegistry = registryLookup.lookupOrThrow(Registries.ENCHANTMENT);
                ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable((ItemEnchantments)stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
                Optional vanishingOpt = enchantmentRegistry.get(Enchantments.VANISHING_CURSE);
                if (vanishingOpt.isPresent()) {
                    builder.upgrade((Holder)vanishingOpt.get(), 1);
                }
                stack.set(DataComponents.ENCHANTMENTS, builder.toImmutable());
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.loreTexts != null && !this.loreTexts.isEmpty()) {
            stack.set(DataComponents.LORE, new ItemLore(this.loreTexts));
        }
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
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

