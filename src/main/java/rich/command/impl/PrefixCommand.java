
package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.impl.HelpCommand;
import rich.util.config.impl.prefix.PrefixConfig;

public class PrefixCommand
extends Command {
    public PrefixCommand() {
        super("prefix", "Change command prefix", new String[0]);
    }

    @Override
    public void execute(String label, String[] args) {
        CommandManager manager = CommandManager.getInstance();
        if (args.length == 0) {
            this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            this.logDirect("\u00a7f\u00a7lCOMMAND PREFIX");
            this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            this.logDirect("\u00a77Current prefix: \u00a7f" + manager.getPrefix());
            this.logDirect("\u00a77> prefix set <symbol> \u00a78- \u00a7fSet new prefix");
            this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            return;
        }
        String action = args[0].toLowerCase();
        if (action.equals("set")) {
            if (args.length < 2) {
                this.logDirect("Usage: prefix set <symbol>", ChatFormatting.RED);
                return;
            }
            String newPrefix = args[1];
            if (newPrefix.length() > 3) {
                this.logDirect("Prefix cannot be longer than 3 characters!", ChatFormatting.RED);
                return;
            }
            if (newPrefix.contains(" ")) {
                this.logDirect("Prefix cannot contain spaces!", ChatFormatting.RED);
                return;
            }
            PrefixConfig.getInstance().setPrefixAndSave(newPrefix);
            this.logDirect(String.format("\u00a7aPrefix changed to: \u00a7f%s", newPrefix), ChatFormatting.GREEN);
            this.logDirect(String.format("\u00a77Commands are now entered as: \u00a7f%shelp", newPrefix), ChatFormatting.GREEN);
        } else {
            this.logDirect("Usage: prefix set <symbol>", ChatFormatting.RED);
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        if (args.length == 1) {
            return Stream.of("set").filter(s -> s.startsWith(args[0].toLowerCase()));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return Stream.of(".", "!", "$", "#", "-", "/").filter(s -> s.startsWith(args[1]));
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Change command prefix";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Command to change client command prefix", "Usage:", "> prefix - Show current prefix", "> prefix set <symbol> - Set new prefix");
    }
}

