
package rich.command.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;
import rich.command.impl.HelpCommand;
import rich.util.repository.staff.StaffUtils;

public class StaffCommand
extends Command {
    public StaffCommand() {
        super("staff", "Staff list management", new String[0]);
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                if (args.length < 2) {
                    this.logDirect("Usage: staff add <name>", ChatFormatting.RED);
                    return;
                }
                String name = args[1];
                if (StaffUtils.isStaff(name)) {
                    this.logDirect(String.format("Player %s is already in staff list!", name), ChatFormatting.RED);
                    return;
                }
                StaffUtils.addStaffAndSave(name);
                this.logDirect(String.format("Player %s added to staff!", name), ChatFormatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("Usage: staff remove <name>", ChatFormatting.RED);
                    return;
                }
                String name = args[1];
                if (!StaffUtils.isStaff(name)) {
                    this.logDirect(String.format("Player %s not found in staff list!", name), ChatFormatting.RED);
                    return;
                }
                StaffUtils.removeStaffAndSave(name);
                this.logDirect(String.format("Player %s removed from staff!", name), ChatFormatting.GREEN);
                break;
            }
            case "clear": {
                int count = StaffUtils.size();
                StaffUtils.clearAndSave();
                this.logDirect(String.format("Staff list cleared! Removed: %d", count), ChatFormatting.GREEN);
                break;
            }
            case "list": {
                List<String> staff;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                if ((staff = StaffUtils.getStaffNames()).isEmpty()) {
                    this.logDirect("Staff list is empty!", ChatFormatting.RED);
                    return;
                }
                Paginator<String> paginator = new Paginator<String>(staff);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7lSTAFF LIST \u00a77(" + staff.size() + ")");
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                }, staffName -> {
                    MutableComponent nameComponent = Component.literal((String)("  \u00a7c\u25cf \u00a7f" + staffName));
                    MutableComponent hoverText = Component.literal((String)("\u00a77Click to remove \u00a7f" + staffName + " \u00a77of staff"));
                    String removeCommand = manager.getPrefix() + "staff remove " + staffName;
                    nameComponent.setStyle(nameComponent.getStyle().withHoverEvent(new HoverEvent.ShowText(hoverText)).withClickEvent(new ClickEvent.RunCommand(removeCommand)));
                    return nameComponent;
                }, manager.getPrefix() + label + " list");
                break;
            }
            default: {
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lSTAFF MANAGEMENT");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> staff add <name> \u00a78- \u00a7fAdd player to staff");
                this.logDirect("\u00a77> staff remove <name> \u00a78- \u00a7fDelete player of staff");
                this.logDirect("\u00a77> staff list \u00a78- \u00a7fShow staff list");
                this.logDirect("\u00a77> staff clear \u00a78- \u00a7fClear staff list");
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
                return new TabCompleteHelper().append(this.getOnlinePlayers().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
            if (action.equals("remove") || action.equals("del") || action.equals("delete")) {
                return new TabCompleteHelper().append(StaffUtils.getStaffNames().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Staff list management";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Command to manage server staff list", "Usage:", "> staff add <name> - Add player to staff", "> staff remove <name> - Delete player of staff", "> staff list - Show staff list", "> staff clear - Clear staff list");
    }

    private List<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<String>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo entry : mc.getConnection().getOnlinePlayers()) {
                String name = entry.getProfile().name();
                if (StaffUtils.isStaff(name)) continue;
                players.add(name);
            }
        }
        return players;
    }
}

