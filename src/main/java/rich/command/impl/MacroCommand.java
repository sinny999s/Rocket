
package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;
import rich.command.impl.HelpCommand;
import rich.util.repository.macro.Macro;
import rich.util.repository.macro.MacroRepository;
import rich.util.string.KeyHelper;

public class MacroCommand
extends Command {
    public MacroCommand() {
        super("macro", "Macro management", "macros");
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        MacroRepository macroRepository = MacroRepository.getInstance();
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                if (args.length < 4) {
                    this.logDirect("Usage: macro add <key> <name> <message>", ChatFormatting.RED);
                    return;
                }
                String keyName = args[1];
                int key = KeyHelper.getKeyCode(keyName);
                if (key == -1) {
                    this.logDirect(String.format("Unknown key: %s", keyName), ChatFormatting.RED);
                    return;
                }
                String name = args[2];
                StringBuilder messageBuilder = new StringBuilder();
                for (int i = 3; i < args.length; ++i) {
                    if (i > 3) {
                        messageBuilder.append(" ");
                    }
                    messageBuilder.append(args[i]);
                }
                String message = messageBuilder.toString();
                if (macroRepository.hasMacro(name)) {
                    this.logDirect(String.format("Macro named %s already exists!", name), ChatFormatting.RED);
                    return;
                }
                macroRepository.addMacroAndSave(name, message, key);
                this.logDirect(String.format("\u00a7aAdded macro \u00a7f%s \u00a7ato key \u00a7f%s \u00a7awith command \u00a7f%s", name, KeyHelper.getKeyName(key).toLowerCase(), message), ChatFormatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("Usage: macro remove <name>", ChatFormatting.RED);
                    return;
                }
                String name = args[1];
                if (!macroRepository.hasMacro(name)) {
                    this.logDirect(String.format("Macro %s not found!", name), ChatFormatting.RED);
                    return;
                }
                macroRepository.deleteMacroAndSave(name);
                this.logDirect(String.format("Macro %s removed!", name), ChatFormatting.GREEN);
                break;
            }
            case "clear": {
                int count = macroRepository.size();
                macroRepository.clearListAndSave();
                this.logDirect(String.format("All macros removed! Removed: %d", count), ChatFormatting.GREEN);
                break;
            }
            case "list": {
                List<Macro> macros;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException key) {
                        // empty catch block
                    }
                }
                if ((macros = macroRepository.getMacroList()).isEmpty()) {
                    this.logDirect("Macro list is empty!", ChatFormatting.RED);
                    return;
                }
                Paginator<Macro> paginator = new Paginator<Macro>(macros);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7lMACRO LIST \u00a77(" + macros.size() + ")");
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                }, macro -> {
                    String macroName = macro.name();
                    String keyName = KeyHelper.getKeyName(macro.key()).toLowerCase();
                    String message = macro.message();
                    MutableComponent component = Component.literal((String)("  \u00a7e\u25cf \u00a7f" + macroName)).append(Component.literal((String)(" \u00a78[\u00a77" + keyName + "\u00a78]"))).append(Component.literal((String)(" \u00a78-> \u00a77" + message)));
                    MutableComponent hoverText = Component.literal((String)("\u00a77Click to remove macro \u00a7f" + macroName));
                    String removeCommand = manager.getPrefix() + "macro remove " + macroName;
                    component.setStyle(component.getStyle().withHoverEvent(new HoverEvent.ShowText(hoverText)).withClickEvent(new ClickEvent.RunCommand(removeCommand)));
                    return component;
                }, manager.getPrefix() + label + " list");
                break;
            }
            default: {
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lMACRO MANAGEMENT");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> macro add <key> <name> <message> \u00a78- \u00a7fAdd macro");
                this.logDirect("\u00a77> macro remove <name> \u00a78- \u00a7fDelete macro");
                this.logDirect("\u00a77> macro list \u00a78- \u00a7fShow macro list");
                this.logDirect("\u00a77> macro clear \u00a78- \u00a7fDelete all macros");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        if (args.length == 1) {
            return new TabCompleteHelper().append("add", "remove", "list", "clear").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        if (args.length == 2) {
            String action = args[0].toLowerCase();
            if (action.equals("add")) {
                return new TabCompleteHelper().append(KeyHelper.getAllKeyNames()).filterPrefix(args[1]).stream();
            }
            if (action.equals("remove") || action.equals("del") || action.equals("delete")) {
                return new TabCompleteHelper().append(MacroRepository.getInstance().getMacroNames().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Macro management";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Command to manage macros", "Usage:", "> macro add <key> <name> <message> - Add macro", "> macro remove <name> - Delete macro", "> macro list - Show macro list", "> macro clear - Delete all macros");
    }
}

