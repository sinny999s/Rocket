/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.loader.api.FabricLoader
 *  net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint
 */
package rich.util.mods.config.wave;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class WaveCapesConfigOverride
implements PreLaunchEntrypoint {
    private static final String CONFIG_CONTENT = "{\n  \"configVersion\": 2,\n  \"windMode\": \"WAVES\",\n  \"capeStyle\": \"SMOOTH\",\n  \"capeMovement\": \"BASIC_SIMULATION_3D\",\n  \"gravity\": 15,\n  \"heightMultiplier\": 5,\n  \"straveMultiplier\": 5\n}\n";

    public void onPreLaunch() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path waveCapesConfig = configDir.resolve("waveycapes.json");
        try {
            Files.writeString((Path)waveCapesConfig, (CharSequence)CONFIG_CONTENT, (OpenOption[])new OpenOption[0]);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}

