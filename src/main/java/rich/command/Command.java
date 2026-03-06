
package rich.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import rich.command.CommandManager;
import rich.util.string.chat.ChatMessage;

public abstract class Command {
    private final String name;
    private final String description;
    private final List<String> aliases;

    public Command(String name, String description, String ... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = Arrays.asList(aliases);
    }

    public abstract void execute(String var1, String[] var2);

    public Stream<String> tabComplete(String label, String[] args) {
        return Stream.empty();
    }

    public String getShortDesc() {
        return this.description;
    }

    public List<String> getLongDesc() {
        return Arrays.asList(this.description, "", "Usage:", "> " + this.name + " - " + this.description);
    }

    public boolean hiddenFromHelp() {
        return false;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public List<String> getAllNames() {
        ArrayList<String> names = new ArrayList<String>();
        names.add(this.name);
        names.addAll(this.aliases);
        return names;
    }

    public boolean matches(String input) {
        return this.name.equalsIgnoreCase(input) || this.aliases.stream().anyMatch(alias -> alias.equalsIgnoreCase(input));
    }

    protected void logDirect(String message) {
        ChatMessage.brandmessage(message);
    }

    protected void logDirect(String message, ChatFormatting formatting) {
        CommandManager manager = CommandManager.getInstance();
        if (formatting == ChatFormatting.RED) {
            manager.sendError(message);
        } else if (formatting == ChatFormatting.GREEN) {
            manager.sendSuccess(message);
        } else {
            manager.sendMessage(message);
        }
    }

    protected void logDirect(Component text) {
        ChatMessage.brandmessage(text.getString());
    }

    protected void logDirect(MutableComponent text) {
        ChatMessage.brandmessage(text.getString());
    }

    protected void logDirectRaw(Component text) {
        CommandManager.getInstance().sendRaw(text);
    }

    protected void logDirectRaw(MutableComponent text) {
        CommandManager.getInstance().sendRaw(text);
    }
}

