/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.properties.Property
 *  lombok.Generated
 */
package rich.modules.impl.misc.autoparser.dev;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Generated;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.ResolvableProfile;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.util.string.chat.ChatMessage;

public class ItemParser
extends ModuleStructure {
    private static ItemParser instance;
    private final BooleanSetting showInChat = new BooleanSetting("Show in chat", "").setValue(true);
    private final BooleanSetting saveToFile = new BooleanSetting("Save to file", "").setValue(true);
    private int parseCounter = 0;
    private static final Set<String> IGNORED_ITEMS;
    private static final Map<String, String> EFFECT_TO_STATUSEFFECTS;

    public ItemParser() {
        super("Item Parser", "Parsing item information", ModuleCategory.MISC);
        instance = this;
        this.settings(this.showInChat, this.saveToFile);
    }

    public static ItemParser getInstance() {
        return instance;
    }

    public void parseAllSlots(List<Slot> slots, int containerSize, String containerTitle) {
        ++this.parseCounter;
        StringBuilder info = new StringBuilder();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        info.append("// PARSING #").append(this.parseCounter).append(" | ").append(timestamp).append("\n");
        info.append("// Container: ").append(containerTitle).append("\n\n");
        int itemCount = 0;
        for (int i = 0; i < containerSize && i < slots.size(); ++i) {
            String itemId;
            Slot slot = slots.get(i);
            ItemStack stack = slot.getItem();
            if (stack.isEmpty() || IGNORED_ITEMS.contains(itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())) continue;
            ++itemCount;
            info.append("// --- SLOT ").append(i).append(" ---\n");
            this.parseItemCompact(stack, info);
            info.append("\n");
        }
        info.append("// TOTAL: ").append(itemCount).append(" items\n");
        String result = info.toString();
        if (this.showInChat.isValue()) {
            ChatMessage.autobuymessage("\u00a76Parsing #" + this.parseCounter + " | \u00a7bItems: \u00a7f" + itemCount);
        }
        if (this.saveToFile.isValue()) {
            this.saveToFile(result, this.parseCounter);
            ChatMessage.autobuymessageSuccess("File: parse_" + this.parseCounter + ".txt");
        }
    }

    private void parseItemCompact(ItemStack stack, StringBuilder info) {
        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        Component customName = (Component)stack.get(DataComponents.CUSTOM_NAME);
        String displayName = customName != null ? customName.getString() : stack.getHoverName().getString();
        info.append("// ").append(displayName).append(" (").append(itemId).append(")\n");
        ItemLore lore = (ItemLore)stack.get(DataComponents.LORE);
        if (lore != null && !lore.lines().isEmpty()) {
            info.append("List<Text> lore = List.of(\n");
            for (int i = 0; i < lore.lines().size(); ++i) {
                String line = ((Component)lore.lines().get(i)).getString();
                if (line.trim().isEmpty()) continue;
                info.append("    Text.literal(\"").append(this.escapeString(line)).append("\")");
                if (i < lore.lines().size() - 1) {
                    info.append(",");
                }
                info.append("\n");
            }
            info.append(");\n");
        }
        if (stack.getItem() == Items.PLAYER_HEAD) {
            this.generateHeadCode(stack, displayName, info);
        } else if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
            this.generateTalismanCode(displayName, info);
        } else if (stack.getItem() == Items.POTION || stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION) {
            this.generatePotionCode(stack, displayName, info);
        } else {
            this.generateGenericCode(stack, displayName, info);
        }
    }

    private void generateHeadCode(ItemStack stack, String displayName, StringBuilder info) {
        Iterator iterator;
        ResolvableProfile profile = (ResolvableProfile)stack.get(DataComponents.PROFILE);
        if (profile == null) {
            info.append("// NO PROFILE\n");
            return;
        }
        GameProfile gameProfile = profile.partialProfile();
        String uuid = gameProfile.id() != null ? gameProfile.id().toString() : "unknown";
        String texture = "";
        Collection<Property> textures = gameProfile.properties().get("textures");
        if (textures != null && !textures.isEmpty() && (iterator = textures.iterator()).hasNext()) {
            Property property = (Property)iterator.next();
            texture = property.value();
        }
        String cleanName = displayName.replace("[★] ", "");
        info.append("spheres.add(createSphere(\"").append(displayName).append("\", \"").append(uuid).append("\", \"").append(texture).append("\", ").append("Defaultpricec.getPrice(\"").append(cleanName).append("\"), lore));\n");
    }

    private void generateTalismanCode(String displayName, StringBuilder info) {
        String cleanName = displayName.replace("[★] ", "");
        info.append("talismans.add(new CustomItem(\"").append(displayName).append("\", null, Items.TOTEM_OF_UNDYING, Defaultpricec.getPrice(\"").append(cleanName).append("\"), null, lore));\n");
    }

    private void generatePotionCode(ItemStack stack, String displayName, StringBuilder info) {
        String itemType = stack.getItem() == Items.SPLASH_POTION ? "SPLASH_POTION" : (stack.getItem() == Items.LINGERING_POTION ? "LINGERING_POTION" : "POTION");
        PotionContents potionContents = (PotionContents)stack.get(DataComponents.POTION_CONTENTS);
        if (potionContents != null) {
            ArrayList<MobEffectInstance> effects = new ArrayList<MobEffectInstance>();
            for (MobEffectInstance effect : potionContents.getAllEffects()) {
                effects.add(effect);
            }
            int color = potionContents.getColor();
            String colorHex = String.format("0x%06X", color & 0xFFFFFF);
            if (!effects.isEmpty()) {
                info.append("List<StatusEffectInstance> ").append(this.toVariableName(displayName)).append("Effects = List.of(\n");
                for (int i = 0; i < effects.size(); ++i) {
                    MobEffectInstance effect = (MobEffectInstance)effects.get(i);
                    String effectId = effect.getEffect().getRegisteredName();
                    if (effectId != null) {
                        effectId = effectId.replace("minecraft:", "");
                    }
                    String statusEffect = EFFECT_TO_STATUSEFFECTS.getOrDefault(effectId, "StatusEffects." + effectId.toUpperCase());
                    int duration = effect.getDuration();
                    int amplifier = effect.getAmplifier();
                    info.append("        new StatusEffectInstance(").append(statusEffect).append(", ").append(duration).append(", ").append(amplifier).append(")");
                    if (i < effects.size() - 1) {
                        info.append(",");
                    }
                    info.append(" // ").append(effectId).append(" lvl:").append(amplifier + 1).append(" dur:").append(this.formatDuration(duration)).append("\n");
                }
                info.append(");\n");
                String cleanName = displayName.replace("[★] ", "").replace("[\ud83c\udf79] ", "").replace("[$] ", "");
                info.append("potions.add(new CustomItem(\"").append(displayName).append("\", null, Items.").append(itemType).append(", Defaultpricec.getPrice(\"").append(cleanName).append("\"),\n");
                info.append("        new PotionContentsComponent(Optional.empty(), Optional.of(").append(colorHex).append("), ").append(this.toVariableName(displayName)).append("Effects, Optional.empty()), lore));\n");
            } else {
                info.append("// Potion without effects, color: ").append(colorHex).append("\n");
                String cleanName = displayName.replace("[★] ", "").replace("[\ud83c\udf79] ", "").replace("[$] ", "");
                info.append("potions.add(new CustomItem(\"").append(displayName).append("\", null, Items.").append(itemType).append(", Defaultpricec.getPrice(\"").append(cleanName).append("\"), null, lore));\n");
            }
        } else {
            info.append("// No PotionContentsComponent\n");
            String cleanName = displayName.replace("[★] ", "").replace("[\ud83c\udf79] ", "").replace("[$] ", "");
            info.append("potions.add(new CustomItem(\"").append(displayName).append("\", null, Items.").append(itemType).append(", Defaultpricec.getPrice(\"").append(cleanName).append("\"), null, lore));\n");
        }
    }

    private void generateGenericCode(ItemStack stack, String displayName, StringBuilder info) {
        String itemConst = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().toUpperCase();
        info.append("items.add(new CustomItem(\"").append(displayName).append("\", null, Items.").append(itemConst).append(", price, null, lore));\n");
    }

    private String toVariableName(String displayName) {
        String clean = displayName.replace("[★] ", "").replace("[\ud83c\udf79] ", "").replace("[$] ", "").replace(" ", "").replace("-", "").replace(".", "");
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < clean.length(); ++i) {
            char c = clean.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                if (i == 0) {
                    sb.append(Character.toLowerCase(c));
                    continue;
                }
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                    continue;
                }
                sb.append(c);
                continue;
            }
            nextUpper = true;
        }
        if (sb.length() == 0) {
            return "potion";
        }
        return sb.toString();
    }

    private String formatDuration(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        return String.format("%d:%02d", minutes, seconds %= 60);
    }

    private String escapeString(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void saveToFile(String content, int number) {
        try {
            Minecraft mc = Minecraft.getInstance();
            File dir = new File(mc.gameDirectory, "item_parser");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, "parse_" + number + ".txt");
            try (PrintWriter writer = new PrintWriter(new FileWriter(file));){
                writer.print(content);
            }
        }
        catch (IOException e) {
            ChatMessage.autobuymessageError("Error: " + e.getMessage());
        }
    }

    @Generated
    public BooleanSetting getShowInChat() {
        return this.showInChat;
    }

    @Generated
    public BooleanSetting getSaveToFile() {
        return this.saveToFile;
    }

    @Generated
    public int getParseCounter() {
        return this.parseCounter;
    }

    static {
        IGNORED_ITEMS = Set.of((String[])new String[]{"minecraft:glass_pane", "minecraft:white_stained_glass_pane", "minecraft:orange_stained_glass_pane", "minecraft:magenta_stained_glass_pane", "minecraft:light_blue_stained_glass_pane", "minecraft:yellow_stained_glass_pane", "minecraft:lime_stained_glass_pane", "minecraft:pink_stained_glass_pane", "minecraft:gray_stained_glass_pane", "minecraft:light_gray_stained_glass_pane", "minecraft:cyan_stained_glass_pane", "minecraft:purple_stained_glass_pane", "minecraft:blue_stained_glass_pane", "minecraft:brown_stained_glass_pane", "minecraft:green_stained_glass_pane", "minecraft:red_stained_glass_pane", "minecraft:black_stained_glass_pane", "minecraft:glass", "minecraft:white_stained_glass", "minecraft:orange_stained_glass", "minecraft:magenta_stained_glass", "minecraft:light_blue_stained_glass", "minecraft:yellow_stained_glass", "minecraft:lime_stained_glass", "minecraft:pink_stained_glass", "minecraft:gray_stained_glass", "minecraft:light_gray_stained_glass", "minecraft:cyan_stained_glass", "minecraft:purple_stained_glass", "minecraft:blue_stained_glass", "minecraft:brown_stained_glass", "minecraft:green_stained_glass", "minecraft:red_stained_glass", "minecraft:black_stained_glass", "minecraft:air", "minecraft:barrier"});
        EFFECT_TO_STATUSEFFECTS = Map.ofEntries((Map.Entry[])new Map.Entry[]{Map.entry((Object)"speed", (Object)"StatusEffects.SPEED"), Map.entry((Object)"slowness", (Object)"StatusEffects.SLOWNESS"), Map.entry((Object)"haste", (Object)"StatusEffects.HASTE"), Map.entry((Object)"mining_fatigue", (Object)"StatusEffects.MINING_FATIGUE"), Map.entry((Object)"strength", (Object)"StatusEffects.STRENGTH"), Map.entry((Object)"instant_health", (Object)"StatusEffects.INSTANT_HEALTH"), Map.entry((Object)"instant_damage", (Object)"StatusEffects.INSTANT_DAMAGE"), Map.entry((Object)"jump_boost", (Object)"StatusEffects.JUMP_BOOST"), Map.entry((Object)"nausea", (Object)"StatusEffects.NAUSEA"), Map.entry((Object)"regeneration", (Object)"StatusEffects.REGENERATION"), Map.entry((Object)"resistance", (Object)"StatusEffects.RESISTANCE"), Map.entry((Object)"fire_resistance", (Object)"StatusEffects.FIRE_RESISTANCE"), Map.entry((Object)"water_breathing", (Object)"StatusEffects.WATER_BREATHING"), Map.entry((Object)"invisibility", (Object)"StatusEffects.INVISIBILITY"), Map.entry((Object)"blindness", (Object)"StatusEffects.BLINDNESS"), Map.entry((Object)"night_vision", (Object)"StatusEffects.NIGHT_VISION"), Map.entry((Object)"hunger", (Object)"StatusEffects.HUNGER"), Map.entry((Object)"weakness", (Object)"StatusEffects.WEAKNESS"), Map.entry((Object)"poison", (Object)"StatusEffects.POISON"), Map.entry((Object)"wither", (Object)"StatusEffects.WITHER"), Map.entry((Object)"health_boost", (Object)"StatusEffects.HEALTH_BOOST"), Map.entry((Object)"absorption", (Object)"StatusEffects.ABSORPTION"), Map.entry((Object)"saturation", (Object)"StatusEffects.SATURATION"), Map.entry((Object)"glowing", (Object)"StatusEffects.GLOWING"), Map.entry((Object)"levitation", (Object)"StatusEffects.LEVITATION"), Map.entry((Object)"luck", (Object)"StatusEffects.LUCK"), Map.entry((Object)"unluck", (Object)"StatusEffects.UNLUCK"), Map.entry((Object)"slow_falling", (Object)"StatusEffects.SLOW_FALLING"), Map.entry((Object)"conduit_power", (Object)"StatusEffects.CONDUIT_POWER"), Map.entry((Object)"dolphins_grace", (Object)"StatusEffects.DOLPHINS_GRACE"), Map.entry((Object)"bad_omen", (Object)"StatusEffects.BAD_OMEN"), Map.entry((Object)"hero_of_the_village", (Object)"StatusEffects.HERO_OF_THE_VILLAGE"), Map.entry((Object)"darkness", (Object)"StatusEffects.DARKNESS")});
    }
}

