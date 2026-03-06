
package rich.modules.module.setting;

import java.util.List;
import rich.modules.module.setting.Setting;
import rich.modules.module.setting.implement.BindSetting;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ButtonSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.TextSetting;
import rich.screens.clickgui.impl.settingsrender.BindComponent;
import rich.screens.clickgui.impl.settingsrender.ButtonComponent;
import rich.screens.clickgui.impl.settingsrender.CheckboxComponent;
import rich.screens.clickgui.impl.settingsrender.ColorComponent;
import rich.screens.clickgui.impl.settingsrender.MultiSelectComponent;
import rich.screens.clickgui.impl.settingsrender.SelectComponent;
import rich.screens.clickgui.impl.settingsrender.SliderComponent;
import rich.screens.clickgui.impl.settingsrender.TextComponent;
import rich.util.interfaces.AbstractSettingComponent;

public class SettingComponentAdder {
    public void addSettingComponent(List<Setting> settings, List<AbstractSettingComponent> components) {
        settings.forEach(setting -> {
            if (setting instanceof BooleanSetting) {
                BooleanSetting booleanSetting = (BooleanSetting)setting;
                components.add(new CheckboxComponent(booleanSetting));
            }
            if (setting instanceof BindSetting) {
                BindSetting bindSetting = (BindSetting)setting;
                components.add(new BindComponent(bindSetting));
            }
            if (setting instanceof ColorSetting) {
                ColorSetting colorSetting = (ColorSetting)setting;
                components.add(new ColorComponent(colorSetting));
            }
            if (setting instanceof TextSetting) {
                TextSetting textSetting = (TextSetting)setting;
                components.add(new TextComponent(textSetting));
            }
            if (setting instanceof SliderSettings) {
                SliderSettings valueSetting = (SliderSettings)setting;
                components.add(new SliderComponent(valueSetting));
            }
            if (setting instanceof ButtonSetting) {
                ButtonSetting buttonSetting = (ButtonSetting)setting;
                components.add(new ButtonComponent(buttonSetting));
            }
            if (setting instanceof SelectSetting) {
                SelectSetting selectSetting = (SelectSetting)setting;
                components.add(new SelectComponent(selectSetting));
            }
            if (setting instanceof MultiSelectSetting) {
                MultiSelectSetting multiSelectSetting = (MultiSelectSetting)setting;
                components.add(new MultiSelectComponent(multiSelectSetting));
            }
        });
    }
}

