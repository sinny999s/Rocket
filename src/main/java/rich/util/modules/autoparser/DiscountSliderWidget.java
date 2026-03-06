
package rich.util.modules.autoparser;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import rich.modules.impl.misc.autoparser.AutoParser;

public class DiscountSliderWidget
extends AbstractSliderButton {
    public DiscountSliderWidget(int x, int y, int width, int height, int initialValue) {
        super(x, y, width, height, Component.literal((String)("Cut prices by: " + initialValue + "%")), (double)(initialValue - 10) / 80.0);
    }

    protected void updateMessage() {
        int percent = (int)(this.value * 80.0) + 10;
        this.setMessage(Component.literal((String)("Cut prices by: " + percent + "%")));
    }

    protected void applyValue() {
        int percent = (int)(this.value * 80.0) + 10;
        AutoParser parser = AutoParser.getInstance();
        if (parser != null) {
            parser.setDiscountPercent(percent);
        }
    }
}

