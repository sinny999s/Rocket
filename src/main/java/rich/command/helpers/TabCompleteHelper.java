
package rich.command.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import rich.command.Command;
import rich.command.CommandManager;

public class TabCompleteHelper {
    private final List<String> completions = new ArrayList<String>();
    private String prefix = "";
    private boolean sorted = false;

    public TabCompleteHelper append(String ... strings) {
        this.completions.addAll(Arrays.asList(strings));
        return this;
    }

    public TabCompleteHelper addCommands(CommandManager manager) {
        String filter = this.prefix.toLowerCase();
        block0: for (Command cmd : manager.getCommands()) {
            String mainName = cmd.getName();
            if (filter.isEmpty()) {
                this.completions.add(mainName);
                continue;
            }
            if (mainName.toLowerCase().startsWith(filter)) {
                this.completions.add(mainName);
                continue;
            }
            for (String alias : cmd.getAliases()) {
                if (!alias.toLowerCase().startsWith(filter)) continue;
                this.completions.add(alias);
                continue block0;
            }
        }
        return this;
    }

    public TabCompleteHelper addCommands(List<Command> commands) {
        String filter = this.prefix.toLowerCase();
        block0: for (Command cmd : commands) {
            String mainName = cmd.getName();
            if (filter.isEmpty()) {
                this.completions.add(mainName);
                continue;
            }
            if (mainName.toLowerCase().startsWith(filter)) {
                this.completions.add(mainName);
                continue;
            }
            for (String alias : cmd.getAliases()) {
                if (!alias.toLowerCase().startsWith(filter)) continue;
                this.completions.add(alias);
                continue block0;
            }
        }
        return this;
    }

    public TabCompleteHelper filterPrefix(String prefix) {
        this.prefix = prefix.toLowerCase();
        return this;
    }

    public TabCompleteHelper sortAlphabetically() {
        this.sorted = true;
        return this;
    }

    public TabCompleteHelper prepend(String ... strings) {
        ArrayList<String> temp = new ArrayList<String>(Arrays.asList(strings));
        temp.addAll(this.completions);
        this.completions.clear();
        this.completions.addAll(temp);
        return this;
    }

    public Stream<String> stream() {
        Stream<String> stream = this.completions.stream().filter(s -> s.toLowerCase().startsWith(this.prefix));
        if (this.sorted) {
            stream = stream.sorted();
        }
        return stream;
    }

    public List<String> toList() {
        return this.stream().toList();
    }

    public String[] toArray() {
        return (String[])this.stream().toArray(String[]::new);
    }
}

