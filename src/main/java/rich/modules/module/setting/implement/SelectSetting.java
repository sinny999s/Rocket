
package rich.modules.module.setting.implement;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import lombok.Generated;
import rich.modules.module.setting.Setting;

public class SelectSetting
extends Setting {
    private String selected;
    private List<String> list;

    public SelectSetting(String name, String description) {
        super(name, description);
    }

    public SelectSetting value(String ... values) {
        this.list = Arrays.asList(values);
        this.selected = this.list.isEmpty() ? "" : this.list.get(0);
        return this;
    }

    public SelectSetting visible(Supplier<Boolean> visible) {
        this.setVisible(visible);
        return this;
    }

    public SelectSetting selected(String string) {
        if (this.list.contains(string)) {
            this.selected = string;
        }
        return this;
    }

    public boolean isSelected(String name) {
        return this.selected.equals(name);
    }

    @Generated
    public String getSelected() {
        return this.selected;
    }

    @Generated
    public List<String> getList() {
        return this.list;
    }

    @Generated
    public void setSelected(String selected) {
        this.selected = selected;
    }
}

