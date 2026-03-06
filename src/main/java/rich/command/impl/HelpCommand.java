
package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;

public class HelpCommand
extends Command {
    public HelpCommand() {
        super("help", "Shows all available commands", new String[0]);
    }

    @Override
    public void execute(String label, String[] args) {
        CommandManager manager = CommandManager.getInstance();
        if (args.length == 0 || this.isInteger(args[0])) {
            int page = 1;
            if (args.length > 0 && this.isInteger(args[0])) {
                page = Integer.parseInt(args[0]);
            }
            List commands = manager.getCommands().stream().filter(cmd -> !cmd.hiddenFromHelp()).collect(Collectors.toList());
            Paginator<Command> paginator = new Paginator<Command>(commands);
            paginator.setPage(page);
            paginator.display(() -> {
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lAVAILABLE COMMANDS");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            }, command -> {
                String name = command.getName();
                String fullName = manager.getPrefix() + name;
                MutableComponent shortDescComponent = Component.literal((String)(" \u00a78- \u00a77" + command.getShortDesc()));
                MutableComponent hoverComponent = Component.literal((String)"");
                hoverComponent.setStyle(hoverComponent.getStyle().withColor(ChatFormatting.GRAY));
                hoverComponent.append(Component.literal((String)fullName).withStyle(ChatFormatting.WHITE));
                hoverComponent.append("\n\u00a77" + command.getShortDesc());
                hoverComponent.append("\n\n\u00a78Click to view full command help");
                String clickCommand = manager.getPrefix() + String.format("%s %s", label, name);
                MutableComponent component = Component.literal((String)("\u00a7f" + fullName));
                component.append(shortDescComponent);
                component.setStyle(component.getStyle().withHoverEvent(new HoverEvent.ShowText(hoverComponent)).withClickEvent(new ClickEvent.RunCommand(clickCommand)));
                return component;
            }, manager.getPrefix() + label);
        } else {
            String commandName = args[0].toLowerCase();
            Command command2 = manager.getCommand(commandName);
            if (command2 == null) {
                this.logDirect("Command '" + commandName + "' not found!", ChatFormatting.RED);
                return;
            }
            this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            this.logDirect("\u00a7f\u00a7l" + command2.getName().toUpperCase());
            this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            List<String> desc = command2.getLongDesc();
            boolean firstLine = true;
            for (String line : desc) {
                if (line.isEmpty()) continue;
                this.logDirect("\u00a77" + line);
                if (!firstLine) continue;
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                firstLine = false;
            }
            this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        if (args.length == 1) {
            return new TabCompleteHelper().filterPrefix(args[0]).addCommands(CommandManager.getInstance()).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "View all available commands";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("View detailed help information on how to use specific commands", "Usage:", "> help - Lists all commands and their brief descriptions.", "> help <command> - Display help information for a specific command.");
    }

    private boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public static String getLine() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) {
            return "\u00a78\u00a7m                    ";
        }
        int chatWidth = ((Double)mc.options.chatWidth().get()).intValue();
        int scaledWidth = chatWidth * 280 + 40;
        int dashWidth = mc.font.width("-");
        if (dashWidth <= 0) {
            dashWidth = 4;
        }
        int dashCount = scaledWidth / dashWidth - 2;
        dashCount = Math.max(10, Math.min(dashCount, 80));
        StringBuilder sb = new StringBuilder("\u00a78\u00a7m");
        for (int i = 0; i < dashCount; ++i) {
            sb.append("-");
        }
        return sb.toString();
    }
}

