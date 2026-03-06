/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.StringUtils
 */
package rich.util.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;
import rich.util.config.impl.proxy.ProxyConfig;
import rich.util.proxy.Proxy;

public class GuiProxy
extends Screen {
    private boolean isSocks4 = false;
    private EditBox ipPort;
    private EditBox username;
    private EditBox password;
    private Checkbox enabledCheck;
    private Screen parentScreen;
    private String msg = "";
    private int[] positionY;
    private int positionX;
    private static String text_proxy = Component.translatable((String)"PROXY").getString();

    public GuiProxy(Screen parentScreen) {
        super(Component.literal((String)text_proxy));
        this.parentScreen = parentScreen;
    }

    private static boolean isValidIpPort(String ipP) {
        if (ipP == null || ipP.isEmpty()) {
            return false;
        }
        String[] split = ipP.split(":");
        if (split.length > 1) {
            if (!StringUtils.isNumeric((CharSequence)split[1])) {
                return false;
            }
            try {
                int port = Integer.parseInt(split[1]);
                return port >= 0 && port <= 65535;
            }
            catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    private boolean checkProxy() {
        if (!GuiProxy.isValidIpPort(this.ipPort.getValue())) {
            this.ipPort.setFocused(true);
            return false;
        }
        return true;
    }

    private void centerButtons(int amount, int buttonLength, int gap) {
        this.positionX = this.width / 2 - buttonLength / 2;
        this.positionY = new int[amount];
        int center = (this.height + amount * gap) / 2;
        int buttonStarts = center - amount * gap;
        for (int i = 0; i != amount; ++i) {
            this.positionY[i] = buttonStarts + gap * i;
        }
    }

    public boolean keyPressed(KeyEvent input) {
        if (input.isEscape()) {
            Minecraft.getInstance().setScreen(this.parentScreen);
            return true;
        }
        super.keyPressed(input);
        this.msg = "";
        return true;
    }

    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        super.render(context, mouseX, mouseY, partialTicks);
        if (this.enabledCheck.selected() && !GuiProxy.isValidIpPort(this.ipPort.getValue())) {
            this.enabledCheck.onPress(null);
        }
        context.drawString(this.font, Component.translatable((String)"Enter IP address and port. Example below").getString(), this.width / 2 - 106, this.positionY[3] - 15, 0xA0A0A0);
        context.drawString(this.font, Component.translatable((String)"IP:Port \u25b8").getString(), this.width / 2 - 140, this.positionY[3] + 15, 0xA0A0A0);
        this.ipPort.render(context, mouseX, mouseY, partialTicks);
        context.drawString(this.font, Component.translatable((String)"Username \u25b8").getString(), this.width / 2 - 131, this.positionY[4] + 15, 0xA0A0A0);
        context.drawString(this.font, Component.translatable((String)"Password \u25b8").getString(), this.width / 2 - 126, this.positionY[5] + 15, 0xA0A0A0);
        this.username.render(context, mouseX, mouseY, partialTicks);
        this.password.render(context, mouseX, mouseY, partialTicks);
        context.drawCenteredString(this.font, this.msg, this.width / 2, this.positionY[6] + 5, 0xA0A0A0);
    }

    public void init() {
        int buttonLength = 160;
        this.centerButtons(10, buttonLength, 26);
        ProxyConfig config = ProxyConfig.getInstance();
        Proxy currentProxy = config.getDefaultProxy();
        this.isSocks4 = currentProxy.type == Proxy.ProxyType.SOCKS4;
        this.ipPort = new EditBox(this.font, this.positionX, this.positionY[3] + 10, buttonLength, 20, Component.literal((String)""));
        this.ipPort.setValue(currentProxy.ipPort);
        this.ipPort.setMaxLength(1024);
        this.ipPort.setFocused(true);
        this.addWidget(this.ipPort);
        this.username = new EditBox(this.font, this.positionX, this.positionY[4] + 10, buttonLength, 20, Component.literal((String)""));
        this.username.setMaxLength(255);
        this.username.setValue(currentProxy.username);
        this.addWidget(this.username);
        this.password = new EditBox(this.font, this.positionX, this.positionY[5] + 10, buttonLength, 20, Component.literal((String)""));
        this.password.setMaxLength(255);
        this.password.setValue(currentProxy.password);
        this.addWidget(this.password);
        int posXButtons = this.width / 2 - buttonLength / 2 * 3 / 2;
        Button apply = Button.builder((Component)Component.translatable((String)"Apply"), button -> {
            ProxyConfig cfg = ProxyConfig.getInstance();
            if (this.enabledCheck.selected()) {
                if (this.checkProxy()) {
                    Proxy newProxy = new Proxy(this.isSocks4, this.ipPort.getValue(), this.username.getValue(), this.password.getValue());
                    cfg.setDefaultProxy(newProxy);
                    cfg.setProxyEnabled(true);
                    cfg.save();
                    Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
                }
            } else {
                Proxy newProxy = new Proxy(this.isSocks4, this.ipPort.getValue(), this.username.getValue(), this.password.getValue());
                cfg.setDefaultProxy(newProxy);
                cfg.setProxyEnabled(false);
                cfg.save();
                Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(new TitleScreen()));
            }
        }).bounds(posXButtons + (buttonLength / 2 - 62) * 2, this.positionY[7] - 10, buttonLength / 2 + 3, 20).build();
        this.addRenderableWidget(apply);
        Checkbox.Builder checkboxBuilder = Checkbox.builder((Component)Component.translatable((String)"Enable Proxy"), (Font)this.font);
        checkboxBuilder.pos(this.width / 2 - 34 - (13 + this.font.width(Component.translatable((String)"Enable Proxy"))) / 2, this.positionY[7] + 15);
        if (config.isProxyEnabled()) {
            checkboxBuilder.selected(true);
        }
        this.enabledCheck = checkboxBuilder.build();
        this.addRenderableWidget(this.enabledCheck);
        Button cancel = Button.builder((Component)Component.translatable((String)"Cancel"), button -> Minecraft.getInstance().setScreen(this.parentScreen)).bounds(posXButtons + (buttonLength / 2 - 16) * 2, this.positionY[7] - 10, buttonLength / 2 - 3, 20).build();
        this.addRenderableWidget(cancel);
    }

    public void onClose() {
        this.msg = "";
        Minecraft.getInstance().setScreen(this.parentScreen);
    }
}

