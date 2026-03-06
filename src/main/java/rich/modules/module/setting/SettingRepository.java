/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package rich.modules.module.setting;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import rich.modules.module.setting.Setting;
import rich.modules.module.setting.Setupable;

public class SettingRepository
implements Setupable {
    private final List<Setting> settings = Lists.newArrayList();

    @Override
    public final void settings(Setting ... setting) {
        this.settings.addAll(Arrays.asList(setting));
    }

    public Setting get(String name) {
        return this.settings.stream().filter(setting -> setting.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Setting> settings() {
        return this.settings;
    }
}

