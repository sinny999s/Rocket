package rich.screens.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.EventLoopGroupHolder;
import net.minecraft.util.Mth;
import rich.util.ColorUtil;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class MultiplayerRenderer {

    private ServerList serverList;
    private final List<ServerData> servers = new ArrayList<>();
    private int selectedIndex = -1;
    private String directConnectIp = "";
    private boolean directConnectFieldFocused = false;
    private boolean showDirectConnect = false;
    private String deleteConfirmServer = null;
    private int deleteConfirmIndex = -1;
    private ServerStatusPinger pinger;
    private EventLoopGroupHolder eventLoopGroupHolder;
    private final Map<String, Identifier> iconCache = new HashMap<>();

    public void loadServers() {
        Minecraft mc = Minecraft.getInstance();
        this.serverList = new ServerList(mc);
        this.serverList.load();
        this.servers.clear();
        for (int i = 0; i < this.serverList.size(); i++) {
            this.servers.add(this.serverList.get(i));
        }
        if (!this.servers.isEmpty() && this.selectedIndex < 0) {
            this.selectedIndex = 0;
        }
        this.pingAllServers();
    }

    private void pingAllServers() {
        if (this.pinger != null) {
            try { this.pinger.removeAll(); } catch (Exception ignored) {}
        }
        this.pinger = new ServerStatusPinger();
        if (this.eventLoopGroupHolder == null) {
            this.eventLoopGroupHolder = EventLoopGroupHolder.remote(false);
        }
        for (ServerData server : this.servers) {
            server.players = null;
            server.motd = null;
            server.ping = -1;
            try {
                this.pinger.pingServer(server, () -> {}, () -> {}, this.eventLoopGroupHolder);
            } catch (Exception ignored) {}
        }
    }

    public void tick() {
        if (this.pinger != null) {
            try { this.pinger.tick(); } catch (Exception ignored) {}
        }
    }

    public void cleanup() {
        if (this.pinger != null) {
            try { this.pinger.removeAll(); } catch (Exception ignored) {}
            this.pinger = null;
        }
        if (this.eventLoopGroupHolder != null) {
            try { this.eventLoopGroupHolder.eventLoopGroup().shutdownGracefully(); } catch (Exception ignored) {}
            this.eventLoopGroupHolder = null;
        }
    }

    public void refreshServers() {
        this.selectedIndex = -1;
        this.iconCache.clear();
        this.loadServers();
    }

    public List<ServerData> getServers() {
        return this.servers;
    }

    public int getSelectedIndex() {
        return this.selectedIndex;
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = index;
    }

    public ServerData getSelectedServer() {
        if (this.selectedIndex >= 0 && this.selectedIndex < this.servers.size()) {
            return this.servers.get(this.selectedIndex);
        }
        return null;
    }

    public ServerList getServerList() {
        return this.serverList;
    }

    public boolean isShowDirectConnect() {
        return this.showDirectConnect;
    }

    public void setShowDirectConnect(boolean show) {
        this.showDirectConnect = show;
        if (show) {
            this.directConnectIp = "";
            this.directConnectFieldFocused = true;
        }
    }

    public String getDirectConnectIp() {
        return this.directConnectIp;
    }

    public void setDirectConnectIp(String ip) {
        this.directConnectIp = ip;
    }

    public boolean isDirectConnectFieldFocused() {
        return this.directConnectFieldFocused;
    }

    public void setDirectConnectFieldFocused(boolean focused) {
        this.directConnectFieldFocused = focused;
    }

    public String getDeleteConfirmServer() {
        return this.deleteConfirmServer;
    }

    public int getDeleteConfirmIndex() {
        return this.deleteConfirmIndex;
    }

    public void setDeleteConfirm(String name, int index) {
        this.deleteConfirmServer = name;
        this.deleteConfirmIndex = index;
    }

    public void clearDeleteConfirm() {
        this.deleteConfirmServer = null;
        this.deleteConfirmIndex = -1;
    }

    public void removeServer(int index) {
        if (index >= 0 && index < this.servers.size()) {
            this.servers.remove(index);
            this.serverList.remove(this.serverList.get(index));
            this.serverList.save();
            if (this.selectedIndex >= this.servers.size()) {
                this.selectedIndex = this.servers.size() - 1;
            }
        }
    }

    public void addServer(ServerData data) {
        this.serverList.add(data, false);
        this.serverList.save();
        this.servers.add(data);
    }

    public void renderLeftPanel(float x, float y, float width, float height, float contentAlpha, float mouseX, float mouseY, long currentTime) {
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
        Fonts.BOLD.drawCentered("Server Info", x + width / 2.0f - 15.0f, y + 6.0f, 8.0f, this.withAlpha(0xFFFFFF, titleAlpha));

        ServerData selected = this.getSelectedServer();
        if (selected != null) {
            float textX = x + 8.0f;
            float textY = y + 28.0f;

            Fonts.REGULARNEW.draw("Name", textX, textY, 5.0f, this.withAlpha(0x808890, titleAlpha));
            Fonts.TEST.draw(this.truncateText(selected.name, width - 16.0f, 6.0f), textX, textY + 8.0f, 6.0f, this.withAlpha(0xFFFFFF, titleAlpha));

            Fonts.REGULARNEW.draw("Address", textX, textY + 22.0f, 5.0f, this.withAlpha(0x808890, titleAlpha));
            Fonts.TEST.draw(this.truncateText(selected.ip, width - 16.0f, 5.5f), textX, textY + 30.0f, 5.5f, this.withAlpha(0xCCCCCC, titleAlpha));

            Fonts.REGULARNEW.draw("Status", textX, textY + 44.0f, 5.0f, this.withAlpha(0x808890, titleAlpha));
            String status = this.getServerStatus(selected);
            int statusColor = this.getServerStatusColor(selected, titleAlpha);
            Fonts.TEST.draw(status, textX, textY + 52.0f, 5.5f, statusColor);

            // Bottom buttons
            float fullBtnWidth = width - 10.0f;
            float btnHeight = 16.0f;

            // Join button
            float joinBtnY = y + height - btnHeight * 3 - 17.0f;
            boolean joinHovered = this.isMouseOver(mouseX, mouseY, x + 5.0f, joinBtnY, fullBtnWidth, btnHeight);
            this.renderActionButton(x + 5.0f, joinBtnY, fullBtnWidth, btnHeight, "Join Server", contentAlpha, joinHovered, false);

            // Direct Connect button
            float directBtnY = joinBtnY + btnHeight + 3.0f;
            boolean directHovered = this.isMouseOver(mouseX, mouseY, x + 5.0f, directBtnY, fullBtnWidth, btnHeight);
            this.renderActionButton(x + 5.0f, directBtnY, fullBtnWidth, btnHeight, "Direct Connect", contentAlpha, directHovered, false);

            // Refresh / Delete row
            float halfBtnWidth = (fullBtnWidth - 3.0f) / 2.0f;
            float rowBtnY = directBtnY + btnHeight + 3.0f;
            boolean refreshHovered = this.isMouseOver(mouseX, mouseY, x + 5.0f, rowBtnY, halfBtnWidth, btnHeight);
            this.renderActionButton(x + 5.0f, rowBtnY, halfBtnWidth, btnHeight, "Refresh", contentAlpha, refreshHovered, false);
            boolean delHovered = this.isMouseOver(mouseX, mouseY, x + 5.0f + halfBtnWidth + 3.0f, rowBtnY, halfBtnWidth, btnHeight);
            this.renderDeleteButton(x + 5.0f + halfBtnWidth + 3.0f, rowBtnY, halfBtnWidth, btnHeight, contentAlpha, delHovered);
        } else {
            Fonts.REGULARNEW.drawCentered("No server selected", x + width / 2.0f, y + 50.0f, 5.0f, this.withAlpha(6318200, titleTextAlpha));

            // Still show buttons when no server selected
            float fullBtnWidth = width - 10.0f;
            float btnHeight = 16.0f;

            float directBtnY = y + height - btnHeight * 2 - 10.0f;
            boolean directHovered = this.isMouseOver(mouseX, mouseY, x + 5.0f, directBtnY, fullBtnWidth, btnHeight);
            this.renderActionButton(x + 5.0f, directBtnY, fullBtnWidth, btnHeight, "Direct Connect", contentAlpha, directHovered, false);

            float refreshBtnY = directBtnY + btnHeight + 3.0f;
            boolean refreshHovered = this.isMouseOver(mouseX, mouseY, x + 5.0f, refreshBtnY, fullBtnWidth, btnHeight);
            this.renderActionButton(x + 5.0f, refreshBtnY, fullBtnWidth, btnHeight, "Refresh", contentAlpha, refreshHovered, false);
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
        Fonts.BOLD.draw("Servers", x + 8.0f, y + 7.0f, 8.0f, this.withAlpha(0xFFFFFF, titleAlpha));

        // Add Server button in header
        float addBtnWidth = 50.0f;
        float addBtnHeight = 14.0f;
        float addBtnX = x + width - addBtnWidth - 6.0f;
        float addBtnY = y + 4.0f;
        boolean addHovered = this.isMouseOver(scaledMouseX, scaledMouseY, addBtnX, addBtnY, addBtnWidth, addBtnHeight);
        this.renderActionButton(addBtnX, addBtnY, addBtnWidth, addBtnHeight, "Add Server", contentAlpha, addHovered, false);

        Render2D.blur(x, y, width, height, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 1));

        float listX = x + 5.0f;
        float listY = y + 28.0f;
        float listWidth = width - 10.0f;
        float listHeight = height - 33.0f;
        float cardHeight = 40.0f;
        float cardGap = 4.0f;
        float scissorScale = (float) guiScale / scale;

        Scissor.enable(listX * scale, listY * scale, listWidth * scale, listHeight * scale, scissorScale);

        if (this.servers.isEmpty()) {
            Fonts.REGULARNEW.drawCentered("No servers added", x + width / 2.0f, y + height / 2.0f + 2.0f, 6.0f, this.withAlpha(6318200, titleTextAlpha));
        } else {
            for (int i = 0; i < this.servers.size(); i++) {
                ServerData server = this.servers.get(i);
                float cardY = listY + (float) i * (cardHeight + cardGap) - scrollOffset;
                if (cardY + cardHeight < listY - 10.0f || cardY > listY + listHeight + 10.0f) continue;
                boolean isSelected = (i == this.selectedIndex);
                this.renderServerCard(listX, cardY, listWidth, cardHeight, server, contentAlpha,
                        scaledMouseX, scaledMouseY, listY, listHeight, isSelected);
            }
        }

        Scissor.disable();
    }

    private void renderServerCard(float x, float y, float width, float height, ServerData server,
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

        // Server icon
        float iconX = x + 6.0f;
        float iconY = y + 6.0f;
        float iconSize = height - 12.0f;
        Identifier iconTex = this.getOrUploadIcon(server);
        if (iconTex != null) {
            int iconColor = this.withAlpha(0xFFFFFF, titleAlpha);
            Render2D.texture(iconTex, iconX, iconY, iconSize, iconSize, 0.0f, 0.0f, 1.0f, 1.0f, iconColor, 1.0f, 3.0f);
        } else {
            int iconBg = this.withAlpha(0x1A2030, (int)(contentAlpha * 200.0f));
            Render2D.rect(iconX, iconY, iconSize, iconSize, iconBg, 3.0f);
            Render2D.outline(iconX, iconY, iconSize, iconSize, 0.5f, this.withAlpha(2435638, (int)(contentAlpha * 60.0f)), 3.0f);
            String initial = server.name.isEmpty() ? "S" : server.name.substring(0, 1).toUpperCase();
            Fonts.BOLD.drawCentered(initial, iconX + iconSize / 2.0f, iconY + iconSize / 2.0f - 4.0f, 12.0f, this.withAlpha(0x607080, titleAlpha));
        }

        float textX = iconX + iconSize + 6.0f;
        float nameY = y + 7.0f;
        float motdY = nameY + 10.0f;
        float infoY = motdY + 9.0f;

        // Server name
        String displayName = this.truncateText(server.name, width - iconSize - 70.0f, 6.5f);
        Fonts.TEST.draw(displayName, textX, nameY, 6.5f, this.withAlpha(0xFFFFFF, titleAlpha));

        // MOTD (first line)
        String motd = "";
        if (server.motd != null) {
            motd = server.motd.getString();
            if (motd.length() > 60) motd = motd.substring(0, 60) + "...";
        }
        if (!motd.isEmpty()) {
            Fonts.TEST.draw(this.truncateText(motd, width - iconSize - 24.0f, 4.5f), textX, motdY, 4.5f, this.withAlpha(0x808890, titleAlpha));
        }

        // Address
        Fonts.TEST.draw(this.truncateText(server.ip, width - iconSize - 24.0f, 4.0f), textX, infoY, 4.0f, this.withAlpha(0x606870, titleAlpha));

        // Player count on right side
        float rightX = x + width - 6.0f;
        String playerCount = this.getPlayerCount(server);
        float pcWidth = Fonts.TEST.getWidth(playerCount, 5.0f);
        Fonts.TEST.draw(playerCount, rightX - pcWidth, nameY + 1.0f, 5.0f, this.withAlpha(0xAAAAAA, titleAlpha));
    }

    public void renderDirectConnectDialog(float centerX, float centerY, float contentAlpha, float mouseX, float mouseY, long currentTime) {
        if (!this.showDirectConnect) return;

        float dialogWidth = 200.0f;
        float dialogHeight = 80.0f;
        float dialogX = centerX - dialogWidth / 2.0f;
        float dialogY = centerY - dialogHeight / 2.0f;

        int titleAlpha = (int)(contentAlpha * 255.0f);
        int titleTextAlpha = (int)(contentAlpha * 155.0f);

        Render2D.rect(0, 0, Render2D.getFixedScaledWidth(), Render2D.getFixedScaledHeight(), this.withAlpha(0x000000, (int)(contentAlpha * 120.0f)));

        int bgAlpha = (int)(contentAlpha * 200.0f);
        int bgColor = this.withAlpha(0x0D1117, bgAlpha);
        Render2D.rect(dialogX, dialogY, dialogWidth, dialogHeight, bgColor, 6.0f);
        Render2D.outline(dialogX, dialogY, dialogWidth, dialogHeight, 1.0f, this.withAlpha(2435638, (int)(contentAlpha * 150.0f)), 6.0f);

        Fonts.BOLD.drawCentered("Direct Connect", centerX, dialogY + 10.0f, 8.0f, this.withAlpha(0xFFFFFF, titleAlpha));

        // IP field
        float fieldX = dialogX + 10.0f;
        float fieldY = dialogY + 28.0f;
        float fieldWidth = dialogWidth - 20.0f;
        float fieldHeight = 16.0f;

        this.renderTextField(fieldX, fieldY, fieldWidth, fieldHeight, contentAlpha,
                this.directConnectIp, this.directConnectFieldFocused, "Enter server IP...", currentTime);

        // Connect / Cancel
        float btnWidth = 80.0f;
        float btnHeight = 16.0f;
        float btnY = dialogY + dialogHeight - btnHeight - 8.0f;
        float connectX = centerX - btnWidth - 3.0f;
        float cancelX = centerX + 3.0f;

        boolean connectHovered = this.isMouseOver(mouseX, mouseY, connectX, btnY, btnWidth, btnHeight);
        boolean cancelHovered = this.isMouseOver(mouseX, mouseY, cancelX, btnY, btnWidth, btnHeight);

        this.renderActionButton(connectX, btnY, btnWidth, btnHeight, "Connect", contentAlpha, connectHovered, this.directConnectIp.isEmpty());
        this.renderActionButton(cancelX, btnY, btnWidth, btnHeight, "Cancel", contentAlpha, cancelHovered, false);
    }

    public void renderDeleteConfirmDialog(float centerX, float centerY, float contentAlpha, float mouseX, float mouseY) {
        if (this.deleteConfirmServer == null) return;

        float dialogWidth = 180.0f;
        float dialogHeight = 70.0f;
        float dialogX = centerX - dialogWidth / 2.0f;
        float dialogY = centerY - dialogHeight / 2.0f;

        int titleAlpha = (int)(contentAlpha * 255.0f);

        Render2D.rect(0, 0, Render2D.getFixedScaledWidth(), Render2D.getFixedScaledHeight(), this.withAlpha(0x000000, (int)(contentAlpha * 120.0f)));

        int bgAlpha = (int)(contentAlpha * 200.0f);
        int bgColor = this.withAlpha(0x0D1117, bgAlpha);
        Render2D.rect(dialogX, dialogY, dialogWidth, dialogHeight, bgColor, 6.0f);
        Render2D.outline(dialogX, dialogY, dialogWidth, dialogHeight, 1.0f, this.withAlpha(0x5A3A3A, (int)(contentAlpha * 150.0f)), 6.0f);

        Fonts.BOLD.drawCentered("Remove Server?", centerX, dialogY + 10.0f, 8.0f, this.withAlpha(0xFF8080, titleAlpha));
        Fonts.REGULARNEW.drawCentered("\"" + this.truncateText(this.deleteConfirmServer, 140.0f, 5.0f) + "\"",
                centerX, dialogY + 24.0f, 5.0f, this.withAlpha(0xCCCCCC, titleAlpha));

        float btnWidth = 70.0f;
        float btnHeight = 16.0f;
        float btnY = dialogY + dialogHeight - btnHeight - 7.0f;
        float confirmX = centerX - btnWidth - 3.0f;
        float cancelX = centerX + 3.0f;

        boolean confirmHovered = this.isMouseOver(mouseX, mouseY, confirmX, btnY, btnWidth, btnHeight);
        boolean cancelHovered = this.isMouseOver(mouseX, mouseY, cancelX, btnY, btnWidth, btnHeight);

        this.renderDeleteBtn(confirmX, btnY, btnWidth, btnHeight, contentAlpha, confirmHovered);
        this.renderActionButton(cancelX, btnY, btnWidth, btnHeight, "Cancel", contentAlpha, cancelHovered, false);
    }

    private void renderTextField(float x, float y, float width, float height, float contentAlpha,
                                  String text, boolean focused, String placeholder, long currentTime) {
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
        String displayText = text.isEmpty() && !focused ? placeholder : text;
        int textColor = text.isEmpty() && !focused ? this.withAlpha(6318200, titleTextAlpha) : this.withAlpha(13685980, titleAlpha);
        Fonts.TEST.draw(displayText, x + 4.0f, y + 4.5f, 5.5f, textColor);
        if (focused && currentTime / 500L % 2L == 0L) {
            float cursorX = x + 4.0f + Fonts.TEST.getWidth(text, 5.5f);
            Render2D.rect(cursorX, y + 3.0f, 0.5f, height - 6.0f, this.withAlpha(13685980, titleAlpha), 0.0f);
        }
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

    private void renderDeleteBtn(float x, float y, float width, float height, float contentAlpha, boolean hovered) {
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

    private void renderDeleteButton(float x, float y, float width, float height, float contentAlpha, boolean hovered) {
        this.renderDeleteBtn(x, y, width, height, contentAlpha, hovered);
    }

    private String getPlayerCount(ServerData server) {
        if (server.players != null) {
            return server.players.online() + "/" + server.players.max();
        }
        return "?/?";
    }

    private String getServerStatus(ServerData server) {
        if (server.players != null) {
            return server.players.online() + "/" + server.players.max() + " players";
        }
        return "Pinging...";
    }

    private int getServerStatusColor(ServerData server, int titleAlpha) {
        if (server.players == null) {
            return this.withAlpha(0xAAAA00, titleAlpha);
        }
        return this.withAlpha(0x55FF55, titleAlpha);
    }

    private Identifier getOrUploadIcon(ServerData server) {
        String key = server.ip;
        if (this.iconCache.containsKey(key)) {
            return this.iconCache.get(key);
        }
        byte[] iconBytes = server.getIconBytes();
        if (iconBytes == null || iconBytes.length == 0) {
            return null;
        }
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(iconBytes);
            NativeImage image = NativeImage.read(bais);
            DynamicTexture texture = new DynamicTexture(() -> "server_icon_" + key, image);
            Identifier id = Identifier.fromNamespaceAndPath("rich", "server_icon/" + key.replace(":", "_").replace("/", "_").replace(".", "_").toLowerCase());
            Minecraft.getInstance().getTextureManager().register(id, texture);
            this.iconCache.put(key, id);
            return id;
        } catch (Exception e) {
            this.iconCache.put(key, null);
            return null;
        }
    }

    public float getMaxScroll(float listHeight) {
        float cardHeight = 40.0f;
        float cardGap = 4.0f;
        int count = this.servers.size();
        float totalHeight = (float) count * (cardHeight + cardGap);
        return Math.max(0.0f, totalHeight - listHeight);
    }

    private String truncateText(String text, float maxWidth, float fontSize) {
        if (text == null) return "";
        if (Fonts.TEST.getWidth(text, fontSize) <= maxWidth) return text;
        while (text.length() > 3 && Fonts.TEST.getWidth(text + "...", fontSize) > maxWidth) {
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
