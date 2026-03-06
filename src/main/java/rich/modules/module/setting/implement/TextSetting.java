
package rich.modules.module.setting.implement;

import java.util.function.Supplier;
import lombok.Generated;
import rich.modules.module.setting.Setting;

public class TextSetting
extends Setting {
    private String text;
    private int min;
    private int max;

    public TextSetting(String name, String description) {
        super(name, description);
    }

    public TextSetting visible(Supplier<Boolean> visible) {
        this.setVisible(visible);
        return this;
    }

    @Generated
    public String getText() {
        return this.text;
    }

    @Generated
    public int getMin() {
        return this.min;
    }

    @Generated
    public int getMax() {
        return this.max;
    }

    @Generated
    public TextSetting setText(String text) {
        this.text = text;
        return this;
    }

    @Generated
    public TextSetting setMin(int min) {
        this.min = min;
        return this;
    }

    @Generated
    public TextSetting setMax(int max) {
        this.max = max;
        return this;
    }
}

