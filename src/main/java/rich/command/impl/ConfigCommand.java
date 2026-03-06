
package rich.command.impl;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
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
import rich.util.config.ConfigSystem;
import rich.util.config.impl.ConfigPath;

public class ConfigCommand
extends Command {
    public ConfigCommand() {
        super("config", "Config management", "cfg");
    }

    @Override
    public void execute(String label, String[] args) {
        String arg;
        CommandManager manager = CommandManager.getInstance();
        switch (arg = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "load": {
                if (args.length < 2) {
                    this.logDirect("Usage: config load <name>", ChatFormatting.RED);
                    return;
                }
                String name = args[1];
                Path configDir = ConfigPath.getConfigDirectory();
                Path configFile = configDir.resolve(name + ".json");
                if (Files.exists(configFile, new LinkOption[0])) {
                    try {
                        ConfigSystem.getInstance().load();
                        this.logDirect(String.format("Configuration %s loaded!", name));
                    }
                    catch (Exception e) {
                        this.logDirect(String.format("Error loading config! Details: %s", e.getMessage()), ChatFormatting.RED);
                    }
                    break;
                }
                this.logDirect(String.format("Configuration %s not found!", name), ChatFormatting.RED);
                break;
            }
            case "save": {
                if (args.length < 2) {
                    ConfigSystem.getInstance().save();
                    this.logDirect("Configuration saved!");
                    return;
                }
                String name = args[1];
                try {
                    Path configDir = ConfigPath.getConfigDirectory();
                    Path newConfig = configDir.resolve(name + ".json");
                    ConfigSystem.getInstance().save();
                    Path currentConfig = ConfigPath.getConfigFile();
                    Files.copy(currentConfig, newConfig, new CopyOption[0]);
                    this.logDirect(String.format("Configuration %s saved!", name));
                }
                catch (Exception e) {
                    this.logDirect(String.format("Error saving config! Details: %s", e.getMessage()), ChatFormatting.RED);
                }
                break;
            }
            case "list": {
                List<String> configs;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException e) {
                        // empty catch block
                    }
                }
                if ((configs = this.getConfigs()).isEmpty()) {
                    this.logDirect("Configurations not found!", ChatFormatting.RED);
                    return;
                }
                Paginator<String> paginator = new Paginator<String>(configs);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7lCONFIG LIST");
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                }, config -> {
                    MutableComponent namesComponent = Component.literal((String)("  \u00a7b\u25cf \u00a7f" + config));
                    MutableComponent hoverText = Component.literal((String)("\u00a77Click to load config \u00a7f" + config));
                    String loadCommand = manager.getPrefix() + "config load " + config;
                    namesComponent.setStyle(namesComponent.getStyle().withHoverEvent(new HoverEvent.ShowText(hoverText)).withClickEvent(new ClickEvent.RunCommand(loadCommand)));
                    return namesComponent;
                }, manager.getPrefix() + label + " list");
                break;
            }
            case "dir": {
                try {
                    Path configDir = ConfigPath.getConfigDirectory();
                    String os = System.getProperty("os.name").toLowerCase();
                    ProcessBuilder pb = os.contains("win") ? new ProcessBuilder("explorer", configDir.toAbsolutePath().toString()) : (os.contains("mac") ? new ProcessBuilder("open", configDir.toAbsolutePath().toString()) : new ProcessBuilder("xdg-open", configDir.toAbsolutePath().toString()));
                    pb.start();
                    this.logDirect("Config folder opened!");
                }
                catch (IOException e) {
                    this.logDirect("Config folder not found! " + e.getMessage(), ChatFormatting.RED);
                }
                break;
            }
            default: {
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lUSAGE");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> config load <name> \u00a78- \u00a7fLoads config.");
                this.logDirect("\u00a77> config save <name> \u00a78- \u00a7fSaves config.");
                this.logDirect("\u00a77> config list \u00a78- \u00a7fReturns config list");
                this.logDirect("\u00a77> config dir \u00a78- \u00a7fOpens config folder.");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        String action;
        if (args.length == 1) {
            return new TabCompleteHelper().append("load", "save", "list", "dir").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        if (args.length == 2 && ((action = args[0].toLowerCase()).equals("load") || action.equals("save"))) {
            return new TabCompleteHelper().append(this.getConfigs().toArray(new String[0])).filterPrefix(args[1]).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Manage client configs";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Load and save client configs", "Usage:", "> config load <name> - Loads config.", "> config save <name> - Saves config.", "> config list - Returns config list", "> config dir - Opens config folder.");
    }

    public List<String> getConfigs() {
        ArrayList<String> configs = new ArrayList<String>();
        try {
            Path configDir = ConfigPath.getConfigDirectory();
            if (Files.exists(configDir, new LinkOption[0])) {
                Files.list(configDir).filter(path -> path.toString().endsWith(".json")).forEach(path -> {
                    String name = path.getFileName().toString();
                    configs.add(name.substring(0, name.length() - 5));
                });
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return configs;
    }
}

