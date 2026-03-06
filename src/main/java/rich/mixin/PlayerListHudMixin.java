/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package rich.mixin;

import com.mojang.authlib.GameProfile;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={PlayerTabOverlay.class})
public class PlayerListHudMixin {
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");

    @Inject(method={"getPlayerInfos"}, at={@At(value="RETURN")}, cancellable=true)
    private void addVanishedEntries(CallbackInfoReturnable<List<PlayerInfo>> cir) {
        Minecraft client = Minecraft.getInstance();
        List originalList = (List)cir.getReturnValue();
        ArrayList<PlayerInfo> vanishedList = new ArrayList<PlayerInfo>();
        Scoreboard scoreboard = client.level.getScoreboard();
        ArrayList<PlayerTeam> teams = new ArrayList<PlayerTeam>(scoreboard.getPlayerTeams());
        teams.sort(Comparator.comparing(PlayerTeam::getName));
        Collection<PlayerInfo> online = client.player.connection.getOnlinePlayers();
        for (PlayerTeam team : teams) {
            boolean present;
            String name;
            Collection members = team.getPlayers();
            if (members.size() != 1 || !NAME_PATTERN.matcher(name = (String)members.iterator().next()).matches() || (present = online.stream().anyMatch(e -> e.getProfile() != null && name.equals(e.getProfile().name())))) continue;
            MutableComponent displayName = Component.empty().append(Component.literal((String)"[").withStyle(ChatFormatting.GRAY)).append(Component.literal((String)"V").withStyle(ChatFormatting.RED)).append(Component.literal((String)"] ").withStyle(ChatFormatting.GRAY)).append(team.getPlayerPrefix()).append(Component.literal((String)name).withStyle(ChatFormatting.GRAY));
            GameProfile fakeProfile = new GameProfile(UUID.randomUUID(), name);
            PlayerInfo fake = new PlayerInfo(fakeProfile, client.isLocalServer());
            fake.setTabListDisplayName(displayName);
            fake.setTabListOrder(Integer.MIN_VALUE);
            vanishedList.add(fake);
        }
        ArrayList<PlayerInfo> finalList = new ArrayList<PlayerInfo>();
        finalList.addAll(vanishedList);
        finalList.addAll(originalList);
        cir.setReturnValue(finalList);
    }
}

