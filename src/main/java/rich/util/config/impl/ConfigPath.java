
package rich.util.config.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigPath {
    private static final String ROOT_DIR = "Rocket";
    private static final String CONFIG_DIR = "configs";
    private static final String AUTO_DIR = "autocfg";
    private static final String CONFIG_FILE = "autoconfig.json";
    private static Path runDirectory;

    public static void init() {
        runDirectory = Paths.get("", new String[0]).toAbsolutePath();
    }

    public static Path getConfigDirectory() {
        return runDirectory.resolve(ROOT_DIR).resolve(CONFIG_DIR).resolve(AUTO_DIR);
    }

    public static Path getConfigFile() {
        return ConfigPath.getConfigDirectory().resolve(CONFIG_FILE);
    }
}

