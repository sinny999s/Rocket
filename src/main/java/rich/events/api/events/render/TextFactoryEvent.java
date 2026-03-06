
package rich.events.api.events.render;

import lombok.Generated;
import rich.events.api.events.Event;

public class TextFactoryEvent
implements Event {
    private String text;

    public void replaceText(String protect, String replaced) {
        if (this.text == null || this.text.isEmpty()) {
            return;
        }
        if (this.text.contains(protect) && (this.text.equalsIgnoreCase(protect) || this.text.contains(protect + " ") || this.text.contains(" " + protect) || this.text.contains("\u23cf" + protect) || this.text.contains(protect + "\u00a7"))) {
            this.text = this.text.replace(protect, replaced);
        }
    }

    @Generated
    public void setText(String text) {
        this.text = text;
    }

    @Generated
    public String getText() {
        return this.text;
    }

    @Generated
    public TextFactoryEvent(String text) {
        this.text = text;
    }
}

