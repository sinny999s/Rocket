package rich.screens.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class RocketAddServerScreen extends Screen {

    private final Screen parent;
    private final ServerData serverData;
    private final java.util.function.Consumer<ServerData> onDone;
    private boolean initialized = false;

    private String serverName = "";
    private String serverAddress = "";
    private boolean nameFieldFocused = true;
    private boolean addressFieldFocused = false;
    private int resourcePackPolicy = 0; // 0=Prompt, 1=Enabled, 2=Disabled
    private static final String[] RESOURCE_PACK_OPTIONS = {"Prompt", "Enabled", "Disabled"};

    public RocketAddServerScreen(Screen parent, ServerData serverData, java.util.function.Consumer<ServerData> onDone) {
        super(Component.literal("Add Server"));
        this.parent = parent;
        this.serverData = serverData;
        this.onDone = onDone;
        if (serverData != null) {
            this.serverName = serverData.name != null ? serverData.name : "";
            this.serverAddress = serverData.ip != null ? serverData.ip : "";
        }
    }

    @Override
    protected void init() {
        this.initialized = false;
    }

    private int getFixedScaledWidth() {
        return (int) Math.ceil((double) this.minecraft.getWindow().getWidth() / 2.0);
    }

    private int getFixedScaledHeight() {
        return (int) Math.ceil((double) this.minecraft.getWindow().getHeight() / 2.0);
    }

    private float toFixedCoord(double coord) {
        float currentScale = this.minecraft.getWindow().getGuiScale();
        return (float) (coord * (double) currentScale / 2.0);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        long currentTime = Util.getMillis();
        if (!this.initialized) {
            this.initialized = true;
        }

        float scaledMouseX = this.toFixedCoord(mouseX);
        float scaledMouseY = this.toFixedCoord(mouseY);
        int fixedWidth = this.getFixedScaledWidth();
        int fixedHeight = this.getFixedScaledHeight();

        Render2D.beginOverlay();
        Render2D.backgroundImage(1.0f);

        float panelWidth = 220.0f;
        float panelHeight = 160.0f;
        float panelX = (float) fixedWidth / 2.0f - panelWidth / 2.0f;
        float panelY = (float) fixedHeight / 2.0f - panelHeight / 2.0f;

        this.renderPanel(panelX, panelY, panelWidth, panelHeight, scaledMouseX, scaledMouseY, currentTime);

        Render2D.endOverlay();
    }

    private void renderPanel(float x, float y, float width, float height, float mouseX, float mouseY, long currentTime) {
        int bgAlpha = 120;
        int headerAlpha = 150;
        int outlineAlpha = 100;
        int blurAlpha = 80;
        int titleAlpha = 255;
        int labelAlpha = 155;

        int bgTopLeft = this.withAlpha(855828, bgAlpha);
        int bgTopRight = this.withAlpha(1053208, bgAlpha);
        int bgBottomLeft = this.withAlpha(526604, bgAlpha);
        int bgBottomRight = this.withAlpha(855828, bgAlpha);
        int headerTopLeft = this.withAlpha(1316639, headerAlpha);
        int headerTopRight = this.withAlpha(1579812, headerAlpha);
        int headerBottomLeft = this.withAlpha(1053466, headerAlpha);
        int headerBottomRight = this.withAlpha(1316639, headerAlpha);
        int outlineColor = this.withAlpha(2435638, outlineAlpha);
        int blurTint = this.withAlpha(395280, blurAlpha);

        Render2D.blur(x, y, width, height, 15.0f, 6.0f, blurTint);
        int[] bgColors = new int[]{bgTopLeft, bgTopRight, bgBottomRight, bgBottomLeft};
        Render2D.gradientRect(x, y, width, height, bgColors, 6.0f);
        int[] headerColors = new int[]{headerTopLeft, headerTopRight, headerBottomRight, headerBottomLeft};
        Render2D.gradientRect(x, y, width, 22.0f, headerColors, 6.0f, 6.0f, 0.0f, 0.0f);
        Render2D.outline(x, y, width, height, 1.0f, outlineColor, 6.0f);
        Fonts.BOLD.drawCentered("Add Server", x + width / 2.0f, y + 6.0f, 8.0f, this.withAlpha(0xFFFFFF, titleAlpha));

        float contentX = x + 10.0f;
        float contentWidth = width - 20.0f;

        // Server Name
        float nameY = y + 28.0f;
        Fonts.REGULARNEW.draw("Server Name", contentX, nameY, 5.0f, this.withAlpha(0xFFFFFF, labelAlpha));
        float nameFieldY = nameY + 9.0f;
        this.renderTextField(contentX, nameFieldY, contentWidth, 16.0f, this.serverName, this.nameFieldFocused, "Minecraft Server", currentTime);

        // Server Address
        float addrLabelY = nameFieldY + 22.0f;
        Fonts.REGULARNEW.draw("Server Address", contentX, addrLabelY, 5.0f, this.withAlpha(0xFFFFFF, labelAlpha));
        float addrFieldY = addrLabelY + 9.0f;
        this.renderTextField(contentX, addrFieldY, contentWidth, 16.0f, this.serverAddress, this.addressFieldFocused, "play.example.com", currentTime);

        // Resource Packs
        float rpY = addrFieldY + 22.0f;
        boolean rpHovered = this.isMouseOver(mouseX, mouseY, contentX, rpY, contentWidth, 18.0f);
        this.renderCycleButton(contentX, rpY, contentWidth, 18.0f, "Server Resource Packs: " + RESOURCE_PACK_OPTIONS[this.resourcePackPolicy], rpHovered);

        // Bottom buttons
        float btnWidth = (contentWidth - 6.0f) / 2.0f;
        float btnHeight = 18.0f;
        float btnY = y + height - btnHeight - 8.0f;

        float doneBtnX = contentX;
        boolean doneHovered = this.isMouseOver(mouseX, mouseY, doneBtnX, btnY, btnWidth, btnHeight);
        this.renderDoneButton(doneBtnX, btnY, btnWidth, btnHeight, doneHovered, !this.serverAddress.isEmpty());

        float cancelBtnX = doneBtnX + btnWidth + 6.0f;
        boolean cancelHovered = this.isMouseOver(mouseX, mouseY, cancelBtnX, btnY, btnWidth, btnHeight);
        this.renderActionButton(cancelBtnX, btnY, btnWidth, btnHeight, "Cancel", cancelHovered);
    }

    private void renderTextField(float x, float y, float width, float height, String text, boolean focused, String placeholder, long currentTime) {
        int fieldBgAlpha = 180;
        int fieldOutlineAlpha = focused ? 180 : 80;
        int fieldBgTop = this.withAlpha(658448, fieldBgAlpha);
        int fieldBgBottom = this.withAlpha(526862, fieldBgAlpha);
        int[] fieldBgColors = new int[]{fieldBgTop, fieldBgTop, fieldBgBottom, fieldBgBottom};
        Render2D.gradientRect(x, y, width, height, fieldBgColors, 3.0f);
        int fieldOutlineColor = focused ? this.withAlpha(3820122, fieldOutlineAlpha) : this.withAlpha(2435638, fieldOutlineAlpha);
        Render2D.outline(x, y, width, height, 0.5f, fieldOutlineColor, 3.0f);
        String displayText = text.isEmpty() && !focused ? placeholder : text;
        int textColor = text.isEmpty() && !focused ? this.withAlpha(6318200, 155) : this.withAlpha(13685980, 255);
        Fonts.TEST.draw(displayText, x + 4.0f, y + 5.0f, 5.5f, textColor);
        if (focused && currentTime / 500L % 2L == 0L) {
            float cursorX = x + 4.0f + Fonts.TEST.getWidth(text, 5.5f);
            Render2D.rect(cursorX, y + 3.0f, 0.5f, height - 6.0f, this.withAlpha(13685980, 255), 0.0f);
        }
    }

    private void renderCycleButton(float x, float y, float width, float height, String text, boolean hovered) {
        int btnAlpha = hovered ? 200 : 140;
        int topLeft = hovered ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int topRight = hovered ? this.withAlpha(1975085, btnAlpha) : this.withAlpha(1579812, btnAlpha);
        int bottomLeft = hovered ? this.withAlpha(1316895, btnAlpha) : this.withAlpha(1053466, btnAlpha);
        int bottomRight = hovered ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int[] colors = new int[]{topLeft, topRight, bottomRight, bottomLeft};
        Render2D.gradientRect(x, y, width, height, colors, 3.0f);
        int outlineColor = hovered ? this.withAlpha(3820122, 150) : this.withAlpha(2435638, 100);
        Render2D.outline(x, y, width, height, 0.5f, outlineColor, 3.0f);
        int textColor = hovered ? this.withAlpha(0xFFFFFF, 255) : this.withAlpha(13687012, 255);
        Fonts.DEFAULT.drawCentered(text, x + width / 2.0f, y + height / 2.0f - 3.0f, 5.0f, textColor);
    }

    private void renderDoneButton(float x, float y, float width, float height, boolean hovered, boolean enabled) {
        if (!enabled) {
            int btnAlpha = 100;
            int[] colors = new int[]{this.withAlpha(0x141414, btnAlpha), this.withAlpha(0x181818, btnAlpha), this.withAlpha(0x141414, btnAlpha), this.withAlpha(0x101010, btnAlpha)};
            Render2D.gradientRect(x, y, width, height, colors, 3.0f);
            Render2D.outline(x, y, width, height, 0.5f, this.withAlpha(2435638, 60), 3.0f);
            Fonts.DEFAULT.drawCentered("Done", x + width / 2.0f, y + height / 2.0f - 3.0f, 5.5f, this.withAlpha(0x666666, 255));
            return;
        }
        int btnAlpha = hovered ? 220 : 160;
        int topLeft = hovered ? this.withAlpha(0x1A3A1A, btnAlpha) : this.withAlpha(0x142E14, btnAlpha);
        int topRight = hovered ? this.withAlpha(0x1E3E1E, btnAlpha) : this.withAlpha(0x183218, btnAlpha);
        int bottomLeft = hovered ? this.withAlpha(0x143414, btnAlpha) : this.withAlpha(0x102810, btnAlpha);
        int bottomRight = hovered ? this.withAlpha(0x1A3A1A, btnAlpha) : this.withAlpha(0x142E14, btnAlpha);
        int[] colors = new int[]{topLeft, topRight, bottomRight, bottomLeft};
        Render2D.gradientRect(x, y, width, height, colors, 3.0f);
        int outlineColor = hovered ? this.withAlpha(0x3A6A3A, 180) : this.withAlpha(0x2A4A2A, 120);
        Render2D.outline(x, y, width, height, 0.5f, outlineColor, 3.0f);
        int textColor = hovered ? this.withAlpha(0x80FF80, 255) : this.withAlpha(0x60CC60, 255);
        Fonts.DEFAULT.drawCentered("Done", x + width / 2.0f, y + height / 2.0f - 3.0f, 5.5f, textColor);
    }

    private void renderActionButton(float x, float y, float width, float height, String text, boolean hovered) {
        int btnAlpha = hovered ? 200 : 140;
        int topLeft = hovered ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int topRight = hovered ? this.withAlpha(1975085, btnAlpha) : this.withAlpha(1579812, btnAlpha);
        int bottomLeft = hovered ? this.withAlpha(1316895, btnAlpha) : this.withAlpha(1053466, btnAlpha);
        int bottomRight = hovered ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int[] colors = new int[]{topLeft, topRight, bottomRight, bottomLeft};
        Render2D.gradientRect(x, y, width, height, colors, 3.0f);
        int outlineColor = hovered ? this.withAlpha(3820122, 150) : this.withAlpha(2435638, 100);
        Render2D.outline(x, y, width, height, 0.5f, outlineColor, 3.0f);
        int textColor = hovered ? this.withAlpha(0xFFFFFF, 255) : this.withAlpha(13687012, 255);
        Fonts.DEFAULT.drawCentered(text, x + width / 2.0f, y + height / 2.0f - 3.0f, 5.5f, textColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);

        float scaledMouseX = this.toFixedCoord(click.x());
        float scaledMouseY = this.toFixedCoord(click.y());
        int fixedWidth = this.getFixedScaledWidth();
        int fixedHeight = this.getFixedScaledHeight();

        float panelWidth = 220.0f;
        float panelHeight = 160.0f;
        float panelX = (float) fixedWidth / 2.0f - panelWidth / 2.0f;
        float panelY = (float) fixedHeight / 2.0f - panelHeight / 2.0f;

        float contentX = panelX + 10.0f;
        float contentWidth = panelWidth - 20.0f;

        // Name field click
        float nameFieldY = panelY + 28.0f + 9.0f;
        if (this.isMouseOver(scaledMouseX, scaledMouseY, contentX, nameFieldY, contentWidth, 16.0f)) {
            this.nameFieldFocused = true;
            this.addressFieldFocused = false;
            return true;
        }

        // Address field click
        float addrFieldY = nameFieldY + 22.0f + 9.0f;
        if (this.isMouseOver(scaledMouseX, scaledMouseY, contentX, addrFieldY, contentWidth, 16.0f)) {
            this.addressFieldFocused = true;
            this.nameFieldFocused = false;
            return true;
        }

        this.nameFieldFocused = false;
        this.addressFieldFocused = false;

        // Resource packs cycle
        float rpY = addrFieldY + 22.0f;
        if (this.isMouseOver(scaledMouseX, scaledMouseY, contentX, rpY, contentWidth, 18.0f)) {
            this.resourcePackPolicy = (this.resourcePackPolicy + 1) % RESOURCE_PACK_OPTIONS.length;
            return true;
        }

        // Bottom buttons
        float btnWidth = (contentWidth - 6.0f) / 2.0f;
        float btnHeight = 18.0f;
        float btnY = panelY + panelHeight - btnHeight - 8.0f;

        // Done
        if (this.isMouseOver(scaledMouseX, scaledMouseY, contentX, btnY, btnWidth, btnHeight)) {
            if (!this.serverAddress.isEmpty()) {
                this.done();
            }
            return true;
        }

        // Cancel
        float cancelBtnX = contentX + btnWidth + 6.0f;
        if (this.isMouseOver(scaledMouseX, scaledMouseY, cancelBtnX, btnY, btnWidth, btnHeight)) {
            this.minecraft.setScreen(this.parent);
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int keyCode = input.key();

        if (keyCode == 256) { // Escape
            this.minecraft.setScreen(this.parent);
            return true;
        }

        if (keyCode == 258) { // Tab - switch focus
            if (this.nameFieldFocused) {
                this.nameFieldFocused = false;
                this.addressFieldFocused = true;
            } else {
                this.nameFieldFocused = true;
                this.addressFieldFocused = false;
            }
            return true;
        }

        if (this.nameFieldFocused) {
            if (keyCode == 259) { // Backspace
                if (!this.serverName.isEmpty()) {
                    this.serverName = this.serverName.substring(0, this.serverName.length() - 1);
                }
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // Enter
                this.nameFieldFocused = false;
                this.addressFieldFocused = true;
                return true;
            }
        }

        if (this.addressFieldFocused) {
            if (keyCode == 259) { // Backspace
                if (!this.serverAddress.isEmpty()) {
                    this.serverAddress = this.serverAddress.substring(0, this.serverAddress.length() - 1);
                }
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // Enter
                if (!this.serverAddress.isEmpty()) {
                    this.done();
                }
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        int codepoint = input.codepoint();

        if (this.nameFieldFocused && this.serverName.length() < 48 && codepoint >= 32) {
            this.serverName = this.serverName + Character.toString(codepoint);
            return true;
        }

        if (this.addressFieldFocused && this.serverAddress.length() < 128 && codepoint >= 32) {
            this.serverAddress = this.serverAddress + Character.toString(codepoint);
            return true;
        }

        return super.charTyped(input);
    }

    private void done() {
        ServerData data = this.serverData != null ? this.serverData : new ServerData(this.serverName, this.serverAddress, ServerData.Type.OTHER);
        data.name = this.serverName.isEmpty() ? "Minecraft Server" : this.serverName;
        data.ip = this.serverAddress;
        if (this.onDone != null) {
            this.onDone.accept(data);
        }
        this.minecraft.setScreen(this.parent);
    }

    private boolean isMouseOver(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | Mth.clamp(alpha, 0, 255) << 24;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
    }
}
