
package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import rich.command.Command;
import rich.command.helpers.TabCompleteHelper;
import rich.command.impl.HelpCommand;
import rich.modules.impl.misc.AutoBuy;
import rich.screens.clickgui.impl.autobuy.items.ItemRegistry;
import rich.screens.clickgui.impl.autobuy.manager.AutoBuyManager;
import rich.util.config.impl.autobuyconfig.AutoBuyConfig;

public class AutoBuyCommand
extends Command {
    public AutoBuyCommand() {
        super("autobuy", "AutoBuy config management", "ab");
    }

    @Override
    public void execute(String label, String[] args) {
        String arg;
        switch (arg = args.length > 0 ? args[0].toLowerCase(Locale.US) : "") {
            case "load": {
                try {
                    AutoBuyConfig.getInstance().load();
                    ItemRegistry.reloadSettings();
                    this.logDirect("Autobuy configuration loaded!", ChatFormatting.GREEN);
                }
                catch (Exception e) {
                    this.logDirect("Error loading configuration: " + e.getMessage(), ChatFormatting.RED);
                }
                break;
            }
            case "save": {
                try {
                    AutoBuyConfig.getInstance().save();
                    this.logDirect("Autobuy configuration saved!", ChatFormatting.GREEN);
                }
                catch (Exception e) {
                    this.logDirect("Error saving configuration: " + e.getMessage(), ChatFormatting.RED);
                }
                break;
            }
            case "reset": {
                try {
                    AutoBuyConfig.getInstance().reset();
                    ItemRegistry.clearCache();
                    ItemRegistry.ensureSettingsLoaded();
                    this.logDirect("Autobuy configuration reset!", ChatFormatting.GREEN);
                }
                catch (Exception e) {
                    this.logDirect("Error resetting configuration: " + e.getMessage(), ChatFormatting.RED);
                }
                break;
            }
            case "status": {
                int totalItems = ItemRegistry.getAllItems().size();
                long enabledItems = ItemRegistry.getAllItems().stream().filter(i -> i.isEnabled()).count();
                AutoBuy autoBuyModule = AutoBuy.getInstance();
                boolean moduleActive = autoBuyModule != null && autoBuyModule.isState();
                boolean buttonEnabled = AutoBuyManager.getInstance().isEnabled();
                String currentStatus = !moduleActive ? "\u00a7cOFF" : (buttonEnabled ? "\u00a7aON" : "\u00a7ePAUSE");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lAUTOBUY STATUS");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77Status: " + currentStatus);
                this.logDirect("\u00a77Module: " + (moduleActive ? "\u00a7aEnabled" : "\u00a7cDisabled"));
                this.logDirect("\u00a77Button: " + (buttonEnabled ? "\u00a7aEnabled" : "\u00a7cDisabled"));
                this.logDirect("\u00a77Active items: \u00a7b" + enabledItems + "\u00a77/\u00a7b" + totalItems);
                if (autoBuyModule != null && moduleActive) {
                    int clients = autoBuyModule.getNetworkManager().getConnectedClientCount();
                    int inAuction = autoBuyModule.getNetworkManager().getClientsInAuctionCount();
                    boolean connected = autoBuyModule.getNetworkManager().isConnectedToServer();
                    boolean isServer = autoBuyModule.getNetworkManager().isServerRunning();
                    if (isServer) {
                        this.logDirect("\u00a77Mode: \u00a7bBuying (Server)");
                        this.logDirect("\u00a77Connected clients: \u00a7b" + clients);
                        this.logDirect("\u00a77In auction: \u00a7b" + inAuction);
                    } else {
                        this.logDirect("\u00a77Mode: \u00a7bChecker (Client)");
                        this.logDirect("\u00a77Connecting to server: " + (connected ? "\u00a7aYes" : "\u00a7cNo"));
                    }
                }
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                break;
            }
            default: {
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lAUTOBUY");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> autobuy load \u00a78- \u00a7fLoads configuration");
                this.logDirect("\u00a77> autobuy save \u00a78- \u00a7fSaves configuration");
                this.logDirect("\u00a77> autobuy reset \u00a78- \u00a7fResets configuration");
                this.logDirect("\u00a77> autobuy status \u00a78- \u00a7fShows autobuy status");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            }
        }
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        if (args.length == 1) {
            return new TabCompleteHelper().append("load", "save", "reset", "status").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "AutoBuy config management";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Command to manage AutoBuy configuration", "Usage:", "> autobuy load - Loads configuration from file", "> autobuy save - Saves current configuration", "> autobuy reset - Resets configuration to defaults", "> autobuy status - Shows current autobuy status");
    }
}

