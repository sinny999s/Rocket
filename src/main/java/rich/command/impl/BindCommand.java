
package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import rich.Initialization;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;
import rich.command.impl.HelpCommand;
import rich.modules.module.ModuleRepository;
import rich.modules.module.ModuleStructure;
import rich.util.config.ConfigSystem;
import rich.util.config.impl.bind.BindConfig;
import rich.util.string.KeyHelper;

public class BindCommand
extends Command {
    public BindCommand() {
        super("bind", "Module bind management", "b");
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        ModuleRepository repository = this.getModuleRepository();
        if (repository == null) {
            this.logDirect("Module repository not found!", ChatFormatting.RED);
            return;
        }
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                if (args.length < 3) {
                    this.logDirect("Usage: bind add <module> <key>", ChatFormatting.RED);
                    return;
                }
                String moduleName = args[1];
                String keyName = args[2];
                ModuleStructure module2 = this.findModule(repository, moduleName);
                if (module2 == null) {
                    this.logDirect(String.format("Module %s not found!", moduleName), ChatFormatting.RED);
                    return;
                }
                int key = KeyHelper.getKeyCode(keyName);
                if (key == -1) {
                    this.logDirect(String.format("Unknown key: %s", keyName), ChatFormatting.RED);
                    return;
                }
                module2.setKey(key);
                ConfigSystem.getInstance().save();
                this.logDirect(String.format("\u00a7aModule \u00a7f%s \u00a7abound to key \u00a7f%s", module2.getName(), KeyHelper.getKeyName(key).toLowerCase()), ChatFormatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("Usage: bind remove <module>", ChatFormatting.RED);
                    return;
                }
                String moduleName = args[1];
                ModuleStructure module3 = this.findModule(repository, moduleName);
                if (module3 == null) {
                    this.logDirect(String.format("Module %s not found!", moduleName), ChatFormatting.RED);
                    return;
                }
                module3.setKey(-1);
                ConfigSystem.getInstance().save();
                this.logDirect(String.format("Bind for module %s removed!", module3.getName()), ChatFormatting.GREEN);
                break;
            }
            case "clear": {
                int count = 0;
                for (ModuleStructure module4 : repository.modules()) {
                    if (module4.getKey() == -1) continue;
                    module4.setKey(-1);
                    ++count;
                }
                ConfigSystem.getInstance().save();
                this.logDirect(String.format("All module binds removed! Removed: %d", count), ChatFormatting.GREEN);
                break;
            }
            case "set": {
                if (args.length < 3) {
                    this.logDirect("Usage: bind set <target> <key>", ChatFormatting.RED);
                    this.logDirect("Available targets: Bind", ChatFormatting.RED);
                    return;
                }
                String target = args[1].toLowerCase(Locale.US);
                String keyName = args[2];
                int key = KeyHelper.getKeyCode(keyName);
                if (key == -1) {
                    this.logDirect(String.format("Unknown key: %s", keyName), ChatFormatting.RED);
                    return;
                }
                if (target.equals("Bind")) {
                    BindConfig.getInstance().setKeyAndSave(key);
                    this.logDirect(String.format("\u00a7aBind key changed to: \u00a7f%s", KeyHelper.getKeyName(key).toLowerCase()), ChatFormatting.GREEN);
                    break;
                }
                this.logDirect(String.format("Unknown target: %s", target), ChatFormatting.RED);
                break;
            }
            case "list": {
                List boundModules;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException keyName) {
                        // empty catch block
                    }
                }
                if ((boundModules = repository.modules().stream().filter(m -> m.getKey() != -1 && m.getKey() != -1).collect(Collectors.toList())).isEmpty()) {
                    this.logDirect("No modules with binds!", ChatFormatting.RED);
                    return;
                }
                Paginator<ModuleStructure> paginator = new Paginator<ModuleStructure>(boundModules);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7lBIND LIST \u00a77(" + boundModules.size() + ")");
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                }, module -> {
                    String name = module.getName();
                    String keyName = KeyHelper.getKeyName(module.getKey()).toLowerCase();
                    MutableComponent component = Component.literal((String)("  \u00a7b\u25cf \u00a7f" + name)).append(Component.literal((String)(" \u00a78[\u00a77" + keyName + "\u00a78]")));
                    MutableComponent hoverText = Component.literal((String)("\u00a77Click to remove bind for \u00a7f" + name));
                    String removeCommand = manager.getPrefix() + "bind remove " + name;
                    component.setStyle(component.getStyle().withHoverEvent(new HoverEvent.ShowText(hoverText)).withClickEvent(new ClickEvent.RunCommand(removeCommand)));
                    return component;
                }, manager.getPrefix() + label + " list");
                break;
            }
            default: {
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lBIND MANAGEMENT");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> bind add <module> <key> \u00a78- \u00a7fBind module to key");
                this.logDirect("\u00a77> bind remove <module> \u00a78- \u00a7fRemove module bind");
                this.logDirect("\u00a77> bind list \u00a78- \u00a7fShow bind list");
                this.logDirect("\u00a77> bind clear \u00a78- \u00a7fDelete all binds");
                this.logDirect("\u00a77> bind set Bind <key> \u00a78- \u00a7fChange Bind key");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        String action;
        ModuleRepository repository = this.getModuleRepository();
        if (args.length == 1) {
            return new TabCompleteHelper().append("add", "remove", "list", "clear", "set").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        if (args.length == 2) {
            action = args[0].toLowerCase();
            if (action.equals("add")) {
                return new TabCompleteHelper().append(this.getModuleNames(repository)).filterPrefix(args[1]).stream();
            }
            if (action.equals("remove") || action.equals("del") || action.equals("delete")) {
                return new TabCompleteHelper().append(this.getBoundModuleNames(repository)).filterPrefix(args[1]).stream();
            }
            if (action.equals("set")) {
                return new TabCompleteHelper().append("Bind").filterPrefix(args[1]).stream();
            }
        }
        if (args.length == 3 && ((action = args[0].toLowerCase()).equals("add") || action.equals("set"))) {
            return new TabCompleteHelper().append(KeyHelper.getAllKeyNames()).filterPrefix(args[2]).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Module bind management";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Command to manage module binds and GUI", "Usage:", "> bind add <module> <key> - Bind module to key", "> bind remove <module> - Remove module bind", "> bind list - Show bind list", "> bind clear - Delete all binds", "> bind set Bind <key> - Change Bind key");
    }

    private ModuleRepository getModuleRepository() {
        Initialization instance = Initialization.getInstance();
        if (instance != null && instance.getManager() != null) {
            return instance.getManager().getModuleRepository();
        }
        return null;
    }

    private ModuleStructure findModule(ModuleRepository repository, String name) {
        return repository.modules().stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private String[] getModuleNames(ModuleRepository repository) {
        if (repository == null) {
            return new String[0];
        }
        return (String[])repository.modules().stream().map(ModuleStructure::getName).toArray(String[]::new);
    }

    private String[] getBoundModuleNames(ModuleRepository repository) {
        if (repository == null) {
            return new String[0];
        }
        return (String[])repository.modules().stream().filter(m -> m.getKey() != -1 && m.getKey() != -1).map(ModuleStructure::getName).toArray(String[]::new);
    }
}

