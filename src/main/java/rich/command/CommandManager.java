
package rich.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import rich.command.Command;
import rich.command.impl.AutoBuyCommand;
import rich.command.impl.BindCommand;
import rich.command.impl.BlockESPCommand;
import rich.command.impl.ConfigCommand;
import rich.command.impl.FriendCommand;
import rich.command.impl.HelpCommand;
import rich.command.impl.MacroCommand;
import rich.command.impl.PrefixCommand;
import rich.command.impl.StaffCommand;
import rich.command.impl.WayCommand;
import rich.events.api.EventHandler;
import rich.events.api.EventManager;
import rich.events.impl.ChatEvent;
import rich.events.impl.TabCompleteEvent;
import rich.util.config.impl.prefix.PrefixConfig;
import rich.util.string.chat.ChatMessage;

public class CommandManager {
    private static CommandManager instance;
    private final List<Command> commands;
    private String prefix;

    public CommandManager() {
        instance = this;
        this.commands = new CopyOnWriteArrayList<Command>();
        this.prefix = PrefixConfig.getInstance().getPrefix();
    }

    public static CommandManager getInstance() {
        return instance;
    }

    public void init() {
        this.registerCommand(new HelpCommand());
        this.registerCommand(new ConfigCommand());
        this.registerCommand(new AutoBuyCommand());
        this.registerCommand(new FriendCommand());
        this.registerCommand(new MacroCommand());
        this.registerCommand(new BindCommand());
        this.registerCommand(new PrefixCommand());
        this.registerCommand(new WayCommand());
        this.registerCommand(new StaffCommand());
        this.registerCommand(new BlockESPCommand());
        EventManager.register(this);
    }

    public void registerCommand(Command command) {
        this.commands.add(command);
    }

    public void unregisterCommand(Command command) {
        this.commands.remove(command);
    }

    public Command getCommand(String name) {
        return this.commands.stream().filter(cmd -> cmd.matches(name)).findFirst().orElse(null);
    }

    public List<Command> getCommands() {
        return new ArrayList<Command>(this.commands);
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        String msg = event.getMessage();
        if (msg.startsWith(this.prefix)) {
            event.cancel();
            String commandStr = msg.substring(this.prefix.length());
            if (commandStr.trim().isEmpty()) {
                this.execute("help");
                return;
            }
            if (!this.execute(commandStr)) {
                this.sendError("Unknown command. Use " + this.prefix + "help for a list of commands.");
            }
        }
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        String eventPrefix = event.prefix;
        if (!eventPrefix.startsWith(this.prefix)) {
            return;
        }
        String msg = eventPrefix.substring(this.prefix.length());
        Stream<String> stream = this.tabComplete(msg);
        String[] parts = msg.split(" ", -1);
        if (parts.length <= 1) {
            stream = stream.map(x -> this.prefix + x);
        }
        event.completions = (String[])stream.toArray(String[]::new);
    }

    public boolean execute(String input) {
        if (input == null || input.trim().isEmpty()) {
            return this.execute("help");
        }
        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        String[] args = parts.length > 1 ? parts[1].split("\\s+") : new String[]{};
        Command command = this.getCommand(commandName);
        if (command != null) {
            try {
                command.execute(commandName, args);
                return true;
            }
            catch (Exception e) {
                this.sendError("Error executing command: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return false;
    }

    public Stream<String> tabComplete(String input) {
        String[] args;
        if (input == null) {
            input = "";
        }
        if ((args = input.split("\\s+", -1)).length <= 1) {
            String partial = args.length == 0 ? "" : args[0].toLowerCase();
            return this.getCommandSuggestions(partial);
        }
        String commandName = args[0];
        Command command = this.getCommand(commandName);
        if (command != null) {
            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return command.tabComplete(commandName, subArgs);
        }
        return Stream.empty();
    }

    private Stream<String> getCommandSuggestions(String partial) {
        if (partial.isEmpty()) {
            return this.commands.stream().map(Command::getName).sorted();
        }
        LinkedHashSet<String> suggestions = new LinkedHashSet<String>();
        block0: for (Command cmd : this.commands) {
            String mainName = cmd.getName();
            if (mainName.toLowerCase().startsWith(partial)) {
                suggestions.add(mainName);
                continue;
            }
            for (String alias : cmd.getAliases()) {
                if (!alias.toLowerCase().startsWith(partial)) continue;
                suggestions.add(alias);
                continue block0;
            }
        }
        return suggestions.stream().sorted();
    }

    public void sendMessage(String message) {
        ChatMessage.brandmessage(message);
    }

    public void sendSuccess(String message) {
        if (Minecraft.getInstance().player != null) {
            MutableComponent prefixText = ChatMessage.brandmessage();
            MutableComponent formattedMessage = prefixText.copy().append(Component.literal((String)" -> ").withStyle(ChatFormatting.DARK_GRAY)).append(Component.literal((String)message).withStyle(ChatFormatting.GREEN));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public void sendError(String message) {
        if (Minecraft.getInstance().player != null) {
            MutableComponent prefixText = ChatMessage.brandmessage();
            MutableComponent formattedMessage = prefixText.copy().append(Component.literal((String)" -> ").withStyle(ChatFormatting.DARK_GRAY)).append(Component.literal((String)message).withStyle(ChatFormatting.RED));
            Minecraft.getInstance().player.displayClientMessage(formattedMessage, false);
        }
    }

    public void sendRaw(Component text) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(text, false);
        }
    }
}

