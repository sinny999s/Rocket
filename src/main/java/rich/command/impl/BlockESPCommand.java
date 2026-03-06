
package rich.command.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import rich.Initialization;
import rich.command.Command;
import rich.command.CommandManager;
import rich.command.helpers.Paginator;
import rich.command.helpers.TabCompleteHelper;
import rich.command.impl.HelpCommand;
import rich.modules.impl.render.BlockESP;
import rich.util.config.impl.blockesp.BlockESPConfig;

public class BlockESPCommand
extends Command {
    public BlockESPCommand() {
        super("blockesp", "Block ESP management", "besp");
    }

    @Override
    public void execute(String label, String[] args) {
        String action;
        CommandManager manager = CommandManager.getInstance();
        BlockESPConfig config = BlockESPConfig.getInstance();
        switch (action = args.length > 0 ? args[0].toLowerCase(Locale.US) : "help") {
            case "add": {
                Block block;
                if (args.length < 2) {
                    this.logDirect("Usage: blockesp add <block>", ChatFormatting.RED);
                    this.logDirect("Example: blockesp add minecraft:diamond_ore", ChatFormatting.RED);
                    return;
                }
                Object blockId = args[1].toLowerCase();
                if (!((String)blockId).contains(":")) {
                    blockId = "minecraft:" + (String)blockId;
                }
                if ((block = (Block)BuiltInRegistries.BLOCK.getValue(Identifier.tryParse((String)blockId))) == null || block == Blocks.AIR) {
                    this.logDirect(String.format("Block %s not found!", args[1]), ChatFormatting.RED);
                    return;
                }
                String registryName = BuiltInRegistries.BLOCK.getKey(block).toString();
                if (config.hasBlock(registryName)) {
                    this.logDirect(String.format("Block %s already in list!", registryName), ChatFormatting.RED);
                    return;
                }
                config.addBlockAndSave(registryName);
                this.syncWithModule();
                this.logDirect(String.format("\u00a7aBlock \u00a7f%s \u00a7aadded to BlockESP!", registryName), ChatFormatting.GREEN);
                break;
            }
            case "remove": 
            case "del": 
            case "delete": {
                Block block;
                Object registryName;
                if (args.length < 2) {
                    this.logDirect("Usage: blockesp remove <block>", ChatFormatting.RED);
                    return;
                }
                Object blockId = args[1].toLowerCase();
                if (!((String)blockId).contains(":")) {
                    blockId = "minecraft:" + (String)blockId;
                }
                Object object = registryName = (block = (Block)BuiltInRegistries.BLOCK.getValue(Identifier.tryParse((String)blockId))) != null ? BuiltInRegistries.BLOCK.getKey(block).toString() : blockId;
                if (!config.hasBlock((String)registryName)) {
                    this.logDirect(String.format("Block %s not found in list!", registryName), ChatFormatting.RED);
                    return;
                }
                config.removeBlockAndSave((String)registryName);
                this.syncWithModule();
                this.logDirect(String.format("Block %s removed from BlockESP!", registryName), ChatFormatting.GREEN);
                break;
            }
            case "clear": {
                int count = config.size();
                config.clearAndSave();
                this.syncWithModule();
                this.logDirect(String.format("BlockESP list cleared! Removed: %d", count), ChatFormatting.GREEN);
                break;
            }
            case "list": {
                List<String> blocks;
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException block) {
                        // empty catch block
                    }
                }
                if ((blocks = config.getBlockList()).isEmpty()) {
                    this.logDirect("BlockESP list is empty!", ChatFormatting.RED);
                    return;
                }
                Paginator<String> paginator = new Paginator<String>(blocks);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7lBLOCKESP BLOCKS \u00a77(" + blocks.size() + ")");
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                }, blockName -> {
                    String shortName = blockName.replace("minecraft:", "");
                    MutableComponent component = Component.literal((String)("  \u00a76\u25cf \u00a7f" + shortName)).append(Component.literal((String)(" \u00a78(" + blockName + ")")));
                    MutableComponent hoverText = Component.literal((String)("\u00a77Click to remove \u00a7f" + shortName));
                    String removeCommand = manager.getPrefix() + "blockesp remove " + blockName;
                    component.setStyle(component.getStyle().withHoverEvent(new HoverEvent.ShowText(hoverText)).withClickEvent(new ClickEvent.RunCommand(removeCommand)));
                    return component;
                }, manager.getPrefix() + label + " list");
                break;
            }
            case "blocks": 
            case "allblocks": {
                int page = 1;
                if (args.length > 1) {
                    try {
                        page = Integer.parseInt(args[1]);
                    }
                    catch (NumberFormatException blocks) {
                        // empty catch block
                    }
                }
                List allBlocks = BuiltInRegistries.BLOCK.keySet().stream().map(Identifier::toString).sorted().toList();
                Paginator<String> paginator = new Paginator<String>(allBlocks, 15);
                paginator.setPage(page);
                paginator.display(() -> {
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                    this.logDirect("\u00a7f\u00a7lALL BLOCKS \u00a77(" + allBlocks.size() + ")");
                    this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                }, blockName -> {
                    boolean inList = config.hasBlock((String)blockName);
                    String prefix = inList ? "\u00a7a\u2713" : "\u00a78\u25cb";
                    MutableComponent component = Component.literal((String)("  " + prefix + " \u00a7f" + blockName.replace("minecraft:", "")));
                    String command = inList ? manager.getPrefix() + "blockesp remove " + blockName : manager.getPrefix() + "blockesp add " + blockName;
                    MutableComponent hoverText = Component.literal((String)(inList ? "\u00a77Click to remove" : "\u00a77Click to add"));
                    component.setStyle(component.getStyle().withHoverEvent(new HoverEvent.ShowText(hoverText)).withClickEvent(new ClickEvent.RunCommand(command)));
                    return component;
                }, manager.getPrefix() + label + " blocks");
                break;
            }
            default: {
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a7f\u00a7lBLOCKESP");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77> blockesp add <block> \u00a78- \u00a7fAdd block");
                this.logDirect("\u00a77> blockesp remove <block> \u00a78- \u00a7fRemove block");
                this.logDirect("\u00a77> blockesp list \u00a78- \u00a7fShow added blocks");
                this.logDirect("\u00a77> blockesp clear \u00a78- \u00a7fClear list");
                this.logDirect("\u00a77> blockesp blocks \u00a78- \u00a7fShow all game blocks");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
                this.logDirect("\u00a77Examples:");
                this.logDirect("\u00a78> \u00a7fblockesp add diamond_ore");
                this.logDirect("\u00a78> \u00a7fblockesp add minecraft:ancient_debris");
                this.logDirectRaw(Component.literal((String)HelpCommand.getLine()));
            }
        }
    }

    private void syncWithModule() {
        BlockESP module = this.getBlockESPModule();
        if (module != null) {
            module.getBlocksToHighlight().clear();
            module.getBlocksToHighlight().addAll(BlockESPConfig.getInstance().getBlocks());
        }
    }

    private BlockESP getBlockESPModule() {
        if (Initialization.getInstance() == null || Initialization.getInstance().getManager() == null) {
            return null;
        }
        return Initialization.getInstance().getManager().getModuleRepository().modules().stream().filter(m -> m instanceof BlockESP).map(m -> (BlockESP)m).findFirst().orElse(null);
    }

    @Override
    public Stream<String> tabComplete(String label, String[] args) {
        if (args.length == 1) {
            return new TabCompleteHelper().append("add", "remove", "list", "clear", "blocks").sortAlphabetically().filterPrefix(args[0]).stream();
        }
        if (args.length == 2) {
            String action = args[0].toLowerCase();
            if (action.equals("add")) {
                return BuiltInRegistries.BLOCK.keySet().stream().map(Identifier::toString).filter(name -> name.toLowerCase().contains(args[1].toLowerCase())).limit(50L);
            }
            if (action.equals("remove") || action.equals("del") || action.equals("delete")) {
                return new TabCompleteHelper().append(BlockESPConfig.getInstance().getBlockList().toArray(new String[0])).filterPrefix(args[1]).stream();
            }
        }
        return Stream.empty();
    }

    @Override
    public String getShortDesc() {
        return "Block ESP management";
    }

    @Override
    public List<String> getLongDesc() {
        return Arrays.asList("Command to manage BlockESP block list", "Usage:", "> blockesp add <block> - Add block to list", "> blockesp remove <block> - Remove block from list", "> blockesp list - Show added blocks", "> blockesp clear - Clear list", "> blockesp blocks - Show all game blocks");
    }
}
