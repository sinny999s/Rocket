
package rich.modules.module.category;

import lombok.Generated;

public enum ModuleCategory {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    MISC("Misc"),
    AUTOBUY("AutoBuy"),
    WORLD("World");

    final String readableName;

    @Generated
    private ModuleCategory(String readableName) {
        this.readableName = readableName;
    }

    @Generated
    public String getReadableName() {
        return this.readableName;
    }
}

