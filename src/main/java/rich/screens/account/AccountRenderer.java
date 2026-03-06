
package rich.screens.account;

import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import rich.screens.account.AccountEntry;
import rich.screens.account.SkinManager;
import rich.util.ColorUtil;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class AccountRenderer {
    private static final float BLUR_RADIUS = 15.0f;
    private static final float OUTLINE_THICKNESS = 1.0f;

    public void renderLeftPanelTop(float x, float y, float width, float height, float contentAlpha, String nicknameText, boolean nicknameFieldFocused, float scaledMouseX, float scaledMouseY, long currentTime) {
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
        int[] bgColors = new int[]{bgTopLeft, bgTopRight, bgBottomRight, bgBottomLeft};
        Render2D.gradientRect(x, y, width, height, bgColors, 6.0f);
        int[] headerColors = new int[]{headerTopLeft, headerTopRight, headerBottomRight, headerBottomLeft};
        Render2D.gradientRect(x, y, width, 22.0f, headerColors, 6.0f, 6.0f, 0.0f, 0.0f);
        Render2D.outline(x, y, width, height, 1.0f, outlineColor, 6.0f);
        Fonts.BOLD.drawCentered("Account Panel", x + width / 2.0f - 15.0f, y + 7.0f, 8.0f, this.withAlpha(0xFFFFFF, titleAlpha));
        Fonts.REGULARNEW.draw("Nickname", x + 5.0f, y + 28.0f, 5.5f, this.withAlpha(0xFFFFFF, titleTextAlpha));
        float fieldX = x + 5.0f;
        float fieldY = y + 38.0f;
        float fieldHeight = 14.0f;
        float addButtonSize = 14.0f;
        float buttonGap = 3.0f;
        float fieldWidth = width - 10.0f - addButtonSize - buttonGap;
        this.renderNicknameField(fieldX, fieldY, fieldWidth, fieldHeight, contentAlpha, nicknameText, nicknameFieldFocused, currentTime);
        float addButtonX = fieldX + fieldWidth + buttonGap;
        boolean addButtonHovered = this.isMouseOver(scaledMouseX, scaledMouseY, addButtonX, fieldY, addButtonSize, addButtonSize);
        this.renderAddButton(addButtonX, fieldY, addButtonSize, contentAlpha, addButtonHovered, titleAlpha);
        float buttonWidth = width - 10.0f;
        float buttonHeight = 16.0f;
        float randomButtonX = x + 5.0f;
        float randomButtonY = fieldY + fieldHeight + 6.0f;
        boolean randomButtonHovered = this.isMouseOver(scaledMouseX, scaledMouseY, randomButtonX, randomButtonY, buttonWidth, buttonHeight);
        this.renderRandomButton(randomButtonX, randomButtonY, buttonWidth, buttonHeight, contentAlpha, randomButtonHovered, titleAlpha);
        float clearButtonX = x + 5.0f;
        float clearButtonY = randomButtonY + buttonHeight + 5.0f;
        boolean clearButtonHovered = this.isMouseOver(scaledMouseX, scaledMouseY, clearButtonX, clearButtonY, buttonWidth, buttonHeight);
        this.renderClearAllButton(clearButtonX, clearButtonY, buttonWidth, buttonHeight, contentAlpha, clearButtonHovered, titleAlpha);
    }

    private void renderNicknameField(float x, float y, float width, float height, float contentAlpha, String nicknameText, boolean focused, long currentTime) {
        int titleAlpha = (int)(contentAlpha * 255.0f);
        int titleTextAlpha = (int)(contentAlpha * 155.0f);
        int fieldBgAlpha = (int)(contentAlpha * 180.0f);
        int fieldOutlineAlpha = focused ? (int)(contentAlpha * 180.0f) : (int)(contentAlpha * 80.0f);
        int fieldBgTop = this.withAlpha(658448, fieldBgAlpha);
        int fieldBgBottom = this.withAlpha(526862, fieldBgAlpha);
        int[] fieldBgColors = new int[]{fieldBgTop, fieldBgTop, fieldBgBottom, fieldBgBottom};
        Render2D.gradientRect(x, y, width, height, fieldBgColors, 3.0f);
        int fieldOutlineColor = focused ? this.withAlpha(3820122, fieldOutlineAlpha) : this.withAlpha(2435638, fieldOutlineAlpha);
        Render2D.outline(x, y, width, height, 0.5f, fieldOutlineColor, 3.0f);
        String displayText = nicknameText.isEmpty() && !focused ? "Enter nick..." : nicknameText;
        int textColor = nicknameText.isEmpty() && !focused ? this.withAlpha(6318200, titleTextAlpha) : this.withAlpha(13685980, titleAlpha);
        Fonts.TEST.draw(displayText, x + 4.0f, y + 4.5f, 5.5f, textColor);
        if (focused && currentTime / 500L % 2L == 0L) {
            float cursorX = x + 4.0f + Fonts.TEST.getWidth(nicknameText, 5.5f);
            Render2D.rect(cursorX, y + 3.0f, 0.5f, height - 6.0f, this.withAlpha(13685980, titleAlpha), 0.0f);
        }
    }

    private void renderAddButton(float x, float y, float size, float contentAlpha, boolean hovered, int titleAlpha) {
        int btnAlpha = hovered ? (int)(contentAlpha * 180.0f) : (int)(contentAlpha * 140.0f);
        int btnTopLeft = this.withAlpha(1316639, btnAlpha);
        int btnTopRight = this.withAlpha(1579812, btnAlpha);
        int btnBottomLeft = this.withAlpha(1053466, btnAlpha);
        int btnBottomRight = this.withAlpha(1316639, btnAlpha);
        int[] btnColors = new int[]{btnTopLeft, btnTopRight, btnBottomRight, btnBottomLeft};
        Render2D.gradientRect(x, y, size, size, btnColors, 3.0f);
        Render2D.outline(x, y, size, size, 0.5f, this.withAlpha(2435638, (int)(contentAlpha * 100.0f)), 3.0f);
        float plusCenterX = x + size / 2.0f;
        float plusCenterY = y + size / 2.0f;
        float plusSize = 5.0f;
        float plusThickness = 1.2f;
        Render2D.rect(plusCenterX - plusSize / 2.0f, plusCenterY - plusThickness / 2.0f, plusSize, plusThickness, this.withAlpha(0xFFFFFF, titleAlpha), 0.5f);
        Render2D.rect(plusCenterX - plusThickness / 2.0f, plusCenterY - plusSize / 2.0f, plusThickness, plusSize, this.withAlpha(0xFFFFFF, titleAlpha), 0.5f);
    }

    private void renderRandomButton(float x, float y, float width, float height, float contentAlpha, boolean hovered, int titleAlpha) {
        int btnAlpha = hovered ? (int)(contentAlpha * 200.0f) : (int)(contentAlpha * 140.0f);
        int btnTopLeft = hovered ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int btnTopRight = hovered ? this.withAlpha(1975085, btnAlpha) : this.withAlpha(1579812, btnAlpha);
        int btnBottomLeft = hovered ? this.withAlpha(1316895, btnAlpha) : this.withAlpha(1053466, btnAlpha);
        int btnBottomRight = hovered ? this.withAlpha(1711912, btnAlpha) : this.withAlpha(1316639, btnAlpha);
        int[] btnColors = new int[]{btnTopLeft, btnTopRight, btnBottomRight, btnBottomLeft};
        Render2D.gradientRect(x, y, width, height, btnColors, 3.0f);
        int outlineColor = hovered ? this.withAlpha(3820122, (int)(contentAlpha * 150.0f)) : this.withAlpha(2435638, (int)(contentAlpha * 100.0f));
        Render2D.outline(x, y, width, height, 0.5f, outlineColor, 3.0f);
        int textColor = hovered ? this.withAlpha(0xFFFFFF, titleAlpha) : this.withAlpha(13687012, titleAlpha);
        Fonts.DEFAULT.draw("Random", x + 6.0f, y + 5.0f, 5.5f, textColor);
        Fonts.ICONS.draw("R", x + 75.0f, y + 3.5f, 10.0f, textColor);
    }

    private void renderClearAllButton(float x, float y, float width, float height, float contentAlpha, boolean hovered, int titleAlpha) {
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
        Fonts.DEFAULT.draw("Clear All", x + 6.0f, y + 5.0f, 5.5f, textColor);
        Fonts.GUI_ICONS.draw("O", x + 77.0f, y + 2.5f, 11.0f, textColor);
    }

    public void renderLeftPanelBottom(float x, float y, float width, float height, float contentAlpha, String activeAccountName, String activeAccountDate, Identifier activeAccountSkin) {
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
        Fonts.BOLD.drawCentered("Active Session", x + width / 2.0f - 15.0f, y + 6.0f, 8.0f, this.withAlpha(0xFFFFFF, titleAlpha));
        if (!activeAccountName.isEmpty()) {
            float faceX = x + 8.0f;
            float faceY = y + 28.0f;
            float faceSize = 24.0f;
            Identifier skinTexture = SkinManager.getSkin(activeAccountName);
            int faceColor = this.withAlpha(0xFFFFFF, titleAlpha);
            this.drawPlayerFace(skinTexture, faceX, faceY, faceSize, faceColor);
            float textX = faceX + faceSize + 6.0f;
            float nameY = faceY + 4.0f;
            float dateY = nameY + 10.0f;
            Fonts.TEST.draw(activeAccountName, textX, nameY, 6.0f, this.withAlpha(0xFFFFFF, titleAlpha));
            Fonts.TEST.draw(activeAccountDate, textX, dateY, 4.5f, this.withAlpha(0x808890, titleAlpha));
        } else {
            Fonts.REGULARNEW.drawCentered("No account selected", x + 50.0f, y + 36.0f, 5.0f, this.withAlpha(6318200, titleTextAlpha));
        }
    }

    public void renderRightPanel(float x, float y, float width, float height, float contentAlpha, List<AccountEntry> accounts, float scrollOffset, float scaledMouseX, float scaledMouseY, float scale, int guiScale) {
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
        Fonts.BOLD.draw("Accounts List", x + 8.0f, y + 7.0f, 8.0f, this.withAlpha(0xFFFFFF, titleAlpha));
        Render2D.blur(x, y, width, height, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 1));
        float accountListX = x + 5.0f;
        float accountListY = y + 28.0f;
        float accountListWidth = width - 10.0f;
        float accountListHeight = height - 31.0f;
        float cardWidth = (accountListWidth - 5.0f) / 2.0f;
        float cardHeight = 40.0f;
        float cardGap = 5.0f;
        float scissorScale = (float)guiScale / scale;
        Scissor.enable(accountListX * scale, accountListY * scale, accountListWidth * scale, accountListHeight * scale, scissorScale);
        for (int i = 0; i < accounts.size(); ++i) {
            AccountEntry account = accounts.get(i);
            int col = i % 2;
            int row = i / 2;
            float cardX = accountListX + (float)col * (cardWidth + cardGap);
            float cardY = accountListY + (float)row * (cardHeight + cardGap) - scrollOffset;
            if (cardY + cardHeight < accountListY - 10.0f || cardY > accountListY + accountListHeight + 10.0f) continue;
            this.renderAccountCard(cardX, cardY, cardWidth, cardHeight, account, contentAlpha, scaledMouseX, scaledMouseY, accountListY, accountListHeight);
        }
        Scissor.disable();
        if (accounts.isEmpty()) {
            Fonts.REGULARNEW.drawCentered("No accounts added", x + width / 2.0f, y + height / 2.0f + 2.0f, 6.0f, this.withAlpha(6318200, titleTextAlpha));
        }
    }

    private void renderAccountCard(float x, float y, float width, float height, AccountEntry account, float contentAlpha, float mouseX, float mouseY, float listY, float listHeight) {
        int pinOutlineColor;
        int pinBtnColor;
        int pinBtnAlpha;
        int titleAlpha = (int)(contentAlpha * 255.0f);
        boolean cardHovered = this.isMouseOver(mouseX, mouseY, x, y, width, height) && mouseY >= listY && mouseY <= listY + listHeight;
        int cardAlpha = cardHovered ? (int)(contentAlpha * 160.0f) : (int)(contentAlpha * 120.0f);
        int cardTopLeft = this.withAlpha(1185052, cardAlpha);
        int cardTopRight = this.withAlpha(1448482, cardAlpha);
        int cardBottomLeft = this.withAlpha(921622, cardAlpha);
        int cardBottomRight = this.withAlpha(1185052, cardAlpha);
        int[] cardColors = new int[]{cardTopLeft, cardTopRight, cardBottomRight, cardBottomLeft};
        Render2D.gradientRect(x, y, width, height, cardColors, 4.0f);
        Render2D.blur(x, y, 1.0f, 1.0f, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 0));
        int cardOutlineColor = this.withAlpha(2435638, (int)(contentAlpha * 80.0f));
        Render2D.outline(x, y, width, height, 0.5f, cardOutlineColor, 4.0f);
        float faceX = x + 7.0f;
        float faceY = y + 7.0f;
        float faceSize = 25.0f;
        Identifier skinTexture = SkinManager.getSkin(account.getName());
        this.drawPlayerFace(skinTexture, faceX, faceY, faceSize, this.withAlpha(0xFFFFFF, titleAlpha));
        float textX = faceX + faceSize + 5.0f;
        float nameY = faceY + 2.0f;
        float dateY = nameY + 9.0f;
        Object displayName = account.getName();
        float maxNameWidth = width - faceSize - 45.0f;
        if (Fonts.TEST.getWidth((String)displayName, 7.0f) > maxNameWidth) {
            while (Fonts.TEST.getWidth((String)displayName + "...", 7.0f) > maxNameWidth && ((String)displayName).length() > 3) {
                displayName = ((String)displayName).substring(0, ((String)displayName).length() - 1);
            }
            displayName = (String)displayName + "...";
        }
        Fonts.TEST.draw((String)displayName, textX, nameY, 7.0f, this.withAlpha(0xFFFFFF, titleAlpha));
        Fonts.TEST.draw(account.getDate(), textX, dateY, 6.0f, this.withAlpha(0x707888, titleAlpha));
        float buttonSize = 12.0f;
        float buttonYPos = y + height - buttonSize - 5.0f;
        float pinButtonX = x + width - buttonSize * 2.0f - 8.0f;
        float deleteButtonX = x + width - buttonSize - 5.0f;
        boolean pinHovered = this.isMouseOver(mouseX, mouseY, pinButtonX, buttonYPos, buttonSize, buttonSize) && mouseY >= listY && mouseY <= listY + listHeight;
        boolean deleteHovered = this.isMouseOver(mouseX, mouseY, deleteButtonX, buttonYPos, buttonSize, buttonSize) && mouseY >= listY && mouseY <= listY + listHeight;
        int n = pinBtnAlpha = pinHovered ? (int)(contentAlpha * 220.0f) : (int)(contentAlpha * 160.0f);
        if (account.isPinned()) {
            pinBtnColor = this.withAlpha(4864528, pinBtnAlpha);
            pinOutlineColor = this.withAlpha(13934615, (int)(contentAlpha * 180.0f));
        } else {
            pinBtnColor = this.withAlpha(1711396, pinBtnAlpha);
            pinOutlineColor = this.withAlpha(3488326, (int)(contentAlpha * 100.0f));
        }
        int[] pinBtnColors = new int[]{pinBtnColor, pinBtnColor, pinBtnColor, pinBtnColor};
        Render2D.gradientRect(pinButtonX, buttonYPos, buttonSize, buttonSize, pinBtnColors, 3.0f);
        Render2D.outline(pinButtonX, buttonYPos, buttonSize, buttonSize, 0.5f, pinOutlineColor, 3.0f);
        Render2D.blur(x, y, 1.0f, 1.0f, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 0));
        int pinIconColor = account.isPinned() ? this.withAlpha(16766720, titleAlpha) : this.withAlpha(12634324, titleAlpha);
        Fonts.MAINMENUSCREEN.drawCentered("c", pinButtonX + buttonSize / 2.0f, buttonYPos + 1.5f, 9.0f, pinIconColor);
        int delBtnAlpha = deleteHovered ? (int)(contentAlpha * 200.0f) : (int)(contentAlpha * 140.0f);
        int delBtnColor = deleteHovered ? this.withAlpha(0x5A2A2A, delBtnAlpha) : this.withAlpha(1711396, delBtnAlpha);
        int[] delBtnColors = new int[]{delBtnColor, delBtnColor, delBtnColor, delBtnColor};
        Render2D.gradientRect(deleteButtonX, buttonYPos, buttonSize, buttonSize, delBtnColors, 3.0f);
        Render2D.outline(deleteButtonX, buttonYPos, buttonSize, buttonSize, 0.5f, this.withAlpha(3488326, (int)(contentAlpha * 100.0f)), 3.0f);
        Render2D.blur(x, y, 1.0f, 1.0f, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 0));
        int delIconColor = deleteHovered ? this.withAlpha(0xFF8080, titleAlpha) : this.withAlpha(12634324, titleAlpha);
        Fonts.GUI_ICONS.drawCentered("O", deleteButtonX + buttonSize / 2.0f, buttonYPos + 0.5f, 11.0f, delIconColor);
    }

    public void drawPlayerFace(Identifier skin, float x, float y, float size, int color) {
        float u0 = 0.125f;
        float v0 = 0.125f;
        float u1 = 0.25f;
        float v1 = 0.25f;
        Render2D.texture(skin, x, y, size, size, u0, v0, u1, v1, color, 0.0f, 3.0f);
        float hatScale = 1.12f;
        float hatSize = size * hatScale;
        float hatOffset = (hatSize - size) / 2.0f;
        float hatU0 = 0.625f;
        float hatV0 = 0.125f;
        float hatU1 = 0.75f;
        float hatV1 = 0.25f;
        Render2D.texture(skin, x - hatOffset, y - hatOffset, hatSize, hatSize, hatU0, hatV0, hatU1, hatV1, color, 0.0f, 3.0f);
    }

    public boolean isMouseOver(float mouseX, float mouseY, float x, float y, float width, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public int withAlpha(int color, int alpha) {
        return color & 0xFFFFFF | Mth.clamp((int)alpha, (int)0, (int)255) << 24;
    }
}

