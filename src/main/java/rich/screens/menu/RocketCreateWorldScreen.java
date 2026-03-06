package rich.screens.menu;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;

public class RocketCreateWorldScreen extends Screen {

    private final Screen parent;
    private long screenStartTime = 0L;
    private long lastRenderTime = 0L;
    private boolean initialized = false;

    // Tab system
    private int activeTab = 0; // 0=Game, 1=World, 2=More
    private static final String[] TAB_NAMES = {"Game", "World", "More"};

    // Game tab fields
    private String worldName = "New World";
    private boolean worldNameFocused = false;
    private int gameModeIndex = 0; // 0=Survival, 1=Creative, 2=Hardcore, 3=Adventure
    private static final String[] GAME_MODES = {"Survival", "Creative", "Hardcore", "Adventure"};
    private int difficultyIndex = 2; // 0=Peaceful, 1=Easy, 2=Normal, 3=Hard
    private static final String[] DIFFICULTIES = {"Peaceful", "Easy", "Normal", "Hard"};
    private boolean allowCommands = false;

    // World tab fields
    private String seedText = "";
    private boolean seedFieldFocused = false;
    private boolean generateStructures = true;
    private boolean bonusChest = false;

    public RocketCreateWorldScreen(Screen parent) {
        super(Component.literal("Create New World"));
        this.parent = parent;
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
            this.screenStartTime = currentTime;
            this.lastRenderTime = currentTime;
            this.initialized = true;
        }

        float scaledMouseX = this.toFixedCoord(mouseX);
        float scaledMouseY = this.toFixedCoord(mouseY);
        int fixedWidth = this.getFixedScaledWidth();
        int fixedHeight = this.getFixedScaledHeight();

        Render2D.beginOverlay();
        Render2D.backgroundImage(1.0f);

        float panelWidth = 280.0f;
        float panelHeight = 200.0f;
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

        // Tabs in header
        float tabWidth = (width - 20.0f) / 3.0f;
        float tabX = x + 5.0f;
        float tabY = y + 4.0f;
        float tabHeight = 14.0f;

        for (int i = 0; i < 3; i++) {
            float tx = tabX + i * (tabWidth + 2.5f);
            boolean tabHovered = this.isMouseOver(mouseX, mouseY, tx, tabY, tabWidth, tabHeight);
            boolean isActive = (i == this.activeTab);
            this.renderTab(tx, tabY, tabWidth, tabHeight, TAB_NAMES[i], isActive, tabHovered);
        }

        // Content area
        float contentX = x + 10.0f;
        float contentY = y + 28.0f;
        float contentWidth = width - 20.0f;

        switch (this.activeTab) {
            case 0:
                this.renderGameTab(contentX, contentY, contentWidth, mouseX, mouseY, currentTime);
                break;
            case 1:
                this.renderWorldTab(contentX, contentY, contentWidth, mouseX, mouseY, currentTime);
                break;
            case 2:
                this.renderMoreTab(contentX, contentY, contentWidth, mouseX, mouseY);
                break;
        }

        // Bottom buttons
        float btnWidth = (width - 30.0f) / 2.0f;
        float btnHeight = 18.0f;
        float btnY = y + height - btnHeight - 8.0f;

        float createBtnX = x + 10.0f;
        boolean createHovered = this.isMouseOver(mouseX, mouseY, createBtnX, btnY, btnWidth, btnHeight);
        this.renderCreateButton(createBtnX, btnY, btnWidth, btnHeight, createHovered);

        float cancelBtnX = createBtnX + btnWidth + 10.0f;
        boolean cancelHovered = this.isMouseOver(mouseX, mouseY, cancelBtnX, btnY, btnWidth, btnHeight);
        this.renderActionButton(cancelBtnX, btnY, btnWidth, btnHeight, "Cancel", cancelHovered);
    }

    private void renderGameTab(float x, float y, float width, float mouseX, float mouseY, long currentTime) {
        int titleAlpha = 255;
        int labelAlpha = 155;

        // World Name
        Fonts.REGULARNEW.draw("World Name", x, y, 5.5f, this.withAlpha(0xFFFFFF, labelAlpha));
        float fieldY = y + 10.0f;
        float fieldHeight = 16.0f;
        this.renderTextField(x, fieldY, width, fieldHeight, this.worldName, this.worldNameFocused, "Enter world name...", currentTime);

        // Game Mode
        float modeY = fieldY + fieldHeight + 12.0f;
        boolean modeHovered = this.isMouseOver(mouseX, mouseY, x, modeY, width, 18.0f);
        this.renderCycleButton(x, modeY, width, 18.0f, "Game Mode: " + GAME_MODES[this.gameModeIndex], modeHovered);

        // Difficulty
        float diffY = modeY + 24.0f;
        boolean diffHovered = this.isMouseOver(mouseX, mouseY, x, diffY, width, 18.0f);
        this.renderCycleButton(x, diffY, width, 18.0f, "Difficulty: " + DIFFICULTIES[this.difficultyIndex], diffHovered);

        // Allow Commands
        float cmdY = diffY + 24.0f;
        boolean cmdHovered = this.isMouseOver(mouseX, mouseY, x, cmdY, width, 18.0f);
        this.renderToggleButton(x, cmdY, width, 18.0f, "Allow Commands", this.allowCommands, cmdHovered);
    }

    private void renderWorldTab(float x, float y, float width, float mouseX, float mouseY, long currentTime) {
        int labelAlpha = 155;

        // Seed
        Fonts.REGULARNEW.draw("Seed for the world generator", x, y, 5.5f, this.withAlpha(0xFFFFFF, labelAlpha));
        float fieldY = y + 10.0f;
        float fieldHeight = 16.0f;
        this.renderTextField(x, fieldY, width, fieldHeight, this.seedText, this.seedFieldFocused, "Leave blank for a random seed", currentTime);

        // Generate Structures
        float structY = fieldY + fieldHeight + 12.0f;
        boolean structHovered = this.isMouseOver(mouseX, mouseY, x, structY, width, 18.0f);
        this.renderToggleButton(x, structY, width, 18.0f, "Generate Structures", this.generateStructures, structHovered);

        // Bonus Chest
        float bonusY = structY + 24.0f;
        boolean bonusHovered = this.isMouseOver(mouseX, mouseY, x, bonusY, width, 18.0f);
        this.renderToggleButton(x, bonusY, width, 18.0f, "Bonus Chest", this.bonusChest, bonusHovered);
    }

    private void renderMoreTab(float x, float y, float width, float mouseX, float mouseY) {
        // Game Rules button
        float btnHeight = 18.0f;
        boolean rulesHovered = this.isMouseOver(mouseX, mouseY, x, y, width, btnHeight);
        this.renderActionButton(x, y, width, btnHeight, "Game Rules", rulesHovered);

        // Experiments button
        float expY = y + btnHeight + 6.0f;
        boolean expHovered = this.isMouseOver(mouseX, mouseY, x, expY, width, btnHeight);
        this.renderActionButton(x, expY, width, btnHeight, "Experiments", expHovered);

        // Data Packs button
        float dpY = expY + btnHeight + 6.0f;
        boolean dpHovered = this.isMouseOver(mouseX, mouseY, x, dpY, width, btnHeight);
        this.renderActionButton(x, dpY, width, btnHeight, "Data Packs", dpHovered);
    }

    private void renderTab(float x, float y, float width, float height, String text, boolean active, boolean hovered) {
        int alpha = active ? 200 : (hovered ? 160 : 120);
        int topLeft, topRight, bottomLeft, bottomRight;
        if (active) {
            topLeft = this.withAlpha(1711912, alpha);
            topRight = this.withAlpha(1975085, alpha);
            bottomLeft = this.withAlpha(1316895, alpha);
            bottomRight = this.withAlpha(1711912, alpha);
        } else {
            topLeft = this.withAlpha(1185052, alpha);
            topRight = this.withAlpha(1448482, alpha);
            bottomLeft = this.withAlpha(921622, alpha);
            bottomRight = this.withAlpha(1185052, alpha);
        }
        int[] colors = new int[]{topLeft, topRight, bottomRight, bottomLeft};
        Render2D.gradientRect(x, y, width, height, colors, 3.0f);
        if (active) {
            Render2D.outline(x, y, width, height, 0.5f, this.withAlpha(3820122, 150), 3.0f);
        }
        int textColor = active ? this.withAlpha(0xFFFFFF, 255) : (hovered ? this.withAlpha(0xDDDDDD, 255) : this.withAlpha(0xAAAAAA, 255));
        Fonts.DEFAULT.drawCentered(text, x + width / 2.0f, y + height / 2.0f - 3.0f, 5.5f, textColor);
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
        Fonts.DEFAULT.drawCentered(text, x + width / 2.0f, y + height / 2.0f - 3.0f, 5.5f, textColor);
    }

    private void renderToggleButton(float x, float y, float width, float height, String text, boolean enabled, boolean hovered) {
        int btnAlpha = hovered ? 200 : 140;
        int topLeft = hovered ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int topRight = hovered ? this.withAlpha(1975085, btnAlpha) : this.withAlpha(1579812, btnAlpha);
        int bottomLeft = hovered ? this.withAlpha(1316895, btnAlpha) : this.withAlpha(1053466, btnAlpha);
        int bottomRight = hovered ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int[] colors = new int[]{topLeft, topRight, bottomRight, bottomLeft};
        Render2D.gradientRect(x, y, width, height, colors, 3.0f);
        int outlineColor = hovered ? this.withAlpha(3820122, 150) : this.withAlpha(2435638, 100);
        Render2D.outline(x, y, width, height, 0.5f, outlineColor, 3.0f);

        String label = text + ": ";
        String stateText = enabled ? "ON" : "OFF";
        int labelColor = hovered ? this.withAlpha(0xFFFFFF, 255) : this.withAlpha(13687012, 255);
        int stateColor = enabled ? this.withAlpha(0x55FF55, 255) : this.withAlpha(0xFF5555, 255);
        float labelWidth = Fonts.DEFAULT.getWidth(label, 5.5f);
        float totalWidth = labelWidth + Fonts.DEFAULT.getWidth(stateText, 5.5f);
        float startX = x + width / 2.0f - totalWidth / 2.0f;
        float textY = y + height / 2.0f - 3.0f;
        Fonts.DEFAULT.draw(label, startX, textY, 5.5f, labelColor);
        Fonts.DEFAULT.draw(stateText, startX + labelWidth, textY, 5.5f, stateColor);
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

    private void renderCreateButton(float x, float y, float width, float height, boolean hovered) {
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
        Fonts.DEFAULT.drawCentered("Create New World", x + width / 2.0f, y + height / 2.0f - 3.0f, 5.5f, textColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);

        float scaledMouseX = this.toFixedCoord(click.x());
        float scaledMouseY = this.toFixedCoord(click.y());
        int fixedWidth = this.getFixedScaledWidth();
        int fixedHeight = this.getFixedScaledHeight();

        float panelWidth = 280.0f;
        float panelHeight = 200.0f;
        float panelX = (float) fixedWidth / 2.0f - panelWidth / 2.0f;
        float panelY = (float) fixedHeight / 2.0f - panelHeight / 2.0f;

        // Tab clicks
        float tabWidth = (panelWidth - 20.0f) / 3.0f;
        float tabX = panelX + 5.0f;
        float tabY = panelY + 4.0f;
        float tabHeight = 14.0f;
        for (int i = 0; i < 3; i++) {
            float tx = tabX + i * (tabWidth + 2.5f);
            if (this.isMouseOver(scaledMouseX, scaledMouseY, tx, tabY, tabWidth, tabHeight)) {
                this.activeTab = i;
                this.worldNameFocused = false;
                this.seedFieldFocused = false;
                return true;
            }
        }

        float contentX = panelX + 10.0f;
        float contentY = panelY + 28.0f;
        float contentWidth = panelWidth - 20.0f;

        // Content clicks per tab
        switch (this.activeTab) {
            case 0:
                return this.handleGameTabClick(contentX, contentY, contentWidth, scaledMouseX, scaledMouseY, panelX, panelY, panelWidth, panelHeight);
            case 1:
                return this.handleWorldTabClick(contentX, contentY, contentWidth, scaledMouseX, scaledMouseY, panelX, panelY, panelWidth, panelHeight);
            case 2:
                return this.handleMoreTabClick(contentX, contentY, contentWidth, scaledMouseX, scaledMouseY, panelX, panelY, panelWidth, panelHeight);
        }

        return this.handleBottomButtons(panelX, panelY, panelWidth, panelHeight, scaledMouseX, scaledMouseY);
    }

    private boolean handleGameTabClick(float x, float y, float width, float mouseX, float mouseY,
                                        float panelX, float panelY, float panelWidth, float panelHeight) {
        // World name field
        float fieldY = y + 10.0f;
        float fieldHeight = 16.0f;
        if (this.isMouseOver(mouseX, mouseY, x, fieldY, width, fieldHeight)) {
            this.worldNameFocused = true;
            this.seedFieldFocused = false;
            return true;
        }
        this.worldNameFocused = false;

        // Game Mode
        float modeY = fieldY + fieldHeight + 12.0f;
        if (this.isMouseOver(mouseX, mouseY, x, modeY, width, 18.0f)) {
            this.gameModeIndex = (this.gameModeIndex + 1) % GAME_MODES.length;
            return true;
        }

        // Difficulty
        float diffY = modeY + 24.0f;
        if (this.isMouseOver(mouseX, mouseY, x, diffY, width, 18.0f)) {
            this.difficultyIndex = (this.difficultyIndex + 1) % DIFFICULTIES.length;
            return true;
        }

        // Allow Commands
        float cmdY = diffY + 24.0f;
        if (this.isMouseOver(mouseX, mouseY, x, cmdY, width, 18.0f)) {
            this.allowCommands = !this.allowCommands;
            return true;
        }

        return this.handleBottomButtons(panelX, panelY, panelWidth, panelHeight, mouseX, mouseY);
    }

    private boolean handleWorldTabClick(float x, float y, float width, float mouseX, float mouseY,
                                         float panelX, float panelY, float panelWidth, float panelHeight) {
        // Seed field
        float fieldY = y + 10.0f;
        float fieldHeight = 16.0f;
        if (this.isMouseOver(mouseX, mouseY, x, fieldY, width, fieldHeight)) {
            this.seedFieldFocused = true;
            this.worldNameFocused = false;
            return true;
        }
        this.seedFieldFocused = false;

        // Generate Structures
        float structY = fieldY + fieldHeight + 12.0f;
        if (this.isMouseOver(mouseX, mouseY, x, structY, width, 18.0f)) {
            this.generateStructures = !this.generateStructures;
            return true;
        }

        // Bonus Chest
        float bonusY = structY + 24.0f;
        if (this.isMouseOver(mouseX, mouseY, x, bonusY, width, 18.0f)) {
            this.bonusChest = !this.bonusChest;
            return true;
        }

        return this.handleBottomButtons(panelX, panelY, panelWidth, panelHeight, mouseX, mouseY);
    }

    private boolean handleMoreTabClick(float x, float y, float width, float mouseX, float mouseY,
                                        float panelX, float panelY, float panelWidth, float panelHeight) {
        // More tab buttons are informational for now - could open vanilla sub-screens
        return this.handleBottomButtons(panelX, panelY, panelWidth, panelHeight, mouseX, mouseY);
    }

    private boolean handleBottomButtons(float panelX, float panelY, float panelWidth, float panelHeight,
                                         float mouseX, float mouseY) {
        float btnWidth = (panelWidth - 30.0f) / 2.0f;
        float btnHeight = 18.0f;
        float btnY = panelY + panelHeight - btnHeight - 8.0f;

        // Create button
        float createBtnX = panelX + 10.0f;
        if (this.isMouseOver(mouseX, mouseY, createBtnX, btnY, btnWidth, btnHeight)) {
            this.createWorld();
            return true;
        }

        // Cancel button
        float cancelBtnX = createBtnX + btnWidth + 10.0f;
        if (this.isMouseOver(mouseX, mouseY, cancelBtnX, btnY, btnWidth, btnHeight)) {
            this.minecraft.setScreen(this.parent);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int keyCode = input.key();

        if (keyCode == 256) { // Escape
            this.minecraft.setScreen(this.parent);
            return true;
        }

        if (this.worldNameFocused) {
            if (keyCode == 259) { // Backspace
                if (!this.worldName.isEmpty()) {
                    this.worldName = this.worldName.substring(0, this.worldName.length() - 1);
                }
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // Enter
                this.worldNameFocused = false;
                return true;
            }
        }

        if (this.seedFieldFocused) {
            if (keyCode == 259) { // Backspace
                if (!this.seedText.isEmpty()) {
                    this.seedText = this.seedText.substring(0, this.seedText.length() - 1);
                }
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // Enter
                this.seedFieldFocused = false;
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharacterEvent input) {
        int codepoint = input.codepoint();

        if (this.worldNameFocused) {
            if (this.worldName.length() < 40 && codepoint >= 32) {
                this.worldName = this.worldName + Character.toString(codepoint);
                return true;
            }
        }

        if (this.seedFieldFocused) {
            if (this.seedText.length() < 32 && (Character.isLetterOrDigit(codepoint) || codepoint == '-' || codepoint == ' ')) {
                this.seedText = this.seedText + Character.toString(codepoint);
                return true;
            }
        }

        return super.charTyped(input);
    }

    private void createWorld() {
        try {
            // Open vanilla CreateWorldScreen which handles all the complex world gen setup
            // Pass our configured settings via the screen
            net.minecraft.client.gui.screens.worldselection.CreateWorldScreen.openFresh(this.minecraft, () -> this.minecraft.setScreen(this.parent));
        } catch (Exception e) {
            this.minecraft.setScreen(this.parent);
        }
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
