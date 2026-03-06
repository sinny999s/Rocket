
package rich.util.render.font;

import java.util.LinkedHashMap;
import java.util.Map;
import rich.util.render.font.Font;

public class Fonts {
    private static final Map<String, String> FONT_REGISTRY = new LinkedHashMap<String, String>();
    public static final Font BOLD = Fonts.register("bold", "bold");
    public static final Font ICONS = Fonts.register("icons", "icons");
    public static final Font ICONSTYPETHO = Fonts.register("iconstypetho", "iconstypetho");
    public static final Font GUI_ICONS = Fonts.register("guiicons", "guiicons");
    public static final Font HUD_ICONS = Fonts.register("hudicons", "hudicons");
    public static final Font CATEGORY_ICONS = Fonts.register("categoryicons", "categoryicons");
    public static final Font DEFAULT = Fonts.register("default", "default");
    public static final Font REGULAR = Fonts.register("regular", "regular");
    public static final Font TEST = Fonts.register("test", "test");
    public static final Font INTER = Fonts.register("inter", "inter");
    public static final Font REGULARNEW = Fonts.register("regularnew", "regularnew");
    public static final Font MAINMENUSCREEN = Fonts.register("mainmenuicons", "mainmenuicons");

    private static Font register(String name, String path) {
        FONT_REGISTRY.put(name, path);
        return new Font(name);
    }

    public static Map<String, String> getRegistry() {
        return FONT_REGISTRY;
    }

    private Fonts() {
    }
}

