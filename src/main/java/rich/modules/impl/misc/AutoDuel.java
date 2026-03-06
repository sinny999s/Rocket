/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.authlib.GameProfile
 */
package rich.modules.impl.misc;

import antidaunleak.api.annotation.Native;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import java.lang.invoke.LambdaMetafactory;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import rich.events.api.EventHandler;
import rich.events.impl.PacketEvent;
import rich.events.impl.TickEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.modules.module.setting.implement.TextSetting;
import rich.util.timer.TimerUtil;

public class AutoDuel
extends ModuleStructure {
    private final Pattern pattern = Pattern.compile("^\\w{3,16}$");
    private final SelectSetting mode = new SelectSetting("Mode", "Duel mode").value("Spheres", "Shield", "Thorns 3", "Netherite", "Cheater Paradise", "Bow", "Classic", "Totems", "No Debuff").selected("Spheres");
    private final SliderSettings slowTime = new SliderSettings("Send speed", "Delay between requests").setValue(500.0f).range(300.0f, 1000.0f);
    private final BooleanSetting babki = new BooleanSetting("Play for money", "Coin bet").setValue(false);
    private final TextSetting money = new TextSetting("Coins", "Coin count for bet").setText("10000").visible(() -> this.babki.isValue());
    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private final List<String> sent = Lists.newArrayList();
    private final TimerUtil counter = TimerUtil.create();
    private final TimerUtil counter2 = TimerUtil.create();
    private final TimerUtil counterChoice = TimerUtil.create();
    private final TimerUtil counterTo = TimerUtil.create();

    public AutoDuel() {
        super("AutoDuel", "Auto Duel", ModuleCategory.MISC);
        this.settings(this.mode, this.slowTime, this.babki, this.money);
    }

    @Override
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void activate() {
        this.counter.resetCounter();
        this.counter2.resetCounter();
        this.counterChoice.resetCounter();
        this.counterTo.resetCounter();
        this.sent.clear();
        super.activate();
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginUltra)
    public void onTick(TickEvent event) {
        if (AutoDuel.mc.player == null || AutoDuel.mc.level == null) {
            return;
        }
        this.handleDuelLogic();
        this.handleScreenInteraction();
    }

    @EventHandler
    @Native(type=Native.Type.VMProtectBeginMutation)
    public void onPacket(PacketEvent event) {
        ClientboundSystemChatPacket chat;
        String text;
        Packet<?> packet;
        if (event.getType() == PacketEvent.Type.RECEIVE && (packet = event.getPacket()) instanceof ClientboundSystemChatPacket && ((text = (chat = (ClientboundSystemChatPacket)packet).content().getString()).contains("start") && text.contains("through") && text.contains("seconds!") || text.contains("duel") && text.contains("prohibited"))) {
            this.setState(false);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private void handleDuelLogic() {
        List<String> players = this.getOnlinePlayers();
        double distance = Math.sqrt(Math.pow(this.lastPosX - AutoDuel.mc.player.getX(), 2.0) + Math.pow(this.lastPosY - AutoDuel.mc.player.getY(), 2.0) + Math.pow(this.lastPosZ - AutoDuel.mc.player.getZ(), 2.0));
        if (distance > 500.0) {
            this.setState(false);
            return;
        }
        this.lastPosX = AutoDuel.mc.player.getX();
        this.lastPosY = AutoDuel.mc.player.getY();
        this.lastPosZ = AutoDuel.mc.player.getZ();
        if (this.counter2.hasTimeElapsed(800L * (long)players.size())) {
            this.sent.clear();
            this.counter2.resetCounter();
        }
        for (String player : players) {
            if (this.sent.contains(player) || player.equals(AutoDuel.mc.player.getGameProfile().name()) || !this.counter.hasTimeElapsed((long)this.slowTime.getValue())) continue;
            this.sendDuelRequest(player);
            this.sent.add(player);
            this.counter.resetCounter();
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void sendDuelRequest(String player) {
        if (this.babki.isValue()) {
            AutoDuel.mc.player.connection.sendCommand("duel " + player + " " + this.money.getText());
        } else {
            AutoDuel.mc.player.connection.sendCommand("duel " + player);
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private void handleScreenInteraction() {
        AbstractContainerMenu screenHandler;
        if (AutoDuel.mc.screen != null && (screenHandler = AutoDuel.mc.player.containerMenu) instanceof AbstractContainerMenu) {
            AbstractContainerMenu chest = screenHandler;
            String title = AutoDuel.mc.screen.getTitle().getString();
            if (title.contains("Set selection (1/1)")) {
                if (this.counterChoice.hasTimeElapsed(150L)) {
                    int slotID = this.getKitSlot();
                    if (slotID >= 0) {
                        AutoDuel.mc.gameMode.handleInventoryMouseClick(AutoDuel.mc.player.containerMenu.containerId, slotID, 0, ClickType.QUICK_MOVE, AutoDuel.mc.player);
                    }
                    this.counterChoice.resetCounter();
                }
            } else if (title.contains("Duel settings") && this.counterTo.hasTimeElapsed(150L)) {
                AutoDuel.mc.gameMode.handleInventoryMouseClick(chest.containerId, 0, 0, ClickType.QUICK_MOVE, AutoDuel.mc.player);
                this.counterTo.resetCounter();
            }
        }
    }

    @Native(type=Native.Type.VMProtectBeginMutation)
    private int getKitSlot() {
        return switch (this.mode.getSelected()) {
            case "Shield" -> 0;
            case "Thorns 3" -> 1;
            case "Bow" -> 2;
            case "Totems" -> 3;
            case "No Debuff" -> 4;
            case "Spheres" -> 5;
            case "Classic" -> 6;
            case "Cheater Paradise" -> 7;
            case "Netherite" -> 8;
            default -> -1;
        };
    }

    private List<String> getOnlinePlayers() {
        return AutoDuel.mc.player.connection.getOnlinePlayers().stream().map(PlayerInfo::getProfile).map(GameProfile::name).filter(profileName -> this.pattern.matcher((CharSequence)profileName).matches()).collect(Collectors.toList());
    }
}

