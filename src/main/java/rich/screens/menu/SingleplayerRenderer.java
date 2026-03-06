package rich.screens.menu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import rich.util.ColorUtil;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class SingleplayerRenderer {

    private List<LevelSummary> worlds = new ArrayList<>();
    private boolean loading = true;
    private int selectedIndex = -1;
    private String deleteConfirmWorld = null;

    public void loadWorlds() {
        this.loading = true;
        this.worlds.clear();
        this.selectedIndex = -1;
        this.deleteConfirmWorld = null;
        CompletableFuture.runAsync(() -> {
            try {
                Minecraft mc = Minecraft.getInstance();
                LevelStorageSource source = mc.getLevelSource();
                LevelStorageSource.LevelCandidates candidates = source.findLevelCandidates();
                CompletableFuture<List<LevelSummary>> future = source.loadLevelSummaries(candidates);
                List<LevelSummary> summaries = future.join();
                List<LevelSummary> filtered = new ArrayList<>();
                for (LevelSummary s : summaries) {
                    if (!s.isDisabled()) {
                        filtered.add(s);
                    }
                }
                filtered.sort((a, b) -> Long.compare(b.getLastPlayed(), a.getLastPlayed()));
                synchronized (this) {
                    this.worlds = filtered;
                    this.loading = false;
                    if (!this.worlds.isEmpty()) {
                        this.selectedIndex = 0;
                    }
                }
            } catch (Exception e) {
                synchronized (this) {
                    this.loading = false;
                }
            }
        });
    }

    public synchronized List<LevelSummary> getWorlds() {
        return this.worlds;
    }

    public synchronized int getSelectedIndex() {
        return this.selectedIndex;
    }

    public synchronized void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }

    public synchronized boolean isLoading() {
        return this.loading;
    }

    public synchronized LevelSummary getSelectedWorld() {
        if (this.selectedIndex >= 0 && this.selectedIndex < this.worlds.size()) {
            return this.worlds.get(this.selectedIndex);
        }
        return null;
    }

    public String getDeleteConfirmWorld() {
        return this.deleteConfirmWorld;
    }

    public void setDeleteConfirmWorld(String world) {
        this.deleteConfirmWorld = world;
    }

    public void renderLeftPanel(float x, float y, float width, float height, float contentAlpha, float mouseX, float mouseY) {
        int bgAlpha = (int)(contentAlpha * 120.0f);
        int headerAlpha = (int)(contentAlpha * 150.0f);
        int outlineAlpha = (int)(contentAlpha * 100.0f);
        int titleAlpha = (int)(contentAlpha * 255.0f);
        int titleTextAlpha = (int)(contentAlpha * 155.0f);

        int bgTopLeft = this.withAlpha(855828, bgAlpha);
        int bgTopRight = this.withAlpha(1053208, bgAlpha);
        int bgBottomLeft = this.withAlpha(526604, bgAlpha);
        int bgBottomRight = this.withAlpha(855828, bgAlpha);
        int headerTopLeft = this.withAlpha(1316639, headerAlpha);
        int headerTopRight = this.withAlpha(1579812, headerAlpha);
        int headerBottomLeft = this.withAlpha(1053466, headerAlpha);
        int headerBottomRight = this.withAlpha(1316639, headerAlpha);
        int outlineColor = this.withAlpha(2435638, outlineAlpha);

        int[] bgColors = new int[]{bgTopLeft, bgTopRight, bgBottomRight, bgBottomLeft};
        Render2D.gradientRect(x, y, width, height, bgColors, 6.0f);
        int[] headerColors = new int[]{headerTopLeft, headerTopRight, headerBottomRight, headerBottomLeft};
        Render2D.gradientRect(x, y, width, 22.0f, headerColors, 6.0f, 6.0f, 0.0f, 0.0f);
        Render2D.outline(x, y, width, height, 1.0f, outlineColor, 6.0f);
        Fonts.BOLD.drawCentered("World Info", x + width / 2.0f - 15.0f, y + 6.0f, 8.0f, this.withAlpha(0xFFFFFF, titleAlpha));

        LevelSummary selected = this.getSelectedWorld();
        if (selected != null) {
            float textX = x + 8.0f;
            float textY = y + 28.0f;

            Fonts.REGULARNEW.draw("Name", textX, textY, 5.0f, this.withAlpha(0x808890, titleAlpha));
            Fonts.TEST.draw(this.truncateText(selected.getLevelName(), width - 16.0f, 6.0f), textX, textY + 8.0f, 6.0f, this.withAlpha(0xFFFFFF, titleAlpha));

            Fonts.REGULARNEW.draw("Last Played", textX, textY + 22.0f, 5.0f, this.withAlpha(0x808890, titleAlpha));
            String dateStr = this.formatDate(selected.getLastPlayed());
            Fonts.TEST.draw(dateStr, textX, textY + 30.0f, 5.5f, this.withAlpha(0xCCCCCC, titleAlpha));

            Fonts.REGULARNEW.draw("Game Mode", textX, textY + 44.0f, 5.0f, this.withAlpha(0x808890, titleAlpha));
            String mode = this.getGameModeString(selected);
            Fonts.TEST.draw(mode, textX, textY + 52.0f, 5.5f, this.withAlpha(0xCCCCCC, titleAlpha));

            Fonts.REGULARNEW.draw("Folder", textX, textY + 66.0f, 5.0f, this.withAlpha(0x808890, titleAlpha));
            Fonts.TEST.draw(this.truncateText(selected.getLevelId(), width - 16.0f, 5.0f), textX, textY + 74.0f, 5.0f, this.withAlpha(0xAAAAAA, titleAlpha));

            // Bottom buttons
            float btnWidth = (width - 16.0f - 3.0f) / 2.0f;
            float btnHeight = 16.0f;
            float btnY = y + height - btnHeight - 5.0f;

            // Play button
            float playBtnX = x + 5.0f;
            boolean playHovered = this.isMouseOver(mouseX, mouseY, playBtnX, btnY, btnWidth, btnHeight);
            this.renderActionButton(playBtnX, btnY, btnWidth, btnHeight, "Play", contentAlpha, playHovered, false);

            // Delete button
            float delBtnX = playBtnX + btnWidth + 3.0f;
            boolean delHovered = this.isMouseOver(mouseX, mouseY, delBtnX, btnY, btnWidth, btnHeight);
            this.renderDeleteButton(delBtnX, btnY, btnWidth, btnHeight, contentAlpha, delHovered);
        } else {
            Fonts.REGULARNEW.drawCentered("No world selected", x + width / 2.0f, y + height / 2.0f, 5.0f, this.withAlpha(6318200, titleTextAlpha));
        }
    }

    public void renderRightPanel(float x, float y, float width, float height, float contentAlpha,
                                  float scrollOffset, float scaledMouseX, float scaledMouseY, float scale, int guiScale) {
        int bgAlpha = (int)(contentAlpha * 120.0f);
        int headerAlpha = (int)(contentAlpha * 150.0f);
        int outlineAlpha = (int)(contentAlpha * 100.0f);
        int blurAlpha = (int)(contentAlpha * 80.0f);
        int titleAlpha = (int)(contentAlpha * 255.0f);
        int titleTextAlpha = (int)(contentAlpha * 155.0f);

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
        Fonts.BOLD.draw("Worlds", x + 8.0f, y + 7.0f, 8.0f, this.withAlpha(0xFFFFFF, titleAlpha));

        // Create New World button in header
        float createBtnWidth = 60.0f;
        float createBtnHeight = 14.0f;
        float createBtnX = x + width - createBtnWidth - 6.0f;
        float createBtnY = y + 4.0f;
        boolean createHovered = this.isMouseOver(scaledMouseX, scaledMouseY, createBtnX, createBtnY, createBtnWidth, createBtnHeight);
        this.renderActionButton(createBtnX, createBtnY, createBtnWidth, createBtnHeight, "Create World", contentAlpha, createHovered, false);

        Render2D.blur(x, y, width, height, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 1));

        float listX = x + 5.0f;
        float listY = y + 28.0f;
        float listWidth = width - 10.0f;
        float listHeight = height - 33.0f;
        float cardHeight = 36.0f;
        float cardGap = 4.0f;
        float scissorScale = (float) guiScale / scale;

        Scissor.enable(listX * scale, listY * scale, listWidth * scale, listHeight * scale, scissorScale);

        List<LevelSummary> worldList = this.getWorlds();

        if (this.isLoading()) {
            Fonts.REGULARNEW.drawCentered("Loading worlds...", x + width / 2.0f, y + height / 2.0f + 2.0f, 6.0f, this.withAlpha(0xAAAAAA, titleAlpha));
        } else if (worldList.isEmpty()) {
            Fonts.REGULARNEW.drawCentered("No worlds found", x + width / 2.0f, y + height / 2.0f + 2.0f, 6.0f, this.withAlpha(6318200, titleTextAlpha));
        } else {
            for (int i = 0; i < worldList.size(); i++) {
                LevelSummary world = worldList.get(i);
                float cardY = listY + (float) i * (cardHeight + cardGap) - scrollOffset;
                if (cardY + cardHeight < listY - 10.0f || cardY > listY + listHeight + 10.0f) continue;
                boolean isSelected = (i == this.getSelectedIndex());
                this.renderWorldCard(listX, cardY, listWidth, cardHeight, world, contentAlpha,
                        scaledMouseX, scaledMouseY, listY, listHeight, isSelected);
            }
        }

        Scissor.disable();
    }

    private void renderWorldCard(float x, float y, float width, float height, LevelSummary world,
                                  float contentAlpha, float mouseX, float mouseY, float listY, float listHeight, boolean isSelected) {
        int titleAlpha = (int)(contentAlpha * 255.0f);
        boolean cardHovered = this.isMouseOver(mouseX, mouseY, x, y, width, height) && mouseY >= listY && mouseY <= listY + listHeight;
        int cardAlpha;
        if (isSelected) {
            cardAlpha = (int)(contentAlpha * 180.0f);
        } else if (cardHovered) {
            cardAlpha = (int)(contentAlpha * 150.0f);
        } else {
            cardAlpha = (int)(contentAlpha * 110.0f);
        }

        int cardTopLeft, cardTopRight, cardBottomLeft, cardBottomRight;
        if (isSelected) {
            cardTopLeft = this.withAlpha(1316666, cardAlpha);
            cardTopRight = this.withAlpha(1580096, cardAlpha);
            cardBottomLeft = this.withAlpha(1053236, cardAlpha);
            cardBottomRight = this.withAlpha(1316666, cardAlpha);
        } else {
            cardTopLeft = this.withAlpha(1185052, cardAlpha);
            cardTopRight = this.withAlpha(1448482, cardAlpha);
            cardBottomLeft = this.withAlpha(921622, cardAlpha);
            cardBottomRight = this.withAlpha(1185052, cardAlpha);
        }

        int[] cardColors = new int[]{cardTopLeft, cardTopRight, cardBottomRight, cardBottomLeft};
        Render2D.gradientRect(x, y, width, height, cardColors, 4.0f);
        Render2D.blur(x, y, 1.0f, 1.0f, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 0));

        int cardOutlineColor;
        if (isSelected) {
            cardOutlineColor = this.withAlpha(3820122, (int)(contentAlpha * 120.0f));
        } else {
            cardOutlineColor = this.withAlpha(2435638, (int)(contentAlpha * 80.0f));
        }
        Render2D.outline(x, y, width, height, 0.5f, cardOutlineColor, 4.0f);

        // World icon placeholder (small colored rect)
        float iconX = x + 6.0f;
        float iconY = y + 6.0f;
        float iconSize = height - 12.0f;
        int iconBg = this.withAlpha(0x1A2030, (int)(contentAlpha * 200.0f));
        Render2D.rect(iconX, iconY, iconSize, iconSize, iconBg, 3.0f);
        Render2D.outline(iconX, iconY, iconSize, iconSize, 0.5f, this.withAlpha(2435638, (int)(contentAlpha * 60.0f)), 3.0f);
        // World icon letter
        String initial = world.getLevelName().isEmpty() ? "W" : world.getLevelName().substring(0, 1).toUpperCase();
        Fonts.BOLD.drawCentered(initial, iconX + iconSize / 2.0f, iconY + iconSize / 2.0f - 4.0f, 10.0f, this.withAlpha(0x607080, titleAlpha));

        float textX = iconX + iconSize + 6.0f;
        float nameY = y + 7.0f;
        float infoY = nameY + 10.0f;
        float dateY = infoY + 9.0f;

        // World name
        String displayName = this.truncateText(world.getLevelName(), width - iconSize - 24.0f, 6.5f);
        Fonts.TEST.draw(displayName, textX, nameY, 6.5f, this.withAlpha(0xFFFFFF, titleAlpha));

        // Game mode + folder
        String mode = this.getGameModeString(world);
        String folder = world.getLevelId();
        String info = mode + " - " + folder;
        info = this.truncateTextFont(info, width - iconSize - 24.0f, 5.0f, Fonts.TEST);
        Fonts.TEST.draw(info, textX, infoY, 5.0f, this.withAlpha(0x808890, titleAlpha));

        // Last played date
        String dateStr = this.formatDate(world.getLastPlayed());
        Fonts.TEST.draw(dateStr, textX, dateY, 4.5f, this.withAlpha(0x606870, titleAlpha));
    }

    private void renderActionButton(float x, float y, float width, float height, String text,
                                     float contentAlpha, boolean hovered, boolean disabled) {
        int titleAlpha = (int)(contentAlpha * 255.0f);
        int btnAlpha = hovered && !disabled ? (int)(contentAlpha * 200.0f) : (int)(contentAlpha * 140.0f);
        int btnTopLeft = hovered && !disabled ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int btnTopRight = hovered && !disabled ? this.withAlpha(1975085, btnAlpha) : this.withAlpha(1579812, btnAlpha);
        int btnBottomLeft = hovered && !disabled ? this.withAlpha(1316895, btnAlpha) : this.withAlpha(1053466, btnAlpha);
        int btnBottomRight = hovered && !disabled ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int[] btnColors = new int[]{btnTopLeft, btnTopRight, btnBottomRight, btnBottomLeft};
        Render2D.gradientRect(x, y, width, height, btnColors, 3.0f);
        int outlineColor = hovered && !disabled ? this.withAlpha(3820122, (int)(contentAlpha * 150.0f)) : this.withAlpha(2435638, (int)(contentAlpha * 100.0f));
        Render2D.outline(x, y, width, height, 0.5f, outlineColor, 3.0f);
        int textColor = hovered && !disabled ? this.withAlpha(0xFFFFFF, titleAlpha) : this.withAlpha(13687012, titleAlpha);
        if (disabled) textColor = this.withAlpha(0x666666, titleAlpha);
        Fonts.DEFAULT.drawCentered(text, x + width / 2.0f, y + height / 2.0f - 3.0f, 5.5f, textColor);
    }

    private void renderDeleteButton(float x, float y, float width, float height, float contentAlpha, boolean hovered) {
        int titleAlpha = (int)(contentAlpha * 255.0f);
        int btnAlpha = hovered ? (int)(contentAlpha * 200.0f) : (int)(contentAlpha * 140.0f);
        int btnTopLeft = hovered ? this.withAlpha(0x2A1A1A, btnAlpha) : this.withAlpha(1709078, btnAlpha);
        int btnTopRight = hovered ? this.withAlpha(0x2E1E1E, btnAlpha) : this.withAlpha(1971736, btnAlpha);
        int btnBottomLeft = hovered ? this.withAlpha(0x241414, btnAlpha) : this.withAlpha(1445906, btnAlpha);
        int btnBottomRight = hovered ? this.withAlpha(0x2A1A1A, btnAlpha) : this.withAlpha(1709078, btnAlpha);
        int[] btnColors = new int[]{btnTopLeft, btnTopRight, btnBottomRight, btnBottomLeft};
        Render2D.gradientRect(x, y, width, height, btnColors, 3.0f);
        int outlineColor = hovered ? this.withAlpha(0x5A3A3A, (int)(contentAlpha * 150.0f)) : this.withAlpha(3484202, (int)(contentAlpha * 100.0f));
        Render2D.outline(x, y, width, height, 0.5f, outlineColor, 3.0f);
        int textColor = hovered ? this.withAlpha(0xFF8080, titleAlpha) : this.withAlpha(0xD0A0A0, titleAlpha);
        Fonts.DEFAULT.drawCentered("Delete", x + width / 2.0f, y + height / 2.0f - 3.0f, 5.5f, textColor);
    }

    public void renderDeleteConfirmDialog(float centerX, float centerY, float contentAlpha, float mouseX, float mouseY) {
        if (this.deleteConfirmWorld == null) return;

        float dialogWidth = 180.0f;
        float dialogHeight = 70.0f;
        float dialogX = centerX - dialogWidth / 2.0f;
        float dialogY = centerY - dialogHeight / 2.0f;

        int titleAlpha = (int)(contentAlpha * 255.0f);

        // Dim background
        Render2D.rect(0, 0, Render2D.getFixedScaledWidth(), Render2D.getFixedScaledHeight(), this.withAlpha(0x000000, (int)(contentAlpha * 120.0f)));

        // Dialog panel
        int bgAlpha = (int)(contentAlpha * 200.0f);
        int bgColor = this.withAlpha(0x0D1117, bgAlpha);
        Render2D.rect(dialogX, dialogY, dialogWidth, dialogHeight, bgColor, 6.0f);
        Render2D.outline(dialogX, dialogY, dialogWidth, dialogHeight, 1.0f, this.withAlpha(0x5A3A3A, (int)(contentAlpha * 150.0f)), 6.0f);

        Fonts.BOLD.drawCentered("Delete World?", centerX, dialogY + 10.0f, 8.0f, this.withAlpha(0xFF8080, titleAlpha));
        Fonts.REGULARNEW.drawCentered("\"" + this.truncateText(this.deleteConfirmWorld, 140.0f, 5.0f) + "\"",
                centerX, dialogY + 24.0f, 5.0f, this.withAlpha(0xCCCCCC, titleAlpha));
        Fonts.REGULARNEW.drawCentered("This cannot be undone!", centerX, dialogY + 33.0f, 4.5f, this.withAlpha(0xFF6666, titleAlpha));

        // Confirm / Cancel buttons
        float btnWidth = 70.0f;
        float btnHeight = 16.0f;
        float btnY = dialogY + dialogHeight - btnHeight - 7.0f;
        float confirmX = centerX - btnWidth - 3.0f;
        float cancelX = centerX + 3.0f;

        boolean confirmHovered = this.isMouseOver(mouseX, mouseY, confirmX, btnY, btnWidth, btnHeight);
        boolean cancelHovered = this.isMouseOver(mouseX, mouseY, cancelX, btnY, btnWidth, btnHeight);

        this.renderDeleteButton(confirmX, btnY, btnWidth, btnHeight, contentAlpha, confirmHovered);
        this.renderActionButton(cancelX, btnY, btnWidth, btnHeight, "Cancel", contentAlpha, cancelHovered, false);
    }

    public float getMaxScroll(float listHeight) {
        float cardHeight = 36.0f;
        float cardGap = 4.0f;
        int count = this.getWorlds().size();
        float totalHeight = (float) count * (cardHeight + cardGap);
        return Math.max(0.0f, totalHeight - listHeight);
    }

    private String getGameModeString(LevelSummary world) {
        try {
            return world.getGameMode().getShortDisplayName().getString();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String formatDate(long timestamp) {
        try {
            if (timestamp <= 0) return "Unknown";
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String truncateText(String text, float maxWidth, float fontSize) {
        return this.truncateTextFont(text, maxWidth, fontSize, Fonts.TEST);
    }

    private String truncateTextFont(String text, float maxWidth, float fontSize, rich.util.render.font.Font font) {
        if (font.getWidth(text, fontSize) <= maxWidth) return text;
        while (text.length() > 3 && font.getWidth(text + "...", fontSize) > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

    public boolean isMouseOver(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | Mth.clamp((int) alpha, (int) 0, (int) 255) << 24;
    }
}
