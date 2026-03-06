
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
import rich.util.repository.friend.FriendUtils;

public class FriendCommand
extends Command {
    public FriendCommand() {
        super("friend", "Friend list management", "f", "friends");
    }

    @Override
    public void execute(String label, String[] args) {
        String arg;
        CommandManager manager = CommandManager.getInstance();
        switch (arg = args.length > 0 ? args[0].toLowerCase(Locale.US) : "list") {
            case "add": {
                if (args.length < 2) {
                    this.logDirect("Usage: friend add <name>", ChatFormatting.RED);
                    return;
                }
                String name = args[1];
                if (FriendUtils.isFriend(name)) {
                    this.logDirect(String.format("Player %s is already a friend!", name), ChatFormatting.RED);
                    return;
                }
                FriendUtils.addFriendAndSave(name);
                this.logDirect(String.format("Player %s added to friends!", name), ChatFormatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                if (args.length < 2) {
                    this.logDirect("Usage: friend remove <name>", ChatFormatting.RED);
                    return;
                }
                String name = args[1];
                if (!FriendUtils.isFriend(name)) {
                    this.logDirect(String.format("Player %s not found in friends!", name), ChatFormatting.RED);
                    return;
                }
                FriendUtils.removeFriendAndSave(name);
                this.logDirect(String.format("Player %s removed from friends!", name), ChatFormatting.GREEN);
                break;
            }
            case "clear": {
                int count = FriendUtils.size();
                FriendUtils.clearAndSave();
                this.logDirect(String.format("Friends list cleared! Removed: %d", count), ChatFormatting.GREEN);
                break;
            }
            case "list": {
                List<String> friends;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException numberFormatException) {
                        // empty catch block
                    }
                }
                if ((friends = FriendUtils.getFriendNames()).isEmpty()) {
                    this.logDirect("Friends list is empty!", ChatFormatting.RED);
                    return;
                }
                Paginator<String> paginator = new Paginator<String>(friends);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7lFRIENDS LIST \u00a77(" + friends.size() + ")");
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                }, friend -> {
                    MutableComponent nameComponent = Component.literal((String)("  \u00a7a\u25cf \u00a7f" + friend));
                    MutableComponent hoverText = Component.literal((String)("\u00a77Click to remove \u00a7f" + friend + " \u00a77of friends"));
                    String removeCommand = manager.getPrefix() + "friend remove " + friend;
                    nameComponent.setStyle(nameComponent.getStyle().withHoverEvent(new HoverEvent.ShowText(hoverText)).withClickEvent(new ClickEvent.RunCommand(removeCommand)));
                    return nameComponent;
                }, manager.getPrefix() + label + " list");
                break;
            }
            default: {
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lFRIENDS MANAGEMENT");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> friend add <name> \u00a78- \u00a7fAdd player to friends");
                this.logDirect("\u00a77> friend remove <name> \u00a78- \u00a7fDelete player of friends");
                this.logDirect("\u00a77> friend list \u00a78- \u00a7fShow friends list");
                this.logDirect("\u00a77> friend clear \u00a78- \u00a7fClear friends list");
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
                return new TabCompleteHelper().append(FriendUtils.getFriendNames().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Friend list management";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Command to manage friends list", "Usage:", "> friend add <name> - Add player to friends", "> friend remove <name> - Delete player of friends", "> friend list - Show friends list", "> friend clear - Clear friends list");
    }

    private List<String> getOnlinePlayers() {
        ArrayList<String> players = new ArrayList<String>();
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            for (PlayerInfo entry : mc.getConnection().getOnlinePlayers()) {
                String name = entry.getProfile().name();
                if (FriendUtils.isFriend(name)) continue;
                players.add(name);
            }
        }
        return players;
    }
}

