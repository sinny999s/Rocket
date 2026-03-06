package rich.modules.impl.misc;

import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.Instance;

public class BetterChat extends ModuleStructure {

    private static BetterChat instance;

    public BetterChat() {
        super("Better Chat", "Modern floating chat input bar", ModuleCategory.MISC);
        instance = this;
    }

    public static BetterChat getInstance() {
        if (instance == null) {
            instance = Instance.get(BetterChat.class);
        }
        return instance;
    }
}
