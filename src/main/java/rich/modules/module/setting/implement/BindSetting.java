
package rich.modules.module.setting.implement;

import java.util.function.Supplier;
import lombok.Generated;
import rich.modules.module.setting.Setting;

public class BindSetting
extends Setting {
    private int key = -1;
    private int type = 1;

    public BindSetting(String name, String description) {
        super(name, description);
    }

    public BindSetting visible(Supplier<Boolean> visible) {
        this.setVisible(visible);
        return this;
    }

    @Generated
    public int getKey() {
        return this.key;
    }

    @Generated
    public int getType() {
        return this.type;
    }

    @Generated
    public BindSetting setKey(int key) {
        this.key = key;
        return this;
    }

    @Generated
    public BindSetting setType(int type) {
        this.type = type;
        return this;
    }
}

