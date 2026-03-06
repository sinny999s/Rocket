
package rich.command.helpers;

import java.util.List;
import java.util.function.Function;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import rich.command.CommandManager;
import rich.command.impl.HelpCommand;

public class Paginator<T> {
    private final List<T> items;
    private final int itemsPerPage;
    private int currentPage;

    public Paginator(List<T> items) {
        this(items, 8);
    }

    public Paginator(List<T> items, int itemsPerPage) {
        this.items = items;
        this.itemsPerPage = itemsPerPage;
        this.currentPage = 1;
    }

    public int getTotalPages() {
        return Math.max(1, (int)Math.ceil((double)this.items.size() / (double)this.itemsPerPage));
    }

    public List<T> getCurrentPageItems() {
        int start = (this.currentPage - 1) * this.itemsPerPage;
        int end = Math.min(start + this.itemsPerPage, this.items.size());
        return this.items.subList(start, end);
    }

    public void setPage(int page) {
        this.currentPage = Math.max(1, Math.min(page, this.getTotalPages()));
    }

    public void display(Runnable header, Function<T, MutableComponent> itemFormatter, String commandPrefix) {
        CommandManager manager = CommandManager.getInstance();
        if (header != null) {
            header.run();
        }
        for (T item : this.getCurrentPageItems()) {
            MutableComponent formatted = itemFormatter.apply(item);
            manager.sendRaw(formatted);
        }
        if (this.getTotalPages() > 1) {
            this.displayNavigation(manager, commandPrefix);
        } else {
            manager.sendRaw(Component.literal((String)HelpCommand.getLine()));
        }
    }

    private void displayNavigation(CommandManager manager, String commandPrefix) {
        manager.sendRaw(Component.literal((String)HelpCommand.getLine()));
        MutableComponent navigation = Component.literal((String)"");
        if (this.currentPage > 1) {
            MutableComponent prevButton = Component.literal((String)"\u00a78[\u00a7b\u25c4 Back\u00a78]");
            String prevCommand = commandPrefix + " " + (this.currentPage - 1);
            prevButton.setStyle(prevButton.getStyle().withHoverEvent(new HoverEvent.ShowText(Component.literal((String)("\u00a77Page " + (this.currentPage - 1))))).withClickEvent(new ClickEvent.RunCommand(prevCommand)));
            navigation.append(prevButton);
        } else {
            navigation.append(Component.literal((String)"\u00a78[\u00a77\u25c4 Back\u00a78]"));
        }
        navigation.append(Component.literal((String)(" \u00a77Page \u00a7b" + this.currentPage + "\u00a77/\u00a7b" + this.getTotalPages() + " ")));
        if (this.currentPage < this.getTotalPages()) {
            MutableComponent nextButton = Component.literal((String)"\u00a78[\u00a7bForward \u25ba\u00a78]");
            String nextCommand = commandPrefix + " " + (this.currentPage + 1);
            nextButton.setStyle(nextButton.getStyle().withHoverEvent(new HoverEvent.ShowText(Component.literal((String)("\u00a77Page " + (this.currentPage + 1))))).withClickEvent(new ClickEvent.RunCommand(nextCommand)));
            navigation.append(nextButton);
        } else {
            navigation.append(Component.literal((String)"\u00a78[\u00a77Forward \u25ba\u00a78]"));
        }
        manager.sendRaw(navigation);
    }

    public static <T> void paginate(String[] args, Paginator<T> paginator, Runnable header, Function<T, MutableComponent> itemFormatter, String commandPrefix) {
        if (args.length > 0) {
            try {
                int page = Integer.parseInt(args[0]);
                paginator.setPage(page);
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        paginator.display(header, itemFormatter, commandPrefix);
    }
}

