
package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import rich.IMinecraft;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;
import rich.command.impl.HelpCommand;
import rich.util.config.impl.way.WayConfig;
import rich.util.repository.way.Way;
import rich.util.repository.way.WayRepository;

public class WayCommand
extends Command
implements IMinecraft {
    public WayCommand() {
        super("way", "Waypoint management", "waypoint", "wp");
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        WayRepository repository = WayRepository.getInstance();
        if (WayCommand.mc.player == null) {
            this.logDirect("You must be in-game!", ChatFormatting.RED);
            return;
        }
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                BlockPos pos;
                if (args.length < 2) {
                    this.logDirect("Usage: way add <name> [x] [y] [z]", ChatFormatting.RED);
                    return;
                }
                String name = args[1];
                if (args.length >= 5) {
                    try {
                        int x = Integer.parseInt(args[2]);
                        int y = Integer.parseInt(args[3]);
                        int z = Integer.parseInt(args[4]);
                        pos = new BlockPos(x, y, z);
                    }
                    catch (NumberFormatException e) {
                        this.logDirect("Invalid coordinates!", ChatFormatting.RED);
                        return;
                    }
                } else {
                    pos = WayCommand.mc.player.blockPosition();
                }
                String server = repository.getCurrentServer();
                if (server.isEmpty()) {
                    this.logDirect("Could not determine server!", ChatFormatting.RED);
                    return;
                }
                if (repository.hasWay(name)) {
                    this.logDirect(String.format("Waypoint named %s already exists!", name), ChatFormatting.RED);
                    return;
                }
                String dimension = WayRepository.getCurrentDimension();
                repository.addWayAndSave(name, pos, server, dimension);
                this.logDirect(String.format("\u00a7aWaypoint \u00a7f%s \u00a7aadded at coordinates \u00a7f%d %d %d", name, pos.getX(), pos.getY(), pos.getZ()), ChatFormatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("Usage: way remove <name>", ChatFormatting.RED);
                    return;
                }
                String name = args[1];
                if (!repository.hasWay(name)) {
                    this.logDirect(String.format("Waypoint %s not found!", name), ChatFormatting.RED);
                    return;
                }
                repository.deleteWayAndSave(name);
                this.logDirect(String.format("Waypoint %s deleted!", name), ChatFormatting.GREEN);
                break;
            }
            case "clear": {
                String server = repository.getCurrentServer();
                int count = 0;
                List<Way> toRemove = repository.getWayList().stream().filter(way -> way.server().equalsIgnoreCase(server)).toList();
                for (Way way2 : toRemove) {
                    repository.deleteWay(way2.name());
                    ++count;
                }
                if (count > 0) {
                    WayConfig.getInstance().save();
                }
                this.logDirect(String.format("Deleted waypoints for this server: %d", count), ChatFormatting.GREEN);
                break;
            }
            case "clearall": {
                int count = repository.size();
                repository.clearListAndSave();
                this.logDirect(String.format("All waypoints deleted! Removed: %d", count), ChatFormatting.GREEN);
                break;
            }
            case "list": {
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException count) {
                        // empty catch block
                    }
                }
                String server = repository.getCurrentServer();
                List serverWays = repository.getWayList().stream().filter(way -> way.server().equalsIgnoreCase(server)).toList();
                if (serverWays.isEmpty()) {
                    this.logDirect("No waypoints for this server!", ChatFormatting.RED);
                    return;
                }
                Paginator<Way> paginator = new Paginator<Way>(serverWays);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7lWAYPOINTS \u00a77(" + serverWays.size() + ")");
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                }, way -> {
                    String wayName = way.name();
                    BlockPos pos = way.pos();
                    double distance = WayCommand.mc.player.position().distanceTo(pos.getCenter());
                    MutableComponent component = Component.literal((String)("  \u00a7d\u25cf \u00a7f" + wayName)).append(Component.literal((String)String.format(" \u00a78[\u00a77%d %d %d\u00a78]", pos.getX(), pos.getY(), pos.getZ()))).append(Component.literal((String)String.format(" \u00a78(\u00a77%.1fm\u00a78)", distance)));
                    MutableComponent hoverText = Component.literal((String)"\u00a77Click to remove waypoint");
                    String removeCommand = manager.getPrefix() + "way remove " + wayName;
                    component.setStyle(component.getStyle().withHoverEvent(new HoverEvent.ShowText(hoverText)).withClickEvent(new ClickEvent.RunCommand(removeCommand)));
                    return component;
                }, manager.getPrefix() + label + " list");
                break;
            }
            default: {
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lWAYPOINT MANAGEMENT");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> way add <name> [x y z] \u00a78- \u00a7fAdd waypoint");
                this.logDirect("\u00a77> way remove <name> \u00a78- \u00a7fDelete waypoint");
                this.logDirect("\u00a77> way list \u00a78- \u00a7fShow waypoint list");
                this.logDirect("\u00a77> way clear \u00a78- \u00a7fDelete waypoints for this server");
                this.logDirect("\u00a77> way clearall \u00a78- \u00a7fDelete all waypoints");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        String action;
        WayRepository repository = WayRepository.getInstance();
        if (args.length == 1) {
            return new TabCompleteHelper().append("add", "remove", "list", "clear", "clearall").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        if (args.length == 2 && ((action = args[0].toLowerCase()).equals("remove") || action.equals("del") || action.equals("delete"))) {
            String server = repository.getCurrentServer();
            return new TabCompleteHelper().append(repository.getWayNamesForServer(server).toArray(new String[0])).filterPrefix(args[1]).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Waypoint management";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Command to manage waypoints", "Waypoints are displayed on screen with distance", "Usage:", "> way add <name> [x y z] - Add waypoint (without coords - current position)", "> way remove <name> - Delete waypoint", "> way list - Show waypoints for current server", "> way clear - Delete all waypoints for current server", "> way clearall - Delete all waypoints");
    }
}
