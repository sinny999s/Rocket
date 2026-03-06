
package rich.util.string.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import rich.util.string.chat.helper.TextHelper;

public class ChatMessage {
    public static MutableComponent brandmessage() {
        return (MutableComponent)TextHelper.applyPredefinedGradient("Rocket", "black_light_purple", true);
    }

    public static MutableComponent blockesp() {
        return (MutableComponent)TextHelper.applyPredefinedGradient("Block Esp", "black_light_purple", true);
    }

    public static MutableComponent autobuy() {
        return (MutableComponent)TextHelper.applyPredefinedGradient("Auto Buy", "black_light_purple", true);
    }

    public static MutableComponent autobuiparcer() {
        return (MutableComponent)TextHelper.applyPredefinedGradient("Parce price", "black_light_purple", true);
    }

    public static void brandmessage(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("Rocket -> ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void autobuymessage(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void autobuymessageSuccess(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void autobuymessageError(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void autobuymessageWarning(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("AutoBuy -> ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message).setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void ancientmessage(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("Ancient Xray -> ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void helpmessage(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("Help -> ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void swapmessage(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("AutoSwap -> ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void ircmessage(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void ircmessageWithGreen(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message).setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static void ircmessageWithRed(String message) {
        if (Minecraft.getInstance().player != null) {
            Component prefix = TextHelper.applyPredefinedGradient("[IRC] ", "black_light_purple", true);
            MutableComponent formattedMessage = prefix.copy().append(Component.literal((String)message).setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public static Component ircprefixDeveloper(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Developer ", "dark_red_bright_red", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixCurator(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Curator ", "dark_red", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixYouTube(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("YouTube ", "red_white", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixPikmi(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Pikmi ", "purple_bright_pink", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixLabuba(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Labuba ", "pink_dark_pink", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixZapen(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Zapen ", "bright_red", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixBoost(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Boost ", "dark_green_bright_green", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixRich(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Rocket ", "red_orange", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixPanda(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Panda ", "white_black", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixSmiley(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("(\u25cf'\u25e1'\u25cf) ", "turquoise_blue", true);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixBibi(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Bibi...! ", "cyan_orange_fade", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixBenena(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Benena ", "yellow_cyan", false);
        return prefix.copy().append(Component.literal((String)message));
    }

    public static Component ircprefixBlyabuba(String message) {
        Component prefix = TextHelper.applyPredefinedGradient("Blyabuba ", "purple_red_fade", false);
        return prefix.copy().append(Component.literal((String)message));
    }
}

